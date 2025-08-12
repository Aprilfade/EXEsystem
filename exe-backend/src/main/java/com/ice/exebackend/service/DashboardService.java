package com.ice.exebackend.service;

import com.ice.exebackend.dto.DashboardStatsDTO;

public interface DashboardService {
    /**
     * 获取工作台所需的所有统计数据
     * @param month 可选的月份参数 (格式: "YYYY-MM")
     * @return DashboardStatsDTO 包含所有统计信息的对象
     */
    DashboardStatsDTO getDashboardStats(String month); // 【修改点】: 在方法签名中添加 month 参数
}