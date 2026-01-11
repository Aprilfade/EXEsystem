/**
 * 智能推荐引擎
 * 实现协同过滤、内容推荐和混合推荐策略
 * 适用于题目、课程、知识点的智能推荐
 */

export interface UserBehavior {
  userId: string
  itemId: string
  itemType: 'question' | 'course' | 'knowledgePoint'
  behaviorType: 'view' | 'practice' | 'collect' | 'correct' | 'wrong'
  timestamp: number
  duration?: number // 学习时长（秒）
  score?: number // 得分（0-100）
}

export interface Item {
  id: string
  type: 'question' | 'course' | 'knowledgePoint'
  title: string
  difficulty?: number // 难度 1-5
  subject?: string
  tags?: string[]
  knowledgePoints?: string[]
  avgScore?: number
  practiceCount?: number
  features?: Record<string, any>
}

export interface RecommendationResult {
  item: Item
  score: number
  reason: string
  confidence: number
  diversity: number
  novelty: number
  explanation: string[]
}

export class SmartRecommendationEngine {
  private userBehaviors: Map<string, UserBehavior[]> = new Map()
  private itemFeatures: Map<string, Item> = new Map()
  private userItemMatrix: Map<string, Map<string, number>> = new Map()

  // 协同过滤参数
  private readonly CF_WEIGHT = 0.4
  private readonly CONTENT_WEIGHT = 0.4
  private readonly POPULARITY_WEIGHT = 0.2

  /**
   * 添加用户行为数据
   */
  addBehavior(behavior: UserBehavior) {
    const userId = behavior.userId
    if (!this.userBehaviors.has(userId)) {
      this.userBehaviors.set(userId, [])
    }
    this.userBehaviors.get(userId)!.push(behavior)

    // 更新用户-物品评分矩阵
    this.updateUserItemMatrix(behavior)
  }

  /**
   * 批量添加行为数据
   */
  addBehaviors(behaviors: UserBehavior[]) {
    behaviors.forEach(b => this.addBehavior(b))
  }

  /**
   * 添加物品特征
   */
  addItem(item: Item) {
    this.itemFeatures.set(item.id, item)
  }

  /**
   * 批量添加物品
   */
  addItems(items: Item[]) {
    items.forEach(item => this.addItem(item))
  }

  /**
   * 更新用户-物品评分矩阵
   */
  private updateUserItemMatrix(behavior: UserBehavior) {
    const { userId, itemId, behaviorType, score, duration } = behavior

    if (!this.userItemMatrix.has(userId)) {
      this.userItemMatrix.set(userId, new Map())
    }

    const userMatrix = this.userItemMatrix.get(userId)!

    // 计算隐式评分
    let rating = 0
    switch (behaviorType) {
      case 'view':
        rating = 1
        break
      case 'practice':
        rating = 3
        break
      case 'collect':
        rating = 4
        break
      case 'correct':
        rating = 5
        break
      case 'wrong':
        rating = 2
        break
    }

    // 考虑学习时长和得分
    if (duration) {
      rating += Math.min(duration / 300, 1) // 最多加1分
    }
    if (score !== undefined) {
      rating += (score / 100) * 2 // 最多加2分
    }

    // 时间衰减
    const daysSince = (Date.now() - behavior.timestamp) / (1000 * 60 * 60 * 24)
    const timeDecay = Math.exp(-daysSince / 30) // 30天衰减周期
    rating = rating * (0.5 + 0.5 * timeDecay)

    // 累积评分
    const currentRating = userMatrix.get(itemId) || 0
    userMatrix.set(itemId, currentRating + rating)
  }

