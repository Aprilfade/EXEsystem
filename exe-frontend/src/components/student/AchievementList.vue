<template>
  <div class="achievement-grid">
    <div
        v-for="item in list"
        :key="item.id"
        class="ach-item"
        :class="{ locked: !item.isUnlocked }"
    >
      <div class="icon-box">
        <div class="icon">{{ item.iconUrl || '🏅' }}</div>
        <div v-if="!item.isUnlocked" class="lock-overlay"><el-icon><Lock /></el-icon></div>
      </div>
      <div class="info">
        <div class="name">{{ item.name }}</div>
        <div class="desc">{{ item.description }}</div>
        <div class="progress" v-if="!item.isUnlocked">
          目标: {{ item.threshold }}
        </div>
        <div class="date" v-else>
          {{ formatDate(item.unlockTime) }} 解锁
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue';

defineProps<{ list: any[] }>();

const formatDate = (str: string) => {
  return str ? str.split('T')[0] : '';
};
</script>

<style scoped>
.achievement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 15px;
}
.ach-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px 10px;
  text-align: center;
  transition: all 0.3s;
}
.ach-item:not(.locked):hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  border-color: #FFD700;
}
.ach-item.locked {
  background: #f5f7fa;
  filter: grayscale(100%);
  opacity: 0.7;
}
.icon-box {
  font-size: 40px;
  margin-bottom: 8px;
  position: relative;
  height: 50px;
}
.lock-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  color: #909399;
}
.name { font-weight: bold; font-size: 14px; color: #303133; margin-bottom: 4px; }
.desc { font-size: 12px; color: #909399; line-height: 1.4; margin-bottom: 5px; height: 34px; overflow: hidden; }
.progress, .date { font-size: 12px; color: #67C23A; }
.ach-item.locked .progress { color: #909399; }
</style>