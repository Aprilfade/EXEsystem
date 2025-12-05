import request from '@/utils/request';
import type { ApiResult } from './user';

export interface CultivationProfile {
    studentId: number;
    realmLevel: number;
    currentExp: number;
    maxExp: number;
    attack: number;
    defense: number;
}

export function fetchGameProfile(): Promise<ApiResult<any>> {
    return request({ url: '/api/v1/student/game/profile', method: 'get' });
}

export function breakthrough(): Promise<ApiResult<string>> {
    return request({ url: '/api/v1/student/game/breakthrough', method: 'post' });
}

export function meditate(): Promise<ApiResult<string>> {
    return request({ url: '/api/v1/student/game/meditate', method: 'post' });
}