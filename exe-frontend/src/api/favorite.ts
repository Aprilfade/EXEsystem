import request from '@/utils/request';
import type { ApiResult } from './user';
import type { Question } from './question';

/**
 * 收藏/取消收藏题目
 */
export function toggleFavorite(questionId: number): Promise<ApiResult<string>> {
    return request({
        url: `/api/v1/student/favorites/${questionId}`,
        method: 'post'
    });
}

/**
 * 检查题目是否已收藏
 */
export function checkFavoriteStatus(questionId: number): Promise<ApiResult<boolean>> {
    return request({
        url: `/api/v1/student/favorites/check/${questionId}`,
        method: 'get'
    });
}

/**
 * 获取我的收藏列表
 */
export function fetchMyFavorites(params: { current: number; size: number }): Promise<ApiResult<any>> {
    return request({
        url: '/api/v1/student/favorites',
        method: 'get',
        params
    });
}