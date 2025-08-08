<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="80%"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="试卷名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入试卷名称" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="试卷编码" prop="code">
            <el-input v-model="form.code" placeholder="请输入试卷编码" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="所属科目" prop="subjectId">
            <el-select v-model="form.subjectId" placeholder="请选择科目" style="width: 100%;">
              <el-option v-for="sub in props.subjects" :key="sub.id" :label="sub.name" :value="sub.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="试卷描述" prop="description">
        <el-input v-model="form.description" type="textarea" placeholder="请输入试卷描述" />
      </el-form-item>
    </el-form>

    <el-divider />

    <paper-question-manager
        v-if="form.subjectId"
        :paper-questions="form.questions"
        :subject-id="form.subjectId"
        @update:questions="updateQuestions"
    />
    <el-alert v-else title="请先选择一个科目来管理试题" type="info" show-icon :closable="false" />


    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>



<script setup lang="ts">
import { ref, reactive, computed, watchEffect } from 'vue'; // 注意：这里导入了 watchEffect
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { createPaper, updatePaper, fetchPaperById } from '@/api/paper';
import type { Paper, PaperQuestion } from '@/api/paper';
import type { Subject } from '@/api/subject';
import PaperQuestionManager from './PaperQuestionManager.vue';

const props = defineProps<{
  visible: boolean;
  paperId?: number;
  subjects: Subject[];
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
// 初始化一个默认的空表单结构
const form = ref<Partial<Paper>>({ name: '', code: '', subjectId: undefined, description: '', questions: [] });

const dialogTitle = computed(() => (props.paperId ? '编辑试卷' : '新增试卷'));

// 【核心修改】使用 watchEffect 替代 watch
watchEffect(async () => {
  // 这个函数会在 props.visible 或 props.paperId 变化时自动重新运行
  if (props.visible) {
    if (props.paperId) {
      // 编辑模式：根据 paperId 获取现有试卷数据
      try {
        const res = await fetchPaperById(props.paperId);
        form.value = res.data;
      } catch (e) {
        console.error(e);
        ElMessage.error('加载试卷信息失败!');
        // 加载失败时重置，避免显示脏数据
        form.value = { name: '', code: '', subjectId: undefined, description: '', questions: [] };
      }
    } else {
      // 新增模式：重置为一个干净的空表单
      form.value = { name: '', code: '', subjectId: undefined, description: '', questions: [] };
    }
  }
});

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择所属科目', trigger: 'change' }],
});

const updateQuestions = (newQuestions: PaperQuestion[]) => {
  form.value.questions = newQuestions;
}

const handleClose = () => emit('update:visible', false);

const submitForm = async () => {
  if (!formRef.value || !form.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        form.value.totalScore = form.value.questions?.reduce((sum, q) => sum + (q.score || 0), 0);

        if (form.value.id) {
          await updatePaper(form.value.id, form.value);
          ElMessage.success('更新成功');
        } else {
          await createPaper(form.value);
          ElMessage.success('新增成功');
        }
        emit('success');
        handleClose();
      } catch (error) {
        console.error('操作失败:', error);
        // 可以在这里给用户更友好的错误提示
        ElMessage.error("操作失败，请重试");
      }
    }
  });
};
</script>