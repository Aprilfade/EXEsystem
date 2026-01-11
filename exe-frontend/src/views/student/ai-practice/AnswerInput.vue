<template>
  <el-card class="answer-input" shadow="hover">
    <template #header>
      <div class="header-flex">
        <div class="header-left">
          <el-icon :size="20" color="#409eff"><Edit /></el-icon>
          <span class="header-title">我的答案</span>
        </div>
        <div class="header-right" v-if="timeSpent > 0">
          <el-icon :size="16" color="#909399"><Clock /></el-icon>
          <span class="time-text">{{ formatTime(timeSpent) }}</span>
        </div>
      </div>
    </template>

    <div class="input-area">
      <!-- 单选题 -->
      <div v-if="questionType === 1" class="single-choice">
        <el-radio-group v-model="localAnswer" @change="handleChange" size="large">
          <el-radio
            v-for="(option, index) in options"
            :key="index"
            :label="String.fromCharCode(65 + index)"
            border
            class="option-radio"
          >
            <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
            <span class="option-text">{{ option }}</span>
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 多选题 -->
      <div v-if="questionType === 2" class="multiple-choice">
        <el-checkbox-group v-model="localAnswerArray" @change="handleMultipleChange">
          <el-checkbox
            v-for="(option, index) in options"
            :key="index"
            :label="String.fromCharCode(65 + index)"
            border
            size="large"
            class="option-checkbox"
          >
            <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
            <span class="option-text">{{ option }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- 填空题 -->
      <div v-if="questionType === 3" class="blank-answer">
        <el-input
          v-model="localAnswer"
          @input="handleChange"
          placeholder="请输入答案"
          size="large"
          clearable
        >
          <template #prefix>
            <el-icon><EditPen /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 判断题 -->
      <div v-if="questionType === 4" class="judge-answer">
        <el-radio-group v-model="localAnswer" @change="handleChange" size="large">
          <el-radio label="T" border class="judge-option correct-option">
            <el-icon :size="24"><Select /></el-icon>
            <span>正确</span>
          </el-radio>
          <el-radio label="F" border class="judge-option wrong-option">
            <el-icon :size="24"><CloseBold /></el-icon>
            <span>错误</span>
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 主观题/计算题 -->
      <div v-if="questionType === 5 || questionType === 6" class="subjective-answer">
        <el-input
          v-model="localAnswer"
          @input="handleChange"
          type="textarea"
          :rows="8"
          placeholder="请输入你的答案..."
          show-word-limit
          :maxlength="2000"
        />

        <!-- 工具栏 -->
        <div class="toolbar">
          <el-button :icon="Refresh" size="small" @click="clearAnswer">清空</el-button>
          <span class="word-count">{{ localAnswer.length }} / 2000</span>
        </div>
      </div>

      <!-- 答案提示 -->
      <div v-if="showHint && hint" class="answer-hint">
        <el-alert type="info" :closable="false">
          <template #title>
            <div class="hint-content">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ hint }}</span>
            </div>
          </template>
        </el-alert>
      </div>
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="footer-actions">
        <div class="action-left">
          <el-button
            v-if="!submitted"
            :icon="Document"
            @click="showHint = !showHint"
            size="small"
          >
            {{ showHint ? '隐藏提示' : '显示提示' }}
          </el-button>
        </div>

        <div class="action-right">
          <el-button @click="handleClear" size="small" v-if="!submitted">
            清空答案
          </el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :disabled="!canSubmit"
            :loading="submitting"
            size="small"
          >
            <el-icon v-if="!submitting"><CircleCheck /></el-icon>
            {{ submitting ? '提交中...' : submitted ? '已提交' : '提交答案' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import {
  Edit,
  Clock,
  EditPen,
  Select,
  CloseBold,
  Refresh,
  InfoFilled,
  Document,
  CircleCheck
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { playCorrectSound, playWrongSound } from '@/utils/soundEffects'

interface Props {
  questionType: number
  options?: string[]
  modelValue?: string | string[]
  submitted?: boolean
  submitting?: boolean
  timeSpent?: number
  hint?: string
  correctAnswer?: string
}

const props = withDefaults(defineProps<Props>(), {
  submitted: false,
  submitting: false,
  timeSpent: 0
})

const emit = defineEmits<{
  'update:modelValue': [value: string | string[]]
  submit: [answer: string | string[]]
  clear: []
}>()

const localAnswer = ref<string>('')
const localAnswerArray = ref<string[]>([])
const showHint = ref(false)

// 初始化答案
watch(
  () => props.modelValue,
  (newVal) => {
    if (Array.isArray(newVal)) {
      localAnswerArray.value = [...newVal]
    } else {
      localAnswer.value = newVal || ''
    }
  },
  { immediate: true }
)

/**
 * 是否可以提交
 */
const canSubmit = computed(() => {
  if (props.submitted) return false

  if (props.questionType === 2) {
    return localAnswerArray.value.length > 0
  }

  return localAnswer.value.trim() !== ''
})

/**
 * 处理单选/填空/判断/主观题答案变化
 */
const handleChange = () => {
  emit('update:modelValue', localAnswer.value)
}

/**
 * 处理多选题答案变化
 */
const handleMultipleChange = () => {
  // 多选题答案按字母排序
  const sorted = [...localAnswerArray.value].sort()
  emit('update:modelValue', sorted)
}

/**
 * 清空答案
 */
const handleClear = () => {
  if (props.questionType === 2) {
    localAnswerArray.value = []
  } else {
    localAnswer.value = ''
  }
  emit('clear')
  ElMessage.info('已清空答案')
}

/**
 * 清空主观题答案
 */
const clearAnswer = () => {
  localAnswer.value = ''
  emit('update:modelValue', '')
}

/**
 * 提交答案
 */
const handleSubmit = () => {
  if (!canSubmit.value) {
    ElMessage.warning('请先完成答题')
    return
  }

  const answer = props.questionType === 2 ? localAnswerArray.value : localAnswer.value

  emit('submit', answer)
}

/**
 * 格式化时间
 */
const formatTime = (seconds: number): string => {
  if (seconds < 60) {
    return `${seconds}秒`
  }

  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes}分${secs}秒`
}
</script>

<style scoped lang="scss">
.answer-input {
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

    .header-right {
      display: flex;
      align-items: center;
      gap: 6px;

      .time-text {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .input-area {
    .single-choice,
    .multiple-choice {
      .option-radio,
      .option-checkbox {
        display: flex;
        width: 100%;
        margin-bottom: 12px;
        padding: 16px;
        align-items: center;

        &:last-child {
          margin-bottom: 0;
        }

        :deep(.el-radio__label),
        :deep(.el-checkbox__label) {
          display: flex;
          align-items: center;
          width: 100%;
          gap: 8px;

          .option-label {
            font-weight: 600;
            color: #409eff;
          }

          .option-text {
            flex: 1;
            color: #606266;
          }
        }
      }

      .option-radio:hover,
      .option-checkbox:hover {
        background: #ecf5ff;
      }
    }

    .blank-answer {
      :deep(.el-input__inner) {
        font-size: 16px;
      }
    }

    .judge-answer {
      display: flex;
      justify-content: center;
      gap: 24px;

      .judge-option {
        min-width: 150px;
        padding: 24px;

        :deep(.el-radio__label) {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 8px;
          font-size: 16px;
          font-weight: 600;
        }

        &.correct-option {
          :deep(.el-radio__label) {
            color: #67c23a;
          }
        }

        &.wrong-option {
          :deep(.el-radio__label) {
            color: #f56c6c;
          }
        }
      }
    }

    .subjective-answer {
      .toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 12px;

        .word-count {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .answer-hint {
      margin-top: 16px;

      .hint-content {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
      }
    }
  }

  .footer-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .action-right {
      display: flex;
      gap: 8px;
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .answer-input {
    .input-area {
      .judge-answer {
        flex-direction: column;
        gap: 12px;

        .judge-option {
          width: 100%;
        }
      }
    }

    .footer-actions {
      flex-direction: column;
      gap: 12px;

      .action-left,
      .action-right {
        width: 100%;
      }

      .action-right {
        justify-content: flex-end;
      }
    }
  }
}
</style>
