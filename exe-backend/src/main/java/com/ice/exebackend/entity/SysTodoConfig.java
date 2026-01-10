package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待办事项配置实体类
 * 用于配置化管理待办事项的展示和行为
 *
 * @author Claude
 * @date 2026-01-10
 */
@Data
@TableName("sys_todo_config")
public class SysTodoConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 待办类型唯一标识
     * 如：pending_papers, pending_questions, expiring_exams
     */
    @TableField("type")
    private String type;

    /**
     * 待办标题（显示在前端）
     * 如：待批改试卷、待审核题目
     */
    @TableField("title")
    private String title;

    /**
     * 图标名称（Element Plus图标）
     * 如：Document, Files, Timer, Warning
     */
    @TableField("icon")
    private String icon;

    /**
     * 渐变色CSS
     * 如：linear-gradient(135deg, #667eea 0%, #764ba2 100%)
     */
    @TableField("color")
    private String color;

    /**
     * 跳转路由（不含前导斜杠）
     * 如：score-manage?status=1, questions
     */
    @TableField("action")
    private String action;

    /**
     * Mapper方法名
     * 对应 DashboardMapper 中的查询方法
     * 如：getPendingPapersCount, getPendingQuestionsCount
     */
    @TableField("mapper_method")
    private String mapperMethod;

    /**
     * 是否启用
     * 1-启用，0-禁用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 排序序号
     * 越小越靠前，相同序号按待办数量排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 说明描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
