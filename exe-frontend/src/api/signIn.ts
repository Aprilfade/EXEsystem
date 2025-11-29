import request from '@/utils/request';
import type { ApiResult } from './user';

export function doSignIn(): Promise<ApiResult<any>> {
    return request({
        url: '/api/v1/student/sign-in',
        method: 'post'
    });
}

export function getSignInStatus(month: string): Promise<ApiResult<any>> {
    return request({
        url: '/api/v1/student/sign-in/status',
        method: 'get',
        params: { month }
    });
}