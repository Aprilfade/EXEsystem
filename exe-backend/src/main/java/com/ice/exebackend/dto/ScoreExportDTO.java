package com.ice.exebackend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;

import java.util.Date;
import java.util.Objects;

/**
 * 成绩导出 DTO
 */
public class ScoreExportDTO {
    @ExcelProperty("试卷名称")
    private String paperName;

    @ExcelProperty("学生姓名")
    private String studentName;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("科目")
    private String subjectName;

    @ExcelProperty("分数")
    private Integer score;

    @ExcelProperty("总分")
    private Integer totalScore;

    @ExcelProperty("切屏次数")
    private Integer violationCount;

    @ExcelProperty("提交时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ExcelProperty("评语")
    private String comment;

    @ExcelProperty("状态")
    private String status;

    // Getters and Setters
    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreExportDTO that = (ScoreExportDTO) o;
        return Objects.equals(paperName, that.paperName) &&
                Objects.equals(studentName, that.studentName) &&
                Objects.equals(studentNo, that.studentNo) &&
                Objects.equals(className, that.className) &&
                Objects.equals(subjectName, that.subjectName) &&
                Objects.equals(score, that.score) &&
                Objects.equals(totalScore, that.totalScore) &&
                Objects.equals(violationCount, that.violationCount) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paperName, studentName, studentNo, className, subjectName,
                score, totalScore, violationCount, createTime, comment, status);
    }

    @Override
    public String toString() {
        return "ScoreExportDTO{" +
                "paperName='" + paperName + '\'' +
                ", studentName='" + studentName + '\'' +
                ", studentNo='" + studentNo + '\'' +
                ", className='" + className + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", score=" + score +
                ", totalScore=" + totalScore +
                ", violationCount=" + violationCount +
                ", createTime=" + createTime +
                ", comment='" + comment + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
