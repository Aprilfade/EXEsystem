<template>
  <div class="practice-container">
    <el-card v-if="practiceState === 'selectingGrade'" class="selection-card">
      <template #header><h2>请选择您的年级</h2></template>
      <div class="selection-grid">
        <div v-for="grade in grades" :key="grade" class="grid-item" @click="selectGrade(grade)">
          <el-icon><Reading /></el-icon>
          <span>{{ grade }}</span>
        </div>
      </div>
    </el-card>

    <el-card v-else-if="practiceState === 'selectingSubject'" class="selection-card">
      <template #header>
        <div class="card-header-flex">
          <h2>请选择科目（{{ currentGrade }}）</h2>
          <el-button type="primary" link @click="resetPractice">返回重选年级</el-button>
        </div>
      </template>
      <div class="selection-grid">
        <div v-for="subject in filteredSubjects" :key="subject.id" class="grid-item" @click="startPractice(subject)">
          <el-icon><Collection /></el-icon>
          <span>{{ subject.name }}</span>
        </div>
      </div>
      <el-empty v-if="filteredSubjects.length === 0" description="该年级下暂无科目"></el-empty>
    </el-card>

    <el-card v-else-if="practiceState === 'practicing'" class="practice-card">
      <template #header>
        <div class="card-header-flex">
          <h2>{{ currentSubject?.name }} - 在线练习</h2>
          <el-button type="primary" link @click="resetPractice">退出练习</el-button>
        </div>
      </template>
      <div v-if="questions.length > 0">
        <div class="question-progress">题目 {{ currentQuestionIndex + 1 }} / {{ questions.length }}</div>
        <div class="question-content">
          <p>{{ currentQuestion.content }}</p>
          <el-image v-if="currentQuestion.imageUrl" :src="currentQuestion.imageUrl" fit="contain" style="max-width: 300px; margin-top: 10px;"/>
        </div>
        <div class="options-container">
          <el-radio-group v-if="currentQuestion.questionType === 1 || currentQuestion.questionType === 2" v-model="userAnswers[currentQuestion.id]">
            <el-radio v-for="option in parsedOptions" :key="option.key" :label="option.key" border class="option-item">
              {{ option.key }}. {{ option.value }}
            </el-radio>
          </el-radio-group>
          <el-input
              v-else-if="currentQuestion.questionType === 3"
              v-model="userAnswers[currentQuestion.id]"
              placeholder="请输入答案"
              size="large"
          />
          <el-radio-group v-else-if="currentQuestion.questionType === 4" v-model="userAnswers[currentQuestion.id]">
            <el-radio label="T" border class="option-item">正确</el-radio>
            <el-radio label="F" border class="option-item">错误</el-radio>
          </el-radio-group>
          <el-input
              v-else-if="currentQuestion.questionType === 5"
              v-model="userAnswers[currentQuestion.id]"
              type="textarea"
              :rows="5"
              placeholder="请输入你的解答"
          />
        </div>
        <div class="action-buttons">
          <el-button @click="prevQuestion" :disabled="currentQuestionIndex === 0">上一题</el-button>
          <el-button v-if="currentQuestionIndex < questions.length - 1" type="primary" @click="nextQuestion">下一题</el-button>
          <el-button v-else type="success" @click="submitPractice" :loading="isSubmitting">提交作答</el-button>
        </div>
      </div>
      <el-empty v-else description="该科目下暂无练习题"></el-empty>
    </el-card>

    <el-card v-else-if="practiceState === 'result'" class="result-card">
      <template #header>
        <div class="card-header-flex">
          <h2>练习结果</h2>
          <el-button type="primary" @click="resetPractice">再来一次</el-button>
        </div>
      </template>
      <div v-if="practiceResult">
        <div class="result-summary">
          <el-progress type="circle" :percentage="correctPercentage" :width="120">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}%</span>
              <span class="percentage-label">正确率</span>
            </template>
          </el-progress>
          <div class="summary-text">
            <h3>总题数: {{ practiceResult.totalQuestions }}</h3>
            <h3>答对: {{ practiceResult.correctCount }}</h3>
            <h3>答错: {{ practiceResult.totalQuestions - practiceResult.correctCount }}</h3>
          </div>
        </div>
        <el-divider />
        <div v-for="(result, index) in practiceResult.results" :key="result.question.id" class="result-item" :class="{ correct: result.isCorrect, wrong: !result.isCorrect }">
          <div class="result-question-content">
            <p><b>{{ index + 1 }}. {{ result.question.content }}</b></p>
          </div>
          <div class="result-answer-info">
            <p>你的答案: <el-tag :type="result.isCorrect ? 'success' : 'danger'">{{ result.userAnswer || '未作答' }}</el-tag></p>
            <p v-if="!result.isCorrect">正确答案: <el-tag type="success">{{ result.question.answer }}</el-tag></p>
          </div>
          <el-collapse-transition>
            <div v-if="!result.isCorrect" class="result-explanation">
              <b>解析：</b>{{ result.question.description || '暂无解析' }}
            </div>
          </el-collapse-transition>
        </div>
      </div>
      <el-empty v-else description="正在加载练习结果..."></el-empty>
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import request from '@/utils/request';
// 【修复】从正确的路径导入类型
import type { Subject } from '@/api/subject';
import type { Question, QuestionOption } from '@/api/question';
import type { ApiResult } from '@/api/user'; // 引入通用的 ApiResult 类型
import { ElMessage } from 'element-plus';
import { Collection, Reading } from '@element-plus/icons-vue';

