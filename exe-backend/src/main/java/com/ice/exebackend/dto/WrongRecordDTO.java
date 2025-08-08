package com.ice.exebackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class WrongRecordDTO {
    private Long id;
    private Long questionId;
    private List<Long> studentIds; // 支持为多个学生创建
    private Long paperId;
    private String wrongAnswer;
    private String wrongReason;

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public Long getQuestionId(){
        return questionId;
    }
    public void setQuestionId(Long questionId){
        this.questionId = questionId;
    }
    public List<Long> getStudentIds(){
        return studentIds;
    }
    public void setStudentIds(List<Long> studentIds){
        this.studentIds = studentIds;
    }
    public Long getPaperId(){
        return paperId;
    }
    public void setPaperId(Long paperId){
        this.paperId = paperId;
    }
    public String getWrongAnswer(){
        return wrongAnswer;
    }
    public void setWrongAnswer(String wrongAnswer){
        this.wrongAnswer = wrongAnswer;
    }
    public String getWrongReason(){
        return wrongReason;
    }
    public void setWrongReason(String wrongReason){
        this.wrongReason = wrongReason;
    }
}
