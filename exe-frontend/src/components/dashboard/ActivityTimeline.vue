<template>
  <el-card shadow="never" class="section-card activities-card">
    <template #header>
      <div class="card-header-enhanced">
        <div class="header-title">
          <el-icon><Clock /></el-icon>
          <span>最近活动</span>
        </div>
        <el-tag size="small" type="info">最近 7 天</el-tag>
      </div>
    </template>
    <div class="activities-container">
      <el-empty
        v-if="!activities || activities.length === 0"
        description="暂无活动记录"
        :image-size="80"
      />
      <el-timeline v-else>
        <el-timeline-item
          v-for="(activity, index) in activities"
          :key="index"
          :timestamp="activity.time"
          :color="getColor(activity.color)"
          placement="top"
        >
          <div class="activity-content">
            <el-icon class="activity-icon" :style="{ color: getColor(activity.color) }">
              <component :is="getIcon(activity.icon)" />
            </el-icon>
            <span>{{ activity.content }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import {
  Clock,
  Files,
  Edit,
  Upload,
  Bell,
  InfoFilled,
  Document,
  Checked,
} from '@element-plus/icons-vue';
import type { RecentActivity } from '@/api/dashboard';

interface Props {
  activities: RecentActivity[];
}

defineProps<Props>();

const getIcon = (iconName: string) => {
  const icons: Record<string, any> = {
    Files,
    Edit,
    Upload,
    Bell,
    InfoFilled,
    Document,
    Checked,
  };
  return icons[iconName] || InfoFilled;
};

const getColor = (colorName: string) => {
  const colors: Record<string, string> = {
    primary: '#409EFF',
    success: '#67C23A',
    warning: '#E6A23C',
    danger: '#F56C6C',
    info: '#909399',
  };
  return colors[colorName] || colors.info;
};
</script>

<style scoped>
.activities-card {
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

.activities-container {
  max-height: 400px;
  overflow-y: auto;
}

.activities-container::-webkit-scrollbar {
  width: 6px;
}

.activities-container::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.activities-container::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.activity-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.activity-icon {
  font-size: 16px;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #909399;
}
</style>
