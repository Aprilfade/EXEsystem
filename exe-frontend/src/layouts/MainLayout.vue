<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="aside">
      <div class="logo-area">
        <el-icon :size="32" color="#409eff"><Management /></el-icon>
        <h1>试题管理系统</h1>
      </div>
      <el-menu :default-active="$route.path" router class="main-menu">
        <el-menu-item index="/home">
          <el-icon><House /></el-icon><span>工作台</span>
        </el-menu-item>

        <el-menu-item index="/subjects">
          <el-icon><Collection /></el-icon><span>科目管理</span>
        </el-menu-item>
        <el-menu-item index="/knowledge-points">
          <el-icon><Reading /></el-icon><span>知识点管理</span>
        </el-menu-item>
        <el-menu-item index="/questions">
          <el-icon><Tickets /></el-icon><span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/papers">
          <el-icon><DocumentCopy /></el-icon><span>试卷管理</span>
        </el-menu-item>

        <el-menu-item index="/students">
          <el-icon><Avatar /></el-icon><span>学生管理</span>
        </el-menu-item>

        <el-sub-menu index="error-management">
          <template #title><el-icon><CircleClose /></el-icon><span>错题管理</span></template>
          <el-menu-item index="/wrong-records">错题管理</el-menu-item>
          <el-menu-item index="/wrong-record-stats">错题统计</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/statistics">
          <el-icon><DataLine /></el-icon><span>教学统计分析</span>
        </el-menu-item>

        <el-menu-item index="/notifications">
          <el-icon><Bell /></el-icon><span>通知管理</span>
        </el-menu-item>

        <el-menu-item index="/users" v-if="authStore.isAdmin">
          <el-icon><User /></el-icon><span>成员管理</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="user-profile-small">
          <el-avatar size="small">{{ authStore.userNickname.charAt(0) }}</el-avatar>
          <div class="user-details">
            <span class="nickname">{{ authStore.userNickname }}</span>
            <span class="email">{{ authStore.user?.username }}@probysui.com</span>
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
          <el-button circle :icon="Search"></el-button>
          <el-button circle :icon="Bell"></el-button>
          <el-dropdown @command="handleCommand">
            <el-avatar>{{ authStore.userNickname.charAt(0) }}</el-avatar>
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
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
// 【修改点】: 移除了不再使用的 Setting 图标
import {
  Management, House, Collection, Reading, Tickets, DocumentCopy, Avatar, CircleClose, DataLine,
  User, Search, Bell, ArrowRight
} from '@element-plus/icons-vue';

const authStore = useAuthStore();
const router = useRouter();

const handleCommand = (command: string) => {
  if (command === 'logout') {
    authStore.logout();
  } else if (command === 'profile') {
    router.push('/home');
  }
};
</script>

<style scoped>
/* 样式部分无需改动 */
.main-layout { height: 100vh; background-color: var(--bg-color); }
.aside { background-color: var(--sidebar-bg); border-right: 1px solid var(--border-color); display: flex; flex-direction: column; }
.logo-area { display: flex; align-items: center; padding: 20px; gap: 12px; }
.logo-area h1 { font-size: 20px; font-weight: 600; color: var(--text-color-primary); }
.main-menu { flex-grow: 1; border-right: none; }
.main-menu .el-menu-item.is-active { background-color: #ecf5ff; color: var(--brand-color); }
.sidebar-footer { padding: 16px; border-top: 1px solid var(--border-color); }
.user-profile-small { display: flex; align-items: center; cursor: pointer; }
.user-details { margin-left: 12px; display: flex; flex-direction: column; }
.user-details .nickname { font-weight: 600; font-size: 14px; }
.user-details .email { font-size: 12px; color: var(--text-color-regular); }
.user-profile-small .arrow { margin-left: auto; }
.header { background-color: var(--header-bg); display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid var(--border-color); }
.header-right { display: flex; align-items: center; gap: 16px; }
.main-content { padding: 24px; background-color: var(--bg-color); }
</style>