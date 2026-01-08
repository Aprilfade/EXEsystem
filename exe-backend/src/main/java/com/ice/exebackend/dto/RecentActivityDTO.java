package com.ice.exebackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 最近活动 DTO
 *
 * @author ice
 */
public class RecentActivityDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 活动 ID
     */
    private Long id;

    /**
     * 活动类型
     * submit_paper - 提交试卷
     * complete_exam - 完成考试
     * create_question - 创建题目
     * import_student - 导入学生
     * publish_notice - 发布通知
     */
    private String type;

    /**
     * 活动内容描述
     */
    private String content;

    /**
     * 时间描述（如 "2分钟前"）
     */
    private String time;

    /**
     * 图标名称（前端使用）
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentActivityDTO that = (RecentActivityDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(type, that.type) &&
                Objects.equals(content, that.content) &&
                Objects.equals(time, that.time) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(color, that.color) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, content, time, icon, color, userId, userName, createTime);
    }

    @Override
    public String toString() {
        return "RecentActivityDTO{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", time='" + time + '\'' +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
