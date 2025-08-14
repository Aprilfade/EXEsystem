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
import org.springframework.util.StringUtils;

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

        // 如果当前页没有数据，直接返回一个空的 DTO 分页对象
        if (CollectionUtils.isEmpty(pageRequest.getRecords())) {
            Page<SubjectStatsDTO> emptyDtoPage = new Page<>(pageRequest.getCurrent(), pageRequest.getSize(), 0);
            emptyDtoPage.setRecords(Collections.emptyList());
            return emptyDtoPage;
        }

        // 1. 获取当前页所有科目的 ID
        List<Long> subjectIds = pageRequest.getRecords().stream()
                .map(BizSubject::getId)
                .collect(Collectors.toList());


        // 2. 一次性查询出这些科目对应的所有知识点和试题
        Map<Long, Long> kpCounts = knowledgePointService.list(new QueryWrapper<BizKnowledgePoint>().in("subject_id", subjectIds))
                .stream()
                .collect(Collectors.groupingBy(BizKnowledgePoint::getSubjectId, Collectors.counting()));

        // **【关键修改点】**
        // a. 获取所有相关题目
        List<BizQuestion> allQuestions = questionService.list(new QueryWrapper<BizQuestion>().in("subject_id", subjectIds));
        // b. 按科目ID对题目进行分组，方便后续处理
        Map<Long, List<BizQuestion>> questionsBySubject = allQuestions.stream()
                .collect(Collectors.groupingBy(BizQuestion::getSubjectId));


        // 3. 将 Page<BizSubject> 转换为 Page<SubjectStatsDTO>
        return (Page<SubjectStatsDTO>) pageRequest.convert(subject -> {
            SubjectStatsDTO dto = new SubjectStatsDTO();
            BeanUtils.copyProperties(subject, dto); // 复制基础属性

            // 设置知识点数量 (逻辑不变)
            dto.setKnowledgePointCount(kpCounts.getOrDefault(subject.getId(), 0L));

            // **【关键修改点】**
            // c. 进行更精确的试题计数
            List<BizQuestion> subjectQuestions = questionsBySubject.getOrDefault(subject.getId(), Collections.emptyList());
            long questionCount;

            // 如果科目本身有年级 (例如 "五年级语文")
            if (StringUtils.hasText(subject.getGrade())) {
                // 那么只统计那些年级也匹配的题目
                questionCount = subjectQuestions.stream()
                        .filter(q -> subject.getGrade().equals(q.getGrade()))
                        .count();
            } else {
                // 如果科目没有指定年级，则统计所有关联题目
                questionCount = subjectQuestions.size();
            }
            dto.setQuestionCount(questionCount);

            return dto;
        });

    }
    // 【新增】实现新方法的逻辑
    @Override
    public List<BizQuestion> getQuestionsForSubject(Long subjectId) {
        // 1. 先根据ID获取科目信息
        BizSubject subject = this.getById(subjectId);
        if (subject == null) {
            return Collections.emptyList(); // 如果科目不存在，返回空列表
        }

        // 2. 创建查询条件
        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("subject_id", subjectId);

        // 3. 【关键逻辑】如果科目本身有年级，则必须用这个年级去筛选试题
        if (StringUtils.hasText(subject.getGrade())) {
            queryWrapper.eq("grade", subject.getGrade());
        }

        // 4. 返回查询结果 (这里的 questionService 应该已经在你的文件里注入了)
        return questionService.list(queryWrapper);
    }

}