<template>
  <div class="course-card-skeleton">
    <!-- 封面骨架 -->
    <div class="skeleton-cover">
      <el-skeleton-item variant="image" class="skeleton-image" />
    </div>

    <!-- 内容骨架 -->
    <div class="skeleton-content">
      <el-skeleton animated>
        <template #template>
          <!-- 标题 -->
          <el-skeleton-item variant="h3" style="width: 80%; margin-bottom: 12px;" />

          <!-- 元信息 -->
          <div style="display: flex; gap: 16px; margin-bottom: 12px;">
            <el-skeleton-item variant="text" style="width: 100px;" />
            <el-skeleton-item variant="text" style="width: 100px;" />
          </div>

          <!-- 描述 -->
          <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 8px;" />
          <el-skeleton-item variant="text" style="width: 60%; margin-bottom: 16px;" />

          <!-- 底部 -->
          <div style="display: flex; justify-content: space-between; padding-top: 12px; border-top: 1px solid #f0f0f0;">
            <el-skeleton-item variant="text" style="width: 60px;" />
            <el-skeleton-item variant="circle" style="width: 32px; height: 32px;" />
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>
</template>

<style scoped lang="scss">
.course-card-skeleton {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.skeleton-cover {
  height: 200px;
  background: #f5f7fa;

  .skeleton-image {
    width: 100%;
    height: 100%;
  }
}

.skeleton-content {
  padding: 20px;
  flex: 1;
}

@media (max-width: 768px) {
  .skeleton-cover {
    height: 160px;
  }

  .skeleton-content {
    padding: 16px;
  }
}
</style>
