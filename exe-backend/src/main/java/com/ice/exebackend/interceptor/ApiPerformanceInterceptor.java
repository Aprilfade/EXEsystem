package com.ice.exebackend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * API响应时间监控拦截器
 *
 * 功能：
 * 1. 记录每个API请求的响应时间
 * 2. 检测慢接口（响应时间超过阈值）
 * 3. 生成请求追踪ID，便于日志关联
 * 4. 记录请求来源、用户代理等信息
 *
 * @author Ice
 * @date 2026-01-12
 */
@Component
public class ApiPerformanceInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiPerformanceInterceptor.class);

    /**
     * 慢接口阈值（毫秒）
     * 超过此时间的接口会被记录为慢接口
     */
    private static final long SLOW_API_THRESHOLD_MS = 1000; // 1秒

    /**
     * 超慢接口阈值（毫秒）
     * 超过此时间的接口会记录WARNING级别日志
     */
    private static final long VERY_SLOW_API_THRESHOLD_MS = 3000; // 3秒

    /**
     * 请求开始时间的属性名
     */
    private static final String START_TIME_ATTRIBUTE = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 生成并设置追踪ID
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);
        request.setAttribute("traceId", traceId);

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        // 记录请求信息（仅DEBUG级别）
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] 开始处理请求: {} {} | IP: {} | User-Agent: {}",
                    traceId,
                    request.getMethod(),
                    request.getRequestURI(),
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"));
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 此方法在controller执行完成后、视图渲染前调用
        // 对于RESTful API，通常不需要特殊处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 计算请求处理时间
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            if (startTime == null) {
                return;
            }

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            String traceId = (String) request.getAttribute("traceId");

            // 获取请求信息
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUrl = queryString != null ? uri + "?" + queryString : uri;
            int status = response.getStatus();
            String clientIp = getClientIpAddress(request);

            // 根据响应时间和HTTP状态码选择日志级别
            if (ex != null) {
                // 请求处理过程中发生异常
                logger.error("[{}] 请求异常 | {} {} | 状态码: {} | 耗时: {}ms | IP: {} | 异常: {}",
                        traceId, method, fullUrl, status, executionTime, clientIp, ex.getMessage());

            } else if (executionTime >= VERY_SLOW_API_THRESHOLD_MS) {
                // 超慢接口：WARNING级别
                logger.warn("[{}] ⚠️ 超慢接口检测 | {} {} | 状态码: {} | 耗时: {}ms | IP: {}",
                        traceId, method, fullUrl, status, executionTime, clientIp);

            } else if (executionTime >= SLOW_API_THRESHOLD_MS) {
                // 慢接口：INFO级别
                logger.info("[{}] 🐌 慢接口检测 | {} {} | 状态码: {} | 耗时: {}ms | IP: {}",
                        traceId, method, fullUrl, status, executionTime, clientIp);

            } else if (status >= 400) {
                // 4xx 或 5xx 错误，即使响应快也记录
                logger.warn("[{}] HTTP错误 | {} {} | 状态码: {} | 耗时: {}ms | IP: {}",
                        traceId, method, fullUrl, status, executionTime, clientIp);

            } else {
                // 正常请求：DEBUG级别
                if (logger.isDebugEnabled()) {
                    logger.debug("[{}] 请求完成 | {} {} | 状态码: {} | 耗时: {}ms | IP: {}",
                            traceId, method, fullUrl, status, executionTime, clientIp);
                }
            }

            // 【可选】将性能数据记录到数据库或监控系统
            // recordApiPerformance(traceId, method, uri, status, executionTime, clientIp);

        } finally {
            // 清理MDC（防止内存泄漏）
            MDC.clear();
        }
    }

    /**
     * 获取客户端真实IP地址
     * 考虑代理、负载均衡等场景
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 【可选】将API性能数据记录到数据库或监控系统
     *
     * @param traceId       追踪ID
     * @param method        HTTP方法
     * @param uri           请求URI
     * @param status        响应状态码
     * @param executionTime 执行时间（毫秒）
     * @param clientIp      客户端IP
     */
    private void recordApiPerformance(String traceId, String method, String uri, int status, long executionTime, String clientIp) {
        // 这里可以实现将性能数据写入数据库或发送到监控系统（如Prometheus、Grafana等）
        // 示例：
        // apiPerformanceService.record(traceId, method, uri, status, executionTime, clientIp);
    }
}
