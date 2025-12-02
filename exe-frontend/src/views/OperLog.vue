<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>操作日志</h2>
        <p>监控系统关键操作，追踪数据变更记录</p>
      </div>
      <div v-if="isSuperAdmin">
        <el-button type="danger" plain icon="Delete" @click="handleClean">清空日志</el-button>
      </div>
    </div>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="系统模块">
            <el-input v-model="queryParams.title" placeholder="请输入模块标题" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="操作人员">
            <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="queryParams.businessType" placeholder="业务类型" clearable style="width: 120px">
              <el-option v-for="(label, value) in businessTypeMap" :key="value" :label="label" :value="parseInt(value)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="操作状态" clearable style="width: 100px">
              <el-option label="成功" :value="0" />
              <el-option label="失败" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-loading="loading" :data="logList" style="width: 100%; margin-top: 10px;">
        <el-table-column prop="id" label="日志编号" width="80" align="center" />
        <el-table-column prop="title" label="系统模块" width="150" show-overflow-tooltip />
        <el-table-column prop="businessType" label="业务类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getBusinessTypeTag(scope.row.businessType)">
              {{ businessTypeMap[scope.row.businessType] || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="请求方式" width="100" align="center" />
        <el-table-column prop="operName" label="操作人员" width="120" show-overflow-tooltip />
        <el-table-column prop="operIp" label="主机地址" width="130" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="180" sortable />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link icon="View" @click="handleDetail(scope.row)">详情</el-button>
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
          @size-change="getList"
          @current-change="getList"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="操作模块">{{ currentLog.title }} / {{ businessTypeMap[currentLog.businessType] }}</el-descriptions-item>
        <el-descriptions-item label="登录信息">{{ currentLog.operName }} / {{ currentLog.operIp }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">{{ currentLog.requestMethod }} {{ currentLog.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作方法">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <div class="code-box">{{ currentLog.operParam }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" v-if="currentLog.status === 0">
          <div class="code-box">{{ currentLog.jsonResult }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentLog.status === 1">
          <div class="code-box error">{{ currentLog.errorMsg }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { fetchOperLogList, cleanOperLog } from '@/api/log';
import type { OperLog, OperLogPageParams } from '@/api/log';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { Delete, Search, Refresh, View } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const isSuperAdmin = computed(() => authStore.isSuperAdmin);

const logList = ref<OperLog[]>([]);
const total = ref(0);
const loading = ref(true);
const detailVisible = ref(false);
const currentLog = ref<OperLog | null>(null);

// 业务类型映射 (对应后端 BusinessType 枚举)
const businessTypeMap: Record<number, string> = {
  0: '其他', 1: '新增', 2: '修改', 3: '删除', 4: '授权', 5: '导出', 6: '导入', 7: '强退', 8: '清空'
};

const queryParams = reactive<OperLogPageParams>({
  current: 1,
  size: 10,
  title: '',
  operName: '',
  status: undefined,
  businessType: undefined
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await fetchOperLogList(queryParams);
    logList.value = res.data;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.current = 1;
  getList();
};

const resetQuery = () => {
  queryParams.title = '';
  queryParams.operName = '';
  queryParams.status = undefined;
  queryParams.businessType = undefined;
  handleQuery();
};

const handleDetail = (row: OperLog) => {
  currentLog.value = row;
  detailVisible.value = true;
};

const handleClean = () => {
  ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await cleanOperLog();
    ElMessage.success('日志已清空');
    getList();
  });
};

const getBusinessTypeTag = (type: number) => {
  switch (type) {
    case 1: return 'success'; // 新增
    case 2: return 'warning'; // 修改
    case 3: return 'danger';  // 删除
    case 4: return 'primary'; // 授权
    default: return 'info';
  }
};

onMounted(getList);
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; margin-bottom: 4px; }
.page-header p { color: var(--text-color-regular); font-size: 14px; }
.content-card { background-color: var(--bg-color-container); }
.content-header { margin-bottom: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.code-box {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
  word-break: break-all;
}
.code-box.error { color: #f56c6c; background-color: #fef0f0; }
.search-form .el-form-item { margin-bottom: 10px; }
</style>