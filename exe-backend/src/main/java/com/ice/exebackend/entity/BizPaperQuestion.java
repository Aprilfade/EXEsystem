package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_paper_question")
public class BizPaperQuestion {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private Long questionId;
    private Integer score;
    private Integer sortOrder;

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
    public Long getQuestionId() {
        return questionId;
    }
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    public Integer getScore() {
        return score;
    }
    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}