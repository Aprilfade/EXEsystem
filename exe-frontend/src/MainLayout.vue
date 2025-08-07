<template>
  <el-container style="height: 100vh;">
    <el-header style="background-color: #409eff; color: white; display: flex; align-items: center;">
      <h2>试题管理系统</h2>
      <el-button type="danger" style="margin-left: auto;" @click="handleLogout">退出登录</el-button>
    </el-header>

    <el-container>
      <el-aside width="200px" style="background-color: #f5f5f5;">
        <el-menu router>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { User } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>
