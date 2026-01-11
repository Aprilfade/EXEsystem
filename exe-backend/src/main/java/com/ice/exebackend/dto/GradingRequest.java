package com.ice.exebackend.dto;

import lombok.Data;

/**
 * AI智能批改请求DTO
 * 用于在线练习功能中的主观题批改
 */
@Data
public class GradingRequest {
    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 题目类型 (1:单选 2:多选 3:填空 4:判断 5:主观题 6:计算题)
     */
    private Integer questionType;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 正确答案/参考答案
     */
    private String correctAnswer;

    /**
     * 学生答案
     */
    private String userAnswer;

    /**
     * 题目满分 (可选,默认100)
     */
    private Integer maxScore;
}
