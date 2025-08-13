// src/api/log.ts
import request from '@/utils/request';
import type { ApiResult } from './user';

export interface LoginLog {
    id: number;
    username: string;
    ipAddress: string;
    logType: string;
    userAgent: string;
    logTime: string;
}

export interface LogPageParams {
    current: number;
    size: number;
    username?: string;
}

export function fetchLoginLogList(params: LogPageParams): Promise<ApiResult<LoginLog[]>> {
    return request({
        url: '/api/v1/logs/login',
        method: 'get',
        params
    });
}