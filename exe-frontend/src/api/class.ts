import request from '@/utils/request';
import type { ApiResult } from './user';

// 管理员/教师接口
export function fetchClassList(): Promise<ApiResult<any>> {
    return request({ url: '/api/v1/classes', method: 'get' });
}

export function createClass(data: any): Promise<ApiResult<any>> {
    return request({ url: '/api/v1/classes', method: 'post', data });
}

export function updateClass(id: number, data: any): Promise<ApiResult<any>> {
    return request({ url: `/api/v1/classes/${id}`, method: 'put', data });
}

export function deleteClass(id: number): Promise<ApiResult<any>> {
    return request({ url: `/api/v1/classes/${id}`, method: 'delete' });
}

export function regenerateCode(id: number): Promise<ApiResult<string>> {
    return request({ url: `/api/v1/classes/${id}/regenerate-code`, method: 'post' });
}

export function removeStudent(classId: number, studentId: number): Promise<ApiResult<any>> {
    return request({ url: `/api/v1/classes/${classId}/students/${studentId}`, method: 'delete' });
}

export function fetchClassStudents(classId: number): Promise<ApiResult<any>> {
    return request({ url: `/api/v1/classes/${classId}/students`, method: 'get' });
}

export function assignHomework(classId: number, data: any): Promise<ApiResult<any>> {
    return request({ url: `/api/v1/classes/${classId}/homework`, method: 'post', data });
}

// 学生接口
export function joinClass(code: string): Promise<ApiResult<any>> {
    return request({ url: '/api/v1/student/classes/join', method: 'post', data: { code } });
}

export function fetchMyClasses(): Promise<ApiResult<any>> {
    return request({ url: '/api/v1/student/classes/my', method: 'get' });
}

export function fetchClassHomework(classId: number): Promise<ApiResult<any>> {
    return request({ url: `/api/v1/student/classes/${classId}/homework`, method: 'get' });
}