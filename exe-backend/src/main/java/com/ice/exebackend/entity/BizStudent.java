package com.ice.exebackend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_student")
public class BizStudent {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("联系方式")
    private String contact;

    // 所属科目ID在导入时需要特殊处理，不直接映射
    private Long subjectId;

    @ExcelProperty("年级")
    private String grade;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getStudentNo(){
        return studentNo;
    }
    public void setStudentNo(String studentNo){
        this.studentNo = studentNo;
    }
    public String getContact(){
        return contact;
    }
    public void setContact(String contact){
        this.contact = contact;
    }
    public Long getSubjectId(){
        return subjectId;
    }
    public void setSubjectId(Long subjectId){
        this.subjectId = subjectId;
    }
    public String getGrade(){
        return grade;
    }
    public void setGrade(String grade){
        this.grade = grade;
    }
    public LocalDateTime getCreateTime(){
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime){
        this.createTime = createTime;
    }
    public LocalDateTime getUpdateTime(){
        return updateTime;
    }
    public void setUpdateTime(LocalDateTime updateTime){
        this.updateTime = updateTime;
    }
}