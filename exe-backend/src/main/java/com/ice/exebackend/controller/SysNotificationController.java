package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysNotification;
import com.ice.exebackend.service.SysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // 1. 导入 RedisTemplate
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/notifications")
public class SysNotificationController {

    @Autowired
    private SysNotificationService notificationService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 3. 定义与 Service 中一致的缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    public Result createNotification(@RequestBody SysNotification notification) {
        if (notification.getIsPublished() != null && notification.getIsPublished()) {
            notification.setPublishTime(LocalDateTime.now());
        }
        boolean success = notificationService.save(notification);
        if (success) {
            // 4. 当成功创建一个新通知后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
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
    public Result updateNotification(@PathVariable Long id, @RequestBody SysNotification notification) {
        notification.setId(id);
        SysNotification oldNotification = notificationService.getById(id);
        if (oldNotification != null && !oldNotification.getIsPublished() && notification.getIsPublished()) {
            notification.setPublishTime(LocalDateTime.now());
        }
        boolean success = notificationService.updateById(notification);
        if (success) {
            // 4. 成功更新通知后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    public Result deleteNotification(@PathVariable Long id) {
        boolean success = notificationService.removeById(id);
        if (success) {
            // 4. 成功删除通知后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }
}