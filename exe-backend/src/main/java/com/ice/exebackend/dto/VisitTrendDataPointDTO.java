package com.ice.exebackend.dto;

import lombok.Data;

import java.util.Map;

/**
 * 访问趋势数据点DTO
 *
 * @author System
 * @version v3.05
 */
@Data
public class VisitTrendDataPointDTO {
    /**
     * 日期，格式：YYYY-MM-DD
     */
    private String date;

    /**
     * 访问次数
     */
    private Integer count;

    /**
     * 各系统的访问次数详情
     */
    private Map<String, Integer> systems;
}
