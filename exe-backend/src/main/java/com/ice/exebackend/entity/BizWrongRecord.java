package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_wrong_record")
public class BizWrongRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long questionId;
    private Long paperId;
    private String wrongAnswer;
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
    public Long getQuestionId(){
        return questionId;
    }
    public void setQuestionId(Long questionId){
        this.questionId = questionId;
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
    public LocalDateTime getCreateTime(){
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime){
        this.createTime = createTime;
    }
}