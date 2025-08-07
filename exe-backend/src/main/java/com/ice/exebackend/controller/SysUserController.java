package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result; // 导入您定义的Result类
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;

// 假设 SysUser 和 SysUserService 已经正确定义

@RestController
@RequestMapping("/api/v1/users") // 使用版本化的、名词复数的URI
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 新增用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:create')")
    public Result createUser(@RequestBody SysUser user) {
        boolean success = sysUserService.createUser(user);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result getUserList(@RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> page = new Page<>(current, size);
        IPage<SysUser> userPage = sysUserService.getUserPage(page);
        // 使用suc(data, total)方法返回分页结果
        return Result.suc(userPage.getRecords(), userPage.getTotal());
    }

    /**
     * 获取单个用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:read')")
    public Result getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        // 使用suc(data)方法返回单个对象
        return Result.suc(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        // 实际项目中，更复杂的更新逻辑应在Service层处理
        boolean success = sysUserService.updateById(user);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result deleteUser(@PathVariable Long id) {
        boolean success = sysUserService.deleteUserById(id);
        return success ? Result.suc() : Result.fail();
    }
}