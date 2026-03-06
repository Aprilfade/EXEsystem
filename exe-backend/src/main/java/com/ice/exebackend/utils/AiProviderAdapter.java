package com.ice.exebackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * AI Provider适配器
 * 处理不同AI提供商的API格式差异
 * 支持: DeepSeek (OpenAI格式), Claude (Anthropic格式), Gemini (Google格式)
 */
@Component
public class AiProviderAdapter {

    private static final Logger log = LoggerFactory.getLogger(AiProviderAdapter.class);

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient;

    public AiProviderAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /**
     * 统一的AI请求接口
     */
    public String sendRequest(String apiKey, String providerKey, List<Map<String, String>> messages,
                              Double temperature, int timeoutSec) throws Exception {

        AiConfig.ProviderConfig providerConfig = aiConfig.getProviderConfig(providerKey);
        if (providerConfig == null || !providerConfig.isEnabled()) {
            throw new RuntimeException("AI 提供商未配置或已禁用: " + providerKey);
        }

        // 根据提供商类型路由到不同的实现
        switch (providerKey.toLowerCase()) {
            case "deepseek":
                return sendOpenAIFormatRequest(apiKey, providerConfig, messages, temperature, timeoutSec);
            case "claude":
                return sendClaudeRequest(apiKey, providerConfig, messages, temperature, timeoutSec);
            case "gemini":
                return sendGeminiRequest(apiKey, providerConfig, messages, temperature, timeoutSec);
            default:
                // 默认使用OpenAI格式
                return sendOpenAIFormatRequest(apiKey, providerConfig, messages, temperature, timeoutSec);
        }
    }

