<template>
  <div class="result-page" v-loading="loading">
    <div class="page-header" v-if="paper">
      <el-page-header @back="$router.back()">
        <template #content>
          <span class="header-title">{{ paper.name }} - 考试回顾</span>
        </template>
        <template #extra>
          <div class="score-badge">
            得分：<span class="score-val">{{ examResult?.score }}</span> / {{ examResult?.totalScore }}
          </div>
        </template>
      </el-page-header>
    </div>

    <div class="paper-content" v-if="paper">
      <template v-if="!paper.paperType || paper.paperType === 1">
        <div v-for="group in paper.groups" :key="group.id" class="group-section">
          <div class="group-title">{{ group.name }}</div>
          <div v-for="(pq, index) in group.questions" :key="pq.id" class="question-box">
            <div class="q-header">
              <span class="q-no">{{ index + 1 }}</span>
              <span class="q-score">({{ pq.score }}分)</span>
              <el-tag :type="isCorrect(pq.questionId) ? 'success' : 'danger'" size="small" class="status-tag">
                {{ isCorrect(pq.questionId) ? '正确' : '错误' }}
              </el-tag>
            </div>

            <div class="q-content">
              <div v-html="questionsMap[pq.questionId]?.content"></div>
              <el-image
                  v-if="questionsMap[pq.questionId]?.imageUrl"
                  :src="questionsMap[pq.questionId]?.imageUrl"
                  style="max-width: 300px; margin-top:10px"
              />
            </div>

            <div class="q-options" v-if="[1, 2].includes(questionsMap[pq.questionId]?.questionType)">
              <div
                  v-for="opt in parseOptions(questionsMap[pq.questionId]?.options)"
                  :key="opt.key"
                  class="option-row"
                  :class="{
                  'user-selected': isUserSelected(pq.questionId, opt.key),
                  'correct-answer': isCorrectOption(pq.questionId, opt.key)
                }"
              >
                <span class="opt-key">{{ opt.key }}.</span>
                <span class="opt-val">{{ opt.value }}</span>
                <el-icon v-if="isUserSelected(pq.questionId, opt.key) && !isCorrectOption(pq.questionId, opt.key)" color="#F56C6C"><Close /></el-icon>
                <el-icon v-if="isCorrectOption(pq.questionId, opt.key)" color="#67C23A"><Check /></el-icon>
              </div>
            </div>

            <div class="analysis-box">
              <div class="analysis-row">
                <span class="label">你的答案：</span>
                <span class="user-ans" :class="{ error: !isCorrect(pq.questionId) }">
                  {{ getUserAnswer(pq.questionId) || '未作答' }}
                </span>
              </div>
              <div class="analysis-row">
                <span class="label">正确答案：</span>
                <span class="correct-ans">{{ questionsMap[pq.questionId]?.answer }}</span>
              </div>
              <div class="analysis-row">
                <span class="label">解析：</span>
                <div class="desc-content">
                  {{ questionsMap[pq.questionId]?.description || '暂无解析' }}
                  <div v-if="questionsMap[pq.questionId]?.answerImageUrl">
                    <el-image :src="questionsMap[pq.questionId]?.answerImageUrl" style="max-width: 200px;" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="paper.paperType === 2">
        <el-alert title="图片试卷需要老师人工阅卷，请耐心等待成绩更新。" type="info" show-icon :closable="false" style="margin-bottom: 20px;" />

        <div class="paper-images-container">
          <div v-for="(img, idx) in paper.paperImages" :key="img.id || idx" class="paper-image-item">
            <el-image
                :src="img.imageUrl"
                fit="contain"
                :preview-src-list="paper.paperImages.map((i:any) => i.imageUrl)"
                :initial-index="idx"
                style="width: 100%; border: 1px solid #eee; margin-bottom: 20px; border-radius: 8px;"
            />
          </div>
        </div>

        <div class="answer-sheet-view">
          <h3>我的答题卡</h3>
          <div class="answer-grid">
            <div v-for="(val, key) in userAnswersMap" :key="key" class="answer-item">
              <div class="ans-index">第 {{ key }} 题</div>
              <div class="ans-val">{{ val }}</div>
            </div>
            <el-empty v-if="Object.keys(userAnswersMap).length === 0" description="未读取到答题记录" image-size="60" />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { fetchExamHistoryDetail } from '@/api/studentAuth';
