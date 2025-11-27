import request from '@/utils/request';
import type { ApiResult } from './user';
import type { Student } from './student';
import type {Question} from "./question.ts"; // 复用已有的 Student 类型
import type { Paper } from './paper'; // 确保有这个导入

// 登录请求参数
interface StudentLoginCredentials {
    studentNo: string;
    password: string;
}

// 登录响应
interface LoginResponse {
    token: string;
}

/**
 * 学生登录
 */
export function studentLogin(data: StudentLoginCredentials): Promise<ApiResult<LoginResponse>> {
    return request({
        url: '/api/v1/student/auth/login', // 指向学生登录API
        method: 'post',
        data
    });
}

/**
 * 获取当前登录学生的信息
 */
export function getStudentInfo(): Promise<ApiResult<Student>> {
    return request({
        url: '/api/v1/student/auth/me', // 指向获取学生信息的API
        method: 'get'
    });
}
/**
 * 【新增】学生更新自己的个人资料
 */
export function updateStudentProfile(data: Partial<Student>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/student/auth/me', // 指向我们新创建的后端接口
        method: 'put',
        data
    });
}
// 【新增】定义仪表盘统计数据的接口类型
export interface StudentDashboardStats {
    totalAnswered: number;
    averageAccuracy: number;
    wrongRecordCount: number;
    studyDurationHours: number;
}

// 【新增】获取学生仪表盘统计数据的API函数
export function fetchStudentDashboardStats(): Promise<ApiResult<StudentDashboardStats>> {
    return request({
        url: '/api/v1/student/dashboard-stats',
        method: 'get'
    });
}
/**
 * 【修改】获取练习题
 */
export function fetchPracticeQuestions(params: {
    subjectId: number;
    grade: string;
    mode?: string; // 【新增】可选的 mode 参数
}): Promise<ApiResult<Question[]>> {
    return request({
        url: '/api/v1/student/practice-questions',
        method: 'get',
        params
    });
}
/**
 * 获取适合当前登录学生的模拟试卷列表
 */
export function fetchStudentPapers(params: { current: number; size: number; subjectId?: number }): Promise<ApiResult<any>> {
    return request({
        url: '/api/v1/student/papers',
        method: 'get',
        params
    });
}

/**
 * 获取考试试卷详情 (包含题目结构)
 */
export function fetchExamPaperDetail(paperId: number): Promise<ApiResult<any>> {
    return request({
        url: `/api/v1/student/exam/${paperId}`,
        method: 'get'
    });
}

/**
 * 提交考试试卷
 */
export function submitExamPaper(paperId: number, answers: Record<number, string>): Promise<ApiResult<any>> {
    return request({
        url: '/api/v1/student/exam/submit',
        method: 'post',
        params: { paperId },
        data: { answers }
    });
}
/**
 * 获取学生可练习的科目列表
 */
export function fetchPracticeSubjects(): Promise<ApiResult<any[]>> {
    return request({
        url: '/api/v1/student/subjects', // 必须以 /api/v1/student/ 开头才能走正确的 Token 逻辑
        method: 'get'
    });
}
/**
 * 获取考试历史记录
 */
export function fetchExamHistory(params: { current: number; size: number }): Promise<ApiResult<any>> {
    return request({
        url: '/api/v1/student/history',
        method: 'get',
        params
    });
}

/**
 * 获取单次考试结果详情
 */
export function fetchExamHistoryDetail(resultId: number): Promise<ApiResult<any>> {
    return request({
        url: `/api/v1/student/history/${resultId}`,
        method: 'get'
    });
}