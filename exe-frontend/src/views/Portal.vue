<template>
  <div class="portal-wrapper">
    <div class="portal-container">
      <h1 class="portal-title">欢迎使用综合系统平台</h1>
      <p class="portal-subtitle">请选择您要进入的系统</p>
      <div class="system-grid">
        <div class="system-card" @click="navigateTo('/home')">
          <el-icon :size="48" color="#409eff"><Management /></el-icon>
          <h2 class="system-name">试题管理系统</h2>
          <p class="system-description">（教师/管理员端）高效管理科目、知识点、试题与试卷。</p>
        </div>

        <div class="system-card" @click="navigateTo('/student/dashboard')">
          <el-icon :size="48" color="#67c23a"><School /></el-icon>
          <h2 class="system-name">在线学习系统</h2>
          <p class="system-description">（学生端）进行在线练习、查看错题、参加模拟考试。</p>
        </div>

        <div class="system-card" @click="navigateToExternal('http://localhost:8081')">
          <el-icon :size="48" color="#90939"><DataAnalysis /></el-icon>
          <h2 class="system-name">仓库管理系统</h2>
          <p class="system-description">为学校搭建的仓库管理系统，用于管理学校物品、设备、资产等。</p>
        </div>

        <div class="system-card" @click="navigateToExternal('https://www.bilibili.com/')">
          <el-icon :size="48" color="#00a1d6"><VideoPlay /></el-icon>
          <h2 class="system-name">哔哩哔哩</h2>
          <p class="system-description">丰富的学习资源与知识分享社区，寓教于乐。</p>
        </div>

        <div class="system-card" @click="navigateToExternal('https://www.douyin.com/')">
          <el-icon :size="48" color="#fe2c55"><Headset /></el-icon>
          <h2 class="system-name">抖音</h2>
          <p class="system-description">发现生活中的美好，获取新鲜资讯与创意灵感。</p>
        </div>

        <div class="system-card" @click="navigateToExternal('https://store.steampowered.com/')">
          <el-icon :size="48" color="#171a21"><Monitor /></el-icon>
          <h2 class="system-name">Steam</h2>
          <p class="system-description">全球领先的数字游戏发行平台，放松身心的好去处。</p>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Management, School, DataAnalysis, VideoPlay, Monitor, Headset } from '@element-plus/icons-vue';

const router = useRouter();

// 跳转到内部系统（会触发登录）
const navigateTo = (path: string) => {
  router.push(path);
};

// 跳转到外部系统
const navigateToExternal = (url: string) => {
  window.open(url, '_blank');
};
</script>

<style scoped>
/* 样式部分无需修改，保持原样 */
.portal-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: url("/back.jpg") center/cover no-repeat fixed;
}

.portal-container {
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.25);
  text-align: center;
  color: #fff;
  max-width: 900px;
}

.portal-title {
  font-size: 2.5rem;
  font-weight: 600;
  text-shadow: 1px 1px 5px rgba(0,0,0,0.3);
}

.portal-subtitle {
  font-size: 1.2rem;
  margin-top: 15px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.system-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.system-card {
  padding: 25px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 15px;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.system-card:hover {
  transform: translateY(-10px);
  background-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 12px 20px 0 rgba(31, 38, 135, 0.3);
}

.system-card.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
.system-card.disabled:hover {
  transform: none;
  background-color: rgba(255, 255, 255, 0.2);
  box-shadow: none;
}

.system-name {
  font-size: 1.4rem;
  margin: 15px 0 10px;
}

.system-description {
  font-size: 0.9rem;
  opacity: 0.8;
  line-height: 1.5;
  flex-grow: 1;
}
</style>