<template>
  <div class="battle-container">

    <div v-if="battleStore.gameState === 'IDLE'" class="lobby-layer">
      <div class="lobby-content">
        <h1 class="game-title">知识竞技场</h1>

        <!-- 音效控制按钮 -->
        <div class="sound-control">
          <el-tooltip :content="soundEnabled ? '关闭音效' : '开启音效'" placement="bottom">
            <el-button
                circle
                :icon="soundEnabled ? 'el-icon-video-play' : 'el-icon-video-pause'"
                @click="toggleSound"
                class="sound-btn"
            >
              <el-icon v-if="soundEnabled"><VideoPlay /></el-icon>
              <el-icon v-else><VideoPause /></el-icon>
            </el-button>
          </el-tooltip>
        </div>

        <div class="rank-panel">
          <div class="panel-header">🏆 赛季排行榜</div>
          <div class="rank-list">
            <!-- 骨架屏 -->
            <template v-if="rankLoading">
              <div v-for="i in 5" :key="'skeleton-'+i" class="rank-item skeleton">
                <div class="rank-idx skeleton-circle"></div>
                <div class="rank-user">
                  <div class="skeleton-avatar"></div>
                  <div class="user-info">
                    <div class="skeleton-text skeleton-name"></div>
                    <div class="skeleton-text skeleton-tier"></div>
                  </div>
                </div>
                <div class="skeleton-text skeleton-score"></div>
              </div>
            </template>

            <!-- 实际数据 -->
            <template v-else>
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
            </template>
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

        <el-dialog v-model="historyVisible" title="⚔️ 近期战绩" width="650px" append-to-body class="battle-history-dialog">
          <!-- 战绩统计卡片 -->
          <div v-if="!historyLoading && historyList.length > 0" class="stats-cards">
            <div class="stat-card win">
              <div class="stat-icon">🏆</div>
              <div class="stat-content">
                <div class="stat-label">胜利</div>
                <div class="stat-value">{{ winCount }}</div>
              </div>
            </div>
            <div class="stat-card lose">
              <div class="stat-icon">😢</div>
              <div class="stat-content">
                <div class="stat-label">失败</div>
                <div class="stat-value">{{ loseCount }}</div>
              </div>
            </div>
            <div class="stat-card draw">
              <div class="stat-icon">🤝</div>
              <div class="stat-content">
                <div class="stat-label">平局</div>
                <div class="stat-value">{{ drawCount }}</div>
              </div>
            </div>
            <div class="stat-card rate">
              <div class="stat-icon">📊</div>
              <div class="stat-content">
                <div class="stat-label">胜率</div>
                <div class="stat-value">{{ winRate }}%</div>
              </div>
            </div>
          </div>

          <!-- 积分趋势图 -->
          <div v-if="!historyLoading && historyList.length > 0" class="chart-container">
            <div class="chart-title">积分趋势</div>
            <div ref="trendChart" class="trend-chart"></div>
          </div>

          <!-- 战绩列表 -->
          <el-table :data="historyList" stripe style="width: 100%; margin-top: 15px;" v-loading="historyLoading" max-height="300">
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

      <transition name="fade">
        <div v-if="battleStore.activeEffects.includes('FOG')" class="fog-overlay">
          <div class="fog-cloud">🌫️</div>
          <div class="fog-text">迷雾遮蔽了你的视野！</div>
        </div>
      </transition>

      <div class="players-header">
        <div class="player-card me">
          <div class="avatar-wrapper">
            <UserAvatar :src="authStore.student?.avatar" :name="authStore.studentName" :size="70" :frame-style="authStore.student?.avatarFrameStyle" />

            <transition name="score-float">
              <div v-if="showMyScoreAnim" class="floating-score">
                +{{ battleStore.myScoreChange }}
              </div>
            </transition>

            <transition name="bounce">
              <div v-if="battleStore.myCombo > 1" class="combo-badge">
                Combo x{{ battleStore.myCombo }} 🔥
              </div>
            </transition>
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
            <transition name="score-float"><div v-if="showOppScoreAnim" class="floating-score enemy">+??</div></transition>
          </div>
        </div>
      </div>

      <div class="timer-section">
        <el-progress :percentage="timePercentage" :format="timeFormat" :status="timerStatus" :stroke-width="12" striped striped-flow text-inside />
      </div>

      <div class="question-board" :class="{ 'result-mode': battleStore.gameState === 'ROUND_RESULT' }">
        <div class="question-text">{{ battleStore.currentQuestion?.content }}</div>
        <div class="options-container">
          <div
              v-for="opt in parsedOptions"
              :key="opt.key"
              class="option-item"
              :class="[
               getOptionClass(opt.key),
               { 'is-excluded': battleStore.excludedOptions.includes(opt.key) }
            ]"
              @click="handleAnswer(opt.key)"
          >
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

      <div v-if="battleStore.gameState === 'PLAYING' && !hasAnswered" class="item-bar">
        <el-tooltip content="排除一个错误选项" placement="top">
          <div class="item-btn" @click="battleStore.useItem('HINT')" :class="{ disabled: battleStore.myItems['HINT'] <= 0 }">
            <span class="icon">💡</span>
            <span class="count">x{{ battleStore.myItems['HINT'] }}</span>
            <span class="label">排除卡</span>
          </div>
        </el-tooltip>

        <el-tooltip content="遮挡对手视野3秒" placement="top">
          <div class="item-btn" @click="battleStore.useItem('FOG')" :class="{ disabled: battleStore.myItems['FOG'] <= 0 }">
            <span class="icon">🌫️</span>
            <span class="count">x{{ battleStore.myItems['FOG'] }}</span>
            <span class="label">迷雾卡</span>
          </div>
        </el-tooltip>
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
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useBattleStore } from '@/stores/battle';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { Search, Select, CloseBold, Loading, Trophy, VideoPlay, VideoPause } from '@element-plus/icons-vue';
import UserAvatar from '@/components/UserAvatar.vue';
import { fetchBattleLeaderboard, fetchMyBattleRecords } from '@/api/student';
import * as echarts from 'echarts';
import type { ECharts } from 'echarts';

