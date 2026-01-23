<template>
  <div class="student-notifications-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <el-icon class="header-icon"><Bell /></el-icon>
          <div>
            <h2>我的通知</h2>
            <p class="subtitle">及时查看系统消息、成绩更新和评论回复</p>
          </div>
        </div>
        <div class="header-actions">
          <el-badge :value="stats.unread" :hidden="stats.unread === 0" class="unread-badge">
            <el-button type="primary" :icon="Check" @click="handleMarkAllAsRead" :disabled="stats.unread === 0">
              全部已读
            </el-button>
          </el-badge>
          <el-button :icon="RefreshRight" @click="fetchData">刷新</el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card shadow="hover" class="stat-card total">
        <div class="stat-content">
          <el-icon class="stat-icon"><Message /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">全部通知</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card unread">
        <div class="stat-content">
          <el-icon class="stat-icon"><MessageBox /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ stats.unread }}</div>
            <div class="stat-label">未读通知</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card read">
        <div class="stat-content">
          <el-icon class="stat-icon"><Checked /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ stats.read }}</div>
            <div class="stat-label">已读通知</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="main-card">
      <!-- 筛选工具栏 -->
      <div class="toolbar">
        <div class="filter-tabs">
          <el-radio-group v-model="filterType" @change="handleFilterChange" size="large">
            <el-radio-button :label="undefined">
              <el-icon><List /></el-icon>
              全部通知
            </el-radio-button>
            <el-radio-button :label="0">
              <el-icon><Warning /></el-icon>
              未读通知
            </el-radio-button>
            <el-radio-button :label="1">
              <el-icon><CircleCheck /></el-icon>
              已读通知
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 通知列表 -->
      <div v-loading="loading" class="notification-list">
        <!-- 空状态 -->
        <el-empty v-if="!loading && notificationList.length === 0" :description="getEmptyText()">
          <template #image>
            <el-icon :size="100" color="#909399"><BellFilled /></el-icon>
          </template>
        </el-empty>

        <!-- 通知项 -->
        <div v-else class="notification-items">
          <transition-group name="notification">
            <div
              v-for="item in notificationList"
              :key="item.id"
              class="notification-item"
              :class="{ 'is-unread': item.isRead === 0 }"
              @click="handleNotificationClick(item)"
            >
              <!-- 未读标记 -->
              <div class="unread-indicator" v-if="item.isRead === 0"></div>

              <!-- 通知图标 -->
              <div class="notification-icon" :class="getNotificationTypeClass(item.type)">
                <el-icon :size="24">
                  <component :is="getNotificationIcon(item.type)" />
                </el-icon>
              </div>

              <!-- 通知内容 -->
              <div class="notification-content">
                <div class="notification-header">
                  <div class="notification-title">
                    <span class="title-text">{{ item.title }}</span>
                    <el-tag :type="getNotificationTagType(item.type)" size="small" class="type-tag">
                      {{ getNotificationTypeText(item.type) }}
                    </el-tag>
                  </div>
                  <div class="notification-time">
                    <el-icon><Clock /></el-icon>
                    {{ formatTime(item.createTime) }}
                  </div>
                </div>
                <div class="notification-body">
                  {{ item.content }}
                </div>
                <div class="notification-footer" v-if="item.isRead === 1 && item.readTime">
                  <el-icon><View /></el-icon>
                  <span>已读于 {{ formatTime(item.readTime) }}</span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="notification-actions">
                <el-tooltip content="标记已读" placement="top" v-if="item.isRead === 0">
                  <el-button
                    :icon="Check"
                    circle
                    size="small"
                    type="success"
                    @click.stop="handleMarkAsRead(item)"
                  />
                </el-tooltip>
                <el-tooltip content="查看详情" placement="top" v-if="item.relatedId">
                  <el-button
                    :icon="View"
                    circle
                    size="small"
                    type="primary"
                    @click.stop="handleViewDetail(item)"
                  />
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button
                    :icon="Delete"
                    circle
                    size="small"
                    type="danger"
                    @click.stop="handleDelete(item)"
                  />
                </el-tooltip>
              </div>
            </div>
          </transition-group>
        </div>
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
        :page-sizes="[10, 20, 50]"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Bell, BellFilled, Check, RefreshRight, View, Delete, Clock, Message, MessageBox, Checked,
  List, Warning, CircleCheck, TrophyBase, ChatLineSquare, InfoFilled
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

    // 从当前列表计算统计（简化版）
    const allRes = await getMyNotifications({ current: 1, size: 999 });
    if (allRes.code === 200) {
      const allNotifications = allRes.data || [];
      stats.total = allNotifications.length;
      stats.read = allNotifications.filter(n => n.isRead === 1).length;
    }
  } catch (error) {
    console.error('更新统计数据失败:', error);
  }
};

