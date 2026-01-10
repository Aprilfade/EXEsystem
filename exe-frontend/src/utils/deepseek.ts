/**
 * DeepSeek AI客户端
 * 用于AI家教功能的智能问答、知识点讲解、习题辅导等
 */

export interface ChatMessage {
  role: 'system' | 'user' | 'assistant'
  content: string
}

export interface DeepSeekConfig {
  apiKey: string
  baseURL?: string
  model?: string
  temperature?: number
  maxTokens?: number
}

export class DeepSeekClient {
  private config: Required<DeepSeekConfig>

  constructor(config: DeepSeekConfig) {
    this.config = {
      apiKey: config.apiKey,
      baseURL: config.baseURL || 'https://api.deepseek.com/v1',
      model: config.model || 'deepseek-chat',
      temperature: config.temperature ?? 0.7,
      maxTokens: config.maxTokens || 2000
    }
  }

  /**
   * 发送聊天请求
   */
  async chat(messages: ChatMessage[]): Promise<string> {
    try {
      const response = await fetch(`${this.config.baseURL}/chat/completions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.config.apiKey}`
        },
        body: JSON.stringify({
          model: this.config.model,
          messages: messages,
          temperature: this.config.temperature,
          max_tokens: this.config.maxTokens,
          stream: false
        })
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.error?.message || '请求失败')
      }

      const data = await response.json()
      return data.choices[0].message.content
    } catch (error: any) {
      console.error('DeepSeek API调用失败:', error)
      throw new Error(error.message || 'AI服务暂时不可用')
    }
  }

  /**
   * 流式聊天（用于实时显示AI回复）
   */
  async *chatStream(messages: ChatMessage[]): AsyncGenerator<string> {
    try {
      const response = await fetch(`${this.config.baseURL}/chat/completions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.config.apiKey}`
        },
        body: JSON.stringify({
          model: this.config.model,
          messages: messages,
          temperature: this.config.temperature,
          max_tokens: this.config.maxTokens,
          stream: true
        })
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.error?.message || '请求失败')
      }

      const reader = response.body?.getReader()
      const decoder = new TextDecoder()

      if (!reader) {
        throw new Error('无法读取响应流')
      }

      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6)
            if (data === '[DONE]') continue

            try {
              const parsed = JSON.parse(data)
              const content = parsed.choices[0]?.delta?.content
              if (content) {
                yield content
              }
            } catch (e) {
              // 忽略解析错误
            }
          }
        }
      }
    } catch (error: any) {
      console.error('DeepSeek Stream API调用失败:', error)
      throw new Error(error.message || 'AI服务暂时不可用')
    }
  }

  /**
   * 讲解知识点
   */
  async explainKnowledgePoint(
    kpName: string,
    grade: string,
    subject: string
  ): Promise<string> {
    const messages: ChatMessage[] = [
      {
        role: 'system',
        content: `你是一位经验丰富的${subject}老师，擅长用通俗易懂的语言给${grade}学生讲解知识。请用循序渐进、生动有趣的方式解释概念，并给出实际例子。`
      },
      {
        role: 'user',
        content: `请详细讲解"${kpName}"这个知识点，要求：
1. 先用一句话简单概括核心概念
2. 详细解释原理和要点
3. 给出2-3个具体例子
4. 总结重点和易错点

请用Markdown格式输出，包含适当的标题和列表。`
      }
    ]

    return await this.chat(messages)
  }

  /**
   * 辅导习题
   */
  async tutorExercise(
    question: string,
    studentAnswer: string,
    correctAnswer: string,
    subject: string
  ): Promise<string> {
    const messages: ChatMessage[] = [
      {
        role: 'system',
        content: `你是一位耐心的${subject}辅导老师，专注于引导学生理解错误并掌握正确方法。`
      },
      {
        role: 'user',
        content: `题目：${question}

学生答案：${studentAnswer}
正确答案：${correctAnswer}

请作为老师进行辅导：
1. 指出学生答案的问题所在
2. 详细讲解正确的解题思路和方法
3. 给出易错点提醒
4. 鼓励学生继续努力

请用温和、鼓励的语气，帮助学生真正理解。`
      }
    ]

    return await this.chat(messages)
  }

  /**
   * 生成练习题
   */
  async generateExercises(
    kpName: string,
    difficulty: '简单' | '中等' | '困难',
    count: number,
    subject: string
  ): Promise<Array<{
    question: string
    options?: string[]
    answer: string
    explanation: string
  }>> {
    const messages: ChatMessage[] = [
      {
        role: 'system',
        content: `你是一位${subject}老师，擅长根据知识点设计合适难度的练习题。`
      },
      {
        role: 'user',
        content: `请针对"${kpName}"这个知识点，生成${count}道${difficulty}难度的练习题。

要求：
1. 题目类型可以是选择题、填空题或简答题
2. 每道题都要有详细的答案和解析
3. 难度符合${difficulty}等级
4. 请以JSON格式输出，格式如下：
[
  {
    "question": "题目内容",
    "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"], // 选择题才有
    "answer": "正确答案",
    "explanation": "详细解析"
  }
]

请直接返回JSON数组，不要其他文字。`
      }
    ]

    const response = await this.chat(messages)

    // 提取JSON
    const jsonMatch = response.match(/\[[\s\S]*\]/)
    if (jsonMatch) {
      return JSON.parse(jsonMatch[0])
    }

    throw new Error('AI返回格式错误')
  }

  /**
   * 学习建议
   */
  async getStudyAdvice(
    subject: string,
    weakPoints: string[],
    studyTime: number,
    accuracy: number
  ): Promise<string> {
    const messages: ChatMessage[] = [
      {
        role: 'system',
        content: `你是一位专业的学习顾问，擅长分析学生学习情况并给出针对性建议。`
      },
      {
        role: 'user',
        content: `请根据学生的${subject}学习情况给出个性化建议：

薄弱知识点：${weakPoints.join('、')}
累计学习时长：${studyTime}分钟
练习正确率：${accuracy}%

请给出：
1. 当前学习状态评价
2. 薄弱点的针对性学习建议（按优先级排序）
3. 学习方法建议
4. 后续学习计划建议

请用鼓励的语气，给出实用的建议。`
      }
    ]

    return await this.chat(messages)
  }

  /**
   * 答疑解惑
   */
  async answerQuestion(
    question: string,
    context: string,
    subject: string
  ): Promise<string> {
    const messages: ChatMessage[] = [
      {
        role: 'system',
        content: `你是一位专业的${subject}老师，擅长解答学生疑问。请用清晰、准确的语言回答问题。`
      },
      {
        role: 'user',
        content: `上下文：${context}

学生提问：${question}

请详细解答这个问题，要求：
1. 直接回答问题
2. 解释背后的原理
3. 如果有必要，给出例子说明
4. 指出相关的知识点

请用通俗易懂的语言。`
      }
    ]

    return await this.chat(messages)
  }
}

// 创建默认实例（需要配置API Key）
export function createDeepSeekClient(apiKey?: string): DeepSeekClient {
  const key = apiKey || import.meta.env.VITE_DEEPSEEK_API_KEY || ''

  if (!key) {
    console.warn('DeepSeek API Key未配置，AI功能将无法使用')
  }

  return new DeepSeekClient({
    apiKey: key,
    temperature: 0.7,
    maxTokens: 2000
  })
}
