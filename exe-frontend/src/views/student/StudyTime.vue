<template>
  <div class="study-time-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <el-icon class="header-icon"><Clock /></el-icon>
          <div>
            <h2>学习时长统计</h2>
            <p class="subtitle">记录您的学习足迹，见证成长每一步</p>
          </div>
        </div>
        <div class="header-actions">
          <el-radio-group v-model="timeRange" @change="handleTimeRangeChange" size="large">
            <el-radio-button :label="7">近7天</el-radio-button>
            <el-radio-button :label="30">近30天</el-radio-button>
            <el-radio-button :label="90">近90天</el-radio-button>
          </el-radio-group>
          <el-button :icon="RefreshRight" @click="refreshData">刷新</el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards" v-loading="loading">
      <el-card shadow="hover" class="stat-card total">
        <div class="stat-content">
          <el-icon class="stat-icon"><Timer /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ formatDuration(stats.totalMinutes) }}</div>
            <div class="stat-label">累计学习</div>
            <div class="stat-desc">总学习时长</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card today">
        <div class="stat-content">
          <el-icon class="stat-icon"><Sunrise /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ formatDuration(stats.todayMinutes) }}</div>
            <div class="stat-label">今日学习</div>
            <div class="stat-desc">今天已学习</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card week">
        <div class="stat-content">
          <el-icon class="stat-icon"><Calendar /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ formatDuration(stats.weekMinutes) }}</div>
            <div class="stat-label">本周学习</div>
            <div class="stat-desc">本周累计</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card month">
        <div class="stat-content">
          <el-icon class="stat-icon"><Histogram /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ formatDuration(stats.monthMinutes) }}</div>
            <div class="stat-label">本月学习</div>
            <div class="stat-desc">本月累计</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card avg">
        <div class="stat-content">
          <el-icon class="stat-icon"><TrendCharts /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ formatDuration(stats.avgDailyMinutes) }}</div>
            <div class="stat-label">日均学习</div>
            <div class="stat-desc">平均每天</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card rank">
        <div class="stat-content">
          <el-icon class="stat-icon"><TrophyBase /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ ranking.rank || '-' }}</div>
            <div class="stat-label">学习排名</div>
            <div class="stat-desc" v-if="ranking.rank">超过{{ ranking.percentile }}%同学</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 学习趋势图 -->
      <el-card shadow="never" class="chart-card full-width">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <el-icon><TrendCharts /></el-icon>
              <span>学习时长趋势</span>
            </div>
            <el-tag type="info" size="small">{{ timeRange }}天数据</el-tag>
          </div>
        </template>
        <div ref="trendChartRef" class="chart-container" v-loading="chartLoading"></div>
      </el-card>

      <!-- 活动类型分布 -->
      <el-card shadow="never" class="chart-card half-width">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <el-icon><PieChart /></el-icon>
              <span>活动类型分布</span>
            </div>
          </div>
        </template>
        <div ref="typeChartRef" class="chart-container" v-loading="chartLoading"></div>
      </el-card>

      <!-- 科目学习时长 -->
      <el-card shadow="never" class="chart-card half-width">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <el-icon><Histogram /></el-icon>
              <span>科目学习时长</span>
            </div>
          </div>
        </template>
        <div ref="subjectChartRef" class="chart-container" v-loading="chartLoading"></div>
      </el-card>
    </div>

    <!-- 学习建议 -->
    <el-card shadow="never" class="tips-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><ChatLineSquare /></el-icon>
            <span>学习建议</span>
          </div>
        </div>
      </template>
      <div class="tips-content">
        <el-alert
          v-for="(tip, index) in studyTips"
          :key="index"
          :title="tip.title"
          :description="tip.description"
          :type="tip.type"
          :closable="false"
          show-icon
          class="tip-item"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Clock, Timer, Sunrise, Calendar, Histogram, TrendCharts, TrophyBase, PieChart,
  ChatLineSquare, RefreshRight
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import type { EChartsType } from 'echarts';
import {
  getStudyTimeStats,
  getDailyStudyTime,
  getActivityTypeStats,
  getSubjectStudyTime,
  getStudyRanking,
  type StudyTimeStats,
  type StudyRanking
} from '@/api/learningActivity';

// 加载状态
const loading = ref(false);
const chartLoading = ref(false);

// 时间范围
const timeRange = ref(30);

// 统计数据
const stats = reactive<StudyTimeStats>({
  totalMinutes: 0,
  todayMinutes: 0,
  weekMinutes: 0,
  monthMinutes: 0,
  avgDailyMinutes: 0
});

// 排名信息
const ranking = reactive<StudyRanking>({
  rank: 0,
  totalStudents: 0,
  percentile: 0
});

// 图表实例
let trendChart: EChartsType | null = null;
let typeChart: EChartsType | null = null;
let subjectChart: EChartsType | null = null;

// 图表DOM引用
const trendChartRef = ref<HTMLElement>();
const typeChartRef = ref<HTMLElement>();
const subjectChartRef = ref<HTMLElement>();

/**
 * 格式化时长
 */
