package com.ice.exebackend.dto;

import lombok.Data;
import java.io.Serializable; // 1. 导入 Serializable 接口
import java.util.List;

@Data
// 2. 让 DashboardStatsDTO 实现 Serializable 接口
public class DashboardStatsDTO implements Serializable {

    // 【新增】为序列化版本提供一个固定的ID，这是一个好习惯
    private static final long serialVersionUID = 1L;

    // 顶部卡片数据
    private long studentCount;
    private long subjectCount;
    private long knowledgePointCount;
    private long questionCount;
    private long paperCount;

    // "知识点&题目总览" 图表数据
    private ChartData kpAndQuestionStats;

    // 通知列表
    private List<Notification> notifications;

    // 用于图表的通用数据结构
    @Data
    // 3. 让所有嵌套的内部类也实现 Serializable 接口
    public static class ChartData implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<String> categories;
        private List<SeriesData> series;
    }

    // 用于图表系列的数据结构
    @Data
    public static class SeriesData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private List<Long> data;
    }

    @Data
    public static class Notification implements Serializable {
        private static final long serialVersionUID = 1L;
        private String content;
        private String date;
    }
}