/**
 * AI对话历史智能分析器
 * 分析用户的对话历史，提供学习洞察和个性化建议
 */

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  sessionId: string
  chatType?: 'casual' | 'study' | 'qa' | 'motivation' | 'planning'
  subject?: string
  keywords?: string[]
}

export interface ConversationInsight {
  totalConversations: number
  totalMessages: number
  avgMessagesPerSession: number
  mostActiveHours: number[]
  mostDiscussedTopics: Array<{ topic: string; count: number }>
  chatTypeDistribution: Record<string, number>
  subjectDistribution: Record<string, number>
  learningPatterns: LearningPattern[]
  recommendations: string[]
}

export interface LearningPattern {
  pattern: string
  frequency: number
  confidence: number
  description: string
  suggestion: string
}

export interface TopicTrend {
  topic: string
  mentions: Array<{ date: string; count: number }>
  trend: 'rising' | 'stable' | 'declining'
  relatedTopics: string[]
}

export interface SessionAnalysis {
  sessionId: string
  startTime: number
  endTime: number
  duration: number
  messageCount: number
  topics: string[]
  sentiment: 'positive' | 'neutral' | 'negative'
  quality: number // 对话质量评分 0-100
  keyInsights: string[]
}

export class ChatHistoryAnalyzer {
  private messages: ChatMessage[] = []
  private sessions: Map<string, ChatMessage[]> = new Map()

  // NLP关键词库
  private readonly TOPIC_KEYWORDS: Record<string, string[]> = {
    '数学': ['数学', '代数', '几何', '函数', '方程', '不等式', '数列', '概率', '统计'],
    '物理': ['物理', '力学', '电学', '光学', '热学', '声学', '磁场', '电路'],
    '化学': ['化学', '有机', '无机', '元素', '反应', '方程式', '化合物', '离子'],
    '英语': ['英语', 'English', '语法', '词汇', '阅读', '写作', '听力', '口语'],
    '语文': ['语文', '作文', '阅读理解', '古诗', '文言文', '现代文', '写作'],
    '生物': ['生物', '细胞', '遗传', '进化', '生态', 'DNA', 'RNA', '蛋白质'],
    '解题方法': ['怎么做', '如何解', '解题', '思路', '方法', '步骤', '技巧'],
    '概念理解': ['是什么', '什么是', '定义', '概念', '原理', '含义', '解释'],
    '错题分析': ['错了', '为什么错', '哪里错', '错题', '错误', '失误'],
    '学习方法': ['怎么学', '如何学', '学习方法', '学习技巧', '复习', '记忆'],
    '考试': ['考试', '测验', '模拟', '真题', '考点', '重点'],
    '作业': ['作业', '练习', '习题', '题目', '练习题']
  }

  /**
   * 添加消息
   */
  addMessage(message: ChatMessage) {
    this.messages.push(message)

    // 按会话组织
    if (!this.sessions.has(message.sessionId)) {
      this.sessions.set(message.sessionId, [])
    }
    this.sessions.get(message.sessionId)!.push(message)

    // 自动提取关键词
    if (!message.keywords) {
      message.keywords = this.extractKeywords(message.content)
    }
  }

  /**
   * 批量添加消息
   */
  addMessages(messages: ChatMessage[]) {
    messages.forEach(m => this.addMessage(m))
  }

  /**
   * 提取关键词
   */
  private extractKeywords(text: string): string[] {
    const keywords = new Set<string>()

    // 基于预定义词库匹配
    for (const [topic, wordList] of Object.entries(this.TOPIC_KEYWORDS)) {
      for (const word of wordList) {
        if (text.includes(word)) {
          keywords.add(topic)
          break
        }
      }
    }

    return Array.from(keywords)
  }

