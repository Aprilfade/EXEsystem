package com.ice.exebackend.dto;

import com.ice.exebackend.entity.BizKnowledgePoint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgePointStatsDTO extends BizKnowledgePoint {

    /**
     * 该知识点关联的试题数量
     */
    private Long questionCount;

    public Long getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Long questionCount) {
        this.questionCount = questionCount;
    }
}