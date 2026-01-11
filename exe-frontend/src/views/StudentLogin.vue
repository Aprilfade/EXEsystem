<template>
  <div class="login-page-wrapper">
    <div class="login-container">
      <div class="login-form">
        <h2 class="form-title">在线学习系统</h2>
        <el-form ref="formRef" :model="loginForm" :rules="rules" @submit.prevent="handleLogin">
          <el-form-item prop="studentNo">
            <el-input v-model="loginForm.studentNo" placeholder="请输入学号" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password size="large" :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" size="large" style="width: 100%;">登 录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

const formRef = ref<FormInstance>();
const studentAuthStore = useStudentAuthStore();
const route = useRoute();

const loginForm = reactive({
  studentNo: '',
  password: '',
});

const rules = reactive<FormRules>({
  studentNo: [{ required: true, message: '学号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
});

const handleLogin = async () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        await studentAuthStore.login(loginForm, route.query.redirect as string | undefined);
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败，请检查学号和密码');
      }
    } else {
      ElMessage.error('请检查输入项');
    }
  });
};
</script>

<style scoped>
/* 可以复用或修改管理后台的登录页样式 */
.login-page-wrapper {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url("/back.jpg") center/cover no-repeat fixed;
}
.login-container {
  width: 400px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.25);
}
.form-title {
  font-size: 2rem;
  color: white;
  text-align: center;
  margin-bottom: 30px;
  text-shadow: 1px 1px 4px rgba(0,0,0,0.3);
}
</style>