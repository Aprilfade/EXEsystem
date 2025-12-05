<template>
  <div class="battle-container">

    <div v-if="battleStore.gameState === 'IDLE'" class="lobby-layer">
      <div class="lobby-content">
        <h1 class="game-title">知识竞技场</h1>

        <div class="rank-panel">
          <div class="panel-header">🏆 赛季排行榜</div>
          <div class="rank-list" v-loading="rankLoading">
            <div v-for="(item, index) in rankList" :key="index" class="rank-item">
              <div class="rank-idx" :class="'top-'+(index+1)">{{ index + 1 }}</div>
              <div class="rank-user">
                <UserAvatar
                    :src="item.avatar"
                    :name="item.name"
                    :size="36"
                    :frame-style="item.avatarFrameStyle"
                />
                <div class="user-info">
                  <span class="u-name">{{ item.name }}</span>
                  <span class="u-tier" :class="item.tier">{{ item.tierName }}</span>
                </div>
              </div>
              <div class="rank-score">{{ item.points }} pts</div>
            </div>
          </div>
        </div>

        <div class="lobby-actions">
          <div class="my-status">
            我的积分: <span>{{ myPoints }}</span>
          </div>
          <el-button type="primary" size="large" class="start-btn" @click="handleStartMatch">
            开始匹配
          </el-button>
          <el-button text class="history-btn" @click="openHistory">
            <el-icon><Trophy /></el-icon> 查看近期战绩
          </el-button>
        </div>

        <el-dialog v-model="historyVisible" title="⚔️ 近期战绩" width="500px" append-to-body class="battle-history-dialog">
          <el-table :data="historyList" stripe style="width: 100%" v-loading="historyLoading">
            <el-table-column prop="createTime" label="时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="opponentName" label="对手" />
            <el-table-column prop="result" label="结果" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.result === 'WIN'" type="success" effect="dark">胜利</el-tag>
                <el-tag v-else-if="row.result === 'LOSE'" type="danger" effect="dark">失败</el-tag>
                <el-tag v-else type="info" effect="dark">平局</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scoreChange" label="变动" width="80" align="right">
              <template #default="{ row }">
            <span :style="{ color: row.scoreChange > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
              {{ row.scoreChange > 0 ? '+' : '' }}{{ row.scoreChange }}
            </span>
              </template>
            </el-table-column>
          </el-table>
        </el-dialog>


      </div>
    </div>

    <div v-else-if="battleStore.gameState === 'MATCHING'" class="matching-layer">
      <div class="radar-scanner">
        <div class="scan-beam"></div>
        <el-icon class="search-icon" :size="48"><Search /></el-icon>
      </div>
      <h2 class="loading-text">正在寻找旗鼓相当的对手...</h2>
      <el-button round class="cancel-btn" @click="battleStore.leave">取消匹配</el-button>
    </div>

    <div v-else-if="['PLAYING', 'ROUND_RESULT'].includes(battleStore.gameState)" class="game-layer">
      <div class="players-header">
        <div class="player-card me">
          <div class="avatar-wrapper">
            <UserAvatar :src="authStore.student?.avatar" :name="authStore.studentName" :size="70" :frame-style="authStore.student?.avatarFrameStyle" />
            <transition name="score-float"><div v-if="showMyScoreAnim" class="floating-score">+20</div></transition>
          </div>
          <div class="info-block">
            <div class="name">我</div>
            <div class="progress-bg"><div class="progress-fill" :style="{ width: myScorePercent + '%' }"></div></div>
            <div class="score-text">{{ battleStore.myScore }}</div>
          </div>
        </div>
        <div class="vs-status">
          <div class="vs-logo">VS</div>
          <div class="round-badge">Round {{ battleStore.currentRound }} / {{ battleStore.totalRound }}</div>
        </div>
        <div class="player-card opponent">
          <div class="info-block align-right">
            <div class="name">{{ battleStore.opponent.name }}</div>
            <div class="progress-bg"><div class="progress-fill enemy" :style="{ width: oppScorePercent + '%' }"></div></div>
            <div class="score-text">{{ battleStore.oppScore }}</div>
          </div>
          <div class="avatar-wrapper">
            <UserAvatar :src="battleStore.opponent.avatar" :name="battleStore.opponent.name" :size="70" :frame-style="battleStore.opponent.avatarFrameStyle" />
            <transition name="score-float"><div v-if="showOppScoreAnim" class="floating-score enemy">+20</div></transition>
          </div>
        </div>
      </div>
      <div class="timer-section">
        <el-progress :percentage="timePercentage" :format="timeFormat" :status="timerStatus" :stroke-width="12" striped striped-flow text-inside />
      </div>
      <div class="question-board" :class="{ 'result-mode': battleStore.gameState === 'ROUND_RESULT' }">
        <div class="question-text">{{ battleStore.currentQuestion?.content }}</div>
        <div class="options-container">
          <div v-for="opt in parsedOptions" :key="opt.key" class="option-item" :class="getOptionClass(opt.key)" @click="handleAnswer(opt.key)">
            <span class="opt-tag">{{ opt.key }}</span>
            <span class="opt-content">{{ opt.value }}</span>
            <el-icon v-if="showResultIcon(opt.key) === 'correct'" class="status-icon correct"><Select /></el-icon>
            <el-icon v-if="showResultIcon(opt.key) === 'wrong'" class="status-icon wrong"><CloseBold /></el-icon>
          </div>
        </div>
        <div v-if="hasAnswered && battleStore.gameState === 'PLAYING'" class="waiting-tip">
          <el-icon class="is-loading"><Loading /></el-icon> 等待对手作答...
        </div>
      </div>
    </div>

    <div v-else-if="battleStore.gameState === 'GAME_OVER'" class="result-layer">
      <div class="result-content">
        <div class="result-anim">
          <span v-if="gameResult === 'YOU'" class="emoji-anim">🏆</span>
          <span v-if="gameResult === 'DRAW'" class="emoji-anim">🤝</span>
          <span v-if="gameResult === 'OPPONENT'" class="emoji-anim">😭</span>
        </div>
        <h1 :class="['result-title', gameResult]">{{ getResultTitle(gameResult) }}</h1>
        <div class="final-score-board">
          <div class="score-box me"><div class="label">我</div><div class="val">{{ battleStore.myScore }}</div></div>
          <div class="vs-divider">:</div>
          <div class="score-box opp"><div class="label">{{ battleStore.opponent.name }}</div><div class="val">{{ battleStore.oppScore }}</div></div>
        </div>
        <div class="action-btns">
          <el-button type="primary" size="large" round @click="battleStore.startMatch">再来一局</el-button>
          <el-button size="large" round @click="returnToLobby">返回大厅</el-button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useBattleStore } from '@/stores/battle';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { Search, Select, CloseBold, Loading } from '@element-plus/icons-vue';
