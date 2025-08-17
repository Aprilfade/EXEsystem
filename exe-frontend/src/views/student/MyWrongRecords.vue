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
  </div>
</template>

<script setup lang="ts">
// 【修改】导入 reactive
import { ref, onMounted, reactive } from 'vue';
import { ElMessage } from 'element-plus';
// 【修改】导入类型 WrongRecordPageParams
import type { WrongRecordVO, WrongRecordPageParams } from '@/api/wrongRecord';
import request from '@/utils/request';

const wrongRecords = ref<WrongRecordVO[]>([]);
const loading = ref(true);
// 【新增】分页相关的响应式变量
const total = ref(0);
const queryParams = reactive<WrongRecordPageParams>({
  current: 1,
  size: 10,
});

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