package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_exam_result")
public class BizExamResult {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long paperId;
    private String paperName;
    private Integer score;
    private Integer totalScore;
    private String userAnswers; // JSON String
    private LocalDateTime createTime;
    private Integer violationCount; // 切屏次数
    // 存储 AI 分析结果的 JSON 字符串
    private String resultDetails;
    // 教师评语
    private String comment;
    // 是否发布给学生查看
    private Boolean published;
    // 批改状态：0-未提交，1-待批改（已提交未批阅），2-已批改（已批阅未发布），3-已发布
    private Integer status;
    // 批阅教师ID
    private Long gradedBy;
    // 批阅时间
    private LocalDateTime gradedTime;
    // AI自动评分（原始分数，不可修改）
    private Integer originalScore;
    // 乐观锁版本号
    @Version
    private Integer version;

    // === 手动添加 Getter 和 Setter ===
    public String getResultDetails() { return resultDetails; }
    public void setResultDetails(String resultDetails) { this.resultDetails = resultDetails; }

    public Integer getViolationCount() { return violationCount; }
    public void setViolationCount(Integer violationCount) { this.violationCount = violationCount; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    // 其他手动添加 Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public String getUserAnswers() { return userAnswers; }
    public void setUserAnswers(String userAnswers) { this.userAnswers = userAnswers; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}