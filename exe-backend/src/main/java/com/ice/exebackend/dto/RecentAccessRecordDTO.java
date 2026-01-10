package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 最近访问记录DTO
 *
 * @author System
 * @version v3.05
 */
@Data
public class RecentAccessRecordDTO {
    /**
     * 系统ID
     */
    private String id;

    /**
     * 系统名称
     */
    private String name;

    /**
     * 图标（前端使用）
     */
    private String icon;

    /**
     * 相对时间，如 "5分钟前"
     */
    private String time;

    /**
     * 路径
     */
    private String path;

    /**
     * 渐变色（前端使用）
     */
    private String gradient;
}
