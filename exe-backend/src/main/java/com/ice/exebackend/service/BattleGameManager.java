package com.ice.exebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.dto.BattleMessage;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizStudent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 确保引入 QueryWrapper
import java.util.concurrent.ThreadLocalRandom; // 用于生成随机延迟和概率
import com.alibaba.fastjson.JSON; // 或者使用你项目里的 Jackson/Fastjson 解析选项




import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Component
public class BattleGameManager {

    @Autowired
    private BizQuestionService questionService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BizStudentService studentService;

    // 线程池：用于倒计时和匹配轮询
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    // 常量定义
    private static final int ROUND_TIMEOUT_SECONDS = 20; // 每题限时
    private static final int ROUND_RESULT_VIEW_TIME = 3; // 结果展示时间

    // 段位常量
    private static final String TIER_BRONZE = "BRONZE"; // < 200
    private static final String TIER_SILVER = "SILVER"; // 200 - 500
    private static final String TIER_GOLD = "GOLD";     // > 500

    // --- 新增：ELO 匹配相关结构 ---

    // 内部类：封装等待的玩家信息
    private static class WaitingPlayer {
        WebSocketSession session;
        BizStudent student;
        String tier;
        long joinTime;

        public WaitingPlayer(WebSocketSession session, BizStudent student, String tier) {
            this.session = session;
            this.student = student;
            this.tier = tier;
            this.joinTime = System.currentTimeMillis();
        }
    }

    // 分段位的等待队列 (Key: 段位名, Value: 等待玩家列表)
    private final Map<String, CopyOnWriteArrayList<WaitingPlayer>> tierQueues = new ConcurrentHashMap<>();

    // 快速查找映射：SessionID -> WaitingPlayer (用于处理离开)
    private final Map<String, WaitingPlayer> sessionWaitingMap = new ConcurrentHashMap<>();

    // 正在进行的房间映射
    private final Map<String, String> playerRoomMap = new ConcurrentHashMap<>();
    private final Map<String, BattleRoom> rooms = new ConcurrentHashMap<>();

