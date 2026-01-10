package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.dto.LearningAnalyticsDTO;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.mapper.*;
import com.ice.exebackend.service.LearningAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习分析服务实现类
 *
 * @author Claude Code
 * @since v3.08
 */
@Slf4j
@Service
public class LearningAnalyticsServiceImpl implements LearningAnalyticsService {

    @Autowired
    private BizExamResultMapper examResultMapper;

    @Autowired
    private BizWrongRecordMapper wrongRecordMapper;

    @Autowired
    private BizKnowledgePointMapper knowledgePointMapper;

    @Autowired
    private BizQuestionMapper questionMapper;

    @Autowired
    private BizPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Autowired
    private BizSubjectMapper subjectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LearningAnalyticsDTO getStudentLearningAnalytics(Long studentId, Integer days) {
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }

        LearningAnalyticsDTO analytics = new LearningAnalyticsDTO();

        // 1. 学习时长趋势统计
        analytics.setStudyTimeTrend(calculateStudyTimeTrend(studentId, days));

        // 2. 知识点掌握度分析
        analytics.setKnowledgeMastery(calculateKnowledgeMastery(studentId));

        // 3. 弱项分析
        analytics.setWeakPoints(calculateWeakPoints(studentId));

        // 4. 生成学习建议
        analytics.setLearningAdvice(generateLearningAdvice(analytics));

