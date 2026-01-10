package com.ice.exebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizQuestionKnowledgePoint;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.mapper.BizExamResultMapper;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.ice.exebackend.mapper.BizKnowledgePointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生学习画像服务
 *
 * @author AI功能组
 * @version v3.04
 */
@Service
public class StudentLearningProfileService {

    @Autowired
    private BizExamResultMapper examResultMapper;

    @Autowired
    private BizQuestionMapper questionMapper;

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Autowired
    private BizKnowledgePointMapper knowledgePointMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 学生学习画像
     */
    public static class StudentLearningProfile {
        private Long userId;
        private String level;               // 整体水平：优秀/良好/中等/较差
        private String learningStyle;       // 学习风格：视觉型/听觉型/动手型
        private List<String> weakPoints;    // 薄弱知识点
        private List<String> strongPoints;  // 擅长知识点
        private double averageAccuracy;     // 平均正确率
        private Map<String, Double> subjectAccuracy; // 各科目正确率
        private String commonMistakeType;   // 常见错误类型
        private int totalQuestionsDone;     // 总答题数
        private LocalDateTime lastActiveTime; // 最后活跃时间

        public StudentLearningProfile() {
            this.weakPoints = new ArrayList<>();
            this.strongPoints = new ArrayList<>();
            this.subjectAccuracy = new HashMap<>();
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public String getLearningStyle() { return learningStyle; }
        public void setLearningStyle(String learningStyle) { this.learningStyle = learningStyle; }

        public List<String> getWeakPoints() { return weakPoints; }
        public void setWeakPoints(List<String> weakPoints) { this.weakPoints = weakPoints; }

        public List<String> getStrongPoints() { return strongPoints; }
        public void setStrongPoints(List<String> strongPoints) { this.strongPoints = strongPoints; }

        public double getAverageAccuracy() { return averageAccuracy; }
        public void setAverageAccuracy(double averageAccuracy) { this.averageAccuracy = averageAccuracy; }

        public Map<String, Double> getSubjectAccuracy() { return subjectAccuracy; }
        public void setSubjectAccuracy(Map<String, Double> subjectAccuracy) { this.subjectAccuracy = subjectAccuracy; }

        public String getCommonMistakeType() { return commonMistakeType; }
        public void setCommonMistakeType(String commonMistakeType) { this.commonMistakeType = commonMistakeType; }

        public int getTotalQuestionsDone() { return totalQuestionsDone; }
        public void setTotalQuestionsDone(int totalQuestionsDone) { this.totalQuestionsDone = totalQuestionsDone; }

        public LocalDateTime getLastActiveTime() { return lastActiveTime; }
        public void setLastActiveTime(LocalDateTime lastActiveTime) { this.lastActiveTime = lastActiveTime; }
    }

    /**
     * 获取学生学习画像
     *
     * @param userId 学生ID
     * @return 学习画像
     */
    public StudentLearningProfile getProfile(Long userId) {
        StudentLearningProfile profile = new StudentLearningProfile();
        profile.setUserId(userId);

        // 查询最近6个月的答题记录
        LambdaQueryWrapper<BizExamResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizExamResult::getStudentId, userId);
        wrapper.ge(BizExamResult::getCreateTime, LocalDateTime.now().minusMonths(6));
        wrapper.orderByDesc(BizExamResult::getCreateTime);

        List<BizExamResult> examResults = examResultMapper.selectList(wrapper);

        if (examResults.isEmpty()) {
            // 新用户，返回默认画像
            profile.setLevel("新手");
            profile.setLearningStyle("探索中");
            profile.setAverageAccuracy(0.0);
            profile.setTotalQuestionsDone(0);
            return profile;
        }

        // 1. 计算整体正确率
        int totalCorrect = 0;
        int totalQuestions = 0;

        for (BizExamResult result : examResults) {
            if (result.getScore() != null && result.getTotalScore() != null && result.getTotalScore() > 0) {
                totalQuestions++;
                totalCorrect += result.getScore();
            }
        }

        double averageAccuracy = totalQuestions > 0 ?
            (double) totalCorrect / (examResults.size() * 100.0) : 0.0;
        profile.setAverageAccuracy(averageAccuracy);
        profile.setTotalQuestionsDone(totalQuestions);

        // 2. 确定整体水平
        if (averageAccuracy >= 0.85) {
            profile.setLevel("优秀");
        } else if (averageAccuracy >= 0.70) {
            profile.setLevel("良好");
        } else if (averageAccuracy >= 0.60) {
            profile.setLevel("中等");
        } else {
            profile.setLevel("待提升");
        }

        // 3. 分析薄弱知识点（从错题中提取）
        List<String> weakPoints = analyzeWeakPoints(userId, examResults);
        profile.setWeakPoints(weakPoints);

        // 4. 分析擅长知识点
        List<String> strongPoints = analyzeStrongPoints(userId, examResults);
        profile.setStrongPoints(strongPoints);

        // 5. 常见错误类型分析
        String commonMistakeType = analyzeCommonMistakeType(examResults);
        profile.setCommonMistakeType(commonMistakeType);

