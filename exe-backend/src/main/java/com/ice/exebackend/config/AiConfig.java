package com.ice.exebackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 功能配置类
 * 支持多AI提供商配置
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /**
     * 是否启用AI功能
     */
    private boolean enabled = true;

    /**
     * 默认AI提供商
     */
    private String defaultProvider = "deepseek";

    /**
     * AI提供商配置
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();

    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * 超时配置
     */
    private TimeoutConfig timeout = new TimeoutConfig();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public Map<String, ProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, ProviderConfig> providers) {
        this.providers = providers;
    }

    public CacheConfig getCache() {
        return cache;
    }

    public void setCache(CacheConfig cache) {
        this.cache = cache;
    }

    public RetryConfig getRetry() {
        return retry;
    }

    public void setRetry(RetryConfig retry) {
        this.retry = retry;
    }

    public RateLimitConfig getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitConfig rateLimit) {
        this.rateLimit = rateLimit;
    }

    public TimeoutConfig getTimeout() {
        return timeout;
    }

    public void setTimeout(TimeoutConfig timeout) {
        this.timeout = timeout;
    }

    /**
     * AI提供商配置
     */
    public static class ProviderConfig {
        /**
         * API端点URL
         */
        private String url;

        /**
         * 模型名称
         */
        private String model;

        /**
         * 默认温度参数
         */
        private Double temperature = 0.7;

        /**
         * 最大tokens
         */
        private Integer maxTokens = 2000;

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 优先级（数字越小优先级越高）
         */
        private int priority = 999;

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }

    /**
     * 缓存配置
     */
    public static class CacheConfig {
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;

        /**
         * 缓存过期时间（秒）
         */
        private long ttl = 3600;

        /**
         * 缓存key前缀
         */
        private String keyPrefix = "ai:cache:";

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getTtl() {
            return ttl;
        }

        public void setTtl(long ttl) {
            this.ttl = ttl;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }

    /**
     * 重试配置
     */
    public static class RetryConfig {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;

        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;

        /**
         * 初始退避时间（毫秒）
         */
        private long backoff = 1000;

        /**
         * 退避倍数
         */
        private double multiplier = 2.0;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getBackoff() {
            return backoff;
        }

        public void setBackoff(long backoff) {
            this.backoff = backoff;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    /**
     * 限流配置
     */
    public static class RateLimitConfig {
        /**
         * 是否启用限流
         */
        private boolean enabled = true;

        /**
         * 全局最大并发数
         */
        private int maxConcurrent = 10;

        /**
         * 单用户最大并发数
         */
        private int maxPerUser = 2;

        /**
         * 每分钟最大请求数（全局）
         */
        private int maxPerMinute = 100;

        /**
         * 每分钟最大请求数（单用户）
         */
        private int maxPerUserPerMinute = 10;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxConcurrent() {
            return maxConcurrent;
        }

        public void setMaxConcurrent(int maxConcurrent) {
            this.maxConcurrent = maxConcurrent;
        }

        public int getMaxPerUser() {
            return maxPerUser;
        }

        public void setMaxPerUser(int maxPerUser) {
            this.maxPerUser = maxPerUser;
        }

        public int getMaxPerMinute() {
            return maxPerMinute;
        }

        public void setMaxPerMinute(int maxPerMinute) {
            this.maxPerMinute = maxPerMinute;
        }

        public int getMaxPerUserPerMinute() {
            return maxPerUserPerMinute;
        }

        public void setMaxPerUserPerMinute(int maxPerUserPerMinute) {
            this.maxPerUserPerMinute = maxPerUserPerMinute;
        }
    }

    /**
     * 超时配置
     */
    public static class TimeoutConfig {
        /**
         * 连接超时（秒）
         */
        private int connect = 10;

        /**
         * 错题分析超时（秒）
         */
        private int analyze = 30;

        /**
         * 智能出题超时（秒）
         */
        private int generate = 60;

        /**
         * 主观题批改超时（秒）
         */
        private int grading = 30;

        /**
         * 知识点提取超时（秒）
         */
        private int extract = 30;

        // Getters and Setters
        public int getConnect() {
            return connect;
        }

        public void setConnect(int connect) {
            this.connect = connect;
        }

        public int getAnalyze() {
            return analyze;
        }

        public void setAnalyze(int analyze) {
            this.analyze = analyze;
        }

        public int getGenerate() {
            return generate;
        }

        public void setGenerate(int generate) {
            this.generate = generate;
        }

        public int getGrading() {
            return grading;
        }

        public void setGrading(int grading) {
            this.grading = grading;
        }

        public int getExtract() {
            return extract;
        }

        public void setExtract(int extract) {
            this.extract = extract;
        }
    }

    /**
     * 获取指定提供商的配置
     */
    public ProviderConfig getProviderConfig(String provider) {
        return providers.getOrDefault(
                provider != null ? provider.toLowerCase() : defaultProvider,
                providers.get(defaultProvider)
        );
    }
}
