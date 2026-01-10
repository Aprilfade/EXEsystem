package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.service.PortalService;
import com.ice.exebackend.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Portal访问统计控制器
 *
 * @author System
 * @version v3.05
 */
@Tag(name = "Portal访问统计", description = "Portal导航页访问统计相关接口")
@RestController
@RequestMapping("/api/v1/portal")
public class PortalController {

    @Autowired
    private PortalService portalService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserIdOrNull() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username == null || "anonymousUser".equals(username)) {
                return null;
            }
            SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Operation(summary = "获取系统访问统计")
    @GetMapping("/visit-stats")
    public Result<List<SystemVisitStatsDTO>> getVisitStats(
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        Long userId = getCurrentUserIdOrNull();
        List<SystemVisitStatsDTO> stats = portalService.getSystemVisitStats(days, userId);
        return Result.suc(stats);
    }

    @Operation(summary = "获取访问趋势数据")
    @GetMapping("/visit-trend")
    public Result<List<VisitTrendDataPointDTO>> getVisitTrend(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        Long userId = getCurrentUserIdOrNull();
        List<VisitTrendDataPointDTO> trend = portalService.getVisitTrendData(days, userId);
        return Result.suc(trend);
    }

    @Operation(summary = "获取系统使用分布")
    @GetMapping("/usage-distribution")
    public Result<List<Map<String, Object>>> getUsageDistribution() {
        Long userId = getCurrentUserIdOrNull();
        List<Map<String, Object>> distribution = portalService.getSystemUsageDistribution(userId);
        return Result.suc(distribution);
    }

    @Operation(summary = "获取热力图数据")
    @GetMapping("/heatmap")
    public Result<List<HeatmapCellDTO>> getHeatmap(
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        Long userId = getCurrentUserIdOrNull();
        List<HeatmapCellDTO> heatmap = portalService.getHeatmapData(days, userId);
        return Result.suc(heatmap);
    }

    @Operation(summary = "获取最近访问记录")
    @GetMapping("/recent-access")
    public Result<List<RecentAccessRecordDTO>> getRecentAccess(
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        Long userId = getCurrentUserIdOrNull();
        if (userId == null) {
            // 未登录用户返回空列表
            return Result.suc(List.of());
        }
        List<RecentAccessRecordDTO> recentAccess = portalService.getRecentAccess(userId, limit);
        return Result.suc(recentAccess);
    }

    @Operation(summary = "获取访问统计汇总")
    @GetMapping("/visit-summary")
    public Result<Map<String, Object>> getVisitSummary() {
        Long userId = getCurrentUserIdOrNull();
        Map<String, Object> summary = portalService.getVisitSummary(userId);
        return Result.suc(summary);
    }

    @Operation(summary = "记录系统访问")
    @PostMapping("/record-visit")
    public Result<Void> recordVisit(@RequestBody VisitRecordDTO visitRecord) {
        // 如果前端没有传userId，尝试从当前登录信息获取
        if (visitRecord.getUserId() == null) {
            visitRecord.setUserId(getCurrentUserIdOrNull());
        }
        portalService.recordVisit(visitRecord);
        return Result.suc();
    }

    @Operation(summary = "批量记录访问（离线同步）")
    @PostMapping("/batch-record-visits")
    public Result<Void> batchRecordVisits(@RequestBody Map<String, List<VisitRecordDTO>> request) {
        List<VisitRecordDTO> records = request.get("records");
        if (records != null && !records.isEmpty()) {
            portalService.batchRecordVisits(records);
        }
        return Result.suc();
    }
}
