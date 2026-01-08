package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.StudentKnowledgeMasteryDTO;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.mapper.*;
import com.ice.exebackend.service.BizKnowledgePointService;
import com.ice.exebackend.service.BizPaperService;
import com.ice.exebackend.service.BizStudentKnowledgeMasteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生知识点掌握度服务实现
 *
 * @author ice
 * @since 2026-01-08
 */
@Service
public class BizStudentKnowledgeMasteryServiceImpl
        extends ServiceImpl<BizStudentKnowledgeMasteryMapper, BizStudentKnowledgeMastery>
        implements BizStudentKnowledgeMasteryService {

    @Autowired
    private BizStudentKnowledgeMasteryMapper masteryMapper;

    @Autowired
    private BizExamResultMapper examResultMapper;

    @Autowired
    private BizPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Autowired
    private BizWrongRecordMapper wrongRecordMapper;

    @Autowired
    private BizPaperService paperService;

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    @Override
    @Transactional
    public void updateMasteryAfterExam(Long studentId, Long examResultId) {
        // 1. 获取考试结果
        BizExamResult examResult = examResultMapper.selectById(examResultId);
        if (examResult == null) {
            return;
        }

        Long paperId = examResult.getPaperId();

        // 2. 获取试卷的所有题目
        List<BizPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<BizPaperQuestion>().eq("paper_id", paperId)
        );

        if (paperQuestions.isEmpty()) {
            return;
        }

        // 3. 获取该学生在这张试卷上的所有错题
        Set<Long> wrongQuestionIds = wrongRecordMapper.selectList(
                new QueryWrapper<BizWrongRecord>()
                        .eq("student_id", studentId)
                        .eq("paper_id", paperId)
        ).stream()
                .map(BizWrongRecord::getQuestionId)
                .collect(Collectors.toSet());

        // 4. 按知识点分组统计答题情况
        Map<Long, int[]> kpStats = new HashMap<>(); // key: 知识点ID, value: [correctCount, totalCount]

        for (BizPaperQuestion pq : paperQuestions) {
            Long questionId = pq.getQuestionId();

            // 判断该题是否答对
            boolean isCorrect = !wrongQuestionIds.contains(questionId);

            // 获取该题关联的知识点
            List<Long> kpIds = questionKnowledgePointMapper.selectList(
                    new QueryWrapper<BizQuestionKnowledgePoint>()
                            .eq("question_id", questionId)
            ).stream()
                    .map(BizQuestionKnowledgePoint::getKnowledgePointId)
                    .collect(Collectors.toList());

            // 更新每个知识点的统计
            for (Long kpId : kpIds) {
                kpStats.putIfAbsent(kpId, new int[]{0, 0});
                int[] counts = kpStats.get(kpId);
                if (isCorrect) {
                    counts[0]++; // 正确数+1
                }
                counts[1]++; // 总数+1
            }
        }

        // 5. 更新掌握度表
        for (Map.Entry<Long, int[]> entry : kpStats.entrySet()) {
            Long kpId = entry.getKey();
            int correctInc = entry.getValue()[0];
            int totalInc = entry.getValue()[1];

            // 查询现有记录
            BizStudentKnowledgeMastery mastery = masteryMapper.selectOne(
                    new QueryWrapper<BizStudentKnowledgeMastery>()
                            .eq("student_id", studentId)
                            .eq("knowledge_point_id", kpId)
            );

            if (mastery == null) {
                // 新建记录
                mastery = new BizStudentKnowledgeMastery();
                mastery.setStudentId(studentId);
                mastery.setKnowledgePointId(kpId);
                mastery.setCorrectCount(correctInc);
                mastery.setTotalCount(totalInc);
            } else {
                // 累加统计
                mastery.setCorrectCount(mastery.getCorrectCount() + correctInc);
                mastery.setTotalCount(mastery.getTotalCount() + totalInc);
            }

            // 计算掌握度
            double rate = (mastery.getCorrectCount() * 100.0) / mastery.getTotalCount();
            mastery.setMasteryRate(BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP));

            // 保存或更新
            this.saveOrUpdate(mastery);
        }
    }

    @Override
    public List<StudentKnowledgeMasteryDTO> getStudentMastery(Long studentId) {
        List<BizStudentKnowledgeMastery> masteries = masteryMapper.selectList(
                new QueryWrapper<BizStudentKnowledgeMastery>()
                        .eq("student_id", studentId)
                        .orderByDesc("mastery_rate")
        );

        return convertToDTOList(masteries);
    }

    @Override
    public List<StudentKnowledgeMasteryDTO> getStudentMasteryBySubject(Long studentId, Long subjectId) {
        // 1. 获取该科目下所有知识点的ID
        List<Long> kpIds = knowledgePointService.list(
                new QueryWrapper<BizKnowledgePoint>().eq("subject_id", subjectId)
        ).stream()
                .map(BizKnowledgePoint::getId)
                .collect(Collectors.toList());

        if (kpIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 查询学生对这些知识点的掌握度
        List<BizStudentKnowledgeMastery> masteries = masteryMapper.selectList(
                new QueryWrapper<BizStudentKnowledgeMastery>()
                        .eq("student_id", studentId)
                        .in("knowledge_point_id", kpIds)
                        .orderByDesc("mastery_rate")
        );

        return convertToDTOList(masteries);
    }

    @Override
    public Map<String, Object> getStudentMasteryRadar(Long studentId, Long subjectId) {
        List<StudentKnowledgeMasteryDTO> masteries = subjectId != null
                ? getStudentMasteryBySubject(studentId, subjectId)
                : getStudentMastery(studentId);

        // 构造雷达图数据
        Map<String, Object> radarData = new HashMap<>();

        // 知识点名称列表
        List<String> indicators = masteries.stream()
                .map(StudentKnowledgeMasteryDTO::getKnowledgePointName)
                .collect(Collectors.toList());

        // 掌握度数据
        List<BigDecimal> values = masteries.stream()
                .map(StudentKnowledgeMasteryDTO::getMasteryRate)
                .collect(Collectors.toList());

        radarData.put("indicators", indicators);
        radarData.put("values", values);

        return radarData;
    }

    /**
     * 转换为DTO列表
     */
    private List<StudentKnowledgeMasteryDTO> convertToDTOList(List<BizStudentKnowledgeMastery> masteries) {
        if (masteries.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量获取知识点名称
        List<Long> kpIds = masteries.stream()
                .map(BizStudentKnowledgeMastery::getKnowledgePointId)
                .collect(Collectors.toList());

        Map<Long, String> kpNameMap = knowledgePointService.listByIds(kpIds).stream()
                .collect(Collectors.toMap(BizKnowledgePoint::getId, BizKnowledgePoint::getName));

        // 转换为DTO
        return masteries.stream().map(mastery -> {
            StudentKnowledgeMasteryDTO dto = new StudentKnowledgeMasteryDTO();
            dto.setKnowledgePointId(mastery.getKnowledgePointId());
            dto.setKnowledgePointName(kpNameMap.get(mastery.getKnowledgePointId()));
            dto.setCorrectCount(mastery.getCorrectCount());
            dto.setTotalCount(mastery.getTotalCount());
            dto.setMasteryRate(mastery.getMasteryRate());
            dto.setLastUpdateTime(mastery.getLastUpdateTime());

            // 计算掌握等级
            BigDecimal rate = mastery.getMasteryRate();
            if (rate.compareTo(BigDecimal.valueOf(90)) >= 0) {
                dto.setMasteryLevel("优秀");
            } else if (rate.compareTo(BigDecimal.valueOf(75)) >= 0) {
                dto.setMasteryLevel("良好");
            } else if (rate.compareTo(BigDecimal.valueOf(60)) >= 0) {
                dto.setMasteryLevel("及格");
            } else {
                dto.setMasteryLevel("待提升");
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
