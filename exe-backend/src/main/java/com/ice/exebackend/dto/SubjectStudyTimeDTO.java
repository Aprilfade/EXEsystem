package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 科目学习时长DTO
 */
@Data
public class SubjectStudyTimeDTO {
    private Long subjectId;      // 科目ID
    private String subjectName;  // 科目名称
    private Integer minutes;     // 学习时长（分钟）
    private Double percentage;   // 百分比
}
