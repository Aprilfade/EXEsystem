package com.ice.exebackend.dto;

import java.util.Map;
import java.util.Objects;

/**
 * 成绩统计数据 DTO
 */
public class ScoreStatsDTO {
    /**
     * 总考试人数
     */
    private Integer totalCount;

    /**
     * 平均分
     */
    private Double averageScore;

    /**
     * 最高分
     */
    private Integer maxScore;

    /**
     * 最低分
     */
    private Integer minScore;

    /**
     * 及格人数（>=60分）
     */
    private Integer passCount;

    /**
     * 及格率（%）
     */
    private Double passRate;

    /**
     * 优秀人数（>=90分）
     */
    private Integer excellentCount;

    /**
     * 优秀率（%）
     */
    private Double excellentRate;

    /**
     * 成绩分段统计
     * key: 分数段（如"0-59", "60-69"等）
     * value: 人数
     */
    private Map<String, Integer> scoreDistribution;

    // Getters and Setters
    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getMinScore() {
        return minScore;
    }

    public void setMinScore(Integer minScore) {
        this.minScore = minScore;
    }

    public Integer getPassCount() {
        return passCount;
    }

    public void setPassCount(Integer passCount) {
        this.passCount = passCount;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }

    public Integer getExcellentCount() {
        return excellentCount;
    }

    public void setExcellentCount(Integer excellentCount) {
        this.excellentCount = excellentCount;
    }

    public Double getExcellentRate() {
        return excellentRate;
    }

    public void setExcellentRate(Double excellentRate) {
        this.excellentRate = excellentRate;
    }

    public Map<String, Integer> getScoreDistribution() {
        return scoreDistribution;
    }

    public void setScoreDistribution(Map<String, Integer> scoreDistribution) {
        this.scoreDistribution = scoreDistribution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreStatsDTO that = (ScoreStatsDTO) o;
        return Objects.equals(totalCount, that.totalCount) &&
                Objects.equals(averageScore, that.averageScore) &&
                Objects.equals(maxScore, that.maxScore) &&
                Objects.equals(minScore, that.minScore) &&
                Objects.equals(passCount, that.passCount) &&
                Objects.equals(passRate, that.passRate) &&
                Objects.equals(excellentCount, that.excellentCount) &&
                Objects.equals(excellentRate, that.excellentRate) &&
                Objects.equals(scoreDistribution, that.scoreDistribution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCount, averageScore, maxScore, minScore, passCount,
                passRate, excellentCount, excellentRate, scoreDistribution);
    }

    @Override
    public String toString() {
        return "ScoreStatsDTO{" +
                "totalCount=" + totalCount +
                ", averageScore=" + averageScore +
                ", maxScore=" + maxScore +
                ", minScore=" + minScore +
                ", passCount=" + passCount +
                ", passRate=" + passRate +
                ", excellentCount=" + excellentCount +
                ", excellentRate=" + excellentRate +
                ", scoreDistribution=" + scoreDistribution +
                '}';
    }
}
