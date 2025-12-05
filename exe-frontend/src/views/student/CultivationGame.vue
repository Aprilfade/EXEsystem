<template>
  <div class="cultivation-container">
    <div v-if="isBreaking" class="effect-overlay lightning-effect"></div>

    <div class="game-panel" :class="{ shake: shakeEffect }">
      <div class="panel-left">
        <div class="character-box">
          <div class="meditation-visual" :class="'aura-' + Math.min(profile.realmLevel || 0, 9)">
            🧘
          </div>
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

          <div class="action-card highlight" @click="handleBreakthrough" :class="{ disabled: !canBreak, 'pulse-anim': canBreak }">
            <div class="icon">⚡</div>
            <div class="name">境界突破</div>
            <div class="desc" v-if="canBreak">瓶颈松动 (成功率: {{ successChance }}%)</div>
            <div class="desc" v-else>修为不足，需积累至 {{ profile.maxExp }}</div>
          </div>
        </div>

        <div class="log-box" ref="logBoxRef">
          <div class="log-title">
            修仙日志
            <el-button link size="small" @click="clearLogs" style="float: right; padding: 0;">清空</el-button>
          </div>
          <transition-group name="list" tag="div">
            <div v-for="log in logs" :key="log.id" class="log-item" :class="log.type">
              {{ log.content }}
            </div>
          </transition-group>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { fetchGameProfile, breakthrough, meditate } from '@/api/game';
import { ElMessage, ElNotification } from 'element-plus';
import { fetchGameProfile, breakthrough, meditate, breakthroughWithQuiz } from '@/api/game'; // 导入新API
import { fetchPracticeQuestions } from '@/api/studentAuth'; // 导入获取题目API
import request from '@/utils/request'; // 如果 fetchPracticeQuestions 没有导出，可以用 request


// 定义日志接口结构
interface LogItem {
  id: number;
  content: string;
  type: 'info' | 'success' | 'danger' | 'event';
}

const profile = ref<any>({ currentExp: 0, maxExp: 100, attack: 0, defense: 0, realmLevel: 0 });
const realmName = ref('凡人');
const logs = ref<LogItem[]>([]);
// 【新增】天劫试炼相关状态
const tribulationVisible = ref(false);
const tribulationQuestion = ref<any>(null);
const selectedAnswer = ref('');
const tribulationLoading = ref(false);
// 视觉状态控制
const isBreaking = ref(false);
const shakeEffect = ref(false);

const expPercentage = computed(() => {
  if (profile.value.maxExp === 0) return 100;
  let p = (profile.value.currentExp / profile.value.maxExp) * 100;
  return p > 100 ? 100 : p;
});

const canBreak = computed(() => profile.value.currentExp >= profile.value.maxExp);

// 纯前端展示用的成功率，增加沉浸感 (与后端配置保持一致，给用户心理预期)
const successChance = computed(() => {
  const rates = [100, 90, 80, 70, 60, 50, 40, 30, 20, 10];
  const lvl = profile.value.realmLevel || 0;
  return lvl < rates.length ? rates[lvl] : 10;
});

const formatExp = () => `${profile.value.currentExp}/${profile.value.maxExp}`;

// --- 日志系统 (持久化) ---

const loadLogs = () => {
  const saved = localStorage.getItem('cultivation_logs');
  if (saved) {
    try {
      logs.value = JSON.parse(saved);
    } catch (e) {
      logs.value = [];
    }
  } else {
    addLog('欢迎回到修仙界，道友请入座。', 'info');
  }
};

const addLog = (msg: string, type: 'info' | 'success' | 'danger' | 'event' = 'info') => {
  const time = new Date().toLocaleTimeString();
  logs.value.unshift({
    id: Date.now(), // 使用时间戳作为唯一key
    content: `[${time}] ${msg}`,
    type
  });
  // 只保留最近 50 条日志
  if (logs.value.length > 50) logs.value.pop();

  // 持久化存储
  localStorage.setItem('cultivation_logs', JSON.stringify(logs.value));
};

const clearLogs = () => {
  logs.value = [];
  localStorage.removeItem('cultivation_logs');
};

