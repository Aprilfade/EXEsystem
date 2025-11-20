<template>
  <div class="exam-container" v-loading="loading">
    <div class="exam-header">
      <div class="left">
        <h2>{{ paper?.name }}</h2>
      </div>
      <div class="center">
        <div class="timer">
          <el-icon><Timer /></el-icon>
          <span>{{ formattedTime }}</span>
        </div>
      </div>
      <div class="right">
        <el-button type="primary" @click="handleSubmit">交卷</el-button>
      </div>
    </div>

    <div class="exam-body" v-if="paper">
      <div class="question-area">
        <div v-for="group in paper.groups" :key="group.id" class="group-section">
          <div class="group-title">{{ group.name }}</div>
          <div v-for="(pq, index) in group.questions" :key="pq.id" :id="'q-' + pq.questionId" class="question-box">
            <div class="q-header">
              <span class="q-no">{{ index + 1 }}</span>
              <span class="q-score">({{ pq.score }}分)</span>
            </div>
            <div class="q-content">
              <div v-html="getQuestion(pq.questionId)?.content"></div>
              <el-image
                  v-if="getQuestion(pq.questionId)?.imageUrl"
                  :src="getQuestion(pq.questionId)?.imageUrl"
                  style="max-width: 300px; margin-top:10px"
                  :preview-src-list="[getQuestion(pq.questionId)?.imageUrl]"
              />
            </div>

            <div class="q-options">
              <template v-if="[1, 2].includes(getQuestion(pq.questionId)?.questionType)">
                <div
                    v-for="opt in parseOptions(getQuestion(pq.questionId)?.options)"
                    :key="opt.key"
                    class="option-row"
                    @click="handleOptionClick(pq.questionId, opt.key, getQuestion(pq.questionId)?.questionType)"
                    :class="{ selected: isOptionSelected(pq.questionId, opt.key) }"
                >
                  <span class="opt-key">{{ opt.key }}</span>
                  <span class="opt-val">{{ opt.value }}</span>
                </div>
              </template>

              <template v-else-if="[3, 5].includes(getQuestion(pq.questionId)?.questionType)">
                <el-input
                    v-model="answers[pq.questionId]"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入答案"
                />
              </template>

              <template v-else-if="getQuestion(pq.questionId)?.questionType === 4">
                <el-radio-group v-model="answers[pq.questionId]">
                  <el-radio label="T" border>正确</el-radio>
                  <el-radio label="F" border>错误</el-radio>
                </el-radio-group>
              </template>
            </div>
          </div>
        </div>
      </div>

      <div class="answer-card">
        <div class="card-title">答题卡</div>
        <div class="card-content">
          <div v-for="group in paper.groups" :key="group.id" class="card-group">
            <div class="cg-name">{{ group.name }}</div>
            <div class="cg-grid">
              <div
                  v-for="(pq, index) in group.questions"
                  :key="pq.id"
                  class="card-item"
                  :class="{ answered: !!answers[pq.questionId] }"
                  @click="scrollToQuestion('q-' + pq.questionId)"
              >
                {{ index + 1 }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="resultVisible" title="考试结果" width="500px" center :close-on-click-modal="false" :show-close="false">
    <div class="result-content">
      <el-progress type="dashboard" :percentage="scorePercentage" :color="customColors">
        <template #default="{ percentage }">
          <span class="score-text">{{ examResult?.score }}</span>
          <span class="score-label">分</span>
        </template>
      </el-progress>
      <div class="result-detail">
        <p>试卷总分：{{ examResult?.totalScore }}</p>
        <p>本次得分：{{ examResult?.score }}</p>
      </div>
    </div>
    <template #footer>
      <el-button type="primary" @click="$router.push('/student/exams')">返回列表</el-button>
      <el-button @click="$router.push('/student/wrong-records')">查看错题</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchExamPaperDetail, submitExamPaper } from '@/api/studentAuth';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Timer } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const paperId = parseInt(route.params.paperId as string);

const loading = ref(true);
const paper = ref<any>(null);
const questionsMap = ref<Record<number, any>>({});
const answers = reactive<Record<number, string>>({});
const resultVisible = ref(false);
const examResult = ref<any>(null);

// 计时器
const duration = ref(0);
let timer: any = null;

