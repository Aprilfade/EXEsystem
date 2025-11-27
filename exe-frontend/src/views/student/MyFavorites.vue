<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <h2>我的收藏夹</h2>
      </template>
      <el-table :data="favoritesList" v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column label="题干" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            {{ stripHtml(row.content) }}
          </template>
        </el-table-column>
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ getQuestionType(row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">查看</el-button>
            <el-button type="danger" link @click="removeFav(scope.row.id)">取消收藏</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          class="pagination"
          background
          layout="total, prev, pager, next"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          @current-change="getList"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="题目详情" width="600px">
      <div v-if="currentQuestion">
        <div v-html="currentQuestion.content" class="q-content"></div>
        <div v-if="currentQuestion.imageUrl">
          <el-image :src="currentQuestion.imageUrl" style="max-width: 200px;" />
        </div>
        <el-divider>答案与解析</el-divider>
        <p><strong>答案：</strong> {{ currentQuestion.answer }}</p>
        <p><strong>解析：</strong> {{ currentQuestion.description }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { fetchMyFavorites, toggleFavorite } from '@/api/favorite';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const favoritesList = ref([]);
const total = ref(0);
const detailVisible = ref(false);
const currentQuestion = ref<any>(null);
const queryParams = reactive({ current: 1, size: 10 });

const getQuestionType = (type: number) => {
  const map: Record<number, string> = { 1: '单选', 2: '多选', 3: '填空', 4: '判断', 5: '主观' };
  return map[type] || '未知';
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await fetchMyFavorites(queryParams);
    if (res.code === 200) {
      favoritesList.value = res.data.records || res.data; // 兼容分页返回
      total.value = res.data.total || res.total;
    }
  } finally {
    loading.value = false;
  }
};
// 【新增】去除HTML标签的辅助函数，仅用于列表展示
const stripHtml = (html: string) => {
  if (!html) return '';
  const tmp = document.createElement('DIV');
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || '';
};
const viewDetail = (row: any) => {
  currentQuestion.value = row;
  detailVisible.value = true;
};

const removeFav = async (id: number) => {
  try {
    await toggleFavorite(id);
    ElMessage.success('已取消');
    getList(); // 刷新列表
  } catch(e) {}
};

onMounted(getList);
</script>

<style scoped>
.page-container { padding: 24px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.q-content { font-size: 16px; margin-bottom: 10px; line-height: 1.6; }
</style>