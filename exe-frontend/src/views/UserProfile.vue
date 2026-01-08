<template>
  <div class="profile-container">
    <!-- 个人信息头部卡片 -->
    <el-card class="profile-header-card" shadow="never">
      <div class="profile-header">
        <div class="profile-header-bg"></div>
        <div class="profile-header-content">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader-large"
              action="/api/v1/files/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="{ 'Authorization': 'Bearer ' + authStore.token }"
            >
              <div class="avatar-wrapper">
                <el-avatar :size="120" :src="userInfo.avatar" class="user-avatar">
                  <el-icon :size="50"><UserFilled /></el-icon>
                </el-avatar>
                <div class="avatar-hover-mask">
                  <el-icon :size="24"><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
            </el-upload>
          </div>

          <!-- 用户信息 -->
          <div class="user-info-section">
            <h2 class="user-name">{{ userInfo.nickName || userInfo.username }}</h2>
            <p class="user-role">
              <el-tag type="success" effect="dark">管理员</el-tag>
            </p>
            <p class="user-meta">
              <el-icon><User /></el-icon>
              <span>{{ userInfo.username }}</span>
              <el-divider direction="vertical" />
              <el-icon><Clock /></el-icon>
              <span>加入于 {{ formatDate(userInfo.createTime) }}</span>
            </p>
          </div>

          <!-- 快捷操作 -->
          <div class="quick-actions">
            <el-button :icon="Edit" @click="activeTab = 'basic'">编辑资料</el-button>
            <el-button :icon="Lock" @click="activeTab = 'security'">安全设置</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab 切换内容 -->
    <el-card class="profile-content-card" shadow="never">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form
            ref="basicFormRef"
            :model="basicForm"
            :rules="basicRules"
            label-width="100px"
            class="profile-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名">
                  <el-input v-model="basicForm.username" disabled>
                    <template #prefix><el-icon><User /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="昵称" prop="nickName">
                  <el-input v-model="basicForm.nickName" placeholder="请输入昵称">
                    <template #prefix><el-icon><EditPen /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="basicForm.email" placeholder="请输入邮箱">
                    <template #prefix><el-icon><Message /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="basicForm.phone" placeholder="请输入手机号">
                    <template #prefix><el-icon><Phone /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="个人简介">
              <el-input
                v-model="basicForm.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下自己吧..."
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitBasicForm" :loading="saving">
                保存修改
              </el-button>
              <el-button @click="resetBasicForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <el-form
            ref="securityFormRef"
            :model="securityForm"
            :rules="securityRules"
            label-width="120px"
            class="profile-form"
          >
            <el-alert
              title="密码安全提示"
              type="info"
              description="为了您的账号安全，建议定期更换密码，密码长度至少6位，包含字母和数字"
              :closable="false"
              show-icon
              class="security-alert"
            />

            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="securityForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入当前密码"
              >
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="securityForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              >
                <template #prefix><el-icon><Key /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="securityForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              >
                <template #prefix><el-icon><Key /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitSecurityForm" :loading="saving">
                修改密码
              </el-button>
              <el-button @click="resetSecurityForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 偏好设置 -->
        <el-tab-pane label="偏好设置" name="preferences">
          <div class="preferences-section">
            <div class="preference-item">
              <div class="preference-label">
                <el-icon><Moon /></el-icon>
                <span>深色模式</span>
              </div>
              <el-switch v-model="isDark" @change="toggleDarkMode" />
            </div>

            <el-divider />

            <div class="preference-item">
              <div class="preference-label">
                <el-icon><Bell /></el-icon>
                <span>消息通知</span>
              </div>
              <el-switch v-model="preferences.notifications" />
            </div>

            <el-divider />

            <div class="preference-item">
              <div class="preference-label">
                <el-icon><Message /></el-icon>
                <span>邮件通知</span>
              </div>
              <el-switch v-model="preferences.emailNotifications" />
            </div>

            <el-divider />

            <div class="preference-item">
              <div class="preference-label">
                <el-icon><View /></el-icon>
                <span>显示操作提示</span>
              </div>
              <el-switch v-model="preferences.showTooltips" />
            </div>

            <div class="preference-actions">
              <el-button type="primary" @click="savePreferences">保存偏好</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules, UploadProps } from 'element-plus';
import {
  UserFilled, Camera, Edit, Lock, User, Clock, EditPen, Message, Phone,
  Key, Moon, Bell, View, DocumentCopy, Tickets, Avatar as AvatarIcon, DataLine
} from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';
import { useDarkMode } from '@/composables/useDarkMode';
import { updateMyProfile } from '@/api/user';
import type { UserInfo } from '@/api/user';

const authStore = useAuthStore();
const { isDark, toggleDarkMode } = useDarkMode();

// 当前激活的 Tab
const activeTab = ref('basic');
const saving = ref(false);

// 用户信息
const userInfo = computed(() => authStore.user || {});

