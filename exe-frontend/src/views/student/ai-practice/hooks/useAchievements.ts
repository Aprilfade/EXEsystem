import { computed, h } from 'vue'
import { ElNotification } from 'element-plus'
import { usePracticeStore } from '@/stores/practiceStore'
import type { Achievement, AchievementCondition } from '@/types/practice'

/**
 * 成就配置
 */
export const ACHIEVEMENTS: Achievement[] = [
  // 基础成就
  {
    id: 'first_blood',
    name: '初次尝试',
    description: '完成第一道题目',
    icon: '🎯',
    rarity: 'common',
    condition: { type: 'total_correct', target: 1 },
    reward: { xp: 10 }
  },
  {
    id: 'getting_started',
    name: '入门选手',
    description: '累计答对10道题',
    icon: '📝',
    rarity: 'common',
    condition: { type: 'total_correct', target: 10 },
    reward: { xp: 50 }
  },
  {
    id: 'practice_makes_perfect',
    name: '勤学苦练',
    description: '累计答对50道题',
    icon: '📚',
    rarity: 'rare',
    condition: { type: 'total_correct', target: 50 },
    reward: { xp: 200 }
  },
  {
    id: 'master',
    name: '练习大师',
    description: '累计答对200道题',
    icon: '👑',
    rarity: 'epic',
    condition: { type: 'total_correct', target: 200 },
    reward: { xp: 500, title: '练习大师' }
  },

  // 连击成就
  {
    id: 'streak_5',
    name: '五连胜',
    description: '连续答对5题',
    icon: '🔥',
    rarity: 'rare',
    condition: { type: 'streak', target: 5 },
    reward: { xp: 50 }
  },
  {
    id: 'streak_10',
    name: '十连胜',
    description: '连续答对10题',
    icon: '⚡',
    rarity: 'epic',
    condition: { type: 'streak', target: 10 },
    reward: { xp: 150, title: '连击大师' }
  },
  {
    id: 'streak_20',
    name: '无懈可击',
    description: '连续答对20题',
    icon: '💫',
    rarity: 'legendary',
    condition: { type: 'streak', target: 20 },
    reward: { xp: 500, title: '无懈可击', badge: 'streak_legend' }
  },

  // 完美练习
  {
    id: 'perfect_practice',
    name: '完美练习',
    description: '单次练习全部答对（至少10题）',
    icon: '💎',
    rarity: 'epic',
    condition: { type: 'perfect_day', target: 10 },
    reward: { xp: 200, badge: 'perfect' }
  },
  {
    id: 'flawless_victory',
    name: '完美胜利',
    description: '单次练习20题以上全部答对',
    icon: '🏆',
    rarity: 'legendary',
    condition: { type: 'perfect_day', target: 20 },
    reward: { xp: 500, title: '完美主义者' }
  },

  // 速度成就
  {
    id: 'speed_demon',
    name: '闪电侠',
    description: '10秒内答对一题',
    icon: '⚡',
    rarity: 'rare',
    condition: { type: 'speed', target: 10 },
    reward: { xp: 80 }
  },
  {
    id: 'quick_thinker',
    name: '思维敏捷',
    description: '5秒内答对一题',
    icon: '🚀',
    rarity: 'epic',
    condition: { type: 'speed', target: 5 },
    reward: { xp: 150 }
  },

  // AI互动成就
  {
    id: 'ai_learner',
    name: 'AI学习者',
    description: '使用AI批改功能10次',
    icon: '🤖',
    rarity: 'rare',
    condition: { type: 'ai_grading', target: 10 },
    reward: { xp: 100 }
  },
  {
    id: 'ai_master',
    name: 'AI助手专家',
    description: '使用AI批改功能50次',
    icon: '🎓',
    rarity: 'epic',
    condition: { type: 'ai_grading', target: 50 },
    reward: { xp: 300, title: 'AI学习专家' }
  },

  // 学科精通
  {
    id: 'math_master',
    name: '数学大师',
    description: '数学题目正确率达到90%（至少100题）',
    icon: '📐',
    rarity: 'legendary',
    condition: {
      type: 'subject_master',
      target: 90,
      metadata: { subject: '数学', minQuestions: 100 }
    },
    reward: { xp: 500, title: '数学大师', badge: 'math_master' }
  },

  // 坚持成就
  {
    id: 'daily_warrior',
    name: '每日战士',
    description: '连续练习7天',
    icon: '📅',
    rarity: 'rare',
    condition: { type: 'daily_practice', target: 7 },
    reward: { xp: 200 }
  },
  {
    id: 'monthly_champion',
    name: '月度冠军',
    description: '连续练习30天',
    icon: '🏅',
    rarity: 'legendary',
    condition: { type: 'daily_practice', target: 30 },
    reward: { xp: 1000, title: '坚持不懈', badge: 'champion' }
  }
]

