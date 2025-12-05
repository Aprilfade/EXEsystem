package com.ice.exebackend.dto;

import com.ice.exebackend.entity.BizQuestion;
import lombok.Data;
import java.util.List;

@Data
public class PracticeResultDTO {
    private int totalQuestions;
    private int correctCount;
    private List<AnswerResult> results;

    // 【新增】携带本次获得的奖励信息
    private Integer expGain;
    private Integer pointsGain;

    @Data
    public static class AnswerResult {
        private BizQuestion question;
        private String userAnswer;
        private boolean isCorrect;
        private Integer earnedScore;

        // ... AnswerResult 的 Getter/Setter ...
        public Integer getEarnedScore() { return earnedScore; }
        public void setEarnedScore(Integer earnedScore) { this.earnedScore = earnedScore; }
        public BizQuestion getQuestion() { return question; }
        public void setQuestion(BizQuestion question) { this.question = question; }
        public String getUserAnswer() { return userAnswer; }
        public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
    }

    // ... PracticeResultDTO 的 Getter/Setter ...
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public List<AnswerResult> getResults() { return results; }
    public void setResults(List<AnswerResult> results) { this.results = results; }

    // 【新增】Getter/Setter
    public Integer getExpGain() { return expGain; }
    public void setExpGain(Integer expGain) { this.expGain = expGain; }
    public Integer getPointsGain() { return pointsGain; }
    public void setPointsGain(Integer pointsGain) { this.pointsGain = pointsGain; }
}