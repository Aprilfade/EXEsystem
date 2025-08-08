package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class PaperStatsVO {
    private Long questionId;
    private String questionContent;
    private Integer questionType;
    private Integer sortOrder;
    private Long errorCount;

    public Long getQuestionId(){
        return questionId;
    }
    public void setQuestionId(Long questionId){
        this.questionId = questionId;
    }
    public String getQuestionContent(){
        return questionContent;
    }
    public void setQuestionContent(String questionContent){
        this.questionContent = questionContent;
    }
    public Integer getQuestionType(){
        return questionType;
    }
    public void setQuestionType(Integer questionType){
        this.questionType = questionType;
    }
    public Integer getSortOrder(){
        return sortOrder;
    }
    public void setSortOrder(Integer sortOrder){
        this.sortOrder = sortOrder;
    }
    public Long getErrorCount(){
        return errorCount;
    }
    public void setErrorCount(Long errorCount){
        this.errorCount = errorCount;
    }
}