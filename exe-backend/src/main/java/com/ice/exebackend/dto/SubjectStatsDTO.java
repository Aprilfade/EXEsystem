package com.ice.exebackend.dto;

import com.ice.exebackend.entity.BizSubject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectStatsDTO extends BizSubject {
    /**
     * 该科目下关联的知识点数量
     */
    private Long knowledgePointCount;

    public Long getKnowledgePointCount() {
        return knowledgePointCount;
    }

    public void setKnowledgePointCount(Long knowledgePointCount) {
        this.knowledgePointCount = knowledgePointCount;
    }
}