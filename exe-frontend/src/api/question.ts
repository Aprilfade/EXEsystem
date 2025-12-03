import request from '@/utils/request';
import type { ApiResult } from './user';
import { useStudentAuthStore } from '@/stores/studentAuth'; // 借用 store 获取 key，或者新建 admin store

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
/**
 * 【新增】批量更新试题的科目和年级
 */
export function batchUpdateQuestions(data: { questionIds: number[]; subjectId?: number; grade?: string }): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/questions/batch-update',
        method: 'put',
        data
    });
}
/**
 * 【新增】AI 智能生成题目
 */
export function generateQuestionsFromText(data: { text: string; count: number; type: number }): Promise<ApiResult<any[]>> {
    // 注意：这里需要获取管理员的 Key。
    // 如果你在后台没有做管理员的 Key 配置界面，可以暂时借用 studentAuthStore 的逻辑，
    // 或者在 localStorage 中单独存一个 'admin_ai_key'。
    // 这里假设你用 localStorage 'student_ai_key' 或者你在页面上弹窗让老师输入。
    const apiKey = localStorage.getItem('student_ai_key') || '';
    const provider = localStorage.getItem('student_ai_provider') || 'DEEPSEEK';

    return request({
        url: '/api/v1/questions/ai-generate',
        method: 'post',
        data,
        headers: {
            'X-Ai-Api-Key': apiKey,
            'X-Ai-Provider': provider
        }
    });
}
/**
 * 【新增】批量删除试题
 */
export function batchDeleteQuestions(ids: number[]): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/questions/batch',
        method: 'delete',
        data: ids
    });
}