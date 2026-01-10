import request from '@/utils/request';

/**
 * 学习分析API
 */

// ==================== 类型定义 ====================

/**
 * 学习时长数据点
 */
export interface StudyTimePoint {
  date: string;              // 日期（yyyy-MM-dd）
  studyMinutes: number;      // 学习时长（分钟）
  questionCount: number;     // 完成题目数
}

/**
 * 知识点掌握度数据点
 */
export interface KnowledgeMasteryPoint {
  knowledgePointId: number;
  knowledgePointName: string;
  masteryRate: number;       // 掌握度（0-100）
  totalQuestions: number;
  correctQuestions: number;
}

/**
 * 弱项数据点
 */
export interface WeakPoint {
  knowledgePointId: number;
  knowledgePointName: string;
  scoreRate: number;         // 得分率（0-100）
  wrongCount: number;        // 错题数量
  recommendedPracticeCount: number;  // 建议练习次数
  subjectName: string;
}

/**
 * 学习分析数据
 */
export interface LearningAnalytics {
  studyTimeTrend: StudyTimePoint[];
  knowledgeMastery: KnowledgeMasteryPoint[];
  weakPoints: WeakPoint[];
  learningAdvice: string;
}

// ==================== API 函数 ====================

/**
 * 获取学习分析数据
 * @param days 分析天数（7或30）
 */
export function getLearningAnalytics(days: number = 7) {
  return request.get<LearningAnalytics>('/api/v1/student/learning-analytics', {
    params: { days }
  });
}
