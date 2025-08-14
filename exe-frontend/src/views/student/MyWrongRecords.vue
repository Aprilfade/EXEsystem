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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchWrongRecordList } from '@/api/wrongRecord'; // 复用API
import type { WrongRecordVO } from '@/api/wrongRecord';
import request from '@/utils/request';

const wrongRecords = ref<WrongRecordVO[]>([]);
const loading = ref(true);

const getMyRecords = async () => {
  loading.value = true;
  try {
    // 这里我们直接使用封装好的request，因为它会自动携带token
    const res = await request({
      // 【核心修改】更新URL，指向新的学生专用接口
      url: '/api/v1/student/my-wrong-records',
      method: 'get',
    });
    if (res.code === 200) {
      wrongRecords.value = res.data;
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
</style>