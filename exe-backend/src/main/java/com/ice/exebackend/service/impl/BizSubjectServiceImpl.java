package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.SubjectStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizSubject;
import com.ice.exebackend.mapper.BizSubjectMapper;
import com.ice.exebackend.service.BizKnowledgePointService;
import com.ice.exebackend.service.BizSubjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.service.BizQuestionService;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizSubjectServiceImpl extends ServiceImpl<BizSubjectMapper, BizSubject> implements BizSubjectService {

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    @Autowired
    private BizQuestionService questionService;

    /**
     * 【优化版】获取包含统计数据的科目分页列表
     * 解决了 N+1 查询问题，使用 GROUP BY 批量聚合，极大提升性能
     */
    @Override
    public Page<SubjectStatsDTO> getSubjectStatsPage(Page<BizSubject> pageRequest, QueryWrapper<BizSubject> queryWrapper) {
        // 1. 基础查询：获取当页的科目列表
        this.page(pageRequest, queryWrapper);

        // 如果当前页没有数据，直接返回空页，避免后续报错
        if (CollectionUtils.isEmpty(pageRequest.getRecords())) {
            Page<SubjectStatsDTO> emptyDtoPage = new Page<>(pageRequest.getCurrent(), pageRequest.getSize(), 0);
            emptyDtoPage.setRecords(Collections.emptyList());
            return emptyDtoPage;
        }

        // 2. 提取当前页所有科目的 ID 列表
        List<Long> subjectIds = pageRequest.getRecords().stream()
                .map(BizSubject::getId)
                .collect(Collectors.toList());

        // 3. 批量统计：查询这些科目关联的【知识点】数量
        // SQL: SELECT subject_id, COUNT(*) FROM biz_knowledge_point WHERE subject_id IN (...) GROUP BY subject_id
        Map<Long, Long> kpCounts = knowledgePointService.list(new QueryWrapper<BizKnowledgePoint>()
                        .select("subject_id") // 仅查询必要字段，减少IO
                        .in("subject_id", subjectIds))
                .stream()
                .collect(Collectors.groupingBy(BizKnowledgePoint::getSubjectId, Collectors.counting()));

        // 4. 批量统计：查询这些科目关联的【试题】数量 (核心优化点)
        // SQL: SELECT subject_id, COUNT(*) as count FROM biz_question WHERE subject_id IN (...) GROUP BY subject_id
        // 使用 listMaps 可以直接获取聚合结果
        List<Map<String, Object>> questionCountMaps = questionService.listMaps(new QueryWrapper<BizQuestion>()
                .select("subject_id", "COUNT(*) as count")
                .in("subject_id", subjectIds)
                .groupBy("subject_id"));

        // 将查询结果转换为 Map<SubjectId, Count> 结构，方便 O(1) 获取
        Map<Long, Long> questionCounts = questionCountMaps.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("subject_id")).longValue(), // Key: subject_id
                        m -> ((Number) m.get("count")).longValue()       // Value: count
                ));

        // 5. 内存组装：将 Entity 转换为 DTO 并填充统计数据
        return (Page<SubjectStatsDTO>) pageRequest.convert(subject -> {
            SubjectStatsDTO dto = new SubjectStatsDTO();
            BeanUtils.copyProperties(subject, dto);

            // 从 Map 中直接取值，如果没有记录则默认为 0
            dto.setKnowledgePointCount(kpCounts.getOrDefault(subject.getId(), 0L));
            dto.setQuestionCount(questionCounts.getOrDefault(subject.getId(), 0L));

            return dto;
        });
    }

    /**
     * 【新增】获取包含统计数据的科目列表（不分页）
     * 用于学生端，与管理端数据同步，返回相同格式的统计数据
     */
    @Override
    public List<SubjectStatsDTO> getSubjectStatsList(QueryWrapper<BizSubject> queryWrapper) {
        // 1. 查询所有符合条件的科目
        List<BizSubject> subjects = this.list(queryWrapper);

        // 如果没有数据，直接返回空列表
        if (CollectionUtils.isEmpty(subjects)) {
            return Collections.emptyList();
        }

        // 2. 提取所有科目的 ID 列表
        List<Long> subjectIds = subjects.stream()
                .map(BizSubject::getId)
                .collect(Collectors.toList());

        // 3. 批量统计：知识点数量
        Map<Long, Long> kpCounts = knowledgePointService.list(new QueryWrapper<BizKnowledgePoint>()
                        .select("subject_id")
                        .in("subject_id", subjectIds))
                .stream()
                .collect(Collectors.groupingBy(BizKnowledgePoint::getSubjectId, Collectors.counting()));

        // 4. 批量统计：试题数量
        List<Map<String, Object>> questionCountMaps = questionService.listMaps(new QueryWrapper<BizQuestion>()
                .select("subject_id", "COUNT(*) as count")
                .in("subject_id", subjectIds)
                .groupBy("subject_id"));

        Map<Long, Long> questionCounts = questionCountMaps.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("subject_id")).longValue(),
                        m -> ((Number) m.get("count")).longValue()
                ));

        // 5. 转换为 DTO 并填充统计数据
        return subjects.stream().map(subject -> {
            SubjectStatsDTO dto = new SubjectStatsDTO();
            BeanUtils.copyProperties(subject, dto);
            dto.setKnowledgePointCount(kpCounts.getOrDefault(subject.getId(), 0L));
            dto.setQuestionCount(questionCounts.getOrDefault(subject.getId(), 0L));
            return dto;
        }).collect(Collectors.toList());
    }

    // 【同步修复】修复获取科目下试题列表的逻辑，使其与上面的统计逻辑一致
    @Override
    public List<BizQuestion> getQuestionsForSubject(Long subjectId) {
        // 原有逻辑先查科目，再按名称匹配，完全多余
        // 优化：直接根据 subject_id 查询，利用数据库索引，速度极快且准确
        return questionService.list(
                new QueryWrapper<BizQuestion>().eq("subject_id", subjectId)
        );
    }
}