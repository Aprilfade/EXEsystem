/**
 * Claude AI客户端
 * 使用Anthropic Claude API提供智能问答功能
 */

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface ClaudeConfig {
  apiKey: string
  baseURL?: string
  model?: string
  temperature?: number
  maxTokens?: number
}

export class ClaudeClient {
  private config: Required<ClaudeConfig>

  constructor(config: ClaudeConfig) {
    this.config = {
      apiKey: config.apiKey,
      baseURL: config.baseURL || 'https://api.anthropic.com/v1',
      model: config.model || 'claude-3-5-sonnet-20241022',
      temperature: config.temperature ?? 0.7,
      maxTokens: config.maxTokens || 2000
    }
  }

  /**
   * 发送聊天请求
   */
  async chat(messages: ChatMessage[], systemPrompt?: string): Promise<string> {
    try {
      const response = await fetch(`${this.config.baseURL}/messages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-api-key': this.config.apiKey,
          'anthropic-version': '2023-06-01'
        },
        body: JSON.stringify({
          model: this.config.model,
          messages: messages,
          system: systemPrompt,
          temperature: this.config.temperature,
          max_tokens: this.config.maxTokens
        })
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(`Claude API错误: ${error.error?.message || response.statusText}`)
      }

      const data = await response.json()
      return data.content[0].text
    } catch (error) {
      console.error('Claude API调用失败:', error)
      throw error
    }
  }

  /**
   * 流式聊天（支持实时响应）
   */
  async chatStream(
    messages: ChatMessage[],
    systemPrompt?: string,
    onChunk?: (text: string) => void
  ): Promise<string> {
    try {
      const response = await fetch(`${this.config.baseURL}/messages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-api-key': this.config.apiKey,
          'anthropic-version': '2023-06-01'
        },
        body: JSON.stringify({
          model: this.config.model,
          messages: messages,
          system: systemPrompt,
          temperature: this.config.temperature,
          max_tokens: this.config.maxTokens,
          stream: true
        })
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(`Claude API错误: ${error.error?.message || response.statusText}`)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('无法获取响应流')
      }

      const decoder = new TextDecoder()
      let fullText = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value)
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6)
            if (data === '[DONE]') continue

            try {
              const parsed = JSON.parse(data)

              if (parsed.type === 'content_block_delta') {
                const text = parsed.delta?.text || ''
                if (text) {
                  fullText += text
                  onChunk?.(text)
                }
              }
            } catch (e) {
              // 忽略解析错误
            }
          }
        }
      }

      return fullText
    } catch (error) {
      console.error('Claude流式调用失败:', error)
      throw error
    }
  }

  /**
   * AI讲解知识点
   */
  async explainKnowledgePoint(topic: string, level: string = '初中'): Promise<string> {
    const systemPrompt = `你是一位专业的${level}教师，擅长用简单易懂的方式讲解知识点。`

    const messages: ChatMessage[] = [
      {
        role: 'user',
        content: `请详细讲解以下知识点，包括：
1. 核心概念
2. 重要公式或定理
3. 常见题型
4. 易错点提示

知识点：${topic}`
      }
    ]

    return this.chat(messages, systemPrompt)
  }

  /**
   * AI辅导习题
   */
  async tutorExercise(question: string, studentAnswer?: string): Promise<string> {
    const systemPrompt = '你是一位耐心的辅导老师，善于引导学生思考，而不是直接给出答案。'

    let content = `题目：${question}`
    if (studentAnswer) {
      content += `\n\n学生的答案：${studentAnswer}\n\n请分析学生的答案，指出问题并给予提示。`
    } else {
      content += '\n\n请给出解题思路和提示，引导学生自己解答。'
    }

    const messages: ChatMessage[] = [
      { role: 'user', content }
    ]

    return this.chat(messages, systemPrompt)
  }

  /**
   * AI生成练习题
   */
  async generateExercises(topic: string, count: number = 3, difficulty: string = '中等'): Promise<string> {
    const systemPrompt = '你是一位经验丰富的出题老师。'

    const messages: ChatMessage[] = [
      {
        role: 'user',
        content: `请生成${count}道关于"${topic}"的${difficulty}难度练习题，包含：
1. 题目内容
2. 参考答案
3. 解题思路

以Markdown格式输出。`
      }
    ]

    return this.chat(messages, systemPrompt)
  }

  /**
   * AI学习建议
   */
  async getStudyAdvice(subject: string, weakPoints: string[]): Promise<string> {
    const systemPrompt = '你是一位学习规划专家，擅长制定个性化学习计划。'

    const messages: ChatMessage[] = [
      {
        role: 'user',
        content: `学科：${subject}\n薄弱知识点：${weakPoints.join('、')}\n\n请给出针对性的学习建议和复习计划。`
      }
    ]

    return this.chat(messages, systemPrompt)
  }

  /**
   * AI答疑解惑
   */
  async answerQuestion(question: string, context?: string): Promise<string> {
    const systemPrompt = '你是一位知识渊博的老师，擅长解答学生的各种问题。'

    let content = question
    if (context) {
      content = `背景信息：${context}\n\n问题：${question}`
    }

    const messages: ChatMessage[] = [
      { role: 'user', content }
    ]

    return this.chat(messages, systemPrompt)
  }
}

/**
 * 创建Claude客户端实例
 */
export function createClaudeClient(apiKey?: string): ClaudeClient {
  const key = apiKey || import.meta.env.VITE_CLAUDE_API_KEY

  if (!key) {
    throw new Error('未配置Claude API Key，请在.env文件中设置VITE_CLAUDE_API_KEY')
  }

  return new ClaudeClient({ apiKey: key })
}

export default ClaudeClient
