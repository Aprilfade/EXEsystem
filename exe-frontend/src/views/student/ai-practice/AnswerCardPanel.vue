<template>
  <el-card class="answer-card-panel" shadow="hover">
    <template #header>
      <div class="header-flex">
        <span class="header-title">答题卡</span>
        <el-tag :type="getProgressType()">
          {{ answeredCount }} / {{ totalCount }}
        </el-tag>
      </div>
    </template>

    <div class="card-body">
      <!-- 进度条 -->
      <div class="progress-section">
        <el-progress
          :percentage="progressPercentage"
          :color="getProgressColor()"
          :stroke-width="12"
        >
          <template #default="{ percentage }">
            <span class="progress-text">{{ percentage }}%</span>
          </template>
        </el-progress>
      </div>

      <!-- 题号网格 -->
      <div class="question-grid">
        <div
          v-for="(question, index) in questions"
          :key="question.id"
          :class="['question-item', getQuestionClass(question.id, index)]"
          @click="handleQuestionClick(index)"
        >
          <span class="question-number">{{ index + 1 }}</span>
          <el-icon v-if="getQuestionStatus(question.id) === 'correct'" class="status-icon" color="#67c23a">
            <CircleCheck />
          </el-icon>
          <el-icon v-else-if="getQuestionStatus(question.id) === 'wrong'" class="status-icon" color="#f56c6c">
            <CircleClose />
          </el-icon>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <div class="stat-item">
          <div class="stat-icon answered">
            <el-icon :size="16"><Edit /></el-icon>
          </div>
          <span class="stat-label">已答</span>
          <span class="stat-value">{{ answeredCount }}</span>
        </div>

        <div class="stat-item">
          <div class="stat-icon correct">
            <el-icon :size="16"><CircleCheck /></el-icon>
          </div>
          <span class="stat-label">答对</span>
          <span class="stat-value">{{ correctCount }}</span>
        </div>

        <div class="stat-item">
          <div class="stat-icon wrong">
            <el-icon :size="16"><CircleClose /></el-icon>
          </div>
          <span class="stat-label">答错</span>
          <span class="stat-value">{{ wrongCount }}</span>
        </div>

        <div class="stat-item">
          <div class="stat-icon unanswered">
            <el-icon :size="16"><QuestionFilled /></el-icon>
          </div>
          <span class="stat-label">未答</span>
          <span class="stat-value">{{ unansweredCount }}</span>
        </div>
      </div>

      <!-- 图例说明 -->
      <div class="legend-section">
        <div class="legend-item">
          <div class="legend-box current"></div>
          <span>当前题</span>
        </div>
        <div class="legend-item">
          <div class="legend-box answered"></div>
          <span>已答题</span>
        </div>
        <div class="legend-item">
          <div class="legend-box unanswered"></div>
          <span>未答题</span>
        </div>
      </div>
    </div>

    <!-- 底部操作 -->
    <template #footer v-if="showActions">
      <div class="footer-actions">
        <el-button :icon="RefreshLeft" @click="handleReset" size="small">
          重新开始
        </el-button>
        <el-button type="primary" :icon="Check" @click="handleSubmitAll" size="small">
          提交全部
        </el-button>
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Edit,
  CircleCheck,
  CircleClose,
  QuestionFilled,
  RefreshLeft,
  Check
} from '@element-plus/icons-vue'
import type { Question, UserAnswer } from '@/types/practice'

interface Props {
  questions: Question[]
  currentIndex: number
  userAnswers: Map<number, UserAnswer>
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showActions: true
})

const emit = defineEmits<{
  'question-click': [index: number]
  'reset': []
  'submit-all': []
}>()

/**
 * 总题数
 */
const totalCount = computed(() => props.questions.length)

/**
 * 已答题数
 */
const answeredCount = computed(() => props.userAnswers.size)

/**
 * 答对题数
 */
const correctCount = computed(() => {
  let count = 0
  props.userAnswers.forEach(answer => {
    if (answer.isCorrect) count++
  })
  return count
})

/**
 * 答错题数
 */
const wrongCount = computed(() => {
  let count = 0
  props.userAnswers.forEach(answer => {
    if (!answer.isCorrect) count++
  })
  return count
})

/**
 * 未答题数
 */
const unansweredCount = computed(() => {
  return totalCount.value - answeredCount.value
})

/**
 * 进度百分比
 */
const progressPercentage = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((answeredCount.value / totalCount.value) * 100)
})

/**
 * 获取进度类型
 */
