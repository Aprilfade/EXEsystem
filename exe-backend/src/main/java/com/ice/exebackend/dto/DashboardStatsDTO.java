package com.ice.exebackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardStatsDTO {
    // 顶部卡片数据
    private long studentCount;
    private long subjectCount;
    private long knowledgePointCount;
    private long questionCount;
    private long paperCount;

    // "知识点&题目总览" 图表数据
    private ChartData kpAndQuestionStats;

    // 通知列表 (简化实现)
    private List<Notification> notifications;

    // 用于图表的通用数据结构
    @Data
    public static class ChartData {
        private List<String> categories; // X轴分类 (如：科目名称)
        private List<SeriesData> series; // Y轴数据系列
    }

    // 用于图表系列的数据结构
    @Data
    public static class SeriesData {
        private String name; // 系列名称 (如："知识点总数")
        private List<Long> data; // 该系列的数据
    }

    @Data
    public static class Notification {
        private String content;
        private String date;
    }
}