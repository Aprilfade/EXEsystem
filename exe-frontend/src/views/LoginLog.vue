<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Key /></el-icon>
        </div>
        <div>
          <h2>登录日志</h2>
          <p>审计和追踪所有用户的登录与登出活动</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" @click="handleRefresh" :loading="loading">刷新</el-button>
        <el-button icon="Download" @click="handleExport" type="primary" plain>导出</el-button>
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
            <div class="stat-label">总登录次数</div>
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
            <div class="stat-label">成功登录</div>
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
            <div class="stat-label">失败登录</div>
            <div class="stat-value">{{ stats.failureCount }}</div>
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
          <el-form-item label="用户名">
            <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" style="width: 180px" />
          </el-form-item>
          <el-form-item label="事件类型">
            <el-select v-model="queryParams.logType" placeholder="请选择" clearable style="width: 130px">
              <el-option label="登录成功" value="LOGIN_SUCCESS" />
              <el-option label="登录失败" value="LOGIN_FAILURE" />
              <el-option label="主动登出" value="LOGOUT" />
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
        <el-table-column prop="username" label="用户名" width="150" show-overflow-tooltip>
          <template #default="scope">
            <div class="user-cell">
              <el-icon><User /></el-icon>
              <span>{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="logType" label="事件类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.logType)" effect="dark">
              <el-icon v-if="scope.row.logType === 'LOGIN_SUCCESS'"><SuccessFilled /></el-icon>
              <el-icon v-else-if="scope.row.logType === 'LOGIN_FAILURE'"><CircleCloseFilled /></el-icon>
              <el-icon v-else><SwitchButton /></el-icon>
              {{ getTypeText(scope.row.logType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="150" show-overflow-tooltip>
          <template #default="scope">
            <div class="ip-cell">
              <el-icon><Location /></el-icon>
              <span>{{ scope.row.ipAddress }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="logTime" label="登录时间" width="180" sortable>
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(scope.row.logTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userAgent" label="客户端信息" show-overflow-tooltip>
          <template #default="scope">
            <div class="agent-cell">
              <el-icon><Monitor /></el-icon>
              <span>{{ getBrowserInfo(scope.row.userAgent) }}</span>
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
    <el-dialog v-model="detailVisible" title="登录日志详情" width="700px" class="detail-dialog">
      <el-descriptions :column="2" border v-if="currentLog" class="log-descriptions">
        <el-descriptions-item label="日志编号" :span="1">
          <el-tag>ID: {{ currentLog.id }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="事件类型" :span="1">
          <el-tag :type="getTagType(currentLog.logType)" effect="dark">
            {{ getTypeText(currentLog.logType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户名" :span="2">
          <div class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ currentLog.username }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址" :span="1">
          <div class="ip-info">
            <el-icon><Location /></el-icon>
            <span>{{ currentLog.ipAddress }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="登录时间" :span="1">
          <div class="time-info">
            <el-icon><Clock /></el-icon>
            <span>{{ formatTime(currentLog.logTime) }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="客户端信息" :span="2">
          <div class="agent-box">
            <el-icon><Monitor /></el-icon>
            <span class="agent-text">{{ currentLog.userAgent }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="浏览器" :span="1">
          {{ getBrowserInfo(currentLog.userAgent) }}
        </el-descriptions-item>
        <el-descriptions-item label="操作系统" :span="1">
          {{ getOSInfo(currentLog.userAgent) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { fetchLoginLogList } from '@/api/log';
import type { LoginLog, LogPageParams } from '@/api/log';
import { ElMessage } from 'element-plus';
import {
  Key, Search, Refresh, View, Download, DataLine,
  SuccessFilled, CircleCloseFilled, TrendCharts,
  User, Location, Clock, Monitor, SwitchButton
} from '@element-plus/icons-vue';
import * as XLSX from 'xlsx';

const logList = ref<LoginLog[]>([]);
const total = ref(0);
const loading = ref(false);
const detailVisible = ref(false);
const currentLog = ref<LoginLog | null>(null);
const dateRange = ref<[Date, Date] | null>(null);

// 统计数据
const stats = computed(() => {
  const successCount = logList.value.filter(log => log.logType === 'LOGIN_SUCCESS').length;
  const failureCount = logList.value.filter(log => log.logType === 'LOGIN_FAILURE').length;
  return { successCount, failureCount };
});

const successRate = computed(() => {
  if (total.value === 0) return 0;
  return ((stats.value.successCount / total.value) * 100).toFixed(1);
});

const queryParams = reactive<LogPageParams>({
  current: 1,
  size: 10,
  username: '',
  logType: undefined
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

const resetQuery = () => {
  queryParams.username = '';
  queryParams.logType = undefined;
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

const handleDetail = (row: LoginLog) => {
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
      ['登录日志统计报告'],
      [''],
      ['导出时间', new Date().toLocaleString('zh-CN')],
      ['总记录数', total.value],
      ['成功登录', stats.value.successCount],
      ['失败登录', stats.value.failureCount],
      ['成功率', `${successRate.value}%`],
      [''],
      ['详细数据']
    ];

    // 详细数据表头
    const headers = ['序号', '用户名', '事件类型', 'IP地址', '登录时间', '浏览器', '操作系统'];

    // 详细数据
    const detailData = logList.value.map((log, index) => [
      index + 1,
      log.username,
      getTypeText(log.logType),
      log.ipAddress,
      formatTime(log.logTime),
      getBrowserInfo(log.userAgent),
      getOSInfo(log.userAgent)
    ]);

    // 合并统计和详细数据
    const allData = [...summaryData, headers, ...detailData];

    // 创建工作表
    const worksheet = XLSX.utils.aoa_to_sheet(allData);

    // 设置列宽
    worksheet['!cols'] = [
      { wch: 8 },   // 序号
      { wch: 15 },  // 用户名
      { wch: 12 },  // 事件类型
      { wch: 18 },  // IP地址
      { wch: 20 },  // 登录时间
      { wch: 15 },  // 浏览器
      { wch: 15 }   // 操作系统
    ];

    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(workbook, worksheet, '登录日志');

    // 生成文件名
    const fileName = `登录日志_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.xlsx`;

    // 导出
    XLSX.writeFile(workbook, fileName);

    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  }
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

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(0, 19);
};

const getBrowserInfo = (userAgent: string) => {
  if (!userAgent) return '未知';
  if (userAgent.includes('Chrome')) return 'Chrome';
  if (userAgent.includes('Firefox')) return 'Firefox';
  if (userAgent.includes('Safari')) return 'Safari';
  if (userAgent.includes('Edge')) return 'Edge';
  if (userAgent.includes('MSIE') || userAgent.includes('Trident')) return 'IE';
  return '其他浏览器';
};

const getOSInfo = (userAgent: string) => {
  if (!userAgent) return '未知';
  if (userAgent.includes('Windows')) return 'Windows';
  if (userAgent.includes('Mac OS')) return 'macOS';
  if (userAgent.includes('Linux')) return 'Linux';
  if (userAgent.includes('Android')) return 'Android';
  if (userAgent.includes('iOS') || userAgent.includes('iPhone')) return 'iOS';
  return '其他系统';
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
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
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

.stat-total::before { background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%); }
.stat-total .stat-icon {
  background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(245, 87, 108, 0.1) 100%);
  color: #f093fb;
}
.stat-total .stat-value { color: #f093fb; }

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
.user-cell, .ip-cell, .time-cell, .agent-cell {
  display: flex;
  align-items: center;
  gap: 8px;
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

.agent-box {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.agent-text {
  font-family: monospace;
  font-size: 12px;
  word-break: break-all;
  line-height: 1.6;
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
