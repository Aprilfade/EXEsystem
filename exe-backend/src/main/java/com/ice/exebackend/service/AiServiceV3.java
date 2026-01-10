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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI 服务（增强版 V3）
 * 集成缓存、重试、限流、统计等完整特性
 */
@Service
public class AiServiceV3 {

    private static final Logger log = LoggerFactory.getLogger(AiServiceV3.class);

    @Autowired
    private AiHttpClient aiHttpClient;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AiRateLimiter rateLimiter;

    @Autowired
    private AiCallLogService callLogService;

    @Autowired
    private AiCircuitBreaker circuitBreaker;

    @Autowired
    private BasicAnalyzer basicAnalyzer;

    /**
     * AI 错题分析（增强版 + 优雅降级）
     */
    public String analyzeWrongQuestion(String apiKey, String providerKey, AiAnalysisReq req, Long userId) throws Exception {
        return executeWithRateLimit(userId, "analyze", providerKey, () -> {
            log.info("AI错题分析请求: provider={}, user={}", providerKey, userId);

            // 【新增】检查断路器状态
            if (!circuitBreaker.allowRequest()) {
                log.warn("断路器打开，使用降级方案");
                String fallbackResult = basicAnalyzer.analyzeWrongQuestion(req);
                logCall(userId, "STUDENT", "analyze", "fallback", true, 0, false, 0,
                    null, "使用降级方案");
                return fallbackResult;
            }

            // 检查缓存
            String cacheKey = generateCacheKey("analyze", req);
            String cachedResult = getFromCache(cacheKey);
            if (cachedResult != null) {
                log.info("命中缓存: {}", cacheKey);
                logCall(userId, "STUDENT", "analyze", providerKey, true, 0, true, 0, null, "cached");
                circuitBreaker.recordSuccess(); // 【新增】缓存命中也算成功
                return cachedResult;
            }

            long startTime = System.currentTimeMillis();
            int retryCount = 0;
            boolean success = false;
            String errorMessage = null;
            String result = null;

            try {
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

                List<Map<String, String>> messages = List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                );

                result = aiHttpClient.sendRequest(
                        apiKey, providerKey, messages, 0.7, aiConfig.getTimeout().getAnalyze()
                );

                success = true;
                circuitBreaker.recordSuccess(); // 【新增】记录成功
                saveToCache(cacheKey, result);

            } catch (Exception e) {
                errorMessage = e.getMessage();
                circuitBreaker.recordFailure(); // 【新增】记录失败

                // 【新增】如果AI失败，使用降级方案
                log.warn("AI分析失败，使用降级方案: {}", e.getMessage());
                result = basicAnalyzer.analyzeWrongQuestion(req);
                success = true; // 降级成功也算成功
                errorMessage = "AI不可用，已降级: " + e.getMessage();

            } finally {
                long duration = System.currentTimeMillis() - startTime;
                logCall(userId, "STUDENT", "analyze", providerKey, success, duration, false, retryCount,
                        errorMessage, req.getQuestionContent().substring(0, Math.min(100, req.getQuestionContent().length())));
            }

            return result;
        });
    }

    /**
     * AI 主观题批改（增强版）
     */
    public AiGradingResult gradeSubjectiveQuestion(String apiKey, String providerKey,
                                                   String questionContent, String referenceAnswer,
                                                   String studentAnswer, int maxScore, Long userId) throws Exception {
        return executeWithRateLimit(userId, "grading", providerKey, () -> {
            log.info("AI主观题批改请求: provider={}, user={}, maxScore={}", providerKey, userId, maxScore);

            long startTime = System.currentTimeMillis();
            boolean success = false;
            String errorMessage = null;
            AiGradingResult result = null;

            try {
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

                List<Map<String, String>> messages = List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                );

                String content = aiHttpClient.sendRequest(
                        apiKey, providerKey, messages, 0.3, aiConfig.getTimeout().getGrading()
                );

                String jsonString = aiHttpClient.extractJson(content);
                result = objectMapper.readValue(jsonString, AiGradingResult.class);
                success = true;

            } catch (Exception e) {
                errorMessage = e.getMessage();
                throw new RuntimeException("AI 批改结果格式错误，请重试");
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                logCall(userId, "STUDENT", "grading", providerKey, success, duration, false, 0,
                        errorMessage, questionContent.substring(0, Math.min(100, questionContent.length())));
            }

            return result;
        });
    }

    /**
     * AI 智能出题（增强版）
     */
    public List<AiGeneratedQuestionDTO> generateQuestionsFromText(String apiKey, String providerKey,
                                                                   String text, int count, int type, Long userId) throws Exception {
        return executeWithRateLimit(userId, "generate", providerKey, () -> {
            log.info("AI智能出题请求: provider={}, user={}, count={}", providerKey, userId, count);

            // 检查缓存
            String cacheKey = generateCacheKey("generate", Map.of("text", text, "count", count, "type", type));
            String cachedResult = getFromCache(cacheKey);
            if (cachedResult != null) {
                log.info("命中缓存: {}", cacheKey);
                logCall(userId, "TEACHER", "generate", providerKey, true, 0, true, 0, null, "cached");
                return objectMapper.readValue(cachedResult, new TypeReference<List<AiGeneratedQuestionDTO>>() {
                });
            }

            long startTime = System.currentTimeMillis();
            boolean success = false;
            String errorMessage = null;
            List<AiGeneratedQuestionDTO> result = null;

            try {
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

                List<Map<String, String>> messages = List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                );

                String content = aiHttpClient.sendRequest(
                        apiKey, providerKey, messages, 0.5, aiConfig.getTimeout().getGenerate()
                );

                String cleanJson = aiHttpClient.extractJson(content);
                result = parseQuestionList(cleanJson);
                success = true;

                saveToCache(cacheKey, objectMapper.writeValueAsString(result));

            } catch (Exception e) {
                errorMessage = e.getMessage();
                throw e;
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                logCall(userId, "TEACHER", "generate", providerKey, success, duration, false, 0,
                        errorMessage, text.substring(0, Math.min(100, text.length())));
            }

            return result;
        });
    }

    /**
     * AI 提取知识点（增强版 + 大文本支持）
     */
    public List<Map<String, String>> generateKnowledgePointsFromText(String apiKey, String providerKey,
                                                                       String text, int count, Long userId) throws Exception {
        return executeWithRateLimit(userId, "extract", providerKey, () -> {
            log.info("AI知识点提取请求: provider={}, user={}, count={}, textLength={}",
                providerKey, userId, count, text.length());

            // 检查断路器状态
            if (!circuitBreaker.allowRequest()) {
                log.warn("断路器打开，使用降级方案");
                List<Map<String, String>> fallbackResult = basicAnalyzer.extractKnowledgePoints(text, count);
                logCall(userId, "TEACHER", "extract", "fallback", true, 0, false, 0,
                    null, "使用降级方案");
                return fallbackResult;
            }

            // 检查缓存
            String cacheKey = generateCacheKey("extract", Map.of("text", text, "count", count));
            String cachedResult = getFromCache(cacheKey);
            if (cachedResult != null) {
                log.info("命中缓存: {}", cacheKey);
                logCall(userId, "TEACHER", "extract", providerKey, true, 0, true, 0, null, "cached");
                circuitBreaker.recordSuccess();
                return objectMapper.readValue(cachedResult, new TypeReference<List<Map<String, String>>>() {
                });
            }

            long startTime = System.currentTimeMillis();
            boolean success = false;
            String errorMessage = null;
            List<Map<String, String>> result = null;

            try {
                // 【优化】根据文本长度智能处理
                if (text.length() <= 8000) {
                    // 小文本：直接处理
                    result = extractKnowledgePointsDirect(apiKey, providerKey, text, count);
                } else {
                    // 大文本：分块处理
                    result = extractKnowledgePointsChunked(apiKey, providerKey, text, count);
                }

                success = true;
                circuitBreaker.recordSuccess();
                saveToCache(cacheKey, objectMapper.writeValueAsString(result));

            } catch (Exception e) {
                errorMessage = e.getMessage();
                circuitBreaker.recordFailure();

                // 降级方案
                log.warn("AI知识点提取失败，使用降级方案: {}", e.getMessage());
                result = basicAnalyzer.extractKnowledgePoints(text, count);
                success = true;
                errorMessage = "AI不可用，已降级: " + e.getMessage();

            } finally {
                long duration = System.currentTimeMillis() - startTime;
                logCall(userId, "TEACHER", "extract", providerKey, success, duration, false, 0,
                        errorMessage, text.substring(0, Math.min(100, text.length())));
            }

            return result;
        });
    }

    /**
     * 直接提取知识点（小文本）
     */
    private List<Map<String, String>> extractKnowledgePointsDirect(
            String apiKey, String providerKey, String text, int count) throws Exception {

        String systemPrompt = "你是一位资深的教学专家。请根据用户提供的文本内容，提取出 " + count + " 个核心知识点。\n" +
                "请务必严格只返回一个合法的 JSON 数组，不要包含 Markdown 代码块标记（如 ```json），也不要包含其他多余文字。\n" +
                "JSON 数组中每个对象的格式如下：\n" +
                "{\n" +
                "  \"name\": \"知识点名称（简练准确，10-30字）\",\n" +
                "  \"description\": \"知识点详细描述或定义（50-200字）\"\n" +
                "}";

        String userPrompt = "文本内容如下：\n" + text;

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        String content = aiHttpClient.sendRequest(
                apiKey, providerKey, messages, 0.5, aiConfig.getTimeout().getExtract()
        );

        String cleanJson = aiHttpClient.extractJson(content);
        return objectMapper.readValue(cleanJson, new TypeReference<List<Map<String, String>>>() {
        });
    }

    /**
     * 分块提取知识点（大文本）
     */
    private List<Map<String, String>> extractKnowledgePointsChunked(
            String apiKey, String providerKey, String text, int count) throws Exception {

        log.info("大文本分块处理: textLength={}, targetCount={}", text.length(), count);

        // 将文本分成多个块（每块约6000字符，有200字符重叠）
        int chunkSize = 6000;
        int overlap = 200;
        List<String> chunks = splitTextIntoChunks(text, chunkSize, overlap);

        log.info("文本已分成 {} 块", chunks.size());

        // 每块提取的数量
        int pointsPerChunk = Math.max(1, count / chunks.size());
        List<Map<String, String>> allPoints = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            int chunkCount = (i == chunks.size() - 1) ?
                (count - allPoints.size()) : pointsPerChunk; // 最后一块取剩余的数量

            if (chunkCount <= 0) break;

            log.info("处理第 {}/{} 块，提取 {} 个知识点", i + 1, chunks.size(), chunkCount);

            try {
                List<Map<String, String>> chunkPoints = extractKnowledgePointsDirect(
                    apiKey, providerKey, chunk, chunkCount
                );
                allPoints.addAll(chunkPoints);

                // 避免请求过快
                if (i < chunks.size() - 1) {
                    Thread.sleep(500);
                }

            } catch (Exception e) {
                log.warn("第 {} 块处理失败: {}", i + 1, e.getMessage());
                // 继续处理下一块
            }
        }

        // 去重和合并相似知识点
        List<Map<String, String>> deduplicated = deduplicateKnowledgePoints(allPoints);

        // 如果结果超过目标数量，取前N个
        if (deduplicated.size() > count) {
            return deduplicated.subList(0, count);
        }

        return deduplicated;
    }

    /**
     * 将文本分块
     */
    private List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // 尝试在段落边界分割
            if (end < text.length()) {
                int lastParagraph = text.lastIndexOf('\n', end);
                if (lastParagraph > start + chunkSize / 2) {
                    end = lastParagraph;
                }
            }

            chunks.add(text.substring(start, end));
            start = end - overlap;
        }

        return chunks;
    }

    /**
     * 去重知识点
     */
    private List<Map<String, String>> deduplicateKnowledgePoints(List<Map<String, String>> points) {
        List<Map<String, String>> result = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (Map<String, String> point : points) {
            String name = point.get("name");
            if (name == null || name.isEmpty()) continue;

            // 简单去重：名称完全相同
            String normalizedName = name.toLowerCase().trim();
            if (!seenNames.contains(normalizedName)) {
                seenNames.add(normalizedName);
                result.add(point);
            }
        }

        return result;
    }

    /**
     * 执行带限流控制的操作
     */
    private <T> T executeWithRateLimit(Long userId, String functionType, String providerKey, SupplierWithException<T> supplier) throws Exception {
        // 检查全局速率限制
        if (!rateLimiter.checkGlobalRateLimit()) {
            throw new RuntimeException("系统繁忙，请稍后再试");
        }

        // 检查用户速率限制
        if (!rateLimiter.checkUserRateLimit(userId)) {
            int remaining = rateLimiter.getRemainingQuota(userId);
            throw new RuntimeException("您的请求过于频繁，请稍后再试。当前剩余配额: " + remaining + " 次/分钟");
        }

        // 检查全局并发
        if (!rateLimiter.tryAcquireGlobalConcurrent()) {
            throw new RuntimeException("系统繁忙，请稍后再试");
        }

        try {
            return supplier.get();
        } finally {
            rateLimiter.releaseGlobalConcurrent();
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

    /**
     * 解析题目列表（容错处理）
     */
    private List<AiGeneratedQuestionDTO> parseQuestionList(String jsonString) throws Exception {
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<AiGeneratedQuestionDTO>>() {
            });
        } catch (Exception e) {
            try {
                JsonNode root = objectMapper.readTree(jsonString);
                if (root.isObject()) {
                    Iterator<String> fieldNames = root.fieldNames();
                    while (fieldNames.hasNext()) {
                        String fieldName = fieldNames.next();
                        JsonNode field = root.get(fieldName);
                        if (field.isArray()) {
                            return objectMapper.convertValue(field, new TypeReference<List<AiGeneratedQuestionDTO>>() {
                            });
                        }
                    }
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

    /**
     * 记录调用日志
     */
    private void logCall(Long userId, String userType, String functionType, String provider,
                         boolean success, long responseTime, boolean cached, int retryCount,
                         String errorMessage, String requestSummary) {
        callLogService.logAsync(userId, userType, functionType, provider, success, responseTime,
                cached, retryCount, errorMessage, requestSummary);
    }

    /**
     * AI 错题分析（流式响应版本）
     * 返回 SseEmitter 对象用于实时推送分析内容
     */
    public SseEmitter analyzeWrongQuestionStream(String apiKey, String providerKey,
                                                  AiAnalysisReq req, Long userId) throws Exception {
        // 创建 SseEmitter，设置超时时间为60秒
        SseEmitter emitter = new SseEmitter(60000L);

        // 在新线程中执行流式请求，避免阻塞主线程
        new Thread(() -> {
            try {
                // 限流检查
                if (!rateLimiter.checkGlobalRateLimit()) {
                    emitter.completeWithError(new RuntimeException("系统繁忙，请稍后再试"));
                    return;
                }

                if (!rateLimiter.checkUserRateLimit(userId)) {
                    int remaining = rateLimiter.getRemainingQuota(userId);
                    emitter.completeWithError(new RuntimeException(
                        "您的请求过于频繁，请稍后再试。当前剩余配额: " + remaining + " 次/分钟"));
                    return;
                }

                if (!rateLimiter.tryAcquireGlobalConcurrent()) {
                    emitter.completeWithError(new RuntimeException("系统繁忙，请稍后再试"));
                    return;
                }

                long startTime = System.currentTimeMillis();
                StringBuilder fullContent = new StringBuilder();

                try {
                    log.info("开始流式AI错题分析: provider={}, user={}", providerKey, userId);

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

                    List<Map<String, String>> messages = List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    );

                    // 发送流式请求
                    aiHttpClient.sendStreamRequest(
                        apiKey,
                        providerKey,
                        messages,
                        0.7,
                        aiConfig.getTimeout().getAnalyze(),
                        // onChunk: 接收到数据块时发送给前端
                        (chunk) -> {
                            try {
                                fullContent.append(chunk);
                                emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(chunk));
                            } catch (Exception e) {
                                log.error("发送SSE数据失败", e);
                            }
                        },
                        // onComplete: 完成时通知前端
                        () -> {
                            try {
                                long duration = System.currentTimeMillis() - startTime;

                                // 记录日志
                                logCall(userId, "STUDENT", "analyze", providerKey, true, duration,
                                    false, 0, null,
                                    req.getQuestionContent().substring(0, Math.min(100, req.getQuestionContent().length())));

                                // 缓存完整结果
                                String cacheKey = generateCacheKey("analyze", req);
                                saveToCache(cacheKey, fullContent.toString());

                                emitter.send(SseEmitter.event().name("done").data(""));
                                emitter.complete();
                                log.info("流式AI错题分析完成: user={}, duration={}ms", userId, duration);
                            } catch (Exception e) {
                                log.error("完成SSE时出错", e);
                                emitter.completeWithError(e);
                            }
                        },
                        // onError: 发生错误时通知前端
                        (error) -> {
                            long duration = System.currentTimeMillis() - startTime;
                            logCall(userId, "STUDENT", "analyze", providerKey, false, duration,
                                false, 0, error.getMessage(),
                                req.getQuestionContent().substring(0, Math.min(100, req.getQuestionContent().length())));

                            emitter.completeWithError(error);
                        }
                    );

                } finally {
                    rateLimiter.releaseGlobalConcurrent();
                }

            } catch (Exception e) {
                log.error("流式AI分析异常", e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * 【流式响应】生成知识点（流式）
     * 返回 SseEmitter 对象用于实时推送生成内容
     */
    public SseEmitter generateKnowledgePointsFromTextStream(String apiKey, String providerKey,
                                                             String text, int count, Long userId) {
        // 【修复】增加超时时间到5分钟（300秒），与配置文件中的generate超时一致
        SseEmitter emitter = new SseEmitter(300000L);

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            StringBuilder fullResponse = new StringBuilder();
            // 【修复】添加标志位防止重复完成
            java.util.concurrent.atomic.AtomicBoolean isCompleted = new java.util.concurrent.atomic.AtomicBoolean(false);

            try {
                // 限流检查
                if (!rateLimiter.checkGlobalRateLimit()) {
                    emitter.completeWithError(new RuntimeException("系统繁忙，请稍后再试"));
                    isCompleted.set(true);
                    return;
                }

                if (!rateLimiter.checkUserRateLimit(userId)) {
                    int remaining = rateLimiter.getRemainingQuota(userId);
                    emitter.completeWithError(new RuntimeException(
                        "您的请求过于频繁，请稍后再试。当前剩余配额: " + remaining + " 次/分钟"));
                    isCompleted.set(true);
                    return;
                }

                if (!rateLimiter.tryAcquireGlobalConcurrent()) {
                    emitter.completeWithError(new RuntimeException("系统繁忙，请稍后再试"));
                    isCompleted.set(true);
                    return;
                }

                try {
                    // 构建提示词（与非流式版本相同）
                    String systemPrompt = "你是一位资深的教学专家。请根据用户提供的文本内容，提取出 " + count + " 个核心知识点。\n" +
                            "请务必严格只返回一个合法的 JSON 数组，不要包含 Markdown 代码块标记（如 ```json），也不要包含其他多余文字。\n" +
                            "JSON 数组中每个对象的格式如下：\n" +
                            "{\n" +
                            "  \"name\": \"知识点名称（简练准确）\",\n" +
                            "  \"description\": \"知识点详细描述或定义（50-200字）\"\n" +
                            "}";

                    String userPrompt = "文本内容如下：\n" + (text.length() > 8000 ? text.substring(0, 8000) + "..." : text);

                    List<Map<String, String>> messages = List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    );

                    log.info("开始流式知识点提取: provider={}, user={}, count={}", providerKey, userId, count);

                    // 发送流式请求
                    aiHttpClient.sendStreamRequest(
                            apiKey,
                            providerKey,
                            messages,
                            0.7,
                            aiConfig.getTimeout().getGenerate(),
                            // onChunk: 接收到数据块时推送给前端
                            (chunk) -> {
                                // 【修复】检查emitter是否已完成，避免重复发送
                                if (isCompleted.get()) {
                                    return;
                                }
                                fullResponse.append(chunk);
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(chunk));
                                } catch (Exception e) {
                                    log.error("发送SSE数据失败: {}", e.getMessage());
                                    isCompleted.set(true);
                                }
                            },
                            // onComplete: 完成时解析JSON并发送
                            () -> {
                                // 【修复】检查emitter是否已完成
                                if (isCompleted.get()) {
                                    return;
                                }
                                try {
                                    long duration = System.currentTimeMillis() - startTime;
                                    String jsonContent = aiHttpClient.extractJson(fullResponse.toString());

                                    // 解析JSON为知识点列表
                                    List<Map<String, String>> points = objectMapper.readValue(
                                            jsonContent,
                                            new TypeReference<List<Map<String, String>>>() {}
                                    );

                                    // 记录日志
                                    logCall(userId, "TEACHER", "extract", providerKey, true, duration,
                                            false, 0, null, text.substring(0, Math.min(100, text.length())));

                                    // 【修复】发送完成事件前再次检查
                                    if (!isCompleted.get()) {
                                        emitter.send(SseEmitter.event()
                                                .name("done")
                                                .data(objectMapper.writeValueAsString(points)));
                                        emitter.complete();
                                        isCompleted.set(true);
                                        log.info("流式知识点提取完成: user={}, count={}, duration={}ms",
                                                userId, points.size(), duration);
                                    }
                                } catch (Exception e) {
                                    log.error("解析知识点JSON失败: {}", e.getMessage());
                                    if (!isCompleted.get()) {
                                        emitter.completeWithError(e);
                                        isCompleted.set(true);
                                    }
                                }
                            },
                            // onError: 发生错误时通知前端
                            (error) -> {
                                // 【修复】检查emitter是否已完成
                                if (isCompleted.get()) {
                                    return;
                                }
                                long duration = System.currentTimeMillis() - startTime;
                                logCall(userId, "TEACHER", "extract", providerKey, false, duration,
                                        false, 0, error.getMessage(),
                                        text.substring(0, Math.min(100, text.length())));
                                emitter.completeWithError(error);
                                isCompleted.set(true);
                            }
                    );

                } finally {
                    rateLimiter.releaseGlobalConcurrent();
                }

            } catch (Exception e) {
                log.error("流式知识点提取异常: {}", e.getMessage());
                // 【修复】检查emitter是否已完成
                if (!isCompleted.get()) {
                    emitter.completeWithError(e);
                    isCompleted.set(true);
                }
            }
        }).start();

        return emitter;
    }

    /**
     * 【流式响应】智能出题（流式）
     * 返回 SseEmitter 对象用于实时推送生成内容
     */
    public SseEmitter generateQuestionsFromTextStream(String apiKey, String providerKey,
                                                      String text, int count, int type, Long userId) {
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时，智能出题需要更长时间

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            StringBuilder fullResponse = new StringBuilder();

            try {
                // 限流检查
                if (!rateLimiter.checkGlobalRateLimit()) {
                    emitter.completeWithError(new RuntimeException("系统繁忙，请稍后再试"));
                    return;
                }

                if (!rateLimiter.checkUserRateLimit(userId)) {
                    int remaining = rateLimiter.getRemainingQuota(userId);
                    emitter.completeWithError(new RuntimeException(
                        "您的请求过于频繁，请稍后再试。当前剩余配额: " + remaining + " 次/分钟"));
                    return;
                }

                if (!rateLimiter.tryAcquireGlobalConcurrent()) {
                    emitter.completeWithError(new RuntimeException("系统繁忙，请稍后再试"));
                    return;
                }

                try {
                    // 构建题型描述
                    String typeDesc = switch (type) {
                        case 1 -> "单选题";
                        case 2 -> "多选题";
                        case 3 -> "填空题";
                        case 4 -> "判断题";
                        case 5 -> "主观题（简答题、论述题、计算题等）";
                        default -> "混合题型（单选、多选、填空、判断、主观题混合）";
                    };

                    // 构建提示词
                    String systemPrompt = "你是一位资深的出题专家。请根据用户提供的文本内容，生成 " + count + " 道高质量的" + typeDesc + "。\n" +
                            "请严格按照JSON格式返回，不要包含Markdown代码块标记。\n" +
                            "JSON数组中每个对象的格式：\n" +
                            "{\n" +
                            "  \"type\": 题型编号(1:单选,2:多选,3:填空,4:判断,5:主观),\n" +
                            "  \"content\": \"题目内容\",\n" +
                            "  \"options\": [\"A选项\",\"B选项\",\"C选项\",\"D选项\"], // 仅选择题需要\n" +
                            "  \"correctAnswer\": \"正确答案\",\n" +
                            "  \"analysis\": \"解析说明\",\n" +
                            "  \"difficulty\": 难度等级(1-5)\n" +
                            "}";

                    String userPrompt = "请根据以下文本内容出题：\n" + (text.length() > 5000 ? text.substring(0, 5000) + "..." : text);

                    List<Map<String, String>> messages = List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    );

                    log.info("开始流式智能出题: provider={}, user={}, count={}, type={}",
                            providerKey, userId, count, type);

                    // 发送流式请求
                    aiHttpClient.sendStreamRequest(
                            apiKey,
                            providerKey,
                            messages,
                            0.8,
                            aiConfig.getTimeout().getGenerate(),
                            // onChunk: 接收到数据块时推送给前端
                            (chunk) -> {
                                fullResponse.append(chunk);
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(chunk));
                                } catch (Exception e) {
                                    log.error("发送SSE数据失败", e);
                                }
                            },
                            // onComplete: 完成时解析JSON并发送
                            () -> {
                                try {
                                    long duration = System.currentTimeMillis() - startTime;
                                    String jsonContent = aiHttpClient.extractJson(fullResponse.toString());

                                    // 解析JSON为题目列表
                                    List<AiGeneratedQuestionDTO> questions = parseQuestionList(jsonContent);

                                    // 记录日志
                                    logCall(userId, "TEACHER", "generate", providerKey, true, duration,
                                            false, 0, null, text.substring(0, Math.min(100, text.length())));

                                    // 发送完成事件，包含解析后的JSON
                                    emitter.send(SseEmitter.event()
                                            .name("done")
                                            .data(objectMapper.writeValueAsString(questions)));
                                    emitter.complete();
                                    log.info("流式智能出题完成: user={}, count={}, duration={}ms",
                                            userId, questions.size(), duration);
                                } catch (Exception e) {
                                    log.error("解析题目JSON失败", e);
                                    emitter.completeWithError(e);
                                }
                            },
                            // onError: 发生错误时通知前端
                            (error) -> {
                                long duration = System.currentTimeMillis() - startTime;
                                logCall(userId, "TEACHER", "generate", providerKey, false, duration,
                                        false, 0, error.getMessage(),
                                        text.substring(0, Math.min(100, text.length())));
                                emitter.completeWithError(error);
                            }
                    );

                } finally {
                    rateLimiter.releaseGlobalConcurrent();
                }

            } catch (Exception e) {
                log.error("流式智能出题异常", e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * AI 智能生成试卷（流式响应）
     * @param apiKey AI API Key
     * @param providerKey AI 提供商
     * @param paperTitle 试卷标题
     * @param subjectName 科目名称
     * @param knowledgePoints 知识点列表（JSON字符串）
     * @param difficultyDistribution 难度分布（例如："简单:30%,中等:50%,困难:20%"）
     * @param questionTypes 题型配置（JSON字符串，例如：[{"type":"单选","count":10},{"type":"多选","count":5}]）
     * @param totalScore 试卷总分
     * @param userId 用户ID
     * @return SseEmitter 流式响应对象
     */
    public SseEmitter generatePaperStream(String apiKey, String providerKey,
                                          String paperTitle, String subjectName,
                                          String knowledgePoints, String difficultyDistribution,
                                          String questionTypes, int totalScore, Long userId) {
        SseEmitter emitter = new SseEmitter(600000L); // 10分钟超时

        // 【修复】保存当前线程的 SecurityContext，以便在新线程中使用
        org.springframework.security.core.context.SecurityContext securityContext =
            org.springframework.security.core.context.SecurityContextHolder.getContext();

        new Thread(() -> {
            try {
                // 【修复】在新线程中设置 SecurityContext
                org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

                // 检查限流
                if (!rateLimiter.checkGlobalRateLimit()) {
                    emitter.completeWithError(new RuntimeException("系统繁忙，请稍后再试"));
                    return;
                }

                if (!rateLimiter.checkUserRateLimit(userId)) {
                    emitter.completeWithError(new RuntimeException("请求过于频繁，请稍后再试"));
                    return;
                }

                if (!rateLimiter.tryAcquireGlobalConcurrent()) {
                    emitter.completeWithError(new RuntimeException("系统繁忙，请稍后再试"));
                    return;
                }

                try {
                    long startTime = System.currentTimeMillis();
                    StringBuilder fullResponse = new StringBuilder();
                    final java.util.concurrent.atomic.AtomicInteger chunkCount = new java.util.concurrent.atomic.AtomicInteger(0);  // 【修复】使用AtomicInteger
                    final String[] lastChunk = {""};  // 【修复】使用数组

                    // 构建系统提示词
                    String systemPrompt = "你是一位专业的出题专家。请根据用户提供的要求，生成一套完整的试卷。\n" +
                            "要求：\n" +
                            "1. 题目必须覆盖指定的知识点\n" +
                            "2. 题目难度需符合指定的难度分布\n" +
                            "3. 题型和数量需符合要求\n" +
                            "4. 每道题目必须包含：题干、选项（如果是选择题）、正确答案、解析\n" +
                            "5. 题目分值分配要合理，总分为" + totalScore + "分\n" +
                            "6. **重要：解析请简洁明了，每题解析不超过50字**\n" +
                            "7. 请以JSON格式输出，格式如下：\n" +
                            "```json\n" +
                            "{\n" +
                            "  \"paperName\": \"试卷标题\",\n" +
                            "  \"description\": \"试卷说明（不超过100字）\",\n" +
                            "  \"totalScore\": " + totalScore + ",\n" +
                            "  \"questions\": [\n" +
                            "    {\n" +
                            "      \"type\": \"单选\",\n" +
                            "      \"content\": \"题干内容\",\n" +
                            "      \"options\": [{\"key\":\"A\",\"value\":\"选项A\"},{\"key\":\"B\",\"value\":\"选项B\"}],\n" +
                            "      \"answer\": \"A\",\n" +
                            "      \"analysis\": \"简洁的解析（不超过50字）\",\n" +
                            "      \"score\": 5,\n" +
                            "      \"difficulty\": \"中等\",\n" +
                            "      \"knowledgePoint\": \"对应的知识点\"\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}\n" +
                            "```";

                    // 构建用户提示词
                    String userPrompt = String.format(
                            "请生成一套试卷：\n" +
                                    "【试卷标题】：%s\n" +
                                    "【科目】：%s\n" +
                                    "【知识点要求】：%s\n" +
                                    "【难度分布】：%s\n" +
                                    "【题型配置】：%s\n" +
                                    "【总分】：%d分\n\n" +
                                    "请严格按照上述要求生成试卷，确保题目质量高、覆盖全面。",
                            paperTitle, subjectName, knowledgePoints,
                            difficultyDistribution, questionTypes, totalScore
                    );

                    List<Map<String, String>> messages = new ArrayList<>();
                    messages.add(Map.of("role", "system", "content", systemPrompt));
                    messages.add(Map.of("role", "user", "content", userPrompt));

                    int timeout = aiConfig.getTimeout().getGenerate();

                    // 流式请求
                    aiHttpClient.sendStreamRequest(
                            apiKey, providerKey, messages, 0.7, timeout,
                            // onChunk: 接收每个数据块
                            (chunk) -> {
                                try {
                                    fullResponse.append(chunk);
                                    chunkCount.incrementAndGet();
                                    lastChunk[0] = chunk;
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(chunk));
                                } catch (Exception e) {
                                    log.error("发送SSE数据失败", e);
                                }
                            },
                            // onComplete: 完成时解析JSON并发送
                            () -> {
                                try {
                                    long duration = System.currentTimeMillis() - startTime;

                                    // 【新增】记录接收到的chunk信息
                                    log.info("📊 接收chunk统计: 总数={}, 最后chunk长度={}", chunkCount.get(), lastChunk[0].length());
                                    if (lastChunk[0].length() > 0) {
                                        log.info("📝 最后chunk内容: {}", lastChunk[0].substring(0, Math.min(200, lastChunk[0].length())));
                                    }

                                    // 【新增】记录原始响应长度
                                    String fullResponseStr = fullResponse.toString();
                                    log.info("📊 AI原始响应长度: {} 字节 ({} KB)", fullResponseStr.length(), fullResponseStr.length() / 1024);

                                    String jsonContent = aiHttpClient.extractJson(fullResponseStr);

                                    // 【新增】检查JSON大小，防止超大数据
                                    int jsonSizeKB = jsonContent.length() / 1024;
                                    log.info("生成的试卷JSON大小: {} KB (原始响应: {} KB)", jsonSizeKB, fullResponseStr.length() / 1024);

                                    // 【新增】验证JSON完整性
                                    boolean isValidJson = false;
                                    try {
                                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonContent);
                                        log.info("✅ JSON格式验证通过");
                                        isValidJson = true;
                                    } catch (Exception e) {
                                        log.error("❌ JSON格式验证失败", e);
                                        log.error("JSON内容前500字符: {}", jsonContent.substring(0, Math.min(500, jsonContent.length())));
                                        log.error("JSON内容后500字符: {}", jsonContent.substring(Math.max(0, jsonContent.length() - 500)));

                                        // 【关键修改】即使JSON不完整，也尝试发送，让前端处理
                                        log.warn("⚠️ JSON不完整，但仍然发送给前端，由前端判断是否可用");
                                    }

                                    if (jsonSizeKB > 1024) { // 超过1MB
                                        log.warn("⚠️ 试卷JSON过大: {} KB，可能导致传输问题", jsonSizeKB);
                                    }

                                    // 记录日志
                                    logCall(userId, "TEACHER", "generate_paper", providerKey, true, duration,
                                            false, 0, null, paperTitle);

                                    // 发送完成事件，包含解析后的JSON
                                    log.info("📤 准备发送done事件，JSON长度: {} 字节", jsonContent.length());
                                    emitter.send(SseEmitter.event()
                                            .name("done")
                                            .data(jsonContent));
                                    log.info("📤 done事件已发送");
                                    emitter.complete();
                                    log.info("✅ AI生成试卷完成: user={}, paper={}, duration={}ms, size={}KB",
                                            userId, paperTitle, duration, jsonSizeKB);
                                } catch (Exception e) {
                                    log.error("❌ 解析试卷JSON失败", e);
                                    emitter.completeWithError(e);
                                }
                            },
                            // onError: 发生错误时通知前端
                            (error) -> {
                                long duration = System.currentTimeMillis() - startTime;
                                logCall(userId, "TEACHER", "generate_paper", providerKey, false, duration,
                                        false, 0, error.getMessage(), paperTitle);
                                emitter.completeWithError(error);
                            }
                    );

                } finally {
                    rateLimiter.releaseGlobalConcurrent();
                    // 【修复】清理 SecurityContext，防止内存泄漏
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                }

            } catch (Exception e) {
                log.error("AI生成试卷异常", e);
                emitter.completeWithError(e);
                // 【修复】异常情况下也要清理 SecurityContext
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
            }
        }).start();

        return emitter;
    }
}
