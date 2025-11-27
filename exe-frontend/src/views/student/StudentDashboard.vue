<template>
  <div class="dashboard-container">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <el-avatar :size="72" :src="studentAuth.student?.avatar || ''" class="welcome-avatar">
          {{ studentAuth.studentName.charAt(0) }}
        </el-avatar>
        <div class="welcome-text">
          <h2>{{ welcomeMessage }}，{{ studentAuth.studentName }} 同学！</h2>
          <p>“学而不思则罔，思而不学则殆。” 坚持学习，不断进步！</p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon><Tickets /></el-icon>
            <div class="stat-text">
              <div class="label">累计答题总数</div>
              <div class="value">{{ stats.totalAnswered }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon color="#67C23A"><Select /></el-icon>
            <div class="stat-text">
              <div class="label">平均正确率</div>
              <div class="value">{{ stats.averageAccuracy }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon color="#F56C6C"><CloseBold /></el-icon>
            <div class="stat-text">
              <div class="label">错题总数</div>
              <div class="value">{{ stats.wrongRecordCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon color="#E6A23C"><Clock /></el-icon>
            <div class="stat-text">
              <div class="label">学习时长</div>
              <div class="value">{{ stats.studyDurationHours }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>


    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>📅 学习打卡记录 (过去一年)</span>
            </div>
          </template>
          <div ref="heatmapChartRef" style="height: 180px; width: 100%;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" style="margin-bottom: 20px;">
        </el-card>

        <el-card shadow="never" class="leaderboard-card">
          <template #header>
            <div class="card-header">
              <span>🏆 学霸排行榜</span>
              <el-tag type="warning" effect="dark">我的积分: {{ myPoints }}</el-tag>
            </div>
          </template>
          <div class="leaderboard-list">
            <div v-for="(student, index) in leaderboard" :key="index" class="rank-item">
              <div class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <el-avatar :size="30" :src="student.avatar">{{ student.name.charAt(0) }}</el-avatar>
              <div class="rank-info">
                <span class="name">{{ student.name }}</span>
                <span class="grade">{{ student.grade }}</span>
              </div>
              <div class="rank-score">{{ student.points || 0 }} 分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div class="quick-access-grid">
            <div class="access-item" @click="navigateTo('/student/practice')">
              <el-icon><EditPen /></el-icon>
              <span>开始练习</span>
            </div>
            <div class="access-item" @click="navigateTo('/student/wrong-records')">
              <el-icon><Memo /></el-icon>
              <span>我的错题本</span>
            </div>
            <div class="access-item" @click="navigateTo('/student/exams')">
              <el-icon><DataLine /></el-icon>
              <span>参加模拟考</span>
            </div>
            <div class="access-item" @click="navigateTo('/student/history')">
              <el-icon><Finished /></el-icon>
              <span>历史记录</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>学习动态</span>
            </div>
          </template>
          <el-timeline class="timeline">
            <el-empty v-if="activities.length === 0" description="暂无学习动态" :image-size="60" />
            <el-timeline-item v-else v-for="activity in activities" :key="activity.id" :timestamp="activity.createTime">
              {{ activity.description }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { Tickets, Select, CloseBold, Clock, EditPen, Memo, DataLine, Finished } from '@element-plus/icons-vue';
// 【新增】导入API函数和类型
import { fetchStudentDashboardStats, type StudentDashboardStats } from '@/api/studentAuth';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import type { ApiResult } from '@/api/user';
import type { BizLearningActivity } from '@/api/learningActivity'; // 假定你已创建此类型
// 1. 引入 echarts
import * as echarts from 'echarts';


const studentAuth = useStudentAuthStore();
const router = useRouter();

// 2. 定义 ref
const heatmapChartRef = ref<HTMLElement | null>(null);
// 3. 定义渲染函数
const initHeatmap = (dataMap: Record<string, number>) => {
  if (!heatmapChartRef.value) return;

  const myChart = echarts.init(heatmapChartRef.value);
  const currentYear = new Date().getFullYear();

  // 转换数据格式 [ ['2023-01-01', 5], ... ]
  const chartData = Object.entries(dataMap).map(([date, count]) => [date, count]);
  // 1. 定义变量
  const leaderboard = ref<any[]>([]);
  const myPoints = ref(0);

  const option = {
    tooltip: {
      position: 'top',
      formatter: (p: any) => {
        const format = echarts.format.formatTime('yyyy-MM-dd', p.data[0]);
        return `${format}: ${p.data[1]} 次学习活动`;
      }
    },
    visualMap: {
      min: 0,
      max: 10, // 每天超过10次就算非常活跃
      calculable: false,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#ebedf0', '#9be9a8', '#40c463', '#30a14e', '#216e39'] }, // GitHub 绿
      text: ['勤奋', '少'],
      show: true
    },
    calendar: {
      top: 30,
      left: 30,
      right: 30,
      cellSize: ['auto', 13],
      range: currentYear, // 显示当年
      itemStyle: { borderWidth: 0.5, borderColor: '#fff' },
      yearLabel: { show: false }
    },
    series: [{
      type: 'heatmap',
      coordinateSystem: 'calendar',
      data: chartData
    }]
  };

  myChart.setOption(option);
  window.addEventListener('resize', () => myChart.resize());
};
// 【新增】用于存储统计数据的响应式变量
const loading = ref(true);
const stats = ref<StudentDashboardStats>({
  totalAnswered: 0,
  averageAccuracy: 0,
  wrongRecordCount: 0,
  studyDurationHours: 0,
});
// 【新增】学习活动列表
const activities = ref<BizLearningActivity[]>([]);

const welcomeMessage = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '凌晨好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const navigateTo = (path: string) => {
  router.push(path);
};

// 【新增】在组件挂载后获取数据
onMounted(async () => {
  loading.value = true;
  try {
    const res = await fetchStudentDashboardStats();
    if (res.code === 200) {
      stats.value = res.data;
    }
    // 【新增】获取学习活动日志
    const activitiesRes: ApiResult<BizLearningActivity[]> = await request({
      url: '/api/v1/student/learning-activities',
      method: 'get'
    });
    if (activitiesRes.code === 200) {
      activities.value = activitiesRes.data;
    }
  } catch (error) {
    console.error("获取仪表盘统计数据失败:", error);
    ElMessage.error("获取统计数据失败，请稍后重试");
  } finally {
    loading.value = false;
  }
  // 获取热力图数据
  try {
    const heatRes = await request.get('/api/v1/student/dashboard/activity-heatmap');
    if (heatRes.code === 200) {
      initHeatmap(heatRes.data);
    }
  } catch(e) { console.error(e); }
  // 获取排行榜
  const rankRes = await request.get('/api/v1/student/dashboard/leaderboard');
  if (rankRes.code === 200) {
    leaderboard.value = rankRes.data;
  }

  // 获取我的最新信息(含积分)
  const myInfoRes = await request.get('/api/v1/student/auth/me');
  if(myInfoRes.code === 200) {
    myPoints.value = myInfoRes.data.points || 0;
  }
});
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
}
.welcome-card { margin-bottom: 20px; }
.welcome-content { display: flex; align-items: center; }
.welcome-avatar { margin-right: 20px; flex-shrink: 0; }
.welcome-text h2 { font-size: 1.5rem; font-weight: 600; margin: 0 0 8px 0; }
.welcome-text p { color: #606266; font-size: 0.9rem; margin: 0; }
.stats-row { margin-bottom: 20px; }
.stat-item { display: flex; align-items: center; gap: 16px; }
.stat-item .el-icon { font-size: 48px; color: #409EFF; }
.stat-text .label { font-size: 14px; color: #909399; margin-bottom: 4px; }
.stat-text .value { font-size: 24px; font-weight: bold; }
.card-header { font-size: 1rem; font-weight: 600; }
.quick-access-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; height: 120px; }
.access-item { display: flex; flex-direction: column; justify-content: center; align-items: center; border: 1px solid #e4e7ed; border-radius: 8px; cursor: pointer; transition: all 0.2s ease-in-out; }
.access-item:hover { border-color: #409EFF; color: #409EFF; box-shadow: 0 4px 12px rgba(0,0,0,0.1); transform: translateY(-4px); }
.access-item .el-icon { font-size: 32px; margin-bottom: 8px; }
.timeline { padding-left: 5px; height: 120px; }
.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  background: #f8f9fa;
}
.rank-num {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  font-weight: bold;
  border-radius: 50%;
  background: #e0e0e0;
  color: #666;
  font-size: 12px;
}
/* 前三名高亮 */
.rank-1 { background: #FFD700; color: #fff; }
.rank-2 { background: #C0C0C0; color: #fff; }
.rank-3 { background: #CD7F32; color: #fff; }

.rank-info {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}
.rank-info .name { font-size: 14px; font-weight: 600; }
.rank-info .grade { font-size: 12px; color: #999; }
.rank-score {
  font-weight: bold;
  color: #f56c6c;
}
</style>