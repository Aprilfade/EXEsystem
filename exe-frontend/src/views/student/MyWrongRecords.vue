<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <h2>我的错题本</h2>
      </template>
      <ResponsiveTable :data="wrongRecords" :loading="loading">
        <!-- 桌面端表格列 -->
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="questionContent" label="题干" show-overflow-tooltip />
        <el-table-column prop="paperName" label="来源试卷" width="150" />
        <el-table-column prop="wrongReason" label="错误原因" width="120" />
        <el-table-column prop="createTime" label="记录时间" width="180" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button link type="primary" @click="handleReview(scope.row)">详情</el-button>
            <el-button link type="warning" :icon="MagicStick" @click="handleAiAnalysis(scope.row)">AI 解析</el-button>
          </template>
        </el-table-column>

        <!-- 移动端卡片插槽 -->
        <template #card="{ row, index }">
          <div class="wrong-record-card">
            <!-- 卡片头部 -->
            <div class="card-header">
              <el-tag type="info" size="small">{{ index + 1 }}</el-tag>
              <el-tag type="warning" size="small">{{ row.paperName }}</el-tag>
            </div>

            <!-- 题目内容 -->
            <div class="card-question">
              <div class="question-label">题干：</div>
              <div class="question-text text-ellipsis-3">{{ row.questionContent }}</div>
            </div>

            <!-- 错误原因 -->
            <div class="card-reason">
              <span class="reason-label">错误原因：</span>
              <span class="reason-text">{{ row.wrongReason }}</span>
            </div>

            <!-- 时间 -->
            <div class="card-time">
              <el-icon><Clock /></el-icon>
              <span>{{ row.createTime }}</span>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <el-button type="primary" size="small" @click="handleReview(row)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button type="warning" size="small" :icon="MagicStick" @click="handleAiAnalysis(row)">
                AI 解析
              </el-button>
            </div>
          </div>
        </template>
      </ResponsiveTable>

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
      <div v-if="prerequisitePoints.length > 0" class="prerequisite-box">
        <div class="box-title">💡 知识链溯源</div>
        <p>检测到该题涉及的知识点 <strong>{{ currentKpName }}</strong> 掌握不牢。</p>
        <p>建议优先复习其前置基础：</p>
        <div class="tag-group">
          <el-tag
              v-for="kp in prerequisitePoints"
              :key="kp.id"
              type="warning"
              effect="dark"
              @click="goToKpReview(kp)"
              style="cursor: pointer; margin-right: 8px;"
          >
            {{ kp.name }} <el-icon><Right /></el-icon>
          </el-tag>
        </div>
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
import { Clock, View } from '@element-plus/icons-vue';
import ResponsiveTable from '@/components/common/ResponsiveTable.vue';
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
import { fetchPrerequisites } from '@/api/knowledgePoint';



const md = new MarkdownIt();
const prerequisitePoints = ref<any[]>([]);
const currentKpName = ref('');
// 当打开错题详情时调用
// 【修改】完整的加载前置知识点逻辑
const loadPrerequisites = async () => {
  // 1. 从当前详情中的题目对象获取关联知识点
  // reviewQuestion 是我们在 handleReview 中赋值的题目详情
  if (!reviewQuestion.value?.knowledgePointIds || reviewQuestion.value.knowledgePointIds.length === 0) {
    return;
  }

  // 2. 取第一个知识点ID作为主知识点进行溯源
  const kpId = reviewQuestion.value.knowledgePointIds[0];

  if (!kpId) return;

  try {
    const res = await fetchPrerequisites(kpId);
    if (res.code === 200) {
      prerequisitePoints.value = res.data;
    }
  } catch (e) {
    console.error("加载知识图谱失败", e);
  }
};


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

      currentWrongRecord.value = {
        ...record,
        wrongAnswer: (res.data as any).wrongAnswer || record.wrongAnswer
      };

      // 【新增】在这里调用加载前置知识点
      prerequisitePoints.value = []; // 先清空旧数据
      await loadPrerequisites();

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
/* === 移动端错题卡片样式 === */
.wrong-record-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-header {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.card-question {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.question-label {
  font-weight: 600;
  color: #606266;
  font-size: 13px;
  margin-bottom: 6px;
}

.question-text {
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
}

.card-reason {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  background-color: #fef0f0;
  border-left: 3px solid #f56c6c;
  border-radius: 4px;
}

.reason-label {
  font-weight: 600;
  color: #f56c6c;
  font-size: 13px;
  flex-shrink: 0;
}

.reason-text {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

.card-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 12px;
}

.card-time .el-icon {
  font-size: 14px;
}

.card-actions {
  display: flex;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #e4e7ed;
}

.card-actions .el-button {
  flex: 1;
}

/* 文本截断工具类 */
.text-ellipsis-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>