package com.ice.exebackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WrongRecordVO {
    private Long id;

    // 学生信息
    private Long studentId;
    private String studentName;
    private String studentNo;

    // 题目信息
    private Long questionId;
    private String questionContent;

    // 试卷信息
    private Long paperId;
    private String paperName;

    private String wrongReason;
    private LocalDateTime createTime;

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public Long getStudentId(){
        return studentId;
    }
    public void setStudentId(Long studentId){
        this.studentId = studentId;
    }
    public String getStudentName(){
        return studentName;
    }
    public void setStudentName(String studentName){
        this.studentName = studentName;
    }
    public String getStudentNo(){
        return studentNo;
    }
    public void setStudentNo(String studentNo){
        this.studentNo = studentNo;
    }
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
    public Long getPaperId(){
        return paperId;
    }
    public void setPaperId(Long paperId){
        this.paperId = paperId;
    }
    public String getPaperName(){
        return paperName;
    }
    public void setPaperName(String paperName){
        this.paperName = paperName;
    }
    public String getWrongReason(){
        return wrongReason;
    }
    public void setWrongReason(String wrongReason){
        this.wrongReason = wrongReason;
    }
    public LocalDateTime getCreateTime(){
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime){
        this.createTime = createTime;
    }
}