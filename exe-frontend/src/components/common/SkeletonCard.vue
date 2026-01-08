<template>
  <el-card :shadow="shadow" :class="['skeleton-card', customClass]">
    <template v-if="showHeader" #header>
      <div class="skeleton-header">
        <el-skeleton :rows="0" animated>
          <template #template>
            <el-skeleton-item variant="text" style="width: 30%" />
          </template>
        </el-skeleton>
      </div>
    </template>
    <div :class="['skeleton-content', contentClass]">
      <el-skeleton :rows="rows" animated :loading="true">
        <template #template>
          <slot name="skeleton">
            <el-skeleton-item
              v-for="i in rows"
              :key="i"
              variant="text"
              :style="{ width: getWidth(i) }"
            />
          </slot>
        </template>
      </el-skeleton>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  rows?: number;
  shadow?: 'always' | 'hover' | 'never';
  showHeader?: boolean;
  customClass?: string;
  contentClass?: string;
}

const props = withDefaults(defineProps<Props>(), {
  rows: 3,
  shadow: 'never',
  showHeader: false,
  customClass: '',
  contentClass: ''
});

const getWidth = (index: number) => {
  const widths = ['100%', '80%', '90%', '70%', '95%'];
  return widths[(index - 1) % widths.length];
};
</script>

<style scoped>
.skeleton-card {
  border-radius: 12px;
  border: none;
}

.skeleton-header {
  padding: 4px 0;
}

.skeleton-content {
  padding: 8px 0;
}
</style>
