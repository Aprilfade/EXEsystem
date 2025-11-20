package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_paper")
public class BizPaper {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Long subjectId;
    private String description;
    private String grade; // 【新增此行】
    private Integer totalScore;
    private Integer paperType; // 【新增】 1-手动选题, 2-图片拼接
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status; // 【新增】状态字段


    // 【新增】添加 Getter 和 Setter
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    // 【新增】为 grade 字段添加 Getter 和 Setter
    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public Integer getPaperType() {
        return paperType;
    }
    public void setPaperType(Integer paperType) {
        this.paperType = paperType;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public Integer getTotalScore() {
        return totalScore;
    }
    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}