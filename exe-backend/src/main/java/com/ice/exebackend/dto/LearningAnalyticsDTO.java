package com.ice.exebackend.dto;

import lombok.Data;
import java.util.List;

/**
 * 学习分析数据传输对象
 *
 * @author Claude Code
 * @since v3.08
 */
@Data
public class LearningAnalyticsDTO {

    /**
     * 学习时长趋势（最近7天或30天）
     */
    private List<StudyTimePoint> studyTimeTrend;

    /**
     * 知识点掌握度分析
     */
    private List<KnowledgeMasteryPoint> knowledgeMastery;

    /**
     * 弱项分析
     */
    private List<WeakPoint> weakPoints;

    /**
     * 学习建议
     */
    private String learningAdvice;

    /**
     * 学习时长数据点
     */
    @Data
    public static class StudyTimePoint {
        /**
         * 日期（格式：yyyy-MM-dd）
         */
        private String date;

        /**
         * 学习时长（分钟）
         */
        private Integer studyMinutes;

        /**
         * 完成题目数
         */
        private Integer questionCount;
    }

    /**
     * 知识点掌握度数据点
     */
    @Data
    public static class KnowledgeMasteryPoint {
        /**
         * 知识点ID
         */
        private Long knowledgePointId;

        /**
         * 知识点名称
         */
        private String knowledgePointName;

        /**
         * 掌握度（0-100）
         */
        private Double masteryRate;

        /**
         * 总题数
         */
        private Integer totalQuestions;

        /**
         * 正确题数
         */
        private Integer correctQuestions;
    }

    /**
     * 弱项数据点
     */
    @Data
    public static class WeakPoint {
        /**
         * 知识点ID
         */
        private Long knowledgePointId;

        /**
         * 知识点名称
         */
        private String knowledgePointName;

        /**
         * 得分率（0-100）
         */
        private Double scoreRate;

        /**
         * 错题数量
         */
        private Integer wrongCount;

        /**
         * 建议练习次数
         */
        private Integer recommendedPracticeCount;

        /**
         * 科目名称
         */
        private String subjectName;
    }
}
