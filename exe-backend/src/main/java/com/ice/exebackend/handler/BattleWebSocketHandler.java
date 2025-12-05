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

    // 【新增】注入 Jackson 的 ObjectMapper，保持全项目统一
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接建立，等待用户发送 MATCH 指令
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 【修改】使用 Jackson 解析 JSON
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String type = payload.has("type") ? payload.get("type").asText() : "";

        if ("MATCH".equals(type)) {
            battleManager.joinQueue(session);
        } else if ("ANSWER".equals(type)) {
            // 注意：这里 data 字段可能是对象也可能是字符串，根据之前逻辑它是选项 Key (String)
            String answer = payload.has("data") ? payload.get("data").asText() : "";
            battleManager.handleAnswer(session, answer);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        battleManager.leave(session);
    }
}