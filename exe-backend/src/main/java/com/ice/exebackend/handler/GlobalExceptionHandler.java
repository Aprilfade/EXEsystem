package com.ice.exebackend.handler;

import com.ice.exebackend.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理器（增强版）
 *
 * 功能：
 * 1. 统一捕获各类异常，记录详细日志
 * 2. 返回标准 Result 格式，提升前端体验
 * 3. 生产环境不暴露敏感错误信息
 * 4. 添加请求追踪ID，便于问题排查
 * 5. 覆盖数据库、验证、安全、IO等多种异常
 *
 * @author Ice
 * @date 2026-01-12
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 生成追踪ID（用于日志关联）
     */
    private String generateTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put("traceId", traceId);
        }
        return traceId;
    }

    /**
     * 判断是否为生产环境
     */
    private boolean isProduction() {
        return "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
    }

    // ==================== 安全相关异常 ====================

    /**
     * 处理认证失败异常 (如登录失败)
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 认证失败 [{}]: {}", traceId, request.getRequestURI(), e.getMessage());
        return Result.fail("用户名或密码错误");
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 权限不足 [{}] User: {}", traceId, request.getRequestURI(), request.getRemoteUser());
        return Result.fail("您没有权限执行此操作");
    }

    // ==================== 参数验证相关异常 ====================

    /**
     * 处理 @Valid 参数验证异常（用于 @RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String traceId = generateTraceId();

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        logger.warn("[{}] 参数验证失败 [{}]: {}", traceId, request.getRequestURI(), errorMessage);
        return Result.fail("参数验证失败: " + errorMessage);
    }

    /**
     * 处理 Bean Validation 异常（用于 @RequestParam 和路径参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String traceId = generateTraceId();

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        logger.warn("[{}] 约束验证失败 [{}]: {}", traceId, request.getRequestURI(), errorMessage);
        return Result.fail("参数验证失败: " + errorMessage);
    }

    /**
     * 处理绑定异常（表单提交）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBindException(BindException e, HttpServletRequest request) {
        String traceId = generateTraceId();

        String errorMessage = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        logger.warn("[{}] 绑定异常 [{}]: {}", traceId, request.getRequestURI(), errorMessage);
        return Result.fail("参数绑定失败: " + errorMessage);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 缺少参数 [{}]: {}", traceId, request.getRequestURI(), e.getParameterName());
        return Result.fail("缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 参数类型错误 [{}]: {} 期望类型 {}",
                traceId, request.getRequestURI(), e.getName(), e.getRequiredType());
        return Result.fail("参数类型错误: " + e.getName());
    }

    /**
     * 处理请求体解析异常（JSON格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 请求体解析失败 [{}]: {}", traceId, request.getRequestURI(), e.getMessage());
        return Result.fail("请求数据格式错误");
    }

    // ==================== HTTP 请求相关异常 ====================

    /**
     * 处理不支持的请求方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 不支持的请求方法 [{}]: {} 期望 {}",
                traceId, request.getRequestURI(), e.getMethod(), e.getSupportedHttpMethods());
        return Result.fail("不支持的请求方法: " + e.getMethod());
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 文件上传超限 [{}]: {}", traceId, request.getRequestURI(), e.getMessage());
        return Result.fail("上传文件大小超过限制");
    }

    // ==================== 数据库相关异常 ====================

    /**
     * 处理数据库唯一约束违反异常（重复插入）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 数据重复 [{}]: {}", traceId, request.getRequestURI(), e.getMessage());

        // 生产环境不暴露具体错误信息
        if (isProduction()) {
            return Result.fail("数据已存在，无法重复提交");
        }
        return Result.fail("数据重复: " + extractKeyInfo(e.getMessage()));
    }

    /**
     * 处理数据完整性约束违反异常（外键约束等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 数据完整性违反 [{}]: {}", traceId, request.getRequestURI(), e.getMessage());

        if (isProduction()) {
            return Result.fail("操作失败，数据存在关联约束");
        }
        return Result.fail("数据完整性错误: " + e.getMostSpecificCause().getMessage());
    }

    /**
     * 处理一般数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.error("[{}] 数据库访问异常 [{}]: ", traceId, request.getRequestURI(), e);

        if (isProduction()) {
            return Result.fail("数据库操作失败，请稍后重试");
        }
        return Result.fail("数据库错误: " + e.getMostSpecificCause().getMessage());
    }

    /**
     * 处理 SQL 异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleSQLException(SQLException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.error("[{}] SQL异常 [{}] SQLState: {}, ErrorCode: {}",
                traceId, request.getRequestURI(), e.getSQLState(), e.getErrorCode(), e);

        if (isProduction()) {
            return Result.fail("系统错误，请联系管理员");
        }
        return Result.fail("SQL错误: " + e.getMessage());
    }

    // ==================== 业务运行时异常 ====================

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.error("[{}] 空指针异常 [{}]: ", traceId, request.getRequestURI(), e);

        if (isProduction()) {
            return Result.fail("系统错误，请联系管理员 (错误ID: " + traceId + ")");
        }
        return Result.fail("空指针异常: " + e.getMessage());
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.warn("[{}] 非法参数 [{}]: {}", traceId, request.getRequestURI(), e.getMessage());
        return Result.fail("参数错误: " + e.getMessage());
    }

    /**
     * 处理运行时异常（业务异常）
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.error("[{}] 运行时异常 [{}]: ", traceId, request.getRequestURI(), e);

        // 如果异常消息是业务相关的，直接返回（假设业务异常消息是用户友好的）
        if (e.getMessage() != null && !e.getMessage().contains("Exception") && !e.getMessage().contains("Error")) {
            return Result.fail(e.getMessage());
        }

        if (isProduction()) {
            return Result.fail("操作失败，请稍后重试 (错误ID: " + traceId + ")");
        }
        return Result.fail("系统错误: " + e.getMessage());
    }

    // ==================== 兜底异常处理 ====================

    /**
     * 兜底异常处理器（处理所有未捕获的异常）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e, HttpServletRequest request) {
        String traceId = generateTraceId();
        logger.error("[{}] 系统未知异常 [{}]: ", traceId, request.getRequestURI(), e);

        if (isProduction()) {
            return Result.fail("系统内部错误，请联系管理员 (错误ID: " + traceId + ")");
        }
        return Result.fail("系统内部错误: " + e.getMessage());
    }

    // ==================== 辅助方法 ====================

    /**
     * 从数据库错误信息中提取关键信息
     */
    private String extractKeyInfo(String message) {
        if (message == null) {
            return "未知错误";
        }

        // 提取唯一键名称
        if (message.contains("key '")) {
            int start = message.indexOf("key '") + 5;
            int end = message.indexOf("'", start);
            if (end > start) {
                return message.substring(start, end);
            }
        }

        return message.length() > 100 ? message.substring(0, 100) + "..." : message;
    }
}