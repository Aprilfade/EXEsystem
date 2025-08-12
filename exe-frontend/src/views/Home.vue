<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="24 / 5" class="stat-card-item">
        <el-card shadow="hover">
          <div class="stat-card-v2">
            <span>学生数量</span>
            <strong>{{ stats.studentCount }}</strong>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24 / 5" class="stat-card-item">
        <el-card shadow="hover">
          <div class="stat-card-v2">
            <span>科目数量</span>
            <strong>{{ stats.subjectCount }}</strong>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24 / 5" class="stat-card-item">
        <el-card shadow="hover">
          <div class="stat-card-v2">
            <span>知识点</span>
            <strong>{{ stats.knowledgePointCount }}</strong>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24 / 5" class="stat-card-item">
        <el-card shadow="hover">
          <div class="stat-card-v2">
            <span>题目数量</span>
            <strong>{{ stats.questionCount }}</strong>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24 / 5" class="stat-card-item">
        <el-card shadow="hover">
          <div class="stat-card-v2">
            <span>试卷数量</span>
            <strong>{{ stats.paperCount }}</strong>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="main-content-row">
      <el-col :span="8">
        <el-card shadow="never" class="grid-card wrong-stats-card" style="margin-bottom: 20px;">
          <template #header><div class="card-header-v2">错题统计</div></template>
          <el-empty description="此图表需后端接口支持" />
        </el-card>
        <el-card shadow="never" class="grid-card notification-card">
          <template #header><div class="card-header-v2">系统通知</div></template>
          <ul class="notification-list">
            <el-empty v-if="!stats.notifications || stats.notifications.length === 0" description="暂无通知" :image-size="60" />
            <li v-else v-for="item in stats.notifications" :key="item.id" @click="handleNotificationClick(item.id)" class="notification-item">
              <span>{{ item.content }}</span>
              <span class="date">{{ item.date }}</span>
            </li>
          </ul>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="full-height-card">
          <template #header>
            <div class="card-header-v2">
              <span>知识点&题目统计</span>
              <div class="header-stats">
                <span>知识点: <strong>{{ stats.knowledgePointCount }}</strong></span>
                <span>题目: <strong>{{ stats.questionCount }}</strong></span>
              </div>
            </div>
          </template>
          <div ref="kpAndQuestionChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><div class="card-header-v2">科目统计</div></template>
          <el-empty description="此图表需后端接口支持(按年级统计)" />
        </el-card>
      </el-col>
    </el-row>

    <notification-preview-dialog
        v-model:visible="isPreviewVisible"
        :notification="selectedNotification"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import { getDashboardStats, type DashboardStats, type ChartData } from '@/api/dashboard';
import NotificationPreviewDialog from "@/components/notification/NotificationPreviewDialog.vue";
import { fetchNotificationById, type Notification } from '@/api/notification';
import { gsap } from "gsap";

const kpAndQuestionChart = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;
let resizeObserver: ResizeObserver | null = null;

const isPreviewVisible = ref(false);
const selectedNotification = ref<Notification | undefined>(undefined);
const stats = ref<DashboardStats>({
  studentCount: 0, subjectCount: 0, knowledgePointCount: 0,
  questionCount: 0, paperCount: 0, notifications: [],
  kpAndQuestionStats: { categories: [], series: [] }
});

const initChart = () => {
  if (kpAndQuestionChart.value) {
    chartInstance = echarts.init(kpAndQuestionChart.value);
    setChartOptions(stats.value.kpAndQuestionStats);
  }
};

const setChartOptions = (chartData: ChartData) => {
  if (!chartInstance || !chartData?.series) return;
  const seriesColors = ['#5470c6', '#91cc75'];
  chartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: chartData.series.map(s => s.name), right: '4%', top: 0, icon: 'circle', itemWidth: 8, itemHeight: 8 },
    grid: { top: '22%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: chartData.categories, axisTick: { show: false }, axisLine: { lineStyle: { color: '#DCDFE6' } }, axisLabel: { color: '#606266', interval: 0 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    series: chartData.series.map((s: any, index: number) => ({ // 为 series 添加 any 和 index 类型
      name: s.name, type: 'bar', barWidth: '12px', data: s.data,
      itemStyle: { color: seriesColors[index % seriesColors.length], borderRadius: [4, 4, 0, 0] },
    })),
    animation: true,
  });
};

