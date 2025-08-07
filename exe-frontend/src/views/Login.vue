<template>
  <div class="login-container">
    <el-form ref="loginFormRef" :model="form" :rules="rules" class="login-form">
      <h3 class="title">试题管理系统</h3>
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input type="password" v-model="form.password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" style="width:100%;" @click.prevent="handleLogin">登 录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useAuthStore } from '@/stores/auth';
import type { FormInstance, FormRules } from 'element-plus';

const authStore = useAuthStore();
const loginFormRef = ref<FormInstance>();

const form = reactive({
  username: 'admin', // 预填，方便测试
  password: 'password', // 预填，方便测试
});

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
});

const handleLogin = () => {
  loginFormRef.value?.validate((valid) => {
    if (valid) {
      authStore.login(form);
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-form {
  width: 400px;
  padding: 35px;
  background: #fff;
  border-radius: 6px;
}
.title {
  margin-bottom: 20px;
  text-align: center;
  color: #333;
}
</style>