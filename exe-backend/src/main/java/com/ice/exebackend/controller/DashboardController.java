package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // 【新增】导入 @RequestParam
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 【修改点】:
     * 1. 在方法签名中添加 @RequestParam(required = false) String month，
     * 使其可以接收一个可选的 "month" 参数 (格式如 "2025-08")。
     * 2. 将接收到的 month 参数传递给 service 层。
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result getStats(@RequestParam(required = false) String month) {
        DashboardStatsDTO stats = dashboardService.getDashboardStats(month);
        return Result.suc(stats);
    }
}