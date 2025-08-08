import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 知识点接口定义
 */
export interface KnowledgePoint {
    id: number;
    subjectId: number;
    code: string;
    name: string;
    description: string;
    tags?: string;
    remark?: string;
    createTime?: string;
    updateTime?: string;
    questionCount?: number; // 【新增此行】
}

/**
 * 分页查询参数
 */
export interface KnowledgePointPageParams {
    current: number;
    size: number;
    subjectId?: number;
    name?: string;
}

/**
 * 分页获取知识点列表
 */
export function fetchKnowledgePointList(params: KnowledgePointPageParams): Promise<ApiResult<KnowledgePoint[]>> {
    return request({
        url: '/api/v1/knowledge-points',
        method: 'get',
        params
    });
}

/**
 * 根据ID获取单个知识点详情
 */
export function fetchKnowledgePointById(id: number): Promise<ApiResult<KnowledgePoint>> {
    return request({
        url: `/api/v1/knowledge-points/${id}`,
        method: 'get'
    });
}


/**
 * 创建一个新知识点
 */
export function createKnowledgePoint(data: Partial<KnowledgePoint>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/knowledge-points',
        method: 'post',
        data
    });
}

/**
 * 更新一个已存在的知识点信息
 */
export function updateKnowledgePoint(id: number, data: Partial<KnowledgePoint>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/knowledge-points/${id}`,
        method: 'put',
        data
    });
}

/**
 * 删除一个知识点
 */
export function deleteKnowledgePoint(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/knowledge-points/${id}`,
        method: 'delete'
    });
}