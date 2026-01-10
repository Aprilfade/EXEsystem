package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class AiAnalysisReq {
    private Long questionId;        // 题目ID
    private String questionContent; // 题干
    private String studentAnswer;   // 学生答案
    private String correctAnswer;   // 正确答案
    private String analysis;        // 原有解析（可选，供AI参考）
    private Long knowledgePointId;  // 知识点ID（用于知识图谱溯源）

    // === 手动添加 Getter 和 Setter (解决找不到符号的问题) ===

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(Long knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }
}