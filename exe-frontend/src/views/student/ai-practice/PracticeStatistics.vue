<template>
  <div class="practice-statistics">
    <!-- 关键指标卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card correct-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="32" color="#67c23a"><CircleCheck /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ sessionData.correctCount }}</div>
              <div class="card-label">答对题数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card accuracy-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="32" color="#409eff"><TrendCharts /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ sessionData.accuracy }}%</div>
              <div class="card-label">正确率</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card streak-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="32" color="#e6a23c"><Medal /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ sessionData.maxStreak }}</div>
              <div class="card-label">最高连击</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card xp-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="32" color="#f56c6c"><Star /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">+{{ sessionData.xpGained }}</div>
              <div class="card-label">获得经验</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 正确率趋势图 -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span class="chart-title">答题正确率趋势</span>
          </template>
          <div ref="accuracyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 答题时间分布 -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span class="chart-title">答题时间分布</span>
          </template>
          <div ref="timeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 知识点掌握雷达图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="chart-title">知识点掌握情况</span>
          </template>
          <div ref="knowledgeChartRef" class="chart-container-large"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { CircleCheck, TrendCharts, Medal, Star } from '@element-plus/icons-vue'
import type { PracticeSession } from '@/types/practice'

interface Props {
  sessionData: PracticeSession
}

const props = defineProps<Props>()

const accuracyChartRef = ref<HTMLElement>()
const timeChartRef = ref<HTMLElement>()
const knowledgeChartRef = ref<HTMLElement>()

let accuracyChart: ECharts | null = null
let timeChart: ECharts | null = null
let knowledgeChart: ECharts | null = null

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  disposeCharts()
  window.removeEventListener('resize', handleResize)
})

watch(
  () => props.sessionData,
  () => {
    updateCharts()
  },
  { deep: true }
)

/**
 * 初始化图表
 */
const initCharts = () => {
  if (!accuracyChartRef.value || !timeChartRef.value || !knowledgeChartRef.value) {
    return
  }

  // 1. 正确率趋势图
  accuracyChart = echarts.init(accuracyChartRef.value)
  const accuracyData = calculateAccuracyTrend()
  accuracyChart.setOption({
    title: {
      show: false
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>正确率: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: accuracyData.labels,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        type: 'line',
        data: accuracyData.values,
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        },
        lineStyle: {
          width: 3,
          color: '#409eff'
        },
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  })

  // 2. 答题时间分布饼图
  timeChart = echarts.init(timeChartRef.value)
  const timeData = calculateTimeDistribution()
  timeChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}题 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center'
    },
    series: [
      {
        name: '答题时间',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c}题'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        data: timeData
      }
    ]
  })

  // 3. 知识点掌握雷达图
  knowledgeChart = echarts.init(knowledgeChartRef.value)
  const knowledgeData = calculateKnowledgeMastery()
  knowledgeChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: knowledgeData.indicators,
      radius: '70%',
      splitNumber: 5,
      splitArea: {
        areaStyle: {
          color: [
            'rgba(64, 158, 255, 0.1)',
            'rgba(64, 158, 255, 0.2)',
            'rgba(64, 158, 255, 0.3)',
            'rgba(64, 158, 255, 0.4)',
            'rgba(64, 158, 255, 0.5)'
          ]
        }
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(64, 158, 255, 0.5)'
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(64, 158, 255, 0.5)'
        }
      }
    },
    series: [
      {
        name: '知识点掌握度',
        type: 'radar',
        data: [
          {
            value: knowledgeData.values,
            name: '掌握程度',
            areaStyle: {
              color: 'rgba(64, 158, 255, 0.3)'
            },
            lineStyle: {
              width: 2,
              color: '#409eff'
            },
            itemStyle: {
              color: '#409eff'
            }
          }
        ]
      }
    ]
  })
}

/**
 * 更新图表
 */
const updateCharts = () => {
  if (!accuracyChart || !timeChart || !knowledgeChart) return

  const accuracyData = calculateAccuracyTrend()
  accuracyChart.setOption({
    xAxis: {
      data: accuracyData.labels
    },
    series: [
      {
        data: accuracyData.values
      }
    ]
  })

  const timeData = calculateTimeDistribution()
  timeChart.setOption({
    series: [
      {
        data: timeData
      }
    ]
  })

  const knowledgeData = calculateKnowledgeMastery()
  knowledgeChart.setOption({
    radar: {
      indicator: knowledgeData.indicators
    },
    series: [
      {
        data: [
          {
            value: knowledgeData.values
          }
        ]
      }
    ]
  })
}

/**
 * 计算正确率趋势
 */
