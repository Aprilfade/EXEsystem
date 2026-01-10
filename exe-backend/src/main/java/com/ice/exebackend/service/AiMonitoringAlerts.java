package com.ice.exebackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * AI 监控告警服务
 * 定时检查AI调用指标，发现异常时触发告警
 */
@Service
public class AiMonitoringAlerts {

    private static final Logger log = LoggerFactory.getLogger(AiMonitoringAlerts.class);

    @Autowired
    private AiCallLogService aiCallLogService;

    // 告警阈值配置
    private static final double MIN_SUCCESS_RATE = 0.9;        // 最低成功率：90%
    private static final BigDecimal MAX_HOURLY_COST = new BigDecimal("100.00");  // 最大小时成本：100元
    private static final BigDecimal MAX_DAILY_COST = new BigDecimal("500.00");   // 最大每日成本：500元
    private static final long MAX_AVG_RESPONSE_TIME = 30000;   // 最大平均响应时间：30秒
    private static final int MAX_ERROR_COUNT = 10;             // 最大连续错误次数：10次

    // 告警状态（防止重复告警）
    private boolean lowSuccessRateAlerted = false;
    private boolean highCostAlerted = false;
    private boolean slowResponseAlerted = false;
    private LocalDateTime lastAlertTime = null;
    private static final long ALERT_COOLDOWN_MINUTES = 30;     // 告警冷却时间：30分钟

    /**
     * 每分钟执行一次指标检查
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void checkMetrics() {
        try {
            // 检查是否在冷却期
            if (isInCooldown()) {
                return;
            }

            log.debug("开始执行AI监控指标检查...");

            // 1. 检查成功率
            checkSuccessRate();

            // 2. 检查成本
            checkCost();

            // 3. 检查响应时间
            checkResponseTime();

            // 4. 检查错误频率
            checkErrorFrequency();

        } catch (Exception e) {
            log.error("AI监控检查异常", e);
        }
    }

    /**
     * 检查成功率
     */
    private void checkSuccessRate() {
        try {
            Map<String, Object> stats = aiCallLogService.getStatistics(1); // 最近1天

            if (stats.containsKey("successRate")) {
                double successRate = ((Number) stats.get("successRate")).doubleValue();

                if (successRate < MIN_SUCCESS_RATE && !lowSuccessRateAlerted) {
                    String message = String.format(
                        "⚠️ AI成功率告警：当前成功率 %.2f%% 低于阈值 %.2f%%",
                        successRate * 100,
                        MIN_SUCCESS_RATE * 100
                    );
                    sendAlert("成功率过低", message);
                    lowSuccessRateAlerted = true;
                } else if (successRate >= MIN_SUCCESS_RATE) {
                    lowSuccessRateAlerted = false; // 恢复正常
                }
            }
        } catch (Exception e) {
            log.error("检查成功率失败", e);
        }
    }

    /**
     * 检查成本
     */
    private void checkCost() {
        try {
            // 检查小时成本
            Map<String, Object> hourlyStats = aiCallLogService.getRecentStats(60); // 最近1小时
            if (hourlyStats.containsKey("totalCost")) {
                BigDecimal hourlyCost = new BigDecimal(hourlyStats.get("totalCost").toString());

                if (hourlyCost.compareTo(MAX_HOURLY_COST) > 0 && !highCostAlerted) {
                    String message = String.format(
                        "⚠️ AI成本告警：最近1小时消耗 %.2f 元，超过阈值 %.2f 元",
                        hourlyCost,
                        MAX_HOURLY_COST
                    );
                    sendAlert("成本异常", message);
                    highCostAlerted = true;
                }
            }

            // 检查每日成本
            Map<String, Object> dailyStats = aiCallLogService.getStatistics(1); // 最近1天
            if (dailyStats.containsKey("totalCost")) {
                BigDecimal dailyCost = new BigDecimal(dailyStats.get("totalCost").toString());

                if (dailyCost.compareTo(MAX_DAILY_COST) > 0 && !highCostAlerted) {
                    String message = String.format(
                        "⚠️ AI成本告警：今日消耗 %.2f 元，超过阈值 %.2f 元",
                        dailyCost,
                        MAX_DAILY_COST
                    );
                    sendAlert("每日成本超标", message);
                    highCostAlerted = true;
                }
            }

            // 如果成本恢复正常（小于80%阈值），重置告警状态
            Map<String, Object> recentStats = aiCallLogService.getRecentStats(10); // 最近10分钟
            if (recentStats.containsKey("totalCost")) {
                BigDecimal recentCost = new BigDecimal(recentStats.get("totalCost").toString());
                BigDecimal scaledCost = recentCost.multiply(new BigDecimal("6")); // 10分钟 * 6 = 1小时

                if (scaledCost.compareTo(MAX_HOURLY_COST.multiply(new BigDecimal("0.8"))) < 0) {
                    highCostAlerted = false;
                }
            }

        } catch (Exception e) {
            log.error("检查成本失败", e);
        }
    }

