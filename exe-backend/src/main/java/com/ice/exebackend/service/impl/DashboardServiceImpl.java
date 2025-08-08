package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 【修复点】: 添加了 @Service 注解，并确保实现了 DashboardService 接口
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired private BizStudentService studentService;
    @Autowired private BizSubjectService subjectService;
    @Autowired private BizKnowledgePointService knowledgePointService;
    @Autowired private BizQuestionService questionService;
    @Autowired private BizPaperService paperService;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();

        // 1. 获取顶部卡片统计
        dto.setStudentCount(studentService.count());
        dto.setSubjectCount(subjectService.count());
        dto.setKnowledgePointCount(knowledgePointService.count());
        dto.setQuestionCount(questionService.count());
        dto.setPaperCount(paperService.count());

        // 2. 获取 "知识点&题目总览" 图表数据
        List<BizSubject> subjects = subjectService.list();
        List<String> subjectNames = subjects.stream().map(BizSubject::getName).collect(Collectors.toList());

        Map<Long, Long> kpCountsBySubject = knowledgePointService.list().stream()
                .collect(Collectors.groupingBy(BizKnowledgePoint::getSubjectId, Collectors.counting()));

        Map<Long, Long> questionCountsBySubject = questionService.list().stream()
                .collect(Collectors.groupingBy(BizQuestion::getSubjectId, Collectors.counting()));

        DashboardStatsDTO.ChartData chartData = new DashboardStatsDTO.ChartData();
        chartData.setCategories(subjectNames);

        List<DashboardStatsDTO.SeriesData> seriesList = new ArrayList<>();

        DashboardStatsDTO.SeriesData kpSeries = new DashboardStatsDTO.SeriesData();
        kpSeries.setName("知识点总数");
        kpSeries.setData(subjects.stream().map(s -> kpCountsBySubject.getOrDefault(s.getId(), 0L)).collect(Collectors.toList()));
        seriesList.add(kpSeries);

        DashboardStatsDTO.SeriesData questionSeries = new DashboardStatsDTO.SeriesData();
        questionSeries.setName("题目总数");
        questionSeries.setData(subjects.stream().map(s -> questionCountsBySubject.getOrDefault(s.getId(), 0L)).collect(Collectors.toList()));
        seriesList.add(questionSeries);

        chartData.setSeries(seriesList);
        dto.setKpAndQuestionStats(chartData);

        // 3. 模拟通知数据
        DashboardStatsDTO.Notification n1 = new DashboardStatsDTO.Notification();
        n1.setContent("这是一条系统通知消息");
        n1.setDate("04-23");
        dto.setNotifications(Arrays.asList(n1, n1, n1));

        return dto;
    }
}