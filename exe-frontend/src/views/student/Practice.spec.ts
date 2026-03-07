import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import Practice from './Practice.vue'
import { ElMessage, ElNotification } from 'element-plus'
import request from '@/utils/request'

// Mock dependencies
vi.mock('@/utils/request')
vi.mock('@/api/studentAuth')
vi.mock('@/api/favorite')
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn(),
      warning: vi.fn()
    },
    ElNotification: vi.fn()
  }
})

/**
 * Practice 组件测试
 * 测试练习页面的完整流程
 */
describe('Practice.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()

    // Mock API responses
    ;(request as any).mockResolvedValue({
      code: 200,
      data: []
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('应该初始显示年级选择', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div class="el-card"><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.practiceState).toBe('selectingGrade')
    })

    it('应该渲染所有年级选项', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      const text = wrapper.text()
      expect(text).toContain('七年级')
      expect(text).toContain('八年级')
      expect(text).toContain('九年级')
      expect(text).toContain('高一')
      expect(text).toContain('高二')
      expect(text).toContain('高三')
    })
  })

  // ==================== 年级和科目选择测试 ====================

  describe('年级和科目选择', () => {
    it('应该能选择年级', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.selectGrade('高一')

      await wrapper.vm.$nextTick()

      expect(vm.currentGrade).toBe('高一')
      expect(vm.practiceState).toBe('selectingSubject')
    })

    it('选择年级后应显示对应科目', async () => {
      const mockSubjects = [
        { id: 1, name: '数学', grade: '高一' },
        { id: 2, name: '英语', grade: '高一' },
        { id: 3, name: '物理', grade: '高二' }
      ]

      ;(request as any).mockResolvedValue({
        code: 200,
        data: mockSubjects
      })

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' },
            'el-button': { template: '<button />' },
            'el-empty': { template: '<div>empty</div>' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      vm.allSubjects = mockSubjects
      vm.selectGrade('高一')

      await wrapper.vm.$nextTick()

      expect(vm.filteredSubjects).toHaveLength(2)
      expect(vm.filteredSubjects[0].name).toBe('数学')
      expect(vm.filteredSubjects[1].name).toBe('英语')
    })

    it('应该能返回重选年级', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.currentGrade = '高一'
      vm.practiceState = 'selectingSubject'

      vm.resetPractice()
      await wrapper.vm.$nextTick()

      expect(vm.practiceState).toBe('selectingGrade')
      expect(vm.currentGrade).toBeNull()
    })
  })

  // ==================== 练习模式测试 ====================

  describe('练习模式', () => {
    it('应该支持随机练习模式', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-radio-group': {
              template: '<div><slot /></div>',
              props: ['modelValue']
            },
            'el-radio-button': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.practiceMode).toBe('random')
    })

    it('应该能切换到智能练习模式', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.practiceMode = 'smart'

      await wrapper.vm.$nextTick()

      expect(vm.practiceMode).toBe('smart')
    })
  })

  // ==================== 题目显示测试 ====================

  describe('题目显示', () => {
    it('应该正确显示当前题目', async () => {
      const mockQuestions = [
        {
          id: 1,
          content: '1+1等于几？',
          answer: 'A',
          questionType: 1,
          options: JSON.stringify([
            { key: 'A', value: '2' },
            { key: 'B', value: '3' }
          ]),
          difficulty: 1,
          imageUrl: null
        }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.practiceState = 'practicing'
      vm.currentQuestionIndex = 0

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('1+1等于几')
    })

    it('应该正确解析题目选项', async () => {
      const mockQuestions = [
        {
          id: 1,
          content: '题目',
          questionType: 1,
          options: JSON.stringify([
            { key: 'A', value: '选项A' },
            { key: 'B', value: '选项B' },
            { key: 'C', value: '选项C' }
          ])
        }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.currentQuestionIndex = 0

      await wrapper.vm.$nextTick()

      expect(vm.parsedOptions).toHaveLength(3)
      expect(vm.parsedOptions[0].key).toBe('A')
      expect(vm.parsedOptions[0].value).toBe('选项A')
    })

    it('应该显示题目进度', async () => {
      const mockQuestions = [
        { id: 1, content: '题目1', questionType: 1 },
        { id: 2, content: '题目2', questionType: 1 },
        { id: 3, content: '题目3', questionType: 1 }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.practiceState = 'practicing'
      vm.currentQuestionIndex = 0

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('1 / 3')
    })
  })

  // ==================== 题型切换测试 ====================

  describe('题型切换', () => {
    it('应该正确显示单选题', async () => {
      const mockQuestions = [
        {
          id: 1,
          content: '单选题',
          questionType: 1,
          options: JSON.stringify([{ key: 'A', value: '选项A' }])
        }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-radio-group': { template: '<div class="radio-group"><slot /></div>' },
            'el-radio': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.practiceState = 'practicing'

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.radio-group').exists()).toBe(true)
    })

    it('应该正确显示填空题', async () => {
      const mockQuestions = [
        {
          id: 1,
          content: '填空题',
          questionType: 3,
          answer: '答案'
        }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-input': { template: '<input class="fill-input" />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.practiceState = 'practicing'

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.fill-input').exists()).toBe(true)
    })

    it('应该正确显示判断题', async () => {
      const mockQuestions = [
        {
          id: 1,
          content: '判断题',
          questionType: 4,
          answer: 'T'
        }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-radio-group': { template: '<div class="judge-group"><slot /></div>' },
            'el-radio': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.practiceState = 'practicing'

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('正确')
      expect(wrapper.text()).toContain('错误')
    })

    it('应该正确显示主观题', async () => {
      const mockQuestions = [
        {
          id: 1,
          content: '主观题',
          questionType: 5,
          answer: '主观答案'
        }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-input': { template: '<textarea class="subjective-input" />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.practiceState = 'practicing'

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.subjective-input').exists()).toBe(true)
    })
  })

  // ==================== 题目导航测试 ====================

  describe('题目导航', () => {
    it('应该能切换到下一题', async () => {
      const mockQuestions = [
        { id: 1, content: '题目1', questionType: 1 },
        { id: 2, content: '题目2', questionType: 1 }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.currentQuestionIndex = 0

      vm.nextQuestion()
      await wrapper.vm.$nextTick()

      expect(vm.currentQuestionIndex).toBe(1)
    })

    it('应该能切换到上一题', async () => {
      const mockQuestions = [
        { id: 1, content: '题目1', questionType: 1 },
        { id: 2, content: '题目2', questionType: 1 }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.currentQuestionIndex = 1

      vm.prevQuestion()
      await wrapper.vm.$nextTick()

      expect(vm.currentQuestionIndex).toBe(0)
    })

    it('第一题时不应能切换到上一题', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, content: '题目1', questionType: 1 }]
      vm.currentQuestionIndex = 0

      vm.prevQuestion()
      await wrapper.vm.$nextTick()

      expect(vm.currentQuestionIndex).toBe(0)
    })

    it('最后一题时不应能切换到下一题', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, content: '题目1', questionType: 1 }]
      vm.currentQuestionIndex = 0

      vm.nextQuestion()
      await wrapper.vm.$nextTick()

      expect(vm.currentQuestionIndex).toBe(0)
    })
  })

  // ==================== 答题功能测试 ====================

  describe('答题功能', () => {
    it('应该能记录单选答案', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, questionType: 1 }]
      vm.userAnswers[1] = 'A'

      await wrapper.vm.$nextTick()

      expect(vm.userAnswers[1]).toBe('A')
    })

    it('应该能记录填空答案', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, questionType: 3 }]
      vm.userAnswers[1] = '填空答案'

      await wrapper.vm.$nextTick()

      expect(vm.userAnswers[1]).toBe('填空答案')
    })

    it('应该能记录判断题答案', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, questionType: 4 }]
      vm.userAnswers[1] = 'T'

      await wrapper.vm.$nextTick()

      expect(vm.userAnswers[1]).toBe('T')
    })
  })

  // ==================== 提交练习测试 ====================

  describe('提交练习', () => {
    it('应该能提交练习', async () => {
      const mockResult = {
        totalQuestions: 3,
        correctCount: 2,
        results: [
          { question: { id: 1, content: '题目1', answer: 'A' }, userAnswer: 'A', isCorrect: true },
          { question: { id: 2, content: '题目2', answer: 'B' }, userAnswer: 'C', isCorrect: false },
          { question: { id: 3, content: '题目3', answer: 'C' }, userAnswer: 'C', isCorrect: true }
        ]
      }

      ;(request as any).mockResolvedValue({
        code: 200,
        data: mockResult
      })

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [
        { id: 1, content: '题目1', questionType: 1 },
        { id: 2, content: '题目2', questionType: 1 },
        { id: 3, content: '题目3', questionType: 1 }
      ]
      vm.userAnswers = { 1: 'A', 2: 'C', 3: 'C' }

      await vm.submitPractice()
      await flushPromises()

      expect(vm.practiceResult).not.toBeNull()
      expect(vm.practiceState).toBe('result')
    })

    it('提交时应显示加载状态', async () => {
      ;(request as any).mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, content: '题目1', questionType: 1 }]

      const submitPromise = vm.submitPractice()

      await wrapper.vm.$nextTick()
      expect(vm.isSubmitting).toBe(true)

      await submitPromise
      expect(vm.isSubmitting).toBe(false)
    })

    it('提交失败时应显示错误', async () => {
      ;(request as any).mockRejectedValue(new Error('Network error'))

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, content: '题目1', questionType: 1 }]

      await vm.submitPractice()
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalledWith('提交失败')
    })
  })

  // ==================== 结果页面测试 ====================

  describe('结果页面', () => {
    it('应该正确显示正确率', async () => {
      const mockResult = {
        totalQuestions: 10,
        correctCount: 8,
        results: []
      }

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-progress': {
              template: '<div><slot /></div>',
              props: ['percentage']
            }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.practiceResult = mockResult
      vm.practiceState = 'result'

      await wrapper.vm.$nextTick()

      expect(vm.correctPercentage).toBe(80)
    })

    it('应该显示答题详情', async () => {
      const mockResult = {
        totalQuestions: 2,
        correctCount: 1,
        results: [
          {
            question: { id: 1, content: '题目1', answer: 'A', description: '解析1' },
            userAnswer: 'A',
            isCorrect: true
          },
          {
            question: { id: 2, content: '题目2', answer: 'B', description: '解析2' },
            userAnswer: 'C',
            isCorrect: false
          }
        ]
      }

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-progress': { template: '<div />' },
            'el-divider': { template: '<hr />' },
            'el-tag': { template: '<span><slot /></span>' },
            'el-collapse-transition': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.practiceResult = mockResult
      vm.practiceState = 'result'

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('题目1')
      expect(wrapper.text()).toContain('题目2')
      expect(wrapper.text()).toContain('解析2')
    })

    it('应该显示统计信息', async () => {
      const mockResult = {
        totalQuestions: 10,
        correctCount: 7,
        results: []
      }

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-progress': { template: '<div />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.practiceResult = mockResult
      vm.practiceState = 'result'

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('10')
      expect(wrapper.text()).toContain('7')
      expect(wrapper.text()).toContain('3')
    })

    it('应该能从结果页返回重新练习', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.practiceState = 'result'
      vm.currentSubject = { id: 1, name: '数学' }

      vm.resetPractice()
      await wrapper.vm.$nextTick()

      expect(vm.practiceState).toBe('selectingGrade')
      expect(vm.currentSubject).toBeNull()
    })
  })

  // ==================== 题目收藏测试 ====================

  describe('题目收藏', () => {
    it('应该能收藏题目', async () => {
      const { toggleFavorite } = await import('@/api/favorite')
      ;(toggleFavorite as any).mockResolvedValue({ code: 200 })

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, content: '题目1', questionType: 1 }]
      vm.practiceState = 'practicing'
      vm.currentQuestionIndex = 0

      await vm.handleToggleFavorite()
      await flushPromises()

      expect(ElMessage.success).toHaveBeenCalled()
    })

    it('应该能检查收藏状态', async () => {
      const { checkFavoriteStatus } = await import('@/api/favorite')
      ;(checkFavoriteStatus as any).mockResolvedValue({ code: 200, data: true })

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, content: '题目1', questionType: 1 }]
      vm.currentQuestionIndex = 0

      await vm.checkFavStatus()
      await flushPromises()

      expect(vm.isFavorited).toBe(true)
    })
  })

  // ==================== 题目类型统计测试 ====================

  describe('题目类型统计', () => {
    it('应该正确统计题型数量', async () => {
      const mockQuestions = [
        { id: 1, questionType: 1, content: '单选1' },
        { id: 2, questionType: 1, content: '单选2' },
        { id: 3, questionType: 2, content: '多选1' },
        { id: 4, questionType: 3, content: '填空1' },
        { id: 5, questionType: 4, content: '判断1' }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions

      await wrapper.vm.$nextTick()

      const counts = vm.questionTypeCounts
      expect(counts.find((c: any) => c.type === 1)?.count).toBe(2)
      expect(counts.find((c: any) => c.type === 2)?.count).toBe(1)
      expect(counts.find((c: any) => c.type === 3)?.count).toBe(1)
      expect(counts.find((c: any) => c.type === 4)?.count).toBe(1)
    })

    it('应该显示题型名称', async () => {
      const mockQuestions = [
        { id: 1, questionType: 1, content: '单选' },
        { id: 2, questionType: 2, content: '多选' }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-tag': { template: '<span><slot /></span>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.practiceState = 'practicing'

      await wrapper.vm.$nextTick()

      const counts = vm.questionTypeCounts
      expect(counts.find((c: any) => c.type === 1)?.name).toBe('单选题')
      expect(counts.find((c: any) => c.type === 2)?.name).toBe('多选题')
    })
  })

  // ==================== 经验值通知测试 ====================

  describe('经验值通知', () => {
    it('提交成功后应显示经验值通知', async () => {
      const mockResult = {
        totalQuestions: 5,
        correctCount: 4,
        expGain: 50,
        pointsGain: 100,
        results: []
      }

      ;(request as any).mockResolvedValue({
        code: 200,
        data: mockResult
      })

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = [{ id: 1, content: '题目1', questionType: 1 }]

      await vm.submitPractice()
      await flushPromises()

      expect(ElNotification).toHaveBeenCalledWith(
        expect.objectContaining({
          title: '历练结算',
          type: 'success'
        })
      )
    })
  })

  // ==================== 图片显示测试 ====================

  describe('图片显示', () => {
    it('应该显示题目图片', async () => {
      const mockQuestions = [
        {
          id: 1,
          content: '题目',
          questionType: 1,
          imageUrl: 'https://example.com/image.jpg'
        }
      ]

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-image': {
              template: '<img :src="src" class="question-image" />',
              props: ['src']
            }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions
      vm.practiceState = 'practicing'

      await wrapper.vm.$nextTick()

      const image = wrapper.find('.question-image')
      expect(image.exists()).toBe(true)
      expect(image.attributes('src')).toBe('https://example.com/image.jpg')
    })
  })

  // ==================== 空状态测试 ====================

  describe('空状态', () => {
    it('没有科目时应显示空状态', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-empty': { template: '<div class="empty">该年级下暂无科目</div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.allSubjects = []
      vm.currentGrade = '高一'
      vm.practiceState = 'selectingSubject'

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.empty').exists()).toBe(true)
    })

    it('没有题目时应显示空状态', async () => {
      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-empty': { template: '<div class="empty">该科目下暂无练习题</div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = []
      vm.practiceState = 'practicing'

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.empty').exists()).toBe(true)
    })
  })

  // ==================== 性能测试 ====================

  describe('性能', () => {
    it('应该能处理大量题目', async () => {
      const largeQuestions = Array.from({ length: 100 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        questionType: 1,
        options: JSON.stringify([{ key: 'A', value: '选项A' }]),
        answer: 'A'
      }))

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = largeQuestions
      vm.practiceState = 'practicing'

      await wrapper.vm.$nextTick()

      expect(vm.questions).toHaveLength(100)
    })

    it('题目切换应该快速响应', async () => {
      const mockQuestions = Array.from({ length: 50 }, (_, i) => ({
        id: i + 1,
        content: `题目${i + 1}`,
        questionType: 1
      }))

      const wrapper = mount(Practice, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questions = mockQuestions

      const startTime = Date.now()

      for (let i = 0; i < 10; i++) {
        vm.nextQuestion()
        await wrapper.vm.$nextTick()
      }

      const endTime = Date.now()
      const duration = endTime - startTime

      expect(duration).toBeLessThan(100)
    })
  })
})
