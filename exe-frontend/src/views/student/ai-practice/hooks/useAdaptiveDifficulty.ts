import { computed } from 'vue'
import { ElNotification } from 'element-plus'
import { usePracticeStore } from '@/stores/practiceStore'

/**
 * 自适应难度Hook
 */
export function useAdaptiveDifficulty() {
  const store = usePracticeStore()

  /**
   * 获取推荐的下一题难度
   */
  const getNextQuestionDifficulty = (): number => {
    return Math.round(store.difficultyLevel)
  }

  /**
   * 获取难度等级名称
   */
  const getDifficultyLabel = (level: number): string => {
    const labels = ['未知', '简单', '较简单', '中等', '较难', '困难']
    const index = Math.round(level)
    return labels[Math.min(index, labels.length - 1)] || '中等'
  }

  /**
   * 获取难度颜色
   */
  const getDifficultyColor = (level: number): string => {
    if (level <= 2) return '#67c23a' // 绿色-简单
    if (level === 3) return '#e6a23c' // 黄色-中等
    return '#f56c6c' // 红色-困难
  }

  /**
   * 显示难度调整反馈
   */
  const showDifficultyFeedback = (message: string, type: 'success' | 'warning' | 'info' = 'info') => {
    ElNotification({
      title: '难度调整',
      message,
      type,
      duration: 3000,
      position: 'bottom-right'
    })
  }

  /**
   * 计算当前难度趋势
   */
  const getDifficultyTrend = (): 'rising' | 'falling' | 'stable' => {
    const history = store.adaptiveHistory.adjustmentHistory
    if (history.length < 2) return 'stable'

    const recent = history.slice(-3)
    const firstLevel = recent[0].newLevel
    const lastLevel = recent[recent.length - 1].newLevel

    if (lastLevel > firstLevel) return 'rising'
    if (lastLevel < firstLevel) return 'falling'
    return 'stable'
  }

  /**
   * 获取平均表现
   */
  const getAveragePerformance = (): number => {
    if (store.recentPerformance.length === 0) return 0
    const sum = store.recentPerformance.reduce((a, b) => a + b, 0)
    return sum / store.recentPerformance.length
  }

  /**
   * 获取表现等级
   */
  const getPerformanceGrade = (): '优秀' | '良好' | '一般' | '需加油' => {
    const avg = getAveragePerformance()
    if (avg >= 0.8) return '优秀'
    if (avg >= 0.6) return '良好'
    if (avg >= 0.4) return '一般'
    return '需加油'
  }

  /**
   * 是否需要调整难度
   */
  const shouldAdjustDifficulty = (): { should: boolean; direction?: 'up' | 'down'; reason?: string } => {
    const avg = getAveragePerformance()
    const currentLevel = store.difficultyLevel

    if (avg > 0.8 && currentLevel < 5) {
      return {
        should: true,
        direction: 'up',
        reason: '表现优秀，建议提升难度以获得更大挑战'
      }
    }

    if (avg < 0.4 && currentLevel > 1) {
      return {
        should: true,
        direction: 'down',
        reason: '可适当降低难度，循序渐进地学习'
      }
    }

    return { should: false }
  }

  /**
   * 手动调整难度
   */
  const manualAdjustDifficulty = (newLevel: number) => {
    if (newLevel < 1 || newLevel > 5) {
      console.warn('Invalid difficulty level:', newLevel)
      return
    }

    const oldLevel = store.difficultyLevel
    store.difficultyLevel = newLevel

    store.adaptiveHistory.adjustmentHistory.push({
      timestamp: new Date(),
      oldLevel,
      newLevel,
      reason: '手动调整',
      performanceScore: getAveragePerformance()
    })

    showDifficultyFeedback(
      `难度已调整为：${getDifficultyLabel(newLevel)}`,
      'success'
    )
  }

  /**
   * 重置难度
   */
  const resetDifficulty = () => {
    store.difficultyLevel = 3
    store.recentPerformance = []
    showDifficultyFeedback('难度已重置为中等', 'info')
  }

  return {
    // State
    currentLevel: computed(() => store.difficultyLevel),
    recentPerformance: computed(() => store.recentPerformance),
    adjustmentHistory: computed(() => store.adaptiveHistory.adjustmentHistory),

    // Computed
    averagePerformance: computed(() => getAveragePerformance()),
    performanceGrade: computed(() => getPerformanceGrade()),
    difficultyTrend: computed(() => getDifficultyTrend()),
    nextQuestionDifficulty: computed(() => getNextQuestionDifficulty()),

    // Helpers
    getDifficultyLabel,
    getDifficultyColor,
    shouldAdjustDifficulty,

    // Actions
    manualAdjustDifficulty,
    resetDifficulty,
    showDifficultyFeedback
  }
}
