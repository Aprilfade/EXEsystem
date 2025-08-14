package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class QuestionPageParams {
    private int current = 1;
    private int size = 10;
    private Long subjectId;
    private Integer questionType;
    private String grade;
    private String content;

    // --- 手动添加 Getter 和 Setter ---
    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Integer getQuestionType() { return questionType; }
    public void setQuestionType(Integer questionType) { this.questionType = questionType; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}