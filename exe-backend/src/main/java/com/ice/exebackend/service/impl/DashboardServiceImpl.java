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
import java.util.Collections;
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
    //@Cacheable(value = "dashboardStats", key = "'stats:' + (#month == null ? 'all' : #month)")
    public DashboardStatsDTO getDashboardStats(String month) {
        DashboardStatsDTO dto = new DashboardStatsDTO();

        // 1. 顶部卡片统计
        Map<String, Long> topStats = dashboardMapper.getTopCardStats();
        dto.setStudentCount(topStats.getOrDefault("studentCount", 0L));
        dto.setSubjectCount(topStats.getOrDefault("subjectCount", 0L));
        dto.setKnowledgePointCount(topStats.getOrDefault("knowledgePointCount", 0L));
        dto.setQuestionCount(topStats.getOrDefault("questionCount", 0L));
        dto.setPaperCount(topStats.getOrDefault("paperCount", 0L));

        // 2. 知识点&题目总览图表
        List<Map<String, Object>> kpAndQuestionResult = dashboardMapper.getKpAndQuestionStatsBySubject(month);
        DashboardStatsDTO.ChartData kpAndQuestionChart = new DashboardStatsDTO.ChartData();
        kpAndQuestionChart.setCategories(kpAndQuestionResult.stream().map(r -> (String) r.get("subjectName")).collect(Collectors.toList()));
        DashboardStatsDTO.SeriesData kpSeries = new DashboardStatsDTO.SeriesData();
        kpSeries.setName("知识点数量");
        kpSeries.setData(kpAndQuestionResult.stream().map(r -> ((Number) r.get("knowledgePointCount")).longValue()).collect(Collectors.toList()));
        DashboardStatsDTO.SeriesData questionSeries = new DashboardStatsDTO.SeriesData();
        questionSeries.setName("题目数量");
        questionSeries.setData(kpAndQuestionResult.stream().map(r -> ((Number) r.get("questionCount")).longValue()).collect(Collectors.toList()));
        kpAndQuestionChart.setSeries(List.of(kpSeries, questionSeries));
        dto.setKpAndQuestionStats(kpAndQuestionChart);

        // 3. 错题统计图表
        List<Map<String, Object>> wrongQuestionResult = dashboardMapper.getWrongQuestionStatsBySubject();
        DashboardStatsDTO.ChartData wrongQuestionChart = new DashboardStatsDTO.ChartData();
        wrongQuestionChart.setCategories(wrongQuestionResult.stream().map(r -> (String) r.get("subjectName")).collect(Collectors.toList()));
        DashboardStatsDTO.SeriesData wrongCountSeries = new DashboardStatsDTO.SeriesData();
        wrongCountSeries.setName("错题数量");
        wrongCountSeries.setData(wrongQuestionResult.stream().map(r -> ((Number) r.get("wrongCount")).longValue()).collect(Collectors.toList()));
        wrongQuestionChart.setSeries(Collections.singletonList(wrongCountSeries));
        dto.setWrongQuestionStats(wrongQuestionChart);

        // 4. 每月新增题目图表
        List<Map<String, Object>> monthlyCreationResult = dashboardMapper.getMonthlyQuestionCreationStats();
        DashboardStatsDTO.ChartData monthlyCreationChart = new DashboardStatsDTO.ChartData();
        monthlyCreationChart.setCategories(monthlyCreationResult.stream().map(r -> (String) r.get("month")).collect(Collectors.toList()));
        DashboardStatsDTO.SeriesData monthlyCountSeries = new DashboardStatsDTO.SeriesData();
        monthlyCountSeries.setName("新增题目数");
        monthlyCountSeries.setData(monthlyCreationResult.stream().map(r -> ((Number) r.get("count")).longValue()).collect(Collectors.toList()));
        monthlyCreationChart.setSeries(Collections.singletonList(monthlyCountSeries));
        dto.setMonthlyQuestionCreationStats(monthlyCreationChart);

        // 5. 科目统计 (按科目统计学生人数)
        List<Map<String, Object>> subjectStudentStatsResult = dashboardMapper.getSubjectStatsByGrade();
        DashboardStatsDTO.ChartData subjectStatsChart = new DashboardStatsDTO.ChartData();
        subjectStatsChart.setCategories(subjectStudentStatsResult.stream().map(r -> (String) r.get("subjectName")).collect(Collectors.toList()));
        DashboardStatsDTO.SeriesData studentCountSeries = new DashboardStatsDTO.SeriesData();
        studentCountSeries.setName("学生数量");
        studentCountSeries.setData(subjectStudentStatsResult.stream().map(r -> ((Number) r.get("studentCount")).longValue()).collect(Collectors.toList()));
        subjectStatsChart.setSeries(Collections.singletonList(studentCountSeries));
        dto.setSubjectStatsByGrade(subjectStatsChart);

        // 6. 系统通知
        List<SysNotification> notifications = notificationService.list(
                new QueryWrapper<SysNotification>().eq("is_published", true).orderByDesc("publish_time").last("LIMIT 3")
        );
        dto.setNotifications(notifications.stream().map(n -> {
            DashboardStatsDTO.Notification notificationDto = new DashboardStatsDTO.Notification();
            notificationDto.setId(n.getId());
            notificationDto.setContent(n.getTitle());
            notificationDto.setDate(n.getPublishTime().format(DateTimeFormatter.ofPattern("MM-dd")));
            return notificationDto;
        }).collect(Collectors.toList()));

        return dto;
    }
}