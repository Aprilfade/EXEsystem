import request from '@/utils/request';
import type { ApiResult, UserInfo } from './user';

// 定义登录请求参数类型
interface LoginCredentials {
    username: string;
    password: string;
}

// 定义登录响应数据类型
interface LoginResponse {
    token: string;
}
// 【新增】定义注册请求的数据类型
interface RegisterCredentials {
    username: string;
    password: string;
    nickName: string; // 后端 SysUser 实体有这个字段，注册时可以一起传
}

// 【新增】定义后端返回的 Permission 接口类型
export interface Permission {
    id: number;
    name: string;
    code: string;
    type: number;
    parentId: number;
}

// 【新增】定义 /me 接口的完整响应体类型
export interface UserInfoResponse {
    user: UserInfo;
    permissions: string[];
}


export function login(data: LoginCredentials): Promise<ApiResult<LoginResponse>> {
    return request({
        url: '/api/v1/auth/login',
        method: 'post',
        data
    });
}

/**
 * 【新增】用户注册
 * @param data 注册信息
 */
export function register(data: RegisterCredentials): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/auth/register',
        method: 'post',
        data
    });
}
/**
 * 【修改】获取当前登录用户信息 (包含权限)
 * @returns Promise<ApiResult<UserInfoResponse>>
 */
export function getUserInfo(): Promise<ApiResult<UserInfoResponse>> {
    return request({
        url: '/api/v1/auth/me',
        method: 'get'
    });
}
export function logoutApi(): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/auth/logout',
        method: 'post'
    });
}