const formatDuration = (minutes: number): string => {
  if (!minutes || minutes === 0) return '0分钟';

  const hours = Math.floor(minutes / 60);
  const mins = Math.floor(minutes % 60);

  if (hours > 0) {
    return mins > 0 ? `${hours}小时${mins}分` : `${hours}小时`;
  }
  return `${mins}分钟`;
};

/**
 * 获取统计数据
 */
const fetchStats = async () => {
  loading.value = true;
  try {
    const res = await getStudyTimeStats();
    if (res.code === 200 && res.data) {
      Object.assign(stats, res.data);
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
  } finally {
    loading.value = false;
  }
};

/**
 * 获取排名信息
 */
const fetchRanking = async () => {
  try {
    const res = await getStudyRanking();
    if (res.code === 200 && res.data) {
      Object.assign(ranking, res.data);
    }
  } catch (error) {
    console.error('获取排名失败:', error);
  }
};

/**
 * 初始化趋势图表
 */
const initTrendChart = async () => {
  if (!trendChartRef.value) return;

  chartLoading.value = true;
  try {
    const res = await getDailyStudyTime(timeRange.value);

    if (res.code === 200 && res.data) {
      const data = res.data;
      const dates = data.map(item => item.date);
      const minutes = data.map(item => item.minutes);

      if (!trendChart) {
        trendChart = echarts.init(trendChartRef.value);
      }

      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params: any) => {
            const item = params[0];
            return `${item.name}<br/>学习时长: ${formatDuration(item.value)}`;
          }
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
          boundaryGap: false,
          axisLabel: {
            formatter: (value: string) => {
              const date = new Date(value);
              return `${date.getMonth() + 1}/${date.getDate()}`;
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '分钟',
          axisLabel: {
            formatter: (value: number) => {
              const hours = Math.floor(value / 60);
              return hours > 0 ? `${hours}h` : `${value}m`;
            }
          }
        },
        series: [
          {
            name: '学习时长',
            type: 'line',
            smooth: true,
            data: minutes,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
                { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
              ])
            },
            lineStyle: {
              width: 3,
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#667eea' },
                { offset: 1, color: '#764ba2' }
              ])
            },
            itemStyle: {
              color: '#667eea'
            }
          }
        ]
      };

      trendChart.setOption(option);
    }
  } catch (error) {
    console.error('加载趋势图失败:', error);
  } finally {
    chartLoading.value = false;
  }
};

/**
 * 初始化活动类型图表
 */
const initTypeChart = async () => {
  if (!typeChartRef.value) return;

  try {
    const res = await getActivityTypeStats(timeRange.value);

    if (res.code === 200 && res.data) {
      const data = res.data;

      if (!typeChart) {
        typeChart = echarts.init(typeChartRef.value);
      }

      const typeNameMap: Record<string, string> = {
        'PRACTICE': '练习',
        'EXAM': '考试',
        'REVIEW': '复习',
        'COURSE': '课程',
        'AI_CHAT': 'AI辅导'
      };

      const pieData = data.map(item => ({
        name: typeNameMap[item.activityType] || item.activityType,
        value: item.minutes
      }));

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params: any) => {
            return `${params.name}<br/>时长: ${formatDuration(params.value)}<br/>占比: ${params.percent}%`;
          }
        },
        legend: {
          orient: 'vertical',
          right: '10%',
          top: 'center'
        },
        series: [
          {
            name: '活动类型',
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
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: pieData,
            color: ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a']
          }
        ]
      };

      typeChart.setOption(option);
    }
  } catch (error) {
    console.error('加载活动类型图失败:', error);
  }
};

/**
 * 初始化科目图表
 */
const initSubjectChart = async () => {
  if (!subjectChartRef.value) return;

  try {
    const res = await getSubjectStudyTime(timeRange.value);

    if (res.code === 200 && res.data) {
      const data = res.data;

      if (!subjectChart) {
        subjectChart = echarts.init(subjectChartRef.value);
      }

      const subjects = data.map(item => item.subjectName);
      const minutes = data.map(item => item.minutes);

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: (params: any) => {
            const item = params[0];
            return `${item.name}<br/>学习时长: ${formatDuration(item.value)}`;
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: subjects,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '分钟',
          axisLabel: {
            formatter: (value: number) => {
              const hours = Math.floor(value / 60);
              return hours > 0 ? `${hours}h` : `${value}m`;
            }
          }
        },
        series: [
          {
            name: '学习时长',
            type: 'bar',
            data: minutes,
            barWidth: '50%',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#667eea' },
                { offset: 1, color: '#764ba2' }
              ]),
              borderRadius: [8, 8, 0, 0]
            }
          }
        ]
      };

      subjectChart.setOption(option);
    }
  } catch (error) {
    console.error('加载科目图失败:', error);
  }
};

/**
 * 初始化所有图表
 */
const initCharts = () => {
  setTimeout(() => {
    initTrendChart();
    initTypeChart();
    initSubjectChart();
  }, 100);
};

/**
 * 时间范围变化
 */
const handleTimeRangeChange = () => {
  initCharts();
};

/**
 * 刷新数据
 */
const refreshData = () => {
  ElMessage.success('正在刷新数据...');
  fetchStats();
  fetchRanking();
  initCharts();
};

