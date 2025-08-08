package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.SubjectStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizQuestion; // 1. 导入 BizQuestion 实体
import com.ice.exebackend.entity.BizSubject;
import com.ice.exebackend.service.BizKnowledgePointService;
import com.ice.exebackend.service.BizQuestionService; // 2. 导入 BizQuestionService
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

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    // 3. 注入 BizQuestionService
    @Autowired
    private BizQuestionService questionService;


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
     * 分页获取科目列表 (已更新为包含统计数据)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result getSubjectList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size) {
        // 2. 使用新的 service 方法
        Page<BizSubject> pageRequest = new Page<>(current, size);
        Page<SubjectStatsDTO> statsPage = subjectService.getSubjectStatsPage(pageRequest);
        return Result.suc(statsPage.getRecords(), statsPage.getTotal());
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
     * 删除科目 (已加入完整的安全删除检查)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result deleteSubject(@PathVariable Long id) {
        // 4. 安全删除检查: 检查该科目下是否有知识点
        long knowledgePointCount = knowledgePointService.count(new QueryWrapper<BizKnowledgePoint>().eq("subject_id", id));
        if (knowledgePointCount > 0) {
            return Result.fail("无法删除：该科目下还存在 " + knowledgePointCount + " 个关联的知识点。");
        }

        // 5. 安全删除检查: 检查该科目下是否有试题
        long questionCount = questionService.count(new QueryWrapper<BizQuestion>().eq("subject_id", id));
        if (questionCount > 0) {
            return Result.fail("无法删除：该科目下还存在 " + questionCount + " 个关联的试题。");
        }

        // 只有当所有检查都通过时，才执行删除
        boolean success = subjectService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }
}