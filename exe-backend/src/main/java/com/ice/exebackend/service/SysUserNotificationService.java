package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.SysUserNotification;

/**
 * 用户通知服务接口
 *
 * @author Claude Code Assistant
 * @since v3.06
 */
public interface SysUserNotificationService extends IService<SysUserNotification> {

    /**
     * 创建通知
     *
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param type 类型
     * @param relatedId 关联业务ID
     */
    void createNotification(Long userId, String title, String content, String type, Long relatedId);

    /**
     * 分页查询用户通知
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @param isRead 是否已读（null=全部）
     * @return 分页结果
     */
    Page<SysUserNotification> getUserNotifications(Long userId, Integer current, Integer size, Integer isRead);

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @param userId 用户ID（权限校验）
     * @return 是否成功
     */
    boolean markAsRead(Long id, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 更新条数
     */
    Integer markAllAsRead(Long userId);

    /**
     * 统计未读数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer countUnread(Long userId);

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @param userId 用户ID（权限校验）
     * @return 是否成功
     */
    boolean deleteNotification(Long id, Long userId);
}
