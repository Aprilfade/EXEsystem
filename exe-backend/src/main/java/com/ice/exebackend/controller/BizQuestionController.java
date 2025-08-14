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
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/questions")
@PreAuthorize("hasAuthority('sys:question:list')")
public class BizQuestionController {

    private static final Logger logger = LoggerFactory.getLogger(BizQuestionController.class);

    @Autowired
    private BizQuestionService questionService;

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
                                  @RequestParam(required = false) Integer questionType,
                                  // **【最终修复点】**
                                  // 虽然之前就有这个参数，但我们要确保它在下面的逻辑中被正确使用了
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

        // **【最终修复点】**
        // **这里的代码是解决问题的关键。它确保了如果前端传递了 grade 参数，**
        // **那么数据库查询时一定会加上 AND grade = '五年级' 这个条件。**
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

    @PutMapping("/{id}")
    public Result updateQuestion(@PathVariable Long id, @RequestBody QuestionDTO questionDTO) {
        questionDTO.setId(id);
        boolean success = questionService.updateQuestionWithKnowledgePoints(questionDTO);
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    public Result deleteQuestion(@PathVariable Long id) {
        boolean success = questionService.removeById(id);
        return success ? Result.suc() : Result.fail();
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

        LevenshteinDistance distance = new LevenshteinDistance();
        List<BizQuestion> similarQuestions = candidates.stream()
                .filter(q -> {
                    double similarity = 1.0 - (double) distance.apply(content, q.getContent()) / Math.max(content.length(), q.getContent().length());
                    return similarity > 0.8;
                })
                .collect(Collectors.toList());

        return Result.suc(similarQuestions);
    }
}