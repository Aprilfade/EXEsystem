<template>
  <div class="chapter-tree-editor">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAddRootChapter">
        添加根章节
      </el-button>
      <el-button icon="Expand" @click="expandAll">全部展开</el-button>
      <el-button icon="Fold" @click="collapseAll">全部折叠</el-button>
      <el-button icon="Refresh" @click="loadTree">刷新</el-button>
    </div>

    <!-- 章节树 -->
    <el-scrollbar v-loading="loading">
      <el-tree
        ref="treeRef"
        :data="chapterTree"
        :props="treeProps"
        node-key="id"
        draggable
        :allow-drop="allowDrop"
        :allow-drag="allowDrag"
        @node-drop="handleNodeDrop"
        default-expand-all
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <!-- 图标 -->
            <el-icon v-if="data.type === 'chapter'" class="node-icon">
              <Folder />
            </el-icon>
            <el-icon v-else class="node-icon">
              <Document />
            </el-icon>

            <!-- 标签 -->
            <span class="node-label">{{ data.name }}</span>

            <!-- 操作按钮 -->
            <span class="node-actions">
              <el-button
                v-if="data.type === 'chapter'"
                link
                size="small"
                type="primary"
                @click.stop="handleAddChild(data)"
              >
                <el-icon><Plus /></el-icon>
              </el-button>
              <el-button
                link
                size="small"
                type="primary"
                @click.stop="handleEdit(data)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                link
                size="small"
                type="danger"
                @click.stop="handleDelete(data)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </span>
          </div>
        </template>
      </el-tree>
    </el-scrollbar>

    <!-- 章节表单弹窗 -->
    <ChapterFormDialog
      v-model:visible="dialogVisible"
      :chapter="currentChapter"
      :parent-id="currentParentId"
      :course-id="courseId"
      @success="loadTree"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { ElTree } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Folder, Document, Plus, Edit, Delete, Expand, Fold, Refresh } from '@element-plus/icons-vue';
import { getChapterTree, deleteChapter, batchUpdateChapterSort } from '@/api/courseAdmin';
import type { ChapterNode, ChapterSortDTO } from '@/api/courseAdmin';
import ChapterFormDialog from './ChapterFormDialog.vue';

interface Props {
  courseId: number;
}

const props = defineProps<Props>();

const loading = ref(false);
const treeRef = ref<InstanceType<typeof ElTree>>();
const chapterTree = ref<ChapterNode[]>([]);
const dialogVisible = ref(false);
const currentChapter = ref<ChapterNode | null>(null);
const currentParentId = ref<number>(0);

const treeProps = {
  children: 'children',
  label: 'name'
};

// 加载章节树
const loadTree = async () => {
  loading.value = true;
  try {
    const res = await getChapterTree(props.courseId);
    if (res.code === 200) {
      chapterTree.value = res.data;
    }
  } catch (error: any) {
    ElMessage.error('加载章节树失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 拖拽控制
const allowDrag = (node: any) => {
  // 只允许章节拖拽，资源不可拖拽
  return node.data.type === 'chapter';
};

const allowDrop = (draggingNode: any, dropNode: any, type: string) => {
  // 不允许资源作为父节点
  if (type === 'inner' && dropNode.data.type === 'resource') {
    return false;
  }
  return true;
};

// 拖拽完成处理
const handleNodeDrop = async (
  draggingNode: any,
  dropNode: any,
  dropType: 'before' | 'after' | 'inner'
) => {
  try {
    // 收集新的排序数据
    const sortList: ChapterSortDTO[] = [];

    const collectSort = (nodes: any[], parentId: number = 0) => {
      nodes.forEach((node, index) => {
        if (node.type === 'chapter') {
          sortList.push({
            id: node.id,
            sortOrder: index,
            parentId: parentId
          });
          if (node.children) {
            collectSort(node.children, node.id);
          }
        }
      });
    };

    collectSort(chapterTree.value);

    // 批量更新排序
    await batchUpdateChapterSort(sortList);
    ElMessage.success('章节顺序已更新');
    loadTree();
  } catch (error: any) {
    ElMessage.error('更新排序失败: ' + error.message);
    loadTree(); // 恢复原状
  }
};

// 添加根章节
const handleAddRootChapter = () => {
  currentChapter.value = null;
  currentParentId.value = 0;
  dialogVisible.value = true;
};

// 添加子章节
const handleAddChild = (data: ChapterNode) => {
  currentChapter.value = null;
  currentParentId.value = data.id;
  dialogVisible.value = true;
};

// 编辑章节
const handleEdit = (data: ChapterNode) => {
  if (data.type === 'resource') {
    ElMessage.warning('这是资源，请在资源管理中编辑');
    return;
  }
  currentChapter.value = { ...data };
  currentParentId.value = data.parentId;
  dialogVisible.value = true;
};

// 删除章节
const handleDelete = async (data: ChapterNode) => {
  if (data.type === 'resource') {
    ElMessage.warning('这是资源，请在资源管理中删除');
    return;
  }

  try {
    await ElMessageBox.confirm(
      '删除章节将同时删除其子章节，章节下的资源将变为未分类资源，确定删除吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    const res = await deleteChapter(data.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadTree();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message);
    }
  }
};

// 全部展开
const expandAll = () => {
  const nodes = treeRef.value?.store.nodesMap;
  if (nodes) {
    for (let key in nodes) {
      nodes[key].expanded = true;
    }
  }
};

// 全部折叠
const collapseAll = () => {
  const nodes = treeRef.value?.store.nodesMap;
  if (nodes) {
    for (let key in nodes) {
      nodes[key].expanded = false;
    }
  }
};

// 初始化
onMounted(() => {
  loadTree();
});
</script>

<style scoped lang="scss">
.chapter-tree-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;

  .toolbar {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    gap: 8px;
  }

  .el-scrollbar {
    flex: 1;
    padding: 16px;
  }

  .tree-node {
    display: flex;
    align-items: center;
    width: 100%;
    padding: 8px;
    border-radius: 4px;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f7fa;

      .node-actions {
        display: flex;
      }
    }

    .node-icon {
      margin-right: 8px;
      font-size: 16px;
      color: #606266;
    }

    .node-label {
      flex: 1;
      font-size: 14px;
      color: #303133;
    }

    .node-actions {
      display: none;
      gap: 4px;
    }
  }
}

:deep(.el-tree-node__content) {
  height: auto;
  min-height: 36px;
  padding: 4px 0;
}

:deep(.el-tree-node.is-drop-inner > .el-tree-node__content) {
  background-color: #409eff;
  color: #fff;
}
</style>
