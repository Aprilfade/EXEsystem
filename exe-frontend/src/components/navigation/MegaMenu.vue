<template>
  <div class="mega-menu-wrapper" @mouseleave="handleMenuLeave">
    <div class="mega-menu-header">
      <div
        v-for="category in menuCategories"
        :key="category.key"
        class="menu-category"
        :class="{ active: activeCategory === category.key }"
        @mouseenter="handleCategoryEnter(category.key)"
      >
        <el-icon v-if="category.icon" class="category-icon">
          <component :is="category.icon" />
        </el-icon>
        <span class="category-title">{{ category.title }}</span>
        <el-icon class="arrow-icon"><ArrowDown /></el-icon>
      </div>
    </div>

    <transition name="mega-menu-fade">
      <div v-if="activeCategory" class="mega-menu-dropdown" @mouseenter="cancelCloseTimer" @mouseleave="handleMenuLeave">
        <div class="dropdown-content">
          <div v-for="section in currentSections" :key="section.title" class="menu-section">
            <div class="section-header">
              <el-icon v-if="section.icon" class="section-icon">
                <component :is="section.icon" />
              </el-icon>
              <span class="section-title">{{ section.title }}</span>
            </div>
            <div class="menu-items">
              <router-link
                v-for="item in section.items"
                :key="item.path"
                :to="item.path"
                class="menu-item"
                :class="{ active: $route.path === item.path, featured: item.featured }"
                @click="closeMenu"
              >
                <el-icon v-if="item.icon" class="item-icon">
                  <component :is="item.icon" />
                </el-icon>
                <div class="item-content">
                  <div class="item-title">
                    {{ item.title }}
                    <el-tag v-if="item.badge" type="danger" size="small" effect="dark" class="item-badge">
                      {{ item.badge }}
                    </el-tag>
                    <el-tag v-if="item.tag" :type="item.tagType || 'info'" size="small" class="item-tag">
                      {{ item.tag }}
                    </el-tag>
                  </div>
                  <div v-if="item.description" class="item-description">{{ item.description }}</div>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import {
  ArrowDown,
  Reading,
  ChatDotRound,
  User,
  House,
  VideoPlay,
  Tickets,
  Document,
  UserFilled,
  Lightning,
  Star,
  Edit,
  TrendCharts,
  Histogram,
  Trophy,
  Clock,
  CollectionTag,
  Message,
  ChatLineSquare,
  DocumentCopy,
  DataAnalysis
} from '@element-plus/icons-vue';

const route = useRoute();
const activeCategory = ref<string | null>(null);
let openTimer: number | null = null;
let closeTimer: number | null = null;

const menuCategories = [
  { key: 'learning', title: '学习中心', icon: Reading },
  { key: 'ai', title: 'AI助手', icon: ChatDotRound },
  { key: 'my', title: '我的', icon: User }
];

const menuStructure = {
  learning: [
    {
      title: '课程学习',
      icon: VideoPlay,
      items: [
        { path: '/student/dashboard', title: '我的首页', icon: House, description: '查看学习概览和待办事项' },
        { path: '/student/courses', title: '课程中心', icon: VideoPlay, description: '浏览所有可用课程' },
        { path: '/student/classes', title: '我的班级', icon: UserFilled, description: '查看班级信息和同学' }
      ]
    },
    {
      title: '练习与考试',
      icon: Edit,
      items: [
        { path: '/student/practice', title: '在线练习', icon: Tickets, description: '日常练习巩固知识', tag: '推荐', tagType: 'success' },
        { path: '/student/exams', title: '模拟考试', icon: Document, description: '参加模拟考试测试' },
        { path: '/student/battle', title: '知识对战', icon: Lightning, description: '与同学实时PK', featured: true, tag: '热门', tagType: 'danger' }
      ]
    },
    {
      title: '学习工具',
      icon: CollectionTag,
      items: [
        { path: '/student/wrong-records', title: '我的错题本', icon: DocumentCopy, description: '复习错题提升成绩' },
        { path: '/student/favorites', title: '我的收藏', icon: Star, description: '收藏的题目和资源' }
      ]
    }
  ],
  ai: [
    {
      title: 'AI学习助手',
      icon: ChatLineSquare,
      items: [
        { path: '/student/ai-chat', title: 'AI对话助手', icon: ChatDotRound, description: '与AI助手交流学习问题', featured: true, tag: 'NEW', tagType: 'warning' },
        { path: '/student/ai-tutor', title: 'AI家教', icon: Reading, description: '个性化学习辅导' },
        { path: '/student/ai-practice', title: 'AI练习生成', icon: Edit, description: '智能生成练习题' }
      ]
    },
    {
      title: 'AI分析',
      icon: DataAnalysis,
      items: [
        { path: '/student/learning-analysis', title: '学习分析', icon: TrendCharts, description: 'AI分析学习数据' },
        { path: '/student/knowledge-graph', title: '知识图谱', icon: Histogram, description: '可视化知识掌握情况' }
      ]
    }
  ],
  my: [
    {
      title: '个人中心',
      icon: User,
      items: [
        { path: '/student/profile', title: '个人资料', icon: User, description: '查看和编辑个人信息' },
        { path: '/student/achievements', title: '我的成就', icon: Trophy, description: '查看获得的成就徽章' },
        { path: '/student/study-time', title: '学习时长', icon: Clock, description: '统计学习时间' }
      ]
    },
    {
      title: '消息中心',
      icon: Message,
      items: [
        { path: '/student/notifications', title: '系统通知', icon: Message, description: '查看系统消息' }
      ]
    }
  ]
};

