package com.ice.exebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // 【新增】导入 @Param 注解
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    /**
     * 一次性获取顶部卡片需要的所有统计数据。
     * @return 一个 Map，键是统计项 (e.g., "studentCount")，值是对应的数量。
     */
    Map<String, Long> getTopCardStats();

    /**
     * 【修复点】:
     * 1. 在方法签名中添加 String month 参数。
     * 2. 使用 @Param("month") 注解，将这个Java方法的参数 "month"
     * 与 DashboardMapper.xml 文件中 SQL 语句里的 #{month} 占位符关联起来。
     */
    List<Map<String, Object>> getKpAndQuestionStatsBySubject(@Param("month") String month);

    List<Map<String, Object>> getWrongQuestionStatsBySubject();

    List<Map<String, Object>> getMonthlyQuestionCreationStats();
}