import { Check, Close } from '@element-plus/icons-vue';

const route = useRoute();
const resultId = parseInt(route.params.resultId as string);

const loading = ref(true);
const examResult = ref<any>(null);
const paper = ref<any>(null);
const questionsMap = ref<any>({});
const userAnswersMap = ref<Record<string, string>>({}); // key可能是数字字符串

const loadData = async () => {
  try {
    const res = await fetchExamHistoryDetail(resultId);
    if (res.code === 200) {
      examResult.value = res.data.examResult;
      paper.value = res.data.paper;
      questionsMap.value = res.data.questions || {};

      if (examResult.value.userAnswers) {
        try {
          userAnswersMap.value = JSON.parse(examResult.value.userAnswers);
        } catch (e) {
          console.error("答案解析失败", e);
        }
      }
    }
  } finally {
    loading.value = false;
  }
};

const parseOptions = (optsStr: any) => {
  if (typeof optsStr === 'string') {
    try { return JSON.parse(optsStr); } catch { return []; }
  }
  return optsStr || [];
};

const getUserAnswer = (qId: number) => userAnswersMap.value[qId.toString()];

const isCorrect = (qId: number) => {
  const userAns = getUserAnswer(qId);
  const correctAns = questionsMap.value[qId]?.answer;
  if (!userAns || !correctAns) return false;
  const sortStr = (s: string) => s.split(',').map(i=>i.trim()).sort().join(',');
  return sortStr(userAns.toString()).toLowerCase() === sortStr(correctAns.toString()).toLowerCase();
};

const isUserSelected = (qId: number, key: string) => {
  const ans = getUserAnswer(qId);
  return ans ? ans.split(',').includes(key) : false;
};

const isCorrectOption = (qId: number, key: string) => {
  const ans = questionsMap.value[qId]?.answer;
  return ans ? ans.split(',').includes(key) : false;
};

onMounted(loadData);
</script>

<style scoped>
.result-page { padding: 20px; background-color: #f5f7fa; min-height: 100vh; }
.page-header { background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.header-title { font-size: 18px; font-weight: 600; }
.score-badge { font-size: 16px; color: #606266; }
.score-val { font-size: 24px; font-weight: bold; color: #409EFF; }

.paper-content { background: #fff; padding: 30px; border-radius: 8px; max-width: 1000px; margin: 0 auto; }
.group-title { font-size: 18px; font-weight: bold; margin: 20px 0 15px; border-left: 4px solid #409eff; padding-left: 10px; }
.question-box { margin-bottom: 30px; border-bottom: 1px dashed #eee; padding-bottom: 20px; }
.q-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.q-no { font-size: 18px; font-weight: bold; color: #409eff; }
.q-score { color: #909399; font-size: 14px; }
.status-tag { margin-left: auto; }

.option-row { display: flex; align-items: center; padding: 10px 15px; border: 1px solid #dcdfe6; border-radius: 4px; margin-bottom: 8px; gap: 10px; }
.option-row.user-selected { background-color: #fef0f0; border-color: #fde2e2; }
.option-row.correct-answer { background-color: #f0f9eb; border-color: #e1f3d8; }
.option-row.user-selected.correct-answer { background-color: #f0f9eb; border-color: #e1f3d8; }
.opt-key { font-weight: bold; }

.analysis-box { background-color: #f9f9f9; padding: 15px; border-radius: 4px; margin-top: 15px; }
.analysis-row { margin-bottom: 8px; display: flex; }
.analysis-row .label { font-weight: bold; width: 80px; flex-shrink: 0; color: #606266; }
.user-ans { font-weight: bold; }
.user-ans.error { color: #F56C6C; }
.correct-ans { color: #67C23A; font-weight: bold; }
.desc-content { color: #606266; line-height: 1.6; }

/* 图片试卷相关样式 */
.paper-images-container { text-align: center; margin-bottom: 30px; }
.answer-sheet-view { margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; }
.answer-sheet-view h3 { margin-bottom: 15px; font-size: 16px; font-weight: 600; }
.answer-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 10px; }
.answer-item { border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; text-align: center; }
.ans-index { background: #f5f7fa; font-size: 12px; color: #909399; padding: 4px; border-bottom: 1px solid #ebeef5; }
.ans-val { padding: 8px; font-weight: bold; color: #409EFF; }
</style>