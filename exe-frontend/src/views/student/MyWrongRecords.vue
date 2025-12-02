<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <h2>我的错题本</h2>
      </template>
      <el-table :data="wrongRecords" v-loading="loading">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="questionContent" label="题干" show-overflow-tooltip />
        <el-table-column prop="paperName" label="来源试卷" />
        <el-table-column prop="wrongReason" label="错误原因" />
        <el-table-column prop="createTime" label="记录时间" />
        <el-table-column label="操作" width="220"> <template #default="scope">
          <el-button link type="primary" @click="handleReview(scope.row)">详情</el-button>
          <el-button link type="warning" :icon="MagicStick" @click="handleAiAnalysis(scope.row)">AI 解析</el-button>
        </template>
        </el-table-column>
      </el-table>

      <el-pagination
          class="pagination"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          @size-change="getMyRecords"
          @current-change="getMyRecords"
      />
    </el-card>

    <el-dialog v-model="aiResultVisible" title="🤖 AI 智能助教" width="600px">
      <div v-loading="aiLoading" class="ai-content">
        <div v-if="aiResponse" class="markdown-body">
          <pre style="white-space: pre-wrap; font-family: sans-serif; line-height: 1.6;">{{ aiResponse }}</pre>
        </div>
        <el-empty v-else description="正在思考中..." />
      </div>
    </el-dialog>

    <ai-key-dialog
        v-model:visible="keyDialogVisible"
        @saved="onKeySaved"
    />




    <el-dialog v-model="isReviewDialogVisible" title="错题解析" width="700px">
      <div v-if="reviewQuestion">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="题干">
            <div v-html="reviewQuestion.content"></div>
            <el-image v-if="reviewQuestion.imageUrl" :src="reviewQuestion.imageUrl" style="max-width: 200px;"/>
          </el-descriptions-item>
          <el-descriptions-item v-if="reviewQuestion.options" label="选项">
            <p v-for="option in JSON.parse(reviewQuestion.options as string)" :key="option.key">
              {{ option.key }}. {{ option.value }}
            </p>
          </el-descriptions-item>
          <el-descriptions-item label="我的答案">
            <el-tag type="danger">{{ currentWrongRecord?.wrongAnswer || '未作答' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="正确答案">
            <el-tag type="success">{{ reviewQuestion.answer }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="解析">
            <div v-html="reviewQuestion.description"></div>
            <el-image v-if="reviewQuestion.answerImageUrl" :src="reviewQuestion.answerImageUrl" style="max-width: 200px;"/>
          </el-descriptions-item>
          <el-descriptions-item label="错误原因分析">
            {{ currentWrongRecord?.wrongReason }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <div v-if="reviewQuestion">
          <el-button type="success" @click="handleMarkAsMastered">标记为已掌握</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
// 【修改】导入 reactive
import { ref, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
// 【修改】导入类型 WrongRecordPageParams
import type { WrongRecordVO, WrongRecordPageParams } from '@/api/wrongRecord';
import request from '@/utils/request';
import { fetchWrongRecordDetail, markWrongRecordAsMastered } from '@/api/wrongRecord';
import type { Question } from '@/api/question';
import { MagicStick } from '@element-plus/icons-vue'; // 记得引入图标
// 新增引入
import { useStudentAuthStore } from '@/stores/studentAuth';
import AiKeyDialog from '@/components/student/AiKeyDialog.vue';
import { analyzeQuestion } from '@/api/ai';
import MarkdownIt from 'markdown-it';
const md = new MarkdownIt();




const wrongRecords = ref<WrongRecordVO[]>([]);
const loading = ref(true);
// 【新增】分页相关的响应式变量
const total = ref(0);
const queryParams = reactive<WrongRecordPageParams>({
  current: 1,
  size: 10,
});
// 【新增】错题重练相关变量
const isReviewDialogVisible = ref(false);
const reviewQuestion = ref<Question | null>(null);
const currentWrongRecord = ref<WrongRecordVO | null>(null);


// 【修改】更新 getMyRecords 方法以支持分页
const getMyRecords = async () => {
  loading.value = true;
  try {
    const res = await request({
      url: '/api/v1/student/my-wrong-records',
      method: 'get',
      // 【新增】将分页参数传递给后端
      params: queryParams,
    });
    if (res.code === 200) {
      wrongRecords.value = res.data;
      // 【新增】更新总数
      total.value = res.total;
    }
  } catch (error) {
    ElMessage.error('加载错题本失败');
  } finally {
    loading.value = false;
  }
};
// 【新增】处理重新练习按钮
const handleReview = async (record: WrongRecordVO) => {
  if (!record.id) return;
  try {
    const res = await fetchWrongRecordDetail(record.id);
    if (res.code === 200) {
      reviewQuestion.value = res.data;

      // 【核心修复】将详情接口返回的 wrongAnswer 同步到 currentWrongRecord
      // 因为 res.data 是后端返回的完整 VO，里面包含了 wrongAnswer
      currentWrongRecord.value = {
        ...record,
        // 使用类型断言 (as any) 访问可能未在前端类型定义的字段，确保取到值
        wrongAnswer: (res.data as any).wrongAnswer || record.wrongAnswer
      };
      isReviewDialogVisible.value = true;
    }
  } catch (error) {
    ElMessage.error('加载题目详情失败');
  }
};

// 【新增】处理标记为已掌握按钮
const handleMarkAsMastered = async () => {
  if (!currentWrongRecord.value || !currentWrongRecord.value.id) return;
  await markWrongRecordAsMastered(currentWrongRecord.value.id);
  ElMessage.success('已标记为已掌握，该记录将不再显示');
  isReviewDialogVisible.value = false;
  await getMyRecords(); // 重新加载列表
};

// --- AI 相关状态 ---
const store = useStudentAuthStore();
const keyDialogVisible = ref(false);
const aiResultVisible = ref(false);
const aiLoading = ref(false);
const aiResponse = ref('');
const currentRecordForAi = ref<WrongRecordVO | null>(null); // 暂存当前操作的记录

// 点击 AI 解析按钮
const handleAiAnalysis = async (record: WrongRecordVO) => {
  // 1. 检查是否有 Key
  if (!store.aiKey) {
    currentRecordForAi.value = record; // 记住当前想操作的记录
    keyDialogVisible.value = true; // 打开设置弹窗
    return;
  }

  // 2. 执行分析
  performAiAnalysis(record);
};

// Key 设置成功后的回调
const onKeySaved = () => {
  if (currentRecordForAi.value) {
    performAiAnalysis(currentRecordForAi.value);
    currentRecordForAi.value = null;
  }
};

// 执行 AI 分析的核心逻辑
const performAiAnalysis = async (record: WrongRecordVO) => {
  aiResultVisible.value = true;
  aiLoading.value = true;
  aiResponse.value = ''; // 清空旧内容

  try {
    // 1. 获取题目详情（需要题干、正确答案、解析等完整信息）
    const detailRes = await fetchWrongRecordDetail(record.id);
    if (detailRes.code !== 200) throw new Error("获取题目详情失败");

    const question = detailRes.data;

    // 2. 调用 AI 接口
    const res = await analyzeQuestion({
      questionContent: question.content,
      studentAnswer: record.wrongAnswer || '未作答', // 这里需要后端 WrongRecordVO 返回 wrongAnswer
      correctAnswer: question.answer,
      analysis: question.description
    });

    if (res.code === 200) {
      aiResponse.value = res.data;
    }
  } catch (error: any) {
    ElMessage.error(error.message || 'AI 分析请求失败，请检查 API Key 是否正确');
    aiResultVisible.value = false;
  } finally {
    aiLoading.value = false;
  }
};
onMounted(getMyRecords);
</script>

<style scoped>
.page-container {
  padding: 24px;
}
/* 【新增】分页组件的样式 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.ai-content {
  min-height: 200px;
  padding: 10px;
  background-color: #f9f9fa;
  border-radius: 8px;
}
</style>