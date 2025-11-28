package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizCourse;
import com.ice.exebackend.service.BizCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student/courses")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentCourseController {

    @Autowired
    private BizCourseService courseService;

    /**
     * 学生获取课程列表
     */
    @GetMapping
    public Result list(@RequestParam(defaultValue = "1") int current,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String grade,
                       @RequestParam(required = false) Long subjectId) {

        Page<BizCourse> page = new Page<>(current, size);
        QueryWrapper<BizCourse> query = new QueryWrapper<>();

        if (StringUtils.hasText(name)) query.like("name", name);
        if (StringUtils.hasText(grade)) query.eq("grade", grade);
        if (subjectId != null) query.eq("subject_id", subjectId);

        query.orderByDesc("create_time");
        return Result.suc(courseService.page(page, query));
    }

    /**
     * 学生获取课程详情（含资源）
     */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        // 复用 Service 中已有的获取详情方法
        return Result.suc(courseService.getCourseWithResources(id));
    }
}