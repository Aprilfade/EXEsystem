<template>
  <el-dialog
      :model-value="visible"
      :title="`批量修改 ${questionIds.length} 道题目`"
      width="500px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" label-width="100px">
      <el-alert
          title="注意：只会更新您填写了的字段，留空的字段将保持不变。"
          type="info"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
      />
      <el-form-item label="新科目">
        <el-select v-model="form.subjectId" placeholder="选择一个新的科目" clearable style="width: 100%;">
          <el-option v-for="sub in allSubjects" :key="sub.id" :label="`${sub.name} (${sub.grade})`" :value="sub.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="新年级">
        <el-select v-model="form.grade" placeholder="选择一个新的年级" clearable style="width: 100%;">
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
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="loading">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { batchUpdateQuestions } from '@/api/question';
import { fetchAllSubjects, type Subject } from '@/api/subject';

const props = defineProps<{
  visible: boolean;
  questionIds: number[];
}>();

const emit = defineEmits(['update:visible', 'success']);

const loading = ref(false);
const form = reactive({
  subjectId: undefined as number | undefined,
  grade: undefined as string | undefined,
});
const allSubjects = ref<Subject[]>([]);

const loadSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) {
    allSubjects.value = res.data;
  }
};

const handleClose = () => {
  emit('update:visible', false);
};

const submitForm = async () => {
  if (!form.subjectId && !form.grade) {
    ElMessage.warning('请至少选择一个新的科目或年级');
    return;
  }

  loading.value = true;
  try {
    await batchUpdateQuestions({
      questionIds: props.questionIds,
      subjectId: form.subjectId,
      grade: form.grade
    });
    ElMessage.success('批量修改成功');
    emit('success');
    handleClose();
  } catch (error) {
    console.error('批量修改失败:', error);
    ElMessage.error('操作失败，请重试');
  } finally {
    loading.value = false;
  }
};

onMounted(loadSubjects);
</script>