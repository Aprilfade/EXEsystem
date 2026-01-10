<template>
  <div class="notification-center-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>通知中心</h2>
      <p class="page-desc">查看您的所有系统通知</p>
    </div>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="main-card">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <!-- 筛选标签 -->
          <el-radio-group v-model="filterType" @change="handleFilterChange">
            <el-radio-button :label="undefined">
              全部通知
              <el-badge v-if="stats.total > 0" :value="stats.total" class="badge-item" />
            </el-radio-button>
            <el-radio-button :label="0">
              未读通知
              <el-badge v-if="stats.unread > 0" :value="stats.unread" class="badge-item" type="danger" />
            </el-radio-button>
            <el-radio-button :label="1">
              已读通知
              <el-badge v-if="stats.read > 0" :value="stats.read" class="badge-item" type="info" />
            </el-radio-button>
          </el-radio-group>
        </div>
        <div class="toolbar-right">
          <el-button
            type="primary"
            :icon="Check"
            @click="handleMarkAllAsRead"
            :disabled="stats.unread === 0"
          >
            全部已读
          </el-button>
          <el-button :icon="RefreshRight" @click="fetchData">
            刷新
          </el-button>
        </div>
      </div>

      <!-- 通知列表 -->
      <div v-loading="loading" class="notification-list">
        <!-- 空状态 -->
        <el-empty v-if="!loading && notificationList.length === 0" :description="getEmptyText()" />

        <!-- 时间线样式通知 -->
        <el-timeline v-else>
          <el-timeline-item
            v-for="item in notificationList"
            :key="item.id"
            :timestamp="formatTime(item.createTime)"
            placement="top"
            :color="getNotificationColor(item)"
            :hollow="item.isRead === 1"
          >
            <el-card
              shadow="hover"
              class="notification-card"
              :class="{ 'is-unread': item.isRead === 0 }"
              @click="handleNotificationClick(item)"
            >
              <div class="notification-header">
                <div class="notification-title">
                  <span v-if="item.isRead === 0" class="unread-dot"></span>
                  <el-tag :type="getNotificationTagType(item.type)" size="small">
                    {{ getNotificationTypeName(item.type) }}
                  </el-tag>
                  <span class="title-text">{{ item.title }}</span>
                </div>
                <div class="notification-actions">
                  <el-button
                    v-if="item.isRead === 0"
                    type="primary"
                    link
                    :icon="View"
                    @click.stop="handleMarkAsRead(item)"
                  >
                    标为已读
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    :icon="Delete"
                    @click.stop="handleDelete(item)"
                  >
                    删除
                  </el-button>
                </div>
              </div>

              <div class="notification-content">
                {{ item.content }}
              </div>

              <div v-if="item.readTime" class="notification-meta">
                <el-icon><Clock /></el-icon>
                <span>已读于 {{ formatTime(item.readTime) }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="notificationList.length > 0"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Check, RefreshRight, View, Delete, Clock
} from '@element-plus/icons-vue';
import {
  getMyNotifications,
  getUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteUserNotification,
  type UserNotification,
  type NotificationQueryParams
} from '@/api/notification';

const router = useRouter();

// 加载状态
const loading = ref(false);

// 筛选类型
const filterType = ref<number | undefined>(undefined);

// 通知列表
const notificationList = ref<UserNotification[]>([]);
const total = ref(0);

// 查询参数
const queryParams = reactive<NotificationQueryParams>({
  current: 1,
  size: 10,
  isRead: undefined
});

// 统计数据
const stats = reactive({
  total: 0,
  unread: 0,
  read: 0
});

/**
 * 获取通知列表
 */
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getMyNotifications(queryParams);
    if (res.code === 200) {
      notificationList.value = res.data || [];
      total.value = res.total || 0;

      // 更新统计
      updateStats();
    } else {
      ElMessage.error(res.msg || '获取通知失败');
    }
  } catch (error) {
    console.error('获取通知失败:', error);
    ElMessage.error('获取通知失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 更新统计数据
 */
const updateStats = async () => {
  try {
    const res = await getUnreadCount();
    if (res.code === 200) {
      stats.unread = res.data.count || 0;
    }

    // 计算已读和总数（基于当前筛选）
    if (queryParams.isRead === undefined) {
      stats.total = total.value;
      stats.read = total.value - stats.unread;
    } else if (queryParams.isRead === 0) {
      stats.total = total.value + stats.unread;
      stats.read = total.value;
    } else {
      stats.total = total.value + stats.unread;
      stats.read = total.value;
    }
  } catch (error) {
    console.error('获取统计失败:', error);
  }
};

/**
 * 筛选切换
 */
const handleFilterChange = (value: number | undefined) => {
  queryParams.isRead = value;
  queryParams.current = 1;
  fetchData();
};

/**
 * 标记单个通知为已读
 */
const handleMarkAsRead = async (item: UserNotification) => {
  try {
    const res = await markNotificationAsRead(item.id);
    if (res.code === 200) {
      ElMessage.success('已标记为已读');
      fetchData();
    } else {
      ElMessage.error(res.msg || '标记失败');
    }
  } catch (error) {
    console.error('标记失败:', error);
    ElMessage.error('标记失败');
  }
};

/**
 * 标记所有通知为已读
 */
const handleMarkAllAsRead = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要将所有未读通知标记为已读吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    const res = await markAllNotificationsAsRead();
    if (res.code === 200) {
      ElMessage.success(`已标记 ${res.data.count} 条通知为已读`);
      fetchData();
    } else {
      ElMessage.error(res.msg || '标记失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记失败:', error);
      ElMessage.error('标记失败');
    }
  }
};