        // 6. 学习风格推断（基于答题时间和正确率）
        String learningStyle = inferLearningStyle(examResults);
        profile.setLearningStyle(learningStyle);

        // 7. 最后活跃时间
        if (!examResults.isEmpty()) {
            profile.setLastActiveTime(examResults.get(0).getCreateTime());
        }

        return profile;
    }

    /**
     * 分析薄弱知识点
     */
    private List<String> analyzeWeakPoints(Long userId, List<BizExamResult> examResults) {
        Map<String, Integer> topicErrorCount = new HashMap<>();

        for (BizExamResult result : examResults) {
            try {
                if (result.getUserAnswers() == null) continue;

                JsonNode answersNode = objectMapper.readTree(result.getUserAnswers());
                if (!answersNode.isArray()) continue;

                for (JsonNode answerNode : answersNode) {
                    boolean correct = answerNode.path("correct").asBoolean(true);
                    if (!correct) {
                        Long questionId = answerNode.path("questionId").asLong();
                        String knowledgePointName = getKnowledgePointNameByQuestionId(questionId);

                        if (knowledgePointName != null) {
                            topicErrorCount.put(knowledgePointName,
                                topicErrorCount.getOrDefault(knowledgePointName, 0) + 1);
                        }
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，跳过
            }
        }

        // 返回错误次数前5的知识点
        return topicErrorCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 分析擅长知识点
     */
    private List<String> analyzeStrongPoints(Long userId, List<BizExamResult> examResults) {
        Map<String, Integer> topicCorrectCount = new HashMap<>();
        Map<String, Integer> topicTotalCount = new HashMap<>();

        for (BizExamResult result : examResults) {
            try {
                if (result.getUserAnswers() == null) continue;

                JsonNode answersNode = objectMapper.readTree(result.getUserAnswers());
                if (!answersNode.isArray()) continue;

                for (JsonNode answerNode : answersNode) {
                    Long questionId = answerNode.path("questionId").asLong();
                    boolean correct = answerNode.path("correct").asBoolean(true);

                    String knowledgePointName = getKnowledgePointNameByQuestionId(questionId);
                    if (knowledgePointName != null) {
                        topicTotalCount.put(knowledgePointName,
                            topicTotalCount.getOrDefault(knowledgePointName, 0) + 1);

                        if (correct) {
                            topicCorrectCount.put(knowledgePointName,
                                topicCorrectCount.getOrDefault(knowledgePointName, 0) + 1);
                        }
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，跳过
            }
        }

        // 计算正确率，返回正确率>90%且做题数>3的知识点
        List<String> strongPoints = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : topicTotalCount.entrySet()) {
            String topic = entry.getKey();
            int total = entry.getValue();
            int correct = topicCorrectCount.getOrDefault(topic, 0);

            if (total >= 3 && (double) correct / total >= 0.90) {
                strongPoints.add(topic);
            }
        }

        return strongPoints.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * 分析常见错误类型
     */
    private String analyzeCommonMistakeType(List<BizExamResult> examResults) {
        // 基于正确率和答题数量推断
        int totalExams = examResults.size();
        if (totalExams == 0) return "数据不足";

        double avgScore = examResults.stream()
                .filter(r -> r.getScore() != null && r.getTotalScore() != null && r.getTotalScore() > 0)
                .mapToDouble(r -> (double) r.getScore() / r.getTotalScore())
                .average()
                .orElse(0.0);

        if (avgScore >= 0.85) {
            return "整体表现优秀，偶尔因粗心失分";
        } else if (avgScore >= 0.70) {
            return "知识掌握良好，需加强难题训练";
        } else if (avgScore >= 0.60) {
            return "基础知识存在薄弱环节，需系统复习";
        } else {
            return "基础知识不够扎实，建议从基础开始巩固";
        }
    }

    /**
     * 推断学习风格
     */
    private String inferLearningStyle(List<BizExamResult> examResults) {
        // 简化版本：基于答题数量和频率
        int totalExams = examResults.size();

        if (totalExams >= 30) {
            return "勤奋型学习者 - 通过大量练习巩固知识";
        } else if (totalExams >= 15) {
            return "稳健型学习者 - 注重质量与效率平衡";
        } else if (totalExams >= 5) {
            return "探索型学习者 - 正在摸索学习节奏";
        } else {
            return "新手学习者 - 建议多做练习积累经验";
        }
    }

    /**
     * 根据题目ID获取知识点名称
     */
    private String getKnowledgePointNameByQuestionId(Long questionId) {
        if (questionId == null) {
            return null;
        }

        // 查询题目-知识点关联
        LambdaQueryWrapper<BizQuestionKnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizQuestionKnowledgePoint::getQuestionId, questionId);
        wrapper.last("LIMIT 1"); // 只取第一个关联的知识点

        BizQuestionKnowledgePoint relation = questionKnowledgePointMapper.selectOne(wrapper);
        if (relation == null) {
            return null;
        }

        // 查询知识点详情
        BizKnowledgePoint knowledgePoint = knowledgePointMapper.selectById(relation.getKnowledgePointId());
        return knowledgePoint != null ? knowledgePoint.getName() : null;
    }
}
