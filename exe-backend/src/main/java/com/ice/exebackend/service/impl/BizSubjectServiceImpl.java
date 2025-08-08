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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizSubjectServiceImpl extends ServiceImpl<BizSubjectMapper, BizSubject> implements BizSubjectService {

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    @Override
    public Page<SubjectStatsDTO> getSubjectStatsPage(Page<BizSubject> pageRequest) {
        // 1. 首先，按常规方式分页查询科目
        page(pageRequest, new QueryWrapper<BizSubject>().orderByDesc("create_time"));

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

        // 4. 一次性查询出这些科目对应的所有知识点，并按科目ID分组计数
        Map<Long, Long> kpCounts = knowledgePointService.list(new QueryWrapper<BizKnowledgePoint>().in("subject_id", subjectIds))
                .stream()
                .collect(Collectors.groupingBy(BizKnowledgePoint::getSubjectId, Collectors.counting()));

        // 5. 将 Page<BizSubject> 转换为 Page<SubjectStatsDTO>
        return (Page<SubjectStatsDTO>) pageRequest.convert(subject -> {
            SubjectStatsDTO dto = new SubjectStatsDTO();
            BeanUtils.copyProperties(subject, dto); // 复制基础属性
            // 从 Map 中获取该科目的知识点数量，如果没有则默认为 0
            dto.setKnowledgePointCount(kpCounts.getOrDefault(subject.getId(), 0L));
            return dto;
        });
    }
}