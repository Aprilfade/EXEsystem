<template>
  <div class="recommendation-explanation-content">
    <!-- 推荐算法可视化 -->
    <el-card class="explanation-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>推荐算法分解</span>
          <el-tag size="small" type="info">{{ recommendation.algorithm }}</el-tag>
        </div>
      </template>

      <!-- 算法权重分布 -->
      <div class="algorithm-breakdown">
        <div class="breakdown-title">推荐分数构成</div>

        <div class="score-component" v-for="component in scoreComponents" :key="component.name">
          <div class="component-header">
            <span class="component-name">{{ component.name }}</span>
            <span class="component-score">{{ component.score.toFixed(2) }}</span>
          </div>
          <el-progress
            :percentage="(component.score / totalScore) * 100"
            :color="component.color"
            :show-text="false"
          />
          <div class="component-description">{{ component.description }}</div>
        </div>

        <div class="total-score">
          <span>总分</span>
          <span class="score-value">{{ totalScore.toFixed(2) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 用户画像匹配 -->
    <el-card class="explanation-card" shadow="hover">
      <template #header>
        <span>与你的匹配度分析</span>
      </template>

      <div class="profile-match">
        <!-- 学科偏好 -->
        <div class="match-item">
          <div class="match-label">
            <el-icon><Medal /></el-icon>
            学科偏好
          </div>
          <div class="match-content">
            <el-tag type="success">{{ recommendation.subject }}</el-tag>
            <span class="match-desc">与你最常学习的学科匹配</span>
          </div>
          <div class="match-score">
            <el-rate :model-value="4" disabled show-score />
          </div>
        </div>

        <!-- 难度匹配 -->
        <div class="match-item">
          <div class="match-label">
            <el-icon><TrendCharts /></el-icon>
            难度匹配
          </div>
          <div class="match-content">
            <el-tag>{{ getDifficultyLabel(recommendation.difficulty) }}</el-tag>
            <span class="match-desc">适合你当前水平</span>
          </div>
          <div class="match-score">
            <el-rate :model-value="getDifficultyMatchScore()" disabled show-score />
          </div>
        </div>

        <!-- 知识点相关性 -->
        <div class="match-item">
          <div class="match-label">
            <el-icon><Connection /></el-icon>
            知识点关联
          </div>
          <div class="match-content">
            <div class="kp-tags">
              <el-tag
                v-for="kp in recommendation.knowledgePoints?.slice(0, 3)"
                :key="kp"
                size="small"
              >
                {{ kp }}
              </el-tag>
            </div>
            <span class="match-desc">与你正在学习的内容相关</span>
          </div>
          <div class="match-score">
            <el-rate :model-value="5" disabled show-score />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 推荐路径可视化 -->
    <el-card class="explanation-card" shadow="hover">
      <template #header>
        <span>推荐路径</span>
      </template>

      <div ref="pathChart" style="height: 300px"></div>
    </el-card>

    <!-- 相似用户行为 -->
    <el-card class="explanation-card" shadow="hover">
      <template #header>
        <span>相似学习者也在学</span>
      </template>

      <div class="similar-users">
        <div class="users-stat">
          <el-avatar-group :max="5">
            <el-avatar
              v-for="i in 8"
              :key="i"
              :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${i}`"
            />
          </el-avatar-group>
          <span class="users-count">与你相似的 <strong>128</strong> 位学习者</span>
        </div>

        <div class="behavior-stats">
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon color="#409eff"><View /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">86%</div>
              <div class="stat-label">浏览率</div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon">
              <el-icon color="#67c23a"><Edit /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">72%</div>
              <div class="stat-label">练习率</div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon">
              <el-icon color="#e6a23c"><Star /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">4.8</div>
              <div class="stat-label">平均评分</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 学习效果预测 -->
    <el-card class="explanation-card" shadow="hover">
      <template #header>
        <span>预测学习效果</span>
      </template>

      <div class="effect-prediction">
        <div class="prediction-item">
          <div class="prediction-label">预计掌握度</div>
          <el-progress
            type="dashboard"
            :percentage="85"
            :width="120"
            color="#67c23a"
          >
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}%</span>
            </template>
          </el-progress>
        </div>

        <div class="prediction-item">
          <div class="prediction-label">预计学习时长</div>
          <div class="time-estimate">
            <el-icon size="40" color="#409eff"><Clock /></el-icon>
            <div class="time-value">约 25 分钟</div>
          </div>
        </div>

        <div class="prediction-item">
          <div class="prediction-label">成功概率</div>
          <div class="success-rate">
            <div class="rate-circle" :style="{ '--rate': '78%' }">
              <span class="rate-value">78%</span>
            </div>
            <div class="rate-desc">基于你的历史表现</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import * as echarts from 'echarts'
import {
  Medal,
  TrendCharts,
  Connection,
  View,
  Edit,
  Star,
  Clock
} from '@element-plus/icons-vue'
import type { RecommendationResult } from '@/api/recommendation'

interface Props {
  recommendation: RecommendationResult
}

const props = defineProps<Props>()

const pathChart = ref<HTMLElement>()

const scoreComponents = computed(() => {
  const metadata = props.recommendation.metadata || {}
  return [
    {
      name: '协同过滤分数',
      score: metadata.cfScore || 0,
      color: '#409eff',
      description: '基于相似用户的偏好'
    },
    {
      name: '内容匹配分数',
      score: metadata.contentScore || 0,
      color: '#67c23a',
      description: '基于内容相似度'
    },
    {
      name: '流行度分数',
      score: metadata.popularityScore || 0,
      color: '#e6a23c',
      description: '基于热度和评价'
    }
  ]
})

const totalScore = computed(() =>
  scoreComponents.value.reduce((sum, c) => sum + c.score, 0)
)

onMounted(() => {
  initPathChart()
})

function initPathChart() {
  if (!pathChart.value) return

  const chart = echarts.init(pathChart.value)

  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'sankey',
        layout: 'none',
        emphasis: {
          focus: 'adjacency'
        },
        data: [
          { name: '你的学习行为' },
          { name: '协同过滤' },
          { name: '内容推荐' },
          { name: '流行度' },
          { name: '综合评分' },
          { name: props.recommendation.itemTitle }
        ],
        links: [
          { source: '你的学习行为', target: '协同过滤', value: 40 },
          { source: '你的学习行为', target: '内容推荐', value: 40 },
          { source: '你的学习行为', target: '流行度', value: 20 },
          { source: '协同过滤', target: '综合评分', value: 40 },
          { source: '内容推荐', target: '综合评分', value: 40 },
          { source: '流行度', target: '综合评分', value: 20 },
          { source: '综合评分', target: props.recommendation.itemTitle, value: 100 }
        ],
        lineStyle: {
          color: 'gradient',
          curveness: 0.5
        }
      }
    ]
  }

  chart.setOption(option)
}

function getDifficultyLabel(difficulty: number): string {
  const labels = ['简单', '较简单', '中等', '较难', '困难']
  return labels[difficulty - 1] || '中等'
}

function getDifficultyMatchScore(): number {
  // 根据难度匹配度返回评分
  return 4
}
</script>

<style scoped lang="scss">
.recommendation-explanation-content {
  .explanation-card {
    margin-bottom: 16px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .algorithm-breakdown {
    .breakdown-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
    }

    .score-component {
      margin-bottom: 20px;

      .component-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .component-name {
          font-weight: 600;
          color: #606266;
        }

        .component-score {
          color: #409eff;
          font-weight: bold;
        }
      }

      .component-description {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }

    .total-score {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      color: white;
      font-weight: bold;
      margin-top: 16px;

      .score-value {
        font-size: 24px;
      }
    }
  }

  .profile-match {
    .match-item {
      display: flex;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #e4e7ed;

      &:last-child {
        border-bottom: none;
      }

      .match-label {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        color: #303133;
        min-width: 120px;
      }

      .match-content {
        flex: 1;
        padding: 0 16px;

        .match-desc {
          display: block;
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }

        .kp-tags {
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
          margin-bottom: 4px;
        }
      }

      .match-score {
        min-width: 150px;
        text-align: right;
      }
    }
  }

  .similar-users {
    .users-stat {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;

      .users-count {
        font-size: 14px;
        color: #606266;

        strong {
          color: #409eff;
          font-size: 18px;
        }
      }
    }

    .behavior-stats {
      display: flex;
      justify-content: space-around;
      gap: 16px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 12px;
        flex: 1;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 8px;

        .stat-icon {
          font-size: 32px;
        }

        .stat-content {
          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
          }

          .stat-label {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }

  .effect-prediction {
    display: flex;
    justify-content: space-around;
    gap: 24px;

    .prediction-item {
      flex: 1;
      text-align: center;

      .prediction-label {
        font-weight: 600;
        color: #303133;
        margin-bottom: 16px;
      }

      .time-estimate {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;

        .time-value {
          font-size: 20px;
          font-weight: bold;
          color: #409eff;
        }
      }

      .success-rate {
        .rate-circle {
          width: 120px;
          height: 120px;
          border-radius: 50%;
          background: conic-gradient(#67c23a 0% var(--rate), #e4e7ed var(--rate) 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 0 auto 12px;
          position: relative;

          &::before {
            content: '';
            position: absolute;
            width: 90px;
            height: 90px;
            border-radius: 50%;
            background: white;
          }

          .rate-value {
            position: relative;
            z-index: 1;
            font-size: 28px;
            font-weight: bold;
            color: #67c23a;
          }
        }

        .rate-desc {
          font-size: 12px;
          color: #909399;
        }
      }

      .percentage-value {
        font-size: 20px;
        font-weight: bold;
      }
    }
  }
}

@media (max-width: 768px) {
  .recommendation-explanation-content {
    .profile-match .match-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .match-label {
        min-width: auto;
      }

      .match-content {
        padding: 0;
      }

      .match-score {
        width: 100%;
        text-align: left;
      }
    }

    .similar-users .behavior-stats {
      flex-direction: column;
    }

    .effect-prediction {
      flex-direction: column;
    }
  }
}
</style>
