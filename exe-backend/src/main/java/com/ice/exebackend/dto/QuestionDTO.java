package com.ice.exebackend.dto;

import com.ice.exebackend.entity.BizQuestion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionDTO extends BizQuestion {

    // 关联的知识点ID列表
    private List<Long> knowledgePointIds;

    public List<Long> getKnowledgePointIds() {
        return knowledgePointIds;
    }
    public void setKnowledgePointIds(List<Long> knowledgePointIds) {
        this.knowledgePointIds = knowledgePointIds;
    }
}