package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.BizLearningActivity;

import java.util.List;

public interface BizLearningActivityService extends IService<BizLearningActivity> {

    /**
     * 获取学习时长统计
     */
    StudyTimeStatsDTO getStudyTimeStats(Long studentId);

    /**
     * 获取每日学习时长
     */
    List<DailyStudyTimeDTO> getDailyStudyTime(Long studentId, Integer days);

    /**
     * 按活动类型统计
     */
    List<ActivityTypeStatsDTO> getActivityTypeStats(Long studentId, Integer days);

    /**
     * 按科目统计学习时长
     */
    List<SubjectStudyTimeDTO> getSubjectStudyTime(Long studentId, Integer days);

    /**
     * 获取学习排名
     */
    StudyRankingDTO getStudyRanking(Long studentId);

    /**
     * 记录学习活动（带时长）
     */
    void recordActivity(Long studentId, String activityType, Integer durationSeconds,
                       Long subjectId, Long relatedId, String description);

    /**
     * 开始学习会话（前端心跳模式）
     */
    String startSession(Long studentId, String activityType, Long subjectId);

    /**
     * 更新学习会话时长（心跳）
     */
    void updateSessionDuration(String sessionId, Integer durationSeconds);

    /**
     * 结束学习会话
     */
    void endSession(String sessionId);
}