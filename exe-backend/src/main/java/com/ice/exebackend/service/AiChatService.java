package com.ice.exebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ice.exebackend.entity.AiChatHistory;
import com.ice.exebackend.mapper.AiChatHistoryMapper;
import com.ice.exebackend.utils.AiHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI学习助手对话服务
 *
 * @author AI功能组
 * @version v3.05
 */
@Service
public class AiChatService {

    @Autowired
    private AiChatHistoryMapper chatHistoryMapper;

    @Autowired
    private AiHttpClient aiHttpClient;

    @Autowired
    private StudentLearningProfileService profileService;

    /**
     * 对话请求
     */
    public static class ChatRequest {
        private String message;          // 用户消息
        private String sessionId;        // 会话ID
        private String chatType;         // 对话类型
        private boolean useContext;      // 是否使用上下文
        private int contextSize;         // 上下文轮数

        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getChatType() { return chatType; }
        public void setChatType(String chatType) { this.chatType = chatType; }

        public boolean isUseContext() { return useContext; }
        public void setUseContext(boolean useContext) { this.useContext = useContext; }

        public int getContextSize() { return contextSize; }
        public void setContextSize(int contextSize) { this.contextSize = contextSize; }
    }

    /**
     * 对话响应
     */
    public static class ChatResponse {
        private String message;          // AI回复
        private String sessionId;        // 会话ID
        private List<String> suggestions; // 后续建议问题
        private String chatType;         // 对话类型

        public ChatResponse() {
            this.suggestions = new ArrayList<>();
        }

        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

        public String getChatType() { return chatType; }
        public void setChatType(String chatType) { this.chatType = chatType; }
    }

    /**
     * 对话消息
     */
    public static class ChatMessage {
        private String role;     // user/assistant/system
        private String content;  // 消息内容
        private LocalDateTime createTime;

        public ChatMessage() {}

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.createTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }

    /**
     * 发送对话消息
     *
     * @param userId 学生ID
     * @param apiKey AI API Key
     * @param provider AI提供商
     * @param request 对话请求
     * @return 对话响应
     */
    public ChatResponse chat(Long userId, String apiKey, String provider, ChatRequest request) throws Exception {
        // 1. 生成或使用现有会话ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // 2. 构建消息列表
        List<Map<String, String>> messages = buildMessages(userId, sessionId, request);

        // 3. 调用AI
        String aiResponse = aiHttpClient.sendRequest(apiKey, provider, messages, 0.8, 60);

        // 4. 保存对话历史
        saveChatHistory(userId, sessionId, "user", request.getMessage(), request.getChatType(), provider);
        saveChatHistory(userId, sessionId, "assistant", aiResponse, request.getChatType(), provider);

        // 5. 构建响应
        ChatResponse response = new ChatResponse();
        response.setMessage(aiResponse);
        response.setSessionId(sessionId);
        response.setChatType(request.getChatType());
        response.setSuggestions(generateSuggestions(request.getChatType()));

        return response;
    }

    /**
     * 构建消息列表（包含系统提示词和上下文）
     */
    private List<Map<String, String>> buildMessages(Long userId, String sessionId, ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 1. 添加系统提示词
        String systemPrompt = buildSystemPrompt(userId, request.getChatType());
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // 2. 加载历史上下文（如果需要）
        if (request.isUseContext()) {
            List<AiChatHistory> history = loadChatHistory(userId, sessionId, request.getContextSize());
            for (AiChatHistory h : history) {
                messages.add(Map.of("role", h.getRole(), "content", h.getContent()));
            }
        }

        // 3. 添加当前用户消息
        messages.add(Map.of("role", "user", "content", request.getMessage()));

        return messages;
    }

    /**
     * 构建系统提示词（个性化）
     */
    private String buildSystemPrompt(Long userId, String chatType) {
        StringBuilder prompt = new StringBuilder();

        // 基础角色定义
        prompt.append("你是一个专业的AI学习助手，名字叫\"小艾\"。你的职责是帮助学生更好地学习。\n\n");

        // 获取学生画像
        try {
            StudentLearningProfileService.StudentLearningProfile profile = profileService.getProfile(userId);

            prompt.append("### 学生信息\n");
            prompt.append(String.format("- 学习水平：%s\n", profile.getLevel()));
            prompt.append(String.format("- 学习风格：%s\n", profile.getLearningStyle()));
            prompt.append(String.format("- 平均正确率：%.0f%%\n", profile.getAverageAccuracy() * 100));

            if (!profile.getWeakPoints().isEmpty()) {
                prompt.append(String.format("- 薄弱知识点：%s\n",
                        String.join("、", profile.getWeakPoints())));
            }

            prompt.append("\n");
        } catch (Exception e) {
            // 如果获取画像失败，使用默认提示词
        }

        // 根据对话类型定制提示词
        switch (chatType != null ? chatType : "general") {
            case "learning":
                prompt.append("### 当前场景：学习辅导\n");
                prompt.append("请提供详细的学习建议和知识点讲解，语言要通俗易懂，多举例子。\n\n");
                break;

            case "question":
                prompt.append("### 当前场景：答疑解惑\n");
                prompt.append("请耐心解答学生的问题，如果是题目，请给出详细的解题步骤。\n\n");
                break;

            case "motivation":
                prompt.append("### 当前场景：学习激励\n");
                prompt.append("请给予学生鼓励和正能量，帮助他们建立学习信心。语气要温暖友好。\n\n");
                break;

            case "planning":
                prompt.append("### 当前场景：学习规划\n");
                prompt.append("请帮助学生制定合理的学习计划，给出具体可执行的建议。\n\n");
                break;

            default: // general
                prompt.append("### 当前场景：日常对话\n");
                prompt.append("请友好地与学生交流，识别他们的需求并提供帮助。\n\n");
                break;
        }

        // 通用原则
        prompt.append("### 对话原则\n");
        prompt.append("1. 语气友好亲切，像一个耐心的学长/学姐\n");
        prompt.append("2. 回答要简洁明了，避免过于专业的术语\n");
        prompt.append("3. 多使用鼓励性的语言，帮助学生建立信心\n");
        prompt.append("4. 如果学生问学习以外的问题，礼貌地引导回学习话题\n");
        prompt.append("5. 可以适当使用emoji让对话更生动（但不要过多）\n");

        return prompt.toString();
    }

