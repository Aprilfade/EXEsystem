package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.SysUserNotification;
import com.ice.exebackend.mapper.SysUserNotificationMapper;
import com.ice.exebackend.service.SysUserNotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户通知服务实现类
 *
 * @author Claude Code Assistant
 * @since v3.06
 */
@Service
public class SysUserNotificationServiceImpl extends ServiceImpl<SysUserNotificationMapper, SysUserNotification>
        implements SysUserNotificationService {

    @Override
    public void createNotification(Long userId, String title, String content, String type, Long relatedId) {
        SysUserNotification notification = new SysUserNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());

        this.save(notification);
    }

    @Override
    public Page<SysUserNotification> getUserNotifications(Long userId, Integer current, Integer size, Integer isRead) {
        LambdaQueryWrapper<SysUserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserNotification::getUserId, userId);

        if (isRead != null) {
            wrapper.eq(SysUserNotification::getIsRead, isRead);
        }

        wrapper.orderByDesc(SysUserNotification::getCreateTime);

        Page<SysUserNotification> page = new Page<>(current, size);
        return this.page(page, wrapper);
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        SysUserNotification notification = this.getById(id);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }

        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        return this.updateById(notification);
    }

    @Override
    public Integer markAllAsRead(Long userId) {
        return baseMapper.markAllAsRead(userId);
    }

    @Override
    public Integer countUnread(Long userId) {
        return baseMapper.countUnread(userId);
    }

    @Override
    public boolean deleteNotification(Long id, Long userId) {
        SysUserNotification notification = this.getById(id);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }

        return this.removeById(id);
    }
}
