<template>
  <div class="paper-knowledge-point-chart">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">知识点分布</span>
          <el-radio-group v-model="chartType" size="small">
            <el-radio-button label="pie">饼图</el-radio-button>
            <el-radio-button label="bar">柱状图</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-skeleton :loading="loading" animated>
        <template #template>
          <el-skeleton-item variant="image" style="width: 100%; height: 400px;" />
        </template>
        <template #default>
          <div v-if="knowledgePoints.length > 0">
            <div ref="chartRef" class="chart-container"></div>

            <!-- 知识点详情表格 -->
            <el-divider>知识点详情</el-divider>
            <el-table :data="knowledgePoints" border stripe>
              <el-table-column prop="knowledgePointName" label="知识点" min-width="150" />
              <el-table-column prop="questionCount" label="题目数" width="100" align="center">
                <template #default="{ row }">
                  <el-tag type="primary" size="small">{{ row.questionCount }} 题</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="totalScore" label="分值" width="100" align="center">
                <template #default="{ row }">
                  <el-tag type="success" size="small">{{ row.totalScore }} 分</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="percentage" label="占比" width="120" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="row.percentage"
                    :stroke-width="16"
                    :color="getColorByPercentage(row.percentage)"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无知识点数据" />
        </template>
      </el-skeleton>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';
import type { ECharts } from 'echarts';
import { fetchPaperKnowledgePoints, type PaperKnowledgePointDTO } from '@/api/paper';
import { ElMessage } from 'element-plus';

const props = defineProps<{
  paperId: number;
}>();

const loading = ref(false);
const knowledgePoints = ref<PaperKnowledgePointDTO[]>([]);
const chartType = ref<'pie' | 'bar'>('pie');
const chartRef = ref<HTMLDivElement>();
let chartInstance: ECharts | null = null;

// 获取知识点分布数据
const loadKnowledgePoints = async () => {
  loading.value = true;
  try {
    const res = await fetchPaperKnowledgePoints(props.paperId);
    if (res.code === 200 && res.data) {
      knowledgePoints.value = res.data;
      await nextTick();
      initChart();
    }
  } catch (error) {
    ElMessage.error('获取知识点分布失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 初始化图表
const initChart = () => {
  if (!chartRef.value || knowledgePoints.value.length === 0) return;

  if (chartInstance) {
    chartInstance.dispose();
  }

  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

// 更新图表
const updateChart = () => {
  if (!chartInstance) return;

  const option = chartType.value === 'pie' ? getPieOption() : getBarOption();
  chartInstance.setOption(option);
};

// 饼图配置
const getPieOption = () => {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}分 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      type: 'scroll',
      pageIconColor: '#409EFF',
      pageIconInactiveColor: '#aaa',
      pageTextStyle: {
        color: '#666'
      }
    },
    series: [
      {
        name: '知识点分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: knowledgePoints.value.map(item => ({
          name: item.knowledgePointName,
          value: item.totalScore
        }))
      }
    ],
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
  };
};

// 柱状图配置
const getBarOption = () => {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const item = params[0];
        const kp = knowledgePoints.value[item.dataIndex];
        return `${item.name}<br/>分值: ${item.value}分<br/>题目数: ${kp.questionCount}题<br/>占比: ${kp.percentage.toFixed(1)}%`;
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
      data: knowledgePoints.value.map(item => item.knowledgePointName),
      axisLabel: {
        interval: 0,
        rotate: 30,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '分值'
    },
    series: [
      {
        name: '分值',
        type: 'bar',
        data: knowledgePoints.value.map(item => item.totalScore),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        barMaxWidth: 50
      }
    ]
  };
};

// 根据百分比获取颜色
const getColorByPercentage = (percentage: number) => {
  if (percentage >= 30) return '#F56C6C';
  if (percentage >= 20) return '#E6A23C';
  if (percentage >= 10) return '#409EFF';
  return '#67C23A';
};

// 监听图表类型变化
watch(chartType, () => {
  updateChart();
});

// 窗口大小变化时调整图表
const handleResize = () => {
  chartInstance?.resize();
};

onMounted(() => {
  loadKnowledgePoints();
  window.addEventListener('resize', handleResize);
});

// 组件卸载时销毁图表
import { onUnmounted } from 'vue';
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped lang="scss">
.paper-knowledge-point-chart {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .chart-container {
    width: 100%;
    height: 400px;
    margin-bottom: 20px;
  }
}
</style>
