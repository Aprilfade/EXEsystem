package com.ice.exebackend.service;

import com.ice.exebackend.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * AI 限流器
 * 实现多级限流：全局并发、用户并发、速率限制
 */
@Component
public class AiRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(AiRateLimiter.class);

    @Autowired
    private AiConfig aiConfig;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    // 全局并发控制
    private Semaphore globalConcurrentSemaphore;

    // Lua脚本：基于滑动窗口的限流
    private static final String RATE_LIMIT_SCRIPT =
            "local key = KEYS[1]\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local current = tonumber(redis.call('GET', key) or '0')\n" +
            "if current < limit then\n" +
            "  redis.call('INCR', key)\n" +
            "  if current == 0 then\n" +
            "    redis.call('EXPIRE', key, window)\n" +
            "  end\n" +
            "  return 1\n" +
            "else\n" +
            "  return 0\n" +
            "end";

    public AiRateLimiter() {
        // 初始化时会在配置加载后调用init方法
    }

    @Autowired
    public void init(AiConfig aiConfig) {
        if (aiConfig.getRateLimit().isEnabled()) {
            this.globalConcurrentSemaphore = new Semaphore(aiConfig.getRateLimit().getMaxConcurrent());
            log.info("AI限流器初始化完成: 全局并发={}", aiConfig.getRateLimit().getMaxConcurrent());
        }
    }

    /**
     * 尝试获取全局并发许可
     *
     * @return 是否获取成功
     */
    public boolean tryAcquireGlobalConcurrent() {
        if (!aiConfig.getRateLimit().isEnabled()) {
            return true;
        }

        boolean acquired = globalConcurrentSemaphore.tryAcquire();
        if (!acquired) {
            log.warn("全局并发限制：当前并发已达上限 {}", aiConfig.getRateLimit().getMaxConcurrent());
        }
        return acquired;
    }

    /**
     * 释放全局并发许可
     */
    public void releaseGlobalConcurrent() {
        if (aiConfig.getRateLimit().isEnabled()) {
            globalConcurrentSemaphore.release();
        }
    }

    /**
     * 检查用户速率限制（每分钟）
     *
     * @param userId 用户ID
     * @return 是否允许请求
     */
    public boolean checkUserRateLimit(Long userId) {
        if (!aiConfig.getRateLimit().isEnabled() || redisTemplate == null) {
            return true;
        }

        String key = "ai:ratelimit:user:" + userId;
        int maxPerMinute = aiConfig.getRateLimit().getMaxPerUserPerMinute();

        try {
            Long result = redisTemplate.execute(
                    RedisScript.of(RATE_LIMIT_SCRIPT, Long.class),
                    Collections.singletonList(key),
                    String.valueOf(maxPerMinute),
                    "60"  // 60秒窗口
            );

            if (result != null && result == 1) {
                return true;
            } else {
                log.warn("用户速率限制：用户 {} 超过每分钟 {} 次限制", userId, maxPerMinute);
                return false;
            }
        } catch (Exception e) {
            log.error("检查用户速率限制失败，允许通过", e);
            return true;  // 降级：出错时允许通过
        }
    }

    /**
     * 检查全局速率限制（每分钟）
     *
     * @return 是否允许请求
     */
    public boolean checkGlobalRateLimit() {
        if (!aiConfig.getRateLimit().isEnabled() || redisTemplate == null) {
            return true;
        }

        String key = "ai:ratelimit:global";
        int maxPerMinute = aiConfig.getRateLimit().getMaxPerMinute();

        try {
            Long result = redisTemplate.execute(
                    RedisScript.of(RATE_LIMIT_SCRIPT, Long.class),
                    Collections.singletonList(key),
                    String.valueOf(maxPerMinute),
                    "60"
            );

            if (result != null && result == 1) {
                return true;
            } else {
                log.warn("全局速率限制：超过每分钟 {} 次限制", maxPerMinute);
                return false;
            }
        } catch (Exception e) {
            log.error("检查全局速率限制失败，允许通过", e);
            return true;
        }
    }

    /**
     * 获取用户当前速率使用情况
     *
     * @param userId 用户ID
     * @return 当前使用次数
     */
    public int getUserCurrentRate(Long userId) {
        if (redisTemplate == null) {
            return 0;
        }

        try {
            String key = "ai:ratelimit:user:" + userId;
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            log.error("获取用户速率使用情况失败", e);
            return 0;
        }
    }

    /**
     * 获取剩余配额
     *
     * @param userId 用户ID
     * @return 剩余次数
     */
    public int getRemainingQuota(Long userId) {
        int current = getUserCurrentRate(userId);
        return Math.max(0, aiConfig.getRateLimit().getMaxPerUserPerMinute() - current);
    }
}
