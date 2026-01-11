<template>
  <el-card class="question-display" shadow="hover">
    <template #header>
      <div class="question-header">
        <div class="header-left">
          <el-tag :type="getQuestionTypeTag(question.questionType)" size="large">
            {{ getQuestionTypeName(question.questionType) }}
          </el-tag>
          <el-tag type="warning" size="large" v-if="question.difficulty">
            {{ getDifficultyName(question.difficulty) }}
          </el-tag>
          <el-tag type="info" size="large" v-if="question.knowledgePoint">
            {{ question.knowledgePoint }}
          </el-tag>
        </div>
        <div class="header-right">
          <span class="question-number">第 {{ questionNumber }} 题</span>
        </div>
      </div>
    </template>

    <div class="question-body">
      <!-- 题目内容 -->
      <div class="question-content">
        <div class="content-text" v-html="formatContent(question.content)"></div>
      </div>

      <!-- 题目图片 -->
      <div v-if="question.imageUrl" class="question-image">
        <el-image
          :src="question.imageUrl"
          :preview-src-list="[question.imageUrl]"
          fit="contain"
          style="max-width: 100%; max-height: 400px;"
        >
          <template #error>
            <div class="image-error">
              <el-icon :size="48"><Picture /></el-icon>
              <span>图片加载失败</span>
            </div>
          </template>
        </el-image>
      </div>

      <!-- 选项（单选/多选） -->
      <div
        v-if="question.questionType === 1 || question.questionType === 2"
        class="question-options"
      >
        <div
          v-for="(option, index) in parsedOptions"
          :key="index"
          :class="['option-item', getOptionClass(index)]"
        >
          <div class="option-label">{{ String.fromCharCode(65 + index) }}</div>
          <div class="option-content" v-html="formatContent(option)"></div>
          <div v-if="showAnswer && isCorrectOption(index)" class="correct-mark">
            <el-icon color="#67c23a" :size="20"><Check /></el-icon>
          </div>
        </div>
      </div>

      <!-- 答案和解析（可选显示） -->
      <transition name="slide-fade">
        <div v-if="showAnswer" class="answer-section">
          <el-divider />

          <div class="answer-block">
            <div class="answer-header">
              <el-icon color="#67c23a" :size="18"><CircleCheck /></el-icon>
              <span class="answer-label">正确答案</span>
            </div>
            <div class="answer-content">{{ question.answer }}</div>
          </div>

          <div v-if="question.description" class="analysis-block">
            <div class="analysis-header">
              <el-icon color="#409eff" :size="18"><Reading /></el-icon>
              <span class="analysis-label">题目解析</span>
            </div>
            <div class="analysis-content" v-html="formatContent(question.description)"></div>
          </div>

          <div v-if="question.knowledgePoints && question.knowledgePoints.length" class="knowledge-block">
            <div class="knowledge-header">
              <el-icon color="#e6a23c" :size="18"><Notebook /></el-icon>
              <span class="knowledge-label">知识点</span>
            </div>
            <div class="knowledge-tags">
              <el-tag
                v-for="(kp, idx) in question.knowledgePoints"
                :key="idx"
                type="warning"
                effect="plain"
                size="small"
              >
                {{ kp }}
              </el-tag>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- 底部操作栏 -->
    <template #footer v-if="showFooter">
      <div class="question-footer">
        <el-button
          v-if="!showAnswer"
          :icon="View"
          @click="toggleAnswer"
          size="small"
        >
          查看答案
        </el-button>
        <el-button
          v-else
          :icon="Hide"
          @click="toggleAnswer"
          size="small"
        >
          隐藏答案
        </el-button>

        <div class="footer-right">
          <slot name="footer-actions"></slot>
        </div>
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Picture, Check, CircleCheck, Reading, Notebook, View, Hide } from '@element-plus/icons-vue'
import type { Question } from '@/types/practice'

interface Props {
  question: Question
  questionNumber?: number
  showAnswerByDefault?: boolean
  showFooter?: boolean
  highlightCorrect?: boolean
  userAnswer?: string | string[]
}

const props = withDefaults(defineProps<Props>(), {
  questionNumber: 1,
  showAnswerByDefault: false,
  showFooter: true,
  highlightCorrect: false
})

const showAnswer = ref(props.showAnswerByDefault)

/**
 * 解析选项
 */
const parsedOptions = computed(() => {
  if (!props.question.options) return []

  if (typeof props.question.options === 'string') {
    try {
      return JSON.parse(props.question.options)
    } catch {
      return []
    }
  }

  return props.question.options
})

/**
 * 获取题型名称
 */
const getQuestionTypeName = (type: number): string => {
  const types: Record<number, string> = {
    1: '单选题',
    2: '多选题',
    3: '填空题',
    4: '判断题',
    5: '主观题',
    6: '计算题'
  }
  return types[type] || '未知题型'
}

/**
 * 获取题型标签类型
 */
const getQuestionTypeTag = (type: number): string => {
  const tags: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'danger',
    5: 'info',
    6: ''
  }
  return tags[type] || ''
}

