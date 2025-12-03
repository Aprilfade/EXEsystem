package com.ice.exebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.dto.AiAnalysisReq;
import com.ice.exebackend.dto.AiGeneratedQuestionDTO;
import com.ice.exebackend.dto.AiGradingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference; // 导入 TypeReference

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
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
    /**
     * 【新增】AI 主观题智能批改
     * @param apiKey API Key
     * @param providerKey 模型提供商
     * @param questionContent 题目内容
     * @param referenceAnswer 参考答案
     * @param studentAnswer 学生答案
     * @param maxScore 该题满分
     * @return 评分结果对象
     */

    public AiGradingResult gradeSubjectiveQuestion(String apiKey, String providerKey,
                                                   String questionContent, String referenceAnswer,
                                                   String studentAnswer, int maxScore) throws Exception {
        // 1. 确定提供商 (复用原有逻辑)
        AiProvider provider;
        try {
            provider = AiProvider.valueOf(providerKey != null ? providerKey.toUpperCase() : "DEEPSEEK");
        } catch (IllegalArgumentException | NullPointerException e) {
            provider = AiProvider.DEEPSEEK;
        }

        // 2. 构建 Prompt：强制要求 AI 返回 JSON 格式
        String systemPrompt = "你是一位公正的阅卷老师。请根据题目、参考答案和学生答案进行打分。" +
                "请务必严格只返回一个合法的 JSON 字符串，不要包含 Markdown 格式（如 ```json ... ```），也不要包含其他多余文字。\n" +
                "JSON 格式要求：\n" +
                "{\n" +
                "  \"score\": (整数，0 到 " + maxScore + " 之间),\n" +
                "  \"feedback\": \"(简短的评语，指出优点或不足)\"\n" +
                "}";

        String userPrompt = String.format(
                "题目：%s\n" +
                        "参考答案：%s\n" +
                        "该题满分：%d 分\n" +
                        "学生回答：%s",
                questionContent, referenceAnswer, maxScore, studentAnswer
        );

        // 3. 构建请求体 (复用原有逻辑结构)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", provider.model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.3); // 降低随机性，使评分更稳定
        requestBody.put("stream", false);
        // ... (省略 HTTP 请求发送部分，与原 analyzeWrongQuestion 方法一致) ...

        // 4. 发起请求并获取响应字符串 (假设你已经把发送请求的逻辑封装好了，或者直接复制 analyzeWrongQuestion 的发送代码)
        // 为了演示清晰，这里重复一下发送逻辑，实际代码中建议抽取 sendRequest 私有方法
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMinutes(4)) // 【新增】设置请求读取超时为 3分钟
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("AI 批改失败: " + response.statusCode());
        }

        // 5. 解析 AI 响应外层
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        // 6. 【核心修改】使用新的提取方法清洗内容
        String jsonString = extractJson(content);

        // 7. 解析清洗后的 JSON
        try {
            return objectMapper.readValue(jsonString, AiGradingResult.class);
        } catch (Exception e) {
            // 如果解析依然失败，打印原始返回内容以便调试
            System.err.println("AI 返回的原始内容无法解析: " + content);
            throw e; // 继续抛出，让 Controller 捕获
        }
    }

    // 【修改】辅助方法：更智能地提取 JSON 部分
    private String extractJson(String content) {
        if (content == null) return "[]";

        // 优先寻找数组标记
        int firstBracket = content.indexOf("[");
        int lastBracket = content.lastIndexOf("]");

        // 如果找到了成对的 []，且它们之间包含了主要内容，则优先截取数组
        if (firstBracket != -1 && lastBracket != -1 && firstBracket < lastBracket) {
            return content.substring(firstBracket, lastBracket + 1);
        }

        // 如果没找到数组，寻找对象标记 {} (作为容错)
        int firstBrace = content.indexOf("{");
        int lastBrace = content.lastIndexOf("}");
        if (firstBrace != -1 && lastBrace != -1 && firstBrace < lastBrace) {
            return content.substring(firstBrace, lastBrace + 1);
        }

        return content.trim();
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
                .connectTimeout(Duration.ofSeconds(60)) // 放宽一点超时时间
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.url)) // 使用动态 URL
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMinutes(4)) // 【新增】显式设置读取超时为 3分钟，防止 AI 生成过长导致挂起
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
    /**
     * 【修改】AI 智能出题 - 支持主观题
     */
    public List<AiGeneratedQuestionDTO> generateQuestionsFromText(String apiKey, String providerKey, String text, int count, int type) throws Exception {
        // 1. 确定提供商 (保持不变)
        AiProvider provider;
        try {
            provider = AiProvider.valueOf(providerKey != null ? providerKey.toUpperCase() : "DEEPSEEK");
        } catch (Exception e) {
            provider = AiProvider.DEEPSEEK;
        }

        // 2. 【修改点 1】构建 Prompt，增加主观题类型说明
        String typeDesc = switch (type) {
            case 1 -> "单选题";
            case 2 -> "多选题";
            case 3 -> "填空题";
            case 4 -> "判断题";
            case 5 -> "主观题"; // 新增
            default -> "混合题型（包含单选、多选、填空、判断、主观题）"; // 修改描述
        };

        // 【修改点 2】更新 JSON 结构说明，告知 AI type=5 的规则
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

        // 3. 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", provider.model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.5);
        requestBody.put("stream", false);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 4. 发送请求
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMinutes(3))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("AI 请求失败: " + response.statusCode() + " " + response.body());
        }

        // 5. 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

        // 6. 【核心修改】增强清洗并解析 JSON
        String cleanJson = extractJson(content);

        try {
            // 尝试直接解析为 List
            return objectMapper.readValue(cleanJson, new TypeReference<List<AiGeneratedQuestionDTO>>() {});
        } catch (Exception e) {
            // 如果解析 List 失败，尝试解析为 JsonNode (处理 AI 返回对象的情况)
            try {
                JsonNode root = objectMapper.readTree(cleanJson);
                // 情况 A: 返回的是个对象，但里面包了数组 (例如 {"questions": [...]})
                if (root.isObject()) {
                    // 遍历所有字段，找到第一个是数组的字段
                    Iterator<String> fieldNames = root.fieldNames();
                    while (fieldNames.hasNext()) {
                        String fieldName = fieldNames.next();
                        JsonNode field = root.get(fieldName);
                        if (field.isArray()) {
                            return objectMapper.convertValue(field, new TypeReference<List<AiGeneratedQuestionDTO>>() {});
                        }
                    }
                    // 情况 B: 返回的是单个题目对象，不是数组
                    // 尝试将整个对象转为单个 DTO 放入 List
                    try {
                        AiGeneratedQuestionDTO singleQuestion = objectMapper.convertValue(root, AiGeneratedQuestionDTO.class);
                        if (singleQuestion.getContent() != null) {
                            return List.of(singleQuestion);
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ex) {
                // ignore, throw original error below
            }
            // 如果补救措施也失败，抛出原始异常，并在日志中打印 AI 返回的内容以便调试
            System.err.println("AI 返回的非标准 JSON 内容: " + cleanJson);
            throw new RuntimeException("解析 AI 结果失败，请重试或检查文本内容。");
        }
    }
    /**
     * 【新增】AI 智能提取知识点
     */
    public List<Map<String, String>> generateKnowledgePointsFromText(String apiKey, String providerKey, String text, int count) throws Exception {
        // 1. 确定提供商
        AiProvider provider;
        try {
            provider = AiProvider.valueOf(providerKey != null ? providerKey.toUpperCase() : "DEEPSEEK");
        } catch (Exception e) {
            provider = AiProvider.DEEPSEEK;
        }

        // 2. 构建 Prompt
        String systemPrompt = "你是一位资深的教学专家。请根据用户提供的文本内容，提取出 " + count + " 个核心知识点。\n" +
                "请务必严格只返回一个合法的 JSON 数组，不要包含 Markdown 代码块标记（如 ```json），也不要包含其他多余文字。\n" +
                "JSON 数组中每个对象的格式如下：\n" +
                "{\n" +
                "  \"name\": \"知识点名称（简练准确）\",\n" +
                "  \"description\": \"知识点详细描述或定义（50-200字）\"\n" +
                "}";

        String userPrompt = "文本内容如下：\n" + (text.length() > 3000 ? text.substring(0, 3000) : text);

        // 3. 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", provider.model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.5);
        requestBody.put("stream", false);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 4. 发送请求
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMinutes(3))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("AI 请求失败: " + response.statusCode() + " " + response.body());
        }

        // 5. 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

        // 6. 清洗并解析 JSON
        String cleanJson = extractJson(content);

        return objectMapper.readValue(cleanJson, new TypeReference<List<Map<String, String>>>() {});
    }


}