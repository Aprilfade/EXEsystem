<template>
  <div class="draggable-module" :class="{ 'is-editing': isEditMode }">
    <!-- 编辑模式下的控制栏 -->
    <div v-if="isEditMode" class="module-header">
      <div class="module-controls">
        <el-icon class="drag-handle">
          <Rank />
        </el-icon>
        <span class="module-title">{{ title }}</span>
        <el-badge :value="badgeCount" :hidden="badgeCount === 0" class="module-badge" />
      </div>
      <div class="module-actions">
        <el-tooltip content="显示/隐藏" placement="top">
          <el-switch
            v-model="moduleEnabled"
            size="small"
            @change="handleToggle"
          />
        </el-tooltip>
      </div>
    </div>

    <!-- 模块内容 -->
    <div v-show="moduleEnabled" class="module-content">
      <slot />
    </div>

    <!-- 编辑模式下的遮罩 -->
    <div v-if="isEditMode && moduleEnabled" class="edit-overlay">
      <el-icon><EditPen /></el-icon>
      <span>拖拽调整顺序</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { Rank, EditPen } from '@element-plus/icons-vue';

interface Props {
  title: string;
  enabled: boolean;
  isEditMode: boolean;
  badgeCount?: number;
}

const props = withDefaults(defineProps<Props>(), {
  badgeCount: 0
});

const emit = defineEmits<{
  (e: 'update:enabled', value: boolean): void;
}>();

const moduleEnabled = ref(props.enabled);

watch(() => props.enabled, (val) => {
  moduleEnabled.value = val;
});

const handleToggle = () => {
  emit('update:enabled', moduleEnabled.value);
};
</script>

<style scoped>
.draggable-module {
  position: relative;
  transition: all 0.3s ease;
}

.draggable-module.is-editing {
  border: 2px dashed #409EFF;
  border-radius: 8px;
  padding: 8px;
  background: rgba(64, 158, 255, 0.05);
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: move;
}

.module-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.drag-handle {
  cursor: grab;
  font-size: 18px;
  color: #909399;
}

.drag-handle:active {
  cursor: grabbing;
}

.module-title {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.module-badge {
  margin-left: 4px;
}

.module-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.module-content {
  transition: opacity 0.3s ease;
}

.edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(64, 158, 255, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  z-index: 10;
}

.draggable-module:hover .edit-overlay {
  opacity: 1;
}

.edit-overlay .el-icon {
  font-size: 32px;
  color: #409EFF;
}

.edit-overlay span {
  color: #409EFF;
  font-size: 14px;
  font-weight: 500;
}
</style>