/**
 * 删除通知
 */
const handleDelete = async (item: UserNotification) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条通知吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    const res = await deleteUserNotification(item.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      fetchData();
    } else {
      ElMessage.error(res.msg || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

/**
 * 通知点击事件 - 跳转到相关业务
 */
const handleNotificationClick = async (item: UserNotification) => {
  // 如果未读，先标记为已读
  if (item.isRead === 0) {
    await markNotificationAsRead(item.id);
  }

  // 根据通知类型跳转到相应页面
  if (item.relatedId) {
    switch (item.type) {
      case 'SCORE_UPDATE':
      case 'COMMENT_UPDATE':
        // 跳转到成绩详情页（学生端）
        router.push({
          name: 'StudentExamResultDetail',
          params: { id: item.relatedId }
        });
        break;
      case 'SYSTEM':
        // 系统通知暂不跳转
        break;
    }
  }

  // 刷新列表
  fetchData();
};

/**
 * 格式化时间
 */
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 7) {
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } else if (days > 0) {
    return `${days}天前`;
  } else if (hours > 0) {
    return `${hours}小时前`;
  } else if (minutes > 0) {
    return `${minutes}分钟前`;
  } else {
    return '刚刚';
  }
};

/**
 * 获取通知类型名称
 */
const getNotificationTypeName = (type: string) => {
  const map: Record<string, string> = {
    'SCORE_UPDATE': '成绩更新',
    'COMMENT_UPDATE': '评语更新',
    'SYSTEM': '系统通知'
  };
  return map[type] || type;
};

/**
 * 获取通知标签类型
 */
const getNotificationTagType = (type: string) => {
  const map: Record<string, any> = {
    'SCORE_UPDATE': 'warning',
    'COMMENT_UPDATE': 'success',
    'SYSTEM': 'info'
  };
  return map[type] || 'info';
};

/**
 * 获取通知颜色
 */
const getNotificationColor = (item: UserNotification) => {
  if (item.isRead === 1) return '#c0c4cc';

  const map: Record<string, string> = {
    'SCORE_UPDATE': '#E6A23C',
    'COMMENT_UPDATE': '#67C23A',
    'SYSTEM': '#409EFF'
  };
  return map[item.type] || '#409EFF';
};

/**
 * 获取空状态文本
 */
const getEmptyText = () => {
  if (filterType.value === 0) return '暂无未读通知';
  if (filterType.value === 1) return '暂无已读通知';
  return '暂无通知';
};

// 页面加载时获取数据
onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.notification-center-container {
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

.main-card {
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 16px;

    .toolbar-left {
      .badge-item {
        margin-left: 8px;
      }
    }

    .toolbar-right {
      display: flex;
      gap: 12px;
    }
  }

  .notification-list {
    min-height: 400px;

    :deep(.el-timeline) {
      padding-left: 10px;
    }

    :deep(.el-timeline-item__timestamp) {
      color: #909399;
      font-size: 12px;
    }
  }

  .notification-card {
    cursor: pointer;
    transition: all 0.3s ease;
    border-left: 3px solid transparent;

    &.is-unread {
      background-color: #f0f9ff;
      border-left-color: #409eff;
    }

    &:hover {
      transform: translateX(4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .notification-title {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;

        .unread-dot {
          display: inline-block;
          width: 8px;
          height: 8px;
          background-color: #f56c6c;
          border-radius: 50%;
          animation: pulse 2s infinite;
        }

        .title-text {
          font-weight: 600;
          color: #303133;
          font-size: 15px;
        }
      }

      .notification-actions {
        display: flex;
        gap: 8px;
        opacity: 0;
        transition: opacity 0.3s ease;
      }
    }

    &:hover .notification-actions {
      opacity: 1;
    }

    .notification-content {
      color: #606266;
      line-height: 1.6;
      margin-bottom: 12px;
      font-size: 14px;
    }

    .notification-meta {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #909399;
      font-size: 12px;
    }
  }

  .pagination {
    margin-top: 24px;
    display: flex;
    justify-content: center;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
