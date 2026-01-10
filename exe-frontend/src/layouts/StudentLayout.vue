<template>
  <el-container class="student-layout" :style="layoutStyle">
    <el-header class="header" :class="{ 'mobile-header': isMobile }">
      <!-- Logo区域 -->
      <div class="header-logo">
        <el-icon><School /></el-icon>
        <span v-show="!isMobile">在线学习系统</span>
      </div>

      <!-- 桌面端：水平菜单 -->
      <div v-if="!isMobile" class="header-menu">
        <MegaMenu />
      </div>

      <!-- AI智能搜索栏（桌面端） -->
      <div v-if="!isMobile" class="header-search">
        <SmartSearchBar />
      </div>

      <!-- 用户信息 -->
      <div class="header-user">
        <!-- 主题切换按钮 -->
        <el-tooltip :content="isDark ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
          <el-button
            :icon="isDark ? Sunny : Moon"
            circle
            @click="toggleDarkMode"
            class="theme-toggle-btn"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-tooltip>

        <el-dropdown @command="handleCommand">
      <span class="avatar-dropdown">
        <UserAvatar
            :src="studentAuth.student?.avatar"
            :name="studentAuth.studentName"
            :size="isMobile ? 28 : 32"
            :frame-style="studentAuth.student?.avatarFrameStyle"
        />
        <span class="nickname-header" v-show="!isMobile" style="margin-left: 8px;">
          {{ studentAuth.studentName }}
        </span>
        <el-icon class="el-icon--right" v-show="!isMobile"><arrow-down /></el-icon>
      </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main-content" :class="{ 'mobile-content': isMobile }">
      <router-view />
    </el-main>

    <!-- 移动端：底部 TabBar -->
    <el-footer v-if="isMobile" class="mobile-tabbar">
      <div class="tabbar-container">
        <div
            v-for="item in navItems"
            :key="item.path"
            class="tab-item"
            :class="{ active: $route.path === item.path }"
            @click="$router.push(item.path)"
        >
          <el-icon :size="22">
            <component :is="item.icon" />
          </el-icon>
          <span class="tab-label">{{ item.label }}</span>
        </div>
        <!-- More menu button -->
        <div class="tab-item" :class="{ active: showMoreMenu }" @click="showMoreMenu = !showMoreMenu">
          <el-icon :size="22">
            <More />
          </el-icon>
          <span class="tab-label">更多</span>
        </div>
      </div>

      <!-- More menu popup -->
      <transition name="slide-up">
        <div v-if="showMoreMenu" class="more-menu-overlay" @click="showMoreMenu = false">
          <div class="more-menu-content" @click.stop>
            <div class="more-menu-header">
              <h3>更多功能</h3>
              <el-button text @click="showMoreMenu = false">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <div class="more-menu-grid">
              <div
                v-for="item in moreMenuItems"
                :key="item.path"
                class="more-menu-item"
                @click="handleMoreMenuClick(item.path)"
              >
                <div class="more-item-icon" :style="{ background: item.iconBg }">
                  <el-icon :size="24" :color="item.iconColor">
                    <component :is="item.icon" />
                  </el-icon>
                </div>
                <span class="more-item-label">{{ item.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </el-footer>

    <profile-edit-dialog
        v-model:visible="isProfileDialogVisible"
        @success="() => {}"
    />
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStudentAuthStore } from '@/stores/studentAuth';
import {School, ArrowDown, Lightning, House, VideoPlay, UserFilled, Tickets, Trophy, Star, Document, Moon, Sunny, More, Close, ChatDotRound, DocumentCopy, TrendCharts, Edit
} from '@element-plus/icons-vue';
import { useResponsive } from '@/composables/useResponsive';
import { onMounted, onUnmounted } from 'vue';
import { useNotificationSocketStore } from '@/stores/notificationSocket';
import ProfileEditDialog from '@/components/student/ProfileEditDialog.vue';
import UserAvatar from '@/components/UserAvatar.vue';
import { useDarkMode } from '@/composables/useDarkMode';
import SmartSearchBar from '@/components/ai/SmartSearchBar.vue';
import MegaMenu from '@/components/navigation/MegaMenu.vue';

const studentAuth = useStudentAuthStore();
const router = useRouter();
const isProfileDialogVisible = ref(false);
const { isMobile } = useResponsive();
const { isDark, toggleDarkMode } = useDarkMode();
const showMoreMenu = ref(false);

const navItems = [
  { path: '/student/dashboard', label: '首页', icon: House },
  { path: '/student/courses', label: '课程', icon: VideoPlay },
  { path: '/student/practice', label: '练习', icon: Tickets },
  { path: '/student/exams', label: '考试', icon: Document }
];

const moreMenuItems = [
  {
    path: '/student/classes',
    label: '我的班级',
    icon: UserFilled,
    iconBg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    iconColor: '#fff'
  },
  {
    path: '/student/battle',
    label: '知识对战',
    icon: Lightning,
    iconBg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    iconColor: '#fff'
  },
  {
    path: '/student/wrong-records',
    label: '错题本',
    icon: DocumentCopy,
    iconBg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    iconColor: '#fff'
  },
  {
    path: '/student/favorites',
    label: '我的收藏',
    icon: Star,
    iconBg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    iconColor: '#fff'
  },
  {
    path: '/student/ai-chat',
    label: 'AI助手',
    icon: ChatDotRound,
    iconBg: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    iconColor: '#fff'
  },
  {
    path: '/student/learning-analysis',
    label: '学习分析',
    icon: TrendCharts,
    iconBg: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    iconColor: '#fff'
  },
  {
    path: '/student/achievements',
    label: '我的成就',
    icon: Trophy,
    iconBg: 'linear-gradient(135deg, #fddb92 0%, #d1fdff 100%)',
    iconColor: '#fff'
  },
  {
    path: '/student/ai-practice',
    label: 'AI练习',
    icon: Edit,
    iconBg: 'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)',
    iconColor: '#fff'
  }
];

const handleMoreMenuClick = (path: string) => {
  router.push(path);
  showMoreMenu.value = false;
};
const handleCommand = (command: string) => {
  if (command === 'logout') {
    studentAuth.logout();
  } else if (command === 'profile') {
    router.push('/student/profile');
  }
};

const layoutStyle = computed(() => {
  const bgUrl = studentAuth.student?.backgroundUrl;
  if (bgUrl) {
    return {
      backgroundImage: `url(${bgUrl})`,
      backgroundSize: 'cover',
      backgroundAttachment: 'fixed',
      backgroundPosition: 'center'
    };
  }
  return {};
});

const socketStore = useNotificationSocketStore();

onMounted(() => {
  socketStore.connect();
});

onUnmounted(() => {
  socketStore.disconnect();
});
</script>

<style scoped>
.student-layout { height: 100vh; }
.header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  background-color: white;
}
.header-logo {
  font-size: 1.5rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--brand-color);
}
.header-menu {
  margin: 0 auto;
}
.header-menu .el-menu {
  border-bottom: none;
}
.header-user {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
.avatar-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}
.main-content {
  background-color: #f5f7fa;
}

