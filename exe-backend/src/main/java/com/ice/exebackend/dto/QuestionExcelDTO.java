// exe-backend/src/main/java/com/ice/exebackend/dto/QuestionExcelDTO.java
package com.ice.exebackend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionExcelDTO {

    @ExcelProperty("科目ID")
    private Long subjectId;

    @ExcelProperty("年级")
    private String grade;

    @ExcelProperty("题型 (1:单选, 2:多选, 3:填空, 4:判断, 5:主观)")
    private Integer questionType;

    @ExcelProperty("题干")
    private String content;

    @ExcelProperty("题目图片URL")
    private String imageUrl;

    @ExcelProperty("选项 (JSON格式)")
    private String options;

    @ExcelProperty("参考答案")
    private String answer;

    @ExcelProperty("答案图片URL")
    private String answerImageUrl;

    @ExcelProperty("解析")
    private String description;

    @ExcelProperty("关联知识点ID (逗号分隔)")
    private String knowledgePointIds;

    public String getKnowledgePointIds(){
        return knowledgePointIds;
    }
    public void setKnowledgePointIds(String knowledgePointIds){
        this.knowledgePointIds = knowledgePointIds;
    }
    public Long getSubjectId(){
        return subjectId;
    }
    public void setSubjectId(Long subjectId){
        this.subjectId = subjectId;
    }
    public String getGrade(){
        return grade;
    }
    public void setGrade(String grade){
        this.grade = grade;
    }
    public Integer getQuestionType(){
        return questionType;
    }
    public void setQuestionType(Integer questionType){
        this.questionType = questionType;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content = content;
    }
    public String getImageUrl(){
        return imageUrl;
    }
    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
    public String getOptions(){
        return options;
    }
    public void setOptions(String options){
        this.options = options;
    }
    public String getAnswer(){
        return answer;
    }
    public void setAnswer(String answer){
        this.answer = answer;
    }
    public String getAnswerImageUrl(){
        return answerImageUrl;
    }
    public void setAnswerImageUrl(String answerImageUrl){
        this.answerImageUrl = answerImageUrl;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
}