<template>
  <div class="modern-course-card" @click="handleClick">
    <!-- 封面图片区域 -->
    <div class="card-cover-section">
      <div class="card-cover">
        <img
          :src="course.coverUrl || defaultCover"
          :alt="course.name"
          @error="handleImageError"
        />
        <!-- 悬停遮罩 -->
        <div class="cover-overlay">
          <el-button type="primary" size="large" round class="continue-btn">
            <el-icon><VideoPlay /></el-icon>
            {{ progressPercent > 0 ? '继续学习' : '开始学习' }}
          </el-button>
        </div>
      </div>

      <!-- 进度环 -->
      <div class="progress-ring" v-if="progressPercent > 0">
        <el-progress
          type="circle"
          :percentage="progressPercent"
          :width="60"
          :stroke-width="6"
          :color="progressColor"
        >
          <template #default="{ percentage }">
            <span class="progress-value">{{ percentage }}%</span>
          </template>
        </el-progress>
      </div>

      <!-- 状态标签 -->
      <div class="status-tags">
        <el-tag v-if="isNew" type="danger" effect="dark" size="small" class="status-tag">
          <el-icon><Star /></el-icon>
          NEW
        </el-tag>
        <el-tag v-if="isCompleted" type="success" effect="dark" size="small" class="status-tag">
          <el-icon><CircleCheck /></el-icon>
          已完成
        </el-tag>
        <el-tag v-else-if="progressPercent > 0" type="warning" effect="dark" size="small" class="status-tag">
          <el-icon><Clock /></el-icon>
          学习中
        </el-tag>
      </div>

      <!-- 科目标签 -->
      <div class="subject-badge">{{ subjectName }}</div>
    </div>

    <!-- 课程信息区域 -->
    <div class="card-content">
      <h3 class="course-title" :title="course.name">{{ course.name }}</h3>

      <div class="course-meta">
        <div class="meta-item">
          <el-icon><User /></el-icon>
          <span>{{ course.teacherName || '未知教师' }}</span>
        </div>
        <div class="meta-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(course.createTime) }}</span>
        </div>
      </div>

      <p class="course-desc">{{ course.description || '暂无课程简介...' }}</p>

      <!-- 学习统计 -->
      <div class="study-stats" v-if="progressPercent > 0">
        <div class="stat-item">
          <span class="stat-label">学习进度</span>
          <el-progress
            :percentage="progressPercent"
            :stroke-width="6"
            :show-text="false"
            :color="progressColor"
          />
        </div>
        <div class="stat-item">
          <span class="stat-label">已完成</span>
          <span class="stat-value">{{ completedCount }} / {{ totalCount }}</span>
        </div>
        <div class="stat-item" v-if="studyTime > 0">
          <span class="stat-label">学习时长</span>
          <span class="stat-value">{{ formatStudyTime(studyTime) }}</span>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="card-footer">
        <div class="footer-left">
          <el-tag size="small" effect="plain">{{ course.grade }}</el-tag>
        </div>
        <div class="footer-right">
          <el-button type="primary" text @click.stop="handleQuickStart">
            <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Course } from '@/api/course';
import { VideoPlay, Star, CircleCheck, Clock, User, Calendar, Right } from '@element-plus/icons-vue';

/**
 * 组件属性
 */
interface Props {
  course: Course;
  subjectName?: string;
  progressPercent?: number;
  completedCount?: number;
  totalCount?: number;
  studyTime?: number; // 秒
}

const props = withDefaults(defineProps<Props>(), {
  subjectName: '综合',
  progressPercent: 0,
  completedCount: 0,
  totalCount: 0,
  studyTime: 0
});

/**
 * 组件事件
 */
const emit = defineEmits<{
  (e: 'click', course: Course): void;
  (e: 'quickStart', course: Course): void;
}>();

/**
 * 默认封面图
 */
const defaultCover = '/default-course.jpg';

/**
 * 是否为新课程（7天内）
 */
const isNew = computed(() => {
  if (!props.course.createTime) return false;
  const createTime = new Date(props.course.createTime).getTime();
  const now = Date.now();
  const daysDiff = (now - createTime) / (1000 * 60 * 60 * 24);
  return daysDiff <= 7;
});

/**
 * 是否已完成
 */
const isCompleted = computed(() => {
  return props.progressPercent >= 95;
});

/**
 * 进度条颜色
 */
const progressColor = computed(() => {
  if (props.progressPercent >= 95) return '#67C23A';
  if (props.progressPercent >= 50) return '#409EFF';
  return '#E6A23C';
});

/**
 * 格式化日期
 */
const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return '今天';
  if (diffDays === 1) return '昨天';
  if (diffDays < 7) return `${diffDays}天前`;
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`;

  return dateStr.split('T')[0];
};

/**
 * 格式化学习时长
 */
const formatStudyTime = (seconds: number): string => {
  if (seconds < 60) return `${seconds}秒`;
  if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60);
    return `${minutes}分钟`;
  }
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${hours}小时${minutes}分`;
};

/**
 * 图片加载失败处理
 */
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement;
  img.src = defaultCover;
};

/**
 * 卡片点击
 */
const handleClick = () => {
  emit('click', props.course);
};

/**
 * 快速开始
 */
const handleQuickStart = () => {
  emit('quickStart', props.course);
};
</script>

<style scoped lang="scss">
.modern-course-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 100%;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);

    .card-cover img {
      transform: scale(1.1);
    }

    .cover-overlay {
      opacity: 1;
    }
  }
}

// 封面区域
.card-cover-section {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-cover {
  width: 100%;
  height: 100%;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.continue-btn {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);

  .el-icon {
    margin-right: 8px;
    font-size: 18px;
  }
}

// 进度环
.progress-ring {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.progress-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

// 状态标签
.status-tags {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-tag {
  backdrop-filter: blur(4px);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;

  .el-icon {
    font-size: 14px;
  }
}

// 科目标签
.subject-badge {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.95);
  color: #606266;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

// 内容区域
.card-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;

  .el-icon {
    font-size: 14px;
  }
}

.course-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 16px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

// 学习统计
.study-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  .stat-label {
    font-size: 13px;
    color: #909399;
    white-space: nowrap;
  }

  .stat-value {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .el-progress {
    flex: 1;
  }
}

// 底部
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: auto;
}

.footer-left {
  flex: 1;
}

.footer-right {
  .el-button {
    font-size: 18px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .card-cover-section {
    height: 160px;
  }

  .course-title {
    font-size: 16px;
  }

  .card-content {
    padding: 16px;
  }

  .continue-btn {
    padding: 10px 24px;
    font-size: 14px;
  }
}
</style>
