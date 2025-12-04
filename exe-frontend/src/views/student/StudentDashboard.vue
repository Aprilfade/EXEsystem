<template>
  <div class="dashboard-container">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <UserAvatar
            :src="studentAuth.student?.avatar"
            :name="studentAuth.studentName"
            :size="72"
            :frame-style="studentAuth.student?.avatarFrameStyle"
            class="welcome-avatar"
            style="margin-right: 20px;"
        />
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
      <el-col :span="16">
        <el-card shadow="never" style="height: 100%;">
          <template #header>
            <div class="card-header">
              <span>📅 学习打卡记录 (过去一年)</span>
            </div>
          </template>
          <div ref="heatmapChartRef" style="height: 240px; width: 100%;"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="leaderboard-card" style="height: 100%;">
          <template #header>
            <div class="card-header">
              <span>🏆 学霸排行榜</span>
              <el-tag type="warning" effect="dark">我的积分: {{ myPoints }}</el-tag>
            </div>
          </template>
          <div class="leaderboard-list">
            <div v-for="(student, index) in leaderboard" :key="index" class="rank-item">
              <div class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <UserAvatar
                  :src="student.avatar"
                  :name="student.name"
                  :size="30"
                  :frame-style="student.avatarFrameStyle"
              />
              <div class="rank-info" style="margin-left: 10px;">
                <span class="name">{{ student.name }}</span>
                <span class="grade">{{ student.grade }}</span>
              </div>
              <div class="rank-score">{{ student.points || 0 }} 分</div>
            </div>
            <el-empty v-if="leaderboard.length === 0" description="暂无排名数据" :image-size="50" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="24" style="margin-top: 20px;">
        <el-card shadow="never">
          <template #header>
            <div class="card-header"><span>🏅 我的成就墙</span></div>
          </template>
          <AchievementList :list="achievements" />
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
            <div class="access-item item-blue" @click="navigateTo('/student/practice')">
              <div class="icon-wrapper">
                <el-icon><EditPen /></el-icon>
              </div>
              <span class="title">开始练习</span>
              <span class="desc">随机刷题 / 智能推题</span>
            </div>

            <div class="access-item item-red" @click="navigateTo('/student/wrong-records')">
              <div class="icon-wrapper">
                <el-icon><Memo /></el-icon>
              </div>
              <span class="title">我的错题本</span>
              <span class="desc">温故知新 / 举一反三</span>
            </div>

            <div class="access-item item-green" @click="navigateTo('/student/review')">
              <div class="icon-wrapper">
                <el-icon><AlarmClock /></el-icon>
              </div>
              <span class="title">智能复习</span>
              <span class="desc">基于遗忘曲线 / 科学记忆</span>
            </div>

            <div class="access-item item-purple" @click="navigateTo('/student/exams')">
              <div class="icon-wrapper">
                <el-icon><DataLine /></el-icon>
              </div>
              <span class="title">模拟考试</span>
              <span class="desc">全真模拟 / 实战演练</span>
            </div>

            <div class="access-item item-orange" @click="navigateTo('/student/history')">
              <div class="icon-wrapper">
                <el-icon><Finished /></el-icon>
              </div>
              <span class="title">考试记录</span>
              <span class="desc">查看过往成绩详情</span>
            </div>

            <div class="access-item item-green" @click="navigateTo('/student/favorites')">
              <div class="icon-wrapper">
                <el-icon><Star /></el-icon>
              </div>
              <span class="title">我的收藏</span>
              <span class="desc">重点难题一键回顾</span>
            </div>

            <div class="access-item item-gold" @click="isMallVisible = true">
              <div class="icon-wrapper">
                <el-icon><Present /></el-icon>
              </div>
              <span class="title">积分商城</span>
              <span class="desc">兑换专属奖励</span>
            </div>
            <points-mall-dialog v-model:visible="isMallVisible" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <SignInCalendar style="margin-bottom: 20px;" />
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
// 【修复】添加 Star 图标导入
import { Tickets, Select, CloseBold, Clock, EditPen, Memo, DataLine, Finished, Star , AlarmClock} from '@element-plus/icons-vue';
import { fetchStudentDashboardStats, type StudentDashboardStats } from '@/api/studentAuth';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import type { ApiResult } from '@/api/user';
import type { BizLearningActivity } from '@/api/learningActivity';
import * as echarts from 'echarts';
import PointsMallDialog from '@/components/student/PointsMallDialog.vue';
import { Present } from '@element-plus/icons-vue'; // 引入礼品图标
import UserAvatar from '@/components/UserAvatar.vue';
import SignInCalendar from '@/components/student/SignInCalendar.vue';
import AchievementList from '@/components/student/AchievementList.vue'; // 引入组件
import { fetchMyAchievements } from '@/api/student'; // 引入API



const achievements = ref([]);
const studentAuth = useStudentAuthStore();
const router = useRouter();
const isMallVisible = ref(false);

const heatmapChartRef = ref<HTMLElement | null>(null);
const leaderboard = ref<any[]>([]);
const myPoints = ref(0);
const loading = ref(true);
const stats = ref<StudentDashboardStats>({
  totalAnswered: 0,
  averageAccuracy: 0,
  wrongRecordCount: 0,
  studyDurationHours: 0,
});
const activities = ref<BizLearningActivity[]>([]);

