<template>
  <el-card class="ai-grading-panel" shadow="hover">
    <template #header>
      <div class="header-flex">
        <div class="header-left">
          <el-icon :size="20" color="#409eff"><ChatDotSquare /></el-icon>
          <span class="header-title">AI智能批改</span>
        </div>
        <el-button
          type="primary"
          size="small"
          :loading="grading"
          :disabled="!canGrade"
          @click="handleRequestGrading"
        >
          <el-icon v-if="!grading"><MagicStick /></el-icon>
          {{ grading ? '批改中...' : gradingResult ? '重新批改' : '请AI批改' }}
        </el-button>
      </div>
    </template>

    <!-- 未批改状态 -->
    <div v-if="!gradingResult && !grading" class="empty-state">
      <el-empty description="点击按钮开始AI智能批改">
        <template #image>
          <el-icon :size="64" color="#909399"><Document /></el-icon>
        </template>
      </el-empty>
    </div>

    <!-- 批改中状态 -->
    <div v-if="grading" class="grading-state">
      <div class="grading-animation">
        <el-icon class="rotating" :size="48" color="#409eff"><Loading /></el-icon>
        <p class="grading-text">AI正在批改中，请稍候...</p>
      </div>

      <!-- 流式显示分析内容 -->
      <div v-if="streamingAnalysis" class="streaming-content">
        <div class="analysis-header">
          <el-icon color="#409eff"><Reading /></el-icon>
          <span>实时分析</span>
        </div>
        <div class="analysis-text" v-html="renderMarkdown(streamingAnalysis)"></div>
      </div>
    </div>

    <!-- 批改结果 -->
    <div v-if="gradingResult && !grading" class="grading-result">
      <!-- 评分显示 -->
      <div class="score-section">
        <div class="score-display">
          <el-progress
            type="circle"
            :percentage="gradingResult.score"
            :width="100"
            :stroke-width="10"
            :color="getScoreColor(gradingResult.score)"
          >
            <template #default="{ percentage }">
              <div class="percentage-content">
                <span class="percentage-value">{{ percentage }}</span>
                <span class="percentage-label">分</span>
              </div>
            </template>
          </el-progress>
          <div class="score-info">
            <div class="score-grade">{{ getScoreGrade(gradingResult.score) }}</div>
            <div class="score-text">{{ getScoreText(gradingResult.score) }}</div>
          </div>
        </div>
      </div>

      <!-- AI分析 -->
      <div class="analysis-section">
        <div class="section-header">
          <el-icon color="#409eff"><Reading /></el-icon>
          <span>详细分析</span>
        </div>
        <div class="analysis-content markdown-body" v-html="renderMarkdown(gradingResult.analysis)"></div>
      </div>

      <!-- 优点和不足 -->
      <el-row :gutter="16" class="feedback-section" v-if="gradingResult.strengths || gradingResult.weaknesses">
        <el-col :span="12" v-if="gradingResult.strengths">
          <div class="feedback-card strengths">
            <div class="feedback-header">
              <el-icon color="#67c23a"><Select /></el-icon>
              <span>答案优点</span>
            </div>
            <ul class="feedback-list">
              <li v-for="(item, index) in gradingResult.strengths" :key="index">
                <el-icon color="#67c23a"><Check /></el-icon>
                <span>{{ item }}</span>
              </li>
            </ul>
          </div>
        </el-col>

        <el-col :span="12" v-if="gradingResult.weaknesses">
          <div class="feedback-card weaknesses">
            <div class="feedback-header">
              <el-icon color="#f56c6c"><CloseBold /></el-icon>
              <span>需要改进</span>
            </div>
            <ul class="feedback-list">
              <li v-for="(item, index) in gradingResult.weaknesses" :key="index">
                <el-icon color="#f56c6c"><Close /></el-icon>
                <span>{{ item }}</span>
              </li>
            </ul>
          </div>
        </el-col>
      </el-row>

      <!-- 改进建议 -->
      <div v-if="gradingResult.suggestions" class="suggestions-section">
        <div class="section-header">
          <el-icon color="#e6a23c"><Opportunity /></el-icon>
          <span>改进建议</span>
        </div>
        <div class="suggestions-list">
          <div
            v-for="(suggestion, index) in gradingResult.suggestions"
            :key="index"
            class="suggestion-item"
          >
            <div class="suggestion-number">{{ index + 1 }}</div>
            <div class="suggestion-text">{{ suggestion }}</div>
          </div>
        </div>
      </div>

      <!-- 知识点 -->
      <div v-if="gradingResult.knowledgePoints" class="knowledge-section">
        <div class="section-header">
          <el-icon color="#409eff"><Notebook /></el-icon>
          <span>涉及知识点</span>
        </div>
        <div class="knowledge-tags">
          <el-tag
            v-for="(kp, index) in gradingResult.knowledgePoints"
            :key="index"
            type="info"
            effect="plain"
            class="knowledge-tag"
          >
            {{ kp }}
          </el-tag>
        </div>
      </div>

      <!-- 批改时间 -->
      <div class="grading-time">
        <el-icon :size="14" color="#909399"><Clock /></el-icon>
        <span>批改时间: {{ formatTime(gradingResult.gradingTime) }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotSquare,
  MagicStick,
  Document,
  Loading,
  Reading,
  Select,
  Check,
  CloseBold,
  Close,
  Opportunity,
  Notebook,
  Clock
} from '@element-plus/icons-vue'
import { marked } from 'marked'
import { useAiGrading } from '../hooks/useAiGrading'
import { useAchievements } from '../hooks/useAchievements'
import { playAchievementSound } from '@/utils/soundEffects'
import type { Question, AiGradingResult } from '@/types/practice'

