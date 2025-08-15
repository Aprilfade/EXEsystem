package com.ice.exebackend.dto;

import java.util.List;
import lombok.Data;

@Data
public class QuestionBatchUpdateDTO {
    private List<Long> questionIds;
    private Long subjectId;
    private String grade;

    public List<Long> getQuestionIds() {
        return questionIds;
    }
    public void setQuestionIds(List<Long> questionIds) {
        this.questionIds = questionIds;
    }
    public Long getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
}