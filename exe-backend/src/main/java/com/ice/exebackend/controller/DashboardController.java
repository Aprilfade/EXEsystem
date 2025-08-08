package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // 【修复点1】: 导入 PreAuthorize
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
// 【修复点2】: 添加权限控制注解，与项目中其他控制器保持一致
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result getStats() {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return Result.suc(stats);
    }
}