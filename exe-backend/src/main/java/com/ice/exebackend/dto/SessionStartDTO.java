package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 学习会话开始DTO
 * 用于开始学习会话
 */
@Data
public class SessionStartDTO {

    /** 课程ID */
    private Long courseId;

    /** 资源ID */
    private Long resourceId;

    // === 手动添加 Getter 和 Setter ===
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
}
