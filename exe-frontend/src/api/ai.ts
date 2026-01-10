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

/**
 * 流式AI错题分析
 * @param data 分析请求数据
 * @param onChunk 接收到数据块时的回调函数
 * @param onComplete 完成时的回调函数
 * @param onError 错误时的回调函数
 */
export function analyzeQuestionStream(
    data: AiAnalysisReq,
    onChunk: (text: string) => void,
    onComplete: () => void,
    onError: (error: Error) => void
): void {
    const store = useStudentAuthStore();
    const baseUrl = import.meta.env.VITE_APP_BASE_API || '';

    fetch(baseUrl + '/api/v1/student/ai/analyze-stream', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Ai-Api-Key': store.aiKey || '',
            'X-Ai-Provider': store.aiProvider || 'deepseek',
            // 从 localStorage 获取 token
            'Authorization': 'Bearer ' + localStorage.getItem('studentToken')
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        if (!response.body) {
            throw new Error('Response body is null');
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        // 递归读取流式数据
        function read() {
            reader.read().then(({ done, value }) => {
                if (done) {
                    onComplete();
                    return;
                }

                // 解码数据块
                const chunk = decoder.decode(value, { stream: true });

                // SSE 格式解析
                const lines = chunk.split('\n');
                for (const line of lines) {
                    if (line.startsWith('data: ')) {
                        const data = line.substring(6).trim();

                        // 忽略空数据和结束标记
                        if (data && data !== '[DONE]') {
                            onChunk(data);
                        }
                    } else if (line.startsWith('event: ')) {
                        const event = line.substring(7).trim();
                        if (event === 'done') {
                            onComplete();
                            return;
                        }
                    }
                }

                // 继续读取
                read();
            }).catch(error => {
                onError(error);
            });
        }

        read();
    })
    .catch(error => {
        onError(error);
    });
}