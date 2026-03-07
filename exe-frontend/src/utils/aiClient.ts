/**
 * AI客户端管理器
 * 统一管理多个AI提供商（DeepSeek、Claude、Gemini）
 */

import { DeepSeekClient, createDeepSeekClient } from './deepseek'
import { ClaudeClient, createClaudeClient } from './claude'
import { GeminiClient, createGeminiClient } from './gemini'

export type AIProvider = 'deepseek' | 'claude' | 'gemini'

export interface AIMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export interface AIClientConfig {
  provider?: AIProvider
  apiKey?: string
  model?: string
  temperature?: number
  maxTokens?: number
}

/**
 * AI客户端管理器
 * 自动根据配置选择合适的AI提供商
 */
export class AIClientManager {
  private provider: AIProvider
  private deepseekClient?: DeepSeekClient
  private claudeClient?: ClaudeClient
  private geminiClient?: GeminiClient

  constructor(config: AIClientConfig = {}) {
    this.provider = config.provider || this.getDefaultProvider()
    this.initializeClient(config)
  }

  /**
   * 获取默认提供商（根据环境变量）
   */
  private getDefaultProvider(): AIProvider {
    if (import.meta.env.VITE_CLAUDE_API_KEY) return 'claude'
    if (import.meta.env.VITE_GEMINI_API_KEY) return 'gemini'
    if (import.meta.env.VITE_DEEPSEEK_API_KEY) return 'deepseek'

    return 'deepseek' // 默认
  }

  /**
   * 初始化客户端
   */
  private initializeClient(config: AIClientConfig) {
    try {
      switch (this.provider) {
        case 'deepseek':
          this.deepseekClient = config.apiKey
            ? new DeepSeekClient({ apiKey: config.apiKey, ...config })
            : createDeepSeekClient()
          break
        case 'claude':
          this.claudeClient = config.apiKey
            ? new ClaudeClient({ apiKey: config.apiKey, ...config })
            : createClaudeClient()
          break
        case 'gemini':
          this.geminiClient = config.apiKey
            ? new GeminiClient({ apiKey: config.apiKey, ...config })
            : createGeminiClient()
          break
      }
    } catch (error) {
      console.error(`初始化${this.provider}客户端失败:`, error)
      // 尝试降级到其他提供商
      this.fallbackToAvailableProvider()
    }
  }

  /**
   * 降级到可用的提供商
   */
  private fallbackToAvailableProvider() {
    const providers: AIProvider[] = ['deepseek', 'claude', 'gemini']

    for (const provider of providers) {
      if (provider === this.provider) continue

      try {
        this.provider = provider
        this.initializeClient({})
        console.log(`已降级到${provider}提供商`)
        return
      } catch (error) {
        continue
      }
    }

    throw new Error('没有可用的AI提供商')
  }

  /**
   * 获取当前提供商
   */
  getProvider(): AIProvider {
    return this.provider
  }

  /**
   * 切换提供商
   */
  switchProvider(provider: AIProvider, apiKey?: string) {
    this.provider = provider
    this.initializeClient({ provider, apiKey })
  }

  /**
   * 统一的聊天接口
   */
  async chat(messages: AIMessage[], systemPrompt?: string): Promise<string> {
    try {
      switch (this.provider) {
        case 'deepseek':
          if (!this.deepseekClient) throw new Error('DeepSeek客户端未初始化')
          return await this.deepseekClient.chat(
            systemPrompt
              ? [{ role: 'system', content: systemPrompt }, ...messages]
              : messages
          )

        case 'claude':
          if (!this.claudeClient) throw new Error('Claude客户端未初始化')
          // Claude不支持system role，需要转换
          const claudeMessages = messages.filter(m => m.role !== 'system')
          return await this.claudeClient.chat(claudeMessages, systemPrompt)

        case 'gemini':
          if (!this.geminiClient) throw new Error('Gemini客户端未初始化')
          return await this.geminiClient.chat(messages, systemPrompt)

        default:
          throw new Error(`不支持的提供商: ${this.provider}`)
      }
    } catch (error) {
      console.error(`${this.provider}调用失败:`, error)
      throw error
    }
  }

  /**
   * 统一的流式聊天接口
   */
  async chatStream(
    messages: AIMessage[],
    systemPrompt?: string,
    onChunk?: (text: string) => void
  ): Promise<string> {
    try {
      switch (this.provider) {
        case 'deepseek':
          if (!this.deepseekClient) throw new Error('DeepSeek客户端未初始化')
          return await this.deepseekClient.chatStream(
            systemPrompt
              ? [{ role: 'system', content: systemPrompt }, ...messages]
              : messages,
            onChunk
          )

        case 'claude':
          if (!this.claudeClient) throw new Error('Claude客户端未初始化')
          const claudeMessages = messages.filter(m => m.role !== 'system')
          return await this.claudeClient.chatStream(claudeMessages, systemPrompt, onChunk)

        case 'gemini':
          if (!this.geminiClient) throw new Error('Gemini客户端未初始化')
          return await this.geminiClient.chatStream(messages, systemPrompt, onChunk)

        default:
          throw new Error(`不支持的提供商: ${this.provider}`)
      }
    } catch (error) {
      console.error(`${this.provider}流式调用失败:`, error)
      throw error
    }
  }

  /**
   * AI讲解知识点
   */
  async explainKnowledgePoint(topic: string, level: string = '初中'): Promise<string> {
    const client = this.getCurrentClient()
    return await client.explainKnowledgePoint(topic, level)
  }

  /**
   * AI辅导习题
   */
  async tutorExercise(question: string, studentAnswer?: string): Promise<string> {
    const client = this.getCurrentClient()
    return await client.tutorExercise(question, studentAnswer)
  }

  /**
   * AI生成练习题
   */
  async generateExercises(topic: string, count: number = 3, difficulty: string = '中等'): Promise<string> {
    const client = this.getCurrentClient()
    return await client.generateExercises(topic, count, difficulty)
  }

  /**
   * AI学习建议
   */
  async getStudyAdvice(subject: string, weakPoints: string[]): Promise<string> {
    const client = this.getCurrentClient()
    return await client.getStudyAdvice(subject, weakPoints)
  }

  /**
   * AI答疑解惑
   */
  async answerQuestion(question: string, context?: string): Promise<string> {
    const client = this.getCurrentClient()
    return await client.answerQuestion(question, context)
  }

  /**
   * 获取当前客户端
   */
  private getCurrentClient(): DeepSeekClient | ClaudeClient | GeminiClient {
    switch (this.provider) {
      case 'deepseek':
        if (!this.deepseekClient) throw new Error('DeepSeek客户端未初始化')
        return this.deepseekClient
      case 'claude':
        if (!this.claudeClient) throw new Error('Claude客户端未初始化')
        return this.claudeClient
      case 'gemini':
        if (!this.geminiClient) throw new Error('Gemini客户端未初始化')
        return this.geminiClient
      default:
        throw new Error(`不支持的提供商: ${this.provider}`)
    }
  }
}

/**
 * 创建默认的AI客户端管理器
 */
let defaultManager: AIClientManager | null = null

export function getAIClientManager(config?: AIClientConfig): AIClientManager {
  if (!defaultManager) {
    defaultManager = new AIClientManager(config)
  }
  return defaultManager
}

/**
 * 重置默认客户端管理器（用于测试或重新配置）
 */
export function resetAIClientManager() {
  defaultManager = null
}

export default AIClientManager
