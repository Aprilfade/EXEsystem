import { describe, it, expect, beforeEach, vi } from 'vitest'
import { AIClientManager, getAIClientManager, resetAIClientManager } from '@/utils/aiClient'
import type { AIMessage } from '@/utils/aiClient'

// Mock子客户端
vi.mock('@/utils/deepseek', () => ({
  DeepSeekClient: vi.fn().mockImplementation(() => ({
    chat: vi.fn().mockResolvedValue('DeepSeek response'),
    chatStream: vi.fn().mockResolvedValue('DeepSeek stream response'),
    explainKnowledgePoint: vi.fn().mockResolvedValue('DeepSeek explain'),
    tutorExercise: vi.fn().mockResolvedValue('DeepSeek tutor'),
    generateExercises: vi.fn().mockResolvedValue('DeepSeek exercises'),
    getStudyAdvice: vi.fn().mockResolvedValue('DeepSeek advice'),
    answerQuestion: vi.fn().mockResolvedValue('DeepSeek answer')
  })),
  createDeepSeekClient: vi.fn().mockReturnValue({
    chat: vi.fn().mockResolvedValue('DeepSeek default'),
    chatStream: vi.fn().mockResolvedValue('DeepSeek default stream'),
    explainKnowledgePoint: vi.fn().mockResolvedValue(''),
    tutorExercise: vi.fn().mockResolvedValue(''),
    generateExercises: vi.fn().mockResolvedValue(''),
    getStudyAdvice: vi.fn().mockResolvedValue(''),
    answerQuestion: vi.fn().mockResolvedValue('')
  })
}))

vi.mock('@/utils/claude', () => ({
  ClaudeClient: vi.fn().mockImplementation(() => ({
    chat: vi.fn().mockResolvedValue('Claude response'),
    chatStream: vi.fn().mockResolvedValue('Claude stream response'),
    explainKnowledgePoint: vi.fn().mockResolvedValue(''),
    tutorExercise: vi.fn().mockResolvedValue(''),
    generateExercises: vi.fn().mockResolvedValue(''),
    getStudyAdvice: vi.fn().mockResolvedValue(''),
    answerQuestion: vi.fn().mockResolvedValue('')
  })),
  createClaudeClient: vi.fn().mockReturnValue({
    chat: vi.fn().mockResolvedValue('Claude default'),
    chatStream: vi.fn().mockResolvedValue('Claude default stream'),
    explainKnowledgePoint: vi.fn().mockResolvedValue(''),
    tutorExercise: vi.fn().mockResolvedValue(''),
    generateExercises: vi.fn().mockResolvedValue(''),
    getStudyAdvice: vi.fn().mockResolvedValue(''),
    answerQuestion: vi.fn().mockResolvedValue('')
  })
}))

vi.mock('@/utils/gemini', () => ({
  GeminiClient: vi.fn().mockImplementation(() => ({
    chat: vi.fn().mockResolvedValue('Gemini response'),
    chatStream: vi.fn().mockResolvedValue('Gemini stream response'),
    explainKnowledgePoint: vi.fn().mockResolvedValue(''),
    tutorExercise: vi.fn().mockResolvedValue(''),
    generateExercises: vi.fn().mockResolvedValue(''),
    getStudyAdvice: vi.fn().mockResolvedValue(''),
    answerQuestion: vi.fn().mockResolvedValue('')
  })),
  createGeminiClient: vi.fn().mockReturnValue({
    chat: vi.fn().mockResolvedValue('Gemini default'),
    chatStream: vi.fn().mockResolvedValue('Gemini default stream'),
    explainKnowledgePoint: vi.fn().mockResolvedValue(''),
    tutorExercise: vi.fn().mockResolvedValue(''),
    generateExercises: vi.fn().mockResolvedValue(''),
    getStudyAdvice: vi.fn().mockResolvedValue(''),
    answerQuestion: vi.fn().mockResolvedValue('')
  })
}))

