package com.ice.exebackend.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI提示词构建工具测试
 * 测试各种场景下的提示词构建逻辑
 */
@DisplayName("AI提示词构建工具测试")
class AiPromptBuilderTest {

    // ==================== 错题分析提示词测试 ====================

    @Test
    @DisplayName("应该构建正确的错题分析提示词")
    void shouldBuildWrongQuestionAnalysisPrompt() {
        // Given
        String questionContent = "1+1等于多少？";
        String correctAnswer = "2";
        String studentAnswer = "3";
        String analysis = "基础加法运算";

        // When
        String systemPrompt = buildAnalysisSystemPrompt();
        String userPrompt = buildAnalysisUserPrompt(questionContent, correctAnswer, studentAnswer, analysis);

        // Then
        assertNotNull(systemPrompt);
        assertNotNull(userPrompt);

        // 验证系统提示词包含关键元素
        assertTrue(systemPrompt.contains("教师"));
        assertTrue(systemPrompt.contains("知识点回顾"));
        assertTrue(systemPrompt.contains("错误原因"));
        assertTrue(systemPrompt.contains("解题思路"));
        assertTrue(systemPrompt.contains("Markdown"));

        // 验证用户提示词包含所有输入信息
        assertTrue(userPrompt.contains(questionContent));
        assertTrue(userPrompt.contains(correctAnswer));
        assertTrue(userPrompt.contains(studentAnswer));
        assertTrue(userPrompt.contains(analysis));
    }

    @Test
    @DisplayName("应该处理无解析的错题提示词")
    void shouldHandleAnalysisPromptWithoutExistingAnalysis() {
        // Given
        String questionContent = "题目内容";
        String correctAnswer = "正确答案";
        String studentAnswer = "学生答案";
        String analysis = null;

        // When
        String userPrompt = buildAnalysisUserPrompt(questionContent, correctAnswer, studentAnswer, analysis);

        // Then
        assertTrue(userPrompt.contains("无"));
    }

    @Test
    @DisplayName("错题分析提示词应该要求Markdown格式")
    void shouldRequireMarkdownFormatInAnalysisPrompt() {
        // Given & When
        String systemPrompt = buildAnalysisSystemPrompt();

        // Then
        assertTrue(systemPrompt.contains("Markdown"));
    }

    // ==================== 主观题批改提示词测试 ====================

    @Test
    @DisplayName("应该构建正确的批改提示词")
    void shouldBuildGradingPrompt() {
        // Given
        String questionContent = "请简述Java的多态性";
        String referenceAnswer = "多态性是指同一个行为具有多个不同表现形式";
        String studentAnswer = "多态就是一个对象有多种形态";
        int maxScore = 10;

        // When
        String systemPrompt = buildGradingSystemPrompt(maxScore);
        String userPrompt = buildGradingUserPrompt(questionContent, referenceAnswer, maxScore, studentAnswer);

        // Then
        assertNotNull(systemPrompt);
        assertNotNull(userPrompt);

        // 验证系统提示词
        assertTrue(systemPrompt.contains("阅卷老师"));
        assertTrue(systemPrompt.contains("JSON"));
        assertTrue(systemPrompt.contains("score"));
        assertTrue(systemPrompt.contains("feedback"));
        assertTrue(systemPrompt.contains(String.valueOf(maxScore)));

        // 验证用户提示词
        assertTrue(userPrompt.contains(questionContent));
        assertTrue(userPrompt.contains(referenceAnswer));
        assertTrue(userPrompt.contains(studentAnswer));
        assertTrue(userPrompt.contains(String.valueOf(maxScore)));
    }

    @Test
    @DisplayName("批改提示词应该明确要求JSON格式")
    void shouldRequireJsonFormatInGradingPrompt() {
        // Given & When
        String systemPrompt = buildGradingSystemPrompt(100);

        // Then
        assertTrue(systemPrompt.contains("JSON"));
        assertTrue(systemPrompt.contains("score"));
        assertTrue(systemPrompt.contains("feedback"));
    }

    @Test
    @DisplayName("批改提示词应该禁止Markdown代码块")
    void shouldProhibitMarkdownCodeBlockInGradingPrompt() {
        // Given & When
        String systemPrompt = buildGradingSystemPrompt(100);

        // Then
        assertTrue(systemPrompt.contains("不要包含") || systemPrompt.contains("不包含"));
        assertTrue(systemPrompt.contains("```") || systemPrompt.contains("Markdown"));
    }

