package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.entity.SysNotification;
import com.ice.exebackend.mapper.DashboardMapper;
import com.ice.exebackend.service.DashboardService;
import com.ice.exebackend.service.SysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit; // 1. 导入 TimeUnit 用于设置过期时间
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private SysNotificationService notificationService;

    // 2. 注入我们之前配置好的 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 3. 定义一个静态常量作为缓存的 Key，便于统一管理和引用
    private static final String DASHBOARD_STATS_KEY_PREFIX = "dashboard:stats:";

    @Override
    public DashboardStatsDTO getDashboardStats(String month) {
        // 4. 根据 month 参数动态生成唯一的缓存 Key
        String cacheKey = DASHBOARD_STATS_KEY_PREFIX + (month == null ? "all" : month);

        // 5. 首先，尝试从 Redis 中获取缓存数据
        try {
            DashboardStatsDTO cachedStats = (DashboardStatsDTO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedStats != null) {
                // 如果缓存命中 (cachedStats 不为 null)，直接返回缓存的结果，不再查询数据库
                return cachedStats;
            }
        } catch (Exception e) {
            // 在缓存读取出错时记录日志，然后继续执行数据库查询，保证服务可用性
            // logger.error("从Redis读取缓存失败", e);
        }


        // 6. 如果缓存中没有数据 (缓存未命中)，则执行从数据库查询的逻辑
        DashboardStatsDTO statsFromDb = getStatsFromDatabase(month);

        // 7. 将从数据库查询到的结果存入 Redis 缓存
        try {
            if (statsFromDb != null) {
                // 设置缓存，并指定过期时间为 1 小时
                redisTemplate.opsForValue().set(cacheKey, statsFromDb, 1, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            // 缓存写入失败不应影响主流程，记录日志即可
            // logger.error("向Redis写入缓存失败", e);
        }


        // 8. 返回从数据库中获取的数据
        return statsFromDb;
    }

    /**
     * 将原始的数据库查询逻辑封装到一个私有方法中，使代码结构更清晰。
     * @param month 月份参数
     * @return 从数据库获取的统计数据
     */
    private DashboardStatsDTO getStatsFromDatabase(String month) {
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
                new QueryWrapper<SysNotification>()
                        .and(wrapper -> wrapper
                                // 条件1: 已经是 "立即发布" 状态的
                                .eq("is_published", true)
                                // 条件2: 或者是 "定时发布" 且时间已到或已过的
                                .or(i -> i.isNotNull("publish_time")
                                        .le("publish_time", LocalDateTime.now())))
                        .orderByDesc("publish_time") // 按发布时间倒序
                        .last("LIMIT 3") // 只取最新的3条
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