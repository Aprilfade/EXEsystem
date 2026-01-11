<template>
  <div class="advanced-learning-charts">
    <!-- 学习概览卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in overviewStats" :key="stat.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div :class="['stat-icon', stat.color]">
              <component :is="stat.icon" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-change" :class="stat.changeType">
                {{ stat.change }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 核心可视化图表 -->
    <el-row :gutter="20">
      <!-- 学习时长趋势图 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>学习时长趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="7">近7天</el-radio-button>
                <el-radio-button label="30">近30天</el-radio-button>
                <el-radio-button label="90">近90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="studyTimeChart" style="height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 知识点掌握度雷达图 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>知识点掌握度分析</span>
              <el-select v-model="selectedSubject" size="small" style="width: 120px">
                <el-option label="全部" value="all" />
                <el-option label="数学" value="math" />
                <el-option label="物理" value="physics" />
                <el-option label="化学" value="chemistry" />
              </el-select>
            </div>
          </template>
          <div ref="masteryRadarChart" style="height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 学习效果预测图 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <el-card shadow="hover">
          <template #header>
            <span>学习效果预测（未来7天）</span>
          </template>
          <div ref="predictionChart" style="height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 答题正确率热力图 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <el-card shadow="hover">
          <template #header>
            <span>答题正确率热力图</span>
          </template>
          <div ref="accuracyHeatmap" style="height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 学习路径桑基图 -->
      <el-col :xs="24" class="mb-4">
        <el-card shadow="hover">
          <template #header>
            <span>智能学习路径规划</span>
          </template>
          <div ref="learningPathChart" style="height: 500px"></div>
        </el-card>
      </el-col>

      <!-- 错题分析树图 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <el-card shadow="hover">
          <template #header>
            <span>错题知识点分布</span>
          </template>
          <div ref="wrongQuestionTree" style="height: 450px"></div>
        </el-card>
      </el-col>

      <!-- AI使用统计仪表盘 -->
      <el-col :xs="24" :lg="12" class="mb-4">
        <el-card shadow="hover">
          <template #header>
            <span>AI助手使用统计</span>
          </template>
          <div ref="aiUsageGauge" style="height: 450px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { TrendCharts, Radar, PieChart, DataAnalysis } from '@element-plus/icons-vue'

interface OverviewStat {
  title: string
  value: string | number
  change: string
  changeType: 'up' | 'down' | 'neutral'
  icon: any
  color: string
}

const timeRange = ref('7')
const selectedSubject = ref('all')

// 图表实例
const studyTimeChart = ref<HTMLElement>()
const masteryRadarChart = ref<HTMLElement>()
const predictionChart = ref<HTMLElement>()
const accuracyHeatmap = ref<HTMLElement>()
const learningPathChart = ref<HTMLElement>()
const wrongQuestionTree = ref<HTMLElement>()
const aiUsageGauge = ref<HTMLElement>()

let charts: ECharts[] = []

// 概览统计数据
const overviewStats = ref<OverviewStat[]>([
  {
    title: '总学习时长',
    value: '48.5h',
    change: '↑ 12.3% vs 上周',
    changeType: 'up',
    icon: TrendCharts,
    color: 'blue'
  },
  {
    title: '知识点掌握',
    value: '78%',
    change: '↑ 5.2% vs 上周',
    changeType: 'up',
    icon: Radar,
    color: 'green'
  },
  {
    title: '平均正确率',
    value: '85.6%',
    change: '↑ 3.1% vs 上周',
    changeType: 'up',
    icon: PieChart,
    color: 'orange'
  },
  {
    title: 'AI互动次数',
    value: '156',
    change: '↑ 23.5% vs 上周',
    changeType: 'up',
    icon: DataAnalysis,
    color: 'purple'
  }
])

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  charts.forEach(chart => chart.dispose())
  window.removeEventListener('resize', handleResize)
})

watch(timeRange, () => {
  updateStudyTimeChart()
})

watch(selectedSubject, () => {
  updateMasteryRadarChart()
})

function handleResize() {
  charts.forEach(chart => chart.resize())
}

