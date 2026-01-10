package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 访问记录提交DTO
 *
 * @author System
 * @version v3.05
 */
@Data
public class VisitRecordDTO {
    /**
     * 系统ID
     */
    private String systemId;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 访问时间（ISO 8601格式）
     */
    private String visitTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型: admin/teacher/student
     */
    private String userType;
}
