package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为记录表
 * 用于记录用户的所有学习行为，支持智能推荐算法
 */
@Data
@TableName("biz_user_behavior")
public class BizUserBehavior {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 物品ID（题目ID、课程ID等）
     */
    private String itemId;

    /**
     * 物品类型：question-题目, course-课程, knowledge_point-知识点
     */
    private String itemType;

    /**
     * 行为类型：view-浏览, practice-练习, collect-收藏, correct-答对, wrong-答错
     */
    private String behaviorType;

    /**
     * 行为时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 学习时长（秒）
     */
    private Integer duration;

    /**
     * 得分（0-100）
     */
    private Integer score;

    /**
     * 难度（1-5）
     */
    private Integer difficulty;

    /**
     * 科目
     */
    private String subject;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 扩展字段（JSON格式）
     */
    private String extraData;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
