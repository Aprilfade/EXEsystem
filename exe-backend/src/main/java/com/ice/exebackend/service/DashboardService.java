package com.ice.exebackend.service;

import com.ice.exebackend.dto.ActivityListDTO;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.dto.TodoListDTO;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardStatsDTO getDashboardStats(String month);

    /**
     * 获取待办事项列表
     */
    TodoListDTO getTodoList();

    /**
     * 获取最近活动列表
     * @param limit 数量限制
     */
    ActivityListDTO getRecentActivities(int limit);

    /**
     * 【知识点功能增强】获取知识点覆盖率统计
     */
    List<Map<String, Object>> getKnowledgePointCoverage();

    /**
     * 【知识点功能增强】获取薄弱知识点Top10
     */
    List<Map<String, Object>> getWeakKnowledgePointsTop10();
}