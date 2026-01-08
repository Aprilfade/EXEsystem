import request from '@/utils/request';

export interface AiCallLog {
    id: number;
    userId: number;
    userType: string;
    functionType: string;
    provider: string;
    success: boolean;
    responseTime: number;
    cached: boolean;
    retryCount: number;
    errorMessage?: string;
    requestSummary?: string;
    tokensUsed?: number;
    estimatedCost?: number;
    createTime: string;
}

export interface AiStats {
    totalCalls: number;
    successRate: number;
    cacheHitRate: number;
    totalCost: number;
    functionStats: Array<{
        function_type: string;
        count: number;
    }>;
}

export interface AiQuota {
    remaining: number;
    currentUsage: number;
}

/**
 * 分页查询AI调用日志
 */
export function getAiLogs(params: {
    current?: number;
    size?: number;
    userId?: number;
    userType?: string;
    functionType?: string;
    provider?: string;
    success?: boolean;
    startTime?: string;
    endTime?: string;
}) {
    return request({
        url: '/api/v1/ai-monitor/logs',
        method: 'get',
        params
    });
}

/**
 * 获取AI使用统计
 */
export function getAiStats(days: number = 7) {
    return request({
        url: '/api/v1/ai-monitor/stats',
        method: 'get',
        params: { days }
    });
}

/**
 * 获取用户AI使用统计
 */
export function getUserAiStats(userId: number, days: number = 7) {
    return request({
        url: `/api/v1/ai-monitor/user-stats/${userId}`,
        method: 'get',
        params: { days }
    });
}

/**
 * 获取用户剩余配额
 */
export function getUserQuota(userId: number) {
    return request({
        url: `/api/v1/ai-monitor/quota/${userId}`,
        method: 'get'
    });
}

/**
 * 获取AI调用趋势
 */
export function getAiTrend(days: number = 30) {
    return request({
        url: '/api/v1/ai-monitor/trend',
        method: 'get',
        params: { days }
    });
}

/**
 * 获取Top用户排行
 */
export function getTopUsers(days: number = 7, limit: number = 10) {
    return request({
        url: '/api/v1/ai-monitor/top-users',
        method: 'get',
        params: { days, limit }
    });
}

/**
 * 获取错误日志
 */
export function getErrorLogs(params: {
    current?: number;
    size?: number;
    days?: number;
}) {
    return request({
        url: '/api/v1/ai-monitor/errors',
        method: 'get',
        params
    });
}

/**
 * 获取日志详情
 */
export function getLogDetail(id: number) {
    return request({
        url: `/api/v1/ai-monitor/logs/${id}`,
        method: 'get'
    });
}

/**
 * 健康检查
 */
export function checkHealth() {
    return request({
        url: '/api/v1/ai-monitor/health',
        method: 'get'
    });
}

/**
 * 导出日志
 */
export function exportLogs(params: {
    startTime?: string;
    endTime?: string;
}) {
    return request({
        url: '/api/v1/ai-monitor/export',
        method: 'get',
        params,
        responseType: 'blob'
    });
}