/**
 * 筛选变化
 */
const handleFilterChange = () => {
  queryParams.isRead = filterType.value;
  queryParams.current = 1;
  fetchData();
};

/**
 * 标记单个通知为已读
 */
const handleMarkAsRead = async (item: UserNotification) => {
  if (item.isRead === 1) return;

  try {
    const res = await markNotificationAsRead(item.id);
    if (res.code === 200) {
      ElMessage.success('已标记为已读');
      item.isRead = 1;
      item.readTime = new Date().toISOString();
      updateStats();
    } else {
      ElMessage.error(res.msg || '操作失败');
    }
  } catch (error) {
    console.error('标记已读失败:', error);
    ElMessage.error('操作失败');
  }
};

/**
 * 标记全部已读
 */
const handleMarkAllAsRead = async () => {
  if (stats.unread === 0) {
    ElMessage.info('没有未读通知');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要将所有 ${stats.unread} 条未读通知标记为已读吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    );

    const res = await markAllNotificationsAsRead();
    if (res.code === 200) {
      ElMessage.success(`已标记 ${res.data?.count || 0} 条通知为已读`);
      fetchData();
    } else {
      ElMessage.error(res.msg || '操作失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('全部标记已读失败:', error);
      ElMessage.error('操作失败');
    }
  }
};

/**
 * 删除通知
 */
const handleDelete = async (item: UserNotification) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条通知吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定删除',
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
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

/**
 * 点击通知
 */
const handleNotificationClick = async (item: UserNotification) => {
  // 如果未读，标记为已读
  if (item.isRead === 0) {
    await handleMarkAsRead(item);
  }

  // 如果有关联ID，跳转到详情
  if (item.relatedId) {
    handleViewDetail(item);
  }
};

/**
 * 查看详情
 */
const handleViewDetail = (item: UserNotification) => {
  if (!item.relatedId) return;

  switch (item.type) {
    case 'SCORE_UPDATE':
      // 跳转到考试记录详情
      router.push(`/student/history/${item.relatedId}`);
      break;
    case 'COMMENT_UPDATE':
      // 跳转到考试记录详情（查看评论）
      router.push(`/student/history/${item.relatedId}`);
      break;
    default:
      ElMessage.info('暂无详情页面');
  }
};

/**
 * 获取通知类型文本
 */
const getNotificationTypeText = (type: string): string => {
  const typeMap: Record<string, string> = {
    'SCORE_UPDATE': '成绩更新',
    'COMMENT_UPDATE': '评论回复',
    'SYSTEM': '系统通知'
  };
  return typeMap[type] || '通知';
};

/**
 * 获取通知类型样式
 */
const getNotificationTagType = (type: string): any => {
  const typeMap: Record<string, string> = {
    'SCORE_UPDATE': 'success',
    'COMMENT_UPDATE': 'warning',
    'SYSTEM': 'info'
  };
  return typeMap[type] || 'info';
};

/**
 * 获取通知图标
 */
const getNotificationIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    'SCORE_UPDATE': TrophyBase,
    'COMMENT_UPDATE': ChatLineSquare,
    'SYSTEM': InfoFilled
  };
  return iconMap[type] || Bell;
};

/**
 * 获取通知类型样式类
 */
const getNotificationTypeClass = (type: string): string => {
  const classMap: Record<string, string> = {
    'SCORE_UPDATE': 'icon-score',
    'COMMENT_UPDATE': 'icon-comment',
    'SYSTEM': 'icon-system'
  };
  return classMap[type] || 'icon-default';
};

/**
 * 格式化时间
 */
const formatTime = (dateStr: string | null): string => {
  if (!dateStr) return '-';

  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  // 1分钟内
  if (diff < 60 * 1000) {
    return '刚刚';
  }

  // 1小时内
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000));
    return `${minutes}分钟前`;
  }

  // 24小时内
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000));
    return `${hours}小时前`;
  }

  // 7天内
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000));
    return `${days}天前`;
  }

  // 超过7天显示具体日期
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

/**
 * 获取空状态文本
 */
const getEmptyText = (): string => {
  if (filterType.value === 0) {
    return '暂无未读通知';
  } else if (filterType.value === 1) {
    return '暂无已读通知';
  }
  return '暂无通知';
};

