<template>
  <div class="data-screen-container">
    <header class="screen-header">
      <div class="header-left">
        <span class="time">{{ currentTime }}</span>
      </div>
      <div class="header-center">
        <h1>智慧教育大数据指挥中心</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" link @click="exitScreen">退出大屏</el-button>
      </div>
    </header>

    <div class="screen-body" v-loading="loading" element-loading-background="rgba(0, 0, 0, 0.7)">
      <div class="column left-col">
        <div class="chart-box">
          <div class="box-title">题库建设趋势 (近12个月)</div>
          <div ref="trendChartRef" class="chart"></div>
        </div>
        <div class="chart-box">
          <div class="box-title">学科资源投入 (知识点/题目)</div>
          <div ref="resourceChartRef" class="chart"></div>
        </div>
      </div>

      <div class="column center-col">
        <div class="digital-board">
          <div class="digital-item">
            <div class="label">实时在线</div>
            <div class="value highlight">{{ onlineCount }}</div>
          </div>
          <div class="digital-item">
            <div class="label">学生总数</div>
            <div class="value">{{ stats.studentCount }}</div>
          </div>
          <div class="digital-item">
            <div class="label">题库总量</div>
            <div class="value">{{ stats.questionCount }}</div>
          </div>
          <div class="digital-item">
            <div class="label">试卷总数</div>
            <div class="value">{{ stats.paperCount }}</div>
          </div>
        </div>

        <div class="map-box">
          <div class="box-title">年级/科目学生分布</div>
          <div ref="gradeChartRef" class="chart main-chart"></div>
        </div>
      </div>

      <div class="column right-col">
        <div class="chart-box">
          <div class="box-title">错题高发学科 TOP5</div>
          <div ref="wrongChartRef" class="chart"></div>
        </div>
        <div class="chart-box notification-box">
          <div class="box-title">系统实时公告</div>
          <div class="notification-list">
            <div v-for="(item, index) in stats.notifications" :key="index" class="notice-item">
              <span class="notice-time">{{ item.date }}</span>
              <span class="notice-content">{{ item.content }}</span>
            </div>
            <el-empty v-if="!stats.notifications || stats.notifications.length === 0" description="暂无公告" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import { getDashboardStats, fetchOnlineStudentCount } from '@/api/dashboard';
import { useNotificationSocketStore } from '@/stores/notificationSocket';
import dayjs from 'dayjs';

const router = useRouter();
const socketStore = useNotificationSocketStore();

// 数据状态
const loading = ref(true);
const currentTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'));
const stats = ref<any>({});
const onlineCount = computed(() => socketStore.onlineStudentCount);

// 图表 Refs
const trendChartRef = ref<HTMLElement | null>(null);
const resourceChartRef = ref<HTMLElement | null>(null);
const gradeChartRef = ref<HTMLElement | null>(null);
const wrongChartRef = ref<HTMLElement | null>(null);

// 定时器
let timeTimer: any = null;
let dataTimer: any = null;
let chartInstances: echarts.ECharts[] = [];

// ECharts 通用暗色配置
const darkTheme = {
  textStyle: { color: '#ccc' },
  title: { textStyle: { color: '#fff' } },
  line: { itemStyle: { color: '#00f2f1' } }
};

// 1. 初始化图表 - 题库趋势
const initTrendChart = () => {
  if (!trendChartRef.value) return;
  const chart = echarts.init(trendChartRef.value);
  const data = stats.value.monthlyQuestionCreationStats || { categories: [], series: [] };

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: '15%', bottom: '10%', left: '10%', right: '5%' },
    xAxis: {
      type: 'category',
      data: data.categories,
      axisLine: { lineStyle: { color: '#404a59' } },
      axisLabel: { color: '#ccc' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#404a59', type: 'dashed' } },
      axisLabel: { color: '#ccc' }
    },
    series: [{
      data: data.series[0]?.data || [],
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 242, 241, 0.6)' },
          { offset: 1, color: 'rgba(0, 242, 241, 0)' }
        ])
      },
      itemStyle: { color: '#00f2f1' }
    }]
  });
  chartInstances.push(chart);
};

// 2. 初始化图表 - 学科资源
const initResourceChart = () => {
  if (!resourceChartRef.value) return;
  const chart = echarts.init(resourceChartRef.value);
  const data = stats.value.kpAndQuestionStats || { categories: [], series: [] };

  // 只取前5个科目以免拥挤
  const categories = data.categories.slice(0, 6);
  const seriesKp = data.series[0]?.data.slice(0, 6) || [];
  const seriesQ = data.series[1]?.data.slice(0, 6) || [];

  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { textStyle: { color: '#ccc' }, bottom: 0 },
    grid: { top: '10%', bottom: '15%', left: '5%', right: '5%', containLabel: true },
    xAxis: { type: 'value', splitLine: { show: false }, axisLabel: { color: '#ccc' } },
    yAxis: { type: 'category', data: categories, axisLabel: { color: '#ccc' } },
    series: [
      { name: '知识点', type: 'bar', stack: 'total', data: seriesKp, itemStyle: { color: '#e6a23c' } },
      { name: '题目', type: 'bar', stack: 'total', data: seriesQ, itemStyle: { color: '#409eff' } }
    ]
  });
  chartInstances.push(chart);
};

// 3. 初始化图表 - 错题分布 (右侧)
const initWrongChart = () => {
  if (!wrongChartRef.value) return;
  const chart = echarts.init(wrongChartRef.value);
  const data = stats.value.wrongQuestionStats || { categories: [], series: [] };

  // 构造饼图数据
  const pieData = data.categories.map((name: string, i: number) => ({
    name, value: data.series[0]?.data[i]
  })).sort((a: any, b: any) => b.value - a.value).slice(0, 5);

  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left', textStyle: { color: '#ccc' } },
    series: [{
      name: '错题分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      itemStyle: {
        borderRadius: 5,
        borderColor: '#0f1c3a',
        borderWidth: 2
      },
      data: pieData,
      label: { color: '#fff' }
    }]
  });
  chartInstances.push(chart);
};