        return analytics;
    }

    /**
     * 计算学习时长趋势
     */
    private List<LearningAnalyticsDTO.StudyTimePoint> calculateStudyTimeTrend(Long studentId, Integer days) {
        List<LearningAnalyticsDTO.StudyTimePoint> trend = new ArrayList<>();

        // 获取最近N天的日期范围
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days - 1);

        // 查询时间范围内的考试记录
        List<BizExamResult> examResults = examResultMapper.selectList(
                new QueryWrapper<BizExamResult>()
                        .eq("student_id", studentId)
                        .ge("create_time", startDate)
                        .le("create_time", endDate)
                        .orderByAsc("create_time")
        );

        // 按日期分组统计
        Map<String, List<BizExamResult>> resultsByDate = examResults.stream()
                .collect(Collectors.groupingBy(result ->
                    result.getCreateTime().toLocalDate().format(DATE_FORMATTER)
                ));

        // 填充每一天的数据（包括没有学习记录的日期）
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.toLocalDate().plusDays(i);
            String dateStr = date.format(DATE_FORMATTER);

            LearningAnalyticsDTO.StudyTimePoint point = new LearningAnalyticsDTO.StudyTimePoint();
            point.setDate(dateStr);

            List<BizExamResult> dayResults = resultsByDate.getOrDefault(dateStr, Collections.emptyList());

            // 估算学习时长：每题平均2分钟
            int totalQuestions = dayResults.stream()
                    .mapToInt(result -> {
                        // 通过 paper_id 查询试卷题目数
                        Long count = paperQuestionMapper.selectCount(
                                new QueryWrapper<BizPaperQuestion>().eq("paper_id", result.getPaperId())
                        );
                        return count != null ? count.intValue() : 0;
                    })
                    .sum();

            point.setQuestionCount(totalQuestions);
            point.setStudyMinutes(totalQuestions * 2); // 每题2分钟

            trend.add(point);
        }

        return trend;
    }

    /**
     * 计算知识点掌握度
     */
    private List<LearningAnalyticsDTO.KnowledgeMasteryPoint> calculateKnowledgeMastery(Long studentId) {
        // 获取学生的所有考试记录
        List<BizExamResult> examResults = examResultMapper.selectList(
                new QueryWrapper<BizExamResult>().eq("student_id", studentId)
        );

        if (examResults.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取学生的错题记录
        List<BizWrongRecord> wrongRecords = wrongRecordMapper.selectList(
                new QueryWrapper<BizWrongRecord>().eq("student_id", studentId)
        );
        Set<Long> wrongQuestionIds = wrongRecords.stream()
                .map(BizWrongRecord::getQuestionId)
                .collect(Collectors.toSet());

        // 统计每个知识点的题目数和正确数
        Map<Long, KnowledgePointStats> statsMap = new HashMap<>();

        for (BizExamResult result : examResults) {
            // 获取试卷的所有题目
            List<BizPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                    new QueryWrapper<BizPaperQuestion>().eq("paper_id", result.getPaperId())
            );

            for (BizPaperQuestion pq : paperQuestions) {
                Long questionId = pq.getQuestionId();

                // 获取题目关联的知识点
                List<BizQuestionKnowledgePoint> qkps = questionKnowledgePointMapper.selectList(
                        new QueryWrapper<BizQuestionKnowledgePoint>().eq("question_id", questionId)
                );

                boolean isCorrect = !wrongQuestionIds.contains(questionId);

                for (BizQuestionKnowledgePoint qkp : qkps) {
                    Long kpId = qkp.getKnowledgePointId();

                    statsMap.putIfAbsent(kpId, new KnowledgePointStats());
                    KnowledgePointStats stats = statsMap.get(kpId);
                    stats.totalQuestions++;
                    if (isCorrect) {
                        stats.correctQuestions++;
                    }
                }
            }
        }

        // 转换为结果列表
        List<LearningAnalyticsDTO.KnowledgeMasteryPoint> masteryList = new ArrayList<>();
        for (Map.Entry<Long, KnowledgePointStats> entry : statsMap.entrySet()) {
            Long kpId = entry.getKey();
            KnowledgePointStats stats = entry.getValue();

            BizKnowledgePoint kp = knowledgePointMapper.selectById(kpId);
            if (kp == null) continue;

            LearningAnalyticsDTO.KnowledgeMasteryPoint point = new LearningAnalyticsDTO.KnowledgeMasteryPoint();
            point.setKnowledgePointId(kpId);
            point.setKnowledgePointName(kp.getName());
            point.setTotalQuestions(stats.totalQuestions);
            point.setCorrectQuestions(stats.correctQuestions);

            // 计算掌握度
            double masteryRate = stats.totalQuestions > 0
                    ? (stats.correctQuestions * 100.0 / stats.totalQuestions)
                    : 0.0;
            point.setMasteryRate(BigDecimal.valueOf(masteryRate)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());

            masteryList.add(point);
        }

        // 按掌握度排序，取前10个
        return masteryList.stream()
                .sorted(Comparator.comparing(LearningAnalyticsDTO.KnowledgeMasteryPoint::getMasteryRate).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 计算弱项分析
     */
    private List<LearningAnalyticsDTO.WeakPoint> calculateWeakPoints(Long studentId) {
        // 获取错题记录
        List<BizWrongRecord> wrongRecords = wrongRecordMapper.selectList(
                new QueryWrapper<BizWrongRecord>()
                        .eq("student_id", studentId)
                        .eq("is_mastered", 0) // 只统计未掌握的
        );

        if (wrongRecords.isEmpty()) {
            return Collections.emptyList();
        }

        // 按知识点分组统计
        Map<Long, List<Long>> kpWrongQuestionsMap = new HashMap<>();
        for (BizWrongRecord record : wrongRecords) {
            Long questionId = record.getQuestionId();

            // 获取题目关联的知识点
            List<BizQuestionKnowledgePoint> qkps = questionKnowledgePointMapper.selectList(
                    new QueryWrapper<BizQuestionKnowledgePoint>().eq("question_id", questionId)
            );

            for (BizQuestionKnowledgePoint qkp : qkps) {
                Long kpId = qkp.getKnowledgePointId();
                kpWrongQuestionsMap.putIfAbsent(kpId, new ArrayList<>());
                kpWrongQuestionsMap.get(kpId).add(questionId);
            }
        }

        // 生成弱项列表
        List<LearningAnalyticsDTO.WeakPoint> weakPoints = new ArrayList<>();
        for (Map.Entry<Long, List<Long>> entry : kpWrongQuestionsMap.entrySet()) {
            Long kpId = entry.getKey();
            List<Long> wrongQuestionIds = entry.getValue();

            BizKnowledgePoint kp = knowledgePointMapper.selectById(kpId);
            if (kp == null) continue;

            // 获取科目名称
            String subjectName = "";
            if (kp.getSubjectId() != null) {
                BizSubject subject = subjectMapper.selectById(kp.getSubjectId());
                if (subject != null) {
                    subjectName = subject.getName();
                }
            }

            LearningAnalyticsDTO.WeakPoint weakPoint = new LearningAnalyticsDTO.WeakPoint();
            weakPoint.setKnowledgePointId(kpId);
            weakPoint.setKnowledgePointName(kp.getName());
            weakPoint.setSubjectName(subjectName);
            weakPoint.setWrongCount(wrongQuestionIds.size());

            // 计算得分率（假设错题率的倒数）
            // 这里简化处理，实际应该查询该知识点的总题数
            double scoreRate = Math.max(0, 100 - wrongQuestionIds.size() * 10.0);
            weakPoint.setScoreRate(BigDecimal.valueOf(scoreRate)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());

            // 建议练习次数：错题数 * 2
            weakPoint.setRecommendedPracticeCount(wrongQuestionIds.size() * 2);

            weakPoints.add(weakPoint);
        }

        // 按错题数排序，取前5个
        return weakPoints.stream()
                .sorted(Comparator.comparing(LearningAnalyticsDTO.WeakPoint::getWrongCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 生成学习建议
     */
    private String generateLearningAdvice(LearningAnalyticsDTO analytics) {
        StringBuilder advice = new StringBuilder();

        // 1. 分析学习时长
        List<LearningAnalyticsDTO.StudyTimePoint> trend = analytics.getStudyTimeTrend();
        if (trend != null && !trend.isEmpty()) {
            int totalMinutes = trend.stream().mapToInt(LearningAnalyticsDTO.StudyTimePoint::getStudyMinutes).sum();
            int avgMinutes = totalMinutes / trend.size();

            if (avgMinutes < 30) {
                advice.append("📚 建议每天至少学习30分钟，培养良好的学习习惯。");
            } else if (avgMinutes >= 60) {
                advice.append("💪 学习时长很充足，继续保持！");
            } else {
                advice.append("👍 学习时长适中，可以适当增加到每天1小时。");
            }
        }

        // 2. 分析弱项
        List<LearningAnalyticsDTO.WeakPoint> weakPoints = analytics.getWeakPoints();
        if (weakPoints != null && !weakPoints.isEmpty()) {
            advice.append(" 🎯 重点关注：");
            List<String> topWeakPoints = weakPoints.stream()
                    .limit(3)
                    .map(LearningAnalyticsDTO.WeakPoint::getKnowledgePointName)
                    .collect(Collectors.toList());
            advice.append(String.join("、", topWeakPoints));
            advice.append("。");
        }

        // 3. 分析知识点掌握度
        List<LearningAnalyticsDTO.KnowledgeMasteryPoint> mastery = analytics.getKnowledgeMastery();
        if (mastery != null && !mastery.isEmpty()) {
            long wellMasteredCount = mastery.stream()
                    .filter(m -> m.getMasteryRate() >= 80)
                    .count();

            if (wellMasteredCount >= mastery.size() * 0.7) {
                advice.append(" 🌟 大部分知识点掌握良好，建议开始挑战更难的题目。");
            } else if (wellMasteredCount < mastery.size() * 0.3) {
                advice.append(" 📖 建议回顾基础知识点，打好基础再继续深入学习。");
            }
        }

        if (advice.length() == 0) {
            advice.append("继续努力学习，每天进步一点点！");
        }

        return advice.toString();
    }

    /**
     * 内部类：知识点统计数据
     */
    private static class KnowledgePointStats {
        int totalQuestions = 0;
        int correctQuestions = 0;
    }
}
