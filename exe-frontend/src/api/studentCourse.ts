import request from '@/utils/request';
import type { ApiResult } from './user';
import type { Course } from './course'; // 复用 Course 类型定义

// 获取学生课程列表
export function fetchStudentCourseList(params: any): Promise<ApiResult<any>> {
    return request({
        url: '/api/v1/student/courses',
        method: 'get',
        params
    });
}

// 获取学生课程详情
export function fetchStudentCourseDetail(id: number): Promise<ApiResult<Course>> {
    return request({
        url: `/api/v1/student/courses/${id}`,
        method: 'get'
    });
}