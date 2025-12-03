package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.mapper.*;
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/classes")
public class TeacherClassController {

    @Autowired private BizClassMapper classMapper;
    @Autowired private BizHomeworkMapper homeworkMapper;
    @Autowired private BizClassStudentMapper classStudentMapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private SysUserService sysUserService;

    // 辅助方法：获取当前登录教师ID
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
        return user != null ? user.getId() : null;
    }

    // 1. 班级列表
    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result listClasses() {
        return Result.suc(classMapper.selectList(new QueryWrapper<BizClass>().orderByDesc("create_time")));
    }

    // 2. 创建班级
    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result createClass(@RequestBody BizClass bizClass) {
        String code;
        do {
            code = String.valueOf(new Random().nextInt(900000) + 100000);
        } while (classMapper.selectCount(new QueryWrapper<BizClass>().eq("code", code)) > 0);

        bizClass.setCode(code);
        bizClass.setTeacherId(getCurrentUserId());
        bizClass.setCreateTime(LocalDateTime.now());
        classMapper.insert(bizClass);
        return Result.suc(bizClass);
    }

    // 3. 发布作业
    @PostMapping("/{classId}/homework")
    public Result assignHomework(@PathVariable Long classId, @RequestBody BizHomework homework) {
        // 【修复】这里之前报错 setClassId/setCreateTime 找不到，现在 BizHomework 加了 Setter 后正常
        homework.setClassId(classId);
        homework.setCreateTime(LocalDateTime.now());
        homeworkMapper.insert(homework);
        return Result.suc();
    }

    // 4. 获取班级学生列表
    @GetMapping("/{classId}/students")
    public Result getClassStudents(@PathVariable Long classId) {
        List<BizClassStudent> relations = classStudentMapper.selectList(
                new QueryWrapper<BizClassStudent>().eq("class_id", classId)
        );

        if (relations.isEmpty()) {
            return Result.suc(Collections.emptyList());
        }

        List<Long> studentIds = relations.stream()
                .map(BizClassStudent::getStudentId)
                .collect(Collectors.toList());

        // 【修复】替换 selectBatchIds 为 selectList + in 查询
        List<BizStudent> students = studentMapper.selectList(
                new QueryWrapper<BizStudent>().in("id", studentIds)
        );
        return Result.suc(students);
    }
}