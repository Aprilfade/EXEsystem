package com.ice.exebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizQuestionKnowledgePoint;
import com.ice.exebackend.mapper.BizExamResultMapper;
import com.ice.exebackend.mapper.BizKnowledgePointMapper;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识图谱服务
 * 用于分析知识点之间的依赖关系和学生的掌握情况
 *
 * @author AI功能组
 * @version v3.04
 */
@Service
public class KnowledgeGraphService {

    @Autowired
    private BizKnowledgePointMapper knowledgePointMapper;

    @Autowired
    private BizQuestionMapper questionMapper;

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Autowired
    private BizExamResultMapper examResultMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 知识点（带掌握度）
     */
    public static class KnowledgePoint {
        private Long id;
        private String code;
        private String name;
        private String description;
        private double masteryLevel;  // 掌握度 0-1
        private List<Long> prerequisites; // 前置知识点ID列表
        private int errorCount;       // 错误次数
        private int totalCount;       // 总答题次数

        public KnowledgePoint() {
            this.prerequisites = new ArrayList<>();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getMasteryLevel() { return masteryLevel; }
        public void setMasteryLevel(double masteryLevel) { this.masteryLevel = masteryLevel; }

        public List<Long> getPrerequisites() { return prerequisites; }
        public void setPrerequisites(List<Long> prerequisites) { this.prerequisites = prerequisites; }

        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    }

    /**
     * 追溯前置知识点
     * 分析某个知识点的所有前置依赖，并计算学生的掌握度
     *
     * @param knowledgePointId 目标知识点ID
     * @param userId 学生ID
     * @return 前置知识点列表（按依赖层级排序）
     */
    public List<KnowledgePoint> tracePrerequisites(Long knowledgePointId, Long userId) {
        if (knowledgePointId == null) {
            return Collections.emptyList();
        }

        BizKnowledgePoint targetPoint = knowledgePointMapper.selectById(knowledgePointId);
        if (targetPoint == null) {
            return Collections.emptyList();
        }

        // 基于知识点编码推断依赖关系
        // 例如：编码为 "A.1.2" 的知识点依赖 "A.1" 和 "A"
        List<KnowledgePoint> prerequisites = new ArrayList<>();
        String code = targetPoint.getCode();

        if (code != null && code.contains(".")) {
            // 解析编码层级
            String[] parts = code.split("\\.");
            StringBuilder parentCode = new StringBuilder();

            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) parentCode.append(".");
                parentCode.append(parts[i]);

                // 查找父级知识点
                LambdaQueryWrapper<BizKnowledgePoint> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BizKnowledgePoint::getCode, parentCode.toString());
                wrapper.eq(BizKnowledgePoint::getSubjectId, targetPoint.getSubjectId());

