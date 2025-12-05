import request from '@/utils/request';
import type { ApiResult } from './user';

export interface CultivationProfile {
    studentId: number;
    realmLevel: number;
    currentExp: number;
    maxExp: number;
    attack: number;
    defense: number;
    // 扩展字段
    afkReward?: { minutes: number; exp: number } | 'NONE';
}

// 丹药商品接口
export interface GameItem {
    id: number;
    name: string;
    description: string;
    type: string;
    resourceValue: string; // "0.2" 代表 20%
    imageUrl?: string;
}

export function fetchGameProfile(): Promise<ApiResult<any>> {
    return request({ url: '/api/v1/student/game/profile', method: 'get' });
}

// 原有的概率突破 (不带参数)
export function breakthrough(): Promise<ApiResult<string>> {
    return request({ url: '/api/v1/student/game/breakthrough', method: 'post' });
}

// 【新增】带丹药的突破 (V2)
export function breakthroughWithItem(data: { goodsId?: number }): Promise<ApiResult<string>> {
    return request({ url: '/api/v1/student/game/breakthrough-v2', method: 'post', data });
}

// 【修改】打坐现在返回对象 { msg, type }
export function meditate(): Promise<ApiResult<any>> {
    return request({ url: '/api/v1/student/game/meditate', method: 'post' });
}

// 答题突破
export function breakthroughWithQuiz(data: { questionId: number; answer: string }): Promise<ApiResult<string>> {
    return request({
        url: '/api/v1/student/game/breakthrough-with-quiz',
        method: 'post',
        data
    });
}

// 【新增】获取我的丹药
export function fetchMyPills(): Promise<ApiResult<GameItem[]>> {
    return request({ url: '/api/v1/student/game/my-pills', method: 'get' });
}