import UserAvatar from '@/components/UserAvatar.vue';
import { fetchBattleLeaderboard, fetchMyBattleRecords } from '@/api/student'; // 【修改】引入 fetchMyBattleRecords
import { Trophy } from '@element-plus/icons-vue'; // 【新增】引入图标



// --- 【新增】战绩历史逻辑 ---
const historyVisible = ref(false);
const historyLoading = ref(false);
const historyList = ref([]);
const battleStore = useBattleStore();
const authStore = useStudentAuthStore();

// --- 【新增】排行榜相关逻辑 ---
const rankLoading = ref(false);
const rankList = ref<any[]>([]);
const myPoints = computed(() => authStore.student?.points || 0);

const loadLeaderboard = async () => {
  rankLoading.value = true;
  try {
    // 刷新个人信息以获取最新积分
    await authStore.fetchStudentInfo();
    const res = await fetchBattleLeaderboard();
    if(res.code === 200) {
      rankList.value = res.data;
    }
  } finally {
    rankLoading.value = false;
  }
};
const openHistory = async () => {
  historyVisible.value = true;
  historyLoading.value = true;
  try {
    const res = await fetchMyBattleRecords();
    if (res.code === 200) {
      historyList.value = res.data;
    }
  } finally {
    historyLoading.value = false;
  }
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(5, 16); // 只显示 MM-DD HH:mm
};
const handleStartMatch = () => {
  battleStore.startMatch();
}

const returnToLobby = () => {
  battleStore.gameState = 'IDLE'; // 强制回大厅
  loadLeaderboard(); // 刷新榜单
}

