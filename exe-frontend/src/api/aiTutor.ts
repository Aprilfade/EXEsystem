/**
 * AI家教相关API
 */
import request from '@/utils/request'
import { createDeepSeekClient } from '@/utils/deepseek'

const deepseek = createDeepSeekClient()

// ==================== AI功能（调用DeepSeek） ====================

/**
 * AI讲解知识点
 */
export async function explainKnowledgePoint(params: {
  kpName: string
  grade: string
  subject: string
}) {
  try {
    return await deepseek.explainKnowledgePoint(
      params.kpName,
      params.grade,
      params.subject
    )
  } catch (error: any) {
    throw new Error(error.message || 'AI讲解失败')
  }
}

/**
 * AI辅导习题
 */
export async function tutorExercise(params: {
  question: string
  studentAnswer: string
  correctAnswer: string
  subject: string
}) {
  try {
    return await deepseek.tutorExercise(
      params.question,
      params.studentAnswer,
      params.correctAnswer,
      params.subject
    )
  } catch (error: any) {
    throw new Error(error.message || 'AI辅导失败')
  }
}

/**
 * AI生成练习题
 */
export async function generateExercises(params: {
  kpName: string
  difficulty: '简单' | '中等' | '困难'
  count: number
  subject: string
}) {
  try {
    return await deepseek.generateExercises(
      params.kpName,
      params.difficulty,
      params.count,
      params.subject
    )
  } catch (error: any) {
    throw new Error(error.message || 'AI生成习题失败')
  }
}

/**
 * AI学习建议
 */
export async function getStudyAdvice(params: {
  subject: string
  weakPoints: string[]
  studyTime: number
  accuracy: number
}) {
  try {
    return await deepseek.getStudyAdvice(
      params.subject,
      params.weakPoints,
      params.studyTime,
      params.accuracy
    )
  } catch (error: any) {
    throw new Error(error.message || '获取学习建议失败')
  }
}

/**
 * AI答疑
 */
export async function answerQuestion(params: {
  question: string
  context: string
  subject: string
}) {
  try {
    return await deepseek.answerQuestion(
      params.question,
      params.context,
      params.subject
    )
  } catch (error: any) {
    throw new Error(error.message || 'AI答疑失败')
  }
}

/**
 * 流式AI讲解（用于实时显示）
 */
export async function* explainKnowledgePointStream(params: {
  kpName: string
  grade: string
  subject: string
}) {
  const messages = [
    {
      role: 'system' as const,
      content: `你是一位经验丰富的${params.subject}老师，擅长用通俗易懂的语言给${params.grade}学生讲解知识。请用循序渐进、生动有趣的方式解释概念，并给出实际例子。`
    },
    {
      role: 'user' as const,
      content: `请详细讲解"${params.kpName}"这个知识点，要求：
1. 先用一句话简单概括核心概念
2. 详细解释原理和要点
3. 给出2-3个具体例子
4. 总结重点和易错点

请用Markdown格式输出，包含适当的标题和列表。`
    }
  ]

  try {
    for await (const chunk of deepseek.chatStream(messages)) {
      yield chunk
    }
  } catch (error: any) {
    throw new Error(error.message || 'AI讲解失败')
  }
}

// ==================== 后端API（数据持久化） ====================

/**
 * 保存学习记录
 */
export function saveStudyRecord(data: {
  subject: string
  chapterId: number
  chapterName: string
  sectionId?: number
  sectionTitle?: string
  studyTime: number
  exerciseCount: number
  correctCount: number
  completed?: boolean
  progress?: number
}) {
  return request({
    url: '/api/v1/student/ai-tutor/study-record',
    method: 'post',
    data
  })
}

/**
 * 获取学习统计
 */
export function getStudyStats(subject?: string) {
  return request({
    url: '/api/v1/student/ai-tutor/stats',
    method: 'get',
    params: { subject }
  })
}

/**
 * 获取学习记录列表
 */
export function getStudyRecords(params?: {
  subject?: string
  page?: number
  pageSize?: number
}) {
  return request({
    url: '/api/v1/student/ai-tutor/study-records',
    method: 'get',
    params
  })
}

/**
 * 保存笔记
 */
export function saveNote(data: {
  title: string
  content: string
  tag?: string
  chapterId?: number
  sectionId?: number
  subject?: string
}) {
  return request({
    url: '/api/v1/student/ai-tutor/note',
    method: 'post',
    data
  })
}

/**
 * 获取我的笔记
 */
export function getMyNotes(params?: {
  keyword?: string
  tag?: string
  page?: number
  pageSize?: number
}) {
  return request({
    url: '/api/v1/student/ai-tutor/notes',
    method: 'get',
    params
  })
}

/**
 * 删除笔记
 */
export function deleteNote(id: number) {
  return request({
    url: `/api/v1/student/ai-tutor/note/${id}`,
    method: 'delete'
  })
}

/**
 * 更新笔记
 */
export function updateNote(id: number, data: {
  title: string
  content: string
  tag?: string
}) {
  return request({
    url: `/api/v1/student/ai-tutor/note/${id}`,
    method: 'put',
    data
  })
}

