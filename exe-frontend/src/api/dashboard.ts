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

export interface TrendData {
    studentCountTrend: number;
    subjectCountTrend: number;
    knowledgePointCountTrend: number;
    questionCountTrend: number;
    paperCountTrend: number;
    onlineCountTrend: number;
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
    trends?: TrendData; // 【新增】趋势数据
}

export interface TodoItem {
    type: string;
    title: string;
    count: number;
    time: string;
    icon: string;
    color: string;
    action: string;
}

export interface TodoList {
    items: TodoItem[];
    totalCount: number;
}

export interface RecentActivity {
    id?: number;
    type: string;
    content: string;
    time: string;
    icon: string;
    color: string;
    userId?: number;
    userName?: string;
    createTime?: string;
}

export interface ActivityList {
    activities: RecentActivity[];
    total: number;
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

// 【新增】获取待办事项列表
export function getTodoList(): Promise<ApiResult<TodoList>> {
    return request({
        url: '/api/v1/dashboard/todos',
        method: 'get'
    });
}

// 【新增】获取最近活动列表
export function getRecentActivities(limit = 5): Promise<ApiResult<ActivityList>> {
    return request({
        url: '/api/v1/dashboard/recent-activities',
        method: 'get',
        params: { limit }
    });
}

// 【新增】获取用户布局配置
export function getLayoutConfig(): Promise<ApiResult<string>> {
    return request({
        url: '/api/v1/dashboard/layout-config',
        method: 'get'
    });
}

// 【新增】保存用户布局配置
export function saveLayoutConfig(config: string): Promise<ApiResult<string>> {
    return request({
        url: '/api/v1/dashboard/layout-config',
        method: 'post',
        data: config,
        headers: {
            'Content-Type': 'application/json'
        }
    });
}

// 【新增】重置布局配置
export function resetLayoutConfig(): Promise<ApiResult<string>> {
    return request({
        url: '/api/v1/dashboard/layout-reset',
        method: 'post'
    });
}

/**
 * 【知识点功能增强】知识点覆盖率统计数据接口
 */
export interface KnowledgePointCoverage {
    subject_id: number;
    subject_name: string;
    total_kp_count: number;
    covered_kp_count: number;
    coverage_rate: number;
}

/**
 * 【知识点功能增强】薄弱知识点数据接口
 */
export interface WeakKnowledgePoint {
    knowledge_point_id: number;
    knowledge_point_name: string;
    subject_name: string;
    avg_mastery_rate: number;
    student_count: number;
}

/**
 * 【知识点功能增强】获取知识点覆盖率统计
 */
export function getKnowledgePointCoverage(): Promise<ApiResult<KnowledgePointCoverage[]>> {
    return request({
        url: '/api/v1/dashboard/knowledge-point-coverage',
        method: 'get'
    });
}

/**
 * 【知识点功能增强】获取薄弱知识点Top10
 */
export function getWeakKnowledgePoints(): Promise<ApiResult<WeakKnowledgePoint[]>> {
    return request({
        url: '/api/v1/dashboard/weak-knowledge-points',
        method: 'get'
    });
}
