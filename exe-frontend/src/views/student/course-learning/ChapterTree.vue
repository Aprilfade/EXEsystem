<template>
  <div class="chapter-tree-wrapper">
    <!-- 顶部统计信息 -->
    <div class="tree-header">
      <div class="header-stats">
        <span class="stat-item">
          <el-icon><Document /></el-icon>
          共 {{ totalResourceCount }} 个资源
        </span>
        <span class="stat-item completed">
          <el-icon><CircleCheck /></el-icon>
          已完成 {{ completedCount }} 个
        </span>
        <span class="stat-item">
          <el-icon><Clock /></el-icon>
          学习时长: {{ formatStudyTime(totalStudyTime) }}
        </span>
      </div>
      <div class="header-progress">
        <el-progress
          :percentage="courseProgress"
          :color="courseProgress === 100 ? '#67C23A' : '#409EFF'"
          :stroke-width="12"
          style="width: 100%"
        />
      </div>
    </div>

    <!-- 章节树 -->
    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="treeProps"
      :default-expand-all="defaultExpandAll"
      :expand-on-click-node="false"
      node-key="id"
      class="chapter-tree"
    >
      <template #default="{ node, data }">
        <div class="tree-node-content" :class="{ 'is-resource': data.type === 'resource' }">
          <!-- 章节节点 -->
          <div v-if="data.type === 'chapter'" class="chapter-node">
            <el-icon class="node-icon"><Folder /></el-icon>
            <span class="node-label">{{ data.name }}</span>
            <span v-if="data.description" class="node-desc">{{ data.description }}</span>
          </div>

          <!-- 资源节点 -->
          <div
            v-else
            class="resource-node"
            :class="{ 'is-completed': isResourceCompleted(data.id) }"
            @click="handleResourceClick(data)"
          >
            <!-- 资源类型图标 -->
            <el-icon class="resource-icon" :class="`icon-${data.resourceType?.toLowerCase()}`">
              <VideoCamera v-if="data.resourceType === 'VIDEO'" />
              <Document v-else-if="data.resourceType === 'PDF'" />
              <Reading v-else-if="data.resourceType === 'PPT'" />
              <Link v-else />
            </el-icon>

            <!-- 资源名称 -->
            <span class="resource-label">{{ data.name }}</span>

            <!-- 完成标记 -->
            <el-tag
              v-if="isResourceCompleted(data.id)"
              type="success"
              size="small"
              effect="dark"
              class="completed-tag"
            >
              <el-icon><CircleCheck /></el-icon>
              已完成
            </el-tag>

            <!-- 进度条 -->
            <div v-else class="resource-progress">
              <el-progress
                :percentage="getResourcePercent(data.id)"
                :show-text="false"
                :stroke-width="6"
                :color="getResourcePercent(data.id) >= 95 ? '#67C23A' : '#409EFF'"
              />
              <span class="progress-text">{{ getResourcePercent(data.id) }}%</span>
            </div>
          </div>
        </div>
      </template>
    </el-tree>

    <!-- 空状态 -->
    <el-empty
      v-if="!treeData || treeData.length === 0"
      description="暂无课程内容"
      :image-size="100"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { ChapterTreeNode, CourseResource } from '@/api/courseProgress';
import { useCourseStore } from '@/stores/courseStore';
import { useCourseProgress } from '@/hooks/useCourseProgress';
import {
  Document,
  Folder,
  VideoCamera,
  Reading,
  Link,
  CircleCheck,
  Clock
} from '@element-plus/icons-vue';

/**
 * 组件属性
 */
interface Props {
  chapters: ChapterTreeNode[];
  defaultExpandAll?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  defaultExpandAll: true
});

/**
 * 组件事件
 */
const emit = defineEmits<{
  (e: 'resourceClick', resource: CourseResource): void;
}>();

// Hooks
const store = useCourseStore();
const {
  getResourcePercent,
  isResourceCompleted,
  completedResourceCount,
  totalResourceCount,
  totalStudyTime,
  formatStudyTime
} = useCourseProgress();

// 树形控件引用
const treeRef = ref();

/**
 * 树形配置
 */
const treeProps = {
  children: 'children',
  label: 'name'
};

/**
 * 构建树形数据
 */
const treeData = computed(() => {
  return buildTreeData(props.chapters);
});

/**
 * 递归构建树形数据
 */
const buildTreeData = (nodes: ChapterTreeNode[]): any[] => {
  if (!nodes || nodes.length === 0) return [];

  return nodes.map(node => {
    const treeNode: any = {
      id: `chapter_${node.id}`,
      name: node.name,
      description: node.description,
      type: 'chapter',
      children: []
    };

    // 递归添加子章节
    if (node.children && node.children.length > 0) {
      treeNode.children.push(...buildTreeData(node.children));
    }

    // 添加资源节点
    if (node.resources && node.resources.length > 0) {
      const resourceNodes = node.resources.map(resource => ({
        id: resource.id,
        name: resource.name,
        type: 'resource',
        resourceType: resource.resourceType,
        resourceUrl: resource.resourceUrl,
        chapterId: resource.chapterId,
        courseId: resource.courseId
      }));
      treeNode.children.push(...resourceNodes);
    }

    return treeNode;
  });
};

