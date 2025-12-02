package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ice.exebackend.handler.NotificationWebSocketHandler; // 导入 Handler

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

}