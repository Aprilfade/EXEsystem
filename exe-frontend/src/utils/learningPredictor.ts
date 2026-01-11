/**
 * 学习效果预测模型
 * 基于 BKT (Bayesian Knowledge Tracing) 和 DKT (Deep Knowledge Tracing) 思想
 * 预测学生对知识点的掌握程度和未来表现
 */

export interface LearningRecord {
  knowledgePointId: string
  knowledgePointName: string
  timestamp: number
  isCorrect: boolean
  responseTime: number // 答题时间（秒）
  difficulty: number // 1-5
  attemptCount: number // 第几次尝试
  hintUsed: boolean // 是否使用提示
}

export interface KnowledgeState {
  knowledgePointId: string
  knowledgePointName: string
  masteryLevel: number // 掌握度 0-1
  confidence: number // 置信度 0-1
  forgettingRate: number // 遗忘率 0-1
  lastPracticeTime: number
  totalAttempts: number
  correctAttempts: number
  avgResponseTime: number
  trend: 'improving' | 'stable' | 'declining'
}

export interface PredictionResult {
  knowledgePointId: string
  knowledgePointName: string
  currentMastery: number // 当前掌握度
  predictedMastery: number // 预测掌握度（7天后）
  successProbability: number // 下次答对概率
  recommendedAction: 'review' | 'practice' | 'advance' | 'master'
  timeToMastery: number // 预计掌握所需时间（小时）
  riskLevel: 'low' | 'medium' | 'high' // 遗忘风险
  suggestions: string[]
}

export interface LearningPath {
  knowledgePointId: string
  priority: number // 学习优先级 1-10
  estimatedTime: number // 预计学习时间（分钟）
  prerequisites: string[] // 前置知识点
  difficulty: number
  reason: string
}

export class LearningEffectPredictor {
  private knowledgeStates: Map<string, KnowledgeState> = new Map()
  private learningHistory: Map<string, LearningRecord[]> = new Map()

  // BKT 参数
  private readonly P_INIT = 0.1 // 初始掌握概率
  private readonly P_LEARN = 0.3 // 学习概率（从未掌握到掌握）
  private readonly P_FORGET = 0.1 // 遗忘概率
  private readonly P_SLIP = 0.1 // 失误概率（已掌握但答错）
  private readonly P_GUESS = 0.2 // 猜对概率（未掌握但答对）

  /**
   * 添加学习记录
   */
  addLearningRecord(record: LearningRecord) {
    const kpId = record.knowledgePointId

    // 更新历史记录
    if (!this.learningHistory.has(kpId)) {
      this.learningHistory.set(kpId, [])
    }
    this.learningHistory.get(kpId)!.push(record)

    // 更新知识状态
    this.updateKnowledgeState(record)
  }

  /**
   * 批量添加学习记录
   */
  addLearningRecords(records: LearningRecord[]) {
    records.forEach(r => this.addLearningRecord(r))
  }

