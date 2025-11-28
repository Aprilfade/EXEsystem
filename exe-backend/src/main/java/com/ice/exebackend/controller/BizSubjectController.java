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

    // 【新增】定义获取所有科目的缓存 Key
    private static final String SUBJECT_ALL_CACHE_KEY = "sys:subject:all";

    @PostMapping
    public Result createSubject(@RequestBody BizSubject subject) {
        boolean success = subjectService.save(subject);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
            redisTemplate.delete(SUBJECT_ALL_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @GetMapping
    public Result getSubjectList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String grade, // 【新增】接收年级参数
                                 @RequestParam(required = false) String sortField,
                                 @RequestParam(required = false) String sortOrder) {
        Page<BizSubject> pageRequest = new Page<>(current, size);
        QueryWrapper<BizSubject> queryWrapper = new QueryWrapper<>();

        // 1. 名称模糊搜索 (同时匹配名称或简介)
        if (StringUtils.hasText(name)) {
            queryWrapper.and(w -> w.like("name", name).or().like("description", name));
        }

        // 2. 【新增】年级精确筛选
        if (StringUtils.hasText(grade)) {
            queryWrapper.eq("grade", grade);
        }

        pageRequest.setOptimizeCountSql(false);

        // 3. 排序逻辑 (保持原有白名单校验)
        if (StringUtils.hasText(sortField) && StringUtils.hasText(sortOrder)) {
            String dbColumn = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(sortField);
            if ("grade".equals(dbColumn) || "create_time".equals(dbColumn)) {
                if ("asc".equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByAsc(dbColumn);
                } else {
                    queryWrapper.orderByDesc(dbColumn);
                }
            } else {
                queryWrapper.orderByDesc("create_time");
            }
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        // 使用之前优化过的 Service 方法获取统计数据
        Page<SubjectStatsDTO> statsPage = subjectService.getSubjectStatsPage(pageRequest, queryWrapper);
        return Result.suc(statsPage.getRecords(), statsPage.getTotal());
    }

    @GetMapping("/all")
    public Result getAllSubjects() {
        // 1. 先尝试从 Redis 缓存中获取数据
        try {
            List<BizSubject> cachedList = (List<BizSubject>) redisTemplate.opsForValue().get(SUBJECT_ALL_CACHE_KEY);
            if (cachedList != null && !cachedList.isEmpty()) {
                // 如果缓存命中，直接返回缓存数据
                return Result.suc(cachedList);
            }
        } catch (Exception e) {
            // 缓存读取异常不应影响主业务，记录日志即可（此处省略日志）
        }

        // 2. 缓存未命中，查询数据库
        List<BizSubject> list = subjectService.list();

        // 3. 将查询结果写入 Redis 缓存
        // 设置过期时间为 1 天，防止数据永久不一致
        if (list != null && !list.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(SUBJECT_ALL_CACHE_KEY, list, 1, java.util.concurrent.TimeUnit.DAYS);
            } catch (Exception e) {
                // 缓存写入异常忽略
            }
        }

        return Result.suc(list);
    }

    @PutMapping("/{id}")
    public Result updateSubject(@PathVariable Long id, @RequestBody BizSubject subject) {
        subject.setId(id);
        boolean success = subjectService.updateById(subject);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
            redisTemplate.delete(SUBJECT_ALL_CACHE_KEY);
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
            redisTemplate.delete(SUBJECT_ALL_CACHE_KEY);
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