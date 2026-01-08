package com.ice.exebackend.dto;

import lombok.Data;

/**
 * 试卷分页查询参数封装
 * 用于接收前端传来的查询条件
 */
@Data
public class PaperPageParams {
    // 当前页码，默认1
    private Integer current = 1;
    // 每页条数，默认10
    private Integer size = 10;

    // 筛选条件
    private Integer subjectId;
    private String grade;
    private Integer status; // 0:草稿, 1:已发布

    // 搜索关键词（匹配试卷名称或编码）
    private String name;

    public Integer getCurrent() {
        return current;
    }
    public void setCurrent(Integer current) {
        this.current = current;
    }
    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    public Integer getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }
    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}