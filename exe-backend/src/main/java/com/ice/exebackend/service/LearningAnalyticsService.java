package com.ice.exebackend.service;

import com.ice.exebackend.dto.LearningAnalyticsDTO;

/**
 * 学习分析服务接口
 *
 * @author Claude Code
 * @since v3.08
 */
public interface LearningAnalyticsService {

    /**
     * 获取学生的学习分析数据
     *
     * @param studentId 学生ID
     * @param days      分析天数（7或30）
     * @return 学习分析数据
     */
    LearningAnalyticsDTO getStudentLearningAnalytics(Long studentId, Integer days);
}