  /**
   * 分析对话历史，生成洞察
   */
  analyze(): ConversationInsight {
    if (this.messages.length === 0) {
      return this.getEmptyInsight()
    }

    const totalConversations = this.sessions.size
    const totalMessages = this.messages.length
    const avgMessagesPerSession = totalMessages / totalConversations

    // 分析最活跃时段
    const mostActiveHours = this.analyzeMostActiveHours()

    // 分析最常讨论的主题
    const mostDiscussedTopics = this.analyzeMostDiscussedTopics()

    // 分析对话类型分布
    const chatTypeDistribution = this.analyzeChatTypeDistribution()

    // 分析学科分布
    const subjectDistribution = this.analyzeSubjectDistribution()

    // 发现学习模式
    const learningPatterns = this.discoverLearningPatterns()

    // 生成个性化推荐
    const recommendations = this.generateRecommendations(
      mostDiscussedTopics,
      chatTypeDistribution,
      learningPatterns
    )

    return {
      totalConversations,
      totalMessages,
      avgMessagesPerSession,
      mostActiveHours,
      mostDiscussedTopics,
      chatTypeDistribution,
      subjectDistribution,
      learningPatterns,
      recommendations
    }
  }

  /**
   * 分析最活跃时段
   */
  private analyzeMostActiveHours(): number[] {
    const hourCounts = new Array(24).fill(0)

    for (const msg of this.messages) {
      if (msg.role === 'user') {
        const hour = new Date(msg.timestamp).getHours()
        hourCounts[hour]++
      }
    }

    // 找出消息数最多的3个时段
    const hourWithCounts = hourCounts.map((count, hour) => ({ hour, count }))
    return hourWithCounts
      .sort((a, b) => b.count - a.count)
      .slice(0, 3)
      .map(h => h.hour)
  }

