package com.ice.exebackend.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接建立，等待用户发送 MATCH 指令
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject payload = JSON.parseObject(message.getPayload());
        String type = payload.getString("type");

        if ("MATCH".equals(type)) {
            battleManager.joinQueue(session);
        } else if ("ANSWER".equals(type)) {
            String answer = payload.getString("data");
            battleManager.handleAnswer(session, answer);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        battleManager.leave(session);
    }
}