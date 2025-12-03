package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_homework")
public class BizHomework {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long classId;
    private String title;
    private Long paperId;
    private LocalDateTime deadline;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String paperName; // 用于前端显示
    @TableField(exist = false)
    private Integer status;   // 学生端显示状态

}