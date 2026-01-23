package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizLearningActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface BizLearningActivityMapper extends BaseMapper<BizLearningActivity> {

    /**
     * 获取学生学习时长统计（总计、今日、本周、本月）
     */
    Map<String, Object> getStudyTimeStats(@Param("studentId") Long studentId);

    /**
     * 获取每日学习时长（最近N天）
     */
    List<Map<String, Object>> getDailyStudyTime(@Param("studentId") Long studentId, @Param("days") Integer days);

    /**
     * 按活动类型统计时长
     */
    List<Map<String, Object>> getActivityTypeStats(@Param("studentId") Long studentId, @Param("days") Integer days);

    /**
     * 按科目统计学习时长
     */
    List<Map<String, Object>> getSubjectStudyTime(@Param("studentId") Long studentId, @Param("days") Integer days);

    /**
     * 获取学习排名
     */
    Map<String, Object> getStudyRanking(@Param("studentId") Long studentId);
}