<template>
  <div>
    <el-form :inline="true" :model="queryParams">
      <el-form-item label="题型">
        <el-select v-model="queryParams.questionType" placeholder="请选择题型" clearable @change="loadQuestions">
          <el-option label="单选题" :value="1" />
          <el-option label="多选题" :value="2" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table ref="tableRef" :data="questionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="content" label="题干" />
      <el-table-column prop="questionType" label="题型" width="100">
        <template #default="scope">
          <el-tag>{{ questionTypeMap[scope.row.questionType] }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        style="margin-top: 16px;"
        :total="total"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        @current-change="loadQuestions"
    />

    <div style="text-align: right; margin-top: 16px;">
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" @click="confirmAdd">确认添加</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { fetchQuestionList } from '@/api/question';
import type { Question, QuestionPageParams } from '@/api/question';

const props = defineProps<{
  subjectId: number;
}>();

const emit = defineEmits(['add', 'close']);

const questionList = ref<Question[]>([]);
const selectedQuestions = ref<Question[]>([]);
const total = ref(0);

const queryParams = reactive<QuestionPageParams>({
  current: 1,
  size: 5,
  subjectId: props.subjectId,
  questionType: undefined
});

const questionTypeMap = { 1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题' };


const loadQuestions = async () => {
  const res = await fetchQuestionList(queryParams);
  questionList.value = res.data;
  total.value = res.total;
};

const handleSelectionChange = (val: Question[]) => {
  selectedQuestions.value = val;
};

const confirmAdd = () => {
  emit('add', selectedQuestions.value);
};
// 2. 【新增】添加这个 watch 侦听器
watch(() => props.subjectId, (newSubjectId) => {
  // 当外部传入的 subjectId 变化时，执行以下操作：
  queryParams.subjectId = newSubjectId; // 更新查询参数
  queryParams.current = 1; // 重置到第一页
  questionList.value = []; // 立即清空旧数据，提升体验
  total.value = 0;
  loadQuestions(); // 重新加载题目列表
});

onMounted(loadQuestions);

</script>