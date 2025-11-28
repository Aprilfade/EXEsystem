package com.ice.exebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.dto.AiAnalysisReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Autowired
    private ObjectMapper objectMapper;

    // 定义支持的 AI 提供商配置
    private enum AiProvider {
        DEEPSEEK("https://api.deepseek.com/chat/completions", "deepseek-chat"),
        QWEN("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", "qwen-plus"); // 通义千问使用兼容接口

        final String url;
        final String model;

        AiProvider(String url, String model) {
            this.url = url;
            this.model = model;
        }
    }

    // 在方法签名中增加 provider 参数
    @SuppressWarnings("unchecked")
    public String analyzeWrongQuestion(String apiKey, String providerKey, AiAnalysisReq req) throws Exception {
        // 1. 确定提供商配置 (默认 DeepSeek)
        AiProvider provider;
        try {
            provider = AiProvider.valueOf(providerKey.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            provider = AiProvider.DEEPSEEK;
        }

        // 2. 构建 Prompt (保持不变)
        String systemPrompt = "你是一位经验丰富的中学全科教师。请根据学生提供的错题信息，进行深入浅出的分析。包含三个部分：\n" +
                "1. 【知识点回顾】：简要回顾题目涉及的核心考点。\n" +
                "2. 【错误原因推测】：分析学生为什么会填这个错误答案（例如概念混淆、计算失误等）。\n" +
                "3. 【解题思路】：给出正确的推导步骤。\n" +
                "请使用 Markdown 格式输出，保持语气鼓励和耐心。";

        String userPrompt = String.format("题目：%s\n正确答案：%s\n学生的错误答案：%s\n原解析参考：%s",
                req.getQuestionContent(), req.getCorrectAnswer(), req.getStudentAnswer(), req.getAnalysis());

        // 3. 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", provider.model); // 使用动态模型名
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", false);

        // 针对通义千问，可能需要关闭搜索增强以加快速度（可选）
        if (provider == AiProvider.QWEN) {
            // requestBody.put("enable_search", false);
        }

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 4. 发起 HTTP 请求
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15)) // 放宽一点超时时间
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.url)) // 使用动态 URL
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("AI 接口调用失败 (" + provider.name() + "): " + response.statusCode() + " - " + response.body());
        }

        // 5. 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI 返回内容为空");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}