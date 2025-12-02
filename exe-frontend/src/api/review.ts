import request from '@/utils/request';
import type { ApiResult } from './user';
import type { WrongRecordVO } from './wrongRecord';

// 获取今日复习列表
export function fetchDailyReviewList(): Promise<ApiResult<WrongRecordVO[]>> {
    return request({
        url: '/api/v1/student/review/daily',
        method: 'get'
    });
}

// 提交复习结果
export function submitReviewResult(recordId: number, isCorrect: boolean): Promise<ApiResult<string>> {
    return request({
        url: `/api/v1/student/review/${recordId}/result`,
        method: 'post',
        data: { isCorrect }
    });
}