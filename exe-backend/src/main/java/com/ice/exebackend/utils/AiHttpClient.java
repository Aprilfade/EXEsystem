package com.ice.exebackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI HTTP 客户端工具类
 * 统一管理 AI 请求的发送和重试逻辑
 */
@Component
public class AiHttpClient {

    private static final Logger log = LoggerFactory.getLogger(AiHttpClient.class);

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient;

    public AiHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /**
     * 发送 AI 请求（带重试）
     *
     * @param apiKey       API Key
     * @param providerKey  提供商标识
     * @param messages     消息列表
     * @param temperature  温度参数
     * @param timeoutSec   超时时间（秒）
     * @return AI 响应内容
     */
    public String sendRequest(String apiKey, String providerKey, List<Map<String, String>> messages,
                              Double temperature, int timeoutSec) throws Exception {

        // 获取提供商配置
        AiConfig.ProviderConfig providerConfig = aiConfig.getProviderConfig(providerKey);
        if (providerConfig == null || !providerConfig.isEnabled()) {
            throw new RuntimeException("AI 提供商未配置或已禁用: " + providerKey);
        }

        // 构建请求体
        Map<String, Object> requestBody = Map.of(
                "model", providerConfig.getModel(),
                "messages", messages,
                "temperature", temperature != null ? temperature : providerConfig.getTemperature(),
                "stream", false
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 执行请求（带重试）
        return executeWithRetry(apiKey, providerConfig.getUrl(), jsonBody, timeoutSec, providerKey);
    }

    /**
     * 执行请求并处理重试
     */
    private String executeWithRetry(String apiKey, String url, String jsonBody, int timeoutSec,
                                     String providerKey) throws Exception {
        int maxAttempts = aiConfig.getRetry().isEnabled() ? aiConfig.getRetry().getMaxAttempts() : 1;
        long backoff = aiConfig.getRetry().getBackoff();
        double multiplier = aiConfig.getRetry().getMultiplier();

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("AI请求 [{}] 第 {}/{} 次尝试", providerKey, attempt, maxAttempts);

                // 构建 HTTP 请求
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .timeout(Duration.ofSeconds(timeoutSec))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                // 发送请求
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // 检查响应状态码
                if (response.statusCode() == 200) {
                    log.info("AI请求成功 [{}]", providerKey);
                    return extractContent(response.body());
                } else if (response.statusCode() >= 500 && attempt < maxAttempts) {
                    // 服务器错误，可以重试
                    log.warn("AI请求失败 [{}] 状态码: {}, 将重试", providerKey, response.statusCode());
                    lastException = new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                } else {
                    // 客户端错误或最后一次尝试，不再重试
                    throw new RuntimeException("AI 请求失败 (" + providerKey + "): " + response.statusCode() + " - " + response.body());
                }

            } catch (Exception e) {
                lastException = e;
                log.error("AI请求异常 [{}] 第 {}/{} 次尝试: {}", providerKey, attempt, maxAttempts, e.getMessage());

                // 最后一次尝试，直接抛出异常
                if (attempt >= maxAttempts) {
                    throw e;
                }
            }

            // 等待后重试
            if (attempt < maxAttempts) {
                long waitTime = (long) (backoff * Math.pow(multiplier, attempt - 1));
                log.info("等待 {} ms 后重试", waitTime);
                Thread.sleep(waitTime);
            }
        }

        throw lastException != null ? lastException : new RuntimeException("AI 请求失败");
    }

    /**
     * 从响应中提取 AI 内容
     */
    private String extractContent(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI 返回内容为空");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("AI 返回的content为空");
        }

        return content;
    }

    /**
     * 提取JSON内容（增强版）
     * 支持从Markdown代码块中提取JSON
     */
    public String extractJson(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "{}";
        }

        content = content.trim();

        // 移除 Markdown 代码块标记
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }

        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }

        content = content.trim();

        // 优先查找数组
        int firstBracket = content.indexOf("[");
        int lastBracket = content.lastIndexOf("]");

        if (firstBracket != -1 && lastBracket != -1 && firstBracket < lastBracket) {
            return content.substring(firstBracket, lastBracket + 1);
        }

        // 查找对象
        int firstBrace = content.indexOf("{");
        int lastBrace = content.lastIndexOf("}");

        if (firstBrace != -1 && lastBrace != -1 && firstBrace < lastBrace) {
            return content.substring(firstBrace, lastBrace + 1);
        }

        return content;
    }
}
