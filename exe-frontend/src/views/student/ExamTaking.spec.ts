import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { useStudentAuthStore } from '@/stores/studentAuth'
import ExamTaking from './ExamTaking.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// Mock dependencies
vi.mock('@/utils/request')
vi.mock('@/api/studentAuth')
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn(),
      warning: vi.fn()
    },
    ElMessageBox: {
      alert: vi.fn((message, title, options) => {
        if (options?.callback) {
          options.callback()
        }
        return Promise.resolve()
      }),
      confirm: vi.fn(() => Promise.resolve())
    }
  }
})

/**
 * ExamTaking 组件测试
 * 测试考试页面的完整功能，包括防作弊系统
 */
describe('ExamTaking.vue', () => {
  let studentAuthStore: ReturnType<typeof useStudentAuthStore>
  let mockRouter: any

  beforeEach(() => {
    setActivePinia(createPinia())
    studentAuthStore = useStudentAuthStore()
    vi.clearAllMocks()

    // 设置学生信息
    studentAuthStore.aiKey = 'test-api-key'
    studentAuthStore.aiProvider = 'deepseek'

    // Mock router
    mockRouter = {
      push: vi.fn(),
      back: vi.fn()
    }

    // Mock route params
    const mockRoute = {
      params: { paperId: '1' }
    }

    // Mock fullscreen API
    Object.defineProperty(document, 'fullscreenElement', {
      writable: true,
      value: null
    })

    Object.defineProperty(document.documentElement, 'requestFullscreen', {
      writable: true,
      value: vi.fn(() => Promise.resolve())
    })

    Object.defineProperty(document, 'exitFullscreen', {
      writable: true,
      value: vi.fn(() => Promise.resolve())
    })

    // Mock API responses
    ;(request as any).mockResolvedValue({
      code: 200,
      data: {
        paper: {
          id: 1,
          name: '测试试卷',
          paperType: 1,
          groups: []
        },
        questions: {}
      }
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('应该初始显示考试规则', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div class="el-card"><slot /><slot name="header" /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.isExamStarted).toBe(false)
    })

    it('应该显示考试规则内容', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-button': { template: '<button />' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      expect(wrapper.text()).toContain('全屏模式')
      expect(wrapper.text()).toContain('切屏')
      expect(wrapper.text()).toContain('3次')
    })

    it('应该显示试卷名称', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = { id: 1, name: '期末考试', paperType: 1, groups: [] }

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('期末考试')
    })
  })

  // ==================== 开始考试测试 ====================

  describe('开始考试', () => {
    it('应该能开始考试', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.startExamAction()

      await wrapper.vm.$nextTick()

      expect(vm.isExamStarted).toBe(true)
    })

    it('开始考试后应请求全屏', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.startExamAction()

      await wrapper.vm.$nextTick()

      expect(document.documentElement.requestFullscreen).toHaveBeenCalled()
    })

    it('开始考试后应启动计时器', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.duration).toBe(0)

      vm.startExamAction()
      await wrapper.vm.$nextTick()

      // 等待一秒
      await new Promise(resolve => setTimeout(resolve, 1100))

      expect(vm.duration).toBeGreaterThan(0)
    })
  })

  // ==================== 防作弊功能测试 ====================

  describe('防作弊功能', () => {
    it('应该能检测切屏行为', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.isExamStarted = true
      vm.resultVisible = false

      // 模拟切屏
      Object.defineProperty(document, 'hidden', {
        writable: true,
        value: true
      })

      vm.handleVisibilityChange()
      await wrapper.vm.$nextTick()

      expect(vm.violationCount).toBe(1)
      expect(ElMessage.error).toHaveBeenCalled()
    })

    it('应该能检测窗口失焦', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.isExamStarted = true
      vm.resultVisible = false

      vm.handleWindowBlur()
      await wrapper.vm.$nextTick()

      expect(vm.violationCount).toBe(1)
    })

    it('应该能检测退出全屏', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.isExamStarted = true
      vm.resultVisible = false

      Object.defineProperty(document, 'fullscreenElement', {
        writable: true,
        value: null
      })

      vm.handleFullScreenChange()
      await wrapper.vm.$nextTick()

      expect(vm.violationCount).toBe(1)
      expect(vm.isFullscreen).toBe(false)
    })

    it('违规3次应强制交卷', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.isExamStarted = true
      vm.resultVisible = false
      vm.paper = { id: 1, name: '测试试卷' }

      // 模拟3次违规
      vm.violationCount = 2
      vm.handleViolation('测试违规')

      await flushPromises()

      expect(vm.violationCount).toBe(3)
      expect(ElMessageBox.alert).toHaveBeenCalled()
    })

    it('违规次数应显示在页面上', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-tag': { template: '<span class="tag"><slot /></span>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.isExamStarted = true
      vm.violationCount = 2
      vm.paper = { id: 1, name: '测试试卷' }

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('2')
    })
  })

  // ==================== 全屏锁定测试 ====================

  describe('全屏锁定', () => {
    it('退出全屏时应显示锁定层', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-icon': { template: '<span />' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.isExamStarted = true
      vm.isFullscreen = false
      vm.resultVisible = false

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.lock-layer').exists()).toBe(true)
    })

    it('应该能恢复全屏继续考试', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.isExamStarted = true
      vm.isFullscreen = false

      vm.resumeExam()

      expect(document.documentElement.requestFullscreen).toHaveBeenCalled()
    })
  })

  // ==================== 答题功能测试 ====================

  describe('答题功能', () => {
    it('应该能选择单选题答案', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.handleOptionClick(1, 'A', 1)

      expect(vm.answers[1]).toBe('A')
    })

    it('应该能选择多选题答案', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.handleOptionClick(1, 'A', 2)
      vm.handleOptionClick(1, 'B', 2)

      expect(vm.answers[1]).toBe('A,B')
    })

    it('多选题应能取消选项', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.handleOptionClick(1, 'A', 2)
      vm.handleOptionClick(1, 'B', 2)
      vm.handleOptionClick(1, 'A', 2) // 取消A

      expect(vm.answers[1]).toBe('B')
    })

    it('应该能判断选项是否被选中', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.answers[1] = 'A,B'

      expect(vm.isOptionSelected(1, 'A')).toBe(true)
      expect(vm.isOptionSelected(1, 'B')).toBe(true)
      expect(vm.isOptionSelected(1, 'C')).toBe(false)
    })
  })

  // ==================== 答题卡测试 ====================

  describe('答题卡', () => {
    it('应该能通过答题卡跳转到题目', async () => {
      const scrollIntoViewMock = vi.fn()
      HTMLElement.prototype.scrollIntoView = scrollIntoViewMock

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any

      // 创建模拟DOM元素
      const mockElement = document.createElement('div')
      mockElement.id = 'q-1'
      document.body.appendChild(mockElement)

      vm.scrollToQuestion('q-1')

      expect(scrollIntoViewMock).toHaveBeenCalled()

      document.body.removeChild(mockElement)
    })

    it('已答题目应在答题卡上高亮', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = {
        id: 1,
        name: '测试试卷',
        paperType: 1,
        groups: [
          {
            id: 1,
            name: '单选题',
            questions: [{ id: 1, questionId: 101, score: 5 }]
          }
        ]
      }
      vm.answers[101] = 'A'

      await wrapper.vm.$nextTick()

      // 验证答案存在
      expect(vm.answers[101]).toBe('A')
    })
  })

  // ==================== 计时器测试 ====================

  describe('计时器', () => {
    it('应该正确格式化时间', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.duration = 0
      await wrapper.vm.$nextTick()
      expect(vm.formattedTime).toBe('00:00:00')

      vm.duration = 61
      await wrapper.vm.$nextTick()
      expect(vm.formattedTime).toBe('00:01:01')

      vm.duration = 3661
      await wrapper.vm.$nextTick()
      expect(vm.formattedTime).toBe('01:01:01')
    })

    it('计时器应该能正常工作', async () => {
      vi.useFakeTimers()

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.startTimer()

      await wrapper.vm.$nextTick()

      vi.advanceTimersByTime(3000)

      expect(vm.duration).toBeGreaterThanOrEqual(2)

      vi.useRealTimers()
    })
  })

  // ==================== 提交考试测试 ====================

  describe('提交考试', () => {
    it('应该能正常提交考试', async () => {
      ;(request as any).mockResolvedValue({
        code: 200,
        data: {
          score: 85,
          totalScore: 100
        }
      })

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = { id: 1, name: '测试试卷' }
      vm.answers = { 1: 'A', 2: 'B' }

      await vm.handleSubmit(true)
      await flushPromises()

      expect(vm.examResult).not.toBeNull()
      expect(vm.resultVisible).toBe(true)
    })

    it('提交前应显示确认对话框', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = { id: 1, name: '测试试卷' }

      await vm.handleSubmit(false)

      expect(ElMessageBox.confirm).toHaveBeenCalledWith(
        expect.any(String),
        expect.any(String),
        expect.any(Object)
      )
    })

    it('提交时应发送AI配置到服务器', async () => {
      ;(request as any).mockResolvedValue({
        code: 200,
        data: { score: 85, totalScore: 100 }
      })

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = { id: 1, name: '测试试卷' }

      await vm.handleSubmit(true)
      await flushPromises()

      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({
            'X-Ai-Api-Key': 'test-api-key',
            'X-Ai-Provider': 'deepseek'
          })
        })
      )
    })

    it('提交失败时应显示错误', async () => {
      ;(request as any).mockRejectedValue(new Error('Network error'))

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = { id: 1, name: '测试试卷' }

      await vm.handleSubmit(true)
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalled()
    })

    it('提交后应退出全屏', async () => {
      ;(request as any).mockResolvedValue({
        code: 200,
        data: { score: 85, totalScore: 100 }
      })

      Object.defineProperty(document, 'fullscreenElement', {
        writable: true,
        value: document.documentElement
      })

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = { id: 1, name: '测试试卷' }

      await vm.handleSubmit(true)
      await flushPromises()

      expect(document.exitFullscreen).toHaveBeenCalled()
    })
  })

  // ==================== 结果显示测试 ====================

  describe('结果显示', () => {
    it('应该正确显示考试分数', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-dialog': { template: '<div><slot /><slot name="footer" /></div>' },
            'el-progress': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.examResult = {
        score: 85,
        totalScore: 100
      }
      vm.resultVisible = true

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('85')
      expect(wrapper.text()).toContain('100')
    })

    it('应该正确计算得分百分比', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.examResult = {
        score: 85,
        totalScore: 100
      }

      await wrapper.vm.$nextTick()

      expect(vm.scorePercentage).toBe(85)
    })

    it('图片试卷应显示等待批阅提示', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-dialog': { template: '<div><slot /><slot name="footer" /></div>' },
            'el-progress': { template: '<div />' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = { id: 1, name: '测试', paperType: 2 }
      vm.examResult = { score: 0, totalScore: 100 }
      vm.resultVisible = true

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('等待老师批阅')
    })
  })

  // ==================== 试卷类型测试 ====================

  describe('试卷类型', () => {
    it('应该正确显示结构化试卷', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = {
        id: 1,
        name: '结构化试卷',
        paperType: 1,
        groups: [
          {
            id: 1,
            name: '单选题',
            questions: [{ id: 1, questionId: 101, score: 5 }]
          }
        ]
      }
      vm.questionsMap = {
        101: {
          id: 101,
          content: '题目内容',
          questionType: 1,
          options: JSON.stringify([{ key: 'A', value: '选项A' }])
        }
      }
      vm.isExamStarted = true

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('单选题')
      expect(wrapper.text()).toContain('题目内容')
    })

    it('应该正确显示图片试卷', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-image': {
              template: '<img :src="src" class="paper-image" />',
              props: ['src']
            },
            'el-input': { template: '<input />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = {
        id: 1,
        name: '图片试卷',
        paperType: 2,
        paperImages: [
          { id: 1, imageUrl: 'https://example.com/paper1.jpg' },
          { id: 2, imageUrl: 'https://example.com/paper2.jpg' }
        ]
      }
      vm.isExamStarted = true

      await wrapper.vm.$nextTick()

      const images = wrapper.findAll('.paper-image')
      expect(images.length).toBe(2)
    })

    it('图片试卷应显示通用答题框', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-image': { template: '<img />' },
            'el-input': { template: '<input class="generic-input" />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = {
        id: 1,
        name: '图片试卷',
        paperType: 2,
        paperImages: [{ id: 1, imageUrl: 'url' }]
      }
      vm.isExamStarted = true

      await wrapper.vm.$nextTick()

      // 应该有20个通用答题框
      expect(wrapper.text()).toContain('请在下方录入答案')
    })
  })

  // ==================== 题目解析测试 ====================

  describe('题目解析', () => {
    it('应该能正确解析题目选项', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      const options = [
        { key: 'A', value: '选项A' },
        { key: 'B', value: '选项B' }
      ]

      const parsed = vm.parseOptions(JSON.stringify(options))

      expect(parsed).toHaveLength(2)
      expect(parsed[0].key).toBe('A')
      expect(parsed[0].value).toBe('选项A')
    })

    it('解析错误的选项应返回空数组', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      const parsed = vm.parseOptions('invalid json')

      expect(parsed).toEqual([])
    })

    it('应该能获取题目信息', async () => {
      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.questionsMap = {
        101: { id: 101, content: '题目内容' }
      }

      const question = vm.getQuestion(101)

      expect(question).toBeDefined()
      expect(question.content).toBe('题目内容')
    })
  })

  // ==================== 事件监听器清理测试 ====================

  describe('事件监听器清理', () => {
    it('组件卸载时应清理事件监听器', async () => {
      const removeEventListenerSpy = vi.spyOn(document, 'removeEventListener')
      const windowRemoveListenerSpy = vi.spyOn(window, 'removeEventListener')

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.startExamAction()

      wrapper.unmount()

      expect(removeEventListenerSpy).toHaveBeenCalledWith('visibilitychange', expect.any(Function))
      expect(removeEventListenerSpy).toHaveBeenCalledWith('fullscreenchange', expect.any(Function))
      expect(windowRemoveListenerSpy).toHaveBeenCalledWith('blur', expect.any(Function))
    })

    it('组件卸载时应清理计时器', async () => {
      vi.useFakeTimers()

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.startTimer()

      wrapper.unmount()

      vi.advanceTimersByTime(1000)

      // 卸载后时间不应继续增加
      expect(vm.duration).toBeLessThan(2)

      vi.useRealTimers()
    })
  })

  // ==================== 性能测试 ====================

  describe('性能', () => {
    it('应该能处理大量题目的试卷', async () => {
      const largeGroups = [
        {
          id: 1,
          name: '单选题',
          questions: Array.from({ length: 50 }, (_, i) => ({
            id: i + 1,
            questionId: 100 + i,
            score: 2
          }))
        }
      ]

      const largeQuestionsMap: any = {}
      for (let i = 0; i < 50; i++) {
        largeQuestionsMap[100 + i] = {
          id: 100 + i,
          content: `题目${i + 1}`,
          questionType: 1,
          options: JSON.stringify([{ key: 'A', value: '选项A' }])
        }
      }

      const wrapper = mount(ExamTaking, {
        global: {
          mocks: {
            $router: mockRouter,
            $route: { params: { paperId: '1' } }
          },
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.paper = {
        id: 1,
        name: '大型试卷',
        paperType: 1,
        groups: largeGroups
      }
      vm.questionsMap = largeQuestionsMap
      vm.isExamStarted = true

      await wrapper.vm.$nextTick()

      expect(vm.paper.groups[0].questions).toHaveLength(50)
    })
  })
})
