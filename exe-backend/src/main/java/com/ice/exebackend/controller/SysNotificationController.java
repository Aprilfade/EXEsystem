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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class SysNotificationController {

    @Autowired
    private SysNotificationService notificationService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @Log(title = "通知管理", businessType = BusinessType.INSERT)
    public Result createNotification(@RequestBody SysNotification notification) {
        // 1. 设置默认目标为 ALL
        if (notification.getTargetType() == null || notification.getTargetType().isEmpty()) {
            notification.setTargetType("ALL");
        }

        // 2. 如果是立即发布，设置发布时间
        if (Boolean.TRUE.equals(notification.getIsPublished())) {
            notification.setPublishTime(LocalDateTime.now());
        }

        boolean success = notificationService.save(notification);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);

            // 3. 发送 WebSocket 通知
            if (Boolean.TRUE.equals(notification.getIsPublished())) {
                sendBroadcast(notification);
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

        // 1. 获取旧数据（只查一次数据库）
        SysNotification oldNotification = notificationService.getById(id);
        if (oldNotification == null) {
            return Result.fail("通知不存在");
        }

        // 2. 处理 TargetType：如果前端没传，沿用旧值
        if (notification.getTargetType() == null) {
            notification.setTargetType(oldNotification.getTargetType() != null ? oldNotification.getTargetType() : "ALL");
        }

        // 3. 处理发布时间逻辑
        // 情况A: 从"未发布"变为"已发布" -> 设置当前时间
        if ((oldNotification.getIsPublished() == null || !oldNotification.getIsPublished()) &&
                (notification.getIsPublished() != null && notification.getIsPublished())) {
            notification.setPublishTime(LocalDateTime.now());
        }
        // 情况B: 从"已发布"变为"未发布"（撤回）-> 清空时间
        else if (oldNotification.getIsPublished() != null && oldNotification.getIsPublished() &&
                (notification.getIsPublished() != null && !notification.getIsPublished())) {
            notification.setPublishTime(null);
        }

        boolean success = notificationService.updateById(notification);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);

            // 4. 发送 WebSocket 通知（需要确保 title 和 content 存在）
            if (Boolean.TRUE.equals(notification.getIsPublished())) {
                // 如果前端只传了状态变更，需要把旧数据的标题内容补全，否则 WebSocket 会发空消息
                if (notification.getTitle() == null) notification.setTitle(oldNotification.getTitle());
                if (notification.getContent() == null) notification.setContent(oldNotification.getContent());

                sendBroadcast(notification);
            }
        }
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    @Log(title = "通知管理", businessType = BusinessType.DELETE)
    public Result deleteNotification(@PathVariable Long id) {
        boolean success = notificationService.removeById(id);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    // 辅助方法：发送广播
    private void sendBroadcast(SysNotification notification) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("type", "SYSTEM_NOTICE");
            map.put("title", notification.getTitle() != null ? notification.getTitle() : "系统通知");
            map.put("content", notification.getContent() != null ? notification.getContent() : "");

            String msg = JSON.toJSONString(map);
            webSocketHandler.broadcast(msg, notification.getTargetType());
        } catch (Exception e) {
            // 捕获异常，防止通知发送失败影响主业务流程
            e.printStackTrace();
        }
    }
}