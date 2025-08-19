package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// 【关键修复】移除无效的 import 别名语法
// import com.baomidou.mybatisplus.core.toolkit.StringUtils as MybatisStringUtils;
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
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    public Result createSubject(@RequestBody BizSubject subject) {
        boolean success = subjectService.save(subject);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @GetMapping
    public Result getSubjectList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String sortField,
                                 @RequestParam(required = false) String sortOrder) {
        Page<BizSubject> pageRequest = new Page<>(current, size);
        QueryWrapper<BizSubject> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name).or().like("description", name);
        }
        pageRequest.setOptimizeCountSql(false);

        // 动态排序逻辑
        if (StringUtils.hasText(sortField) && StringUtils.hasText(sortOrder)) {
            // 【关键修复1】使用MyBatis-Plus自带的StringUtils的全限定名进行调用
            String dbColumn = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(sortField);

            // 【关键修复2】白名单校验：只允许对数据库中真实存在的列进行排序
            if ("grade".equals(dbColumn) || "create_time".equals(dbColumn)) {
                if ("asc".equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByAsc(dbColumn);
                } else {
                    queryWrapper.orderByDesc(dbColumn);
                }
            } else {
                // 如果传入了非法的排序列名，则使用默认排序，防止SQL注入和报错
                queryWrapper.orderByDesc("create_time");
            }
        } else {
            // 如果前端没有传递排序参数，则使用默认排序
            queryWrapper.orderByDesc("create_time");
        }

        Page<SubjectStatsDTO> statsPage = subjectService.getSubjectStatsPage(pageRequest, queryWrapper);
        return Result.suc(statsPage.getRecords(), statsPage.getTotal());
    }

    @GetMapping("/all")
    public Result getAllSubjects() {
        List<BizSubject> list = subjectService.list();
        return Result.suc(list);
    }

    @PutMapping("/{id}")
    public Result updateSubject(@PathVariable Long id, @RequestBody BizSubject subject) {
        subject.setId(id);
        boolean success = subjectService.updateById(subject);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

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
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @GetMapping("/{id}/questions")
    public Result getQuestionsForSubject(@PathVariable Long id) {
        List<BizQuestion> questions = subjectService.getQuestionsForSubject(id);
        return Result.suc(questions);
    }

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