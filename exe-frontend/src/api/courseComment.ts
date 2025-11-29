import request from '@/utils/request';
import type { ApiResult } from './user';

export interface CourseComment {
    id: number;
    courseId: number;
    content: string;
    createTime: string;
    studentId: number;
    studentName: string;
    studentAvatar?: string;
    avatarFrameStyle?: string;
    isSelf: boolean;
}

// 获取课程评论
export function fetchComments(courseId: number): Promise<ApiResult<CourseComment[]>> {
    return request({
        url: `/api/v1/student/comments/${courseId}`,
        method: 'get'
    });
}

// 发布评论
export function addComment(data: { courseId: number; content: string }): Promise<ApiResult<any>> {
    return request({
        url: '/api/v1/student/comments',
        method: 'post',
        data
    });
}

// 删除评论
export function deleteComment(id: number): Promise<ApiResult<any>> {
    return request({
        url: `/api/v1/student/comments/${id}`,
        method: 'delete'
    });
}