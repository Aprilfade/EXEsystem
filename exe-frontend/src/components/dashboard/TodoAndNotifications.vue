<template>
  <div class="sidebar-section">
    <!-- 待办事项 -->
    <el-card shadow="never" class="section-card todo-card">
      <template #header>
        <div class="card-header-enhanced">
          <div class="header-title">
            <el-icon><Checked /></el-icon>
            <span>待办事项</span>
          </div>
          <el-badge :value="todoCount" :hidden="todoCount === 0" type="danger" />
        </div>
      </template>
      <div class="todo-list">
        <div v-if="todoItems.length === 0" class="empty-state">
          <el-icon :size="48" color="#C0C4CC"><Select /></el-icon>
          <p>暂无待办事项</p>
        </div>
        <div v-else class="todo-item" v-for="item in todoItems" :key="item.type">
          <div class="todo-icon" :style="{ background: item.color }">
            <el-icon><component :is="getIcon(item.icon)" /></el-icon>
          </div>
          <div class="todo-content">
            <div class="todo-title">{{ item.title }}</div>
            <div class="todo-meta">{{ item.count }} 项 · {{ item.time }}</div>
          </div>
          <el-button type="primary" link @click="handleTodoClick(item)">
            处理
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 系统通知 -->
    <el-card shadow="never" class="section-card notification-card">
      <template #header>
        <div class="card-header-enhanced">
          <div class="header-title">
            <el-icon><Bell /></el-icon>
            <span>系统通知</span>
          </div>
          <el-button type="primary" link @click="$router.push('/notifications')">
            查看全部
          </el-button>
        </div>
      </template>
      <div class="notification-list">
        <el-empty
          v-if="!notifications || notifications.length === 0"
          description="暂无通知"
          :image-size="60"
        />
        <div
          v-else
          class="notification-item"
          v-for="item in notifications"
          :key="item.id"
          @click="$emit('notification-click', item.id)"
        >
          <div class="notification-dot"></div>
          <div class="notification-content">
            <div class="notification-text">{{ item.content }}</div>
            <div class="notification-date">{{ item.date }}</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  Checked,
  Bell,
  Select,
  Document,
  Edit,
  Files,
} from '@element-plus/icons-vue';
import type { TodoItem } from '@/api/dashboard';

interface Props {
  todoItems: TodoItem[];
  notifications: Array<{ id: number; content: string; date: string }>;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'notification-click', id: number): void;
}>();

const router = useRouter();

const todoCount = computed(() =>
  props.todoItems.reduce((sum, item) => sum + item.count, 0)
);

const getIcon = (iconName: string) => {
  const icons: Record<string, any> = {
    Document,
    Edit,
    Files,
  };
  return icons[iconName] || Document;
};

const handleTodoClick = (item: TodoItem) => {
  router.push(item.action);
};
</script>

<style scoped>
.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  border-radius: 12px;
}

.card-header-enhanced {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.todo-list,
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-state p {
  margin-top: 12px;
  font-size: 14px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.todo-item:hover {
  background: #e9ecef;
  transform: translateX(4px);
}

.todo-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.todo-meta {
  font-size: 12px;
  color: #909399;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  margin-top: 6px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-date {
  font-size: 12px;
  color: #909399;
}
</style>
