package com.ice.exebackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 学生知识点掌握度DTO
 * 用于学生详情页展示知识点掌握情况
 *
 * @author ice
 * @since 2026-01-08
 */
public class StudentKnowledgeMasteryDTO {

    /**
     * 知识点ID
     */
    private Long knowledgePointId;

    /**
     * 知识点名称
     */
    private String knowledgePointName;

    /**
     * 答对题数（累计）
     */
    private Integer correctCount;

    /**
     * 总答题数（累计）
     */
    private Integer totalCount;

    /**
     * 掌握度（百分比，0-100）
     */
    private BigDecimal masteryRate;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 掌握等级：优秀(>=90)、良好(>=75)、及格(>=60)、待提升(<60)
     */
    private String masteryLevel;

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

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getMasteryRate() {
        return masteryRate;
    }

    public void setMasteryRate(BigDecimal masteryRate) {
        this.masteryRate = masteryRate;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(String masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentKnowledgeMasteryDTO that = (StudentKnowledgeMasteryDTO) o;
        return Objects.equals(knowledgePointId, that.knowledgePointId) &&
                Objects.equals(knowledgePointName, that.knowledgePointName) &&
                Objects.equals(correctCount, that.correctCount) &&
                Objects.equals(totalCount, that.totalCount) &&
                Objects.equals(masteryRate, that.masteryRate) &&
                Objects.equals(lastUpdateTime, that.lastUpdateTime) &&
                Objects.equals(masteryLevel, that.masteryLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knowledgePointId, knowledgePointName, correctCount, totalCount,
                masteryRate, lastUpdateTime, masteryLevel);
    }

    @Override
    public String toString() {
        return "StudentKnowledgeMasteryDTO{" +
                "knowledgePointId=" + knowledgePointId +
                ", knowledgePointName='" + knowledgePointName + '\'' +
                ", correctCount=" + correctCount +
                ", totalCount=" + totalCount +
                ", masteryRate=" + masteryRate +
                ", lastUpdateTime=" + lastUpdateTime +
                ", masteryLevel='" + masteryLevel + '\'' +
                '}';
    }
}