  /**
   * 分析最常讨论的主题
   */
  private analyzeMostDiscussedTopics(): Array<{ topic: string; count: number }> {
    const topicCounts = new Map<string, number>()

    for (const msg of this.messages) {
      if (msg.keywords) {
        msg.keywords.forEach(keyword => {
          topicCounts.set(keyword, (topicCounts.get(keyword) || 0) + 1)
        })
      }
    }

    return Array.from(topicCounts.entries())
      .map(([topic, count]) => ({ topic, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 10)
  }

  /**
   * 分析对话类型分布
   */
  private analyzeChatTypeDistribution(): Record<string, number> {
    const distribution: Record<string, number> = {}

    for (const msg of this.messages) {
      if (msg.role === 'user' && msg.chatType) {
        distribution[msg.chatType] = (distribution[msg.chatType] || 0) + 1
      }
    }

    return distribution
  }

  /**
   * 分析学科分布
   */
  private analyzeSubjectDistribution(): Record<string, number> {
    const distribution: Record<string, number> = {}

    for (const msg of this.messages) {
      if (msg.subject) {
        distribution[msg.subject] = (distribution[msg.subject] || 0) + 1
      }
    }

    return distribution
  }

  /**
   * 发现学习模式
   */
  private discoverLearningPatterns(): LearningPattern[] {
    const patterns: LearningPattern[] = []

    // 模式1：高频提问时段
    const hourCounts = new Array(24).fill(0)
    for (const msg of this.messages) {
      if (msg.role === 'user') {
        const hour = new Date(msg.timestamp).getHours()
        hourCounts[hour]++
      }
    }
    const maxHour = hourCounts.indexOf(Math.max(...hourCounts))
    patterns.push({
      pattern: '最佳学习时段',
      frequency: hourCounts[maxHour],
      confidence: 0.8,
      description: `你通常在 ${maxHour}:00-${maxHour + 1}:00 最活跃`,
      suggestion: `建议在此时段安排重要的学习任务`
    })

    // 模式2：学习周期
    const sessionDurations = this.calculateSessionDurations()
    const avgDuration = sessionDurations.reduce((a, b) => a + b, 0) / sessionDurations.length
    patterns.push({
      pattern: '平均学习时长',
      frequency: sessionDurations.length,
      confidence: 0.7,
      description: `平均每次学习 ${Math.round(avgDuration / 60)} 分钟`,
      suggestion: avgDuration > 3600
        ? '学习时间较长，注意劳逸结合'
        : '可以适当延长学习时长'
    })

    // 模式3：提问偏好
    const questionTypes = this.analyzeQuestionTypes()
    const dominantType = this.getDominantType(questionTypes)
    if (dominantType) {
      patterns.push({
        pattern: '提问偏好',
        frequency: questionTypes[dominantType],
        confidence: 0.75,
        description: `你更倾向于提问 ${dominantType} 类问题`,
        suggestion: this.getSuggestionForQuestionType(dominantType)
      })
    }

    // 模式4：学科偏好
    const subjectDist = this.analyzeSubjectDistribution()
    const favSubject = this.getDominantType(subjectDist)
    if (favSubject) {
      patterns.push({
        pattern: '学科偏好',
        frequency: subjectDist[favSubject],
        confidence: 0.85,
        description: `你对 ${favSubject} 最感兴趣`,
        suggestion: `可以深入学习 ${favSubject}，建立优势学科`
      })
    }

    return patterns
  }

  /**
   * 计算会话时长
   */
  private calculateSessionDurations(): number[] {
    const durations: number[] = []

    for (const [, messages] of this.sessions) {
      if (messages.length < 2) continue

      const startTime = messages[0].timestamp
      const endTime = messages[messages.length - 1].timestamp
      durations.push((endTime - startTime) / 1000) // 秒
    }

    return durations
  }

  /**
   * 分析提问类型
   */
  private analyzeQuestionTypes(): Record<string, number> {
    const types: Record<string, number> = {}

    for (const msg of this.messages) {
      if (msg.role === 'user' && msg.keywords) {
        msg.keywords.forEach(keyword => {
          types[keyword] = (types[keyword] || 0) + 1
        })
      }
    }

    return types
  }

  /**
   * 获取占主导地位的类型
   */
  private getDominantType(distribution: Record<string, number>): string | null {
    const entries = Object.entries(distribution)
    if (entries.length === 0) return null

    return entries.reduce((max, curr) =>
      curr[1] > max[1] ? curr : max
    )[0]
  }

  /**
   * 根据提问类型给建议
   */
  private getSuggestionForQuestionType(type: string): string {
    const suggestions: Record<string, string> = {
      '解题方法': '建议多思考解题思路，培养独立思考能力',
      '概念理解': '建议系统学习基础概念，打好基础',
      '错题分析': '错题是宝贵资源，建议建立错题本',
      '学习方法': '好的学习方法很重要，建议多实践验证',
      '考试': '建议结合真题练习，熟悉考试节奏'
    }

    return suggestions[type] || '保持好奇心，持续学习'
  }

  /**
   * 生成个性化推荐
   */
  private generateRecommendations(
    topics: Array<{ topic: string; count: number }>,
    chatTypes: Record<string, number>,
    patterns: LearningPattern[]
  ): string[] {
    const recommendations: string[] = []

    // 基于讨论主题的推荐
    if (topics.length > 0) {
      const topTopic = topics[0].topic
      recommendations.push(`你对"${topTopic}"很感兴趣，建议深入学习相关内容`)
    }

    // 基于对话类型的推荐
    const qaCount = chatTypes['qa'] || 0
    const studyCount = chatTypes['study'] || 0

    if (qaCount > studyCount * 2) {
      recommendations.push('你的提问较多，建议先系统学习基础知识，再针对性提问')
    } else if (studyCount > qaCount * 2) {
      recommendations.push('学习很扎实！可以尝试通过提问来检验学习效果')
    }

    // 基于学习模式的推荐
    for (const pattern of patterns) {
      if (pattern.suggestion) {
        recommendations.push(pattern.suggestion)
      }
    }

    // 基于活跃度的推荐
    const avgMsgsPerDay = this.messages.length / this.getDaysSinceFirstMessage()
    if (avgMsgsPerDay < 5) {
      recommendations.push('建议增加与AI助手的互动频率，养成每日学习习惯')
    } else if (avgMsgsPerDay > 30) {
      recommendations.push('学习很积极！注意劳逸结合，保持可持续的学习节奏')
    }

    return recommendations.slice(0, 5) // 最多5条推荐
  }

  /**
   * 获取自首次消息以来的天数
   */
  private getDaysSinceFirstMessage(): number {
    if (this.messages.length === 0) return 1

    const firstMsgTime = Math.min(...this.messages.map(m => m.timestamp))
    const daysDiff = (Date.now() - firstMsgTime) / (1000 * 60 * 60 * 24)

    return Math.max(1, daysDiff)
  }

  /**
   * 分析单个会话
   */
  analyzeSession(sessionId: string): SessionAnalysis | null {
    const messages = this.sessions.get(sessionId)
    if (!messages || messages.length === 0) return null

    const startTime = messages[0].timestamp
    const endTime = messages[messages.length - 1].timestamp
    const duration = (endTime - startTime) / 1000 // 秒

    // 提取主题
    const topicsSet = new Set<string>()
    messages.forEach(msg => {
      if (msg.keywords) {
        msg.keywords.forEach(kw => topicsSet.add(kw))
      }
    })

    // 情感分析（简单实现）
    const sentiment = this.analyzeSentiment(messages)

    // 对话质量评分
    const quality = this.calculateQuality(messages, duration)

    // 关键洞察
    const keyInsights = this.extractKeyInsights(messages)

    return {
      sessionId,
      startTime,
      endTime,
      duration,
      messageCount: messages.length,
      topics: Array.from(topicsSet),
      sentiment,
      quality,
      keyInsights
    }
  }

  /**
   * 简单情感分析
   */
  private analyzeSentiment(messages: ChatMessage[]): 'positive' | 'neutral' | 'negative' {
    const positiveWords = ['明白', '懂了', '谢谢', '好的', '理解', '清楚']
    const negativeWords = ['不懂', '不明白', '困惑', '难', '还是不会']

    let positiveCount = 0
    let negativeCount = 0

    for (const msg of messages) {
      if (msg.role === 'user') {
        const content = msg.content.toLowerCase()
        positiveWords.forEach(word => {
          if (content.includes(word)) positiveCount++
        })
        negativeWords.forEach(word => {
          if (content.includes(word)) negativeCount++
        })
      }
    }

    if (positiveCount > negativeCount) return 'positive'
    if (negativeCount > positiveCount) return 'negative'
    return 'neutral'
  }

  /**
   * 计算对话质量
   */
  private calculateQuality(messages: ChatMessage[], duration: number): number {
    let score = 50 // 基础分

    // 因素1：消息数量（适中为佳）
    const msgCount = messages.length
    if (msgCount >= 5 && msgCount <= 20) {
      score += 20
    } else if (msgCount > 20) {
      score += 10
    }

    // 因素2：时长（不宜过短或过长）
    const minutes = duration / 60
    if (minutes >= 5 && minutes <= 30) {
      score += 20
    } else if (minutes > 30) {
      score += 10
    }

    // 因素3：用户消息长度（详细提问更好）
    const userMsgs = messages.filter(m => m.role === 'user')
    const avgUserMsgLength = userMsgs.reduce((sum, m) => sum + m.content.length, 0) / userMsgs.length
    if (avgUserMsgLength > 20) {
      score += 10
    }

    return Math.min(100, score)
  }

  /**
   * 提取关键洞察
   */
  private extractKeyInsights(messages: ChatMessage[]): string[] {
    const insights: string[] = []

    // 洞察1：讨论的主要主题
    const topics = new Set<string>()
    messages.forEach(msg => {
      if (msg.keywords) {
        msg.keywords.forEach(kw => topics.add(kw))
      }
    })
    if (topics.size > 0) {
      insights.push(`本次主要讨论了：${Array.from(topics).join('、')}`)
    }

    // 洞察2：用户理解程度
    const lastUserMsg = [...messages].reverse().find(m => m.role === 'user')
    if (lastUserMsg) {
      if (lastUserMsg.content.includes('明白') || lastUserMsg.content.includes('懂了')) {
        insights.push('用户已理解内容')
      } else if (lastUserMsg.content.includes('不懂') || lastUserMsg.content.includes('还是')) {
        insights.push('用户可能需要更多帮助')
      }
    }

    // 洞察3：学习深度
    const questionDepth = messages.filter(m =>
      m.role === 'user' && (m.content.includes('为什么') || m.content.includes('如何'))
    ).length
    if (questionDepth >= 3) {
      insights.push('深度学习：提出了多个深层次问题')
    }

    return insights
  }

  /**
   * 分析主题趋势
   */
  analyzeTopicTrends(days: number = 30): TopicTrend[] {
    const topicMentions = new Map<string, Map<string, number>>()
    const cutoffTime = Date.now() - days * 24 * 60 * 60 * 1000

    // 统计每天各主题的提及次数
    for (const msg of this.messages) {
      if (msg.timestamp < cutoffTime) continue
      if (!msg.keywords) continue

      const date = new Date(msg.timestamp).toISOString().split('T')[0]

      for (const keyword of msg.keywords) {
        if (!topicMentions.has(keyword)) {
          topicMentions.set(keyword, new Map())
        }
        const dateCounts = topicMentions.get(keyword)!
        dateCounts.set(date, (dateCounts.get(date) || 0) + 1)
      }
    }

    // 生成趋势
    const trends: TopicTrend[] = []
    for (const [topic, dateCounts] of topicMentions) {
      const mentions = Array.from(dateCounts.entries())
        .map(([date, count]) => ({ date, count }))
        .sort((a, b) => a.date.localeCompare(b.date))

      // 计算趋势
      const trend = this.calculateTrend(mentions.map(m => m.count))

      trends.push({
        topic,
        mentions,
        trend,
        relatedTopics: this.findRelatedTopics(topic)
      })
    }

    return trends.sort((a, b) =>
      b.mentions.reduce((sum, m) => sum + m.count, 0) -
      a.mentions.reduce((sum, m) => sum + m.count, 0)
    )
  }

  /**
   * 计算趋势方向
   */
  private calculateTrend(counts: number[]): 'rising' | 'stable' | 'declining' {
    if (counts.length < 3) return 'stable'

    const recent = counts.slice(-3)
    const earlier = counts.slice(0, Math.max(1, counts.length - 3))

    const recentAvg = recent.reduce((a, b) => a + b, 0) / recent.length
    const earlierAvg = earlier.reduce((a, b) => a + b, 0) / earlier.length

    if (recentAvg > earlierAvg * 1.2) return 'rising'
    if (recentAvg < earlierAvg * 0.8) return 'declining'
    return 'stable'
  }

  /**
   * 查找相关主题
   */
  private findRelatedTopics(topic: string): string[] {
    const relatedMap: Record<string, string[]> = {
      '数学': ['代数', '几何', '函数'],
      '物理': ['力学', '电学', '光学'],
      '化学': ['有机', '无机', '反应'],
      '解题方法': ['概念理解', '错题分析'],
      '学习方法': ['复习', '记忆', '考试']
    }

    return relatedMap[topic] || []
  }

  /**
   * 获取空洞察（无数据时）
   */
  private getEmptyInsight(): ConversationInsight {
    return {
      totalConversations: 0,
      totalMessages: 0,
      avgMessagesPerSession: 0,
      mostActiveHours: [],
      mostDiscussedTopics: [],
      chatTypeDistribution: {},
      subjectDistribution: {},
      learningPatterns: [],
      recommendations: ['开始使用AI助手，记录你的学习旅程吧！']
    }
  }

  /**
   * 清空数据
   */
  clear() {
    this.messages = []
    this.sessions.clear()
  }
}

/**
 * 创建对话分析器
 */
export function createChatAnalyzer(): ChatHistoryAnalyzer {
  return new ChatHistoryAnalyzer()
}

/**
 * 全局分析器单例
 */
let globalAnalyzer: ChatHistoryAnalyzer | null = null

export function getGlobalChatAnalyzer(): ChatHistoryAnalyzer {
  if (!globalAnalyzer) {
    globalAnalyzer = new ChatHistoryAnalyzer()
  }
  return globalAnalyzer
}
