package com.ice.exebackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.config.AiConfig;
import com.ice.exebackend.dto.AiAnalysisReq;
import com.ice.exebackend.dto.AiGeneratedQuestionDTO;
import com.ice.exebackend.dto.AiGradingResult;
import com.ice.exebackend.utils.AiHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 服务（优化版）
 * 集成缓存、重试、配置化等特性
 */
@Service
public class AiServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(AiServiceV2.class);

    @Autowired
    private AiHttpClient aiHttpClient;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    /**
     * AI 错题分析（优化版）
     *
     * @param apiKey      API Key
     * @param providerKey 提供商标识
     * @param req         分析请求
     * @return 分析结果
     */
    public String analyzeWrongQuestion(String apiKey, String providerKey, AiAnalysisReq req) throws Exception {
        log.info("AI错题分析请求: provider={}, question={}", providerKey, req.getQuestionContent().substring(0, Math.min(50, req.getQuestionContent().length())));

        // 检查缓存
        String cacheKey = generateCacheKey("analyze", req);
        String cachedResult = getFromCache(cacheKey);
        if (cachedResult != null) {
            log.info("命中缓存: {}", cacheKey);
            return cachedResult;
        }

        // 构建提示词
        String systemPrompt = "你是一位经验丰富的中学全科教师。请根据学生提供的错题信息，进行深入浅出的分析。包含三个部分：\n" +
                "1. 【知识点回顾】：简要回顾题目涉及的核心考点。\n" +
                "2. 【错误原因推测】：分析学生为什么会填这个错误答案（例如概念混淆、计算失误等）。\n" +
                "3. 【解题思路】：给出正确的推导步骤。\n" +
                "请使用 Markdown 格式输出，保持语气鼓励和耐心。";

        String userPrompt = String.format(
                "题目：%s\n正确答案：%s\n学生的错误答案：%s\n原解析参考：%s",
                req.getQuestionContent(), req.getCorrectAnswer(),
                req.getStudentAnswer(), req.getAnalysis() != null ? req.getAnalysis() : "无"
        );

        // 发送请求
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        String result = aiHttpClient.sendRequest(
                apiKey, providerKey, messages, 0.7, aiConfig.getTimeout().getAnalyze()
        );

        // 保存到缓存
        saveToCache(cacheKey, result);

        return result;
    }

    /**
     * AI 主观题批改（优化版）
     *
     * @param apiKey          API Key
     * @param providerKey     提供商标识
     * @param questionContent 题目内容
     * @param referenceAnswer 参考答案
     * @param studentAnswer   学生答案
     * @param maxScore        满分
     * @return 评分结果
     */
    public AiGradingResult gradeSubjectiveQuestion(String apiKey, String providerKey,
                                                   String questionContent, String referenceAnswer,
                                                   String studentAnswer, int maxScore) throws Exception {
        log.info("AI主观题批改请求: provider={}, maxScore={}", providerKey, maxScore);

        // 构建提示词
        String systemPrompt = "你是一位公正的阅卷老师。请根据题目、参考答案和学生答案进行打分。" +
                "请务必严格只返回一个合法的 JSON 字符串，不要包含 Markdown 格式（如 ```json ... ```），也不要包含其他多余文字。\n" +
                "JSON 格式要求：\n" +
                "{\n" +
                "  \"score\": (整数，0 到 " + maxScore + " 之间),\n" +
                "  \"feedback\": \"(简短的评语，指出优点或不足)\"\n" +
                "}";

        String userPrompt = String.format(
                "题目：%s\n参考答案：%s\n该题满分：%d 分\n学生回答：%s",
                questionContent, referenceAnswer, maxScore, studentAnswer
        );

        // 发送请求
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        String content = aiHttpClient.sendRequest(
                apiKey, providerKey, messages, 0.3, aiConfig.getTimeout().getGrading()
        );

        // 解析JSON
        String jsonString = aiHttpClient.extractJson(content);

        try {
            return objectMapper.readValue(jsonString, AiGradingResult.class);
        } catch (Exception e) {
            log.error("AI批改结果解析失败: {}", content);
            throw new RuntimeException("AI 批改结果格式错误，请重试");
        }
    }

    /**
     * AI 智能出题（优化版）
     *
     * @param apiKey      API Key
     * @param providerKey 提供商标识
     * @param text        文本内容
     * @param count       生成数量
     * @param type        题目类型
     * @return 题目列表
     */
    public List<AiGeneratedQuestionDTO> generateQuestionsFromText(String apiKey, String providerKey,
                                                                   String text, int count, int type) throws Exception {
        log.info("AI智能出题请求: provider={}, count={}, type={}", providerKey, count, type);

        // 检查缓存
        String cacheKey = generateCacheKey("generate", Map.of("text", text, "count", count, "type", type));
        String cachedResult = getFromCache(cacheKey);
        if (cachedResult != null) {
            log.info("命中缓存: {}", cacheKey);
            return objectMapper.readValue(cachedResult, new TypeReference<List<AiGeneratedQuestionDTO>>() {
            });
        }

        // 构建提示词
        String typeDesc = switch (type) {
            case 1 -> "单选题";
            case 2 -> "多选题";
            case 3 -> "填空题";
            case 4 -> "判断题";
            case 5 -> "主观题";
            default -> "混合题型（包含单选、多选、填空、判断、主观题）";
        };

        String systemPrompt = "你是一位专业的出题专家。请根据用户提供的文本内容，生成 " + count + " 道 " + typeDesc + "。\n" +
                "请务必严格只返回一个合法的 JSON 数组，不要包含 Markdown 代码块标记（如 ```json），也不要包含其他多余文字。\n" +
                "JSON 数组中每个对象的格式如下：\n" +
                "{\n" +
                "  \"content\": \"题目内容\",\n" +
                "  \"questionType\": 1, // 1单选 2多选 3填空 4判断 5主观题\n" +
                "  \"options\": [{\"key\":\"A\",\"value\":\"选项1\"},{\"key\":\"B\",\"value\":\"选项2\"}], // 选择题必填，填空/判断/主观题可为空数组\n" +
                "  \"answer\": \"参考答案\", // 答案\n" +
                "  \"description\": \"解析或评分要点\"\n" +
                "}";

        String userPrompt = "文本内容如下：\n" + (text.length() > 3000 ? text.substring(0, 3000) : text);

        // 发送请求
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        String content = aiHttpClient.sendRequest(
                apiKey, providerKey, messages, 0.5, aiConfig.getTimeout().getGenerate()
        );

        // 解析JSON
        String cleanJson = aiHttpClient.extractJson(content);
        List<AiGeneratedQuestionDTO> result = parseQuestionList(cleanJson);

        // 保存到缓存
        saveToCache(cacheKey, objectMapper.writeValueAsString(result));

        return result;
    }

    /**
     * AI 提取知识点（优化版）
     *
     * @param apiKey      API Key
     * @param providerKey 提供商标识
     * @param text        文本内容
     * @param count       提取数量
     * @return 知识点列表
     */
    public List<Map<String, String>> generateKnowledgePointsFromText(String apiKey, String providerKey,
                                                                       String text, int count) throws Exception {
        log.info("AI知识点提取请求: provider={}, count={}", providerKey, count);

        // 检查缓存
        String cacheKey = generateCacheKey("extract", Map.of("text", text, "count", count));
        String cachedResult = getFromCache(cacheKey);
        if (cachedResult != null) {
            log.info("命中缓存: {}", cacheKey);
            return objectMapper.readValue(cachedResult, new TypeReference<List<Map<String, String>>>() {
            });
        }

        // 构建提示词
        String systemPrompt = "你是一位资深的教学专家。请根据用户提供的文本内容，提取出 " + count + " 个核心知识点。\n" +
                "请务必严格只返回一个合法的 JSON 数组，不要包含 Markdown 代码块标记（如 ```json），也不要包含其他多余文字。\n" +
                "JSON 数组中每个对象的格式如下：\n" +
                "{\n" +
                "  \"name\": \"知识点名称（简练准确）\",\n" +
                "  \"description\": \"知识点详细描述或定义（50-200字）\"\n" +
                "}";

        String userPrompt = "文本内容如下：\n" + (text.length() > 3000 ? text.substring(0, 3000) : text);

        // 发送请求
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        String content = aiHttpClient.sendRequest(
                apiKey, providerKey, messages, 0.5, aiConfig.getTimeout().getExtract()
        );

        // 解析JSON
        String cleanJson = aiHttpClient.extractJson(content);
        List<Map<String, String>> result = objectMapper.readValue(cleanJson, new TypeReference<List<Map<String, String>>>() {
        });

        // 保存到缓存
        saveToCache(cacheKey, objectMapper.writeValueAsString(result));

        return result;
    }

    /**
     * 解析题目列表（容错处理）
     */
    private List<AiGeneratedQuestionDTO> parseQuestionList(String jsonString) throws Exception {
        try {
            // 尝试直接解析为 List
            return objectMapper.readValue(jsonString, new TypeReference<List<AiGeneratedQuestionDTO>>() {
            });
        } catch (Exception e) {
            // 容错处理：尝试从对象中提取数组
            try {
                JsonNode root = objectMapper.readTree(jsonString);
                if (root.isObject()) {
                    // 遍历所有字段，找到第一个是数组的字段
                    Iterator<String> fieldNames = root.fieldNames();
                    while (fieldNames.hasNext()) {
                        String fieldName = fieldNames.next();
                        JsonNode field = root.get(fieldName);
                        if (field.isArray()) {
                            return objectMapper.convertValue(field, new TypeReference<List<AiGeneratedQuestionDTO>>() {
                            });
                        }
                    }
                    // 单个对象转为List
                    try {
                        AiGeneratedQuestionDTO singleQuestion = objectMapper.convertValue(root, AiGeneratedQuestionDTO.class);
                        if (singleQuestion.getContent() != null) {
                            return List.of(singleQuestion);
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ex) {
                log.error("AI返回的非标准JSON内容: {}", jsonString);
            }
            throw new RuntimeException("解析 AI 结果失败，请重试或检查文本内容。");
        }
    }

    /**
     * 生成缓存key
     */
    private String generateCacheKey(String operation, Object params) {
        try {
            String paramsJson = objectMapper.writeValueAsString(params);
            String hash = DigestUtils.md5DigestAsHex(paramsJson.getBytes(StandardCharsets.UTF_8));
            return aiConfig.getCache().getKeyPrefix() + operation + ":" + hash;
        } catch (Exception e) {
            log.error("生成缓存key失败", e);
            return aiConfig.getCache().getKeyPrefix() + operation + ":" + System.currentTimeMillis();
        }
    }

    /**
     * 从缓存获取
     */
    private String getFromCache(String key) {
        if (!aiConfig.getCache().isEnabled() || redisTemplate == null) {
            return null;
        }

        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("从缓存获取数据失败: {}", key, e);
            return null;
        }
    }

    /**
     * 保存到缓存
     */
    private void saveToCache(String key, String value) {
        if (!aiConfig.getCache().isEnabled() || redisTemplate == null) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, value, aiConfig.getCache().getTtl(), TimeUnit.SECONDS);
            log.info("缓存已保存: {}", key);
        } catch (Exception e) {
            log.error("保存到缓存失败: {}", key, e);
        }
    }
}
