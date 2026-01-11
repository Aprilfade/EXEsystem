/**
 * 练习系统类型定义
 */

// ========== 题目相关 ==========

export interface Question {
  id: number
  subjectId: number
  grade: string
  questionType: number // 1=单选 2=多选 3=填空 4=判断 5=主观
  content: string
  imageUrl?: string
  options?: string[] // JSON解析后的选项数组
  answer: string
  description?: string // 解析说明
  difficulty: number // 1-5难度等级
  knowledgePoint?: string
  knowledgePoints?: string[]
  createTime?: string
}

export type QuestionType = 'single' | 'multiple' | 'blank' | 'judge' | 'subjective' | 'calculation'

// ========== 练习配置 ==========

export interface PracticeConfig {
  mode: 'weakness' | 'knowledge' | 'wrong' | 'custom' // 生成方式
  practiceMode: 'normal' | 'challenge' | 'timed' // 练习模式
  timeLimit?: number // 时间限制（分钟）
  subject?: string
  weaknessPoints?: string[] // 薄弱知识点ID
  knowledgePoints?: string[] // 指定知识点ID
  questionCount?: number // 题目数量
  difficulty?: 'easy' | 'medium' | 'hard' | 'auto' // 难度
  questionTypes?: QuestionType[] // 题型
}

// ========== 练习会话 ==========

export interface PracticeSession {
  id: string
  config: PracticeConfig
  questions: Question[]
  currentIndex: number
  userAnswers: Map<number, UserAnswer>
  startTime: Date
  endTime?: Date
  isPaused: boolean

  // 统计数据
  correctCount: number
  wrongCount: number
  skippedCount: number
  accuracy: number
  maxStreak: number // 最高连击
  currentStreak: number // 当前连击
  xpGained: number // 获得经验值

  // 答题时间记录
  timeRecords: Map<number, number> // questionId -> 耗时(秒)
}

export interface UserAnswer {
  questionId: number
  answer: string | string[] // 用户答案
  isCorrect: boolean
  timeSpent: number // 答题耗时(秒)
  submitTime: Date
  aiGradingResult?: AiGradingResult
}

// ========== AI批改 ==========

export interface AiGradingResult {
  questionId: number
  score: number // 0-100分
  analysis: string // AI分析（Markdown格式）
  strengths?: string[] // 优点
  weaknesses?: string[] // 不足
  suggestions?: string[] // 改进建议
  knowledgePoints?: string[] // 涉及知识点
  gradingTime: Date
}

export interface GradingRequest {
  questionId: number
  questionType: number
  questionContent: string
  correctAnswer: string
  userAnswer: string
}

// ========== 成就系统 ==========

export interface Achievement {
  id: string
  name: string
  description: string
  icon: string
  rarity: 'common' | 'rare' | 'epic' | 'legendary'
  condition: AchievementCondition
  reward: AchievementReward
  unlockedAt?: Date
  progress?: number
}

export interface AchievementCondition {
  type: 'streak' | 'total_correct' | 'perfect_day' | 'speed' | 'subject_master' | 'ai_grading' | 'daily_practice'
  target: number
  metadata?: Record<string, any>
}

export interface AchievementReward {
  xp: number
  title?: string
  badge?: string
  coins?: number
}

// ========== 统计数据 ==========

export interface PracticeStatistics {
  // 基础统计
  totalSessions: number
  totalQuestions: number
  totalCorrect: number
  totalWrong: number
  overallAccuracy: number

  // 时间统计
  totalTimeSpent: number // 总耗时(分钟)
  averageSessionTime: number
  continuousDays: number

  // 难度分布
  difficultyDistribution: {
    easy: number
    medium: number
    hard: number
  }

  // 科目统计
  subjectStats: Map<string, SubjectStat>

  // 知识点统计
  knowledgePointStats: Map<string, KnowledgePointStat>

  // 成就进度
  achievements: Achievement[]
  totalXp: number
  level: number
}

export interface SubjectStat {
  subject: string
  questionCount: number
  correctCount: number
  accuracy: number
  averageTimeSpent: number
}

export interface KnowledgePointStat {
  knowledgePoint: string
  masteryRate: number // 掌握率 0-100
  questionCount: number
  correctCount: number
  lastPracticeTime?: Date
  trend: 'improving' | 'stable' | 'declining'
}

// ========== 自适应难度 ==========

export interface AdaptiveDifficulty {
  currentLevel: number // 1-5
  recentPerformance: number[] // 最近10题的表现得分 0-1
  adjustmentHistory: DifficultyAdjustment[]
}

export interface DifficultyAdjustment {
  timestamp: Date
  oldLevel: number
  newLevel: number
  reason: string
  performanceScore: number
}

// ========== 学习分析 ==========

export interface LearningAnalysis {
  // 正确率趋势
  accuracyTrend: TrendData[]

  // 答题时间分布
  timeDistribution: {
    fast: number // <30秒
    normal: number // 30-60秒
    slow: number // 60-120秒
    verySlow: number // >120秒
  }

  // 知识点掌握雷达图数据
  knowledgeRadar: RadarData[]

  // 薄弱环节
  weaknesses: WeaknessItem[]

  // 推荐练习
  recommendations: PracticeRecommendation[]
}

export interface TrendData {
  date: string
  value: number
  label?: string
}

export interface RadarData {
  name: string
  value: number
  max: number
}

export interface WeaknessItem {
  knowledgePoint: string
  masteryRate: number
  priority: 'high' | 'medium' | 'low'
  recommendedPracticeCount: number
}

export interface PracticeRecommendation {
  title: string
  description: string
  config: Partial<PracticeConfig>
  priority: number
  reason: string
}

// ========== API响应 ==========

export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

export interface PracticeGenerationRequest {
  mode: string
  subject?: string
  knowledgePoints?: string[]
  questionCount?: number
  difficulty?: string
  questionTypes?: string[]
}

export interface PracticeGenerationResponse {
  sessionId: string
  questions: Question[]
  estimatedTime: number
}

// ========== LocalStorage数据结构 ==========

export interface StoredPracticeSession {
  id: string
  config: PracticeConfig
  questions: Question[]
  currentIndex: number
  userAnswers: Record<number, UserAnswer> // Map转换为Object
  startTime: string // Date转换为ISO string
  endTime?: string
  isPaused: boolean
  statistics: {
    correctCount: number
    wrongCount: number
    accuracy: number
    maxStreak: number
    xpGained: number
  }
}

export interface StoredStatistics {
  totalSessions: number
  totalQuestions: number
  totalCorrect: number
  continuousDays: number
  lastPracticeDate: string
  achievements: string[] // achievement IDs
  totalXp: number
  level: number
}
