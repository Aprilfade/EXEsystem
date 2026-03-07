import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ElCard, ElButton, ElInput, ElMessage, ElMessageBox, ElDrawer } from 'element-plus'
import AiChatPanel from './AiChatPanel.vue'
import { useStudentAuthStore } from '@/stores/studentAuth'
import { setActivePinia, createPinia } from 'pinia'
import request from '@/utils/request'

// Mock request
vi.mock('@/utils/request', () => ({
  default: vi.fn()
}))

// Mock marked
vi.mock('marked', () => ({
  marked: vi.fn((content: string) => content)
}))

// Mock ElMessage and ElMessageBox
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

describe('AiChatPanel 组件测试', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()

    // Mock successful AI response by default
    vi.mocked(request).mockResolvedValue({
      code: 200,
      data: {
        message: 'AI回复内容',
        sessionId: 'session-123',
        suggestions: ['建议问题1', '建议问题2']
      }
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('应该正确渲染组件', () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('.ai-chat-panel').exists()).toBe(true)
    })

    it('应该显示欢迎消息', () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      expect(wrapper.find('.welcome-message').exists()).toBe(true)
      expect(wrapper.text()).toContain('你好！我是小艾')
    })

    it('应该显示快捷问题按钮', () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const quickButtons = wrapper.findAll('.quick-questions .el-button')
      expect(quickButtons.length).toBeGreaterThan(0)
    })
  })

  // ==================== 消息发送测试 ====================

  describe('消息发送', () => {
    it('应该能发送消息', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')
      localStorage.setItem('student_ai_provider', 'deepseek')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      await vm.sendMessage()
      await flushPromises()

      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: '/api/v1/student/ai-chat/send',
          method: 'post',
          data: expect.objectContaining({
            message: '测试消息'
          })
        })
      )

      expect(vm.messages).toHaveLength(2) // 用户消息 + AI回复
    })

    it('没有AI Key时应该提示用户配置', async () => {
      localStorage.removeItem('student_ai_key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      await vm.sendMessage()

      expect(ElMessage.warning).toHaveBeenCalledWith('请先在个人设置中配置AI Key')
      expect(request).not.toHaveBeenCalled()
    })

    it('发送空消息时不应该调用API', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '   ' // 空白字符

      await vm.sendMessage()

      expect(request).not.toHaveBeenCalled()
    })

    it('发送消息时应该显示loading状态', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      const sendPromise = vm.sendMessage()

      expect(vm.isSending).toBe(true)
      expect(vm.isTyping).toBe(true)

      await sendPromise
      await flushPromises()

      expect(vm.isSending).toBe(false)
      expect(vm.isTyping).toBe(false)
    })

    it('发送失败时应该显示错误消息', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')
      vi.mocked(request).mockRejectedValue(new Error('Network error'))

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      await vm.sendMessage()
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalled()
    })

    it('发送成功后应该清空输入框', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      await vm.sendMessage()
      await flushPromises()

      expect(vm.inputMessage).toBe('')
    })
  })

  // ==================== 快捷问题测试 ====================

  describe('快捷问题', () => {
    it('点击快捷问题应该发送消息', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      await vm.sendQuickQuestion('帮我分析一下我的学习情况', 'general')
      await flushPromises()

      expect(request).toHaveBeenCalled()
      expect(vm.chatType).toBe('general')
    })
  })

  // ==================== 消息显示测试 ====================

  describe('消息显示', () => {
    it('应该正确显示用户消息', async () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.messages = [
        { role: 'user', content: '用户消息' }
      ]

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.message-user').exists()).toBe(true)
      expect(wrapper.text()).toContain('用户消息')
    })

    it('应该正确显示AI消息', async () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.messages = [
        { role: 'assistant', content: 'AI回复' }
      ]

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.message-ai').exists()).toBe(true)
      expect(wrapper.text()).toContain('AI回复')
    })

    it('应该支持Markdown渲染', async () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      const rendered = vm.renderMarkdown('**加粗文本**')

      expect(rendered).toBeDefined()
    })

    it('应该显示建议问题', async () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.showSuggestions = true
      vm.suggestions = ['建议问题1', '建议问题2']

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.suggestions-bar').exists()).toBe(true)
    })
  })

  // ==================== 对话类型测试 ====================

  describe('对话类型', () => {
    it('应该支持切换对话类型', async () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.chatType = 'learning'

      await wrapper.vm.$nextTick()

      expect(vm.chatType).toBe('learning')
    })

    it('应该有多种对话类型选项', () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.chatTypeOptions).toHaveLength(5)
      expect(vm.chatTypeOptions[0].value).toBe('general')
    })
  })

  // ==================== 历史会话测试 ====================

  describe('历史会话', () => {
    it('应该能加载会话列表', async () => {
      vi.mocked(request).mockResolvedValue({
        code: 200,
        data: [
          { sessionId: 'session-1', title: '会话1', messageCount: 5, lastMessageTime: '2024-01-01' },
          { sessionId: 'session-2', title: '会话2', messageCount: 3, lastMessageTime: '2024-01-02' }
        ]
      })

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput, ElDrawer }
        }
      })

      const vm = wrapper.vm as any
      await vm.loadSessions()
      await flushPromises()

      expect(vm.sessions).toHaveLength(2)
    })

    it('应该能加载历史会话消息', async () => {
      const mockMessages = [
        { role: 'user', content: '历史消息1' },
        { role: 'assistant', content: '历史回复1' }
      ]

      vi.mocked(request).mockResolvedValue({
        code: 200,
        data: mockMessages
      })

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      await vm.loadSession('session-123')
      await flushPromises()

      expect(vm.messages).toEqual(mockMessages)
      expect(vm.currentSessionId).toBe('session-123')
      expect(ElMessage.success).toHaveBeenCalledWith('会话已加载')
    })

    it('应该能删除会话', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm' as any)
      vi.mocked(request).mockResolvedValue({ code: 200 })

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.currentSessionId = 'session-123'

      await vm.deleteSession('session-123')
      await flushPromises()

      expect(vm.messages).toEqual([])
      expect(vm.currentSessionId).toBe('')
      expect(ElMessage.success).toHaveBeenCalledWith('会话已删除')
    })
  })

  // ==================== 命令处理测试 ====================

  describe('命令处理', () => {
    it('应该能创建新对话', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm' as any)

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.messages = [{ role: 'user', content: '旧消息' }]

      await vm.createNewChat()
      await flushPromises()

      expect(vm.messages).toEqual([])
      expect(vm.currentSessionId).toBe('')
      expect(ElMessage.success).toHaveBeenCalledWith('已开始新对话')
    })

    it('应该能清空对话', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm' as any)

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.messages = [{ role: 'user', content: '消息' }]

      await vm.clearChat()
      await flushPromises()

      expect(vm.messages).toEqual([])
      expect(ElMessage.success).toHaveBeenCalledWith('对话已清空')
    })

    it('取消清空对话时不应该清空消息', async () => {
      vi.mocked(ElMessageBox.confirm).mockRejectedValue('cancel')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.messages = [{ role: 'user', content: '消息' }]

      await vm.clearChat()
      await flushPromises()

      expect(vm.messages).toHaveLength(1)
    })
  })

  // ==================== 辅助功能测试 ====================

  describe('辅助功能', () => {
    it('应该支持复制消息', async () => {
      const mockClipboard = {
        writeText: vi.fn().mockResolvedValue(undefined)
      }
      Object.assign(navigator, {
        clipboard: mockClipboard
      })

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      await vm.copyMessage('要复制的内容')

      expect(mockClipboard.writeText).toHaveBeenCalledWith('要复制的内容')
      expect(ElMessage.success).toHaveBeenCalledWith('已复制到剪贴板')
    })

    it('复制失败时应该显示错误', async () => {
      const mockClipboard = {
        writeText: vi.fn().mockRejectedValue(new Error('Copy failed'))
      }
      Object.assign(navigator, {
        clipboard: mockClipboard
      })

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      await vm.copyMessage('内容')

      expect(ElMessage.error).toHaveBeenCalledWith('复制失败')
    })

    it('应该正确格式化时间', () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any

      // 刚刚
      const justNow = new Date().toISOString()
      expect(vm.formatTime(justNow)).toBe('刚刚')

      // 几分钟前
      const minutesAgo = new Date(Date.now() - 5 * 60 * 1000).toISOString()
      expect(vm.formatTime(minutesAgo)).toContain('分钟前')
    })
  })

  // ==================== 键盘事件测试 ====================

  describe('键盘事件', () => {
    it('按Enter应该发送消息', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      const event = new KeyboardEvent('keydown', { key: 'Enter', shiftKey: false })
      const preventDefaultSpy = vi.spyOn(event, 'preventDefault')

      await vm.handleEnter(event)
      await flushPromises()

      expect(preventDefaultSpy).toHaveBeenCalled()
      expect(request).toHaveBeenCalled()
    })

    it('按Shift+Enter应该换行而不发送', () => {
      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      const event = new KeyboardEvent('keydown', { key: 'Enter', shiftKey: true })
      vm.handleEnter(event)

      expect(request).not.toHaveBeenCalled()
    })
  })

  // ==================== 会话管理测试 ====================

  describe('会话管理', () => {
    it('发送消息时应该更新会话ID', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      await vm.sendMessage()
      await flushPromises()

      expect(vm.currentSessionId).toBe('session-123')
    })

    it('应该支持在现有会话中继续对话', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.currentSessionId = 'existing-session'
      vm.inputMessage = '继续对话'

      await vm.sendMessage()
      await flushPromises()

      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({
            sessionId: 'existing-session'
          })
        })
      )
    })

    it('应该支持上下文记忆', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.useContext = true
      vm.inputMessage = '测试消息'

      await vm.sendMessage()
      await flushPromises()

      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({
            useContext: true,
            contextSize: 5
          })
        })
      )
    })
  })

  // ==================== 边界情况测试 ====================

  describe('边界情况', () => {
    it('应该处理API返回非200状态码', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')
      vi.mocked(request).mockResolvedValue({
        code: 500,
        message: '服务器错误'
      })

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      await vm.sendMessage()
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalledWith('服务器错误')
    })

    it('应该处理空的建议列表', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')
      vi.mocked(request).mockResolvedValue({
        code: 200,
        data: {
          message: 'AI回复',
          sessionId: 'session-123',
          suggestions: []
        }
      })

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      await vm.sendMessage()
      await flushPromises()

      expect(vm.suggestions).toEqual([])
    })

    it('应该处理没有sessionId的响应', async () => {
      localStorage.setItem('student_ai_key', 'test-api-key')
      vi.mocked(request).mockResolvedValue({
        code: 200,
        data: {
          message: 'AI回复'
        }
      })

      const wrapper = mount(AiChatPanel, {
        global: {
          components: { ElCard, ElButton, ElInput }
        }
      })

      const vm = wrapper.vm as any
      vm.inputMessage = '测试消息'

      await vm.sendMessage()
      await flushPromises()

      // 应该能正常处理
      expect(vm.messages.length).toBeGreaterThan(0)
    })
  })
})
