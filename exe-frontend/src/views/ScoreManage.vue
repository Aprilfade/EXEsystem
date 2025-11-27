<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成绩管理 & 批阅</h2>
    </div>

    <el-card shadow="never">
      <div class="toolbar" style="margin-bottom: 20px;">
        <el-input v-model="queryParams.paperName" placeholder="试卷名称" style="width: 200px; margin-right: 10px;" />
        <el-input v-model="queryParams.studentName" placeholder="学生姓名" style="width: 200px; margin-right: 10px;" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="paperName" label="试卷名称" />
        <el-table-column label="学生ID" prop="studentId" width="120" />
        <el-table-column label="提交时间" prop="createTime" width="180" />
        <el-table-column label="切屏次数" prop="violationCount" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.violationCount > 0 ? 'danger' : 'success'">{{ row.violationCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前得分" prop="score" width="120">
          <template #default="{ row }">
            <span style="font-weight: bold; font-size: 16px; color: #409EFF;">{{ row.score }}</span>
            <span style="color: #999;"> / {{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openGrading(row)">批阅 / 改分</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          style="margin-top: 20px; justify-content: flex-end;"
          background
          layout="total, prev, pager, next"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          @current-change="fetchData"
      />
    </el-card>

    <grading-dialog v-if="gradingVisible" v-model:visible="gradingVisible" :result-id="currentResultId" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import request from '@/utils/request';
import GradingDialog from '@/components/grading/GradingDialog.vue';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const gradingVisible = ref(false);
const currentResultId = ref<number>(0);

const queryParams = reactive({
  current: 1,
  size: 10,
  paperName: '',
  studentName: ''
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/api/v1/exam-results', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data;
      total.value = res.total;
    }
  } finally {
    loading.value = false;
  }
};

const openGrading = (row: any) => {
  currentResultId.value = row.id;
  gradingVisible.value = true;
};

onMounted(fetchData);
</script>