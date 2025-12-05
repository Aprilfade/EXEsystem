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
          <div class="action-card" @click="handleMeditate" :class="{ 'glow-gold': lastEventType === 'LUCKY' }">
            <div class="icon">🧘</div>
            <div class="name">静心打坐</div>
            <div class="desc">机缘与风险并存</div>
          </div>

          <div class="action-card" @click="$router.push('/student/practice')">
            <div class="icon">⚔️</div>
            <div class="name">外出历练</div>
            <div class="desc">去题库刷题获取大量修为</div>
          </div>

          <div class="action-card highlight" @click="openBreakDialog" :class="{ disabled: !canBreak, 'pulse-anim': canBreak }">
            <div class="icon">⚡</div>
            <div class="name">境界突破</div>
            <div class="desc" v-if="canBreak">瓶颈松动 (点击渡劫)</div>
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

    <el-dialog v-model="showBreakDialog" title="⚡ 渡劫准备" width="420px" append-to-body>
      <div class="break-modal">
        <div class="info-row">
          <span>当前境界：</span><strong>{{ realmName }}</strong>
        </div>
        <div class="info-row">
          <span>基础成功率：</span><span class="rate-text">{{ baseSuccessRate }}%</span>
        </div>

        <el-divider content-position="left">天材地宝辅助</el-divider>

        <el-select v-model="selectedPillId" placeholder="选择丹药护体 (可选)" clearable style="width: 100%">
          <el-option
              v-for="pill in myPills"
              :key="pill.id"
              :label="pill.name + ' (成功率+' + (parseFloat(pill.resourceValue)*100).toFixed(0) + '%)'"
              :value="pill.id"
          />
        </el-select>
        <div v-if="myPills.length === 0" class="no-pill-tip">
          背包空空如也，可去 <el-button link type="primary" @click="$router.push('/student/points-mall')">积分商城</el-button> 兑换
        </div>

        <div class="final-rate-box">
          预计最终成功率：
          <span :class="finalRate > 80 ? 'high-rate' : (finalRate < 40 ? 'low-rate' : 'mid-rate')">
             {{ finalRate }}%
           </span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showBreakDialog = false">暂缓</el-button>
          <el-button type="danger" :loading="breaking" @click="confirmBreakthroughWithItem">
            逆天而行 (开始)
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import {
  fetchGameProfile,
  meditate,
  breakthroughWithItem,
  fetchMyPills
} from '@/api/game';

// 定义接口
interface LogItem {
  id: number;
  content: string;
  type: 'info' | 'success' | 'danger' | 'event';
}

// 状态变量
const profile = ref<any>({ currentExp: 0, maxExp: 100, attack: 0, defense: 0, realmLevel: 0 });
const realmName = ref('凡人');
const logs = ref<LogItem[]>([]);
const isBreaking = ref(false);
const shakeEffect = ref(false);
const lastEventType = ref(''); // 控制打坐特效

// 突破弹窗相关
const showBreakDialog = ref(false);
const myPills = ref<any[]>([]);
const selectedPillId = ref<number | undefined>(undefined);
const breaking = ref(false);

// 计算属性
const expPercentage = computed(() => {
  if (profile.value.maxExp === 0) return 100;
  let p = (profile.value.currentExp / profile.value.maxExp) * 100;
  return p > 100 ? 100 : p;
});

const canBreak = computed(() => (profile.value.currentExp || 0) >= (profile.value.maxExp || 1));

const formatExp = () => `${profile.value.currentExp}/${profile.value.maxExp}`;

// 基础成功率 (需与后端 RealmEnum 逻辑保持一致)
const baseSuccessRate = computed(() => {
  const lvl = profile.value.realmLevel || 0;

  // 【修复】核心修改：先除以 10 获取大境界索引，再查表
  const realmIndex = Math.floor(lvl / 10);

  // 对应 RealmEnum:
  // 0:凡人(100%), 1:炼气(90%), 2:筑基(80%), 3:金丹(70%), 4:元婴(60%)
  // 5:化神(50%), 6:炼虚(40%), 7:合体(30%), 8:大乘(20%), 9:渡劫(10%)
  const rates = [100, 90, 80, 70, 60, 50, 40, 30, 20, 10];

  if (realmIndex < rates.length) {
    return rates[realmIndex];
  }
  return 0; // 飞升后或异常情况
});

// 计算最终成功率
const finalRate = computed(() => {
  let rate = baseSuccessRate.value;
  if (selectedPillId.value) {
    const pill = myPills.value.find((p: any) => p.id === selectedPillId.value);
    if (pill) {
      rate += (parseFloat(pill.resourceValue) * 100);
    }
  }
  return Math.min(95, rate); // 封顶95%
});

// --- 方法实现 ---

const loadData = async () => {
  const res = await fetchGameProfile();
  if (res.code === 200) {
    profile.value = res.data.data;
    realmName.value = res.data.realmName;

    // 【新增】处理离线收益
    const afk = res.data.afkReward;
    if (afk && afk !== 'NONE') {
      ElNotification({
        title: '闭关收益',
        message: `道友离线闭关 ${afk.minutes} 分钟，自动运转周天，获得修为 +${afk.exp}`,
        type: 'success',
        duration: 6000
      });
      addLog(`[离线] 闭关 ${afk.minutes} 分钟，获得修为 +${afk.exp}`, 'success');
    }
  }
};

