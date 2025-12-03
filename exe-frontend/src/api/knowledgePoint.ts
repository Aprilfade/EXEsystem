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
    grade?: string; // 【新增此行】
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
/**
 * 【新增】根据知识点ID获取其关联的所有试题
 */
export function fetchQuestionsForKnowledgePoint(id: number): Promise<ApiResult<any[]>> {
    return request({
        url: `/api/v1/knowledge-points/${id}/questions`,
        method: 'get'
    });
}
// 获取图谱数据
export function fetchKnowledgeGraph(subjectId: number): Promise<ApiResult<any>> {
    return request({
        url: `/api/v1/knowledge-points/graph/${subjectId}`,
        method: 'get'
    });
}

// 添加关联
export function addKpRelation(data: { parentId: number; childId: number }): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/knowledge-points/relation',
        method: 'post',
        data
    });
}

// 删除关联
export function removeKpRelation(data: { parentId: number; childId: number }): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/knowledge-points/relation/delete',
        method: 'post',
        data
    });
}

// 获取前置知识点
export function fetchPrerequisites(id: number): Promise<ApiResult<KnowledgePoint[]>> {
    return request({
        url: `/api/v1/knowledge-points/${id}/prerequisites`,
        method: 'get'
    });
}
// 【新增】AI 智能生成知识点
export function generateKnowledgePointsFromText(data: { text: string; count: number }): Promise<ApiResult<any[]>> {
    const apiKey = localStorage.getItem('student_ai_key') || ''; // 复用之前存的 Key，或者新建 admin_ai_key
    const provider = localStorage.getItem('student_ai_provider') || 'DEEPSEEK';

    return request({
        url: '/api/v1/knowledge-points/ai-generate',
        method: 'post',
        data,
        headers: {
            'X-Ai-Api-Key': apiKey,
            'X-Ai-Provider': provider
        }
    });
}