// 4. 初始化图表 - 年级统计 (中间)
const initGradeChart = () => {
  if (!gradeChartRef.value) return;
  const chart = echarts.init(gradeChartRef.value);
  const data = stats.value.subjectStatsByGrade || { categories: [], series: [] };

  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.categories,
      axisLabel: { color: '#fff', interval: 0, rotate: 30 }
    },
    yAxis: { type: 'value', splitLine: { show: false }, axisLabel: { color: '#fff' } },
    series: [{
      data: data.series[0]?.data || [],
      type: 'bar',
      showBackground: true,
      backgroundStyle: { color: 'rgba(180, 180, 180, 0.2)' },
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      }
    }]
  });
  chartInstances.push(chart);
};

const fetchData = async () => {
  try {
    const res = await getDashboardStats();
    if (res.code === 200) {
      stats.value = res.data;
      // 重新渲染所有图表
      chartInstances.forEach(c => c.dispose());
      chartInstances = [];
      initTrendChart();
      initResourceChart();
      initWrongChart();
      initGradeChart();
    }

    // 更新在线人数
    const onlineRes = await fetchOnlineStudentCount();
    if (onlineRes.code === 200) {
      socketStore.onlineStudentCount = onlineRes.data.count;
    }
  } finally {
    loading.value = false;
  }
};

const exitScreen = () => {
  router.push('/home');
};

const handleResize = () => {
  chartInstances.forEach(chart => chart.resize());
};

onMounted(() => {
  fetchData();

  // 启动时钟
  timeTimer = setInterval(() => {
    currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
  }, 1000);

  // 启动数据轮询 (30秒刷新一次)
  dataTimer = setInterval(fetchData, 30000);

  window.addEventListener('resize', handleResize);

  // 尝试进入全屏
  document.documentElement.requestFullscreen().catch(() => {});
});

onUnmounted(() => {
  clearInterval(timeTimer);
  clearInterval(dataTimer);
  window.removeEventListener('resize', handleResize);
  chartInstances.forEach(c => c.dispose());
});
</script>

<style scoped>
.data-screen-container {
  width: 100vw;
  height: 100vh;
  background-color: #0f1c3a; /* 深蓝背景 */
  color: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  font-family: "Microsoft YaHei", sans-serif;
}

/* 头部 */
.screen-header {
  height: 80px;
  background: url('/src/assets/header-bg.png') no-repeat center center; /* 假设有个背景图，或者用CSS渐变 */
  background-size: cover;
  background-color: #0b142b;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  border-bottom: 2px solid #00f2f1;
}
.header-center h1 {
  font-size: 32px;
  letter-spacing: 4px;
  text-shadow: 0 0 10px #00f2f1;
  margin: 0;
  background: linear-gradient(to bottom, #fff, #b3e5fc);
  -webkit-background-clip: text;
  color: transparent;
}
.time { font-size: 18px; color: #00f2f1; font-family: monospace; }

/* 主体布局 */
.screen-body {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
}
.column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.left-col, .right-col { flex: 1; }
.center-col { flex: 2; }

/* 通用卡片样式 */
.chart-box {
  flex: 1;
  background: rgba(16, 30, 60, 0.6);
  border: 1px solid rgba(0, 242, 241, 0.2);
  border-radius: 8px;
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 15px;
  box-shadow: inset 0 0 20px rgba(0, 242, 241, 0.05);
}
.chart-box::before {
  content: "";
  position: absolute;
  top: 0; left: 0; width: 10px; height: 10px;
  border-top: 2px solid #00f2f1;
  border-left: 2px solid #00f2f1;
}
.chart-box::after {
  content: "";
  position: absolute;
  bottom: 0; right: 0; width: 10px; height: 10px;
  border-bottom: 2px solid #00f2f1;
  border-right: 2px solid #00f2f1;
}

.box-title {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  padding-left: 10px;
  border-left: 4px solid #00f2f1;
  margin-bottom: 15px;
}

.chart { flex: 1; width: 100%; min-height: 200px; }

/* 中间数字大屏 */
.digital-board {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 15px;
}
.digital-item {
  flex: 1;
  background: rgba(0, 242, 241, 0.1);
  border: 1px solid rgba(0, 242, 241, 0.3);
  padding: 20px;
  text-align: center;
  border-radius: 6px;
}
.digital-item .label { font-size: 14px; color: #a6c1ee; margin-bottom: 5px; }
.digital-item .value { font-size: 36px; font-weight: bold; color: #fff; font-family: 'Impact', sans-serif; }
.digital-item .value.highlight { color: #ffff00; text-shadow: 0 0 10px #ffff00; }

.map-box { flex: 1; background: rgba(16, 30, 60, 0.6); border: 1px solid rgba(0, 242, 241, 0.2); padding: 15px; display:flex; flex-direction:column;}

/* 公告列表 */
.notification-box { overflow: hidden; }
.notification-list { flex: 1; overflow-y: auto; }
.notice-item {
  padding: 10px 0;
  border-bottom: 1px dashed rgba(255,255,255,0.1);
  display: flex;
  justify-content: space-between;
  color: #ccc;
  font-size: 14px;
}
.notice-item:hover { color: #00f2f1; cursor: pointer; }
.notice-time { color: #666; font-size: 12px; margin-right: 10px; }

/* 滚动条样式 */
::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { background: #00f2f1; border-radius: 2px; }
::-webkit-scrollbar-track { background: rgba(255,255,255,0.05); }
</style>