function initCharts() {
  initStudyTimeChart()
  initMasteryRadarChart()
  initPredictionChart()
  initAccuracyHeatmap()
  initLearningPathChart()
  initWrongQuestionTree()
  initAiUsageGauge()
}

// 1. 学习时长趋势图（折线图+柱状图组合）
function initStudyTimeChart() {
  if (!studyTimeChart.value) return

  const chart = echarts.init(studyTimeChart.value)
  charts.push(chart)

  const dates = generateDateRange(7)
  const studyTime = [2.5, 3.2, 1.8, 4.5, 3.0, 2.8, 5.2]
  const targetTime = Array(7).fill(3)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['实际学习时长', '目标时长', '累计学习']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: true
    },
    yAxis: [
      {
        type: 'value',
        name: '时长(h)',
        position: 'left'
      },
      {
        type: 'value',
        name: '累计(h)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '实际学习时长',
        type: 'bar',
        data: studyTime,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      },
      {
        name: '目标时长',
        type: 'line',
        data: targetTime,
        lineStyle: { type: 'dashed', color: '#ff7875' },
        itemStyle: { color: '#ff7875' }
      },
      {
        name: '累计学习',
        type: 'line',
        yAxisIndex: 1,
        data: studyTime.reduce((acc, val, i) => {
          acc.push(i === 0 ? val : acc[i - 1] + val)
          return acc
        }, [] as number[]),
        smooth: true,
        itemStyle: { color: '#52c41a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.1)' }
          ])
        }
      }
    ]
  }

  chart.setOption(option)
}

// 2. 知识点掌握度雷达图
function initMasteryRadarChart() {
  if (!masteryRadarChart.value) return

  const chart = echarts.init(masteryRadarChart.value)
  charts.push(chart)

  const option = {
    tooltip: {},
    legend: {
      data: ['当前掌握度', '预期掌握度', '班级平均']
    },
    radar: {
      indicator: [
        { name: '代数', max: 100 },
        { name: '几何', max: 100 },
        { name: '函数', max: 100 },
        { name: '概率统计', max: 100 },
        { name: '数列', max: 100 },
        { name: '立体几何', max: 100 }
      ],
      splitNumber: 4,
      shape: 'polygon',
      axisName: {
        color: '#333',
        fontSize: 14
      }
    },
    series: [
      {
        name: '掌握度对比',
        type: 'radar',
        data: [
          {
            value: [85, 72, 90, 78, 68, 82],
            name: '当前掌握度',
            itemStyle: { color: '#1890ff' },
            areaStyle: { color: 'rgba(24, 144, 255, 0.3)' }
          },
          {
            value: [90, 85, 95, 88, 80, 90],
            name: '预期掌握度',
            itemStyle: { color: '#52c41a' },
            lineStyle: { type: 'dashed' },
            areaStyle: { color: 'rgba(82, 196, 26, 0.1)' }
          },
          {
            value: [75, 70, 80, 72, 65, 75],
            name: '班级平均',
            itemStyle: { color: '#faad14' },
            lineStyle: { type: 'dotted' }
          }
        ]
      }
    ]
  }

  chart.setOption(option)
}

// 3. 学习效果预测图（双Y轴）
function initPredictionChart() {
  if (!predictionChart.value) return

  const chart = echarts.init(predictionChart.value)
  charts.push(chart)

  const dates = generateDateRange(7, true)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['预测掌握度', '答对概率', '推荐练习量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '掌握度/概率(%)',
        min: 0,
        max: 100,
        position: 'left'
      },
      {
        type: 'value',
        name: '练习量',
        position: 'right'
      }
    ],
    series: [
      {
        name: '预测掌握度',
        type: 'line',
        smooth: true,
        data: [78, 80, 82, 85, 87, 89, 91],
        itemStyle: { color: '#1890ff' },
        markLine: {
          data: [{ type: 'average', name: '平均值' }]
        }
      },
      {
        name: '答对概率',
        type: 'line',
        smooth: true,
        data: [85, 86, 88, 89, 90, 91, 93],
        itemStyle: { color: '#52c41a' }
      },
      {
        name: '推荐练习量',
        type: 'bar',
        yAxisIndex: 1,
        data: [5, 4, 4, 3, 3, 2, 2],
        itemStyle: { color: '#faad14', opacity: 0.6 }
      }
    ]
  }

  chart.setOption(option)
}

