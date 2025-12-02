package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysOperLog;
import com.ice.exebackend.service.SysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logs/operation")
public class SysOperLogController {

    @Autowired
    private SysOperLogService operLogService;

    /**
     * 分页查询操作日志
     * 权限标识建议新建：sys:log:oper，或者暂时复用 sys:user:list
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sys:log:oper') or hasRole('SUPER_ADMIN')")
    public Result getOperLogList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String title,
                                 @RequestParam(required = false) String operName,
                                 @RequestParam(required = false) Integer status,
                                 @RequestParam(required = false) Integer businessType) {
        Page<SysOperLog> page = new Page<>(current, size);
        QueryWrapper<SysOperLog> query = new QueryWrapper<>();

        if (StringUtils.hasText(title)) {
            query.like("title", title);
        }
        if (StringUtils.hasText(operName)) {
            query.like("oper_name", operName);
        }
        if (status != null) {
            query.eq("status", status);
        }
        if (businessType != null) {
            query.eq("business_type", businessType);
        }

        query.orderByDesc("oper_time"); // 按时间倒序

        operLogService.page(page, query);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 删除日志 (仅限超级管理员)
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result deleteOperLog(@PathVariable Long[] ids) {
        operLogService.removeByIds(Arrays.asList(ids));
        return Result.suc();
    }

    /**
     * 清空日志 (仅限超级管理员)
     */
    @DeleteMapping("/clean")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result cleanOperLog() {
        operLogService.remove(new QueryWrapper<>());
        return Result.suc();
    }
}