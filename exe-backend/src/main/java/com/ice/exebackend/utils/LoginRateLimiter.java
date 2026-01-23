package com.ice.exebackend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录限流器 - 防止暴力破解
 *
 * 功能：
 * 1. 记录登录失败次数
 * 2. 超过阈值后锁定账户
 * 3. 基于Redis实现分布式限流
 *
 * @author Ice
 * @date 2026-01-12
 */
@Component
public class LoginRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimiter.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 最大失败尝试次数
     */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * 锁定时长（分钟）
     */
    private static final int LOCK_MINUTES = 30;

    /**
     * 失败记录过期时间（分钟）
     */
    private static final int FAIL_RECORD_EXPIRE_MINUTES = 15;

    /**
     * Redis Key前缀 - 失败次数
     */
    private static final String KEY_PREFIX_FAIL = "login:fail:";

    /**
     * Redis Key前缀 - 账户锁定
     */
    private static final String KEY_PREFIX_LOCK = "login:lock:";

    /**
     * 检查账户是否被锁定
     *
     * @param username 用户名
     * @return true=已锁定，false=未锁定
     */
    public boolean isLocked(String username) {
        String lockKey = KEY_PREFIX_LOCK + username;
        Boolean hasKey = redisTemplate.hasKey(lockKey);
        return Boolean.TRUE.equals(hasKey);
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     */
    public void recordFailure(String username) {
        String key = KEY_PREFIX_FAIL + username;

        // 增加失败次数
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == null) {
            attempts = 1L;
        }

        // 首次失败时设置过期时间
        if (attempts == 1) {
            redisTemplate.expire(key, FAIL_RECORD_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        logger.debug("登录失败记录: username={}, attempts={}", username, attempts);

        // 超过最大次数，锁定账户
        if (attempts >= MAX_ATTEMPTS) {
            lockAccount(username);
        }
    }

    /**
     * 锁定账户
     *
     * @param username 用户名
     */
    private void lockAccount(String username) {
        String lockKey = KEY_PREFIX_LOCK + username;
        redisTemplate.opsForValue().set(lockKey, true, LOCK_MINUTES, TimeUnit.MINUTES);

        logger.warn("账户已被锁定: username={}, lockMinutes={}", username, LOCK_MINUTES);
    }

    /**
     * 清除失败记录（登录成功时调用）
     *
     * @param username 用户名
     */
    public void clearFailures(String username) {
        String failKey = KEY_PREFIX_FAIL + username;
        redisTemplate.delete(failKey);

        logger.debug("清除失败记录: username={}", username);
    }

    /**
     * 获取剩余尝试次数
     *
     * @param username 用户名
     * @return 剩余次数
     */
    public int getRemainingAttempts(String username) {
        // 如果已锁定，返回0
        if (isLocked(username)) {
            return 0;
        }

        String key = KEY_PREFIX_FAIL + username;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            return MAX_ATTEMPTS;
        }

        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    /**
     * 获取锁定剩余时间（秒）
     *
     * @param username 用户名
     * @return 剩余秒数，-1表示未锁定
     */
    public long getLockRemainingSeconds(String username) {
        if (!isLocked(username)) {
            return -1;
        }

        String lockKey = KEY_PREFIX_LOCK + username;
        Long expire = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);

        return expire != null ? expire : 0;
    }
}