/**
 * 课程总进度
 */
const courseProgress = computed(() => {
  if (totalResourceCount.value === 0) return 0;
  return Math.floor((completedResourceCount.value / totalResourceCount.value) * 100);
});

/**
 * 完成数量
 */
const completedCount = computed(() => completedResourceCount.value);

/**
 * 处理资源点击
 */
const handleResourceClick = (data: any) => {
  if (data.type === 'resource') {
    const resource: CourseResource = {
      id: data.id,
      name: data.name,
      resourceType: data.resourceType,
      resourceUrl: data.resourceUrl,
      courseId: data.courseId,
      chapterId: data.chapterId,
      knowledgePointId: null,
      sortOrder: 0,
      createTime: ''
    };
    emit('resourceClick', resource);
  }
};

/**
 * 展开所有节点
 */
const expandAll = () => {
  const tree = treeRef.value;
  if (tree) {
    const allNodes = tree.store._getAllNodes();
    allNodes.forEach((node: any) => {
      node.expanded = true;
    });
  }
};

/**
 * 折叠所有节点
 */
const collapseAll = () => {
  const tree = treeRef.value;
  if (tree) {
    const allNodes = tree.store._getAllNodes();
    allNodes.forEach((node: any) => {
      node.expanded = false;
    });
  }
};

/**
 * 定位到指定资源
 */
const locateResource = (resourceId: number) => {
  const tree = treeRef.value;
  if (tree) {
    tree.setCurrentKey(resourceId);
    const node = tree.getNode(resourceId);
    if (node && node.parent) {
      // 展开父节点
      let parent = node.parent;
      while (parent) {
        parent.expanded = true;
        parent = parent.parent;
      }
    }
  }
};

/**
 * 暴露给父组件的方法
 */
defineExpose({
  expandAll,
  collapseAll,
  locateResource
});

/**
 * 监听chapters变化
 */
watch(() => props.chapters, () => {
  // 章节数据变化时，可以执行一些操作
}, { deep: true });
</script>

<style scoped lang="scss">
.chapter-tree-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.tree-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;

  .header-stats {
    display: flex;
    gap: 20px;
    margin-bottom: 15px;
    font-size: 14px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 5px;

      &.completed {
        color: #a0f3b5;
      }

      .el-icon {
        font-size: 16px;
      }
    }
  }

  .header-progress {
    :deep(.el-progress__text) {
      color: #fff !important;
      font-weight: 600;
    }

    :deep(.el-progress-bar__outer) {
      background-color: rgba(255, 255, 255, 0.2);
    }
  }
}

.chapter-tree {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: #fff;

  :deep(.el-tree-node__content) {
    height: auto;
    min-height: 40px;
    padding: 5px 0;
    border-radius: 6px;
    transition: all 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }
  }

  :deep(.el-tree-node__expand-icon) {
    font-size: 16px;
    color: #909399;
  }
}

.tree-node-content {
  flex: 1;
  display: flex;
  align-items: center;
  width: 100%;
  padding: 5px 10px;

  &.is-resource {
    cursor: pointer;
  }
}

.chapter-node {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;

  .node-icon {
    font-size: 18px;
    color: #409EFF;
  }

  .node-label {
    flex: 1;
  }

  .node-desc {
    font-size: 12px;
    font-weight: normal;
    color: #909399;
    margin-left: 10px;
  }
}

.resource-node {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s;

  &:hover {
    background-color: #ecf5ff;
    transform: translateX(5px);
  }

  &.is-completed {
    background-color: #f0f9ff;
  }

  .resource-icon {
    font-size: 20px;
    flex-shrink: 0;

    &.icon-video {
      color: #409EFF;
    }

    &.icon-pdf {
      color: #F56C6C;
    }

    &.icon-ppt {
      color: #E6A23C;
    }

    &.icon-link {
      color: #67C23A;
    }
  }

  .resource-label {
    flex: 1;
    font-size: 14px;
    color: #606266;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .completed-tag {
    flex-shrink: 0;
    font-size: 12px;
    padding: 0 8px;
    height: 24px;
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .resource-progress {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    width: 120px;

    .el-progress {
      flex: 1;
    }

    .progress-text {
      font-size: 12px;
      color: #909399;
      font-weight: 600;
      width: 35px;
      text-align: right;
    }
  }
}

// 滚动条样式
.chapter-tree {
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;

    &:hover {
      background: #a8a8a8;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .tree-header {
    padding: 15px;

    .header-stats {
      flex-direction: column;
      gap: 10px;
    }
  }

  .resource-node {
    .resource-progress {
      width: 80px;

      .progress-text {
        display: none;
      }
    }
  }
}
</style>