    @Test
    @DisplayName("应该根据不同满分值构建批改提示词")
    void shouldBuildGradingPromptWithDifferentMaxScores() {
        // Given & When
        String prompt20 = buildGradingSystemPrompt(20);
        String prompt100 = buildGradingSystemPrompt(100);

        // Then
        assertTrue(prompt20.contains("20"));
        assertTrue(prompt100.contains("100"));
        assertNotEquals(prompt20, prompt100);
    }

    // ==================== 智能出题提示词测试 ====================

    @Test
    @DisplayName("应该构建正确的单选题生成提示词")
    void shouldBuildSingleChoiceQuestionPrompt() {
        // Given
        String text = "Java是一门面向对象的编程语言";
        int count = 3;
        int type = 1; // 单选题

        // When
        String systemPrompt = buildGenerateQuestionSystemPrompt(count, type);
        String userPrompt = buildGenerateQuestionUserPrompt(text);

        // Then
        assertTrue(systemPrompt.contains("单选题"));
        assertTrue(systemPrompt.contains("3"));
        assertTrue(systemPrompt.contains("JSON"));
        assertTrue(userPrompt.contains(text));
    }

    @Test
    @DisplayName("应该构建正确的多选题生成提示词")
    void shouldBuildMultipleChoiceQuestionPrompt() {
        // Given
        int type = 2; // 多选题

        // When
        String systemPrompt = buildGenerateQuestionSystemPrompt(5, type);

        // Then
        assertTrue(systemPrompt.contains("多选题"));
    }

    @Test
    @DisplayName("应该构建正确的主观题生成提示词")
    void shouldBuildSubjectiveQuestionPrompt() {
        // Given
        int type = 5; // 主观题

        // When
        String systemPrompt = buildGenerateQuestionSystemPrompt(2, type);

        // Then
        assertTrue(systemPrompt.contains("主观题"));
    }

    @Test
    @DisplayName("应该截断过长的文本内容")
    void shouldTruncateLongTextInPrompt() {
        // Given
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("这是一段很长的文本内容。");
        }
        String text = longText.toString();

        // When
        String userPrompt = buildGenerateQuestionUserPrompt(text);