/**
 * 学习建议
 */
const studyTips = computed(() => {
  const tips = [];

  // 根据今日学习时长给建议
  if (stats.todayMinutes === 0) {
    tips.push({
      type: 'warning',
      title: '今日尚未学习',
      description: '开始学习吧！每天坚持学习30分钟，养成良好的学习习惯。'
    });
  } else if (stats.todayMinutes < 30) {
    tips.push({
      type: 'info',
      title: '学习时间偏少',
      description: '建议每天学习至少30分钟，保持学习的连续性。'
    });
  } else if (stats.todayMinutes >= 120) {
    tips.push({
      type: 'success',
      title: '学习状态极佳',
      description: '今天的学习时长很充足！记得适当休息，劳逸结合。'
    });
  }

  // 根据日均学习时长给建议
  if (stats.avgDailyMinutes > 0 && stats.avgDailyMinutes < 20) {
    tips.push({
      type: 'warning',
      title: '日均学习时间不足',
      description: '建议增加每日学习时长，保持稳定的学习节奏。'
    });
  }

  // 根据排名给鼓励
  if (ranking.rank > 0) {
    if (ranking.percentile >= 80) {
      tips.push({
        type: 'success',
        title: '排名优秀',
        description: `您的学习时长排名第${ranking.rank}位，超过了${ranking.percentile}%的同学，继续保持！`
      });
    } else if (ranking.percentile < 50) {
      tips.push({
        type: 'info',
        title: '还有提升空间',
        description: '增加学习时长可以提升排名，加油！'
      });
    }
  }

  // 默认建议
  if (tips.length === 0) {
    tips.push({
      type: 'info',
      title: '保持良好学习习惯',
      description: '建议每天学习30-60分钟，循序渐进，稳步提升。'
    });
  }

  return tips;
});

/**
 * 窗口大小变化时重绘图表
 */
const handleResize = () => {
  trendChart?.resize();
  typeChart?.resize();
  subjectChart?.resize();
};

// 初始化
onMounted(() => {
  fetchStats();
  fetchRanking();
  initCharts();
  window.addEventListener('resize', handleResize);
});

// 清理
onUnmounted(() => {
  trendChart?.dispose();
  typeChart?.dispose();
  subjectChart?.dispose();
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped lang="scss">
.study-time-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;

  // 页面头部
  .page-header {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 24px 32px;
      background: white;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

      .title-section {
        display: flex;
        align-items: center;
        gap: 20px;

        .header-icon {
          font-size: 48px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        h2 {
          margin: 0;
          font-size: 28px;
          font-weight: 600;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .subtitle {
          margin: 4px 0 0 0;
          font-size: 14px;
          color: #909399;
        }
      }

      .header-actions {
        display: flex;
        gap: 12px;
      }
    }
  }

  // 统计卡片
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
      border-radius: 12px;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }

      &.today {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
      }

      &.week {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: white;
      }

      &.month {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        color: white;
      }

      &.avg {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        color: white;
      }

      &.rank {
        background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
        color: white;
      }

      .stat-icon {
        color: rgba(255, 255, 255, 0.9);
      }

      .stat-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 8px;
        text-align: center;

        .stat-icon {
          font-size: 36px;
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 14px;
            opacity: 0.9;
            margin-bottom: 2px;
          }

          .stat-desc {
            font-size: 12px;
            opacity: 0.8;
          }
        }
      }
    }
  }

  // 图表容器
  .charts-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .chart-card {
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

      &.full-width {
        grid-column: 1 / -1;
      }

      &.half-width {
        grid-column: span 1;
      }

      :deep(.el-card__header) {
        padding: 16px 24px;
        border-bottom: 1px solid #f0f0f0;
      }

      :deep(.el-card__body) {
        padding: 24px;
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .header-left {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 16px;
          font-weight: 600;
          color: #303133;

          .el-icon {
            font-size: 20px;
            color: #667eea;
          }
        }
      }

      .chart-container {
        width: 100%;
        height: 350px;
      }
    }
  }

  // 学习建议卡片
  .tips-card {
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

    :deep(.el-card__header) {
      padding: 16px 24px;
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.el-card__body) {
      padding: 24px;
    }

    .tips-content {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .tip-item {
        border-radius: 8px;
      }
    }
  }
}

// 响应式
@media (max-width: 1400px) {
  .stats-cards {
    grid-template-columns: repeat(3, 1fr) !important;
  }
}

@media (max-width: 992px) {
  .study-time-page {
    padding: 12px;

    .page-header .header-content {
      flex-direction: column;
      gap: 16px;
      padding: 20px;

      .header-actions {
        width: 100%;
        flex-direction: column;

        .el-radio-group {
          width: 100%;

          :deep(.el-radio-button) {
            flex: 1;
          }
        }
      }
    }

    .stats-cards {
      grid-template-columns: repeat(2, 1fr) !important;
    }

    .charts-container {
      grid-template-columns: 1fr !important;

      .chart-card {
        &.full-width,
        &.half-width {
          grid-column: 1;
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .stats-cards {
    grid-template-columns: 1fr !important;
  }
}
</style>