    /**
     * 检查响应时间
     */
    private void checkResponseTime() {
        try {
            Map<String, Object> stats = aiCallLogService.getRecentStats(30); // 最近30分钟

            if (stats.containsKey("avgResponseTime")) {
                long avgResponseTime = ((Number) stats.get("avgResponseTime")).longValue();

                if (avgResponseTime > MAX_AVG_RESPONSE_TIME && !slowResponseAlerted) {
                    String message = String.format(
                        "⚠️ AI响应时间告警：平均响应时间 %d 毫秒，超过阈值 %d 毫秒",
                        avgResponseTime,
                        MAX_AVG_RESPONSE_TIME
                    );
                    sendAlert("响应时间过长", message);
                    slowResponseAlerted = true;
                } else if (avgResponseTime <= MAX_AVG_RESPONSE_TIME) {
                    slowResponseAlerted = false; // 恢复正常
                }
            }
        } catch (Exception e) {
            log.error("检查响应时间失败", e);
        }
    }

    /**
     * 检查错误频率
     */
    private void checkErrorFrequency() {
        try {
            Map<String, Object> stats = aiCallLogService.getRecentStats(10); // 最近10分钟

            if (stats.containsKey("errorCount")) {
                int errorCount = ((Number) stats.get("errorCount")).intValue();

                if (errorCount >= MAX_ERROR_COUNT) {
                    String message = String.format(
                        "⚠️ AI错误频率告警：最近10分钟内发生 %d 次错误",
                        errorCount
                    );
                    sendAlert("错误频率异常", message);
                }
            }
        } catch (Exception e) {
            log.error("检查错误频率失败", e);
        }
    }

    /**
     * 发送告警
     *
     * @param title   告警标题
     * @param message 告警消息
     */
    private void sendAlert(String title, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String fullMessage = String.format("[%s] %s\n%s", timestamp, title, message);

        // 记录到日志
        log.warn("【AI监控告警】{}", fullMessage);

        // TODO: 集成实际的告警渠道
        // 1. 发送邮件
        // emailService.sendAlert(adminEmail, title, message);

        // 2. 发送企业微信/钉钉通知
        // wechatService.sendAlert(message);

        // 3. 发送短信
        // smsService.sendAlert(adminPhone, message);

        // 4. 写入数据库
        // alertLogService.save(title, message);

        // 更新最后告警时间
        lastAlertTime = LocalDateTime.now();
    }

    /**
     * 检查是否在告警冷却期
     */
    private boolean isInCooldown() {
        if (lastAlertTime == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastAlert = java.time.Duration.between(lastAlertTime, now).toMinutes();

        return minutesSinceLastAlert < ALERT_COOLDOWN_MINUTES;
    }

    /**
     * 手动触发告警检查（用于测试或紧急情况）
     */
    public void manualCheck() {
        log.info("手动触发AI监控检查");
        checkMetrics();
    }

    /**
     * 获取当前告警状态
     */
    public Map<String, Object> getAlertStatus() {
        return Map.of(
            "lowSuccessRateAlerted", lowSuccessRateAlerted,
            "highCostAlerted", highCostAlerted,
            "slowResponseAlerted", slowResponseAlerted,
            "lastAlertTime", lastAlertTime != null ? lastAlertTime.toString() : "无",
            "inCooldown", isInCooldown(),
            "thresholds", Map.of(
                "minSuccessRate", MIN_SUCCESS_RATE,
                "maxHourlyCost", MAX_HOURLY_COST,
                "maxDailyCost", MAX_DAILY_COST,
                "maxAvgResponseTime", MAX_AVG_RESPONSE_TIME,
                "maxErrorCount", MAX_ERROR_COUNT
            )
        );
    }

    /**
     * 重置所有告警状态
     */
    public void resetAlertStatus() {
        lowSuccessRateAlerted = false;
        highCostAlerted = false;
        slowResponseAlerted = false;
        lastAlertTime = null;
        log.info("已重置所有AI告警状态");
    }
}
