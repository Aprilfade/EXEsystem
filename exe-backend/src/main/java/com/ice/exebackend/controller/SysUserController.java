package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result; // 导入您定义的Result类
import com.ice.exebackend.dto.UserInfoDTO;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result createUser(@RequestBody SysUser user) {
        boolean success = sysUserService.createUser(user);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result getUserList(@RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> page = new Page<>(current, size);
        IPage<UserInfoDTO> userPage = sysUserService.getUserPage(page);
        // 使用suc(data, total)方法返回分页结果
        return Result.suc(userPage.getRecords(), userPage.getTotal());
    }

    /**
     * 获取单个用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        // 使用suc(data)方法返回单个对象
        return Result.suc(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        // 修改前: boolean success = sysUserService.updateById(user);
        // 修改后:
        boolean success = sysUserService.updateUserAndRoles(user);
        return success ? Result.suc() : Result.fail();
    }
    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result deleteUser(@PathVariable Long id) {
        boolean success = sysUserService.deleteUserById(id);
        return success ? Result.suc() : Result.fail();
    }
    /**
     * 当前登录用户更新自己的个人信息
     */
    @PutMapping("/me")
    public Result updateMyProfile(@RequestBody SysUser user) {
        // 从安全上下文中获取当前登录用户的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 根据用户名从数据库找到完整的用户信息
        SysUser currentUser = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
        if (currentUser == null) {
            return Result.fail(); // 理论上，如果token有效，这里不会失败
        }

        // 关键：将从前端传来的数据中的ID强制设置为当前登录用户的ID
        // 这样可以防止恶意用户通过伪造请求体来修改其他人的信息
        user.setId(currentUser.getId());

        // 调用我们新创建的、更安全的服务方法
        boolean success = sysUserService.updateUserProfile(user);
        return success ? Result.suc() : Result.fail();
    }

}