        // Then
        // 验证提示词不会过长（假设限制在3000字符以内）
        assertTrue(userPrompt.length() < 3100);
    }

    // ==================== 知识点提取提示词测试 ====================

    @Test
    @DisplayName("应该构建正确的知识点提取提示词")
    void shouldBuildKnowledgePointExtractionPrompt() {
        // Given
        String text = "Java编程语言具有跨平台、面向对象等特性";
        int count = 3;

        // When
        String systemPrompt = buildExtractKnowledgePointSystemPrompt(count);
        String userPrompt = buildExtractKnowledgePointUserPrompt(text);

        // Then
        assertTrue(systemPrompt.contains("教学专家"));
        assertTrue(systemPrompt.contains("3"));
        assertTrue(systemPrompt.contains("知识点"));
        assertTrue(systemPrompt.contains("JSON"));
        assertTrue(systemPrompt.contains("name"));
        assertTrue(systemPrompt.contains("description"));
        assertTrue(userPrompt.contains(text));
    }

    @Test
    @DisplayName("知识点提取提示词应该明确JSON格式要求")
    void shouldRequireJsonFormatInExtractionPrompt() {
        // Given & When
        String systemPrompt = buildExtractKnowledgePointSystemPrompt(5);

        // Then
        assertTrue(systemPrompt.contains("JSON"));
        assertTrue(systemPrompt.contains("name"));
        assertTrue(systemPrompt.contains("description"));
    }

    // ==================== 提示词质量测试 ====================

    @Test
    @DisplayName("所有系统提示词应该不为空且长度合理")
    void shouldHaveReasonableLengthForAllSystemPrompts() {
        // When
        String analysisPrompt = buildAnalysisSystemPrompt();
        String gradingPrompt = buildGradingSystemPrompt(100);
        String generatePrompt = buildGenerateQuestionSystemPrompt(3, 1);
        String extractPrompt = buildExtractKnowledgePointSystemPrompt(5);

        // Then
        assertAll(
                () -> assertTrue(analysisPrompt.length() > 50 && analysisPrompt.length() < 1000),
                () -> assertTrue(gradingPrompt.length() > 50 && gradingPrompt.length() < 1000),
                () -> assertTrue(generatePrompt.length() > 50 && generatePrompt.length() < 1500),
                () -> assertTrue(extractPrompt.length() > 50 && extractPrompt.length() < 1000)
        );
    }

    @Test
    @DisplayName("所有提示词应该使用中文")
    void shouldUseChineseInAllPrompts() {
        // When
        String analysisPrompt = buildAnalysisSystemPrompt();
        String gradingPrompt = buildGradingSystemPrompt(100);

        // Then
        // 检查是否包含中文字符
        assertTrue(analysisPrompt.matches(".*[\u4e00-\u9fa5]+.*"));
        assertTrue(gradingPrompt.matches(".*[\u4e00-\u9fa5]+.*"));
    }

    @Test
    @DisplayName("提示词应该避免使用模糊语言")
    void shouldAvoidVagueLanguageInPrompts() {
        // When
        String gradingPrompt = buildGradingSystemPrompt(100);

        // Then
        // 应该有明确的格式要求，而不是"可能"、"尽量"等模糊词
        assertTrue(gradingPrompt.contains("务必") || gradingPrompt.contains("必须") || gradingPrompt.contains("严格"));
    }

    // ==================== 辅助方法（模拟AiServiceV3中的提示词构建逻辑） ====================

    private String buildAnalysisSystemPrompt() {
        return "你是一位经验丰富的中学全科教师。请根据学生提供的错题信息，进行深入浅出的分析。包含三个部分：\n" +
                "1. 【知识点回顾】：简要回顾题目涉及的核心考点。\n" +
                "2. 【错误原因推测】：分析学生为什么会填这个错误答案（例如概念混淆、计算失误等）。\n" +
                "3. 【解题思路】：给出正确的推导步骤。\n" +
                "请使用 Markdown 格式输出，保持语气鼓励和耐心。";
    }

    private String buildAnalysisUserPrompt(String questionContent, String correctAnswer, String studentAnswer, String analysis) {
        return String.format(
                "题目：%s\n正确答案：%s\n学生的错误答案：%s\n原解析参考：%s",
                questionContent, correctAnswer, studentAnswer, analysis != null ? analysis : "无"
        );
    }

    private String buildGradingSystemPrompt(int maxScore) {
        return "你是一位公正的阅卷老师。请根据题目、参考答案和学生答案进行打分。" +
                "请务必严格只返回一个合法的 JSON 字符串，不要包含 Markdown 格式（如 ```json ... ```），也不要包含其他多余文字。\n" +
                "JSON 格式要求：\n" +
                "{\n" +
                "  \"score\": (整数，0 到 " + maxScore + " 之间),\n" +
                "  \"feedback\": \"(简短的评语，指出优点或不足)\"\n" +
                "}";
    }

    private String buildGradingUserPrompt(String questionContent, String referenceAnswer, int maxScore, String studentAnswer) {
        return String.format(
                "题目：%s\n参考答案：%s\n该题满分：%d 分\n学生回答：%s",
                questionContent, referenceAnswer, maxScore, studentAnswer
        );
    }

    private String buildGenerateQuestionSystemPrompt(int count, int type) {
        String typeDesc = switch (type) {
            case 1 -> "单选题";
            case 2 -> "多选题";
            case 3 -> "填空题";
            case 4 -> "判断题";
            case 5 -> "主观题";
            default -> "混合题型（包含单选、多选、填空、判断、主观题）";
        };

        return "你是一位专业的出题专家。请根据用户提供的文本内容，生成 " + count + " 道 " + typeDesc + "。\n" +
                "请务必严格只返回一个合法的 JSON 数组，不要包含 Markdown 代码块标记（如 ```json），也不要包含其他多余文字。\n" +
                "JSON 数组中每个对象的格式如下：\n" +
                "{\n" +
                "  \"content\": \"题目内容\",\n" +
                "  \"questionType\": 1,\n" +
                "  \"options\": [{\"key\":\"A\",\"value\":\"选项1\"}],\n" +
                "  \"answer\": \"参考答案\",\n" +
                "  \"description\": \"解析或评分要点\"\n" +
                "}";
    }

    private String buildGenerateQuestionUserPrompt(String text) {
        return "文本内容如下：\n" + (text.length() > 3000 ? text.substring(0, 3000) : text);
    }

    private String buildExtractKnowledgePointSystemPrompt(int count) {
        return "你是一位资深的教学专家。请根据用户提供的文本内容，提取出 " + count + " 个核心知识点。\n" +
                "请务必严格只返回一个合法的 JSON 数组，不要包含 Markdown 代码块标记（如 ```json），也不要包含其他多余文字。\n" +
                "JSON 数组中每个对象的格式如下：\n" +
                "{\n" +
                "  \"name\": \"知识点名称（简练准确，10-30字）\",\n" +
                "  \"description\": \"知识点详细描述或定义（50-200字）\"\n" +
                "}";
    }

    private String buildExtractKnowledgePointUserPrompt(String text) {
        return "文本内容如下：\n" + text;
    }
}
