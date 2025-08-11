<template>
  <el-dialog
      :model-value="visible"
      title="通知详情"
      width="600px"
      @close="$emit('update:visible', false)"
  >
    <div v-if="notification" class="preview-container">
      <h3 class="notification-title">{{ notification.title }}</h3>
      <div class="notification-meta">
        <span>发布时间：{{ notification.publishTime }}</span>
      </div>
      <el-divider />
      <div class="notification-content" v-html="notification.content"></div>
    </div>
    <div v-else>
      <p>加载中...</p>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import type { Notification } from '@/api/notification';

defineProps<{
  visible: boolean;
  notification?: Notification;
}>();

defineEmits(['update:visible']);
</script>

<style scoped>
.preview-container {
  padding: 0 10px;
}
.notification-title {
  font-size: 20px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 10px;
}
.notification-meta {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}
.notification-content {
  font-size: 16px;
  line-height: 1.8;
}
</style>