import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 课程学习进度相关接口
 */

// ========== 类型定义 ==========

/** 学习进度 */
export interface CourseProgress {
  id: number;
  studentId: number;
  courseId: number;
  resourceId: number;
  resourceType: string;
  progressPercent: number;
  lastPosition: string;
  studyDuration: number;
  isCompleted: number;
  completedTime: string | null;
  createdTime: string;
  updatedTime: string;
}

/** 进度更新参数 */
export interface ProgressUpdateDTO {
  resourceId: number;
  progressPercent: number;
  lastPosition: string;
  studyDuration: number;
}

/** 会话开始参数 */
export interface SessionStartDTO {
  courseId: number;
  resourceId: number;
}

/** 章节树节点 */
export interface ChapterTreeNode {
  id: number;
  name: string;
  description?: string;
  type: 'chapter' | 'resource';
  children?: ChapterTreeNode[];
  resources?: CourseResource[];
}

/** 课程资源 */
export interface CourseResource {
  id: number;
  courseId: number;
  chapterId: number | null;
  name: string;
  resourceType: 'VIDEO' | 'PDF' | 'PPT' | 'LINK';
  resourceUrl: string;
  knowledgePointId: number | null;
  sortOrder: number;
  createTime: string;
}

// ========== API方法 ==========

/**
 * 更新学习进度
 */
export function updateProgress(data: ProgressUpdateDTO): Promise<ApiResult<string>> {
  return request({
    url: '/api/v1/student/course-progress/update',
    method: 'post',
    data
  });
}

/**
 * 获取课程学习进度列表
 */
export function getCourseProgress(courseId: number): Promise<ApiResult<CourseProgress[]>> {
  return request({
    url: `/api/v1/student/course-progress/course/${courseId}`,
    method: 'get'
  });
}

/**
 * 获取资源学习进度
 */
export function getResourceProgress(resourceId: number): Promise<ApiResult<CourseProgress>> {
  return request({
    url: `/api/v1/student/course-progress/resource/${resourceId}`,
    method: 'get'
  });
}

/**
 * 获取课程完成率
 */
export function getCourseCompletion(courseId: number): Promise<ApiResult<number>> {
  return request({
    url: `/api/v1/student/course-progress/course/${courseId}/completion`,
    method: 'get'
  });
}

/**
 * 获取课程章节树
 */
export function getChapterTree(courseId: number): Promise<ApiResult<ChapterTreeNode[]>> {
  return request({
    url: `/api/v1/student/course-progress/course/${courseId}/chapter-tree`,
    method: 'get'
  });
}

/**
 * 开始学习会话
 */
export function startSession(data: SessionStartDTO): Promise<ApiResult<number>> {
  return request({
    url: '/api/v1/student/course-progress/session/start',
    method: 'post',
    data
  });
}

/**
 * 结束学习会话
 */
export function endSession(sessionId: number): Promise<ApiResult<string>> {
  return request({
    url: `/api/v1/student/course-progress/session/${sessionId}/end`,
    method: 'post'
  });
}
