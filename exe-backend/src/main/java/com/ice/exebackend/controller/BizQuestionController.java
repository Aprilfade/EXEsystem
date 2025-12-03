package com.ice.exebackend.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.service.BizQuestionService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // 1. 导入 RedisTemplate
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
// 必须导入这两个，且不要导入 lombok.Log
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.AiService; // 导入 AiService
import com.ice.exebackend.dto.AiGeneratedQuestionDTO; // 导入 DTO
import jakarta.servlet.http.HttpServletRequest; // 导入 HttpServletRequest




import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/questions")
@PreAuthorize("hasAuthority('sys:question:list')")

public class BizQuestionController {

    private static final Logger logger = LoggerFactory.getLogger(BizQuestionController.class);

    @Autowired
    private BizQuestionService questionService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Autowired
    private AiService aiService; // 注入 AI 服务
    // 3. 定义缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @PreAuthorize("hasAuthority('sys:question:list')")
    @Log(title = "题库管理", businessType = BusinessType.INSERT) // 新增
    public Result createQuestion(@RequestBody QuestionDTO questionDTO) {
        boolean success = questionService.createQuestionWithKnowledgePoints(questionDTO);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    // ... [此处省略所有 GET 查询和查重方法，它们不需要修改] ...
    @GetMapping
    public Result getQuestionList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Long subjectId,
                                  @RequestParam(required = false) Integer questionType,
                                  @RequestParam(required = false) String grade) {

        logger.info("开始查询试题列表... subjectId: {}, grade: {}, questionType: {}", subjectId, grade, questionType);
        Page<BizQuestion> page = new Page<>(current, size);
        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        if (questionType != null) {
            queryWrapper.eq("question_type", questionType);
        }
        if (StringUtils.hasText(grade)) {
            queryWrapper.eq("grade", grade);
        }
        queryWrapper.orderByDesc("id");
        questionService.page(page, queryWrapper);
        logger.info("查询完成。为 subjectId: {} 找到了 {} 条记录。", subjectId, page.getRecords().size());
        return Result.suc(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public Result getQuestionById(@PathVariable Long id) {
        QuestionDTO questionDTO = questionService.getQuestionWithKnowledgePointsById(id);
        return Result.suc(questionDTO);
    }

    @PostMapping("/check-duplicate")
    public Result checkDuplicate(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        Integer subjectId = (Integer) request.get("subjectId");
        Long currentId = request.get("currentId") != null ? Long.valueOf(request.get("currentId").toString()) : null;
        if (!StringUtils.hasText(content) || subjectId == null) {
            return Result.suc(Collections.emptyList());
        }
        List<BizQuestion> candidates = questionService.lambdaQuery()
                .eq(BizQuestion::getSubjectId, subjectId)
                .ne(currentId != null, BizQuestion::getId, currentId)
                .select(BizQuestion::getId, BizQuestion::getContent)
                .list();
        // 【修改点】使用 getDefaultInstance() 替代 new LevenshteinDistance()
        LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();
        List<BizQuestion> similarQuestions = candidates.stream()
                .filter(q -> {
                    double similarity = 1.0 - (double) distance.apply(content, q.getContent()) / Math.max(content.length(), q.getContent().length());
                    return similarity > 0.8;
                })
                .collect(Collectors.toList());
        return Result.suc(similarQuestions);
    }

    @GetMapping("/export")
    @Log(title = "题库管理", businessType = BusinessType.EXPORT) // 导出
    public void exportQuestions(HttpServletResponse response, QuestionPageParams params) throws IOException {
        List<QuestionExcelDTO> list = questionService.getQuestionsForExport(params);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("题库试题", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), QuestionExcelDTO.class).sheet("试题列表").doWrite(list);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:question:list')") // 这里您之前的代码可能没有加权限，建议加上
    @Log(title = "题库管理", businessType = BusinessType.UPDATE) // 修改
    public Result updateQuestion(@PathVariable Long id, @RequestBody QuestionDTO questionDTO) {
        questionDTO.setId(id);
        boolean success = questionService.updateQuestionWithKnowledgePoints(questionDTO);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    @Log(title = "题库管理", businessType = BusinessType.DELETE) // 删除
    public Result deleteQuestion(@PathVariable Long id) {
        boolean success = questionService.removeById(id);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @PostMapping("/import")
    @Log(title = "题库管理", businessType = BusinessType.IMPORT) // 导入
    public Result importQuestions(@RequestParam("file") MultipartFile file) {
        try {
            questionService.importQuestions(file);
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
            return Result.suc("导入成功");
        } catch (Exception e) {
            return Result.fail("导入失败: " + e.getMessage());
        }
    }

    @PutMapping("/batch-update")
    @PreAuthorize("hasAuthority('sys:question:list')")
    @Log(title = "题库管理", businessType = BusinessType.UPDATE) // 批量修改
    public Result batchUpdateQuestions(@RequestBody QuestionBatchUpdateDTO dto) {
        boolean success = questionService.batchUpdateQuestions(dto);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc("批量更新成功") : Result.fail("批量更新失败");
    }
    /**
     * 【新增】AI 智能生成题目
     */
    @PostMapping("/ai-generate")
    @Log(title = "题库管理", businessType = BusinessType.OTHER)
    public Result generateQuestions(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String text = (String) params.get("text");
        Integer count = (Integer) params.get("count");
        Integer type = (Integer) params.get("type"); // 1,2,3,4,0

        // 从 Header 获取 Key (因为管理员/教师也需要在前端配置 Key，或者你可以写死系统 Key)
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            // 如果 Header 没传，可以尝试从配置文件或数据库读取系统默认 Key
            // 这里为了演示，直接报错
            return Result.fail("请在设置中配置 AI API Key");
        }

        try {
            List<AiGeneratedQuestionDTO> questions = aiService.generateQuestionsFromText(
                    apiKey, provider, text,
                    count == null ? 5 : count,
                    type == null ? 1 : type
            );
            return Result.suc(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("生成失败: " + e.getMessage());
        }
    }
    /**
     * 【新增】批量删除试题
     */
    @DeleteMapping("/batch")
    @Log(title = "题库管理", businessType = BusinessType.DELETE)
    public Result batchDeleteQuestions(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("请选择要删除的试题");
        }
        boolean success = questionService.removeByIds(ids);
        if (success) {
            // 清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc("批量删除成功") : Result.fail("删除失败");
    }
}