// 【修复】为 practiceResult 提供更具体的类型
interface PracticeResult {
  totalQuestions: number;
  correctCount: number;
  results: Array<{
    question: Question;
    userAnswer: string;
    isCorrect: boolean;
  }>;
}

const practiceState = ref('selectingGrade');
const currentGrade = ref<string | null>(null);
const currentSubject = ref<Subject | null>(null);
const grades = ref(['七年级', '八年级', '九年级', '高一', '高二', '高三']);
const allSubjects = ref<Subject[]>([]);
const questions = ref<Question[]>([]);
const currentQuestionIndex = ref(0);
const userAnswers = ref<Record<number, string>>({});
const practiceResult = ref<PracticeResult | null>(null); // 使用修复后的类型
const isSubmitting = ref(false);
const filteredSubjects = computed(() => {
  if (!currentGrade.value) return [];
  return allSubjects.value.filter(subject => subject.grade === currentGrade.value);
});

const currentQuestion = computed<Partial<Question>>(() => questions.value[currentQuestionIndex.value] || {});

const parsedOptions = computed((): QuestionOption[] => {
  if (currentQuestion.value && typeof currentQuestion.value.options === 'string') {
    try { return JSON.parse(currentQuestion.value.options); } catch (e) { return []; }
  }
  return [];
});

const correctPercentage = computed(() => {
  if (!practiceResult.value || practiceResult.value.totalQuestions === 0) return 0;
  return Math.round((practiceResult.value.correctCount / practiceResult.value.totalQuestions) * 100);
});

const loadAllSubjects = async () => {
  try {
    // 【修复】为API返回结果添加明确的类型
    const res: ApiResult<Subject[]> = await request({ url: '/api/v1/student/subjects', method: 'get' });
    if (res.code === 200) {
      allSubjects.value = res.data;
    }
  } catch (error) {
    ElMessage.error('获取科目列表失败');
  }
};

const selectGrade = (grade: string) => {
  currentGrade.value = grade;
  practiceState.value = 'selectingSubject';
};

// 【修复】为参数 subject 添加类型
const startPractice = async (subject: Subject) => {
  currentSubject.value = subject;
  try {
    // 【修复】为API返回结果添加明确的类型
    const res: ApiResult<Question[]> = await request({
      url: '/api/v1/student/practice-questions',
      method: 'get',
      params: {
        subjectId: subject.id,
        grade: currentGrade.value
      }
    });
    if (res.code === 200) {
      questions.value = res.data;
      currentQuestionIndex.value = 0;
      userAnswers.value = {};
      practiceState.value = 'practicing';
    }
  } catch (e) { ElMessage.error('获取练习题失败'); }
};

const resetPractice = () => {
  practiceState.value = 'selectingGrade';
  currentGrade.value = null;
  currentSubject.value = null;
  questions.value = [];
};

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) currentQuestionIndex.value++;
};

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) currentQuestionIndex.value--;
};

const submitPractice = async () => {
  isSubmitting.value = true;
  try {
    // 【修复】为API返回结果添加明确的类型
    const res: ApiResult<PracticeResult> = await request({
      url: '/api/v1/student/submit-practice',
      method: 'post',
      data: { answers: userAnswers.value }
    });
    if (res.code === 200) {
      practiceResult.value = res.data;
      practiceState.value = 'result';
    }
  } catch (error) {
    ElMessage.error('提交失败');
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(loadAllSubjects);

</script>

<style scoped>
/* <style> 部分的代码无需改动，保持原样即可 */
.practice-container { padding: 24px; }
.selection-card { max-width: 800px; margin: 40px auto; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.selection-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 16px; }
.grid-item { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px; border: 1px solid #e4e7ed; border-radius: 8px; cursor: pointer; transition: all 0.2s ease; }
.grid-item:hover { border-color: #409EFF; color: #409EFF; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1); }
.grid-item .el-icon { font-size: 32px; margin-bottom: 8px; }
.practice-card { max-width: 900px; margin: auto; }
.question-progress { text-align: right; color: #909399; margin-bottom: 20px; }
.question-content { font-size: 1.2rem; line-height: 1.8; margin-bottom: 30px; }
.options-container { display: flex; flex-direction: column; gap: 15px; }
.option-item { width: 100%; margin: 0 !important; height: auto; padding: 15px; }
.action-buttons { margin-top: 40px; text-align: center; }
.result-card { max-width: 900px; margin: auto; }
.result-summary { display: flex; align-items: center; justify-content: center; gap: 40px; padding: 20px 0; }
.percentage-value { display: block; font-size: 24px; font-weight: bold; }
.percentage-label { display: block; font-size: 14px; color: #909399; }
.summary-text h3 { margin: 5px 0; }
.result-item { border: 1px solid #e4e7ed; border-radius: 8px; margin-bottom: 16px; padding: 16px; }
.result-item.correct { border-left: 5px solid #67C23A; }
.result-item.wrong { border-left: 5px solid #F56C6C; }
.result-answer-info { display: flex; gap: 20px; margin: 10px 0; }
.result-explanation { background-color: #f5f7fa; padding: 10px; border-radius: 4px; margin-top: 10px; font-size: 0.9rem; color: #606266; }
</style>