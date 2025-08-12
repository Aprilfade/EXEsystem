package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.QuestionDTO;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.service.BizQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.text.similarity.LevenshteinDistance; // 需要添加依赖

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/questions")
@PreAuthorize("hasAuthority('sys:question:list')") // 类级别权限
public class BizQuestionController {

    @Autowired
    private BizQuestionService questionService;

    /**
     * 新增试题
     */
    @PostMapping
    public Result createQuestion(@RequestBody QuestionDTO questionDTO) {
        boolean success = questionService.createQuestionWithKnowledgePoints(questionDTO);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 分页、条件查询试题列表
     */
    @GetMapping
    public Result getQuestionList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Long subjectId,
                                  @RequestParam(required = false) Integer questionType) {
        Page<BizQuestion> page = new Page<>(current, size);
        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        if (questionType != null) {
            queryWrapper.eq("question_type", questionType);
        }
        queryWrapper.orderByDesc("id");

        questionService.page(page, queryWrapper);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 获取单个试题详情（包含知识点）
     */
    @GetMapping("/{id}")
    public Result getQuestionById(@PathVariable Long id) {
        QuestionDTO questionDTO = questionService.getQuestionWithKnowledgePointsById(id);
        return Result.suc(questionDTO);
    }


    /**
     * 更新试题信息
     */
    @PutMapping("/{id}")
    public Result updateQuestion(@PathVariable Long id, @RequestBody QuestionDTO questionDTO) {
        questionDTO.setId(id);
        boolean success = questionService.updateQuestionWithKnowledgePoints(questionDTO);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 删除试题
     * 权限：仅限管理员
     */
    @DeleteMapping("/{id}")
    public Result deleteQuestion(@PathVariable Long id) {
        // 安全起见，删除试题前也删除其与知识点的关联关系
    /*    questionService.updateQuestionWithKnowledgePoints(new QuestionDTO() {{
            setId(id);
        }});*/
        boolean success = questionService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }
    /**
     * 检查题目内容是否与现有题目重复
     * @param request 包含 content, subjectId, currentId (编辑时排除自身)
     * @return 相似的题目列表
     */
    @PostMapping("/check-duplicate")
    public Result checkDuplicate(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        Integer subjectId = (Integer) request.get("subjectId");
        Long currentId = request.get("currentId") != null ? Long.valueOf(request.get("currentId").toString()) : null;

        if (!StringUtils.hasText(content) || subjectId == null) {
            return Result.suc(Collections.emptyList());
        }

        // 简单的基于编辑距离的查重逻辑
        List<BizQuestion> candidates = questionService.lambdaQuery()
                .eq(BizQuestion::getSubjectId, subjectId)
                .ne(currentId != null, BizQuestion::getId, currentId) // 编辑时排除自己
                .select(BizQuestion::getId, BizQuestion::getContent)
                .list();

        LevenshteinDistance distance = new LevenshteinDistance();
        List<BizQuestion> similarQuestions = candidates.stream()
                .filter(q -> {
                    // 计算两个字符串的相似度（1 - 编辑距离 / 较长字符串长度）
                    double similarity = 1.0 - (double) distance.apply(content, q.getContent()) / Math.max(content.length(), q.getContent().length());
                    return similarity > 0.8; // 相似度阈值设为 80%
                })
                .collect(Collectors.toList());

        return Result.suc(similarQuestions);
    }

}