  /**
   * 协同过滤推荐（基于用户的协同过滤）
   */
  private collaborativeFiltering(userId: string, candidateItems: Item[]): Map<string, number> {
    const scores = new Map<string, number>()
    const userMatrix = this.userItemMatrix.get(userId)

    if (!userMatrix || userMatrix.size === 0) {
      return scores
    }

    // 计算用户相似度
    const similarities = this.calculateUserSimilarities(userId)

    // 为每个候选物品计算协同过滤得分
    for (const item of candidateItems) {
      if (userMatrix.has(item.id)) {
        continue // 跳过已交互的物品
      }

      let weightedSum = 0
      let similaritySum = 0

      // 遍历相似用户
      for (const [otherUserId, similarity] of similarities) {
        const otherMatrix = this.userItemMatrix.get(otherUserId)
        if (otherMatrix && otherMatrix.has(item.id)) {
          weightedSum += similarity * otherMatrix.get(item.id)!
          similaritySum += similarity
        }
      }

      if (similaritySum > 0) {
        scores.set(item.id, weightedSum / similaritySum)
      }
    }

    return scores
  }

  /**
   * 计算用户相似度（余弦相似度）
   */
  private calculateUserSimilarities(userId: string): Map<string, number> {
    const similarities = new Map<string, number>()
    const userMatrix = this.userItemMatrix.get(userId)

    if (!userMatrix) return similarities

    // 与其他用户比较
    for (const [otherUserId, otherMatrix] of this.userItemMatrix) {
      if (otherUserId === userId) continue

      const similarity = this.cosineSimilarity(userMatrix, otherMatrix)
      if (similarity > 0.1) { // 只保留相似度 > 0.1 的用户
        similarities.set(otherUserId, similarity)
      }
    }

    // 排序并返回 Top K 相似用户
    return new Map(
      Array.from(similarities.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 20) // Top 20 相似用户
    )
  }

