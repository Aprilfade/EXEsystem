package com.ice.exebackend.config;

import com.ice.exebackend.handler.BattleWebSocketHandler;
import com.ice.exebackend.handler.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private NotificationWebSocketHandler notificationHandler;

    @Autowired
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Autowired
    private BattleWebSocketHandler battleHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 原有的通知 WebSocket
        registry.addHandler(notificationHandler, "/ws/notifications")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*");

        // 【新增】对战 WebSocket
        registry.addHandler(battleHandler, "/ws/battle")
                .addInterceptors(jwtHandshakeInterceptor) // 复用鉴权
                .setAllowedOriginPatterns("*");
    }
}