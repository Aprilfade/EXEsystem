package com.ice.exebackend.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    private static final CopyOnWriteArraySet<WebSocketSession> SESSIONS = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SESSIONS.add(session);
        String username = (String) session.getAttributes().get("username");
        String userType = (String) session.getAttributes().get("userType"); // 获取身份
        logger.info("用户上线: {} [{}] (当前在线: {})", username, userType, SESSIONS.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.remove(session);
        logger.info("用户下线 (剩余在线: {})", SESSIONS.size());
    }

    /**
     * 【修改】定向广播消息
     * @param message 消息内容
     * @param targetType 发送目标: "ALL", "STUDENT", "TEACHER"
     */
    public void broadcast(String message, String targetType) {
        for (WebSocketSession session : SESSIONS) {
            if (session.isOpen()) {
                // 获取该连接的用户类型
                String userType = (String) session.getAttributes().get("userType");

                // 判断是否需要发送
                boolean shouldSend = false;
                if ("ALL".equals(targetType) || targetType == null) {
                    shouldSend = true;
                } else if (targetType.equals(userType)) {
                    shouldSend = true;
                }

                if (shouldSend) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        logger.error("发送消息失败", e);
                    }
                }
            }
        }
    }
}