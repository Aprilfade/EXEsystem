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
 * 【新增】获取所有知识点（不分页）
 */
export function fetchAllKnowledgePoints(params?: { subjectId?: number }): Promise<ApiResult<KnowledgePoint[]>> {
    return request({
        url: '/api/v1/knowledge-points/all',
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

/**
 * AI 智能生成知识点（标准版本）
 */
export function generateKnowledgePointsFromText(data: { text: string; count: number }): Promise<ApiResult<any[]>> {
    const apiKey = localStorage.getItem('student_ai_key') || '';
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

/**
 * AI 智能生成知识点（流式版本）
 */
export function generateKnowledgePointsFromTextStream(
    data: { text: string; count: number },
    onChunk: (text: string) => void,
    onComplete: (points: any[]) => void,
    onError: (error: Error) => void
): void {
    const apiKey = localStorage.getItem('student_ai_key') || '';
    const provider = localStorage.getItem('student_ai_provider') || 'DEEPSEEK';

    const url = `${import.meta.env.VITE_API_BASE_URL || ''}/api/v1/knowledge-points/ai-generate-stream`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Ai-Api-Key': apiKey,
            'X-Ai-Provider': provider,
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(data)
    }).then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const reader = response.body!.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let currentEvent = '';

        function processText(text: string) {
            buffer += text;
            const lines = buffer.split('\n');
            buffer = lines.pop() || ''; // 保留最后的不完整行

            for (let i = 0; i < lines.length; i++) {
                const line = lines[i].trim();
                if (!line) {
                    currentEvent = ''; // 空行表示事件结束
                    continue;
                }

                // SSE格式: event: xxx 或 data: xxx
                if (line.startsWith('event:')) {
                    currentEvent = line.substring(6).trim();
                } else if (line.startsWith('data:')) {
                    const dataContent = line.substring(5).trim();

                    if (currentEvent === 'done') {
                        // 完成：解析JSON结果
                        try {
                            const points = JSON.parse(dataContent);
                            onComplete(points);
                        } catch (e) {
                            console.error('解析结果失败:', e, dataContent);
                            onError(new Error('解析结果失败'));
                        }
                    } else if (currentEvent === 'message' || !currentEvent) {
                        // 流式数据块
                        onChunk(dataContent);
                    }
                }
            }
        }

        function readStream(): void {
            reader.read().then(({ done, value }) => {
                if (done) {
                    if (buffer) {
                        processText('\n'); // 处理剩余数据
                    }
                    return;
                }

                const text = decoder.decode(value, { stream: true });
                processText(text);
                readStream();
            }).catch(onError);
        }

        readStream();
    }).catch(onError);
}

/**
 * 【知识点功能增强】知识点全局统计接口
 */
export interface KnowledgePointGlobalStats {
    totalKpCount: number; // 知识点总数
    kpWithQuestionsCount: number; // 有题目的知识点数量
    totalQuestionCount: number; // 总题目数量
    avgQuestionsPerKp: number; // 平均每个知识点的题目数
    coverageRate: number; // 知识点覆盖率（百分比）
}

/**
 * 【知识点功能增强】获取知识点全局统计数据
 */
export function fetchKnowledgePointGlobalStats(): Promise<ApiResult<KnowledgePointGlobalStats>> {
    return request({
        url: '/api/v1/knowledge-points/global-stats',
        method: 'get'
    });
}

/**
 * 【批量编辑】批量更新知识点
 */
export interface KnowledgePointBatchUpdateParams {
    knowledgePointIds: number[];
    subjectId?: number;
    grade?: string;
    tags?: string;
}

export function batchUpdateKnowledgePoints(data: KnowledgePointBatchUpdateParams): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/knowledge-points/batch-update',
        method: 'put',
        data
    });
}
