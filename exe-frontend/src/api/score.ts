import request from '@/utils/request';

/**
 * 成绩管理 API
 */

// 查询参数接口
export interface ScoreQueryParams {
  current?: number;
  size?: number;
  paperName?: string;
  studentName?: string;
  classId?: number;
  subjectId?: number;
  paperId?: number;
  minScore?: number;
  maxScore?: number;
  startTime?: string;
  endTime?: string;
  published?: boolean;
  status?: number; // 【优化】状态: 0=未提交, 1=待批改（已提交未批阅）, 2=已批改（已批阅未发布）, 3=已发布
  sortBy?: 'score' | 'createTime' | 'violationCount';
  sortOrder?: 'asc' | 'desc';
}

// 成绩详情接口
export interface ExamResultDetail {
  id: number;
  paperId: number;
  paperName: string;
  studentId: number;
  studentName: string;
  studentNo: string;
  classId: number;
  className: string;
  subjectId: number;
  subjectName: string;
  score: number;
  totalScore: number;
  violationCount: number;
  userAnswers: string;
  createTime: string;
  comment: string;
  published: boolean;
  status: number; // 状态: 0=未提交, 1=待批改, 2=已批改, 3=已发布
  // 【优化】新增批阅相关字段
  gradedBy: number | null; // 批阅教师ID
  gradedTime: string | null; // 批阅时间
  originalScore: number | null; // AI自动评分（原始分数）
  version: number; // 乐观锁版本号
}

// 成绩统计接口
export interface ScoreStats {
  totalCount: number;
  averageScore: number;
  maxScore: number;
  minScore: number;
  passCount: number;
  passRate: number;
  excellentCount: number;
  excellentRate: number;
  scoreDistribution: {
    '0-59': number;
    '60-69': number;
    '70-79': number;
    '80-89': number;
    '90-100': number;
  };
}

// 学生成绩趋势接口
export interface ScoreTrend {
  paperName: string;
  score: number;
  totalScore: number;
  createTime: string;
}

/**
 * 获取成绩列表（分页）
 */
export const getScoreList = (params: ScoreQueryParams) => {
  return request.get('/api/v1/exam-results', { params });
};

/**
 * 获取成绩统计数据
 */
export const getScoreStats = (params?: {
  paperId?: number;
  classId?: number;
  subjectId?: number;
  startTime?: string;
  endTime?: string;
}) => {
  return request.get('/api/v1/exam-results/stats', { params });
};

/**
 * 获取学生成绩趋势
 */
export const getStudentScoreTrend = (studentId: number, subjectId?: number) => {
  return request.get(`/api/v1/exam-results/student/${studentId}/trend`, {
    params: { subjectId }
  });
};

/**
 * 导出成绩Excel
 */
export const exportScores = (params: ScoreQueryParams) => {
  const queryString = new URLSearchParams(params as any).toString();
  window.open(`/api/v1/exam-results/export?${queryString}`);
};

/**
 * 获取成绩详情
 */
export const getScoreDetail = (id: number) => {
  return request.get(`/api/v1/exam-results/${id}`);
};

/**
 * 更新成绩分数
 */
export const updateScore = (id: number, payload: { score: number; reason?: string } | number, legacyReason?: string) => {
  // 支持两种调用方式：新的 payload 对象方式和旧的参数方式
  if (typeof payload === 'object') {
    return request.put(`/api/v1/exam-results/${id}/score`, payload);
  } else {
    // 兼容旧的调用方式
    return request.put(`/api/v1/exam-results/${id}/score`, { score: payload, reason: legacyReason });
  }
};

/**
 * 更新成绩评语
 */
export const updateComment = (id: number, comment: string, reason?: string) => {
  return request.put(`/api/v1/exam-results/${id}/comment`, { comment, reason });
};

/**
 * 批量删除成绩
 */
export const batchDeleteScores = (ids: number[]) => {
  return request.delete('/api/v1/exam-results/batch', { data: ids });
};

/**
 * 批量发布/隐藏成绩
 */
export const batchPublishScores = (ids: number[], published: boolean) => {
  return request.put('/api/v1/exam-results/batch/publish', { ids, published });
};

/**
 * 【知识点功能增强】知识点成绩分析DTO
 */
export interface KnowledgePointScoreAnalysisDTO {
  knowledgePointId: number;
  knowledgePointName: string;
  score: number;
  maxScore: number;
  scoreRate: number; // 得分率（百分比）
  questionCount: number;
}

/**
 * 【知识点功能增强】获取考试成绩的知识点分析
 */
export const getKnowledgePointAnalysis = (examResultId: number) => {
  return request.get(`/api/v1/exam-results/${examResultId}/knowledge-analysis`);
};

/**
 * 【批阅历史记录】批阅历史接口
 */
export interface GradingHistory {
  id: number;
  examResultId: number;
  graderId: number;
  graderName: string;
  actionType: 'UPDATE_SCORE' | 'UPDATE_COMMENT' | 'BATCH_UPDATE';
  oldScore: number | null;
  newScore: number | null;
  oldComment: string | null;
  newComment: string | null;
  reason: string | null;
  createTime: string;
}

/**
 * 【批阅历史记录】批阅人统计接口
 */
export interface GraderStats {
  graderId: number;
  totalOperations: number;
}

/**
 * 【批阅历史记录】获取某条成绩的批阅历史
 */
export const getGradingHistory = (examResultId: number) => {
  return request.get(`/api/v1/exam-results/${examResultId}/grading-history`);
};

/**
 * 【批阅历史记录】获取批阅人的操作历史
 */
export const getGraderHistory = (graderId: number, limit?: number) => {
  return request.get(`/api/v1/exam-results/grader/${graderId}/history`, {
    params: { limit }
  });
};

/**
 * 【批阅历史记录】统计批阅人的操作次数
 */
export const getGraderStats = (graderId: number) => {
  return request.get(`/api/v1/exam-results/grader/${graderId}/stats`);
};

/**
 * 【批阅历史记录】导出批阅历史Excel
 */
export const exportGradingHistory = (examResultId: number) => {
  window.open(`/api/v1/exam-results/${examResultId}/grading-history/export`);
};

