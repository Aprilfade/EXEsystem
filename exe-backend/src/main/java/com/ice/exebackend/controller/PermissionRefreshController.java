package com.ice.exebackend.controller;

import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.PermissionCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限刷新管理Controller
 * 提供权限缓存的手动刷新功能，用于管理员修改权限后立即生效
 */
@Tag(name = "权限刷新管理", description = "管理员刷新用户权限缓存")
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionRefreshController {

    @Autowired
    private PermissionCacheService permissionCacheService;

    /**
     * 刷新单个用户的权限缓存
     * 使用场景：修改用户角色后，需要立即刷新该用户的权限
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "刷新单个用户权限")
    @PostMapping("/refresh/{userId}")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @Log(title = "权限刷新", businessType = BusinessType.UPDATE)
    public Result refreshUserPermission(@PathVariable Long userId) {
        permissionCacheService.clearUserPermissions(userId);
        return Result.suc("权限刷新成功");
    }

    /**
     * 批量刷新多个用户的权限缓存
     * 使用场景：批量修改用户角色后，需要刷新所有相关用户的权限
     *
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量刷新用户权限")
    @PostMapping("/refresh/batch")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @Log(title = "权限刷新", businessType = BusinessType.UPDATE)
    public Result refreshUsersPermissions(@RequestBody List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.fail("用户ID列表不能为空");
        }
        permissionCacheService.clearUsersPermissions(userIds);
        return Result.suc("批量刷新成功，共刷新 " + userIds.size() + " 个用户");
    }
}
