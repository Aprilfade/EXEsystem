<template>
  <div class="ai-monitor-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>AI 调用监控</h2>
      <p class="subtitle">实时监控AI功能使用情况，追踪所有调用记录</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #ecf5ff; color: #409eff;">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总调用次数</div>
              <div class="stat-value">{{ stats.totalCalls || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff; color: #67c23a;">
              <el-icon :size="28"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">成功率</div>
              <div class="stat-value">{{ stats.successRate ? stats.successRate.toFixed(1) + '%' : '0%' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0; color: #f56c6c;">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">缓存命中率</div>
              <div class="stat-value">{{ stats.cacheHitRate ? stats.cacheHitRate.toFixed(1) + '%' : '0%' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c;">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总成本</div>
              <div class="stat-value">¥{{ stats.totalCost ? stats.totalCost.toFixed(2) : '0.00' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 功能使用统计图表 -->
    <el-card shadow="never" class="chart-card" v-if="stats.functionStats && stats.functionStats.length > 0">
      <template #header>
        <span>功能使用分布</span>
      </template>
      <div ref="functionChart" style="height: 300px;"></div>
    </el-card>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="用户ID">
          <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable style="width: 180px;" />
        </el-form-item>

        <el-form-item label="用户类型">
          <el-select v-model="queryForm.userType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="功能类型">
          <el-select v-model="queryForm.functionType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="错题分析" value="analyze" />
            <el-option label="智能出题" value="generate" />
            <el-option label="主观题批改" value="grading" />
            <el-option label="知识点提取" value="extract" />
          </el-select>
        </el-form-item>

        <el-form-item label="AI提供商">
          <el-select v-model="queryForm.provider" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.success" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="成功" :value="true" />
            <el-option label="失败" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 360px;"
            @change="handleDateChange"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>调用日志</span>
          <el-button type="text" @click="loadData">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="userId" label="用户ID" width="100" />

        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.userType === 'STUDENT' ? 'success' : 'warning'" size="small">
              {{ row.userType === 'STUDENT' ? '学生' : '教师' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="functionType" label="功能类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getFunctionTypeColor(row.functionType)" size="small">
              {{ getFunctionTypeName(row.functionType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="provider" label="AI提供商" width="120">
          <template #default="{ row }">
            <span>{{ row.provider === 'deepseek' ? 'DeepSeek' : row.provider === 'qwen' ? '通义千问' : row.provider }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="success" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">
              {{ row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="responseTime" label="响应时间" width="100" align="right">
          <template #default="{ row }">
            <span>{{ row.responseTime }} ms</span>
          </template>
        </el-table-column>

        <el-table-column prop="cached" label="缓存" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.cached" color="#67c23a" :size="18"><Check /></el-icon>
            <el-icon v-else color="#dcdfe6" :size="18"><Close /></el-icon>
          </template>
        </el-table-column>

        <el-table-column prop="retryCount" label="重试次数" width="100" align="center" />

        <el-table-column prop="estimatedCost" label="成本(元)" width="100" align="right">
          <template #default="{ row }">
            <span>¥{{ row.estimatedCost ? row.estimatedCost.toFixed(4) : '0.0000' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="requestSummary" label="请求摘要" min-width="200" show-overflow-tooltip />

        <el-table-column prop="createTime" label="调用时间" width="180">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[20, 50, 100, 200]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="调用详情"
      width="800px"
      destroy-on-close
    >
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentLog.userId }}</el-descriptions-item>

        <el-descriptions-item label="用户类型">
          <el-tag :type="currentLog.userType === 'STUDENT' ? 'success' : 'warning'">
            {{ currentLog.userType === 'STUDENT' ? '学生' : '教师' }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="功能类型">
          <el-tag :type="getFunctionTypeColor(currentLog.functionType)">
            {{ getFunctionTypeName(currentLog.functionType) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="AI提供商">
          {{ currentLog.provider === 'deepseek' ? 'DeepSeek' : currentLog.provider === 'qwen' ? '通义千问' : currentLog.provider }}
        </el-descriptions-item>

        <el-descriptions-item label="调用状态">
          <el-tag :type="currentLog.success ? 'success' : 'danger'">
            {{ currentLog.success ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="响应时间">{{ currentLog.responseTime }} ms</el-descriptions-item>
        <el-descriptions-item label="缓存命中">{{ currentLog.cached ? '是' : '否' }}</el-descriptions-item>

        <el-descriptions-item label="重试次数">{{ currentLog.retryCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="预估成本">¥{{ currentLog.estimatedCost ? currentLog.estimatedCost.toFixed(4) : '0.0000' }}</el-descriptions-item>

        <el-descriptions-item label="Token消耗" v-if="currentLog.tokensUsed">{{ currentLog.tokensUsed }}</el-descriptions-item>
        <el-descriptions-item label="调用时间">{{ formatDateTime(currentLog.createTime) }}</el-descriptions-item>

        <el-descriptions-item label="请求摘要" :span="2">
          <div class="request-summary">{{ currentLog.requestSummary || '无' }}</div>
        </el-descriptions-item>

        <el-descriptions-item label="错误信息" :span="2" v-if="!currentLog.success && currentLog.errorMessage">
          <el-alert type="error" :closable="false">
            {{ currentLog.errorMessage }}
          </el-alert>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAiLogs, getAiStats, type AiCallLog, type AiStats } from '@/api/aiMonitor';
import * as echarts from 'echarts';

// 状态
const loading = ref(false);
const tableData = ref<AiCallLog[]>([]);
const stats = ref<AiStats>({
  totalCalls: 0,
  successRate: 0,
  cacheHitRate: 0,
  totalCost: 0,
  functionStats: []
});

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

const queryForm = reactive({
  userId: undefined as number | undefined,
  userType: '',
  functionType: '',
  provider: '',
  success: undefined as boolean | undefined,
  startTime: '',
  endTime: ''
});

const dateRange = ref<[Date, Date] | null>(null);
const detailVisible = ref(false);
const currentLog = ref<AiCallLog | null>(null);
const functionChart = ref<HTMLElement | null>(null);

// 方法
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      userId: queryForm.userId,
      userType: queryForm.userType || undefined,
      functionType: queryForm.functionType || undefined,
      provider: queryForm.provider || undefined,
      success: queryForm.success,
      startTime: queryForm.startTime || undefined,
      endTime: queryForm.endTime || undefined
    };

    const res = await getAiLogs(params);
    if (res.code === 200) {
      tableData.value = res.data;
      pagination.total = res.total || 0;
    } else {
      ElMessage.error(res.message || '加载失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const loadStats = async () => {
  try {
    const res = await getAiStats(7);
    if (res.code === 200) {
      stats.value = res.data;

      // 绘制图表
      setTimeout(() => {
        renderFunctionChart();
      }, 100);
    }
  } catch (error: any) {
    console.error('加载统计数据失败:', error);
  }
};

const renderFunctionChart = () => {
  if (!functionChart.value || !stats.value.functionStats || stats.value.functionStats.length === 0) {
    return;
  }

  const chart = echarts.init(functionChart.value);
  const functionNames: Record<string, string> = {
    'analyze': '错题分析',
    'generate': '智能出题',
    'grading': '主观题批改',
    'extract': '知识点提取'
  };

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '功能使用',
        type: 'pie',
        radius: '60%',
        data: stats.value.functionStats.map((item: any) => ({
          name: functionNames[item.function_type] || item.function_type,
          value: item.count
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };

  chart.setOption(option);

  // 响应式
  window.addEventListener('resize', () => {
    chart.resize();
  });
};

const handleDateChange = (value: [Date, Date] | null) => {
  if (value) {
    queryForm.startTime = value[0].toISOString();
    queryForm.endTime = value[1].toISOString();
  } else {
    queryForm.startTime = '';
    queryForm.endTime = '';
  }
};

const resetQuery = () => {
  queryForm.userId = undefined;
  queryForm.userType = '';
  queryForm.functionType = '';
  queryForm.provider = '';
  queryForm.success = undefined;
  queryForm.startTime = '';
  queryForm.endTime = '';
  dateRange.value = null;
  pagination.current = 1;
  loadData();
};

const viewDetail = (row: AiCallLog) => {
  currentLog.value = row;
  detailVisible.value = true;
};

const getFunctionTypeName = (type: string) => {
  const names: Record<string, string> = {
    'analyze': '错题分析',
    'generate': '智能出题',
    'grading': '主观题批改',
    'extract': '知识点提取'
  };
  return names[type] || type;
};

const getFunctionTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    'analyze': '',
    'generate': 'success',
    'grading': 'warning',
    'extract': 'info'
  };
  return colors[type] || '';
};

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 生命周期
onMounted(() => {
  loadData();
  loadStats();
});
</script>

<style scoped lang="scss">
.ai-monitor-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }

  .subtitle {
    margin: 0;
    font-size: 14px;
    color: #909399;
  }
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 16px;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.chart-card {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;

  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.request-summary {
  max-height: 100px;
  overflow-y: auto;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  word-break: break-all;
}
</style>
