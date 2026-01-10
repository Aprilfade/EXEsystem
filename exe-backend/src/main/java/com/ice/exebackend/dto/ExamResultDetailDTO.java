package com.ice.exebackend.dto;

import java.util.Date;
import java.util.Objects;

/**
 * 成绩查询增强 DTO
 * 包含学生、班级、科目等关联信息
 */
public class ExamResultDetailDTO {
    private Long id;
    private Long paperId;
    private String paperName;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private Integer score;
    private Integer totalScore;
    private Integer violationCount;
    private String userAnswers;
    private Date createTime;
    private String comment;
    private Boolean published;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(Integer violationCount) {
        this.violationCount = violationCount;
    }

    public String getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(String userAnswers) {
        this.userAnswers = userAnswers;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamResultDetailDTO that = (ExamResultDetailDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(paperId, that.paperId) &&
                Objects.equals(paperName, that.paperName) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(studentName, that.studentName) &&
                Objects.equals(studentNo, that.studentNo) &&
                Objects.equals(classId, that.classId) &&
                Objects.equals(className, that.className) &&
                Objects.equals(subjectId, that.subjectId) &&
                Objects.equals(subjectName, that.subjectName) &&
                Objects.equals(score, that.score) &&
                Objects.equals(totalScore, that.totalScore) &&
                Objects.equals(violationCount, that.violationCount) &&
                Objects.equals(userAnswers, that.userAnswers) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(published, that.published);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paperId, paperName, studentId, studentName, studentNo, classId,
                className, subjectId, subjectName, score, totalScore, violationCount,
                userAnswers, createTime, comment, published);
    }

    @Override
    public String toString() {
        return "ExamResultDetailDTO{" +
                "id=" + id +
                ", paperId=" + paperId +
                ", paperName='" + paperName + '\'' +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", studentNo='" + studentNo + '\'' +
                ", classId=" + classId +
                ", className='" + className + '\'' +
                ", subjectId=" + subjectId +
                ", subjectName='" + subjectName + '\'' +
                ", score=" + score +
                ", totalScore=" + totalScore +
                ", violationCount=" + violationCount +
                ", userAnswers='" + userAnswers + '\'' +
                ", createTime=" + createTime +
                ", comment='" + comment + '\'' +
                ", published=" + published +
                '}';
    }
}