const handleMeditate = async () => {
  try {
    const res = await meditate();
    if (res.code === 200) {
      // 后端返回结构: { msg: string, type: string }
      const data = res.data;

      // 触发特效
      lastEventType.value = data.type;
      setTimeout(() => lastEventType.value = '', 1000);

      if (data.type === 'LUCKY') {
        ElMessage.success(data.msg);
        addLog(data.msg, 'event'); // 金色日志
      } else if (data.type === 'BAD') {
        ElMessage.error(data.msg);
        triggerShake();
        addLog(data.msg, 'danger');
      } else {
        ElMessage.info(data.msg);
        addLog(data.msg, 'info');
      }
      loadData();
    }
  } catch (e) {
    // 错误处理
  }
};

// 打开突破弹窗
const openBreakDialog = async () => {
  if (!canBreak.value) {
    ElMessage.warning('修为不足，切勿急躁！');
    return;
  }
  // 获取背包中的丹药
  try {
    const res = await fetchMyPills();
    if (res.code === 200) {
      myPills.value = res.data || [];
    }
  } catch (e) {
    myPills.value = [];
  }

  selectedPillId.value = undefined;
  showBreakDialog.value = true;
};

// 执行突破 (使用道具)
const confirmBreakthroughWithItem = async () => {
  breaking.value = true;
  try {
    const res = await breakthroughWithItem({
      goodsId: selectedPillId.value
    });

    if (res.code === 200) {
      showBreakDialog.value = false;
      isBreaking.value = true; // 播放全屏闪电特效

      // 延迟显示结果，配合动画
      setTimeout(() => {
        isBreaking.value = false;
        ElNotification({
          title: '渡劫成功',
          message: res.data,
          type: 'success',
          duration: 5000
        });
        addLog(res.data, 'success');
        // 播放音效 (如果有)
        // const audio = new Audio('/audio/win.mp3'); audio.play().catch(()=>{});
        loadData();
      }, 1500);
    }
  } catch (e: any) {
    showBreakDialog.value = false;
    triggerShake();
    addLog(`[道心破碎] ${e.message || '渡劫失败'}`, 'danger');
    loadData();
  } finally {
    breaking.value = false;
  }
};

// --- 日志与辅助 ---
const addLog = (msg: string, type: 'info' | 'success' | 'danger' | 'event' = 'info') => {
  const time = new Date().toLocaleTimeString();
  logs.value.unshift({
    id: Date.now(),
    content: `[${time}] ${msg}`,
    type
  });
  if (logs.value.length > 50) logs.value.pop();
  localStorage.setItem('cultivation_logs', JSON.stringify(logs.value));
};

const loadLogs = () => {
  const saved = localStorage.getItem('cultivation_logs');
  if (saved) {
    try { logs.value = JSON.parse(saved); } catch (e) { logs.value = []; }
  } else {
    addLog('欢迎回到修仙界，道友请入座。', 'info');
  }
};

const clearLogs = () => {
  logs.value = [];
  localStorage.removeItem('cultivation_logs');
};

const triggerShake = () => {
  shakeEffect.value = true;
  setTimeout(() => shakeEffect.value = false, 500);
};

onMounted(() => {
  loadLogs();
  loadData();
});
</script>

<style scoped>
/* 原有基础样式保持不变... */
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
  position: relative;
}

/* === 新增特效样式 === */

/* 打坐奇遇金光特效 */
.glow-gold {
  animation: glow 0.8s ease-in-out;
  box-shadow: 0 0 20px #ffd700 !important;
  border-color: #ffd700 !important;
  background-color: #fffbf0 !important;
}
@keyframes glow {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); box-shadow: 0 0 30px #ffd700; }
  100% { transform: scale(1); }
}

/* 震动动画 */
.shake {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}
@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}

/* 呼吸灯 */
.pulse-anim {
  animation: pulse-border 2s infinite;
  border-color: #e6a23c !important;
}
@keyframes pulse-border {
  0% { box-shadow: 0 0 0 0 rgba(230, 162, 60, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(230, 162, 60, 0); }
  100% { box-shadow: 0 0 0 0 rgba(230, 162, 60, 0); }
}

/* 境界光环 */
.aura-0 { text-shadow: 0 0 10px #fff; }
.aura-1 { text-shadow: 0 0 15px #aaffaa; color: #aaffaa; }
.aura-2 { text-shadow: 0 0 20px #00ffff; color: #00ffff; }
.aura-3 { text-shadow: 0 0 25px #ffff00; color: #ffff00; }
.aura-4 { text-shadow: 0 0 30px #ffaa00; color: #ffaa00; }
.aura-5 { text-shadow: 0 0 35px #ff0000; color: #ff0000; }

/* 闪电 */
.lightning-effect {
  position: fixed;
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

/* 日志区域 */
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
}
.log-item {
  margin-bottom: 4px;
  border-bottom: 1px dashed #e0e0e0;
  padding-bottom: 2px;
  color: #606266;
}
.log-item.success { color: #67C23A; font-weight: bold; }
.log-item.danger { color: #F56C6C; font-weight: bold; }
.log-item.event { color: #E6A23C; font-weight: bold; }

/* 列表动画 */
.list-enter-active, .list-leave-active { transition: all 0.5s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateX(-20px); }

/* 弹窗样式 */
.break-modal { padding: 10px; font-size: 16px; }
.info-row { margin-bottom: 10px; }
.rate-text { font-weight: bold; color: #F56C6C; }
.no-pill-tip { font-size: 12px; color: #909399; margin-top: 8px; }
.final-rate-box {
  margin-top: 25px;
  text-align: right;
  font-size: 15px;
  border-top: 1px solid #eee;
  padding-top: 15px;
}
.high-rate { color: #67C23A; font-weight: bold; font-size: 22px; }
.mid-rate { color: #E6A23C; font-weight: bold; font-size: 22px; }
.low-rate { color: #F56C6C; font-weight: bold; font-size: 22px; }
</style>