const fetchData = async () => {
  try {
    const res = await getDashboardStats();
    if (res.code === 200) {
      stats.value = res.data;
    }
  } catch (error) {
    ElMessage.error("获取统计数据失败");
  }
};

onMounted(async () => {
  gsap.from(".stat-card-item", {
    duration: 0.5, y: 50, opacity: 0,
    stagger: 0.1, ease: "back.out(1.7)",
  });

  await fetchData();

  // 【核心修改】: 将图表初始化操作放入 setTimeout 中
  // 这样可以确保在 DOM 渲染完成后再执行初始化
  setTimeout(() => {
    initChart();
  }, 0); // 延迟0毫秒即可

  if (kpAndQuestionChart.value) {
    resizeObserver = new ResizeObserver(() => {
      chartInstance?.resize();
    });
    resizeObserver.observe(kpAndQuestionChart.value);
  }
});
onUnmounted(() => {
  chartInstance?.dispose();
  if (kpAndQuestionChart.value && resizeObserver) {
    resizeObserver.unobserve(kpAndQuestionChart.value);
  }
});

const handleNotificationClick = async (id: number) => {
  try {
    const res = await fetchNotificationById(id);
    if (res.code === 200) {
      selectedNotification.value = res.data;
      isPreviewVisible.value = true;
    }
  } catch (error) { /* silent fail */ }
};
</script>

/* 文件: exe-frontend/src/views/Home.vue */

<style scoped>
/* 样式部分保持不变 */
.dashboard-container {
  padding: 24px;
  background-color: #f5f7fa;
}
.stat-row {
  display: flex;
  margin-bottom: 20px;
}
.stat-row .el-col {
  flex: 1 1 0;
  min-width: 0;
}
.stat-card-v2 {
  padding: 8px 16px;
}
.stat-card-v2 span {
  font-size: 14px;
  color: #606266;
}
.stat-card-v2 strong {
  display: block;
  font-size: 30px;
  font-weight: 700;
  color: #303133;
  margin-top: 8px;
  line-height: 1.2;
}
.main-content-row {
  display: flex;
}
.card-header-v2 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}
.header-stats {
  font-size: 13px;
  font-weight: normal;
  color: #909399;
}
.header-stats span {
  margin-left: 20px;
}
.header-stats strong {
  color: #606266;
  margin-left: 4px;
}
.wrong-stats-card :deep(.el-card__body) {
  height: 220px;
}
.notification-card :deep(.el-card__body) {
  height: 220px;
}

/* --- 核心修改开始 --- */
.full-height-card {
  height: calc(220px * 2 + 68px + 32px*2); /* 保持总高度不变 */
  display: flex;
  flex-direction: column;
}

/*
  让 el-card 的内容区域 (body) 自动填充剩余空间。
  这是解决问题的关键。
*/
.full-height-card :deep(.el-card__body) {
  flex-grow: 1; /* 应用到正确的元素上 */
  display: flex; /* 让内部的 chart-container 可以使用 height: 100% */
  padding: 16px;
}

.chart-container {
  height: 100%; /* 占满父容器 (el-card__body) 的高度 */
  width: 100%;
}
/* --- 核心修改结束 --- */

.notification-list {
  list-style: none;
  padding: 0;
  margin: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.notification-list .el-empty {
  flex-grow: 1;
}
.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 4px;
  font-size: 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--border-color);
}
.notification-item:hover { background-color: #f5f7fa; }
.notification-item:last-child { border-bottom: none; }
.notification-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notification-item .date {
  color: #909399;
  flex-shrink: 0;
  margin-left: 16px;
  font-size: 13px;
}
:deep(.el-card__header) {
  padding: 16px;
}
.stat-row :deep(.el-card__body) {
  padding: 0;
}
</style>
