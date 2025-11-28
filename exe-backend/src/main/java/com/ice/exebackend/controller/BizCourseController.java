package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizCourse;
import com.ice.exebackend.entity.BizCourseResource;
import com.ice.exebackend.service.BizCourseService; // 假设你已创建接口
import com.ice.exebackend.mapper.BizCourseResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
public class BizCourseController {

    @Autowired
    private BizCourseService courseService;

    @Autowired
    private BizCourseResourceMapper resourceMapper;

    // 获取课程列表 (支持分页和搜索)
    @GetMapping
    public Result list(@RequestParam(defaultValue = "1") int current,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String grade) {
        Page<BizCourse> page = new Page<>(current, size);
        QueryWrapper<BizCourse> query = new QueryWrapper<>();
        if (StringUtils.hasText(name)) query.like("name", name);
        if (StringUtils.hasText(grade)) query.eq("grade", grade);
        query.orderByDesc("create_time");
        return Result.suc(courseService.page(page, query));
    }

    // 获取课程详情（包含资源列表）
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.suc(courseService.getCourseWithResources(id));
    }

    // 创建/更新课程
    @PostMapping
    @PreAuthorize("hasAuthority('sys:course:edit')") // 需在数据库添加相应权限
    public Result save(@RequestBody BizCourse course) {
        courseService.saveOrUpdate(course);
        return Result.suc(course.getId());
    }

    // 添加/更新资源
    @PostMapping("/resource")
    @PreAuthorize("hasAuthority('sys:course:edit')")
    public Result saveResource(@RequestBody BizCourseResource resource) {
        courseService.saveResource(resource);
        return Result.suc();
    }

    // 删除资源
    @DeleteMapping("/resource/{id}")
    @PreAuthorize("hasAuthority('sys:course:edit')")
    public Result deleteResource(@PathVariable Long id) {
        resourceMapper.deleteById(id);
        return Result.suc();
    }

    // 删除课程
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:course:edit')")
    public Result deleteCourse(@PathVariable Long id) {
        courseService.removeById(id);
        resourceMapper.delete(new QueryWrapper<BizCourseResource>().eq("course_id", id));
        return Result.suc();
    }
}