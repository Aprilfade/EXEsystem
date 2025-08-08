package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.KnowledgePointStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizQuestionKnowledgePoint;
import com.ice.exebackend.mapper.BizKnowledgePointMapper;
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.ice.exebackend.service.BizKnowledgePointService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizKnowledgePointServiceImpl extends ServiceImpl<BizKnowledgePointMapper, BizKnowledgePoint> implements BizKnowledgePointService {

    // 【新增】注入关联表 Mapper
    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Override
    public Page<KnowledgePointStatsDTO> getKnowledgePointStatsPage(Page<BizKnowledgePoint> pageRequest, QueryWrapper<BizKnowledgePoint> queryWrapper) {
        // 1. 正常分页查询知识点
        this.page(pageRequest, queryWrapper);

        // 2. 如果当前页没有数据，则直接返回
        if (CollectionUtils.isEmpty(pageRequest.getRecords())) {
            Page<KnowledgePointStatsDTO> emptyDtoPage = new Page<>(pageRequest.getCurrent(), pageRequest.getSize(), 0);
            emptyDtoPage.setRecords(Collections.emptyList());
            return emptyDtoPage;
        }

        // 3. 获取当前页所有知识点的ID
        List<Long> kpIds = pageRequest.getRecords().stream()
                .map(BizKnowledgePoint::getId)
                .collect(Collectors.toList());

        // 4. 一次性查询出这些知识点关联的题目数量
        Map<Long, Long> questionCounts = questionKnowledgePointMapper.selectList(
                new QueryWrapper<BizQuestionKnowledgePoint>().in("knowledge_point_id", kpIds)
        ).stream().collect(Collectors.groupingBy(BizQuestionKnowledgePoint::getKnowledgePointId, Collectors.counting()));

        // 5. 将 Page<BizKnowledgePoint> 转换为 Page<KnowledgePointStatsDTO>
        return (Page<KnowledgePointStatsDTO>) pageRequest.convert(kp -> {
            KnowledgePointStatsDTO dto = new KnowledgePointStatsDTO();
            BeanUtils.copyProperties(kp, dto);
            dto.setQuestionCount(questionCounts.getOrDefault(kp.getId(), 0L));
            return dto;
        });
    }
}