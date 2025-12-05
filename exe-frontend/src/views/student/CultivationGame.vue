<template>
  <div class="cultivation-container">
    <div class="game-panel">
      <div class="panel-left">
        <div class="character-box">
          <div class="meditation-visual">🧘</div>
          <div class="realm-title">{{ realmName }}</div>
        </div>

        <div class="stats-box">
          <div class="stat-row">
            <span>攻击力(道法):</span> <strong>{{ profile.attack }}</strong>
          </div>
          <div class="stat-row">
            <span>防御力(道心):</span> <strong>{{ profile.defense }}</strong>
          </div>
        </div>
      </div>

      <div class="panel-right">
        <h2 class="sect-title">我的洞府</h2>

        <div class="exp-section">
          <div class="exp-label">修为进度 ({{ profile.currentExp }} / {{ profile.maxExp }})</div>
          <el-progress
              :percentage="expPercentage"
              :format="formatExp"
              :stroke-width="15"
              striped
              striped-flow
              color="#67C23A"
          />
        </div>

        <div class="action-grid">
          <div class="action-card" @click="handleMeditate">
            <div class="icon">🧘</div>
            <div class="name">静心打坐</div>
            <div class="desc">少量获取修为</div>
          </div>

          <div class="action-card" @click="$router.push('/student/practice')">
            <div class="icon">⚔️</div>
            <div class="name">外出历练</div>
            <div class="desc">去题库刷题获取大量修为</div>
          </div>

          <div class="action-card highlight" @click="handleBreakthrough" :class="{ disabled: !canBreak }">
            <div class="icon">⚡</div>
            <div class="name">境界突破</div>
            <div class="desc" v-if="canBreak">瓶颈松动，点击突破！</div>
            <div class="desc" v-else>修为不足，需积累至 {{ profile.maxExp }}</div>
          </div>
        </div>

        <div class="log-box">
          <div class="log-title">修仙日志</div>
          <div v-for="(log, i) in logs" :key="i" class="log-item">{{ log }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { fetchGameProfile, breakthrough, meditate } from '@/api/game';
import { ElMessage, ElNotification } from 'element-plus';

const profile = ref<any>({ currentExp: 0, maxExp: 100, attack: 0, defense: 0 });
const realmName = ref('凡人');
const logs = ref<string[]>(['欢迎回到修仙界，道友请入座。']);

const expPercentage = computed(() => {
  if (profile.value.maxExp === 0) return 100;
  let p = (profile.value.currentExp / profile.value.maxExp) * 100;
  return p > 100 ? 100 : p;
});

const canBreak = computed(() => profile.value.currentExp >= profile.value.maxExp);

const formatExp = () => `${profile.value.currentExp}/${profile.value.maxExp}`;

const loadData = async () => {
  const res = await fetchGameProfile();
  if (res.code === 200) {
    profile.value = res.data.data;
    realmName.value = res.data.realmName;
  }
};

const handleMeditate = async () => {
  try {
    const res = await meditate();
    if(res.code === 200) {
      ElMessage.success(res.data);
      addLog(res.data);
      loadData();
    }
  } catch(e){}
};

const handleBreakthrough = async () => {
  if (!canBreak.value) {
    ElMessage.warning('修为不足，切勿急躁，以免走火入魔！');
    return;
  }
  try {
    const res = await breakthrough();
    if (res.code === 200) {
      ElNotification({
        title: '渡劫成功',
        message: res.data,
        type: 'success',
        duration: 5000
      });
      addLog(`[大事件] ${res.data}`);
      loadData();
    } else {
      ElMessage.error(res.msg);
    }
  } catch (e) {}
};

const addLog = (msg: string) => {
  const time = new Date().toLocaleTimeString();
  logs.value.unshift(`[${time}] ${msg}`);
};

onMounted(loadData);
</script>

<style scoped>
.cultivation-container {
  padding: 20px;
  background: url('https://cdn.pixabay.com/photo/2016/11/14/03/46/fog-1822509_1280.jpg') center/cover no-repeat fixed; /* 建议换成中国风水墨背景 */
  min-height: calc(100vh - 60px);
  display: flex;
  justify-content: center;
  align-items: center;
}

.game-panel {
  width: 900px;
  height: 600px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
  overflow: hidden;
}

.panel-left {
  width: 300px;
  background: linear-gradient(180deg, #2c3e50 0%, #000000 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
}

.meditation-visual {
  font-size: 80px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

.realm-title {
  font-size: 28px;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
  margin-bottom: 40px;
  font-family: "Kaiti", "STKaiti", serif; /* 楷体更有修仙感 */
}

.stats-box {
  width: 100%;
  background: rgba(255,255,255,0.1);
  padding: 20px;
  border-radius: 8px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 16px;
}

.panel-right {
  flex: 1;
  padding: 30px;
  display: flex;
  flex-direction: column;
}

.sect-title {
  margin: 0 0 20px 0;
  color: #333;
  border-left: 5px solid #409eff;
  padding-left: 10px;
}

.exp-section {
  margin-bottom: 30px;
}
.exp-label { margin-bottom: 8px; color: #666; font-size: 14px; }

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.action-card {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.action-card .icon { font-size: 32px; margin-bottom: 5px; }
.action-card .name { font-weight: bold; color: #303133; }
.action-card .desc { font-size: 12px; color: #909399; margin-top: 4px; }

.action-card.highlight {
  border-color: #e6a23c;
  background: #fdf6ec;
}
.action-card.highlight.disabled {
  filter: grayscale(100%);
  opacity: 0.7;
  cursor: not-allowed;
}

.log-box {
  flex: 1;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
  overflow-y: auto;
  border: 1px solid #eee;
  font-size: 13px;
  color: #606266;
}
.log-title { font-weight: bold; margin-bottom: 5px; color: #303133; }
.log-item { margin-bottom: 4px; border-bottom: 1px dashed #e0e0e0; padding-bottom: 2px; }
</style>