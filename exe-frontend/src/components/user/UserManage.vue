<template>
  <div class="user-manage-container">
    <el-card shadow="never">
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增用户</el-button>
    </el-card>

    <el-card shadow="never" class="table-container">
      <el-table v-loading="loading" :data="userList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickName" label="昵称" />
        <el-table-column label="角色">
          <template #default="scope">
            <el-tag v-for="role in scope.row.roles" :key="role.id" style="margin-right: 5px;">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'info'">
              {{ scope.row.isEnabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.id === authStore.user?.id" type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
            <div v-if="canManage(scope.row)" style="display: inline-block;">
              <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
              <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
              <el-button :type="scope.row.isEnabled === 1 ? 'warning' : 'success'" link :icon="scope.row.isEnabled === 1 ? Remove : CircleCheck" @click="handleDisable(scope.row)">
                {{ scope.row.isEnabled === 1 ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" background layout="total, sizes, prev, pager, next, jumper" :total="total"
                     v-model:current-page="queryParams.current" v-model:page-size="queryParams.size" @size-change="getList" @current-change="getList" />
    </el-card>

    <user-edit-dialog
        v-model:visible="isDialogVisible"
        :user-id="editingUserId"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {fetchUserList, deleteUser, updateUser} from '../../api/user';
import type { UserInfo, UserPageParams } from '../../api/user';
import { useAuthStore } from '../../stores/auth';
import { Plus, Edit, Delete, Remove, CircleCheck } from '@element-plus/icons-vue';
import UserEditDialog from './UserEditDialog.vue'; // 导入新组件

const authStore = useAuthStore();
const userList = ref<UserInfo[]>([]);
const total = ref(0);
const loading = ref(true);

// 用于控制弹窗的状态
const isDialogVisible = ref(false);
const editingUserId = ref<number | undefined>(undefined);

const queryParams = reactive<UserPageParams>({
  current: 1,
  size: 10,
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchUserList(queryParams);
    userList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const handleCreate = () => {
  editingUserId.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (id: number) => {
  editingUserId.value = id;
  isDialogVisible.value = true;
};

const canManage = (targetUser: UserInfo) => {
  const currentUser = authStore.user;
  if (!currentUser || currentUser.id === targetUser.id) return false;
  if (authStore.isSuperAdmin) return true;
  if (authStore.isAdmin) {
    const targetIsAnyAdmin = targetUser.roles?.some(r => r.code === 'ADMIN' || r.code === 'SUPER_ADMIN');
    return !targetIsAnyAdmin;
  }
  return false;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该用户吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteUser(id);
    ElMessage.success('删除成功');
    getList();
  }).catch(() => {});
};

const handleDisable = async (user: UserInfo) => {
  const newStatus = user.isEnabled === 1 ? 0 : 1;
  const actionText = newStatus === 1 ? '启用' : '禁用';
  ElMessageBox.confirm(`确定要${actionText}用户 "${user.username}" 吗?`, '提示', { type: 'warning' }).then(async () => {
    await updateUser(user.id, { isEnabled: newStatus });
    ElMessage.success(`${actionText}成功`);
    getList();
  }).catch(() => {});
};

onMounted(getList);
</script>

<style scoped>
.user-manage-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.table-container {
  margin-top: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>