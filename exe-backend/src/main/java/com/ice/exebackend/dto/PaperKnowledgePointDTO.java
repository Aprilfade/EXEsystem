package com.ice.exebackend.dto;

import java.util.Objects;

/**
 * 试卷知识点分布DTO
 * 用于试卷详情页展示知识点分布
 *
 * @author ice
 * @since 2026-01-08
 */
public class PaperKnowledgePointDTO {

    /**
     * 知识点ID
     */
    private Long knowledgePointId;

    /**
     * 知识点名称
     */
    private String knowledgePointName;

    /**
     * 该知识点的题目数量
     */
    private Integer questionCount;

    /**
     * 该知识点的总分值
     */
    private Integer totalScore;

    /**
     * 占比（百分比）
     */
    private Double percentage;

    // Getters and Setters
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

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaperKnowledgePointDTO that = (PaperKnowledgePointDTO) o;
        return Objects.equals(knowledgePointId, that.knowledgePointId) &&
                Objects.equals(knowledgePointName, that.knowledgePointName) &&
                Objects.equals(questionCount, that.questionCount) &&
                Objects.equals(totalScore, that.totalScore) &&
                Objects.equals(percentage, that.percentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knowledgePointId, knowledgePointName, questionCount, totalScore, percentage);
    }

    @Override
    public String toString() {
        return "PaperKnowledgePointDTO{" +
                "knowledgePointId=" + knowledgePointId +
                ", knowledgePointName='" + knowledgePointName + '\'' +
                ", questionCount=" + questionCount +
                ", totalScore=" + totalScore +
                ", percentage=" + percentage +
                '}';
    }
}
