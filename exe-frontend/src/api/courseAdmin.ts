import request from '@/utils/request';

/**
 * 管理端课程章节和学习数据API
 */

// ========== 类型定义 ==========

/** 章节节点 */
export interface ChapterNode {
  id: number;
  courseId: number;
  parentId: number;
  name: string;
  description?: string;
  sortOrder: number;
  createdTime?: string;
  updatedTime?: string;
  children?: ChapterNode[];
  resources?: any[];  // 简化，实际使用CourseResource类型
  type?: 'chapter' | 'resource';  // 用于树形展示
}

/** 章节排序DTO */
export interface ChapterSortDTO {
  id: number;
  sortOrder: number;
  parentId: number;
}

/** 课程学习概览 */
export interface CourseProgressOverview {
  totalStudents: number;      // 总学生数
  completedStudents: number;  // 完成人数
  averageProgress: number;    // 平均进度（百分比）
  averageStudyTime: number;   // 平均学习时长（秒）
}

/** 学生进度项 */
export interface StudentProgressItem {
  studentId: number;
  studentName: string;
  studentNo: string;
  grade: string;
  className: string;
  completionRate: number;      // 完成率（百分比）
  totalStudyTime: number;      // 总学习时长（秒）
  lastStudyTime: string | null; // 最后学习时间
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

/** API响应 */
export interface ApiResult<T = any> {
  code: number;
  message: string;
  data: T;
}

// ========== 章节管理API ==========

/**
 * 获取课程章节树
 * @param courseId 课程ID
 */
export function getChapterTree(courseId: number): Promise<ApiResult<ChapterNode[]>> {
  return request({
    url: `/api/v1/admin/course-chapter/course/${courseId}/tree`,
    method: 'get'
  });
}

/**
 * 创建章节
 * @param data 章节数据
 */
export function createChapter(data: Partial<ChapterNode>): Promise<ApiResult<number>> {
  return request({
    url: '/api/v1/admin/course-chapter',
    method: 'post',
    data
  });
}

/**
 * 更新章节
 * @param id 章节ID
 * @param data 章节数据
 */
export function updateChapter(id: number, data: Partial<ChapterNode>): Promise<ApiResult<string>> {
  return request({
    url: `/api/v1/admin/course-chapter/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除章节
 * @param id 章节ID
 */
export function deleteChapter(id: number): Promise<ApiResult<string>> {
  return request({
    url: `/api/v1/admin/course-chapter/${id}`,
    method: 'delete'
  });
}

/**
 * 批量更新章节排序
 * @param sortList 排序列表
 */
export function batchUpdateChapterSort(sortList: ChapterSortDTO[]): Promise<ApiResult<string>> {
  return request({
    url: '/api/v1/admin/course-chapter/batch-sort',
    method: 'post',
    data: sortList
  });
}

/**
 * 移动章节
 * @param id 章节ID
 * @param newParentId 新的父章节ID
 */
export function moveChapter(id: number, newParentId: number): Promise<ApiResult<string>> {
  return request({
    url: `/api/v1/admin/course-chapter/${id}/move`,
    method: 'post',
    params: { newParentId }
  });
}

// ========== 学习数据查询API ==========

/**
 * 获取课程学习概览
 * @param courseId 课程ID
 * @param grade 年级（可选）
 * @param classNo 班级（可选）
 */
export function getCourseProgressOverview(
  courseId: number,
  grade?: string,
  classNo?: string
): Promise<ApiResult<CourseProgressOverview>> {
  return request({
    url: `/api/v1/admin/course-progress/course/${courseId}/overview`,
    method: 'get',
    params: { grade, classNo }
  });
}

/**
 * 获取课程学生进度列表
 * @param courseId 课程ID
 * @param current 当前页
 * @param size 每页大小
 * @param grade 年级（可选）
 * @param classNo 班级（可选）
 */
export function getCourseStudentProgress(
  courseId: number,
  current: number = 1,
  size: number = 10,
  grade?: string,
  classNo?: string
): Promise<ApiResult<PageResult<StudentProgressItem>>> {
  return request({
    url: `/api/v1/admin/course-progress/course/${courseId}/students`,
    method: 'get',
    params: { current, size, grade, classNo }
  });
}

/**
 * 获取学生详细学习进度
 * @param studentId 学生ID
 * @param courseId 课程ID
 */
export function getStudentDetailProgress(
  studentId: number,
  courseId: number
): Promise<ApiResult<any>> {
  return request({
    url: `/api/v1/admin/course-progress/student/${studentId}/course/${courseId}`,
    method: 'get'
  });
}

/**
 * 导出课程学习数据
 * @param courseId 课程ID
 */
export function exportCourseProgress(courseId: number): void {
  window.open(`/api/v1/admin/course-progress/course/${courseId}/export`, '_blank');
}
