<template>
  <el-dialog
      :model-value="visible"
      title="个人中心"
      width="600px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="头像" prop="avatar">
        <el-upload
            class="avatar-uploader"
            action="/api/v1/files/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="{ 'Authorization': 'Bearer ' + authStore.token }"
        >
          <img v-if="form.avatar" :src="form.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="用户名">
        <el-input :value="form.username" disabled />
      </el-form-item>
      <el-form-item label="昵称" prop="nickName">
        <el-input v-model="form.nickName" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="新密码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="留空则不修改密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submitForm">保 存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import type { FormInstance, FormRules, UploadProps } from 'element-plus';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';
import { updateMyProfile } from '@/api/user';
import type { UserInfo } from '@/api/user';

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits(['update:visible', 'success']);

const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const form = ref<Partial<UserInfo>>({});

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    // 弹窗打开时，从 authStore 加载当前用户信息
    form.value = { ...authStore.user, password: '' };
  }
});

const rules = reactive<FormRules>({
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
});

const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    form.value.avatar = response.data;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.msg || '头像上传失败');
  }
};

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isJpgOrPng = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png';
  if (!isJpgOrPng) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!');
    return false;
  }
  const isLt2M = rawFile.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

const handleClose = () => emit('update:visible', false);

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = { ...form.value };
        // 如果密码为空，则不提交该字段
        if (!formData.password) {
          delete formData.password;
        }
        await updateMyProfile(formData);
        ElMessage.success('个人信息更新成功');
        await authStore.fetchUserInfo(); // 关键：更新Store中的信息
        emit('success');
        handleClose();
      } catch (error) {
        console.error("操作失败:", error);
      }
    }
  });
};
</script>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
.avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>