// 4. 答题正确率热力图
function initAccuracyHeatmap() {
  if (!accuracyHeatmap.value) return

  const chart = echarts.init(accuracyHeatmap.value)
  charts.push(chart)

  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  const data = []
  for (let i = 0; i < 7; i++) {
    for (let j = 0; j < 24; j++) {
      const accuracy = Math.floor(Math.random() * 100)
      if (accuracy > 30) {
        // 过滤掉没有学习的时段
        data.push([j, i, accuracy])
      }
    }
  }

  const option = {
    tooltip: {
      position: 'top',
      formatter: (params: any) => {
        return `${days[params.value[1]]} ${hours[params.value[0]]}<br/>正确率: ${params.value[2]}%`
      }
    },
    grid: {
      height: '70%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: hours,
      splitArea: { show: true },
      axisLabel: { interval: 1, rotate: 45 }
    },
    yAxis: {
      type: 'category',
      data: days,
      splitArea: { show: true }
    },
    visualMap: {
      min: 0,
      max: 100,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '5%',
      inRange: {
        color: ['#e74c3c', '#f39c12', '#f1c40f', '#2ecc71', '#27ae60']
      }
    },
    series: [
      {
        name: '正确率',
        type: 'heatmap',
        data: data,
        label: {
          show: false
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  chart.setOption(option)
}

// 5. 学习路径桑基图
function initLearningPathChart() {
  if (!learningPathChart.value) return

  const chart = echarts.init(learningPathChart.value)
  charts.push(chart)

  const option = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove'
    },
    series: [
      {
        type: 'sankey',
        layout: 'none',
        emphasis: {
          focus: 'adjacency'
        },
        data: [
          { name: '基础代数' },
          { name: '函数入门' },
          { name: '一次函数' },
          { name: '二次函数' },
          { name: '指数函数' },
          { name: '对数函数' },
          { name: '三角函数' },
          { name: '函数综合' },
          { name: '微积分初步' },
          { name: '已掌握' }
        ],
        links: [
          { source: '基础代数', target: '函数入门', value: 5 },
          { source: '函数入门', target: '一次函数', value: 3 },
          { source: '函数入门', target: '二次函数', value: 2 },
          { source: '一次函数', target: '函数综合', value: 2 },
          { source: '二次函数', target: '函数综合', value: 2 },
          { source: '基础代数', target: '指数函数', value: 2 },
          { source: '指数函数', target: '对数函数', value: 2 },
          { source: '对数函数', target: '函数综合', value: 1 },
          { source: '函数入门', target: '三角函数', value: 2 },
          { source: '三角函数', target: '函数综合', value: 2 },
          { source: '函数综合', target: '微积分初步', value: 3 },
          { source: '微积分初步', target: '已掌握', value: 1 }
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

// 6. 错题知识点树图
function initWrongQuestionTree() {
  if (!wrongQuestionTree.value) return

  const chart = echarts.init(wrongQuestionTree.value)
  charts.push(chart)

  const option = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      formatter: '{b}: {c}题'
    },
    series: [
      {
        type: 'treemap',
        data: [
          {
            name: '数学',
            value: 45,
            children: [
              {
                name: '代数',
                value: 20,
                children: [
                  { name: '方程', value: 8 },
                  { name: '不等式', value: 7 },
                  { name: '数列', value: 5 }
                ]
              },
              {
                name: '几何',
                value: 15,
                children: [
                  { name: '三角形', value: 6 },
                  { name: '圆', value: 5 },
                  { name: '立体几何', value: 4 }
                ]
              },
              {
                name: '函数',
                value: 10,
                children: [
                  { name: '二次函数', value: 6 },
                  { name: '指数对数', value: 4 }
                ]
              }
            ]
          },
          {
            name: '物理',
            value: 30,
            children: [
              { name: '力学', value: 12 },
              { name: '电学', value: 10 },
              { name: '光学', value: 8 }
            ]
          },
          {
            name: '化学',
            value: 25,
            children: [
              { name: '有机化学', value: 10 },
              { name: '无机化学', value: 9 },
              { name: '化学反应', value: 6 }
            ]
          }
        ],
        levels: [
          {
            itemStyle: {
              borderColor: '#777',
              borderWidth: 2,
              gapWidth: 1
            }
          },
          {
            colorSaturation: [0.35, 0.5],
            itemStyle: {
              borderWidth: 5,
              gapWidth: 1,
              borderColorSaturation: 0.6
            }
          }
        ]
      }
    ]
  }

  chart.setOption(option)
}

// 7. AI使用统计仪表盘
function initAiUsageGauge() {
  if (!aiUsageGauge.value) return

  const chart = echarts.init(aiUsageGauge.value)
  charts.push(chart)

  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 200,
        splitNumber: 10,
        itemStyle: {
          color: '#1890ff'
        },
        progress: {
          show: true,
          width: 18
        },
        pointer: {
          show: false
        },
        axisLine: {
          lineStyle: {
            width: 18
          }
        },
        axisTick: {
          distance: -30,
          splitNumber: 5,
          lineStyle: {
            width: 2,
            color: '#999'
          }
        },
        splitLine: {
          distance: -40,
          length: 14,
          lineStyle: {
            width: 3,
            color: '#999'
          }
        },
        axisLabel: {
          distance: -20,
          color: '#999',
          fontSize: 14
        },
        anchor: {
          show: false
        },
        title: {
          show: false
        },
        detail: {
          valueAnimation: true,
          width: '60%',
          lineHeight: 40,
          borderRadius: 8,
          offsetCenter: [0, '-15%'],
          fontSize: 40,
          fontWeight: 'bolder',
          formatter: '{value}',
          color: 'inherit'
        },
        data: [
          {
            value: 156,
            name: 'AI互动次数'
          }
        ]
      },
      {
        type: 'gauge',
        center: ['50%', '70%'],
        startAngle: 200,
        endAngle: -20,
        min: 0,
        max: 100,
        splitNumber: 10,
        itemStyle: {
          color: '#52c41a'
        },
        progress: {
          show: true,
          width: 8
        },
        pointer: {
          show: false
        },
        axisLine: {
          show: false
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          show: false
        },
        detail: {
          show: false
        },
        data: [
          {
            value: 87
          }
        ]
      }
    ]
  }

  chart.setOption(option)
}

function updateStudyTimeChart() {
  // 根据时间范围更新数据
  const chart = charts[0]
  if (!chart) return

  const days = parseInt(timeRange.value)
  const dates = generateDateRange(days)
  const studyTime = Array.from({ length: days }, () => Math.random() * 4 + 1)

  chart.setOption({
    xAxis: { data: dates },
    series: [
      { data: studyTime },
      { data: Array(days).fill(3) },
      {
        data: studyTime.reduce((acc, val, i) => {
          acc.push(i === 0 ? val : acc[i - 1] + val)
          return acc
        }, [] as number[])
      }
    ]
  })
}

function updateMasteryRadarChart() {
  // 根据科目更新数据
  const chart = charts[1]
  if (!chart) return

  // 可以根据 selectedSubject 加载不同的数据
  // 这里仅作示例
}

function generateDateRange(days: number, future: boolean = false): string[] {
  const dates = []
  const today = new Date()

  for (let i = future ? 1 : -days + 1; i <= (future ? days : 0); i++) {
    const date = new Date(today)
    date.setDate(date.getDate() + i)
    dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
  }

  return dates
}
</script>

<style scoped lang="scss">
.advanced-learning-charts {
  padding: 20px;

  .stat-card {
    height: 100%;
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-5px);
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;

        &.blue {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        &.green {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }

        &.orange {
          background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
          color: white;
        }

        &.purple {
          background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
          color: white;
        }
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #333;
          margin-bottom: 4px;
        }

        .stat-title {
          font-size: 14px;
          color: #666;
          margin-bottom: 6px;
        }

        .stat-change {
          font-size: 12px;

          &.up {
            color: #52c41a;
          }

          &.down {
            color: #ff4d4f;
          }

          &.neutral {
            color: #999;
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .mb-4 {
    margin-bottom: 20px;
  }
}
</style>
