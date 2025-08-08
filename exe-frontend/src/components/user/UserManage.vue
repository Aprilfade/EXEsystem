<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>成员管理</h2>
        <p>精准控制系统访问权限与用户角色分工</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">新增用户</el-button>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">成员数量</p>
            <p class="value">{{ total }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">随登入次数</p>
            <p class="value">6</p> </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">非常成员</p>
            <p class="value">2</p> </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-input v-model="searchQuery" placeholder="输入姓名或账号搜索" size="large" style="width: 300px;" />
        <div>
        </div>
      </div>

      <el-table :data="filteredList" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="nickName" label="姓名" />
        <el-table-column prop="username" label="账号" />
        <el-table-column label="角色">
          <template #default="scope">
            <el-tag v-if="scope.row.roles && scope.row.roles.length > 0" v-for="role in scope.row.roles" :key="role.id" style="margin-right: 5px;">
              {{ role.name }}
            </el-tag>
            <span v-else>未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'info'">
              {{ scope.row.isEnabled === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleUpdate(scope.row.id)">编辑</el-button>
            <div v-if="canManage(scope.row)" style="display: inline-block;">
              <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
            </div>
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

    <user-edit-dialog
        v-model:visible="isDialogVisible"
        :user-id="editingUserId"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {fetchUserList, deleteUser, updateUser} from '../../api/user';
import type { UserInfo, UserPageParams } from '../../api/user';
import { useAuthStore } from '../../stores/auth';
import { Plus, Edit, Delete, Remove, CircleCheck } from '@element-plus/icons-vue';
import UserEditDialog from './UserEditDialog.vue';

const authStore = useAuthStore();
const allUserList = ref<UserInfo[]>([]); // 用于前端搜索
const userList = ref<UserInfo[]>([]); // 用于表格分页展示
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingUserId = ref<number | undefined>(undefined);
const searchQuery = ref('');

const queryParams = reactive<UserPageParams>({
  current: 1,
  size: 10,
});

// 前端实时搜索
const filteredList = computed(() => {
  if (!searchQuery.value) {
    return userList.value;
  }
  const query = searchQuery.value.toLowerCase();
  // 注意: 搜索应在所有用户中进行，但为了简单起见，这里只搜索当前页
  // 真实项目可能需要后端支持搜索
  return userList.value.filter(user =>
      user.nickName.toLowerCase().includes(query) ||
      user.username.toLowerCase().includes(query)
  );
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
  });
};

onMounted(getList);
</script>

<style scoped>
/* 复用之前的样式 */
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; font-size: 14px; }
.stats-cards { margin-bottom: 20px; }
.stat-item { padding: 8px; }
.stat-item .label { color: var(--text-color-regular); font-size: 14px; margin-bottom: 8px;}
.stat-item .value { font-size: 28px; font-weight: bold; }
.content-card { background-color: var(--bg-color-container); padding: 20px; }
.content-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>