// 【核心修复 2】修复热力图配置
const initHeatmap = (dataMap: Record<string, number>) => {
  if (!heatmapChartRef.value) return;

  const myChart = echarts.init(heatmapChartRef.value);

  // 计算日期范围：从一年前的今天 到 今天
  const endDate = new Date();
  const startDate = new Date();
  startDate.setFullYear(endDate.getFullYear() - 1);

  // 格式化为 'YYYY-MM-DD'
  const formatDate = (date: Date) => date.toISOString().split('T')[0];

  const chartData = Object.entries(dataMap).map(([date, count]) => [date, count]);

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
      max: 10,
      calculable: false,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#ebedf0', '#9be9a8', '#40c463', '#30a14e', '#216e39'] },
      text: ['勤奋', '少'],
      show: true
    },
    calendar: {
      top: 30,
      left: 30,
      right: 30,
      cellSize: ['auto', 13],
      // 关键修复：设置正确的 range 范围，不仅是当前年份，而是后端返回数据的区间
      range: [formatDate(startDate), formatDate(endDate)],
      itemStyle: { borderWidth: 0.5, borderColor: '#fff' },
      yearLabel: { show: true }
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

onMounted(async () => {
  loading.value = true;
  try {
    // 1. 获取统计数据
    const res = await fetchStudentDashboardStats();
    if (res.code === 200) stats.value = res.data;

    // 2. 获取学习活动日志
    const activitiesRes: ApiResult<BizLearningActivity[]> = await request({
      url: '/api/v1/student/learning-activities',
      method: 'get'
    });
    if (activitiesRes.code === 200) activities.value = activitiesRes.data;

    // 3. 获取热力图数据
    const heatRes = await request.get('/api/v1/student/dashboard/activity-heatmap');
    if (heatRes.code === 200) {
      initHeatmap(heatRes.data);
    }

    // 4. 获取排行榜
    const rankRes = await request.get('/api/v1/student/dashboard/leaderboard');
    if (rankRes.code === 200) {
      leaderboard.value = rankRes.data;
    }

    // 5. 获取我的积分
    const myInfoRes = await request.get('/api/v1/student/auth/me');
    if(myInfoRes.code === 200) {
      myPoints.value = myInfoRes.data.points || 0;
    }

  } catch (error) {
    console.error("获取数据失败:", error);
  } finally {
    loading.value = false;
  }
  const achRes = await fetchMyAchievements();
  if (achRes.code === 200) {
    achievements.value = achRes.data;
  }
});
</script>

<style scoped>
.dashboard-container { padding: 24px; }
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
.card-header { font-size: 1rem; font-weight: 600; display: flex; justify-content: space-between; align-items: center; }
.access-item .el-icon { font-size: 32px; margin-bottom: 8px; }
.timeline { padding-left: 5px; height: 120px; }
.leaderboard-list { display: flex; flex-direction: column; gap: 12px; }
.rank-item { display: flex; align-items: center; gap: 10px; padding: 8px; border-radius: 8px; background: #f8f9fa; }
.rank-num { width: 24px; height: 24px; line-height: 24px; text-align: center; font-weight: bold; border-radius: 50%; background: #e0e0e0; color: #666; font-size: 12px; }
.rank-1 { background: #FFD700; color: #fff; }
.rank-2 { background: #C0C0C0; color: #fff; }
.rank-3 { background: #CD7F32; color: #fff; }
.rank-info { flex-grow: 1; display: flex; flex-direction: column; }
.rank-info .name { font-size: 14px; font-weight: 600; }
.rank-info .grade { font-size: 12px; color: #999; }
.rank-score { font-weight: bold; color: #f56c6c; }
/* --- 快捷入口样式优化 --- */
.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr); /* 改为5列，容纳所有入口 */
  gap: 20px;
  margin-top: 10px;
}

.access-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  border: 1px solid #f0f2f5;
  border-radius: 12px;
  padding: 24px 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.access-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
  border-color: transparent;
}

/* 图标容器 */
.icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  font-size: 28px;
  transition: all 0.3s;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.desc {
  font-size: 12px;
  color: #909399;
}

/* 各个卡片的配色方案 */
/* 蓝色 */
.item-blue .icon-wrapper { background: #e8f3ff; color: #409eff; }
.item-blue:hover .icon-wrapper { background: #409eff; color: #fff; }

/* 红色 */
.item-red .icon-wrapper { background: #fde2e2; color: #f56c6c; }
.item-red:hover .icon-wrapper { background: #f56c6c; color: #fff; }

/* 紫色 */
.item-purple .icon-wrapper { background: #f3e5f5; color: #9c27b0; }
.item-purple:hover .icon-wrapper { background: #9c27b0; color: #fff; }

/* 橙色 */
.item-orange .icon-wrapper { background: #fdf6ec; color: #e6a23c; }
.item-orange:hover .icon-wrapper { background: #e6a23c; color: #fff; }

/* 绿色 */
.item-green .icon-wrapper { background: #e1f3d8; color: #67c23a; }
.item-green:hover .icon-wrapper { background: #67c23a; color: #fff; }

.item-gold .icon-wrapper { background: #fffbf0; color: #faad14; }
.item-gold:hover .icon-wrapper { background: #faad14; color: #fff; }
</style>