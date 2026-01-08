<template>
  <div class="virtual-list" ref="containerRef" @scroll="handleScroll">
    <div class="virtual-list-phantom" :style="{ height: totalHeight + 'px' }"></div>
    <div class="virtual-list-content" :style="{ transform: `translateY(${offsetY}px)` }">
      <div
        v-for="item in visibleData"
        :key="getItemKey(item)"
        class="virtual-list-item"
        :style="{ height: itemHeight + 'px' }"
      >
        <slot :item="item" :index="item.index"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';

interface Props {
  data: any[]; // 列表数据
  itemHeight: number; // 每项的高度（px）
  visibleCount?: number; // 可见项数量（可选，会自动计算）
  bufferSize?: number; // 缓冲区大小（上下各渲染多少项）
  itemKey?: string; // 用于 key 的字段名，默认为 'id'
}

const props = withDefaults(defineProps<Props>(), {
  visibleCount: 10,
  bufferSize: 3,
  itemKey: 'id'
});

const containerRef = ref<HTMLElement | null>(null);
const scrollTop = ref(0);
const containerHeight = ref(0);

// 计算总高度
const totalHeight = computed(() => props.data.length * props.itemHeight);

// 计算可见区域的项数
const actualVisibleCount = computed(() => {
  if (containerHeight.value > 0) {
    return Math.ceil(containerHeight.value / props.itemHeight);
  }
  return props.visibleCount;
});

// 计算起始索引
const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemHeight);
  return Math.max(0, index - props.bufferSize);
});

// 计算结束索引
const endIndex = computed(() => {
  const index = startIndex.value + actualVisibleCount.value + props.bufferSize * 2;
  return Math.min(props.data.length, index);
});

// 计算可见数据
const visibleData = computed(() => {
  return props.data.slice(startIndex.value, endIndex.value).map((item, idx) => ({
    ...item,
    index: startIndex.value + idx
  }));
});

// 计算偏移量
const offsetY = computed(() => startIndex.value * props.itemHeight);

// 滚动处理
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement;
  scrollTop.value = target.scrollTop;
};

// 获取项的 key
const getItemKey = (item: any) => {
  return item[props.itemKey] || item.index;
};

// 监听容器大小变化
let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
  if (!containerRef.value) return;

  // 初始化容器高度
  containerHeight.value = containerRef.value.clientHeight;

  // 监听容器大小变化
  if ('ResizeObserver' in window) {
    resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        containerHeight.value = entry.contentRect.height;
      }
    });
    resizeObserver.observe(containerRef.value);
  }
});

onUnmounted(() => {
  resizeObserver?.disconnect();
  resizeObserver = null;
});

// 暴露方法：滚动到指定索引
const scrollToIndex = (index: number) => {
  if (!containerRef.value) return;
  const targetScrollTop = index * props.itemHeight;
  containerRef.value.scrollTop = targetScrollTop;
};

// 暴露方法：滚动到顶部
const scrollToTop = () => {
  if (!containerRef.value) return;
  containerRef.value.scrollTop = 0;
};

// 暴露方法：滚动到底部
const scrollToBottom = () => {
  if (!containerRef.value) return;
  containerRef.value.scrollTop = totalHeight.value;
};

defineExpose({
  scrollToIndex,
  scrollToTop,
  scrollToBottom
});
</script>

<style scoped>
.virtual-list {
  position: relative;
  overflow-y: auto;
  height: 100%;
  width: 100%;
}

.virtual-list-phantom {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  z-index: -1;
}

.virtual-list-content {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
}

.virtual-list-item {
  box-sizing: border-box;
}

/* 滚动条样式优化 */
.virtual-list::-webkit-scrollbar {
  width: 6px;
}

.virtual-list::-webkit-scrollbar-track {
  background: var(--bg-secondary, #f5f5f5);
  border-radius: 3px;
}

.virtual-list::-webkit-scrollbar-thumb {
  background: var(--border-color, #dcdfe6);
  border-radius: 3px;
}

.virtual-list::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary, #909399);
}
</style>
