package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizKnowledgePoint;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizKnowledgePointMapper extends BaseMapper<BizKnowledgePoint> {
    // 基础的 CRUD 已由 BaseMapper 提供
}