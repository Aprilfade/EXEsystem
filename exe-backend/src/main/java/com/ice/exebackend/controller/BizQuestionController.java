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

@RestController
@RequestMapping("/api/v1/questions")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result deleteQuestion(@PathVariable Long id) {
        // 安全起见，删除试题前也删除其与知识点的关联关系
    /*    questionService.updateQuestionWithKnowledgePoints(new QuestionDTO() {{
            setId(id);
        }});*/
        boolean success = questionService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }
}