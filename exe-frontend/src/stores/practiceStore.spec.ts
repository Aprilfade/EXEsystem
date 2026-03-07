import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePracticeStore } from '@/stores/practiceStore'
import type { Question, PracticeConfig } from '@/types/practice'

describe('Practice Store测试', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  // ==================== 初始状态测试 ====================

  describe('初始状态', () => {
    it('应该正确初始化状态', () => {
      const store = usePracticeStore()

      expect(store.currentSession).toBeNull()
      expect(store.questions).toEqual([])
      expect(store.currentIndex).toBe(0)
      expect(store.userAnswers.size).toBe(0)
      expect(store.difficultyLevel).toBe(3)
      expect(store.currentStreak).toBe(0)
      expect(store.totalXp).toBe(0)
      expect(store.level).toBe(1)
      expect(store.isPracticing).toBe(false)
      expect(store.showResults).toBe(false)
    })
  })

  // ==================== Getters测试 ====================

  describe('Getters', () => {
    it('currentQuestion应该返回当前题目', () => {
      const store = usePracticeStore()
      const mockQuestions: Question[] = [
        { id: 1, content: '题目1', answer: 'A', questionType: 1, difficulty: 3 } as Question,
        { id: 2, content: '题目2', answer: 'B', questionType: 1, difficulty: 3 } as Question
      ]

      store.questions = mockQuestions
      store.currentIndex = 0

      expect(store.currentQuestion).toEqual(mockQuestions[0])
    })

    it('没有题目时currentQuestion应该返回null', () => {
      const store = usePracticeStore()

      expect(store.currentQuestion).toBeNull()
    })

    it('progressPercentage应该正确计算进度', () => {
      const store = usePracticeStore()
      store.questions = Array(10).fill(null).map((_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        difficulty: 3
      } as Question))
      store.currentIndex = 4 // 第5题

      expect(store.progressPercentage).toBe(50) // (4+1)/10 * 100 = 50%
    })

    it('answeredCount应该返回已答题数', () => {
      const store = usePracticeStore()
      store.userAnswers.set(1, { questionId: 1, answer: 'A', isCorrect: true, timeSpent: 30, submitTime: new Date() })
      store.userAnswers.set(2, { questionId: 2, answer: 'B', isCorrect: false, timeSpent: 45, submitTime: new Date() })

      expect(store.answeredCount).toBe(2)
    })

    it('correctCount应该返回答对题数', () => {
      const store = usePracticeStore()
      store.userAnswers.set(1, { questionId: 1, answer: 'A', isCorrect: true, timeSpent: 30, submitTime: new Date() })
      store.userAnswers.set(2, { questionId: 2, answer: 'B', isCorrect: false, timeSpent: 45, submitTime: new Date() })
      store.userAnswers.set(3, { questionId: 3, answer: 'C', isCorrect: true, timeSpent: 20, submitTime: new Date() })

      expect(store.correctCount).toBe(2)
    })

    it('wrongCount应该返回答错题数', () => {
      const store = usePracticeStore()
      store.userAnswers.set(1, { questionId: 1, answer: 'A', isCorrect: true, timeSpent: 30, submitTime: new Date() })
      store.userAnswers.set(2, { questionId: 2, answer: 'B', isCorrect: false, timeSpent: 45, submitTime: new Date() })
      store.userAnswers.set(3, { questionId: 3, answer: 'C', isCorrect: false, timeSpent: 50, submitTime: new Date() })

      expect(store.wrongCount).toBe(2)
    })

    it('currentAccuracy应该正确计算正确率', () => {
      const store = usePracticeStore()
      store.userAnswers.set(1, { questionId: 1, answer: 'A', isCorrect: true, timeSpent: 30, submitTime: new Date() })
      store.userAnswers.set(2, { questionId: 2, answer: 'B', isCorrect: true, timeSpent: 25, submitTime: new Date() })
      store.userAnswers.set(3, { questionId: 3, answer: 'C', isCorrect: false, timeSpent: 40, submitTime: new Date() })
      store.userAnswers.set(4, { questionId: 4, answer: 'D', isCorrect: true, timeSpent: 35, submitTime: new Date() })

      expect(store.currentAccuracy).toBe(75) // 3/4 * 100 = 75%
    })

    it('isCompleted应该判断是否完成所有题目', () => {
      const store = usePracticeStore()
      store.questions = Array(5).fill(null).map((_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        difficulty: 3
      } as Question))

      store.userAnswers.set(1, { questionId: 1, answer: 'A', isCorrect: true, timeSpent: 30, submitTime: new Date() })
      store.userAnswers.set(2, { questionId: 2, answer: 'B', isCorrect: true, timeSpent: 25, submitTime: new Date() })

      expect(store.isCompleted).toBe(false)

      store.userAnswers.set(3, { questionId: 3, answer: 'C', isCorrect: true, timeSpent: 30, submitTime: new Date() })
      store.userAnswers.set(4, { questionId: 4, answer: 'D', isCorrect: true, timeSpent: 30, submitTime: new Date() })
      store.userAnswers.set(5, { questionId: 5, answer: 'A', isCorrect: true, timeSpent: 30, submitTime: new Date() })

      expect(store.isCompleted).toBe(true)
    })

    it('userLevel应该根据XP计算等级', () => {
      const store = usePracticeStore()

      store.totalXp = 0
      expect(store.userLevel).toBe(1)

      store.totalXp = 1000
      expect(store.userLevel).toBe(2)

      store.totalXp = 2500
      expect(store.userLevel).toBe(3)
    })
  })

  // ==================== 开始练习测试 ====================

  describe('开始练习', () => {
    it('应该成功开始练习', async () => {
      const store = usePracticeStore()
      const mockConfig: PracticeConfig = {
        subjectId: 1,
        subjectName: '数学',
        questionCount: 10,
        difficulty: 3,
        questionTypes: [1, 2]
      }
      const mockQuestions: Question[] = Array(10).fill(null).map((_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        questionType: 1,
        difficulty: 3
      } as Question))

      await store.startPractice(mockConfig, mockQuestions)

      expect(store.currentSession).not.toBeNull()
      expect(store.questions).toEqual(mockQuestions)
      expect(store.currentIndex).toBe(0)
      expect(store.isPracticing).toBe(true)
      expect(store.showResults).toBe(false)
    })

    it('开始练习应该记录今天的日期', async () => {
      const store = usePracticeStore()
      const today = new Date().toISOString().split('T')[0]

      await store.startPractice(
        { subjectId: 1, questionCount: 5 } as PracticeConfig,
        Array(5).fill(null).map((_, i) => ({ id: i + 1, content: `题目${i}`, answer: 'A', difficulty: 3 } as Question))
      )

      expect(store.practiceHistory.has(today)).toBe(true)
    })
  })

  // ==================== 提交答案测试 ====================

  describe('提交答案', () => {
    beforeEach(async () => {
      const store = usePracticeStore()
      const mockQuestions: Question[] = [
        { id: 1, content: '1+1=?', answer: '2', questionType: 3, difficulty: 1, subject: '数学' } as Question,
        { id: 2, content: '选择A', answer: 'A', questionType: 1, difficulty: 2, subject: '数学' } as Question
      ]

      await store.startPractice({ subjectId: 1, questionCount: 2 } as PracticeConfig, mockQuestions)
    })

    it('应该正确提交答案', async () => {
      const store = usePracticeStore()

      await store.submitAnswer(1, '2', 30)

      expect(store.userAnswers.size).toBe(1)
      expect(store.userAnswers.get(1)?.answer).toBe('2')
      expect(store.userAnswers.get(1)?.isCorrect).toBe(true)
      expect(store.userAnswers.get(1)?.timeSpent).toBe(30)
    })

    it('答对题目应该增加连击数', async () => {
      const store = usePracticeStore()

      await store.submitAnswer(1, '2', 30)
      expect(store.currentStreak).toBe(1)

      await store.submitAnswer(2, 'A', 25)
      expect(store.currentStreak).toBe(2)
    })

    it('答错题目应该重置连击数', async () => {
      const store = usePracticeStore()

      await store.submitAnswer(1, '2', 30)
      expect(store.currentStreak).toBe(1)

      await store.submitAnswer(2, 'B', 40) // 错误答案
      expect(store.currentStreak).toBe(0)
    })

    it('应该更新学科统计', async () => {
      const store = usePracticeStore()

      await store.submitAnswer(1, '2', 30) // 正确
      await store.submitAnswer(2, 'B', 40) // 错误

      const mathStats = store.subjectStatistics.get('数学')
      expect(mathStats?.totalQuestions).toBe(2)
      expect(mathStats?.correctQuestions).toBe(1)
      expect(mathStats?.accuracy).toBe(50)
    })

    it('答对应该获得XP', async () => {
      const store = usePracticeStore()
      const initialXp = store.totalXp

      await store.submitAnswer(1, '2', 30)

      expect(store.totalXp).toBeGreaterThan(initialXp)
    })

    it('答错不应该获得XP', async () => {
      const store = usePracticeStore()
      const initialXp = store.totalXp

      await store.submitAnswer(1, 'wrong', 30)

      expect(store.totalXp).toBe(initialXp)
    })
  })

  // ==================== 答案检查测试 ====================

  describe('答案检查', () => {
    it('应该正确检查单选题答案', () => {
      const store = usePracticeStore()
      const question: Question = { id: 1, content: '题目', answer: 'A', questionType: 1, difficulty: 3 } as Question

      expect(store.checkAnswer(question, 'A')).toBe(true)
      expect(store.checkAnswer(question, 'a')).toBe(true) // 不区分大小写
      expect(store.checkAnswer(question, 'B')).toBe(false)
    })

    it('应该正确检查多选题答案', () => {
      const store = usePracticeStore()
      const question: Question = { id: 1, content: '题目', answer: 'a,b,c', questionType: 2, difficulty: 3 } as Question

      expect(store.checkAnswer(question, ['A', 'B', 'C'])).toBe(true)
      expect(store.checkAnswer(question, ['C', 'A', 'B'])).toBe(true) // 顺序无关
      expect(store.checkAnswer(question, ['A', 'B'])).toBe(false) // 不完整
    })

    it('应该正确检查填空题答案', () => {
      const store = usePracticeStore()
      const question: Question = { id: 1, content: '题目', answer: '答案', questionType: 3, difficulty: 3 } as Question

      expect(store.checkAnswer(question, '答案')).toBe(true)
      expect(store.checkAnswer(question, ' 答案 ')).toBe(true) // trim空格
      expect(store.checkAnswer(question, '错误答案')).toBe(false)
    })

    it('应该支持多个正确答案（用###分隔）', () => {
      const store = usePracticeStore()
      const question: Question = { id: 1, content: '题目', answer: '答案1###答案2###答案3', questionType: 3, difficulty: 3 } as Question

      expect(store.checkAnswer(question, '答案1')).toBe(true)
      expect(store.checkAnswer(question, '答案2')).toBe(true)
      expect(store.checkAnswer(question, '答案3')).toBe(true)
      expect(store.checkAnswer(question, '答案4')).toBe(false)
    })
  })

  // ==================== 导航测试 ====================

  describe('题目导航', () => {
    beforeEach(async () => {
      const store = usePracticeStore()
      const mockQuestions: Question[] = Array(5).fill(null).map((_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        difficulty: 3
      } as Question))

      await store.startPractice({ subjectId: 1, questionCount: 5 } as PracticeConfig, mockQuestions)
    })

    it('nextQuestion应该跳到下一题', () => {
      const store = usePracticeStore()
      store.currentIndex = 0

      store.nextQuestion()

      expect(store.currentIndex).toBe(1)
    })

    it('最后一题时nextQuestion不应该越界', () => {
      const store = usePracticeStore()
      store.currentIndex = 4 // 最后一题

      store.nextQuestion()

      expect(store.currentIndex).toBe(4)
    })

    it('previousQuestion应该跳到上一题', () => {
      const store = usePracticeStore()
      store.currentIndex = 2

      store.previousQuestion()

      expect(store.currentIndex).toBe(1)
    })

    it('第一题时previousQuestion不应该越界', () => {
      const store = usePracticeStore()
      store.currentIndex = 0

      store.previousQuestion()

      expect(store.currentIndex).toBe(0)
    })

    it('goToQuestion应该跳到指定题目', () => {
      const store = usePracticeStore()

      store.goToQuestion(3)

      expect(store.currentIndex).toBe(3)
    })

    it('goToQuestion不应该跳到无效索引', () => {
      const store = usePracticeStore()
      store.currentIndex = 2

      store.goToQuestion(10) // 超出范围
      expect(store.currentIndex).toBe(2) // 不变

      store.goToQuestion(-1) // 负数
      expect(store.currentIndex).toBe(2) // 不变
    })
  })

  // ==================== 练习控制测试 ====================

  describe('练习控制', () => {
    it('pausePractice应该暂停练习', () => {
      const store = usePracticeStore()
      store.isPaused = false

      store.pausePractice()

      expect(store.isPaused).toBe(true)
    })

    it('resumePractice应该恢复练习', () => {
      const store = usePracticeStore()
      store.isPaused = true

      store.resumePractice()

      expect(store.isPaused).toBe(false)
    })

    it('completePractice应该完成练习', async () => {
      const store = usePracticeStore()
      await store.startPractice(
        { subjectId: 1, questionCount: 2 } as PracticeConfig,
        Array(2).fill(null).map((_, i) => ({ id: i + 1, content: `题目${i}`, answer: 'A', difficulty: 3 } as Question))
      )

      store.completePractice()

      expect(store.isPracticing).toBe(false)
      expect(store.showResults).toBe(true)
      expect(store.currentSession?.endTime).toBeDefined()
    })

    it('resetPractice应该重置所有状态', async () => {
      const store = usePracticeStore()
      await store.startPractice(
        { subjectId: 1, questionCount: 2 } as PracticeConfig,
        Array(2).fill(null).map((_, i) => ({ id: i + 1, content: `题目${i}`, answer: 'A', difficulty: 3 } as Question))
      )

      store.resetPractice()

      expect(store.currentSession).toBeNull()
      expect(store.questions).toEqual([])
      expect(store.currentIndex).toBe(0)
      expect(store.userAnswers.size).toBe(0)
      expect(store.isPracticing).toBe(false)
    })
  })

  // ==================== XP计算测试 ====================

  describe('XP计算', () => {
    it('答对应该根据难度获得XP', () => {
      const store = usePracticeStore()

      const xp1 = store.calculateXpGain(true, 30, 1) // 简单题
      const xp5 = store.calculateXpGain(true, 30, 5) // 困难题

      expect(xp5).toBeGreaterThan(xp1) // 困难题XP更多
    })

    it('快速答题应该有速度加成', () => {
      const store = usePracticeStore()

      const xpFast = store.calculateXpGain(true, 20, 3) // 20秒
      const xpSlow = store.calculateXpGain(true, 80, 3) // 80秒

      expect(xpFast).toBeGreaterThan(xpSlow)
    })

    it('连击数应该增加XP', () => {
      const store = usePracticeStore()
      store.currentStreak = 0

      const xpNoStreak = store.calculateXpGain(true, 30, 3)

      store.currentStreak = 5
      const xpWithStreak = store.calculateXpGain(true, 30, 3)

      expect(xpWithStreak).toBeGreaterThan(xpNoStreak)
    })

    it('答错不应该获得XP', () => {
      const store = usePracticeStore()

      const xp = store.calculateXpGain(false, 30, 3)

      expect(xp).toBe(0)
    })
  })

  // ==================== 连续练习天数测试 ====================

  describe('连续练习天数', () => {
    it('应该正确计算连续练习天数', () => {
      const store = usePracticeStore()
      const today = new Date()
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
      const dayBeforeYesterday = new Date(today.getTime() - 2 * 24 * 60 * 60 * 1000)

      store.practiceHistory.add(today.toISOString().split('T')[0])
      store.practiceHistory.add(yesterday.toISOString().split('T')[0])
      store.practiceHistory.add(dayBeforeYesterday.toISOString().split('T')[0])

      const consecutiveDays = store.getConsecutivePracticeDays()

      expect(consecutiveDays).toBe(3)
    })

    it('中断后应该重新计数', () => {
      const store = usePracticeStore()
      const today = new Date()
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
      const threeDaysAgo = new Date(today.getTime() - 3 * 24 * 60 * 60 * 1000)

      store.practiceHistory.add(today.toISOString().split('T')[0])
      store.practiceHistory.add(yesterday.toISOString().split('T')[0])
      store.practiceHistory.add(threeDaysAgo.toISOString().split('T')[0])

      const consecutiveDays = store.getConsecutivePracticeDays()

      expect(consecutiveDays).toBe(2) // 只计算连续的天数
    })
  })
})
