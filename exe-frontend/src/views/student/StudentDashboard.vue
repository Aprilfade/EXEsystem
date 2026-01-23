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
          <p>"学而不思则罔，思而不学则殆。" 坚持学习，不断进步！</p>
        </div>
      </div>
    </el-card>

    <!-- 快捷入口 - 移到顶部 -->
    <el-card shadow="never" class="quick-access-card">
      <template #header>
        <div class="card-header quick-access-header">
          <div class="header-left">
            <div class="header-icon-bg">
              <el-icon><Operation /></el-icon>
            </div>
            <div class="header-text">
              <h3>快捷入口</h3>
              <p>快速访问常用功能</p>
            </div>
          </div>
        </div>
      </template>
      <div class="quick-access-grid">
        <div class="access-item item-blue" @click="navigateTo('/student/practice')">
          <div class="item-bg-decoration"></div>
          <div class="icon-wrapper">
            <el-icon><EditPen /></el-icon>
          </div>
          <span class="title">开始练习</span>
          <span class="desc">随机刷题 / 智能推题</span>
          <div class="hover-indicator"></div>
        </div>

        <div class="access-item item-red" @click="navigateTo('/student/wrong-records')">
          <div class="item-bg-decoration"></div>
          <div class="icon-wrapper">
            <el-icon><Memo /></el-icon>
          </div>
          <span class="title">我的错题本</span>
          <span class="desc">温故知新 / 举一反三</span>
          <div class="hover-indicator"></div>
        </div>

        <div class="access-item item-green" @click="navigateTo('/student/review')">
          <div class="item-bg-decoration"></div>
          <div class="icon-wrapper">
            <el-icon><AlarmClock /></el-icon>
          </div>
          <span class="title">智能复习</span>
          <span class="desc">基于遗忘曲线 / 科学记忆</span>
          <div class="hover-indicator"></div>
        </div>

        <div class="access-item item-purple" @click="navigateTo('/student/exams')">
          <div class="item-bg-decoration"></div>
          <div class="icon-wrapper">
            <el-icon><DataLine /></el-icon>
          </div>
          <span class="title">模拟考试</span>
          <span class="desc">全真模拟 / 实战演练</span>
          <div class="hover-indicator"></div>
        </div>

        <div class="access-item item-orange" @click="navigateTo('/student/history')">
          <div class="item-bg-decoration"></div>
          <div class="icon-wrapper">
            <el-icon><Finished /></el-icon>
          </div>
          <span class="title">考试记录</span>
          <span class="desc">查看过往成绩详情</span>
          <div class="hover-indicator"></div>
        </div>

        <div class="access-item item-teal" @click="navigateTo('/student/favorites')">
          <div class="item-bg-decoration"></div>
          <div class="icon-wrapper">
            <el-icon><Star /></el-icon>
          </div>
          <span class="title">我的收藏</span>
          <span class="desc">重点难题一键回顾</span>
          <div class="hover-indicator"></div>
        </div>

        <div class="access-item item-indigo" @click="navigateTo('/student/battle')">
          <div class="item-bg-decoration"></div>
          <div class="icon-wrapper">
            <el-icon><Lightning /></el-icon>
          </div>
          <span class="title">知识对战</span>
          <span class="desc">1V1 实时竞技 PK</span>
          <div class="hover-indicator"></div>
        </div>

        <div class="access-item item-gold" @click="isMallVisible = true">
          <div class="item-bg-decoration"></div>
          <div class="icon-wrapper">
            <el-icon><Present /></el-icon>
          </div>
          <span class="title">积分商城</span>
          <span class="desc">兑换专属奖励</span>
          <div class="hover-indicator"></div>
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

    <!-- 学习分析组件 -->
    <LearningAnalytics style="margin-bottom: 20px;" />

    <!-- AI智能推荐面板 -->
    <RecommendationPanel />

    <!-- AI学习助手 - 右侧悬浮 -->
    <div class="floating-ai-assistant">
      <!-- 收起/展开按钮 -->
      <transition name="slide-fade">
        <div
          v-show="!showAiChat"
          class="ai-toggle-btn"
          @click="showAiChat = true"
          title="打开小艾助手"
        >
          <el-icon :size="24"><ChatDotRound /></el-icon>
          <span class="btn-text">小艾</span>
        </div>
      </transition>

      <!-- AI聊天面板 -->
      <transition name="slide">
        <div v-show="showAiChat" class="ai-chat-container">
          <div class="ai-chat-header">
            <span>🤖 小艾学习助手</span>
            <el-button
              circle
              size="small"
              @click="showAiChat = false"
              title="收起"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <AiChatPanel />
        </div>
      </transition>
    </div>

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
      <el-col :span="24">
        <SignInCalendar style="margin-bottom: 20px;" />
      </el-col>
      <el-col :span="24">
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

    <!-- 积分商城对话框 -->
    <points-mall-dialog v-model:visible="isMallVisible" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStudentAuthStore } from '@/stores/studentAuth';
