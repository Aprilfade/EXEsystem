package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.SubjectStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizPaper;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizSubject;
import com.ice.exebackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // 1. 导入 RedisTemplate
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/subjects")
@PreAuthorize("hasAuthority('sys:subject:list')")
public class BizSubjectController {

    @Autowired
    private BizSubjectService subjectService;

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    @Autowired
    private BizQuestionService questionService;

    @Autowired
    private BizStudentService studentService;

    @Autowired
    private BizPaperService paperService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 3. 定义缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    /**
     * 新增科目
     */
    @PostMapping
    public Result createSubject(@RequestBody BizSubject subject) {
        boolean success = subjectService.save(subject);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @GetMapping
    public Result getSubjectList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String name) {
        Page<BizSubject> pageRequest = new Page<>(current, size);
        QueryWrapper<BizSubject> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name).or().like("description", name);
        }
        pageRequest.setOptimizeCountSql(false);
        Page<SubjectStatsDTO> statsPage = subjectService.getSubjectStatsPage(pageRequest, queryWrapper);
        return Result.suc(statsPage.getRecords(), statsPage.getTotal());
    }

    /**
     * 获取所有科目列表（不分页，用于下拉框）
     */
    @GetMapping("/all")
    public Result getAllSubjects() {
        List<BizSubject> list = subjectService.list();
        return Result.suc(list);
    }

    /**
     * 更新科目信息
     */
    @PutMapping("/{id}")
    public Result updateSubject(@PathVariable Long id, @RequestBody BizSubject subject) {
        subject.setId(id);
        boolean success = subjectService.updateById(subject);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 删除科目 (已加入完整的安全删除检查)
     */
    @DeleteMapping("/{id}")
    public Result deleteSubject(@PathVariable Long id) {
        long knowledgePointCount = knowledgePointService.count(new QueryWrapper<BizKnowledgePoint>().eq("subject_id", id));
        if (knowledgePointCount > 0) {
            return Result.fail("无法删除：该科目下还存在 " + knowledgePointCount + " 个关联的知识点。");
        }
        long questionCount = questionService.count(new QueryWrapper<BizQuestion>().eq("subject_id", id));
        if (questionCount > 0) {
            return Result.fail("无法删除：该科目下还存在 " + questionCount + " 个关联的试题。");
        }
        long studentCount = studentService.count(new QueryWrapper<BizStudent>().eq("subject_id", id));
        if (studentCount > 0) {
            return Result.fail("无法删除：该科目下还存在 " + studentCount + " 个关联的学生。");
        }
        long paperCount = paperService.count(new QueryWrapper<BizPaper>().eq("subject_id", id));
        if (paperCount > 0) {
            return Result.fail("无法删除：该科目下还存在 " + paperCount + " 个关联的试卷。");
        }
        boolean success = subjectService.removeById(id);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 【新增】根据科目ID获取其关联的试题列表 (已按年级智能筛选)
     */
    @GetMapping("/{id}/questions")
    public Result getQuestionsForSubject(@PathVariable Long id) {
        List<BizQuestion> questions = subjectService.getQuestionsForSubject(id);
        return Result.suc(questions);
    }

    /**
     * 【新增】获取所有科目中不重复的年级列表
     */
    @GetMapping("/grades")
    public Result getDistinctGrades() {
        QueryWrapper<BizSubject> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT grade");
        List<BizSubject> subjectsWithGrades = subjectService.list(queryWrapper);
        List<String> grades = subjectsWithGrades.stream()
                .map(BizSubject::getGrade)
                .filter(grade -> grade != null && !grade.trim().isEmpty())
                .sorted()
                .collect(Collectors.toList());
        return Result.suc(grades);
    }
}