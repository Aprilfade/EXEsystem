import request from '@/utils/request';

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
