import { defineStore } from 'pinia'
import type {
  PracticeConfig,
  PracticeSession,
  Question,
  UserAnswer,
  Achievement,
  AiGradingResult,
  AdaptiveDifficulty,
  PracticeStatistics
} from '@/types/practice'

interface PracticeState {
  // 当前练习会话
  currentSession: PracticeSession | null

  // 题目数据
  questions: Question[]
  currentIndex: number
  userAnswers: Map<number, UserAnswer>

  // 自适应难度
  difficultyLevel: number // 1-5
  recentPerformance: number[] // 最近10题表现 (0-1)
  adaptiveHistory: AdaptiveDifficulty

  // 成就系统
  achievements: Achievement[]
  currentStreak: number // 连续答对
  totalXp: number
  level: number

  // AI批改
  gradingResults: Map<number, AiGradingResult>
  aiExplanations: Map<number, string>

  // 统计数据
  statistics: PracticeStatistics | null

  // UI状态
  isPracticing: boolean
  isPaused: boolean
  showResults: boolean
}

export const usePracticeStore = defineStore('practice', {
  state: (): PracticeState => ({
    currentSession: null,
    questions: [],
    currentIndex: 0,
    userAnswers: new Map(),

    difficultyLevel: 3,
    recentPerformance: [],
    adaptiveHistory: {
      currentLevel: 3,
      recentPerformance: [],
      adjustmentHistory: []
    },

    achievements: [],
    currentStreak: 0,
    totalXp: 0,
    level: 1,

    gradingResults: new Map(),
    aiExplanations: new Map(),

    statistics: null,

    isPracticing: false,
    isPaused: false,
    showResults: false
  }),

  getters: {
    /**
     * 当前题目
     */
    currentQuestion(state): Question | null {
      if (!state.questions.length || state.currentIndex >= state.questions.length) {
        return null
      }
      return state.questions[state.currentIndex]
    },

    /**
     * 答题进度百分比
     */
    progressPercentage(state): number {
      if (!state.questions.length) return 0
      return Math.round(((state.currentIndex + 1) / state.questions.length) * 100)
    },

    /**
     * 已答题数
     */
    answeredCount(state): number {
      return state.userAnswers.size
    },

    /**
     * 答对题数
     */
    correctCount(state): number {
      let count = 0
      state.userAnswers.forEach(answer => {
        if (answer.isCorrect) count++
      })
      return count
    },

    /**
     * 答错题数
     */
    wrongCount(state): number {
      let count = 0
      state.userAnswers.forEach(answer => {
        if (!answer.isCorrect) count++
      })
      return count
    },

    /**
     * 当前正确率
     */
    currentAccuracy(state): number {
      if (state.userAnswers.size === 0) return 0
      const correct = this.correctCount as number
      return Math.round((correct / state.userAnswers.size) * 100)
    },

    /**
     * 是否完成所有题目
     */
    isCompleted(state): boolean {
      return state.userAnswers.size === state.questions.length && state.questions.length > 0
    },

    /**
     * 练习时长（秒）
     */
    practiceDuration(state): number {
      if (!state.currentSession) return 0
      const start = new Date(state.currentSession.startTime).getTime()
      const end = state.currentSession.endTime
        ? new Date(state.currentSession.endTime).getTime()
        : Date.now()
      return Math.floor((end - start) / 1000)
    },

    /**
     * 平均答题时间
     */
    averageAnswerTime(state): number {
      if (state.userAnswers.size === 0) return 0
      let total = 0
      state.userAnswers.forEach(answer => {
        total += answer.timeSpent
      })
      return Math.round(total / state.userAnswers.size)
    },

    /**
     * 获取用户等级
     */
    userLevel(state): number {
      return Math.floor(state.totalXp / 1000) + 1
    },

    /**
     * 当前等级进度
     */
    levelProgress(state): number {
      const currentLevelXp = (this.userLevel - 1) * 1000
      const nextLevelXp = this.userLevel * 1000
      const progress = state.totalXp - currentLevelXp
      const total = nextLevelXp - currentLevelXp
      return Math.round((progress / total) * 100)
    }
  },

  actions: {
    /**
     * 开始练习
     */
    async startPractice(config: PracticeConfig, questions: Question[]) {
      const sessionId = `session_${Date.now()}`

      this.currentSession = {
        id: sessionId,
        config,
        questions,
        currentIndex: 0,
        userAnswers: new Map(),
        startTime: new Date(),
        isPaused: false,
        correctCount: 0,
        wrongCount: 0,
        skippedCount: 0,
        accuracy: 0,
        maxStreak: 0,
        currentStreak: 0,
        xpGained: 0,
        timeRecords: new Map()
      }

      this.questions = questions
      this.currentIndex = 0
      this.userAnswers = new Map()
      this.isPracticing = true
      this.showResults = false

      // 保存到localStorage
      this.saveSession()
    },

    /**
     * 提交答案
     */
    async submitAnswer(questionId: number, answer: string | string[], timeSpent: number) {
      const question = this.questions.find(q => q.id === questionId)
      if (!question) return

      // 判断是否正确
      const isCorrect = this.checkAnswer(question, answer)

      // 创建答案记录
      const userAnswer: UserAnswer = {
        questionId,
        answer,
        isCorrect,
        timeSpent,
        submitTime: new Date()
      }

      // 保存答案
      this.userAnswers.set(questionId, userAnswer)

      // 更新统计
      if (isCorrect) {
        this.currentStreak++
        if (this.currentSession) {
          this.currentSession.correctCount++
          this.currentSession.maxStreak = Math.max(
            this.currentSession.maxStreak,
            this.currentStreak
          )
        }
      } else {
        this.currentStreak = 0
        if (this.currentSession) {
          this.currentSession.wrongCount++
        }
      }

      // 更新自适应难度
      this.updateAdaptiveDifficulty(isCorrect, timeSpent, question.difficulty)

      // 计算获得XP
      const xpGain = this.calculateXpGain(isCorrect, timeSpent, question.difficulty)
      this.totalXp += xpGain
      if (this.currentSession) {
        this.currentSession.xpGained += xpGain
      }

      // 保存会话
      this.saveSession()
    },

    /**
     * 检查答案是否正确
     */
    checkAnswer(question: Question, userAnswer: string | string[]): boolean {
      const correctAnswer = question.answer.trim().toLowerCase()

      if (Array.isArray(userAnswer)) {
        // 多选题
        const userAnswerStr = userAnswer.sort().join(',').toLowerCase()
        return userAnswerStr === correctAnswer
      } else {
        // 单选、填空、判断
        // 统一转换中文逗号为英文逗号
        const correctLower = correctAnswer.replace(/，/g, ',')
        const userAnswerNormalized = userAnswer.trim().toLowerCase().replace(/，/g, ',')

        if (!correctLower.includes('###')) {
          // 单答案，直接匹配
          return userAnswerNormalized === correctLower
        }

        const parts = correctLower.split('###').map(ans => ans.trim())

        // 检查是否为多空题格式（某些部分包含逗号，某些不包含）
        const templatePart = parts.find(p => p.includes(','))

        if (templatePart && userAnswerNormalized.includes(',')) {
          // 多空题：模板格式 "固定部分，可变部分1###可变部分2###可变部分3"
          const templateFields = templatePart.split(',').map(f => f.trim())
          const userFields = userAnswerNormalized.split(',').map(f => f.trim())

          if (templateFields.length !== userFields.length) {
            // 空格数量不匹配，尝试完整匹配
            return parts.some(ans => userAnswerNormalized === ans)
          }

          // 逐个字段检查
          return templateFields.every((templateField, index) => {
            const userField = userFields[index]

            // 收集这个位置的所有可能答案
            const possibleAnswers: string[] = []

            parts.forEach(p => {
              if (p.includes(',')) {
                // 从完整答案中提取对应字段
                const fields = p.split(',').map(f => f.trim())
                if (fields[index]) {
                  possibleAnswers.push(fields[index])
                }
              } else if (index === templateFields.length - 1) {
                // 单个词可能是最后一个字段的替代答案
                possibleAnswers.push(p)
              }
            })

            // 检查用户输入是否在可能答案中
            return possibleAnswers.includes(userField)
          })
        } else {
          // 单空多答案：直接匹配任意一个
          return parts.some(ans => userAnswerNormalized === ans)
        }
      }
    },

    /**
     * 下一题
     */
    nextQuestion() {
      if (this.currentIndex < this.questions.length - 1) {
        this.currentIndex++
      }
    },

    /**
     * 上一题
     */
    previousQuestion() {
      if (this.currentIndex > 0) {
        this.currentIndex--
      }
    },

    /**
     * 跳转到指定题目
     */
    goToQuestion(index: number) {
      if (index >= 0 && index < this.questions.length) {
        this.currentIndex = index
      }
    },

    /**
     * 暂停练习
     */
    pausePractice() {
      this.isPaused = true
      this.saveSession()
    },

    /**
     * 继续练习
     */
    resumePractice() {
      this.isPaused = false
    },

    /**
     * 完成练习
     */
    completePractice() {
      if (!this.currentSession) return

      this.currentSession.endTime = new Date()
      this.currentSession.accuracy = this.currentAccuracy
      this.isPracticing = false
      this.showResults = true

      // 保存最终结果
      this.saveSession()
      this.saveToHistory()
    },

    /**
     * 更新自适应难度
     */
    updateAdaptiveDifficulty(isCorrect: boolean, timeSpent: number, questionDifficulty: number) {
      // 计算表现分数 (0-1)
      let performanceScore = isCorrect ? 1 : 0

      // 时间因子调整
      if (isCorrect && timeSpent < 30) {
        performanceScore += 0.2 // 快速答题奖励
      } else if (isCorrect && timeSpent > 120) {
        performanceScore -= 0.1 // 过慢答题惩罚
      }

      performanceScore = Math.max(0, Math.min(1.2, performanceScore))

      // 记录最近表现
      this.recentPerformance.push(performanceScore)
      if (this.recentPerformance.length > 10) {
        this.recentPerformance.shift()
      }

      // 计算平均表现
      const avgPerformance =
        this.recentPerformance.reduce((a, b) => a + b, 0) / this.recentPerformance.length

      const oldLevel = this.difficultyLevel

      // 动态调整难度
      if (avgPerformance > 0.8 && this.difficultyLevel < 5) {
        this.difficultyLevel = Math.min(5, this.difficultyLevel + 0.5)
      } else if (avgPerformance < 0.4 && this.difficultyLevel > 1) {
        this.difficultyLevel = Math.max(1, this.difficultyLevel - 0.5)
      }

      // 记录调整历史
      if (oldLevel !== this.difficultyLevel) {
        this.adaptiveHistory.adjustmentHistory.push({
          timestamp: new Date(),
          oldLevel,
          newLevel: this.difficultyLevel,
          reason: avgPerformance > 0.8 ? '表现优秀，提升难度' : '需要调整，降低难度',
          performanceScore: avgPerformance
        })
      }

      this.adaptiveHistory.currentLevel = this.difficultyLevel
      this.adaptiveHistory.recentPerformance = [...this.recentPerformance]
    },

    /**
     * 计算获得XP
     */
    calculateXpGain(isCorrect: boolean, timeSpent: number, difficulty: number): number {
      if (!isCorrect) return 0

      let baseXp = 10 // 基础XP
      let difficultyBonus = difficulty * 5 // 难度加成
      let speedBonus = 0

      // 速度加成
      if (timeSpent < 30) {
        speedBonus = 10
      } else if (timeSpent < 60) {
        speedBonus = 5
      }

      // 连击加成
      let streakBonus = Math.min(this.currentStreak * 2, 20)

      return baseXp + difficultyBonus + speedBonus + streakBonus
    },

    /**
     * 保存AI批改结果
     */
    saveAiGrading(questionId: number, result: AiGradingResult) {
      this.gradingResults.set(questionId, result)

      // 更新用户答案中的批改结果
      const userAnswer = this.userAnswers.get(questionId)
      if (userAnswer) {
        userAnswer.aiGradingResult = result
        this.userAnswers.set(questionId, userAnswer)
      }

      this.saveSession()
    },

    /**
     * 解锁成就
     */
    unlockAchievement(achievement: Achievement) {
      const existing = this.achievements.find(a => a.id === achievement.id)
      if (existing) return // 已解锁

      this.achievements.push({
        ...achievement,
        unlockedAt: new Date()
      })

      // 奖励XP
      this.totalXp += achievement.reward.xp

      // 保存
      this.saveAchievements()
    },

    /**
     * 重置练习
     */
    resetPractice() {
      this.currentSession = null
      this.questions = []
      this.currentIndex = 0
      this.userAnswers = new Map()
      this.isPracticing = false
      this.isPaused = false
      this.showResults = false
      this.currentStreak = 0
      this.gradingResults = new Map()
      this.aiExplanations = new Map()
    },

    /**
     * 保存会话到LocalStorage
     */
    saveSession() {
      if (!this.currentSession) return

      const sessionData = {
        ...this.currentSession,
        userAnswers: Object.fromEntries(this.userAnswers),
        timeRecords: Object.fromEntries(this.currentSession.timeRecords),
        startTime: this.currentSession.startTime.toISOString(),
        endTime: this.currentSession.endTime?.toISOString()
      }

      localStorage.setItem('current_practice_session', JSON.stringify(sessionData))
    },

    /**
     * 保存到历史记录
     */
    saveToHistory() {
      if (!this.currentSession) return

      const history = JSON.parse(localStorage.getItem('practice_history') || '[]')
      history.unshift({
        id: this.currentSession.id,
        config: this.currentSession.config,
        startTime: this.currentSession.startTime.toISOString(),
        endTime: this.currentSession.endTime?.toISOString(),
        questionCount: this.questions.length,
        correctCount: this.currentSession.correctCount,
        accuracy: this.currentSession.accuracy,
        xpGained: this.currentSession.xpGained,
        maxStreak: this.currentSession.maxStreak
      })

      // 只保留最近50条记录
      if (history.length > 50) {
        history.splice(50)
      }

      localStorage.setItem('practice_history', JSON.stringify(history))
    },

    /**
     * 保存成就
     */
    saveAchievements() {
      const data = {
        achievements: this.achievements.map(a => ({
          ...a,
          unlockedAt: a.unlockedAt?.toISOString()
        })),
        totalXp: this.totalXp,
        level: this.userLevel
      }
      localStorage.setItem('achievements', JSON.stringify(data))
    },

    /**
     * 加载成就
     */
    loadAchievements() {
      const data = localStorage.getItem('achievements')
      if (!data) return

      try {
        const parsed = JSON.parse(data)
        this.achievements = parsed.achievements.map((a: any) => ({
          ...a,
          unlockedAt: a.unlockedAt ? new Date(a.unlockedAt) : undefined
        }))
        this.totalXp = parsed.totalXp || 0
      } catch (e) {
        console.error('Failed to load achievements:', e)
      }
    },

    /**
     * 恢复会话
     */
    restoreSession() {
      const data = localStorage.getItem('current_practice_session')
      if (!data) return false

      try {
        const session = JSON.parse(data)
        this.currentSession = {
          ...session,
          userAnswers: new Map(Object.entries(session.userAnswers)),
          timeRecords: new Map(Object.entries(session.timeRecords)),
          startTime: new Date(session.startTime),
          endTime: session.endTime ? new Date(session.endTime) : undefined
        }

        this.questions = session.questions
        this.currentIndex = session.currentIndex || 0
        this.userAnswers = this.currentSession.userAnswers
        this.isPracticing = true

        return true
      } catch (e) {
        console.error('Failed to restore session:', e)
        return false
      }
    }
  }
})