// ... (原有 audios, playSound, timer, computed properties 保持不变) ...
// 为了完整性，你需要把原文件 script 中除了 onMounted/onUnmounted 外的逻辑都保留在这里
const audios: Record<string, HTMLAudioElement> = {
  bgm: new Audio('/audio/battle_bgm.mp3'),
  correct: new Audio('/audio/correct.mp3'),
  wrong: new Audio('/audio/wrong.mp3'),
  win: new Audio('/audio/win.mp3'),
  lose: new Audio('/audio/lose.mp3'),
  match: new Audio('/audio/match.mp3')
};
const playSound = (name: string) => { const audio = audios[name]; if (audio) { audio.currentTime = 0; audio.play().catch(() => {}); } };
const TOTAL_TIME = 20;
const timeLeft = ref(TOTAL_TIME);
let timerInterval: any = null;
const timePercentage = computed(() => (timeLeft.value / TOTAL_TIME) * 100);
const timerStatus = computed(() => timeLeft.value <= 5 ? 'exception' : 'success');
const timeFormat = () => `${timeLeft.value}s`;
const startTimer = () => { clearInterval(timerInterval); timeLeft.value = TOTAL_TIME; timerInterval = setInterval(() => { if (timeLeft.value > 0) timeLeft.value--; else clearInterval(timerInterval); }, 1000); };
const myAnswer = ref('');
const showMyScoreAnim = ref(false);
const showOppScoreAnim = ref(false);
const maxScoreRef = computed(() => Math.max(100, battleStore.myScore + 40, battleStore.oppScore + 40));
const myScorePercent = computed(() => (battleStore.myScore / maxScoreRef.value) * 100);
const oppScorePercent = computed(() => (battleStore.oppScore / maxScoreRef.value) * 100);
const hasAnswered = computed(() => !!myAnswer.value);
const parsedOptions = computed(() => { try { return typeof battleStore.currentQuestion?.options === 'string' ? JSON.parse(battleStore.currentQuestion.options) : battleStore.currentQuestion?.options || []; } catch { return []; } });
const gameResult = computed(() => battleStore.finalResult?.result || '');
const getOptionClass = (key: string) => { if (battleStore.gameState === 'ROUND_RESULT') { const correctKey = battleStore.roundResult?.correctAnswer; if (key === correctKey) return 'is-correct'; if (key === myAnswer.value && key !== correctKey) return 'is-wrong'; return 'is-dimmed'; } if (key === myAnswer.value) return 'is-selected'; return ''; };
const showResultIcon = (key: string) => { if (battleStore.gameState !== 'ROUND_RESULT') return ''; const correctKey = battleStore.roundResult?.correctAnswer; if (key === correctKey) return 'correct'; if (key === myAnswer.value && key !== correctKey) return 'wrong'; return ''; };
const getResultTitle = (res: string) => { const map: Record<string, string> = { 'YOU': '大获全胜！', 'OPPONENT': '遗憾惜败...', 'DRAW': '平分秋色' }; return map[res] || '游戏结束'; };
const handleAnswer = (key: string) => { if (hasAnswered.value || battleStore.gameState !== 'PLAYING') return; myAnswer.value = key; battleStore.submitAnswer(key); };

watch(() => battleStore.currentQuestion, (newVal) => { if (newVal) { myAnswer.value = ''; showMyScoreAnim.value = false; showOppScoreAnim.value = false; startTimer(); } });
watch(() => battleStore.roundResult, (res) => { if (res) { clearInterval(timerInterval); if (res.isCorrect) { playSound('correct'); showMyScoreAnim.value = true; } else { playSound('wrong'); } if (res.oppAnswer === res.correctAnswer) { showOppScoreAnim.value = true; } } });
watch(() => battleStore.gameState, (state) => { if (state === 'GAME_OVER') { clearInterval(timerInterval); if (gameResult.value === 'YOU') playSound('win'); else playSound('lose'); } else if (state === 'MATCHING') { playSound('match'); } });

// --- 生命周期修改 ---
onMounted(() => {
  battleStore.connect();
  // 【修改】不再自动开始匹配，而是加载排行榜
  if (battleStore.gameState === 'IDLE') {
    loadLeaderboard();
  }
});

onUnmounted(() => {
  battleStore.leave();
  clearInterval(timerInterval);
});
</script>

