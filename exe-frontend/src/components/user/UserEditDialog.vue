<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="500px"
      @close="handleClose"
  >
    <el-form v-if="userForm" ref="userFormRef" :model="userForm" :rules="rules" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="isUsernameDisabled" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickName">
        <el-input v-model="userForm.nickName" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="userForm.password" type="password" show-password placeholder="留空则不修改密码" />
      </el-form-item>

      <el-form-item v-if="showRoleSelector" label="角色" prop="roleIds">
        <el-select v-model="userForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
          <el-option
              v-for="item in allRoles"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="showStatusSwitch" label="状态" prop="isEnabled">
        <el-switch v-model="userForm.isEnabled" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useAuthStore } from '../../stores/auth';
import { fetchAllRoles } from '../../api/role';
import {createUser, updateUser, fetchUserById, updateMyProfile} from '../../api/user'; // 导入新的 fetchUserById
import type { UserInfo, Role } from '../../api/user';

const props = defineProps<{
  visible: boolean;
  userId?: number;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'success'): void;
}>();

const authStore = useAuthStore();
const allRoles = ref<Role[]>([]);
const userFormRef = ref<FormInstance>();
const userForm = ref<Partial<UserInfo> & { roleIds?: number[] } | null>(null);

const dialogTitle = computed(() => (props.userId ? '编辑用户' : '新增用户'));

// 监控弹窗的显示状态，这是新的、更健壮的逻辑
watch(() => props.visible, (isVisible) => {
  if (!isVisible) {
    userFormRef.value?.resetFields();
    userForm.value = null;
    return;
  }

  // 弹窗打开时的逻辑
  const currentUserId = authStore.user?.id;

  // 场景1：编辑自己
  if (props.userId && props.userId === currentUserId) {
    const selfData = authStore.user;
    userForm.value = {
      ...selfData,
      password: '',
      roleIds: selfData.roles ? selfData.roles.map(r => r.id) : [],
    };
  }
  // 场景2：管理员编辑他人
  else if (props.userId) {
    fetchUserById(props.userId).then(res => {
      if (res.code === 200) {
        userForm.value = { ...res.data, password: '', roleIds: res.data.roles?.map(r => r.id) ?? [] };
      }
    });
  }
  // 场景3：新增用户
  else {
    userForm.value = {
      id: undefined, username: '', nickName: '', password: '', isEnabled: 1, roleIds: [],
    };
  }

  // 获取角色列表（仅当需要显示时）
  if (showRoleSelector.value && allRoles.value.length === 0) {
    if (authStore.isAdmin || authStore.isSuperAdmin) {
      fetchAllRoles().then(res => {
        if (res.code === 200) allRoles.value = res.data;
      });
    }
  }
});


const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  password: [{ required: !props.userId, message: '请输入密码', trigger: 'blur' }],
}));

const handleClose = () => emit('update:visible', false);

const submitForm = () => {
  userFormRef.value?.validate(async (valid) => {
    if (valid && userForm.value) {
      const formData = { ...userForm.value };
      if (formData.id && !formData.password) delete formData.password;

      try {
        // 判断：如果是在编辑自己
        if (isEditingSelf.value) {
          await updateMyProfile(formData); // 调用专用于更新自己的API
        }
        // 判断：如果是管理员在编辑他人
        else if (formData.id) {
          await updateUser(formData.id, formData); // 调用专用于管理员的API
        }
        // 判断：如果是管理员在新增用户
        else {
          await createUser(formData);
        }

        ElMessage.success('操作成功');
        if (isEditingSelf.value) {
          await authStore.fetchUserInfo(); // 更新自己的信息后，刷新Store中的用户信息
        }
        emit('success'); // 通知父组件操作成功（用于刷新列表）
        handleClose();

      } catch (error) {
        console.error("操作失败", error);
        // 可以在这里给用户更友好的错误提示
        ElMessage.error("操作失败，请重试");
      }
    }
  });
};

// --- 权限控制逻辑 (保持不变) ---
const isEditingSelf = computed(() => props.userId === authStore.user?.id);
const isUsernameDisabled = computed(() => !!props.userId);
const showRoleSelector = computed(() => {
  if (!props.userId) return authStore.isAdmin || authStore.isSuperAdmin;
  if (isEditingSelf.value) return authStore.isSuperAdmin;
  return authStore.isAdmin || authStore.isSuperAdmin;
});
const showStatusSwitch = computed(() => {
  if (isEditingSelf.value) return false;
  return authStore.isAdmin || authStore.isSuperAdmin;
});
</script>