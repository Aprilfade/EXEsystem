import request from '@/utils/request';
import type { ApiResult } from './user';

// 定义与后端DTO匹配的TypeScript接口
export interface SeriesData {
    name: string;
    data: number[];
}

export interface ChartData {
    categories: string[];
    series: SeriesData[];
}

export interface DashboardStats {
    studentCount: number;
    subjectCount: number;
    knowledgePointCount: number;
    questionCount: number;
    paperCount: number;
    notifications: { id: number; content: string, date: string }[];
    kpAndQuestionStats: ChartData;
    // 添加上在Home.vue中使用的另外两个图表数据
    wrongQuestionStats: ChartData;
    monthlyQuestionCreationStats: ChartData;
    subjectStatsByGrade: ChartData;
    duplicateCount?: number; // 确保这个字段存在
}

export function getDashboardStats(): Promise<ApiResult<DashboardStats>> {
    return request({
        url: '/api/v1/dashboard/stats',
        method: 'get'
    });
}