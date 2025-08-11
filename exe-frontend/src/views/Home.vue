<template>
  <div class="dashboard-container">
    <div class="welcome-header">
      <el-avatar v-if="authStore.user?.avatar" :size="60" :src="authStore.user.avatar" class="welcome-avatar" />
      <el-avatar v-else :size="60" class="welcome-avatar">{{ authStore.userNickname.charAt(0) }}</el-avatar>

      <div class="welcome-text">
        <h3 class="greeting">{{ greeting }}, {{ authStore.userNickname }}! 欢迎回来。</h3>
        <div class="location-time">
          <el-icon><Location /></el-icon>
          <span>{{ currentLocation }}</span>
          <el-divider direction="vertical" />
          <span>{{ currentTime }}</span>
        </div>
      </div>


    </div>
    <el-row :gutter="20" style="margin-top: 24px;">
      <el-col :span="4"><el-card><p class="stat-label">学生数量</p><p class="stat-value">{{ stats.studentCount }}</p></el-card></el-col>
      <el-col :span="4"><el-card><p class="stat-label">科目数量</p><p class="stat-value">{{ stats.subjectCount }}</p></el-card></el-col>
      <el-col :span="4"><el-card><p class="stat-label">知识点数量</p><p class="stat-value">{{ stats.knowledgePointCount }}</p></el-card></el-col>
      <el-col :span="4"><el-card><p class="stat-label">题目数量</p><p class="stat-value">{{ stats.questionCount }}</p></el-card></el-col>
      <el-col :span="4"><el-card><p class="stat-label">试卷数量</p><p class="stat-value">{{ stats.paperCount }}</p></el-card></el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <h4>知识点 & 题目总览</h4>
          <div ref="mainChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <h4>通知</h4>
          <ul class="notification-list">
            <li v-if="stats.notifications.length === 0">
              <el-empty description="暂无通知" :image-size="60" />
            </li>
            <li v-else v-for="item in stats.notifications" :key="item.id" @click="handleNotificationClick(item.id)" class="notification-item">
              <span>{{ item.content }}</span>
              <span class="date">{{ item.date }}</span>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <notification-preview-dialog
      v-model:visible="isPreviewVisible"
      :notification="selectedNotification"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, reactive, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { Location } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import { getDashboardStats, type DashboardStats, type ChartData } from '@/api/dashboard';
import NotificationPreviewDialog from "@/components/notification/NotificationPreviewDialog.vue";
// 【新增】导入获取通知详情的API方法和类型
import { fetchNotificationById, type Notification } from '@/api/notification';


// 【新增】用于控制弹窗的状态
const isPreviewVisible = ref(false);
const selectedNotification = ref<Notification | undefined>(undefined);

const authStore = useAuthStore();
const mainChart = ref<HTMLElement | null>(null);

const currentTime = ref('');
const currentLocation = ref('正在获取...');
let timer: number | null = null;
const stats = ref<DashboardStats>({
  studentCount: 0, subjectCount: 0, knowledgePointCount: 0,
  questionCount: 0, paperCount: 0, notifications: [],
  kpAndQuestionStats: { categories: [], series: [] }
});

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return "凌晨好";
  if (hour < 12) return "上午好";
  if (hour < 18) return "下午好";
  return "晚上好";
});
// --- 方法 ---
const formatTime = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = (now.getMonth() + 1).toString().padStart(2, '0');
  const date = now.getDate().toString().padStart(2, '0');
  const hours = now.getHours().toString().padStart(2, '0');
  const minutes = now.getMinutes().toString().padStart(2, '0');
  const seconds = now.getSeconds().toString().padStart(2, '0');
  const day = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][now.getDay()];
  currentTime.value = `${year}-${month}-${date} ${hours}:${minutes}:${seconds} ${day}`;
};
const getLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
        async (position) => {
          try {
            const response = await fetch(`https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${position.coords.latitude}&longitude=${position.coords.longitude}&localityLanguage=zh-CN`);
            const data = await response.json();
            currentLocation.value = data.city || data.countryName || '未知地点';
          } catch (error) {
            currentLocation.value = '地点解析失败';
          }
        },
        () => { currentLocation.value = '未知地点'; }
    );
  } else {
    currentLocation.value = '浏览器不支持';
  }
};

const fetchData = async () => {
  try {
    const res = await getDashboardStats();
    if (res.code === 200) {
      stats.value = res.data;
      // 数据获取后再初始化图表
      initMainChart(stats.value.kpAndQuestionStats);
    }
  } catch (error) {
    ElMessage.error("获取统计数据失败");
  }
};

const initMainChart = (chartData: ChartData) => {
  if (mainChart.value && chartData) {
    const chart = echarts.init(mainChart.value);
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: {
        data: chartData.series.map(s => s.name)
      },
      xAxis: {
        type: 'category',
        data: chartData.categories
      },
      yAxis: { type: 'value' },
      series: chartData.series.map(s => ({
        name: s.name,
        type: 'bar',
        data: s.data
      }))
    });
  }
};
// 【新增】处理通知点击事件的方法
const handleNotificationClick = async (id: number) => {
  try {
    const res = await fetchNotificationById(id);
    if (res.code === 200) {
      selectedNotification.value = res.data;
      isPreviewVisible.value = true;
    } else {
      ElMessage.error(res.msg || '获取通知详情失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后再试');
  }
};


onMounted(() => {
  formatTime();
  timer = setInterval(formatTime, 1000);
  getLocation();
  fetchData();
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
}
.welcome-header {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background-color: var(--bg-color-container);
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: var(--card-shadow);
}
.welcome-avatar {
  margin-right: 16px;
  background-color: #f0faff;
  color: #409eff;
  font-size: 24px;
  font-weight: 600;
  /* 【修复点】: 防止头像被压缩 */
  flex-shrink: 0;
}
.welcome-text {
  /* 【修复点】: 确保文字容器能正常伸展 */
  flex-grow: 1;
}
.welcome-text .greeting {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--text-color-primary);
}
.welcome-text .location-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-color-regular);
}
.welcome-text .location-time .el-icon {
  margin-right: -4px;
}
.stat-label { font-size: 14px; color: var(--text-color-regular); margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: bold; }
.notification-list { list-style: none; padding: 0; }
.notification-list li { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border-color); font-size: 14px; }
.notification-list li:last-child { border-bottom: none; }
.notification-list .date { color: var(--text-color-regular); }

/* 【新增】为可点击的通知项添加样式 */
.notification-item {
  cursor: pointer;
  transition: background-color 0.2s;
}
.notification-item:hover {
  background-color: #f5f7fa;
}
</style>