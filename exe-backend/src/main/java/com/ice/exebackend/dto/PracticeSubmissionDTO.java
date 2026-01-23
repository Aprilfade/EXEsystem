package com.ice.exebackend.dto;

import java.util.Map;

// Lombok注解，自动生成getter/setter等
import lombok.Data;

@Data
public class PracticeSubmissionDTO {
    // 提交的答案，键是题目ID (Long)，值是学生的答案 (String)
    private Map<Long, String> answers;

    private Integer violationCount; // 【新增】
    private Integer duration; // 【新增】学习时长（秒）

    public Integer getViolationCount() { return violationCount; }
    public void setViolationCount(Integer violationCount) { this.violationCount = violationCount; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Map<Long, String> getAnswers() {
        return answers;
    }
    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}