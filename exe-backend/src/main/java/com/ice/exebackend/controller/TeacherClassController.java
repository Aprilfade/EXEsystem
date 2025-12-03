package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classes")
public class TeacherClassController {

    @Autowired private BizClassMapper classMapper;
    @Autowired private BizHomeworkMapper homeworkMapper;
    @Autowired private BizClassStudentMapper classStudentMapper;
    @Autowired private BizStudentMapper studentMapper;

    // 1. 班级列表
    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')") // 暂时复用权限
    public Result listClasses() {
        return Result.suc(classMapper.selectList(new QueryWrapper<BizClass>().orderByDesc("create_time")));
    }

    // 2. 创建班级
    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result createClass(@RequestBody BizClass bizClass) {
        // 生成6位随机邀请码
        bizClass.setCode(String.valueOf(new Random().nextInt(900000) + 100000));
        bizClass.setTeacherId(1L); // 暂定为当前登录用户ID，需从SecurityContext获取
        bizClass.setCreateTime(LocalDateTime.now());
        classMapper.insert(bizClass);
        return Result.suc(bizClass);
    }

    // 3. 发布作业
    @PostMapping("/{classId}/homework")
    public Result assignHomework(@PathVariable Long classId, @RequestBody BizHomework homework) {
        homework.setClassId(classId);
        homework.setCreateTime(LocalDateTime.now());
        homeworkMapper.insert(homework);
        return Result.suc();
    }

    // 4. 获取班级学生列表
    @GetMapping("/{classId}/students")
    public Result getClassStudents(@PathVariable Long classId) {
        // 这里简化逻辑，实际应使用 JOIN 查询
        List<BizClassStudent> relations = classStudentMapper.selectList(new QueryWrapper<BizClassStudent>().eq("class_id", classId));
        if(relations.isEmpty()) return Result.suc(List.of());

        List<Long> studentIds = relations.stream().map(BizClassStudent::getStudentId).toList();
        List<BizStudent> students = studentMapper.selectBatchIds(studentIds);
        return Result.suc(students);
    }
}