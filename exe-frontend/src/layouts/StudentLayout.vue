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
        <el-menu mode="horizontal" :default-active="$route.path" router :ellipsis="false">
          <el-menu-item index="/student/dashboard">我的首页</el-menu-item>
          <el-menu-item index="/student/courses">课程中心</el-menu-item>
          <el-menu-item index="/student/classes">我的班级</el-menu-item>
          <el-menu-item index="/student/wrong-records">我的错题本</el-menu-item>
          <el-menu-item index="/student/practice">在线练习</el-menu-item>
          <el-menu-item index="/student/battle"><el-icon><Lightning /></el-icon> 知识对战</el-menu-item>
          <el-menu-item index="/student/exams">模拟考试</el-menu-item>
          <el-menu-item index="/student/favorites">我的收藏</el-menu-item>
        </el-menu>
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
      </div>
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
import {School, ArrowDown, Lightning, House, VideoPlay, UserFilled, Tickets, Trophy, Star, Document, Moon, Sunny
} from '@element-plus/icons-vue';
import { useResponsive } from '@/composables/useResponsive';
import { onMounted, onUnmounted } from 'vue';
import { useNotificationSocketStore } from '@/stores/notificationSocket';
import ProfileEditDialog from '@/components/student/ProfileEditDialog.vue';
import UserAvatar from '@/components/UserAvatar.vue';
import { useDarkMode } from '@/composables/useDarkMode';

const studentAuth = useStudentAuthStore();
const router = useRouter();
const isProfileDialogVisible = ref(false);
const { isMobile } = useResponsive();
const { isDark, toggleDarkMode } = useDarkMode();
const navItems = [
  { path: '/student/dashboard', label: '首页', icon: House },
  { path: '/student/courses', label: '课程', icon: VideoPlay },
  { path: '/student/classes', label: '班级', icon: UserFilled },
  { path: '/student/practice', label: '练习', icon: Tickets },
  { path: '/student/exams', label: '考试', icon: Document }
];
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
</style>