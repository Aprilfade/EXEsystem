<template>
  <div class="learning-analytics-container">
    <!-- 学习建议卡片 -->
    <el-card v-if="analytics" shadow="hover" class="advice-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="20"><TrendCharts /></el-icon>
          <span>学习建议</span>
        </div>
      </template>
      <div class="advice-content">
        <el-icon class="advice-icon" :size="40" color="#409EFF"><Sunny /></el-icon>
        <p>{{ analytics.learningAdvice }}</p>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 学习时长趋势 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="18"><Clock /></el-icon>
              <span>学习时长趋势</span>
              <el-radio-group v-model="days" size="small" @change="handleDaysChange" class="days-selector">
                <el-radio-button :label="7">近7天</el-radio-button>
                <el-radio-button :label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="studyTimeChart" class="chart" v-loading="loading"></div>
        </el-card>
      </el-col>

      <!-- 知识点掌握度雷达图 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="18"><DataAnalysis /></el-icon>
              <span>知识点掌握度</span>
            </div>
          </template>
          <div v-if="analytics && analytics.knowledgeMastery && analytics.knowledgeMastery.length > 0">
            <div ref="masteryChart" class="chart" v-loading="loading"></div>
          </div>
          <el-empty v-else description="暂无知识点数据，多做些练习吧！" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 弱项分析 -->
    <el-card shadow="hover" class="chart-card weak-points-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="18"><Warning /></el-icon>
          <span>弱项分析</span>
        </div>
      </template>
      <div v-if="analytics && analytics.weakPoints.length > 0">
        <div ref="weakPointsChart" class="chart chart-horizontal" v-loading="loading"></div>
        <div class="weak-points-tips">
          <el-alert
            type="info"
            :closable="false"
            show-icon
          >
            <template #title>
              针对性练习建议：重点攻克错题数量最多的知识点，建议按照推荐次数进行专项练习
            </template>
          </el-alert>
        </div>
      </div>
      <el-empty v-else description="暂无弱项数据，继续加油！" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Clock, TrendCharts, Warning, Sunny, DataAnalysis } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import type { ECharts } from 'echarts';
import { getLearningAnalytics, type LearningAnalytics } from '@/api/learningAnalytics';

const loading = ref(false);
const days = ref(7);
const analytics = ref<LearningAnalytics | null>(null);

// 图表实例
const studyTimeChart = ref<HTMLDivElement>();
const masteryChart = ref<HTMLDivElement>();
const weakPointsChart = ref<HTMLDivElement>();

let studyTimeChartInstance: ECharts | null = null;
let masteryChartInstance: ECharts | null = null;
let weakPointsChartInstance: ECharts | null = null;

