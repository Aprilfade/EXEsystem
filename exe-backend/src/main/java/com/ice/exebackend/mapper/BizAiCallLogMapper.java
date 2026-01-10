package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizAiCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BizAiCallLogMapper extends BaseMapper<BizAiCallLog> {

    /**
     * 统计用户AI调用次数
     */
    @Select("SELECT COUNT(*) FROM biz_ai_call_log WHERE user_id = #{userId} AND create_time >= #{startTime}")
    Integer countUserCalls(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 统计成功率
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) as success_count " +
            "FROM biz_ai_call_log " +
            "WHERE create_time >= #{startTime}")
    Map<String, Object> getSuccessRate(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计各功能使用情况
     */
    @Select("SELECT function_type, COUNT(*) as count " +
            "FROM biz_ai_call_log " +
            "WHERE create_time >= #{startTime} " +
            "GROUP BY function_type")
    List<Map<String, Object>> getFunctionStats(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计总成本
     */
    @Select("SELECT SUM(estimated_cost) FROM biz_ai_call_log WHERE create_time >= #{startTime}")
    Double getTotalCost(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取缓存命中率
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN cached = 1 THEN 1 ELSE 0 END) as cached_count " +
            "FROM biz_ai_call_log " +
            "WHERE create_time >= #{startTime}")
    Map<String, Object> getCacheHitRate(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取平均响应时间
     */
    @Select("SELECT AVG(response_time) FROM biz_ai_call_log " +
            "WHERE create_time >= #{startTime} AND success = 1")
    Double getAvgResponseTime(@Param("startTime") LocalDateTime startTime);
}
