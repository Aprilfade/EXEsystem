package com.ice.exebackend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StudentAbilityDTO {
    // 用于雷达图
    private List<String> radarLabels; // e.g., ["单选题", "多选题", "知识点A", "知识点B"]
    private List<Double> errorRates;  // 对应的错误率 [0.2, 0.5, 0.8, 0.1]

    // 可选：用于更详细的表格展示
    private Map<String, Long> errorsByQuestionType;
    private List<KnowledgePointErrorStatsDTO> errorsByKnowledgePoint;
}