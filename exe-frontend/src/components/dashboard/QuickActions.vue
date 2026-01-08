<template>
  <el-card shadow="never" class="quick-actions-card">
    <div class="quick-actions">
      <div
        class="action-item"
        v-for="action in quickActions"
        :key="action.id"
        @click="handleAction(action)"
      >
        <div class="action-icon" :style="{ background: action.gradient }">
          <el-icon :size="24">
            <component :is="action.icon" />
          </el-icon>
        </div>
        <div class="action-info">
          <div class="action-title">{{ action.title }}</div>
          <div class="action-desc">{{ action.desc }}</div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { DocumentAdd, Upload, Bell, Edit } from '@element-plus/icons-vue';

const router = useRouter();

const quickActions = [
  {
    id: 1,
    title: '创建试卷',
    desc: '快速创建新试卷',
    icon: DocumentAdd,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    route: '/paper-manage'
  },
  {
    id: 2,
    title: '导入学生',
    desc: '批量导入学生信息',
    icon: Upload,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    route: '/student-manage'
  },
  {
    id: 3,
    title: '发布通知',
    desc: '向学生发送通知',
    icon: Bell,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    route: '/notifications'
  },
  {
    id: 4,
    title: '添加题目',
    desc: '新增题库题目',
    icon: Edit,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    route: '/question-manage'
  }
];

const handleAction = (action: any) => {
  router.push(action.route);
};
</script>

<style scoped>
.quick-actions-card {
  margin-bottom: 20px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
  flex-shrink: 0;
}

.action-info {
  flex: 1;
}

.action-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 12px;
  color: #909399;
}
</style>
