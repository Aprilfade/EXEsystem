package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class KnowledgePointErrorStatsDTO {
    private Long knowledgePointId;
    private String knowledgePointName;
    private String subjectName;
    private Long totalQuestions; // 该知识点下的题目总数
    private Long totalErrors;    // 总错误人次

    public Long getKnowledgePointId() {
        return knowledgePointId;
    }
    public void setKnowledgePointId(Long knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }
    public String getKnowledgePointName() {
        return knowledgePointName;
    }
    public void setKnowledgePointName(String knowledgePointName) {
        this.knowledgePointName = knowledgePointName;
    }

    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setTotalErrors(Long totalErrors) {

        this.totalErrors = totalErrors;
    }
    public Long getTotalErrors() {
        return totalErrors;
    }
    public void setTotalQuestions(Long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    public Long getTotalQuestions() {
        return totalQuestions;
    }
}