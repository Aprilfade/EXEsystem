package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.entity.SysUserNotification;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.SysUserNotificationService;
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户通知控制器
 *
 * @author Claude Code Assistant
 * @since v3.06
 */
@RestController
@RequestMapping("/api/v1/user-notifications")
public class SysUserNotificationController {

    @Autowired
    private SysUserNotificationService notificationService;

    @Autowired
    private SysUserService userService;

    /**
     * 分页查询当前用户的通知
     */
    @GetMapping
    public Result getMyNotifications(@RequestParam(defaultValue = "1") Integer current,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) Integer isRead,
                                     Authentication authentication) {
        // 获取当前用户ID
        String username = authentication.getName();
        SysUser currentUser = userService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();

        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        Page<SysUserNotification> page = notificationService.getUserNotifications(
                currentUser.getId(), current, size, isRead);

        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result getUnreadCount(Authentication authentication) {
        String username = authentication.getName();
        SysUser currentUser = userService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();

        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        Integer count = notificationService.countUnread(currentUser.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("count", count);

        return Result.suc(data);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @Log(title = "通知管理", businessType = BusinessType.UPDATE)
    public Result markAsRead(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        SysUser currentUser = userService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();

        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        boolean success = notificationService.markAsRead(id, currentUser.getId());
        return success ? Result.suc("标记成功") : Result.fail("标记失败");
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @Log(title = "通知管理", businessType = BusinessType.UPDATE)
    public Result markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        SysUser currentUser = userService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();

        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        Integer count = notificationService.markAllAsRead(currentUser.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("count", count);

        return Result.suc(data);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @Log(title = "通知管理", businessType = BusinessType.DELETE)
    public Result deleteNotification(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        SysUser currentUser = userService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();

        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        boolean success = notificationService.deleteNotification(id, currentUser.getId());
        return success ? Result.suc("删除成功") : Result.fail("删除失败");
    }
}
