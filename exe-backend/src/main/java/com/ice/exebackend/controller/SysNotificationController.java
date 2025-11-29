package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysNotification;
import com.ice.exebackend.enums.BusinessType;
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
    @Log(title = "通知管理", businessType = BusinessType.INSERT)
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
    @Log(title = "通知管理", businessType = BusinessType.UPDATE)
    public Result updateNotification(@PathVariable Long id, @RequestBody SysNotification notification) {
        notification.setId(id);

        // 【修改】获取旧数据，用于判断状态变更
        SysNotification oldNotification = notificationService.getById(id);

        // 场景1：从 "草稿" 或 "定时发布" 变为 "立即发布"
        if (oldNotification != null &&
                (oldNotification.getIsPublished() == null || !oldNotification.getIsPublished()) &&
                (notification.getIsPublished() != null && notification.getIsPublished())) {

            notification.setPublishTime(LocalDateTime.now());
        }
        // 场景2：从 "立即发布" 改为 "草稿" (清除定时时间)
        else if (oldNotification != null &&
                oldNotification.getIsPublished() &&
                (notification.getIsPublished() != null && !notification.getIsPublished()) &&
                notification.getPublishTime() == null) { // 并且前端没有指定新的定时时间

            notification.setPublishTime(null);
        }
        // 场景3：设置为 "草稿" (isPublished=false, publishTime=null)
        // 场景4：设置为 "定时发布" (isPublished=false, publishTime=future)
        // 场景5：从 "立即发布" 改为 "定时发布"
        // 这三种情况，我们都信任前端传来的 notification 对象的 (isPublished 和 publishTime) 值，无需额外处理。

        boolean success = notificationService.updateById(notification);
        if (success) {
            // 4. 成功更新通知后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
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