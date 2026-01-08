<template>
  <el-row :gutter="20" class="stat-row">
    <el-col :xs="12" :sm="8" :md="24/6" v-for="i in 6" :key="i" class="stat-card-item">
      <el-card shadow="hover" class="stat-card-skeleton">
        <div class="stat-content">
          <el-skeleton animated :loading="true">
            <template #template>
              <div class="skeleton-icon"></div>
              <div class="skeleton-info">
                <el-skeleton-item variant="text" style="width: 60%; height: 14px; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 40%; height: 32px;" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}

.stat-card-skeleton {
  border-radius: 12px;
  border: none;
}

.stat-card-skeleton :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.skeleton-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(90deg, #f2f2f2 25%, #e6e6e6 37%, #f2f2f2 63%);
  animation: skeleton-loading 1.4s ease infinite;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
  min-width: 0;
}

@keyframes skeleton-loading {
  0% {
    background-position: -100% 0;
  }
  100% {
    background-position: 100% 0;
  }
}
</style>
