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
export interface OperLog {
    id: number;
    title: string;        // 模块标题
    businessType: number; // 0其它 1新增 2修改 3删除...
    method: string;       // 方法名称
    requestMethod: string;// 请求方式
    operName: string;     // 操作人员
    operUrl: string;      // 请求URL
    operIp: string;       // 主机地址
    operParam: string;    // 请求参数
    jsonResult: string;   // 返回参数
    status: number;       // 0正常 1异常
    errorMsg: string;     // 错误消息
    operTime: string;     // 操作时间
}

export interface OperLogPageParams {
    current: number;
    size: number;
    title?: string;
    operName?: string;
    status?: number;
    businessType?: number;
}

export function fetchOperLogList(params: OperLogPageParams): Promise<ApiResult<OperLog[]>> {
    return request({
        url: '/api/v1/logs/operation',
        method: 'get',
        params
    });
}

export function deleteOperLog(ids: number[]): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/logs/operation/${ids.join(',')}`,
        method: 'delete'
    });
}

export function cleanOperLog(): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/logs/operation/clean',
        method: 'delete'
    });
}