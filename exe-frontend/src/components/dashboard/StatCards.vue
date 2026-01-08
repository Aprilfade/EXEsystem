<template>
  <el-row :gutter="20" class="stat-row">
    <el-col
      :xs="12"
      :sm="8"
      :md="24 / 6"
      class="stat-card-item"
      v-for="(card, index) in statCards"
      :key="index"
    >
      <el-card shadow="hover" :class="['stat-card-enhanced', card.class]">
        <div class="stat-content">
          <div class="stat-icon-wrapper" :style="{ background: card.gradient }">
            <el-icon :size="28">
              <component :is="card.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">
              <transition name="number-slide" mode="out-in">
                <span :key="card.value">{{ card.value }}</span>
              </transition>
            </div>
            <div
              class="stat-trend"
              v-if="card.trend !== null && card.trend !== undefined"
            >
              <el-icon
                :color="
                  card.trend > 0
                    ? '#67C23A'
                    : card.trend < 0
                    ? '#F56C6C'
                    : '#909399'
                "
                :size="12"
              >
                <component
                  :is="
                    card.trend > 0
                      ? ArrowUp
                      : card.trend < 0
                      ? ArrowDown
                      : Minus
                  "
                />
              </el-icon>
              <span
                :style="{
                  color:
                    card.trend > 0
                      ? '#67C23A'
                      : card.trend < 0
                      ? '#F56C6C'
                      : '#909399',
                }"
              >
                {{ card.trend === 0 ? '持平' : Math.abs(card.trend) + '%' }}
              </span>
              <span class="trend-label">较上周</span>
            </div>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  UserFilled,
  Notebook,
  CollectionTag,
  DocumentCopy,
  FolderOpened,
  Reading,
  ArrowUp,
  ArrowDown,
  Minus,
} from '@element-plus/icons-vue';
import type { DashboardStats } from '@/api/dashboard';

interface Props {
  stats: DashboardStats;
  onlineCount: number;
}

const props = defineProps<Props>();

const statCards = computed(() => [
  {
    label: '在线学生',
    value: props.onlineCount,
    icon: Reading,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    trend: props.stats.trends?.onlineCountTrend || 0,
  },
  {
    label: '学生总数',
    value: props.stats.studentCount,
    icon: UserFilled,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    trend: props.stats.trends?.studentCountTrend || 0,
  },
  {
    label: '科目数量',
    value: props.stats.subjectCount,
    icon: Notebook,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    trend: props.stats.trends?.subjectCountTrend || 0,
  },
  {
    label: '知识点',
    value: props.stats.knowledgePointCount,
    icon: CollectionTag,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    trend: props.stats.trends?.knowledgePointCountTrend || 0,
  },
  {
    label: '题目数量',
    value: props.stats.questionCount,
    icon: DocumentCopy,
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    trend: props.stats.trends?.questionCountTrend || 0,
  },
  {
    label: '试卷数量',
    value: props.stats.paperCount,
    icon: FolderOpened,
    gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
    trend: props.stats.trends?.paperCountTrend || 0,
  },
]);
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}

.stat-card-enhanced {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-card-enhanced:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.trend-label {
  color: #909399;
  margin-left: 4px;
}

/* 数字滑动动画 */
.number-slide-enter-active,
.number-slide-leave-active {
  transition: all 0.3s ease;
}

.number-slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.number-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
