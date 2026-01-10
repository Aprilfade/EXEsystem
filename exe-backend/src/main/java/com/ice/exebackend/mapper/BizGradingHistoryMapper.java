package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizGradingHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 批阅历史记录Mapper
 *
 * @author Claude Code Assistant
 * @since v3.04
 */
@Mapper
public interface BizGradingHistoryMapper extends BaseMapper<BizGradingHistory> {

    /**
     * 根据成绩ID查询批阅历史
     *
     * @param examResultId 成绩ID
     * @return 批阅历史列表，按时间倒序
     */
    List<BizGradingHistory> selectByExamResultId(@Param("examResultId") Long examResultId);

    /**
     * 根据批阅人ID查询批阅历史
     *
     * @param graderId 批阅人ID
     * @param limit 限制数量
     * @return 批阅历史列表
     */
    List<BizGradingHistory> selectByGraderId(@Param("graderId") Long graderId, @Param("limit") Integer limit);

    /**
     * 统计批阅人的操作次数
     *
     * @param graderId 批阅人ID
     * @return 操作次数
     */
    Integer countByGraderId(@Param("graderId") Long graderId);
}
