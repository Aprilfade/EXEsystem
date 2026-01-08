package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 学生知识点掌握度表
 * 用于记录学生对各知识点的掌握情况
 *
 * @author ice
 * @since 2026-01-08
 */
@TableName("biz_student_knowledge_mastery")
public class BizStudentKnowledgeMastery {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 知识点ID
     */
    private Long knowledgePointId;

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
     * 创建时间
     */
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(Long knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BizStudentKnowledgeMastery that = (BizStudentKnowledgeMastery) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(knowledgePointId, that.knowledgePointId) &&
                Objects.equals(correctCount, that.correctCount) &&
                Objects.equals(totalCount, that.totalCount) &&
                Objects.equals(masteryRate, that.masteryRate) &&
                Objects.equals(lastUpdateTime, that.lastUpdateTime) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, knowledgePointId, correctCount, totalCount,
                masteryRate, lastUpdateTime, createTime);
    }

    @Override
    public String toString() {
        return "BizStudentKnowledgeMastery{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", knowledgePointId=" + knowledgePointId +
                ", correctCount=" + correctCount +
                ", totalCount=" + totalCount +
                ", masteryRate=" + masteryRate +
                ", lastUpdateTime=" + lastUpdateTime +
                ", createTime=" + createTime +
                '}';
    }
}
