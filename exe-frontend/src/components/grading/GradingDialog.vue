<template>
  <el-dialog
      :model-value="visible"
      title="试卷批阅"
      width="80%"
      top="5vh"
      @close="$emit('update:visible', false)"
  >
    <div class="grading-container" v-loading="loading">
      <div class="paper-view" v-if="paper">
        <div v-if="paper.paperType === 2">
          <div v-for="(img, idx) in paper.paperImages" :key="idx">
            <el-image :src="img.imageUrl" style="width: 100%; margin-bottom: 10px; border: 1px solid #eee;" />
          </div>
        </div>
        <div v-else>
          <el-empty description="普通试卷已自动判分，如需人工干预请直接修改分数" />
        </div>
      </div>

      <div class="grading-panel">
        <h3>学生作答</h3>
        <div class="answers-grid">
          <div v-for="(val, key) in userAnswers" :key="key" class="ans-item">
            <span class="key">{{ key }}.</span>
            <span class="val">{{ val }}</span>
          </div>
        </div>

        <el-divider />

        <h3>评分</h3>
        <el-form>
          <el-form-item label="总分修改为">
            <el-input-number v-model="newScore" :min="0" :max="paper?.totalScore || 100" />
          </el-form-item>
          <el-form-item label="修改原因" v-if="showReasonInput">
            <el-input
              v-model="reason"
              type="textarea"
              :rows="3"
              placeholder="分数变化较大，请填写修改原因（用于审批）"
              maxlength="500"
              show-word-limit
            />
            <div style="color: #E6A23C; font-size: 12px; margin-top: 4px;">
              <el-icon><InfoFilled /></el-icon>
              提示：分数变化超过审批阈值，需要提交审批
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submitGrade">确认提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { fetchExamHistoryDetail } from '@/api/studentAuth'; // 复用这个API即可，虽然是student包下的，但逻辑通用
import request from '@/utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { InfoFilled } from '@element-plus/icons-vue';

const props = defineProps<{ visible: boolean; resultId: number }>();
const emit = defineEmits(['update:visible', 'success']);

const loading = ref(true);
const paper = ref<any>(null);
const userAnswers = ref<Record<string, string>>({});
const newScore = ref(0);
const originalScore = ref(0); // 保存原始分数
const reason = ref(''); // 修改原因

// 计算是否需要显示原因输入框（分数变化超过10分或20%）
const showReasonInput = computed(() => {
  const scoreChange = Math.abs(newScore.value - originalScore.value);
  if (scoreChange >= 10) return true;

  if (originalScore.value !== 0) {
    const changePercentage = (scoreChange / Math.abs(originalScore.value)) * 100;
    if (changePercentage >= 20) return true;
  }

  return false;
});

const loadDetail = async () => {
  loading.value = true;
  try {
    // 注意：这里我们暂时复用 /student/history/{id} 接口。
    // 在实际生产中，应该为教师单独写一个 /api/v1/exam-results/{id} 接口，
    // 因为 /student/ 接口会校验当前登录人必须是该试卷的所有者（学生），教师调用会报 "无权访问"。
    // **修正**：我们需要修改后端 StudentDataController.getExamResultDetail 的权限，
    // 或者为教师新增接口。为简单起见，建议在 StudentDataController 中放开该接口给 TEACHER 角色，或者新建接口。
    // 鉴于您之前已经复制了 StudentDataController，我们假设您已经在 BizExamResultController 中添加了类似 getResultDetail 的逻辑。
    // 这里我们假设您已经在 BizExamResultController 实现了 detail 接口：

    // 使用新接口 (需要您在 BizExamResultController 中实现，代码见下方补充)
    const res = await request.get(`/api/v1/exam-results/${props.resultId}`);

    if (res.code === 200) {
      paper.value = res.data.paper;
      newScore.value = res.data.examResult.score || 0;
      originalScore.value = res.data.examResult.score || 0; // 保存原始分数
      if (res.data.examResult.userAnswers) {
        userAnswers.value = JSON.parse(res.data.examResult.userAnswers);
      }
    }
  } catch (e) {
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
};

const submitGrade = async () => {
  try {
    // 检查是否需要审批但没有填写原因
    if (showReasonInput.value && !reason.value.trim()) {
      ElMessage.warning('分数变化较大，请填写修改原因');
      return;
    }

    const payload: any = { score: newScore.value };
    if (showReasonInput.value) {
      payload.reason = reason.value;
    }

    const res = await request.put(`/api/v1/exam-results/${props.resultId}/score`, payload);

    // 检查是否需要审批
    if (res.data && res.data.needsApproval) {
      await ElMessageBox.alert(
        res.data.message || '分数变化超过审批阈值，已提交审批，请等待审核',
        '审批提示',
        {
          confirmButtonText: '我知道了',
          type: 'warning',
          callback: () => {
            emit('success');
            emit('update:visible', false);
          }
        }
      );
    } else {
      ElMessage.success('批阅完成');
      emit('success');
      emit('update:visible', false);
    }
  } catch (e: any) {
    console.error('保存失败', e);
    ElMessage.error(e.response?.data?.msg || e.message || '提交失败');
  }
};

onMounted(loadDetail);
</script>

<style scoped>
.grading-container { display: flex; height: 60vh; gap: 20px; }
.paper-view { flex: 2; overflow-y: auto; border: 1px solid #eee; padding: 10px; }
.grading-panel { flex: 1; overflow-y: auto; padding: 10px; background: #f9f9f9; }
.answers-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.ans-item { background: #fff; padding: 8px; border-radius: 4px; border: 1px solid #e4e7ed; }
.ans-item .key { font-weight: bold; color: #909399; margin-right: 5px; }
.ans-item .val { color: #409EFF; font-weight: bold; }
</style>