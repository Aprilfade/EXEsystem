<template>
  <el-drawer
      :model-value="visible"
      :title="`为角色 [${role?.name}] 分配权限`"
      direction="rtl"
      size="500px"
      @close="handleClose"
  >
    <div v-loading="loading" class="drawer-content">
      <el-tree
          v-if="!loading && permissionTree.length > 0"
          ref="treeRef"
          :data="permissionTree"
          :props="treeProps"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedIds"
          :default-expand-all="true"
      />
      <el-empty v-else-if="!loading" description="暂无可分配的权限" />
    </div>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="confirmUpdate">确认</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { ElTree } from 'element-plus';
import type { Role } from '@/api/role';
import { getRolePermissions, updateRolePermissions } from '@/api/role';
import type { Permission } from '@/api/auth';

const props = defineProps<{
  visible: boolean;
  role: Role | null;
}>();

const emit = defineEmits(['update:visible', 'success']);

const loading = ref(true);
const permissionTree = ref<any[]>([]);
const checkedIds = ref<number[]>([]);
const treeRef = ref<InstanceType<typeof ElTree>>();

const treeProps = ref({
  label: 'name',
  children: 'children',
});

const loadPermissions = async () => {
  if (!props.role) return;
  loading.value = true;
  try {
    const res = await getRolePermissions(props.role.id);
    if (res.code === 200) {
      permissionTree.value = buildTree(res.data.allPermissions);
      checkedIds.value = res.data.checkedIds;
    }
  } finally {
    loading.value = false;
  }
};

const buildTree = (permissions: Permission[]): any[] => {
  const map = new Map<number, any>();
  const tree: any[] = [];
  permissions.forEach(p => {
    map.set(p.id, { ...p, children: [] });
  });
  permissions.forEach(p => {
    const node = map.get(p.id)!;
    if (p.parentId && p.parentId !== 0 && map.has(p.parentId)) {
      map.get(p.parentId)!.children.push(node);
    } else {
      tree.push(node);
    }
  });
  return tree;
};

const handleClose = () => {
  emit('update:visible', false);
};

// 【核心修复】在这里添加了对 treeRef.value 的判断
const confirmUpdate = async () => {
  // 在尝试访问 treeRef.value 之前，确保它和 role 都存在
  if (!props.role || !treeRef.value) {
    console.error("角色或树形组件引用不存在!");
    return;
  }

  const selectedIds = treeRef.value.getCheckedKeys(false) as number[];
  const halfSelectedIds = treeRef.value.getHalfCheckedKeys() as number[];
  const allIds = [...selectedIds, ...halfSelectedIds];

  await updateRolePermissions(props.role.id, allIds);
  emit('success');
};

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    // 每次打开时重置状态，防止旧数据残留
    permissionTree.value = [];
    checkedIds.value = [];
    loadPermissions();
  }
});

</script>

<style scoped>
.drawer-content {
  padding: 16px;
  height: 100%;
  overflow-y: auto;
}
</style>