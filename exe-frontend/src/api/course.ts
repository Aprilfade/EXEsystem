import request from '@/utils/request';
import type { ApiResult } from './user';

export interface CourseResource {
    id?: number;
    courseId: number;
    name: string;
    resourceType: 'VIDEO' | 'PDF' | 'PPT' | 'LINK';
    resourceUrl: string;
    sortOrder?: number;
}

export interface Course {
    id?: number;
    name: string;
    description: string;
    coverUrl: string;
    subjectId: number;
    grade: string;
    resources?: CourseResource[];
}

export function fetchCourseList(params: any): Promise<ApiResult<any>> {
    return request({ url: '/api/v1/courses', method: 'get', params });
}

export function fetchCourseDetail(id: number): Promise<ApiResult<Course>> {
    return request({ url: `/api/v1/courses/${id}`, method: 'get' });
}

export function saveCourse(data: Course): Promise<ApiResult<number>> {
    return request({ url: '/api/v1/courses', method: 'post', data });
}

export function deleteCourse(id: number): Promise<ApiResult<null>> {
    return request({ url: `/api/v1/courses/${id}`, method: 'delete' });
}

export function saveResource(data: CourseResource): Promise<ApiResult<null>> {
    return request({ url: '/api/v1/courses/resource', method: 'post', data });
}

export function deleteResource(id: number): Promise<ApiResult<null>> {
    return request({ url: `/api/v1/courses/resource/${id}`, method: 'delete' });
}