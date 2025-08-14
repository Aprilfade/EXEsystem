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

    // 【新增】密码字段
    private String password;

    // 【新增】头像字段
    private String avatar;

    // 所属科目ID在导入时需要特殊处理，不直接映射
    private Long subjectId;

    @ExcelProperty("年级")
    private String grade;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 【新增】为新字段添加 Getter 和 Setter
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

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