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
              <div class="value">358</div>
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
              <div class="value">88%</div>
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
              <div class="value">43</div>
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
              <div class="value">28 小时</div>
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
            <el-timeline-item timestamp="2025/08/14">完成了一次 "数学" 单元测试</el-timeline-item>
            <el-timeline-item timestamp="2025/08/13">复习了 5 道错题</el-timeline-item>
            <el-timeline-item timestamp="2025/08/11">进行了 "物理" 科目在线练习</el-timeline-item>
            <el-timeline-item timestamp="2025/08/10">登录了学习系统</el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { Tickets, Select, CloseBold, Clock, EditPen, Memo, DataLine, Finished } from '@element-plus/icons-vue';

const studentAuth = useStudentAuthStore();
const router = useRouter();

const welcomeMessage = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '凌晨好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const navigateTo = (path: string) => {
  if (path === '/student/history') {
    // 假设历史记录功能也待开发
    ElMessage.info('功能开发中，敬请期待');
    return;
  }
  router.push(path);
};
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  align-items: center;
}

.welcome-avatar {
  margin-right: 20px;
  flex-shrink: 0; /* 防止头像被压缩 */
}

.welcome-text h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-text p {
  color: #606266;
  font-size: 0.9rem;
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item .el-icon {
  font-size: 48px;
  color: #409EFF;
}

.stat-text .label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-text .value {
  font-size: 24px;
  font-weight: bold;
}

.card-header {
  font-size: 1rem;
  font-weight: 600;
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  height: 120px;
}

.access-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.access-item:hover {
  border-color: #409EFF;
  color: #409EFF;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-4px);
}

.access-item .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.timeline {
  padding-left: 5px;
  height: 120px; /* 限制高度使其不会太长 */
}
</style>