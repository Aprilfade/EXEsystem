package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class AiGradingResult {
    private Integer score;      // AI 给出的得分
    private String feedback;    // AI 的评语/分析
    private String reason;      // 扣分原因 (可选)

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
}