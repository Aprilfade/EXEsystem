<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>通知管理</h2>
        <p>在此发布和管理系统通知</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">发布新通知</el-button>
    </div>

    <el-card shadow="never" class="content-card">
      <el-table :data="notificationList" v-loading="loading" style="width: 100%;">
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isPublished" type="success">
              已发布
            </el-tag>
            <el-tag v-else-if="!scope.row.isPublished && scope.row.publishTime" type="warning">
              定时发布
            </el-tag>
            <el-tag v-else type="info">
              草稿
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="200" />
        <el-table-column prop="createTime" label="创建时间" width="200" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleUpdate(scope.row.id)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
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
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchNotificationList, deleteNotification } from '@/api/notification';
import type { Notification, NotificationPageParams } from '@/api/notification';
import { Plus } from '@element-plus/icons-vue';
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

onMounted(getList);
</script>

<style scoped>
/* 这里可以直接复用你项目中已有的页面样式 */
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; font-size: 14px; }
.content-card { background-color: var(--bg-color-container); padding: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>