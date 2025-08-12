<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>角色管理</h2>
        <p>为不同角色分配精细化的菜单与操作权限</p>
      </div>
    </div>

    <el-card shadow="never" class="content-card">
      <el-table :data="roleList" v-loading="loading" style="width: 100%;">
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button
                type="primary"
                link
                :icon="Edit"
                @click="handleEditPermissions(scope.row)"
                v-permission="'sys:role:perm'"
            >
              分配权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <permission-drawer
        v-if="isDrawerVisible"
        v-model:visible="isDrawerVisible"
        :role="editingRole"
        @success="handleDrawerSuccess"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { fetchAllRoles, type Role } from '@/api/role';
import { Edit } from '@element-plus/icons-vue';
import PermissionDrawer from '@/components/role/PermissionDrawer.vue';
import { ElMessage } from 'element-plus';

const roleList = ref<Role[]>([]);
const loading = ref(true);
const isDrawerVisible = ref(false);
const editingRole = ref<Role | null>(null);

const getList = async () => {
  loading.value = true;
  try {
    const res = await fetchAllRoles();
    if (res.code === 200) {
      roleList.value = res.data;
    }
  } finally {
    loading.value = false;
  }
};

const handleEditPermissions = (role: Role) => {
  editingRole.value = role;
  isDrawerVisible.value = true;
};

const handleDrawerSuccess = () => {
  ElMessage.success('权限更新成功！');
  // 可以选择性地重新加载一些数据，但这里只是关闭抽屉
  isDrawerVisible.value = false;
}

onMounted(getList);
</script>

<style scoped>
/* 复用之前的样式 */
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; font-size: 14px; }
.content-card { background-color: var(--bg-color-container); }
</style>