.theme-toggle-btn {
  background-color: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.theme-toggle-btn:hover {
  background-color: var(--bg-tertiary);
  border-color: var(--border-light);
}
/* === 移动端样式 === */
.mobile-header {
  padding: 0 12px;
  height: 50px;
}

.mobile-header .header-logo {
  font-size: 1.2rem;
}

.mobile-content {
  padding: 12px;
  padding-bottom: 70px; /* 为底部 TabBar 留出空间 */
}

.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 0;
  z-index: 1000;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.tabbar-container {
  display: flex;
  height: 100%;
  padding-bottom: env(safe-area-inset-bottom); /* iPhone X 适配 */
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-item:active {
  background-color: #f5f7fa;
}

.tab-item.active {
  color: #409eff;
}

.tab-item.active::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 3px;
  background-color: #409eff;
  border-radius: 0 0 3px 3px;
}

.tab-label {
  font-size: 11px;
  font-weight: 500;
}

/* 移动端头像下拉菜单优化 */
@media (max-width: 768px) {
  .header-user {
    margin-left: auto;
  }

  .avatar-dropdown {
    gap: 0;
  }
}

/* More menu styles */
.more-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  align-items: flex-end;
}

.more-menu-content {
  width: 100%;
  background: white;
  border-radius: 20px 20px 0 0;
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.more-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.more-menu-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.more-menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.more-menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.more-item-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s;
}

.more-menu-item:active .more-item-icon {
  transform: scale(0.9);
}

.more-item-label {
  font-size: 12px;
  color: var(--el-text-color-primary);
  text-align: center;
}

/* Slide up animation */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translateY(100%);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.slide-up-enter-active .more-menu-overlay,
.slide-up-leave-active .more-menu-overlay {
  transition: opacity 0.3s;
}

.slide-up-enter-from .more-menu-overlay {
  opacity: 0;
}

</style>