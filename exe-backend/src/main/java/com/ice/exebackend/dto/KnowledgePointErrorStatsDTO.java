package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class KnowledgePointErrorStatsDTO {
    private Long knowledgePointId;
    private String knowledgePointName;
    private String subjectName;
    private Long totalQuestions; // 该知识点下的题目总数
    private Long totalErrors;    // 总错误人次
}