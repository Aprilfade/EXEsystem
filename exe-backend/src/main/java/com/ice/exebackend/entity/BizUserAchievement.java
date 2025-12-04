package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_user_achievement")
public class BizUserAchievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long achievementId;
    private LocalDateTime createTime;
}