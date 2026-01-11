<template>
  <div class="learning-analytics-wrapper">
    <!-- 关键指标卡片 -->
    <div class="metrics-cards">
      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <el-icon class="metric-icon" :size="40" color="#409EFF"><Clock /></el-icon>
          <div class="metric-info">
            <div class="metric-label">总学习时长</div>
            <div class="metric-value">{{ formatStudyTime(totalStudyTime) }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <el-icon class="metric-icon" :size="40" color="#67C23A"><CircleCheck /></el-icon>
          <div class="metric-info">
            <div class="metric-label">已完成资源</div>
            <div class="metric-value">{{ completedResourceCount }} / {{ totalResourceCount }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <el-icon class="metric-icon" :size="40" color="#E6A23C"><TrendCharts /></el-icon>
          <div class="metric-info">
            <div class="metric-label">课程完成率</div>
            <div class="metric-value">{{ courseCompletionRate }}%</div>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <el-icon class="metric-icon" :size="40" color="#F56C6C"><Calendar /></el-icon>
          <div class="metric-info">
            <div class="metric-label">平均进度</div>
            <div class="metric-value">{{ averageProgress }}%</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 资源类型分布饼图 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">资源类型分布</span>
            <el-tag size="small">总计 {{ totalResourceCount }} 个</el-tag>
          </div>
        </template>
        <div ref="pieChartRef" class="chart-container"></div>
      </el-card>

      <!-- 学习进度统计图 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">学习进度统计</span>
            <el-tag size="small" type="success">{{ completedResourceCount }} 已完成</el-tag>
          </div>
        </template>
        <div ref="progressChartRef" class="chart-container"></div>
      </el-card>
    </div>

    <!-- 详细数据表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">学习详情</span>
          <el-button type="primary" size="small" @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </template>

      <el-table :data="progressTableData" style="width: 100%" :default-sort="{ prop: 'progressPercent', order: 'descending' }">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="resourceName" label="资源名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="resourceType" label="资源类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getResourceTypeTag(row.resourceType)" size="small">
              {{ row.resourceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progressPercent" label="完成进度" width="150" sortable>
          <template #default="{ row }">
            <el-progress
              :percentage="row.progressPercent"
              :color="row.progressPercent >= 95 ? '#67C23A' : '#409EFF'"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
        <el-table-column prop="studyDuration" label="学习时长" width="120" sortable>
          <template #default="{ row }">
            {{ formatStudyTime(row.studyDuration) }}
          </template>
        </el-table-column>
        <el-table-column prop="isCompleted" label="状态" width="100" align="center" sortable>
          <template #default="{ row }">
            <el-tag v-if="row.isCompleted === 1" type="success" size="small">
              <el-icon><CircleCheck /></el-icon>
              已完成
            </el-tag>
            <el-tag v-else type="info" size="small">学习中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="最后学习时间" width="160" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.updatedTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import * as echarts from 'echarts';
import type { ECharts } from 'echarts';
import { useCourseStore } from '@/stores/courseStore';
import { useCourseProgress } from '@/hooks/useCourseProgress';
import {
  Clock,
  CircleCheck,
  TrendCharts,
  Calendar,
  Refresh
} from '@element-plus/icons-vue';

/**
 * 组件属性
 */
interface Props {
  courseId?: number;
}

const props = defineProps<Props>();

// Hooks
const store = useCourseStore();
const { totalStudyTime, completedResourceCount, totalResourceCount, formatStudyTime } = useCourseProgress();

// 图表引用
const pieChartRef = ref<HTMLElement>();
const progressChartRef = ref<HTMLElement>();
let pieChart: ECharts | null = null;
let progressChart: ECharts | null = null;

/**
 * 课程完成率
 */
const courseCompletionRate = computed(() => {
  return store.courseCompletionRate;
});

/**
 * 平均进度
 */
const averageProgress = computed(() => {
  if (store.resources.length === 0) return 0;

  const totalProgress = store.resources.reduce((sum, resource) => {
    const progress = store.progressMap.get(resource.id);
    return sum + (progress?.progressPercent || 0);
  }, 0);

  return Math.floor(totalProgress / store.resources.length);
});

/**
 * 资源类型统计
 */
const resourceTypeStats = computed(() => {
  const stats = new Map<string, number>();

  store.resources.forEach(resource => {
    const type = resource.resourceType;
    stats.set(type, (stats.get(type) || 0) + 1);
  });

  return Array.from(stats.entries()).map(([name, value]) => ({ name, value }));
});

/**
 * 进度表格数据
 */
const progressTableData = computed(() => {
  return store.resources.map(resource => {
    const progress = store.progressMap.get(resource.id);
    return {
      resourceId: resource.id,
      resourceName: resource.name,
      resourceType: resource.resourceType,
      progressPercent: progress?.progressPercent || 0,
      studyDuration: progress?.studyDuration || 0,
      isCompleted: progress?.isCompleted || 0,
      updatedTime: progress?.updatedTime || ''
    };
  });
});

/**
 * 初始化饼图
 */
const initPieChart = () => {
  if (!pieChartRef.value) return;

  // 检查DOM尺寸，如果为0则延迟初始化
  const width = pieChartRef.value.clientWidth;
  const height = pieChartRef.value.clientHeight;

  if (width === 0 || height === 0) {
    // 延迟200ms重试
    setTimeout(() => initPieChart(), 200);
    return;
  }

  pieChart = echarts.init(pieChartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 个 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '资源类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c} 个'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: resourceTypeStats.value.map(item => ({
          name: getResourceTypeName(item.name),
          value: item.value
        })),
        color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
      }
    ]
  };

  pieChart.setOption(option);
};

/**
 * 初始化进度统计图
 */
const initProgressChart = () => {
  if (!progressChartRef.value) return;

  // 检查DOM尺寸，如果为0则延迟初始化
  const width = progressChartRef.value.clientWidth;
  const height = progressChartRef.value.clientHeight;

  if (width === 0 || height === 0) {
    // 延迟200ms重试
    setTimeout(() => initProgressChart(), 200);
    return;
  }

  progressChart = echarts.init(progressChartRef.value);

  // 统计进度分布
  const progressRanges = [
    { name: '0-20%', min: 0, max: 20, count: 0 },
    { name: '20-40%', min: 20, max: 40, count: 0 },
    { name: '40-60%', min: 40, max: 60, count: 0 },
    { name: '60-80%', min: 60, max: 80, count: 0 },
    { name: '80-95%', min: 80, max: 95, count: 0 },
    { name: '已完成', min: 95, max: 100, count: 0 }
  ];

  progressTableData.value.forEach(item => {
    const range = progressRanges.find(
      r => item.progressPercent >= r.min && item.progressPercent <= r.max
    );
    if (range) {
      range.count++;
    }
  });

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
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
      data: progressRanges.map(r => r.name),
      axisLabel: {
        interval: 0,
        rotate: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '资源数量',
      minInterval: 1
    },
    series: [
      {
        name: '资源数量',
        type: 'bar',
        data: progressRanges.map(r => r.count),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ]),
          borderRadius: [8, 8, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  };

  progressChart.setOption(option);
};

/**
 * 获取资源类型名称
 */
const getResourceTypeName = (type: string): string => {
  const typeMap: Record<string, string> = {
    VIDEO: '视频',
    PDF: 'PDF文档',
    PPT: 'PPT课件',
    LINK: '外部链接'
  };
  return typeMap[type] || type;
};

/**
 * 获取资源类型标签
 */
const getResourceTypeTag = (type: string): string => {
  const tagMap: Record<string, string> = {
    VIDEO: 'primary',
    PDF: 'danger',
    PPT: 'warning',
    LINK: 'success'
  };
  return tagMap[type] || 'info';
};

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTimeStr: string): string => {
  if (!dateTimeStr) return '-';
  return dateTimeStr.replace('T', ' ').substring(0, 16);
};

/**
 * 刷新数据
 */
const refreshData = () => {
  // 重新初始化图表
  nextTick(() => {
    initPieChart();
    initProgressChart();
  });
};

/**
 * 监听资源变化，更新图表
 */
watch(() => store.resources.length, () => {
  refreshData();
});

/**
 * 监听进度变化，更新图表
 */
watch(() => store.progressMap.size, () => {
  refreshData();
});

/**
 * 组件挂载
 */
onMounted(() => {
  nextTick(() => {
    initPieChart();
    initProgressChart();

    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      pieChart?.resize();
      progressChart?.resize();
    });
  });
});

/**
 * 组件卸载
 */
onUnmounted(() => {
  pieChart?.dispose();
  progressChart?.dispose();
  window.removeEventListener('resize', () => {});
});
</script>

<script lang="ts">
import { onUnmounted } from 'vue';
</script>

<style scoped lang="scss">
.learning-analytics-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
  background: #f5f7fa;
}

// 关键指标卡片
.metrics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.metric-card {
  border-radius: 12px;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.metric-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.metric-icon {
  flex-shrink: 0;
}

.metric-info {
  flex: 1;
}

.metric-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

// 图表区域
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  border-radius: 12px;

  :deep(.el-card__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 15px 20px;
    border-bottom: none;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.chart-container {
  width: 100%;
  height: 300px;
}

// 表格卡片
.table-card {
  border-radius: 12px;

  :deep(.el-card__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 15px 20px;
    border-bottom: none;

    .card-title {
      color: #fff;
    }
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .metrics-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .metric-value {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .metrics-cards {
    grid-template-columns: 1fr;
  }
}
</style>