const calculateAccuracyTrend = () => {
  const labels: string[] = []
  const values: number[] = []

  const questions = props.sessionData.questions
  const userAnswers = props.sessionData.userAnswers

  // 按每5题计算一次正确率
  for (let i = 0; i < questions.length; i += 5) {
    const end = Math.min(i + 5, questions.length)
    let correct = 0

    for (let j = i; j < end; j++) {
      const questionId = questions[j].id
      const answer = userAnswers.get(questionId)
      if (answer && answer.isCorrect) {
        correct++
      }
    }

    labels.push(`第${i + 1}-${end}题`)
    values.push(Math.round((correct / (end - i)) * 100))
  }

  return { labels, values }
}

/**
 * 计算答题时间分布
 */
const calculateTimeDistribution = () => {
  const distribution = {
    fast: 0, // <30秒
    normal: 0, // 30-60秒
    slow: 0, // 60-120秒
    verySlow: 0 // >120秒
  }

  props.sessionData.userAnswers.forEach(answer => {
    const time = answer.timeSpent
    if (time < 30) {
      distribution.fast++
    } else if (time < 60) {
      distribution.normal++
    } else if (time < 120) {
      distribution.slow++
    } else {
      distribution.verySlow++
    }
  })

  return [
    { value: distribution.fast, name: '<30秒', itemStyle: { color: '#67c23a' } },
    { value: distribution.normal, name: '30-60秒', itemStyle: { color: '#409eff' } },
    { value: distribution.slow, name: '60-120秒', itemStyle: { color: '#e6a23c' } },
    { value: distribution.verySlow, name: '>120秒', itemStyle: { color: '#f56c6c' } }
  ]
}

/**
 * 计算知识点掌握情况
 */
const calculateKnowledgeMastery = () => {
  // 统计每个知识点的正确率
  const knowledgeMap = new Map<string, { correct: number; total: number }>()

  props.sessionData.questions.forEach(question => {
    const kp = question.knowledgePoint || '未分类'
    const answer = props.sessionData.userAnswers.get(question.id)

    if (!knowledgeMap.has(kp)) {
      knowledgeMap.set(kp, { correct: 0, total: 0 })
    }

    const stat = knowledgeMap.get(kp)!
    stat.total++
    if (answer && answer.isCorrect) {
      stat.correct++
    }
  })

  const indicators: Array<{ name: string; max: 100 }> = []
  const values: number[] = []

  knowledgeMap.forEach((stat, kp) => {
    indicators.push({ name: kp, max: 100 })
    values.push(Math.round((stat.correct / stat.total) * 100))
  })

  // 如果没有数据，显示默认
  if (indicators.length === 0) {
    return {
      indicators: [
        { name: '暂无数据', max: 100 }
      ],
      values: [0]
    }
  }

  return { indicators, values }
}

/**
 * 处理窗口大小变化
 */
const handleResize = () => {
  accuracyChart?.resize()
  timeChart?.resize()
  knowledgeChart?.resize()
}

/**
 * 销毁图表
 */
const disposeCharts = () => {
  accuracyChart?.dispose()
  timeChart?.dispose()
  knowledgeChart?.dispose()
}
</script>

<style scoped lang="scss">
.practice-statistics {
  .stats-cards {
    margin-bottom: 20px;

    .stat-card {
      .card-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .card-icon {
          flex-shrink: 0;
          width: 64px;
          height: 64px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
        }

        .card-info {
          flex: 1;

          .card-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
            margin-bottom: 8px;
          }

          .card-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }

      &.correct-card .card-icon {
        background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ed 100%);
      }

      &.accuracy-card .card-icon {
        background: linear-gradient(135deg, #f0f9ff 0%, #e6f0ff 100%);
      }

      &.streak-card .card-icon {
        background: linear-gradient(135deg, #fff7e6 0%, #fff 100%);
      }

      &.xp-card .card-icon {
        background: linear-gradient(135deg, #fef0f0 0%, #fff 100%);
      }
    }
  }

  .chart-row {
    margin-bottom: 20px;

    .chart-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .chart-container {
      width: 100%;
      height: 300px;
    }

    .chart-container-large {
      width: 100%;
      height: 400px;
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .practice-statistics {
    .stats-cards {
      .stat-card .card-content {
        flex-direction: column;
        text-align: center;

        .card-icon {
          width: 56px;
          height: 56px;
        }

        .card-info .card-value {
          font-size: 24px;
        }
      }
    }

    .chart-row {
      .chart-container,
      .chart-container-large {
        height: 250px;
      }
    }
  }
}
</style>
