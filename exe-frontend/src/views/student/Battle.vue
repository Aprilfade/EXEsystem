<template>
  <div class="battle-container">
    <div v-if="gameState === 'MATCHING'" class="matching-box">
      <div class="radar">
        <div class="scan"></div>
        <el-icon class="search-icon" :size="50"><Search /></el-icon>
      </div>
      <h2>正在寻找对手...</h2>
      <el-button round @click="cancelMatch">取消匹配</el-button>
    </div>

    <div v-else-if="gameState === 'PLAYING' || gameState === 'ROUND_RESULT'" class="game-box">
      <div class="players-bar">
        <div class="player me">
          <UserAvatar
              :src="store.student?.avatar"
              :name="store.studentName"
              :size="60"
              :frame-style="store.student?.avatarFrameStyle"
          />
          <div class="score">{{ myScore }} 分</div>
          <div class="name">我</div>
        </div>

        <div class="vs-icon">VS</div>
        <div class="player opponent">
          <UserAvatar
              :src="opponentInfo.avatar"
              :name="opponentInfo.name"
              :size="60"
              :frame-style="opponentInfo.avatarFrameStyle"
          />
          <div class="score">{{ oppScore }} 分</div>
          <div class="name">{{ opponentInfo.name }}</div>
        </div>

      </div>

      <div class="question-card">
        <div class="round-info">第 {{ currentRound }} / {{ totalRound }} 题</div>
        <div class="q-content">{{ currentQuestion?.content }}</div>

        <div class="options-grid">
          <div
              v-for="opt in parseOptions(currentQuestion?.options)"
              :key="opt.key"
              class="option-btn"
              :class="{
              'selected': myAnswer === opt.key,
              'correct': gameState === 'ROUND_RESULT' && opt.key === roundResult?.correctAnswer,
              'wrong': gameState === 'ROUND_RESULT' && myAnswer === opt.key && myAnswer !== roundResult?.correctAnswer
            }"
              @click="submitAnswer(opt.key)"
          >
            {{ opt.key }}. {{ opt.value }}
          </div>
        </div>

        <div v-if="gameState === 'ROUND_RESULT'" class="round-tip">
          <el-tag type="info">等待下一题...</el-tag>
        </div>
      </div>
    </div>

    <div v-else-if="gameState === 'GAME_OVER'" class="result-box">
      <img v-if="finalResult === 'YOU'" src="/1.png" class="trophy" />
      <h1 :class="finalResult === 'YOU' ? 'win' : 'lose'">
        {{ finalResult === 'YOU' ? '胜利！' : (finalResult === 'DRAW' ? '平局' : '惜败') }}
      </h1>
      <div class="final-score">
        {{ myScore }} : {{ oppScore }}
      </div>
      <el-button type="primary" size="large" @click="startMatch">再来一局</el-button>
      <el-button @click="$router.push('/student/dashboard')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import UserAvatar from '@/components/UserAvatar.vue'; // 确保引入头像组件

const store = useStudentAuthStore();
const socket = ref<WebSocket | null>(null);

// 状态机: IDLE, MATCHING, PLAYING, ROUND_RESULT, GAME_OVER
const gameState = ref('IDLE');

const currentQuestion = ref<any>(null);
const currentRound = ref(0);
const totalRound = ref(0);
const myScore = ref(0);
const oppScore = ref(0);
const myAnswer = ref('');
const roundResult = ref<any>(null);
const finalResult = ref('');

