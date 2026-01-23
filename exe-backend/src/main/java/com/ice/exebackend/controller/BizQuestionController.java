package com.ice.exebackend.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.service.BizQuestionService;
import com.ice.exebackend.utils.BatchOperationValidator;  // 新增
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
// 必须导入这两个，且不要导入 lombok.Log
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.annotation.AuditLog;  // 【新增】审计日志注解
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.AiServiceV3; // 【修改】导入 AiServiceV3
import com.ice.exebackend.dto.AiGeneratedQuestionDTO; // 导入 DTO
import jakarta.servlet.http.HttpServletRequest; // 导入 HttpServletRequest
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.service.SysUserService;
import org.springframework.security.core.context.SecurityContextHolder;




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

    // 【修改】注入 AiServiceV3
    @Autowired
    private AiServiceV3 aiServiceV3;

    // 注入 SysUserService
    @Autowired
    private SysUserService sysUserService;

    // 3. 定义缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @PreAuthorize("hasAuthority('sys:question:create')") // ✅ 修改为细粒度权限
    @Log(title = "题库管理", businessType = BusinessType.INSERT) // 新增
    public Result createQuestion(@RequestBody QuestionDTO questionDTO) {
        boolean success = questionService.createQuestionWithKnowledgePoints(questionDTO);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
            // 返回创建的题目ID
            return Result.suc(questionDTO.getId());
        }
        return Result.fail();
    }

    // ... [此处省略所有 GET 查询和查重方法，它们不需要修改] ...
    @GetMapping
    public Result getQuestionList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Long subjectId,
                                  @RequestParam(required = false) Integer questionType,
                                  @RequestParam(required = false) String grade,
                                  @RequestParam(required = false) String content) {  // ✅ 新增题干内容搜索参数

        logger.info("开始查询试题列表... subjectId: {}, grade: {}, questionType: {}, content: {}", subjectId, grade, questionType, content);
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
        // ✅ 新增：题干内容模糊搜索
        if (StringUtils.hasText(content)) {
            queryWrapper.like("content", content);
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
    @PreAuthorize("hasAuthority('sys:question:list')") // ✅ 添加权限控制
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
    @PreAuthorize("hasAuthority('sys:question:update')") // ✅ 修改为细粒度权限
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
    @PreAuthorize("hasAuthority('sys:question:delete')") // ✅ 添加细粒度权限
    @Log(title = "题库管理", businessType = BusinessType.DELETE) // 删除
    @AuditLog(module = "题目管理", operationType = AuditLog.OperationType.DELETE, description = "删除题目")  // 【新增】审计日志
    public Result deleteQuestion(@PathVariable Long id) {
        try {
            boolean success = questionService.deleteQuestionWithCascade(id);
            if (success) {
                // 4. 操作成功后，清除缓存
                redisTemplate.delete(DASHBOARD_CACHE_KEY);
                return Result.suc();
            }
            return Result.fail("删除失败");
        } catch (RuntimeException e) {
            // 捕获级联删除抛出的业务异常（如题目被试卷使用）
            logger.warn("删除题目失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
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
    @PreAuthorize("hasAuthority('sys:question:update')") // ✅ 修改为细粒度权限
    @Log(title = "题库管理", businessType = BusinessType.UPDATE) // 批量修改
    @AuditLog(module = "题目管理", operationType = AuditLog.OperationType.UPDATE, description = "批量更新题目")  // 【新增】审计日志
    public Result batchUpdateQuestions(@RequestBody QuestionBatchUpdateDTO dto) {
        // 【安全检查】批量操作数量限制
        String validationError = BatchOperationValidator.validateBatchUpdate(dto.getQuestionIds());
        if (validationError != null) {
            return Result.fail(validationError);
        }

        boolean success = questionService.batchUpdateQuestions(dto);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc("批量更新成功") : Result.fail("批量更新失败");
    }
    /**
     * 【修改】AI 智能生成题目 - 使用AiServiceV3
     */
    @PostMapping("/ai-generate")
    @Log(title = "题库管理", businessType = BusinessType.OTHER)
    public Result generateQuestions(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String text = (String) params.get("text");
        Integer count = (Integer) params.get("count");
        Integer type = (Integer) params.get("type"); // 1,2,3,4,5,0

        // 从 Header 获取 Key
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("请在设置中配置 AI API Key");
        }

        // 获取当前登录用户ID
        Long userId = null;
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
            if (user != null) {
                userId = user.getId();
            }
        } catch (Exception e) {
            logger.warn("获取用户ID失败，将使用匿名模式", e);
        }

        try {
            // 使用 AiServiceV3，支持缓存、限流和日志记录
            List<AiGeneratedQuestionDTO> questions = aiServiceV3.generateQuestionsFromText(
                    apiKey, provider, text,
                    count == null ? 5 : count,
                    type == null ? 1 : type,
                    userId
            );
            return Result.suc(questions);
        } catch (Exception e) {
            logger.error("AI生成题目失败", e);
            return Result.fail("生成失败: " + e.getMessage());
        }
    }

    /**
     * 【新增】AI 智能生成题目（流式版本）
     */
    @PostMapping("/ai-generate-stream")
    @Log(title = "题库管理", businessType = BusinessType.OTHER)
    public SseEmitter generateQuestionsStream(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String text = (String) params.get("text");
        Integer count = (Integer) params.get("count");
        Integer type = (Integer) params.get("type"); // 1,2,3,4,5,0

        // 从 Header 获取 Key
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("请在设置中配置 AI API Key"));
            return emitter;
        }

        // 获取当前登录用户ID
        Long userId = null;
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
            if (user != null) {
                userId = user.getId();
            }
        } catch (Exception e) {
            logger.warn("获取用户ID失败，将使用匿名模式", e);
        }

        try {
            // 使用 AiServiceV3 的流式方法
            return aiServiceV3.generateQuestionsFromTextStream(
                    apiKey, provider, text,
                    count == null ? 5 : count,
                    type == null ? 1 : type,
                    userId
            );
        } catch (Exception e) {
            logger.error("AI生成题目（流式）失败", e);
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(e);
            return emitter;
        }
    }

    /**
     * 【新增】批量查询题目详情（用于试卷管理等场景）
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('sys:question:list')")
    public Result getQuestionsByIds(@RequestBody List<Long> ids) {
        // 【安全检查】批量操作数量限制
        String validationError = BatchOperationValidator.validateBatchQuery(ids);
        if (validationError != null) {
            return Result.fail(validationError);
        }

        List<BizQuestion> questions = questionService.listByIds(ids);
        return Result.suc(questions);
    }

    /**
     * 【新增】批量删除试题 - 使用级联删除
     */
    @DeleteMapping("/batch")
    @Log(title = "题库管理", businessType = BusinessType.DELETE)
    @AuditLog(module = "题目管理", operationType = AuditLog.OperationType.DELETE, description = "批量删除题目")  // 【新增】审计日志
    public Result batchDeleteQuestions(@RequestBody List<Long> ids) {
        // 【安全检查】批量操作数量限制
        String validationError = BatchOperationValidator.validateBatchDelete(ids);
        if (validationError != null) {
            return Result.fail(validationError);
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();

        // 逐个删除，确保级联处理和错误反馈
        for (Long id : ids) {
            try {
                boolean success = questionService.deleteQuestionWithCascade(id);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (RuntimeException e) {
                failCount++;
                if (errorMsg.length() > 0) {
                    errorMsg.append("; ");
                }
                errorMsg.append("题目ID ").append(id).append(": ").append(e.getMessage());
            }
        }

        // 清除缓存
        if (successCount > 0) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }

        if (failCount == 0) {
            return Result.suc("批量删除成功，共删除 " + successCount + " 道题目");
        } else if (successCount == 0) {
            return Result.fail("批量删除失败: " + errorMsg.toString());
        } else {
            return Result.fail("部分删除成功(" + successCount + "道)，部分失败(" + failCount + "道): " + errorMsg.toString());
        }
    }

    /**
     * 【新增】学生获取练习题目（公开接口，无需管理员权限）
     */
    @GetMapping("/practice")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getPracticeQuestions(@RequestParam(defaultValue = "1") int current,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) Long subjectId,
                                       @RequestParam(required = false) Integer questionType) {
        logger.info("学生获取练习题目: subjectId={}, questionType={}, size={}", subjectId, questionType, size);

        Page<BizQuestion> page = new Page<>(current, size);
        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();

        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        if (questionType != null) {
            queryWrapper.eq("question_type", questionType);
        }

        // 随机排序，让每次获取的题目不同
        queryWrapper.orderByAsc("RAND()");

        questionService.page(page, queryWrapper);

        logger.info("成功获取 {} 道练习题目", page.getRecords().size());
        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 【新增】学生获取题目详情（公开接口，无需管理员权限）
     */
    @GetMapping("/practice/{id}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getQuestionDetail(@PathVariable Long id) {
        logger.info("学生获取题目详情: questionId={}", id);

        BizQuestion question = questionService.getById(id);
        if (question == null) {
            logger.warn("题目不存在: questionId={}", id);
            return Result.fail("题目不存在");
        }

        logger.info("成功获取题目详情: questionId={}", id);
        return Result.suc(question);
    }
}