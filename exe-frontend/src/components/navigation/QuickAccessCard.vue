<template>
  <div class="quick-access-wrapper">
    <div class="section-header">
      <h3 class="section-title">
        <el-icon><Lightning /></el-icon>
        快捷访问
      </h3>
      <el-button text @click="handleCustomize">
        <el-icon><Setting /></el-icon>
        自定义
      </el-button>
    </div>

    <div class="quick-access-grid">
      <div
        v-for="item in quickAccessItems"
        :key="item.id"
        class="quick-card"
        :class="{ 'card-highlight': item.highlight }"
        @click="handleCardClick(item)"
      >
        <div class="card-icon" :style="{ background: item.iconBg }">
          <el-icon :size="24" :color="item.iconColor">
            <component :is="item.icon" />
          </el-icon>
        </div>
        <div class="card-content">
          <div class="card-title">
            {{ item.title }}
            <el-badge v-if="item.count" :value="item.count" :max="99" class="card-badge" />
          </div>
          <div class="card-description">{{ item.description }}</div>
        </div>
        <div v-if="item.tag" class="card-tag" :style="{ background: item.tagColor }">
          {{ item.tag }}
        </div>
      </div>
    </div>

    <!-- Customize Dialog -->
    <el-dialog
      v-model="customizeDialogVisible"
      title="自定义快捷访问"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="customize-content">
        <p class="customize-hint">拖拽调整顺序，点击切换显示/隐藏（最多显示8个）</p>
        <div class="available-items">
          <div
            v-for="item in allAvailableItems"
            :key="item.id"
            class="available-item"
            :class="{ selected: isItemSelected(item.id), disabled: !isItemSelected(item.id) && selectedCount >= 8 }"
            @click="toggleItem(item.id)"
          >
            <el-icon :size="20">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
            <el-icon v-if="isItemSelected(item.id)" class="check-icon" color="#67c23a">
              <Check />
            </el-icon>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="customizeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCustomization">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  Lightning,
  Setting,
  Check,
  Tickets,
  Document,
  ChatDotRound,
  DocumentCopy,
  Star,
  TrendCharts,
  Trophy,
  VideoPlay,
  UserFilled,
  Edit,
  House,
  Message,
  Clock
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();
const customizeDialogVisible = ref(false);
const selectedItemIds = ref<string[]>([]);

interface QuickAccessItem {
  id: string;
  title: string;
  description: string;
  icon: any;
  iconBg: string;
  iconColor: string;
  path: string;
  count?: number;
  tag?: string;
  tagColor?: string;
  highlight?: boolean;
}

// All available quick access items
const allAvailableItems: QuickAccessItem[] = [
  {
    id: 'practice',
    title: '开始练习',
    description: '继续今日练习任务',
    icon: Tickets,
    iconBg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    iconColor: '#fff',
    path: '/student/practice',
    count: 5,
    tag: '推荐',
    tagColor: '#67c23a'
  },
  {
    id: 'exam',
    title: '模拟考试',
    description: '参加模拟考试',
    icon: Document,
    iconBg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    iconColor: '#fff',
    path: '/student/exams',
    tag: '热门',
    tagColor: '#f56c6c'
  },
  {
    id: 'ai-chat',
    title: 'AI助手',
    description: '与AI助手对话',
    icon: ChatDotRound,
    iconBg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    iconColor: '#fff',
    path: '/student/ai-chat',
    highlight: true,
    tag: 'NEW',
    tagColor: '#e6a23c'
  },
  {
    id: 'wrong-records',
    title: '错题本',
    description: '复习我的错题',
    icon: DocumentCopy,
    iconBg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    iconColor: '#fff',
    path: '/student/wrong-records',
    count: 12
  },
  {
    id: 'favorites',
    title: '我的收藏',
    description: '查看收藏内容',
    icon: Star,
    iconBg: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    iconColor: '#fff',
    path: '/student/favorites'
  },
  {
    id: 'analysis',
    title: '学习分析',
    description: '查看学习数据',
    icon: TrendCharts,
    iconBg: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    iconColor: '#fff',
    path: '/student/learning-analysis'
  },
  {
    id: 'achievements',
    title: '我的成就',
    description: '查看获得的成就',
    icon: Trophy,
    iconBg: 'linear-gradient(135deg, #fddb92 0%, #d1fdff 100%)',
    iconColor: '#fff',
    path: '/student/achievements'
  },
  {
    id: 'courses',
    title: '课程中心',
    description: '浏览所有课程',
    icon: VideoPlay,
    iconBg: 'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)',
    iconColor: '#fff',
    path: '/student/courses'
  },
  {
    id: 'classes',
    title: '我的班级',
    description: '查看班级信息',
    icon: UserFilled,
    iconBg: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)',
    iconColor: '#fff',
    path: '/student/classes'
  },
  {
    id: 'battle',
    title: '知识对战',
    description: '与同学PK',
    icon: Lightning,
    iconBg: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
    iconColor: '#fff',
    path: '/student/battle',
    tag: '热门',
    tagColor: '#f56c6c'
  },
  {
    id: 'ai-practice',
    title: 'AI练习生成',
    description: '智能生成练习题',
    icon: Edit,
    iconBg: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
    iconColor: '#fff',
    path: '/student/ai-practice'
  },
  {
    id: 'dashboard',
    title: '我的首页',
    description: '返回首页概览',
    icon: House,
    iconBg: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
    iconColor: '#fff',
    path: '/student/dashboard'
  },
  {
    id: 'notifications',
    title: '系统通知',
    description: '查看消息通知',
    icon: Message,
    iconBg: 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)',
    iconColor: '#fff',
    path: '/student/notifications',
    count: 3
  },
  {
    id: 'study-time',
    title: '学习时长',
    description: '统计学习时间',
    icon: Clock,
    iconBg: 'linear-gradient(135deg, #fdcbf1 0%, #e6dee9 100%)',
    iconColor: '#fff',
    path: '/student/study-time'
  }
];

