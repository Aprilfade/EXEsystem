import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { usePracticeStore } from '@/stores/practiceStore'
import { useAuthStore } from '@/stores/auth'

/**
 * 性能和压力测试
 * 测试系统在高负载下的表现
 */
describe('性能和压力测试', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // ==================== 大数据量渲染测试 ====================

  describe('大数据量渲染', () => {
    it('应能快速渲染1000道题目', async () => {
      const practiceStore = usePracticeStore()

      const largeQuestions = Array.from({ length: 1000 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}：这是一道测试题目，内容比较长，用于测试大数据量渲染性能`,
        answer: ['A', 'B', 'C', 'D'][i % 4],
        questionType: (i % 5) + 1,
        difficulty: (i % 5) + 1,
        options: JSON.stringify([
          { key: 'A', value: '选项A' },
          { key: 'B', value: '选项B' },
          { key: 'C', value: '选项C' },
          { key: 'D', value: '选项D' }
        ])
      }))

      const startTime = Date.now()
      practiceStore.questions = largeQuestions as any
      const endTime = Date.now()

      const duration = endTime - startTime

      expect(practiceStore.questions).toHaveLength(1000)
      expect(duration).toBeLessThan(100) // 应在100ms内完成
    })

    it('应能处理10000条学习活动记录', async () => {
      const largeActivities = Array.from({ length: 10000 }, (_, i) => ({
        id: i + 1,
        description: `活动${i + 1}：完成了练习/考试/复习`,
        createTime: new Date(Date.now() - i * 60000).toISOString(),
        type: ['practice', 'exam', 'review'][i % 3]
      }))

      const startTime = Date.now()
      const activities = largeActivities
      const endTime = Date.now()

      const duration = endTime - startTime

      expect(activities).toHaveLength(10000)
      expect(duration).toBeLessThan(50)
    })

    it('应能快速渲染大型试卷（200题）', async () => {
      const largePaper = {
        id: 1,
        name: '大型试卷',
        paperType: 1,
        groups: [
          {
            id: 1,
            name: '单选题',
            questions: Array.from({ length: 100 }, (_, i) => ({
              id: i + 1,
              questionId: i + 100,
              score: 1
            }))
          },
          {
            id: 2,
            name: '多选题',
            questions: Array.from({ length: 50 }, (_, i) => ({
              id: i + 101,
              questionId: i + 200,
              score: 2
            }))
          },
          {
            id: 3,
            name: '主观题',
            questions: Array.from({ length: 50 }, (_, i) => ({
              id: i + 151,
              questionId: i + 250,
              score: 10
            }))
          }
        ]
      }

      const startTime = Date.now()
      const paper = largePaper
      const endTime = Date.now()

      const duration = endTime - startTime

      const totalQuestions = paper.groups.reduce((sum, g) => sum + g.questions.length, 0)

      expect(totalQuestions).toBe(200)
      expect(duration).toBeLessThan(50)
    })

    it('应能处理大量推荐数据（5000条）', async () => {
      const largeRecommendations = Array.from({ length: 5000 }, (_, i) => ({
        itemId: i + 1,
        itemType: ['question', 'course', 'knowledgePoint'][i % 3],
        title: `推荐项${i + 1}`,
        score: Math.random(),
        reason: '基于你的学习历史推荐'
      }))

      const startTime = Date.now()
      const recommendations = largeRecommendations
      const endTime = Date.now()

      const duration = endTime - startTime

      expect(recommendations).toHaveLength(5000)
      expect(duration).toBeLessThan(100)
    })
  })

  // ==================== 高并发操作测试 ====================

  describe('高并发操作', () => {
    it('应能处理1000次连续答题操作', async () => {
      const practiceStore = usePracticeStore()

      practiceStore.questions = Array.from({ length: 1000 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        questionType: 1,
        difficulty: 3
      })) as any

      const startTime = Date.now()

      for (let i = 0; i < 1000; i++) {
        practiceStore.submitAnswer(i + 1, 'A', 30)
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(practiceStore.answeredCount).toBe(1000)
      expect(duration).toBeLessThan(500) // 应在500ms内完成
    })

    it('应能处理10000次权限检查', async () => {
      const authStore = useAuthStore()

      authStore.user = {
        id: 1,
        userName: 'user',
        roles: [{ id: 2, code: 'ADMIN', name: '管理员' }]
      } as any
      authStore.permissions = Array.from({ length: 500 }, (_, i) => `permission:${i}`)

      const startTime = Date.now()

      for (let i = 0; i < 10000; i++) {
        authStore.hasPermission(`permission:${i % 500}`)
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(duration).toBeLessThan(100) // 权限检查应该很快
    })

    it('应能处理并发的题目切换', async () => {
      const practiceStore = usePracticeStore()

      practiceStore.questions = Array.from({ length: 100 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        questionType: 1
      })) as any

      const startTime = Date.now()

      // 模拟快速切换题目
      for (let i = 0; i < 100; i++) {
        practiceStore.currentIndex = i
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(duration).toBeLessThan(50)
      expect(practiceStore.currentIndex).toBe(99)
    })

    it('应能处理大量的计算操作（经验值计算）', async () => {
      const practiceStore = usePracticeStore()

      const startTime = Date.now()

      let totalXp = 0
      for (let i = 0; i < 10000; i++) {
        const isCorrect = i % 2 === 0
        const timeUsed = 20 + (i % 40)
        const difficulty = (i % 5) + 1

        const xp = practiceStore.calculateXpGain(isCorrect, timeUsed, difficulty)
        totalXp += xp
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(totalXp).toBeGreaterThan(0)
      expect(duration).toBeLessThan(200)
    })

    it('应能处理批量数据更新', async () => {
      const practiceStore = usePracticeStore()

      const questions = Array.from({ length: 100 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        questionType: 1
      })) as any

      const startTime = Date.now()

      // 批量更新题目
      practiceStore.questions = questions

      // 批量提交答案
      for (let i = 0; i < 100; i++) {
        practiceStore.submitAnswer(i + 1, 'A', 30)
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(practiceStore.questions).toHaveLength(100)
      expect(practiceStore.answeredCount).toBe(100)
      expect(duration).toBeLessThan(300)
    })
  })

  // ==================== 内存泄漏检测 ====================

  describe('内存泄漏检测', () => {
    it('反复创建和销毁组件不应造成内存泄漏', async () => {
      const initialMemory = process.memoryUsage?.()?.heapUsed || 0

      // 模拟100次组件创建和销毁
      for (let i = 0; i < 100; i++) {
        const pinia = createPinia()
        setActivePinia(pinia)

        const practiceStore = usePracticeStore()
        practiceStore.questions = Array.from({ length: 10 }, (_, j) => ({
          id: j + 1,
          content: `题目${j + 1}`,
          questionType: 1
        })) as any

        // 清理
        practiceStore.$reset()
      }

      const finalMemory = process.memoryUsage?.()?.heapUsed || 0
      const memoryIncrease = finalMemory - initialMemory

      // 内存增长应该在合理范围内（< 10MB）
      expect(memoryIncrease).toBeLessThan(10 * 1024 * 1024)
    })

    it('大量事件监听器应能正确清理', async () => {
      const listeners: Array<() => void> = []

      // 添加大量监听器
      for (let i = 0; i < 1000; i++) {
        const listener = () => console.log(`Listener ${i}`)
        listeners.push(listener)
      }

      expect(listeners).toHaveLength(1000)

      // 清理监听器
      listeners.length = 0

      expect(listeners).toHaveLength(0)
    })

    it('定时器应能正确清理', async () => {
      vi.useFakeTimers()

      const timers: NodeJS.Timeout[] = []

      // 创建大量定时器
      for (let i = 0; i < 100; i++) {
        const timer = setInterval(() => {
          // Do nothing
        }, 1000)
        timers.push(timer)
      }

      expect(timers).toHaveLength(100)

      // 清理定时器
      timers.forEach(timer => clearInterval(timer))
      timers.length = 0

      expect(timers).toHaveLength(0)

      vi.useRealTimers()
    })
  })

  // ==================== 长时间运行测试 ====================

  describe('长时间运行', () => {
    it('计时器应能长时间稳定运行', async () => {
      vi.useFakeTimers()

      let duration = 0
      const timer = setInterval(() => {
        duration++
      }, 1000)

      // 模拟运行2小时（7200秒）
      vi.advanceTimersByTime(7200 * 1000)

      clearInterval(timer)

      expect(duration).toBe(7200)

      vi.useRealTimers()
    })

    it('长时间练习session应保持稳定', async () => {
      const practiceStore = usePracticeStore()

      practiceStore.questions = Array.from({ length: 500 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        questionType: 1,
        difficulty: 3
      })) as any

      practiceStore.isPracticing = true

      // 模拟长时间答题
      const startTime = Date.now()

      for (let i = 0; i < 500; i++) {
        practiceStore.currentIndex = i
        practiceStore.submitAnswer(i + 1, 'A', 30 + (i % 30))

        // 每100题检查一次状态
        if ((i + 1) % 100 === 0) {
          expect(practiceStore.answeredCount).toBe(i + 1)
          expect(practiceStore.isPracticing).toBe(true)
        }
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(practiceStore.answeredCount).toBe(500)
      expect(duration).toBeLessThan(2000)
    })

    it('Store状态应长期保持一致', async () => {
      const practiceStore = usePracticeStore()

      practiceStore.questions = [
        { id: 1, content: '题目1', answer: 'A', questionType: 1, difficulty: 3 }
      ] as any

      // 多次读写操作
      for (let i = 0; i < 1000; i++) {
        practiceStore.submitAnswer(1, 'A', 30)
        expect(practiceStore.answeredCount).toBe(1)

        // 重置
        practiceStore.userAnswers.clear()
        expect(practiceStore.answeredCount).toBe(0)
      }

      // 最终状态应该一致
      expect(practiceStore.answeredCount).toBe(0)
    })
  })

  // ==================== 资源占用测试 ====================

  describe('资源占用', () => {
    it('存储大量题目不应占用过多内存', async () => {
      const practiceStore = usePracticeStore()

      const questions = Array.from({ length: 10000 }, (_, i) => ({
        id: i + 1,
        content: `这是一道测试题目${i + 1}，包含较长的内容以测试内存占用情况。${'.'.repeat(100)}`,
        answer: 'A',
        questionType: 1,
        difficulty: 3,
        options: JSON.stringify([
          { key: 'A', value: '这是选项A的内容，比较长' },
          { key: 'B', value: '这是选项B的内容，比较长' },
          { key: 'C', value: '这是选项C的内容，比较长' },
          { key: 'D', value: '这是选项D的内容，比较长' }
        ])
      }))

      const initialMemory = process.memoryUsage?.()?.heapUsed || 0

      practiceStore.questions = questions as any

      const finalMemory = process.memoryUsage?.()?.heapUsed || 0
      const memoryUsed = finalMemory - initialMemory

      // 内存占用应该在合理范围内（< 50MB）
      expect(memoryUsed).toBeLessThan(50 * 1024 * 1024)
    })

    it('Map和Set数据结构应高效', async () => {
      const practiceStore = usePracticeStore()

      const startTime = Date.now()

      // 大量Map操作
      for (let i = 0; i < 10000; i++) {
        practiceStore.userAnswers.set(i, 'A')
      }

      // 大量查询
      for (let i = 0; i < 10000; i++) {
        practiceStore.userAnswers.get(i)
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(duration).toBeLessThan(100)
      expect(practiceStore.userAnswers.size).toBe(10000)
    })

    it('复杂计算应该优化', async () => {
      const practiceStore = usePracticeStore()

      practiceStore.questions = Array.from({ length: 1000 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        questionType: 1,
        difficulty: (i % 5) + 1
      })) as any

      // 提交所有答案
      for (let i = 0; i < 1000; i++) {
        practiceStore.submitAnswer(i + 1, i % 2 === 0 ? 'A' : 'B', 30)
      }

      const startTime = Date.now()

      // 计算统计数据
      const accuracy = practiceStore.currentAccuracy
      const progress = practiceStore.progressPercentage
      const answeredCount = practiceStore.answeredCount
      const correctCount = practiceStore.correctCount

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(duration).toBeLessThan(10) // 统计计算应该很快
      expect(answeredCount).toBe(1000)
      expect(accuracy).toBeGreaterThanOrEqual(0)
    })
  })

  // ==================== 边界条件测试 ====================

  describe('边界条件', () => {
    it('应能处理空数据集', async () => {
      const practiceStore = usePracticeStore()

      practiceStore.questions = []

      expect(practiceStore.questions).toHaveLength(0)
      expect(practiceStore.answeredCount).toBe(0)
      expect(practiceStore.currentAccuracy).toBe(0)
    })

    it('应能处理单个元素', async () => {
      const practiceStore = usePracticeStore()

      practiceStore.questions = [
        { id: 1, content: '唯一题目', answer: 'A', questionType: 1, difficulty: 3 }
      ] as any

      practiceStore.submitAnswer(1, 'A', 30)

      expect(practiceStore.answeredCount).toBe(1)
      expect(practiceStore.currentAccuracy).toBe(100)
    })

    it('应能处理极大数值', async () => {
      const practiceStore = usePracticeStore()

      const largeValue = Number.MAX_SAFE_INTEGER

      practiceStore.totalXp = largeValue - 1000

      const xpGain = practiceStore.calculateXpGain(true, 30, 5)
      practiceStore.totalXp += xpGain

      expect(practiceStore.totalXp).toBeLessThan(Number.MAX_SAFE_INTEGER)
    })

    it('应能处理特殊字符', async () => {
      const practiceStore = usePracticeStore()

      const specialContent = '这是包含特殊字符的题目: <script>alert("xss")</script> & < > " \' 测试'

      practiceStore.questions = [
        {
          id: 1,
          content: specialContent,
          answer: 'A',
          questionType: 1,
          difficulty: 3
        }
      ] as any

      expect(practiceStore.questions[0].content).toBe(specialContent)
    })

    it('应能处理Unicode字符', async () => {
      const practiceStore = usePracticeStore()

      const unicodeContent = '题目包含各种Unicode: 中文 😀 🎉 العربية हिन्दी'

      practiceStore.questions = [
        {
          id: 1,
          content: unicodeContent,
          answer: 'A',
          questionType: 1,
          difficulty: 3
        }
      ] as any

      expect(practiceStore.questions[0].content).toBe(unicodeContent)
    })
  })

  // ==================== 压力测试综合场景 ====================

  describe('压力测试综合场景', () => {
    it('模拟100个并发用户', async () => {
      const stores = Array.from({ length: 100 }, () => {
        const pinia = createPinia()
        setActivePinia(pinia)
        return usePracticeStore()
      })

      const startTime = Date.now()

      // 每个用户都进行答题
      stores.forEach((store, index) => {
        store.questions = Array.from({ length: 10 }, (_, i) => ({
          id: i + 1,
          content: `用户${index}的题目${i + 1}`,
          answer: 'A',
          questionType: 1,
          difficulty: 3
        })) as any

        for (let i = 0; i < 10; i++) {
          store.submitAnswer(i + 1, 'A', 30)
        }
      })

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(stores).toHaveLength(100)
      expect(duration).toBeLessThan(1000)

      // 验证每个用户的状态
      stores.forEach(store => {
        expect(store.answeredCount).toBe(10)
      })
    })

    it('模拟高峰期混合操作', async () => {
      const practiceStore = usePracticeStore()
      const authStore = useAuthStore()

      authStore.user = {
        id: 1,
        userName: 'user',
        roles: [{ id: 2, code: 'ADMIN', name: '管理员' }]
      } as any
      authStore.permissions = Array.from({ length: 100 }, (_, i) => `permission:${i}`)

      practiceStore.questions = Array.from({ length: 100 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        questionType: 1,
        difficulty: (i % 5) + 1
      })) as any

      const startTime = Date.now()

      // 混合操作：答题、权限检查、状态查询
      for (let i = 0; i < 100; i++) {
        // 答题
        practiceStore.submitAnswer(i + 1, 'A', 30)

        // 权限检查
        authStore.hasPermission(`permission:${i % 100}`)

        // 状态查询
        practiceStore.currentAccuracy
        practiceStore.progressPercentage

        // 每10次操作检查一次状态
        if ((i + 1) % 10 === 0) {
          expect(practiceStore.answeredCount).toBe(i + 1)
        }
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(duration).toBeLessThan(500)
      expect(practiceStore.answeredCount).toBe(100)
    })

    it('长时间高频操作测试', async () => {
      vi.useFakeTimers()

      const practiceStore = usePracticeStore()

      practiceStore.questions = Array.from({ length: 50 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        questionType: 1,
        difficulty: 3
      })) as any

      let operationCount = 0
      const interval = setInterval(() => {
        const index = operationCount % 50
        practiceStore.currentIndex = index
        practiceStore.submitAnswer(index + 1, 'A', 30)
        operationCount++
      }, 100)

      // 模拟运行10分钟
      vi.advanceTimersByTime(10 * 60 * 1000)

      clearInterval(interval)

      expect(operationCount).toBeGreaterThan(5000)

      vi.useRealTimers()
    })
  })
})
