<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑章节' : '新建章节'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="章节名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入章节名称"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="章节描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入章节描述（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="排序" prop="sortOrder">
        <el-input-number
          v-model="form.sortOrder"
          :min="0"
          :max="999"
          placeholder="排序值越小越靠前"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { createChapter, updateChapter } from '@/api/courseAdmin';
import type { ChapterNode } from '@/api/courseAdmin';

interface Props {
  visible: boolean;
  chapter: ChapterNode | null;
  parentId: number;
  courseId: number;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  'success': [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = ref({
  name: '',
  description: '',
  sortOrder: 0
});

const rules: FormRules = {
  name: [
    { required: true, message: '请输入章节名称', trigger: 'blur' },
    { min: 1, max: 200, message: '长度在 1 到 200 个字符', trigger: 'blur' }
  ]
};

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

const isEdit = computed(() => !!props.chapter);

// 监听章节数据变化
watch(() => props.chapter, (newVal) => {
  if (newVal) {
    form.value = {
      name: newVal.name,
      description: newVal.description || '',
      sortOrder: newVal.sortOrder || 0
    };
  } else {
    form.value = {
      name: '',
      description: '',
      sortOrder: 0
    };
  }
}, { immediate: true });

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    loading.value = true;

    const data = {
      ...form.value,
      courseId: props.courseId,
      parentId: props.parentId
    };

    if (isEdit.value && props.chapter) {
      // 更新
      const res = await updateChapter(props.chapter.id, data);
      if (res.code === 200) {
        ElMessage.success('章节更新成功');
        emit('success');
        handleClose();
      } else {
        ElMessage.error(res.message || '更新失败');
      }
    } else {
      // 创建
      const res = await createChapter(data);
      if (res.code === 200) {
        ElMessage.success('章节创建成功');
        emit('success');
        handleClose();
      } else {
        ElMessage.error(res.message || '创建失败');
      }
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + error.message);
    }
  } finally {
    loading.value = false;
  }
};

// 关闭弹窗
const handleClose = () => {
  formRef.value?.resetFields();
  emit('update:visible', false);
};
</script>
