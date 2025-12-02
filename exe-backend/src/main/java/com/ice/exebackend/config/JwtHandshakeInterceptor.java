package com.ice.exebackend.config;

import com.ice.exebackend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");

            if (StringUtils.hasText(token)) {
                try {
                    String username = jwtUtil.getUsernameFromToken(token);
                    if (username != null) {
                        attributes.put("username", username);

                        // 【新增】解析用户角色类型
                        // 默认为 TEACHER，如果检测到 ROLE_STUDENT 则设为 STUDENT
                        String userType = "TEACHER";

                        // 获取权限列表
                        List<Map<String, String>> authorities = jwtUtil.getClaimFromToken(token, claims -> claims.get("authorities", List.class));

                        if (authorities != null) {
                            for (Map<String, String> auth : authorities) {
                                if ("ROLE_STUDENT".equals(auth.get("authority"))) {
                                    userType = "STUDENT";
                                    break;
                                }
                            }
                        }

                        attributes.put("userType", userType); // 存入 session 属性
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}