// --- 核心交互 ---

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
      addLog(res.data, 'info');
      loadData();
    }
  } catch(e){}
};
// 【修改】原本的 handleBreakthrough
const handleBreakthrough = async () => {
  if (!canBreak.value) {
    ElMessage.warning('修为不足，切勿急躁，以免走火入魔！');
    return;
  }

  // 1. 尝试获取“天劫题目”
  // 这里为了简单，随机获取一道单选题。为了增加难度，你可以指定 subjectId 或特定 tag
  try {
    tribulationLoading.value = true;
    // 假设获取当前用户年级的一道单选题
    // 注意：你需要确保后端 fetchPracticeQuestions 支持 size 参数，或者获取列表后取第一个
    const res = await fetchPracticeQuestions({
      subjectId: 1, // 这里的ID最好动态获取用户的科目，或者让用户选
      grade: '高三', // 同上
      size: 1,
      questionType: 1 // 只取单选
    } as any);

    if (res.code === 200 && res.data && res.data.length > 0) {
      tribulationQuestion.value = res.data[0];
      selectedAnswer.value = ''; // 重置答案
      tribulationVisible.value = true; // 打开弹窗
    } else {
      // 获取题目失败（可能题库空了），降级为普通突破
      doDirectBreakthrough();
    }
  } catch (e) {
    console.error(e);
    doDirectBreakthrough();
  } finally {
    tribulationLoading.value = false;
  }
};
// 【新增】原有的直接突破逻辑（作为降级方案）
const doDirectBreakthrough = async () => {
  // ... 也就是你之前的 handleBreakthrough 里的 setTimeout ... 逻辑
  // 这里可以保留，用于当无法获取题目时的备选
  // 或者直接提示“天劫未至”（无法获取题目）
  ElMessage.info('今日天象平和，无需试炼，直接尝试突破...');
  // 接着走你原来的普通突破接口...
};

// 【新增】确认提交答案并突破
const confirmBreakthrough = async () => {
  if (!selectedAnswer.value) {
    ElMessage.warning('请选择心中的道（答案）！');
    return;
  }

  isBreaking.value = true; // 开启全屏特效

  // 模拟一点延迟，营造紧张感
  setTimeout(async () => {
    try {
      const res = await breakthroughWithQuiz({
        questionId: tribulationQuestion.value.id,
        answer: selectedAnswer.value
      });

      isBreaking.value = false;
      tribulationVisible.value = false; // 关闭弹窗

      if (res.code === 200) {
        ElNotification({
          title: '渡劫成功',
          message: res.data,
          type: 'success',
          duration: 6000
        });
        addLog(`[逆天改命] 答对天劫试题，${res.data}`, 'success');
        // 播放音效
        const audio = new Audio('/audio/win.mp3'); // 确保你有这个文件
        audio.play().catch(()=>{});
        loadData();
      }
    } catch (e: any) {
      isBreaking.value = false;
      tribulationVisible.value = false;
      triggerShake(); // 震动
      // 后端返回 400 会进入这里
      // 错误信息在 request 拦截器可能已经弹出了，这里记录日志
      addLog(`[道心破碎] ${e.message || '渡劫失败'}`, 'danger');
      const audio = new Audio('/audio/lose.mp3');
      audio.play().catch(()=>{});
      loadData(); // 刷新数据（因为扣了经验）
    }
  }, 1000);
};






const triggerShake = () => {
  shakeEffect.value = true;
  setTimeout(() => shakeEffect.value = false, 500);
}

onMounted(() => {
  loadLogs();
  loadData();
});
</script>

<style scoped>
/* 原有样式基础 */
.cultivation-container {
  padding: 20px;
  background: url('https://cdn.pixabay.com/photo/2016/11/14/03/46/fog-1822509_1280.jpg') center/cover no-repeat fixed;
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
  position: relative; /* 确保震动动画生效 */
}

/* === 新增特效样式 === */

/* 1. 震动动画 (受伤/失败) */
.shake {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}

