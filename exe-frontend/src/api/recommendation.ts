import request from '@/utils/request';

/**
 * 推荐结果
 */
export interface RecommendationResult {
  itemId: string;
  itemType: 'question' | 'course' | 'knowledgePoint';
  itemTitle: string;
  score: number;
  reason: string;
  confidence: number;
  difficulty?: number;
  subject?: string;
  tags?: string[];
  knowledgePoints?: string[];
  explanation?: string[];
  metadata?: Record<string, any>;
  logId?: number;
  algorithm?: string;
  isNew?: boolean;
}

/**
 * 推荐统计
 */
export interface RecommendationStats {
  totalRecommendations: number;
  clickedRecommendations: number;
  completedRecommendations: number;
  clickRate: number;
  completeRate: number;
  avgScore: number;
  avgStudyDuration: number;
}

/**
 * 获取推荐
 */
export function getRecommendations(params: {
  itemType: string;
  limit?: number;
  page?: number;
  strategyVersion?: string;
  diversityWeight?: number;
  difficultyPreference?: string;
  includePracticed?: boolean;
}) {
  return request({
    url: '/api/v1/student/recommendation',
    method: 'get',
    params
  });
}

/**
 * 记录点击
 */
export function recordClick(data: { logId: number }) {
  return request({
    url: '/api/v1/student/recommendation/click',
    method: 'post',
    data
  });
}

/**
 * 获取推荐统计
 */
export function getRecommendationStats(params: { strategyVersion?: string }) {
  return request({
    url: '/api/v1/student/recommendation/stats',
    method: 'get',
    params
  });
}

// ============ 以下是旧版 API，保留兼容性 ============

/**
 * 题目推荐结果
 */
export interface QuestionRecommendation {
  questionId: number;
  content: string;
  score: number;
  reason: string;
  questionType: number;
  subjectName?: string;
}

/**
 * 课程推荐结果
 */
export interface CourseRecommendation {
  courseId: number;
  name: string;
  description: string;
  score: number;
  reason: string;
  coverUrl?: string;
}

/**
 * 获取题目推荐
 */
export function getQuestionRecommendations(params: {
  subjectId?: number;
  limit?: number;
}) {
  return request({
    url: '/api/v1/student/recommendation/questions',
    method: 'get',
    params
  });
}

/**
 * 获取课程推荐
 */
export function getCourseRecommendations(params: {
  limit?: number;
}) {
  return request({
    url: '/api/v1/student/recommendation/courses',
    method: 'get',
    params
  });
}

/**
 * 自然语言搜索
 */
export function nlpSearch(query: string) {
  return request({
    url: '/api/v1/student/nlp-search/query',
    method: 'post',
    data: { query }
  });
}