  /**
   * 余弦相似度计算
   */
  private cosineSimilarity(
    vec1: Map<string, number>,
    vec2: Map<string, number>
  ): number {
    let dotProduct = 0
    let norm1 = 0
    let norm2 = 0

    const allKeys = new Set([...vec1.keys(), ...vec2.keys()])

    for (const key of allKeys) {
      const v1 = vec1.get(key) || 0
      const v2 = vec2.get(key) || 0
      dotProduct += v1 * v2
      norm1 += v1 * v1
      norm2 += v2 * v2
    }

    if (norm1 === 0 || norm2 === 0) return 0

    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2))
  }

  /**
   * 基于内容的推荐
   */
  private contentBasedFiltering(userId: string, candidateItems: Item[]): Map<string, number> {
    const scores = new Map<string, number>()

    // 获取用户历史偏好
    const userProfile = this.buildUserProfile(userId)
    if (!userProfile) return scores

    // 为每个候选物品计算内容相似度
    for (const item of candidateItems) {
      const similarity = this.calculateContentSimilarity(userProfile, item)
      if (similarity > 0) {
        scores.set(item.id, similarity)
      }
    }

    return scores
  }

  /**
   * 构建用户画像
   */
  private buildUserProfile(userId: string): Item | null {
    const behaviors = this.userBehaviors.get(userId)
    if (!behaviors || behaviors.length === 0) return null

    // 统计用户偏好
    const subjectCount = new Map<string, number>()
    const tagCount = new Map<string, number>()
    const kpCount = new Map<string, number>()
    let totalDifficulty = 0
    let difficultyCount = 0

    for (const behavior of behaviors) {
      const item = this.itemFeatures.get(behavior.itemId)
      if (!item) continue

      // 统计科目
      if (item.subject) {
        subjectCount.set(item.subject, (subjectCount.get(item.subject) || 0) + 1)
      }

      // 统计标签
      if (item.tags) {
        item.tags.forEach(tag => {
          tagCount.set(tag, (tagCount.get(tag) || 0) + 1)
        })
      }

      // 统计知识点
      if (item.knowledgePoints) {
        item.knowledgePoints.forEach(kp => {
          kpCount.set(kp, (kpCount.get(kp) || 0) + 1)
        })
      }

      // 统计难度
      if (item.difficulty) {
        totalDifficulty += item.difficulty
        difficultyCount++
      }
    }

    // 构建用户画像
    const profile: Item = {
      id: 'user-profile-' + userId,
      type: 'knowledgePoint',
      title: 'User Profile',
      subject: this.getTopItem(subjectCount),
      tags: Array.from(tagCount.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 5)
        .map(([tag]) => tag),
      knowledgePoints: Array.from(kpCount.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10)
        .map(([kp]) => kp),
      difficulty: difficultyCount > 0 ? totalDifficulty / difficultyCount : undefined
    }

    return profile
  }

  /**
   * 计算内容相似度
   */
  private calculateContentSimilarity(profile: Item, item: Item): number {
    let similarity = 0
    let weightSum = 0

    // 科目匹配（权重 0.3）
    if (profile.subject && item.subject) {
      weightSum += 0.3
      if (profile.subject === item.subject) {
        similarity += 0.3
      }
    }

    // 标签匹配（权重 0.3）
    if (profile.tags && item.tags) {
      weightSum += 0.3
      const intersection = profile.tags.filter(t => item.tags!.includes(t))
      const union = new Set([...profile.tags, ...item.tags])
      similarity += 0.3 * (intersection.length / union.size)
    }

    // 知识点匹配（权重 0.3）
    if (profile.knowledgePoints && item.knowledgePoints) {
      weightSum += 0.3
      const intersection = profile.knowledgePoints.filter(kp =>
        item.knowledgePoints!.includes(kp)
      )
      const union = new Set([...profile.knowledgePoints, ...item.knowledgePoints])
      similarity += 0.3 * (intersection.length / union.size)
    }

    // 难度匹配（权重 0.1）
    if (profile.difficulty && item.difficulty) {
      weightSum += 0.1
      const diffDiff = Math.abs(profile.difficulty - item.difficulty)
      similarity += 0.1 * Math.max(0, 1 - diffDiff / 5)
    }

    return weightSum > 0 ? similarity / weightSum : 0
  }

  /**
   * 流行度推荐
   */
  private popularityBasedFiltering(candidateItems: Item[]): Map<string, number> {
    const scores = new Map<string, number>()

    // 计算每个物品的流行度得分
    for (const item of candidateItems) {
      const practiceCount = item.practiceCount || 0
      const avgScore = item.avgScore || 50

      // 流行度 = log(练习次数 + 1) * 平均分权重
      const popularity = Math.log10(practiceCount + 1) * (avgScore / 100)
      scores.set(item.id, popularity)
    }

    // 归一化
    const maxScore = Math.max(...scores.values(), 1)
    scores.forEach((score, id) => {
      scores.set(id, score / maxScore)
    })

    return scores
  }

  /**
   * 混合推荐策略
   */
  recommend(
    userId: string,
    candidateItems: Item[],
    topN: number = 10,
    diversityWeight: number = 0.3
  ): RecommendationResult[] {
    // 三种推荐策略得分
    const cfScores = this.collaborativeFiltering(userId, candidateItems)
    const contentScores = this.contentBasedFiltering(userId, candidateItems)
    const popularityScores = this.popularityBasedFiltering(candidateItems)

    // 计算混合得分
    const finalScores = new Map<string, number>()
    const explanations = new Map<string, string[]>()

    for (const item of candidateItems) {
      const cfScore = cfScores.get(item.id) || 0
      const contentScore = contentScores.get(item.id) || 0
      const popScore = popularityScores.get(item.id) || 0

      const finalScore =
        cfScore * this.CF_WEIGHT +
        contentScore * this.CONTENT_WEIGHT +
        popScore * this.POPULARITY_WEIGHT

      finalScores.set(item.id, finalScore)

      // 生成解释
      const exp: string[] = []
      if (cfScore > 0.5) {
        exp.push('与你兴趣相似的学生也在学习')
      }
      if (contentScore > 0.5) {
        exp.push('符合你的学习偏好和知识体系')
      }
      if (popScore > 0.5) {
        exp.push('热门内容，广受好评')
      }
      if (exp.length === 0) {
        exp.push('为你精心挑选的学习内容')
      }
      explanations.set(item.id, exp)
    }

    // 计算多样性得分（避免推荐过于相似的内容）
    const diversityScores = this.calculateDiversity(candidateItems, finalScores)

    // 计算新颖度得分（推荐用户未接触过的内容）
    const noveltyScores = this.calculateNovelty(userId, candidateItems)

    // 综合排序
    const results: RecommendationResult[] = candidateItems.map(item => {
      const baseScore = finalScores.get(item.id) || 0
      const diversity = diversityScores.get(item.id) || 0
      const novelty = noveltyScores.get(item.id) || 0

      const finalScoreWithDiversity =
        baseScore * (1 - diversityWeight) + diversity * diversityWeight

      return {
        item,
        score: finalScoreWithDiversity,
        reason: this.generateReason(item, baseScore),
        confidence: this.calculateConfidence(baseScore, diversity, novelty),
        diversity,
        novelty,
        explanation: explanations.get(item.id) || []
      }
    })

    // 排序并返回 Top N
    return results
      .sort((a, b) => b.score - a.score)
      .slice(0, topN)
  }

  /**
   * 计算推荐多样性
   */
  private calculateDiversity(
    items: Item[],
    scores: Map<string, number>
  ): Map<string, number> {
    const diversityScores = new Map<string, number>()

    for (const item of items) {
      // 基于标签和知识点的多样性
      let diversity = 1.0

      // 如果有标签，计算标签独特性
      if (item.tags && item.tags.length > 0) {
        const tagFrequency = new Map<string, number>()
        for (const otherItem of items) {
          if (otherItem.tags) {
            otherItem.tags.forEach(tag => {
              tagFrequency.set(tag, (tagFrequency.get(tag) || 0) + 1)
            })
          }
        }

        // 标签越独特，多样性越高
        const uniqueness = item.tags.reduce((sum, tag) => {
          const freq = tagFrequency.get(tag) || 1
          return sum + 1 / freq
        }, 0) / item.tags.length

        diversity *= uniqueness
      }

      diversityScores.set(item.id, diversity)
    }

    return diversityScores
  }

  /**
   * 计算新颖度
   */
  private calculateNovelty(userId: string, items: Item[]): Map<string, number> {
    const noveltyScores = new Map<string, number>()
    const userMatrix = this.userItemMatrix.get(userId)

    for (const item of items) {
      // 用户从未接触过的内容，新颖度为 1
      if (!userMatrix || !userMatrix.has(item.id)) {
        noveltyScores.set(item.id, 1.0)
      } else {
        // 接触过的内容，新颖度降低
        const interactionLevel = userMatrix.get(item.id)!
        noveltyScores.set(item.id, Math.max(0, 1 - interactionLevel / 10))
      }
    }

    return noveltyScores
  }

  /**
   * 生成推荐理由
   */
  private generateReason(item: Item, score: number): string {
    if (score > 0.8) return '强烈推荐'
    if (score > 0.6) return '推荐'
    if (score > 0.4) return '建议尝试'
    return '可以了解'
  }

  /**
   * 计算推荐置信度
   */
  private calculateConfidence(
    baseScore: number,
    diversity: number,
    novelty: number
  ): number {
    // 基础分数越高，多样性和新颖度越合理，置信度越高
    return (baseScore * 0.6 + diversity * 0.2 + novelty * 0.2) * 100
  }

  /**
   * 获取 Map 中值最大的键
   */
  private getTopItem<T>(map: Map<T, number>): T | undefined {
    let maxKey: T | undefined
    let maxValue = -Infinity

    for (const [key, value] of map) {
      if (value > maxValue) {
        maxValue = value
        maxKey = key
      }
    }

    return maxKey
  }

  /**
   * 清空数据（用于测试）
   */
  clear() {
    this.userBehaviors.clear()
    this.itemFeatures.clear()
    this.userItemMatrix.clear()
  }
}

/**
 * 创建推荐引擎实例
 */
export function createRecommendationEngine(): SmartRecommendationEngine {
  return new SmartRecommendationEngine()
}

/**
 * 单例模式获取全局推荐引擎
 */
let globalEngine: SmartRecommendationEngine | null = null

export function getGlobalRecommendationEngine(): SmartRecommendationEngine {
  if (!globalEngine) {
    globalEngine = new SmartRecommendationEngine()
  }
  return globalEngine
}
