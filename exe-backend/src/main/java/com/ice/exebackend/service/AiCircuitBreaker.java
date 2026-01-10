package com.ice.exebackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI服务断路器（Circuit Breaker）
 * 当AI服务频繁失败时，自动熔断，避免级联故障
 */
@Component
public class AiCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(AiCircuitBreaker.class);

    // 断路器状态
    private enum State {
        CLOSED,      // 关闭（正常工作）
        OPEN,        // 打开（熔断中）
        HALF_OPEN    // 半开（尝试恢复）
    }

    private volatile State state = State.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile LocalDateTime openedTime = null;

    // 配置参数
    private static final int FAILURE_THRESHOLD = 10;           // 失败阈值：连续10次失败
    private static final long OPEN_DURATION_MILLIS = 300000;   // 熔断持续时间：5分钟
    private static final int HALF_OPEN_SUCCESS_THRESHOLD = 3;  // 半开状态成功阈值：连续3次成功
    private static final int HALF_OPEN_MAX_ATTEMPTS = 5;       // 半开状态最大尝试次数

    /**
     * 检查是否允许请求通过
     *
     * @return true:允许通过, false:被熔断
     */
    public synchronized boolean allowRequest() {
        switch (state) {
            case CLOSED:
                // 关闭状态，允许所有请求
                return true;

            case OPEN:
                // 打开状态，检查是否到了尝试恢复的时间
                if (shouldAttemptReset()) {
                    transitionToHalfOpen();
                    return true;
                }
                log.debug("断路器处于OPEN状态，拒绝请求");
                return false;

            case HALF_OPEN:
                // 半开状态，允许少量请求尝试
                int attempts = failureCount.get() + successCount.get();
                if (attempts < HALF_OPEN_MAX_ATTEMPTS) {
                    log.debug("断路器处于HALF_OPEN状态，允许尝试请求（{}/{}）",
                        attempts + 1, HALF_OPEN_MAX_ATTEMPTS);
                    return true;
                }
                log.debug("断路器HALF_OPEN状态尝试次数已达上限，拒绝请求");
                return false;

            default:
                return false;
        }
    }

    /**
     * 记录成功调用
     */
    public synchronized void recordSuccess() {
        switch (state) {
            case CLOSED:
                // 关闭状态，重置失败计数
                failureCount.set(0);
                break;

            case HALF_OPEN:
                // 半开状态，累计成功次数
                int successes = successCount.incrementAndGet();
                log.info("断路器HALF_OPEN状态记录成功（{}/{}）",
                    successes, HALF_OPEN_SUCCESS_THRESHOLD);

                if (successes >= HALF_OPEN_SUCCESS_THRESHOLD) {
                    // 达到成功阈值，恢复到关闭状态
                    transitionToClosed();
                }
                break;

            case OPEN:
                // 打开状态不应该有成功记录，但如果有，说明正在恢复
                log.warn("断路器在OPEN状态收到成功记录，忽略");
                break;
        }
    }

    /**
     * 记录失败调用
     */
    public synchronized void recordFailure() {
        switch (state) {
            case CLOSED:
                // 关闭状态，累计失败次数
                int failures = failureCount.incrementAndGet();
                log.warn("断路器记录失败（{}/{}）", failures, FAILURE_THRESHOLD);

                if (failures >= FAILURE_THRESHOLD) {
                    // 达到失败阈值，打开断路器
                    transitionToOpen();
                }
                break;

            case HALF_OPEN:
                // 半开状态遇到失败，立即重新打开
                log.warn("断路器HALF_OPEN状态遇到失败，重新打开");
                transitionToOpen();
                break;

            case OPEN:
                // 打开状态继续记录失败（但不影响状态）
                log.debug("断路器已处于OPEN状态，继续熔断");
                break;
        }
    }

    /**
     * 转换到关闭状态
     */
    private void transitionToClosed() {
        state = State.CLOSED;
        failureCount.set(0);
        successCount.set(0);
        openedTime = null;
        log.info("⚡ AI服务断路器已关闭，服务恢复正常");
    }

    /**
     * 转换到打开状态
     */
    private void transitionToOpen() {
        state = State.OPEN;
        openedTime = LocalDateTime.now();
        failureCount.set(0);
        successCount.set(0);
        log.error("⚠️ AI服务断路器已打开，暂停调用（持续{}分钟）",
            OPEN_DURATION_MILLIS / 60000);
    }

    /**
     * 转换到半开状态
     */
    private void transitionToHalfOpen() {
        state = State.HALF_OPEN;
        failureCount.set(0);
        successCount.set(0);
        log.info("🔄 AI服务断路器进入HALF_OPEN状态，尝试恢复");
    }

    /**
     * 检查是否应该尝试重置
     */
    private boolean shouldAttemptReset() {
        if (openedTime == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long millisSinceOpened = java.time.Duration.between(openedTime, now).toMillis();

        return millisSinceOpened >= OPEN_DURATION_MILLIS;
    }

    /**
     * 获取当前状态
     */
    public String getState() {
        return state.name();
    }

    /**
     * 获取失败次数
     */
    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * 手动重置断路器（用于管理操作）
     */
    public synchronized void reset() {
        log.info("手动重置AI服务断路器");
        transitionToClosed();
    }

    /**
     * 手动打开断路器（用于维护）
     */
    public synchronized void open() {
        log.info("手动打开AI服务断路器");
        transitionToOpen();
    }

    /**
     * 获取断路器统计信息
     */
    public CircuitBreakerStats getStats() {
        return new CircuitBreakerStats(
            state.name(),
            failureCount.get(),
            successCount.get(),
            openedTime != null ? openedTime.toString() : null,
            FAILURE_THRESHOLD,
            HALF_OPEN_SUCCESS_THRESHOLD
        );
    }

    /**
     * 断路器统计信息
     */
    public static class CircuitBreakerStats {
        private final String state;
        private final int failureCount;
        private final int successCount;
        private final String openedTime;
        private final int failureThreshold;
        private final int successThreshold;

        public CircuitBreakerStats(String state, int failureCount, int successCount,
                                   String openedTime, int failureThreshold, int successThreshold) {
            this.state = state;
            this.failureCount = failureCount;
            this.successCount = successCount;
            this.openedTime = openedTime;
            this.failureThreshold = failureThreshold;
            this.successThreshold = successThreshold;
        }

        // Getters
        public String getState() { return state; }
        public int getFailureCount() { return failureCount; }
        public int getSuccessCount() { return successCount; }
        public String getOpenedTime() { return openedTime; }
        public int getFailureThreshold() { return failureThreshold; }
        public int getSuccessThreshold() { return successThreshold; }
    }
}
