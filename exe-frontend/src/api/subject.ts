import request from '@/utils/request';
import type { ApiResult } from './user';
import type {Question} from "./question.ts";

/**
 * 科目接口定义
 */
export interface Subject {
    id: number;
    name: string;
    description: string;
    grade?: string; // 【新增此行】
    createTime?: string;
    updateTime?: string;
    knowledgePointCount?: number; // 【新增此行】
    questionCount?: number; // 【新增此行】
}

/**
 * 分页查询参数
 */
export interface SubjectPageParams {
    current: number;
    size: number;
    name?: string;
    grade?: string; // 【新增】添加 grade 字段
    sortField?: string;
    sortOrder?: string;
}

/**
 * 分页获取科目列表
 */
export function fetchSubjectList(params: SubjectPageParams): Promise<ApiResult<Subject[]>> {
    return request({
        url: '/api/v1/subjects',
        method: 'get',
        params
    });
}

/**
 * 创建一个新科目
 */
export function createSubject(data: Partial<Subject>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/subjects',
        method: 'post',
        data
    });
}

/**
 * 更新一个已存在的科目信息
 */
export function updateSubject(id: number, data: Partial<Subject>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/subjects/${id}`,
        method: 'put',
        data
    });
}

/**
 * 删除一个科目
 */
export function deleteSubject(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/subjects/${id}`,
        method: 'delete'
    });
}
/**
 * 获取所有科目列表（不分页）
 */
export function fetchAllSubjects(): Promise<ApiResult<Subject[]>> {
    return request({
        url: '/api/v1/subjects/all', // 对应后端的 /all 接口
        method: 'get'
    });

}

/**
 * 【优化】根据科目ID获取其关联的试题列表 - 支持分页
 */
export function fetchQuestionsForSubject(
    subjectId: number,
    current: number = 1,
    size: number = 50
): Promise<ApiResult<Question[]>> {
    return request({
        url: `/api/v1/subjects/${subjectId}/questions`,
        method: 'get',
        params: { current, size }
    });
}