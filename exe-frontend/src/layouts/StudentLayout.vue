<template>
  <el-container class="student-layout" :style="layoutStyle">
    <el-header class="header">
      <div class="header-logo">
        <el-icon><School /></el-icon>
        <span>在线学习系统</span>
      </div>
      <div class="header-menu">
        <el-menu mode="horizontal" :default-active="$route.path" router :ellipsis="false">
          <el-menu-item index="/student/dashboard">我的首页</el-menu-item>
          <el-menu-item index="/student/wrong-records">我的错题本</el-menu-item>
          <el-menu-item index="/student/practice">在线练习</el-menu-item>
          <el-menu-item index="/student/exams">模拟考试</el-menu-item>
          <el-menu-item index="/student/favorites">我的收藏</el-menu-item>
        </el-menu>
      </div>
      <div class="header-user">
        <el-dropdown @command="handleCommand">
          <span class="avatar-dropdown">
            <el-avatar :size="32" :src="studentAuth.student?.avatar || ''">{{ studentAuth.studentName.charAt(0) }}</el-avatar>
            <span class="nickname-header">{{ studentAuth.studentName }}</span>
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
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
    <el-main class="main-content">
      <router-view />
    </el-main>

    <profile-edit-dialog
        v-model:visible="isProfileDialogVisible"
        @success="() => {}"
    />
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'; // 【修改】导入 ref
import { computed } from 'vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { School, ArrowDown } from '@element-plus/icons-vue';
// 【新增】导入我们新创建的弹窗组件
import ProfileEditDialog from '@/components/student/ProfileEditDialog.vue';

const studentAuth = useStudentAuthStore();
// 【新增】控制弹窗显示的变量
const isProfileDialogVisible = ref(false);

const handleCommand = (command: string) => {
  if (command === 'logout') {
    studentAuth.logout();
  } else if (command === 'profile') {
    // 【修改】点击后，将变量设为 true 来显示弹窗
    isProfileDialogVisible.value = true;
  }
};
// 【核心】计算背景样式
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
  return {}; // 没有设置则使用默认 CSS 中的背景
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
</style>