<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="aside">
      <div class="logo-area">
        <el-icon :size="32" color="#409eff"><Management /></el-icon>
        <h1>试题管理系统</h1>
      </div>
      <el-menu :default-active="$route.path" router class="main-menu">
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

      <div class="sidebar-footer">
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

    <el-container>
      <el-header class="header">
        <div class="header-left">
        </div>
        <div class="header-right">
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
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import ProfileEditDialog from '@/components/user/ProfileEditDialog.vue';
// 【核心修复】在这里导入 Lock 时使用别名 LockIcon
import {
  Management, House, Collection, Reading, Tickets, DocumentCopy, Avatar, CircleClose, DataLine,
  User, Search, Bell, ArrowRight, ArrowDown, Lock as LockIcon, Setting
} from '@element-plus/icons-vue';

const authStore = useAuthStore();
const router = useRouter();
const isProfileDialogVisible = ref(false);

const allMenus = [
  { path: '/home', name: '工作台', permission: 'sys:home', icon: House },
  { path: '/subjects', name: '科目管理', permission: 'sys:subject:list', icon: Collection },
  { path: '/knowledge-points', name: '知识点管理', permission: 'sys:kp:list', icon: Reading },
  { path: '/questions', name: '题库管理', permission: 'sys:question:list', icon: Tickets },
  { path: '/papers', name: '试卷管理', permission: 'sys:paper:list', icon: DocumentCopy },
  { path: '/students', name: '学生管理', permission: 'sys:student:list', icon: Avatar },
  {
    path: '/error-management', name: '错题管理', permission: 'sys:wrong:list', icon: CircleClose,
    children: [
      { path: '/wrong-records', name: '错题管理',permission: 'sys:wrong:list' },
      { path: '/wrong-record-stats', name: '错题统计',permission: 'sys:wrong:list' },
    ]
  },
  { path: '/statistics', name: '教学统计分析', permission: 'sys:stats:list', icon: DataLine },
  { path: '/notifications', name: '通知管理', permission: 'sys:notify:list', icon: Bell },
  {
    path: '/system', name: '系统管理', permission: 'sys:user:list', icon: Setting,
    children: [
      { path: '/users', name: '成员管理',  permission: 'sys:user:list',icon: User },
      // 【核心修复】在这里使用别名 LockIcon
      { path: '/roles', name: '角色管理', permission: 'sys:user:list', icon: LockIcon },
    ]
  },
];

const visibleMenus = computed(() => {
  return allMenus.filter(menu => {
    // 如果菜单有子菜单，只要有一个子菜单有权限，父菜单就应该显示
    if (menu.children && menu.children.length > 0) {
      return menu.children.some(child => authStore.hasPermission(child.permission || ''));
    }
    // 否则，按自身权限判断
    return authStore.hasPermission(menu.permission);
  });
});


const handleCommand = (command: string) => {
  if (command === 'logout') {
    authStore.logout();
  } else if (command === 'profile') {
    isProfileDialogVisible.value = true;
  }
};
</script>

<style scoped>
/* ... style 部分不变 ... */
.main-layout { height: 100vh; background-color: var(--bg-color); }
.aside { background-color: var(--sidebar-bg); border-right: 1px solid var(--border-color); display: flex; flex-direction: column; }
.logo-area { display: flex; align-items: center; padding: 20px; gap: 12px; }
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
</style>