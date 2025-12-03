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

    // === 手动添加 Getter 和 Setter (解决找不到符号的问题) ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}