// 连接 WebSocket
const connect = () => {
  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  const wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  const domain = apiBase.replace(/^https?:\/\//, '');
  const url = `${wsProtocol}://${domain}/ws/battle?token=${store.token}`;

  socket.value = new WebSocket(url);

  socket.value.onopen = () => {
    console.log('Battle WS Connected');
    startMatch(); // 自动开始匹配
  };

  socket.value.onmessage = (event) => {
    const msg = JSON.parse(event.data);
    handleMessage(msg);
  };

  socket.value.onclose = () => {
    console.log('Battle WS Closed');
  };
};

// 【新增】对手信息状态
const opponentInfo = ref({
  name: '匹配中...',
  avatar: '',
  avatarFrameStyle: ''
});


const handleMessage = (msg: any) => {
  if (msg.type === 'MATCH_SUCCESS') {
    gameState.value = 'PLAYING'; // 准备开始，实际等待题目
    ElMessage.success('匹配成功！准备开始');
    // 【核心修改】更新对手信息
    if (msg.data && msg.data.opponent) {
      opponentInfo.value = msg.data.opponent;
    }

    myScore.value = 0;
    oppScore.value = 0;
  } else if (msg.type === 'QUESTION') {
    gameState.value = 'PLAYING';
    currentQuestion.value = msg.data;
    currentRound.value = msg.data.round;
    totalRound.value = msg.data.total;
    myAnswer.value = '';
    roundResult.value = null;
  } else if (msg.type === 'ROUND_RESULT') {
    // 【修改开始】
    gameState.value = 'ROUND_RESULT';
    roundResult.value = msg.data;

    // 核心修复：直接使用后端转换好的相对分数
    myScore.value = msg.data.myScore;
    oppScore.value = msg.data.oppScore;

    // 可选：增加一些动画或提示，例如：
    if (msg.data.isCorrect) {
      ElMessage.success('回答正确 +20分！');
    } else {
      ElMessage.error('回答错误');
    }
    // 【修改结束】

  } else if (msg.type === 'GAME_OVER') {
    gameState.value = 'GAME_OVER';
    finalResult.value = msg.data.result;
    myScore.value = msg.data.myScore;
    oppScore.value = msg.data.oppScore;
  } else if (msg.type === 'OPPONENT_LEFT') {
    // 对手离开时，重置信息
    opponentInfo.value.name = '对手已离开';
  }
};

// 在 startMatch 或重置时，记得重置 opponentInfo
const startMatch = () => {
  // 重置显示的对手信息
  opponentInfo.value = { name: '寻找对手...', avatar: '', avatarFrameStyle: '' };
  gameState.value = 'MATCHING';
  socket.value?.send(JSON.stringify({ type: 'MATCH' }));
};

const cancelMatch = () => {
  socket.value?.close();
  gameState.value = 'IDLE';
};

const submitAnswer = (key: string) => {
  if (myAnswer.value || gameState.value !== 'PLAYING') return; // 已答过
  myAnswer.value = key;
  socket.value?.send(JSON.stringify({ type: 'ANSWER', data: key }));
};

const parseOptions = (json: string) => {
  try { return JSON.parse(json); } catch { return []; }
};

onMounted(() => {
  connect();
});

onUnmounted(() => {
  socket.value?.close();
});
</script>

<style scoped>
.battle-container {
  padding: 20px;
  min-height: 80vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1c1c1c, #2a2a2a);
  color: white;
}

/* 匹配雷达样式 */
.matching-box { text-align: center; }
.radar {
  width: 100px; height: 100px;
  border: 2px solid #409eff;
  border-radius: 50%;
  position: relative;
  margin: 0 auto 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.scan {
  position: absolute;
  width: 100%; height: 100%;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent 0deg, rgba(64,158,255, 0.5) 360deg);
  animation: spin 2s linear infinite;
}
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

/* 游戏区域样式 */
.game-box { width: 100%; max-width: 800px; }
.players-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 0 40px;
}
.player { text-align: center; }
.score { font-size: 24px; font-weight: bold; color: #ffba00; margin: 5px 0; }
.vs-icon { font-size: 40px; font-style: italic; font-weight: 900; color: #f56c6c; }

.question-card {
  background: rgba(255,255,255,0.1);
  padding: 30px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
}
.round-info { color: #909399; margin-bottom: 10px; }
.q-content { font-size: 20px; margin-bottom: 30px; font-weight: bold; line-height: 1.5; }
.options-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.option-btn {
  background: rgba(255,255,255,0.05);
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}
.option-btn:hover { background: rgba(255,255,255,0.15); }
.option-btn.selected { border-color: #409eff; background: rgba(64,158,255, 0.2); }
.option-btn.correct { border-color: #67c23a; background: rgba(103,194,58, 0.2); }
.option-btn.wrong { border-color: #f56c6c; background: rgba(245,108,108, 0.2); }

/* 结算样式 */
.result-box { text-align: center; }
.win { color: #ffba00; font-size: 40px; text-shadow: 0 0 20px rgba(255, 186, 0, 0.5); }
.lose { color: #909399; font-size: 40px; }
.final-score { font-size: 60px; font-weight: bold; margin: 20px 0; }
</style>