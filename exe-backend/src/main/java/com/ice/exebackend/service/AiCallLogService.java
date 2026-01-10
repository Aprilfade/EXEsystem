package com.ice.exebackend.service;

import com.ice.exebackend.entity.BizAiCallLog;
import com.ice.exebackend.mapper.BizAiCallLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI调用统计服务
 * 用于记录和分析AI功能使用情况
 */
@Service
public class AiCallLogService {

    private static final Logger log = LoggerFactory.getLogger(AiCallLogService.class);

    @Autowired(required = false)
    private BizAiCallLogMapper aiCallLogMapper;

    /**
     * 异步记录AI调用日志
     *
     * @param userId         用户ID
     * @param userType       用户类型
     * @param functionType   功能类型
     * @param provider       AI提供商
     * @param success        是否成功
     * @param responseTime   响应时间
     * @param cached         是否缓存
     * @param retryCount     重试次数
     * @param errorMessage   错误信息
     * @param requestSummary 请求摘要
     */
    @Async
    public void logAsync(Long userId, String userType, String functionType, String provider,
                         boolean success, long responseTime, boolean cached, int retryCount,
                         String errorMessage, String requestSummary) {
        if (aiCallLogMapper == null) {
            return;  // 如果表不存在，跳过
        }

        try {
            BizAiCallLog log = new BizAiCallLog();
            log.setUserId(userId);
            log.setUserType(userType);
            log.setFunctionType(functionType);
            log.setProvider(provider);
            log.setSuccess(success);
            log.setResponseTime(responseTime);
            log.setCached(cached);
            log.setRetryCount(retryCount);
            log.setErrorMessage(errorMessage);
            log.setRequestSummary(requestSummary);
            log.setEstimatedCost(estimateCost(functionType, provider, cached));
            log.setCreateTime(LocalDateTime.now());

            aiCallLogMapper.insert(log);
        } catch (Exception e) {
            log.error("记录AI调用日志失败", e);
        }
    }

    /**
     * 估算成本
     */
    private Double estimateCost(String functionType, String provider, boolean cached) {
        if (cached) {
            return 0.0;  // 缓存命中无成本
        }

        // 简单估算：根据功能类型和提供商
        double baseCost = switch (provider) {
            case "deepseek" -> 0.001;  // DeepSeek较便宜
            case "qwen" -> 0.002;       // 通义千问
            default -> 0.01;
        };

        // 根据功能类型调整
        return switch (functionType) {
            case "analyze" -> baseCost * 1.0;
            case "generate" -> baseCost * 3.0;  // 生成题目更复杂
            case "grading" -> baseCost * 1.5;
            case "extract" -> baseCost * 2.0;
            default -> baseCost;
        };
    }

    /**
     * 获取统计数据
     *
     * @param days 统计天数
     * @return 统计结果
     */
    public Map<String, Object> getStats(int days) {
        if (aiCallLogMapper == null) {
            return new HashMap<>();
        }

        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        Map<String, Object> stats = new HashMap<>();

        try {
            // 成功率
            Map<String, Object> successRate = aiCallLogMapper.getSuccessRate(startTime);
            if (successRate != null) {
                long total = ((Number) successRate.getOrDefault("total", 0)).longValue();
                long successCount = ((Number) successRate.getOrDefault("success_count", 0)).longValue();
                stats.put("totalCalls", total);
                stats.put("successRate", total > 0 ? (double) successCount / total * 100 : 0);
            }

            // 各功能使用情况
            List<Map<String, Object>> functionStats = aiCallLogMapper.getFunctionStats(startTime);
            stats.put("functionStats", functionStats);

            // 总成本
            Double totalCost = aiCallLogMapper.getTotalCost(startTime);
            stats.put("totalCost", totalCost != null ? totalCost : 0.0);

            // 缓存命中率
            Map<String, Object> cacheStats = aiCallLogMapper.getCacheHitRate(startTime);
            if (cacheStats != null) {
                long total = ((Number) cacheStats.getOrDefault("total", 0)).longValue();
                long cachedCount = ((Number) cacheStats.getOrDefault("cached_count", 0)).longValue();
                stats.put("cacheHitRate", total > 0 ? (double) cachedCount / total * 100 : 0);
            }

        } catch (Exception e) {
            log.error("获取AI统计数据失败", e);
        }

        return stats;
    }

    /**
     * 获取用户使用统计
     *
     * @param userId 用户ID
     * @param days   统计天数
     * @return 统计结果
     */
    public Map<String, Object> getUserStats(Long userId, int days) {
        if (aiCallLogMapper == null) {
            return new HashMap<>();
        }

        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        Map<String, Object> stats = new HashMap<>();

        try {
            Integer callCount = aiCallLogMapper.countUserCalls(userId, startTime);
            stats.put("callCount", callCount != null ? callCount : 0);
        } catch (Exception e) {
            log.error("获取用户AI统计数据失败", e);
        }

        return stats;
    }

    /**
     * 获取统计数据（别名方法，用于告警服务）
     *
     * @param days 统计天数
     * @return 统计结果
     */
    public Map<String, Object> getStatistics(int days) {
        return getStats(days);
    }

    /**
     * 获取最近N分钟的统计数据（用于实时监控）
     *
     * @param minutes 分钟数
     * @return 统计结果
     */
    public Map<String, Object> getRecentStats(int minutes) {
        if (aiCallLogMapper == null) {
            return new HashMap<>();
        }

        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        Map<String, Object> stats = new HashMap<>();

        try {
            // 总调用次数和成功率
            Map<String, Object> successRate = aiCallLogMapper.getSuccessRate(startTime);
            if (successRate != null) {
                long total = ((Number) successRate.getOrDefault("total", 0)).longValue();
                long successCount = ((Number) successRate.getOrDefault("success_count", 0)).longValue();
                stats.put("totalCalls", total);
                stats.put("successCount", successCount);
                stats.put("errorCount", total - successCount);
                stats.put("successRate", total > 0 ? (double) successCount / total : 1.0);
            } else {
                stats.put("totalCalls", 0);
                stats.put("successCount", 0);
                stats.put("errorCount", 0);
                stats.put("successRate", 1.0);
            }

            // 总成本
            Double totalCost = aiCallLogMapper.getTotalCost(startTime);
            stats.put("totalCost", totalCost != null ? totalCost : 0.0);

            // 平均响应时间
            Double avgResponseTime = aiCallLogMapper.getAvgResponseTime(startTime);
            stats.put("avgResponseTime", avgResponseTime != null ? avgResponseTime.longValue() : 0L);

        } catch (Exception e) {
            log.error("获取最近统计数据失败", e);
        }

        return stats;
    }
}
