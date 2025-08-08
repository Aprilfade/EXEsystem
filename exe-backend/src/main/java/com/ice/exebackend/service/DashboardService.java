package com.ice.exebackend.service;

import com.ice.exebackend.dto.DashboardStatsDTO;

public interface DashboardService {
    /**
     * 获取工作台所需的所有统计数据
     * @return DashboardStatsDTO 包含所有统计信息的对象
     */
    DashboardStatsDTO getDashboardStats();
}