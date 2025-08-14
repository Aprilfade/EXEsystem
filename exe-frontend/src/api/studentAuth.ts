import request from '@/utils/request';
import type { ApiResult } from './user';
import type { Student } from './student'; // 复用已有的 Student 类型

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