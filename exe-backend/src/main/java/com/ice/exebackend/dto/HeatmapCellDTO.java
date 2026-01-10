package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 热力图单元格数据DTO
 *
 * @author System
 * @version v3.05
 */
@Data
public class HeatmapCellDTO {
    /**
     * 日期，格式：YYYY-MM-DD
     */
    private String date;

    /**
     * 星期几，0-6 (周日-周六)
     */
    private Integer day;

    /**
     * 第几周
     */
    private Integer week;

    /**
     * 访问次数
     */
    private Integer count;

    /**
     * 热度等级，0-4
     */
    private Integer level;
}
