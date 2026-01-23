import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 成就实体
 */
export interface Achievement {
    id: number;
    name: string;
    description: string;
    iconUrl: string;
    type: string; // SIGN_IN_STREAK, TOTAL_QUESTIONS, PERFECT_PAPER 等
    threshold: number;
    rewardPoints: number;
    createTime: string;
    isUnlocked: boolean;
    unlockTime?: string;
}

/**
 * 获取我的成就列表
 */
export function getMyAchievements(): Promise<ApiResult<Achievement[]>> {
    return request({
        url: '/api/v1/student/achievements',
        method: 'get'
    });
}
