<template>
  <div class="review-container">
    <el-row :gutter="20" style="width: 100%; max-width: 1200px;">

      <el-col :span="18" :xs="24">
        <el-card class="review-card" v-loading="loading">
          <template #header>
            <div class="header-flex">
              <h2>🧠 智能复习模式</h2>
              <div class="header-right-info">
                <el-tag type="success" effect="dark" style="margin-right: 10px;">
                  进度: {{ currentIndex + 1 }} / {{ totalCount }}
                </el-tag>
              </div>
            </div>
          </template>

          <div v-if="isFinished" class="finished-state">
            <el-result icon="success" title="今日复习完成！" sub-title="坚持就是胜利，明天继续保持哦~">
              <template #extra>
                <el-button type="primary" @click="$router.push('/student/dashboard')">返回首页</el-button>
              </template>
            </el-result>
          </div>

          <div v-else-if="currentRecord" class="question-area">
            <div class="progress-bar">
              <el-progress
                  :percentage="progressPercentage"
                  :show-text="false"
                  :stroke-width="6"
              />
            </div>

            <div class="question-content">
              <el-tag style="margin-bottom: 10px;">{{ getQuestionTypeName(currentQuestionDetail?.questionType) }}</el-tag>
              <div class="q-text" v-html="currentQuestionDetail?.content"></div>
              <el-image
                  v-if="currentQuestionDetail?.imageUrl"
                  :src="currentQuestionDetail.imageUrl"
                  style="max-height: 200px; margin: 10px 0;"
                  fit="contain"
              />

              <div class="options-wrapper" v-if="[1, 2].includes(currentQuestionDetail?.questionType || 0)">
                <div
                    v-for="opt in parseOptions(currentQuestionDetail?.options)"
                    :key="opt.key"
                    class="review-option-item"
                    :class="{
                      'selected': isSelected(opt.key),
                      'is-correct': showAnswer && isCorrectOption(opt.key),
                      'is-wrong': showAnswer && isSelected(opt.key) && !isCorrectOption(opt.key)
                    }"
                    @click="handleOptionClick(opt.key, currentQuestionDetail?.questionType)"
                >
                  <span class="opt-key">{{ opt.key }}.</span>
                  <span class="opt-val">{{ opt.value }}</span>
                  <el-icon v-if="showAnswer && isCorrectOption(opt.key)" class="status-icon success"><Select /></el-icon>
                  <el-icon v-if="showAnswer && isSelected(opt.key) && !isCorrectOption(opt.key)" class="status-icon error"><CloseBold /></el-icon>
                </div>
              </div>

              <div class="options-wrapper" v-if="currentQuestionDetail?.questionType === 4">
                <div
                    class="review-option-item"
                    :class="{ 'selected': userDraft === 'T', 'is-correct': showAnswer && currentQuestionDetail.answer === 'T' }"
                    @click="handleOptionClick('T', 4)"
                >
                  <span class="opt-val">正确 (True)</span>
                </div>
                <div
                    class="review-option-item"
                    :class="{ 'selected': userDraft === 'F', 'is-correct': showAnswer && currentQuestionDetail.answer === 'F' }"
                    @click="handleOptionClick('F', 4)"
                >
                  <span class="opt-val">错误 (False)</span>
                </div>
              </div>

              <div class="input-wrapper" v-if="[3, 5].includes(currentQuestionDetail?.questionType || 0)">
                <el-input
                    v-model="userDraft"
                    type="textarea"
                    :rows="4"
                    placeholder="在此处尝试作答 (草稿仅供自测，不进行系统评分，请点击下方按钮核对答案)"
                />
              </div>
            </div>

            <div v-if="!showAnswer" class="mask-layer" @click="showAnswer = true">
              <el-icon :size="40"><View /></el-icon>
              <p>点击查看答案与解析</p>
            </div>

            <div v-else class="answer-section">
              <el-divider content-position="left">正确答案</el-divider>
              <div class="answer-text">{{ currentQuestionDetail?.answer }}</div>

              <div v-if="[3, 5].includes(currentQuestionDetail?.questionType)" style="margin-bottom: 15px; color: #909399; font-size: 14px;">
                您的尝试：{{ userDraft || '未填写' }}
              </div>

              <el-divider content-position="left">解析</el-divider>
              <div class="desc-text" v-html="currentQuestionDetail?.description || '暂无解析'"></div>
              <el-image
                  v-if="currentQuestionDetail?.answerImageUrl"
                  :src="currentQuestionDetail.answerImageUrl"
                  style="max-width: 200px; margin-bottom: 15px;"
              />

              <div class="judgment-actions">
                <p>请自我评价：</p>
                <el-button type="danger" size="large" @click="handleJudge(false)">
                  <el-icon><CloseBold /></el-icon> 忘记了 / 做错了
                </el-button>
                <el-button type="success" size="large" @click="handleJudge(true)">
                  <el-icon><Select /></el-icon> 记得 / 做对了
                </el-button>
              </div>
            </div>
          </div>

          <el-empty v-else description="太棒了！今日暂无需要复习的题目。" />
        </el-card>
      </el-col>

      <el-col :span="6" :xs="24" v-if="!isFinished && reviewList.length > 0">
        <el-card class="sidebar-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>题目列表 ({{ reviewList.length }})</span>
            </div>
          </template>

          <div class="question-nav-container">
            <div v-for="(group, typeIndex) in groupedQuestions" :key="typeIndex" class="nav-group">
              <div class="nav-group-title">{{ group.typeName }} ({{ group.list.length }})</div>
              <div class="nav-grid">
                <div
                    v-for="item in group.list"
                    :key="item.globalIndex"
                    class="nav-item"
                    :class="{
                    'active': currentIndex === item.globalIndex,
                    'done': false // 如果有记录已完成状态可加
                  }"
                    @click="jumpToQuestion(item.globalIndex)"
                >
                  {{ item.globalIndex + 1 }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { fetchDailyReviewList, submitReviewResult } from '@/api/review';
import { fetchWrongRecordDetail } from '@/api/wrongRecord';
import { ElMessage } from 'element-plus';
import { View, Select, CloseBold } from '@element-plus/icons-vue';
import type { WrongRecordVO } from '@/api/wrongRecord';

const loading = ref(true);
const reviewList = ref<WrongRecordVO[]>([]);
const currentIndex = ref(0);
const showAnswer = ref(false);
const currentQuestionDetail = ref<any>(null);
const userDraft = ref<any>('');

const isFinished = computed(() => reviewList.value.length > 0 && currentIndex.value >= reviewList.value.length);
const totalCount = computed(() => reviewList.value.length);
const currentRecord = computed(() => reviewList.value[currentIndex.value]);
const progressPercentage = computed(() => {
  if (totalCount.value === 0) return 100;
  return Math.floor(((currentIndex.value + 1) / totalCount.value) * 100);
});

// --- 新增：按题型分组的计算属性 ---
const groupedQuestions = computed(() => {
  const groups: Record<number, { typeName: string, list: any[] }> = {};

  reviewList.value.forEach((record, index) => {
    // 如果 record.questionType 为空，归类为 0 (未知)
    const type = record.questionType || 0;
    if (!groups[type]) {
      groups[type] = {
        typeName: getQuestionTypeName(type),
        list: []
      };
    }
    groups[type].list.push({
      ...record,
      globalIndex: index // 记录在原始列表中的索引，用于跳转
    });
  });

  // 按 Key 排序 (单选->多选->填空...)
  return Object.keys(groups).sort().map(key => groups[parseInt(key)]);
});

// --- 新增：跳转逻辑 ---
const jumpToQuestion = async (index: number) => {
  if (index === currentIndex.value) return;
  // 切换前重置状态
  currentIndex.value = index;
  showAnswer.value = false;
  userDraft.value = '';
  await loadQuestionDetail(reviewList.value[index].id);
};

const getQuestionTypeName = (type?: number) => {
  const map: Record<number, string> = { 1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题' };
  return map[type || 0] || '未知题型';
};

const parseOptions = (optsStr: any) => {
  if (!optsStr) return [];
  try {
    return typeof optsStr === 'string' ? JSON.parse(optsStr) : optsStr;
  } catch (e) {
    return [];
  }
};

const isSelected = (key: string) => {
  if (Array.isArray(userDraft.value)) {
    return userDraft.value.includes(key);
  }
  return userDraft.value === key;
};

// 判断该选项是否是正确答案之一（用于高亮显示）
const isCorrectOption = (key: string) => {
  if (!currentQuestionDetail.value?.answer) return false;
  // 简单处理：如果答案包含该 Key
  return currentQuestionDetail.value.answer.includes(key);
};

// --- 修改：点击选项的逻辑 ---
const handleOptionClick = (key: string, type: number) => {
  // 如果已经显示答案，禁止修改选择（可选）
  // if (showAnswer.value) return;

  if (type === 2) {
    // 多选：正常切换，不自动展开（多选需要选完所有项）
    let current = Array.isArray(userDraft.value) ? userDraft.value : [];
    if (current.includes(key)) {
      userDraft.value = current.filter((k: string) => k !== key);
    } else {
      userDraft.value = [...current, key].sort();
    }
  } else {
    // 单选/判断：选中并自动展开答案
    userDraft.value = key;
    // 【核心需求实现】：点击即展开解析
    if (!showAnswer.value) {
      showAnswer.value = true;
    }
  }
};

onMounted(async () => {
  await loadReviewList();
});

const loadReviewList = async () => {
  loading.value = true;
  try {
    const res = await fetchDailyReviewList();
    if (res.code === 200) {
      reviewList.value = res.data;
      if (reviewList.value.length > 0) {
        await loadQuestionDetail(reviewList.value[0].id);
      }
    }
  } finally {
    loading.value = false;
  }
};

const loadQuestionDetail = async (recordId: number) => {
  const res = await fetchWrongRecordDetail(recordId);
  if (res.code === 200) {
    currentQuestionDetail.value = res.data;
    // 重置草稿
    userDraft.value = currentQuestionDetail.value.questionType === 2 ? [] : '';
  }
};

const handleJudge = async (isCorrect: boolean) => {
  const recordId = currentRecord.value.id;
  const nextIndex = currentIndex.value + 1;

  try {
    const res = await submitReviewResult(recordId, isCorrect);
    ElMessage({
      type: isCorrect ? 'success' : 'warning',
      message: res.data,
      duration: 2000
    });

    if (nextIndex < totalCount.value) {
      // 自动跳转下一题
      jumpToQuestion(nextIndex);
    } else {
      currentIndex.value = nextIndex; // 触发完成状态
    }
  } catch (e) {
    ElMessage.error('提交失败，请重试');
  }
};
</script>

<style scoped>
.review-container { padding: 24px; display: flex; justify-content: center; }
/* 左侧卡片 */
.review-card { min-height: 600px; display: flex; flex-direction: column; }
.header-flex { display: flex; justify-content: space-between; align-items: center; }
.question-area { margin-top: 20px; }
.question-content { font-size: 18px; line-height: 1.6; margin: 30px 0; font-weight: 500; }
.progress-bar { margin-bottom: 20px; }

/* 右侧导航栏样式 */
.sidebar-card { position: sticky; top: 20px; }
.question-nav-container { max-height: 500px; overflow-y: auto; }
.nav-group { margin-bottom: 15px; }
.nav-group-title { font-size: 14px; font-weight: bold; color: #606266; margin-bottom: 8px; padding-left: 4px; border-left: 3px solid #409eff; }
.nav-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; }
.nav-item {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  transition: all 0.2s;
}
.nav-item:hover { border-color: #409eff; color: #409eff; }
.nav-item.active { background-color: #409eff; color: #fff; border-color: #409eff; }

/* 遮罩层 */
.mask-layer {
  height: 150px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
  border: 2px dashed #e4e7ed;
  margin-top: 30px;
}
.mask-layer:hover { background: #ecf5ff; color: #409eff; border-color: #409eff; }

/* 答案解析区 */
.answer-section {
  background: #f0f9eb;
  padding: 20px;
  border-radius: 8px;
  animation: fadeIn 0.5s;
  margin-top: 30px;
}
.answer-text { font-size: 16px; font-weight: bold; color: #67c23a; margin-bottom: 15px; }
.desc-text { font-size: 14px; color: #606266; line-height: 1.5; margin-bottom: 20px; }

.judgment-actions { text-align: center; margin-top: 30px; }
.judgment-actions p { margin-bottom: 15px; color: #606266; font-size: 14px; }
.judgment-actions .el-button { width: 160px; margin: 0 15px; }

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.finished-state { padding: 40px; text-align: center; }

/* 选项样式优化 */
.options-wrapper { margin-top: 20px; display: flex; flex-direction: column; gap: 12px; }
.review-option-item {
  padding: 12px 16px;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 15px;
  color: #606266;
  display: flex;
  align-items: flex-start;
  transition: all 0.2s;
  cursor: pointer;
  position: relative;
}
.review-option-item:hover { border-color: #409eff; background-color: #ecf5ff; }
.review-option-item.selected { background-color: #ecf5ff; border-color: #409eff; color: #409eff; font-weight: bold; }

/* 自动展开后的正确/错误样式 */
.review-option-item.is-correct { border-color: #67c23a; background-color: #f0f9eb; color: #67c23a; }
.review-option-item.is-wrong { border-color: #f56c6c; background-color: #fef0f0; color: #f56c6c; }

.status-icon { position: absolute; right: 15px; top: 12px; font-size: 18px; }
.status-icon.success { color: #67c23a; }
.status-icon.error { color: #f56c6c; }

.opt-key { font-weight: bold; margin-right: 10px; min-width: 20px; }
.input-wrapper { margin-top: 20px; }
</style>