import request from '@/utils/request';
import type { ApiResult } from './user';
import type { Question } from './question';

export interface PaperQuestion {
    id?: number;
    paperId: number;
    questionId: number;
    score: number;
    sortOrder: number;
    // For frontend display
    questionDetail?: Question;
}

export interface Paper {
    id: number;
    name: string;
    code: string;
    subjectId: number;
    grade?: string; // 【新增此行】
    description: string;
    totalScore: number;
    paperType?: number; // 【新增】
    createTime?: string;
    updateTime?: string;
    questions?: PaperQuestion[];
    paperImages?: PaperImage[]; // 【新增】
    groups?: PaperGroup[]; // 【修改】
    status?: number; // 【新增】0-草稿, 1-已发布
}

export interface PaperPageParams {
    current: number;
    size: number;
    subjectId?: number;
    grade?: string; // 【新增此行】
}

export interface PaperImage { // 【新增】
    id?: number;
    paperId: number;
    imageUrl: string;
    sortOrder: number;
}
export interface PaperGroup { // 【新增】
    id?: number;
    name: string;
    sortOrder: number;
    questions: PaperQuestion[];
}

export function fetchPaperList(params: PaperPageParams): Promise<ApiResult<Paper[]>> {
    return request({
        url: '/api/v1/papers',
        method: 'get',
        params
    });
}

export function fetchPaperById(id: number): Promise<ApiResult<Paper>> {
    return request({
        url: `/api/v1/papers/${id}`,
        method: 'get'
    });
}

export function createPaper(data: Partial<Paper>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/papers',
        method: 'post',
        data
    });
}

export function updatePaper(id: number, data: Partial<Paper>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/papers/${id}`,
        method: 'put',
        data
    });
}

export function deletePaper(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/papers/${id}`,
        method: 'delete'
    });
}
// 【删除这个函数】
// export function getPaperExportUrl(id: number, includeAnswers: boolean): string {
//     return `/api/v1/papers/export/${id}?includeAnswers=${includeAnswers}`;
// }

// 【新增这个函数】
/**
 * 下载试卷文件
 * @param id 试卷ID
 * @param includeAnswers 是否包含答案
 */
export function downloadPaper(id: number, includeAnswers: boolean): Promise<any> {
    return request({
        url: `/api/v1/papers/export/${id}`,
        method: 'get',
        params: { includeAnswers },
        responseType: 'blob' // 关键：告诉axios期望接收一个二进制对象
    });
}
// 2. 【新增】更新试卷状态 API
export function updatePaperStatus(id: number, status: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/papers/${id}/status`,
        method: 'put',
        params: { status }
    });
}
// 新增请求参数接口
export interface SmartPaperReq {
    subjectId: number;
    grade?: string;
    singleCount?: number;
    multiCount?: number;
    fillCount?: number;
    judgeCount?: number;
    subjectiveCount?: number;
}

// 新增 API 方法
export function generateSmartPaper(data: SmartPaperReq): Promise<ApiResult<PaperGroup[]>> {
    return request({
        url: '/api/v1/papers/generate',
        method: 'post',
        data
    });
}
/**
 * 【新增】下载 PDF 试卷
 */
export function downloadPaperPdf(id: number, includeAnswers: boolean): Promise<any> {
    return request({
        url: `/api/v1/papers/export/pdf/${id}`,
        method: 'get',
        params: { includeAnswers },
        responseType: 'blob' // 必须设置为 blob
    });
}