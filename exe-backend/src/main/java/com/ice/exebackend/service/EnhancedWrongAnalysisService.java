package com.ice.exebackend.service;

import com.ice.exebackend.dto.AiAnalysisReq;
import com.ice.exebackend.utils.AiHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 增强的错因深度分析服务
 * 基于学生画像、知识图谱提供个性化的深度分析
 *
 * @author AI功能组
 * @version v3.04
 */
@Service
public class EnhancedWrongAnalysisService {

    @Autowired
    private AiHttpClient aiHttpClient;

    @Autowired
    private StudentLearningProfileService profileService;

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    /**
     * 深度错因分析结果
     */
    public static class DeepAnalysisResult {
        private String fullAnalysis;         // 完整的AI分析文本（Markdown格式）
        private List<String> knowledgePoints; // 涉及的知识点
        private String errorType;            // 错误类型
        private List<String> suggestions;    // 建议列表
        private List<LearningPathStep> learningPath; // 学习路径
        private double confidence;           // 分析置信度 0-1

        public DeepAnalysisResult() {
            this.knowledgePoints = new ArrayList<>();
            this.suggestions = new ArrayList<>();
            this.learningPath = new ArrayList<>();
        }

        // Getters and Setters
        public String getFullAnalysis() { return fullAnalysis; }
        public void setFullAnalysis(String fullAnalysis) { this.fullAnalysis = fullAnalysis; }

        public List<String> getKnowledgePoints() { return knowledgePoints; }
        public void setKnowledgePoints(List<String> knowledgePoints) { this.knowledgePoints = knowledgePoints; }

        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }

        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

        public List<LearningPathStep> getLearningPath() { return learningPath; }
        public void setLearningPath(List<LearningPathStep> learningPath) { this.learningPath = learningPath; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }

    /**
     * 学习路径步骤
     */
    public static class LearningPathStep {
        private String title;           // 步骤标题
        private String description;     // 步骤描述
        private String type;            // 类型：REVIEW/PRACTICE/IMPROVE
        private Long resourceId;        // 关联资源ID（题目、课程等）
        private String resourceType;    // 资源类型：question/course/video
        private int order;              // 步骤顺序

        public LearningPathStep() {}

