package com.ice.exebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.dto.BattleMessage;
import com.ice.exebackend.dto.BattleRoomData;
import com.ice.exebackend.entity.BizBattleRecord;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizStudent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
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
    @Autowired
    private BizBattleRecordService battleRecordService;

    @Autowired
    private StringRedisTemplate redisTemplate; // 注入 Redis

    // 线程池：用于倒计时和匹配轮询
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    // 常量定义
    private static final int ROUND_TIMEOUT_SECONDS = 20; // 每题限时
    private static final int ROUND_RESULT_VIEW_TIME = 3; // 结果展示时间
    private static final int BASE_SCORE = 10; // 基础分

    // 段位常量
    public static final String TIER_IRON = "IRON";
    public static final String TIER_BRONZE = "BRONZE";
    public static final String TIER_SILVER = "SILVER";
    public static final String TIER_GOLD = "GOLD";
    public static final String TIER_PLATINUM = "PLATINUM";
    public static final String TIER_EMERALD = "EMERALD";
    public static final String TIER_DIAMOND = "DIAMOND";
    public static final String TIER_MASTER = "MASTER";
    public static final String TIER_GRANDMASTER = "GRANDMASTER";
    public static final String TIER_CHALLENGER = "CHALLENGER";


    private static final String CHANNEL_NAME = "battle:channel";
    private static final String QUEUE_PREFIX = "battle:queue:";
    private static final String ROOM_PREFIX = "battle:room:";

    private static final List<String> ORDERED_TIERS = Arrays.asList(
            TIER_IRON, TIER_BRONZE, TIER_SILVER, TIER_GOLD,
            TIER_PLATINUM, TIER_EMERALD, TIER_DIAMOND,
            TIER_MASTER, TIER_GRANDMASTER, TIER_CHALLENGER
    );

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

    // 分段位的等待队列
    private final Map<String, CopyOnWriteArrayList<WaitingPlayer>> tierQueues = new ConcurrentHashMap<>();
    // SessionID -> WaitingPlayer
    private final Map<String, WaitingPlayer> sessionWaitingMap = new ConcurrentHashMap<>();
    // 正在进行的房间映射
    private final Map<String, String> playerRoomMap = new ConcurrentHashMap<>();
    private final Map<String, BattleRoom> rooms = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (String tier : ORDERED_TIERS) {
            tierQueues.put(tier, new CopyOnWriteArrayList<>());
        }
        scheduler.scheduleAtFixedRate(this::processMatchMaking, 1, 1, TimeUnit.SECONDS);
    }

    private BizStudent getStudentBySession(WebSocketSession session) {
        if (session == null) return null;
        String studentNo = (String) session.getAttributes().get("username");
        if (studentNo == null) return null;
        return studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
    }

    /**
     * 1. 用户加入匹配队列 (替换原 joinQueue)
     */
    public void joinQueue(WebSocketSession session) {
        String studentNo = (String) session.getAttributes().get("username");
        String userId = String.valueOf(getStudentId(session)); // 假设有个辅助方法获取ID

        // 1.1 把 Session 存入本地 Map，供 Subscriber 使用
        BattleMessageSubscriber.LOCAL_SESSION_MAP.put(userId, session);

        // 1.2 计算段位
        String tier = calculateTier(getPoints(studentNo));

        // 1.3 存入 Redis 队列 (仅存 UserId，不存 Session 对象)
        // 使用 List: LPUSH
        redisTemplate.opsForList().leftPush(QUEUE_PREFIX + tier, userId);
    }

    /**
     * 核心轮询匹配逻辑 (之前代码中缺失)
     */
    private void processMatchMaking() {
        // 简单匹配逻辑：遍历每个段位的队列，尝试两两匹配
        for (String tier : ORDERED_TIERS) {
            // 这里演示基于 Redis 队列的匹配逻辑 (配合 joinQueue 使用)
            String p1Id = redisTemplate.opsForList().rightPop(QUEUE_PREFIX + tier);
            if (p1Id != null) {
                // 尝试取第二个
                String p2Id = redisTemplate.opsForList().rightPop(QUEUE_PREFIX + tier);
                if (p2Id != null) {
                    // 匹配成功 -> 创建分布式房间
                    createRoomDistributed(p1Id, p2Id);
                } else {
                    // 没有对手，放回队列头部等待 (或者安排机器人)
                    redisTemplate.opsForList().rightPush(QUEUE_PREFIX + tier, p1Id);

                    // TODO: 这里可以判断等待时间，如果太久则触发 createBotMatch
                }
            }
        }
    }
    /**
     * 3. 创建房间并广播
     */
    private void createRoomDistributed(String user1Id, String user2Id) {
        String roomId = UUID.randomUUID().toString();

        // 3.1 初始化房间数据
        BattleRoomData roomData = new BattleRoomData(); // 创建一个纯数据POJO，不含Session
        roomData.setRoomId(roomId);
        roomData.setP1Id(user1Id);
        roomData.setP2Id(user2Id);
        // ... 加载题目等 ...

        // 3.2 保存房间状态到 Redis
        String roomJson = toJson(roomData);
        redisTemplate.opsForValue().set(ROOM_PREFIX + roomId, roomJson, 10, TimeUnit.MINUTES);

        // 3.3 记录玩家当前所在的房间ID (用于重连或答题时查找)
        redisTemplate.opsForValue().set("player:room:" + user1Id, roomId, 10, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("player:room:" + user2Id, roomId, 10, TimeUnit.MINUTES);

        // 3.4 广播“匹配成功”消息
        // 我们不知道 user1 和 user2 连在哪个服务器，所以广播给所有服务器
        broadcastMessage(user1Id, BattleMessage.of("MATCH_SUCCESS", buildUserInfo(user2Id)));
        broadcastMessage(user2Id, BattleMessage.of("MATCH_SUCCESS", buildUserInfo(user1Id)));

        // 3.5 启动倒计时 (略：可以使用 Redis 的过期监听或延迟队列实现)
    }

    /**
     * 辅助：广播消息到 Redis
     */
    private void broadcastMessage(String targetUserId, BattleMessage msg) {
        Map<String, Object> pubMsg = new HashMap<>();
        pubMsg.put("targetUserId", targetUserId);
        pubMsg.put("payload", msg);

        String json = toJson(pubMsg);
        redisTemplate.convertAndSend(CHANNEL_NAME, json);
    }

    /**
     * 获取用户ID
     */
    private Long getStudentId(WebSocketSession session) {
        BizStudent student = getStudentBySession(session);
        return student != null ? student.getId() : null;
    }

    /**
     * 获取用户积分
     */
    private int getPoints(String studentNo) {
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return (student != null && student.getPoints() != null) ? student.getPoints() : 0;
    }

    /**
     * 对象转 JSON 字符串
     */
    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * 构造用户信息 (用于发送给前端)
     */
    private Map<String, Object> buildUserInfo(String userId) {
        if ("-1".equals(userId)) {
            // 机器人信息
            Map<String, Object> bot = new HashMap<>();
            bot.put("name", "AI 智能助教");
            bot.put("avatar", "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
            return bot;
        }
        BizStudent s = studentService.getById(userId);
        return buildStudentInfoMap(s);
    }
    private WaitingPlayer findOpponent(WaitingPlayer p1, List<String> targetTiers) {
        for (String targetTier : targetTiers) {
            List<WaitingPlayer> targetQueue = tierQueues.get(targetTier);
            if (targetQueue == null) continue;
            for (WaitingPlayer p2 : targetQueue) {
                if (p2.session.getId().equals(p1.session.getId())) continue;
                if (sessionWaitingMap.containsKey(p2.session.getId())) {
                    return p2;
                }
            }
        }
        return null;
    }

    public static String calculateTier(int points) {
        if (points < 100) return TIER_IRON;
        if (points < 200) return TIER_BRONZE;
        if (points < 300) return TIER_SILVER;
        if (points < 400) return TIER_GOLD;
        if (points < 500) return TIER_PLATINUM;
        if (points < 600) return TIER_EMERALD;
        if (points < 800) return TIER_DIAMOND;
        if (points < 1000) return TIER_MASTER;
        if (points < 1200) return TIER_GRANDMASTER;
        return TIER_CHALLENGER;
    }

    public static String getTierNameCN(String tier) {
        switch (tier) {
            case TIER_IRON: return "坚韧黑铁";
            case TIER_BRONZE: return "英勇黄铜";
            case TIER_SILVER: return "不屈白银";
            case TIER_GOLD: return "荣耀黄金";
            case TIER_PLATINUM: return "华贵铂金";
            case TIER_EMERALD: return "流光翡翠";
            case TIER_DIAMOND: return "璀璨钻石";
            case TIER_MASTER: return "超凡大师";
            case TIER_GRANDMASTER: return "傲世宗师";
            case TIER_CHALLENGER: return "最强王者";
            default: return "未定级";
        }
    }

    private void addAdjacentTiers(List<String> tiers, String currentTier, int range) {
        int index = ORDERED_TIERS.indexOf(currentTier);
        if (index == -1) return;
        for (int i = 1; i <= range; i++) {
            if (index - i >= 0) tiers.add(ORDERED_TIERS.get(index - i));
        }
        for (int i = 1; i <= range; i++) {
            if (index + i < ORDERED_TIERS.size()) tiers.add(ORDERED_TIERS.get(index + i));
        }
    }

    private synchronized void createRoom(WaitingPlayer wp1, WaitingPlayer wp2) {
        if (!sessionWaitingMap.containsKey(wp1.session.getId()) ||
                !sessionWaitingMap.containsKey(wp2.session.getId())) {
            return;
        }

        removeFromQueue(wp1);
        removeFromQueue(wp2);

        WebSocketSession p1 = wp1.session;
        WebSocketSession p2 = wp2.session;
        String roomId = UUID.randomUUID().toString();

        List<BizQuestion> questions = questionService.list(
                new QueryWrapper<BizQuestion>()
                        .eq("question_type", 1)
                        .last("ORDER BY RAND() LIMIT 5")
        );

        BattleRoom room = new BattleRoom(roomId, p1, p2, questions);
        rooms.put(roomId, room);
        playerRoomMap.put(p1.getId(), roomId);
        playerRoomMap.put(p2.getId(), roomId);

        sendMatchSuccess(p1, wp2.student);
        sendMatchSuccess(p2, wp1.student);

        scheduler.schedule(() -> {
            sendQuestion(room);
            startRoundTimer(room);
        }, 1, TimeUnit.SECONDS);
    }

    private void removeFromQueue(WaitingPlayer wp) {
        sessionWaitingMap.remove(wp.session.getId());
        List<WaitingPlayer> queue = tierQueues.get(wp.tier);
        if (queue != null) {
            queue.remove(wp);
        }
    }

    public void leave(WebSocketSession session) {
        WaitingPlayer wp = sessionWaitingMap.get(session.getId());
        if (wp != null) {
            removeFromQueue(wp);
            return;
        }

        String roomId = playerRoomMap.remove(session.getId());
        if (roomId != null) {
            BattleRoom room = rooms.get(roomId);
            if (room != null) {
                synchronized (room) {
                    if (room.timeoutTask != null) room.timeoutTask.cancel(true);
                    WebSocketSession opponent = room.p1.equals(session) ? room.p2 : room.p1;
                    if (opponent != null) {
                        sendMessage(opponent, BattleMessage.of("OPPONENT_LEFT", null));
                        playerRoomMap.remove(opponent.getId());
                    }
                }
            }
        }
    }

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

    public void handleItemUsage(WebSocketSession session, String itemType) {
        String roomId = playerRoomMap.get(session.getId());
        if (roomId == null) return;
        BattleRoom room = rooms.get(roomId);
        if (room == null) return;

        synchronized (room) {
            boolean isP1 = session.equals(room.p1);
            Map<String, Integer> myItems = isP1 ? room.p1Items : room.p2Items;
            WebSocketSession opponent = isP1 ? room.p2 : room.p1;

            if (myItems.getOrDefault(itemType, 0) > 0) {
                myItems.put(itemType, myItems.get(itemType) - 1);
                if ("FOG".equals(itemType)) {
                    sendMessage(opponent, BattleMessage.of("ITEM_EFFECT", Map.of("effect", "FOG", "duration", 3000)));
                } else if ("HINT".equals(itemType)) {
                    BizQuestion q = room.getCurrentQuestion();
                    String wrongOption = findOneWrongOption(q);
                    sendMessage(session, BattleMessage.of("ITEM_EFFECT", Map.of("effect", "HINT", "wrongOption", wrongOption)));
                }
                sendMessage(session, BattleMessage.of("ITEM_USED_SUCCESS", itemType));
            }
        }
    }

    private String findOneWrongOption(BizQuestion q) {
        String correct = q.getAnswer();
        List<String> candidates = new ArrayList<>();
        for (String opt : Arrays.asList("A", "B", "C", "D")) {
            if (!opt.equalsIgnoreCase(correct)) {
                candidates.add(opt);
            }
        }
        if (candidates.isEmpty()) return "";
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
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

        if (room.isBotGame) {
            scheduleBotAnswer(room);
        }
    }

    /**
     * 【核心优化】模拟机器人答题（拟人化）
     */
    private void scheduleBotAnswer(BattleRoom room) {
        BizQuestion q = room.getCurrentQuestion();

        // 1. 计算拟人化延迟 (基于题目长度 + 随机思考)
        // 假设阅读速度：每字 50ms (人类平均阅读速度)
        int contentLength = q.getContent() != null ? q.getContent().length() : 10;
        long readTime = contentLength * 50L;

        // 思考时间：1 ~ 5秒随机 (模拟反应时间)
        long thinkTime = ThreadLocalRandom.current().nextLong(1000, 5000);

        long totalDelayMs = readTime + thinkTime;

        // 限制范围：最小 2秒，最大 (总时间 - 2秒) 以免超时未答
        long maxDelay = (ROUND_TIMEOUT_SECONDS - 2) * 1000L;
        if (totalDelayMs > maxDelay) totalDelayMs = maxDelay;
        if (totalDelayMs < 2000) totalDelayMs = 2000;

        scheduler.schedule(() -> {
            // 检查房间是否还存在
            if (!rooms.containsKey(room.roomId)) return;

            synchronized (room) {
                // 记录机器人答题时间 (用于结算时的速度分计算)
                room.p2AnswerTime = System.currentTimeMillis();

                // 2. 计算拟人化正确率 (基于题目难度)
                // 获取难度，默认为 0.5 (0.1 简单 ~ 1.0 困难)
                // 难度越高，机器人答对概率越低
                double difficulty = (q.getDifficulty() != null) ? q.getDifficulty() : 0.5;

                // 基础正确率公式：P = 0.95 - (难度 * 0.6)
                // 例：难度0.1 -> 0.89, 难度0.5 -> 0.65, 难度0.9 -> 0.41
                double winRate = 0.95 - (difficulty * 0.6);

                // 增加随机波动 (-0.05 ~ +0.05)，模拟状态起伏
                winRate += (ThreadLocalRandom.current().nextDouble() * 0.1 - 0.05);

                // 判定是否答对
                boolean willBeCorrect = ThreadLocalRandom.current().nextDouble() < winRate;

                String botAnswer;
                if (willBeCorrect) {
                    botAnswer = q.getAnswer();
                } else {
                    // 智能生成错误答案：从非正确选项中随机选一个
                    String[] allOptions = {"A", "B", "C", "D"};
                    List<String> wrongOptions = new ArrayList<>();
                    for(String opt : allOptions) {
                        if(!opt.equalsIgnoreCase(q.getAnswer())) {
                            wrongOptions.add(opt);
                        }
                    }

                    if (!wrongOptions.isEmpty()) {
                        botAnswer = wrongOptions.get(ThreadLocalRandom.current().nextInt(wrongOptions.size()));
                    } else {
                        // 容错：如果没找到错误选项（比如数据异常），随机选一个
                        botAnswer = allOptions[ThreadLocalRandom.current().nextInt(4)];
                    }
                }
                room.p2Answer = botAnswer;

                // 如果双方都答完了，触发结算
                if (room.isRoundComplete()) {
                    if (room.timeoutTask != null) room.timeoutTask.cancel(false);
                    processRoundTransition(room);
                }
            }
        }, totalDelayMs, TimeUnit.MILLISECONDS);
    }

    private void startRoundTimer(BattleRoom room) {
        if (room.timeoutTask != null && !room.timeoutTask.isDone()) {
            room.timeoutTask.cancel(false);
        }
        room.roundStartTime = System.currentTimeMillis();
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
        boolean p1Correct = isCorrect(room.p1Answer, q.getAnswer());
        boolean p2Correct = isCorrect(room.p2Answer, q.getAnswer());

        int p1RoundScore = 0;
        int p2RoundScore = 0;

        if (p1Correct) {
            room.p1Combo++;
            long timeUsedMs = room.p1AnswerTime - room.roundStartTime;
            double timeBonus = Math.max(0, (ROUND_TIMEOUT_SECONDS * 1000 - timeUsedMs) / 1000.0 * 0.5);
            int comboBonus = Math.min(room.p1Combo * 2, 10);
            p1RoundScore = (int) (BASE_SCORE + timeBonus + comboBonus);
        } else {
            room.p1Combo = 0;
        }

        if (p2Correct) {
            room.p2Combo++;
            long timeUsedMs = room.p2AnswerTime - room.roundStartTime;
            double timeBonus = Math.max(0, (ROUND_TIMEOUT_SECONDS * 1000 - timeUsedMs) / 1000.0 * 0.5);
            int comboBonus = Math.min(room.p2Combo * 2, 10);
            p2RoundScore = (int) (BASE_SCORE + timeBonus + comboBonus);
        } else {
            room.p2Combo = 0;
        }

        room.p1Score += p1RoundScore;
        room.p2Score += p2RoundScore;

        Map<String, Object> baseData = new HashMap<>();
        baseData.put("correctAnswer", q.getAnswer());

        sendResultToPlayer(room.p1, baseData, room.p1Answer, room.p2Answer, room.p1Score, room.p2Score, p1Correct, p1RoundScore, room.p1Combo);
        sendResultToPlayer(room.p2, baseData, room.p2Answer, room.p1Answer, room.p2Score, room.p1Score, p2Correct, p2RoundScore, room.p2Combo);
    }

    private void sendResultToPlayer(WebSocketSession session, Map<String, Object> base, String myAns, String oppAns, int myScore, int oppScore, boolean isCorrect, int roundScore, int combo) {
        Map<String, Object> data = new HashMap<>(base);
        data.put("myAnswer", myAns);
        data.put("oppAnswer", oppAns);
        data.put("myScore", myScore);
        data.put("oppScore", oppScore);
        data.put("isCorrect", isCorrect);
        data.put("scoreChange", roundScore);
        data.put("combo", combo);
        sendMessage(session, BattleMessage.of("ROUND_RESULT", data));
    }

    private boolean isCorrect(String userAns, String correctAns) {
        return userAns != null && userAns.equalsIgnoreCase(correctAns);
    }

    private void sendGameOver(BattleRoom room) {
        String resultP1, resultP2;
        String dbResultP1, dbResultP2;
        int scoreP1, scoreP2;

        if (room.p1Score > room.p2Score) {
            resultP1 = "YOU";      dbResultP1 = "WIN";  scoreP1 = 20;
            resultP2 = "OPPONENT"; dbResultP2 = "LOSE"; scoreP2 = -10;
        } else if (room.p1Score < room.p2Score) {
            resultP1 = "OPPONENT"; dbResultP1 = "LOSE"; scoreP1 = -10;
            resultP2 = "YOU";      dbResultP2 = "WIN";  scoreP2 = 20;
        } else {
            resultP1 = "DRAW";     dbResultP1 = "DRAW"; scoreP1 = 5;
            resultP2 = "DRAW";     dbResultP2 = "DRAW"; scoreP2 = 5;
        }

        updatePlayerPoints(room.p1, resultP1);
        updatePlayerPoints(room.p2, resultP2);

        BizStudent s1 = getStudentBySession(room.p1);
        BizStudent s2 = getStudentBySession(room.p2);

        saveBattleRecord(s1, s2, dbResultP1, scoreP1);
        if (s2 != null) {
            saveBattleRecord(s2, s1, dbResultP2, scoreP2);
        }

        Map<String, Object> res1 = new HashMap<>();
        res1.put("result", resultP1);
        res1.put("myScore", room.p1Score);
        res1.put("oppScore", room.p2Score);
        res1.put("scoreChange", scoreP1);
        sendMessage(room.p1, BattleMessage.of("GAME_OVER", res1));

        Map<String, Object> res2 = new HashMap<>();
        res2.put("result", resultP2);
        res2.put("myScore", room.p2Score);
        res2.put("oppScore", room.p1Score);
        res2.put("scoreChange", scoreP2);
        sendMessage(room.p2, BattleMessage.of("GAME_OVER", res2));

        rooms.remove(room.roomId);
        playerRoomMap.remove(room.p1.getId());
        if (room.p2 != null) {
            playerRoomMap.remove(room.p2.getId());
        }
    }

    private void updatePlayerPoints(WebSocketSession session, String result) {
        if (session == null) return;
        try {
            String studentNo = (String) session.getAttributes().get("username");
            if (studentNo == null) return;
            BizStudent student = studentService.getOne(
                    new QueryWrapper<BizStudent>().eq("student_no", studentNo)
            );
            if (student != null) {
                int currentPoints = student.getPoints() != null ? student.getPoints() : 0;
                int delta = 0;
                switch (result) {
                    case "YOU": delta = 20; break;
                    case "DRAW": delta = 5; break;
                    case "OPPONENT": delta = -10; break;
                }
                int newPoints = currentPoints + delta;
                if (newPoints < 0) newPoints = 0;
                student.setPoints(newPoints);
                studentService.updateById(student);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> buildStudentInfoMap(BizStudent s) {
        Map<String, Object> info = new HashMap<>();
        if (s != null) {
            info.put("name", s.getName());
            info.put("avatar", s.getAvatar());
            info.put("avatarFrameStyle", s.getAvatarFrameStyle());
            info.put("points", s.getPoints());
            String tier = calculateTier(s.getPoints() == null ? 0 : s.getPoints());
            info.put("tier", tier);
            info.put("tierName", getTierNameCN(tier));
        } else {
            info.put("name", "神秘对手");
            info.put("avatar", null);
        }
        return info;
    }

    private void sendMessage(WebSocketSession session, BattleMessage msg) {
        if (session == null) return;
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class BattleRoom {
        String roomId;
        WebSocketSession p1, p2;
        List<BizQuestion> questions;
        int currentRoundIndex = 0;
        String p1Answer, p2Answer;
        int p1Score = 0, p2Score = 0;
        ScheduledFuture<?> timeoutTask;
        boolean isBotGame = false;
        long roundStartTime;
        long p1AnswerTime;
        long p2AnswerTime;
        int p1Combo = 0;
        int p2Combo = 0;
        Map<String, Integer> p1Items = new HashMap<>();
        Map<String, Integer> p2Items = new HashMap<>();

        public BattleRoom(String roomId, WebSocketSession p1, WebSocketSession p2, List<BizQuestion> questions) {
            this.roomId = roomId;
            this.p1 = p1;
            this.p2 = p2;
            this.questions = questions;
            p1Items.put("FOG", 1);
            p1Items.put("HINT", 1);
            p2Items.put("FOG", 1);
            p2Items.put("HINT", 1);
        }
        public BizQuestion getCurrentQuestion() { return questions.get(currentRoundIndex); }

        public void submitAnswer(WebSocketSession s, String a) {
            long now = System.currentTimeMillis();
            if (s.equals(p1)) {
                if (p1Answer == null) {
                    p1Answer = a;
                    p1AnswerTime = now;
                }
            }
            if (p2 != null && s.equals(p2)) {
                if (p2Answer == null) {
                    p2Answer = a;
                    p2AnswerTime = now;
                }
            }
        }
        public boolean isRoundComplete() { return p1Answer != null && p2Answer != null; }
        public boolean hasNextQuestion() { return currentRoundIndex < questions.size() - 1; }
        public void nextRound() {
            currentRoundIndex++;
            p1Answer = null; p2Answer = null;
            p1AnswerTime = 0; p2AnswerTime = 0;
        }
    }

    private synchronized void createBotMatch(WaitingPlayer wp1) {
        if (!sessionWaitingMap.containsKey(wp1.session.getId())) return;

        removeFromQueue(wp1);
        WebSocketSession p1 = wp1.session;

        BizStudent botStudent = new BizStudent();
        botStudent.setId(-1L);
        botStudent.setName("AI 智能助教");
        botStudent.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
        botStudent.setPoints(new Random().nextInt(200) + 500);

        String roomId = UUID.randomUUID().toString();
        List<BizQuestion> questions = questionService.list(
                new QueryWrapper<BizQuestion>()
                        .eq("question_type", 1)
                        .last("ORDER BY RAND() LIMIT 5")
        );

        BattleRoom room = new BattleRoom(roomId, p1, null, questions);
        room.isBotGame = true;

        rooms.put(roomId, room);
        playerRoomMap.put(p1.getId(), roomId);

        sendMatchSuccess(p1, botStudent);

        scheduler.schedule(() -> {
            sendQuestion(room);
            startRoundTimer(room);
        }, 1, TimeUnit.SECONDS);
    }

    private void saveBattleRecord(BizStudent player, BizStudent opponent, String result, int scoreChange) {
        if (player == null) return;
        try {
            BizBattleRecord record = new BizBattleRecord();
            record.setPlayerId(player.getId());
            if (opponent != null) {
                record.setOpponentId(opponent.getId());
                record.setOpponentName(opponent.getName());
            } else {
                record.setOpponentId(-1L);
                record.setOpponentName("AI 智能助教");
            }
            record.setResult(result);
            record.setScoreChange(scoreChange);
            record.setCreateTime(LocalDateTime.now());
            battleRecordService.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}