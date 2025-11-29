package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.AiAnalysisReq;
import com.ice.exebackend.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/student/ai")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentAiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/analyze")
    public Result analyzeWrongQuestion(@RequestBody AiAnalysisReq req, HttpServletRequest request) throws Exception { // 允许抛出 Exception
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("未提供 API Key，请在个人设置中配置");
        }

        // 直接调用逻辑，移除 try-catch
        // 如果 aiService 抛出 RuntimeException("AI 接口调用失败...")，全局处理器会捕获它
        String analysisResult = aiService.analyzeWrongQuestion(apiKey, provider, req);

        return Result.suc(analysisResult);
    }
}
