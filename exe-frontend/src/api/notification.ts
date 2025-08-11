import request from '@/utils/request';
import type { ApiResult } from './user';

export interface Notification {
    id: number;
    title: string;
    content: string;
    isPublished: boolean;
    publishTime?: string;
    createTime?: string;
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