interface Props {
  question: Question
  userAnswer: string
  correctAnswer: string
  autoGrade?: boolean // 是否自动批改
}

const props = withDefaults(defineProps<Props>(), {
  autoGrade: false
})

const emit = defineEmits<{
  graded: [result: AiGradingResult]
}>()

const {
  grading,
  gradingResults,
  requestAiGrading,
  getGradingResult,
  getScoreColor,
  getScoreGrade,
  canAiGrade
} = useAiGrading()

const { checkAchievements } = useAchievements()

const streamingAnalysis = ref('')
const gradingResult = ref<AiGradingResult | null>(null)

// 是否可以批改
const canGrade = computed(() => {
  return canAiGrade(props.question.questionType) && props.userAnswer.trim() !== ''
})

/**
 * 请求AI批改
 */
const handleRequestGrading = async () => {
  if (!canGrade.value) {
    ElMessage.warning('该题型暂不支持AI批改')
    return
  }

  streamingAnalysis.value = ''
  gradingResult.value = null

  // 调用AI批改
  const result = await requestAiGrading(props.question, props.userAnswer)

  if (result) {
    gradingResult.value = result
    emit('graded', result)

    // 检查成就
    checkAchievements({
      questionId: props.question.id,
      isCorrect: result.score >= 60
    })

    ElMessage.success('批改完成！')
  }
}

/**
 * 渲染Markdown
 */
const renderMarkdown = (text: string): string => {
  if (!text) return ''
  return marked(text)
}

/**
 * 获取评分文字
 */
const getScoreText = (score: number): string => {
  if (score >= 90) return '表现出色，继续保持！'
  if (score >= 80) return '做得不错，再接再厉！'
  if (score >= 70) return '基本掌握，还需加强！'
  if (score >= 60) return '刚刚及格，需要努力！'
  return '需要加倍努力，加油！'
}

/**
 * 格式化时间
 */
