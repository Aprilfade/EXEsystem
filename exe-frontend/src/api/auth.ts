import request from '@/utils/request';
import type { ApiResult, UserInfo } from './user'; // 1. 导入 UserInfo

// 定义登录请求参数类型
interface LoginCredentials {
    username: string;
    password: string;
}

// 定义登录响应数据类型
interface LoginResponse {
    token: string;
}

export function login(data: LoginCredentials): Promise<ApiResult<LoginResponse>> {
    return request({
        url: '/api/v1/auth/login',
        method: 'post',
        data
    });
}

// 2. 确保 getUserInfo 返回的是 Promise<ApiResult<UserInfo>>
export function getUserInfo(): Promise<ApiResult<UserInfo>> {
    return request({
        url: '/api/v1/auth/me',
        method: 'get'
    });
}