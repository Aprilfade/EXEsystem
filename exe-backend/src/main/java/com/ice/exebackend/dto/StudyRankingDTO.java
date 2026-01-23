package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 学习排名DTO
 */
@Data
public class StudyRankingDTO {
    private Integer rank;          // 排名
    private Integer totalStudents; // 总学生数
    private Double percentile;     // 百分位
}