/**
 * 获取难度名称
 */
const getDifficultyName = (difficulty: number): string => {
  const levels = ['', '简单', '较简单', '中等', '较难', '困难']
  return levels[Math.min(difficulty, levels.length - 1)] || '中等'
}

/**
 * 格式化内容（支持简单的HTML）
 */
const formatContent = (content: string): string => {
  if (!content) return ''

  // 转换换行符
  let formatted = content.replace(/\n/g, '<br>')

  // 转换LaTeX公式（简单处理）
  formatted = formatted.replace(/\$\$(.+?)\$\$/g, '<span class="formula">$1</span>')
  formatted = formatted.replace(/\$(.+?)\$/g, '<span class="inline-formula">$1</span>')

  return formatted
}

/**
 * 切换答案显示
 */
const toggleAnswer = () => {
  showAnswer.value = !showAnswer.value
}

/**
 * 获取选项样式类
 */
const getOptionClass = (index: number): string => {
  if (!props.highlightCorrect || !showAnswer.value) return ''

  const optionLetter = String.fromCharCode(65 + index)
  const correctAnswer = props.question.answer.toUpperCase()

  if (correctAnswer.includes(optionLetter)) {
    return 'correct'
  }

  // 如果有用户答案，标记错误选项
  if (props.userAnswer) {
    const userAnswerStr = Array.isArray(props.userAnswer)
      ? props.userAnswer.join('').toUpperCase()
      : props.userAnswer.toUpperCase()

    if (userAnswerStr.includes(optionLetter) && !correctAnswer.includes(optionLetter)) {
      return 'wrong'
    }
  }

  return ''
}

/**
 * 判断是否为正确选项
 */
const isCorrectOption = (index: number): boolean => {
  if (!showAnswer.value) return false

  const optionLetter = String.fromCharCode(65 + index)
  const correctAnswer = props.question.answer.toUpperCase()

  return correctAnswer.includes(optionLetter)
}
</script>

<style scoped lang="scss">
.question-display {
  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .header-right {
      .question-number {
        font-size: 16px;
        font-weight: 600;
        color: #409eff;
      }
    }
  }

  .question-body {
    .question-content {
      margin-bottom: 20px;

      .content-text {
        font-size: 16px;
        line-height: 1.8;
        color: #303133;

        :deep(br) {
          margin: 8px 0;
        }

        :deep(.formula) {
          font-family: 'Times New Roman', serif;
          font-style: italic;
          color: #606266;
        }

        :deep(.inline-formula) {
          font-family: 'Times New Roman', serif;
          font-style: italic;
          color: #606266;
          padding: 0 4px;
        }
      }
    }

    .question-image {
      margin: 20px 0;
      text-align: center;

      .image-error {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 40px;
        color: #909399;
      }
    }

    .question-options {
      margin: 20px 0;

      .option-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 16px;
        margin-bottom: 12px;
        background: #f5f7fa;
        border: 2px solid #e4e7ed;
        border-radius: 8px;
        transition: all 0.3s;
        position: relative;

        &:hover {
          background: #ecf5ff;
          border-color: #b3d8ff;
        }

        &.correct {
          background: #f0f9ff;
          border-color: #67c23a;

          .option-label {
            background: #67c23a;
            color: white;
          }
        }

        &.wrong {
          background: #fef0f0;
          border-color: #f56c6c;

          .option-label {
            background: #f56c6c;
            color: white;
          }
        }

        .option-label {
          flex-shrink: 0;
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: #409eff;
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          font-size: 14px;
        }

        .option-content {
          flex: 1;
          font-size: 15px;
          line-height: 1.6;
          color: #606266;
        }

        .correct-mark {
          position: absolute;
          top: 8px;
          right: 8px;
        }
      }
    }

    .answer-section {
      margin-top: 24px;

      .answer-block,
      .analysis-block,
      .knowledge-block {
        margin-bottom: 20px;

        .answer-header,
        .analysis-header,
        .knowledge-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;
          font-weight: 600;
          font-size: 15px;
        }

        .answer-content {
          padding: 12px;
          background: #f0f9ff;
          border-left: 3px solid #67c23a;
          border-radius: 4px;
          font-size: 16px;
          font-weight: 600;
          color: #67c23a;
        }

        .analysis-content {
          padding: 16px;
          background: #f5f7fa;
          border-radius: 8px;
          font-size: 14px;
          line-height: 1.8;
          color: #606266;
        }

        .knowledge-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }
      }
    }
  }

  .question-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .footer-right {
      display: flex;
      gap: 8px;
    }
  }
}

/* 动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .question-display {
    .question-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    .question-body {
      .question-content .content-text {
        font-size: 15px;
      }

      .question-options .option-item {
        padding: 12px;

        .option-label {
          width: 28px;
          height: 28px;
          font-size: 13px;
        }

        .option-content {
          font-size: 14px;
        }
      }
    }
  }
}
</style>
