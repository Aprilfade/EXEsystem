package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_achievement")
public class BizAchievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String type; // SIGN_IN_STREAK, TOTAL_QUESTIONS, PERFECT_PAPER
    private Integer threshold;
    private Integer rewardPoints;
    private LocalDateTime createTime;

    // 辅助字段：当前用户是否已获得
    @TableField(exist = false)
    private Boolean isUnlocked;

    // 辅助字段：获得时间
    @TableField(exist = false)
    private LocalDateTime unlockTime;
}