/**
 * 加载数据
 */
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getLearningAnalytics(days.value);
    if (res.code === 200) {
      analytics.value = res.data;
      // 数据加载完成后，初始化图表
      setTimeout(() => {
        initCharts();
      }, 100);
    } else {
      ElMessage.error(res.msg || '加载失败');
    }
  } catch (error) {
    console.error('加载学习分析失败:', error);
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 初始化图表
 */
const initCharts = () => {
  if (!analytics.value) return;

  initStudyTimeChart();
  initMasteryChart();
  initWeakPointsChart();
};

/**
 * 初始化学习时长趋势图
 */
const initStudyTimeChart = () => {
  if (!studyTimeChart.value || !analytics.value) return;

  // 销毁已存在的实例
  if (studyTimeChartInstance) {
    studyTimeChartInstance.dispose();
  }

  studyTimeChartInstance = echarts.init(studyTimeChart.value);

  const dates = analytics.value.studyTimeTrend.map(item => {
    const date = new Date(item.date);
    return `${date.getMonth() + 1}/${date.getDate()}`;
  });
  const minutes = analytics.value.studyTimeTrend.map(item => item.studyMinutes);
  const questions = analytics.value.studyTimeTrend.map(item => item.questionCount);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['学习时长', '完成题数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '分钟',
        position: 'left',
        axisLine: {
          lineStyle: {
            color: '#409EFF'
          }
        }
      },
      {
        type: 'value',
        name: '题数',
        position: 'right',
        axisLine: {
          lineStyle: {
            color: '#67C23A'
          }
        }
      }
    ],
    series: [
      {
        name: '学习时长',
        type: 'line',
        smooth: true,
        data: minutes,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '完成题数',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: questions,
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  };

  studyTimeChartInstance.setOption(option);
};

/**
 * 初始化知识点掌握度雷达图
 */
const initMasteryChart = () => {
  if (!masteryChart.value || !analytics.value) return;

  const mastery = analytics.value.knowledgeMastery.slice(0, 6); // 最多显示6个知识点

  // 如果没有数据，不初始化图表
  if (mastery.length === 0) {
    if (masteryChartInstance) {
      masteryChartInstance.dispose();
      masteryChartInstance = null;
    }
    return;
  }

  if (masteryChartInstance) {
    masteryChartInstance.dispose();
  }

  masteryChartInstance = echarts.init(masteryChart.value);

  const indicator = mastery.map(item => ({
    name: item.knowledgePointName,
    max: 100
  }));

  const values = mastery.map(item => item.masteryRate);

  const option = {
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: indicator,
      radius: '60%',
      splitNumber: 5,
      scale: false,
      axisName: {
        fontSize: 12
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(114, 172, 209, 0.05)', 'rgba(114, 172, 209, 0.1)',
                  'rgba(114, 172, 209, 0.15)', 'rgba(114, 172, 209, 0.2)',
                  'rgba(114, 172, 209, 0.25)']
        }
      }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: values,
            name: '掌握度',
            itemStyle: {
              color: '#409EFF'
            },
            areaStyle: {
              color: 'rgba(64, 158, 255, 0.3)'
            }
          }
        ]
      }
    ]
  };

  masteryChartInstance.setOption(option);
};

/**
 * 初始化弱项分析条形图
 */
const initWeakPointsChart = () => {
  if (!weakPointsChart.value || !analytics.value || analytics.value.weakPoints.length === 0) {
    return;
  }

  if (weakPointsChartInstance) {
    weakPointsChartInstance.dispose();
  }

  weakPointsChartInstance = echarts.init(weakPointsChart.value);

  const weakPoints = analytics.value.weakPoints;
  const names = weakPoints.map(item => item.knowledgePointName);
  const wrongCounts = weakPoints.map(item => item.wrongCount);
  const recommendedCounts = weakPoints.map(item => item.recommendedPracticeCount);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['错题数', '建议练习次数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: names
    },
    series: [
      {
        name: '错题数',
        type: 'bar',
        data: wrongCounts,
        itemStyle: {
          color: '#F56C6C'
        }
      },
      {
        name: '建议练习次数',
        type: 'bar',
        data: recommendedCounts,
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  };

  weakPointsChartInstance.setOption(option);
};

/**
 * 切换天数
 */
const handleDaysChange = () => {
  fetchData();
};

/**
 * 响应式调整图表大小
 */
const handleResize = () => {
  studyTimeChartInstance?.resize();
  masteryChartInstance?.resize();
  weakPointsChartInstance?.resize();
};

onMounted(() => {
  fetchData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  studyTimeChartInstance?.dispose();
  masteryChartInstance?.dispose();
  weakPointsChartInstance?.dispose();
});

// 监听数据变化，重新渲染图表
watch(() => analytics.value, () => {
  if (analytics.value) {
    initCharts();
  }
});
</script>

<style scoped lang="scss">
.learning-analytics-container {
  .advice-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
    }

    .advice-content {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px 0;

      .advice-icon {
        flex-shrink: 0;
      }

      p {
        margin: 0;
        font-size: 15px;
        line-height: 1.6;
        color: #606266;
      }
    }
  }

  .charts-row {
    margin-bottom: 20px;
  }

  .chart-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;

      .days-selector {
        margin-left: auto;
      }
    }

    .chart {
      width: 100%;
      height: 300px;
    }

    .chart-horizontal {
      height: 250px;
    }
  }

  .weak-points-card {
    .weak-points-tips {
      margin-top: 16px;
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .learning-analytics-container {
    .chart-card .chart {
      height: 250px;
    }
  }
}
</style>
