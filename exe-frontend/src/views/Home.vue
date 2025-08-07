<template>
  <div class="home-container">
    <el-card class="profile-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      <div v-if="authStore.user" class="user-profile">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ authStore.user.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ authStore.user.nickName }}</el-descriptions-item>
          <el-descriptions-item label="账户状态">
            <el-tag :type="authStore.user.isEnabled === 1 ? 'success' : 'info'">
              {{ authStore.user.isEnabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ authStore.user.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="card-footer">
          <el-button type="primary" :icon="Edit" @click="isDialogVisible = true">修改信息</el-button>
        </div>
      </div>
      <el-skeleton v-else :rows="4" animated />
    </el-card>

    <user-edit-dialog
        v-if="authStore.user"
        v-model:visible="isDialogVisible"
        :user-id="authStore.user.id"
        @success="handleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { Edit } from '@element-plus/icons-vue';
import UserEditDialog from '@/components/user/UserEditDialog.vue'; // 导入新组件

const authStore = useAuthStore();
const isDialogVisible = ref(false);

// 修改成功后，重新获取当前用户信息以同步昵称等
const handleSuccess = () => {
  authStore.fetchUserInfo();
};
</script>

<style scoped>
.home-container {
  padding: 20px;
}
.profile-card {
  max-width: 600px;
  margin: 0 auto;
}
.card-header {
  font-weight: bold;
}
.card-footer {
  margin-top: 20px;
  text-align: right;
}
</style>