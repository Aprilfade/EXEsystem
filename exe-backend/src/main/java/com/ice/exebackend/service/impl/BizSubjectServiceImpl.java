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
import com.ice.exebackend.entity.BizQuestion; // 新增导入
import com.ice.exebackend.service.BizQuestionService; // 新增导入

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizSubjectServiceImpl extends ServiceImpl<BizSubjectMapper, BizSubject> implements BizSubjectService {

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    // 【新增注入】
    @Autowired
    private BizQuestionService questionService;

    @Override
    public Page<SubjectStatsDTO> getSubjectStatsPage(Page<BizSubject> pageRequest, QueryWrapper<BizSubject> queryWrapper) {
        queryWrapper.orderByDesc("create_time");
        page(pageRequest, queryWrapper);

        // 2. 如果当前页没有数据，直接返回一个空的 DTO 分页对象
        if (CollectionUtils.isEmpty(pageRequest.getRecords())) {
            Page<SubjectStatsDTO> emptyDtoPage = new Page<>(pageRequest.getCurrent(), pageRequest.getSize(), 0);
            emptyDtoPage.setRecords(Collections.emptyList());
            return emptyDtoPage;
        }

        // 3. 获取当前页所有科目的 ID
        List<Long> subjectIds = pageRequest.getRecords().stream()
                .map(BizSubject::getId)
                .collect(Collectors.toList());


        // 4. 【优化】一次性查询出这些科目对应的所有知识点和试题，并按科目ID分组计数
        Map<Long, Long> kpCounts = knowledgePointService.list(new QueryWrapper<BizKnowledgePoint>().in("subject_id", subjectIds))
                .stream()
                .collect(Collectors.groupingBy(BizKnowledgePoint::getSubjectId, Collectors.counting()));

        Map<Long, Long> qCounts = questionService.list(new QueryWrapper<BizQuestion>().in("subject_id", subjectIds))
                .stream()
                .collect(Collectors.groupingBy(BizQuestion::getSubjectId, Collectors.counting()));


        // 5. 将 Page<BizSubject> 转换为 Page<SubjectStatsDTO>
        return (Page<SubjectStatsDTO>) pageRequest.convert(subject -> {
            SubjectStatsDTO dto = new SubjectStatsDTO();
            BeanUtils.copyProperties(subject, dto); // 复制基础属性

            // 设置知识点数量
            dto.setKnowledgePointCount(kpCounts.getOrDefault(subject.getId(), 0L));
            // 【新增】设置试题数量
            dto.setQuestionCount(qCounts.getOrDefault(subject.getId(), 0L));
            return dto;
        });
    }
}