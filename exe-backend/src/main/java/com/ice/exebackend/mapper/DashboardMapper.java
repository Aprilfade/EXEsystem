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
    // 这是为“科目统计”图表准备的查询方法
    List<Map<String, Object>> getSubjectStatsByGrade();
}