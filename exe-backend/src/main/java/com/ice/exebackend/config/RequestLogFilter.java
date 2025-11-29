package com.ice.exebackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 请求日志过滤器
 * 用于记录所有接口的请求路径、IP地址和执行耗时
 */
@Component
public class RequestLogFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 转换为 HttpServletRequest 以便获取特定信息
        HttpServletRequest req = (HttpServletRequest) request;

        // 1. 记录开始时间
        long startTime = System.currentTimeMillis();

        try {
            // 2. 放行请求，让其进入 Controller 执行业务逻辑
            chain.doFilter(request, response);
        } finally {
            // 3. 业务执行完毕（无论成功或失败），计算耗时
            long duration = System.currentTimeMillis() - startTime;

            // 4. 获取真实的客户端 IP (处理代理情况)
            String clientIp = getIpAddress(req);

            // 5. 打印日志
            // 格式: [METHOD] URI | IP | 耗时
            logger.info("Request: [{}] {} | IP: {} | Time: {}ms",
                    req.getMethod(),
                    req.getRequestURI(),
                    clientIp,
                    duration);
        }
    }

    /**
     * 获取客户端真实IP地址
     * 能够处理 Nginx 等反向代理的情况
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多级代理，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 本地开发环境特殊处理
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    private boolean hasText(String str) {
        return StringUtils.hasText(str);
    }
}