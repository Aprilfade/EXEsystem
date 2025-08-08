import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 科目接口定义
 */
export interface Subject {
    id: number;
    name: string;
    description: string;
    createTime?: string;
    updateTime?: string;
}

/**
 * 分页查询参数
 */
export interface SubjectPageParams {
    current: number;
    size: number;
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