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

        public List<String> getCategories() {
            return categories;
        }
        public List<SeriesData> getSeries() {
            return series;
        }
        public void setCategories(List<String> categories) {
            this.categories = categories;
        }
        public void setSeries(List<SeriesData> series) {
            this.series = series;
        }

    }

    // 用于图表系列的数据结构
    @Data
    public static class SeriesData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private List<Long> data;

        public String getName() {
            return name;
        }
        public List<Long> getData() {
            return data;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setData(List<Long> data) {
            this.data = data;
        }
    }

    @Data
    public static class Notification implements Serializable {
        private static final long serialVersionUID = 1L;
        private String content;
        private String date;

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
    public Long getStudentCount(){
        return studentCount;
    }

    public void setStudentCount(Long studentCount){
        this.studentCount = studentCount;
    }

    public Long getSubjectCount(){
        return subjectCount;
    }

    public void setSubjectCount(Long subjectCount){
        this.subjectCount = subjectCount;
    }

    public Long getKnowledgePointCount(){
        return knowledgePointCount;
    }

    public void setKnowledgePointCount(Long knowledgePointCount){
        this.knowledgePointCount = knowledgePointCount;
    }

    public Long getQuestionCount(){
        return questionCount;
    }

    public void setQuestionCount(Long questionCount){
        this.questionCount = questionCount;
    }

    public Long getPaperCount(){
        return paperCount;
    }

    public void setPaperCount(Long paperCount){
        this.paperCount = paperCount;
    }

    public ChartData getKpAndQuestionStats() {
        return kpAndQuestionStats;
    }
    public void setKpAndQuestionStats(ChartData kpAndQuestionStats) {
        this.kpAndQuestionStats = kpAndQuestionStats;
    }
    public List<Notification> getNotifications() {
        return notifications;
    }
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

}