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
                // 通知对手，游戏强制结束
                WebSocketSession opponent = room.p1.equals(session) ? room.p2 : room.p1;
                sendMessage(opponent, BattleMessage.of("OPPONENT_LEFT", null));
                rooms.remove(roomId);
                playerRoomMap.remove(opponent.getId());
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
        room.submitAnswer(session, answerStr);

        // 检查是否双方都已作答
        if (room.isRoundComplete()) {
            // 发送本轮结果
            sendRoundResult(room);
            // 准备下一题
            if (room.hasNextQuestion()) {
                room.nextRound();
                sendQuestion(room); // 发送新题目
            } else {
                sendGameOver(room); // 游戏结束
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

        // 通知双方匹配成功
        sendMessage(p1, BattleMessage.of("MATCH_SUCCESS", "对手已就位"));
        sendMessage(p2, BattleMessage.of("MATCH_SUCCESS", "对手已就位"));

        // 延迟 1秒 发送第一题
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        sendQuestion(room);
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

    private void sendRoundResult(BattleRoom room) {
        // 计算本轮得分逻辑
        // 简单逻辑：答对得20分
        BizQuestion q = room.getCurrentQuestion();
        boolean p1Correct = room.p1Answer.equalsIgnoreCase(q.getAnswer());
        boolean p2Correct = room.p2Answer.equalsIgnoreCase(q.getAnswer());

        if (p1Correct) room.p1Score += 20;
        if (p2Correct) room.p2Score += 20;

        Map<String, Object> result = new HashMap<>();
        result.put("correctAnswer", q.getAnswer());
        result.put("p1Answer", room.p1Answer);
        result.put("p2Answer", room.p2Answer);
        result.put("p1Score", room.p1Score);
        result.put("p2Score", room.p2Score);

        sendMessage(room.p1, BattleMessage.of("ROUND_RESULT", result));
        sendMessage(room.p2, BattleMessage.of("ROUND_RESULT", result));
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
}