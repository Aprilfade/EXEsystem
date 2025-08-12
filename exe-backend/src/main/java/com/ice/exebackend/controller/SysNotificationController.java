package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysNotification;
import com.ice.exebackend.service.SysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/notifications")
public class SysNotificationController {

    @Autowired
    private SysNotificationService notificationService;

    @PostMapping
    //@CacheEvict(value = "dashboardStats", allEntries = true) // 2. 添加注解，清除名为 "dashboardStats" 的所有缓存
    public Result createNotification(@RequestBody SysNotification notification) {
        if (notification.getIsPublished() != null && notification.getIsPublished()) {
            notification.setPublishTime(LocalDateTime.now());
        }
        boolean success = notificationService.save(notification);
        return success ? Result.suc() : Result.fail();
    }

    @GetMapping
    public Result getNotificationList(@RequestParam(defaultValue = "1") int current,
                                      @RequestParam(defaultValue = "10") int size) {
        Page<SysNotification> page = new Page<>(current, size);
        notificationService.page(page, new QueryWrapper<SysNotification>().orderByDesc("create_time"));
        return Result.suc(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public Result getNotificationById(@PathVariable Long id) {
        return Result.suc(notificationService.getById(id));
    }

    @PutMapping("/{id}")
  //  @CacheEvict(value = "dashboardStats", allEntries = true) // 2. 添加注解，清除名为 "dashboardStats" 的所有缓存
    public Result updateNotification(@PathVariable Long id, @RequestBody SysNotification notification) {
        notification.setId(id);
        // 如果状态从“未发布”变为“发布”，则记录当前时间为发布时间
        SysNotification oldNotification = notificationService.getById(id);
        if (oldNotification != null && !oldNotification.getIsPublished() && notification.getIsPublished()) {
            notification.setPublishTime(LocalDateTime.now());
        }
        boolean success = notificationService.updateById(notification);
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
//@CacheEvict(value = "dashboardStats", allEntries = true) // 2. 添加注解，清除名为 "dashboardStats" 的所有缓存
    public Result deleteNotification(@PathVariable Long id) {
        boolean success = notificationService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }
}