/**
 * 成就系统Hook
 */
export function useAchievements() {
  const store = usePracticeStore()

  /**
   * 检查成就条件
   */
  const checkCondition = (
    condition: AchievementCondition,
    context: {
      isCorrect?: boolean
      timeSpent?: number
      questionId?: number
      subject?: string
    }
  ): boolean => {
    switch (condition.type) {
      case 'total_correct':
        return store.correctCount >= condition.target

      case 'streak':
        return store.currentStreak >= condition.target

      case 'perfect_day':
        return (
          store.isCompleted &&
          store.questions.length >= condition.target &&
          store.wrongCount === 0
        )

      case 'speed':
        return context.timeSpent !== undefined && context.timeSpent <= condition.target

      case 'ai_grading':
        return store.gradingResults.size >= condition.target

      case 'subject_master':
        // 学科精通检测
        if (!condition.metadata?.subject || !condition.metadata?.minQuestions) {
          return false
        }

        const subjectStats = store.subjectStatistics.get(condition.metadata.subject)
        if (!subjectStats) return false

        // 检查题目数量是否达标
        if (subjectStats.totalQuestions < condition.metadata.minQuestions) {
          return false
        }

        // 检查正确率是否达标
        return subjectStats.accuracy >= condition.target

      case 'daily_practice':
        // 连续练习天数检测
        const consecutiveDays = store.getConsecutivePracticeDays()
        return consecutiveDays >= condition.target

      default:
        return false
    }
  }

  /**
   * 检查并解锁成就
   */
  const checkAchievements = (context: {
    isCorrect?: boolean
    timeSpent?: number
    questionId?: number
    subject?: string
  }): Achievement[] => {
    const unlocked: Achievement[] = []

    for (const achievement of ACHIEVEMENTS) {
      // 已解锁则跳过
      if (store.achievements.find(a => a.id === achievement.id)) {
        continue
      }

      // 检查条件
      if (checkCondition(achievement.condition, context)) {
        unlockAchievement(achievement)
        unlocked.push(achievement)
      }
    }

    return unlocked
  }

  /**
   * 解锁成就
   */
  const unlockAchievement = (achievement: Achievement) => {
    store.unlockAchievement(achievement)
    showAchievementUnlock(achievement)

    // 播放音效
    playSound('achievement_unlock')
  }

  /**
   * 显示成就解锁通知
   */
  const showAchievementUnlock = (achievement: Achievement) => {
    const rarityColors: Record<string, string> = {
      common: '#909399',
      rare: '#409eff',
      epic: '#9c27b0',
      legendary: '#ff6b00'
    }

    ElNotification({
      title: h('div', { style: 'display: flex; align-items: center; gap: 8px;' }, [
        h('span', '🎉'),
        h('span', { style: { fontWeight: 'bold' } }, '成就解锁！')
      ]),
      message: h('div', { style: 'padding: 8px 0;' }, [
        h('div', {
          style: {
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            marginBottom: '8px'
          }
        }, [
          h('span', { style: { fontSize: '32px' } }, achievement.icon),
          h('div', [
            h('div', {
              style: {
                fontSize: '16px',
                fontWeight: 'bold',
                color: rarityColors[achievement.rarity],
                marginBottom: '4px'
              }
            }, achievement.name),
            h('div', {
              style: {
                fontSize: '13px',
                color: '#606266'
              }
            }, achievement.description)
          ])
        ]),
        h('div', {
          style: {
            fontSize: '14px',
            color: '#67c23a',
            fontWeight: 'bold'
          }
        }, `+${achievement.reward.xp} XP`)
      ]),
      type: 'success',
      duration: 5000,
      customClass: 'achievement-notification',
      position: 'top-right'
    })
  }

  /**
   * 播放音效
   */
  const playSound = (soundName: string) => {
    // 检查用户是否禁用了音效
    const soundEnabled = localStorage.getItem('sound_enabled') !== 'false'
    if (!soundEnabled) return

    try {
      // 使用Web Audio API创建音效
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
      const oscillator = audioContext.createOscillator()
      const gainNode = audioContext.createGain()

      oscillator.connect(gainNode)
      gainNode.connect(audioContext.destination)

      // 根据音效类型设置不同的频率和波形
      switch (soundName) {
        case 'achievement_unlock':
          // 成就解锁音效：上升音调
          oscillator.type = 'sine'
          oscillator.frequency.setValueAtTime(523.25, audioContext.currentTime) // C5
          oscillator.frequency.exponentialRampToValueAtTime(783.99, audioContext.currentTime + 0.1) // G5
          oscillator.frequency.exponentialRampToValueAtTime(1046.50, audioContext.currentTime + 0.2) // C6
          gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
          gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3)
          oscillator.start(audioContext.currentTime)
          oscillator.stop(audioContext.currentTime + 0.3)
          break

        case 'correct_answer':
          // 答对音效：清脆的叮
          oscillator.type = 'sine'
          oscillator.frequency.value = 800
          gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
          gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.15)
          oscillator.start()
          oscillator.stop(audioContext.currentTime + 0.15)
          break

        case 'wrong_answer':
          // 答错音效：低沉的提示
          oscillator.type = 'sine'
          oscillator.frequency.value = 200
          gainNode.gain.setValueAtTime(0.2, audioContext.currentTime)
          gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.2)
          oscillator.start()
          oscillator.stop(audioContext.currentTime + 0.2)
          break

        case 'level_up':
          // 升级音效：连续上升音
          oscillator.type = 'square'
          oscillator.frequency.setValueAtTime(440, audioContext.currentTime)
          oscillator.frequency.exponentialRampToValueAtTime(880, audioContext.currentTime + 0.15)
          oscillator.frequency.exponentialRampToValueAtTime(1320, audioContext.currentTime + 0.3)
          gainNode.gain.setValueAtTime(0.2, audioContext.currentTime)
          gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.4)
          oscillator.start()
          oscillator.stop(audioContext.currentTime + 0.4)
          break

        default:
          console.log('Playing sound:', soundName)
      }
    } catch (error) {
      console.warn('Failed to play sound:', error)
    }
  }

  /**
   * 获取成就进度
   */
  const getAchievementProgress = (achievementId: string): number => {
    const achievement = ACHIEVEMENTS.find(a => a.id === achievementId)
    if (!achievement) return 0

    const condition = achievement.condition

    switch (condition.type) {
      case 'total_correct':
        return Math.min(100, (store.correctCount / condition.target) * 100)

      case 'streak':
        return Math.min(100, (store.currentStreak / condition.target) * 100)

      case 'ai_grading':
        return Math.min(100, (store.gradingResults.size / condition.target) * 100)

      default:
        return 0
    }
  }

  /**
   * 获取稀有度颜色
   */
  const getRarityColor = (rarity: string): string => {
    const colors: Record<string, string> = {
      common: '#909399',
      rare: '#409eff',
      epic: '#9c27b0',
      legendary: '#ff6b00'
    }
    return colors[rarity] || colors.common
  }

  /**
   * 获取稀有度标签
   */
  const getRarityLabel = (rarity: string): string => {
    const labels: Record<string, string> = {
      common: '普通',
      rare: '稀有',
      epic: '史诗',
      legendary: '传说'
    }
    return labels[rarity] || '未知'
  }

  return {
    // State
    achievements: computed(() => store.achievements),
    totalXp: computed(() => store.totalXp),
    level: computed(() => store.userLevel),
    levelProgress: computed(() => store.levelProgress),
    currentStreak: computed(() => store.currentStreak),

    // All achievements
    allAchievements: ACHIEVEMENTS,

    // Actions
    checkAchievements,
    unlockAchievement,
    getAchievementProgress,

    // Helpers
    getRarityColor,
    getRarityLabel,

    // Sound
    playSound
  }
}