    // 初始化
    @PostConstruct
    public void init() {
        // 初始化各段位队列
        tierQueues.put(TIER_BRONZE, new CopyOnWriteArrayList<>());
        tierQueues.put(TIER_SILVER, new CopyOnWriteArrayList<>());
        tierQueues.put(TIER_GOLD, new CopyOnWriteArrayList<>());

        // 启动匹配扫描任务：每 1 秒执行一次
        scheduler.scheduleAtFixedRate(this::processMatchMaking, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 用户申请匹配（进入对应段位的队列）
     */
    public void joinQueue(WebSocketSession session) {
        if (sessionWaitingMap.containsKey(session.getId()) || playerRoomMap.containsKey(session.getId())) {
            return; // 已经在队列或游戏中
        }

        // 1. 获取用户信息和积分
        String studentNo = (String) session.getAttributes().get("username");
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        // 判空保护
        int points = (student != null && student.getPoints() != null) ? student.getPoints() : 0;

        // 2. 计算段位
        String tier = calculateTier(points);

        // 3. 加入队列
        WaitingPlayer player = new WaitingPlayer(session, student, tier);
        tierQueues.get(tier).add(player);
        sessionWaitingMap.put(session.getId(), player);

        System.out.println("玩家加入匹配队列: " + studentNo + " 段位: " + tier + " 积分: " + points);
    }

    /**
     * 核心逻辑：定时扫描匹配
     */
    private void processMatchMaking() {

        long now = System.currentTimeMillis(); // 【核心修复】添加这一行定义 now 变量
        // 遍历所有段位的队列
        for (String tier : tierQueues.keySet()) {
            List<WaitingPlayer> queue = tierQueues.get(tier);

            // 遍历该队列中的玩家（作为匹配发起者）
            // 使用迭代器防止并发修改异常，CopyOnWriteArrayList 支持安全遍历
            for (WaitingPlayer p1 : queue) {
                // 如果玩家已经匹配成功（可能在别的线程被移除了），跳过
                if (!sessionWaitingMap.containsKey(p1.session.getId())) continue;


                // 【新增】检查超时：如果等待超过 10 秒，分配机器人
                if (now - p1.joinTime > 10000) {
                    createBotMatch(p1);
                    continue; // 处理下一个
                }

                // 计算等待时间
                long waitTime = System.currentTimeMillis() - p1.joinTime;

                // 确定搜索范围
                List<String> targetTiers = new ArrayList<>();
                targetTiers.add(p1.tier); // 首先搜同段位

                if (waitTime > 5000) {
                    // 等待 > 5秒，扩大到相邻段位
                    addAdjacentTiers(targetTiers, p1.tier);
                }
                if (waitTime > 10000) {
                    // 等待 > 10秒，全服匹配
                    targetTiers.add(TIER_BRONZE);
                    targetTiers.add(TIER_SILVER);
                    targetTiers.add(TIER_GOLD);
                }

                // 尝试寻找对手
                WaitingPlayer opponent = findOpponent(p1, targetTiers);

                if (opponent != null) {
                    // 找到对手，创建房间
                    createRoom(p1, opponent);
                }
            }
        }
    }

    /**
     * 在指定段位列表中寻找对手
     */
    private WaitingPlayer findOpponent(WaitingPlayer p1, List<String> targetTiers) {
        for (String targetTier : targetTiers) {
            List<WaitingPlayer> targetQueue = tierQueues.get(targetTier);
            if (targetQueue == null) continue;

            for (WaitingPlayer p2 : targetQueue) {
                // 不能匹配自己
                if (p2.session.getId().equals(p1.session.getId())) continue;

                // 确保对手也还在等待中
                if (sessionWaitingMap.containsKey(p2.session.getId())) {
                    return p2;
                }
            }
        }
        return null;
    }

    /**
     * 辅助：计算段位
     */
    private String calculateTier(int points) {
        if (points >= 500) return TIER_GOLD;
        if (points >= 200) return TIER_SILVER;
        return TIER_BRONZE;
    }

    /**
     * 辅助：添加相邻段位
     */
    private void addAdjacentTiers(List<String> tiers, String currentTier) {
        if (TIER_BRONZE.equals(currentTier)) {
            tiers.add(TIER_SILVER);
        } else if (TIER_SILVER.equals(currentTier)) {
            tiers.add(TIER_BRONZE);
            tiers.add(TIER_GOLD);
        } else if (TIER_GOLD.equals(currentTier)) {
            tiers.add(TIER_SILVER);
        }
    }

    /**
     * 创建房间并开始游戏 (加锁保证原子性，防止两人同时被别人匹配)
     */
    private synchronized void createRoom(WaitingPlayer wp1, WaitingPlayer wp2) {
        // 双重检查：确保两人都还在等待映射中
        if (!sessionWaitingMap.containsKey(wp1.session.getId()) ||
                !sessionWaitingMap.containsKey(wp2.session.getId())) {
            return;
        }

        // 1. 从队列移除
        removeFromQueue(wp1);
        removeFromQueue(wp2);

        WebSocketSession p1 = wp1.session;
        WebSocketSession p2 = wp2.session;

        // 2. 创建房间逻辑 (复用原有逻辑，稍作修改使用已查询到的 student 信息)
        String roomId = UUID.randomUUID().toString();

        // 随机抽题
        List<BizQuestion> questions = questionService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BizQuestion>()
                        .eq("question_type", 1)
                        .last("ORDER BY RAND() LIMIT 5")
        );

        BattleRoom room = new BattleRoom(roomId, p1, p2, questions);
        rooms.put(roomId, room);
        playerRoomMap.put(p1.getId(), roomId);
        playerRoomMap.put(p2.getId(), roomId);

        // 3. 发送匹配成功消息 (使用 WaitingPlayer 中缓存的 student 信息，避免重复查库)
        sendMatchSuccess(p1, wp2.student);
        sendMatchSuccess(p2, wp1.student);

        // 4. 延迟发题
        scheduler.schedule(() -> {
            sendQuestion(room);
            startRoundTimer(room);
        }, 1, TimeUnit.SECONDS);
    }

    /**
     * 从队列和映射中移除玩家
     */
    private void removeFromQueue(WaitingPlayer wp) {
        sessionWaitingMap.remove(wp.session.getId());
        List<WaitingPlayer> queue = tierQueues.get(wp.tier);
        if (queue != null) {
            queue.remove(wp);
        }
    }

    /**
     * 用户取消匹配或断开
     */
    public void leave(WebSocketSession session) {
        // 1. 尝试从等待队列移除
        WaitingPlayer wp = sessionWaitingMap.get(session.getId());
        if (wp != null) {
            removeFromQueue(wp);
            return; // 如果在等待队列中，移除后直接返回
        }

        // 2. 如果在游戏中，处理游戏退出逻辑
        String roomId = playerRoomMap.remove(session.getId());
        if (roomId != null) {
            BattleRoom room = rooms.get(roomId);
            if (room != null) {
                synchronized (room) {
                    if (room.timeoutTask != null) room.timeoutTask.cancel(true);

                    WebSocketSession opponent = room.p1.equals(session) ? room.p2 : room.p1;
                    // 如果对手存在（不是机器人），才通知他
                    if (opponent != null) {
                        sendMessage(opponent, BattleMessage.of("OPPONENT_LEFT", null));
                        playerRoomMap.remove(opponent.getId());
                    }
                }
            }
        }
    }

    // --- 以下为游戏进行中的逻辑 (保持原有逻辑基本不变，适配新结构) ---

    private void sendMatchSuccess(WebSocketSession session, BizStudent opponent) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "匹配成功");
        data.put("opponent", buildStudentInfoMap(opponent));
        sendMessage(session, BattleMessage.of("MATCH_SUCCESS", data));
    }

    public void handleAnswer(WebSocketSession session, String answerStr) {
        String roomId = playerRoomMap.get(session.getId());
        if (roomId == null) return;

        BattleRoom room = rooms.get(roomId);
        if (room == null) return;

        synchronized (room) {
            room.submitAnswer(session, answerStr);
            if (room.isRoundComplete()) {
                if (room.timeoutTask != null) room.timeoutTask.cancel(false);
                processRoundTransition(room);
            }
        }
    }

    private void processRoundTransition(BattleRoom room) {
        sendRoundResult(room);
        scheduler.schedule(() -> {
            synchronized (room) {
                if (rooms.get(room.roomId) == null) return;
                if (room.hasNextQuestion()) {
                    room.nextRound();
                    sendQuestion(room);
                    startRoundTimer(room);
                } else {
                    sendGameOver(room);
                }
            }
        }, ROUND_RESULT_VIEW_TIME, TimeUnit.SECONDS);
    }

    private void sendQuestion(BattleRoom room) {
        BizQuestion q = room.getCurrentQuestion();
        Map<String, Object> qData = new HashMap<>();
        qData.put("content", q.getContent());
        qData.put("options", q.getOptions());
        qData.put("round", room.currentRoundIndex + 1);
        qData.put("total", room.questions.size());

        BattleMessage msg = BattleMessage.of("QUESTION", qData);
        sendMessage(room.p1, msg);
        sendMessage(room.p2, msg);

// 【新增】如果是机器人局，安排机器人答题
        if (room.isBotGame) {
            scheduleBotAnswer(room);
        }

    }
    /**
     * 模拟机器人答题行为
     */
    private void scheduleBotAnswer(BattleRoom room) {
        // 随机生成思考时间：3 ~ 15 秒
        int delay = ThreadLocalRandom.current().nextInt(3, 15);

        scheduler.schedule(() -> {
            // 检查房间是否还在 (可能玩家已断开)
            if (!rooms.containsKey(room.roomId)) return;

            synchronized (room) {
                BizQuestion q = room.getCurrentQuestion();
                String botAnswer;

                // 设定机器人智商：60% 概率答对 (可根据段位动态调整)
                boolean willBeCorrect = ThreadLocalRandom.current().nextDouble() < 0.6;

                if (willBeCorrect) {
                    botAnswer = q.getAnswer();
                } else {
                    // 答错：随机选一个非正确答案 (这里简化处理，随机选 A/B/C/D)
                    // 更严谨的做法是解析 options JSON，从中选一个错误的 key
                    String[] options = {"A", "B", "C", "D"};
                    do {
                        botAnswer = options[ThreadLocalRandom.current().nextInt(4)];
                    } while (botAnswer.equalsIgnoreCase(q.getAnswer()) && options.length > 1);
                }

                // 提交答案
                room.p2Answer = botAnswer;

                // 检查回合是否结束 (有可能玩家已经答完了)
                if (room.isRoundComplete()) {
                    if (room.timeoutTask != null) room.timeoutTask.cancel(false);
                    processRoundTransition(room);
                }
            }
        }, delay, TimeUnit.SECONDS);
    }

    private void startRoundTimer(BattleRoom room) {
        if (room.timeoutTask != null && !room.timeoutTask.isDone()) {
            room.timeoutTask.cancel(false);
        }
        int roundAtStart = room.currentRoundIndex;
        room.timeoutTask = scheduler.schedule(() -> {
            synchronized (room) {
                if (room.currentRoundIndex != roundAtStart) return;
                processRoundTransition(room);
            }
        }, ROUND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void sendRoundResult(BattleRoom room) {
        BizQuestion q = room.getCurrentQuestion();
        boolean p1Correct = room.p1Answer != null && room.p1Answer.equalsIgnoreCase(q.getAnswer());
        boolean p2Correct = room.p2Answer != null && room.p2Answer.equalsIgnoreCase(q.getAnswer());

        if (p1Correct) room.p1Score += 20;
        if (p2Correct) room.p2Score += 20;

        Map<String, Object> baseData = new HashMap<>();
        baseData.put("correctAnswer", q.getAnswer());

        // 发给 P1
        Map<String, Object> dataP1 = new HashMap<>(baseData);
        dataP1.put("myAnswer", room.p1Answer);
        dataP1.put("oppAnswer", room.p2Answer);
        dataP1.put("myScore", room.p1Score);
        dataP1.put("oppScore", room.p2Score);
        dataP1.put("isCorrect", p1Correct);
        sendMessage(room.p1, BattleMessage.of("ROUND_RESULT", dataP1));

        // 发给 P2
        Map<String, Object> dataP2 = new HashMap<>(baseData);
        dataP2.put("myAnswer", room.p2Answer);
        dataP2.put("oppAnswer", room.p1Answer);
        dataP2.put("myScore", room.p2Score);
        dataP2.put("oppScore", room.p1Score);
        dataP2.put("isCorrect", p2Correct);
        sendMessage(room.p2, BattleMessage.of("ROUND_RESULT", dataP2));
    }

    /**
     * 发送游戏结束消息并结算积分
     */
    private void sendGameOver(BattleRoom room) {
        // 1. 判定胜负
        String resultP1, resultP2;
        if (room.p1Score > room.p2Score) {
            resultP1 = "YOU";
            resultP2 = "OPPONENT";
        } else if (room.p1Score < room.p2Score) {
            resultP1 = "OPPONENT";
            resultP2 = "YOU";
        } else {
            resultP1 = "DRAW";
            resultP2 = "DRAW";
        }

        // 2. 【核心修复】更新数据库积分
        updatePlayerPoints(room.p1, resultP1);
        updatePlayerPoints(room.p2, resultP2);

        // 3. 发送消息给 P1
        Map<String, Object> res1 = new HashMap<>();
        res1.put("result", resultP1);
        res1.put("myScore", room.p1Score);
        res1.put("oppScore", room.p2Score);
        sendMessage(room.p1, BattleMessage.of("GAME_OVER", res1));

        // 4. 发送消息给 P2
        Map<String, Object> res2 = new HashMap<>();
        res2.put("result", resultP2);
        res2.put("myScore", room.p2Score);
        res2.put("oppScore", room.p1Score);
        sendMessage(room.p2, BattleMessage.of("GAME_OVER", res2));

        // 5. 清理房间
        rooms.remove(room.roomId);
        playerRoomMap.remove(room.p1.getId());
        playerRoomMap.remove(room.p2.getId());
    }


    /**
     * 【新增】更新玩家积分到数据库
     * @param session 玩家的 WebSocket 会话
     * @param result 比赛结果 (YOU:胜, OPPONENT:负, DRAW:平)
     */
    private void updatePlayerPoints(WebSocketSession session, String result) {
        if (session == null) return; // 机器人不更新积分
        try {
            // 从 Session 获取学号 (在握手拦截器或 joinQueue 时放入的)
            String studentNo = (String) session.getAttributes().get("username");
            if (studentNo == null) return;


            // 查询当前学生信息
            BizStudent student = studentService.getOne(
                    new QueryWrapper<BizStudent>().eq("student_no", studentNo)
            );

            if (student != null) {
                int currentPoints = student.getPoints() != null ? student.getPoints() : 0;
                int delta = 0;

                // 积分规则
                switch (result) {
                    case "YOU":
                        delta = 20; // 胜利 +20
                        break;
                    case "DRAW":
                        delta = 5;  // 平局 +5
                        break;
                    case "OPPONENT":
                        delta = -10; // 失败 -10
                        break;
                }

                int newPoints = currentPoints + delta;
                if (newPoints < 0) newPoints = 0; // 积分保底为0

                student.setPoints(newPoints);
                studentService.updateById(student);

                System.out.println("更新积分: " + studentNo + " " + currentPoints + " -> " + newPoints + " (" + result + ")");
            }
        } catch (Exception e) {
            System.err.println("更新积分失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Object> buildStudentInfoMap(BizStudent s) {
        Map<String, Object> info = new HashMap<>();
        if (s != null) {
            info.put("name", s.getName());
            info.put("avatar", s.getAvatar());
            info.put("avatarFrameStyle", s.getAvatarFrameStyle());
            info.put("points", s.getPoints()); // 可以展示对手的段位分
        } else {
            info.put("name", "神秘对手");
            info.put("avatar", null);
        }
        return info;
    }

    private void sendMessage(WebSocketSession session, BattleMessage msg) {
        // 【关键】如果是机器人(session为null)，直接忽略，不发送消息
        if (session == null) return;

        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 内部房间类
    private static class BattleRoom {
        String roomId;
        WebSocketSession p1;
        WebSocketSession p2; // 机器人局时，此字段为 null
        List<BizQuestion> questions;
        int currentRoundIndex = 0;
        String p1Answer, p2Answer;
        int p1Score = 0, p2Score = 0;
        ScheduledFuture<?> timeoutTask;

        // 【新增】标记是否为机器人局
        boolean isBotGame = false;

        public BattleRoom(String roomId, WebSocketSession p1, WebSocketSession p2, List<BizQuestion> questions) {
            this.roomId = roomId;
            this.p1 = p1;
            this.p2 = p2;
            this.questions = questions;
        }

        public BizQuestion getCurrentQuestion() { return questions.get(currentRoundIndex); }

        public void submitAnswer(WebSocketSession s, String a) {
            if (s.equals(p1)) p1Answer = a;
            // 如果 p2 存在且是发送者 (真人对战情况)
            if (p2 != null && s.equals(p2)) p2Answer = a;
        }

        public boolean isRoundComplete() { return p1Answer != null && p2Answer != null; }
        public boolean hasNextQuestion() { return currentRoundIndex < questions.size() - 1; }
        public void nextRound() { currentRoundIndex++; p1Answer = null; p2Answer = null; }
    }
    /**
     * 创建人机对战
     */
    private synchronized void createBotMatch(WaitingPlayer wp1) {
        // 双重检查
        if (!sessionWaitingMap.containsKey(wp1.session.getId())) return;

        // 1. 移除队列
        removeFromQueue(wp1);
        WebSocketSession p1 = wp1.session;

        // 2. 创建虚拟机器人信息
        BizStudent botStudent = new BizStudent();
        botStudent.setId(-1L); // 负数ID代表机器人
        botStudent.setName("AI 智能助教"); // 或者随机名字
        // 设置一个默认头像
        botStudent.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
        botStudent.setPoints(new Random().nextInt(200) + 500); // 随机积分

        // 3. 创建房间
        String roomId = UUID.randomUUID().toString();
        List<BizQuestion> questions = questionService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BizQuestion>()
                        .eq("question_type", 1)
                        .last("ORDER BY RAND() LIMIT 5")
        );

        // p2 传入 null
        BattleRoom room = new BattleRoom(roomId, p1, null, questions);
        room.isBotGame = true; // 标记为机器人局

        rooms.put(roomId, room);
        playerRoomMap.put(p1.getId(), roomId);

        // 4. 发送匹配成功 (只发给 P1)
        sendMatchSuccess(p1, botStudent);

        // 5. 延迟开始
        scheduler.schedule(() -> {
            sendQuestion(room);
            startRoundTimer(room);
        }, 1, TimeUnit.SECONDS);

        System.out.println("创建机器人对局: " + wp1.student.getName() + " vs AI");
    }
}