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
// 【修改】获取在线学生人数
// 增加 params: { _t: Date.now() } 以防止浏览器缓存 GET 请求
export function fetchOnlineStudentCount(): Promise<ApiResult<{ count: number }>> {
    return request({
        url: '/api/v1/dashboard/online-students',
        method: 'get',
        params: {
            _t: new Date().getTime() // 添加时间戳参数，确保每次 URL 都不一样
        }
    });
}