package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 活动类型统计DTO
 */
@Data
public class ActivityTypeStatsDTO {
    private String activityType; // 活动类型
    private Integer minutes;     // 学习时长（分钟）
    private Double percentage;   // 百分比
}
