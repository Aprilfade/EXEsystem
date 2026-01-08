<script setup lang="ts">
/**
 * ResponsiveTable - 响应式表格组件
 * 桌面端显示为标准表格，移动端显示为卡片列表
 */
import { computed } from 'vue'
import { useResponsive } from '@/composables/useResponsive'

interface Props {
  data: any[]
  loading?: boolean
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  emptyText: '暂无数据'
})

const { isMobile } = useResponsive()

// 继承父组件的属性（用于 el-table）
defineOptions({
  inheritAttrs: false
})
</script>

<template>
  <div class="responsive-table-wrapper">
    <!-- 桌面端：标准表格 -->
    <el-table
      v-if="!isMobile"
      v-bind="$attrs"
      :data="props.data"
      :loading="props.loading"
    >
      <slot />
    </el-table>

    <!-- 移动端：卡片列表 -->
    <div v-else class="card-list" v-loading="props.loading">
      <el-card
        v-for="(row, index) in props.data"
        :key="index"
        class="mobile-card"
        shadow="hover"
      >
        <!-- 使用具名插槽渲染卡片内容 -->
        <slot name="card" :row="row" :index="index">
          <!-- 默认卡片渲染 - 如果未提供 card 插槽 -->
          <div class="card-default">
            <div v-for="(value, key) in row" :key="key" class="card-row">
              <span class="card-label">{{ key }}:</span>
              <span class="card-value">{{ value }}</span>
            </div>
          </div>
        </slot>
      </el-card>

      <!-- 空状态 -->
      <el-empty
        v-if="props.data.length === 0 && !props.loading"
        :description="props.emptyText"
      />
    </div>
  </div>
</template>

<style scoped>
.responsive-table-wrapper {
  width: 100%;
}

/* 移动端卡片列表样式 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.mobile-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.mobile-card:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 默认卡片布局 */
.card-default {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

.card-row:last-child {
  border-bottom: none;
}

.card-label {
  font-weight: 500;
  color: #606266;
  font-size: 13px;
  flex-shrink: 0;
  margin-right: 12px;
}

.card-value {
  color: #303133;
  font-size: 14px;
  text-align: right;
  flex: 1;
  word-break: break-word;
}

/* 空状态样式 */
:deep(.el-empty) {
  padding: 40px 0;
}
</style>
