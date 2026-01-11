package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐记录表
 * 记录每次推荐的详细信息，用于效果分析和A/B测试
 */
@Data
@TableName("biz_recommendation_log")
public class BizRecommendationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 推荐物品ID
     */
    private String itemId;

    /**
     * 物品类型
     */
    private String itemType;

    /**
     * 推荐分数
     */
    private Double score;

    /**
     * 推荐理由
     */
    private String reason;

    /**
     * 置信度（0-100）
     */
    private Double confidence;

    /**
     * 推荐算法类型：cf-协同过滤, cbf-内容推荐, hybrid-混合, popular-流行度
     */
    private String algorithm;

    /**
     * 推荐策略版本（用于A/B测试）
     */
    private String strategyVersion;

    /**
     * 推荐位置（第几个）
     */
    private Integer position;

    /**
     * 是否被点击
     */
    private Boolean clicked;

    /**
     * 点击时间
     */
    private LocalDateTime clickTime;

    /**
     * 是否完成学习
     */
    private Boolean completed;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 学习时长（秒）
     */
    private Integer studyDuration;

    /**
     * 学习效果（得分）
     */
    private Integer studyScore;

    /**
     * 推荐时间
     */
    private LocalDateTime recommendTime;

    /**
     * 推荐解释（JSON格式）
     */
    private String explanation;

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
