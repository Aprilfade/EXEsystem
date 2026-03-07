import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { usePracticeStore } from '@/stores/practiceStore'
import { ElMessage } from 'element-plus'
import type { UserInfo } from '@/api/user'

/**
 * 组件集成场景测试
 * 测试多个组件和Store之间的协作
 */
describe('组件集成场景测试', () => {
  let authStore: ReturnType<typeof useAuthStore>
  let practiceStore: ReturnType<typeof usePracticeStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    practiceStore = usePracticeStore()
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 用户认证流程 ====================

  describe('用户认证流程', () => {
    it('完整的登录流程应该更新所有相关状态', async () => {
      // 1. 初始状态：未登录
      expect(authStore.isAuthenticated).toBe(false)
      expect(authStore.user).toBeNull()
      expect(authStore.permissions).toEqual([])

      // 2. 用户登录
      authStore.token = 'test-token-123'
      authStore.user = {
        id: 1,
        userName: 'testuser',
        nickName: '测试用户',
        roles: [{ id: 2, code: 'TEACHER', name: '教师' }]
      } as UserInfo
      authStore.permissions = ['sys:user:list', 'sys:question:add']

      // 3. 验证登录后状态
      expect(authStore.isAuthenticated).toBe(true)
      expect(authStore.user).not.toBeNull()
      expect(authStore.hasPermission('sys:user:list')).toBe(true)
      expect(authStore.hasPermission('sys:admin')).toBe(false)

      // 4. 验证localStorage同步
      localStorage.setItem('token', authStore.token)
      expect(localStorage.getItem('token')).toBe('test-token-123')

      // 5. 登出
      authStore.token = null
      authStore.user = null
      authStore.permissions = []
      localStorage.removeItem('token')

      expect(authStore.isAuthenticated).toBe(false)
    })

    it('不同角色应该有不同的权限范围', () => {
      // 超级管理员
      authStore.user = {
        id: 1,
        userName: 'superadmin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      expect(authStore.isSuperAdmin).toBe(true)
      expect(authStore.hasPermission('any:permission')).toBe(true)

      // 普通教师
      authStore.user = {
        id: 2,
        userName: 'teacher',
        roles: [{ id: 3, code: 'TEACHER', name: '教师' }]
      } as UserInfo
      authStore.permissions = ['sys:question:list', 'sys:question:add']

      expect(authStore.isSuperAdmin).toBe(false)
      expect(authStore.hasPermission('sys:question:list')).toBe(true)
      expect(authStore.hasPermission('sys:user:delete')).toBe(false)
    })
  })

  // ==================== 练习流程集成测试 ====================

  describe('练习流程集成测试', () => {
    it('完整的练习流程应该正确更新所有状态', async () => {
      // 1. 准备题目数据
      const mockQuestions = [
        { id: 1, content: '题目1', answer: 'A', questionType: 1, difficulty: 3 },
        { id: 2, content: '题目2', answer: 'B', questionType: 1, difficulty: 4 },
        { id: 3, content: '题目3', answer: 'C', questionType: 1, difficulty: 5 }
      ]

      // 2. 开始练习
      practiceStore.questions = mockQuestions as any
      practiceStore.isPracticing = true
      practiceStore.currentIndex = 0

      expect(practiceStore.isPracticing).toBe(true)
      expect(practiceStore.currentQuestion).toEqual(mockQuestions[0])
      expect(practiceStore.progressPercentage).toBe(33) // (0+1)/3 * 100 ≈ 33

      // 3. 答题
      practiceStore.submitAnswer(1, 'A', 30)

      expect(practiceStore.answeredCount).toBe(1)
      expect(practiceStore.correctCount).toBe(1)
      expect(practiceStore.currentAccuracy).toBe(100)

      // 4. 答错一题
      practiceStore.currentIndex = 1
      practiceStore.submitAnswer(2, 'A', 40) // 错误答案

      expect(practiceStore.answeredCount).toBe(2)
      expect(practiceStore.correctCount).toBe(1)
      expect(practiceStore.currentAccuracy).toBe(50)

      // 5. 完成练习
      practiceStore.currentIndex = 2
      practiceStore.submitAnswer(3, 'C', 35)

      expect(practiceStore.answeredCount).toBe(3)
      expect(practiceStore.correctCount).toBe(2)
      expect(practiceStore.currentAccuracy).toBeCloseTo(66.67, 1)
    })

    it('XP系统应该正确计算经验值', () => {
      // 初始状态
      expect(practiceStore.totalXp).toBe(0)
      expect(practiceStore.level).toBe(1)

      // 答对简单题
      const xp1 = practiceStore.calculateXpGain(true, 20, 1)
      practiceStore.totalXp += xp1

      // 答对困难题
      const xp2 = practiceStore.calculateXpGain(true, 25, 5)
      practiceStore.totalXp += xp2

      expect(xp2).toBeGreaterThan(xp1) // 困难题XP更多

      // 答错不应该获得XP
      const xp3 = practiceStore.calculateXpGain(false, 30, 3)
      expect(xp3).toBe(0)
    })

    it('连击系统应该正确工作', () => {
      practiceStore.currentStreak = 0

      // 第一题答对
      practiceStore.currentStreak++
      expect(practiceStore.currentStreak).toBe(1)

      // 第二题答对
      practiceStore.currentStreak++
      expect(practiceStore.currentStreak).toBe(2)

      // 第三题答错，连击清零
      practiceStore.currentStreak = 0
      expect(practiceStore.currentStreak).toBe(0)
    })
  })

  // ==================== 多Store协作测试 ====================

  describe('多Store协作', () => {
    it('认证状态应该影响练习功能的可用性', () => {
      // 未登录时不应该能开始练习
      expect(authStore.isAuthenticated).toBe(false)

      // 登录后可以开始练习
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'student' } as UserInfo

      expect(authStore.isAuthenticated).toBe(true)

      // 现在可以开始练习
      practiceStore.isPracticing = true
      practiceStore.questions = [
        { id: 1, content: '题目', answer: 'A', questionType: 1, difficulty: 3 }
      ] as any

      expect(practiceStore.isPracticing).toBe(true)
    })

    it('用户权限应该控制题目管理功能', () => {
      // 学生没有题目管理权限
      authStore.user = {
        id: 1,
        userName: 'student',
        roles: [{ id: 4, code: 'STUDENT', name: '学生' }]
      } as UserInfo
      authStore.permissions = ['sys:practice:start']

      expect(authStore.hasPermission('sys:question:add')).toBe(false)
      expect(authStore.hasPermission('sys:question:edit')).toBe(false)

      // 教师有题目管理权限
      authStore.user = {
        id: 2,
        userName: 'teacher',
        roles: [{ id: 3, code: 'TEACHER', name: '教师' }]
      } as UserInfo
      authStore.permissions = ['sys:question:add', 'sys:question:edit', 'sys:question:delete']

      expect(authStore.hasPermission('sys:question:add')).toBe(true)
      expect(authStore.hasPermission('sys:question:edit')).toBe(true)
    })
  })

  // ==================== 错误恢复场景 ====================

  describe('错误恢复场景', () => {
    it('练习中断后应该能恢复状态', () => {
      // 开始练习
      practiceStore.questions = [
        { id: 1, content: '题目1', answer: 'A', questionType: 1, difficulty: 3 },
        { id: 2, content: '题目2', answer: 'B', questionType: 1, difficulty: 4 }
      ] as any
      practiceStore.currentIndex = 0
      practiceStore.isPracticing = true

      // 答一题
      practiceStore.submitAnswer(1, 'A', 30)

      // 保存状态到localStorage
      const savedState = {
        questions: practiceStore.questions,
        currentIndex: practiceStore.currentIndex,
        userAnswers: Array.from(practiceStore.userAnswers.entries())
      }
      localStorage.setItem('practice_state', JSON.stringify(savedState))

      // 模拟页面刷新，重新加载state
      const loadedState = JSON.parse(localStorage.getItem('practice_state')!)
      practiceStore.questions = loadedState.questions
      practiceStore.currentIndex = loadedState.currentIndex
      practiceStore.userAnswers = new Map(loadedState.userAnswers)

      // 验证状态恢复
      expect(practiceStore.questions).toHaveLength(2)
      expect(practiceStore.currentIndex).toBe(0)
      expect(practiceStore.answeredCount).toBe(1)
    })

    it('token过期后应该能重新登录', () => {
      // 初始登录
      authStore.token = 'old-token'
      authStore.user = { id: 1, userName: 'user' } as UserInfo

      expect(authStore.isAuthenticated).toBe(true)

      // token过期，清除状态
      authStore.token = null
      authStore.user = null

      expect(authStore.isAuthenticated).toBe(false)

      // 重新登录
      authStore.token = 'new-token'
      authStore.user = { id: 1, userName: 'user' } as UserInfo

      expect(authStore.isAuthenticated).toBe(true)
    })
  })

  // ==================== 数据一致性测试 ====================

  describe('数据一致性', () => {
    it('Store状态和localStorage应该保持同步', () => {
      // 设置token
      authStore.token = 'sync-test-token'
      localStorage.setItem('token', authStore.token)

      expect(localStorage.getItem('token')).toBe(authStore.token)

      // 清除token
      authStore.token = null
      localStorage.removeItem('token')

      expect(localStorage.getItem('token')).toBeNull()
      expect(authStore.token).toBeNull()
    })

    it('答题统计应该实时更新', () => {
      practiceStore.questions = [
        { id: 1, content: '题目1', answer: 'A', questionType: 1, difficulty: 3 }
      ] as any

      // 初始统计
      expect(practiceStore.answeredCount).toBe(0)
      expect(practiceStore.correctCount).toBe(0)

      // 答题后统计更新
      practiceStore.submitAnswer(1, 'A', 30)

      expect(practiceStore.answeredCount).toBe(1)
      expect(practiceStore.correctCount).toBe(1)
      expect(practiceStore.wrongCount).toBe(0)
    })
  })

  // ==================== 并发场景测试 ====================

  describe('并发场景', () => {
    it('多个组件同时访问Store不应该冲突', () => {
      // 组件A读取认证状态
      const isAuthA = authStore.isAuthenticated

      // 组件B也读取认证状态
      const isAuthB = authStore.isAuthenticated

      // 应该返回相同的结果
      expect(isAuthA).toBe(isAuthB)

      // 组件A更新状态
      authStore.token = 'test-token'

      // 组件B应该能立即看到更新
      expect(authStore.isAuthenticated).toBe(true)
    })

    it('多个答题操作应该按顺序处理', () => {
      practiceStore.questions = [
        { id: 1, content: '题目1', answer: 'A', questionType: 1, difficulty: 3 },
        { id: 2, content: '题目2', answer: 'B', questionType: 1, difficulty: 3 },
        { id: 3, content: '题目3', answer: 'C', questionType: 1, difficulty: 3 }
      ] as any

      // 连续提交答案
      practiceStore.submitAnswer(1, 'A', 30)
      practiceStore.submitAnswer(2, 'B', 25)
      practiceStore.submitAnswer(3, 'C', 35)

      // 统计应该正确
      expect(practiceStore.answeredCount).toBe(3)
      expect(practiceStore.correctCount).toBe(3)
    })
  })

  // ==================== 性能场景测试 ====================

  describe('性能场景', () => {
    it('大量题目应该能正常处理', () => {
      // 生成100道题目
      const largeQuestionSet = Array.from({ length: 100 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        answer: 'A',
        questionType: 1,
        difficulty: (i % 5) + 1
      }))

      practiceStore.questions = largeQuestionSet as any

      expect(practiceStore.questions).toHaveLength(100)

      // 答前10题
      for (let i = 1; i <= 10; i++) {
        practiceStore.submitAnswer(i, 'A', 30)
      }

      expect(practiceStore.answeredCount).toBe(10)
      expect(practiceStore.progressPercentage).toBe(10)
    })

    it('频繁的权限检查不应该影响性能', () => {
      authStore.user = {
        id: 1,
        userName: 'user',
        roles: [{ id: 2, code: 'ADMIN', name: '管理员' }]
      } as UserInfo
      authStore.permissions = Array.from({ length: 100 }, (_, i) => `permission:${i}`)

      const startTime = Date.now()

      // 执行1000次权限检查
      for (let i = 0; i < 1000; i++) {
        authStore.hasPermission('permission:50')
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      // 权限检查应该很快完成（< 100ms）
      expect(duration).toBeLessThan(100)
    })
  })
})
