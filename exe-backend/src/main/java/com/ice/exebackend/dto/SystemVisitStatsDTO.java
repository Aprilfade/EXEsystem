package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 系统访问统计DTO
 *
 * @author System
 * @version v3.05
 */
@Data
public class SystemVisitStatsDTO {
    /**
     * 系统ID
     */
    private String systemId;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 访问次数
     */
    private Integer visitCount;

    /**
     * 最后访问时间
     */
    private String lastVisitTime;
}
