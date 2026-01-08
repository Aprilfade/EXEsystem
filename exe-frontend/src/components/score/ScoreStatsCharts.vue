<template>
  <div class="score-stats-charts">
    <!-- 成绩分布图 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">成绩分布统计</span>
          <el-tag type="info" size="small">共 {{ stats.totalCount }} 人</el-tag>
        </div>
      </template>
      <div ref="distributionChart" class="chart-container"></div>
    </el-card>

    <!-- 数据详情卡片 -->
    <el-row :gutter="20" class="detail-row">
      <el-col :span="8">
        <el-card shadow="never" class="detail-card">
          <el-statistic title="最高分" :value="stats.maxScore" suffix="分">
            <template #prefix>
              <el-icon color="#67C23A"><TrophyBase /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="detail-card">
          <el-statistic title="最低分" :value="stats.minScore" suffix="分">
            <template #prefix>
              <el-icon color="#F56C6C"><WarningFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="detail-card">
          <el-statistic title="及格人数" :value="stats.passCount" suffix="人">
            <template #prefix>
              <el-icon color="#409EFF"><CircleCheckFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 成绩分段详情表格 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <span class="card-title">成绩分段详情</span>
      </template>
      <el-table :data="distributionData" stripe border>
        <el-table-column prop="range" label="分数段" width="120" align="center" />
        <el-table-column prop="count" label="人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.range)" size="small">{{ row.count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="占比" width="120" align="center">
          <template #default="{ row }">
            {{ ((row.count / stats.totalCount) * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column label="进度" min-width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="(row.count / stats.totalCount) * 100"
              :stroke-width="16"
              :color="getProgressColor(row.range)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 成绩分析 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <span class="card-title">成绩分析总结</span>
      </template>
      <div class="analysis-content">
        <el-alert
          :title="getAnalysisTitle()"
          type="info"
          :closable="false"
          style="margin-bottom: 16px;"
        >
          <template #default>
            <div class="analysis-text">
              {{ getAnalysisText() }}
            </div>
          </template>
        </el-alert>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="平均分">
            <el-tag type="primary" size="large">{{ stats.averageScore?.toFixed(2) }} 分</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="中位数区间">
            <el-tag type="success" size="large">{{ getMedianRange() }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="及格率">
            <el-progress
              :percentage="stats.passRate"
              :color="stats.passRate >= 80 ? '#67C23A' : stats.passRate >= 60 ? '#E6A23C' : '#F56C6C'"
              :stroke-width="20"
            >
              <span style="color: #303133; font-weight: 600;">{{ stats.passRate?.toFixed(1) }}%</span>
            </el-progress>
          </el-descriptions-item>
          <el-descriptions-item label="优秀率">
            <el-progress
              :percentage="stats.excellentRate"
              :color="stats.excellentRate >= 30 ? '#67C23A' : stats.excellentRate >= 15 ? '#E6A23C' : '#F56C6C'"
              :stroke-width="20"
            >
              <span style="color: #303133; font-weight: 600;">{{ stats.excellentRate?.toFixed(1) }}%</span>
            </el-progress>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, nextTick } from 'vue';
import * as echarts from 'echarts';
import { TrophyBase, WarningFilled, CircleCheckFilled } from '@element-plus/icons-vue';
import type { ScoreStats } from '@/api/score';

const props = defineProps<{
  stats: ScoreStats;
  queryParams: any;
}>();

const distributionChart = ref<HTMLElement>();
let distributionChartInstance: echarts.ECharts | null = null;

// 成绩分布数据
const distributionData = computed(() => {
  if (!props.stats.scoreDistribution) return [];
  return [
    { range: '0-59', count: props.stats.scoreDistribution['0-59'] || 0, label: '不及格' },
    { range: '60-69', count: props.stats.scoreDistribution['60-69'] || 0, label: '及格' },
    { range: '70-79', count: props.stats.scoreDistribution['70-79'] || 0, label: '中等' },
    { range: '80-89', count: props.stats.scoreDistribution['80-89'] || 0, label: '良好' },
    { range: '90-100', count: props.stats.scoreDistribution['90-100'] || 0, label: '优秀' }
  ];
});

/**
 * 初始化成绩分布图表
 */
const initDistributionChart = () => {
  if (!distributionChart.value) return;

  if (distributionChartInstance) {
    distributionChartInstance.dispose();
  }

  distributionChartInstance = echarts.init(distributionChart.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const data = params[0];
        const total = props.stats.totalCount || 1;
        const percent = ((data.value / total) * 100).toFixed(1);
        return `${data.name}<br/>人数: ${data.value}<br/>占比: ${percent}%`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: distributionData.value.map(d => d.range),
      axisLabel: {
        interval: 0,
        rotate: 0,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      minInterval: 1
    },
    series: [
      {
        name: '人数',
        type: 'bar',
        data: distributionData.value.map((d) => ({
          value: d.count,
          itemStyle: {
            color: getBarColor(d.range)
          }
        })),
        label: {
          show: true,
          position: 'top',
          formatter: '{c}人'
        },
        barWidth: '50%'
      }
    ]
  };

  distributionChartInstance.setOption(option);

  // 响应式调整
  window.addEventListener('resize', () => {
    distributionChartInstance?.resize();
  });
};

/**
 * 获取柱状图颜色
 */
const getBarColor = (range: string) => {
  const colors: Record<string, string> = {
    '0-59': '#F56C6C',
    '60-69': '#E6A23C',
    '70-79': '#409EFF',
    '80-89': '#67C23A',
    '90-100': '#1890FF'
  };
  return colors[range] || '#909399';
};

/**
 * 获取Tag类型
 */
const getTagType = (range: string) => {
  const types: Record<string, any> = {
    '0-59': 'danger',
    '60-69': 'warning',
    '70-79': 'info',
    '80-89': 'success',
    '90-100': 'primary'
  };
  return types[range] || 'info';
};

/**
 * 获取进度条颜色
 */
const getProgressColor = (range: string) => {
  const colors: Record<string, string> = {
    '0-59': '#F56C6C',
    '60-69': '#E6A23C',
    '70-79': '#409EFF',
    '80-89': '#67C23A',
    '90-100': '#1890FF'
  };
  return colors[range] || '#909399';
};

/**
 * 获取中位数区间
 */
const getMedianRange = () => {
  let cumulative = 0;
  const half = (props.stats.totalCount || 0) / 2;

  for (const item of distributionData.value) {
    cumulative += item.count;
    if (cumulative >= half) {
      return item.range;
    }
  }

  return '60-69';
};

/**
 * 获取分析标题
 */
const getAnalysisTitle = () => {
  const passRate = props.stats.passRate || 0;
  if (passRate >= 90) return '优秀';
  if (passRate >= 80) return '良好';
  if (passRate >= 60) return '及格';
  return '需要改进';
};

/**
 * 获取分析文本
 */
const getAnalysisText = () => {
  const { totalCount, averageScore, passRate, excellentRate } = props.stats;

  const texts = [];
  texts.push(`本次考试共有 ${totalCount} 人参加`);
  texts.push(`平均分为 ${averageScore?.toFixed(2)} 分`);

  if (passRate >= 90) {
    texts.push('及格率优秀，整体表现非常好');
  } else if (passRate >= 80) {
    texts.push('及格率良好，大部分学生掌握了知识点');
  } else if (passRate >= 60) {
    texts.push('及格率中等，还有提升空间');
  } else {
    texts.push('及格率偏低，需要加强辅导');
  }

  if (excellentRate >= 30) {
    texts.push('优秀率较高，学生整体素质较好');
  } else if (excellentRate >= 15) {
    texts.push('优秀率中等，有一定数量的优等生');
  } else {
    texts.push('优秀率偏低，需要培养尖子生');
  }

  return texts.join('，') + '。';
};

// 监听数据变化，重新渲染图表
watch(() => props.stats, () => {
  nextTick(() => {
    initDistributionChart();
  });
}, { deep: true });

onMounted(() => {
  nextTick(() => {
    initDistributionChart();
  });
});
</script>

<style scoped>
.score-stats-charts {
  padding: 20px;
}

.chart-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary, #303133);
}

.chart-container {
  width: 100%;
  height: 400px;
}

.detail-row {
  margin-bottom: 20px;
}

.detail-card {
  text-align: center;
  border-radius: 8px;
}

.analysis-content {
  padding: 10px 0;
}

.analysis-text {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-color-regular, #606266);
}

/* 深色模式适配 */
.dark .chart-card {
  background: #2d2d2d;
  border-color: #404040;
}

.dark .detail-card {
  background: #2d2d2d;
  border-color: #404040;
}
</style>
