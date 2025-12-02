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

    // 线程安全的 Set，存放所有在线的 Session
    private static final CopyOnWriteArraySet<WebSocketSession> SESSIONS = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SESSIONS.add(session);
        String username = (String) session.getAttributes().get("username");
        logger.info("用户上线: {} (当前在线人数: {})", username, SESSIONS.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.remove(session);
        logger.info("用户下线 (剩余在线人数: {})", SESSIONS.size());
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(String message) {
        for (WebSocketSession session : SESSIONS) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.error("发送消息失败", e);
                }
            }
        }
    }

    // 如果需要给指定用户发消息（例如作业批改完成），可扩展此方法：
    // 根据 session.getAttributes().get("username") 筛选
}