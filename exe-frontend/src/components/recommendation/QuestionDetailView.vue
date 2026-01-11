<template>
  <div class="question-detail-view" v-loading="loading">
    <template v-if="questionData">
      <!-- 题目信息 -->
      <el-card class="question-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="question-type-tag">
              {{ getQuestionTypeLabel(questionData.questionType) }}
            </span>
            <div class="header-actions">
              <el-tag v-if="questionData.difficulty" :type="getDifficultyType(questionData.difficulty)">
                {{ getDifficultyLabel(questionData.difficulty) }}
              </el-tag>
              <el-tag v-if="questionData.subject">{{ questionData.subject }}</el-tag>
            </div>
          </div>
        </template>

        <!-- 题目内容 -->
        <div class="question-content">
          <h3>题目内容：</h3>
          <div class="content-text" v-html="questionData.content || questionData.itemTitle"></div>

          <!-- 题目选项（如果是选择题） -->
          <div v-if="questionData.options && questionData.options.length > 0" class="question-options">
            <div
              v-for="(option, index) in questionData.options"
              :key="index"
              class="option-item"
            >
              <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
              <span class="option-content">{{ option }}</span>
            </div>
          </div>
        </div>

        <!-- 知识点标签 -->
        <div v-if="questionData.knowledgePoints && questionData.knowledgePoints.length > 0" class="knowledge-points">
          <span class="kp-label">涉及知识点：</span>
          <el-tag
            v-for="kp in questionData.knowledgePoints"
            :key="kp"
            size="small"
            effect="plain"
            class="kp-tag"
          >
            {{ kp }}
          </el-tag>
        </div>

        <!-- 答案区域（可折叠） -->
        <el-collapse v-model="activeCollapse" class="answer-collapse">
          <el-collapse-item name="answer">
            <template #title>
              <div class="collapse-title">
                <el-icon><View /></el-icon>
                <span>查看答案与解析</span>
              </div>
            </template>

            <div class="answer-content">
              <div v-if="questionData.correctAnswer" class="answer-section">
                <h4>正确答案：</h4>
                <div class="answer-text">{{ questionData.correctAnswer }}</div>
              </div>

              <div v-if="questionData.analysis" class="analysis-section">
                <h4>题目解析：</h4>
                <div class="analysis-text" v-html="questionData.analysis"></div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="startPractice">
          <el-icon><EditPen /></el-icon>
          开始练习
        </el-button>
        <el-button size="large" @click="collectQuestion">
          <el-icon><Star /></el-icon>
          收藏题目
        </el-button>
        <el-button size="large" @click="shareQuestion">
          <el-icon><Share /></el-icon>
          分享
        </el-button>
      </div>

      <!-- 相似题目推荐 -->
      <el-card v-if="similarQuestions.length > 0" class="similar-questions" shadow="never">
        <template #header>
          <span>相似题目推荐</span>
        </template>
        <div class="similar-list">
          <div
            v-for="similar in similarQuestions"
            :key="similar.id"
            class="similar-item"
            @click="viewQuestion(similar.id)"
          >
            <div class="similar-content">{{ similar.content }}</div>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-card>
    </template>

    <!-- 空状态 -->
    <el-empty v-else description="题目不存在或已被删除" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { View, EditPen, Star, Share, ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { RecommendationResult } from '@/api/recommendation'

interface Props {
  recommendation: RecommendationResult
  questionId?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  startPractice: []
  close: []
}>()

const router = useRouter()
const loading = ref(false)
const activeCollapse = ref<string[]>([])
const questionData = ref<any>(null)
const similarQuestions = ref<any[]>([])

onMounted(() => {
  loadQuestionDetail()
})

async function loadQuestionDetail() {
  loading.value = true
  try {
    // 模拟加载题目详情（实际应从API获取）
    // 这里先使用推荐数据构建详情
    questionData.value = {
      id: props.recommendation.itemId,
      itemTitle: props.recommendation.itemTitle,
      content: props.recommendation.itemTitle,
      questionType: getQuestionTypeFromRecommendation(),
      difficulty: props.recommendation.difficulty,
      subject: props.recommendation.subject,
      knowledgePoints: props.recommendation.knowledgePoints || [],
      tags: props.recommendation.tags || [],

      // 示例数据（实际应从API获取）
      options: generateOptionsIfNeeded(),
      correctAnswer: '示例答案（实际应从后端获取）',
      analysis: '这道题主要考查的知识点是...'
    }

    // 加载相似题目（可选）
    // loadSimilarQuestions()
  } catch (error) {
    console.error('加载题目详情失败:', error)
    ElMessage.error('加载题目详情失败')
  } finally {
    loading.value = false
  }
}

function getQuestionTypeFromRecommendation(): number {
  // 从推荐元数据中获取题目类型
  return props.recommendation.metadata?.questionType || 1
}

function generateOptionsIfNeeded(): string[] | null {
  // 如果是选择题，生成选项（实际应从API获取）
  const questionType = getQuestionTypeFromRecommendation()
  if (questionType === 1 || questionType === 2) { // 单选或多选
    return [
      '选项A的内容',
      '选项B的内容',
      '选项C的内容',
      '选项D的内容'
    ]
  }
  return null
}

function getQuestionTypeLabel(type: number): string {
  const labels: Record<number, string> = {
    1: '单选题',
    2: '多选题',
    3: '判断题',
    4: '填空题',
    5: '简答题',
    6: '计算题',
    7: '编程题'
  }
  return labels[type] || '未知类型'
}

function getDifficultyLabel(difficulty: number): string {
  const labels = ['简单', '较简单', '中等', '较难', '困难']
  return labels[difficulty - 1] || '中等'
}

function getDifficultyType(difficulty: number): string {
  if (difficulty <= 2) return 'success'
  if (difficulty === 3) return 'warning'
  return 'danger'
}

function startPractice() {
  emit('startPractice')
  emit('close')

  // 跳转到练习页面
  router.push({
    name: 'StudentPractice',
    query: {
      questionId: props.recommendation.itemId,
      from: 'recommendation'
    }
  })
  ElMessage.success('正在跳转到题目练习...')
}

function collectQuestion() {
  ElMessage.success('收藏成功')
}

function shareQuestion() {
  ElMessage.info('分享功能开发中')
}

function viewQuestion(id: string) {
  // 跳转到练习页面查看相似题目
  router.push({
    name: 'StudentPractice',
    query: {
      questionId: id,
      from: 'similar-question'
    }
  })
}
</script>

<style scoped lang="scss">
.question-detail-view {
  .question-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .question-type-tag {
        font-size: 16px;
        font-weight: 600;
        color: #409eff;
      }

      .header-actions {
        display: flex;
        gap: 8px;
      }
    }

    .question-content {
      margin-bottom: 20px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
      }

      .content-text {
        font-size: 15px;
        line-height: 1.8;
        color: #606266;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 8px;
        margin-bottom: 16px;
      }

      .question-options {
        padding-left: 20px;

        .option-item {
          display: flex;
          align-items: flex-start;
          margin-bottom: 12px;
          padding: 12px;
          background: #fff;
          border: 1px solid #e4e7ed;
          border-radius: 6px;
          transition: all 0.3s;

          &:hover {
            border-color: #409eff;
            background: #f0f9ff;
          }

          .option-label {
            font-weight: 600;
            color: #409eff;
            margin-right: 12px;
            min-width: 30px;
          }

          .option-content {
            flex: 1;
            color: #606266;
            line-height: 1.6;
          }
        }
      }
    }

    .knowledge-points {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      padding: 16px;
      background: #f0f9ff;
      border-radius: 8px;
      margin-bottom: 20px;

      .kp-label {
        font-weight: 600;
        color: #606266;
      }

      .kp-tag {
        cursor: pointer;

        &:hover {
          background: #409eff;
          color: white;
          border-color: #409eff;
        }
      }
    }

    .answer-collapse {
      border: none;

      .collapse-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        color: #409eff;
      }

      .answer-content {
        padding: 20px;
        background: #f5f7fa;
        border-radius: 8px;

        .answer-section,
        .analysis-section {
          margin-bottom: 20px;

          &:last-child {
            margin-bottom: 0;
          }

          h4 {
            font-size: 15px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 12px;
          }

          .answer-text,
          .analysis-text {
            padding: 12px;
            background: white;
            border-radius: 6px;
            color: #606266;
            line-height: 1.8;
          }
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-bottom: 30px;
  }

  .similar-questions {
    .similar-list {
      .similar-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        margin-bottom: 8px;
        background: #f5f7fa;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: #e6f7ff;
          transform: translateX(4px);
        }

        .similar-content {
          flex: 1;
          color: #606266;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}
</style>
