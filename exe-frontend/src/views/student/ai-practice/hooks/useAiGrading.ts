import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { usePracticeStore } from '@/stores/practiceStore'
import { analyzeAnswerStream } from '@/api/ai'
import type { AiGradingResult, Question, GradingRequest } from '@/types/practice'

/**
 * AI批改Hook
 */
export function useAiGrading() {
  const store = usePracticeStore()

  const grading = ref(false)
  const currentGrading = ref<number | null>(null)

  /**
   * 请求AI批改
   */
  const requestAiGrading = async (
    question: Question,
    userAnswer: string
  ): Promise<AiGradingResult | null> => {
    if (grading.value) {
      ElMessage.warning('正在批改中，请稍候...')
      return null
    }

    grading.value = true
    currentGrading.value = question.id

    try {
      const payload: GradingRequest = {
        questionId: question.id,
        questionType: question.questionType,
        questionContent: question.content,
        correctAnswer: question.answer,
        userAnswer
      }

      // 调用流式API
      const result = await callStreamingGradingApi(payload)

      // 保存批改结果
      if (result) {
        store.saveAiGrading(question.id, result)
      }

      return result
    } catch (error) {
      console.error('AI批改失败:', error)
      ElMessage.error('AI批改失败，请稍后重试')
      return null
    } finally {
      grading.value = false
      currentGrading.value = null
    }
  }

  /**
   * 调用流式批改API
   */
  const callStreamingGradingApi = (
    payload: GradingRequest
  ): Promise<AiGradingResult> => {
    return new Promise((resolve, reject) => {
      let fullAnalysis = ''
      let score = 0
      const suggestions: string[] = []

      analyzeAnswerStream(
        payload,
        (chunk) => {
          // 累加分析内容
          fullAnalysis += chunk

          // 解析评分
          const scoreMatch = fullAnalysis.match(/评分[:：]\s*(\d+)/)
          if (scoreMatch) {
            score = parseInt(scoreMatch[1])
          }

          // 解析建议
          const suggMatch = fullAnalysis.match(/建议[:：]\s*(.+?)(?=\n|$)/g)
          if (suggMatch && suggestions.length === 0) {
            suggestions.push(...suggMatch.map(s => s.replace(/建议[:：]\s*/, '')))
          }
        },
        () => {
          // 流结束，返回结果
          const result: AiGradingResult = {
            questionId: payload.questionId,
            score,
            analysis: fullAnalysis,
            suggestions: suggestions.length > 0 ? suggestions : undefined,
            gradingTime: new Date()
          }
          resolve(result)
        },
        (error) => {
          console.error('Stream error:', error)
          reject(error)
        }
      )
    })
  }

  /**
   * 获取题目的批改结果
   */
  const getGradingResult = (questionId: number): AiGradingResult | undefined => {
    return store.gradingResults.get(questionId)
  }

  /**
   * 获取批改分数颜色
   */
  const getScoreColor = (score: number): string => {
    if (score >= 90) return '#67c23a' // 优秀-绿色
    if (score >= 80) return '#409eff' // 良好-蓝色
    if (score >= 70) return '#e6a23c' // 中等-黄色
    if (score >= 60) return '#f56c6c' // 及格-橙色
    return '#909399' // 不及格-灰色
  }

  /**
   * 获取批改等级
   */
  const getScoreGrade = (score: number): string => {
    if (score >= 90) return '优秀'
    if (score >= 80) return '良好'
    if (score >= 70) return '中等'
    if (score >= 60) return '及格'
    return '需努力'
  }

  /**
   * 判断题目是否已批改
   */
  const isGraded = (questionId: number): boolean => {
    return store.gradingResults.has(questionId)
  }

  /**
   * 判断题目是否可以AI批改
   */
  const canAiGrade = (questionType: number): boolean => {
    // 主观题（5）和计算题可以AI批改
    return questionType === 5 || questionType === 6
  }

  /**
   * 清除批改结果
   */
  const clearGradingResult = (questionId: number) => {
    store.gradingResults.delete(questionId)
  }

  /**
   * 清除所有批改结果
   */
  const clearAllGradingResults = () => {
    store.gradingResults.clear()
  }

  /**
   * 获取批改统计
   */
  const getGradingStats = () => {
    const total = store.gradingResults.size
    if (total === 0) {
      return {
        total: 0,
        averageScore: 0,
        excellent: 0,
        good: 0,
        average: 0,
        pass: 0,
        fail: 0
      }
    }

    let totalScore = 0
    let excellent = 0
    let good = 0
    let average = 0
    let pass = 0
    let fail = 0

    store.gradingResults.forEach(result => {
      totalScore += result.score

      if (result.score >= 90) excellent++
      else if (result.score >= 80) good++
      else if (result.score >= 70) average++
      else if (result.score >= 60) pass++
      else fail++
    })

    return {
      total,
      averageScore: Math.round(totalScore / total),
      excellent,
      good,
      average,
      pass,
      fail
    }
  }

  return {
    // State
    grading: computed(() => grading.value),
    currentGrading: computed(() => currentGrading.value),
    gradingResults: computed(() => store.gradingResults),

    // Actions
    requestAiGrading,
    getGradingResult,
    clearGradingResult,
    clearAllGradingResults,

    // Helpers
    getScoreColor,
    getScoreGrade,
    isGraded,
    canAiGrade,
    getGradingStats
  }
}
