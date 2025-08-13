<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="600px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <div v-if="!form" style="padding: 20px; color: red; font-weight: bold;">
      诊断信息：表单数据(form)为空，组件未能正确加载数据。
    </div>

    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
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
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入科目简介" />
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

// 诊断日志 1: 确认脚本已开始执行
console.log('[诊断日志] SubjectEditDialog.vue 组件脚本已加载');

const props = defineProps<{
  visible: boolean;
  subjectData?: Subject;
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
const form = ref<Partial<Subject> | null>(null);

const dialogTitle = computed(() => (props.subjectData?.id ? '编辑科目' : '新增科目'));

watch(() => props.visible, (isVisible) => {
  // 诊断日志 2: 观察 props.visible 的变化
  console.log(`[诊断日志] 弹窗 visible 状态变为: ${isVisible}`);

  if (isVisible) {
    // 诊断日志 3: 查看父组件传递过来的数据
    console.log('[诊断日志] 接收到的 subjectData:', JSON.stringify(props.subjectData || null));

    if (props.subjectData) {
      form.value = { ...props.subjectData };
      // 诊断日志 4: 查看表单数据是否被赋值
      console.log('[诊断日志] 编辑模式，form 被赋值为:', JSON.stringify(form.value));
    } else {
      form.value = { name: '', description: '', grade: '' };
      console.log('[诊断日志] 新增模式，form 已初始化');
    }
  } else {
    form.value = null;
  }
}, { immediate: true }); // <-- 请在这里加上这一行！

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