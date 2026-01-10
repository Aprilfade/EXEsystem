package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.dto.HeatmapCellDTO;
import com.ice.exebackend.dto.SystemVisitStatsDTO;
import com.ice.exebackend.dto.VisitTrendDataPointDTO;
import com.ice.exebackend.entity.BizPortalVisit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Portal访问记录Mapper
 *
 * @author System
 * @version v3.05
 */
@Mapper
public interface BizPortalVisitMapper extends BaseMapper<BizPortalVisit> {

    /**
     * 获取系统访问统计
     *
     * @param startTime 开始时间
     * @param userId 用户ID（可选）
     * @return 统计列表
     */
    List<SystemVisitStatsDTO> getSystemVisitStats(@Param("startTime") LocalDateTime startTime,
                                                    @Param("userId") Long userId);

    /**
     * 获取访问趋势数据
     *
     * @param startTime 开始时间
     * @param userId 用户ID（可选）
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getVisitTrendData(@Param("startTime") LocalDateTime startTime,
                                                  @Param("userId") Long userId);

    /**
     * 获取热力图数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID（可选）
     * @return 热力图数据
     */
    List<Map<String, Object>> getHeatmapData(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("userId") Long userId);

    /**
     * 获取最近访问记录
     *
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 最近访问列表
     */
    List<Map<String, Object>> getRecentAccess(@Param("userId") Long userId,
                                                @Param("limit") Integer limit);

    /**
     * 获取访问统计汇总
     *
     * @param userId 用户ID（可选）
     * @return 统计汇总
     */
    Map<String, Object> getVisitSummary(@Param("userId") Long userId);
}
