package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizSubject;
import com.ice.exebackend.service.BizKnowledgePointService;
import com.ice.exebackend.service.BizSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subjects")
public class BizSubjectController {

    @Autowired
    private BizSubjectService subjectService;

    // 依赖 biz_question 和 biz_knowledge_point 的 Service (假设已存在)
    // @Autowired
    // private BizQuestionService questionService;
     @Autowired
    private BizKnowledgePointService knowledgePointService;

    /**
     * 新增科目
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result createSubject(@RequestBody BizSubject subject) {
        boolean success = subjectService.save(subject);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 分页获取科目列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result getSubjectList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size) {
        Page<BizSubject> page = new Page<>(current, size);
        subjectService.page(page, new QueryWrapper<BizSubject>().orderByDesc("create_time"));
        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 获取所有科目列表（不分页，用于下拉框）
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result getAllSubjects() {
        List<BizSubject> list = subjectService.list();
        return Result.suc(list);
    }

    /**
     * 更新科目信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result updateSubject(@PathVariable Long id, @RequestBody BizSubject subject) {
        subject.setId(id);
        boolean success = subjectService.updateById(subject);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 删除科目
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result deleteSubject(@PathVariable Long id) {
        // **安全删除检查**: 检查该科目下是否有试题或知识点
        // (此处暂时注释，需要您后续完成 Question 和 KnowledgePoint 的 Service)
        // long questionCount = questionService.count(new QueryWrapper<BizQuestion>().eq("subject_id", id));
        // if (questionCount > 0) {
        //     return Result.fail("无法删除：该科目下还存在关联的试题。");
        // }
        // 4. 安全删除检查: 检查该科目下是否有知识点
        long knowledgePointCount = knowledgePointService.count(new QueryWrapper<BizKnowledgePoint>().eq("subject_id", id));
        if (knowledgePointCount > 0) {
            // 如果存在关联的知识点，则返回失败信息，不允许删除
            return Result.fail("无法删除：该科目下还存在 " + knowledgePointCount + " 个关联的知识点。");
        }

        boolean success = subjectService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }
}