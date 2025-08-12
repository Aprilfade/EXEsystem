package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*; // 确保导入 @PathVariable 和 @RequestBody

import java.util.List;
import java.util.Map; // 确保导入 Map

@RestController
@RequestMapping("/api/v1/roles")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 获取所有角色列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')") // 沿用成员管理的查看权限
    public Result getAllRoles() {
        List<SysRole> roles = sysRoleService.list();
        return Result.suc(roles);
    }

    // 【新增】获取单个角色的权限详情
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('sys:role:perm')") // 定义一个新的权限标识
    public Result getRolePermissions(@PathVariable Long id) {
        Map<String, Object> permissions = sysRoleService.getRolePermissions(id);
        return Result.suc(permissions);
    }

    // 【新增】更新角色的权限
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('sys:role:perm')") // 定义一个新的权限标识
    public Result updateRolePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        boolean success = sysRoleService.updateRolePermissions(id, permissionIds);
        return success ? Result.suc() : Result.fail("更新失败");
    }
}