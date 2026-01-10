package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.AiAnalysisReq;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.AiService;
import com.ice.exebackend.service.AiServiceV3;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/student/ai")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentAiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private AiServiceV3 aiServiceV3;

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

    @PostMapping("/analyze")
    public Result analyzeWrongQuestion(@RequestBody AiAnalysisReq req, HttpServletRequest request) throws Exception {
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("未提供 API Key，请在个人设置中配置");
        }

        String analysisResult = aiService.analyzeWrongQuestion(apiKey, provider, req);

        return Result.suc(analysisResult);
    }

    /**
     * 流式AI错题分析（SSE）
     * 前端通过 EventSource 连接此端点，实时接收AI分析内容
     */
    @PostMapping("/analyze-stream")
    public SseEmitter analyzeWrongQuestionStream(@RequestBody AiAnalysisReq req, HttpServletRequest request) throws Exception {
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("未提供 API Key，请在个人设置中配置"));
            return emitter;
        }

        // 获取当前用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("无法获取当前用户信息"));
            return emitter;
        }

        // 调用流式分析服务
        return aiServiceV3.analyzeWrongQuestionStream(
            apiKey,
            provider != null ? provider : "deepseek",
            req,
            userId
        );
    }

    /**
     * 测试AI连接
     */
    @PostMapping("/test-connection")
    public Result testConnection(HttpServletRequest request) {
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("未提供 API Key");
        }

        try {
            // 简单测试：构造一个测试请求
            AiAnalysisReq testReq = new AiAnalysisReq();
            testReq.setQuestionContent("这是一个简单的测试问题，用于验证API连接");
            testReq.setCorrectAnswer("测试答案");
            testReq.setStudentAnswer("测试答案");

            // 调用AI服务进行简单测试
            String testResult = aiService.analyzeWrongQuestion(
                apiKey,
                provider != null ? provider : "deepseek",
                testReq
            );

            if (testResult != null && !testResult.isEmpty()) {
                return Result.suc("连接测试成功");
            } else {
                return Result.fail("连接测试失败：返回结果为空");
            }
        } catch (Exception e) {
            return Result.fail("连接测试失败：" + e.getMessage());
        }
    }
}
