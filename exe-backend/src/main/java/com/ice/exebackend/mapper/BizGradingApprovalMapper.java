package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.entity.BizGradingApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 批阅审批Mapper
 *
 * @author Claude Code Assistant
 * @since v3.07
 */
@Mapper
public interface BizGradingApprovalMapper extends BaseMapper<BizGradingApproval> {

    /**
     * 分页查询审批记录（带详细信息）
     *
     * @param page   分页对象
     * @param params 查询参数
     * @return 审批记录列表
     */
    Page<BizGradingApproval> selectApprovalPage(Page<BizGradingApproval> page, @Param("params") Map<String, Object> params);

    /**
     * 统计各状态审批数量
     *
     * @return 状态统计
     */
    List<Map<String, Object>> countByStatus();

    /**
     * 查询待审批数量
     *
     * @return 待审批数量
     */
    Integer countPending();
}
