package com.ice.exebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.config.AiConfig;
import com.ice.exebackend.dto.AiAnalysisReq;
import com.ice.exebackend.dto.AiGeneratedQuestionDTO;
import com.ice.exebackend.dto.AiGradingResult;
import com.ice.exebackend.utils.AiHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AI服务V3单元测试
 * 测试AI批改、出题、知识点提取等核心功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI服务V3测试")
class AiServiceV3Test {

    @Mock
    private AiHttpClient aiHttpClient;

    @Mock
    private AiConfig aiConfig;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AiRateLimiter rateLimiter;

    @Mock
    private AiCallLogService callLogService;

    @Mock
    private AiCircuitBreaker circuitBreaker;

    @Mock
    private BasicAnalyzer basicAnalyzer;

    @InjectMocks
    private AiServiceV3 aiServiceV3;

    private AiConfig.CacheConfig cacheConfig;
    private AiConfig.TimeoutConfig timeoutConfig;

    @BeforeEach
    void setUp() {
        // 配置缓存
        cacheConfig = new AiConfig.CacheConfig();
        cacheConfig.setEnabled(true);
        cacheConfig.setKeyPrefix("ai:cache:");
        cacheConfig.setTtl(1800);

        // 配置超时
        timeoutConfig = new AiConfig.TimeoutConfig();
        timeoutConfig.setAnalyze(30000);
        timeoutConfig.setGrading(30000);
        timeoutConfig.setGenerate(60000);
        timeoutConfig.setExtract(45000);

        when(aiConfig.getCache()).thenReturn(cacheConfig);
        when(aiConfig.getTimeout()).thenReturn(timeoutConfig);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 默认限流通过
        when(rateLimiter.checkGlobalRateLimit()).thenReturn(true);
        when(rateLimiter.checkUserRateLimit(anyLong())).thenReturn(true);
        when(rateLimiter.tryAcquireGlobalConcurrent()).thenReturn(true);

        // 默认断路器关闭
        when(circuitBreaker.allowRequest()).thenReturn(true);
    }

    // ==================== 主观题批改测试 ====================

    @Test
    @DisplayName("应该成功批改主观题并返回结果")
    void shouldGradeSubjectiveQuestionSuccessfully() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        String questionContent = "请简述Java中的多态性";
        String referenceAnswer = "多态性是指同一个行为具有多个不同表现形式。Java中通过继承和接口实现多态。";
        String studentAnswer = "多态就是一个对象可以有多种形态，通过方法重写实现。";
        int maxScore = 10;
        Long userId = 1L;

        // Mock AI响应
        String aiResponse = "{\"score\": 8, \"feedback\": \"理解基本正确，但缺少接口相关内容\"}";
        when(aiHttpClient.sendRequest(eq(apiKey), eq(provider), anyList(), eq(0.3), anyInt()))
                .thenReturn(aiResponse);
        when(aiHttpClient.extractJson(aiResponse)).thenReturn(aiResponse);

        // Mock ObjectMapper
        AiGradingResult expectedResult = new AiGradingResult();
        expectedResult.setScore(8);
        expectedResult.setFeedback("理解基本正确，但缺少接口相关内容");
        when(objectMapper.readValue(aiResponse, AiGradingResult.class)).thenReturn(expectedResult);

        // When
        AiGradingResult result = aiServiceV3.gradeSubjectiveQuestion(
                apiKey, provider, questionContent, referenceAnswer, studentAnswer, maxScore, userId
        );

        // Then
        assertNotNull(result);
        assertEquals(8, result.getScore());
        assertEquals("理解基本正确，但缺少接口相关内容", result.getFeedback());