const currentSections = computed(() => {
  return activeCategory.value ? menuStructure[activeCategory.value as keyof typeof menuStructure] : [];
});

const handleCategoryEnter = (key: string) => {
  if (closeTimer) {
    clearTimeout(closeTimer);
    closeTimer = null;
  }

  if (openTimer) {
    clearTimeout(openTimer);
  }

  openTimer = window.setTimeout(() => {
    activeCategory.value = key;
  }, 300);
};

const handleMenuLeave = () => {
  if (openTimer) {
    clearTimeout(openTimer);
    openTimer = null;
  }

  if (closeTimer) {
    clearTimeout(closeTimer);
  }

  closeTimer = window.setTimeout(() => {
    activeCategory.value = null;
  }, 500);
};

const cancelCloseTimer = () => {
  if (closeTimer) {
    clearTimeout(closeTimer);
    closeTimer = null;
  }
};

const closeMenu = () => {
  activeCategory.value = null;
  if (openTimer) clearTimeout(openTimer);
  if (closeTimer) clearTimeout(closeTimer);
};
</script>

<style scoped>
.mega-menu-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.mega-menu-header {
  display: flex;
  gap: 4px;
}

.menu-category {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  position: relative;
}

.menu-category:hover,
.menu-category.active {
  background-color: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

.category-icon {
  font-size: 16px;
}

.category-title {
  white-space: nowrap;
}

.arrow-icon {
  font-size: 12px;
  transition: transform 0.3s;
}

.menu-category.active .arrow-icon {
  transform: rotate(180deg);
}

.mega-menu-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  padding: 20px;
  min-width: 600px;
}

.dropdown-content {
  display: flex;
  gap: 30px;
}

.menu-section {
  flex: 1;
  min-width: 180px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--el-border-color-lighter);
}

.section-icon {
  font-size: 18px;
  color: var(--el-color-primary);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.menu-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  text-decoration: none;
  color: var(--el-text-color-regular);
  transition: all 0.3s;
  position: relative;
}

.menu-item:hover {
  background-color: var(--el-fill-color-light);
  color: var(--el-color-primary);
  transform: translateX(2px);
}

.menu-item.active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.menu-item.featured {
  border: 1px solid var(--el-color-primary-light-7);
  background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, white 100%);
}

.item-icon {
  font-size: 18px;
  margin-top: 2px;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.item-badge {
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 16px;
}

.item-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 16px;
}

.item-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.mega-menu-fade-enter-active,
.mega-menu-fade-leave-active {
  transition: all 0.3s ease;
}

.mega-menu-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.mega-menu-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

/* Dark mode support */
html.dark .mega-menu-dropdown {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
}

html.dark .menu-item {
  color: var(--el-text-color-regular);
}

html.dark .menu-item:hover {
  background-color: var(--el-fill-color);
}

html.dark .menu-item.featured {
  background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, var(--el-bg-color) 100%);
}
</style>