// ... (排行榜、历史记录相关代码保持不变) ...
const historyVisible = ref(false);
const historyLoading = ref(false);
const historyList = ref([]);
const trendChart = ref<HTMLDivElement>();
let trendChartInstance: ECharts | null = null;

const battleStore = useBattleStore();
const authStore = useStudentAuthStore();

const rankLoading = ref(false);
const rankList = ref<any[]>([]);
const myPoints = computed(() => authStore.student?.points || 0);

// 战绩统计
const winCount = computed(() => historyList.value.filter((r: any) => r.result === 'WIN').length);
const loseCount = computed(() => historyList.value.filter((r: any) => r.result === 'LOSE').length);
const drawCount = computed(() => historyList.value.filter((r: any) => r.result === 'DRAW').length);
const winRate = computed(() => {
  const total = historyList.value.length;
  if (total === 0) return 0;
  return Math.round((winCount.value / total) * 100);
});

// 音效控制
const soundEnabled = ref(true);
const toggleSound = () => {
  soundEnabled.value = !soundEnabled.value;
  localStorage.setItem('battleSoundEnabled', soundEnabled.value ? '1' : '0');
};

// 从localStorage读取音效设置
onMounted(() => {
  const saved = localStorage.getItem('battleSoundEnabled');
  if (saved !== null) {
    soundEnabled.value = saved === '1';
  }
});

const loadLeaderboard = async () => {
  rankLoading.value = true;
  try {
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
      // 等待DOM更新后初始化图表
      await nextTick();
      initTrendChart();
    }
  } finally {
    historyLoading.value = false;
  }
};

// 初始化积分趋势图
const initTrendChart = () => {
  if (!trendChart.value || historyList.value.length === 0) return;

  if (trendChartInstance) {
    trendChartInstance.dispose();
  }

  trendChartInstance = echarts.init(trendChart.value);

  // 计算累计积分（从最早到最新）
  const records = [...historyList.value].reverse();
  const dates = records.map((r: any) => formatTime(r.createTime).substring(5, 11));

  let currentPoints = myPoints.value;
  // 从当前积分反推历史积分
  const points = records.map((r: any, index: number) => {
    if (index === 0) {
      currentPoints = myPoints.value - records.slice(index).reduce((sum: number, item: any) => sum + item.scoreChange, 0);
    } else {
      currentPoints += records[index - 1].scoreChange;
    }
    return currentPoints;
  });
  // 添加最终的当前积分
  points.push(myPoints.value);
  dates.push('当前');

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0];
        return `${data.name}<br/>积分: ${data.value}`;
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      name: '积分'
    },
    series: [
      {
        type: 'line',
        data: points,
        smooth: true,
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      }
    ]
  };

  trendChartInstance.setOption(option);
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(5, 16);
};
const handleStartMatch = () => {
  battleStore.startMatch();
}
const returnToLobby = () => {
  battleStore.gameState = 'IDLE';
  loadLeaderboard();
}