const getProgressType = () => {
  const percentage = progressPercentage.value
  if (percentage === 100) return 'success'
  if (percentage >= 50) return 'warning'
  return 'info'
}

/**
 * 获取进度条颜色
 */
const getProgressColor = () => {
  const percentage = progressPercentage.value
  if (percentage === 100) return '#67c23a'
  if (percentage >= 75) return '#409eff'
  if (percentage >= 50) return '#e6a23c'
  return '#909399'
}

/**
 * 获取题目样式类
 */
const getQuestionClass = (questionId: number, index: number): string => {
  const classes: string[] = []

  // 当前题
  if (index === props.currentIndex) {
    classes.push('current')
  }

  // 答题状态
  const answer = props.userAnswers.get(questionId)
  if (answer) {
    classes.push('answered')
    if (answer.isCorrect) {
      classes.push('correct')
    } else {
      classes.push('wrong')
    }
  }

  return classes.join(' ')
}

/**
 * 获取题目答题状态
 */
const getQuestionStatus = (questionId: number): 'correct' | 'wrong' | 'unanswered' => {
  const answer = props.userAnswers.get(questionId)
  if (!answer) return 'unanswered'
  return answer.isCorrect ? 'correct' : 'wrong'
}

/**
 * 处理题目点击
 */
const handleQuestionClick = (index: number) => {
  emit('question-click', index)
}

/**
 * 重置
 */
const handleReset = () => {
  emit('reset')
}

/**
 * 提交全部
 */
const handleSubmitAll = () => {
  emit('submit-all')
}
</script>

<style scoped lang="scss">
.answer-card-panel {
  position: sticky;
  top: 20px;

  .header-flex {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .card-body {
    .progress-section {
      margin-bottom: 20px;

      .progress-text {
        font-size: 14px;
        font-weight: 600;
      }
    }

    .question-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 12px;
      margin-bottom: 20px;

      .question-item {
        position: relative;
        aspect-ratio: 1;
        border: 2px solid #dcdfe6;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s;
        background: white;

        .question-number {
          font-size: 16px;
          font-weight: 600;
          color: #606266;
        }

        .status-icon {
          position: absolute;
          top: 2px;
          right: 2px;
        }

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        &.current {
          border-color: #409eff;
          background: #ecf5ff;

          .question-number {
            color: #409eff;
          }
        }

        &.answered {
          background: #f0f9ff;
          border-color: #b3d8ff;
        }

        &.correct {
          background: #f0f9ff;
          border-color: #67c23a;

          .question-number {
            color: #67c23a;
          }
        }

        &.wrong {
          background: #fef0f0;
          border-color: #f56c6c;

          .question-number {
            color: #f56c6c;
          }
        }
      }
    }

    .stats-section {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      margin-bottom: 16px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px;
        background: #f5f7fa;
        border-radius: 6px;

        .stat-icon {
          width: 24px;
          height: 24px;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;

          &.answered {
            background: #409eff;
            color: white;
          }

          &.correct {
            background: #67c23a;
            color: white;
          }

          &.wrong {
            background: #f56c6c;
            color: white;
          }

          &.unanswered {
            background: #909399;
            color: white;
          }
        }

        .stat-label {
          flex: 1;
          font-size: 13px;
          color: #606266;
        }

        .stat-value {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
      }
    }

    .legend-section {
      display: flex;
      justify-content: space-around;
      padding: 12px 0;
      border-top: 1px dashed #dcdfe6;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #606266;

        .legend-box {
          width: 16px;
          height: 16px;
          border-radius: 4px;
          border: 2px solid #dcdfe6;

          &.current {
            background: #ecf5ff;
            border-color: #409eff;
          }

          &.answered {
            background: #f0f9ff;
            border-color: #b3d8ff;
          }

          &.unanswered {
            background: white;
            border-color: #dcdfe6;
          }
        }
      }
    }
  }

  .footer-actions {
    display: flex;
    justify-content: space-between;
    gap: 8px;

    button {
      flex: 1;
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .answer-card-panel {
    position: static;

    .card-body {
      .question-grid {
        grid-template-columns: repeat(4, 1fr);
        gap: 8px;

        .question-item {
          .question-number {
            font-size: 14px;
          }
        }
      }

      .stats-section {
        grid-template-columns: repeat(2, 1fr);
        gap: 8px;

        .stat-item {
          padding: 6px;

          .stat-label {
            font-size: 12px;
          }

          .stat-value {
            font-size: 14px;
          }
        }
      }

      .legend-section {
        flex-wrap: wrap;
        gap: 8px;
      }
    }
  }
}
</style>