    /**
     * 发送OpenAI格式请求 (DeepSeek等兼容OpenAI API的提供商)
     */
    private String sendOpenAIFormatRequest(String apiKey, AiConfig.ProviderConfig config,
                                           List<Map<String, String>> messages, Double temperature, int timeoutSec) throws Exception {

        Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "messages", messages,
                "temperature", temperature != null ? temperature : config.getTemperature(),
                "stream", false
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(timeoutSec))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("AI 请求失败: " + response.statusCode() + " - " + response.body());
        }

        return extractOpenAIContent(response.body());
    }

    /**
     * 发送Claude请求 (Anthropic格式)
     */
    private String sendClaudeRequest(String apiKey, AiConfig.ProviderConfig config,
                                     List<Map<String, String>> messages, Double temperature, int timeoutSec) throws Exception {

        // 提取系统消息
        String systemPrompt = null;
        List<Map<String, String>> claudeMessages = new ArrayList<>();

        for (Map<String, String> msg : messages) {
            if ("system".equals(msg.get("role"))) {
                systemPrompt = msg.get("content");
            } else {
                // Claude使用user和assistant角色
                claudeMessages.add(msg);
            }
        }

        // 构建Claude格式的请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("messages", claudeMessages);
        requestBody.put("temperature", temperature != null ? temperature : config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens() != null ? config.getMaxTokens() : 2000);

        if (systemPrompt != null) {
            requestBody.put("system", systemPrompt);
        }

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl()))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)  // Claude使用x-api-key
                .header("anthropic-version", "2023-06-01")
                .timeout(Duration.ofSeconds(timeoutSec))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Claude 请求失败: " + response.statusCode() + " - " + response.body());
        }

        return extractClaudeContent(response.body());
    }

    /**
     * 发送Gemini请求 (Google格式)
     */
    private String sendGeminiRequest(String apiKey, AiConfig.ProviderConfig config,
                                     List<Map<String, String>> messages, Double temperature, int timeoutSec) throws Exception {

        // 转换消息格式为Gemini格式
        List<Map<String, Object>> geminiContents = new ArrayList<>();
        String systemPrompt = null;

        for (Map<String, String> msg : messages) {
            if ("system".equals(msg.get("role"))) {
                systemPrompt = msg.get("content");
                continue;
            }

            Map<String, Object> content = new HashMap<>();
            content.put("role", "assistant".equals(msg.get("role")) ? "model" : "user");
            content.put("parts", List.of(Map.of("text", msg.get("content"))));
            geminiContents.add(content);
        }

        // 如果有系统提示词，添加到第一条用户消息
        if (systemPrompt != null && !geminiContents.isEmpty()) {
            Map<String, Object> firstContent = geminiContents.get(0);
            List<Map<String, String>> parts = (List<Map<String, String>>) firstContent.get("parts");
            String originalText = parts.get(0).get("text");
            parts.set(0, Map.of("text", systemPrompt + "\n\n" + originalText));
        }

        // 构建Gemini格式的请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", geminiContents);

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature != null ? temperature : config.getTemperature());
        generationConfig.put("maxOutputTokens", config.getMaxTokens() != null ? config.getMaxTokens() : 2000);
        requestBody.put("generationConfig", generationConfig);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // Gemini API URL包含API key
        String url = config.getUrl() + "?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(timeoutSec))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini 请求失败: " + response.statusCode() + " - " + response.body());
        }

        return extractGeminiContent(response.body());
    }

    /**
     * 提取OpenAI格式的响应内容
     */
    private String extractOpenAIContent(String responseBody) throws Exception {
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
     * 提取Claude格式的响应内容
     */
    private String extractClaudeContent(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get("content");

        if (content == null || content.isEmpty()) {
            throw new RuntimeException("Claude 返回内容为空");
        }

        String text = (String) content.get(0).get("text");

        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Claude 返回的text为空");
        }

        return text;
    }

    /**
     * 提取Gemini格式的响应内容
     */
    private String extractGeminiContent(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini 返回内容为空");
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Gemini 返回的parts为空");
        }

        String text = (String) parts.get(0).get("text");

        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Gemini 返回的text为空");
        }

        return text;
    }

    /**
     * 统一的流式AI请求接口
     */
    public void sendStreamRequest(String apiKey, String providerKey, List<Map<String, String>> messages,
                                   Double temperature, int timeoutSec,
                                   Consumer<String> onChunk,
                                   Runnable onComplete,
                                   Consumer<Exception> onError) {

        AiConfig.ProviderConfig providerConfig = aiConfig.getProviderConfig(providerKey);
        if (providerConfig == null || !providerConfig.isEnabled()) {
            onError.accept(new RuntimeException("AI 提供商未配置或已禁用: " + providerKey));
            return;
        }

        // 根据提供商类型路由到不同的流式实现
        switch (providerKey.toLowerCase()) {
            case "deepseek":
                sendOpenAIStreamRequest(apiKey, providerConfig, messages, temperature, timeoutSec, onChunk, onComplete, onError);
                break;
            case "claude":
                sendClaudeStreamRequest(apiKey, providerConfig, messages, temperature, timeoutSec, onChunk, onComplete, onError);
                break;
            case "gemini":
                sendGeminiStreamRequest(apiKey, providerConfig, messages, temperature, timeoutSec, onChunk, onComplete, onError);
                break;
            default:
                sendOpenAIStreamRequest(apiKey, providerConfig, messages, temperature, timeoutSec, onChunk, onComplete, onError);
                break;
        }
    }

    /**
     * OpenAI格式的流式请求
     */
    private void sendOpenAIStreamRequest(String apiKey, AiConfig.ProviderConfig config,
                                         List<Map<String, String>> messages, Double temperature, int timeoutSec,
                                         Consumer<String> onChunk, Runnable onComplete, Consumer<Exception> onError) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature != null ? temperature : config.getTemperature());
            requestBody.put("stream", true);
            requestBody.put("max_tokens", 16384);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeoutSec))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("AI 请求失败: " + response.statusCode() + " - " + errorBody);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8), 65536)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();

                        if ("[DONE]".equals(data)) {
                            if (onComplete != null) {
                                onComplete.run();
                            }
                            break;
                        }

                        try {
                            Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");

                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                if (delta != null) {
                                    String content = (String) delta.get("content");
                                    if (content != null && !content.isEmpty() && onChunk != null) {
                                        onChunk.accept(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // 忽略解析错误
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("OpenAI流式请求异常: {}", e.getMessage(), e);
            if (onError != null) {
                onError.accept(e);
            }
        }
    }

    /**
     * Claude格式的流式请求
     */
    private void sendClaudeStreamRequest(String apiKey, AiConfig.ProviderConfig config,
                                         List<Map<String, String>> messages, Double temperature, int timeoutSec,
                                         Consumer<String> onChunk, Runnable onComplete, Consumer<Exception> onError) {
        try {
            // 提取系统消息
            String systemPrompt = null;
            List<Map<String, String>> claudeMessages = new ArrayList<>();

            for (Map<String, String> msg : messages) {
                if ("system".equals(msg.get("role"))) {
                    systemPrompt = msg.get("content");
                } else {
                    claudeMessages.add(msg);
                }
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", claudeMessages);
            requestBody.put("temperature", temperature != null ? temperature : config.getTemperature());
            requestBody.put("max_tokens", config.getMaxTokens() != null ? config.getMaxTokens() : 2000);
            requestBody.put("stream", true);

            if (systemPrompt != null) {
                requestBody.put("system", systemPrompt);
            }

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl()))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .timeout(Duration.ofSeconds(timeoutSec))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("Claude 请求失败: " + response.statusCode() + " - " + errorBody);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();

                        try {
                            Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                            String type = (String) chunk.get("type");

                            if ("content_block_delta".equals(type)) {
                                Map<String, Object> delta = (Map<String, Object>) chunk.get("delta");
                                if (delta != null) {
                                    String text = (String) delta.get("text");
                                    if (text != null && !text.isEmpty() && onChunk != null) {
                                        onChunk.accept(text);
                                    }
                                }
                            } else if ("message_stop".equals(type)) {
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                                break;
                            }
                        } catch (Exception e) {
                            // 忽略解析错误
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Claude流式请求异常: {}", e.getMessage(), e);
            if (onError != null) {
                onError.accept(e);
            }
        }
    }

    /**
     * Gemini格式的流式请求
     */
    private void sendGeminiStreamRequest(String apiKey, AiConfig.ProviderConfig config,
                                         List<Map<String, String>> messages, Double temperature, int timeoutSec,
                                         Consumer<String> onChunk, Runnable onComplete, Consumer<Exception> onError) {
        try {
            // 转换消息格式
            List<Map<String, Object>> geminiContents = new ArrayList<>();
            String systemPrompt = null;

            for (Map<String, String> msg : messages) {
                if ("system".equals(msg.get("role"))) {
                    systemPrompt = msg.get("content");
                    continue;
                }

                Map<String, Object> content = new HashMap<>();
                content.put("role", "assistant".equals(msg.get("role")) ? "model" : "user");
                content.put("parts", List.of(Map.of("text", msg.get("content"))));
                geminiContents.add(content);
            }

            if (systemPrompt != null && !geminiContents.isEmpty()) {
                Map<String, Object> firstContent = geminiContents.get(0);
                List<Map<String, String>> parts = (List<Map<String, String>>) firstContent.get("parts");
                String originalText = parts.get(0).get("text");
                parts.set(0, Map.of("text", systemPrompt + "\n\n" + originalText));
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", geminiContents);

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", temperature != null ? temperature : config.getTemperature());
            generationConfig.put("maxOutputTokens", config.getMaxTokens() != null ? config.getMaxTokens() : 2000);
            requestBody.put("generationConfig", generationConfig);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // Gemini使用streamGenerateContent端点
            String streamUrl = config.getUrl().replace(":generateContent", ":streamGenerateContent") + "?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(streamUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(timeoutSec))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("Gemini 请求失败: " + response.statusCode() + " - " + errorBody);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    try {
                        Map<String, Object> chunk = objectMapper.readValue(line, Map.class);
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) chunk.get("candidates");

                        if (candidates != null && !candidates.isEmpty()) {
                            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                            if (content != null) {
                                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                                if (parts != null && !parts.isEmpty()) {
                                    String text = (String) parts.get(0).get("text");
                                    if (text != null && !text.isEmpty() && onChunk != null) {
                                        onChunk.accept(text);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                }

                if (onComplete != null) {
                    onComplete.run();
                }
            }

        } catch (Exception e) {
            log.error("Gemini流式请求异常: {}", e.getMessage(), e);
            if (onError != null) {
                onError.accept(e);
            }
        }
    }
}