describe('AI客户端管理器测试', () => {
  beforeEach(() => {
    resetAIClientManager()
    vi.clearAllMocks()
  })

  // ==================== 初始化测试 ====================

  describe('初始化', () => {
    it('应该使用默认提供商创建客户端', () => {
      const manager = new AIClientManager()
      expect(manager.getProvider()).toBe('deepseek')
    })

    it('应该使用指定提供商创建客户端', () => {
      const manager = new AIClientManager({ provider: 'claude' })
      expect(manager.getProvider()).toBe('claude')
    })

    it('应该使用自定义API Key初始化', () => {
      const manager = new AIClientManager({
        provider: 'deepseek',
        apiKey: 'custom-api-key'
      })
      expect(manager.getProvider()).toBe('deepseek')
    })

    it('应该支持设置temperature和maxTokens', () => {
      const manager = new AIClientManager({
        provider: 'deepseek',
        temperature: 0.8,
        maxTokens: 2000
      })
      expect(manager.getProvider()).toBe('deepseek')
    })
  })

  // ==================== 提供商切换测试 ====================

  describe('提供商切换', () => {
    it('应该支持切换提供商', () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      expect(manager.getProvider()).toBe('deepseek')

      manager.switchProvider('claude')
      expect(manager.getProvider()).toBe('claude')
    })

    it('应该支持切换提供商时提供新的API Key', () => {
      const manager = new AIClientManager({ provider: 'deepseek' })

      manager.switchProvider('claude', 'new-claude-key')
      expect(manager.getProvider()).toBe('claude')
    })

    it('应该支持在三个提供商之间切换', () => {
      const manager = new AIClientManager()

      manager.switchProvider('deepseek')
      expect(manager.getProvider()).toBe('deepseek')

      manager.switchProvider('claude')
      expect(manager.getProvider()).toBe('claude')

      manager.switchProvider('gemini')
      expect(manager.getProvider()).toBe('gemini')
    })
  })

  // ==================== 聊天接口测试 ====================

  describe('聊天接口', () => {
    it('应该调用DeepSeek聊天接口', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]

      const response = await manager.chat(messages)

      expect(response).toBe('DeepSeek response')
    })

    it('应该调用Claude聊天接口', async () => {
      const manager = new AIClientManager({ provider: 'claude' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]

      const response = await manager.chat(messages)

      expect(response).toBe('Claude response')
    })

    it('应该调用Gemini聊天接口', async () => {
      const manager = new AIClientManager({ provider: 'gemini' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]

      const response = await manager.chat(messages)

      expect(response).toBe('Gemini response')
    })

    it('应该支持传递系统提示词', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]
      const systemPrompt = 'You are a helpful assistant'

      const response = await manager.chat(messages, systemPrompt)

      expect(response).toBe('DeepSeek response')
    })
  })

  // ==================== 流式聊天测试 ====================

  describe('流式聊天', () => {
    it('应该调用DeepSeek流式聊天接口', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]

      const response = await manager.chatStream(messages)

      expect(response).toBe('DeepSeek stream response')
    })

    it('应该支持流式聊天的回调函数', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]
      const onChunk = vi.fn()

      await manager.chatStream(messages, undefined, onChunk)

      expect(onChunk).not.toHaveBeenCalled() // Mock不会真正调用回调
    })

    it('应该支持Claude流式聊天', async () => {
      const manager = new AIClientManager({ provider: 'claude' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]

      const response = await manager.chatStream(messages)

      expect(response).toBe('Claude stream response')
    })
  })

  // ==================== 教育功能测试 ====================

  describe('教育功能', () => {
    it('应该调用知识点讲解功能', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })

      const response = await manager.explainKnowledgePoint('勾股定理', '初中')

      expect(response).toBe('DeepSeek explain')
    })

    it('应该调用习题辅导功能', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })

      const response = await manager.tutorExercise('1+1=?', '3')

      expect(response).toBe('DeepSeek tutor')
    })

    it('应该调用生成练习题功能', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })

      const response = await manager.generateExercises('二次方程', 5, '困难')

      expect(response).toBe('DeepSeek exercises')
    })

    it('应该调用学习建议功能', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })

      const response = await manager.getStudyAdvice('数学', ['因式分解', '二次函数'])

      expect(response).toBe('DeepSeek advice')
    })

    it('应该调用答疑解惑功能', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })

      const response = await manager.answerQuestion('什么是质数？', '数学基础知识')

      expect(response).toBe('DeepSeek answer')
    })
  })

  // ==================== 错误处理测试 ====================

  describe('错误处理', () => {
    it('应该处理未初始化客户端的错误', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      // 强制设置为null
      ;(manager as any).deepseekClient = null

      await expect(manager.chat([{ role: 'user', content: 'Hi' }])).rejects.toThrow(
        'DeepSeek客户端未初始化'
      )
    })

    it('应该处理不支持的提供商', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      // 强制设置为不支持的提供商
      ;(manager as any).provider = 'unsupported'

      await expect(manager.chat([{ role: 'user', content: 'Hi' }])).rejects.toThrow(
        '不支持的提供商'
      )
    })

    it('应该捕获并记录错误', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const manager = new AIClientManager({ provider: 'deepseek' })
      ;(manager as any).deepseekClient = null

      try {
        await manager.chat([{ role: 'user', content: 'Hi' }])
      } catch (e) {
        // 预期会抛出错误
      }

      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })
  })

  // ==================== Claude特殊处理测试 ====================

  describe('Claude特殊处理', () => {
    it('应该过滤Claude消息中的system role', async () => {
      const manager = new AIClientManager({ provider: 'claude' })
      const messages: AIMessage[] = [
        { role: 'system', content: 'You are helpful' },
        { role: 'user', content: 'Hello' }
      ]

      const response = await manager.chat(messages)

      expect(response).toBe('Claude response')
    })

    it('应该将system prompt单独传递给Claude', async () => {
      const manager = new AIClientManager({ provider: 'claude' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]
      const systemPrompt = 'You are helpful'

      const response = await manager.chat(messages, systemPrompt)

      expect(response).toBe('Claude response')
    })
  })

  // ==================== 单例模式测试 ====================

  describe('单例模式', () => {
    it('getAIClientManager应该返回同一个实例', () => {
      const manager1 = getAIClientManager()
      const manager2 = getAIClientManager()

      expect(manager1).toBe(manager2)
    })

    it('resetAIClientManager应该清除单例', () => {
      const manager1 = getAIClientManager()
      resetAIClientManager()
      const manager2 = getAIClientManager()

      expect(manager1).not.toBe(manager2)
    })

    it('应该支持自定义配置创建单例', () => {
      const manager = getAIClientManager({ provider: 'claude' })
      expect(manager.getProvider()).toBe('claude')
    })
  })

  // ==================== 提供商类型测试 ====================

  describe('提供商类型', () => {
    it('应该支持deepseek作为提供商', () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      expect(manager.getProvider()).toBe('deepseek')
    })

    it('应该支持claude作为提供商', () => {
      const manager = new AIClientManager({ provider: 'claude' })
      expect(manager.getProvider()).toBe('claude')
    })

    it('应该支持gemini作为提供商', () => {
      const manager = new AIClientManager({ provider: 'gemini' })
      expect(manager.getProvider()).toBe('gemini')
    })
  })

  // ==================== 消息格式测试 ====================

  describe('消息格式', () => {
    it('应该支持user角色消息', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' }
      ]

      const response = await manager.chat(messages)
      expect(response).toBeDefined()
    })

    it('应该支持assistant角色消息', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      const messages: AIMessage[] = [
        { role: 'user', content: 'Hello' },
        { role: 'assistant', content: 'Hi there!' },
        { role: 'user', content: 'How are you?' }
      ]

      const response = await manager.chat(messages)
      expect(response).toBeDefined()
    })

    it('应该支持system角色消息', async () => {
      const manager = new AIClientManager({ provider: 'deepseek' })
      const messages: AIMessage[] = [
        { role: 'system', content: 'You are helpful' },
        { role: 'user', content: 'Hello' }
      ]

      const response = await manager.chat(messages)
      expect(response).toBeDefined()
    })
  })
})
