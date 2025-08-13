<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="500px"
      @close="handleClose"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="科目名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入科目名称" />
      </el-form-item>

      <el-form-item label="所属年级" prop="grade">
        <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%;">
          <el-option label="一年级" value="一年级" />
          <el-option label="二年级" value="二年级" />
          <el-option label="三年级" value="三年级" />
          <el-option label="四年级" value="四年级" />
          <el-option label="五年级" value="五年级" />
          <el-option label="六年级" value="六年级" />
          <el-option label="七年级" value="七年级" />
          <el-option label="八年级" value="八年级" />
          <el-option label="九年级" value="九年级" />
          <el-option label="高一" value="高一" />
          <el-option label="高二" value="高二" />
          <el-option label="高三" value="高三" />
        </el-select>
      </el-form-item>

      <el-form-item label="科目简介" prop="description">
        <el-input v-model="form.description" type="textarea" placeholder="请输入科目简介" />
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
import { createSubject, updateSubject } from '@/api/subject';
import type { Subject } from '@/api/subject';

const props = defineProps<{
  visible: boolean;
  subjectData?: Subject;
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
const form = ref<Partial<Subject> | null>(null);

const dialogTitle = computed(() => (props.subjectData?.id ? '编辑科目' : '新增科目'));

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    if (props.subjectData) {
      // 编辑
      form.value = { ...props.subjectData };
    } else {
      // 新增，【修改此行】
      form.value = { name: '', description: '', grade: '' };
    }
  } else {
    form.value = null;
    formRef.value?.resetFields();
  }
});

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
});

const handleClose = () => {
  emit('update:visible', false);
};

const submitForm = async () => {
  if (!formRef.value || !form.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value?.id) {
          // 更新
          await updateSubject(form.value.id, form.value);
          ElMessage.success('更新成功');
        } else {
          // 创建
          await createSubject(form.value!);
          ElMessage.success('新增成功');
        }
        emit('success');
        handleClose();
      } catch (error) {
        console.error('操作失败:', error);
      }
    }
  });
};
</script>