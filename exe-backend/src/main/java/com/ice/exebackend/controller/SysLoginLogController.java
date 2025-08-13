// src/main/java/com/ice/exebackend/controller/SysLoginLogController.java
package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysLoginLog;
import com.ice.exebackend.service.SysLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs/login")
@PreAuthorize("hasAuthority('sys:log:login')") // 需要一个新的权限标识
public class SysLoginLogController {

    @Autowired
    private SysLoginLogService loginLogService;

    @GetMapping
    public Result getLoginLogList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String username) {
        Page<SysLoginLog> page = new Page<>(current, size);
        QueryWrapper<SysLoginLog> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(username)) {
            queryWrapper.like("username", username);
        }
        queryWrapper.orderByDesc("log_time");

        loginLogService.page(page, queryWrapper);
        return Result.suc(page.getRecords(), page.getTotal());
    }
}