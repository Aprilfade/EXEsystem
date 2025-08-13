// src/views/LoginLog.vue
<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>登录日志</h2>
        <p>审计和追踪所有用户的登录与登出活动</p>
      </div>
    </div>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-input
            v-model="queryParams.username"
            placeholder="输入用户名搜索"
            size="large"
            style="width: 300px;"
            @keyup.enter="handleQuery"
            clearable
            @clear="handleQuery"
        />
      </div>

      <el-table :data="logList" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="logType" label="事件类型" align="center">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.logType)">{{ getTypeText(scope.row.logType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" />
        <el-table-column prop="logTime" label="时间" />
        <el-table-column prop="userAgent" label="客户端" show-overflow-tooltip />
      </el-table>

      <el-pagination
          class="pagination"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          @size-change="getList"
          @current-change="getList"
      />
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { fetchLoginLogList } from '@/api/log';
import type { LoginLog, LogPageParams } from '@/api/log';

const logList = ref<LoginLog[]>([]);
const total = ref(0);
const loading = ref(true);

const queryParams = reactive<LogPageParams>({
  current: 1,
  size: 10,
  username: ''
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchLoginLogList(queryParams);
    logList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.current = 1;
  getList();
};

const getTagType = (logType: string) => {
  if (logType === 'LOGIN_SUCCESS') return 'success';
  if (logType === 'LOGOUT') return 'info';
  if (logType === 'LOGIN_FAILURE') return 'danger';
  return 'primary';
};

const getTypeText = (logType: string) => {
  const map: { [key: string]: string } = {
    'LOGIN_SUCCESS': '登录成功',
    'LOGOUT': '主动登出',
    'LOGIN_FAILURE': '登录失败'
  };
  return map[logType] || '未知';
};

onMounted(getList);
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; font-size: 14px; }
.content-card { background-color: var(--bg-color-container); }
.content-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>