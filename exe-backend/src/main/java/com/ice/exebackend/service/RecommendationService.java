package com.ice.exebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.mapper.BizExamResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI智能推荐服务
 * 实现协同过滤算法，为学生推荐题目和课程
 *
 * @author AI功能组
 * @version v3.03
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private BizQuestionMapper questionMapper;

    @Autowired
    private BizExamResultMapper examResultMapper;

    /**
     * 用户行为数据
     */
    public static class UserBehavior {
        private Long userId;
        private Long questionId;
        private boolean correct;
        private int timeSpent; // 秒
        private double difficulty; // 题目难度1-5

        public UserBehavior() {}

        public UserBehavior(Long userId, Long questionId, boolean correct, int timeSpent, double difficulty) {
            this.userId = userId;
            this.questionId = questionId;
            this.correct = correct;
            this.timeSpent = timeSpent;
            this.difficulty = difficulty;
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
        public int getTimeSpent() { return timeSpent; }
        public void setTimeSpent(int timeSpent) { this.timeSpent = timeSpent; }
        public double getDifficulty() { return difficulty; }
        public void setDifficulty(double difficulty) { this.difficulty = difficulty; }
    }

    /**
     * 用户相似度
     */
    static class UserSimilarity implements Comparable<UserSimilarity> {
        Long userId;
        double similarity;

        public UserSimilarity(Long userId, double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }

        @Override
        public int compareTo(UserSimilarity o) {
            return Double.compare(o.similarity, this.similarity); // 降序
        }
    }

    /**
     * 题目推荐结果
     */
    public static class QuestionRecommendation {
        private Long questionId;
        private String content;
        private double score; // 推荐分数
        private String reason; // 推荐理由
        private Integer questionType;
        private String subjectName;

        // Getters and Setters
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Integer getQuestionType() { return questionType; }
        public void setQuestionType(Integer questionType) { this.questionType = questionType; }
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    }

    /**
     * 课程推荐结果
     */
    public static class CourseRecommendation {
        private Long courseId;
        private String name;
        private String description;
        private double score;
        private String reason;
        private String coverUrl;

        // Getters and Setters
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getCoverUrl() { return coverUrl; }
        public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    }

    /**
     * 推荐题目（协同过滤）
     * @param userId 当前用户ID
     * @param subjectId 科目ID（可选）
     * @param limit 推荐数量
     * @return 推荐题目列表
     */
    @Cacheable(value = "questionRecommendations", key = "#userId + '_' + #subjectId + '_' + #limit")
    public List<QuestionRecommendation> recommendQuestions(Long userId, Long subjectId, int limit) {
        log.info("为用户 {} 推荐题目，科目ID: {}, 数量: {}", userId, subjectId, limit);

        try {
            // 1. 获取所有用户的答题行为
            List<UserBehavior> allBehaviors = fetchAllUserBehaviors(subjectId);

            if (allBehaviors.isEmpty()) {
                log.warn("没有足够的用户行为数据，返回热门题目");
                return getPopularQuestions(subjectId, limit);
            }

            // 2. 计算用户相似度
            List<UserSimilarity> similarUsers = calculateSimilarUsers(userId, allBehaviors, 20);

            if (similarUsers.isEmpty()) {
                log.info("没有相似用户，返回热门题目");
                return getPopularQuestions(subjectId, limit);
            }

            log.info("找到 {} 个相似用户", similarUsers.size());

            // 3. 获取当前用户已做题目
            Set<Long> doneQuestions = getDoneQuestionIds(userId);

            // 4. 推荐相似用户做过但当前用户未做的题目
            Map<Long, Double> questionScores = new HashMap<>();
            Map<Long, String> questionReasons = new HashMap<>();

            for (UserSimilarity similarUser : similarUsers) {
                List<UserBehavior> userBehaviors = allBehaviors.stream()
                        .filter(b -> b.userId.equals(similarUser.userId))
                        .collect(Collectors.toList());

                for (UserBehavior behavior : userBehaviors) {
                    if (!doneQuestions.contains(behavior.questionId)) {
                        // 计算推荐分数
                        double score = calculateRecommendScore(behavior, similarUser.similarity);
                        questionScores.put(behavior.questionId,
                                questionScores.getOrDefault(behavior.questionId, 0.0) + score);

                        // 生成推荐理由
                        if (!questionReasons.containsKey(behavior.questionId)) {
                            questionReasons.put(behavior.questionId,
                                    generateRecommendReason(behavior, similarUser.similarity));
                        }
                    }
                }
            }

            // 5. 按分数排序并返回Top N
            List<QuestionRecommendation> recommendations = questionScores.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .limit(limit)
                    .map(entry -> {
                        QuestionRecommendation rec = new QuestionRecommendation();
                        rec.setQuestionId(entry.getKey());
                        rec.setScore(entry.getValue());
                        rec.setReason(questionReasons.get(entry.getKey()));

                        // 查询题目详情
                        BizQuestion question = questionMapper.selectById(entry.getKey());
                        if (question != null) {
                            rec.setContent(question.getContent());
                            rec.setQuestionType(question.getQuestionType());
                        }

                        return rec;
                    })
                    .collect(Collectors.toList());

            log.info("成功生成 {} 条推荐", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("推荐算法执行失败", e);
            return getPopularQuestions(subjectId, limit);
        }
    }

    /**
     * 计算用户相似度（余弦相似度）
     */
    private List<UserSimilarity> calculateSimilarUsers(Long targetUserId, List<UserBehavior> allBehaviors, int k) {
        // 获取目标用户的行为向量
        Map<Long, UserBehavior> targetVector = allBehaviors.stream()
                .filter(b -> b.userId.equals(targetUserId))
                .collect(Collectors.toMap(b -> b.questionId, b -> b, (a, b) -> a));

        if (targetVector.isEmpty()) {
            log.warn("目标用户 {} 没有行为数据", targetUserId);
            return Collections.emptyList();
        }

        // 计算与其他用户的相似度
        Map<Long, List<UserBehavior>> userBehaviors = allBehaviors.stream()
                .filter(b -> !b.userId.equals(targetUserId))
                .collect(Collectors.groupingBy(b -> b.userId));

        List<UserSimilarity> similarities = new ArrayList<>();

        for (Map.Entry<Long, List<UserBehavior>> entry : userBehaviors.entrySet()) {
            Long otherUserId = entry.getKey();
            Map<Long, UserBehavior> otherVector = entry.getValue().stream()
                    .collect(Collectors.toMap(b -> b.questionId, b -> b, (a, b) -> a));

            double similarity = cosineSimilarity(targetVector, otherVector);
            if (similarity > 0.1) { // 相似度阈值
                similarities.add(new UserSimilarity(otherUserId, similarity));
            }
        }

        // 排序并返回Top K
        Collections.sort(similarities);
        return similarities.stream().limit(k).collect(Collectors.toList());
    }

    /**
     * 余弦相似度计算
     */
    private double cosineSimilarity(Map<Long, UserBehavior> vec1, Map<Long, UserBehavior> vec2) {
        Set<Long> commonQuestions = new HashSet<>(vec1.keySet());
        commonQuestions.retainAll(vec2.keySet());

        if (commonQuestions.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long questionId : commonQuestions) {
            UserBehavior b1 = vec1.get(questionId);
            UserBehavior b2 = vec2.get(questionId);

            // 特征向量：[正确性(0/1), 归一化时间(0-1), 难度(1-5)]
            double[] f1 = buildFeatureVector(b1);
            double[] f2 = buildFeatureVector(b2);

            for (int i = 0; i < f1.length; i++) {
                dotProduct += f1[i] * f2[i];
                norm1 += f1[i] * f1[i];
                norm2 += f2[i] * f2[i];
            }
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 构建特征向量
     */
    private double[] buildFeatureVector(UserBehavior behavior) {
        return new double[]{
                behavior.correct ? 1.0 : 0.0,
                Math.min(behavior.timeSpent / 300.0, 1.0), // 归一化到0-1
                behavior.difficulty / 5.0
        };
    }

    /**
     * 计算推荐分数
     */
    private double calculateRecommendScore(UserBehavior behavior, double userSimilarity) {
        // 综合考虑用户相似度、题目难度、答题正确性
        double baseScore = userSimilarity * 10;

        // 如果相似用户答对了，增加权重
        if (behavior.correct) {
            baseScore *= 1.5;
        }

        // 难度适中的题目优先推荐
        double difficultyFactor = 1.0 - Math.abs(behavior.difficulty - 3.0) / 5.0;
        baseScore *= (0.5 + difficultyFactor);

        return baseScore;
    }

    /**
     * 生成推荐理由
     */
    private String generateRecommendReason(UserBehavior behavior, double similarity) {
        if (similarity > 0.7) {
            return "与你学习习惯相似的同学都在练习这道题";
        } else if (behavior.correct) {
            return "这道题难度适中，建议巩固练习";
        } else {
            return "这道题是易错点，值得挑战";
        }
    }

    /**
     * 获取所有用户答题行为
     */
    private List<UserBehavior> fetchAllUserBehaviors(Long subjectId) {
        List<UserBehavior> behaviors = new ArrayList<>();

        try {
            // 查询考试结果，获取用户答题数据
            LambdaQueryWrapper<BizExamResult> wrapper = new LambdaQueryWrapper<>();
            // 只查询最近3个月的数据，避免数据量过大
            wrapper.ge(BizExamResult::getCreateTime,
                LocalDateTime.now().minusMonths(3));
            wrapper.orderByDesc(BizExamResult::getCreateTime);
            wrapper.last("LIMIT 1000"); // 限制数量

            List<BizExamResult> examResults = examResultMapper.selectList(wrapper);

            for (BizExamResult result : examResults) {
                if (result.getUserAnswers() == null) continue;

                try {
                    // 解析答案JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode answersNode = objectMapper.readTree(result.getUserAnswers());

                    if (answersNode.isArray()) {
                        for (JsonNode answerNode : answersNode) {
                            Long questionId = answerNode.get("questionId").asLong();
                            boolean correct = answerNode.has("correct") && answerNode.get("correct").asBoolean();
                            int timeSpent = answerNode.has("timeSpent") ? answerNode.get("timeSpent").asInt() : 60;

                            // 查询题目获取难度和科目
                            BizQuestion question = questionMapper.selectById(questionId);
                            if (question != null) {
                                // 如果指定了科目，只获取该科目的数据
                                if (subjectId != null && !question.getSubjectId().equals(subjectId)) {
                                    continue;
                                }

                                double difficulty = question.getDifficulty() != null ? question.getDifficulty() : 3.0;

                                UserBehavior behavior = new UserBehavior(
                                    result.getStudentId(),
                                    questionId,
                                    correct,
                                    timeSpent,
                                    difficulty
                                );

                                behaviors.add(behavior);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析答案JSON失败: {}", e.getMessage());
                }
            }

            log.info("成功加载 {} 条用户行为数据", behaviors.size());

        } catch (Exception e) {
            log.error("获取用户行为数据失败", e);
        }

        return behaviors;
    }

    /**
     * 获取用户已做题目ID
     */
    private Set<Long> getDoneQuestionIds(Long userId) {
        Set<Long> doneQuestions = new HashSet<>();

        try {
            // 查询该用户所有考试记录
            LambdaQueryWrapper<BizExamResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizExamResult::getStudentId, userId);
            wrapper.orderByDesc(BizExamResult::getCreateTime);

            List<BizExamResult> examResults = examResultMapper.selectList(wrapper);

            ObjectMapper objectMapper = new ObjectMapper();
            for (BizExamResult result : examResults) {
                if (result.getUserAnswers() == null) continue;

                try {
                    JsonNode answersNode = objectMapper.readTree(result.getUserAnswers());
                    if (answersNode.isArray()) {
                        for (JsonNode answerNode : answersNode) {
                            Long questionId = answerNode.get("questionId").asLong();
                            doneQuestions.add(questionId);
                        }
                    }
                } catch (Exception e) {
                    log.error("解析已做题目失败", e);
                }
            }

            log.info("用户 {} 已做 {} 道题目", userId, doneQuestions.size());

        } catch (Exception e) {
            log.error("获取已做题目失败", e);
        }

        return doneQuestions;
    }

    /**
     * 获取热门题目（降级方案）
     */
    private List<QuestionRecommendation> getPopularQuestions(Long subjectId, int limit) {
        log.info("返回热门题目，数量: {}", limit);

        List<QuestionRecommendation> recommendations = new ArrayList<>();

        try {
            LambdaQueryWrapper<BizQuestion> wrapper = new LambdaQueryWrapper<>();
            if (subjectId != null) {
                wrapper.eq(BizQuestion::getSubjectId, subjectId);
            }
            wrapper.orderByDesc(BizQuestion::getId);
            wrapper.last("LIMIT " + limit);

            List<BizQuestion> questions = questionMapper.selectList(wrapper);

            for (BizQuestion question : questions) {
                QuestionRecommendation rec = new QuestionRecommendation();
                rec.setQuestionId(question.getId());
                rec.setContent(question.getContent());
                rec.setScore(5.0);
                rec.setReason("热门题目推荐");
                rec.setQuestionType(question.getQuestionType());
                recommendations.add(rec);
            }

        } catch (Exception e) {
            log.error("查询热门题目失败", e);
        }

        return recommendations;
    }

    /**
     * 推荐课程
     */
    public List<CourseRecommendation> recommendCourses(Long userId, int limit) {
        log.info("为用户 {} 推荐课程，数量: {}", userId, limit);

        List<CourseRecommendation> recommendations = new ArrayList<>();

        // TODO: 实现基于内容的课程推荐
        // 1. 分析用户薄弱科目
        // 2. 推荐对应科目的课程

        // 模拟数据
        for (int i = 0; i < Math.min(limit, 3); i++) {
            CourseRecommendation rec = new CourseRecommendation();
            rec.setCourseId((long) (300 + i));
            rec.setName("推荐课程 #" + (300 + i));
            rec.setDescription("这是一个优质课程");
            rec.setScore(8.5);
            rec.setReason("根据你的薄弱环节推荐");
            recommendations.add(rec);
        }

        return recommendations;
    }
}
