<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="500px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="科目名称" prop="name">
        <el-input v-model="form.name" placeholder="例如：数学" />
      </el-form-item>
      <el-form-item label="所属年级" prop="grade">
        <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%;">
          <el-option v-for="g in GRADE_OPTIONS" :key="g" :label="g" :value="g" />
        </el-select>
      </el-form-item>
      <el-form-item label="科目简介" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入科目简介" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { createSubject, updateSubject } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { GRADE_OPTIONS } from '@/utils/constants'; // 引入常量

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
      form.value = { ...props.subjectData };
    } else {
      form.value = { name: '', description: '', grade: '' };
    }
  } else {
    form.value = null;
  }
}, { immediate: true });

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
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
          await updateSubject(form.value.id, form.value);
          ElMessage.success('更新成功');
        } else {
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