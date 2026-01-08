package com.ice.exebackend.controller;

import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.mapper.*;
import com.ice.exebackend.service.BizClassService;
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师/管理员班级管理控制器
 */
@RestController
@RequestMapping("/api/v1/classes")
public class TeacherClassController {

    @Autowired
    private BizClassService classService;

    @Autowired
    private BizHomeworkMapper homeworkMapper;

    @Autowired
    private BizStudentMapper studentMapper;

    @Autowired
    private SysUserService sysUserService;

    // 辅助方法：获取当前登录教师ID
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
        return user != null ? user.getId() : null;
    }

    /**
     * 1. 获取班级列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sys:class:list')")
    @Log(title = "班级管理", businessType = BusinessType.OTHER)
    public Result listClasses() {
        List<BizClass> classList = classService.list();
        classList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return Result.suc(classList);
    }

    /**
     * 2. 创建班级
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:class:list')")
    @Log(title = "班级管理", businessType = BusinessType.INSERT)
    public Result createClass(@RequestBody BizClass bizClass) {
        bizClass.setTeacherId(getCurrentUserId());
        BizClass created = classService.createClass(bizClass);
        return Result.suc(created);
    }

    /**
     * 3. 编辑班级信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:class:edit')")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    public Result updateClass(@PathVariable Long id, @RequestBody BizClass bizClass) {
        boolean success = classService.updateClass(id, bizClass);
        return success ? Result.suc("更新成功") : Result.fail("班级不存在");
    }

    /**
     * 4. 删除班级
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:class:delete')")
    @Log(title = "班级管理", businessType = BusinessType.DELETE)
    public Result deleteClass(@PathVariable Long id) {
        String result = classService.deleteClass(id);
        if (result.equals("删除成功")) {
            return Result.suc(result);
        } else {
            return Result.fail(result);
        }
    }

    /**
     * 5. 重新生成邀请码
     */
    @PostMapping("/{id}/regenerate-code")
    @PreAuthorize("hasAuthority('sys:class:edit')")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    public Result regenerateCode(@PathVariable Long id) {
        try {
            String newCode = classService.regenerateCode(id);
            return Result.suc(newCode);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 6. 从班级中移除学生
     */
    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasAuthority('sys:class:edit')")
    @Log(title = "班级管理", businessType = BusinessType.DELETE)
    public Result removeStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        boolean success = classService.removeStudent(classId, studentId);
        return success ? Result.suc("移除成功") : Result.fail("移除失败");
    }

    /**
     * 7. 获取班级学生列表
     */
    @GetMapping("/{classId}/students")
    @PreAuthorize("hasAuthority('sys:class:list')")
    public Result getClassStudents(@PathVariable Long classId) {
        List<Long> studentIds = classService.getClassStudentIds(classId);
        if (studentIds.isEmpty()) {
            return Result.suc(List.of());
        }

        List<BizStudent> students = studentMapper.selectBatchIds(studentIds);
        return Result.suc(students);
    }

    /**
     * 8. 发布作业
     */
    @PostMapping("/{classId}/homework")
    @PreAuthorize("hasAuthority('sys:class:edit')")
    @Log(title = "班级管理", businessType = BusinessType.INSERT)
    public Result assignHomework(@PathVariable Long classId, @RequestBody BizHomework homework) {
        homework.setClassId(classId);
        homework.setCreateTime(LocalDateTime.now());
        homeworkMapper.insert(homework);
        return Result.suc("作业发布成功");
    }
}
