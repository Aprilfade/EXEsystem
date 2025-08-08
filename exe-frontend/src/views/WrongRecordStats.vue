<template>
  <div class="stats-container">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="按学生查询" name="byStudent">
          <el-select v-model="selectedStudent" filterable placeholder="请选择学生" @change="queryByStudent" size="large">
            <el-option v-for="s in allStudents" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
          </el-select>
          <el-table :data="studentResult" style="margin-top: 20px;">
            <el-table-column prop="questionContent" label="题干" show-overflow-tooltip />
            <el-table-column prop="paperName" label="来源试卷" />
            <el-table-column prop="wrongReason" label="错误原因" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="按题目查询" name="byQuestion">
          <el-select v-model="selectedQuestion" filterable remote :remote-method="searchQuestions" placeholder="请输入关键词搜索题目" @change="queryByQuestion" size="large" style="width: 100%;">
            <el-option v-for="q in questionOptions" :key="q.id" :label="q.content" :value="q.id" />
          </el-select>
          <el-table :data="questionResult" style="margin-top: 20px;">
            <el-table-column prop="studentName" label="学生姓名" />
            <el-table-column prop="studentNo" label="学号" />
            <el-table-column prop="wrongReason" label="错误原因" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="按试卷分析" name="byPaper">
          <el-select v-model="selectedPaper" filterable placeholder="请选择试卷" @change="queryByPaper" size="large">
            <el-option v-for="p in allPapers" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
          <el-table :data="paperResult" style="margin-top: 20px;">
            <el-table-column prop="sortOrder" label="题号" width="80" />
            <el-table-column prop="questionContent" label="题干" show-overflow-tooltip />
            <el-table-column prop="errorCount" label="错误人数" width="120" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { fetchRecordsByStudent, fetchStudentsByQuestion, fetchStatsByPaper, type WrongRecordVO, type PaperStatsVO } from '@/api/wrongRecord';
import { fetchStudentList, type Student } from '@/api/student';
import { fetchQuestionList, type Question } from '@/api/question';
import { fetchPaperList, type Paper } from '@/api/paper';
import { ElMessage } from 'element-plus';

const activeTab = ref('byStudent');

const allStudents = ref<Student[]>([]);
const selectedStudent = ref<number | null>(null);
const studentResult = ref<WrongRecordVO[]>([]);

const questionOptions = ref<Question[]>([]);
const selectedQuestion = ref<number | null>(null);
const questionResult = ref<WrongRecordVO[]>([]);

const allPapers = ref<Paper[]>([]);
const selectedPaper = ref<number | null>(null);
const paperResult = ref<PaperStatsVO[]>([]);

const loadInitialData = async () => {
  const studentRes = await fetchStudentList({ current: 1, size: 9999 });
  allStudents.value = studentRes.data;
  const paperRes = await fetchPaperList({ current: 1, size: 9999 });
  allPapers.value = paperRes.data;
};

const queryByStudent = async (studentId: number) => {
  if (!studentId) return;
  const res = await fetchRecordsByStudent(studentId);
  studentResult.value = res.data;
};

const searchQuestions = async (query: string) => {
  if (query) {
    const res = await fetchQuestionList({ current: 1, size: 50 });
    questionOptions.value = res.data.filter(q => q.content.toLowerCase().includes(query.toLowerCase()));
  } else {
    questionOptions.value = [];
  }
};

const queryByQuestion = async (questionId: number) => {
  if (!questionId) return;
  const res = await fetchStudentsByQuestion(questionId);
  questionResult.value = res.data;
};

const queryByPaper = async (paperId: number) => {
  if (!paperId) return;
  const res = await fetchStatsByPaper(paperId);
  paperResult.value = res.data;
};

onMounted(loadInitialData);
</script>

<style scoped>
.stats-container {
  padding: 20px;
}
</style>