package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.SubjectStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizQuestion; // 1. 导入 BizQuestion 实体
import com.ice.exebackend.entity.BizSubject;
import com.ice.exebackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.ice.exebackend.entity.BizStudent; // 新增导入
import com.ice.exebackend.entity.BizPaper;   // 新增导入
import com.ice.exebackend.service.BizStudentService; // 新增导入
import com.ice.exebackend.service.BizPaperService;   // 新增导入


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

    // 【新增注入】
    @Autowired
    private BizStudentService studentService;

    @Autowired
    private BizPaperService paperService;



    /**
     * 新增科目
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result createSubject(@RequestBody BizSubject subject) {
        boolean success = subjectService.save(subject);
        return success ? Result.suc() : Result.fail();
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result getSubjectList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 // 【新增】接收搜索参数
                                 @RequestParam(required = false) String name) {
        Page<BizSubject> pageRequest = new Page<>(current, size);

        // 【新增】构建带搜索条件的查询
        QueryWrapper<BizSubject> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name).or().like("description", name);
        }
        pageRequest.setOptimizeCountSql(false); // 优化分页查询

        Page<SubjectStatsDTO> statsPage = subjectService.getSubjectStatsPage(pageRequest, queryWrapper); // 将查询条件传入

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

        // 【新增】安全删除检查: 检查该科目下是否有学生
        long studentCount = studentService.count(new QueryWrapper<BizStudent>().eq("subject_id", id));
        if (studentCount > 0) {
            return Result.fail("无法删除：该科目下还存在 " + studentCount + " 个关联的学生。");
        }

        // 【新增】安全删除检查: 检查该科目下是否有试卷
        long paperCount = paperService.count(new QueryWrapper<BizPaper>().eq("subject_id", id));
        if (paperCount > 0) {
            return Result.fail("无法删除：该科目下还存在 " + paperCount + " 个关联的试卷。");
        }

        // 只有当所有检查都通过时，才执行删除
        boolean success = subjectService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }
}