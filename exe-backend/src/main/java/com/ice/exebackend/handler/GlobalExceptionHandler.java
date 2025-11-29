package com.ice.exebackend.handler;

import com.ice.exebackend.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获异常，记录日志，并返回标准 Result 格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理认证失败异常 (如登录失败)
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logger.warn("认证失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.fail("用户名或密码错误");
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.warn("权限不足 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.fail("您没有权限执行此操作");
    }

    /**
     * 处理业务逻辑异常 (RuntimeException)
     * 同时也作为兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        // 记录完整的堆栈信息，方便排查 BUG
        logger.error("系统异常处理 [{}]: ", request.getRequestURI(), e);

        // 如果是自定义业务异常（假设你未来会加），可以在这里特殊判断
        // if (e instanceof MyBusinessException) { ... }

        // 对于未知的系统异常，返回通用的错误提示，避免将底层报错直接暴露给前端
        return Result.fail("系统内部错误: " + e.getMessage());
    }
}