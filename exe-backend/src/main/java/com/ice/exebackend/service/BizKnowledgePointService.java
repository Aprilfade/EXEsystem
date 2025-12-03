package com.ice.exebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.KnowledgePointStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;

import java.util.List;
import java.util.Map;

public interface BizKnowledgePointService extends IService<BizKnowledgePoint> {
    // 未来可在此处扩展复杂业务逻辑
    /**
     * 【新增】分页获取包含统计数据的知识点列表
     * @param page 分页请求
     * @param queryWrapper 查询条件
     * @return 包含统计数据的分页结果
     */
    Page<KnowledgePointStatsDTO> getKnowledgePointStatsPage(Page<BizKnowledgePoint> page, QueryWrapper<BizKnowledgePoint> queryWrapper);
    /**
     * 获取知识图谱数据 (节点和连线)
     * @param subjectId 科目ID
     */
    Map<String, Object> getKnowledgeGraph(Long subjectId);

    /**
     * 添加知识点关联
     */
    boolean addRelation(Long parentId, Long childId);

    /**
     * 删除关联
     */
    boolean removeRelation(Long parentId, Long childId);

    /**
     * 查找某知识点的前置薄弱点 (递归查找父节点)
     */
    List<BizKnowledgePoint> findPrerequisitePoints(Long pointId);
}