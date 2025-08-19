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

    @Override
    public Page<SubjectStatsDTO> getSubjectStatsPage(Page<BizSubject> pageRequest, QueryWrapper<BizSubject> queryWrapper) {
        // 这一部分保持不变，正常查询科目分页
        page(pageRequest, queryWrapper);

        if (CollectionUtils.isEmpty(pageRequest.getRecords())) {
            Page<SubjectStatsDTO> emptyDtoPage = new Page<>(pageRequest.getCurrent(), pageRequest.getSize(), 0);
            emptyDtoPage.setRecords(Collections.emptyList());
            return emptyDtoPage;
        }

        List<Long> subjectIds = pageRequest.getRecords().stream()
                .map(BizSubject::getId)
                .collect(Collectors.toList());

        Map<Long, Long> kpCounts = knowledgePointService.list(new QueryWrapper<BizKnowledgePoint>().in("subject_id", subjectIds))
                .stream()
                .collect(Collectors.groupingBy(BizKnowledgePoint::getSubjectId, Collectors.counting()));

        // 将 Page<BizSubject> 转换为 Page<SubjectStatsDTO>
        return (Page<SubjectStatsDTO>) pageRequest.convert(subject -> {
            SubjectStatsDTO dto = new SubjectStatsDTO();
            BeanUtils.copyProperties(subject, dto);
            dto.setKnowledgePointCount(kpCounts.getOrDefault(subject.getId(), 0L));

            // --- 【核心修复】---
            // 创建一个新的查询，不再依赖于之前筛选出的题目列表
            QueryWrapper<BizQuestion> questionCountQuery = new QueryWrapper<>();

            // 关键：查询条件是所有 subject_id 指向的科目名 和 当前科目名相同的试题
            questionCountQuery.inSql("subject_id", "SELECT id FROM biz_subject WHERE name = '" + subject.getName() + "'");

            // 并且，如果当前科目有年级，那么试题的年级也必须匹配
            if (StringUtils.hasText(subject.getGrade())) {
                questionCountQuery.eq("grade", subject.getGrade());
            }

            long questionCount = questionService.count(questionCountQuery);
            dto.setQuestionCount(questionCount);
            // --- 修复结束 ---

            return dto;
        });
    }

    // 【同步修复】修复获取科目下试题列表的逻辑，使其与上面的统计逻辑一致
    @Override
    public List<BizQuestion> getQuestionsForSubject(Long subjectId) {
        BizSubject subject = this.getById(subjectId);
        if (subject == null) {
            return Collections.emptyList();
        }

        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();

        // 关键：查询条件是所有 subject_id 指向的科目名 和 当前科目名相同的试题
        queryWrapper.inSql("subject_id", "SELECT id FROM biz_subject WHERE name = '" + subject.getName() + "'");

        // 并且，如果当前科目有年级，那么试题的年级也必须匹配
        if (StringUtils.hasText(subject.getGrade())) {
            queryWrapper.eq("grade", subject.getGrade());
        }

        return questionService.list(queryWrapper);
    }
}