const formattedTime = computed(() => {
  const h = Math.floor(duration.value / 3600).toString().padStart(2, '0');
  const m = Math.floor((duration.value % 3600) / 60).toString().padStart(2, '0');
  const s = (duration.value % 60).toString().padStart(2, '0');
  return `${h}:${m}:${s}`;
});

const scorePercentage = computed(() => {
  if(!examResult.value) return 0;
  return Math.round((examResult.value.score / examResult.value.totalScore) * 100);
});

const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 80 },
  { color: '#1989fa', percentage: 100 },
];

const loadPaper = async () => {
  try {
    const res = await fetchExamPaperDetail(paperId);
    if (res.code === 200) {
      paper.value = res.data.paper;
      questionsMap.value = res.data.questions || {};
      startTimer();
    } else {
      ElMessage.error(res.msg);
      router.back();
    }
  } catch (e) {
    ElMessage.error('试卷加载失败');
  } finally {
    loading.value = false;
  }
};

const getQuestion = (id: number) => questionsMap.value[id];

const parseOptions = (optsStr: string | any[]) => {
  if (typeof optsStr === 'string') {
    try { return JSON.parse(optsStr); } catch { return []; }
  }
  return optsStr || [];
};

const handleOptionClick = (qId: number, key: string, type: number) => {
  if (type === 1) { // 单选
    answers[qId] = key;
  } else if (type === 2) { // 多选
    let current = answers[qId] ? answers[qId].split(',') : [];
    if (current.includes(key)) {
      current = current.filter(k => k !== key);
    } else {
      current.push(key);
    }
    answers[qId] = current.sort().join(',');
  }
};

const isOptionSelected = (qId: number, key: string) => {
  const val = answers[qId];
  if (!val) return false;
  return val.split(',').includes(key);
};

const scrollToQuestion = (id: string) => {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
};

const startTimer = () => {
  timer = setInterval(() => {
    duration.value++;
  }, 1000);
};

const handleSubmit = () => {
  ElMessageBox.confirm('确认交卷吗？交卷后将无法修改答案。', '交卷确认', {
    confirmButtonText: '确认交卷',
    cancelButtonText: '继续答题',
    type: 'warning'
  }).then(async () => {
    clearInterval(timer);
    loading.value = true;
    try {
      const res = await submitExamPaper(paperId, answers);
      if (res.code === 200) {
        examResult.value = res.data;
        resultVisible.value = true;
      }
    } finally {
      loading.value = false;
    }
  });
};

onMounted(() => {
  if (!paperId) {
    router.push('/student/exams');
    return;
  }
  loadPaper();
});

onUnmounted(() => {
  clearInterval(timer);
});
</script>

<style scoped>
.exam-container {
  padding-top: 70px; /* 给固定头部留空间 */
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
}

.exam-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
}
.exam-header .center .timer {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.exam-body {
  width: 1200px;
  display: flex;
  gap: 20px;
  padding: 20px 0;
}

.question-area {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.group-title {
  font-size: 18px;
  font-weight: bold;
  margin: 20px 0 15px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.question-box {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #eee;
}
.q-header { margin-bottom: 10px; }
.q-no { font-size: 18px; font-weight: bold; color: #409eff; margin-right: 8px; }
.q-score { color: #909399; font-size: 14px; }
.q-content { font-size: 16px; line-height: 1.6; margin-bottom: 15px; }

.option-row {
  display: flex;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.option-row:hover { background-color: #f5f7fa; }
.option-row.selected { border-color: #409eff; background-color: #ecf5ff; color: #409eff; }
.opt-key { font-weight: bold; margin-right: 10px; }

.answer-card {
  width: 280px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  height: fit-content;
  position: sticky;
  top: 80px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.card-title { font-size: 16px; font-weight: bold; margin-bottom: 15px; text-align: center; }
.cg-name { font-size: 14px; color: #606266; margin-bottom: 8px; margin-top: 10px; }
.cg-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; }
.card-item {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.card-item.answered { background-color: #409eff; color: #fff; border-color: #409eff; }

.result-content { text-align: center; }
.score-text { font-size: 48px; font-weight: bold; }
.score-label { font-size: 14px; color: #909399; }
.result-detail { margin-top: 20px; font-size: 16px; line-height: 2; }
</style>