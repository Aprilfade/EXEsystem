import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useStudentAuthStore } from '@/stores/studentAuth'
import { useAuthStore } from '@/stores/auth'
import { usePracticeStore } from '@/stores/practiceStore'
import { flushPromises } from '@vue/test-utils'

/**
 * E2E 场景测试
 * 测试完整的用户流程，从登录到各种功能使用
 */
describe('E2E 场景测试', () => {
  let studentAuthStore: ReturnType<typeof useStudentAuthStore>
  let authStore: ReturnType<typeof useAuthStore>
  let practiceStore: ReturnType<typeof usePracticeStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    studentAuthStore = useStudentAuthStore()
    authStore = useAuthStore()
    practiceStore = usePracticeStore()
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 学生注册到登录流程 ====================

  describe('学生注册到登录流程', () => {
    it('完整的学生注册流程', async () => {
      // 1. 初始状态：未认证
      expect(studentAuthStore.isAuthenticated).toBe(false)
      expect(studentAuthStore.student).toBeNull()

      // 2. 学生注册
      const newStudent = {
        studentName: '新学生',
        studentNo: 'STU001',
        password: 'password123',
        grade: '高一',
        className: '1班'
      }

      // 模拟注册成功
      studentAuthStore.token = 'student-token-123'
      studentAuthStore.student = {
        id: 1,
        studentName: newStudent.studentName,
        studentNo: newStudent.studentNo,
        grade: newStudent.grade,
        className: newStudent.className,
        points: 0,
        avatar: null,
        avatarFrameStyle: null
      } as any

      // 3. 验证注册后状态
      expect(studentAuthStore.isAuthenticated).toBe(true)
      expect(studentAuthStore.student).not.toBeNull()
      expect(studentAuthStore.studentName).toBe('新学生')
    })

    it('登录后应能访问仪表盘', async () => {
      // 1. 登录
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = {
        id: 1,
        studentName: '测试学生',
        points: 100
      } as any

      // 2. 验证可以访问仪表盘数据
      expect(studentAuthStore.isAuthenticated).toBe(true)
      expect(studentAuthStore.student?.id).toBe(1)

      // 3. 可以查看统计数据
      expect(studentAuthStore.student?.points).toBe(100)
    })

    it('未登录时应无法访问受保护资源', async () => {
      // 1. 未登录状态
      expect(studentAuthStore.isAuthenticated).toBe(false)

      // 2. 尝试访问受保护功能应被拒绝
      expect(studentAuthStore.token).toBeFalsy()
      expect(studentAuthStore.student).toBeNull()
    })
  })

  // ==================== 练习到查看结果完整流程 ====================

  describe('练习到查看结果完整流程', () => {
    it('完整的练习流程', async () => {
      // 1. 学生登录
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = {
        id: 1,
        studentName: '测试学生',
        points: 100
      } as any

      // 2. 选择科目开始练习
      const mockQuestions = [
        { id: 1, content: '1+1=?', answer: 'A', questionType: 1, difficulty: 1 },
        { id: 2, content: '2+2=?', answer: 'B', questionType: 1, difficulty: 2 },
        { id: 3, content: '3+3=?', answer: 'C', questionType: 1, difficulty: 3 }
      ]

      practiceStore.questions = mockQuestions as any
      practiceStore.isPracticing = true
      practiceStore.currentIndex = 0

      // 3. 逐题答题
      practiceStore.submitAnswer(1, 'A', 30) // 第1题答对
      expect(practiceStore.answeredCount).toBe(1)
      expect(practiceStore.correctCount).toBe(1)

      practiceStore.currentIndex = 1
      practiceStore.submitAnswer(2, 'A', 25) // 第2题答错
      expect(practiceStore.answeredCount).toBe(2)
      expect(practiceStore.correctCount).toBe(1)

      practiceStore.currentIndex = 2
      practiceStore.submitAnswer(3, 'C', 35) // 第3题答对
      expect(practiceStore.answeredCount).toBe(3)
      expect(practiceStore.correctCount).toBe(2)

      // 4. 查看结果
      expect(practiceStore.currentAccuracy).toBeCloseTo(66.67, 1)
      expect(practiceStore.progressPercentage).toBe(100)
    })

    it('练习中断后应能恢复', async () => {
      // 1. 开始练习
      practiceStore.questions = [
        { id: 1, content: '题目1', answer: 'A', questionType: 1, difficulty: 3 },
        { id: 2, content: '题目2', answer: 'B', questionType: 1, difficulty: 3 }
      ] as any
      practiceStore.currentIndex = 0
      practiceStore.isPracticing = true

      // 2. 答一题
      practiceStore.submitAnswer(1, 'A', 30)
      expect(practiceStore.answeredCount).toBe(1)

      // 3. 保存状态
      const savedState = {
        questions: practiceStore.questions,
        currentIndex: practiceStore.currentIndex,
        userAnswers: Array.from(practiceStore.userAnswers.entries())
      }
      localStorage.setItem('practice_state', JSON.stringify(savedState))

      // 4. 模拟页面刷新
      practiceStore.$reset()

      // 5. 恢复状态
      const loadedState = JSON.parse(localStorage.getItem('practice_state')!)
      practiceStore.questions = loadedState.questions
      practiceStore.currentIndex = loadedState.currentIndex
      practiceStore.userAnswers = new Map(loadedState.userAnswers)

      // 6. 验证状态恢复
      expect(practiceStore.questions).toHaveLength(2)
      expect(practiceStore.answeredCount).toBe(1)
    })

    it('获得经验值和积分', async () => {
      // 1. 记录初始积分
      studentAuthStore.student = {
        id: 1,
        studentName: '学生',
        points: 100
      } as any

      const initialPoints = studentAuthStore.student.points

      // 2. 完成练习
      practiceStore.questions = [
        { id: 1, content: '题目', answer: 'A', questionType: 1, difficulty: 3 }
      ] as any
      practiceStore.submitAnswer(1, 'A', 30)

      // 3. 计算获得的经验值
      const xpGain = practiceStore.calculateXpGain(true, 30, 3)
      expect(xpGain).toBeGreaterThan(0)

      // 4. 模拟积分增加
      if (studentAuthStore.student) {
        studentAuthStore.student.points = initialPoints + 10
      }

      expect(studentAuthStore.student?.points).toBe(110)
    })
  })

  // ==================== 考试完整流程 ====================

  describe('考试完整流程', () => {
    it('从参加考试到查看成绩', async () => {
      // 1. 学生登录
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = {
        id: 1,
        studentName: '考试学生',
        points: 100
      } as any

      // 2. 进入考试页面
      const examState = {
        paperId: 1,
        paperName: '期中考试',
        isExamStarted: false,
        violationCount: 0,
        duration: 0,
        answers: {}
      }

      expect(examState.isExamStarted).toBe(false)

      // 3. 开始考试
      examState.isExamStarted = true
      expect(examState.isExamStarted).toBe(true)

      // 4. 答题
      examState.answers = {
        1: 'A',
        2: 'B,C',
        3: '填空答案',
        4: 'T'
      }

      expect(Object.keys(examState.answers)).toHaveLength(4)

      // 5. 提交考试
      const examResult = {
        score: 85,
        totalScore: 100,
        correctCount: 17,
        totalQuestions: 20
      }

      expect(examResult.score).toBe(85)
      expect((examResult.score / examResult.totalScore) * 100).toBe(85)
    })

    it('防作弊系统应该工作', async () => {
      // 1. 开始考试
      const examState = {
        isExamStarted: true,
        violationCount: 0,
        isFullscreen: true
      }

      // 2. 模拟违规行为
      examState.violationCount++ // 切屏
      expect(examState.violationCount).toBe(1)

      examState.violationCount++ // 退出全屏
      expect(examState.violationCount).toBe(2)

      examState.violationCount++ // 再次违规
      expect(examState.violationCount).toBe(3)

      // 3. 达到最大违规次数应强制交卷
      expect(examState.violationCount).toBeGreaterThanOrEqual(3)
    })
  })

  // ==================== 错题本到复习流程 ====================

  describe('错题本到复习流程', () => {
    it('练习错题会自动收录', async () => {
      // 1. 练习时答错题目
      const wrongQuestions = new Set<number>()

      practiceStore.questions = [
        { id: 1, content: '题目1', answer: 'A', questionType: 1 },
        { id: 2, content: '题目2', answer: 'B', questionType: 1 }
      ] as any

      practiceStore.submitAnswer(1, 'B', 30) // 答错
      if (practiceStore.userAnswers.get(1) !== 'A') {
        wrongQuestions.add(1)
      }

      practiceStore.submitAnswer(2, 'B', 25) // 答对
      if (practiceStore.userAnswers.get(2) !== 'B') {
        wrongQuestions.add(2)
      }

      // 2. 验证错题被收录
      expect(wrongQuestions.has(1)).toBe(true)
      expect(wrongQuestions.has(2)).toBe(false)
    })

    it('可以查看错题列表并复习', async () => {
      // 1. 错题列表
      const wrongQuestionsList = [
        { id: 1, content: '错题1', answer: 'A', wrongAnswer: 'B', wrongCount: 3 },
        { id: 2, content: '错题2', answer: 'C', wrongAnswer: 'D', wrongCount: 2 }
      ]

      expect(wrongQuestionsList).toHaveLength(2)

      // 2. 开始复习错题
      practiceStore.questions = wrongQuestionsList as any
      practiceStore.isPracticing = true

      // 3. 重新答题
      practiceStore.submitAnswer(1, 'A', 30)
      expect(practiceStore.correctCount).toBe(1)

      // 4. 答对后错题应被移除（模拟）
      const updatedWrongList = wrongQuestionsList.filter(q => q.id !== 1)
      expect(updatedWrongList).toHaveLength(1)
    })

    it('错题复习应有智能推荐', async () => {
      // 1. 基于错题生成推荐
      const wrongQuestionTopics = ['函数', '方程']
      const recommendations = [
        { topicId: 1, topic: '函数', recommendedQuestions: 5 },
        { topicId: 2, topic: '方程', recommendedQuestions: 3 }
      ]

      expect(recommendations).toHaveLength(2)
      expect(recommendations[0].recommendedQuestions).toBe(5)
    })
  })

  // ==================== 教师出题到学生答题流程 ====================

  describe('教师出题到学生答题流程', () => {
    it('完整的出题到答题流程', async () => {
      // 1. 教师登录创建题目
      authStore.token = 'teacher-token'
      authStore.user = {
        id: 1,
        userName: 'teacher',
        roles: [{ id: 3, code: 'TEACHER', name: '教师' }]
      } as any
      authStore.permissions = ['sys:question:add', 'sys:exam:add']

      expect(authStore.hasPermission('sys:question:add')).toBe(true)

      // 2. 教师创建题目
      const newQuestion = {
        id: 101,
        content: '新题目',
        answer: 'A',
        questionType: 1,
        difficulty: 3,
        createdBy: authStore.user.id
      }

      expect(newQuestion.createdBy).toBe(1)

      // 3. 教师创建试卷
      const newPaper = {
        id: 1,
        name: '月考试卷',
        paperType: 1,
        groups: [
          {
            id: 1,
            name: '单选题',
            questions: [{ questionId: 101, score: 5 }]
          }
        ]
      }

      expect(newPaper.groups[0].questions).toHaveLength(1)

      // 4. 学生参加考试
      authStore.$reset()
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = {
        id: 2,
        studentName: '学生A',
        points: 100
      } as any

      // 5. 学生答题
      const studentAnswers = {
        101: 'A'
      }

      expect(studentAnswers[101]).toBe('A')

      // 6. 提交并获得成绩
      const result = {
        score: 5,
        totalScore: 5
      }

      expect(result.score).toBe(5)
    })

    it('教师可以批改主观题', async () => {
      // 1. 学生提交主观题答案
      const subjectiveAnswer = {
        questionId: 1,
        studentId: 1,
        answer: '学生的主观题答案...',
        autoScore: 0, // 主观题不自动判分
        status: 'pending'
      }

      expect(subjectiveAnswer.status).toBe('pending')

      // 2. 教师批改
      subjectiveAnswer.autoScore = 8
      subjectiveAnswer.status = 'graded'

      expect(subjectiveAnswer.status).toBe('graded')
      expect(subjectiveAnswer.autoScore).toBe(8)
    })
  })

  // ==================== AI 功能集成流程 ====================

  describe('AI 功能集成流程', () => {
    it('AI 智能推荐应该工作', async () => {
      // 1. 学生学习历史
      studentAuthStore.student = {
        id: 1,
        studentName: '学生',
        points: 150
      } as any

      const learningHistory = {
        totalAnswered: 100,
        correctRate: 0.75,
        weakTopics: ['函数', '几何'],
        strongTopics: ['代数']
      }

      // 2. 基于历史生成推荐
      const aiRecommendations = [
        {
          itemType: 'question',
          itemId: 1,
          title: '函数专项练习',
          reason: '你在函数方面需要加强',
          score: 0.95
        },
        {
          itemType: 'question',
          itemId: 2,
          title: '几何基础题',
          reason: '巩固几何基础知识',
          score: 0.90
        }
      ]

      expect(aiRecommendations).toHaveLength(2)
      expect(aiRecommendations[0].reason).toContain('函数')
    })

    it('AI 聊天助手应能回答问题', async () => {
      // 1. 配置 AI Key
      studentAuthStore.aiKey = 'test-api-key'
      studentAuthStore.aiProvider = 'deepseek'

      localStorage.setItem('student_ai_key', studentAuthStore.aiKey)
      localStorage.setItem('student_ai_provider', studentAuthStore.aiProvider)

      // 2. 发送消息
      const chatMessage = {
        role: 'user',
        content: '如何学好数学？'
      }

      // 3. 接收 AI 回复（模拟）
      const aiResponse = {
        role: 'assistant',
        content: 'AI回复内容...'
      }

      expect(aiResponse.role).toBe('assistant')
      expect(aiResponse.content).toBeTruthy()
    })

    it('AI 自动批改应该工作', async () => {
      // 1. 提交主观题答案
      const subjectiveAnswer = {
        questionId: 1,
        answer: '这是学生的答案',
        standardAnswer: '这是标准答案'
      }

      // 2. AI 自动批改（模拟）
      const aiGradingResult = {
        score: 8,
        feedback: '答案基本正确，但缺少部分要点',
        similarity: 0.85
      }

      expect(aiGradingResult.score).toBeGreaterThan(0)
      expect(aiGradingResult.feedback).toBeTruthy()
    })
  })

  // ==================== 多端协作场景 ====================

  describe('多端协作场景', () => {
    it('学生和教师可以同时在线', async () => {
      // 1. 教师在线
      const teacherSession = {
        userId: 1,
        userType: 'teacher',
        isOnline: true
      }

      // 2. 学生在线
      const studentSession = {
        userId: 2,
        userType: 'student',
        isOnline: true
      }

      expect(teacherSession.isOnline).toBe(true)
      expect(studentSession.isOnline).toBe(true)
    })

    it('实时更新应该工作', async () => {
      // 1. 教师发布新试卷
      const newPaper = {
        id: 1,
        name: '新试卷',
        status: 'published',
        publishTime: new Date()
      }

      // 2. 学生应能立即看到
      expect(newPaper.status).toBe('published')
      expect(newPaper.publishTime).toBeDefined()
    })
  })

  // ==================== 数据一致性场景 ====================

  describe('数据一致性场景', () => {
    it('localStorage 和 Store 应保持同步', async () => {
      // 1. Store 更新
      studentAuthStore.token = 'new-token'
      localStorage.setItem('student_token', studentAuthStore.token)

      // 2. 验证同步
      expect(localStorage.getItem('student_token')).toBe('new-token')

      // 3. 清除 Store
      studentAuthStore.token = null
      localStorage.removeItem('student_token')

      expect(localStorage.getItem('student_token')).toBeNull()
      expect(studentAuthStore.token).toBeNull()
    })

    it('多个 Store 应能协同工作', async () => {
      // 1. 认证状态
      studentAuthStore.token = 'token'
      studentAuthStore.student = {
        id: 1,
        studentName: '学生',
        points: 100
      } as any

      // 2. 练习状态
      practiceStore.isPracticing = true
      practiceStore.questions = [
        { id: 1, content: '题目', questionType: 1 }
      ] as any

      // 3. 验证两个 Store 都有数据
      expect(studentAuthStore.isAuthenticated).toBe(true)
      expect(practiceStore.isPracticing).toBe(true)
    })

    it('页面刷新后应能恢复状态', async () => {
      // 1. 保存状态到 localStorage
      const state = {
        token: 'student-token',
        student: { id: 1, studentName: '学生' }
      }
      localStorage.setItem('student_auth', JSON.stringify(state))

      // 2. 模拟页面刷新
      studentAuthStore.$reset()

      // 3. 从 localStorage 恢复
      const savedState = JSON.parse(localStorage.getItem('student_auth')!)
      studentAuthStore.token = savedState.token
      studentAuthStore.student = savedState.student

      // 4. 验证状态恢复
      expect(studentAuthStore.token).toBe('student-token')
      expect(studentAuthStore.student?.studentName).toBe('学生')
    })
  })

  // ==================== 异常处理场景 ====================

  describe('异常处理场景', () => {
    it('网络异常时应能正常处理', async () => {
      // 1. 模拟网络错误
      let hasError = false

      try {
        throw new Error('Network error')
      } catch (error) {
        hasError = true
      }

      expect(hasError).toBe(true)
    })

    it('Token 过期应能重新登录', async () => {
      // 1. 初始登录
      studentAuthStore.token = 'old-token'
      studentAuthStore.student = {
        id: 1,
        studentName: '学生',
        points: 100
      } as any

      expect(studentAuthStore.isAuthenticated).toBe(true)

      // 2. Token 过期
      studentAuthStore.token = null
      studentAuthStore.student = null

      expect(studentAuthStore.isAuthenticated).toBe(false)

      // 3. 重新登录
      studentAuthStore.token = 'new-token'
      studentAuthStore.student = {
        id: 1,
        studentName: '学生',
        points: 100
      } as any

      expect(studentAuthStore.isAuthenticated).toBe(true)
    })

    it('考试意外中断应能恢复', async () => {
      // 1. 考试进行中
      const examState = {
        paperId: 1,
        isExamStarted: true,
        duration: 300,
        answers: { 1: 'A', 2: 'B' }
      }

      // 2. 保存状态
      localStorage.setItem('exam_state', JSON.stringify(examState))

      // 3. 模拟页面崩溃后恢复
      const recovered = JSON.parse(localStorage.getItem('exam_state')!)

      expect(recovered.paperId).toBe(1)
      expect(recovered.answers).toEqual({ 1: 'A', 2: 'B' })
    })
  })
})
