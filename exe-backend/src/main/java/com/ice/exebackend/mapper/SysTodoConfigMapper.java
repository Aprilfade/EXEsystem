package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.SysTodoConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 待办事项配置 Mapper 接口
 *
 * @author Claude
 * @date 2026-01-10
 */
@Mapper
public interface SysTodoConfigMapper extends BaseMapper<SysTodoConfig> {

    /**
     * 查询所有启用的待办事项配置
     * 按 sort_order 排序
     *
     * @return 待办事项配置列表
     */
    List<SysTodoConfig> selectEnabledConfigs();
}
