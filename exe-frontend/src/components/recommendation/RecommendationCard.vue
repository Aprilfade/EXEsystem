<template>
  <el-card
    class="recommendation-card"
    :class="{ 'is-top': index <= 3 }"
    shadow="hover"
  >
    <!-- 排名标签 -->
    <div v-if="index <= 3" class="rank-badge" :class="`rank-${index}`">
      <span class="rank-number">#{{ index }}</span>
      <el-icon v-if="index === 1"><Trophy /></el-icon>
    </div>

    <!-- 卡片主体 -->
    <div class="card-main">
      <!-- 左侧：内容信息 -->
      <div class="content-info">
        <h4 class="item-title">
          {{ recommendation.itemTitle }}
          <el-tag
            v-if="recommendation.isNew"
            size="small"
            type="danger"
            effect="dark"
          >
            新
          </el-tag>
        </h4>

        <div class="item-meta">
          <el-tag size="small" type="info">
            {{ getItemTypeLabel(recommendation.itemType) }}
          </el-tag>

          <el-tag v-if="recommendation.subject" size="small">
            {{ recommendation.subject }}
          </el-tag>

          <el-rate
            v-model="recommendation.difficulty"
            disabled
            :max="5"
            size="small"
            show-score
            score-template="{value}级难度"
          />
        </div>

        <!-- 标签 -->
        <div v-if="recommendation.tags?.length" class="item-tags">
          <el-tag
            v-for="tag in recommendation.tags.slice(0, 3)"
            :key="tag"
            size="small"
            effect="plain"
          >
            {{ tag }}
          </el-tag>
        </div>

        <!-- 推荐原因 -->
        <div class="recommendation-reason">
          <div class="reason-header">
            <el-icon color="#409eff"><InfoFilled /></el-icon>
            <span class="reason-label">推荐理由</span>
            <el-tag :type="getReasonType(recommendation.reason)" size="small">
              {{ recommendation.reason }}
            </el-tag>
          </div>

          <!-- 推荐解释 -->
          <div v-if="recommendation.explanation?.length" class="reason-explanation">
            <div
              v-for="(exp, idx) in recommendation.explanation"
              :key="idx"
              class="explanation-item"
            >
              <el-icon><Check /></el-icon>
              <span>{{ exp }}</span>
            </div>
          </div>
        </div>

        <!-- 知识点 -->
        <div v-if="recommendation.knowledgePoints?.length" class="knowledge-points">
          <span class="kp-label">涉及知识点：</span>
          <span class="kp-list">
            {{ recommendation.knowledgePoints.join('、') }}
          </span>
        </div>
      </div>

      <!-- 右侧：推荐指标 -->
      <div class="recommendation-metrics">
        <!-- 置信度仪表盘 -->
        <div class="confidence-gauge">
          <el-progress
            type="circle"
            :percentage="recommendation.confidence"
            :width="80"
            :stroke-width="8"
            :color="getConfidenceColor(recommendation.confidence)"
          >
            <template #default="{ percentage }">
              <div class="percentage-content">
                <span class="percentage-value">{{ percentage }}</span>
                <span class="percentage-label">%</span>
              </div>
            </template>
          </el-progress>
          <div class="gauge-label">置信度</div>
        </div>

        <!-- 推荐分数 -->
        <div class="score-display">
          <div class="score-value">{{ recommendation.score.toFixed(2) }}</div>
          <div class="score-label">推荐分</div>
        </div>

        <!-- 匹配度指示器 -->
        <div class="match-indicator">
          <div class="match-bar">
            <div
              class="match-fill"
              :style="{ width: recommendation.confidence + '%' }"
            />
          </div>
          <div class="match-label">匹配度</div>
        </div>
      </div>
    </div>

    <!-- 卡片底部：操作按钮 -->
    <div class="card-actions">
      <el-button
        type="primary"
        size="small"
        @click.stop="handleAction('start')"
      >
        <el-icon><VideoPlay /></el-icon>
        开始学习
      </el-button>

      <el-button
        size="small"
        @click.stop="handleAction('collect')"
      >
        <el-icon><Star /></el-icon>
        收藏
      </el-button>

      <el-button
        size="small"
        @click.stop="handleViewDetail"
      >
        <el-icon><View /></el-icon>
        查看详情
      </el-button>

      <el-dropdown @command="handleMoreAction">
        <el-button size="small">
          更多
          <el-icon><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="not-interested">
              <el-icon><CloseBold /></el-icon>
              不感兴趣
            </el-dropdown-item>
            <el-dropdown-item command="share">
              <el-icon><Share /></el-icon>
              分享
            </el-dropdown-item>
            <el-dropdown-item command="report">
              <el-icon><Warning /></el-icon>
              反馈问题
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 推荐详情对话框 -->
    <el-dialog
      v-model="showDetail"
      :title="recommendation.itemTitle"
      width="700px"
    >
      <recommendation-detail :recommendation="recommendation" />
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Trophy,
  InfoFilled,
  Check,
  VideoPlay,
  Star,
  View,
  ArrowDown,
  CloseBold,
  Share,
  Warning
} from '@element-plus/icons-vue'
import RecommendationDetail from './RecommendationDetail.vue'
import type { RecommendationResult } from '@/api/recommendation'
import { recordClick } from '@/api/recommendation'

