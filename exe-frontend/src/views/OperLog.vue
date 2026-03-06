<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div>
          <h2>操作日志</h2>
          <p>监控系统关键操作，追踪数据变更记录</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" @click="handleRefresh" :loading="loading">刷新</el-button>
        <el-button icon="Download" @click="handleExport" type="primary" plain>导出</el-button>
        <el-button v-if="isSuperAdmin" type="danger" plain icon="Delete" @click="handleClean">清空日志</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-total">
          <div class="stat-icon">
            <el-icon :size="32"><DataLine /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总日志数</div>
            <div class="stat-value">{{ total }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-icon">
            <el-icon :size="32"><SuccessFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">成功操作</div>
            <div class="stat-value">{{ stats.successCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-error">
          <div class="stat-icon">
            <el-icon :size="32"><CircleCloseFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">失败操作</div>
            <div class="stat-value">{{ stats.errorCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-rate">
          <div class="stat-icon">
            <el-icon :size="32"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">成功率</div>
            <div class="stat-value">{{ successRate }}%</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="content-card">
      <!-- 搜索表单 -->
      <div class="content-header">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="系统模块">
            <el-input v-model="queryParams.title" placeholder="请输入模块标题" clearable @keyup.enter="handleQuery" style="width: 180px" />
          </el-form-item>
          <el-form-item label="操作人员">
            <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable @keyup.enter="handleQuery" style="width: 150px" />
          </el-form-item>
          <el-form-item label="业务类型">
            <el-select v-model="queryParams.businessType" placeholder="请选择" clearable style="width: 130px">
              <el-option v-for="(label, value) in businessTypeMap" :key="value" :label="label" :value="parseInt(value)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 100px">
              <el-option label="成功" :value="0" />
              <el-option label="失败" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="handleDateChange"
                style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="logList" style="width: 100%; margin-top: 16px;" stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="title" label="系统模块" width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="module-cell">
              <el-icon class="module-icon"><Notebook /></el-icon>
              <span>{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getBusinessTypeTag(scope.row.businessType)" effect="dark">
              {{ businessTypeMap[scope.row.businessType] || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="请求方式" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getMethodTag(scope.row.requestMethod)" size="small">
              {{ scope.row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operName" label="操作人员" width="120" show-overflow-tooltip>
          <template #default="scope">
            <div class="user-cell">
              <el-icon><User /></el-icon>
              <span>{{ scope.row.operName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operIp" label="主机地址" width="140" show-overflow-tooltip>
          <template #default="scope">
            <div class="ip-cell">
              <el-icon><Location /></el-icon>
              <span>{{ scope.row.operIp }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="success" effect="dark">
              <el-icon><SuccessFilled /></el-icon>
              成功
            </el-tag>
            <el-tag v-else type="danger" effect="dark">
              <el-icon><CircleCloseFilled /></el-icon>
              失败
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="180" sortable>
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(scope.row.operTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link icon="View" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          class="pagination"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="getList"
          @current-change="getList"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="800px" class="detail-dialog">
      <el-descriptions :column="2" border v-if="currentLog" class="log-descriptions">
        <el-descriptions-item label="日志编号" :span="1">
          <el-tag>ID: {{ currentLog.id }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作状态" :span="1">
          <el-tag v-if="currentLog.status === 0" type="success" effect="dark">
            <el-icon><SuccessFilled /></el-icon>
            成功
          </el-tag>
          <el-tag v-else type="danger" effect="dark">
            <el-icon><CircleCloseFilled /></el-icon>
            失败
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作模块" :span="2">
          {{ currentLog.title }}
        </el-descriptions-item>
        <el-descriptions-item label="业务类型" :span="1">
          <el-tag :type="getBusinessTypeTag(currentLog.businessType)">
            {{ businessTypeMap[currentLog.businessType] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求方式" :span="1">
          <el-tag :type="getMethodTag(currentLog.requestMethod)">
            {{ currentLog.requestMethod }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人员" :span="1">
          <div class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ currentLog.operName }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="主机地址" :span="1">
          <div class="ip-info">
            <el-icon><Location /></el-icon>
            <span>{{ currentLog.operIp }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">
          <div class="time-info">
            <el-icon><Clock /></el-icon>
            <span>{{ formatTime(currentLog.operTime) }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">
          <div class="url-box">
            <el-tag type="info" size="small">{{ currentLog.requestMethod }}</el-tag>
            <span class="url-text">{{ currentLog.operUrl }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="操作方法" :span="2">
          <div class="method-box">{{ currentLog.method }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="code-box">
            <pre>{{ formatJson(currentLog.operParam) }}</pre>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2" v-if="currentLog.status === 0">
          <div class="code-box success-box">
            <pre>{{ formatJson(currentLog.jsonResult) }}</pre>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.status === 1">
          <div class="code-box error-box">
            <pre>{{ currentLog.errorMsg }}</pre>
          </div>
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
import {
  Delete, Search, Refresh, View, Download, Document, DataLine,
  SuccessFilled, CircleCloseFilled, TrendCharts, Notebook,
  User, Location, Clock
} from '@element-plus/icons-vue';
import * as XLSX from 'xlsx';

const authStore = useAuthStore();
const isSuperAdmin = computed(() => authStore.isSuperAdmin);

const logList = ref<OperLog[]>([]);
const total = ref(0);
const loading = ref(false);
const detailVisible = ref(false);
const currentLog = ref<OperLog | null>(null);
const dateRange = ref<[Date, Date] | null>(null);

// 统计数据
const stats = computed(() => {
  const successCount = logList.value.filter(log => log.status === 0).length;
  const errorCount = logList.value.filter(log => log.status === 1).length;
  return { successCount, errorCount };
});

const successRate = computed(() => {
  if (total.value === 0) return 0;
  return ((stats.value.successCount / total.value) * 100).toFixed(1);
});

// 业务类型映射
const businessTypeMap: Record<number, string> = {
  0: '其他', 1: '新增', 2: '修改', 3: '删除', 4: '授权',
  5: '导出', 6: '导入', 7: '强退', 8: '清空'
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
  dateRange.value = null;
  handleQuery();
};

const handleRefresh = () => {
  getList();
  ElMessage.success('刷新成功');
};

const handleDateChange = (dates: [Date, Date] | null) => {
  if (dates) {
    // 将日期转换为查询参数（如果后端支持）
    // queryParams.startDate = dates[0].toISOString();
    // queryParams.endDate = dates[1].toISOString();
    handleQuery();
  }
};

const handleDetail = (row: OperLog) => {
  currentLog.value = row;
  detailVisible.value = true;
};

const handleExport = () => {
  try {
    if (logList.value.length === 0) {
      ElMessage.warning('暂无数据可导出');
      return;
    }

    // 创建工作簿
    const workbook = XLSX.utils.book_new();

    // 统计信息
    const summaryData = [
      ['操作日志统计报告'],
      [''],
      ['导出时间', new Date().toLocaleString('zh-CN')],
      ['总记录数', total.value],
      ['成功操作', stats.value.successCount],
      ['失败操作', stats.value.errorCount],
      ['成功率', `${successRate.value}%`],
      [''],
      ['详细数据']
    ];

    // 详细数据表头
    const headers = ['序号', '系统模块', '业务类型', '请求方式', '操作人员', '主机地址', '操作状态', '操作时间'];

    // 详细数据
    const detailData = logList.value.map((log, index) => [
      index + 1,
      log.title,
      businessTypeMap[log.businessType] || '其他',
      log.requestMethod,
      log.operName,
      log.operIp,
      log.status === 0 ? '成功' : '失败',
      formatTime(log.operTime)
    ]);

    // 合并统计和详细数据
    const allData = [...summaryData, headers, ...detailData];

    // 创建工作表
    const worksheet = XLSX.utils.aoa_to_sheet(allData);

    // 设置列宽
    worksheet['!cols'] = [
      { wch: 8 },   // 序号
      { wch: 20 },  // 系统模块
      { wch: 12 },  // 业务类型
      { wch: 12 },  // 请求方式
      { wch: 15 },  // 操作人员
      { wch: 18 },  // 主机地址
      { wch: 10 },  // 操作状态
      { wch: 20 }   // 操作时间
    ];

    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(workbook, worksheet, '操作日志');

    // 生成文件名
    const fileName = `操作日志_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.xlsx`;

    // 导出
    XLSX.writeFile(workbook, fileName);

    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  }
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
    case 1: return 'success';
    case 2: return 'warning';
    case 3: return 'danger';
    case 4: return 'primary';
    case 5: case 6: return 'info';
    default: return 'info';
  }
};

const getMethodTag = (method: string) => {
  switch (method?.toUpperCase()) {
    case 'GET': return 'success';
    case 'POST': return 'primary';
    case 'PUT': return 'warning';
    case 'DELETE': return 'danger';
    default: return 'info';
  }
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(0, 19);
};

const formatJson = (jsonStr: string) => {
  if (!jsonStr) return '';
  try {
    const obj = JSON.parse(jsonStr);
    return JSON.stringify(obj, null, 2);
  } catch {
    return jsonStr;
  }
};

onMounted(getList);
</script>

<style scoped>
/* 页面容器 */
.page-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #303133;
}

.page-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.stat-total::before { background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); }
.stat-total .stat-icon {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}
.stat-total .stat-value { color: #667eea; }

.stat-success::before { background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%); }
.stat-success .stat-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(133, 206, 97, 0.1) 100%);
  color: #67c23a;
}
.stat-success .stat-value { color: #67c23a; }

.stat-error::before { background: linear-gradient(90deg, #f56c6c 0%, #f78989 100%); }
.stat-error .stat-icon {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.1) 0%, rgba(247, 137, 137, 0.1) 100%);
  color: #f56c6c;
}
.stat-error .stat-value { color: #f56c6c; }

.stat-rate::before { background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%); }
.stat-rate .stat-icon {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(235, 181, 99, 0.1) 100%);
  color: #e6a23c;
}
.stat-rate .stat-value { color: #e6a23c; }

/* 内容卡片 */
.content-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

.content-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.search-form {
  margin: 0;
}

.search-form .el-form-item {
  margin-bottom: 12px;
  margin-right: 16px;
}

/* 表格单元格样式 */
.module-cell, .user-cell, .ip-cell, .time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.module-icon {
  color: #409eff;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 详情对话框 */
.detail-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.log-descriptions {
  margin-bottom: 0;
}

.user-info, .ip-info, .time-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.url-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.url-text {
  font-family: monospace;
  font-size: 13px;
  word-break: break-all;
}

.method-box {
  font-family: monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  word-break: break-all;
}

.code-box {
  background: #f5f7fa;
  border-radius: 6px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
}

.code-box pre {
  margin: 0;
  padding: 12px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.success-box {
  background: #f0f9ff;
  border-color: #d4e8ff;
}

.success-box pre {
  color: #409eff;
}

.error-box {
  background: #fef0f0;
  border-color: #fde2e2;
}

.error-box pre {
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .search-form {
    display: flex;
    flex-direction: column;
  }

  .search-form .el-form-item {
    margin-right: 0;
    width: 100%;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-value {
    font-size: 24px;
  }
}
</style>
