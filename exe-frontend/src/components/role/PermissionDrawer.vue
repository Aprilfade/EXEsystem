<template>
  <el-drawer
      :model-value="visible"
      :title="`为角色 [${role?.name}] 分配权限`"
      direction="rtl"
      size="500px"
      @close="handleClose"
  >
    <div v-loading="loading" class="drawer-content">
      <!-- 搜索框 -->
      <el-input
          v-model="filterText"
          placeholder="搜索权限名称或代码"
          clearable
          prefix-icon="Search"
          style="margin-bottom: 16px"
      />

      <!-- 快捷操作按钮 -->
      <div class="tree-actions" style="margin-bottom: 12px">
        <el-button size="small" @click="checkAll">全选</el-button>
        <el-button size="small" @click="uncheckAll">全不选</el-button>
      </div>

      <!-- 权限树 -->
      <el-tree
          v-if="!loading && permissionTree.length > 0"
          ref="treeRef"
          :data="permissionTree"
          :props="treeProps"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedIds"
          :default-expand-all="true"
          :filter-node-method="filterNode"
      >
        <template #default="{ data }">
          <span class="custom-tree-node">
            <span>{{ data.name }}</span>
            <span class="permission-code">{{ data.code }}</span>
          </span>
        </template>
      </el-tree>
      <el-empty v-else-if="!loading" description="暂无可分配的权限" />
    </div>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="handleClose">取消</el-button>
        <el-button
            type="primary"
            @click="confirmUpdate"
            v-if="hasUpdatePermission"
        >
          确认
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import {onMounted, ref, watch, computed} from 'vue';
import { ElTree, ElMessage } from 'element-plus';
import type { Role } from '@/api/role.ts';
import { getRolePermissions, updateRolePermissions } from '@/api/role';
import type { Permission } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';

const props = defineProps<{
  visible: boolean;
  role: Role | null;
}>();

const emit = defineEmits(['update:visible', 'success']);

const authStore = useAuthStore();
const loading = ref(true);
const permissionTree = ref<any[]>([]);
const checkedIds = ref<number[]>([]);
const treeRef = ref<InstanceType<typeof ElTree>>();
const filterText = ref(''); // 搜索文本

// 计算属性：检查用户是否有更新权限
const hasUpdatePermission = computed(() => {
  return authStore.hasPermission('sys:role:perm');
});

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
  }catch (error) {
    console.error('加载权限失败:', error);
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

// 搜索过滤方法
const filterNode = (value: string, data: any) => {
  if (!value) return true;
  return data.name.includes(value) || data.code.includes(value);
};

// 监听搜索文本变化，触发树过滤
watch(filterText, (val) => {
  treeRef.value?.filter(val);
});

// 全选
const checkAll = () => {
  const allIds = getAllNodeIds(permissionTree.value);
  treeRef.value?.setCheckedKeys(allIds);
};

// 全不选
const uncheckAll = () => {
  treeRef.value?.setCheckedKeys([]);
};

// 递归获取所有节点ID
const getAllNodeIds = (nodes: any[]): number[] => {
  let ids: number[] = [];
  nodes.forEach(node => {
    ids.push(node.id);
    if (node.children && node.children.length > 0) {
      ids = ids.concat(getAllNodeIds(node.children));
    }
  });
  return ids;
};

const handleClose = () => {
  emit('update:visible', false);
};

const confirmUpdate = async () => {
  // 检查用户是否有权限更新
  if (!hasUpdatePermission.value) {
    ElMessage.warning('您没有更新权限');
    return;
  }

  if (!props.role || !treeRef.value) {
    ElMessage.error('数据异常，请刷新页面重试');
    return;
  }
  const selectedIds = treeRef.value.getCheckedKeys(false) as number[];
  const halfSelectedIds = treeRef.value.getHalfCheckedKeys() as number[];
  const allIds = [...selectedIds, ...halfSelectedIds];

  // 【关键修复】添加try-catch和用户反馈
  try {
    const res = await updateRolePermissions(props.role.id, allIds);
    if (res.code === 200) {
      ElMessage.success('权限更新成功');
      emit('success');
      handleClose();
    } else {
      ElMessage.error(res.message || '权限更新失败');
    }
  } catch (error: any) {
    ElMessage.error('权限更新异常: ' + (error.message || '请稍后重试'));
    console.error('权限更新失败:', error);
  }
};

// 添加 onMounted 钩子确保组件初始化时加载数据
onMounted(() => {
  if (props.visible) {
    loadPermissions();
  }
});

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

.custom-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 16px;
}

.permission-code {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
  font-family: 'Courier New', monospace;
}

.tree-actions {
  display: flex;
  gap: 8px;
}
</style>
