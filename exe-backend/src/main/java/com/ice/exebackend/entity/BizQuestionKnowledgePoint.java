package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_question_knowledge_point")
public class BizQuestionKnowledgePoint {

    private Long questionId; // 试题ID

    private Long knowledgePointId; // 知识点ID

    public Long getQuestionId(){

        return questionId;
    }
    public void setQuestionId(Long questionId){

        this.questionId = questionId;
    }
    public Long getKnowledgePointId(){

        return knowledgePointId;
    }
    public void setKnowledgePointId(Long knowledgePointId){

        this.knowledgePointId = knowledgePointId;
    }
}