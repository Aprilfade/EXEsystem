package com.ice.exebackend.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 知识点成绩分析DTO
 * 用于成绩详情页按知识点分析
 *
 * @author ice
 * @since 2026-01-08
 */
public class KnowledgePointScoreAnalysisDTO {

    /**
     * 知识点ID
     */
    private Long knowledgePointId;

    /**
     * 知识点名称
     */
    private String knowledgePointName;

    /**
     * 该知识点的得分
     */
    private BigDecimal score;

    /**
     * 该知识点的满分
     */
    private Integer maxScore;

    /**
     * 得分率（百分比）
     */
    private BigDecimal scoreRate;

    /**
     * 该知识点的题目数量
     */
    private Integer questionCount;

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

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public BigDecimal getScoreRate() {
        return scoreRate;
    }

    public void setScoreRate(BigDecimal scoreRate) {
        this.scoreRate = scoreRate;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgePointScoreAnalysisDTO that = (KnowledgePointScoreAnalysisDTO) o;
        return Objects.equals(knowledgePointId, that.knowledgePointId) &&
                Objects.equals(knowledgePointName, that.knowledgePointName) &&
                Objects.equals(score, that.score) &&
                Objects.equals(maxScore, that.maxScore) &&
                Objects.equals(scoreRate, that.scoreRate) &&
                Objects.equals(questionCount, that.questionCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knowledgePointId, knowledgePointName, score, maxScore, scoreRate, questionCount);
    }

    @Override
    public String toString() {
        return "KnowledgePointScoreAnalysisDTO{" +
                "knowledgePointId=" + knowledgePointId +
                ", knowledgePointName='" + knowledgePointName + '\'' +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", scoreRate=" + scoreRate +
                ", questionCount=" + questionCount +
                '}';
    }
}
