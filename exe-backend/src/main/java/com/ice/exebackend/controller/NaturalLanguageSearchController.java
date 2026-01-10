package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.service.NaturalLanguageSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 自然语言搜索控制器
 *
 * @author AI功能组
 * @version v3.03
 */
@RestController
@RequestMapping("/api/v1/student/nlp-search")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class NaturalLanguageSearchController {

    @Autowired
    private NaturalLanguageSearchService searchService;

    /**
     * 自然语言搜索
     *
     * @param req 请求体，包含query字段
     * @param request HTTP请求，用于获取AI配置
     * @return 搜索结果
     */
    @PostMapping("/query")
    public Result search(@RequestBody Map<String, String> req, HttpServletRequest request) {
        String query = req.get("query");

        if (!StringUtils.hasText(query)) {
            return Result.fail("搜索内容不能为空");
        }

        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("未配置AI Key，请在个人设置中配置");
        }

        try {
            NaturalLanguageSearchService.SearchResult result =
                    searchService.search(apiKey, provider != null ? provider : "deepseek", query);

            return Result.suc(result);

        } catch (Exception e) {
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }
}
