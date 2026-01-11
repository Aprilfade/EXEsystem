<template>
  <div class="smart-recommendation-panel">
    <!-- 推荐头部 -->
    <div class="recommendation-header">
      <div class="header-left">
        <h3>
          <el-icon><MagicStick /></el-icon>
          智能推荐
        </h3>
        <el-tag v-if="strategyVersion" size="small" type="info">
          策略: {{ strategyVersion }}
        </el-tag>
      </div>
      <div class="header-right">
        <el-button
          size="small"
          :icon="Refresh"
          @click="refreshRecommendations"
          :loading="loading"
        >
          刷新推荐
        </el-button>
        <el-button
          size="small"
          :icon="Setting"
          @click="showSettings = true"
        >
          设置
        </el-button>
      </div>
    </div>

    <!-- 推荐类型切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="题目推荐" name="question">
        <el-icon><Document /></el-icon>
      </el-tab-pane>
      <el-tab-pane label="课程推荐" name="course">
        <el-icon><Reading /></el-icon>
      </el-tab-pane>
      <el-tab-pane label="知识点推荐" name="knowledgePoint">
        <el-icon><Notebook /></el-icon>
      </el-tab-pane>
    </el-tabs>

    <!-- 推荐列表 -->
    <div v-loading="loading" class="recommendation-list">
      <el-empty v-if="recommendations.length === 0 && !loading" description="暂无推荐" />

      <transition-group name="list" tag="div">
        <recommendation-card
          v-for="(item, index) in recommendations"
          :key="item.itemId"
          :recommendation="item"
          :index="index + 1"
          @click="handleItemClick(item)"
          @action="handleItemAction(item, $event)"
        />
      </transition-group>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button
          text
          @click="loadMore"
          :loading="loadingMore"
        >
          加载更多推荐
        </el-button>
      </div>
    </div>

    <!-- 推荐统计 -->
    <div class="recommendation-stats">
      <el-row :gutter="16">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalRecommendations }}</div>
            <div class="stat-label">总推荐数</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-value">{{ (stats.clickRate * 100).toFixed(1) }}%</div>
            <div class="stat-label">点击率</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-value">{{ (stats.completeRate * 100).toFixed(1) }}%</div>
            <div class="stat-label">完成率</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 设置对话框 -->
    <el-dialog
      v-model="showSettings"
      title="推荐设置"
      width="600px"
    >
      <el-form :model="settings" label-width="120px">
        <el-form-item label="推荐数量">
          <el-slider
            v-model="settings.limit"
            :min="5"
            :max="50"
            :step="5"
            show-stops
            show-input
          />
        </el-form-item>

        <el-form-item label="多样性权重">
          <el-slider
            v-model="settings.diversityWeight"
            :min="0"
            :max="1"
            :step="0.1"
            show-stops
            :format-tooltip="(val) => (val * 100) + '%'"
          />
          <div class="form-tip">
            值越大，推荐内容越多样化
          </div>
        </el-form-item>

        <el-form-item label="难度偏好">
          <el-radio-group v-model="settings.difficultyPreference">
            <el-radio label="auto">自动适应</el-radio>
            <el-radio label="easy">简单</el-radio>
            <el-radio label="medium">中等</el-radio>
            <el-radio label="hard">困难</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="是否包含已练习">
          <el-switch v-model="settings.includePracticed" />
        </el-form-item>

        <el-form-item label="推荐策略">
          <el-select v-model="settings.strategyVersion" placeholder="选择策略">
            <el-option label="默认混合策略 (v1.0)" value="v1.0" />
            <el-option label="强化协同过滤 (v2.0)" value="v2.0" />
            <el-option label="强化内容推荐 (v3.0)" value="v3.0" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="applySettings">应用设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  MagicStick,
  Refresh,
  Setting,
  Document,
  Reading,
  Notebook
} from '@element-plus/icons-vue'
import RecommendationCard from './RecommendationCard.vue'
import type { RecommendationResult, RecommendationStats } from '@/api/recommendation'
import { getRecommendations, recordClick, getRecommendationStats } from '@/api/recommendation'

