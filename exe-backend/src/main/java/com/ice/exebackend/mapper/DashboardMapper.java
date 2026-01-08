package com.ice.exebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {
    Map<String, Long> getTopCardStats();
    List<Map<String, Object>> getKpAndQuestionStatsBySubject(@Param("month") String month);
    List<Map<String, Object>> getWrongQuestionStatsBySubject();
    List<Map<String, Object>> getMonthlyQuestionCreationStats();
    // 这是为"科目统计"图表准备的查询方法
    List<Map<String, Object>> getSubjectStatsByGrade();

    // 待办事项相关查询
    Map<String, Object> getPendingPapersCount();
    Map<String, Object> getPendingQuestionsCount();

    // 最近活动查询
    List<Map<String, Object>> getRecentActivities(@Param("limit") int limit);

    // 数据趋势查询
    Map<String, Object> getDataTrends();

    // 【知识点功能增强】知识点覆盖率统计
    List<Map<String, Object>> getKnowledgePointCoverage();

    // 【知识点功能增强】薄弱知识点Top10
    List<Map<String, Object>> getWeakKnowledgePointsTop10();
}