<template>
  <el-dialog
      :model-value="visible"
      title="个人资料"
      width="600px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="头像" prop="avatar">
        <el-upload
            class="avatar-uploader"
            action="/api/v1/student/files/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="{ 'Authorization': 'Bearer ' + studentAuth.token }"
        >
          <div class="avatar-edit-wrapper">
            <UserAvatar
                :src="form.avatar"
                :name="form.name"
                :size="100"
                :frame-style="studentAuth.student?.avatarFrameStyle"
            />
            <div class="hover-mask">
              <el-icon><Plus /></el-icon>
            </div>
          </div>
        </el-upload>
      </el-form-item>
      <el-form-item label="学号">
        <el-input :value="form.studentNo" disabled />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="联系方式" prop="contact">
        <el-input v-model="form.contact" placeholder="请输入联系方式" />
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
import { useStudentAuthStore } from '@/stores/studentAuth';
import { updateStudentProfile } from '@/api/studentAuth';
import type { Student } from '@/api/student';
import UserAvatar from '@/components/UserAvatar.vue'; // 引入新组件

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits(['update:visible', 'success']);

const studentAuth = useStudentAuthStore();
const formRef = ref<FormInstance>();
const form = ref<Partial<Student>>({});

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    // 弹窗打开时，从 authStore 加载当前学生信息
    form.value = { ...studentAuth.student, password: '' };
  }
});

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
});

const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200 && form.value) {
    form.value.avatar = response.data;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.msg || '头像上传失败');
  }
};

const handleClose = () => emit('update:visible', false);

const submitForm = async () => {
  await formRef.value?.validate(async (valid) => {
    if (valid) {
      const formData = { ...form.value };
      if (!formData.password) {
        delete formData.password;
      }
      await updateStudentProfile(formData);
      ElMessage.success('个人信息更新成功');
      await studentAuth.fetchStudentInfo(); // 关键：更新Store中的信息
      emit('success');
      handleClose();
    }
  });
};
</script>

<style>
/* 复用管理后台的头像上传样式 */
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
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
.avatar-wrapper {
  position: relative;
  display: inline-block;
}
/* 如果你的资源值是 CSS 边框代码 */
.avatar-frame {
  position: absolute;
  top: -5px; left: -5px; right: -5px; bottom: -5px;
  pointer-events: none; /* 让点击事件穿透到上传按钮 */
  z-index: 10;
  border-radius: 50%;
}
/* 添加一些样式让上传交互更好看 */
.avatar-edit-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
}
.hover-mask {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 20;
}
.avatar-edit-wrapper:hover .hover-mask {
  opacity: 1;
}
</style>