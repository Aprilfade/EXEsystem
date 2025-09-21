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
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="handleReview(scope.row)">重新练习</el-button>
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
      currentWrongRecord.value = record;
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
</style>