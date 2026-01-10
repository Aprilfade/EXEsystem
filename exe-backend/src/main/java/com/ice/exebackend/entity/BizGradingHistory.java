package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 批阅历史记录实体类
 * 用于记录成绩批阅的完整历史，支持审计追踪
 *
 * @author Claude Code Assistant
 * @since v3.04
 */
@Data
@TableName("biz_grading_history")
public class BizGradingHistory {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考试成绩ID
     */
    private Long examResultId;

    /**
     * 批阅人ID（教师）
     */
    private Long graderId;

    /**
     * 批阅人姓名
     */
    private String graderName;

    /**
     * 操作类型
     * UPDATE_SCORE - 修改分数
     * UPDATE_COMMENT - 修改评语
     * BATCH_UPDATE - 批量修改
     */
    private String actionType;

    /**
     * 修改前分数
     */
    private Integer oldScore;

    /**
     * 修改后分数
     */
    private Integer newScore;

    /**
     * 修改前评语
     */
    private String oldComment;

    /**
     * 修改后评语
     */
    private String newComment;

    /**
     * 修改原因（可选）
     */
    private String reason;

    /**
     * 操作时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 操作类型常量
     */
    public static class ActionType {
        public static final String UPDATE_SCORE = "UPDATE_SCORE";
        public static final String UPDATE_COMMENT = "UPDATE_COMMENT";
        public static final String BATCH_UPDATE = "BATCH_UPDATE";
    }
}
