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

    // 题目信息 (列表用)
    private Long questionId;
    private String questionContent;

    // 试卷信息
    private Long paperId;
    private String paperName;

    private String wrongReason;
    private LocalDateTime createTime;

    // === 【新增】以下字段用于详情展示和 AI 分析 ===
    private String wrongAnswer;     // 学生的错误答案
    private String content;         // 题干 (详情页用)
    private String imageUrl;        // 题目图片
    private String options;         // 选项 (JSON)
    private String answer;          // 正确答案
    private String answerImageUrl;  // 答案图片
    private String description;     // 解析

    // === 手动添加 Getter/Setter (如果 lombok 不生效) ===
    public String getWrongAnswer() { return wrongAnswer; }
    public void setWrongAnswer(String wrongAnswer) { this.wrongAnswer = wrongAnswer; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getAnswerImageUrl() { return answerImageUrl; }
    public void setAnswerImageUrl(String answerImageUrl) { this.answerImageUrl = answerImageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // ... 原有的 getter/setter 保持不变 ...
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public Long getStudentId(){ return studentId; }
    public void setStudentId(Long studentId){ this.studentId = studentId; }
    public String getStudentName(){ return studentName; }
    public void setStudentName(String studentName){ this.studentName = studentName; }
    public String getStudentNo(){ return studentNo; }
    public void setStudentNo(String studentNo){ this.studentNo = studentNo; }
    public Long getQuestionId(){ return questionId; }
    public void setQuestionId(Long questionId){ this.questionId = questionId; }
    public String getQuestionContent(){ return questionContent; }
    public void setQuestionContent(String questionContent){ this.questionContent = questionContent; }
    public Long getPaperId(){ return paperId; }
    public void setPaperId(Long paperId){ this.paperId = paperId; }
    public String getPaperName(){ return paperName; }
    public void setPaperName(String paperName){ this.paperName = paperName; }
    public String getWrongReason(){ return wrongReason; }
    public void setWrongReason(String wrongReason){ this.wrongReason = wrongReason; }
    public LocalDateTime getCreateTime(){ return createTime; }
    public void setCreateTime(LocalDateTime createTime){ this.createTime = createTime; }
}