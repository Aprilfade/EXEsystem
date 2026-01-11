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
 * 【新增】学生获取练习题目（无需管理员权限）
 */
export function fetchPracticeQuestions(params: {
    current: number;
    size: number;
    subjectId?: number;
    questionType?: number;
}): Promise<ApiResult<Question[]>> {
    return request({
        url: '/api/v1/questions/practice',
        method: 'get',
        params
    });
}

/**
 * AI 智能生成题目（标准版本）
 */
export function generateQuestionsFromText(data: { text: string; count: number; type: number }): Promise<ApiResult<any[]>> {
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
 * AI 智能生成题目（流式版本）
 */
export function generateQuestionsFromTextStream(
    data: { text: string; count: number; type: number },
    onChunk: (text: string) => void,
    onComplete: (questions: any[]) => void,
    onError: (error: Error) => void
): void {
    const apiKey = localStorage.getItem('student_ai_key') || '';
    const provider = localStorage.getItem('student_ai_provider') || 'DEEPSEEK';

    const url = `${import.meta.env.VITE_API_BASE_URL || ''}/api/v1/questions/ai-generate-stream`;

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
                            const questions = JSON.parse(dataContent);
                            onComplete(questions);
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
 * 【新增】批量删除试题
 */
export function batchDeleteQuestions(ids: number[]): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/questions/batch',
        method: 'delete',
        data: ids
    });
}