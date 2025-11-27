<template>
  <div class="page-wrapper">
    <div v-if="!isExamStarted && !loading" class="overlay-layer start-layer">
      <el-card class="dialog-card">
        <h2>{{ paper?.name }} - 考试规则</h2>
        <div class="rules-content">
          <p><el-icon><Monitor /></el-icon> 考试全程需保持 <strong>全屏模式</strong></p>
          <p><el-icon><View /></el-icon> 禁止 <strong>切屏</strong> 或移出鼠标，系统将自动记录</p>
          <p><el-icon><WarningFilled /></el-icon> 累计违规超过 <strong>3次</strong> 将自动交卷！</p>
        </div>
        <el-button type="primary" size="large" @click="startExamAction" round>我已阅读，开始答题</el-button>
      </el-card>
    </div>

    <div v-if="isExamStarted && !isFullscreen && !resultVisible" class="overlay-layer lock-layer">
      <div class="lock-content">
        <el-icon class="lock-icon" color="#F56C6C"><Lock /></el-icon>
        <h2>考试已中断</h2>
        <p class="warning-text">检测到您退出了全屏模式，为了保证考试公平，内容已被锁定。</p>
        <p class="tip-text">请点击下方按钮重新进入全屏模式以继续答题。</p>
        <el-button type="danger" size="large" @click="resumeExam" class="resume-btn">
          恢复全屏并继续
        </el-button>
      </div>
    </div>

    <div class="exam-container" v-loading="loading" @contextmenu.prevent @copy.prevent @paste.prevent @selectstart.prevent>
      <div class="exam-header">
        <div class="left">
          <h2>{{ paper?.name }}</h2>
          <el-tag v-if="violationCount > 0" type="danger" effect="dark" style="margin-left: 10px;">
            ⚠ 已违规 {{ violationCount }} 次
          </el-tag>
        </div>
        <div class="center">
          <div class="timer">
            <el-icon><Timer /></el-icon>
            <span>{{ formattedTime }}</span>
          </div>
        </div>
        <div class="right">
          <el-button type="primary" @click="handleSubmit(false)">交卷</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchExamPaperDetail, submitExamPaper } from '@/api/studentAuth';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Timer, Monitor, View, WarningFilled, Lock } from '@element-plus/icons-vue';
// 修复点：添加了 request 导入
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const paperId = parseInt(route.params.paperId as string);

const loading = ref(true);
const paper = ref<any>(null);
const questionsMap = ref<Record<number, any>>({});
const answers = reactive<Record<number, string>>({});
const resultVisible = ref(false);
const examResult = ref<any>(null);

// 防作弊相关状态
const isExamStarted = ref(false);
const violationCount = ref(0);
const MAX_VIOLATIONS = 3;
const isFullscreen = ref(false);

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

// --- 防作弊核心逻辑 ---

const requestFullScreen = () => {
  const element = document.documentElement;
  if (element.requestFullscreen) {
    element.requestFullscreen().catch((err) => {
      console.warn('全屏请求被拦截:', err);
      ElMessage.warning('请允许浏览器进入全屏模式以继续考试');
    });
  }
};

const startExamAction = () => {
  isExamStarted.value = true;
  requestFullScreen();
  startTimer();

  document.addEventListener('visibilitychange', handleVisibilityChange);
  window.addEventListener('blur', handleWindowBlur);
  document.addEventListener('fullscreenchange', handleFullScreenChange);
};

const resumeExam = () => {
  requestFullScreen();
};

const handleViolation = (reason: string) => {
  if (!isExamStarted.value || resultVisible.value) return;

  violationCount.value++;

  if (violationCount.value >= MAX_VIOLATIONS) {

    ElMessageBox.alert(`严重警告：您已累计违规 ${violationCount.value} 次（${reason}），系统将强制交卷！`, '考试终止', {
      confirmButtonText: '确定',
      type: 'error',
      showClose: false,
      closeOnPressEscape: false,
      closeOnClickModal: false,
      callback: () => handleSubmit(true)
    });
  } else {
    ElMessage.error({
      message: `警告：检测到 ${reason}！累计违规 ${violationCount.value}/${MAX_VIOLATIONS} 次`,
      duration: 4000,
      grouping: true
    });
  }
};

const handleVisibilityChange = () => {
  if (document.hidden) {
    handleViolation('切屏/离开页面');
  }
};

const handleWindowBlur = () => {
  if (isExamStarted.value && !resultVisible.value) {
    handleViolation('窗口失焦(切换应用)');
  }
};

const handleFullScreenChange = () => {
  const isFull = !!document.fullscreenElement;
  isFullscreen.value = isFull;

  if (!isFull && isExamStarted.value && !resultVisible.value) {
    handleViolation('退出全屏');
  }
};

const removeListeners = () => {
  document.removeEventListener('visibilitychange', handleVisibilityChange);
  window.removeEventListener('blur', handleWindowBlur);
  document.removeEventListener('fullscreenchange', handleFullScreenChange);
};

const handleSubmit = (force = false) => {
  const submitLogic = async () => {
    clearInterval(timer);
    loading.value = true;
    try {
      const payload = {
        answers: answers,
        violationCount: violationCount.value
      };

      const res = await request({
        url: '/api/v1/student/exam/submit',
        method: 'post',
        params: { paperId },
        data: payload
      });

      if (res.code === 200) {
        examResult.value = res.data;
        resultVisible.value = true;
        removeListeners();
        if (document.fullscreenElement) {
          document.exitFullscreen();
        }
      }
    } finally {
      loading.value = false;
    }
  };

  if (force) {
    submitLogic();
  } else {
    ElMessageBox.confirm('确认交卷吗？交卷后将无法修改答案。', '交卷确认', {
      confirmButtonText: '确认交卷',
      cancelButtonText: '继续答题',
      type: 'warning'
    }).then(submitLogic);
  }
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
  removeListeners();
});
</script>

<style scoped>
.page-wrapper {
  position: relative;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.overlay-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(8px);
}

.start-layer {
  background: rgba(0, 0, 0, 0.8);
}

.lock-layer {
  background: #2c3e50;
  color: #fff;
  flex-direction: column;
}

.dialog-card {
  width: 500px;
  text-align: center;
  padding: 20px;
  border-radius: 16px;
}

.rules-content {
  text-align: left;
  margin: 30px 0;
  font-size: 16px;
  line-height: 2;
  color: #606266;
  background: #f8f8f8;
  padding: 20px;
  border-radius: 8px;
}

.rules-content p {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 10px 0;
}

.rules-content strong {
  color: #F56C6C;
}

.lock-content {
  text-align: center;
  animation: fadeIn 0.3s ease;
}

.lock-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.lock-content h2 {
  font-size: 32px;
  margin-bottom: 16px;
}

.warning-text {
  font-size: 18px;
  color: #ff7875;
  margin-bottom: 10px;
}

.tip-text {
  font-size: 16px;
  color: #d9d9d9;
  margin-bottom: 30px;
}

.resume-btn {
  padding: 12px 40px;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.exam-container {
  padding-top: 70px;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  user-select: none;
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