        // 验证调用
        verify(aiHttpClient).sendRequest(eq(apiKey), eq(provider), anyList(), eq(0.3), eq(30000));
        verify(callLogService).logAsync(eq(userId), eq("STUDENT"), eq("grading"), eq(provider),
                eq(true), anyLong(), eq(false), eq(0), isNull(), anyString());
    }

    @Test
    @DisplayName("应该处理批改失败情况")
    void shouldHandleGradingFailure() {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        Long userId = 1L;

        // Mock AI请求失败
        when(aiHttpClient.sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt()))
                .thenThrow(new RuntimeException("AI服务不可用"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            aiServiceV3.gradeSubjectiveQuestion(
                    apiKey, provider, "题目", "答案", "学生答案", 10, userId
            );
        });

        // 验证记录失败日志
        verify(callLogService).logAsync(eq(userId), eq("STUDENT"), eq("grading"), eq(provider),
                eq(false), anyLong(), eq(false), eq(0), anyString(), anyString());
    }

    @Test
    @DisplayName("应该在限流时拒绝批改请求")
    void shouldRejectGradingWhenRateLimited() {
        // Given
        when(rateLimiter.checkUserRateLimit(anyLong())).thenReturn(false);
        when(rateLimiter.getRemainingQuota(anyLong())).thenReturn(5);

        Long userId = 1L;

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            aiServiceV3.gradeSubjectiveQuestion(
                    "key", "provider", "question", "answer", "student", 10, userId
            );
        });

        assertTrue(exception.getMessage().contains("请求过于频繁"));
        assertTrue(exception.getMessage().contains("5"));
    }

    @Test
    @DisplayName("应该正确提取JSON中的分数和反馈")
    void shouldExtractScoreAndFeedbackFromJson() throws Exception {
        // Given
        String apiKey = "test-key";
        String provider = "deepseek";
        Long userId = 1L;

        // Mock AI返回JSON
        String aiResponse = "{\"score\": 10, \"feedback\": \"完全正确，理解透彻\"}";
        when(aiHttpClient.sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt()))
                .thenReturn(aiResponse);
        when(aiHttpClient.extractJson(aiResponse)).thenReturn(aiResponse);

        AiGradingResult mockResult = new AiGradingResult();
        mockResult.setScore(10);
        mockResult.setFeedback("完全正确，理解透彻");
        when(objectMapper.readValue(aiResponse, AiGradingResult.class)).thenReturn(mockResult);

        // When
        AiGradingResult result = aiServiceV3.gradeSubjectiveQuestion(
                apiKey, provider, "题目", "参考答案", "学生答案", 10, userId
        );

        // Then
        assertEquals(10, result.getScore());
        assertEquals("完全正确，理解透彻", result.getFeedback());
    }

    // ==================== 错题分析测试 ====================

    @Test
    @DisplayName("应该成功分析错题")
    void shouldAnalyzeWrongQuestionSuccessfully() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        Long userId = 1L;

        AiAnalysisReq req = new AiAnalysisReq();
        req.setQuestionContent("1+1等于多少？");
        req.setCorrectAnswer("2");
        req.setStudentAnswer("3");
        req.setAnalysis("基础加法运算");

        String aiResponse = "## 知识点回顾\n这是基础加法运算...\n## 错误原因\n计算失误\n## 解题思路\n1+1=2";

        // Mock缓存未命中
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock断路器允许
        when(circuitBreaker.allowRequest()).thenReturn(true);

        // Mock AI响应
        when(aiHttpClient.sendRequest(eq(apiKey), eq(provider), anyList(), eq(0.7), anyInt()))
                .thenReturn(aiResponse);

        // When
        String result = aiServiceV3.analyzeWrongQuestion(apiKey, provider, req, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("知识点回顾"));

        // 验证缓存保存
        verify(valueOperations).set(anyString(), eq(aiResponse), eq(1800L), any());
        verify(circuitBreaker).recordSuccess();
    }

    @Test
    @DisplayName("应该从缓存加载错题分析")
    void shouldLoadAnalysisFromCache() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        Long userId = 1L;

        AiAnalysisReq req = new AiAnalysisReq();
        req.setQuestionContent("1+1=?");
        req.setCorrectAnswer("2");
        req.setStudentAnswer("3");

        String cachedResult = "缓存的分析结果";

        // Mock缓存命中
        when(valueOperations.get(anyString())).thenReturn(cachedResult);

        // When
        String result = aiServiceV3.analyzeWrongQuestion(apiKey, provider, req, userId);

        // Then
        assertEquals(cachedResult, result);

        // 验证不调用AI
        verify(aiHttpClient, never()).sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt());
        verify(callLogService).logAsync(eq(userId), eq("STUDENT"), eq("analyze"), eq(provider),
                eq(true), eq(0L), eq(true), eq(0), isNull(), eq("cached"));
    }

    @Test
    @DisplayName("应该在断路器打开时使用降级方案")
    void shouldUseFallbackWhenCircuitBreakerOpen() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        Long userId = 1L;

        AiAnalysisReq req = new AiAnalysisReq();
        req.setQuestionContent("测试题目");
        req.setCorrectAnswer("正确答案");
        req.setStudentAnswer("错误答案");

        // Mock断路器打开
        when(circuitBreaker.allowRequest()).thenReturn(false);

        // Mock降级分析
        String fallbackResult = "降级分析：基础错误分析";
        when(basicAnalyzer.analyzeWrongQuestion(req)).thenReturn(fallbackResult);

        // Mock缓存未命中
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        String result = aiServiceV3.analyzeWrongQuestion(apiKey, provider, req, userId);

        // Then
        assertEquals(fallbackResult, result);

        // 验证使用降级方案
        verify(basicAnalyzer).analyzeWrongQuestion(req);
        verify(aiHttpClient, never()).sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt());
    }

    @Test
    @DisplayName("应该在AI失败后使用降级方案")
    void shouldUseFallbackAfterAiFailure() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        Long userId = 1L;

        AiAnalysisReq req = new AiAnalysisReq();
        req.setQuestionContent("题目");
        req.setCorrectAnswer("答案");
        req.setStudentAnswer("学生答案");

        // Mock缓存未命中
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock断路器允许
        when(circuitBreaker.allowRequest()).thenReturn(true);

        // Mock AI失败
        when(aiHttpClient.sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt()))
                .thenThrow(new RuntimeException("AI服务超时"));

        // Mock降级方案
        String fallbackResult = "降级分析结果";
        when(basicAnalyzer.analyzeWrongQuestion(req)).thenReturn(fallbackResult);

        // When
        String result = aiServiceV3.analyzeWrongQuestion(apiKey, provider, req, userId);

        // Then
        assertEquals(fallbackResult, result);
        verify(circuitBreaker).recordFailure();
        verify(basicAnalyzer).analyzeWrongQuestion(req);
    }

    // ==================== 智能出题测试 ====================

    @Test
    @DisplayName("应该成功生成题目列表")
    void shouldGenerateQuestionsSuccessfully() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        String text = "关于Java多线程的知识点：线程是程序执行的最小单位...";
        int count = 3;
        int type = 1; // 单选题
        Long userId = 1L;

        String aiResponse = "[{\"content\":\"什么是线程？\",\"questionType\":1,\"options\":[],\"answer\":\"A\",\"description\":\"解析\"}]";

        // Mock缓存未命中
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock AI响应
        when(aiHttpClient.sendRequest(eq(apiKey), eq(provider), anyList(), eq(0.5), anyInt()))
                .thenReturn(aiResponse);
        when(aiHttpClient.extractJson(aiResponse)).thenReturn(aiResponse);

        // Mock解析
        AiGeneratedQuestionDTO question = new AiGeneratedQuestionDTO();
        question.setContent("什么是线程？");
        question.setQuestionType(1);
        when(objectMapper.readValue(eq(aiResponse), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(question));

        // When
        List<AiGeneratedQuestionDTO> result = aiServiceV3.generateQuestionsFromText(
                apiKey, provider, text, count, type, userId
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("什么是线程？", result.get(0).getContent());

        // 验证缓存
        verify(objectMapper).writeValueAsString(anyList());
        verify(valueOperations).set(anyString(), anyString(), eq(1800L), any());
    }

    @Test
    @DisplayName("应该从缓存加载生成的题目")
    void shouldLoadGeneratedQuestionsFromCache() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        String text = "测试文本";
        int count = 2;
        int type = 1;
        Long userId = 1L;

        String cachedJson = "[{\"content\":\"题目1\"}]";

        // Mock缓存命中
        when(valueOperations.get(anyString())).thenReturn(cachedJson);

        // Mock解析
        AiGeneratedQuestionDTO question = new AiGeneratedQuestionDTO();
        question.setContent("题目1");
        when(objectMapper.readValue(eq(cachedJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(question));

        // When
        List<AiGeneratedQuestionDTO> result = aiServiceV3.generateQuestionsFromText(
                apiKey, provider, text, count, type, userId
        );

        // Then
        assertEquals(1, result.size());
        assertEquals("题目1", result.get(0).getContent());

        // 验证不调用AI
        verify(aiHttpClient, never()).sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt());
    }

    // ==================== 知识点提取测试 ====================

    @Test
    @DisplayName("应该成功提取小文本的知识点")
    void shouldExtractKnowledgePointsFromSmallText() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        String text = "Java是一门面向对象的编程语言，具有跨平台特性。";
        int count = 2;
        Long userId = 1L;

        String aiResponse = "[{\"name\":\"面向对象\",\"description\":\"Java的核心特性\"},{\"name\":\"跨平台\",\"description\":\"一次编写，到处运行\"}]";

        // Mock缓存未命中
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock断路器允许
        when(circuitBreaker.allowRequest()).thenReturn(true);

        // Mock AI响应
        when(aiHttpClient.sendRequest(eq(apiKey), eq(provider), anyList(), eq(0.5), anyInt()))
                .thenReturn(aiResponse);
        when(aiHttpClient.extractJson(aiResponse)).thenReturn(aiResponse);

        // Mock解析
        List<Map<String, String>> mockPoints = List.of(
                Map.of("name", "面向对象", "description", "Java的核心特性"),
                Map.of("name", "跨平台", "description", "一次编写，到处运行")
        );
        when(objectMapper.readValue(eq(aiResponse), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(mockPoints);

        // When
        List<Map<String, String>> result = aiServiceV3.generateKnowledgePointsFromText(
                apiKey, provider, text, count, userId
        );

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("面向对象", result.get(0).get("name"));
        assertEquals("跨平台", result.get(1).get("name"));

        verify(circuitBreaker).recordSuccess();
    }

    @Test
    @DisplayName("应该在提取知识点失败时使用降级方案")
    void shouldUseFallbackWhenExtractKnowledgePointsFails() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        String text = "测试文本内容";
        int count = 3;
        Long userId = 1L;

        // Mock缓存未命中
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock断路器允许
        when(circuitBreaker.allowRequest()).thenReturn(true);

        // Mock AI失败
        when(aiHttpClient.sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt()))
                .thenThrow(new RuntimeException("网络错误"));

        // Mock降级方案
        List<Map<String, String>> fallbackPoints = List.of(
                Map.of("name", "降级知识点1", "description", "基础分析")
        );
        when(basicAnalyzer.extractKnowledgePoints(text, count)).thenReturn(fallbackPoints);

        // When
        List<Map<String, String>> result = aiServiceV3.generateKnowledgePointsFromText(
                apiKey, provider, text, count, userId
        );

        // Then
        assertEquals(fallbackPoints, result);
        verify(circuitBreaker).recordFailure();
        verify(basicAnalyzer).extractKnowledgePoints(text, count);
    }

    @Test
    @DisplayName("应该处理大文本分块提取知识点")
    void shouldHandleLargeTextChunking() throws Exception {
        // Given
        String apiKey = "test-api-key";
        String provider = "deepseek";
        // 生成超过8000字符的大文本
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeText.append("这是一段关于Java编程的文本内容。");
        }
        String text = largeText.toString(); // 约20000字符
        int count = 5;
        Long userId = 1L;

        // Mock缓存未命中
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock断路器允许
        when(circuitBreaker.allowRequest()).thenReturn(true);

        // Mock分块AI响应
        String chunkResponse = "[{\"name\":\"Java编程\",\"description\":\"基础知识\"}]";
        when(aiHttpClient.sendRequest(eq(apiKey), eq(provider), anyList(), eq(0.5), anyInt()))
                .thenReturn(chunkResponse);
        when(aiHttpClient.extractJson(chunkResponse)).thenReturn(chunkResponse);

        List<Map<String, String>> mockPoints = List.of(
                Map.of("name", "Java编程", "description", "基础知识")
        );
        when(objectMapper.readValue(eq(chunkResponse), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(mockPoints);

        // When
        List<Map<String, String>> result = aiServiceV3.generateKnowledgePointsFromText(
                apiKey, provider, text, count, userId
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // 验证调用了多次（分块处理）
        verify(aiHttpClient, atLeastOnce()).sendRequest(eq(apiKey), eq(provider), anyList(), eq(0.5), anyInt());
    }

    // ==================== 限流测试 ====================

    @Test
    @DisplayName("应该在全局限流时拒绝请求")
    void shouldRejectWhenGlobalRateLimited() {
        // Given
        when(rateLimiter.checkGlobalRateLimit()).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            aiServiceV3.gradeSubjectiveQuestion(
                    "key", "provider", "question", "answer", "student", 10, 1L
            );
        });

        assertTrue(exception.getMessage().contains("系统繁忙"));
    }

    @Test
    @DisplayName("应该在全局并发超限时拒绝请求")
    void shouldRejectWhenGlobalConcurrentExceeded() {
        // Given
        when(rateLimiter.tryAcquireGlobalConcurrent()).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            aiServiceV3.gradeSubjectiveQuestion(
                    "key", "provider", "question", "answer", "student", 10, 1L
            );
        });

        assertTrue(exception.getMessage().contains("系统繁忙"));
    }

    @Test
    @DisplayName("应该在请求完成后释放并发信号量")
    void shouldReleaseGlobalConcurrentAfterRequest() throws Exception {
        // Given
        String apiKey = "test-key";
        String provider = "deepseek";
        Long userId = 1L;

        String aiResponse = "{\"score\": 10, \"feedback\": \"优秀\"}";
        when(aiHttpClient.sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt()))
                .thenReturn(aiResponse);
        when(aiHttpClient.extractJson(aiResponse)).thenReturn(aiResponse);

        AiGradingResult mockResult = new AiGradingResult();
        mockResult.setScore(10);
        when(objectMapper.readValue(aiResponse, AiGradingResult.class)).thenReturn(mockResult);

        // When
        aiServiceV3.gradeSubjectiveQuestion(
                apiKey, provider, "题目", "答案", "学生答案", 10, userId
        );

        // Then
        verify(rateLimiter).tryAcquireGlobalConcurrent();
        verify(rateLimiter).releaseGlobalConcurrent();
    }

    @Test
    @DisplayName("应该在发生异常时也释放并发信号量")
    void shouldReleaseGlobalConcurrentEvenOnException() {
        // Given
        when(aiHttpClient.sendRequest(anyString(), anyString(), anyList(), anyDouble(), anyInt()))
                .thenThrow(new RuntimeException("AI错误"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            aiServiceV3.gradeSubjectiveQuestion(
                    "key", "provider", "question", "answer", "student", 10, 1L
            );
        });

        // 验证即使异常也释放
        verify(rateLimiter).releaseGlobalConcurrent();
    }
}
