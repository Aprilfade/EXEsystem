package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizAiCallLog;
import com.ice.exebackend.mapper.BizAiCallLogMapper;
import com.ice.exebackend.service.AiCallLogService;
import com.ice.exebackend.service.AiRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 监控管理接口（增强版）
 * 提供AI使用统计、限流状态、详细日志查询等监控功能
 */
@Tag(name = "AI监控管理", description = "AI功能监控和统计接口")
@RestController
@RequestMapping("/api/v1/ai-monitor")
@PreAuthorize("hasAuthority('sys:stats:list')")  // 需要统计权限
public class AiMonitorControllerV2 {

    @Autowired
    private AiCallLogService aiCallLogService;

    @Autowired
    private AiRateLimiter rateLimiter;

    @Autowired(required = false)
    private BizAiCallLogMapper aiCallLogMapper;

    /**
     * 分页查询AI调用日志（支持多条件筛选）
     *
     * @param current      当前页
     * @param size         每页大小
     * @param userId       用户ID（可选）
     * @param userType     用户类型（可选）
     * @param functionType 功能类型（可选）
     * @param provider     AI提供商（可选）
     * @param success      是否成功（可选）
     * @param startTime    开始时间（可选）
     * @param endTime      结束时间（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询AI调用日志", description = "支持多条件筛选的AI调用日志查询")
    @GetMapping("/logs")
    public Result getLogs(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) Long userId,
                          @RequestParam(required = false) String userType,
                          @RequestParam(required = false) String functionType,
                          @RequestParam(required = false) String provider,
                          @RequestParam(required = false) Boolean success,
                          @RequestParam(required = false) String startTime,
                          @RequestParam(required = false) String endTime) {

        if (aiCallLogMapper == null) {
            return Result.fail("AI日志功能未启用");
        }

        Page<BizAiCallLog> page = new Page<>(current, size);
        QueryWrapper<BizAiCallLog> wrapper = new QueryWrapper<>();

        // 构建查询条件
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (userType != null && !userType.isEmpty()) {
            wrapper.eq("user_type", userType);
        }
        if (functionType != null && !functionType.isEmpty()) {
            wrapper.eq("function_type", functionType);
        }
        if (provider != null && !provider.isEmpty()) {
            wrapper.eq("provider", provider);
        }
        if (success != null) {
            wrapper.eq("success", success);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge("create_time", LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le("create_time", LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME));
        }

        // 按时间倒序排列
        wrapper.orderByDesc("create_time");

        Page<BizAiCallLog> result = aiCallLogMapper.selectPage(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    /**
     * 获取AI使用统计
     *
     * @param days 统计天数（默认7天）
     * @return 统计数据
     */
    @Operation(summary = "获取AI使用统计", description = "获取指定天数内的AI调用统计数据")
    @GetMapping("/stats")
    public Result getStats(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> stats = aiCallLogService.getStats(days);
        return Result.suc(stats);
    }

    /**
     * 获取用户AI使用统计
     *
     * @param userId 用户ID
     * @param days   统计天数
     * @return 统计数据
     */
    @Operation(summary = "获取用户AI使用统计", description = "获取指定用户的AI调用统计")
    @GetMapping("/user-stats/{userId}")
    public Result getUserStats(@PathVariable Long userId,
                                @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> stats = aiCallLogService.getUserStats(userId, days);
        return Result.suc(stats);
    }

    /**
     * 获取用户剩余配额
     *
     * @param userId 用户ID
     * @return 剩余配额信息
     */
    @Operation(summary = "获取用户剩余配额", description = "查询用户当前剩余的AI调用配额")
    @GetMapping("/quota/{userId}")
    @PreAuthorize("permitAll()")  // 用户自己也可以查询
    public Result getUserQuota(@PathVariable Long userId) {
        Map<String, Object> quota = new HashMap<>();
        quota.put("remaining", rateLimiter.getRemainingQuota(userId));
        quota.put("currentUsage", rateLimiter.getUserCurrentRate(userId));
        return Result.suc(quota);
    }

    /**
     * 获取AI调用趋势（按天统计）
     *
     * @param days 统计天数
     * @return 趋势数据
     */
    @Operation(summary = "获取AI调用趋势", description = "获取最近N天的AI调用趋势")
    @GetMapping("/trend")
    public Result getTrend(@RequestParam(defaultValue = "30") int days) {
        if (aiCallLogMapper == null) {
            return Result.fail("AI日志功能未启用");
        }

        // 使用自定义查询获取趋势数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        // 简单实现：返回按天分组的统计
        // 实际项目中可以在Mapper中添加自定义SQL
        Map<String, Object> trend = new HashMap<>();
        trend.put("message", "趋势数据需要在Mapper中添加自定义SQL查询");

        return Result.suc(trend);
    }

    /**
     * 获取Top用户排行
     *
     * @param days 统计天数
     * @param limit 返回数量
     * @return 用户排行
     */
    @Operation(summary = "获取Top用户排行", description = "获取AI调用次数最多的用户")
    @GetMapping("/top-users")
    public Result getTopUsers(@RequestParam(defaultValue = "7") int days,
                               @RequestParam(defaultValue = "10") int limit) {
        if (aiCallLogMapper == null) {
            return Result.fail("AI日志功能未启用");
        }

        // 这里需要在Mapper中添加自定义SQL
        Map<String, Object> result = new HashMap<>();
        result.put("message", "需要在Mapper中添加GROUP BY查询");

        return Result.suc(result);
    }

    /**
     * 获取错误日志
     *
     * @param current 当前页
     * @param size    每页大小
     * @param days    统计天数
     * @return 错误日志
     */
    @Operation(summary = "获取错误日志", description = "获取AI调用失败的日志")
    @GetMapping("/errors")
    public Result getErrors(@RequestParam(defaultValue = "1") int current,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(defaultValue = "7") int days) {
        if (aiCallLogMapper == null) {
            return Result.fail("AI日志功能未启用");
        }

        Page<BizAiCallLog> page = new Page<>(current, size);
        QueryWrapper<BizAiCallLog> wrapper = new QueryWrapper<>();

        wrapper.eq("success", false);
        wrapper.ge("create_time", LocalDateTime.now().minusDays(days));
        wrapper.orderByDesc("create_time");

        Page<BizAiCallLog> result = aiCallLogMapper.selectPage(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    /**
     * 获取日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @Operation(summary = "获取日志详情", description = "获取单条AI调用日志的详细信息")
    @GetMapping("/logs/{id}")
    public Result getLogDetail(@PathVariable Long id) {
        if (aiCallLogMapper == null) {
            return Result.fail("AI日志功能未启用");
        }

        BizAiCallLog log = aiCallLogMapper.selectById(id);
        if (log == null) {
            return Result.fail("日志不存在");
        }

        return Result.suc(log);
    }

    /**
     * 健康检查
     *
     * @return 服务状态
     */
    @Operation(summary = "AI服务健康检查", description = "检查AI服务各组件状态")
    @GetMapping("/health")
    public Result healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("rateLimiter", "enabled");
        health.put("callLog", aiCallLogMapper != null ? "enabled" : "disabled");
        health.put("timestamp", System.currentTimeMillis());
        return Result.suc(health);
    }

    /**
     * 导出日志（CSV格式）
     * TODO: 实现CSV导出功能
     */
    @Operation(summary = "导出AI调用日志", description = "导出AI调用日志为CSV文件")
    @GetMapping("/export")
    public Result exportLogs(@RequestParam(required = false) String startTime,
                             @RequestParam(required = false) String endTime) {
        // TODO: 实现导出功能
        return Result.fail("导出功能开发中");
    }
}
