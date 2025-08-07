<template>
  <template>
    <div class="app-container">
      <el-card shadow="never">
        <el-button type="primary" icon="Plus" @click="handleCreate">新增用户</el-button>
      </el-card>

      <el-card shadow="never" class="table-container">
        <el-table v-loading="loading" :data="userList" style="width: 100%">
          <el-table-column type="index" label="序号" width="80" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="nickName" label="昵称" />
          <el-table-column prop="isEnabled" label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.isEnabled ? 'success' : 'info'">
                {{ scope.row.isEnabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button type="primary" link icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button type="danger" link icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.size"
            @size-change="getList"
            @current-change="getList"
        />
      </el-card>

      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
        <el-form ref="userFormRef" :model="userForm" :rules="rules" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="userForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickName">
            <el-input v-model="userForm.nickName" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="userForm.password" type="password" show-password placeholder="留空则不修改密码" />
          </el-form-item>
          <el-form-item label="状态" prop="isEnabled">
            <el-switch v-model="userForm.isEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </template>
      </el-dialog>
    </div>
  </template>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { fetchUserList, createUser, updateUser, deleteUser } from '@/api/user';
import type { SysUser, UserPageParams } from '@/api/user';

// 分页和查询参数
const queryParams = reactive<UserPageParams>({
  current: 1,
  size: 10,
});

// 表格数据
const userList = ref<SysUser[]>([]);
const total = ref(0);
const loading = ref(true); // 表格加载状态

// 对话框相关状态
const dialogVisible = ref(false);
const dialogTitle = ref('');
const userFormRef = ref<FormInstance>();
const userForm = reactive<Partial<SysUser>>({
  id: undefined,
  username: '',
  nickName: '',
  password: '',
  isEnabled: 1,
});

// 表单校验规则
const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  password: [{ required: false, message: '请输入密码', trigger: 'blur' }], // 初始为可选
});
// 1. 获取用户列表
const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchUserList(queryParams);
    if (response.code === 200) {
      userList.value = response.data;
      total.value = response.total;
    }
  } catch (error) {
    console.error("获取用户列表失败", error);
  } finally {
    loading.value = false;
  }
};

// 2. 处理新增
const handleCreate = () => {
  resetForm();
  dialogTitle.value = '新增用户';
  // 新增时密码为必填
  rules.password[0].required = true;
  dialogVisible.value = true;
};

// 3. 处理更新
const handleUpdate = (row: SysUser) => {
  resetForm();
  Object.assign(userForm, row); // 复制数据到表单
  dialogTitle.value = '编辑用户';
  // 编辑时密码为选填
  rules.password[0].required = false;
  dialogVisible.value = true;
};

// 4. 处理删除
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该用户吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await deleteUser(id);
    ElMessage.success('删除成功');
    getList(); // 重新加载列表
  }).catch(() => {});
};

// 5. 提交表单
const submitForm = () => {
  userFormRef.value?.validate(async (valid) => {
    if (valid) {
      const isUpdate = !!userForm.id;
      if (isUpdate) {
        await updateUser(userForm.id!, userForm);
        ElMessage.success('更新成功');
      } else {
        await createUser(userForm);
        ElMessage.success('新增成功');
      }
      dialogVisible.value = false;
      getList();
    }
  });
};

// 6. 重置表单
const resetForm = () => {
  userForm.id = undefined;
  userForm.username = '';
  userForm.nickName = '';
  userForm.password = '';
  userForm.isEnabled = 1;
  userFormRef.value?.resetFields();
};

// 在组件挂载时初次加载数据
onMounted(() => {
  getList();
});
</script>

<style>
.el-table .warning-row {
  --el-table-tr-bg-color: var(--el-color-warning-light-9);
}
.el-table .success-row {
  --el-table-tr-bg-color: var(--el-color-success-light-9);
}
</style>