  /**
   * 更新知识状态（BKT算法）
   */
  private updateKnowledgeState(record: LearningRecord) {
    const kpId = record.knowledgePointId
    let state = this.knowledgeStates.get(kpId)

    // 初始化状态
    if (!state) {
      state = {
        knowledgePointId: kpId,
        knowledgePointName: record.knowledgePointName,
        masteryLevel: this.P_INIT,
        confidence: 0.5,
        forgettingRate: this.P_FORGET,
        lastPracticeTime: record.timestamp,
        totalAttempts: 0,
        correctAttempts: 0,
        avgResponseTime: 0,
        trend: 'stable'
      }
    }

    // 计算时间衰减（遗忘曲线）
    const daysSinceLastPractice =
      (record.timestamp - state.lastPracticeTime) / (1000 * 60 * 60 * 24)
    const forgettingFactor = Math.exp(-state.forgettingRate * daysSinceLastPractice)
    const decayedMastery = state.masteryLevel * forgettingFactor

    // BKT 更新
    let newMastery = decayedMastery

    if (record.isCorrect) {
      // 答对：增加掌握度
      // P(L_new | correct) = P(L_old) + (1 - P(L_old)) * P(learn)
      const learningGain =
        (1 - decayedMastery) * this.P_LEARN * this.getDifficultyFactor(record.difficulty)

      newMastery = decayedMastery + learningGain

      // 快速回答正确，额外奖励
      if (record.responseTime < 30) {
        newMastery = Math.min(1, newMastery * 1.1)
      }
    } else {
      // 答错：降低掌握度
      // P(L_new | wrong) = P(L_old) * (1 - P(slip)) / (1 - P(L_old) * (1 - P(slip)) - (1 - P(L_old)) * P(guess))
      const slipPenalty = this.P_SLIP * (1 + record.difficulty / 5)
      newMastery = Math.max(0, decayedMastery * (1 - slipPenalty))

      // 使用提示，轻微惩罚
      if (record.hintUsed) {
        newMastery *= 0.9
      }
    }

    // 更新统计信息
    state.totalAttempts++
    if (record.isCorrect) {
      state.correctAttempts++
    }

    // 更新平均响应时间
    state.avgResponseTime =
      (state.avgResponseTime * (state.totalAttempts - 1) + record.responseTime) /
      state.totalAttempts

    // 更新置信度（基于练习次数）
    state.confidence = Math.min(
      1,
      0.5 + state.totalAttempts * 0.05 // 每次练习增加 5%，最高 100%
    )

    // 计算趋势
    state.trend = this.calculateTrend(kpId, newMastery, state.masteryLevel)

    // 更新掌握度和时间
    state.masteryLevel = Math.min(1, Math.max(0, newMastery))
    state.lastPracticeTime = record.timestamp

    // 动态调整遗忘率（根据练习频率）
    state.forgettingRate = this.calculateForgettingRate(kpId)

    this.knowledgeStates.set(kpId, state)
  }

  /**
   * 难度系数（难度越高，学习增益越大）
   */
  private getDifficultyFactor(difficulty: number): number {
    return 0.5 + difficulty * 0.1 // 范围 0.6-1.0
  }

  /**
   * 计算学习趋势
   */
  private calculateTrend(
    kpId: string,
    newMastery: number,
    oldMastery: number
  ): 'improving' | 'stable' | 'declining' {
    const history = this.learningHistory.get(kpId)
    if (!history || history.length < 3) return 'stable'

    // 取最近 5 次记录
    const recentHistory = history.slice(-5)
    const recentCorrectRate =
      recentHistory.filter(r => r.isCorrect).length / recentHistory.length

    if (newMastery > oldMastery && recentCorrectRate > 0.6) {
      return 'improving'
    } else if (newMastery < oldMastery || recentCorrectRate < 0.4) {
      return 'declining'
    }
    return 'stable'
  }

  /**
   * 计算遗忘率（基于练习频率）
   */
  private calculateForgettingRate(kpId: string): number {
    const history = this.learningHistory.get(kpId)
    if (!history || history.length < 2) return this.P_FORGET

    // 计算平均练习间隔
    let totalInterval = 0
    for (let i = 1; i < history.length; i++) {
      const interval = history[i].timestamp - history[i - 1].timestamp
      totalInterval += interval
    }
    const avgInterval = totalInterval / (history.length - 1)
    const avgDays = avgInterval / (1000 * 60 * 60 * 24)

    // 练习越频繁，遗忘率越低
    return this.P_FORGET * Math.min(2, Math.max(0.5, avgDays / 7))
  }

