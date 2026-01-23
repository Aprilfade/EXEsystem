<template>
  <div class="notification-manage-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Bell /></el-icon>
        </div>
        <div>
          <h2>系统通知管理</h2>
          <p>发布和管理系统通知，及时传达重要信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" @click="handleRefresh" :loading="loading">刷新</el-button>
        <el-button icon="Download" type="primary" plain @click="handleExport">导出</el-button>
        <el-button type="primary" :icon="Plus" @click="handleCreate">发布新通知</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-total">
          <div class="stat-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">通知总数</div>
            <div class="stat-value">{{ total }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-published">
          <div class="stat-icon">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">已发布</div>
            <div class="stat-value">{{ publishedCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-draft">
          <div class="stat-icon">
            <el-icon :size="32"><Edit /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">草稿</div>
            <div class="stat-value">{{ draftCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-scheduled">
          <div class="stat-icon">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">定时发布</div>
            <div class="stat-value">{{ scheduledCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card table-card">
      <el-table :data="notificationList" v-loading="loading" style="width: 100%;" stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="200">
          <template #default="scope">
            <div class="title-cell">
              <el-icon><Bell /></el-icon>
              <span>{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isPublished" type="success" effect="dark">
              <el-icon><CircleCheck /></el-icon> 已发布
            </el-tag>
            <el-tag v-else-if="!scope.row.isPublished && scope.row.publishTime" type="warning" effect="dark">
              <el-icon><Clock /></el-icon> 定时发布
            </el-tag>
            <el-tag v-else type="info" effect="dark">
              <el-icon><Edit /></el-icon> 草稿
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="scope">
            <div class="time-cell" v-if="scope.row.publishTime">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(scope.row.publishTime) }}</span>
            </div>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(scope.row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
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
          @current-change="getList" />
    </el-card>

    <notification-edit-dialog
        v-model:visible="isDialogVisible"
        :notification-id="editingId"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchNotificationList, deleteNotification } from '@/api/notification';
import type { Notification, NotificationPageParams } from '@/api/notification';
import { Plus, Bell, Document, CircleCheck, Edit, Clock, Delete, Refresh, Download } from '@element-plus/icons-vue';
import NotificationEditDialog from '@/components/notification/NotificationEditDialog.vue';

const notificationList = ref<Notification[]>([]);
const total = ref(0);
const loading = ref(true);
const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);

const queryParams = reactive<NotificationPageParams>({
  current: 1,
  size: 10,
});

// 统计计算属性
const publishedCount = computed(() => {
  return notificationList.value.filter(n => n.isPublished).length;
});

const draftCount = computed(() => {
  return notificationList.value.filter(n => !n.isPublished && !n.publishTime).length;
});

const scheduledCount = computed(() => {
  return notificationList.value.filter(n => !n.isPublished && n.publishTime).length;
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchNotificationList(queryParams);
    notificationList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const handleCreate = () => {
  editingId.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (id: number) => {
  editingId.value = id;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这条通知吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteNotification(id);
    ElMessage.success('删除成功');
    getList();
  });
};

const handleRefresh = () => {
  getList();
  ElMessage.success('刷新成功');
};

const handleExport = () => {
  ElMessage.info('导出功能开发中...');
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(0, 19);
};

onMounted(getList);
</script>

<style scoped>
/* 页面容器 */
.notification-manage-container {
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

/* 统计卡片颜色 */
.stat-total::before { background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); }
.stat-total .stat-icon {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}
.stat-total .stat-value { color: #667eea; }

.stat-published::before { background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%); }
.stat-published .stat-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(133, 206, 97, 0.1) 100%);
  color: #67c23a;
}
.stat-published .stat-value { color: #67c23a; }

.stat-draft::before { background: linear-gradient(90deg, #909399 0%, #b1b3b8 100%); }
.stat-draft .stat-icon {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.1) 0%, rgba(177, 179, 184, 0.1) 100%);
  color: #909399;
}
.stat-draft .stat-value { color: #909399; }

.stat-scheduled::before { background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%); }
.stat-scheduled .stat-icon {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(235, 181, 99, 0.1) 100%);
  color: #e6a23c;
}
.stat-scheduled .stat-value { color: #e6a23c; }

/* 内容卡片 */
.content-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

.table-card {
  margin-bottom: 0;
}

/* 表格样式 */
.title-cell, .time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-text {
  color: #c0c4cc;
  font-style: italic;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .notification-manage-container {
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