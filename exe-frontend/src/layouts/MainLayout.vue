<template>
  <el-container class="main-layout">
    <el-aside v-if="!isMobile" :width="isCollapsed ? '64px' : '220px'" class="aside">
      <div class="logo-area">
        <el-icon :size="32" color="#409eff"><Management /></el-icon>
        <h1 v-show="!isCollapsed">试题管理系统</h1>
      </div>
      <el-menu :default-active="$route.path" router class="main-menu" :collapse="isCollapsed" :collapse-transition="false">
        <template v-for="menu in visibleMenus" :key="menu.path">
          <el-menu-item v-if="!menu.children" :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
          <el-sub-menu v-else :index="menu.path">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
              <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>

      <div class="sidebar-footer" v-show="!isCollapsed">
        <div class="user-profile-small">
          <el-avatar v-if="authStore.user?.avatar" :size="32" :src="authStore.user.avatar" />
          <el-avatar v-else :size="32">{{ authStore.userNickname.charAt(0) }}</el-avatar>
          <div class="user-details">
            <span class="nickname">{{ authStore.userNickname }}</span>
            <span class="email">{{ authStore.user?.username }}</span>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </el-aside>
    <!-- 移动端：抽屉菜单 -->
    <el-drawer
        v-if="isMobile"
        v-model="drawerVisible"
        direction="left"
        :size="280"
        :show-close="false"
        class="mobile-drawer"
    >
      <template #header>
        <div class="drawer-header">
          <el-icon :size="28" color="#409eff"><Management /></el-icon>
          <h3>试题管理系统</h3>
        </div>
      </template>

      <el-menu :default-active="$route.path" @select="handleMobileMenuSelect" class="mobile-menu">
        <template v-for="menu in visibleMenus" :key="menu.path">
          <el-menu-item v-if="!menu.children" :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
          <el-sub-menu v-else :index="menu.path">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
              <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>

      <template #footer>
        <div class="drawer-footer">
          <div class="user-info-mobile">
            <el-avatar v-if="authStore.user?.avatar" :size="40" :src="authStore.user.avatar" />
            <el-avatar v-else :size="40">{{ authStore.userNickname.charAt(0) }}</el-avatar>
            <div class="user-details">
              <span class="nickname">{{ authStore.userNickname }}</span>
              <span class="email">{{ authStore.user?.username }}</span>
            </div>
          </div>
        </div>
      </template>
    </el-drawer>

    <el-container>
      <el-header class="header" :class="{ 'mobile-header': isMobile }">
        <div class="header-left">
          <!-- 移动端：汉堡菜单按钮 -->
          <el-button
              v-if="isMobile"
              :icon="Menu"
              circle
              @click="drawerVisible = true"
              class="mobile-menu-btn"
          />

          <!-- 桌面端：折叠按钮 -->
          <el-icon v-else class="collapse-btn" @click="isCollapsed = !isCollapsed" :size="22">
            <component :is="isCollapsed ? Expand : Fold" />
          </el-icon>
        </div>

        <div class="header-right">
          <!-- 主题切换按钮 -->
          <el-tooltip :content="isDark ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
            <el-button
              :icon="isDark ? Sunny : Moon"
              circle
              @click="toggleDarkMode"
              class="theme-toggle-btn"
            />
          </el-tooltip>

          <el-dropdown @command="handleCommand">
            <span class="avatar-dropdown">
              <el-avatar v-if="authStore.user?.avatar" :src="authStore.user.avatar" />
              <el-avatar v-else>{{ authStore.userNickname.charAt(0) }}</el-avatar>
              <span class="nickname-header">{{ authStore.userNickname }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <profile-edit-dialog
        v-model:visible="isProfileDialogVisible"
        @success="() => {}"
    />
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import ProfileEditDialog from '@/components/user/ProfileEditDialog.vue';
import { useResponsive } from '@/composables/useResponsive';
import { useDarkMode } from '@/composables/useDarkMode';

// 2. 引入图标和 WebSocket Store
import {
  Management, House, Collection, Reading, Tickets, DocumentCopy, Avatar, CircleClose, DataLine,
  User, Search, Bell, ArrowRight, ArrowDown, Lock as LockIcon, Setting, Fold, Expand, Document, Medal,
  VideoPlay, DataBoard, Share, Menu, Moon, Sunny  // 添加 Menu, Moon, Sunny
} from '@element-plus/icons-vue';
import { useNotificationSocketStore } from '@/stores/notificationSocket';
const drawerVisible = ref(false);
const { isMobile } = useResponsive();

const isCollapsed = ref(false);

const authStore = useAuthStore();
const router = useRouter();
const isProfileDialogVisible = ref(false);
const socketStore = useNotificationSocketStore();
const { isDark, toggleDarkMode } = useDarkMode();

// 移动端菜单点击处理
const handleMobileMenuSelect = (path: string) => {
  router.push(path);
  drawerVisible.value = false;
};

const allMenus = [
  { path: '/home', name: '工作台', permission: 'sys:home', icon: House },
  { path: '/subjects', name: '科目管理', permission: 'sys:subject:list', icon: Collection },
  { path: '/knowledge-points', name: '知识点管理', permission: 'sys:kp:list', icon: Reading },
  { path: '/knowledge-graph', name: '知识图谱', permission: 'sys:kp:list', icon: Share },
  { path: '/questions', name: '题库管理', permission: 'sys:question:list', icon: Tickets },
  { path: '/papers', name: '试卷管理', permission: 'sys:paper:list', icon: DocumentCopy },
  { path: '/courses', name: '课程管理', permission: 'sys:course:edit', icon: VideoPlay },
  { path: '/students', name: '学生管理', permission: 'sys:student:list', icon: Avatar },
  { path: '/classes', name: '班级管理', permission: 'sys:user:list', icon: DataBoard },
  { path: '/score-manage', name: '成绩管理', permission: 'sys:stats:list', icon: Medal }, // 【修改】新增成绩管理菜单
  {
    path: '/error-management', name: '错题管理', permission: 'sys:wrong:list', icon: CircleClose,
    children: [
      { path: '/wrong-records', name: '错题管理',permission: 'sys:wrong:list' },
      { path: '/wrong-record-stats', name: '错题统计',permission: 'sys:wrong:list' },
    ]
  },
  { path: '/statistics', name: '教学统计分析', permission: 'sys:stats:list', icon: DataLine },
  { path: '/ai-monitor', name: 'AI监控', permission: 'sys:stats:list', icon: DataBoard },
  { path: '/notifications', name: '通知管理', permission: 'sys:notify:list', icon: Bell },
  {
    path: '/system', name: '系统管理', permission: 'sys:user:list', icon: Setting,
    children: [
      { path: '/users', name: '成员管理',  permission: 'sys:user:list',icon: User },
      { path: '/roles', name: '角色管理', permission: 'sys:user:list', icon: LockIcon },
      { path: '/logs/login', name: '登录日志', permission: 'sys:log:login', icon: Document },
      { path: '/logs/operation', name: '操作日志', permission: 'sys:log:oper', icon: DataLine },
    ]
  },
];

const visibleMenus = computed(() => {
  return allMenus.filter(menu => {
    if (menu.children && menu.children.length > 0) {
      return menu.children.some(child => authStore.hasPermission(child.permission || ''));
    }
    return authStore.hasPermission(menu.permission);
  });
});


const handleCommand = (command: string) => {
  if (command === 'logout') {
    authStore.logout();
  } else if (command === 'profile') {
    router.push('/profile');
  }
};
// 4. 【核心逻辑】组件挂载时建立连接
onMounted(() => {
  // 管理员登录后，Token 存在于 authStore 中，
  // socketStore.connect() 会自动读取 Token 并建立连接
  socketStore.connect();
});

// 5. 【核心逻辑】组件卸载时断开连接
onUnmounted(() => {
  socketStore.disconnect();
});
</script>

<style scoped>
/* === 移动端样式 === */
.mobile-header {
  padding: 0 12px;
}

.mobile-menu-btn {
  background: transparent;
  border: none;
  color: var(--text-color-primary);
}

.mobile-menu-btn:hover,
.mobile-menu-btn:focus {
  background: rgba(64, 158, 255, 0.1);
  color: var(--brand-color);
}

.mobile-content {
  padding: 12px;
}

.mobile-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drawer-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.mobile-menu {
  border-right: none;
}

.mobile-menu .el-menu-item {
  min-height: 48px;
  line-height: 48px;
}

.mobile-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  color: var(--brand-color);
}

