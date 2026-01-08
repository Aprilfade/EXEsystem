<template>
  <el-dialog
    v-model="dialogVisible"
    title="成绩详情"
    width="1000px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="detail-content">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="详细信息" name="detail">
          <!-- 学生信息卡片 -->
      <el-card shadow="never" class="info-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">考试信息</span>
            <el-tag :type="detailData.published ? 'success' : 'info'">
              {{ detailData.published ? '已发布' : '未发布' }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="试卷名称" :span="2">
            <el-text tag="b" size="large">{{ detailData.paperName }}</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="学生姓名">
            {{ detailData.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ detailData.studentNo }}
          </el-descriptions-item>
          <el-descriptions-item label="班级">
            {{ detailData.className }}
          </el-descriptions-item>
          <el-descriptions-item label="科目">
            {{ detailData.subjectName }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">
            {{ detailData.createTime }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 成绩统计卡片 -->
      <el-card shadow="never" class="score-card">
        <template #header>
          <span class="card-title">成绩统计</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="score-item">
              <div class="score-label">得分</div>
              <div class="score-display">
                <span class="score-value" :class="getScoreClass(detailData.score)">
                  {{ detailData.score }}
                </span>
                <span class="score-total">/ {{ detailData.totalScore }}</span>
              </div>
              <el-progress
                :percentage="(detailData.score / detailData.totalScore * 100)"
                :color="getScoreColor(detailData.score)"
                :stroke-width="20"
                style="margin-top: 10px;"
              />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="score-item">
              <div class="score-label">切屏次数</div>
              <div class="violation-display">
                <el-statistic :value="detailData.violationCount" suffix="次">
                  <template #prefix>
                    <el-icon :color="detailData.violationCount > 5 ? '#F56C6C' : detailData.violationCount > 2 ? '#E6A23C' : '#67C23A'">
                      <WarningFilled v-if="detailData.violationCount > 5" />
                      <Warning v-else-if="detailData.violationCount > 2" />
                      <SuccessFilled v-else />
                    </el-icon>
                  </template>
                </el-statistic>
              </div>
              <el-alert
                v-if="detailData.violationCount > 5"
                title="异常警告"
                type="error"
                :closable="false"
                style="margin-top: 10px;"
              >
                切屏次数过多，可能存在作弊行为
              </el-alert>
              <el-alert
                v-else-if="detailData.violationCount > 2"
                title="注意"
                type="warning"
                :closable="false"
                style="margin-top: 10px;"
              >
                切屏次数较多，请注意
              </el-alert>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 教师评语卡片 -->
      <el-card v-if="detailData.comment" shadow="never" class="comment-card">
        <template #header>
          <span class="card-title">教师评语</span>
        </template>
        <div class="comment-content">
          <el-text>{{ detailData.comment }}</el-text>
        </div>
      </el-card>

      <!-- 答题情况卡片 -->
      <el-card v-if="userAnswersData.length > 0" shadow="never" class="answers-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">答题情况</span>
            <el-text type="info">共 {{ userAnswersData.length }} 题</el-text>
          </div>
        </template>
        <div class="answers-list">
          <div
            v-for="(item, index) in userAnswersData"
            :key="index"
            class="answer-item"
          >
            <div class="answer-header">
              <span class="question-num">第 {{ index + 1 }} 题</span>
              <el-tag :type="item.isCorrect ? 'success' : 'danger'" size="small">
                {{ item.isCorrect ? '正确' : '错误' }}
              </el-tag>
            </div>
            <div class="answer-content">
              <div class="answer-row">
                <span class="answer-label">学生答案：</span>
                <span class="answer-text">{{ item.userAnswer || '未作答' }}</span>
              </div>
              <div v-if="!item.isCorrect" class="answer-row">
                <span class="answer-label">正确答案：</span>
                <span class="answer-text correct">{{ item.correctAnswer }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
        </el-tab-pane>

        <!-- 知识点分析Tab -->
        <el-tab-pane label="知识点分析" name="knowledge">
          <knowledge-point-analysis-table :exam-result-id="props.resultId" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleEdit">
          编辑批阅
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { WarningFilled, Warning, SuccessFilled } from '@element-plus/icons-vue';
import { getScoreDetail } from '@/api/score';
import type { ExamResultDetail } from '@/api/score';
import KnowledgePointAnalysisTable from './KnowledgePointAnalysisTable.vue';

const props = defineProps<{
  visible: boolean;
  resultId: number;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  'edit': [id: number];
}>();

const dialogVisible = ref(false);
const loading = ref(false);
const activeTab = ref('detail');

const detailData = ref<ExamResultDetail>({
  id: 0,
  paperId: 0,
  paperName: '',
  studentId: 0,
  studentName: '',
  studentNo: '',
  classId: 0,
  className: '',
  subjectId: 0,
  subjectName: '',
  score: 0,
  totalScore: 100,
  violationCount: 0,
  userAnswers: '',
  createTime: '',
  comment: '',
  published: false
});

/**
 * 解析用户答案数据
 */
const userAnswersData = computed(() => {
  if (!detailData.value.userAnswers) return [];

  try {
    const answers = JSON.parse(detailData.value.userAnswers);
    return Array.isArray(answers) ? answers : [];
  } catch (error) {
    console.error('解析答题数据失败', error);
    return [];
  }
});

/**
 * 获取成绩详情
 */
const fetchDetail = async () => {
  if (!props.resultId) return;

  loading.value = true;
  try {
    const res = await getScoreDetail(props.resultId);
    if (res.code === 200) {
      detailData.value = res.data;
    } else {
      ElMessage.error(res.message || '获取成绩详情失败');
      handleClose();
    }
  } catch (error) {
    console.error('获取成绩详情失败', error);
    ElMessage.error('获取成绩详情失败');
    handleClose();
  } finally {
    loading.value = false;
  }
};

/**
 * 编辑批阅
 */
const handleEdit = () => {
  emit('edit', props.resultId);
  handleClose();
};

/**
 * 关闭对话框
 */
const handleClose = () => {
  emit('update:visible', false);
};

/**
 * 获取分数颜色类名
 */
const getScoreClass = (score: number) => {
  if (score >= 90) return 'score-excellent';
  if (score >= 80) return 'score-good';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
};

/**
 * 获取分数进度条颜色
 */
const getScoreColor = (score: number) => {
  if (score >= 90) return '#67C23A';
  if (score >= 80) return '#409EFF';
  if (score >= 60) return '#E6A23C';
  return '#F56C6C';
};

// 监听 visible 变化
watch(() => props.visible, (val) => {
  dialogVisible.value = val;
  if (val) {
    fetchDetail();
  }
});

// 监听 dialogVisible 变化
watch(dialogVisible, (val) => {
  if (!val) {
    emit('update:visible', false);
  }
});
</script>

<style scoped>
.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.info-card,
.score-card,
.comment-card,
.answers-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.info-card:last-child,
.score-card:last-child,
.comment-card:last-child,
.answers-card:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary, #303133);
}

.score-item {
  padding: 20px;
  background: var(--bg-secondary, #f5f7fa);
  border-radius: 8px;
}

.score-label {
  font-size: 14px;
  color: var(--text-color-secondary, #909399);
  margin-bottom: 10px;
}

.score-display {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.score-value {
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
}

.score-total {
  font-size: 24px;
  color: var(--text-color-secondary, #909399);
}

.score-excellent {
  color: #67C23A;
}

.score-good {
  color: #409EFF;
}

.score-pass {
  color: #E6A23C;
}

.score-fail {
  color: #F56C6C;
}

.violation-display {
  margin-top: 10px;
}

.comment-content {
  padding: 10px;
  background: var(--bg-secondary, #f5f7fa);
  border-radius: 8px;
  line-height: 1.8;
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.answer-item {
  padding: 16px;
  background: var(--bg-secondary, #f5f7fa);
  border-radius: 8px;
  border-left: 4px solid var(--border-color, #dcdfe6);
  transition: all 0.3s;
}

.answer-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color, #dcdfe6);
}

.question-num {
  font-weight: 600;
  color: var(--text-color-primary, #303133);
}

.answer-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.answer-row {
  display: flex;
  align-items: flex-start;
}

.answer-label {
  min-width: 90px;
  font-weight: 500;
  color: var(--text-color-secondary, #606266);
}

.answer-text {
  flex: 1;
  color: var(--text-color-primary, #303133);
}

.answer-text.correct {
  color: #67C23A;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 深色模式适配 */
.dark .info-card,
.dark .score-card,
.dark .comment-card,
.dark .answers-card {
  background: #2d2d2d;
  border-color: #404040;
}

.dark .card-title {
  color: #e5e5e5;
}

.dark .score-item,
.dark .comment-content,
.dark .answer-item {
  background: #1a1a1a;
  border-color: #404040;
}
</style>
