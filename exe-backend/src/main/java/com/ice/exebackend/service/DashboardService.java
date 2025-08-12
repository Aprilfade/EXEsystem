package com.ice.exebackend.service;

import com.ice.exebackend.dto.DashboardStatsDTO;

public interface DashboardService {
    DashboardStatsDTO getDashboardStats(String month);
}