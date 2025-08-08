import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 错题记录的详细视图对象 (VO)
 */
export interface WrongRecordVO {
    id: number;
    studentId: number;
    studentName: string;
    studentNo: string;
    questionId: number;
    questionContent: string;
    paperId?: number;
    paperName?: string;
    wrongReason: string;
    createTime: string;
}

/**
 * 错题记录的数据传输对象 (DTO)，用于创建
 */
export interface WrongRecordDTO {
    id?: number;
    questionId: number;
    studentIds: number[]; // 支持为多个学生创建
    paperId?: number;
    wrongReason: string;
}


/**
 * 错题记录分页查询参数
 */
export interface WrongRecordPageParams {
    current: number;
    size: number;
    studentId?: number;
    questionId?: number;
}

/**
 * 试卷题目错误统计的视图对象 (VO)
 */
export interface PaperStatsVO {
    questionId: number;
    questionContent: string;
    questionType: number;
    sortOrder: number;
    errorCount: number;
}

/**
 * 分页获取错题记录列表
 */
export function fetchWrongRecordList(params: WrongRecordPageParams): Promise<ApiResult<WrongRecordVO[]>> {
    return request({
        url: '/api/v1/wrong-records',
        method: 'get',
        params
    });
}

/**
 * 创建错题记录
 */
export function createWrongRecord(data: WrongRecordDTO): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/wrong-records',
        method: 'post',
        data
    });
}

/**
 * 更新错题记录
 */
export function updateWrongRecord(id: number, data: Partial<WrongRecordVO>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/wrong-records/${id}`,
        method: 'put',
        data
    });
}

/**
 * 删除错题记录
 */
export function deleteWrongRecord(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/wrong-records/${id}`,
        method: 'delete'
    });
}


// --- 统计查询 API ---

/**
 * 根据学生ID查询其所有错题记录
 */
export function fetchRecordsByStudent(studentId: number): Promise<ApiResult<WrongRecordVO[]>> {
    return request({
        url: '/api/v1/wrong-records/stats/by-student',
        method: 'get',
        params: { studentId }
    });
}

/**
 * 根据题目ID查询所有答错的学生记录
 */
export function fetchStudentsByQuestion(questionId: number): Promise<ApiResult<WrongRecordVO[]>> {
    return request({
        url: '/api/v1/wrong-records/stats/by-question',
        method: 'get',
        params: { questionId }
    });
}

/**
 * 根据试卷ID统计每道题的错误次数
 */
export function fetchStatsByPaper(paperId: number): Promise<ApiResult<PaperStatsVO[]>> {
    return request({
        url: '/api/v1/wrong-records/stats/by-paper',
        method: 'get',
        params: { paperId }
    });
}