  /**
   * 预测学习效果
   */
  predict(knowledgePointId: string): PredictionResult | null {
    const state = this.knowledgeStates.get(knowledgePointId)
    if (!state) return null

    const currentMastery = state.masteryLevel

    // 预测 7 天后的掌握度（考虑遗忘曲线）
    const daysSinceLastPractice =
      (Date.now() - state.lastPracticeTime) / (1000 * 60 * 60 * 24)
    const daysAhead = 7
    const totalDays = daysSinceLastPractice + daysAhead
    const forgettingFactor = Math.exp(-state.forgettingRate * totalDays)
    const predictedMastery = currentMastery * forgettingFactor

    // 下次答对概率
    // P(correct) = P(L) * (1 - P(slip)) + (1 - P(L)) * P(guess)
    const successProbability =
      currentMastery * (1 - this.P_SLIP) + (1 - currentMastery) * this.P_GUESS

    // 推荐行动
    let recommendedAction: 'review' | 'practice' | 'advance' | 'master'
    if (currentMastery >= 0.9) {
      recommendedAction = 'master'
    } else if (currentMastery >= 0.7) {
      recommendedAction = 'advance'
    } else if (currentMastery >= 0.4) {
      recommendedAction = 'practice'
    } else {
      recommendedAction = 'review'
    }

    // 预计掌握所需时间（小时）
    const timeToMastery = this.estimateTimeToMastery(state)

    // 遗忘风险
    let riskLevel: 'low' | 'medium' | 'high'
    if (predictedMastery >= 0.7) {
      riskLevel = 'low'
    } else if (predictedMastery >= 0.4) {
      riskLevel = 'medium'
    } else {
      riskLevel = 'high'
    }

    // 生成建议
    const suggestions = this.generateSuggestions(state, predictedMastery, riskLevel)

    return {
      knowledgePointId,
      knowledgePointName: state.knowledgePointName,
      currentMastery,
      predictedMastery,
      successProbability,
      recommendedAction,
      timeToMastery,
      riskLevel,
      suggestions
    }
  }

  /**
   * 预估掌握所需时间
   */
  private estimateTimeToMastery(state: KnowledgeState): number {
    if (state.masteryLevel >= 0.9) return 0

    // 基于当前掌握度和历史学习速度
    const remainingMastery = 0.9 - state.masteryLevel
    const avgPracticeTime = state.avgResponseTime / 60 // 转换为分钟

    // 假设每次练习提升 5% 掌握度
    const practicesNeeded = Math.ceil(remainingMastery / 0.05)
    const totalMinutes = practicesNeeded * avgPracticeTime * 2 // 包括讲解时间

    return totalMinutes / 60 // 转换为小时
  }

  /**
   * 生成个性化建议
   */
  private generateSuggestions(
    state: KnowledgeState,
    predictedMastery: number,
    riskLevel: string
  ): string[] {
    const suggestions: string[] = []

    // 基于掌握度的建议
    if (state.masteryLevel < 0.4) {
      suggestions.push('建议从基础概念开始系统学习')
      suggestions.push('可以观看相关视频讲解')
    } else if (state.masteryLevel < 0.7) {
      suggestions.push('继续练习巩固，重点关注易错点')
      suggestions.push('尝试中等难度的习题')
    } else if (state.masteryLevel < 0.9) {
      suggestions.push('可以挑战更高难度的题目')
      suggestions.push('尝试总结归纳知识点')
    } else {
      suggestions.push('已充分掌握，可以教别人来巩固')
      suggestions.push('定期复习即可保持熟练度')
    }

    // 基于遗忘风险的建议
    if (riskLevel === 'high') {
      suggestions.push('⚠️ 遗忘风险较高，建议尽快复习')
    } else if (riskLevel === 'medium') {
      suggestions.push('建议在3天内进行一次复习')
    }

    // 基于趋势的建议
    if (state.trend === 'declining') {
      suggestions.push('最近表现下滑，建议调整学习方法')
    } else if (state.trend === 'improving') {
      suggestions.push('学习状态很好，保持当前节奏')
    }

    // 基于练习频率的建议
    const daysSinceLastPractice =
      (Date.now() - state.lastPracticeTime) / (1000 * 60 * 60 * 24)
    if (daysSinceLastPractice > 7) {
      suggestions.push('已超过一周未练习，建议重新复习')
    }

    return suggestions
  }

  /**
   * 获取所有知识点状态
   */
  getAllKnowledgeStates(): KnowledgeState[] {
    return Array.from(this.knowledgeStates.values())
  }

