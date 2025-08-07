<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="logo-title">
        <h2>试题管理系统</h2>
      </div>
      <div class="header-right">
        <div class="user-info">
          <span>欢迎您，{{ authStore.userNickname }}</span>
        </div>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside width="200px" class="aside">

        <div class="debug-panel">
          <p><strong>-- 调试信息 --</strong></p>
          <p><strong>isAdmin:</strong> {{ authStore.isAdmin }}</p>
          <p><strong>User Object:</strong></p>
          <pre>{{ JSON.stringify(authStore.user, null, 2) }}</pre>
        </div>
        <el-menu :default-active="$route.path" router>
          <el-menu-item index="/home">
            <el-icon><House /></el-icon>
            <span>主页</span>
          </el-menu-item>

          <el-menu-item v-if="authStore.isAdmin" index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth';
import { House, User } from '@element-plus/icons-vue';

const authStore = useAuthStore();

const handleLogout = () => {
  authStore.logout();
};
</script>

<style scoped>
/* ... 其他样式保持不变 ... */
.main-layout {
  height: 100%;
}
.header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
.user-info {
  font-size: 14px;
}
.logo-title {
  font-weight: bold;
}
.aside {
  background-color: #ffffff;
  border-right: 1px solid #e6e6e6;
}
.el-menu {
  border-right: none;
}
.main-content {
  padding: 20px;
  background-color: #f5f7fa;
}

/* 调试窗口的样式 */
.debug-panel {
  padding: 10px;
  background-color: #282c34;
  color: #61dafb;
  font-size: 12px;
  border-bottom: 1px solid #444;
}
.debug-panel pre {
  white-space: pre-wrap;
  word-break: break-all;
  margin-top: 5px;
}
</style>