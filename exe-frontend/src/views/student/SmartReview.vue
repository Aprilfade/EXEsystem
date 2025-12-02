<template>
  <div class="review-container">
    <el-card class="review-card" v-loading="loading">
      <template #header>
        <div class="header-flex">
          <h2>🧠 智能复习模式</h2>
          <el-tag type="success" size="large" effect="dark">
            今日待复习: {{ reviewList.length }} 题
          </el-tag>
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
              :format="() => `${currentIndex + 1}/${totalCount}`"
          />
        </div>

        <div class="question-content">
          <div class="q-text" v-html="currentQuestionDetail?.content"></div>
          <el-image
              v-if="currentQuestionDetail?.imageUrl"
              :src="currentQuestionDetail.imageUrl"
              style="max-height: 200px; margin: 10px 0;"
              fit="contain"
          />

          <div class="options-wrapper" v-if="[1, 2].includes(currentQuestionDetail?.questionType)">
            <div
                v-for="opt in parseOptions(currentQuestionDetail?.options)"
                :key="opt.key"
                class="review-option-item"
            >
              <span class="opt-key">{{ opt.key }}.</span>
              <span class="opt-val">{{ opt.value }}</span>
            </div>
          </div>
        </div>
        <div v-if="!showAnswer" class="mask-layer" @click="showAnswer = true">
          <el-icon :size="40"><View /></el-icon>
          <p>点击查看答案与解析</p>
        </div>

        <div v-else class="answer-section">
          <el-divider content-position="left">正确答案</el-divider>
          <div class="answer-text">{{ currentQuestionDetail?.answer }}</div>

          <el-divider content-position="left">解析</el-divider>
          <div class="desc-text">{{ currentQuestionDetail?.description || '暂无解析' }}</div>

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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { fetchDailyReviewList, submitReviewResult } from '@/api/review';
import { fetchWrongRecordDetail } from '@/api/wrongRecord'; // 复用获取详情接口
import { ElMessage } from 'element-plus';
import { View, Select, CloseBold } from '@element-plus/icons-vue';
import type { WrongRecordVO } from '@/api/wrongRecord';
import type { Question } from '@/api/question';

const loading = ref(true);
const reviewList = ref<WrongRecordVO[]>([]);
const currentIndex = ref(0);
const showAnswer = ref(false);
const currentQuestionDetail = ref<Question | null>(null);

const isFinished = computed(() => reviewList.value.length > 0 && currentIndex.value >= reviewList.value.length);
const totalCount = computed(() => reviewList.value.length);
const currentRecord = computed(() => reviewList.value[currentIndex.value]);
const progressPercentage = computed(() => {
  if (totalCount.value === 0) return 100;
  return Math.floor((currentIndex.value / totalCount.value) * 100);
});
// 【新增】解析选项的函数
const parseOptions = (optsStr: any) => {
  if (!optsStr) return [];
  try {
    return typeof optsStr === 'string' ? JSON.parse(optsStr) : optsStr;
  } catch (e) {
    return [];
  }
};
// 初始化
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

// 加载题目详情（为了获取答案和解析）
const loadQuestionDetail = async (recordId: number) => {
  const res = await fetchWrongRecordDetail(recordId);
  if (res.code === 200) {
    currentQuestionDetail.value = res.data;
  }
};

// 处理评判
const handleJudge = async (isCorrect: boolean) => {
  const recordId = currentRecord.value.id;

  // 乐观更新 UI
  const nextIndex = currentIndex.value + 1;

  try {
    const res = await submitReviewResult(recordId, isCorrect);
    ElMessage({
      type: isCorrect ? 'success' : 'warning',
      message: res.data,
      duration: 2000
    });

    // 切换到下一题
    if (nextIndex < totalCount.value) {
      currentIndex.value = nextIndex;
      showAnswer.value = false;
      await loadQuestionDetail(reviewList.value[nextIndex].id);
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
.review-card { width: 100%; max-width: 800px; min-height: 500px; }
.header-flex { display: flex; justify-content: space-between; align-items: center; }
.question-area { margin-top: 20px; }
.question-content { font-size: 18px; line-height: 1.6; margin: 30px 0; font-weight: 500; }
.progress-bar { margin-bottom: 20px; }

.mask-layer {
  height: 200px;
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
}
.mask-layer:hover { background: #ecf5ff; color: #409eff; border-color: #409eff; }

.answer-section {
  background: #f0f9eb;
  padding: 20px;
  border-radius: 8px;
  animation: fadeIn 0.5s;
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
/* 【新增】复习模式选项样式 */
.options-wrapper {
  margin-top: 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-option-item {
  padding: 12px 16px;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 15px;
  color: #606266;
  display: flex;
  align-items: flex-start; /* 防止长文本对齐问题 */
  transition: all 0.2s;
}

/* 鼠标悬停效果，增加交互感 */
.review-option-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.opt-key {
  font-weight: bold;
  margin-right: 10px;
  color: #303133;
  min-width: 20px; /* 保证选项号对齐 */
}

.opt-val {
  line-height: 1.5;
}
</style>