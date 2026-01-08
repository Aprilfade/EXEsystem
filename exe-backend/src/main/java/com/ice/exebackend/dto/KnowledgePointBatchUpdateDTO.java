package com.ice.exebackend.dto;

import java.util.List;
import java.util.Objects;

/**
 * 知识点批量更新DTO
 *
 * @author ice
 * @since 2026-01-08
 */
public class KnowledgePointBatchUpdateDTO {
    /**
     * 要更新的知识点ID列表
     */
    private List<Long> knowledgePointIds;

    /**
     * 新的科目ID（可选）
     */
    private Long subjectId;

    /**
     * 新的年级（可选）
     */
    private String grade;

    /**
     * 新的标签（可选）
     */
    private String tags;

    // Getters and Setters
    public List<Long> getKnowledgePointIds() {
        return knowledgePointIds;
    }

    public void setKnowledgePointIds(List<Long> knowledgePointIds) {
        this.knowledgePointIds = knowledgePointIds;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgePointBatchUpdateDTO that = (KnowledgePointBatchUpdateDTO) o;
        return Objects.equals(knowledgePointIds, that.knowledgePointIds) &&
                Objects.equals(subjectId, that.subjectId) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knowledgePointIds, subjectId, grade, tags);
    }

    @Override
    public String toString() {
        return "KnowledgePointBatchUpdateDTO{" +
                "knowledgePointIds=" + knowledgePointIds +
                ", subjectId=" + subjectId +
                ", grade='" + grade + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }
}
