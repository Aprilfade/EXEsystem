<template>
  <div class="grading-dashboard-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>批阅工作统计</h2>
      <p class="page-desc">查看我的批阅工作量和历史记录</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row" v-loading="statsLoading">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="32"><EditPen /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总批阅次数</div>
              <div class="stat-value">{{ stats.totalOperations || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon :size="32"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">最近30天</div>
              <div class="stat-value">{{ recentCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon :size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">最近操作</div>
              <div class="stat-value stat-time">{{ lastOperationTime }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近批阅历史 -->
    <el-card shadow="never" class="history-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">最近批阅记录（最多显示50条）</span>
          <el-button type="primary" :icon="RefreshRight" @click="fetchData">
            刷新
          </el-button>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 空状态 -->
        <el-empty v-if="!loading && historyList.length === 0" description="暂无批阅记录" />

        <!-- 历史记录表格 -->
        <el-table v-else :data="historyList" stripe border>
          <el-table-column prop="examResultId" label="成绩ID" width="100" align="center" />
          <el-table-column label="操作类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getActionTagType(row.actionType)" size="small">
                {{ getActionText(row.actionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分数变更" width="150" align="center">
            <template #default="{ row }">
              <span v-if="row.actionType === 'UPDATE_SCORE'">
                {{ row.oldScore || '未评分' }} → {{ row.newScore }}
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="评语变更" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.actionType === 'UPDATE_COMMENT'">
                {{ row.newComment || '无' }}
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="修改原因" width="150" show-overflow-tooltip />
          <el-table-column label="操作时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { EditPen, Calendar, Clock, RefreshRight } from '@element-plus/icons-vue';
import { getGraderHistory, getGraderStats, type GradingHistory, type GraderStats } from '@/api/score';
import { getUserInfo } from '@/api/user';

// 加载状态
const loading = ref(false);
const statsLoading = ref(false);

// 当前用户ID
const currentUserId = ref<number>(0);

// 统计数据
const stats = ref<GraderStats>({
  graderId: 0,
  totalOperations: 0
});

// 历史记录
const historyList = ref<GradingHistory[]>([]);

/**
 * 获取当前用户信息
 */
const fetchCurrentUser = async () => {
  try {
    const res = await getUserInfo();
    if (res.code === 200) {
      currentUserId.value = res.data.id;
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
};

/**
 * 获取批阅统计
 */
const fetchStats = async () => {
  if (!currentUserId.value) return;

  statsLoading.value = true;
  try {
    const res = await getGraderStats(currentUserId.value);
    if (res.code === 200) {
      stats.value = res.data;
    } else {
      ElMessage.error(res.msg || '获取统计数据失败');
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
    ElMessage.error('获取统计数据失败');
  } finally {
    statsLoading.value = false;
  }
};

/**
 * 获取批阅历史
 */
const fetchHistory = async () => {
  if (!currentUserId.value) return;

  loading.value = true;
  try {
    const res = await getGraderHistory(currentUserId.value, 50);
    if (res.code === 200) {
      historyList.value = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取批阅历史失败');
    }
  } catch (error) {
    console.error('获取批阅历史失败:', error);
    ElMessage.error('获取批阅历史失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 获取所有数据
 */
const fetchData = async () => {
  await fetchCurrentUser();
  await Promise.all([fetchStats(), fetchHistory()]);
};

/**
 * 计算最近30天的操作次数
 */
const recentCount = computed(() => {
  const thirtyDaysAgo = new Date();
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

  return historyList.value.filter(item => {
    const itemDate = new Date(item.createTime);
    return itemDate >= thirtyDaysAgo;
  }).length;
});

/**
 * 获取最后操作时间
 */
const lastOperationTime = computed(() => {
  if (historyList.value.length === 0) return '暂无记录';

  const lastItem = historyList.value[0];
  return formatTime(lastItem.createTime);
});

/**
 * 格式化时间
 */
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

/**
 * 获取操作类型文本
 */
const getActionText = (actionType: string) => {
  const map: Record<string, string> = {
    'UPDATE_SCORE': '修改分数',
    'UPDATE_COMMENT': '修改评语',
    'BATCH_UPDATE': '批量更新'
  };
  return map[actionType] || actionType;
};

/**
 * 获取操作类型标签类型
 */
const getActionTagType = (actionType: string) => {
  const map: Record<string, any> = {
    'UPDATE_SCORE': 'warning',
    'UPDATE_COMMENT': 'success',
    'BATCH_UPDATE': 'info'
  };
  return map[actionType] || 'info';
};

// 页面加载时获取数据
onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.grading-dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }

  .page-desc {
    color: #909399;
    font-size: 14px;
    margin: 0;
  }
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }

  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      flex-shrink: 0;
    }

    .stat-info {
      flex: 1;

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #303133;

        &.stat-time {
          font-size: 14px;
          font-weight: 400;
        }
      }
    }
  }
}

.history-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .text-muted {
    color: #c0c4cc;
  }
}
</style>