    /**
     * 加载对话历史
     */
    private List<AiChatHistory> loadChatHistory(Long userId, String sessionId, int contextSize) {
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatHistory::getStudentId, userId);
        wrapper.eq(AiChatHistory::getSessionId, sessionId);
        wrapper.orderByDesc(AiChatHistory::getCreateTime);
        wrapper.last("LIMIT " + (contextSize * 2)); // 每轮包含user和assistant两条

        List<AiChatHistory> history = chatHistoryMapper.selectList(wrapper);

        // 反转顺序（最早的在前）
        Collections.reverse(history);

        return history;
    }

    /**
     * 保存对话历史
     */
    private void saveChatHistory(Long userId, String sessionId, String role, String content,
                                  String chatType, String provider) {
        AiChatHistory history = new AiChatHistory();
        history.setStudentId(userId);
        history.setSessionId(sessionId);
        history.setRole(role);
        history.setContent(content);
        history.setChatType(chatType);
        history.setAiProvider(provider);
        history.setFavorited(false);
        history.setCreateTime(LocalDateTime.now());

        chatHistoryMapper.insert(history);
    }

    /**
     * 生成后续建议问题
     */
    private List<String> generateSuggestions(String chatType) {
        List<String> suggestions = new ArrayList<>();

        switch (chatType != null ? chatType : "general") {
            case "learning":
                suggestions.add("能给我讲讲这个知识点的应用吗？");
                suggestions.add("有没有相关的练习题？");
                suggestions.add("这个知识点有哪些常见错误？");
                break;

            case "question":
                suggestions.add("能再详细解释一下吗？");
                suggestions.add("有没有类似的例题？");
                suggestions.add("这种题目有什么解题技巧？");
                break;

            case "motivation":
                suggestions.add("怎样才能保持学习动力？");
                suggestions.add("遇到困难时该怎么办？");
                suggestions.add("如何提高学习效率？");
                break;

            case "planning":
                suggestions.add("如何安排每天的学习时间？");
                suggestions.add("怎样平衡不同科目？");
                suggestions.add("如何制定考试复习计划？");
                break;

            default:
                suggestions.add("我想了解我的学习情况");
                suggestions.add("有什么学习建议吗？");
                suggestions.add("帮我制定学习计划");
                break;
        }

        return suggestions;
    }

    /**
     * 获取会话列表
     */
    public List<ChatSession> getChatSessions(Long userId, int limit) {
        // 查询最近的会话
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatHistory::getStudentId, userId);
        wrapper.groupBy(AiChatHistory::getSessionId);
        wrapper.orderByDesc(AiChatHistory::getCreateTime);
        wrapper.last("LIMIT " + limit);

        List<AiChatHistory> histories = chatHistoryMapper.selectList(wrapper);

        // 转换为会话列表
        return histories.stream()
                .collect(Collectors.groupingBy(AiChatHistory::getSessionId))
                .entrySet().stream()
                .map(entry -> {
                    String sessionId = entry.getKey();
                    List<AiChatHistory> messages = entry.getValue();

                    ChatSession session = new ChatSession();
                    session.setSessionId(sessionId);
                    session.setMessageCount(messages.size());
                    session.setLastMessageTime(messages.stream()
                            .map(AiChatHistory::getCreateTime)
                            .max(LocalDateTime::compareTo)
                            .orElse(null));

                    // 获取第一条用户消息作为标题
                    String title = messages.stream()
                            .filter(m -> "user".equals(m.getRole()))
                            .map(AiChatHistory::getContent)
                            .findFirst()
                            .orElse("新对话");

                    // 截取前20个字符作为标题
                    session.setTitle(title.length() > 20 ? title.substring(0, 20) + "..." : title);

                    return session;
                })
                .sorted((s1, s2) -> s2.getLastMessageTime().compareTo(s1.getLastMessageTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取会话历史消息
     */
    public List<ChatMessage> getSessionMessages(Long userId, String sessionId) {
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatHistory::getStudentId, userId);
        wrapper.eq(AiChatHistory::getSessionId, sessionId);
        wrapper.orderByAsc(AiChatHistory::getCreateTime);

        List<AiChatHistory> histories = chatHistoryMapper.selectList(wrapper);

        return histories.stream()
                .map(h -> {
                    ChatMessage msg = new ChatMessage();
                    msg.setRole(h.getRole());
                    msg.setContent(h.getContent());
                    msg.setCreateTime(h.getCreateTime());
                    return msg;
                })
                .collect(Collectors.toList());
    }

    /**
     * 删除会话
     */
    public void deleteSession(Long userId, String sessionId) {
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatHistory::getStudentId, userId);
        wrapper.eq(AiChatHistory::getSessionId, sessionId);

        chatHistoryMapper.delete(wrapper);
    }

    /**
     * 会话信息
     */
    public static class ChatSession {
        private String sessionId;
        private String title;
        private int messageCount;
        private LocalDateTime lastMessageTime;

        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public int getMessageCount() { return messageCount; }
        public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

        public LocalDateTime getLastMessageTime() { return lastMessageTime; }
        public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    }
}