const audios: Record<string, HTMLAudioElement> = {
  bgm: new Audio('/audio/battle_bgm.mp3'),
  correct: new Audio('/audio/correct.mp3'),
  wrong: new Audio('/audio/wrong.mp3'),
  win: new Audio('/audio/win.mp3'),
  lose: new Audio('/audio/lose.mp3'),
  match: new Audio('/audio/match.mp3')
};
const playSound = (name: string) => {
  if (!soundEnabled.value) return; // 检查音效是否开启
  const audio = audios[name];
  if (audio) {
    audio.currentTime = 0;
    audio.play().catch(() => {});
  }
};

const TOTAL_TIME = 20;
const timeLeft = ref(TOTAL_TIME);
let timerInterval: any = null;
const timePercentage = computed(() => (timeLeft.value / TOTAL_TIME) * 100);
const timerStatus = computed(() => timeLeft.value <= 5 ? 'exception' : 'success');
const timeFormat = () => `${timeLeft.value}s`;

const startTimer = () => {
  clearInterval(timerInterval);
  timeLeft.value = TOTAL_TIME;
  timerInterval = setInterval(() => {
    if (timeLeft.value > 0) timeLeft.value--;
    else clearInterval(timerInterval);
  }, 1000);
};

const myAnswer = ref('');
const showMyScoreAnim = ref(false);
const showOppScoreAnim = ref(false);

const maxScoreRef = computed(() => Math.max(100, battleStore.myScore + 40, battleStore.oppScore + 40));
const myScorePercent = computed(() => (battleStore.myScore / maxScoreRef.value) * 100);
const oppScorePercent = computed(() => (battleStore.oppScore / maxScoreRef.value) * 100);
const hasAnswered = computed(() => !!myAnswer.value);

const parsedOptions = computed(() => {
  try {
    return typeof battleStore.currentQuestion?.options === 'string'
        ? JSON.parse(battleStore.currentQuestion.options)
        : battleStore.currentQuestion?.options || [];
  } catch { return []; }
});

const gameResult = computed(() => battleStore.finalResult?.result || '');

const getOptionClass = (key: string) => {
  if (battleStore.gameState === 'ROUND_RESULT') {
    const correctKey = battleStore.roundResult?.correctAnswer;
    if (key === correctKey) return 'is-correct';
    if (key === myAnswer.value && key !== correctKey) return 'is-wrong';
    return 'is-dimmed';
  }
  if (key === myAnswer.value) return 'is-selected';
  return '';
};

const showResultIcon = (key: string) => {
  if (battleStore.gameState !== 'ROUND_RESULT') return '';
  const correctKey = battleStore.roundResult?.correctAnswer;
  if (key === correctKey) return 'correct';
  if (key === myAnswer.value && key !== correctKey) return 'wrong';
  return '';
};

const getResultTitle = (res: string) => {
  const map: Record<string, string> = { 'YOU': '大获全胜！', 'OPPONENT': '遗憾惜败...', 'DRAW': '平分秋色' };
  return map[res] || '游戏结束';
};

const handleAnswer = (key: string) => {
  // 如果被排除，不能点击
  if (battleStore.excludedOptions.includes(key)) return;
  if (hasAnswered.value || battleStore.gameState !== 'PLAYING') return;
  myAnswer.value = key;
  battleStore.submitAnswer(key);
};

watch(() => battleStore.currentQuestion, (newVal) => {
  if (newVal) {
    myAnswer.value = '';
    showMyScoreAnim.value = false;
    showOppScoreAnim.value = false;
    startTimer();
  }
});

watch(() => battleStore.roundResult, (res) => {
  if (res) {
    clearInterval(timerInterval);
    if (res.isCorrect) {
      playSound('correct');
      showMyScoreAnim.value = true;
    } else {
      playSound('wrong');
    }
    if (res.oppAnswer === res.correctAnswer) {
      showOppScoreAnim.value = true;
    }
  }
});

