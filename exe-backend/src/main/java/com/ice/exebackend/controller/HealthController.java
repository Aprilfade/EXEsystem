package com.ice.exebackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 健康检查控制器
 *
 * 功能：
 * 1. 提供系统健康检查接口
 * 2. 检测数据库连接状态
 * 3. 检测Redis连接状态
 * 4. 监控JVM内存使用情况
 * 5. 监控磁盘空间使用情况
 *
 * @author Ice
 * @date 2026-01-12
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 基础健康检查（仅返回UP/DOWN状态）
     * 用于负载均衡器和容器编排工具
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        boolean isHealthy = true;

        try {
            // 检查数据库
            boolean dbHealthy = checkDatabase();
            health.put("database", dbHealthy ? "UP" : "DOWN");
            if (!dbHealthy) isHealthy = false;

            // 检查Redis
            boolean redisHealthy = checkRedis();
            health.put("redis", redisHealthy ? "UP" : "DOWN");
            if (!redisHealthy) isHealthy = false;

            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return isHealthy
                    ? ResponseEntity.ok(health)
                    : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);

        } catch (Exception e) {
            logger.error("健康检查异常", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    /**
     * 详细健康检查（包含资源使用情况）
     * 用于监控系统和运维人员
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        boolean isHealthy = true;

        try {
            // 数据库检查
            Map<String, Object> dbStatus = checkDatabaseDetailed();
            health.put("database", dbStatus);
            if (!"UP".equals(dbStatus.get("status"))) {
                isHealthy = false;
            }

            // Redis检查
            Map<String, Object> redisStatus = checkRedisDetailed();
            health.put("redis", redisStatus);
            if (!"UP".equals(redisStatus.get("status"))) {
                isHealthy = false;
            }

            // JVM内存检查
            Map<String, Object> memoryStatus = checkMemory();
            health.put("memory", memoryStatus);

            // 磁盘空间检查
            Map<String, Object> diskStatus = checkDiskSpace();
            health.put("disk", diskStatus);

            // 系统信息
            Map<String, Object> systemInfo = getSystemInfo();
            health.put("system", systemInfo);

            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return isHealthy
                    ? ResponseEntity.ok(health)
                    : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);

        } catch (Exception e) {
            logger.error("详细健康检查异常", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    /**
     * 数据库连接检查
     */
    private boolean checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            logger.error("数据库健康检查失败", e);
            return false;
        }
    }

    /**
     * 数据库详细检查
     */
    private Map<String, Object> checkDatabaseDetailed() {
        Map<String, Object> status = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;

            status.put("status", "UP");
            status.put("responseTime", responseTime + "ms");

            // 获取数据库版本信息
            try {
                String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
                status.put("version", version);
            } catch (Exception e) {
                logger.debug("无法获取数据库版本", e);
            }

        } catch (Exception e) {
            logger.error("数据库详细检查失败", e);
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * Redis连接检查
     */
    private boolean checkRedis() {
        try {
            String pong = redisTemplate.execute(connection -> {
                return connection.ping();
            }, false);
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            logger.error("Redis健康检查失败", e);
            return false;
        }
    }

    /**
     * Redis详细检查
     */
    private Map<String, Object> checkRedisDetailed() {
        Map<String, Object> status = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String pong = redisTemplate.execute(connection -> {
                return connection.ping();
            }, false);

            long responseTime = System.currentTimeMillis() - startTime;

            if ("PONG".equalsIgnoreCase(pong)) {
                status.put("status", "UP");
                status.put("responseTime", responseTime + "ms");

                // 获取Redis信息
                try {
                    Object dbSize = redisTemplate.execute(connection -> {
                        return connection.dbSize();
                    }, false);
                    status.put("keysCount", dbSize);
                } catch (Exception e) {
                    logger.debug("无法获取Redis键数量", e);
                }
            } else {
                status.put("status", "DOWN");
            }

        } catch (Exception e) {
            logger.error("Redis详细检查失败", e);
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * JVM内存检查
     */
    private Map<String, Object> checkMemory() {
        Map<String, Object> memoryInfo = new HashMap<>();

        try {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

            // 堆内存
            MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
            Map<String, String> heap = new HashMap<>();
            heap.put("used", formatBytes(heapMemory.getUsed()));
            heap.put("committed", formatBytes(heapMemory.getCommitted()));
            heap.put("max", formatBytes(heapMemory.getMax()));
            heap.put("usage", String.format("%.2f%%", (double) heapMemory.getUsed() / heapMemory.getMax() * 100));
            memoryInfo.put("heap", heap);

            // 非堆内存
            MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();
            Map<String, String> nonHeap = new HashMap<>();
            nonHeap.put("used", formatBytes(nonHeapMemory.getUsed()));
            nonHeap.put("committed", formatBytes(nonHeapMemory.getCommitted()));
            nonHeap.put("max", nonHeapMemory.getMax() > 0 ? formatBytes(nonHeapMemory.getMax()) : "N/A");
            memoryInfo.put("nonHeap", nonHeap);

            // 内存警告判断
            double heapUsagePercent = (double) heapMemory.getUsed() / heapMemory.getMax() * 100;
            if (heapUsagePercent > 90) {
                memoryInfo.put("warning", "堆内存使用率超过90%，建议关注");
            } else if (heapUsagePercent > 80) {
                memoryInfo.put("warning", "堆内存使用率超过80%");
            }

        } catch (Exception e) {
            logger.error("内存检查失败", e);
            memoryInfo.put("error", e.getMessage());
        }

        return memoryInfo;
    }

    /**
     * 磁盘空间检查
     */
    private Map<String, Object> checkDiskSpace() {
        Map<String, Object> diskInfo = new HashMap<>();

        try {
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usableSpace = root.getUsableSpace();
            long usedSpace = totalSpace - freeSpace;

            diskInfo.put("total", formatBytes(totalSpace));
            diskInfo.put("used", formatBytes(usedSpace));
            diskInfo.put("free", formatBytes(freeSpace));
            diskInfo.put("usable", formatBytes(usableSpace));
            diskInfo.put("usage", String.format("%.2f%%", (double) usedSpace / totalSpace * 100));

            // 磁盘空间警告
            double usagePercent = (double) usedSpace / totalSpace * 100;
            if (usagePercent > 90) {
                diskInfo.put("warning", "磁盘空间使用率超过90%，请及时清理");
            } else if (usagePercent > 80) {
                diskInfo.put("warning", "磁盘空间使用率超过80%");
            }

        } catch (Exception e) {
            logger.error("磁盘空间检查失败", e);
            diskInfo.put("error", e.getMessage());
        }

        return diskInfo;
    }

    /**
     * 系统信息
     */
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();

            systemInfo.put("processors", runtime.availableProcessors());
            systemInfo.put("jvmName", System.getProperty("java.vm.name"));
            systemInfo.put("jvmVersion", System.getProperty("java.version"));
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("osVersion", System.getProperty("os.version"));
            systemInfo.put("osArch", System.getProperty("os.arch"));

            // JVM运行时长
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            systemInfo.put("uptime", formatDuration(uptime));

        } catch (Exception e) {
            logger.error("获取系统信息失败", e);
            systemInfo.put("error", e.getMessage());
        }

        return systemInfo;
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 格式化时长
     */
    private String formatDuration(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
