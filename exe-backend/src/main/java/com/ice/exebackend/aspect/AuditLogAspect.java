package com.ice.exebackend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.annotation.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志切面
 *
 * 功能：
 * 1. 拦截标记了@AuditLog注解的方法
 * 2. 记录操作用户、时间、IP、操作内容等信息
 * 3. 记录操作结果（成功/失败）
 * 4. 记录异常信息（如果操作失败）
 *
 * @author Ice
 * @date 2026-01-12
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger logger = LoggerFactory.getLogger("AUDIT_LOG");

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点：所有标记了@AuditLog注解的方法
     */
    @Pointcut("@annotation(com.ice.exebackend.annotation.AuditLog)")
    public void auditLogPointcut() {
    }

    /**
     * 方法执行成功后记录审计日志
     */
    @AfterReturning(pointcut = "auditLogPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleAuditLog(joinPoint, result, null);
    }

    /**
     * 方法执行异常后记录审计日志
     */
    @AfterThrowing(pointcut = "auditLogPointcut()", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, Exception exception) {
        handleAuditLog(joinPoint, null, exception);
    }

    /**
     * 处理审计日志
     */
    private void handleAuditLog(JoinPoint joinPoint, Object result, Exception exception) {
        try {
            // 1. 获取注解信息
            AuditLog auditLog = getAuditLogAnnotation(joinPoint);
            if (auditLog == null) {
                return;
            }

            // 2. 获取请求信息
            HttpServletRequest request = getHttpServletRequest();
            if (request == null) {
                return;
            }

            // 3. 获取当前用户信息
            String username = getCurrentUsername();
            String userRole = getCurrentUserRole();

            // 4. 构建审计日志数据
            Map<String, Object> auditData = new HashMap<>();

            // 基础信息
            auditData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            auditData.put("module", auditLog.module());
            auditData.put("operationType", auditLog.operationType().name());
            auditData.put("description", auditLog.description());

            // 用户信息
            auditData.put("username", username != null ? username : "匿名用户");
            auditData.put("userRole", userRole != null ? userRole : "未知角色");

            // 请求信息
            auditData.put("requestMethod", request.getMethod());
            auditData.put("requestUrl", request.getRequestURI());
            auditData.put("requestIp", getClientIpAddress(request));
            auditData.put("userAgent", request.getHeader("User-Agent"));

            // 方法信息
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            auditData.put("className", className);
            auditData.put("methodName", methodName);

            // 请求参数（脱敏处理）
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = serializeParams(args);
                auditData.put("params", maskSensitiveData(params));
            }

            // 操作结果
            if (exception != null) {
                auditData.put("status", "失败");
                auditData.put("errorMessage", exception.getMessage());
                auditData.put("errorType", exception.getClass().getSimpleName());
            } else {
                auditData.put("status", "成功");
                // 返回结果（可选，避免日志过大）
                // auditData.put("result", result != null ? result.toString() : "null");
            }

            // 5. 记录审计日志
            String logMessage = buildLogMessage(auditData);

            if (exception != null) {
                logger.warn("🔒 " + logMessage);
            } else {
                logger.info("✅ " + logMessage);
            }

            // 【可选】将审计日志写入数据库
            // saveAuditLogToDatabase(auditData);

        } catch (Exception e) {
            // 审计日志记录失败不应影响业务执行
            logger.error("记录审计日志时发生异常", e);
        }
    }

    /**
     * 获取@AuditLog注解
     */
    private AuditLog getAuditLogAnnotation(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            return method.getAnnotation(AuditLog.class);
        } catch (Exception e) {
            logger.error("获取@AuditLog注解失败", e);
            return null;
        }
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                } else {
                    return principal.toString();
                }
            }
        } catch (Exception e) {
            logger.debug("获取当前用户名失败", e);
        }
        return null;
    }

    /**
     * 获取当前用户角色
     */
    private String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getAuthorities() != null) {
                return authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            logger.debug("获取当前用户角色失败", e);
        }
        return null;
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
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
     * 序列化参数
     */
    private String serializeParams(Object[] args) {
        try {
            // 过滤掉不需要序列化的参数（如HttpServletRequest等）
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null &&
                        !(arg instanceof HttpServletRequest) &&
                        !(arg instanceof jakarta.servlet.http.HttpServletResponse)) {

                    if (sb.length() > 0) {
                        sb.append(", ");
                    }

                    // 简单类型直接toString，复杂类型使用JSON序列化
                    if (isSimpleType(arg)) {
                        sb.append(arg);
                    } else {
                        try {
                            String json = objectMapper.writeValueAsString(arg);
                            // 限制长度，避免日志过大
                            if (json.length() > 500) {
                                sb.append(json, 0, 500).append("...");
                            } else {
                                sb.append(json);
                            }
                        } catch (Exception e) {
                            sb.append(arg.getClass().getSimpleName());
                        }
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "参数序列化失败";
        }
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Object obj) {
        return obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Character ||
                obj.getClass().isPrimitive();
    }

    /**
     * 脱敏处理（隐藏敏感信息）
     */
    private String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        // 脱敏密码字段
        data = data.replaceAll("(\"password\"\\s*:\\s*\")[^\"]*\"", "$1******\"");
        data = data.replaceAll("(\"pwd\"\\s*:\\s*\")[^\"]*\"", "$1******\"");
        data = data.replaceAll("(\"oldPassword\"\\s*:\\s*\")[^\"]*\"", "$1******\"");
        data = data.replaceAll("(\"newPassword\"\\s*:\\s*\")[^\"]*\"", "$1******\"");

        // 脱敏身份证号（保留前6位和后4位）
        data = data.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");

        // 脱敏手机号（保留前3位和后4位）
        data = data.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");

        return data;
    }

    /**
     * 构建日志消息
     */
    private String buildLogMessage(Map<String, Object> auditData) {
        return String.format("审计日志 | 用户: %s(%s) | 操作: %s-%s | 描述: %s | URL: %s %s | IP: %s | 状态: %s%s",
                auditData.get("username"),
                auditData.get("userRole"),
                auditData.get("module"),
                auditData.get("operationType"),
                auditData.get("description"),
                auditData.get("requestMethod"),
                auditData.get("requestUrl"),
                auditData.get("requestIp"),
                auditData.get("status"),
                auditData.containsKey("errorMessage") ? " | 错误: " + auditData.get("errorMessage") : ""
        );
    }

    /**
     * 【可选】将审计日志保存到数据库
     */
    private void saveAuditLogToDatabase(Map<String, Object> auditData) {
        // 这里可以实现将审计日志保存到数据库的逻辑
        // 示例：
        // SysAuditLog log = new SysAuditLog();
        // log.setUsername((String) auditData.get("username"));
        // log.setModule((String) auditData.get("module"));
        // ...
        // auditLogService.save(log);
    }
}
