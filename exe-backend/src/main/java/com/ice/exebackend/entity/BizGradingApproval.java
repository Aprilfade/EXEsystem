package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 批阅审批实体类
 *
 * @author Claude Code Assistant
 * @since v3.07
 */
@Data
@TableName("biz_grading_approval")
public class BizGradingApproval {

    /**
     * 审批状态常量
     */
    public static final String STATUS_PENDING = "PENDING";    // 待审批
    public static final String STATUS_APPROVED = "APPROVED";  // 已通过
    public static final String STATUS_REJECTED = "REJECTED";  // 已驳回

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考试结果ID
     */
    private Long examResultId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 试卷标题
     */
    private String paperTitle;

    /**
     * 原分数
     */
    private Integer oldScore;

    /**
     * 新分数
     */
    private Integer newScore;

    /**
     * 分数变化（正数表示加分，负数表示扣分）
     */
    private Integer scoreChange;

    /**
     * 分数变化百分比
     */
    private BigDecimal changePercentage;

    /**
     * 原评语
     */
    private String oldComment;

    /**
     * 新评语
     */
    private String newComment;

    /**
     * 批阅人ID（申请人）
     */
    private Long graderId;

    /**
     * 批阅人姓名
     */
    private String graderName;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 审批状态: PENDING-待审批, APPROVED-已通过, REJECTED-已驳回
     */
    private String status;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
