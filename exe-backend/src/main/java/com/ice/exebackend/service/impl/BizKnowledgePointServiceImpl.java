package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.KnowledgePointBatchUpdateDTO;
import com.ice.exebackend.dto.KnowledgePointStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizKnowledgePointRelation; // 新增导入
import com.ice.exebackend.entity.BizQuestionKnowledgePoint;
import com.ice.exebackend.mapper.BizKnowledgePointMapper;
import com.ice.exebackend.mapper.BizKnowledgePointRelationMapper; // 新增导入
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.ice.exebackend.service.BizKnowledgePointService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap; // 新增导入
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizKnowledgePointServiceImpl extends ServiceImpl<BizKnowledgePointMapper, BizKnowledgePoint> implements BizKnowledgePointService {

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    // 【新增】注入知识图谱关系 Mapper
    @Autowired
    private BizKnowledgePointRelationMapper relationMapper;

    // ================== 原有方法：分页获取统计数据 ==================
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

    // ================== 新增方法：知识图谱相关 ==================

    @Override
    public Map<String, Object> getKnowledgeGraph(Long subjectId) {
        // 1. 获取该科目下所有知识点
        List<BizKnowledgePoint> points = this.list(new QueryWrapper<BizKnowledgePoint>().eq("subject_id", subjectId));
        if (points.isEmpty()) return Map.of("nodes", List.of(), "links", List.of());
        List<Long> pointIds = points.stream().map(BizKnowledgePoint::getId).collect(Collectors.toList());

        // 2. 获取关系
        List<BizKnowledgePointRelation> relations = relationMapper.selectList(
                new QueryWrapper<BizKnowledgePointRelation>()
                        .in("parent_id", pointIds)
                        .in("child_id", pointIds)
        );

        // 3. 【核心修改】组装 Nodes
        List<Map<String, Object>> nodes = points.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            // name 必须唯一且与 links 中的 source/target 一致，这里使用 ID 字符串
            map.put("name", String.valueOf(p.getId()));
            map.put("id", String.valueOf(p.getId()));
            // 新增 realName 用于前端显示真实的中文名称
            map.put("realName", p.getName());
            map.put("value", p.getCode());
            // symbolSize 在前端根据度数动态计算，这里给个默认值即可
            map.put("symbolSize", 20);
            map.put("draggable", true);
            return map;
        }).collect(Collectors.toList());

        // 4. 组装 Links
        List<Map<String, Object>> links = relations.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            // 这里的值现在能正确对应上面的 map.get("name")
            map.put("source", String.valueOf(r.getParentId()));
            map.put("target", String.valueOf(r.getChildId()));
            return map;
        }).collect(Collectors.toList());

        return Map.of("nodes", nodes, "links", links);
    }

    @Override
    public boolean addRelation(Long parentId, Long childId) {
        if (parentId.equals(childId)) throw new RuntimeException("不能关联自己");

        Long reverseCount = relationMapper.selectCount(new QueryWrapper<BizKnowledgePointRelation>()
                .eq("parent_id", childId).eq("child_id", parentId));
        if (reverseCount > 0) throw new RuntimeException("存在循环引用，无法添加");

        // 检查是否已存在
        Long exists = relationMapper.selectCount(new QueryWrapper<BizKnowledgePointRelation>()
                .eq("parent_id", parentId).eq("child_id", childId));
        if(exists > 0) return true;

        try {
            BizKnowledgePointRelation rel = new BizKnowledgePointRelation();
            rel.setParentId(parentId);
            rel.setChildId(childId);
            relationMapper.insert(rel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    public boolean removeRelation(Long parentId, Long childId) {
        return relationMapper.delete(new QueryWrapper<BizKnowledgePointRelation>()
                .eq("parent_id", parentId).eq("child_id", childId)) > 0;
    }

    @Override
    public List<BizKnowledgePoint> findPrerequisitePoints(Long pointId) {
        List<BizKnowledgePointRelation> parents = relationMapper.selectList(
                new QueryWrapper<BizKnowledgePointRelation>().eq("child_id", pointId)
        );

        if (parents.isEmpty()) return Collections.emptyList();

        List<Long> parentIds = parents.stream().map(BizKnowledgePointRelation::getParentId).collect(Collectors.toList());
        return this.listByIds(parentIds);
    }

    @Override
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 获取知识点总数
        long totalKpCount = this.count();
        stats.put("totalKpCount", totalKpCount);

        // 2. 获取所有知识点的题目关联数据
        List<BizQuestionKnowledgePoint> allQkps = questionKnowledgePointMapper.selectList(null);

        // 3. 统计有题目的知识点数量
        long kpWithQuestionsCount = allQkps.stream()
                .map(BizQuestionKnowledgePoint::getKnowledgePointId)
                .distinct()
                .count();
        stats.put("kpWithQuestionsCount", kpWithQuestionsCount);

        // 4. 统计总题目数量（去重）
        long totalQuestionCount = allQkps.stream()
                .map(BizQuestionKnowledgePoint::getQuestionId)
                .distinct()
                .count();
        stats.put("totalQuestionCount", totalQuestionCount);

        // 5. 计算平均每个知识点的题目数
        double avgQuestionsPerKp = kpWithQuestionsCount > 0
            ? (double) allQkps.size() / kpWithQuestionsCount
            : 0;
        stats.put("avgQuestionsPerKp", Math.round(avgQuestionsPerKp * 100.0) / 100.0);

        // 6. 统计知识点覆盖率（有题目的知识点占比）
        double coverageRate = totalKpCount > 0
            ? (kpWithQuestionsCount * 100.0 / totalKpCount)
            : 0;
        stats.put("coverageRate", Math.round(coverageRate * 100.0) / 100.0);

        return stats;
    }

    @Override
    public boolean batchUpdateKnowledgePoints(KnowledgePointBatchUpdateDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getKnowledgePointIds())) {
            return false;
        }

        // 至少需要提供一个更新字段
        if (dto.getSubjectId() == null &&
            !StringUtils.hasText(dto.getGrade()) &&
            !StringUtils.hasText(dto.getTags())) {
            return false;
        }

        LambdaUpdateWrapper<BizKnowledgePoint> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(BizKnowledgePoint::getId, dto.getKnowledgePointIds());

        boolean hasUpdate = false;
        if (dto.getSubjectId() != null) {
            updateWrapper.set(BizKnowledgePoint::getSubjectId, dto.getSubjectId());
            hasUpdate = true;
        }
        if (StringUtils.hasText(dto.getGrade())) {
            updateWrapper.set(BizKnowledgePoint::getGrade, dto.getGrade());
            hasUpdate = true;
        }
        if (StringUtils.hasText(dto.getTags())) {
            updateWrapper.set(BizKnowledgePoint::getTags, dto.getTags());
            hasUpdate = true;
        }

        return hasUpdate ? this.update(updateWrapper) : false;
    }
}