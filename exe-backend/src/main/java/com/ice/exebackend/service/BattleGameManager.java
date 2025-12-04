package com.ice.exebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.dto.BattleMessage;
import com.ice.exebackend.dto.QuestionDTO;
import com.ice.exebackend.entity.BizQuestion;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class BattleGameManager {

    @Autowired
    private BizQuestionService questionService; // 复用你现有的题库服务
    @Autowired
    private ObjectMapper objectMapper;
    // 【新增】定义一个单线程的调度线程池，用于处理所有房间的倒计时
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // 【新增】每题倒计时时间（秒）
    private static final int ROUND_TIMEOUT_SECONDS = 20;

    // 【新增】注入学生服务，用于查询用户信息
    @Autowired
    private BizStudentService studentService;

    // 等待队列 (存放 WebSocketSession)
    private final CopyOnWriteArrayList<WebSocketSession> waitingQueue = new CopyOnWriteArrayList<>();

    // 房间映射: SessionID -> RoomID
    private final Map<String, String> playerRoomMap = new ConcurrentHashMap<>();

    // 房间存储: RoomID -> RoomObj
    private final Map<String, BattleRoom> rooms = new ConcurrentHashMap<>();

    /**
     * 用户申请匹配
     */
    public synchronized void joinQueue(WebSocketSession session) {
        if (waitingQueue.contains(session)) return;

        waitingQueue.add(session);

        // 如果队列有2人，立即开始
        if (waitingQueue.size() >= 2) {
            WebSocketSession p1 = waitingQueue.remove(0);
            WebSocketSession p2 = waitingQueue.remove(0);
            createRoom(p1, p2);
        }
    }

    /**
     * 用户取消匹配或断开
     */
    public void leave(WebSocketSession session) {
        waitingQueue.remove(session);
        String roomId = playerRoomMap.remove(session.getId());
        if (roomId != null) {
            BattleRoom room = rooms.get(roomId);
            if (room != null) {
                synchronized (room) {
                    // 【新增】取消定时任务
                    if (room.timeoutTask != null) {
                        room.timeoutTask.cancel(true);
                    }

                    // 通知对手
                    WebSocketSession opponent = room.p1.equals(session) ? room.p2 : room.p1;
                    sendMessage(opponent, BattleMessage.of("OPPONENT_LEFT", null));
                    rooms.remove(roomId);
                    playerRoomMap.remove(opponent.getId());
                }
            }
        }
    }

    /**
     * 处理用户提交答案
     */
    public void handleAnswer(WebSocketSession session, String answerStr) {
        String roomId = playerRoomMap.get(session.getId());
        if (roomId == null) return;

        BattleRoom room = rooms.get(roomId);

        // 【修改】加锁，保证线程安全
        synchronized (room) {
            room.submitAnswer(session, answerStr);

            // 检查是否双方都已作答
            if (room.isRoundComplete()) {
                // 【新增】双方都答了，立即取消当前的倒计时任务
                if (room.timeoutTask != null) {
                    room.timeoutTask.cancel(false);
                }

                // 发送本轮结果
                sendRoundResult(room);

                // 准备下一题
                if (room.hasNextQuestion()) {
                    room.nextRound();
                    sendQuestion(room); // 发送新题目
                    startRoundTimer(room); // 【新增】启动下一轮倒计时
                } else {
                    sendGameOver(room); // 游戏结束
                }
            }
        }
    }
    // --- 内部逻辑 ---

    private void createRoom(WebSocketSession p1, WebSocketSession p2) {
        String roomId = UUID.randomUUID().toString();

        // 随机抽取 5 道题
        // 注意：这里使用 MyBatis Plus 的随机查询
        List<BizQuestion> questions = questionService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BizQuestion>()
                        .eq("question_type", 1) // 仅限单选题比较适合PK
                        .last("ORDER BY RAND() LIMIT 5")
        );

        BattleRoom room = new BattleRoom(roomId, p1, p2, questions);
        rooms.put(roomId, room);
        playerRoomMap.put(p1.getId(), roomId);
        playerRoomMap.put(p2.getId(), roomId);

        // 【核心修改开始】 获取双方用户信息并交叉发送
        // 从 session 中获取学号 (在 JwtHandshakeInterceptor 中放入的 "username")
        String p1No = (String) p1.getAttributes().get("username");
        String p2No = (String) p2.getAttributes().get("username");

        // 查询数据库获取详细信息
        // 注意：lambdaQuery() 需要 MyBatis-Plus 支持，确保 BizStudentService 继承了 IService
        BizStudent s1 = studentService.lambdaQuery().eq(BizStudent::getStudentNo, p1No).one();
        BizStudent s2 = studentService.lambdaQuery().eq(BizStudent::getStudentNo, p2No).one();

        // 构造发给 P1 的消息 (包含 P2 的信息)
        Map<String, Object> dataForP1 = new HashMap<>();
        dataForP1.put("message", "匹配成功");
        dataForP1.put("opponent", buildStudentInfoMap(s2)); // 封装对手信息
        sendMessage(p1, BattleMessage.of("MATCH_SUCCESS", dataForP1));

        // 构造发给 P2 的消息 (包含 P1 的信息)
        Map<String, Object> dataForP2 = new HashMap<>();
        dataForP2.put("message", "匹配成功");
        dataForP2.put("opponent", buildStudentInfoMap(s1)); // 封装对手信息
        sendMessage(p2, BattleMessage.of("MATCH_SUCCESS", dataForP2));
        // 【核心修改结束】
        // 延迟 1秒 发送第一题
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        sendQuestion(room);
        startRoundTimer(room); // <--- 新增这一行
    }



    // 【新增】辅助方法：构建脱敏的学生信息 Map
    private Map<String, Object> buildStudentInfoMap(BizStudent s) {
        Map<String, Object> info = new HashMap<>();
        if (s != null) {
            info.put("name", s.getName());
            info.put("avatar", s.getAvatar());
            info.put("avatarFrameStyle", s.getAvatarFrameStyle());
        } else {
            info.put("name", "神秘对手");
            info.put("avatar", null);
        }
        return info;
    }
    private void sendQuestion(BattleRoom room) {
        BizQuestion q = room.getCurrentQuestion();
        // 脱敏，不发答案
        Map<String, Object> qData = new HashMap<>();
        qData.put("content", q.getContent());
        qData.put("options", q.getOptions());
        qData.put("round", room.currentRoundIndex + 1);
        qData.put("total", room.questions.size());

        BattleMessage msg = BattleMessage.of("QUESTION", qData);
        sendMessage(room.p1, msg);
        sendMessage(room.p2, msg);
    }

    // 【修改后】修复后的代码
    private void sendRoundResult(BattleRoom room) {
        // 1. 计算本轮得分（保持原逻辑）
        BizQuestion q = room.getCurrentQuestion();

        // 简单的判分逻辑：如果不区分单选多选，假设全匹配才给分
        // 注意：这里使用了 equalsIgnoreCase，实际生产中可能需要更复杂的判分（如多选少选）
        boolean p1Correct = room.p1Answer != null && room.p1Answer.equalsIgnoreCase(q.getAnswer());
        boolean p2Correct = room.p2Answer != null && room.p2Answer.equalsIgnoreCase(q.getAnswer());

        if (p1Correct) room.p1Score += 20;
        if (p2Correct) room.p2Score += 20;

        // 2. 为 P1 构建数据 (P1 是 "我", P2 是 "对手")
        Map<String, Object> dataForP1 = new HashMap<>();
        dataForP1.put("correctAnswer", q.getAnswer());
        dataForP1.put("myAnswer", room.p1Answer);      // P1 的答案是 "myAnswer"
        dataForP1.put("oppAnswer", room.p2Answer);     // P2 的答案是 "oppAnswer"
        dataForP1.put("myScore", room.p1Score);        // P1 的分是 "myScore"
        dataForP1.put("oppScore", room.p2Score);       // P2 的分是 "oppScore"
        dataForP1.put("isCorrect", p1Correct);         // 方便前端展示对错

        sendMessage(room.p1, BattleMessage.of("ROUND_RESULT", dataForP1));

        // 3. 为 P2 构建数据 (P2 是 "我", P1 是 "对手")
        Map<String, Object> dataForP2 = new HashMap<>();
        dataForP2.put("correctAnswer", q.getAnswer());
        dataForP2.put("myAnswer", room.p2Answer);      // P2 的答案是 "myAnswer"
        dataForP2.put("oppAnswer", room.p1Answer);     // P1 的答案是 "oppAnswer"
        dataForP2.put("myScore", room.p2Score);        // P2 的分是 "myScore"
        dataForP2.put("oppScore", room.p1Score);       // P1 的分是 "oppScore"
        dataForP2.put("isCorrect", p2Correct);         // 方便前端展示对错

        sendMessage(room.p2, BattleMessage.of("ROUND_RESULT", dataForP2));
    }

    private void sendGameOver(BattleRoom room) {
        String winner = room.p1Score > room.p2Score ? "YOU" : (room.p1Score < room.p2Score ? "OPPONENT" : "DRAW");

        // 发送给 P1
        Map<String, Object> res1 = new HashMap<>();
        res1.put("result", winner);
        res1.put("myScore", room.p1Score);
        res1.put("oppScore", room.p2Score);
        sendMessage(room.p1, BattleMessage.of("GAME_OVER", res1));

        // 发送给 P2 (反转结果)
        String winner2 = winner.equals("YOU") ? "OPPONENT" : (winner.equals("DRAW") ? "DRAW" : "YOU");
        Map<String, Object> res2 = new HashMap<>();
        res2.put("result", winner2);
        res2.put("myScore", room.p2Score);
        res2.put("oppScore", room.p1Score);
        sendMessage(room.p2, BattleMessage.of("GAME_OVER", res2));

        // 清理
        rooms.remove(room.roomId);
        playerRoomMap.remove(room.p1.getId());
        playerRoomMap.remove(room.p2.getId());
    }

    private void sendMessage(WebSocketSession session, BattleMessage msg) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- 内部类：房间 ---
    private static class BattleRoom {
        String roomId;
        WebSocketSession p1;
        WebSocketSession p2;
        List<BizQuestion> questions;
        int currentRoundIndex = 0;

        String p1Answer = null;
        String p2Answer = null;
        int p1Score = 0;
        int p2Score = 0;

        // 【新增】持有当前回合的超时任务，以便取消
        ScheduledFuture<?> timeoutTask;

        public BattleRoom(String roomId, WebSocketSession p1, WebSocketSession p2, List<BizQuestion> questions) {
            this.roomId = roomId;
            this.p1 = p1;
            this.p2 = p2;
            this.questions = questions;
        }

        public BizQuestion getCurrentQuestion() {
            return questions.get(currentRoundIndex);
        }

        public void submitAnswer(WebSocketSession session, String answer) {
            if (session.equals(p1)) p1Answer = answer;
            if (session.equals(p2)) p2Answer = answer;
        }

        public boolean isRoundComplete() {
            return p1Answer != null && p2Answer != null;
        }

        public boolean hasNextQuestion() {
            return currentRoundIndex < questions.size() - 1;
        }

        public void nextRound() {
            currentRoundIndex++;
            p1Answer = null;
            p2Answer = null;
        }
    }

    /**
     * 【新增】开启回合倒计时
     */
    private void startRoundTimer(BattleRoom room) {
        // 先取消旧任务（防御性编程）
        if (room.timeoutTask != null && !room.timeoutTask.isDone()) {
            room.timeoutTask.cancel(false);
        }

        // 记录当前回合数，用于在回调时校验（防止上一题的延迟任务影响下一题）
        int roundAtStart = room.currentRoundIndex;

        // 调度任务：ROUND_TIMEOUT_SECONDS 后执行
        room.timeoutTask = scheduler.schedule(() -> {
            // 必须加锁，防止和 handleAnswer 冲突
            synchronized (room) {
                // 再次检查：如果已经进入下一轮了，或者房间已销毁，则忽略
                if (room.currentRoundIndex != roundAtStart) {
                    return;
                }

                // 触发超时结算逻辑
                handleRoundTimeout(room);
            }
        }, ROUND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 【新增】处理超时逻辑
     */
    private void handleRoundTimeout(BattleRoom room) {
        // 1. 强制结算当前回合
        // (此时未答题的玩家 answer 字段为 null，sendRoundResult 会判错，符合预期)
        sendRoundResult(room);

        // 2. 进入下一轮或结束
        if (room.hasNextQuestion()) {
            room.nextRound();
            sendQuestion(room);
            // 递归启动下一轮计时
            startRoundTimer(room);
        } else {
            sendGameOver(room);
        }
    }

}