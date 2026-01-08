package com.ice.exebackend.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * 待办事项 DTO
 *
 * @author ice
 */
public class TodoItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 待办类型
     * pending_papers - 待批改试卷
     * pending_questions - 待审核题目
     * pending_students - 待审核学生
     */
    private String type;

    /**
     * 标题
     */
    private String title;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 时间描述（如 "2小时前"）
     */
    private String time;

    /**
     * 图标名称（前端使用）
     */
    private String icon;

    /**
     * 颜色（渐变色）
     */
    private String color;

    /**
     * 跳转路由
     */
    private String action;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoItemDTO that = (TodoItemDTO) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(title, that.title) &&
                Objects.equals(count, that.count) &&
                Objects.equals(time, that.time) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(color, that.color) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, count, time, icon, color, action);
    }

    @Override
    public String toString() {
        return "TodoItemDTO{" +
                "type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", count=" + count +
                ", time='" + time + '\'' +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
