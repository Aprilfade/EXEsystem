package com.ice.exebackend.dto;

import java.util.Map;

// Lombok注解，自动生成getter/setter等
import lombok.Data;

@Data
public class PracticeSubmissionDTO {
    // 提交的答案，键是题目ID (Long)，值是学生的答案 (String)
    private Map<Long, String> answers;


    public Map<Long, String> getAnswers() {
        return answers;
    }
    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}