/* 2. 呼吸灯光效 (可突破状态) */
.pulse-anim {
  animation: pulse-border 2s infinite;
  border-color: #e6a23c !important;
}

@keyframes pulse-border {
  0% { box-shadow: 0 0 0 0 rgba(230, 162, 60, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(230, 162, 60, 0); }
  100% { box-shadow: 0 0 0 0 rgba(230, 162, 60, 0); }
}

/* 3. 境界光环 (Visual Aura) */
.aura-0 { text-shadow: 0 0 10px #fff; } /* 凡人 */
.aura-1 { text-shadow: 0 0 15px #aaffaa; color: #aaffaa; } /* 炼气 */
.aura-2 { text-shadow: 0 0 20px #00ffff; color: #00ffff; } /* 筑基 */
.aura-3 { text-shadow: 0 0 25px #ffff00; color: #ffff00; } /* 金丹 */
.aura-4 { text-shadow: 0 0 30px #ffaa00; color: #ffaa00; } /* 元婴 */
.aura-5 { text-shadow: 0 0 35px #ff0000; color: #ff0000; } /* 化神及以上 */

/* 4. 闪电遮罩 (渡劫中) */
.lightning-effect {
  position: fixed; /* 覆盖全屏 */
  top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(255, 255, 255, 0.8);
  z-index: 9999;
  animation: lightning 0.2s infinite;
  pointer-events: none;
}

@keyframes lightning {
  0% { opacity: 0; background: #fff; }
  10% { opacity: 0.8; }
  20% { opacity: 0; background: #000; }
  30% { opacity: 0.5; }
  100% { opacity: 0; }
}

/* 左侧面板 */
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
  font-family: "Kaiti", "STKaiti", serif;
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

/* 右侧面板 */
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

.exp-section { margin-bottom: 30px; }
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
  border-color: #dcdfe6;
  background: #f5f7fa;
}

/* 日志区域样式优化 */
.log-box {
  flex: 1;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
  overflow-y: auto;
  border: 1px solid #eee;
  font-size: 13px;
}
.log-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.log-item {
  margin-bottom: 4px;
  border-bottom: 1px dashed #e0e0e0;
  padding-bottom: 2px;
  color: #606266;
}
/* 日志颜色区分 */
.log-item.success { color: #67C23A; font-weight: bold; }
.log-item.danger { color: #F56C6C; font-weight: bold; }
.log-item.event { color: #E6A23C; }

/* 列表动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 天劫弹窗样式 */
.tribulation-header {
  text-align: center;
  margin-bottom: 20px;
  animation: shake 2s infinite; /* 让文字微微颤抖，增加紧张感 */
}
.thunder-icon {
  font-size: 60px;
  margin-bottom: 10px;
}
.warning-text {
  color: #F56C6C;
  font-weight: bold;
  font-size: 16px;
  line-height: 1.5;
}

.exam-box {
  background: #2c3e50;
  padding: 20px;
  border-radius: 8px;
  color: #fff;
  border: 2px solid #e6a23c;
  box-shadow: 0 0 20px rgba(230, 162, 60, 0.3);
}
.q-tag {
  background: #f56c6c;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 8px;
  vertical-align: middle;
}
.q-content {
  font-size: 18px;
  margin-bottom: 20px;
  line-height: 1.6;
  font-weight: bold;
}

.options-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}
.opt-item {
  background: rgba(255,255,255,0.1);
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
  display: flex;
  align-items: center;
}
.opt-item:hover {
  background: rgba(255,255,255,0.2);
}
.opt-item.selected {
  background: rgba(230, 162, 60, 0.2);
  border-color: #e6a23c;
  box-shadow: 0 0 10px rgba(230, 162, 60, 0.4);
}
.opt-key {
  font-weight: bold;
  color: #e6a23c;
  margin-right: 10px;
  font-size: 18px;
}

.defy-btn {
  width: 100%;
  font-size: 18px;
  letter-spacing: 2px;
  background: linear-gradient(45deg, #f56c6c, #e6a23c);
  border: none;
}
.defy-btn:hover {
  opacity: 0.9;
  transform: scale(1.02);
}
</style>