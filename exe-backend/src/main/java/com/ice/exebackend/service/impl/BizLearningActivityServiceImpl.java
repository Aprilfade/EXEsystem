package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.BizLearningActivity;
import com.ice.exebackend.mapper.BizLearningActivityMapper;
import com.ice.exebackend.service.BizAchievementService;
import com.ice.exebackend.service.BizLearningActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BizLearningActivityServiceImpl extends ServiceImpl<BizLearningActivityMapper, BizLearningActivity> implements BizLearningActivityService {

    @Autowired
    private BizLearningActivityMapper learningActivityMapper;

    @Lazy
    @Autowired
    private BizAchievementService achievementService;

    @Override
    public StudyTimeStatsDTO getStudyTimeStats(Long studentId) {
        Map<String, Object> map = learningActivityMapper.getStudyTimeStats(studentId);
        StudyTimeStatsDTO dto = new StudyTimeStatsDTO();
        dto.setTotalMinutes(((Number) map.getOrDefault("totalMinutes", 0)).intValue());
        dto.setTodayMinutes(((Number) map.getOrDefault("todayMinutes", 0)).intValue());
        dto.setWeekMinutes(((Number) map.getOrDefault("weekMinutes", 0)).intValue());
        dto.setMonthMinutes(((Number) map.getOrDefault("monthMinutes", 0)).intValue());
        dto.setAvgDailyMinutes(((Number) map.getOrDefault("avgDailyMinutes", 0)).intValue());
        return dto;
    }

    @Override
    public List<DailyStudyTimeDTO> getDailyStudyTime(Long studentId, Integer days) {
        List<Map<String, Object>> list = learningActivityMapper.getDailyStudyTime(studentId, days);
        return list.stream().map(map -> {
            DailyStudyTimeDTO dto = new DailyStudyTimeDTO();
            dto.setDate(map.get("date").toString());
            dto.setMinutes(((Number) map.getOrDefault("minutes", 0)).intValue());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ActivityTypeStatsDTO> getActivityTypeStats(Long studentId, Integer days) {
        List<Map<String, Object>> list = learningActivityMapper.getActivityTypeStats(studentId, days);
        return list.stream().map(map -> {
            ActivityTypeStatsDTO dto = new ActivityTypeStatsDTO();
            dto.setActivityType((String) map.get("activityType"));
            dto.setMinutes(((Number) map.getOrDefault("minutes", 0)).intValue());
            Object percentage = map.get("percentage");
            if (percentage != null) {
                dto.setPercentage(((Number) percentage).doubleValue());
            } else {
                dto.setPercentage(0.0);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SubjectStudyTimeDTO> getSubjectStudyTime(Long studentId, Integer days) {
        List<Map<String, Object>> list = learningActivityMapper.getSubjectStudyTime(studentId, days);
        return list.stream().map(map -> {
            SubjectStudyTimeDTO dto = new SubjectStudyTimeDTO();
            dto.setSubjectId(((Number) map.get("subjectId")).longValue());
            dto.setSubjectName((String) map.get("subjectName"));
            dto.setMinutes(((Number) map.getOrDefault("minutes", 0)).intValue());
            Object percentage = map.get("percentage");
            if (percentage != null) {
                dto.setPercentage(((Number) percentage).doubleValue());
            } else {
                dto.setPercentage(0.0);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public StudyRankingDTO getStudyRanking(Long studentId) {
        Map<String, Object> map = learningActivityMapper.getStudyRanking(studentId);
        StudyRankingDTO dto = new StudyRankingDTO();

        if (map != null && !map.isEmpty()) {
            Object rankObj = map.get("rank");
            dto.setRank(rankObj != null ? ((Number) rankObj).intValue() : 0);

            Object totalObj = map.get("totalStudents");
            dto.setTotalStudents(totalObj != null ? ((Number) totalObj).intValue() : 0);

            Object percentileObj = map.get("percentile");
            dto.setPercentile(percentileObj != null ? ((Number) percentileObj).doubleValue() : 0.0);
        } else {
            dto.setRank(0);
            dto.setTotalStudents(0);
            dto.setPercentile(0.0);
        }

        return dto;
    }

    @Override
    public void recordActivity(Long studentId, String activityType, Integer durationSeconds,
                               Long subjectId, Long relatedId, String description) {
        BizLearningActivity activity = new BizLearningActivity();
        activity.setStudentId(studentId);
        activity.setActivityType(activityType);
        activity.setDurationSeconds(durationSeconds);
        activity.setSubjectId(subjectId);
        activity.setRelatedId(relatedId);
        activity.setDescription(description);

        // 自动计算开始和结束时间
        LocalDateTime now = LocalDateTime.now();
        activity.setEndTime(now);
        activity.setStartTime(now.minusSeconds(durationSeconds));
        activity.setCreateTime(now);

        this.save(activity);

        // 检查学习时长成就（累计学习时长，单位：分钟）
        StudyTimeStatsDTO stats = getStudyTimeStats(studentId);
        achievementService.checkAndAward(studentId, "LEARNING_TIME", stats.getTotalMinutes());
    }

    @Override
    public String startSession(Long studentId, String activityType, Long subjectId) {
        String sessionId = UUID.randomUUID().toString();

        BizLearningActivity activity = new BizLearningActivity();
        activity.setStudentId(studentId);
        activity.setActivityType(activityType);
        activity.setSubjectId(subjectId);
        activity.setSessionId(sessionId);
        activity.setStartTime(LocalDateTime.now());
        activity.setDurationSeconds(0);
        activity.setCreateTime(LocalDateTime.now());

        this.save(activity);
        return sessionId;
    }

    @Override
    public void updateSessionDuration(String sessionId, Integer durationSeconds) {
        BizLearningActivity activity = this.lambdaQuery()
                .eq(BizLearningActivity::getSessionId, sessionId)
                .one();

        if (activity == null) {
            return;
        }

        // 服务端时间校验：防止作弊
        long actualElapsed = Duration.between(activity.getStartTime(), LocalDateTime.now()).getSeconds();
        if (durationSeconds > actualElapsed * 1.2) {
            // 上报时长不能超过实际经过时间的1.2倍（允许20%误差）
            durationSeconds = (int) actualElapsed;
        }

        this.lambdaUpdate()
                .eq(BizLearningActivity::getSessionId, sessionId)
                .set(BizLearningActivity::getDurationSeconds, durationSeconds)
                .update();
    }

    @Override
    public void endSession(String sessionId) {
        this.lambdaUpdate()
                .eq(BizLearningActivity::getSessionId, sessionId)
                .set(BizLearningActivity::getEndTime, LocalDateTime.now())
                .update();
    }
}
