import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 试题选项接口定义
 */
export interface QuestionOption {
    key: string;
    value: string;
}

/**
 * 试题接口定义
 */
export interface Question {
    id: number;
    subjectId: number;
    grade?: string; // 【新增此行】
    questionType: number; // 1-单选, 2-多选, 3-填空, 4-判断, 5-主观
    content: string;
    imageUrl?: string;
    // 选项，存储时是JSON字符串，使用时我们解析为对象数组
    options: string | QuestionOption[];
    answer: string; // 答案
    answerImageUrl?: string; // 【新增】
    description: string; // 解析
    knowledgePointIds?: number[]; // 关联的知识点ID
}

/**
 * 分页查询参数
 */
export interface QuestionPageParams {
    current: number;
    size: number;
    subjectId?: number;
    questionType?: number;
    grade?: string; // 【新增此行】
}

/**
 * 分页获取试题列表
 */
export function fetchQuestionList(params: QuestionPageParams): Promise<ApiResult<Question[]>> {
    return request({
        url: '/api/v1/questions',
        method: 'get',
        params
    });
}

/**
 * 根据ID获取单个试题详情 (包含知识点)
 */
export function fetchQuestionById(id: number): Promise<ApiResult<Question>> {
    return request({
        url: `/api/v1/questions/${id}`,
        method: 'get'
    });
}

/**
 * 创建一个新试题
 */
export function createQuestion(data: Partial<Question>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/questions',
        method: 'post',
        data
    });
}

/**
 * 更新一个已存在的试题信息
 */
export function updateQuestion(id: number, data: Partial<Question>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/questions/${id}`,
        method: 'put',
        data
    });
}

/**
 * 删除一个试题
 */
export function deleteQuestion(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/questions/${id}`,
        method: 'delete'
    });
}
/**
 * 【新增】上传Excel文件导入试题
 */
export function importQuestions(data: FormData): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/questions/import',
        method: 'post',
        data: data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

/**
 * 【新增】导出试题Excel文件
 */
export function exportQuestions(params: QuestionPageParams): Promise<any> {
    return request({
        url: '/api/v1/questions/export',
        method: 'get',
        params: params,
        responseType: 'blob'
    });
}