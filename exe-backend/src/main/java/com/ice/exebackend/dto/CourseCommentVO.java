package com.ice.exebackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseCommentVO {
    private Long id;
    private Long courseId;
    private String content;
    private LocalDateTime createTime;

    // 学生信息
    private Long studentId;
    private String studentName;
    private String studentAvatar;
    private String avatarFrameStyle; // 头像框

    // 标记是否是当前登录用户发的（用于显示删除按钮）
    private Boolean isSelf;

    // 手动 Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentAvatar() { return studentAvatar; }
    public void setStudentAvatar(String studentAvatar) { this.studentAvatar = studentAvatar; }
    public String getAvatarFrameStyle() { return avatarFrameStyle; }
    public void setAvatarFrameStyle(String avatarFrameStyle) { this.avatarFrameStyle = avatarFrameStyle; }
    public Boolean getIsSelf() { return isSelf; }
    public void setIsSelf(Boolean isSelf) { this.isSelf = isSelf; }
}