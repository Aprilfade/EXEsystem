package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.BizPortalVisit;

import java.util.List;
import java.util.Map;

/**
 * Portal访问记录服务接口
 *
 * @author System
 * @version v3.05
 */
public interface PortalService extends IService<BizPortalVisit> {

    /**
     * 获取系统访问统计
     *
     * @param days 统计天数
     * @param userId 用户ID（可选）
     * @return 统计列表
     */
    List<SystemVisitStatsDTO> getSystemVisitStats(Integer days, Long userId);

    /**
     * 获取访问趋势数据
     *
     * @param days 统计天数
     * @param userId 用户ID（可选）
     * @return 趋势数据
     */
    List<VisitTrendDataPointDTO> getVisitTrendData(Integer days, Long userId);

    /**
     * 获取系统使用分布
     *
     * @param userId 用户ID（可选）
     * @return 使用分布数据
     */
    List<Map<String, Object>> getSystemUsageDistribution(Long userId);

    /**
     * 获取热力图数据
     *
     * @param days 统计天数
     * @param userId 用户ID（可选）
     * @return 热力图数据
     */
    List<HeatmapCellDTO> getHeatmapData(Integer days, Long userId);

    /**
     * 记录访问
     *
     * @param visitRecord 访问记录
     */
    void recordVisit(VisitRecordDTO visitRecord);

    /**
     * 批量记录访问
     *
     * @param records 访问记录列表
     */
    void batchRecordVisits(List<VisitRecordDTO> records);

    /**
     * 获取最近访问记录
     *
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 最近访问列表
     */
    List<RecentAccessRecordDTO> getRecentAccess(Long userId, Integer limit);

    /**
     * 获取访问统计汇总
     *
     * @param userId 用户ID（可选）
     * @return 统计汇总
     */
    Map<String, Object> getVisitSummary(Long userId);
}
