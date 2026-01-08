<template>
  <el-dialog
    v-model="dialogVisible"
    title="成绩批阅"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="grading-content">
      <!-- 学生信息 -->
      <el-card shadow="never" class="info-card">
        <template #header>
          <span class="card-title">考试信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="试卷名称">
            {{ resultData.paperName }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ resultData.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="学生姓名">
            {{ resultData.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ resultData.studentNo }}
          </el-descriptions-item>
          <el-descriptions-item label="班级">
            {{ resultData.className }}
          </el-descriptions-item>
          <el-descriptions-item label="科目">
            {{ resultData.subjectName }}
          </el-descriptions-item>
          <el-descriptions-item label="切屏次数">
            <el-tag :type="resultData.violationCount > 5 ? 'danger' : resultData.violationCount > 2 ? 'warning' : 'success'">
              {{ resultData.violationCount }} 次
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布状态">
            <el-tag :type="resultData.published ? 'success' : 'info'">
              {{ resultData.published ? '已发布' : '未发布' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 成绩编辑 -->
      <el-card shadow="never" class="score-card">
        <template #header>
          <span class="card-title">成绩评定</span>
        </template>
        <el-form :model="formData" label-width="100px">
          <el-form-item label="当前得分">
            <div class="score-display">
              <span class="score-value" :class="getScoreClass(resultData.score)">
                {{ resultData.score }}
              </span>
              <span class="score-total">/ {{ resultData.totalScore }}</span>
              <el-progress
                :percentage="(resultData.score / resultData.totalScore * 100)"
                :color="getScoreColor(resultData.score)"
                style="margin-left: 20px; flex: 1;"
              />
            </div>
          </el-form-item>
          <el-form-item label="调整分数">
            <el-input-number
              v-model="formData.score"
              :min="0"
              :max="resultData.totalScore"
              :step="1"
              placeholder="请输入分数"
            />
            <span class="hint-text">如需调整分数，请输入新分数</span>
          </el-form-item>
          <el-form-item label="教师评语">
            <el-input
              v-model="formData.comment"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请输入教师评语（选填）"
            />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getScoreDetail, updateScore, updateComment } from '@/api/score';
import type { ExamResultDetail } from '@/api/score';

const props = defineProps<{
  visible: boolean;
  resultId: number;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  'success': [];
}>();

const dialogVisible = ref(false);
const loading = ref(false);
const submitting = ref(false);

const resultData = ref<ExamResultDetail>({
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

const formData = reactive({
  score: 0,
  comment: ''
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
      resultData.value = res.data;
      formData.score = res.data.score;
      formData.comment = res.data.comment || '';
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
 * 提交保存
 */
const handleSubmit = async () => {
  submitting.value = true;
  try {
    // 判断是否修改了分数
    const scoreChanged = formData.score !== resultData.value.score;
    // 判断是否修改了评语
    const commentChanged = formData.comment !== (resultData.value.comment || '');

    if (!scoreChanged && !commentChanged) {
      ElMessage.info('未做任何修改');
      submitting.value = false;
      return;
    }

    const promises: Promise<any>[] = [];

    // 更新分数
    if (scoreChanged) {
      promises.push(updateScore(props.resultId, formData.score));
    }

    // 更新评语
    if (commentChanged) {
      promises.push(updateComment(props.resultId, formData.comment));
    }

    const results = await Promise.all(promises);

    // 检查所有请求是否成功
    const allSuccess = results.every(res => res.code === 200);

    if (allSuccess) {
      ElMessage.success('保存成功');
      emit('success');
      handleClose();
    } else {
      ElMessage.error('保存失败');
    }
  } catch (error) {
    console.error('保存失败', error);
    ElMessage.error('保存失败');
  } finally {
    submitting.value = false;
  }
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
.grading-content {
  max-height: 70vh;
  overflow-y: auto;
}

.info-card,
.score-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.info-card:last-child,
.score-card:last-child {
  margin-bottom: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary, #303133);
}

.score-display {
  display: flex;
  align-items: center;
  gap: 10px;
}

.score-value {
  font-size: 32px;
  font-weight: 700;
}

.score-total {
  font-size: 18px;
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

.hint-text {
  margin-left: 10px;
  font-size: 12px;
  color: var(--text-color-secondary, #909399);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 深色模式适配 */
.dark .info-card,
.dark .score-card {
  background: #2d2d2d;
  border-color: #404040;
}

.dark .card-title {
  color: #e5e5e5;
}
</style>
