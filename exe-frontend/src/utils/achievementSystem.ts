/**
 * 学习成就系统
 * 包含成就、积分、等级、徽章和激励机制
 */

export interface Achievement {
  id: string
  name: string
  description: string
  icon: string
  category: 'study' | 'practice' | 'social' | 'special'
  rarity: 'common' | 'rare' | 'epic' | 'legendary'
  points: number
  requirement: AchievementRequirement
  unlocked: boolean
  unlockedAt?: number
  progress: number // 0-100
}

export interface AchievementRequirement {
  type:
    | 'study_time'
    | 'practice_count'
    | 'correct_rate'
    | 'streak_days'
    | 'knowledge_mastery'
    | 'ai_chat'
    | 'note_count'
    | 'perfect_score'
  target: number
  current?: number
}

export interface UserLevel {
  level: number
  title: string
  currentExp: number
  expToNextLevel: number
  totalExp: number
  perks: string[]
}

export interface Badge {
  id: string
  name: string
  icon: string
  color: string
  description: string
  earnedAt: number
}

export interface LearningStats {
  totalStudyTime: number // 总学习时长（分钟）
  totalPracticeCount: number // 总练习次数
  avgCorrectRate: number // 平均正确率
  streakDays: number // 连续学习天数
  knowledgePointsMastered: number // 已掌握知识点数
  aiChatCount: number // AI对话次数
  noteCount: number // 笔记数量
  perfectScoreCount: number // 满分次数
}

export interface Reward {
  type: 'points' | 'badge' | 'title' | 'special'
  value: any
  message: string
  animation?: 'confetti' | 'firework' | 'shine'
}

export class AchievementSystem {
  private achievements: Map<string, Achievement> = new Map()
  private stats: LearningStats = {
    totalStudyTime: 0,
    totalPracticeCount: 0,
    avgCorrectRate: 0,
    streakDays: 0,
    knowledgePointsMastered: 0,
    aiChatCount: 0,
    noteCount: 0,
    perfectScoreCount: 0
  }
  private level: UserLevel = {
    level: 1,
    title: '初学者',
    currentExp: 0,
    expToNextLevel: 100,
    totalExp: 0,
    perks: []
  }
  private badges: Badge[] = []
  private unlockedAchievements: string[] = []

  constructor() {
    this.initializeAchievements()
  }