<style scoped>
/* ... (保留原有样式) ... */
.battle-container {
  min-height: calc(100vh - 60px);
  background: radial-gradient(circle at center, #2b323c 0%, #141414 100%);
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  position: relative;
}

/* === 【新增】大厅样式 === */
.lobby-layer {
  width: 100%;
  max-width: 500px;
  text-align: center;
  animation: fadeIn 0.5s;
  display: flex;
  flex-direction: column;
  height: 80vh; /* 占据大部分高度 */
}
.game-title {
  font-size: 40px;
  font-weight: 900;
  margin-bottom: 20px;
  background: linear-gradient(to right, #409eff, #00f2f1);
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: 4px;
}
.rank-panel {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-bottom: 20px;
}
.panel-header {
  padding: 15px;
  background: rgba(0,0,0,0.2);
  font-weight: bold;
  color: #ffd700;
  font-size: 18px;
}
.rank-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}
.rank-item {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 8px;
  background: rgba(255,255,255,0.05);
  border-radius: 8px;
  transition: all 0.2s;
}
.rank-item:hover { background: rgba(255,255,255,0.1); transform: translateX(5px); }
.rank-idx {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  background: #555;
  color: #fff;
  font-weight: bold;
  margin-right: 10px;
  font-size: 12px;
}
.top-1 { background: #FFD700; color: #000; box-shadow: 0 0 10px #FFD700; }
.top-2 { background: #C0C0C0; color: #000; }
.top-3 { background: #CD7F32; color: #000; }

.rank-user {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.u-name { font-size: 14px; font-weight: bold; }
.u-tier { font-size: 12px; padding: 1px 4px; border-radius: 3px; margin-top: 2px; }
.u-tier.GOLD { color: #FFD700; border: 1px solid #FFD700; }
.u-tier.SILVER { color: #C0C0C0; border: 1px solid #C0C0C0; }
.u-tier.BRONZE { color: #CD7F32; border: 1px solid #CD7F32; }

.rank-score { font-weight: bold; color: #409eff; font-family: monospace; }

.lobby-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: center;
}
/* 在 .lobby-actions 样式附近添加 */
.history-btn {
  color: #ddd;
  font-size: 14px;
  margin-top: 10px;
}
.history-btn:hover {
  color: #409eff;
}

/* 优化 Dialog 样式（可选） */
:deep(.battle-history-dialog) {
  border-radius: 12px;
  overflow: hidden;
}
.my-status { color: #ccc; font-size: 14px; }
.my-status span { color: #fff; font-weight: bold; font-size: 16px; }
.start-btn {
  width: 200px;
  height: 50px;
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
  background: linear-gradient(45deg, #409eff, #36cfc9);
  border: none;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
  transition: transform 0.2s;
}
.start-btn:hover { transform: scale(1.05); }
.start-btn:active { transform: scale(0.95); }


/* === 复用原有的样式 (Matching, Game, Result) === */
/* 请将原文件 style 中关于 matching-layer, game-layer, result-layer 等样式粘贴在此处 */
.matching-layer { text-align: center; animation: fadeIn 0.5s; }
.radar-scanner { width: 140px; height: 140px; border-radius: 50%; border: 2px solid rgba(64, 158, 255, 0.3); position: relative; margin: 0 auto 40px; display: flex; justify-content: center; align-items: center; box-shadow: 0 0 30px rgba(64, 158, 255, 0.1); }
.scan-beam { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border-radius: 50%; background: conic-gradient(from 0deg, transparent 0deg, rgba(64, 158, 255, 0.5) 360deg); animation: radar-spin 1.5s linear infinite; }
.search-icon { color: #409eff; z-index: 2; }
.loading-text { font-weight: 300; letter-spacing: 1px; margin-bottom: 30px; }
.cancel-btn { padding: 12px 40px; font-size: 16px; background: transparent; color: #909399; border: 1px solid #4c4d4f; }
.cancel-btn:hover { color: #fff; border-color: #fff; }
@keyframes radar-spin { to { transform: rotate(360deg); } }

/* Game Layer */
.game-layer { width: 100%; max-width: 800px; padding: 20px; display: flex; flex-direction: column; gap: 20px; }
.players-header { display: flex; justify-content: space-between; align-items: center; padding: 0 10px; }
.player-card { display: flex; align-items: center; gap: 15px; width: 35%; }
.player-card.opponent { flex-direction: row-reverse; }
.avatar-wrapper { position: relative; }
.floating-score { position: absolute; top: 0; left: 50%; transform: translateX(-50%); color: #67c23a; font-weight: 900; font-size: 24px; text-shadow: 0 2px 5px rgba(0,0,0,0.8); animation: floatUp 1.2s ease-out forwards; pointer-events: none; }
.floating-score.enemy { color: #f56c6c; }
@keyframes floatUp { 0% { top: 0; opacity: 0; transform: translateX(-50%) scale(0.5); } 20% { opacity: 1; transform: translateX(-50%) scale(1.2); } 100% { top: -50px; opacity: 0; transform: translateX(-50%) scale(1); } }
.info-block { flex: 1; display: flex; flex-direction: column; gap: 5px; }
.info-block.align-right { align-items: flex-end; }
.name { font-size: 16px; font-weight: bold; max-width: 100px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.progress-bg { width: 100%; height: 8px; background: rgba(255, 255, 255, 0.1); border-radius: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: #409eff; transition: width 0.5s ease-out; }
.progress-fill.enemy { background: #f56c6c; }
.score-text { font-size: 20px; font-family: 'Impact', sans-serif; letter-spacing: 1px; }
.vs-status { text-align: center; }
.vs-logo { font-size: 40px; font-weight: 900; font-style: italic; color: #e6a23c; text-shadow: 0 0 10px rgba(230, 162, 60, 0.5); }
.round-badge { font-size: 12px; color: #909399; background: rgba(0, 0, 0, 0.3); padding: 2px 8px; border-radius: 10px; margin-top: 5px; }
.timer-section { margin: 0 20px; }
:deep(.el-progress-bar__outer) { background-color: rgba(255, 255, 255, 0.1) !important; }
.question-board { background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(10px); border-radius: 16px; padding: 30px; border: 1px solid rgba(255, 255, 255, 0.1); transition: all 0.3s; position: relative; }
.question-text { font-size: 20px; line-height: 1.6; margin-bottom: 30px; font-weight: 500; }
.options-container { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.option-item { background: rgba(255, 255, 255, 0.05); border: 2px solid transparent; padding: 15px; border-radius: 10px; cursor: pointer; display: flex; align-items: center; transition: all 0.2s; position: relative; }
.option-item:hover:not(.is-dimmed):not(.is-selected):not(.is-correct):not(.is-wrong) { background: rgba(255, 255, 255, 0.15); transform: translateY(-2px); }
.opt-tag { width: 30px; height: 30px; background: rgba(255, 255, 255, 0.1); border-radius: 50%; display: flex; justify-content: center; align-items: center; font-weight: bold; margin-right: 12px; flex-shrink: 0; }
.opt-content { font-size: 16px; }
.option-item.is-selected { border-color: #409eff; background: rgba(64, 158, 255, 0.1); }
.option-item.is-correct { border-color: #67c23a; background: rgba(103, 194, 58, 0.2); box-shadow: 0 0 15px rgba(103, 194, 58, 0.3); }
.option-item.is-wrong { border-color: #f56c6c; background: rgba(245, 108, 108, 0.2); animation: shake 0.5s; }
.option-item.is-dimmed { opacity: 0.3; cursor: not-allowed; }
.status-icon { position: absolute; right: 15px; font-size: 24px; }
.status-icon.correct { color: #67c23a; }
.status-icon.wrong { color: #f56c6c; }
.waiting-tip { position: absolute; bottom: 10px; left: 0; width: 100%; text-align: center; font-size: 12px; color: #909399; }
@keyframes shake { 0%, 100% { transform: translateX(0); } 20%, 60% { transform: translateX(-5px); } 40%, 80% { transform: translateX(5px); } }

/* Result Layer */
.result-layer { text-align: center; animation: zoomIn 0.4s; }
.result-anim { font-size: 80px; margin-bottom: 20px; }
.emoji-anim { display: inline-block; animation: bounce 2s infinite; }
.result-title { font-size: 48px; margin-bottom: 40px; font-weight: 900; }
.result-title.YOU { background: linear-gradient(to right, #f8b500, #fceabb); -webkit-background-clip: text; color: transparent; }
.result-title.OPPONENT { color: #909399; }
.result-title.DRAW { color: #409eff; }
.final-score-board { display: flex; justify-content: center; align-items: center; gap: 40px; margin-bottom: 50px; }
.score-box { text-align: center; }
.score-box .label { font-size: 18px; margin-bottom: 5px; color: #ccc; }
.score-box .val { font-size: 60px; font-weight: bold; }
.score-box.me .val { color: #e6a23c; text-shadow: 0 0 20px rgba(230, 162, 60, 0.5); }
.vs-divider { font-size: 40px; color: #555; margin-top: 20px; }
.action-btns { display: flex; gap: 20px; justify-content: center; }
@keyframes zoomIn { from { opacity: 0; transform: scale(0.8); } to { opacity: 1; transform: scale(1); } }
@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }
</style>