.drawer-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color);
  background-color: #f5f7fa;
}

.user-info-mobile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info-mobile .user-details {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.user-info-mobile .nickname {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-color-primary);
}

.user-info-mobile .email {
  font-size: 13px;
  color: var(--text-color-regular);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-layout { height: 100vh; background-color: var(--bg-color); }
.aside { background-color: var(--sidebar-bg); border-right: 1px solid var(--border-color); display: flex; flex-direction: column; transition: width 0.3s ease; }
.logo-area { display: flex; align-items: center; padding: 20px; gap: 12px; transition: padding 0.3s ease; }
.logo-area h1 { font-size: 20px; font-weight: 600; color: var(--text-color-primary); }
.main-menu { flex-grow: 1; border-right: none; }
.main-menu .el-menu-item.is-active { background-color: #ecf5ff; color: var(--brand-color); }
.sidebar-footer { padding: 16px; border-top: 1px solid var(--border-color); }
.user-profile-small { display: flex; align-items: center; cursor: pointer; }
.user-details { margin-left: 12px; display: flex; flex-direction: column; flex-grow: 1; min-width: 0; }
.user-details .nickname { font-weight: 600; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-details .email { font-size: 12px; color: var(--text-color-regular); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-profile-small .arrow { margin-left: auto; flex-shrink: 0; }
.header { background-color: var(--header-bg); display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid var(--border-color); }
.header-right { display: flex; align-items: center; gap: 16px; }
.main-content { padding: 24px; background-color: var(--bg-color); }

.el-menu-item .el-icon {
  margin-right: 5px;
}

/* 侧边栏折叠样式 */
.el-aside.is-collapse .logo-area {
  padding: 20px 0;
  justify-content: center;
}
.el-aside:not(.is-collapse) .logo-area {
  padding: 20px;
}

.collapse-btn {
  cursor: pointer;
  color: var(--text-color-regular);
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
</style>