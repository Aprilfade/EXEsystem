package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizPaperKnowledgePoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷-知识点关联表 Mapper
 *
 * @author ice
 * @since 2026-01-08
 */
@Mapper
public interface BizPaperKnowledgePointMapper extends BaseMapper<BizPaperKnowledgePoint> {
}