  /**
   * 初始化成就列表
   */
  private initializeAchievements() {
    const achievementList: Achievement[] = [
      // 学习时长成就
      {
        id: 'study_1h',
        name: '学海初航',
        description: '累计学习1小时',
        icon: '🎯',
        category: 'study',
        rarity: 'common',
        points: 10,
        requirement: { type: 'study_time', target: 60 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'study_10h',
        name: '勤学苦练',
        description: '累计学习10小时',
        icon: '📚',
        category: 'study',
        rarity: 'common',
        points: 50,
        requirement: { type: 'study_time', target: 600 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'study_100h',
        name: '学习达人',
        description: '累计学习100小时',
        icon: '🏆',
        category: 'study',
        rarity: 'rare',
        points: 200,
        requirement: { type: 'study_time', target: 6000 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'study_500h',
        name: '学霸之路',
        description: '累计学习500小时',
        icon: '👑',
        category: 'study',
        rarity: 'epic',
        points: 1000,
        requirement: { type: 'study_time', target: 30000 },
        unlocked: false,
        progress: 0
      },

      // 练习次数成就
      {
        id: 'practice_10',
        name: '初试锋芒',
        description: '完成10次练习',
        icon: '✏️',
        category: 'practice',
        rarity: 'common',
        points: 10,
        requirement: { type: 'practice_count', target: 10 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'practice_100',
        name: '百炼成钢',
        description: '完成100次练习',
        icon: '💪',
        category: 'practice',
        rarity: 'rare',
        points: 100,
        requirement: { type: 'practice_count', target: 100 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'practice_1000',
        name: '千锤百炼',
        description: '完成1000次练习',
        icon: '⚡',
        category: 'practice',
        rarity: 'epic',
        points: 500,
        requirement: { type: 'practice_count', target: 1000 },
        unlocked: false,
        progress: 0
      },

      // 正确率成就
      {
        id: 'accuracy_80',
        name: '精益求精',
        description: '平均正确率达到80%',
        icon: '🎖️',
        category: 'practice',
        rarity: 'rare',
        points: 150,
        requirement: { type: 'correct_rate', target: 80 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'accuracy_90',
        name: '卓越表现',
        description: '平均正确率达到90%',
        icon: '🌟',
        category: 'practice',
        rarity: 'epic',
        points: 300,
        requirement: { type: 'correct_rate', target: 90 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'accuracy_95',
        name: '完美主义',
        description: '平均正确率达到95%',
        icon: '💎',
        category: 'practice',
        rarity: 'legendary',
        points: 500,
        requirement: { type: 'correct_rate', target: 95 },
        unlocked: false,
        progress: 0
      },

      // 连续学习成就
      {
        id: 'streak_7',
        name: '一周坚持',
        description: '连续学习7天',
        icon: '🔥',
        category: 'study',
        rarity: 'common',
        points: 50,
        requirement: { type: 'streak_days', target: 7 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'streak_30',
        name: '一月不辍',
        description: '连续学习30天',
        icon: '🌈',
        category: 'study',
        rarity: 'rare',
        points: 200,
        requirement: { type: 'streak_days', target: 30 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'streak_100',
        name: '百日筑基',
        description: '连续学习100天',
        icon: '🚀',
        category: 'study',
        rarity: 'epic',
        points: 800,
        requirement: { type: 'streak_days', target: 100 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'streak_365',
        name: '全年无休',
        description: '连续学习365天',
        icon: '🏅',
        category: 'study',
        rarity: 'legendary',
        points: 3000,
        requirement: { type: 'streak_days', target: 365 },
        unlocked: false,
        progress: 0
      },

      // 知识点掌握成就
      {
        id: 'mastery_10',
        name: '知识萌芽',
        description: '掌握10个知识点',
        icon: '🌱',
        category: 'study',
        rarity: 'common',
        points: 20,
        requirement: { type: 'knowledge_mastery', target: 10 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'mastery_50',
        name: '学识渐丰',
        description: '掌握50个知识点',
        icon: '🌳',
        category: 'study',
        rarity: 'rare',
        points: 100,
        requirement: { type: 'knowledge_mastery', target: 50 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'mastery_100',
        name: '博学多识',
        description: '掌握100个知识点',
        icon: '🌲',
        category: 'study',
        rarity: 'epic',
        points: 300,
        requirement: { type: 'knowledge_mastery', target: 100 },
        unlocked: false,
        progress: 0
      },

      // AI互动成就
      {
        id: 'ai_chat_10',
        name: 'AI好友',
        description: '与AI助手对话10次',
        icon: '🤖',
        category: 'social',
        rarity: 'common',
        points: 10,
        requirement: { type: 'ai_chat', target: 10 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'ai_chat_100',
        name: 'AI伙伴',
        description: '与AI助手对话100次',
        icon: '💬',
        category: 'social',
        rarity: 'rare',
        points: 50,
        requirement: { type: 'ai_chat', target: 100 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'ai_chat_500',
        name: 'AI挚友',
        description: '与AI助手对话500次',
        icon: '💝',
        category: 'social',
        rarity: 'epic',
        points: 200,
        requirement: { type: 'ai_chat', target: 500 },
        unlocked: false,
        progress: 0
      },

      // 笔记成就
      {
        id: 'note_10',
        name: '笔记新手',
        description: '创建10条笔记',
        icon: '📝',
        category: 'study',
        rarity: 'common',
        points: 20,
        requirement: { type: 'note_count', target: 10 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'note_50',
        name: '笔记达人',
        description: '创建50条笔记',
        icon: '📖',
        category: 'study',
        rarity: 'rare',
        points: 100,
        requirement: { type: 'note_count', target: 50 },
        unlocked: false,
        progress: 0
      },

      // 满分成就
      {
        id: 'perfect_1',
        name: '首次满分',
        description: '第一次获得满分',
        icon: '🎊',
        category: 'special',
        rarity: 'rare',
        points: 100,
        requirement: { type: 'perfect_score', target: 1 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'perfect_10',
        name: '满分常客',
        description: '获得10次满分',
        icon: '🎉',
        category: 'special',
        rarity: 'epic',
        points: 300,
        requirement: { type: 'perfect_score', target: 10 },
        unlocked: false,
        progress: 0
      },
      {
        id: 'perfect_100',
        name: '满分大师',
        description: '获得100次满分',
        icon: '🎆',
        category: 'special',
        rarity: 'legendary',
        points: 1000,
        requirement: { type: 'perfect_score', target: 100 },
        unlocked: false,
        progress: 0
      }
    ]

    achievementList.forEach(achievement => {
      this.achievements.set(achievement.id, achievement)
    })
  }

  /**
   * 更新学习数据
   */
  updateStats(stats: Partial<LearningStats>) {
    Object.assign(this.stats, stats)
    this.checkAchievements()
    this.updateLevel()
  }

  /**
   * 检查成就解锁
   */
  private checkAchievements(): Reward[] {
    const rewards: Reward[] = []

    for (const [id, achievement] of this.achievements) {
      if (achievement.unlocked) continue

      const { type, target } = achievement.requirement
      let current = 0

      // 获取当前进度
      switch (type) {
        case 'study_time':
          current = this.stats.totalStudyTime
          break
        case 'practice_count':
          current = this.stats.totalPracticeCount
          break
        case 'correct_rate':
          current = this.stats.avgCorrectRate
          break
        case 'streak_days':
          current = this.stats.streakDays
          break
        case 'knowledge_mastery':
          current = this.stats.knowledgePointsMastered
          break
        case 'ai_chat':
          current = this.stats.aiChatCount
          break
        case 'note_count':
          current = this.stats.noteCount
          break
        case 'perfect_score':
          current = this.stats.perfectScoreCount
          break
      }

      achievement.requirement.current = current
      achievement.progress = Math.min(100, (current / target) * 100)

      // 检查是否达成
      if (current >= target) {
        achievement.unlocked = true
        achievement.unlockedAt = Date.now()
        this.unlockedAchievements.push(id)

        // 添加积分奖励
        this.addExp(achievement.points)

        rewards.push({
          type: 'points',
          value: achievement.points,
          message: `🎉 解锁成就：${achievement.name}！获得 ${achievement.points} 积分`,
          animation: this.getAnimationByRarity(achievement.rarity)
        })

        // 特殊成就额外奖励徽章
        if (achievement.rarity === 'legendary') {
          const badge: Badge = {
            id: achievement.id + '_badge',
            name: achievement.name,
            icon: achievement.icon,
            color: '#FFD700',
            description: achievement.description,
            earnedAt: Date.now()
          }
          this.badges.push(badge)

          rewards.push({
            type: 'badge',
            value: badge,
            message: `🏆 获得传奇徽章：${badge.name}！`,
            animation: 'confetti'
          })
        }
      }
    }

    return rewards
  }

  /**
   * 根据稀有度获取动画
   */
  private getAnimationByRarity(rarity: string): 'confetti' | 'firework' | 'shine' {
    switch (rarity) {
      case 'legendary':
        return 'confetti'
      case 'epic':
        return 'firework'
      default:
        return 'shine'
    }
  }

  /**
   * 添加经验值
   */
  addExp(exp: number) {
    this.level.currentExp += exp
    this.level.totalExp += exp

    // 检查升级
    while (this.level.currentExp >= this.level.expToNextLevel) {
      this.levelUp()
    }
  }

  /**
   * 升级
   */
  private levelUp() {
    this.level.currentExp -= this.level.expToNextLevel
    this.level.level++
    this.level.expToNextLevel = this.calculateExpForNextLevel(this.level.level)
    this.level.title = this.getLevelTitle(this.level.level)
    this.level.perks = this.getLevelPerks(this.level.level)
  }

  /**
   * 计算下一级所需经验
   */
  private calculateExpForNextLevel(level: number): number {
    return Math.floor(100 * Math.pow(1.5, level - 1))
  }

  /**
   * 获取等级称号
   */
  private getLevelTitle(level: number): string {
    const titles = [
      '初学者',
      '学徒',
      '学者',
      '专家',
      '大师',
      '宗师',
      '传奇',
      '至尊'
    ]

    const index = Math.min(Math.floor(level / 5), titles.length - 1)
    return titles[index]
  }

  /**
   * 获取等级特权
   */
  private getLevelPerks(level: number): string[] {
    const perks: string[] = []

    if (level >= 5) perks.push('解锁高级AI功能')
    if (level >= 10) perks.push('自定义学习计划')
    if (level >= 15) perks.push('专属学习报告')
    if (level >= 20) perks.push('优先答疑服务')
    if (level >= 30) perks.push('学习大师勋章')
    if (level >= 50) perks.push('终身学习荣誉')

    return perks
  }

  /**
   * 更新等级
   */
  private updateLevel() {
    // 等级已在 addExp 中自动更新
  }

  /**
   * 获取所有成就
   */
  getAllAchievements(): Achievement[] {
    return Array.from(this.achievements.values())
  }

  /**
   * 获取已解锁成就
   */
  getUnlockedAchievements(): Achievement[] {
    return Array.from(this.achievements.values()).filter(a => a.unlocked)
  }

  /**
   * 获取进行中的成就（已有进度但未解锁）
   */
  getInProgressAchievements(): Achievement[] {
    return Array.from(this.achievements.values())
      .filter(a => !a.unlocked && a.progress > 0)
      .sort((a, b) => b.progress - a.progress)
  }

  /**
   * 获取下一个即将解锁的成就
   */
  getNextAchievements(count: number = 3): Achievement[] {
    return Array.from(this.achievements.values())
      .filter(a => !a.unlocked)
      .sort((a, b) => b.progress - a.progress)
      .slice(0, count)
  }

  /**
   * 获取用户等级
   */
  getLevel(): UserLevel {
    return { ...this.level }
  }

  /**
   * 获取所有徽章
   */
  getBadges(): Badge[] {
    return [...this.badges]
  }

  /**
   * 获取学习统计
   */
  getStats(): LearningStats {
    return { ...this.stats }
  }

  /**
   * 获取排行榜数据（需要对比其他用户）
   */
  getLeaderboardRank(): {
    totalPoints: number
    level: number
    rank: number
    percentile: number
  } {
    // 这里需要从服务器获取其他用户数据进行对比
    // 当前仅返回本地数据
    return {
      totalPoints: this.level.totalExp,
      level: this.level.level,
      rank: 0, // 需要服务器计算
      percentile: 0 // 需要服务器计算
    }
  }

  /**
   * 获取今日任务
   */
  getDailyTasks(): Array<{
    id: string
    name: string
    description: string
    progress: number
    reward: number
    completed: boolean
  }> {
    return [
      {
        id: 'daily_study_30min',
        name: '每日学习',
        description: '学习30分钟',
        progress: 0,
        reward: 10,
        completed: false
      },
      {
        id: 'daily_practice_5',
        name: '每日练习',
        description: '完成5道练习题',
        progress: 0,
        reward: 10,
        completed: false
      },
      {
        id: 'daily_ai_chat',
        name: 'AI互动',
        description: '与AI助手对话3次',
        progress: 0,
        reward: 5,
        completed: false
      }
    ]
  }

  /**
   * 清空数据
   */
  clear() {
    this.stats = {
      totalStudyTime: 0,
      totalPracticeCount: 0,
      avgCorrectRate: 0,
      streakDays: 0,
      knowledgePointsMastered: 0,
      aiChatCount: 0,
      noteCount: 0,
      perfectScoreCount: 0
    }
    this.level = {
      level: 1,
      title: '初学者',
      currentExp: 0,
      expToNextLevel: 100,
      totalExp: 0,
      perks: []
    }
    this.badges = []
    this.unlockedAchievements = []

    // 重置所有成就
    for (const achievement of this.achievements.values()) {
      achievement.unlocked = false
      achievement.progress = 0
      achievement.unlockedAt = undefined
      if (achievement.requirement.current !== undefined) {
        achievement.requirement.current = 0
      }
    }
  }
}

/**
 * 创建成就系统
 */
export function createAchievementSystem(): AchievementSystem {
  return new AchievementSystem()
}

/**
 * 全局成就系统单例
 */
let globalAchievementSystem: AchievementSystem | null = null

export function getGlobalAchievementSystem(): AchievementSystem {
  if (!globalAchievementSystem) {
    globalAchievementSystem = new AchievementSystem()
  }
  return globalAchievementSystem
}
