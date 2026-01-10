<template>
  <el-dialog
    v-model="dialogVisible"
    title="批阅历史记录"
    width="900px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div v-loading="loading" class="history-container">
      <!-- 空状态 -->
      <el-empty
        v-if="!loading && historyList.length === 0"
        description="暂无批阅历史记录"
      />

      <!-- 历史记录时间线 -->
      <el-timeline v-else>
        <el-timeline-item
          v-for="item in historyList"
          :key="item.id"
          :timestamp="formatTime(item.createTime)"
          placement="top"
          :color="getActionColor(item.actionType)"
        >
          <el-card shadow="hover" class="history-card">
            <div class="history-header">
              <div class="action-badge">
                <el-tag :type="getActionTagType(item.actionType)" size="small">
                  {{ getActionText(item.actionType) }}
                </el-tag>
              </div>
              <div class="grader-info">
                <el-icon><User /></el-icon>
                <span>{{ item.graderName }}</span>
              </div>
            </div>

            <!-- 分数变更 -->
            <div v-if="item.actionType === 'UPDATE_SCORE'" class="history-content">
              <div class="change-row">
                <span class="label">分数变更：</span>
                <el-tag type="info" size="small">{{ item.oldScore || '未评分' }}</el-tag>
                <el-icon class="arrow-icon"><Right /></el-icon>
                <el-tag :type="getScoreTagType(item.newScore)" size="small">
                  {{ item.newScore }}
                </el-tag>
              </div>
              <div v-if="item.reason" class="reason-row">
                <span class="label">修改原因：</span>
                <span class="reason-text">{{ item.reason }}</span>
              </div>
            </div>

            <!-- 评语变更 -->
            <div v-if="item.actionType === 'UPDATE_COMMENT'" class="history-content">
              <div class="change-row">
                <span class="label">评语变更：</span>
              </div>
              <div class="comment-change">
                <div class="comment-box old-comment">
                  <div class="comment-label">原评语：</div>
                  <div class="comment-text">{{ item.oldComment || '无' }}</div>
                </div>
                <div class="comment-arrow">
                  <el-icon><Bottom /></el-icon>
                </div>
                <div class="comment-box new-comment">
                  <div class="comment-label">新评语：</div>
                  <div class="comment-text">{{ item.newComment || '无' }}</div>
                </div>
              </div>
              <div v-if="item.reason" class="reason-row">
                <span class="label">修改原因：</span>
                <span class="reason-text">{{ item.reason }}</span>
              </div>
            </div>

            <!-- 批量更新 -->
            <div v-if="item.actionType === 'BATCH_UPDATE'" class="history-content">
              <div class="batch-update-info">
                <el-icon><Operation /></el-icon>
                <span>批量操作</span>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button type="primary" :icon="Download" @click="handleExport">
        导出历史记录
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Right, Bottom, Operation, Download } from '@element-plus/icons-vue';
import { getGradingHistory, exportGradingHistory, type GradingHistory } from '@/api/score';

interface Props {
  visible: boolean;
  resultId: number;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:visible']);

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

// 数据
const loading = ref(false);
const historyList = ref<GradingHistory[]>([]);

/**
 * 获取批阅历史
 */
const fetchHistory = async () => {
  if (!props.resultId) return;

  loading.value = true;
  try {
    const res = await getGradingHistory(props.resultId);
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

  if (days > 0) {
    return `${days}天前 ${date.toLocaleString('zh-CN')}`;
  } else if (hours > 0) {
    return `${hours}小时前 ${date.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  } else if (minutes > 0) {
    return `${minutes}分钟前 ${date.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  } else {
    return `刚刚 ${date.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  }
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

/**
 * 获取操作颜色
 */
const getActionColor = (actionType: string) => {
  const map: Record<string, string> = {
    'UPDATE_SCORE': '#E6A23C',
    'UPDATE_COMMENT': '#67C23A',
    'BATCH_UPDATE': '#909399'
  };
  return map[actionType] || '#909399';
};

/**
 * 获取分数标签类型
 */
const getScoreTagType = (score: number | null) => {
  if (score === null) return 'info';
  if (score >= 90) return 'success';
  if (score >= 60) return '';
  return 'danger';
};

/**
 * 导出历史记录
 */
const handleExport = () => {
  if (!props.resultId) {
    ElMessage.warning('无效的成绩ID');
    return;
  }

  if (historyList.value.length === 0) {
    ElMessage.warning('暂无历史记录可导出');
    return;
  }

  try {
    exportGradingHistory(props.resultId);
    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出历史记录失败:', error);
    ElMessage.error('导出失败');
  }
};

// 监听对话框打开
watch(() => props.visible, (val) => {
  if (val) {
    fetchHistory();
  }
});
</script>

<style scoped lang="scss">
.history-container {
  max-height: 600px;
  overflow-y: auto;
  padding: 10px;

  :deep(.el-timeline) {
    padding-left: 10px;
  }

  :deep(.el-timeline-item__timestamp) {
    color: #909399;
    font-size: 12px;
  }
}

.history-card {
  margin-bottom: 10px;

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .grader-info {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #606266;
      font-size: 14px;
    }
  }

  .history-content {
    .label {
      color: #606266;
      font-weight: 500;
      margin-right: 8px;
    }

    .change-row {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;

      .arrow-icon {
        color: #909399;
      }
    }

    .reason-row {
      margin-top: 10px;
      padding: 8px;
      background-color: #f5f7fa;
      border-radius: 4px;
      font-size: 14px;

      .reason-text {
        color: #606266;
      }
    }

    .comment-change {
      margin-top: 10px;

      .comment-box {
        padding: 10px;
        border-radius: 4px;
        margin-bottom: 8px;

        &.old-comment {
          background-color: #fef0f0;
          border: 1px solid #fde2e2;

          .comment-label {
            color: #f56c6c;
            font-weight: 500;
            margin-bottom: 6px;
          }
        }

        &.new-comment {
          background-color: #f0f9ff;
          border: 1px solid #d1e7fd;

          .comment-label {
            color: #409eff;
            font-weight: 500;
            margin-bottom: 6px;
          }
        }

        .comment-text {
          color: #606266;
          line-height: 1.6;
          white-space: pre-wrap;
        }
      }

      .comment-arrow {
        text-align: center;
        color: #909399;
        margin: 8px 0;
      }
    }

    .batch-update-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>
