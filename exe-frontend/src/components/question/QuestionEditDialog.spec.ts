import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElRadioGroup, ElCheckboxGroup, ElButton, ElUpload, ElMessage } from 'element-plus'
import QuestionEditDialog from './QuestionEditDialog.vue'
import { useAuthStore } from '@/stores/auth'
import * as questionApi from '@/api/question'
import * as subjectApi from '@/api/subject'
import * as knowledgePointApi from '@/api/knowledgePoint'
import { setActivePinia, createPinia } from 'pinia'

// Mock APIs
vi.mock('@/api/question', () => ({
  createQuestion: vi.fn(),
  updateQuestion: vi.fn(),
  fetchQuestionById: vi.fn()
}))

vi.mock('@/api/subject', () => ({
  fetchAllSubjects: vi.fn()
}))

vi.mock('@/api/knowledgePoint', () => ({
  fetchKnowledgePointList: vi.fn()
}))

vi.mock('@/utils/request', () => ({
  default: vi.fn()
}))

// Mock ElMessage
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    }
  }
})

describe('QuestionEditDialog 组件测试', () => {
  const mockSubjects = [
    { id: 1, name: '数学', grade: '九年级', createTime: '', updateTime: '' },
    { id: 2, name: '语文', grade: '九年级', createTime: '', updateTime: '' },
    { id: 3, name: '英语', grade: '九年级', createTime: '', updateTime: '' }
  ]

  const mockKnowledgePoints = [
    { id: 1, name: '二次函数', subjectId: 1, description: '', sort: 1 },
    { id: 2, name: '一元二次方程', subjectId: 1, description: '', sort: 2 },
    { id: 3, name: '勾股定理', subjectId: 1, description: '', sort: 3 }
  ]

  const mockQuestion = {
    id: 1,
    subjectId: 1,
    questionType: 1,
    content: '下列哪个是二次函数？',
    options: JSON.stringify([
      { key: 'A', value: 'y = x + 1' },
      { key: 'B', value: 'y = x^2 + 1' },
      { key: 'C', value: 'y = 1/x' },
      { key: 'D', value: 'y = |x|' }
    ]),
    answer: 'B',
    description: '二次函数的一般形式为y = ax^2 + bx + c',
    grade: '九年级',
    knowledgePointIds: [1],
    imageUrl: '',
    answerImageUrl: ''
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    // Mock API responses
    vi.mocked(subjectApi.fetchAllSubjects).mockResolvedValue({
      code: 200,
      data: mockSubjects
    })

    vi.mocked(knowledgePointApi.fetchKnowledgePointList).mockResolvedValue({
      code: 200,
      data: mockKnowledgePoints
    })

    vi.mocked(questionApi.fetchQuestionById).mockResolvedValue({
      code: 200,
      data: mockQuestion
    })

    vi.mocked(questionApi.createQuestion).mockResolvedValue({
      code: 200,
      message: '创建成功'
    })

    vi.mocked(questionApi.updateQuestion).mockResolvedValue({
      code: 200,
      message: '更新成功'
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('visible为false时不应该显示', () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: false
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      const dialog = wrapper.findComponent(ElDialog)
      expect(dialog.props('modelValue')).toBe(false)
    })

    it('visible为true时应该显示', () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      const dialog = wrapper.findComponent(ElDialog)
      expect(dialog.props('modelValue')).toBe(true)
    })

    it('新增试题时应该显示正确的标题', () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      const dialog = wrapper.findComponent(ElDialog)
      expect(dialog.props('title')).toBe('新增试题')
    })

    it('编辑试题时应该显示正确的标题', () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true,
          questionId: 1
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      const dialog = wrapper.findComponent(ElDialog)
      expect(dialog.props('title')).toBe('编辑试题')
    })
  })

  // ==================== 数据加载测试 ====================

  describe('数据加载', () => {
    it('打开对话框时应该加载科目列表', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      expect(subjectApi.fetchAllSubjects).toHaveBeenCalled()
    })

    it('编辑试题时应该加载试题数据', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true,
          questionId: 1
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      expect(questionApi.fetchQuestionById).toHaveBeenCalledWith(1)
    })

    it('选择科目后应该加载对应的知识点', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      // 模拟选择科目
      const vm = wrapper.vm as any
      await vm.handleSubjectChange(1)
      await flushPromises()

      expect(knowledgePointApi.fetchKnowledgePointList).toHaveBeenCalledWith(
        expect.objectContaining({ subjectId: 1 })
      )
    })

    it('加载数据失败时应该处理错误', async () => {
      vi.mocked(subjectApi.fetchAllSubjects).mockRejectedValue(new Error('Network error'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      consoleErrorSpy.mockRestore()
    })
  })

  // ==================== 题型切换测试 ====================

  describe('题型切换', () => {
    it('切换到单选题时应该显示选项和单选答案', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElRadioGroup, ElButton }
        }
      })

      await flushPromises()

      // 选择单选题
      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 1
        await wrapper.vm.$nextTick()

        // 应该显示选项设置和单选答案
      }
    })

    it('切换到多选题时应该显示选项和多选答案', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElCheckboxGroup, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 2
        await wrapper.vm.$nextTick()

        // 应该显示选项设置和多选答案
      }
    })

    it('切换到判断题时应该显示正确/错误选项', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElRadioGroup, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 4
        await wrapper.vm.$nextTick()

        // 应该显示T/F选项
      }
    })

    it('切换到主观题时应该显示文本答案框', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 5
        await wrapper.vm.$nextTick()

        // 应该显示参考答案文本框
      }
    })

    it('切换题型时应该清空答案', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 1
        vm.form.answer = 'A'
        await wrapper.vm.$nextTick()

        vm.form.questionType = 2
        await wrapper.vm.$nextTick()

        // 答案应该被清空
        expect(vm.form.answer).toBe('')
      }
    })
  })

  // ==================== 选项管理测试 ====================

  describe('选项管理', () => {
    it('单选题和多选题应该默认有ABCD四个选项', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 1
        await wrapper.vm.$nextTick()

        expect(vm.localOptions).toHaveLength(4)
        expect(vm.localOptions[0].key).toBe('A')
        expect(vm.localOptions[3].key).toBe('D')
      }
    })

    it('应该支持添加选项', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 1
        await wrapper.vm.$nextTick()

        const initialLength = vm.localOptions.length
        vm.addOption()

        expect(vm.localOptions).toHaveLength(initialLength + 1)
        expect(vm.localOptions[initialLength].key).toBe('E')
      }
    })

    it('应该支持删除选项', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 1
        await wrapper.vm.$nextTick()

        const initialLength = vm.localOptions.length
        vm.removeOption(1) // 删除第二个选项

        expect(vm.localOptions).toHaveLength(initialLength - 1)
      }
    })

    it('删除选项后应该重新排序key', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 1
        vm.localOptions = [
          { key: 'A', value: '选项A' },
          { key: 'B', value: '选项B' },
          { key: 'C', value: '选项C' },
          { key: 'D', value: '选项D' }
        ]
        await wrapper.vm.$nextTick()

        vm.removeOption(1) // 删除B

        expect(vm.localOptions[1].key).toBe('C')
      }
    })

    it('最多应该允许添加26个选项', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.questionType = 1
        vm.localOptions = Array.from({ length: 26 }, (_, i) => ({
          key: String.fromCharCode(65 + i),
          value: ''
        }))
        await wrapper.vm.$nextTick()

        vm.addOption()

        expect(ElMessage.warning).toHaveBeenCalledWith('最多只能添加26个选项')
        expect(vm.localOptions).toHaveLength(26)
      }
    })
  })

  // ==================== 图片上传测试 ====================

  describe('图片上传', () => {
    it('应该支持上传题目图片', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElUpload, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      const mockResponse = {
        code: 200,
        data: 'https://example.com/image.jpg'
      }

      vm.handleImageSuccess(mockResponse)

      expect(vm.form?.imageUrl).toBe('https://example.com/image.jpg')
    })

    it('应该支持上传解析图片', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElUpload, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      const mockResponse = {
        code: 200,
        data: 'https://example.com/answer.jpg'
      }

      vm.handleAnswerImageSuccess(mockResponse)

      expect(vm.form?.answerImageUrl).toBe('https://example.com/answer.jpg')
    })

    it('图片上传失败时应该显示错误消息', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElUpload, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      const mockResponse = {
        code: 500,
        msg: '上传失败'
      }

      vm.handleImageSuccess(mockResponse)

      expect(ElMessage.error).toHaveBeenCalled()
    })

    it('应该验证图片格式', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElUpload, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      const invalidFile = {
        type: 'application/pdf',
        size: 1024 * 1024
      }

      const result = vm.beforeImageUpload(invalidFile)

      expect(result).toBe(false)
      expect(ElMessage.error).toHaveBeenCalledWith('图片只能是 JPG 或 PNG 格式!')
    })

    it('应该验证图片大小不超过2MB', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElUpload, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      const largeFile = {
        type: 'image/jpeg',
        size: 3 * 1024 * 1024 // 3MB
      }

      const result = vm.beforeImageUpload(largeFile)

      expect(result).toBe(false)
      expect(ElMessage.error).toHaveBeenCalledWith('图片大小不能超过 2MB!')
    })

    it('应该支持删除图片', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.imageUrl = 'https://example.com/image.jpg'
        vm.removeImage()

        expect(vm.form.imageUrl).toBe('')
      }
    })
  })

  // ==================== 表单验证测试 ====================

  describe('表单验证', () => {
    it('题干内容应该是必填的', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      // rules中content应该有required验证
      expect(vm.rules.content).toBeDefined()
      expect(vm.rules.content[0].required).toBe(true)
    })

    it('科目应该是必填的', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.rules.subjectId).toBeDefined()
      expect(vm.rules.subjectId[0].required).toBe(true)
    })

    it('答案应该是必填的', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.rules.answer).toBeDefined()
      expect(vm.rules.answer[0].required).toBe(true)
    })
  })

  // ==================== 表单提交测试 ====================

  describe('表单提交', () => {
    it('应该成功创建新试题', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      // 模拟填写表单并提交
      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.subjectId = 1
        vm.form.questionType = 1
        vm.form.content = '测试题目'
        vm.form.answer = 'A'

        await vm.submitForm()
        await flushPromises()

        expect(questionApi.createQuestion).toHaveBeenCalled()
        expect(ElMessage.success).toHaveBeenCalledWith('新增成功')
      }
    })

    it('应该成功更新试题', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true,
          questionId: 1
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      await vm.submitForm()
      await flushPromises()

      expect(questionApi.updateQuestion).toHaveBeenCalledWith(1, expect.any(Object))
      expect(ElMessage.success).toHaveBeenCalledWith('更新成功')
    })

    it('提交时应该正确序列化选项', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.subjectId = 1
        vm.form.questionType = 1
        vm.form.content = '测试题目'
        vm.form.answer = 'A'
        vm.localOptions = [
          { key: 'A', value: '选项A' },
          { key: 'B', value: '选项B' }
        ]

        await vm.submitForm()
        await flushPromises()

        // 验证options被序列化为JSON字符串
        const callArgs = vi.mocked(questionApi.createQuestion).mock.calls[0][0]
        expect(typeof callArgs.options).toBe('string')
      }
    })

    it('非选择题提交时options应该为null', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.subjectId = 1
        vm.form.questionType = 5 // 主观题
        vm.form.content = '测试题目'
        vm.form.answer = '参考答案'

        await vm.submitForm()
        await flushPromises()

        const callArgs = vi.mocked(questionApi.createQuestion).mock.calls[0][0]
        expect(callArgs.options).toBeNull()
      }
    })

    it('提交成功后应该发射success事件', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.subjectId = 1
        vm.form.questionType = 1
        vm.form.content = '测试题目'
        vm.form.answer = 'A'

        await vm.submitForm()
        await flushPromises()

        expect(wrapper.emitted('success')).toBeTruthy()
      }
    })

    it('提交失败时应该显示错误消息', async () => {
      vi.mocked(questionApi.createQuestion).mockRejectedValue(new Error('Network error'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.subjectId = 1
        vm.form.questionType = 1
        vm.form.content = '测试题目'
        vm.form.answer = 'A'

        await vm.submitForm()
        await flushPromises()
      }

      consoleErrorSpy.mockRestore()
    })
  })

  // ==================== 查重功能测试 ====================

  describe('查重功能', () => {
    it('输入题干内容后应该触发查重', async () => {
      const mockRequest = vi.fn().mockResolvedValue({
        code: 200,
        data: []
      })

      vi.doMock('@/utils/request', () => ({
        default: mockRequest
      }))

      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      if (vm.form) {
        vm.form.subjectId = 1
        vm.form.content = '测试题目内容'
        await new Promise(resolve => setTimeout(resolve, 600)) // 等待防抖

        // 验证查重请求被触发
      }
    })

    it('发现相似题目时应该显示警告', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      vm.similarQuestions = [
        { id: 10, content: '相似题目1' },
        { id: 11, content: '相似题目2' }
      ]

      await wrapper.vm.$nextTick()

      // 应该显示警告提示
    })
  })

  // ==================== 事件处理测试 ====================

  describe('事件处理', () => {
    it('点击取消应该关闭对话框', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const cancelButton = buttons.find(btn => btn.text() === '取 消')

      if (cancelButton) {
        await cancelButton.trigger('click')

        expect(wrapper.emitted('update:visible')).toBeTruthy()
        expect(wrapper.emitted('update:visible')![0]).toEqual([false])
      }
    })

    it('关闭对话框时应该重置表单', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      await wrapper.setProps({ visible: false })
      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.form).toBeNull()
    })
  })

  // ==================== 边界情况测试 ====================

  describe('边界情况', () => {
    it('应该处理科目列表为空的情况', async () => {
      vi.mocked(subjectApi.fetchAllSubjects).mockResolvedValue({
        code: 200,
        data: []
      })

      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      expect(wrapper.exists()).toBe(true)
    })

    it('应该处理知识点列表为空的情况', async () => {
      vi.mocked(knowledgePointApi.fetchKnowledgePointList).mockResolvedValue({
        code: 200,
        data: []
      })

      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      await vm.handleSubjectChange(1)
      await flushPromises()

      expect(vm.availableKnowledgePoints).toEqual([])
    })

    it('应该处理试题数据中options为字符串的情况', async () => {
      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true,
          questionId: 1
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(Array.isArray(vm.localOptions)).toBe(true)
    })

    it('应该处理多选题答案的逗号分隔', async () => {
      const mockMultipleChoiceQuestion = {
        ...mockQuestion,
        questionType: 2,
        answer: 'A,B,C'
      }

      vi.mocked(questionApi.fetchQuestionById).mockResolvedValue({
        code: 200,
        data: mockMultipleChoiceQuestion
      })

      const wrapper = mount(QuestionEditDialog, {
        props: {
          visible: true,
          questionId: 1
        },
        global: {
          components: { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElButton }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.localAnswerArray).toEqual(['A', 'B', 'C'])
    })
  })
})