// 初始化
onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.student-notifications-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;

  // 页面头部
  .page-header {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 24px 32px;
      background: white;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

      .title-section {
        display: flex;
        align-items: center;
        gap: 20px;

        .header-icon {
          font-size: 48px;
          color: #667eea;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        h2 {
          margin: 0;
          font-size: 28px;
          font-weight: 600;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .subtitle {
          margin: 4px 0 0 0;
          font-size: 14px;
          color: #909399;
        }
      }

      .header-actions {
        display: flex;
        gap: 12px;

        .unread-badge {
          :deep(.el-badge__content) {
            background-color: #f56c6c;
          }
        }
      }
    }
  }

  // 统计卡片
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
      border-radius: 12px;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;

        .stat-icon {
          color: rgba(255, 255, 255, 0.9);
        }
      }

      &.unread {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;

        .stat-icon {
          color: rgba(255, 255, 255, 0.9);
        }
      }

      &.read {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: white;

        .stat-icon {
          color: rgba(255, 255, 255, 0.9);
        }
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: 20px;
        padding: 8px;

        .stat-icon {
          font-size: 48px;
        }

        .stat-info {
          .stat-value {
            font-size: 32px;
            font-weight: 700;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 14px;
            opacity: 0.9;
          }
        }
      }
    }
  }

  // 主卡片
  .main-card {
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

    :deep(.el-card__body) {
      padding: 24px;
    }

    // 工具栏
    .toolbar {
      margin-bottom: 24px;

      .filter-tabs {
        :deep(.el-radio-group) {
          display: flex;
          gap: 12px;

          .el-radio-button {
            .el-radio-button__inner {
              border-radius: 8px;
              border: 1px solid #dcdfe6;
              padding: 10px 20px;
              display: flex;
              align-items: center;
              gap: 8px;
              font-weight: 500;

              &:hover {
                color: #667eea;
                border-color: #667eea;
              }
            }

            &.is-active {
              .el-radio-button__inner {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border-color: #667eea;
                color: white;
              }
            }
          }
        }
      }
    }

    // 通知列表
    .notification-list {
      min-height: 400px;

      .notification-items {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .notification-item {
          position: relative;
          display: flex;
          gap: 20px;
          padding: 20px;
          background: #f8f9fa;
          border-radius: 12px;
          border: 2px solid transparent;
          transition: all 0.3s ease;
          cursor: pointer;

          &.is-unread {
            background: #fff7e6;
            border-color: #ffa940;

            &:hover {
              border-color: #ff7a00;
              box-shadow: 0 4px 12px rgba(255, 122, 0, 0.2);
            }
          }

          &:hover {
            background: white;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
          }

          .unread-indicator {
            position: absolute;
            top: 20px;
            left: 0;
            width: 4px;
            height: 40px;
            background: linear-gradient(135deg, #ffa940 0%, #ff7a00 100%);
            border-radius: 0 4px 4px 0;
          }

          .notification-icon {
            flex-shrink: 0;
            width: 56px;
            height: 56px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 12px;
            background: white;

            &.icon-score {
              background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
              color: white;
            }

            &.icon-comment {
              background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
              color: white;
            }

            &.icon-system {
              background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
              color: white;
            }
          }

          .notification-content {
            flex: 1;

            .notification-header {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              margin-bottom: 12px;

              .notification-title {
                display: flex;
                align-items: center;
                gap: 12px;

                .title-text {
                  font-size: 16px;
                  font-weight: 600;
                  color: #303133;
                }

                .type-tag {
                  border-radius: 4px;
                }
              }

              .notification-time {
                display: flex;
                align-items: center;
                gap: 4px;
                font-size: 13px;
                color: #909399;
              }
            }

            .notification-body {
              font-size: 14px;
              color: #606266;
              line-height: 1.6;
              margin-bottom: 12px;
            }

            .notification-footer {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 12px;
              color: #909399;
            }
          }

          .notification-actions {
            flex-shrink: 0;
            display: flex;
            gap: 8px;
            align-items: flex-start;
          }
        }
      }
    }

    // 分页
    .pagination {
      margin-top: 24px;
      display: flex;
      justify-content: center;
    }
  }
}

// 动画
.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.notification-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

// 响应式
@media (max-width: 768px) {
  .student-notifications-page {
    padding: 12px;

    .page-header .header-content {
      flex-direction: column;
      gap: 16px;
      padding: 20px;

      .title-section {
        .header-icon {
          font-size: 36px;
        }

        h2 {
          font-size: 24px;
        }
      }

      .header-actions {
        width: 100%;

        .el-button {
          flex: 1;
        }
      }
    }

    .stats-cards {
      grid-template-columns: 1fr;
    }

    .main-card {
      :deep(.el-card__body) {
        padding: 16px;
      }

      .notification-list .notification-items .notification-item {
        flex-direction: column;
        padding: 16px;

        .notification-actions {
          flex-direction: row;
          justify-content: flex-end;
        }
      }
    }
  }
}
</style>
