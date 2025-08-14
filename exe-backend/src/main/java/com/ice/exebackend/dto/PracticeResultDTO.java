package com.ice.exebackend.dto;

import com.ice.exebackend.entity.BizQuestion;
import lombok.Data;
import java.util.List;

@Data
public class PracticeResultDTO {
    private int totalQuestions;
    private int correctCount;
    private List<AnswerResult> results;

    @Data
    public static class AnswerResult {
        private BizQuestion question; // 包含题目完整信息
        private String userAnswer;
        private boolean isCorrect;

        public BizQuestion getQuestion() {
            return question;
        }
        public void setQuestion(BizQuestion question) {
            this.question = question;
        }
        public String getUserAnswer() {
            return userAnswer;
        }
        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }
        public boolean isCorrect() {
            return isCorrect;
        }
        public void setCorrect(boolean correct) {
            isCorrect = correct;
        }
    }
    public int getTotalQuestions() {
        return totalQuestions;
    }
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    public int getCorrectCount() {
        return correctCount;
    }
    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }
    public List<AnswerResult> getResults() {
        return results;
    }
    public void setResults(List<AnswerResult> results) {
        this.results = results;
    }
}