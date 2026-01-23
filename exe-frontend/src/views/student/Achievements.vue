<template>
  <div class="achievements-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <el-icon class="header-icon"><Trophy /></el-icon>
          <div>
            <h2>我的成就</h2>
            <p class="subtitle">解锁成就，见证您的学习历程</p>
          </div>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <span class="stat-value">{{ unlockedCount }}</span>
            <span class="stat-label">已解锁</span>
          </div>
          <div class="stat-divider">/</div>
          <div class="stat-item">
            <span class="stat-value">{{ totalCount }}</span>
            <span class="stat-label">总成就</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 成就进度条 -->
    <el-card class="progress-card" shadow="hover">
      <div class="progress-content">
        <div class="progress-info">
          <span class="progress-text">成就完成度</span>
          <span class="progress-percent">{{ progressPercent }}%</span>
        </div>
        <el-progress
          :percentage="progressPercent"
          :stroke-width="12"
          :color="progressColor"
        />
      </div>
    </el-card>

    <!-- 成就筛选 -->
    <div class="filter-section">
      <el-radio-group v-model="filterType" @change="handleFilterChange" size="large">
        <el-radio-button label="all">全部成就</el-radio-button>
        <el-radio-button label="unlocked">已解锁</el-radio-button>
        <el-radio-button label="locked">未解锁</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 成就列表 -->
    <div class="achievements-grid" v-loading="loading">
      <transition-group name="achievement-list">
        <el-card
          v-for="achievement in filteredAchievements"
          :key="achievement.id"
          :class="['achievement-card', achievement.isUnlocked ? 'unlocked' : 'locked']"
          shadow="hover"
        >
          <div class="achievement-content">
            <!-- 成就图标 -->
            <div class="achievement-icon-wrapper">
              <!-- 如果是emoji（单个字符或几个字符），直接显示文字 -->
              <div
                v-if="achievement.iconUrl && isEmoji(achievement.iconUrl)"
                :class="['achievement-icon-emoji', achievement.isUnlocked ? 'unlocked' : 'locked']"
              >
                {{ achievement.iconUrl }}
              </div>
              <!-- 如果是URL（http开头），显示图片 -->
              <img
                v-else-if="achievement.iconUrl && achievement.iconUrl.startsWith('http')"
                :src="achievement.iconUrl"
                :alt="achievement.name"
                class="achievement-icon"
              />
              <!-- 否则显示默认图标 -->
              <el-icon v-else class="achievement-icon-fallback" :size="60">
                <Trophy />
              </el-icon>
              <div v-if="achievement.isUnlocked" class="unlock-badge">
                <el-icon><Check /></el-icon>
              </div>
              <div v-else class="lock-overlay">
                <el-icon><Lock /></el-icon>
              </div>
            </div>

            <!-- 成就信息 -->
            <div class="achievement-info">
              <h3 class="achievement-name">{{ achievement.name }}</h3>
              <p class="achievement-desc">{{ achievement.description }}</p>

              <!-- 进度信息 -->
              <div class="achievement-meta">
                <el-tag :type="achievement.isUnlocked ? 'success' : 'info'" size="small">
                  {{ getTypeLabel(achievement.type) }}
                </el-tag>
                <span class="threshold">目标: {{ achievement.threshold }}</span>
              </div>

              <!-- 奖励积分 -->
              <div class="achievement-reward">
                <el-icon class="coin-icon"><Coin /></el-icon>
                <span>+{{ achievement.rewardPoints }} 积分</span>
              </div>

              <!-- 解锁时间 -->
              <div v-if="achievement.isUnlocked && achievement.unlockTime" class="unlock-time">
                <el-icon><Clock /></el-icon>
                <span>{{ formatUnlockTime(achievement.unlockTime) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </transition-group>

      <!-- 空状态 -->
      <el-empty
        v-if="filteredAchievements.length === 0"
        description="暂无成就数据"
        :image-size="120"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Trophy, Lock, Check, Clock, Coin } from '@element-plus/icons-vue';
import { getMyAchievements, type Achievement } from '@/api/achievement';

// 响应式数据
const loading = ref(false);
const achievements = ref<Achievement[]>([]);
const filterType = ref<'all' | 'unlocked' | 'locked'>('all');

// 计算属性
const totalCount = computed(() => achievements.value.length);
const unlockedCount = computed(() => achievements.value.filter(a => a.isUnlocked).length);
const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0;
  return Math.round((unlockedCount.value / totalCount.value) * 100);
});