  /**
   * 生成智能学习路径
   */
  generateLearningPath(): LearningPath[] {
    const states = this.getAllKnowledgeStates()
    const paths: LearningPath[] = []

    for (const state of states) {
      const prediction = this.predict(state.knowledgePointId)
      if (!prediction) continue

      // 计算优先级（综合考虑多个因素）
      let priority = 0

      // 因素1：掌握度低的优先（权重 40%）
      priority += (1 - state.masteryLevel) * 4

      // 因素2：遗忘风险高的优先（权重 30%）
      if (prediction.riskLevel === 'high') {
        priority += 3
      } else if (prediction.riskLevel === 'medium') {
        priority += 1.5
      }

      // 因素3：最近练习少的优先（权重 20%）
      const daysSinceLastPractice =
        (Date.now() - state.lastPracticeTime) / (1000 * 60 * 60 * 24)
      priority += Math.min(2, daysSinceLastPractice / 7)

      // 因素4：趋势下降的优先（权重 10%）
      if (state.trend === 'declining') {
        priority += 1
      }

      // 归一化到 1-10
      priority = Math.min(10, Math.max(1, priority))

      // 预计学习时间
      const estimatedTime = prediction.timeToMastery * 60 // 转换为分钟

      // 生成推荐理由
      const reason = this.generatePathReason(state, prediction)

      paths.push({
        knowledgePointId: state.knowledgePointId,
        priority,
        estimatedTime,
        prerequisites: [], // 需要外部知识图谱补充
        difficulty: this.estimateDifficulty(state),
        reason
      })
    }

    // 按优先级排序
    return paths.sort((a, b) => b.priority - a.priority)
  }

  /**
   * 估算知识点难度
   */
  private estimateDifficulty(state: KnowledgeState): number {
    const history = this.learningHistory.get(state.knowledgePointId)
    if (!history || history.length === 0) return 3 // 默认中等难度

    // 基于历史难度和正确率
    const avgDifficulty =
      history.reduce((sum, r) => sum + r.difficulty, 0) / history.length
    const correctRate = state.correctAttempts / state.totalAttempts

    // 正确率低则提高感知难度
    return Math.min(5, avgDifficulty * (2 - correctRate))
  }

  /**
   * 生成学习路径推荐理由
   */
  private generatePathReason(state: KnowledgeState, prediction: PredictionResult): string {
    const reasons: string[] = []

    if (state.masteryLevel < 0.4) {
      reasons.push('基础薄弱')
    }

    if (prediction.riskLevel === 'high') {
      reasons.push('遗忘风险高')
    }

    const daysSinceLastPractice =
      (Date.now() - state.lastPracticeTime) / (1000 * 60 * 60 * 24)
    if (daysSinceLastPractice > 7) {
      reasons.push('长时间未练习')
    }

    if (state.trend === 'declining') {
      reasons.push('最近表现下滑')
    }

    if (reasons.length === 0) {
      return '建议继续巩固提升'
    }

    return reasons.join('，')
  }

  /**
   * 获取薄弱知识点（掌握度 < 0.6）
   */
  getWeakKnowledgePoints(): KnowledgeState[] {
    return Array.from(this.knowledgeStates.values())
      .filter(s => s.masteryLevel < 0.6)
      .sort((a, b) => a.masteryLevel - b.masteryLevel)
  }

  /**
   * 获取需要复习的知识点
   */
  getReviewNeeded(): KnowledgeState[] {
    const now = Date.now()
    return Array.from(this.knowledgeStates.values()).filter(s => {
      const daysSinceLastPractice = (now - s.lastPracticeTime) / (1000 * 60 * 60 * 24)
      return daysSinceLastPractice > 3 && s.masteryLevel < 0.9
    })
  }

  /**
   * 清空数据
   */
  clear() {
    this.knowledgeStates.clear()
    this.learningHistory.clear()
  }
}

/**
 * 创建学习效果预测器
 */
export function createLearningPredictor(): LearningEffectPredictor {
  return new LearningEffectPredictor()
}

/**
 * 全局预测器单例
 */
let globalPredictor: LearningEffectPredictor | null = null

export function getGlobalLearningPredictor(): LearningEffectPredictor {
  if (!globalPredictor) {
    globalPredictor = new LearningEffectPredictor()
  }
  return globalPredictor
}