interface Props {
  recommendation: RecommendationResult
  index: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  click: []
  action: [action: string]
}>()

const router = useRouter()
const showDetail = ref(false)

function handleClick() {
  emit('click')
  // ✅ 修复：点击卡片不再自动触发"开始学习"
  // 用户应该明确点击"开始学习"按钮
}

function handleViewDetail() {
  // ✅ 修复：只打开详情对话框，不触发其他操作
  showDetail.value = true
}

async function handleAction(action: string) {
  // 记录点击行为
  if (props.recommendation.logId && action === 'start') {
    try {
      await recordClick({ logId: props.recommendation.logId })
    } catch (error) {
      console.error('记录点击失败:', error)
    }
  }

  // 处理不同的行为
  switch (action) {
    case 'start':
      startPractice()
      break
    case 'collect':
      collectItem()
      break
    default:
      emit('action', action)
  }
}

function startPractice() {
  const { itemType, itemId, itemTitle } = props.recommendation

  // 调试日志
  console.log('推荐跳转信息:', { itemType, itemId, itemTitle })

  // 统一转换为小写，避免大小写不匹配
  const normalizedType = itemType?.toLowerCase()

  // 根据不同的类型跳转到不同的页面
  switch (normalizedType) {
    case 'question':
      // 跳转到练习页面，传递题目ID作为query参数
      console.log('跳转到题目练习页面')
      router.push({
        name: 'StudentPractice',
        query: {
          questionId: itemId,
          from: 'recommendation'
        }
      })
      ElMessage.success(`正在跳转到题目练习...`)
      break

    case 'course':
      // 跳转到课程列表页
      console.log('跳转到课程列表页')
      router.push({
        name: 'StudentCourseList',
        query: { courseId: itemId }
      })
      ElMessage.success(`正在跳转到课程...`)
      break

    case 'knowledgepoint':
      // 跳转到智能复习页面（包含知识点学习）
      console.log('跳转到智能复习页面')
      router.push({
        name: 'StudentSmartReview',
        query: { knowledgePointId: itemId }
      })
      ElMessage.success(`正在跳转到知识点学习...`)
      break

    default:
      console.error('未知的推荐类型:', itemType)
      ElMessage.warning(`暂不支持此类型的学习: ${itemType}`)
  }
}

function collectItem() {
  ElMessage.success('收藏成功')
  emit('action', 'collect')
}

