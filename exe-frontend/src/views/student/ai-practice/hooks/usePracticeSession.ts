import { computed, ref, onMounted, onUnmounted } from 'vue'
import { usePracticeStore } from '@/stores/practiceStore'
import type { PracticeConfig, Question } from '@/types/practice'

/**
 * 练习会话管理Hook
 */
export function usePracticeSession() {
  const store = usePracticeStore()

  // 计时器
  const timer = ref<number | null>(null)
  const currentQuestionStartTime = ref<number>(0)

  /**
   * 开始练习
   */
  const startPractice = async (config: PracticeConfig, questions: Question[]) => {
    await store.startPractice(config, questions)
    startQuestionTimer()
  }

  /**
   * 开始题目计时
   */
  const startQuestionTimer = () => {
    currentQuestionStartTime.value = Date.now()
  }

  /**
   * 获取当前题目已用时间（秒）
   */
  const getCurrentQuestionTime = (): number => {
    if (currentQuestionStartTime.value === 0) return 0
    return Math.floor((Date.now() - currentQuestionStartTime.value) / 1000)
  }

  /**
   * 提交答案
   */
  const submitAnswer = async (questionId: number, answer: string | string[]) => {
    const timeSpent = getCurrentQuestionTime()
    await store.submitAnswer(questionId, answer, timeSpent)

    // 如果还有下一题，开始计时
    if (store.currentIndex < store.questions.length - 1) {
      startQuestionTimer()
    }
  }

  /**
   * 下一题
   */
  const nextQuestion = () => {
    store.nextQuestion()
    startQuestionTimer()
  }

  /**
   * 上一题
   */
  const previousQuestion = () => {
    store.previousQuestion()
    startQuestionTimer()
  }

  /**
   * 跳转到指定题目
   */
  const goToQuestion = (index: number) => {
    store.goToQuestion(index)
    startQuestionTimer()
  }

  /**
   * 暂停练习
   */
  const pausePractice = () => {
    store.pausePractice()
    stopTimer()
  }

  /**
   * 继续练习
   */
  const resumePractice = () => {
    store.resumePractice()
    startQuestionTimer()
  }

  /**
   * 完成练习
   */
  const completePractice = () => {
    stopTimer()
    store.completePractice()
  }

  /**
   * 重置练习
   */
  const resetPractice = () => {
    stopTimer()
    store.resetPractice()
  }

  /**
   * 停止计时器
   */
  const stopTimer = () => {
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
  }

  // 组件挂载时尝试恢复会话
  onMounted(() => {
    const restored = store.restoreSession()
    if (restored) {
      startQuestionTimer()
    }
  })

  // 组件卸载时停止计时
  onUnmounted(() => {
    stopTimer()
  })

  return {
    // State
    currentSession: computed(() => store.currentSession),
    currentQuestion: computed(() => store.currentQuestion),
    currentIndex: computed(() => store.currentIndex),
    questions: computed(() => store.questions),
    userAnswers: computed(() => store.userAnswers),
    isPracticing: computed(() => store.isPracticing),
    isPaused: computed(() => store.isPaused),
    showResults: computed(() => store.showResults),

    // Progress
    progressPercentage: computed(() => store.progressPercentage),
    answeredCount: computed(() => store.answeredCount),
    correctCount: computed(() => store.correctCount),
    wrongCount: computed(() => store.wrongCount),
    currentAccuracy: computed(() => store.currentAccuracy),
    isCompleted: computed(() => store.isCompleted),
    practiceDuration: computed(() => store.practiceDuration),

    // Timing
    currentQuestionTime: computed(() => getCurrentQuestionTime()),

    // Actions
    startPractice,
    submitAnswer,
    nextQuestion,
    previousQuestion,
    goToQuestion,
    pausePractice,
    resumePractice,
    completePractice,
    resetPractice
  }
}
