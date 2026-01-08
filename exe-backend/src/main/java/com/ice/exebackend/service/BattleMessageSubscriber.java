package com.ice.exebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.dto.BattleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BattleMessageSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(BattleMessageSubscriber.class);

    // 用来存放本地的 Session，Key 是用户 ID (String)
    public static final Map<String, WebSocketSession> LOCAL_SESSION_MAP = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理 Redis 广播过来的消息
     * messageBody 格式: { "targetUserId": "1001", "payload": { ...BattleMessage... } }
     */
    public void onMessage(String messageBody) {
        try {
            // 解决 "未经检查的操作" 警告，进行类型转换
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(messageBody, Map.class);

            String targetUserId = String.valueOf(map.get("targetUserId"));

            // 检查目标用户是否连接在【本机】
            WebSocketSession session = LOCAL_SESSION_MAP.get(targetUserId);
            if (session != null && session.isOpen()) {
                // 如果在，就通过 WebSocket 发送真实数据
                Object payload = map.get("payload");
                String jsonMsg = objectMapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(jsonMsg));
            }
        } catch (Exception e) {
            logger.error("处理Redis广播消息失败", e);
        }
    }
}