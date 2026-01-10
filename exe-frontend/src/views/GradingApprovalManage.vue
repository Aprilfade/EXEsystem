<template>
  <div class="grading-approval-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>批阅审批管理</h2>
      <p class="page-desc">管理成绩修改审批流程</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-content">
            <el-icon class="stat-icon"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.PENDING }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card approved">
          <div class="stat-content">
            <el-icon class="stat-icon"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.APPROVED }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card rejected">
          <div class="stat-content">
            <el-icon class="stat-icon"><CircleClose /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.REJECTED }}</div>
              <div class="stat-label">已驳回</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card total">
          <div class="stat-content">
            <el-icon class="stat-icon"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.TOTAL }}</div>
              <div class="stat-label">总计</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="main-card">
      <!-- 筛选工具栏 -->
      <div class="toolbar">
        <el-form :inline="true" :model="queryParams" class="query-form">
          <el-form-item label="审批状态">
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
              <el-option label="待审批" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="学生姓名">
            <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="提交日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 300px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
            <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 审批列表 -->
      <el-table v-loading="loading" :data="approvalList" border stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="expand-row">
                <span class="expand-label">学生信息：</span>
                <span>{{ row.studentName }} (ID: {{ row.studentId }})</span>
              </div>
              <div class="expand-row">
                <span class="expand-label">试卷信息：</span>
                <span>{{ row.paperTitle }} (ID: {{ row.paperId }})</span>
              </div>
              <div class="expand-row">
                <span class="expand-label">申请原因：</span>
                <span>{{ row.reason }}</span>
              </div>
              <div v-if="row.oldComment || row.newComment" class="expand-row">
                <span class="expand-label">评语变更：</span>
                <div class="comment-compare">
                  <div class="comment-item">
                    <div class="comment-label">原评语：</div>
                    <div class="comment-text">{{ row.oldComment || '无' }}</div>
                  </div>
                  <el-icon class="arrow-icon"><Right /></el-icon>
                  <div class="comment-item">
                    <div class="comment-label">新评语：</div>
                    <div class="comment-text">{{ row.newComment || '无' }}</div>
                  </div>
                </div>
              </div>
              <div v-if="row.approvalComment" class="expand-row">
                <span class="expand-label">审批意见：</span>
                <span>{{ row.approvalComment }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column label="分数变化" width="150">
          <template #default="{ row }">
            <div class="score-change">
              <span class="old-score">{{ row.oldScore ?? '未批改' }}</span>
              <el-icon class="arrow"><Right /></el-icon>
              <span class="new-score">{{ row.newScore }}</span>
              <el-tag
                :type="row.scoreChange > 0 ? 'success' : 'danger'"
                size="small"
                style="margin-left: 8px"
              >
                {{ row.scoreChange > 0 ? '+' : '' }}{{ row.scoreChange }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="变化幅度" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.changePercentage" :type="getPercentageType(row.changePercentage)">
              {{ row.changePercentage }}%
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="graderName" label="申请人" width="120" />
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批人" width="120">
          <template #default="{ row }">
            {{ row.approverName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="审批时间" width="180">
          <template #default="{ row }">
            {{ row.approvalTime ? formatTime(row.approvalTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              link
              :icon="CircleCheck"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              link
              :icon="CircleClose"
              @click="handleReject(row)"
            >
              驳回
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="warning"
              link
              :icon="Delete"
              @click="handleCancel(row)"
            >
              撤销
            </el-button>
            <span v-if="row.status !== 'PENDING'" style="color: #909399">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :title="approvalAction === 'approve' ? '审批通过' : '审批驳回'"
      width="500px"
    >
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="学生">
          <span>{{ currentApproval?.studentName }}</span>
        </el-form-item>
        <el-form-item label="分数变化">
          <span>
            {{ currentApproval?.oldScore ?? '未批改' }} → {{ currentApproval?.newScore }}
            ({{ currentApproval?.scoreChange > 0 ? '+' : '' }}{{ currentApproval?.scoreChange }})
          </span>
        </el-form-item>
        <el-form-item label="申请原因">
          <span>{{ currentApproval?.reason }}</span>
        </el-form-item>
        <el-form-item label="审批意见" required>
          <el-input
            v-model="approvalForm.approvalComment"
            type="textarea"
            :rows="4"
            :placeholder="approvalAction === 'approve' ? '请填写审批意见（必填）' : '请填写驳回原因（必填）'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmitApproval"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Clock, CircleCheck, CircleClose, Document, Search, RefreshRight,
  Right, Delete
} from '@element-plus/icons-vue';
import {
  fetchApprovalList,
  approveGrading,
  rejectGrading,
  cancelApproval,
  getApprovalStatistics,
  type GradingApproval,
  type ApprovalQueryParams,
  type ApprovalStatistics
} from '@/api/gradingApproval';

// 加载状态
const loading = ref(false);
const submitting = ref(false);

// 审批列表
const approvalList = ref<GradingApproval[]>([]);
const total = ref(0);

// 查询参数
const queryParams = reactive<ApprovalQueryParams>({
  current: 1,
  size: 10,
  status: undefined,
  studentName: undefined
});

const dateRange = ref<[string, string] | null>(null);

// 统计数据
const stats = ref<ApprovalStatistics>({
  PENDING: 0,
  APPROVED: 0,
  REJECTED: 0,
  TOTAL: 0
});

// 审批对话框
const approvalDialogVisible = ref(false);
const approvalAction = ref<'approve' | 'reject'>('approve');
const currentApproval = ref<GradingApproval | null>(null);
const approvalForm = reactive({
  approvalComment: ''
});

/**
 * 获取审批列表
 */
const fetchData = async () => {
  loading.value = true;
  try {
    // 处理日期范围
    const params = { ...queryParams };
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }

    const res = await fetchApprovalList(params);
    if (res.code === 200) {
      approvalList.value = res.data || [];
      total.value = res.total || 0;
    } else {
      ElMessage.error(res.msg || '获取审批列表失败');
    }
  } catch (error) {
    console.error('获取审批列表失败:', error);
    ElMessage.error('获取审批列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 获取统计数据
 */
const fetchStats = async () => {
  try {
    const res = await getApprovalStatistics();
    if (res.code === 200) {
      stats.value = res.data;
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
  }
};

/**
 * 查询
 */
const handleQuery = () => {
  queryParams.current = 1;
  fetchData();
};

/**
 * 重置
 */
const handleReset = () => {
  queryParams.status = undefined;
  queryParams.studentName = undefined;
  dateRange.value = null;
  queryParams.current = 1;
  fetchData();
};

/**
 * 审批通过
 */
const handleApprove = (row: GradingApproval) => {
  currentApproval.value = row;
  approvalAction.value = 'approve';
  approvalForm.approvalComment = '';
  approvalDialogVisible.value = true;
};

/**
 * 审批驳回
 */
const handleReject = (row: GradingApproval) => {
  currentApproval.value = row;
  approvalAction.value = 'reject';
  approvalForm.approvalComment = '';
  approvalDialogVisible.value = true;
};

/**
 * 提交审批
 */
const handleSubmitApproval = async () => {
  if (!approvalForm.approvalComment.trim()) {
    ElMessage.warning('请填写审批意见');
    return;
  }

  if (!currentApproval.value) return;

  submitting.value = true;
  try {
    const apiFunc = approvalAction.value === 'approve' ? approveGrading : rejectGrading;
    const res = await apiFunc(currentApproval.value.id, approvalForm.approvalComment);

    if (res.code === 200) {
      ElMessage.success(approvalAction.value === 'approve' ? '审批通过' : '已驳回');
      approvalDialogVisible.value = false;
      fetchData();
      fetchStats();
    } else {
      ElMessage.error(res.msg || '操作失败');
    }
  } catch (error) {
    console.error('审批操作失败:', error);
    ElMessage.error('操作失败');
  } finally {
    submitting.value = false;
  }
};

/**
 * 撤销审批
 */
const handleCancel = async (row: GradingApproval) => {
  try {
    await ElMessageBox.confirm('确定要撤销此审批吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const res = await cancelApproval(row.id);
    if (res.code === 200) {
      ElMessage.success('撤销成功');
      fetchData();
      fetchStats();
    } else {
      ElMessage.error(res.msg || '撤销失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销失败:', error);
      ElMessage.error('撤销失败');
    }
  }
};

/**
 * 格式化时间
 */
const formatTime = (timeStr: string) => {
  return new Date(timeStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

/**
 * 获取状态类型
 */
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  };
  return map[status] || 'info';
};

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已驳回'
  };
  return map[status] || status;
};

/**
 * 获取百分比类型
 */
const getPercentageType = (percentage: number) => {
  if (percentage >= 50) return 'danger';
  if (percentage >= 20) return 'warning';
  return 'info';
};

// 页面加载时获取数据
onMounted(() => {
  fetchData();
  fetchStats();
});
</script>

<style scoped lang="scss">
.grading-approval-manage-container {
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

.stats-cards {
  margin-bottom: 20px;

  .stat-card {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        font-size: 40px;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }

    &.pending {
      .stat-icon { color: #E6A23C; }
      .stat-value { color: #E6A23C; }
    }

    &.approved {
      .stat-icon { color: #67C23A; }
      .stat-value { color: #67C23A; }
    }

    &.rejected {
      .stat-icon { color: #F56C6C; }
      .stat-value { color: #F56C6C; }
    }

    &.total {
      .stat-icon { color: #409EFF; }
      .stat-value { color: #409EFF; }
    }
  }
}

.main-card {
  .toolbar {
    margin-bottom: 20px;

    .query-form {
      :deep(.el-form-item) {
        margin-bottom: 0;
      }
    }
  }

  .expand-content {
    padding: 20px;
    background: #f9fafc;

    .expand-row {
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .expand-label {
        font-weight: 600;
        color: #606266;
        margin-right: 8px;
      }

      .comment-compare {
        display: flex;
        align-items: center;
        gap: 16px;
        margin-top: 8px;

        .comment-item {
          flex: 1;
          padding: 12px;
          background: white;
          border-radius: 4px;
          border: 1px solid #dcdfe6;

          .comment-label {
            font-size: 12px;
            color: #909399;
            margin-bottom: 8px;
          }

          .comment-text {
            color: #606266;
            line-height: 1.6;
          }
        }

        .arrow-icon {
          font-size: 20px;
          color: #909399;
        }
      }
    }
  }

  .score-change {
    display: flex;
    align-items: center;
    gap: 8px;

    .old-score {
      color: #909399;
    }

    .arrow {
      color: #909399;
      font-size: 14px;
    }

    .new-score {
      color: #303133;
      font-weight: 600;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>
