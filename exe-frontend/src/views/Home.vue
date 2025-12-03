<template>
  <div class="dashboard-container">
    <div style="text-align: right; margin-bottom: 15px;">
      <el-button type="primary" color="#626aef" :icon="DataLine" @click="$router.push('/data-screen')">
        进入数据大屏模式
      </el-button>
    </div>
    <el-row :gutter="20" class="stat-row">
      <el-col :span="24 / 5" class="stat-card-item">
        <el-card shadow="hover" class="online-card">
          <div class="stat-card-v2">
            <span style="color: #67C23A; display: flex; align-items: center; gap: 5px;">
              <span class="live-dot"></span> 在线学生
            </span>
            <strong>{{ onlineCount }}</strong>
          </div>
        </el-card>
      </el-col>

      <el-col :span="4" class="stat-card-item">
        <el-card shadow="hover">
          <div class="stat-card-v2">
            <span>学生总数</span>
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
          <div ref="wrongStatsChart" class="chart-container"></div>
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
              <div class="header-controls">

                <el-date-picker
                    v-model="selectedMonth"
                    type="month"
                    placeholder="选择月份"
                    format="YYYY.MM"
                    :clearable="false"
                    @change="handleMonthChange"
                    style="width: 120px;"
                />
              </div>
            </div>
          </template>

          <div class="chart-summary-cards">
            <div class="summary-card">
              <span>知识点总数</span>
              <strong>{{ stats.knowledgePointCount }}</strong>
            </div>
            <div class="summary-card">
              <span>题目总数</span>
              <strong>{{ stats.questionCount }}</strong>
            </div>
            <div class="summary-card">
              <span>题目复用率</span>
              <strong style="display: inline-flex; align-items: center; gap: 4px;">
                20%
                <el-tooltip content="此为静态数据，待后端接口支持" placement="top">
                  <el-icon><Warning /></el-icon>
                </el-tooltip>
              </strong>
            </div>
          </div>
          <div ref="kpAndQuestionChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><div class="card-header-v2">科目统计</div></template>
          <div ref="subjectStatsChart" class="chart-container" style="height: 250px;"></div>
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
import { ref, onMounted, onUnmounted , computed } from 'vue';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import { getDashboardStats, type DashboardStats, type ChartData } from '@/api/dashboard';
import NotificationPreviewDialog from "@/components/notification/NotificationPreviewDialog.vue";
import { fetchNotificationById, type Notification } from '@/api/notification';
import { gsap } from "gsap";
import { DataLine, Warning } from '@element-plus/icons-vue';
import { useNotificationSocketStore } from '@/stores/notificationSocket'; // 引入 Store
import { fetchOnlineStudentCount } from '@/api/dashboard'; // 引入新 API


const socketStore = useNotificationSocketStore();
// 计算属性直接绑定 Store 中的数据，实现 WebSocket 实时更新
const onlineCount = computed(() => socketStore.onlineStudentCount);
const kpAndQuestionChart = ref<HTMLElement | null>(null);
const subjectStatsChart = ref<HTMLElement | null>(null); // 【新增】为科目统计图表创建 ref
const wrongStatsChart = ref<HTMLElement | null>(null);
let wrongStatsChartInstance: echarts.ECharts | null = null;

let kpChartInstance: echarts.ECharts | null = null;
let subjectStatsChartInstance: echarts.ECharts | null = null; // 【新增】图表实例变量
let resizeObserver: ResizeObserver | null = null;

// 【修改 1】定义一个定时器变量
let onlineCountTimer: any = null;

const isPreviewVisible = ref(false);
const selectedNotification = ref<Notification | undefined>(undefined);
const stats = ref<DashboardStats>({
  studentCount: 0, subjectCount: 0, knowledgePointCount: 0,
  questionCount: 0, paperCount: 0, notifications: [],
  kpAndQuestionStats: { categories: [], series: [] },
  wrongQuestionStats: { categories: [], series: [] },
  monthlyQuestionCreationStats: { categories: [], series: [] },
  subjectStatsByGrade: { categories: [], series: [] }
});
const selectedMonth = ref(new Date());

const handleMonthChange = (newVal: Date | null) => {
  if (!newVal) return;
  const year = newVal.getFullYear();
  const month = newVal.getMonth() + 1;
  ElMessage.info(`您选择了 ${year}年${month}月，数据刷新功能待后端实现。`);
};

// 初始化“知识点&题目统计”图表
const initKpAndQuestionChart = (chartData: ChartData) => {
  if (kpAndQuestionChart.value && chartData?.series) {
    kpChartInstance = echarts.init(kpAndQuestionChart.value);
    const seriesColors = ['#5B93FF', '#A5D5FF'];
    kpChartInstance.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: chartData.series.map(s => s.name), right: '4%', top: 0, icon: 'circle', itemWidth: 8, itemHeight: 8 },
      grid: { top: '22%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: chartData.categories, axisTick: { show: false }, axisLine: { lineStyle: { color: '#DCDFE6' } }, axisLabel: { color: '#606266', interval: 0 } },
      yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
      series: chartData.series.map((s: any, index: number) => ({
        name: s.name, type: 'bar', barWidth: '30%', data: s.data,
        itemStyle: { color: seriesColors[index % seriesColors.length], borderRadius: [4, 4, 0, 0] },
      })),
    });
  }
};

