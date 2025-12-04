package com.ice.exebackend.dto;

public class SmartPaperReq {
    private Long subjectId;      // 科目ID
    private String grade;        // 年级 (可选)

    // 各题型数量，给予默认值 0，防止空指针
    private Integer singleCount = 0;   // 单选题数量
    private Integer multiCount = 0;    // 多选题数量
    private Integer fillCount = 0;     // 填空题数量
    private Integer judgeCount = 0;    // 判断题数量
    private Integer subjectiveCount = 0; // 主观题数量

    private Double targetDifficulty = 0.5; // 默认难度 0.5

    // --- 手动添加 Getter 和 Setter ---

    public Double getTargetDifficulty() {
        return targetDifficulty;
    }

    public void setTargetDifficulty(Double targetDifficulty) {
        this.targetDifficulty = targetDifficulty;
    }
    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getSingleCount() {
        return singleCount;
    }

    public void setSingleCount(Integer singleCount) {
        this.singleCount = singleCount;
    }

    public Integer getMultiCount() {
        return multiCount;
    }

    public void setMultiCount(Integer multiCount) {
        this.multiCount = multiCount;
    }

    public Integer getFillCount() {
        return fillCount;
    }

    public void setFillCount(Integer fillCount) {
        this.fillCount = fillCount;
    }

    public Integer getJudgeCount() {
        return judgeCount;
    }

    public void setJudgeCount(Integer judgeCount) {
        this.judgeCount = judgeCount;
    }

    public Integer getSubjectiveCount() {
        return subjectiveCount;
    }

    public void setSubjectiveCount(Integer subjectiveCount) {
        this.subjectiveCount = subjectiveCount;
    }
}