package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.AiAnalysisReq;
import com.ice.exebackend.service.EnhancedWrongAnalysisService;
import com.ice.exebackend.service.StudentLearningProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 深度错因分析控制器
 *
 * @author AI功能组
 * @version v3.04
 */
@RestController
@RequestMapping("/api/v1/student/deep-analysis")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class DeepAnalysisController {

    @Autowired
    private EnhancedWrongAnalysisService enhancedWrongAnalysisService;

    @Autowired
    private StudentLearningProfileService profileService;

    /**
     * 深度错因分析
     *
     * @param req 分析请求
     * @param request HTTP请求（用于获取AI配置）
     * @return 深度分析结果
     */
    @PostMapping("/analyze")
    public Result deepAnalyze(@RequestBody AiAnalysisReq req, HttpServletRequest request) {
        // 获取当前用户ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        // 获取AI配置
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("未配置AI Key，请在个人设置中配置");
        }

        // 参数校验
        if (!StringUtils.hasText(req.getQuestionContent())) {
            return Result.fail("题目内容不能为空");
        }
        if (!StringUtils.hasText(req.getCorrectAnswer())) {
            return Result.fail("正确答案不能为空");
        }
        if (!StringUtils.hasText(req.getStudentAnswer())) {
            return Result.fail("学生答案不能为空");
        }

        try {
            // 调用深度分析服务
            EnhancedWrongAnalysisService.DeepAnalysisResult result =
                    enhancedWrongAnalysisService.analyzeWrongQuestionDeep(
                            apiKey,
                            provider != null ? provider : "deepseek",
                            userId,
                            req
                    );

            return Result.suc(result);

        } catch (Exception e) {
            return Result.fail("深度分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生学习画像
     *
     * @return 学习画像
     */
    @GetMapping("/profile")
    public Result getProfile() {
        // 获取当前用户ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        try {
            StudentLearningProfileService.StudentLearningProfile profile =
                    profileService.getProfile(userId);

            return Result.suc(profile);

        } catch (Exception e) {
            return Result.fail("获取学习画像失败: " + e.getMessage());
        }
    }

    /**
     * 快速分析（兼容旧版API）
     *
     * @param req 分析请求
     * @param request HTTP请求
     * @return 分析结果
     */
    @PostMapping("/quick-analyze")
    public Result quickAnalyze(@RequestBody AiAnalysisReq req, HttpServletRequest request) {
        // 快速分析模式：不使用学习画像和知识图谱，直接调用AI
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("未配置AI Key，请在个人设置中配置");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        try {
            // 清空知识点ID，跳过知识图谱溯源
            req.setKnowledgePointId(null);

            EnhancedWrongAnalysisService.DeepAnalysisResult result =
                    enhancedWrongAnalysisService.analyzeWrongQuestionDeep(
                            apiKey,
                            provider != null ? provider : "deepseek",
                            userId,
                            req
                    );

            // 只返回AI分析文本，不返回学习路径
            return Result.suc(result.getFullAnalysis());

        } catch (Exception e) {
            return Result.fail("分析失败: " + e.getMessage());
        }
    }
}
