package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 试卷-知识点关联表
 * 用于记录试卷包含的知识点及其统计信息
 *
 * @author ice
 * @since 2026-01-08
 */
@TableName("biz_paper_knowledge_point")
public class BizPaperKnowledgePoint {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 知识点ID
     */
    private Long knowledgePointId;

    /**
     * 该知识点的题目数量
     */
    private Integer questionCount;

    /**
     * 该知识点的总分值
     */
    private Integer totalScore;

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

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public Long getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(Long knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
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
        BizPaperKnowledgePoint that = (BizPaperKnowledgePoint) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(paperId, that.paperId) &&
                Objects.equals(knowledgePointId, that.knowledgePointId) &&
                Objects.equals(questionCount, that.questionCount) &&
                Objects.equals(totalScore, that.totalScore) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paperId, knowledgePointId, questionCount, totalScore, createTime);
    }

    @Override
    public String toString() {
        return "BizPaperKnowledgePoint{" +
                "id=" + id +
                ", paperId=" + paperId +
                ", knowledgePointId=" + knowledgePointId +
                ", questionCount=" + questionCount +
                ", totalScore=" + totalScore +
                ", createTime=" + createTime +
                '}';
    }
}
