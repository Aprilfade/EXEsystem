package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 每日学习时长DTO
 */
@Data
public class DailyStudyTimeDTO {
    private String date;     // 日期 YYYY-MM-DD
    private Integer minutes; // 学习时长（分钟）
}
