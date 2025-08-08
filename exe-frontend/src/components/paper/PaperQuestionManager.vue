<template>
  <div>
    <el-button type="primary" :icon="Plus" @click="isQuestionSelectVisible = true">从题库选题</el-button>

    <el-table :data="localQuestions" style="width: 100%; margin-top: 16px;" row-key="questionId">
      <el-table-column label="题干" prop="questionDetail.content" show-overflow-tooltip />
      <el-table-column label="题型" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.questionDetail">
            {{ questionTypeMap[scope.row.questionDetail.questionType] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分值" width="150">
        <template #default="scope">
          <el-input-number v-model="scope.row.score" :min="0" :step="1" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="排序" width="150">
        <template #default="scope">
          <el-button :icon="ArrowUp" circle plain size="small" @click="moveUp(scope.$index)"></el-button>
          <el-button :icon="ArrowDown" circle plain size="small" @click="moveDown(scope.$index)"></el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="danger" link :icon="Delete" @click="removeQuestion(scope.$index)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="isQuestionSelectVisible" title="从题库选择试题" width="70%">
      <question-selector :subject-id="subjectId" @add="addQuestions" />
    </el-dialog>

  </div>
</template>


<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue';
import { Plus, Delete, ArrowUp, ArrowDown } from '@element-plus/icons-vue';
import type { PaperQuestion } from '@/api/paper';
import type { Question } from '@/api/question';
import { fetchQuestionList } from '@/api/question';
import QuestionSelector from './QuestionSelector.vue';

const props = defineProps<{
  paperQuestions: PaperQuestion[];
  subjectId: number;
}>();

const emit = defineEmits(['update:questions']);

const localQuestions = ref<PaperQuestion[]>([]);
const isQuestionSelectVisible = ref(false);

const questionTypeMap = { 1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题' };

// --- 【核心修改】将所有函数定义移到 watch 侦听器之前 ---

const enrichQuestionDetails = async () => {
  const questionIds = localQuestions.value.map(q => q.questionId);
  if(questionIds.length === 0) return;
  // 实际项目中，最好有一个根据id列表批量获取题目的接口
  // 为简化，这里获取该科目的所有试题进行匹配
  const res = await fetchQuestionList({current: 1, size: 999, subjectId: props.subjectId});
  const allQuestionsMap = new Map(res.data.map((q: Question) => [q.id, q]));
  localQuestions.value.forEach(pq => {
    pq.questionDetail = allQuestionsMap.get(pq.questionId);
  });
};

const emitUpdate = () => {
  // 更新排序
  localQuestions.value.forEach((q, index) => q.sortOrder = index);
  emit('update:questions', localQuestions.value);
};

const addQuestions = (questions: Question[]) => {
  questions.forEach(q => {
    if (!localQuestions.value.some(pq => pq.questionId === q.id)) {
      localQuestions.value.push({
        paperId: 0, // 临时
        questionId: q.id,
        score: 5, // 默认分
        sortOrder: localQuestions.value.length,
        questionDetail: q
      });
    }
  });
  isQuestionSelectVisible.value = false;
  emitUpdate();
};

const removeQuestion = (index: number) => {
  localQuestions.value.splice(index, 1);
  emitUpdate();
};

const moveUp = (index: number) => {
  if (index === 0) return;
  const temp = localQuestions.value[index];
  localQuestions.value.splice(index, 1);
  localQuestions.value.splice(index - 1, 0, temp);
  emitUpdate();
};

const moveDown = (index: number) => {
  if (index === localQuestions.value.length - 1) return;
  const temp = localQuestions.value[index];
  localQuestions.value.splice(index, 1);
  localQuestions.value.splice(index + 1, 0, temp);
  emitUpdate();
};

// --- watch 侦听器现在放在函数定义的下方 ---

watch(() => props.paperQuestions, (newVal) => {
  localQuestions.value = [...(newVal || [])];
  if(localQuestions.value.length > 0 && !localQuestions.value[0].questionDetail) {
    // 现在调用是安全的
    enrichQuestionDetails();
  }
}, { immediate: true, deep: true });

</script>