package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class PaperDifficultyDTO {
    private Long paperId;
    private String paperName;
    private Integer totalStudents; // 参加考试/有记录的学生总数
    private Double averageErrorRate; // 整张试卷的平均错误率
    private Long easiestQuestionId; // 最简单的题 (错误人次最少)
    private String easiestQuestionContent;
    private Long hardestQuestionId; // 最难的题 (错误人次最多)
    private String hardestQuestionContent;

    // === 手动添加 Getter 和 Setter 以解决序列化报错 ===

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public Integer getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Double getAverageErrorRate() {
        return averageErrorRate;
    }

    public void setAverageErrorRate(Double averageErrorRate) {
        this.averageErrorRate = averageErrorRate;
    }

    public Long getEasiestQuestionId() {
        return easiestQuestionId;
    }

    public void setEasiestQuestionId(Long easiestQuestionId) {
        this.easiestQuestionId = easiestQuestionId;
    }

    public String getEasiestQuestionContent() {
        return easiestQuestionContent;
    }

    public void setEasiestQuestionContent(String easiestQuestionContent) {
        this.easiestQuestionContent = easiestQuestionContent;
    }

    public Long getHardestQuestionId() {
        return hardestQuestionId;
    }

    public void setHardestQuestionId(Long hardestQuestionId) {
        this.hardestQuestionId = hardestQuestionId;
    }

    public String getHardestQuestionContent() {
        return hardestQuestionContent;
    }

    public void setHardestQuestionContent(String hardestQuestionContent) {
        this.hardestQuestionContent = hardestQuestionContent;
    }
}