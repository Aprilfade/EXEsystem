package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.ActivityListDTO;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.dto.TodoListDTO;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.service.DashboardService;
import com.ice.exebackend.service.DashboardExportService;
import com.ice.exebackend.service.SysUserPreferenceService;
import com.ice.exebackend.service.SysUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.ice.exebackend.handler.NotificationWebSocketHandler;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    // 【新增】日志记录器
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    // 【新增】注入 WebSocket Handler 以获取实时数据
    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    // 【新增】注入用户配置服务
    @Autowired
    private SysUserPreferenceService userPreferenceService;

    // 【新增】注入用户服务
    @Autowired
    private SysUserService sysUserService;

    // 【新增】注入导出服务
    @Autowired
    private DashboardExportService dashboardExportService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
        return user != null ? user.getId() : null;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result getStats(@RequestParam(required = false) String month) {
        logger.info("开始获取工作台统计数据...");
        try {
            DashboardStatsDTO stats = dashboardService.getDashboardStats(month);
            logger.info("成功获取统计数据，准备返回。科目统计数据是否为空: {}", stats.getSubjectStatsByGrade() == null);
            return Result.suc(stats);
        } catch (Exception e) {
            // 【新增】捕获任何可能的异常并打印详细日志
            logger.error("获取工作台统计数据时发生严重错误！", e);
            return Result.fail("服务器内部错误，详情请查看后端日志。");
        }
    }
    /**
     * 【新增】获取当前实时在线学生人数
     */
    @GetMapping("/online-students")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result getOnlineStudentCount() {
        long count = webSocketHandler.getOnlineStudentCount();
        return Result.suc(Map.of("count", count));
    }

    /**
     * 获取待办事项列表
     */
    @GetMapping("/todos")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result getTodoList() {
        logger.info("开始获取待办事项列表...");
        try {
            TodoListDTO todos = dashboardService.getTodoList();
            logger.info("成功获取待办事项，共 {} 项", todos.getTotalCount());
            return Result.suc(todos);
        } catch (Exception e) {
            logger.error("获取待办事项时发生错误！", e);
            return Result.fail("获取待办事项失败");
        }
    }

    /**
     * 获取最近活动列表
     */
    @GetMapping("/recent-activities")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result getRecentActivities(@RequestParam(defaultValue = "5") int limit) {
        logger.info("开始获取最近活动，限制 {} 条...", limit);
        try {
            ActivityListDTO activities = dashboardService.getRecentActivities(limit);
            logger.info("成功获取最近活动，共 {} 条", activities.getTotal());
            return Result.suc(activities);
        } catch (Exception e) {
            logger.error("获取最近活动时发生错误！", e);
            return Result.fail("获取最近活动失败");
        }
    }

    /**
     * 获取用户工作台布局配置
     */
    @GetMapping("/layout-config")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result getLayoutConfig() {
        logger.info("开始获取用户布局配置...");
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.fail("无法获取当前用户信息");
            }

            String config = userPreferenceService.getPreference(userId, "dashboard_layout");
            logger.info("成功获取用户 {} 的布局配置", userId);
            return Result.suc(config);
        } catch (Exception e) {
            logger.error("获取布局配置时发生错误！", e);
            return Result.fail("获取布局配置失败");
        }
    }

    /**
     * 保存用户工作台布局配置
     */
    @PostMapping("/layout-config")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result saveLayoutConfig(@RequestBody String config) {
        logger.info("开始保存用户布局配置...");
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.fail("无法获取当前用户信息");
            }

            userPreferenceService.savePreference(userId, "dashboard_layout", config);
            logger.info("成功保存用户 {} 的布局配置", userId);
            return Result.suc("布局配置保存成功");
        } catch (Exception e) {
            logger.error("保存布局配置时发生错误！", e);
            return Result.fail("保存布局配置失败");
        }
    }

    /**
     * 重置用户工作台布局配置为默认值
     */
    @PostMapping("/layout-reset")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result resetLayoutConfig() {
        logger.info("开始重置用户布局配置...");
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.fail("无法获取当前用户信息");
            }

            userPreferenceService.deletePreference(userId, "dashboard_layout");
            logger.info("成功重置用户 {} 的布局配置", userId);
            return Result.suc("布局配置已重置为默认值");
        } catch (Exception e) {
            logger.error("重置布局配置时发生错误！", e);
            return Result.fail("重置布局配置失败");
        }
    }

    /**
     * 导出工作台数据为 Excel
     */
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('sys:home')")
    public void exportToExcel(HttpServletResponse response) {
        logger.info("开始导出工作台数据为 Excel...");
        try {
            dashboardExportService.exportToExcel(response);
            logger.info("Excel 导出成功");
        } catch (Exception e) {
            logger.error("导出 Excel 时发生错误！", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 【知识点功能增强】获取知识点覆盖率统计
     */
    @GetMapping("/knowledge-point-coverage")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result<List<Map<String, Object>>> getKnowledgePointCoverage() {
        logger.info("开始获取知识点覆盖率统计...");
        try {
            List<Map<String, Object>> coverage = dashboardService.getKnowledgePointCoverage();
            logger.info("成功获取知识点覆盖率统计，共 {} 个科目", coverage.size());
            return Result.ok(coverage);
        } catch (Exception e) {
            logger.error("获取知识点覆盖率统计时发生错误！", e);
            return Result.fail("获取知识点覆盖率统计失败");
        }
    }

    /**
     * 【知识点功能增强】获取薄弱知识点Top10
     */
    @GetMapping("/weak-knowledge-points")
    @PreAuthorize("hasAuthority('sys:home')")
    public Result<List<Map<String, Object>>> getWeakKnowledgePointsTop10() {
        logger.info("开始获取薄弱知识点Top10...");
        try {
            List<Map<String, Object>> weakPoints = dashboardService.getWeakKnowledgePointsTop10();
            logger.info("成功获取薄弱知识点，共 {} 个", weakPoints.size());
            return Result.ok(weakPoints);
        } catch (Exception e) {
            logger.error("获取薄弱知识点时发生错误！", e);
            return Result.fail("获取薄弱知识点失败");
        }
    }

}