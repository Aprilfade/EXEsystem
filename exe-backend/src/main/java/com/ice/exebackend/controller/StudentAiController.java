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
    public Result analyzeWrongQuestion(@RequestBody AiAnalysisReq req, HttpServletRequest request) {
        String apiKey = request.getHeader("X-Ai-Api-Key");
        // 【新增】获取提供商标识
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("未提供 API Key，请在个人设置中配置");
        }

        try {
            // 传入 provider
            String analysisResult = aiService.analyzeWrongQuestion(apiKey, provider, req);
            return Result.suc(analysisResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("AI 分析失败：" + e.getMessage());
        }
    }
}