watch(() => battleStore.gameState, (state) => {
  if (state === 'GAME_OVER') {
    clearInterval(timerInterval);
    if (gameResult.value === 'YOU') playSound('win'); else playSound('lose');
  } else if (state === 'MATCHING') {
    playSound('match');
  }
});

onMounted(() => {
  battleStore.connect();
  if (battleStore.gameState === 'IDLE') {
    loadLeaderboard();
  }
});

onUnmounted(() => {
  battleStore.leave();
  clearInterval(timerInterval);
  trendChartInstance?.dispose();
});
</script>

<style scoped>
/* ... 基础结构样式 ... */
.battle-container { min-height: calc(100vh - 60px); background: radial-gradient(circle at center, #2b323c 0%, #141414 100%); color: #fff; display: flex; justify-content: center; align-items: center; overflow: hidden; position: relative; padding: 20px; }
.lobby-layer { width: 100%; max-width: 500px; text-align: center; animation: fadeIn 0.5s; display: flex; flex-direction: column; height: 80vh; position: relative; }
.game-title { font-size: 40px; font-weight: 900; margin-bottom: 20px; background: linear-gradient(to right, #409eff, #00f2f1); -webkit-background-clip: text; color: transparent; letter-spacing: 4px; }

/* 音效控制按钮 */
.sound-control {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 10;
}

.sound-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  transition: all 0.3s;
}

.sound-btn:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: #409eff;
  transform: scale(1.1);
}

.rank-panel { flex: 1; background: rgba(255, 255, 255, 0.1); border-radius: 12px; border: 1px solid rgba(255, 255, 255, 0.1); display: flex; flex-direction: column; overflow: hidden; margin-bottom: 20px; }
.panel-header { padding: 15px; background: rgba(0,0,0,0.2); font-weight: bold; color: #ffd700; font-size: 18px; }
.rank-list { flex: 1; overflow-y: auto; padding: 10px; }

/* 移动端适配 - 大厅 */
@media (max-width: 768px) {
  .battle-container { padding: 15px 10px; min-height: calc(100vh - 60px); }
  .lobby-layer { max-width: 100%; height: auto; min-height: 75vh; }
  .game-title { font-size: 28px; letter-spacing: 2px; margin-bottom: 15px; }
  .panel-header { font-size: 16px; padding: 12px; }
  .rank-panel { margin-bottom: 15px; }
}

@media (max-width: 480px) {
  .game-title { font-size: 24px; }
  .battle-container { padding: 10px 8px; }
}
.rank-item { display: flex; align-items: center; padding: 10px; margin-bottom: 8px; background: rgba(255,255,255,0.05); border-radius: 8px; transition: all 0.2s; }
.rank-item:hover { background: rgba(255,255,255,0.1); transform: translateX(5px); }
.rank-item.skeleton:hover { transform: none; }
.rank-idx { width: 28px; height: 28px; line-height: 28px; text-align: center; border-radius: 50%; background: #555; color: #fff; font-weight: bold; margin-right: 10px; font-size: 12px; }

/* 骨架屏样式 */
.skeleton-circle {
  background: linear-gradient(90deg, rgba(255,255,255,0.05) 25%, rgba(255,255,255,0.15) 50%, rgba(255,255,255,0.05) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(90deg, rgba(255,255,255,0.05) 25%, rgba(255,255,255,0.15) 50%, rgba(255,255,255,0.05) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-text {
  height: 12px;
  border-radius: 4px;
  background: linear-gradient(90deg, rgba(255,255,255,0.05) 25%, rgba(255,255,255,0.15) 50%, rgba(255,255,255,0.05) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-name {
  width: 80px;
  margin-bottom: 4px;
}

.skeleton-tier {
  width: 50px;
}

.skeleton-score {
  width: 60px;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
.top-1 { background: #FFD700; color: #000; box-shadow: 0 0 10px #FFD700; }
.top-2 { background: #C0C0C0; color: #000; }
.top-3 { background: #CD7F32; color: #000; }
.rank-user { flex: 1; display: flex; align-items: center; gap: 10px; }
.user-info { display: flex; flex-direction: column; align-items: flex-start; }
.u-name { font-size: 14px; font-weight: bold; }
.rank-score { font-weight: bold; color: #409eff; font-family: monospace; }
.lobby-actions { display: flex; flex-direction: column; gap: 15px; align-items: center; }
.history-btn { color: #ddd; font-size: 14px; margin-top: 10px; }
.history-btn:hover { color: #409eff; }
:deep(.battle-history-dialog) { border-radius: 12px; overflow: hidden; }
.my-status { color: #ccc; font-size: 14px; }
.my-status span { color: #fff; font-weight: bold; font-size: 16px; }
.start-btn { width: 200px; height: 50px; font-size: 20px; font-weight: bold; letter-spacing: 2px; background: linear-gradient(45deg, #409eff, #36cfc9); border: none; box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4); transition: transform 0.2s; }
.start-btn:hover { transform: scale(1.05); }
.start-btn:active { transform: scale(0.95); }

/* 移动端适配 - 大厅操作区 */
@media (max-width: 768px) {
  .lobby-actions { gap: 12px; }
  .start-btn { width: 180px; height: 45px; font-size: 18px; }
  .my-status { font-size: 13px; }
  .my-status span { font-size: 15px; }
  .history-btn { font-size: 13px; margin-top: 8px; }
}

@media (max-width: 480px) {
  .start-btn { width: 100%; max-width: 280px; height: 42px; font-size: 16px; letter-spacing: 1px; }
  .my-status { font-size: 12px; }
  .history-btn { font-size: 12px; }
}

/* === 核心修改：英雄联盟段位样式 === */
.u-tier { font-size: 12px; padding: 1px 6px; border-radius: 4px; margin-top: 2px; border: 1px solid; font-weight: bold; text-transform: uppercase; }

/* 黑铁 */
.u-tier.IRON { color: #6d6969; border-color: #6d6969; background: rgba(109, 105, 105, 0.1); }
/* 黄铜 */
.u-tier.BRONZE { color: #cd7f32; border-color: #cd7f32; background: rgba(205, 127, 50, 0.1); }
/* 白银 */
.u-tier.SILVER { color: #c0c0c0; border-color: #c0c0c0; background: rgba(192, 192, 192, 0.1); }
/* 黄金 */
.u-tier.GOLD { color: #ffd700; border-color: #ffd700; background: rgba(255, 215, 0, 0.1); text-shadow: 0 0 2px #ffd700; }
/* 铂金 */
.u-tier.PLATINUM { color: #2de0a5; border-color: #2de0a5; background: rgba(45, 224, 165, 0.1); }
/* 翡翠 */
.u-tier.EMERALD { color: #00bba3; border-color: #00bba3; background: rgba(0, 187, 163, 0.1); }
/* 钻石 */
.u-tier.DIAMOND { color: #78b3ff; border-color: #78b3ff; background: rgba(120, 179, 255, 0.1); text-shadow: 0 0 5px #78b3ff; }
/* 大师 */
.u-tier.MASTER { color: #d264ff; border-color: #d264ff; background: rgba(210, 100, 255, 0.1); }
/* 宗师 */
.u-tier.GRANDMASTER { color: #ff4655; border-color: #ff4655; background: rgba(255, 70, 85, 0.1); }
/* 王者 */
.u-tier.CHALLENGER {
  background: linear-gradient(45deg, #f7c978, #f2d8a6, #f7c978);
  -webkit-background-clip: text;
  color: transparent;
  border-color: #f7c978;
  box-shadow: 0 0 10px rgba(247, 201, 120, 0.4);
}

/* 游戏界面部分 */
.matching-layer { text-align: center; animation: fadeIn 0.5s; padding: 20px; }
.radar-scanner { width: 140px; height: 140px; border-radius: 50%; border: 2px solid rgba(64, 158, 255, 0.3); position: relative; margin: 0 auto 40px; display: flex; justify-content: center; align-items: center; box-shadow: 0 0 30px rgba(64, 158, 255, 0.1); }
.scan-beam { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border-radius: 50%; background: conic-gradient(from 0deg, transparent 0deg, rgba(64, 158, 255, 0.5) 360deg); animation: radar-spin 1.5s linear infinite; }
.search-icon { color: #409eff; z-index: 2; }
.loading-text { font-weight: 300; letter-spacing: 1px; margin-bottom: 30px; font-size: 20px; }
.cancel-btn { padding: 12px 40px; font-size: 16px; background: transparent; color: #909399; border: 1px solid #4c4d4f; }
.cancel-btn:hover { color: #fff; border-color: #fff; }
@keyframes radar-spin { to { transform: rotate(360deg); } }

/* 移动端适配 - 匹配界面 */
@media (max-width: 768px) {
  .loading-text { font-size: 16px; padding: 0 20px; }
  .cancel-btn { padding: 10px 30px; font-size: 14px; }
}

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

/* 移动端适配 - 游戏界面 */
@media (max-width: 768px) {
  .game-layer { padding: 15px 10px; gap: 15px; }
  .players-header { padding: 0 5px; flex-wrap: wrap; gap: 10px; }
  .player-card { width: 40%; gap: 8px; }
  .name { font-size: 14px; max-width: 80px; }
  .score-text { font-size: 16px; }
  .vs-status { width: 100%; order: -1; margin-bottom: 10px; }
  .vs-logo { font-size: 30px; }
  .round-badge { font-size: 11px; }
  .timer-section { margin: 0 10px; }
  .floating-score { font-size: 18px; }
}

@media (max-width: 480px) {
  .player-card { width: 45%; }
  .vs-logo { font-size: 24px; }
  .score-text { font-size: 14px; }
}
:deep(.el-progress-bar__outer) { background-color: rgba(255, 255, 255, 0.1) !important; }
.question-board { background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(10px); border-radius: 16px; padding: 30px; border: 1px solid rgba(255, 255, 255, 0.1); transition: all 0.3s; position: relative; }
.question-text { font-size: 20px; line-height: 1.6; margin-bottom: 30px; font-weight: 500; }
.options-container { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.option-item { background: rgba(255, 255, 255, 0.05); border: 2px solid transparent; padding: 15px; border-radius: 10px; cursor: pointer; display: flex; align-items: center; transition: all 0.2s; position: relative; }
.option-item:hover:not(.is-dimmed):not(.is-selected):not(.is-correct):not(.is-wrong) { background: rgba(255, 255, 255, 0.15); transform: translateY(-2px); }
.opt-tag { width: 30px; height: 30px; background: rgba(255, 255, 255, 0.1); border-radius: 50%; display: flex; justify-content: center; align-items: center; font-weight: bold; margin-right: 12px; flex-shrink: 0; }
.opt-content { font-size: 16px; }

/* 移动端适配 - 题目和选项 */
@media (max-width: 768px) {
  .question-board { padding: 20px 15px; border-radius: 12px; }
  .question-text { font-size: 16px; margin-bottom: 20px; line-height: 1.5; }
  .options-container { grid-template-columns: 1fr; gap: 10px; }
  .option-item { padding: 12px; }
  .opt-tag { width: 26px; height: 26px; margin-right: 10px; font-size: 14px; }
  .opt-content { font-size: 14px; }
}

@media (max-width: 480px) {
  .question-board { padding: 15px 12px; }
  .question-text { font-size: 15px; }
  .option-item { padding: 10px; }
}
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

/* 移动端适配 - 道具栏 */
@media (max-width: 768px) {
  .item-bar { gap: 15px; padding: 8px; margin-top: 15px; }
  .item-btn { min-width: 65px; padding: 10px 15px; }
  .item-btn .icon { font-size: 24px; margin-bottom: 3px; }
  .item-btn .label { font-size: 11px; }
}

@media (max-width: 480px) {
  .item-bar { gap: 10px; }
  .item-btn { min-width: 60px; padding: 8px 12px; }
  .item-btn .icon { font-size: 20px; }
}

.result-layer { text-align: center; animation: zoomIn 0.4s; padding: 20px; }
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

/* 移动端适配 - 结果页面 */
@media (max-width: 768px) {
  .result-layer { padding: 15px; }
  .result-anim { font-size: 60px; margin-bottom: 15px; }
  .result-title { font-size: 32px; margin-bottom: 30px; }
  .final-score-board { gap: 20px; margin-bottom: 35px; }
  .score-box .label { font-size: 14px; }
  .score-box .val { font-size: 42px; }
  .vs-divider { font-size: 30px; }
  .action-btns { flex-direction: column; gap: 10px; padding: 0 20px; }
  .action-btns .el-button { width: 100%; }
}

@media (max-width: 480px) {
  .result-anim { font-size: 50px; }
  .result-title { font-size: 28px; }
  .score-box .val { font-size: 36px; }
  .final-score-board { gap: 15px; }
}
@keyframes zoomIn { from { opacity: 0; transform: scale(0.8); } to { opacity: 1; transform: scale(1); } }
@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }
/* --- 新增样式 --- */

/* 1. 连击徽章 */
.combo-badge {
  position: absolute;
  top: 50px;
  right: -20px; /* 调整位置到头像右侧 */
  background: linear-gradient(45deg, #ff0000, #ff7f00);
  color: #fff;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 900;
  font-style: italic;
  transform: rotate(-10deg);
  border: 2px solid #fff;
  box-shadow: 0 0 15px rgba(255, 80, 0, 0.6);
  z-index: 20;
  font-size: 14px;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
}

.bounce-enter-active { animation: bounce-in .5s; }
@keyframes bounce-in {
  0% { transform: scale(0) rotate(0); opacity: 0; }
  50% { transform: scale(1.5) rotate(-10deg); opacity: 1; }
  100% { transform: scale(1) rotate(-10deg); }
}

/* 2. 迷雾遮罩 */
.fog-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(220, 220, 230, 0.9);
  z-index: 99; /* 盖住题目和选项 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(8px);
  border-radius: 16px; /* 匹配 .game-layer 的圆角 */
}
.fog-cloud { font-size: 80px; animation: fog-float 2s infinite ease-in-out; }
.fog-text { font-size: 24px; color: #555; font-weight: bold; margin-top: 10px; text-shadow: 0 1px 2px rgba(255,255,255,0.8); }

@keyframes fog-float {
  0%, 100% { transform: translateY(0); opacity: 0.8; }
  50% { transform: translateY(-10px); opacity: 1; }
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.5s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 3. 道具栏 */
.item-bar {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 20px;
  padding: 10px;
}

.item-btn {
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 12px;
  padding: 12px 20px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  position: relative;
  min-width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.item-btn:hover:not(.disabled) {
  background: rgba(64, 158, 255, 0.2);
  border-color: #409eff;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.item-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  filter: grayscale(100%);
}

.item-btn .icon { font-size: 28px; display: block; margin-bottom: 4px; }
.item-btn .count {
  position: absolute; top: -8px; right: -8px;
  background: #f56c6c; color: #fff; font-size: 12px; font-weight: bold;
  padding: 2px 6px; border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}
.item-btn .label { font-size: 12px; color: #ddd; font-weight: 500; }

/* 4. 排除选项样式 */
.option-item.is-excluded {
  opacity: 0.3;
  pointer-events: none;
  background-color: #1a1a1a !important;
  border-color: #333 !important;
  text-decoration: line-through;
  color: #666;
}

/* 原有 CSS 补充 */
.game-layer {
  position: relative; /* 确保绝对定位的子元素相对于它定位 */
}

/* 战绩统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
  border-radius: 8px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-card.win {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.15) 0%, rgba(103, 194, 58, 0.05) 100%);
  border-color: rgba(103, 194, 58, 0.3);
}

.stat-card.lose {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.15) 0%, rgba(245, 108, 108, 0.05) 100%);
  border-color: rgba(245, 108, 108, 0.3);
}

.stat-card.draw {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.15) 0%, rgba(144, 147, 153, 0.05) 100%);
  border-color: rgba(144, 147, 153, 0.3);
}

.stat-card.rate {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.15) 0%, rgba(230, 162, 60, 0.05) 100%);
  border-color: rgba(230, 162, 60, 0.3);
}

.stat-icon {
  font-size: 28px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

/* 图表容器 */
.chart-container {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.trend-chart {
  width: 100%;
  height: 220px;
}

/* 移动端适配 - 战绩统计 */
@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    margin-bottom: 15px;
  }

  .stat-card {
    padding: 12px;
    gap: 10px;
  }

  .stat-icon {
    font-size: 24px;
  }

  .stat-value {
    font-size: 20px;
  }

  .trend-chart {
    height: 180px;
  }
}

@media (max-width: 480px) {
  .stat-card {
    padding: 10px;
  }

  .stat-icon {
    font-size: 20px;
  }

  .stat-label {
    font-size: 11px;
  }

  .stat-value {
    font-size: 18px;
  }

  .trend-chart {
    height: 160px;
  }
}
</style>