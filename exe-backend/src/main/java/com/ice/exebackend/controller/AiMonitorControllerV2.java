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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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
     */
    @Operation(summary = "导出AI调用日志", description = "导出AI调用日志为CSV文件")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLogs(@RequestParam(required = false) String startTime,
                                             @RequestParam(required = false) String endTime) {
        if (aiCallLogMapper == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 构建查询条件
            QueryWrapper<BizAiCallLog> wrapper = new QueryWrapper<>();

            if (startTime != null && !startTime.isEmpty()) {
                wrapper.ge("create_time", LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME));
            }
            if (endTime != null && !endTime.isEmpty()) {
                wrapper.le("create_time", LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME));
            }

            wrapper.orderByDesc("create_time");

            // 查询所有符合条件的日志
            List<BizAiCallLog> logs = aiCallLogMapper.selectList(wrapper);

            // 构建CSV内容
            StringBuilder csv = new StringBuilder();

            // CSV头部
            csv.append("ID,用户ID,用户类型,功能类型,AI提供商,是否成功,响应时间(ms),是否缓存,重试次数,Token使用量,预估成本,错误信息,创建时间\n");

            // CSV数据行
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (BizAiCallLog log : logs) {
                csv.append(log.getId()).append(",")
                   .append(log.getUserId() != null ? log.getUserId() : "").append(",")
                   .append(log.getUserType() != null ? escapeCSV(log.getUserType()) : "").append(",")
                   .append(log.getFunctionType() != null ? escapeCSV(log.getFunctionType()) : "").append(",")
                   .append(log.getProvider() != null ? escapeCSV(log.getProvider()) : "").append(",")
                   .append(log.getSuccess() != null ? (log.getSuccess() ? "成功" : "失败") : "").append(",")
                   .append(log.getResponseTime() != null ? log.getResponseTime() : "").append(",")
                   .append(log.getCached() != null ? (log.getCached() ? "是" : "否") : "").append(",")
                   .append(log.getRetryCount() != null ? log.getRetryCount() : "0").append(",")
                   .append(log.getTokensUsed() != null ? log.getTokensUsed() : "").append(",")
                   .append(log.getEstimatedCost() != null ? log.getEstimatedCost() : "").append(",")
                   .append(log.getErrorMessage() != null ? escapeCSV(log.getErrorMessage()) : "").append(",")
                   .append(log.getCreateTime() != null ? log.getCreateTime().format(formatter) : "")
                   .append("\n");
            }

            // 转换为字节数组
            byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

            // 添加UTF-8 BOM以支持Excel正确显示中文
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] result = new byte[bom.length + bytes.length];
            System.arraycopy(bom, 0, result, 0, bom.length);
            System.arraycopy(bytes, 0, result, bom.length, bytes.length);

            // 设置响应头
            String fileName = "ai_call_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("no-cache");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * CSV字段转义（处理逗号、引号、换行符）
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // 如果包含逗号、引号或换行符，需要用引号包裹
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // 将引号替换为两个引号
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}
