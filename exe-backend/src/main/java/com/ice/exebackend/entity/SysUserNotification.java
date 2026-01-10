package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户通知记录实体类
 *
 * @author Claude Code Assistant
 * @since v3.06
 */
@Data
@TableName("sys_user_notification")
public class SysUserNotification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 关联业务ID（如成绩ID）
     */
    private Long relatedId;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 通知类型常量
     */
    public static class Type {
        public static final String SCORE_UPDATE = "SCORE_UPDATE";
        public static final String COMMENT_UPDATE = "COMMENT_UPDATE";
        public static final String SYSTEM = "SYSTEM";
    }
}
