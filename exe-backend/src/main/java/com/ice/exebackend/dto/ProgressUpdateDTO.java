package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 学习进度更新DTO
 * 用于前端提交学习进度更新
 */
@Data
public class ProgressUpdateDTO {

    /** 资源ID */
    private Long resourceId;

    /** 完成百分比（0-100） */
    private Integer progressPercent;

    /** 最后学习位置（视频秒数/PDF页码） */
    private String lastPosition;

    /** 累计学习时长（秒） */
    private Integer studyDuration;

    // === 手动添加 Getter 和 Setter ===
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }

    public String getLastPosition() { return lastPosition; }
    public void setLastPosition(String lastPosition) { this.lastPosition = lastPosition; }

    public Integer getStudyDuration() { return studyDuration; }
    public void setStudyDuration(Integer studyDuration) { this.studyDuration = studyDuration; }
}
