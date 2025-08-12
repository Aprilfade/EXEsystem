package com.ice.exebackend.dto;

import java.io.Serializable;
import java.util.List;

// 移除了 @Data 注解，全部使用手动 getter/setter
public class DashboardStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 顶部卡片数据
    private long studentCount;
    private long subjectCount;
    private long knowledgePointCount;
    private long questionCount;
    private long paperCount;

    // "知识点&题目总览" 图表数据
    private ChartData kpAndQuestionStats;

    // "错题统计" 图表数据
    private ChartData wrongQuestionStats;

    // "每月新增题目" 图表数据
    private ChartData monthlyQuestionCreationStats;

    // 通知列表
    private List<Notification> notifications;

    // --- 手动添加所有 Getter 和 Setter ---

    public long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(long studentCount) {
        this.studentCount = studentCount;
    }

    public long getSubjectCount() {
        return subjectCount;
    }

    public void setSubjectCount(long subjectCount) {
        this.subjectCount = subjectCount;
    }

    public long getKnowledgePointCount() {
        return knowledgePointCount;
    }

    public void setKnowledgePointCount(long knowledgePointCount) {
        this.knowledgePointCount = knowledgePointCount;
    }

    public long getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(long questionCount) {
        this.questionCount = questionCount;
    }

    public long getPaperCount() {
        return paperCount;
    }

    public void setPaperCount(long paperCount) {
        this.paperCount = paperCount;
    }

    public ChartData getKpAndQuestionStats() {
        return kpAndQuestionStats;
    }

    public void setKpAndQuestionStats(ChartData kpAndQuestionStats) {
        this.kpAndQuestionStats = kpAndQuestionStats;
    }

    public ChartData getWrongQuestionStats() {
        return wrongQuestionStats;
    }

    public void setWrongQuestionStats(ChartData wrongQuestionStats) {
        this.wrongQuestionStats = wrongQuestionStats;
    }

    public ChartData getMonthlyQuestionCreationStats() {
        return monthlyQuestionCreationStats;
    }

    public void setMonthlyQuestionCreationStats(ChartData monthlyQuestionCreationStats) {
        this.monthlyQuestionCreationStats = monthlyQuestionCreationStats;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }


    // --- 内部类也需要手动添加 Getter 和 Setter ---

    public static class ChartData implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<String> categories;
        private List<SeriesData> series;

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public List<SeriesData> getSeries() {
            return series;
        }

        public void setSeries(List<SeriesData> series) {
            this.series = series;
        }
    }

    public static class SeriesData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private List<Long> data;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Long> getData() {
            return data;
        }

        public void setData(List<Long> data) {
            this.data = data;
        }
    }

    public static class Notification implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String content;
        private String date;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}