interface Settings {
  limit: number
  diversityWeight: number
  difficultyPreference: 'auto' | 'easy' | 'medium' | 'hard'
  includePracticed: boolean
  strategyVersion: string
}

const activeTab = ref('question')
const loading = ref(false)
const loadingMore = ref(false)
const showSettings = ref(false)

const recommendations = ref<RecommendationResult[]>([])
const page = ref(1)
const hasMore = ref(true)

const settings = reactive<Settings>({
  limit: 10,
  diversityWeight: 0.3,
  difficultyPreference: 'auto',
  includePracticed: false,
  strategyVersion: 'v1.0'
})

const stats = ref<RecommendationStats>({
  totalRecommendations: 0,
  clickedRecommendations: 0,
  completedRecommendations: 0,
  clickRate: 0,
  completeRate: 0,
  avgScore: 0,
  avgStudyDuration: 0
})

const strategyVersion = computed(() => settings.strategyVersion)

onMounted(() => {
  loadRecommendations()
  loadStats()
})

async function loadRecommendations() {
  try {
    loading.value = true
    const res = await getRecommendations({
      itemType: activeTab.value,
      limit: settings.limit,
      strategyVersion: settings.strategyVersion,
      diversityWeight: settings.diversityWeight,
      difficultyPreference: settings.difficultyPreference,
      includePracticed: settings.includePracticed
    })

    if (res.code === 200) {
      recommendations.value = res.data
      hasMore.value = res.data.length >= settings.limit
    }
  } catch (error) {
    ElMessage.error('加载推荐失败')
    console.error('加载推荐失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res = await getRecommendationStats({
      strategyVersion: settings.strategyVersion
    })

    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

async function loadMore() {
  try {
    loadingMore.value = true
    page.value++

    const res = await getRecommendations({
      itemType: activeTab.value,
      limit: settings.limit,
      page: page.value,
      strategyVersion: settings.strategyVersion
    })

    if (res.code === 200) {
      recommendations.value.push(...res.data)
      hasMore.value = res.data.length >= settings.limit
    }
  } catch (error) {
    ElMessage.error('加载更多失败')
  } finally {
    loadingMore.value = false
  }
}

function handleTabChange() {
  page.value = 1
  loadRecommendations()
}

function refreshRecommendations() {
  page.value = 1
  loadRecommendations()
  loadStats()
}

function applySettings() {
  showSettings.value = false
  page.value = 1
  loadRecommendations()
}

async function handleItemClick(item: RecommendationResult) {
  // 记录点击
  if (item.logId) {
    await recordClick({ logId: item.logId })
  }

  // 触发点击事件，由父组件处理跳转
  emit('itemClick', item)
}

function handleItemAction(item: RecommendationResult, action: string) {
  emit('itemAction', { item, action })
}

const emit = defineEmits<{
  itemClick: [item: RecommendationResult]
  itemAction: [payload: { item: RecommendationResult; action: string }]
}>()
</script>

<style scoped lang="scss">
.smart-recommendation-panel {
  .recommendation-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      h3 {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        color: #333;
      }
    }

    .header-right {
      display: flex;
      gap: 8px;
    }
  }

  .recommendation-list {
    min-height: 400px;
    margin: 20px 0;

    .list-enter-active,
    .list-leave-active {
      transition: all 0.3s ease;
    }

    .list-enter-from {
      opacity: 0;
      transform: translateX(-30px);
    }

    .list-leave-to {
      opacity: 0;
      transform: translateX(30px);
    }
  }

  .load-more {
    text-align: center;
    padding: 20px;
  }

  .recommendation-stats {
    margin-top: 30px;
    padding: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    color: white;

    .stat-item {
      text-align: center;

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}
</style>
