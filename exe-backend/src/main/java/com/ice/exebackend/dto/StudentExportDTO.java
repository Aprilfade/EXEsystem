package com.ice.exebackend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

@Data
@HeadRowHeight(20) // 设置表头高度
public class StudentExportDTO {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("所属科目")
    private String subjectName;

    @ExcelProperty("年级")
    private String grade;

    @ExcelProperty("联系方式")
    private String contact;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStudentNo() {
        return studentNo;
    }
    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public String getSubjectName(){
        return subjectName;
    }
    public void setSubjectName(String subjectName){
        this.subjectName = subjectName;
    }
    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
}