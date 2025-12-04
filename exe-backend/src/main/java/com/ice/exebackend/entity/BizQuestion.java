package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_question")
public class BizQuestion {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long subjectId; // 关联科目ID

    private String grade; // 【新增此行】

    private Integer questionType; // 题型: 1-单选, 2-多选, 3-填空, 4-判断, 5-主观题

    private String content; // 题干

    private String imageUrl; // 题目图片地址

    private String options; // 选项 (JSON格式字符串)

    private String answer; // 参考答案

    private String answerImageUrl; // 【新增】答案图片地址

    private String description; // 题目解析

    private Double difficulty;
    // 记得添加 Getter/Setter
    public Double getDifficulty() { return difficulty; }
    public void setDifficulty(Double difficulty) { this.difficulty = difficulty; }


    // 【新增】为 grade 字段添加 Getter 和 Setter
    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }


    public String getAnswerImageUrl() {
        return answerImageUrl;
    }

    public void setAnswerImageUrl(String answerImageUrl) {
        this.answerImageUrl = answerImageUrl;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
    public Integer getQuestionType() {
        return questionType;
    }
    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getOptions() {
        return options;
    }
    public void setOptions(String options) {
        this.options = options;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}