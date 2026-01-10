import request from '@/utils/request';
import type { ApiResult } from './user';

export interface Notification {
    id: number;
    title: string;
    content: string;
    isPublished: boolean;
    publishTime?: string;
    createTime?: string;
    targetType: string; // 【新增】
}

export interface NotificationPageParams {
    current: number;
    size: number;
}

export function fetchNotificationList(params: NotificationPageParams): Promise<ApiResult<Notification[]>> {
    return request({
        url: '/api/v1/notifications',
        method: 'get',
        params
    });
}

export function fetchNotificationById(id: number): Promise<ApiResult<Notification>> {
    return request({
        url: `/api/v1/notifications/${id}`,
        method: 'get'
    });
}

export function createNotification(data: Partial<Notification>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/notifications',
        method: 'post',
        data
    });
}

export function updateNotification(id: number, data: Partial<Notification>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/notifications/${id}`,
        method: 'put',
        data
    });
}

export function deleteNotification(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/notifications/${id}`,
        method: 'delete'
    });
}

// ==================== 用户通知中心 API ====================

/**
 * 用户通知类型定义
 * @since v3.06
 */
export interface UserNotification {
    id: number;
    userId: number;
    title: string;
    content: string;
    type: 'SCORE_UPDATE' | 'COMMENT_UPDATE' | 'SYSTEM';
    relatedId: number | null;
    isRead: number; // 0-未读, 1-已读
    createTime: string;
    readTime: string | null;
}

/**
 * 通知查询参数
 */
export interface NotificationQueryParams {
    current?: number;
    size?: number;
    isRead?: number; // 0-未读, 1-已读, undefined-全部
}

/**
 * 未读数量响应
 */
export interface UnreadCountResponse {
    count: number;
}

/**
 * 获取我的通知（分页）
 */
export function getMyNotifications(params: NotificationQueryParams): Promise<ApiResult<UserNotification[]>> {
    return request({
        url: '/api/v1/user-notifications',
        method: 'get',
        params
    });
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount(): Promise<ApiResult<UnreadCountResponse>> {
    return request({
        url: '/api/v1/user-notifications/unread-count',
        method: 'get'
    });
}

/**
 * 标记通知为已读
 */
export function markNotificationAsRead(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/user-notifications/${id}/read`,
        method: 'put'
    });
}

/**
 * 标记所有通知为已读
 */
export function markAllNotificationsAsRead(): Promise<ApiResult<{ count: number }>> {
    return request({
        url: '/api/v1/user-notifications/read-all',
        method: 'put'
    });
}

/**
 * 删除通知
 */
export function deleteUserNotification(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/user-notifications/${id}`,
        method: 'delete'
    });
}
