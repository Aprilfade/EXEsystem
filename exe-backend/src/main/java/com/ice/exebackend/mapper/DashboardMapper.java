package com.ice.exebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
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
     * 【修复点】: 将返回类型修改为 List<Map<String, Object>>
     * 这将与 XML 文件和 Service 实现保持一致。
     */
    List<Map<String, Object>> getKpAndQuestionStatsBySubject();
}