// 【修复】添加 Star 图标导入
import { Tickets, Select, CloseBold, Clock, EditPen, Memo, DataLine, Finished, Star, AlarmClock, Lightning, ChatDotRound, Close } from '@element-plus/icons-vue';
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
import LearningAnalytics from '@/components/student/LearningAnalytics.vue'; // 引入学习分析组件
import { fetchMyAchievements } from '@/api/student'; // 引入API
import RecommendationPanel from '@/components/ai/RecommendationPanel.vue'; // AI智能推荐
import AiChatPanel from '@/components/ai/AiChatPanel.vue'; // AI学习助手



const achievements = ref([]);
const studentAuth = useStudentAuthStore();
const router = useRouter();

// AI助手显示状态
const showAiChat = ref(false);

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
/* === 快捷入口卡片样式 === */
.quick-access-card {
  margin-bottom: 20px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  border: 1px solid #e8ecf4;
}

.quick-access-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: none;
  padding: 20px 24px;
}

.quick-access-header {
  color: white;
}

.quick-access-header .header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.quick-access-header .header-icon-bg {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.quick-access-header .header-text h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.quick-access-header .header-text p {
  margin: 4px 0 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

/* --- 快捷入口网格样式 --- */
.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 4px;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .quick-access-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .quick-access-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

/* 快捷入口卡片项 */
.access-item {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: white;
  border: 2px solid transparent;
  border-radius: 16px;
  padding: 28px 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 背景装饰元素 */
.item-bg-decoration {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  opacity: 0;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

/* 悬停指示器 */
.hover-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  width: 60%;
  height: 3px;
  border-radius: 3px 3px 0 0;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.access-item:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.access-item:hover .item-bg-decoration {
  opacity: 0.08;
  transform: rotate(45deg);
}

.access-item:hover .hover-indicator {
  transform: translateX(-50%) scaleX(1);
}

.access-item:active {
  transform: translateY(-6px) scale(1.01);
}

/* 图标容器 */
.icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  font-size: 32px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
}

.access-item:hover .icon-wrapper {
  transform: rotateY(360deg) scale(1.1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  position: relative;
  z-index: 1;
}

.desc {
  font-size: 12px;
  color: #909399;
  text-align: center;
  line-height: 1.5;
  position: relative;
  z-index: 1;
}

/* === 各个卡片的配色方案 === */

/* 蓝色 - 开始练习 */
.item-blue .icon-wrapper {
  background: linear-gradient(135deg, #e8f3ff 0%, #d4e8ff 100%);
  color: #409eff;
}
.item-blue:hover {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f3ff 100%);
}
.item-blue:hover .icon-wrapper {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}
.item-blue .item-bg-decoration {
  background: radial-gradient(circle, #409eff 0%, transparent 70%);
}
.item-blue .hover-indicator {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
}

/* 红色 - 错题本 */
.item-red .icon-wrapper {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}
.item-red:hover {
  border-color: #f56c6c;
  background: linear-gradient(135deg, #fff5f5 0%, #fef0f0 100%);
}
.item-red:hover .icon-wrapper {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  color: white;
}
.item-red .item-bg-decoration {
  background: radial-gradient(circle, #f56c6c 0%, transparent 70%);
}
.item-red .hover-indicator {
  background: linear-gradient(90deg, #f56c6c 0%, #f78989 100%);
}

/* 绿色 - 智能复习 */
.item-green .icon-wrapper {
  background: linear-gradient(135deg, #f0f9ff 0%, #e1f3d8 100%);
  color: #67c23a;
}
.item-green:hover {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f5fef0 0%, #e1f3d8 100%);
}
.item-green:hover .icon-wrapper {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}
.item-green .item-bg-decoration {
  background: radial-gradient(circle, #67c23a 0%, transparent 70%);
}
.item-green .hover-indicator {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

/* 紫色 - 模拟考试 */
.item-purple .icon-wrapper {
  background: linear-gradient(135deg, #f9f0ff 0%, #f3e5f5 100%);
  color: #9c27b0;
}
.item-purple:hover {
  border-color: #9c27b0;
  background: linear-gradient(135deg, #fcf5ff 0%, #f9f0ff 100%);
}
.item-purple:hover .icon-wrapper {
  background: linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%);
  color: white;
}
.item-purple .item-bg-decoration {
  background: radial-gradient(circle, #9c27b0 0%, transparent 70%);
}
.item-purple .hover-indicator {
  background: linear-gradient(90deg, #9c27b0 0%, #ba68c8 100%);
}

/* 橙色 - 考试记录 */
.item-orange .icon-wrapper {
  background: linear-gradient(135deg, #fffbf0 0%, #fdf6ec 100%);
  color: #e6a23c;
}
.item-orange:hover {
  border-color: #e6a23c;
  background: linear-gradient(135deg, #fffef5 0%, #fffbf0 100%);
}
.item-orange:hover .icon-wrapper {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
  color: white;
}
.item-orange .item-bg-decoration {
  background: radial-gradient(circle, #e6a23c 0%, transparent 70%);
}
.item-orange .hover-indicator {
  background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%);
}

/* 青色 - 我的收藏 */
.item-teal .icon-wrapper {
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%);
  color: #00bcd4;
}
.item-teal:hover {
  border-color: #00bcd4;
  background: linear-gradient(135deg, #e0f7fa 0%, #e0f7fa 100%);
}
.item-teal:hover .icon-wrapper {
  background: linear-gradient(135deg, #00bcd4 0%, #26c6da 100%);
  color: white;
}
.item-teal .item-bg-decoration {
  background: radial-gradient(circle, #00bcd4 0%, transparent 70%);
}
.item-teal .hover-indicator {
  background: linear-gradient(90deg, #00bcd4 0%, #26c6da 100%);
}

/* 靛蓝色 - 知识对战 */
.item-indigo .icon-wrapper {
  background: linear-gradient(135deg, #e8eaf6 0%, #c5cae9 100%);
  color: #5c6bc0;
}
.item-indigo:hover {
  border-color: #5c6bc0;
  background: linear-gradient(135deg, #f3f4fb 0%, #e8eaf6 100%);
}
.item-indigo:hover .icon-wrapper {
  background: linear-gradient(135deg, #5c6bc0 0%, #7986cb 100%);
  color: white;
}
.item-indigo .item-bg-decoration {
  background: radial-gradient(circle, #5c6bc0 0%, transparent 70%);
}
.item-indigo .hover-indicator {
  background: linear-gradient(90deg, #5c6bc0 0%, #7986cb 100%);
}

/* 金色 - 积分商城 */
.item-gold .icon-wrapper {
  background: linear-gradient(135deg, #fffbf0 0%, #fff8dc 100%);
  color: #faad14;
}
.item-gold:hover {
  border-color: #faad14;
  background: linear-gradient(135deg, #fffef7 0%, #fffbf0 100%);
}
.item-gold:hover .icon-wrapper {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
  color: white;
}
.item-gold .item-bg-decoration {
  background: radial-gradient(circle, #faad14 0%, transparent 70%);
}
.item-gold .hover-indicator {
  background: linear-gradient(90deg, #faad14 0%, #ffc53d 100%);
}

/* === 悬浮AI助手样式 === */
.floating-ai-assistant {
  position: fixed;
  right: 20px;
  bottom: 80px;
  z-index: 1000;
}

/* 展开/收起按钮 */
.ai-toggle-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
}

.ai-toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.ai-toggle-btn .btn-text {
  font-size: 12px;
  font-weight: 600;
  margin-top: 2px;
}

/* AI聊天容器 */
.ai-chat-container {
  width: 380px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-chat-header {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  flex-shrink: 0;
}

.ai-chat-header .el-button {
  color: white;
}

/* 修复AI聊天面板内部样式 */
.ai-chat-container :deep(.ai-chat-panel) {
  border: none;
  box-shadow: none;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ai-chat-container :deep(.el-card) {
  border: none;
  box-shadow: none;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ai-chat-container :deep(.el-card__header) {
  display: none;
}

.ai-chat-container :deep(.el-card__body) {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-chat-container :deep(.chat-content) {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.ai-chat-container :deep(.chat-input-wrapper) {
  flex-shrink: 0;
}

/* 过渡动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .ai-chat-container {
    width: 100vw;
    height: 100vh;
    position: fixed;
    right: 0;
    bottom: 0;
    border-radius: 0;
  }

  .floating-ai-assistant {
    right: 16px;
    bottom: 70px;
  }

  .ai-toggle-btn {
    width: 56px;
    height: 56px;
  }
}
</style>