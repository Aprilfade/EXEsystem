package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class PaperDifficultyDTO {
    private Long paperId;
    private String paperName;
    private Integer totalStudents; // 参加考试/有记录的学生总数
    private Double averageErrorRate; // 整张试卷的平均错误率
    private Long easiestQuestionId; // 最简单的题 (错误人次最少)
    private String easiestQuestionContent;
    private Long hardestQuestionId; // 最难的题 (错误人次最多)
    private String hardestQuestionContent;
}