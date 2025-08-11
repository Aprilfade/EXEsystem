<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="600px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="通知标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入通知标题" />
      </el-form-item>
      <el-form-item label="通知内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入通知内容" />
      </el-form-item>
      <el-form-item label="立即发布" prop="isPublished">
        <el-switch v-model="form.isPublished" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { createNotification, updateNotification, fetchNotificationById } from '@/api/notification';
import type { Notification } from '@/api/notification';

const props = defineProps<{
  visible: boolean;
  notificationId?: number;
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
const form = ref<Partial<Notification>>({ title: '', content: '', isPublished: false });

const dialogTitle = computed(() => (props.notificationId ? '编辑通知' : '新增通知'));

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    if (props.notificationId) {
      fetchNotificationById(props.notificationId).then(res => {
        form.value = res.data;
      });
    } else {
      form.value = { title: '', content: '', isPublished: false };
    }
  }
});

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
});

const handleClose = () => {
  emit('update:visible', false);
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.value?.id) {
        await updateNotification(form.value.id, form.value);
        ElMessage.success('更新成功');
      } else {
        await createNotification(form.value!);
        ElMessage.success('新增成功');
      }
      emit('success');
      handleClose();
    }
  });
};
</script>