// Load user's quick access preferences from localStorage
const loadUserPreferences = () => {
  const saved = localStorage.getItem('quickAccessPreferences');
  if (saved) {
    try {
      selectedItemIds.value = JSON.parse(saved);
    } catch (e) {
      // Use default if parsing fails
      selectedItemIds.value = ['practice', 'exam', 'ai-chat', 'wrong-records', 'favorites', 'analysis'];
    }
  } else {
    // Default selection
    selectedItemIds.value = ['practice', 'exam', 'ai-chat', 'wrong-records', 'favorites', 'analysis'];
  }
};

const quickAccessItems = computed(() => {
  return allAvailableItems.filter(item => selectedItemIds.value.includes(item.id));
});

const selectedCount = computed(() => selectedItemIds.value.length);

const isItemSelected = (id: string) => {
  return selectedItemIds.value.includes(id);
};

const toggleItem = (id: string) => {
  if (isItemSelected(id)) {
    selectedItemIds.value = selectedItemIds.value.filter(itemId => itemId !== id);
  } else {
    if (selectedCount.value >= 8) {
      ElMessage.warning('最多只能选择8个快捷访问项');
      return;
    }
    selectedItemIds.value.push(id);
  }
};

const handleCustomize = () => {
  customizeDialogVisible.value = true;
};

const saveCustomization = () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('至少选择一个快捷访问项');
    return;
  }
  localStorage.setItem('quickAccessPreferences', JSON.stringify(selectedItemIds.value));
  ElMessage.success('保存成功');
  customizeDialogVisible.value = false;
};

const handleCardClick = (item: QuickAccessItem) => {
  router.push(item.path);
};

onMounted(() => {
  loadUserPreferences();
});
</script>

<style scoped>
.quick-access-wrapper {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.quick-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.quick-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.quick-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.quick-card:hover::before {
  opacity: 1;
}

.card-highlight {
  border: 1px solid var(--el-color-primary-light-7);
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, white 100%);
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-description {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.card-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  color: white;
  text-transform: uppercase;
}

.card-badge {
  margin-left: 4px;
}

/* Customize Dialog */
.customize-content {
  padding: 12px 0;
}

.customize-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 16px;
}

.available-items {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.available-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.available-item:hover:not(.disabled) {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.available-item.selected {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.available-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.check-icon {
  margin-left: auto;
}

/* Responsive */
@media (max-width: 768px) {
  .quick-access-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .quick-card {
    padding: 16px;
  }

  .card-icon {
    width: 48px;
    height: 48px;
  }

  .available-items {
    grid-template-columns: 1fr;
  }
}

/* Dark mode */
html.dark .quick-card {
  background: var(--el-bg-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

html.dark .quick-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
}

html.dark .available-item {
  background: var(--el-bg-color);
}
</style>
