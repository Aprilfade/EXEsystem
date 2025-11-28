import request from '@/utils/request';
import { useStudentAuthStore } from '@/stores/studentAuth';
import type { ApiResult } from './user';

export interface AiAnalysisReq {
    questionContent: string;
    studentAnswer: string;
    correctAnswer: string;
    analysis?: string;
}

export function analyzeQuestion(data: AiAnalysisReq): Promise<ApiResult<string>> {
    const store = useStudentAuthStore();
    return request({
        url: '/api/v1/student/ai/analyze',
        method: 'post',
        data,
        headers: {
            // 将 Store 中的 Key 放入 Header 传给后端
            'X-Ai-Api-Key': store.aiKey,
            // 【新增】传递用户选择的提供商
            'X-Ai-Provider': store.aiProvider
        }
    });
}