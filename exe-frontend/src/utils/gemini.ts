/**
 * Google Gemini AI客户端
 * 使用Google Gemini API提供智能问答功能
 */

export interface ChatMessage {
  role: 'user' | 'model'
  parts: Array<{ text: string }>
}

export interface GeminiConfig {
  apiKey: string
  baseURL?: string
  model?: string
  temperature?: number
  maxTokens?: number
}

export class GeminiClient {
  private config: Required<GeminiConfig>

  constructor(config: GeminiConfig) {
    this.config = {
      apiKey: config.apiKey,
      baseURL: config.baseURL || 'https://generativelanguage.googleapis.com/v1beta',
      model: config.model || 'gemini-1.5-flash',
      temperature: config.temperature ?? 0.7,
      maxTokens: config.maxTokens || 2000
    }
  }

  /**
   * 发送聊天请求
   */
  async chat(messages: Array<{ role: string; content: string }>, systemPrompt?: string): Promise<string> {
    try {
      // 转换消息格式为Gemini格式
      const contents = messages.map(msg => ({
        role: msg.role === 'assistant' ? 'model' : 'user',
        parts: [{ text: msg.content }]
      }))

      // 如果有系统提示词，添加到第一条消息
      if (systemPrompt && contents.length > 0) {
        contents[0].parts[0].text = `${systemPrompt}\n\n${contents[0].parts[0].text}`
      }

      const response = await fetch(
        `${this.config.baseURL}/models/${this.config.model}:generateContent?key=${this.config.apiKey}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            contents: contents,
            generationConfig: {
              temperature: this.config.temperature,
              maxOutputTokens: this.config.maxTokens
            }
          })
        }
      )

      if (!response.ok) {
        const error = await response.json()
        throw new Error(`Gemini API错误: ${error.error?.message || response.statusText}`)
      }

      const data = await response.json()
      return data.candidates[0].content.parts[0].text
    } catch (error) {
      console.error('Gemini API调用失败:', error)
      throw error
    }
  }

  /**
   * 流式聊天（支持实时响应）
   */
  async chatStream(
    messages: Array<{ role: string; content: string }>,
    systemPrompt?: string,
    onChunk?: (text: string) => void
  ): Promise<string> {
    try {
      // 转换消息格式
      const contents = messages.map(msg => ({
        role: msg.role === 'assistant' ? 'model' : 'user',
        parts: [{ text: msg.content }]
      }))

      // 添加系统提示词
      if (systemPrompt && contents.length > 0) {
        contents[0].parts[0].text = `${systemPrompt}\n\n${contents[0].parts[0].text}`
      }

      const response = await fetch(
        `${this.config.baseURL}/models/${this.config.model}:streamGenerateContent?key=${this.config.apiKey}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            contents: contents,
            generationConfig: {
              temperature: this.config.temperature,
              maxOutputTokens: this.config.maxTokens
            }
          })
        }
      )

      if (!response.ok) {
        const error = await response.json()
        throw new Error(`Gemini API错误: ${error.error?.message || response.statusText}`)
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

        // Gemini的流式响应是以JSON行分隔的
        const lines = chunk.split('\n').filter(line => line.trim())

        for (const line of lines) {
          try {
            const parsed = JSON.parse(line)
            const text = parsed.candidates?.[0]?.content?.parts?.[0]?.text

            if (text) {
              fullText += text
              onChunk?.(text)
            }
          } catch (e) {
            // 忽略解析错误
          }
        }
      }

      return fullText
    } catch (error) {
      console.error('Gemini流式调用失败:', error)
      throw error
    }
  }

  /**
   * AI讲解知识点
   */
  async explainKnowledgePoint(topic: string, level: string = '初中'): Promise<string> {
    const systemPrompt = `你是一位专业的${level}教师，擅长用简单易懂的方式讲解知识点。`

    const messages = [
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

    const messages = [
      { role: 'user', content }
    ]

    return this.chat(messages, systemPrompt)
  }

  /**
   * AI生成练习题
   */
  async generateExercises(topic: string, count: number = 3, difficulty: string = '中等'): Promise<string> {
    const systemPrompt = '你是一位经验丰富的出题老师。'

    const messages = [
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

    const messages = [
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

    const messages = [
      { role: 'user', content }
    ]

    return this.chat(messages, systemPrompt)
  }
}

/**
 * 创建Gemini客户端实例
 */
export function createGeminiClient(apiKey?: string): GeminiClient {
  const key = apiKey || import.meta.env.VITE_GEMINI_API_KEY

  if (!key) {
    throw new Error('未配置Gemini API Key，请在.env文件中设置VITE_GEMINI_API_KEY')
  }

  return new GeminiClient({ apiKey: key })
}

export default GeminiClient
