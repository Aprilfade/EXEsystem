package com.ice.exebackend.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.service.BattleGameManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class BattleWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private BattleGameManager battleManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接建立
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String type = payload.has("type") ? payload.get("type").asText() : "";

        if ("MATCH".equals(type)) {
            battleManager.joinQueue(session);
        } else if ("ANSWER".equals(type)) {
            String answer = payload.has("data") ? payload.get("data").asText() : "";
            battleManager.handleAnswer(session, answer);
        }
        // 【新增】处理道具使用请求
        else if ("USE_ITEM".equals(type)) {
            String itemType = payload.has("data") ? payload.get("data").asText() : "";
            battleManager.handleItemUsage(session, itemType);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        battleManager.leave(session);
    }
}