const formatTime = (date: Date): string => {
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 自动加载已有批改结果
const loadExistingResult = () => {
  const existing = getGradingResult(props.question.id)
  if (existing) {
    gradingResult.value = existing
  }
}

// 自动批改
if (props.autoGrade && canGrade.value) {
  handleRequestGrading()
} else {
  loadExistingResult()
}
</script>

<style scoped lang="scss">
.ai-grading-panel {
  .header-flex {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .header-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .empty-state {
    padding: 40px 20px;
    text-align: center;
  }

  .grading-state {
    padding: 20px;

    .grading-animation {
      text-align: center;
      margin-bottom: 30px;

      .rotating {
        animation: rotate 2s linear infinite;
      }

      @keyframes rotate {
        from {
          transform: rotate(0deg);
        }
        to {
          transform: rotate(360deg);
        }
      }

      .grading-text {
        margin-top: 16px;
        font-size: 14px;
        color: #606266;
      }
    }

    .streaming-content {
      background: #f5f7fa;
      border-radius: 8px;
      padding: 16px;

      .analysis-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        font-weight: 600;
        color: #409eff;
      }

      .analysis-text {
        color: #606266;
        line-height: 1.8;
        font-size: 14px;
      }
    }
  }

  .grading-result {
    .score-section {
      padding: 24px;
      background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
      border-radius: 12px;
      margin-bottom: 20px;

      .score-display {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 32px;

        .percentage-content {
          display: flex;
          flex-direction: column;
          align-items: center;

          .percentage-value {
            font-size: 32px;
            font-weight: bold;
            line-height: 1;
          }

          .percentage-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }

        .score-info {
          text-align: left;

          .score-grade {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 8px;
          }

          .score-text {
            font-size: 14px;
            color: #606266;
          }
        }
      }
    }

    .analysis-section,
    .suggestions-section,
    .knowledge-section {
      margin-bottom: 20px;

      .section-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
      }

      .analysis-content {
        background: #f5f7fa;
        border-radius: 8px;
        padding: 16px;
        color: #606266;
        line-height: 1.8;
        font-size: 14px;
      }
    }

    .feedback-section {
      margin-bottom: 20px;

      .feedback-card {
        background: #f5f7fa;
        border-radius: 8px;
        padding: 16px;

        .feedback-header {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 600;
          margin-bottom: 12px;
        }

        .feedback-list {
          list-style: none;
          padding: 0;
          margin: 0;

          li {
            display: flex;
            align-items: flex-start;
            gap: 8px;
            margin-bottom: 8px;
            font-size: 14px;
            color: #606266;

            &:last-child {
              margin-bottom: 0;
            }
          }
        }

        &.strengths {
          background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ed 100%);

          .feedback-header {
            color: #67c23a;
          }
        }

        &.weaknesses {
          background: linear-gradient(135deg, #fef0f0 0%, #fff 100%);

          .feedback-header {
            color: #f56c6c;
          }
        }
      }
    }

    .suggestions-list {
      .suggestion-item {
        display: flex;
        gap: 12px;
        padding: 12px;
        background: #f5f7fa;
        border-radius: 8px;
        margin-bottom: 8px;

        .suggestion-number {
          flex-shrink: 0;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #e6a23c;
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          font-weight: bold;
        }

        .suggestion-text {
          flex: 1;
          font-size: 14px;
          color: #606266;
          line-height: 1.6;
        }
      }
    }

    .knowledge-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .knowledge-tag {
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: #409eff;
          color: white;
          border-color: #409eff;
        }
      }
    }

    .grading-time {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #909399;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px dashed #dcdfe6;
    }
  }
}

/* Markdown样式 */
.markdown-body {
  :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
    margin-top: 16px;
    margin-bottom: 8px;
    font-weight: 600;
    color: #303133;
  }

  :deep(p) {
    margin: 8px 0;
  }

  :deep(ul), :deep(ol) {
    padding-left: 24px;
    margin: 8px 0;
  }

  :deep(code) {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 13px;
  }

  :deep(pre) {
    background: #f5f7fa;
    padding: 12px;
    border-radius: 6px;
    overflow-x: auto;
    margin: 12px 0;

    code {
      background: none;
      padding: 0;
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .ai-grading-panel {
    .grading-result {
      .score-section .score-display {
        flex-direction: column;
        gap: 20px;

        .score-info {
          text-align: center;
        }
      }

      .feedback-section {
        :deep(.el-col) {
          width: 100%;
          margin-bottom: 12px;
        }
      }
    }
  }
}
</style>