        public LearningPathStep(String title, String description, String type, Long resourceId) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.resourceId = resourceId;
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Long getResourceId() { return resourceId; }
        public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }

        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
    }

    /**
     * 深度错因分析（主方法）
     *
     * @param apiKey AI API Key
     * @param provider AI提供商
     * @param userId 学生ID
     * @param req 分析请求
     * @return 深度分析结果
     */
    public DeepAnalysisResult analyzeWrongQuestionDeep(
            String apiKey,
            String provider,
            Long userId,
            AiAnalysisReq req
    ) throws Exception {

        // 1. 获取学生学习画像
        StudentLearningProfileService.StudentLearningProfile profile =
                profileService.getProfile(userId);

        // 2. 知识图谱溯源（如果有知识点ID）
        List<KnowledgeGraphService.KnowledgePoint> prerequisitePoints = new ArrayList<>();
        if (req.getKnowledgePointId() != null) {
            prerequisitePoints = knowledgeGraphService.tracePrerequisites(
                    req.getKnowledgePointId(), userId);
        }

        // 3. 构建个性化提示词
        String enhancedPrompt = buildEnhancedPrompt(req, profile, prerequisitePoints);

        // 4. 调用AI分析
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", getSystemPrompt()));
        messages.add(Map.of("role", "user", "content", enhancedPrompt));

        String aiResponse = aiHttpClient.sendRequest(apiKey, provider, messages, 0.7, 60);

        // 5. 解析分析结果
        DeepAnalysisResult result = new DeepAnalysisResult();
        result.setFullAnalysis(aiResponse);

        // 6. 提取结构化信息（简化版本）
        extractStructuredInfo(result, aiResponse);

        // 7. 附加学习路径
        List<LearningPathStep> learningPath = generateLearningPath(userId, req, prerequisitePoints);
        result.setLearningPath(learningPath);

        // 8. 设置置信度
        result.setConfidence(calculateConfidence(profile, prerequisitePoints));

        return result;
    }

    /**
     * 构建增强的提示词
     */
    private String buildEnhancedPrompt(
            AiAnalysisReq req,
            StudentLearningProfileService.StudentLearningProfile profile,
            List<KnowledgeGraphService.KnowledgePoint> prerequisites
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("# 学生错题深度分析\n\n");

        // 学生画像部分
        prompt.append("## 📊 学生画像\n\n");
        prompt.append(String.format("- **整体水平**: %s\n", profile.getLevel()));
        prompt.append(String.format("- **学习风格**: %s\n", profile.getLearningStyle()));
        prompt.append(String.format("- **平均正确率**: %.1f%%\n", profile.getAverageAccuracy() * 100));
        prompt.append(String.format("- **总答题数**: %d 题\n", profile.getTotalQuestionsDone()));

        if (!profile.getWeakPoints().isEmpty()) {
            prompt.append(String.format("- **薄弱知识点**: %s\n",
                    String.join("、", profile.getWeakPoints())));
        }

        if (!profile.getStrongPoints().isEmpty()) {
            prompt.append(String.format("- **擅长知识点**: %s\n",
                    String.join("、", profile.getStrongPoints())));
        }

        prompt.append(String.format("- **常见错误**: %s\n\n", profile.getCommonMistakeType()));

        // 题目信息部分
        prompt.append("## 📝 题目信息\n\n");
        prompt.append(String.format("**题目内容**:\n%s\n\n", req.getQuestionContent()));
        prompt.append(String.format("**正确答案**: %s\n\n", req.getCorrectAnswer()));
        prompt.append(String.format("**学生答案**: %s\n\n", req.getStudentAnswer()));

        if (req.getAnalysis() != null && !req.getAnalysis().isEmpty()) {
            prompt.append(String.format("**标准解析**:\n%s\n\n", req.getAnalysis()));
        }

        // 知识图谱溯源部分
        if (!prerequisites.isEmpty()) {
            prompt.append("## 🔍 知识图谱溯源\n\n");
            prompt.append("该题目涉及以下前置知识点：\n\n");

            for (KnowledgeGraphService.KnowledgePoint kp : prerequisites) {
                prompt.append(String.format("- **%s**（掌握度：%.0f%%）\n",
                        kp.getName(), kp.getMasteryLevel() * 100));

                if (kp.getDescription() != null && !kp.getDescription().isEmpty()) {
                    prompt.append(String.format("  - 说明：%s\n", kp.getDescription()));
                }

                if (kp.getTotalCount() > 0) {
                    prompt.append(String.format("  - 练习情况：共答 %d 题，错 %d 题\n",
                            kp.getTotalCount(), kp.getErrorCount()));
                }
            }
            prompt.append("\n");
        }

        // 分析要求部分
        prompt.append("## 🎯 分析要求\n\n");
        prompt.append("请从以下**四个维度**进行深度分析，并用Markdown格式输出：\n\n");

        prompt.append("### 1. 📚 知识点诊断\n");
        prompt.append("- 定位具体的知识漏洞\n");
        prompt.append("- 检查前置知识是否掌握\n");
        prompt.append("- 判断是概念理解错误还是公式记忆错误\n\n");

        prompt.append("### 2. 💭 思维方式分析\n");
        prompt.append("- 分析学生的解题思路哪里出现了偏差\n");
        prompt.append("- 是逻辑推理错误、计算失误还是审题不清\n");
        prompt.append("- 指出正确的思维路径\n\n");

        prompt.append("### 3. 📖 学习习惯评估\n");
        prompt.append("- 结合学生画像，判断是粗心大意还是真的不会\n");
        prompt.append("- 分析时间管理和应试技巧问题\n");
        prompt.append("- 提供针对性的学习习惯建议\n\n");

        prompt.append("### 4. 🌟 个性化建议\n");
        prompt.append("- 结合学生的整体水平和学习风格\n");
        prompt.append("- 给出3-5条具体、可操作的改进建议\n");
        prompt.append("- 建议应该鼓励为主，语气耐心友善\n\n");

        prompt.append("---\n\n");
        prompt.append("**注意事项**:\n");
        prompt.append("1. 分析要深入具体，不要泛泛而谈\n");
        prompt.append("2. 语气要鼓励和耐心，多用\"建议\"\"可以\"等词汇\n");
        prompt.append("3. 给出的建议要具体可操作，避免空洞的套话\n");
        prompt.append("4. 充分利用学生画像信息，做到真正个性化\n");

        return prompt.toString();
    }

    /**
     * 获取系统提示词
     */
    private String getSystemPrompt() {
        return """
            你是一位资深的教育专家和心理咨询师，拥有20年的一线教学经验。

            你的专长：
            1. 深入分析学生的学习问题，不仅看表面现象，更挖掘深层原因
            2. 关注学生的思维方式、学习习惯和心理状态
            3. 给出的建议总是具体、可操作，并且充满鼓励
            4. 擅长因材施教，能够根据学生的个性特点提供个性化指导

            你的原则：
            - 永远保持耐心和鼓励的态度
            - 分析要深入但不要过于严厉
            - 建议要具体但不要太啰嗦
            - 相信每个学生都有进步的潜力

            请严格按照用户给出的格式要求输出分析内容。
            """;
    }

    /**
     * 提取结构化信息（从AI回答中）
     */
    private void extractStructuredInfo(DeepAnalysisResult result, String aiResponse) {
        // 简化版本：使用关键词匹配提取信息

        // 提取知识点
        List<String> knowledgePoints = new ArrayList<>();
        if (aiResponse.contains("知识点") || aiResponse.contains("概念")) {
            // 这里可以用更复杂的NLP技术提取
            knowledgePoints.add("需要复习的相关知识点");
        }
        result.setKnowledgePoints(knowledgePoints);

        // 提取错误类型
        String errorType = "综合性错误";
        if (aiResponse.contains("概念理解")) {
            errorType = "概念理解错误";
        } else if (aiResponse.contains("计算") || aiResponse.contains("粗心")) {
            errorType = "计算失误/粗心";
        } else if (aiResponse.contains("审题")) {
            errorType = "审题不清";
        } else if (aiResponse.contains("逻辑")) {
            errorType = "逻辑推理错误";
        }
        result.setErrorType(errorType);

        // 提取建议（简化版本）
        List<String> suggestions = new ArrayList<>();
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("-") || line.trim().startsWith("•") ||
                line.contains("建议") || line.contains("可以")) {
                String suggestion = line.trim()
                        .replaceFirst("^[-•*]\\s*", "")
                        .trim();
                if (!suggestion.isEmpty() && suggestion.length() > 5) {
                    suggestions.add(suggestion);
                }
            }
        }
        if (suggestions.size() > 5) {
            suggestions = suggestions.subList(0, 5);
        }
        result.setSuggestions(suggestions);
    }

    /**
     * 生成学习路径
     */
    private List<LearningPathStep> generateLearningPath(
            Long userId,
            AiAnalysisReq req,
            List<KnowledgeGraphService.KnowledgePoint> prerequisites
    ) {
        List<LearningPathStep> path = new ArrayList<>();
        int order = 1;

        // 步骤1: 复习前置知识（如果掌握度低）
        for (KnowledgeGraphService.KnowledgePoint kp : prerequisites) {
            if (kp.getMasteryLevel() < 0.7) {
                LearningPathStep step = new LearningPathStep();
                step.setOrder(order++);
                step.setTitle(String.format("巩固基础：%s", kp.getName()));
                step.setDescription(String.format(
                        "当前掌握度仅%.0f%%，建议先复习这个基础知识点",
                        kp.getMasteryLevel() * 100));
                step.setType("REVIEW");
                step.setResourceId(kp.getId());
                step.setResourceType("knowledge_point");
                path.add(step);
            }
        }

        // 步骤2: 专项练习
        LearningPathStep practiceStep = new LearningPathStep();
        practiceStep.setOrder(order++);
        practiceStep.setTitle("专项练习");
        practiceStep.setDescription("针对该类型题目进行专项训练，建议完成10-15道类似题目");
        practiceStep.setType("PRACTICE");
        practiceStep.setResourceType("question_set");
        path.add(practiceStep);

        // 步骤3: 错题回顾
        LearningPathStep reviewStep = new LearningPathStep();
        reviewStep.setOrder(order++);
        reviewStep.setTitle("错题回顾");
        reviewStep.setDescription("3天后重新做这道题，检验是否真正掌握");
        reviewStep.setType("REVIEW");
        reviewStep.setResourceId(req.getQuestionId());
        reviewStep.setResourceType("question");
        path.add(reviewStep);

        // 步骤4: 综合提升
        LearningPathStep improveStep = new LearningPathStep();
        improveStep.setOrder(order++);
        improveStep.setTitle("综合提升");
        improveStep.setDescription("尝试更高难度的综合题目，提升应用能力");
        improveStep.setType("IMPROVE");
        improveStep.setResourceType("advanced_questions");
        path.add(improveStep);

        return path;
    }

    /**
     * 计算分析置信度
     */
    private double calculateConfidence(
            StudentLearningProfileService.StudentLearningProfile profile,
            List<KnowledgeGraphService.KnowledgePoint> prerequisites
    ) {
        double confidence = 0.5; // 基础置信度

        // 如果有足够的历史数据，提高置信度
        if (profile.getTotalQuestionsDone() > 50) {
            confidence += 0.2;
        } else if (profile.getTotalQuestionsDone() > 20) {
            confidence += 0.1;
        }

        // 如果有知识图谱数据，提高置信度
        if (!prerequisites.isEmpty()) {
            confidence += 0.15;
        }

        // 如果学生画像完整，提高置信度
        if (!profile.getWeakPoints().isEmpty() && !profile.getStrongPoints().isEmpty()) {
            confidence += 0.15;
        }

        return Math.min(confidence, 1.0);
    }
}