const progressColor = computed(() => {
  const percent = progressPercent.value;
  if (percent < 30) return '#f56c6c';
  if (percent < 60) return '#e6a23c';
  if (percent < 90) return '#409eff';
  return '#67c23a';
});

const filteredAchievements = computed(() => {
  if (filterType.value === 'all') return achievements.value;
  if (filterType.value === 'unlocked') return achievements.value.filter(a => a.isUnlocked);
  return achievements.value.filter(a => !a.isUnlocked);
});

// 方法
const loadAchievements = async () => {
  loading.value = true;
  try {
    const res = await getMyAchievements();
    if (res.code === 200) {
      achievements.value = res.data;
    } else {
      ElMessage.error(res.message || '加载成就失败');
    }
  } catch (error) {
    console.error('加载成就失败:', error);
    ElMessage.error('加载成就失败');
  } finally {
    loading.value = false;
  }
};

const handleFilterChange = () => {
  // 筛选已在计算属性中处理
};

const isEmoji = (str: string): boolean => {
  // 判断是否为emoji或短字符（长度小于10且不是http开头）
  return str.length < 10 && !str.startsWith('http');
};

const getTypeLabel = (type: string): string => {
  const typeMap: Record<string, string> = {
    'SIGN_IN_STREAK': '签到类',
    'TOTAL_QUESTIONS': '答题类',
    'PERFECT_PAPER': '满分类',
    'LEARNING_TIME': '学习类',
    'BATTLE': '对战类',
    'WRONG_RECORD': '错题类',
    'AI_CHAT': 'AI类',
    'EXAM_SCORE': '成绩类',
    'FAVORITE': '收藏类',
    'COMPREHENSIVE': '综合类'
  };
  return typeMap[type] || '其他';
};

const formatUnlockTime = (time: string): string => {
  const date = new Date(time);
  const now = new Date();
  const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return '今天解锁';
  if (diffDays === 1) return '昨天解锁';
  if (diffDays < 7) return `${diffDays}天前解锁`;
  return date.toLocaleDateString('zh-CN');
};

// 生命周期
onMounted(() => {
  loadAchievements();
});
</script>

<style scoped lang="scss">
.achievements-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  font-size: 48px;
  color: #ffd700;
}

h2 {
  margin: 0;
  font-size: 28px;
  color: #2c3e50;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.stat-divider {
  font-size: 24px;
  opacity: 0.5;
}

/* 进度卡片 */
.progress-card {
  margin-bottom: 24px;
}

.progress-content {
  padding: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.progress-text {
  font-size: 16px;
  color: #2c3e50;
  font-weight: 500;
}

.progress-percent {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

/* 成就网格 */
.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  min-height: 300px;
}

/* 成就卡片 */
.achievement-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }

  &.unlocked {
    border: 2px solid #67c23a;
  }

  &.locked {
    opacity: 0.7;
  }
}

.achievement-content {
  display: flex;
  gap: 16px;
  padding: 8px;
}

/* 成就图标 */
.achievement-icon-wrapper {
  position: relative;
  flex-shrink: 0;
  width: 80px;
  height: 80px;
}

.achievement-icon-emoji {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  border-radius: 12px;
  user-select: none;
  transition: all 0.3s ease;

  &.unlocked {
    background: linear-gradient(135deg, #ffd700, #ffed4e);
  }

  &.locked {
    background: linear-gradient(135deg, #b0b0b0, #d0d0d0);
    filter: grayscale(100%);
    opacity: 0.6;
  }
}

.achievement-icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

.achievement-icon-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  border-radius: 12px;
  color: white;
}

.unlock-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 28px;
  height: 28px;
  background: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.lock-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
}

/* 成就信息 */
.achievement-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.achievement-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.achievement-desc {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.achievement-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
}

.achievement-reward {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #e6a23c;
  font-weight: 600;
}

.coin-icon {
  font-size: 16px;
}

.unlock-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #67c23a;
}

/* 动画 */
.achievement-list-enter-active,
.achievement-list-leave-active {
  transition: all 0.3s ease;
}

.achievement-list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.achievement-list-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .achievements-grid {
    grid-template-columns: 1fr;
  }

  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .header-stats {
    width: 100%;
    justify-content: center;
  }
}
</style>