// 【核心修改】新增错题统计图表的初始化函数
const initWrongStatsChart = (chartData: ChartData) => {
  if (wrongStatsChart.value && chartData?.series?.[0]?.data) {
    wrongStatsChartInstance = echarts.init(wrongStatsChart.value);
    wrongStatsChartInstance.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          name: '错题分布',
          type: 'pie',
          radius: ['40%', '70%'],
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
              fontSize: '20',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: chartData.categories.map((name, index) => ({
            value: chartData.series[0].data[index],
            name: name
          }))
        }
      ]
    });
  }
};

// 【修改】将这个函数替换为下面的新版本
const initSubjectStatsChart = (chartData: ChartData) => {
  if (subjectStatsChart.value && chartData?.series?.[0]?.data) {
    subjectStatsChartInstance = echarts.init(subjectStatsChart.value);
    subjectStatsChartInstance.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: { top: 40, left: 50, right: 20, bottom: 30 },
      xAxis: {
        type: 'category',
        data: chartData.categories,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#DCDFE6' } },
        axisLabel: { color: '#606266' }
      },
      yAxis: {
        type: 'value',
        name: '学生数',
        splitLine: { lineStyle: { type: 'dashed' } }
      },
      series: [{
        name: '学生数量',
        // 1. 将图表类型改为 'bar'
        type: 'bar',
        // 2. 设置柱子宽度
        barWidth: '40%',
        data: chartData.series[0].data,
        // 3. (可选) 为柱子顶部添加圆角，使其更美观
        itemStyle: {
          color: '#5B93FF', // 统一颜色风格
          borderRadius: [4, 4, 0, 0]
        },
      }],
    });
  }
};

const fetchData = async () => {
  try {
    const res = await getDashboardStats();
    if (res.code === 200) {
      stats.value = res.data;
      initKpAndQuestionChart(stats.value.kpAndQuestionStats);
      initSubjectStatsChart(stats.value.subjectStatsByGrade);
      // 【核心修改】调用新增的图表初始化函数
      initWrongStatsChart(stats.value.wrongQuestionStats);

    } else {
      ElMessage.error(res.msg || '获取统计数据失败');
    }
  } catch (error) {
    console.error("获取工作台数据时发生错误:", error);
    ElMessage.error("获取统计数据失败");
  }
};

onMounted(async () => {
  gsap.from(".stat-card-item", {
    duration: 0.5, y: 50, opacity: 0,
    stagger: 0.1, ease: "back.out(1.7)",
  });

  await fetchData();

  if (kpAndQuestionChart.value) {
    resizeObserver = new ResizeObserver(() => {
      kpChartInstance?.resize();
      subjectStatsChartInstance?.resize();
      wrongStatsChartInstance?.resize();
    });
    resizeObserver.observe(kpAndQuestionChart.value);
  }

  // 【修改 3】立即获取一次，并启动定时轮询（每 5 秒刷新一次）
  await updateOnlineCount();
  onlineCountTimer = setInterval(updateOnlineCount, 5000);
});

onUnmounted(() => {
  kpChartInstance?.dispose();
  subjectStatsChartInstance?.dispose();
  wrongStatsChartInstance?.dispose();
  if (kpAndQuestionChart.value && resizeObserver) {
    resizeObserver.unobserve(kpAndQuestionChart.value);
  }

  // 【修改 4】组件卸载时清除定时器
  if (onlineCountTimer) {
    clearInterval(onlineCountTimer);
    onlineCountTimer = null;
  }
});


onUnmounted(() => {
  kpChartInstance?.dispose();
  subjectStatsChartInstance?.dispose();
  wrongStatsChartInstance?.dispose();
  if (kpAndQuestionChart.value && resizeObserver) {
    resizeObserver.unobserve(kpAndQuestionChart.value);
  }

  // 【修改 4】组件卸载时清除定时器
  if (onlineCountTimer) {
    clearInterval(onlineCountTimer);
    onlineCountTimer = null;
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
// 【修改 2】封装更新在线人数的方法
const updateOnlineCount = async () => {
  try {
    const res = await fetchOnlineStudentCount();
    if (res.code === 200) {
      socketStore.onlineStudentCount = res.data.count;
    }
  } catch (e) {
    console.error("获取在线人数失败", e);
  }
};
</script>

/* 文件: exe-frontend/src/views/Home.vue */

<style scoped>
.wrong-stats-card :deep(.el-card__body),
.notification-card :deep(.el-card__body) {
  height: 220px;
}
.wrong-stats-card .chart-container {
  height: 100%;
  width: 100%;
}
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
  padding: 20px 6px;
  font-size: 18px;
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
/* --- 在这里追加下面的新样式 --- */

.chart-summary-cards {
  display: flex;
  justify-content: space-around;
  padding: 0 20px 20px; /* 顶部无间距，底部20px间距 */
  border-bottom: 1px solid var(--border-color); /* 在卡片和图表之间添加一条分割线 */
  margin-bottom: 20px; /* 与下方图表的间距 */
}

.summary-card {
  text-align: center;
}

.summary-card span {
  font-size: 14px;
  color: var(--text-color-regular);
  display: block;
  margin-bottom: 8px;
}

.summary-card strong {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color-primary);
}

/* 调整图表容器的高度以适应新增的卡片 */
.full-height-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column; /* 让内部元素垂直排列 */
  flex-grow: 1;
  padding: 16px;
}

.chart-container {
  flex-grow: 1; /* 让图表容器填充剩余空间 */
  height: 100%;
  width: 100%;
}
/* --- 在这里追加下面的新样式 --- */

.header-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}
/* 【新增】在线状态呼吸灯动画 */
.live-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: #67C23A;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}
</style>