function handleMoreAction(command: string) {
  switch (command) {
    case 'not-interested':
      ElMessage.info('已标记为不感兴趣')
      break
    case 'share':
      ElMessage.info('分享功能开发中')
      break
    case 'report':
      ElMessage.info('感谢反馈')
      break
  }
  emit('action', command)
}

function getItemTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    question: '题目',
    course: '课程',
    knowledgePoint: '知识点'
  }
  return labels[type] || type
}

function getReasonType(reason: string): string {
  const typeMap: Record<string, string> = {
    '强烈推荐': 'danger',
    '推荐': 'success',
    '建议尝试': 'warning',
    '可以了解': 'info'
  }
  return typeMap[reason] || 'info'
}

function getConfidenceColor(confidence: number): string {
  if (confidence >= 80) return '#67c23a'
  if (confidence >= 60) return '#409eff'
  if (confidence >= 40) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped lang="scss">
.recommendation-card {
  margin-bottom: 16px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  &.is-top {
    border: 2px solid #ffd700;

    .rank-badge {
      position: absolute;
      top: -10px;
      left: -10px;
      width: 50px;
      height: 50px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      color: white;
      z-index: 10;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);

      &.rank-1 {
        background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
        font-size: 20px;
      }

      &.rank-2 {
        background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
        font-size: 18px;
      }

      &.rank-3 {
        background: linear-gradient(135deg, #cd7f32 0%, #e5a66c 100%);
        font-size: 16px;
      }

      .rank-number {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .card-main {
    display: flex;
    gap: 24px;
    margin-bottom: 16px;

    .content-info {
      flex: 1;

      .item-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 12px 0;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .item-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        flex-wrap: wrap;
      }

      .item-tags {
        display: flex;
        gap: 6px;
        margin-bottom: 12px;
        flex-wrap: wrap;
      }

      .recommendation-reason {
        background: #f5f7fa;
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 12px;

        .reason-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .reason-label {
            font-weight: 600;
            color: #606266;
          }
        }

        .reason-explanation {
          padding-left: 28px;

          .explanation-item {
            display: flex;
            align-items: center;
            gap: 6px;
            color: #606266;
            font-size: 13px;
            margin-bottom: 4px;

            &:last-child {
              margin-bottom: 0;
            }
          }
        }
      }

      .knowledge-points {
        font-size: 13px;
        color: #909399;

        .kp-label {
          font-weight: 600;
        }

        .kp-list {
          color: #409eff;
        }
      }
    }

    .recommendation-metrics {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
      border-radius: 12px;
      min-width: 140px;

      .confidence-gauge {
        text-align: center;

        .percentage-content {
          display: flex;
          flex-direction: column;
          align-items: center;

          .percentage-value {
            font-size: 20px;
            font-weight: bold;
            line-height: 1;
          }

          .percentage-label {
            font-size: 12px;
            color: #909399;
          }
        }

        .gauge-label {
          margin-top: 8px;
          font-size: 12px;
          color: #606266;
        }
      }

      .score-display {
        text-align: center;

        .score-value {
          font-size: 24px;
          font-weight: bold;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }

        .score-label {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }

      .match-indicator {
        width: 100%;

        .match-bar {
          height: 8px;
          background: #e4e7ed;
          border-radius: 4px;
          overflow: hidden;

          .match-fill {
            height: 100%;
            background: linear-gradient(90deg, #409eff 0%, #67c23a 100%);
            transition: width 0.3s ease;
          }
        }

        .match-label {
          text-align: center;
          font-size: 11px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-start;
    padding-top: 12px;
    border-top: 1px solid #e4e7ed;
  }
}

@media (max-width: 768px) {
  .recommendation-card {
    .card-main {
      flex-direction: column;

      .recommendation-metrics {
        flex-direction: row;
        justify-content: space-around;
        width: 100%;
      }
    }

    .card-actions {
      flex-wrap: wrap;

      button {
        flex: 1;
        min-width: 100px;
      }
    }
  }
}
</style>
