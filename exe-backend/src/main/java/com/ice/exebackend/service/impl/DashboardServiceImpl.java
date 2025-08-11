package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.entity.SysNotification;
import com.ice.exebackend.mapper.DashboardMapper;
import com.ice.exebackend.service.DashboardService;
import com.ice.exebackend.service.SysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private SysNotificationService notificationService;

    @Override
    @Cacheable(value = "dashboardStats", key = "'stats'")
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();

        // 1. 获取顶部卡片统计
        Map<String, Long> topStats = dashboardMapper.getTopCardStats();
        dto.setStudentCount(topStats.getOrDefault("studentCount", 0L));
        dto.setSubjectCount(topStats.getOrDefault("subjectCount", 0L));
        dto.setKnowledgePointCount(topStats.getOrDefault("knowledgePointCount", 0L));
        dto.setQuestionCount(topStats.getOrDefault("questionCount", 0L));
        dto.setPaperCount(topStats.getOrDefault("paperCount", 0L));

        // 2. 一次性获取并处理图表数据
        List<Map<String, Object>> statsResult = dashboardMapper.getKpAndQuestionStatsBySubject();

        DashboardStatsDTO.ChartData chartData = new DashboardStatsDTO.ChartData();

        // 从查询结果中提取科目名称作为X轴分类
        chartData.setCategories(statsResult.stream()
                .map(r -> (String)r.get("subjectName"))
                .collect(Collectors.toList()));

        // 创建知识点数据系列 (Series)
        DashboardStatsDTO.SeriesData kpSeries = new DashboardStatsDTO.SeriesData();
        kpSeries.setName("知识点总数");
        kpSeries.setData(statsResult.stream()
                .map(r -> (Long)r.get("knowledgePointCount"))
                .collect(Collectors.toList()));

        // 创建题目数据系列 (Series)
        DashboardStatsDTO.SeriesData questionSeries = new DashboardStatsDTO.SeriesData();
        questionSeries.setName("题目总数");
        questionSeries.setData(statsResult.stream()
                .map(r -> (Long)r.get("questionCount"))
                .collect(Collectors.toList()));

        chartData.setSeries(Arrays.asList(kpSeries, questionSeries));
        dto.setKpAndQuestionStats(chartData);

        // 3. 获取通知
        List<SysNotification> notifications = notificationService.list(
                new QueryWrapper<SysNotification>()
                        .eq("is_published", true)
                        .orderByDesc("publish_time")
                        .last("LIMIT 3")
        );

        List<DashboardStatsDTO.Notification> notificationDTOs = notifications.stream().map(n -> {
            DashboardStatsDTO.Notification notificationDto = new DashboardStatsDTO.Notification();
            notificationDto.setContent(n.getTitle());
            notificationDto.setDate(n.getPublishTime().format(DateTimeFormatter.ofPattern("MM-dd")));
            return notificationDto;
        }).collect(Collectors.toList());

        dto.setNotifications(notificationDTOs);

        return dto;
    }
}