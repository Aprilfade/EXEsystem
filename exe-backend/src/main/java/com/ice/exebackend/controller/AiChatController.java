package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.AiChatService;
import com.ice.exebackend.service.BizStudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI学习助手对话控制器
 *
 * @author AI功能组
 * @version v3.05
 */
@RestController
@RequestMapping("/api/v1/student/ai-chat")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private BizStudentService bizStudentService;

    /**
     * 获取当前登录学生ID
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BizStudent student = bizStudentService.lambdaQuery()
                .eq(BizStudent::getStudentNo, username)
                .one();
        return student != null ? student.getId() : null;
    }

    /**
     * 发送消息
     *
     * @param request 对话请求
     * @param httpRequest HTTP请求
     * @return 对话响应
     */
    @PostMapping("/send")
    public Result sendMessage(@RequestBody AiChatService.ChatRequest request, HttpServletRequest httpRequest) {
        // 获取当前用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.fail("无法获取当前用户信息");
        }

        // 获取AI配置
        String apiKey = httpRequest.getHeader("X-Ai-Api-Key");
        String provider = httpRequest.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("未配置AI Key，请在个人设置中配置");
        }

        if (!StringUtils.hasText(request.getMessage())) {
            return Result.fail("消息内容不能为空");
        }

        // 设置默认值
        if (request.getChatType() == null || request.getChatType().isEmpty()) {
            request.setChatType("general");
        }

        if (request.getContextSize() == 0) {
            request.setContextSize(5); // 默认保留最近5轮对话
        }

        try {
            AiChatService.ChatResponse response = aiChatService.chat(
                    userId,
                    apiKey,
                    provider != null ? provider : "deepseek",
                    request
            );

            return Result.suc(response);

        } catch (Exception e) {
            return Result.fail("对话失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话列表
     *
     * @param limit 限制数量
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public Result getSessions(@RequestParam(defaultValue = "10") int limit) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.fail("无法获取当前用户信息");
        }

        try {
            List<AiChatService.ChatSession> sessions = aiChatService.getChatSessions(userId, limit);
            return Result.suc(sessions);

        } catch (Exception e) {
            return Result.fail("获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话历史消息
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result getSessionMessages(@PathVariable String sessionId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.fail("无法获取当前用户信息");
        }

        try {
            List<AiChatService.ChatMessage> messages =
                    aiChatService.getSessionMessages(userId, sessionId);

            return Result.suc(messages);

        } catch (Exception e) {
            return Result.fail("获取会话历史失败: " + e.getMessage());
        }
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @return 结果
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result deleteSession(@PathVariable String sessionId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.fail("无法获取当前用户信息");
        }

        try {
            aiChatService.deleteSession(userId, sessionId);
            return Result.suc("会话已删除");

        } catch (Exception e) {
            return Result.fail("删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 创建新会话
     *
     * @return 新会话ID
     */
    @PostMapping("/sessions/new")
    public Result createNewSession() {
        String sessionId = java.util.UUID.randomUUID().toString();
        return Result.suc(sessionId);
    }
}
