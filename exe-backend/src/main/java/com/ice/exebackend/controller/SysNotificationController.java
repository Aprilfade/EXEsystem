package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysNotification;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.handler.NotificationWebSocketHandler;
import com.ice.exebackend.service.SysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // 1. 导入 RedisTemplate
import org.springframework.web.bind.annotation.*;
import com.ice.exebackend.handler.NotificationWebSocketHandler; // 导入 Handler
import com.alibaba.fastjson.JSON; // 导入 JSON 工具 (确保 pom.xml 有 fastjson 或 jackson)

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class SysNotificationController {

    @Autowired
    private SysNotificationService notificationService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 【新增】注入 WebSocket Handler
    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    // 3. 定义与 Service 中一致的缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @Log(title = "通知管理", businessType = BusinessType.INSERT)
    public Result createNotification(@RequestBody SysNotification notification) {
        // 设置默认目标为 ALL
        if (notification.getTargetType() == null || notification.getTargetType().isEmpty()) {
            notification.setTargetType("ALL");
        }

        if (Boolean.TRUE.equals(notification.getIsPublished())) {
            notification.setPublishTime(LocalDateTime.now());
        }

        boolean success = notificationService.save(notification);
        if (success) {
            redisTemplate.delete("dashboard:stats:all");

            // 【修改】传递 targetType
            if (Boolean.TRUE.equals(notification.getIsPublished())) {
                String msg = JSON.toJSONString(Map.of(
                        "type", "SYSTEM_NOTICE",
                        "title", notification.getTitle(),
                        "content", notification.getContent()
                ));
                // 传入目标类型
                webSocketHandler.broadcast(msg, notification.getTargetType());
            }
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
    @Log(title = "通知管理", businessType = BusinessType.UPDATE)
    public Result updateNotification(@PathVariable Long id, @RequestBody SysNotification notification) {
        notification.setId(id);

        // 确保 targetType 不为空
        if (notification.getTargetType() == null && notification.getIsPublished() != null && notification.getIsPublished()) {
            // 如果没传，可能是旧数据或只需要更新状态，建议先查一下旧数据的 targetType，或者默认为 ALL
            SysNotification old = notificationService.getById(id);
            if (old != null && old.getTargetType() != null) {
                notification.setTargetType(old.getTargetType());
            } else {
                notification.setTargetType("ALL");
            }
        }

        // ... (原有的发布时间逻辑保持不变) ...
        SysNotification oldNotification = notificationService.getById(id);
        if (oldNotification != null &&
                (oldNotification.getIsPublished() == null || !oldNotification.getIsPublished()) &&
                (notification.getIsPublished() != null && notification.getIsPublished())) {
            notification.setPublishTime(LocalDateTime.now());
        } else if (oldNotification != null &&
                oldNotification.getIsPublished() &&
                (notification.getIsPublished() != null && !notification.getIsPublished()) &&
                notification.getPublishTime() == null) {
            notification.setPublishTime(null);
        }

        boolean success = notificationService.updateById(notification);
        if (success) {
            redisTemplate.delete("dashboard:stats:all");

            // 【修改】传递 targetType
            if (Boolean.TRUE.equals(notification.getIsPublished())) {
                String msg = JSON.toJSONString(Map.of(
                        "type", "SYSTEM_NOTICE",
                        "title", notification.getTitle(),
                        "content", notification.getContent()
                ));
                webSocketHandler.broadcast(msg, notification.getTargetType());
            }
        }
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    @Log(title = "通知管理", businessType = BusinessType.DELETE)
    public Result deleteNotification(@PathVariable Long id) {
        boolean success = notificationService.removeById(id);
        if (success) {
            // 4. 成功删除通知后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }
}