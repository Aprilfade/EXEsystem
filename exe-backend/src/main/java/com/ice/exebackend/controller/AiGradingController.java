package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.GradingRequest;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.AiServiceV3;
import com.ice.exebackend.service.BizStudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI智能批改控制器
 * 提供在线练习功能的主观题批改服务
 */
@RestController
@RequestMapping("/api/v1/ai/grading")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class AiGradingController {

    private static final Logger log = LoggerFactory.getLogger(AiGradingController.class);

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

    /**
     * 流式AI批改接口
     * 返回SSE流,实时推送批改分析
     *
     * @param request 批改请求
     * @param httpRequest HTTP请求(用于获取Header中的API Key和Provider)
     * @return SseEmitter流对象
     */
    @PostMapping("/stream")
    public SseEmitter streamGrading(@RequestBody GradingRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("学生信息不存在"));
            return emitter;
        }

        // 从Header中获取API配置
        String apiKey = httpRequest.getHeader("X-Ai-Api-Key");
        String provider = httpRequest.getHeader("X-Ai-Provider");

        // 默认值处理
        if (provider == null || provider.isEmpty()) {
            provider = "deepseek";
        }

        if (apiKey == null || apiKey.isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("请先配置AI API Key"));
            return emitter;
        }

        // 设置默认满分
        int maxScore = request.getMaxScore() != null ? request.getMaxScore() : 100;

        log.info("收到AI批改请求: user={}, questionId={}, questionType={}, provider={}",
                userId, request.getQuestionId(), request.getQuestionType(), provider);

        try {
            // 调用Service层的流式批改方法
            return aiServiceV3.gradeSubjectiveQuestionStream(
                    apiKey,
                    provider,
                    request.getQuestionContent(),
                    request.getCorrectAnswer(),
                    request.getUserAnswer(),
                    maxScore,
                    userId
            );
        } catch (Exception e) {
            log.error("AI批改异常: user={}, error={}", userId, e.getMessage(), e);
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(e);
            return emitter;
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.suc("AI批改服务运行正常");
    }
}
