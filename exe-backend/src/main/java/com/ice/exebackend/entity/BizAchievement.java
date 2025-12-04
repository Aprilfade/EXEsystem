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

    // === 【修复】手动添加 Getter 和 Setter ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }

    public Integer getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(Integer rewardPoints) { this.rewardPoints = rewardPoints; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Boolean getIsUnlocked() { return isUnlocked; }
    public void setIsUnlocked(Boolean isUnlocked) { this.isUnlocked = isUnlocked; }

    public LocalDateTime getUnlockTime() { return unlockTime; }
    public void setUnlockTime(LocalDateTime unlockTime) { this.unlockTime = unlockTime; }
}