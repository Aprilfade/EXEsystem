package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 学习时长统计DTO
 */
@Data
public class StudyTimeStatsDTO {
    private Integer totalMinutes;      // 总学习时长（分钟）
    private Integer todayMinutes;      // 今日学习时长
    private Integer weekMinutes;       // 本周学习时长
    private Integer monthMinutes;      // 本月学习时长
    private Integer avgDailyMinutes;   // 日均学习时长
}