                BizKnowledgePoint parentPoint = knowledgePointMapper.selectOne(wrapper);
                if (parentPoint != null) {
                    KnowledgePoint kp = convertToKnowledgePoint(parentPoint, userId);
                    prerequisites.add(kp);
                }
            }
        }

        // 如果没有基于编码的依赖，尝试基于名称查找相关知识点
        if (prerequisites.isEmpty()) {
            prerequisites = findRelatedKnowledgePoints(targetPoint, userId);
        }

        return prerequisites;
    }

    /**
     * 查找相关知识点（基于名称相似度）
     */
    private List<KnowledgePoint> findRelatedKnowledgePoints(BizKnowledgePoint targetPoint, Long userId) {
        List<KnowledgePoint> related = new ArrayList<>();

        // 查询同一科目下的所有知识点
        LambdaQueryWrapper<BizKnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizKnowledgePoint::getSubjectId, targetPoint.getSubjectId());
        wrapper.ne(BizKnowledgePoint::getId, targetPoint.getId());

        List<BizKnowledgePoint> allPoints = knowledgePointMapper.selectList(wrapper);

        // 简单的相关性判断：名称包含关系
        String targetName = targetPoint.getName();
        for (BizKnowledgePoint point : allPoints) {
            String pointName = point.getName();

            // 如果目标名称包含该知识点名称，说明可能是前置知识
            if (targetName.contains(pointName) && !targetName.equals(pointName)) {
                KnowledgePoint kp = convertToKnowledgePoint(point, userId);
                related.add(kp);
            }
        }

        // 限制返回数量
        return related.stream().limit(3).collect(Collectors.toList());
    }

    /**
     * 将数据库实体转换为带掌握度的知识点
     */
    private KnowledgePoint convertToKnowledgePoint(BizKnowledgePoint dbPoint, Long userId) {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setId(dbPoint.getId());
        kp.setCode(dbPoint.getCode());
        kp.setName(dbPoint.getName());
        kp.setDescription(dbPoint.getDescription());

        // 计算掌握度
        double masteryLevel = calculateMasteryLevel(dbPoint.getId(), userId);
        kp.setMasteryLevel(masteryLevel);

        return kp;
    }

    /**
     * 计算学生对某个知识点的掌握度
     *
     * @param knowledgePointId 知识点ID
     * @param userId 学生ID
     * @return 掌握度 0-1
     */
    public double calculateMasteryLevel(Long knowledgePointId, Long userId) {
        // 通过中间表查找该知识点对应的所有题目ID
        LambdaQueryWrapper<BizQuestionKnowledgePoint> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(BizQuestionKnowledgePoint::getKnowledgePointId, knowledgePointId);
        List<BizQuestionKnowledgePoint> relations = questionKnowledgePointMapper.selectList(relationWrapper);

        if (relations.isEmpty()) {
            return 0.5; // 无数据时返回中等掌握度
        }

        Set<Long> questionIds = relations.stream()
                .map(BizQuestionKnowledgePoint::getQuestionId)
                .collect(Collectors.toSet());

        // 查询学生的答题记录
        LambdaQueryWrapper<BizExamResult> examWrapper = new LambdaQueryWrapper<>();
        examWrapper.eq(BizExamResult::getStudentId, userId);
        examWrapper.orderByDesc(BizExamResult::getCreateTime);
        examWrapper.last("LIMIT 100"); // 只查最近100次记录

        List<BizExamResult> examResults = examResultMapper.selectList(examWrapper);

        int correctCount = 0;
        int totalCount = 0;

        // 统计该知识点的正确率
        for (BizExamResult result : examResults) {
            try {
                if (result.getUserAnswers() == null) continue;

                JsonNode answersNode = objectMapper.readTree(result.getUserAnswers());
                if (!answersNode.isArray()) continue;

                for (JsonNode answerNode : answersNode) {
                    Long questionId = answerNode.path("questionId").asLong();

                    if (questionIds.contains(questionId)) {
                        totalCount++;
                        boolean correct = answerNode.path("correct").asBoolean(true);
                        if (correct) {
                            correctCount++;
                        }
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，跳过
            }
        }

        if (totalCount == 0) {
            return 0.5; // 未答过题，返回中等掌握度
        }

        return (double) correctCount / totalCount;
    }

    /**
     * 获取知识点详细信息（包括掌握度和统计）
     */
    public KnowledgePoint getKnowledgePointWithMastery(Long knowledgePointId, Long userId) {
        BizKnowledgePoint dbPoint = knowledgePointMapper.selectById(knowledgePointId);
        if (dbPoint == null) {
            return null;
        }

        KnowledgePoint kp = new KnowledgePoint();
        kp.setId(dbPoint.getId());
        kp.setCode(dbPoint.getCode());
        kp.setName(dbPoint.getName());
        kp.setDescription(dbPoint.getDescription());

        // 计算掌握度和统计数据
        Map<String, Integer> stats = calculateKnowledgePointStats(knowledgePointId, userId);
        kp.setTotalCount(stats.get("total"));
        kp.setErrorCount(stats.get("error"));

        if (stats.get("total") > 0) {
            kp.setMasteryLevel((double) (stats.get("total") - stats.get("error")) / stats.get("total"));
        } else {
            kp.setMasteryLevel(0.5);
        }

        return kp;
    }

    /**
     * 计算知识点统计数据
     */
    private Map<String, Integer> calculateKnowledgePointStats(Long knowledgePointId, Long userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", 0);
        stats.put("error", 0);

        // 通过中间表查找该知识点对应的所有题目ID
        LambdaQueryWrapper<BizQuestionKnowledgePoint> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(BizQuestionKnowledgePoint::getKnowledgePointId, knowledgePointId);
        List<BizQuestionKnowledgePoint> relations = questionKnowledgePointMapper.selectList(relationWrapper);

        if (relations.isEmpty()) {
            return stats;
        }

        Set<Long> questionIds = relations.stream()
                .map(BizQuestionKnowledgePoint::getQuestionId)
                .collect(Collectors.toSet());

        // 查询学生的答题记录
        LambdaQueryWrapper<BizExamResult> examWrapper = new LambdaQueryWrapper<>();
        examWrapper.eq(BizExamResult::getStudentId, userId);
        examWrapper.orderByDesc(BizExamResult::getCreateTime);
        examWrapper.last("LIMIT 100");

        List<BizExamResult> examResults = examResultMapper.selectList(examWrapper);

        for (BizExamResult result : examResults) {
            try {
                if (result.getUserAnswers() == null) continue;

                JsonNode answersNode = objectMapper.readTree(result.getUserAnswers());
                if (!answersNode.isArray()) continue;

                for (JsonNode answerNode : answersNode) {
                    Long questionId = answerNode.path("questionId").asLong();

                    if (questionIds.contains(questionId)) {
                        stats.put("total", stats.get("total") + 1);
                        boolean correct = answerNode.path("correct").asBoolean(true);
                        if (!correct) {
                            stats.put("error", stats.get("error") + 1);
                        }
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，跳过
            }
        }

        return stats;
    }
}
