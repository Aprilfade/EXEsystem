<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="700px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="所属科目">
        <el-select
            v-model="selectedSubjectId"
            placeholder="请先选择科目以筛选题目和学生"
            style="width: 100%;"
            @change="handleSubjectChange"
            clearable
        >
          <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="选择题目" prop="questionId">
        <el-select
            v-model="form.questionId"
            filterable
            remote
            :remote-method="searchQuestions"
            :loading="questionLoading"
            placeholder="请选择或输入关键词搜索题目"
            style="width: 100%;"
            :disabled="!selectedSubjectId"
            no-data-text="没有找到相关题目"
        >
          <el-option v-for="q in questionOptions" :key="q.id" :label="q.content" :value="q.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="选择学生" prop="studentIds">
        <el-select
            v-model="form.studentIds"
            multiple
            filterable
            placeholder="请选择学生"
            style="width: 100%;"
            :disabled="!selectedSubjectId"
            no-data-text="没有找到相关学生"
        >
          <el-option v-for="s in studentOptions" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="错误原因" prop="wrongReason">
        <el-input v-model="form.wrongReason" type="textarea" :rows="4" placeholder="请输入错误原因分析" />
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
import { createWrongRecord } from '@/api/wrongRecord';
import type { WrongRecordDTO, WrongRecordVO } from '@/api/wrongRecord';
import { fetchAllSubjects, type Subject } from '@/api/subject';
import { fetchQuestionList, type Question } from '@/api/question';
import { fetchStudentList, type Student } from '@/api/student';

const props = defineProps<{
  visible: boolean;
  recordData?: WrongRecordVO;
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
// 【修改点2】: questionId 初始值改为 undefined，避免显示0
const form = ref<WrongRecordDTO>({ questionId: undefined, studentIds: [], wrongReason: '' });
const allSubjects = ref<Subject[]>([]);
const selectedSubjectId = ref<number | null>(null);

const questionOptions = ref<Question[]>([]);
const studentOptions = ref<Student[]>([]);
const questionLoading = ref(false);

const dialogTitle = computed(() => (props.recordData?.id ? '编辑错题记录' : '新增错题记录'));

const loadSubjects = async () => {
  if (allSubjects.value.length === 0) {
    const res = await fetchAllSubjects();
    allSubjects.value = res.data;
  }
};

// 【修改点3】: 核心逻辑修改，选择科目后立即加载题目和学生
const handleSubjectChange = async (subjectId: number | '') => {
  // 重置下游选项
  form.value.questionId = undefined;
  form.value.studentIds = [];
  questionOptions.value = [];
  studentOptions.value = [];

  if (subjectId) {
    // 加载学生列表
    const studentRes = await fetchStudentList({ current: 1, size: 999, subjectId });
    studentOptions.value = studentRes.data;

    // 加载题目列表
    await searchQuestions(''); // 传入空字符串，加载默认题目列表
  }
};

// 【修改点4】: 优化远程搜索，支持空查询
const searchQuestions = async (query: string) => {
  if (selectedSubjectId.value) {
    questionLoading.value = true;
    const res = await fetchQuestionList({ current: 1, size: 50, subjectId: selectedSubjectId.value });
    if (query) {
      questionOptions.value = res.data.filter(q => q.content.toLowerCase().includes(query.toLowerCase()));
    } else {
      questionOptions.value = res.data; // 如果查询为空，则展示所有题目
    }
    questionLoading.value = false;
  } else {
    questionOptions.value = [];
  }
};

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    loadSubjects();
    if (props.recordData) {
      // 编辑模式暂缓
    } else {
      // 新增模式
      form.value = { questionId: undefined, studentIds: [], wrongReason: '' };
      selectedSubjectId.value = null;
      questionOptions.value = [];
      studentOptions.value = [];
    }
  }
});

// 【修改点5】: 移除 subjectId 的验证
const rules = reactive<FormRules>({
  questionId: [{ required: true, message: '请选择题目', trigger: 'change' }],
  studentIds: [{ required: true, type: 'array', message: '请至少选择一个学生', trigger: 'change' }],
  wrongReason: [{ required: true, message: '请输入错误原因', trigger: 'blur' }],
});

const handleClose = () => {
  emit('update:visible', false);
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value?.id) {
          // 更新逻辑
        } else {
          await createWrongRecord(form.value!);
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