// 基本信息表单
const basicFormRef = ref<FormInstance>();
const basicForm = reactive<Partial<UserInfo>>({
  username: '',
  nickName: '',
  email: '',
  phone: '',
  bio: '',
  avatar: ''
});

// 安全设置表单
const securityFormRef = ref<FormInstance>();
const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 偏好设置
const preferences = reactive({
  notifications: true,
  emailNotifications: false,
  showTooltips: true
});

// 统计数据
const stats = ref([
  { label: '创建试卷', value: 0, color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', icon: DocumentCopy },
  { label: '创建题目', value: 0, color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', icon: Tickets },
  { label: '管理学生', value: 0, color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', icon: AvatarIcon },
  { label: '数据分析', value: 0, color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', icon: DataLine }
]);

// 表单验证规则
const basicRules = reactive<FormRules>({
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
});

const securityRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== securityForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

// 初始化数据
const initData = () => {
  Object.assign(basicForm, {
    username: userInfo.value.username,
    nickName: userInfo.value.nickName || userInfo.value.name,
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || '',
    bio: userInfo.value.bio || '',
    avatar: userInfo.value.avatar || ''
  });

  // TODO: 从后端加载统计数据
  loadStats();
};

// 加载统计数据
const loadStats = async () => {
  // TODO: 调用后端接口获取统计数据
  stats.value[0].value = 25; // 示例数据
  stats.value[1].value = 156;
  stats.value[2].value = 328;
  stats.value[3].value = 42;
};

// 头像上传
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    basicForm.avatar = response.data;
    userInfo.value.avatar = response.data;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.msg || '头像上传失败');
  }
};

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  const isLt5M = rawFile.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }
  return true;
};

// 提交基本信息
const submitBasicForm = async () => {
  if (!basicFormRef.value) return;
  await basicFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        saving.value = true;
        await updateMyProfile(basicForm);
        await authStore.fetchUserInfo();
        ElMessage.success('个人信息更新成功');
      } catch (error) {
        console.error('更新失败:', error);
      } finally {
        saving.value = false;
      }
    }
  });
};

// 重置基本信息表单
const resetBasicForm = () => {
  initData();
};

// 提交安全设置
const submitSecurityForm = async () => {
  if (!securityFormRef.value) return;
  await securityFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        saving.value = true;
        // TODO: 调用修改密码接口
        await updateMyProfile({
          oldPassword: securityForm.oldPassword,
          password: securityForm.newPassword
        });
        ElMessage.success('密码修改成功');
        resetSecurityForm();
      } catch (error) {
        console.error('修改密码失败:', error);
      } finally {
        saving.value = false;
      }
    }
  });
};

// 重置安全设置表单
const resetSecurityForm = () => {
  securityForm.oldPassword = '';
  securityForm.newPassword = '';
  securityForm.confirmPassword = '';
  securityFormRef.value?.clearValidate();
};

// 保存偏好设置
const savePreferences = () => {
  // TODO: 保存偏好设置到后端
  ElMessage.success('偏好设置已保存');
};

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知';
  return new Date(dateStr).toLocaleDateString('zh-CN');
};

onMounted(() => {
  initData();
});
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 头部卡片 */
.profile-header-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.profile-header {
  position: relative;
}

.profile-header-bg {
  height: 180px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.profile-header-bg::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100px;
  background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.3));
}

.profile-header-content {
  position: relative;
  margin-top: -80px;
  padding: 0 40px 30px;
  display: flex;
  align-items: flex-end;
  gap: 30px;
}

/* 头像区域 */
.avatar-section {
  flex-shrink: 0;
}

.avatar-uploader-large {
  border-radius: 50%;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  border: 4px solid var(--bg-secondary, #fff);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.avatar-hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 12px;
}

.avatar-wrapper:hover .avatar-hover-mask {
  opacity: 1;
}

/* 用户信息 */
.user-info-section {
  flex-grow: 1;
  color: var(--text-primary);
}

.user-name {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.user-role {
  margin-bottom: 12px;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.user-meta .el-icon {
  font-size: 16px;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex-grow: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 内容卡片 */
.profile-content-card {
  border-radius: 12px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.profile-form {
  max-width: 800px;
  padding: 20px;
}

.security-alert {
  margin-bottom: 24px;
}

/* 偏好设置 */
.preferences-section {
  padding: 20px;
  max-width: 600px;
}

.preference-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
}

.preference-label {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: var(--text-primary);
}

.preference-label .el-icon {
  font-size: 20px;
  color: var(--text-secondary);
}

.preference-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-container {
    padding: 12px;
  }

  .profile-header-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 0 20px 20px;
    margin-top: -60px;
  }

  .user-info-section {
    width: 100%;
  }

  .quick-actions {
    width: 100%;
    flex-direction: column;
  }

  .quick-actions .el-button {
    width: 100%;
  }

  .profile-form {
    padding: 12px;
  }

  .profile-form :deep(.el-col) {
    width: 100%;
    max-width: 100%;
  }
}
</style>
