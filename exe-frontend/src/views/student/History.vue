<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <h2>考试历史记录</h2>
      </template>
      <el-table :data="historyList" v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="paperName" label="试卷名称" />
        <el-table-column label="得分" width="150" align="center">
          <template #default="scope">
            <span class="score-text">{{ scope.row.score }}</span>
            <span class="total-text"> / {{ scope.row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="考试时间" width="200" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row.id)">查看详情</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { fetchExamHistory } from '@/api/studentAuth';

const router = useRouter();
const loading = ref(false);
const historyList = ref([]);
const total = ref(0);
const queryParams = reactive({ current: 1, size: 10 });

const getList = async () => {
  loading.value = true;
  try {
    const res = await fetchExamHistory(queryParams);
    if (res.code === 200) {
      historyList.value = res.data;
      total.value = res.total;
    }
  } finally {
    loading.value = false;
  }
};

const viewDetail = (id: number) => {
  router.push(`/student/history/${id}`);
};

onMounted(getList);
</script>

<style scoped>
.page-container { padding: 24px; }
.score-text { font-weight: bold; color: #409eff; font-size: 16px; }
.total-text { color: #909399; font-size: 12px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>