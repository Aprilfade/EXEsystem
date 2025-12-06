<template>
  <div class="game-wrapper h-[calc(100vh-100px)] relative overflow-hidden font-serif select-none">

    <div class="absolute inset-0 bg-slate-900 z-0 overflow-hidden">
      <img src="https://images.unsplash.com/photo-1518066000714-58c45f1a2c0a?q=80&w=2070&auto=format&fit=crop"
           class="absolute inset-0 w-full h-full object-cover opacity-30 mix-blend-overlay animate-pan-slow" />
      <div class="absolute inset-0 bg-gradient-to-t from-black via-slate-900/60 to-slate-900/40"></div>

      <div v-for="n in 20" :key="n" class="spirit-particle"></div>
    </div>

    <div class="absolute top-0 left-0 right-0 z-30 p-4 flex justify-between items-start pointer-events-none">
      <div class="player-hud flex items-center gap-3 pointer-events-auto bg-black/40 backdrop-blur-md px-4 py-2 rounded-full border border-emerald-500/30 shadow-lg">
        <div class="relative group cursor-pointer" @click="showProfile = true">
          <UserAvatar
              :src="studentStore.student?.avatar"
              :name="studentStore.student?.name"
              :size="50"
              class="ring-2 ring-emerald-400"
          />
          <div class="absolute -bottom-1 -right-1 bg-amber-600 text-[10px] text-white px-1.5 rounded-full border border-white/20">
            Lv.{{ profile.realmLevel || 0 }}
          </div>
        </div>
        <div class="flex flex-col min-w-[120px]">
          <div class="flex items-center gap-2">
            <span class="text-emerald-100 font-bold text-lg tracking-widest font-ink">{{ profile.realmName || '凡人' }}</span>
            <span class="text-xs text-emerald-400 font-mono">战力 {{ profile.attack + profile.defense }}</span>
          </div>
          <div class="w-full h-2 bg-slate-700/50 rounded-full mt-1 overflow-hidden relative">
            <div class="h-full bg-gradient-to-r from-emerald-600 to-teal-400 transition-all duration-500"
                 :style="{ width: `${calculateProgress(profile.currentExp, profile.maxExp)}%` }">
            </div>
          </div>
          <div class="text-[10px] text-slate-300 mt-0.5 scale-90 origin-left">
            {{ formatLargeNumber(profile.currentExp) }} / {{ formatLargeNumber(profile.maxExp) }}
          </div>
        </div>
      </div>

      <div class="flex gap-3 pointer-events-auto">
        <button class="icon-btn" @click="toggleMute">
          <el-icon><Microphone /></el-icon>
        </button>
        <button class="icon-btn" @click="showHelp">
          <el-icon><QuestionFilled /></el-icon>
        </button>
      </div>
    </div>

    <div class="absolute inset-0 z-20 flex flex-col items-center justify-center pointer-events-none">

      <div class="absolute w-[500px] h-[500px] opacity-20 animate-spin-slow pointer-events-none">
        <img src="https://www.svgrepo.com/show/305286/bagua.svg" class="w-full h-full invert drop-shadow-xl" />
      </div>

      <div class="relative z-30 pointer-events-auto group mt-[-50px]">
        <template v-if="canBreakthrough">
          <div class="breakthrough-orb w-48 h-48 rounded-full flex items-center justify-center cursor-pointer relative"
               @click="handleBreakthrough()">
            <div class="absolute inset-0 rounded-full bg-red-600 blur-xl animate-pulse"></div>
            <div class="absolute inset-0 rounded-full border-4 border-red-400 border-double animate-spin-reverse"></div>
            <div class="relative z-10 flex flex-col items-center text-red-100 drop-shadow-md">
              <span class="text-4xl font-ink animate-bounce">突破</span>
              <span class="text-xs mt-1">瓶颈已至</span>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="meditate-orb w-40 h-40 rounded-full flex items-center justify-center cursor-pointer transition-all duration-300 active:scale-95"
               :class="isMeditating ? 'shadow-[0_0_50px_rgba(16,185,129,0.6)]' : 'shadow-[0_0_20px_rgba(16,185,129,0.2)] hover:shadow-[0_0_30px_rgba(16,185,129,0.4)]'"
               @click="handleMeditate">

            <div class="absolute inset-0 rounded-full border-2 border-emerald-500/30 scale-110" :class="{ 'animate-spin-slow': isMeditating }"></div>
            <div class="absolute inset-0 rounded-full border border-teal-400/20 scale-125" :class="{ 'animate-spin-reverse': isMeditating }"></div>

            <div class="bg-slate-900/80 w-full h-full rounded-full flex flex-col items-center justify-center backdrop-blur-sm border border-emerald-500/50 relative overflow-hidden">
              <div class="absolute inset-0 bg-emerald-500/10" :class="{ 'animate-pulse': isMeditating }"></div>
              <span class="text-3xl font-ink text-emerald-100 z-10 select-none">
                  {{ isMeditating ? '吐纳' : '修炼' }}
                </span>
              <span class="text-[10px] text-emerald-400/80 mt-1 z-10" v-if="isMeditating">
                   <el-icon class="animate-spin"><Loading /></el-icon>
                </span>
            </div>
          </div>
        </template>

        <div ref="floatingTextContainer" class="absolute left-1/2 top-0 -translate-x-1/2 pointer-events-none w-full h-full"></div>
      </div>

      <div class="mt-8 text-center h-8">
        <transition name="fade" mode="out-in">
          <div :key="currentEventDescription" class="inline-block px-4 py-1 rounded bg-black/30 text-emerald-200/80 text-sm font-ink tracking-widest">
            {{ currentEventDescription }}
          </div>
        </transition>
      </div>

    </div>

    <div class="absolute bottom-24 left-4 z-30 w-80 h-48 pointer-events-none mask-image-gradient">
      <div class="w-full h-full overflow-hidden flex flex-col justify-end">
        <div class="space-y-2 p-2" ref="logContainer">
          <transition-group name="list">
            <div v-for="(log, idx) in visibleLogs" :key="idx"
                 class="text-xs leading-relaxed drop-shadow-md font-sans animate-slide-in"
                 :class="getLogColor(log)">
              <span class="opacity-50 mr-1">[{{ formatTime(new Date()) }}]</span>
              {{ log }}
            </div>
          </transition-group>
        </div>
      </div>
    </div>

    <div class="absolute bottom-6 left-1/2 -translate-x-1/2 z-40 bg-slate-900/90 border border-slate-700/50 rounded-2xl px-6 py-3 flex gap-6 shadow-2xl backdrop-blur-xl">

      <el-tooltip content="角色属性" placement="top" :show-after="500">
        <div class="dock-item group" @click="showProfile = true">
          <div class="icon-box bg-gradient-to-br from-slate-700 to-slate-800 group-hover:from-emerald-800 group-hover:to-emerald-900">
            <el-icon><User /></el-icon>
          </div>
          <span class="text-[10px] mt-1 text-slate-400 group-hover:text-emerald-300">道友</span>
        </div>
      </el-tooltip>

      <el-tooltip content="储物袋" placement="top" :show-after="500">
        <div class="dock-item group" @click="showBag = true">
          <div class="icon-box bg-gradient-to-br from-slate-700 to-slate-800 group-hover:from-amber-800 group-hover:to-amber-900">
            <el-icon><Box /></el-icon>
          </div>
          <span class="text-[10px] mt-1 text-slate-400 group-hover:text-amber-300">行囊</span>
        </div>
      </el-tooltip>

      <div class="dock-item group -mt-4" @click="handlePractice">
        <div class="icon-box w-14 h-14 bg-gradient-to-br from-indigo-600 to-purple-700 shadow-[0_0_15px_rgba(79,70,229,0.5)] border border-indigo-400">
          <el-icon class="text-2xl text-white"><EditPen /></el-icon>
        </div>
        <span class="text-xs font-bold mt-1 text-indigo-300">悟道</span>
      </div>

      <el-tooltip content="坊市交易" placement="top" :show-after="500">
        <div class="dock-item group" @click="showShop = true">
          <div class="icon-box bg-gradient-to-br from-slate-700 to-slate-800 group-hover:from-cyan-800 group-hover:to-cyan-900">
            <el-icon><Shop /></el-icon>
          </div>
          <span class="text-[10px] mt-1 text-slate-400 group-hover:text-cyan-300">坊市</span>
        </div>
      </el-tooltip>

      <el-tooltip content="更多功能" placement="top" :show-after="500">
        <div class="dock-item group">
          <div class="icon-box bg-gradient-to-br from-slate-700 to-slate-800 group-hover:bg-slate-700">
            <el-icon><Menu /></el-icon>
          </div>
          <span class="text-[10px] mt-1 text-slate-400">更多</span>
        </div>
      </el-tooltip>
    </div>

    <el-dialog v-model="showProfile" width="400px" class="game-dialog" :show-close="false" append-to-body align-center>
      <div class="bg-[#1a1c23] border border-slate-600 rounded-lg overflow-hidden relative text-slate-200 p-6">
        <button class="absolute top-2 right-2 text-slate-500 hover:text-white" @click="showProfile = false"><el-icon><Close /></el-icon></button>
        <h3 class="text-xl font-ink text-center text-emerald-400 mb-6 border-b border-white/10 pb-2">道友仙籍</h3>

        <div class="space-y-4">
          <div class="flex justify-between items-center bg-white/5 p-3 rounded">
            <span class="text-slate-400 text-sm">灵根资质</span>
            <div class="flex gap-1">
               <span v-for="(v, k) in spiritRoots" :key="k" class="text-xs px-1.5 py-0.5 rounded border border-white/20"
                     :class="getSpiritColor(k)">{{ k }} {{ v }}</span>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div class="stat-item"><span class="label">攻击</span> <span class="val text-red-400">{{ profile.attack }}</span></div>
            <div class="stat-item"><span class="label">防御</span> <span class="val text-blue-400">{{ profile.defense }}</span></div>
            <div class="stat-item"><span class="label">气血</span> <span class="val text-green-400">{{ profile.maxHp }}</span></div>
            <div class="stat-item"><span class="label">暴击</span> <span class="val text-amber-400">5%</span></div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showBag" width="600px" class="game-dialog" :show-close="false" append-to-body align-center>
      <div class="bg-[#1a1c23] border border-[#5d4037] rounded-lg overflow-hidden relative text-slate-200 h-[500px] flex flex-col">
        <div class="h-10 bg-[#3e2723] flex items-center justify-between px-4 border-b border-[#5d4037]">
          <span class="font-ink text-amber-100 text-lg">百宝囊</span>
          <button @click="showBag = false"><el-icon><Close /></el-icon></button>
        </div>
        <div class="flex-1 overflow-y-auto p-4 bg-[#2d2420]">
          <div v-if="bagItems.length" class="grid grid-cols-5 gap-3">
            <div v-for="item in bagItems" :key="item.id"
                 class="aspect-square bg-[#1a1c23] border border-[#5d4037] hover:border-amber-500 cursor-pointer rounded flex flex-col items-center justify-center p-2 group relative"
                 @click="useItem(item)">
              <div class="text-2xl mb-1">💊</div>
              <div class="text-[10px] text-center w-full truncate text-slate-300">{{ item.name }}</div>
              <div class="absolute right-1 bottom-1 text-[9px] text-slate-500">x{{ item.count || 1 }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无宝物" :image-size="80"></el-empty>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showShop" width="700px" class="game-dialog" :show-close="false" append-to-body align-center>
      <div class="bg-[#1a1c23] border border-cyan-900 rounded-lg overflow-hidden relative text-slate-200 h-[600px] flex flex-col">
        <div class="h-12 bg-cyan-950 flex items-center justify-between px-6 border-b border-cyan-900">
          <span class="font-ink text-cyan-100 text-xl">万宝楼</span>
          <button @click="showShop = false"><el-icon><Close /></el-icon></button>
        </div>
        <div class="p-4 grid grid-cols-2 gap-4 overflow-y-auto bg-[#0f172a]">
          <div v-for="good in shopGoods" :key="good.id" class="flex bg-slate-800/50 border border-slate-700 p-3 rounded hover:border-cyan-500 transition-colors">
            <div class="w-16 h-16 bg-black/30 rounded flex items-center justify-center text-3xl border border-white/5 mr-3">🎁</div>
            <div class="flex-1 flex flex-col justify-between">
              <div>
                <div class="text-cyan-200 font-bold text-sm">{{ good.name }}</div>
                <div class="text-xs text-slate-500 line-clamp-1 mt-1">{{ good.description }}</div>
              </div>
              <div class="flex justify-between items-center mt-2">
                <div class="text-amber-400 text-xs font-mono">灵石 {{ good.price }}</div>
                <button class="px-3 py-1 bg-cyan-800 hover:bg-cyan-700 text-xs rounded text-cyan-100" @click="handleBuyGoods(good)">购买</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <transition name="fade">
      <div v-if="showTribulation" class="fixed inset-0 z-[100] bg-black/90 flex flex-col items-center justify-center backdrop-blur-sm">
        <h1 class="text-6xl text-red-600 font-ink animate-pulse mb-8 tracking-widest" style="text-shadow: 0 0 20px red">天劫降临</h1>
        <div class="bg-black border-2 border-red-800 p-8 rounded-xl max-w-2xl w-full mx-4 shadow-[0_0_50px_rgba(220,38,38,0.5)]">
          <p class="text-red-200 text-center mb-6 text-lg">回答心魔之问，方可证道！</p>
          <div class="text-white text-xl font-bold mb-8 p-4 bg-red-950/30 rounded border border-red-900/50" v-html="currentQuestion?.content"></div>
          <div class="grid grid-cols-1 gap-4">
            <button v-for="opt in currentQuestionOptions" :key="opt.key"
                    @click="answerTribulation(opt.key)"
                    class="py-4 bg-slate-900 border border-slate-700 hover:bg-red-900 hover:border-red-500 hover:text-white transition-all rounded text-slate-300">
              <span class="font-bold mr-2 text-red-500">{{ opt.key }}.</span> {{ opt.value }}
            </button>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { useRouter } from 'vue-router';
import { ElMessage, ElNotification } from 'element-plus';
import {
  User, Box, Shop, EditPen, Menu, Close,
  Loading, Microphone, QuestionFilled
} from '@element-plus/icons-vue';

// 引入API (保持不变)
import {
  fetchGameProfile, meditate, breakthroughWithItem,
  breakthroughWithQuiz, fetchMyPills
} from '@/api/game';
import { fetchGoodsList, exchangeGoods } from '@/api/goods';
import UserAvatar from '@/components/UserAvatar.vue';

// --- 状态定义 ---
const router = useRouter();
const studentStore = useStudentAuthStore();

// 游戏核心数据
const profile = ref<any>({ currentExp: 0, maxExp: 100, attack: 0, defense: 0 });
const spiritRoots = ref<Record<string, number>>({});
const bagItems = ref<any[]>([]);
const shopGoods = ref<any[]>([]);
const logs = ref<string[]>([]);
const isMeditating = ref(false);

// UI控制状态
const showProfile = ref(false);
const showBag = ref(false);
const showShop = ref(false);
const showTribulation = ref(false);
const floatingTextContainer = ref<HTMLElement | null>(null);

// 渡劫题目
const currentQuestion = ref<any>(null);

// --- 计算属性 ---
const canBreakthrough = computed(() => {
  return profile.value.maxExp > 0 && profile.value.currentExp >= profile.value.maxExp;
});

const currentEventDescription = computed(() => {
  if (canBreakthrough.value) return '>>> 境界圆满，等待突破 <<<';
  if (isMeditating.value) return '... 天地灵气 汇入丹田 ...';
  return '道法自然，静候机缘';
});

// 显示最近的7条日志
const visibleLogs = computed(() => logs.value.slice(-7));

const currentQuestionOptions = computed(() => {
  if (!currentQuestion.value?.options) return [];
  try { return JSON.parse(currentQuestion.value.options); } catch (e) { return []; }
});

// --- 核心方法 ---

// 1. 修炼逻辑
const handleMeditate = async () => {
  if (isMeditating.value) return;
  isMeditating.value = true;

  // 模拟点击的视觉反馈（飘字）
  showFloatingText('+灵气', 'text-emerald-400');

  try {
    const res = await meditate();
    setTimeout(() => {
      isMeditating.value = false;
      if (res.code === 200) {
        addLog(`纳气成功，修为大涨。`);
        loadProfile();
        // 成功飘字
        showFloatingText('修为↑', 'text-amber-400 text-xl font-bold');
      } else {
        addLog(`心神不宁: ${res.msg}`);
      }
    }, 1500); // 假装修炼了一会儿，配合动画
  } catch (e) {
    isMeditating.value = false;
  }
};

// 2. 突破逻辑
const handleBreakthrough = async (itemId?: number) => {
  try {
    const res = await breakthroughWithItem({ goodsId: itemId });
    if (res.code === 200) {
      ElNotification({ title: '大能', message: '天降祥瑞，境界突破！', type: 'success', duration: 4000 });
      addLog('>>> 突破成功，境界提升！ <<<');
      loadProfile();
    } else if (res.code === 202) {
      showTribulation.value = true;
      currentQuestion.value = res.data.question;
      addLog('天劫降临，心魔骤生！');
    } else {
      ElMessage.error(res.msg);
    }
  } catch (e: any) {
    ElMessage.error(e.message);
  }
};

// 3. 答题逻辑
const answerTribulation = async (key: string) => {
  try {
    const res = await breakthroughWithQuiz({ questionId: currentQuestion.value.id, answer: key });
    if (res.code === 200) {
      showTribulation.value = false;
      ElMessage.success('道心稳固，渡劫成功！');
      loadProfile();
    } else {
      ElMessage.error('回答错误，渡劫失败！');
      showTribulation.value = false;
    }
  } catch(e) { showTribulation.value = false; }
};

// --- 工具与视觉效果 ---

// 飘字特效
const showFloatingText = (text: string, styleClass: string) => {
  if(!floatingTextContainer.value) return;
  const el = document.createElement('div');
  el.className = `absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 pointer-events-none animate-float-up ${styleClass}`;
  el.innerText = text;
  floatingTextContainer.value.appendChild(el);
  setTimeout(() => el.remove(), 1000);
};

const addLog = (msg: string) => {
  logs.value.push(msg);
};

const getLogColor = (text: string) => {
  if(text.includes('失败') || text.includes('天劫')) return 'text-red-400';
  if(text.includes('成功') || text.includes('突破')) return 'text-amber-400 font-bold';
  return 'text-emerald-100/80';
};

const getSpiritColor = (type: string) => {
  const map: any = { '金': 'text-yellow-200 bg-yellow-900/50', '木': 'text-green-200 bg-green-900/50', '水': 'text-blue-200 bg-blue-900/50', '火': 'text-red-200 bg-red-900/50', '土': 'text-amber-200 bg-amber-900/50' };
  return map[type] || 'text-slate-200';
};

const formatTime = (d: Date) => d.toTimeString().substring(0, 5);
const formatLargeNumber = (num: number) => num > 10000 ? (num/10000).toFixed(1) + '万' : num;
const calculateProgress = (c:number, m:number) => m ? Math.min((c/m)*100, 100) : 0;

// 基础数据加载
const loadProfile = async () => {
  const res = await fetchGameProfile();
  if(res.data) {
    profile.value = res.data.data || res.data;
    try { spiritRoots.value = JSON.parse(profile.value.spiritRoots || '{}'); } catch(e){}
  }
};
const loadBag = async () => { const res = await fetchMyPills(); bagItems.value = res.data || []; };
const loadShop = async () => { const res = await fetchGoodsList(); shopGoods.value = res.data || []; };
const handleBuyGoods = async (item: any) => { await exchangeGoods(item.id); loadBag(); ElMessage.success('购买成功'); };
const useItem = (item: any) => { if(item.name.includes('丹')) handleBreakthrough(item.id); };
const handlePractice = () => router.push('/student/practice');

// 初始化
onMounted(async () => {
  addLog('道友请留步，欢迎回到修仙界。');
  if(!studentStore.student) await studentStore.fetchStudentInfo();
  loadProfile();
  loadBag();
  loadShop();
});
</script>

<style scoped>
/* 引入 Google Fonts 书法字体 */
@import url('https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&display=swap');

.font-ink { font-family: 'Ma Shan Zheng', cursive, serif; }

/* 关键动画 */
@keyframes pan-slow {
  0% { transform: scale(1.1) translate(0, 0); }
  50% { transform: scale(1.15) translate(-1%, -1%); }
  100% { transform: scale(1.1) translate(0, 0); }
}
.animate-pan-slow { animation: pan-slow 60s ease-in-out infinite alternate; }

@keyframes spin-slow { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.animate-spin-slow { animation: spin-slow 10s linear infinite; }

@keyframes spin-reverse { from { transform: rotate(360deg); } to { transform: rotate(0deg); } }
.animate-spin-reverse { animation: spin-reverse 8s linear infinite; }

@keyframes float-up {
  0% { opacity: 1; transform: translate(-50%, -50%); }
  100% { opacity: 0; transform: translate(-50%, -150%); }
}
.animate-float-up { animation: float-up 1s ease-out forwards; }

/* UI 组件样式 */
.icon-btn {
  @apply w-8 h-8 rounded-full bg-black/40 text-slate-300 border border-white/10 hover:bg-emerald-900 hover:text-white flex items-center justify-center transition-colors backdrop-blur-sm;
}

.dock-item {
  @apply flex flex-col items-center cursor-pointer transition-transform duration-200 active:scale-95;
}
.icon-box {
  @apply w-10 h-10 rounded-xl flex items-center justify-center text-slate-200 shadow-lg border border-white/5 transition-all duration-300;
}

.stat-item {
  @apply bg-black/20 p-2 rounded flex justify-between text-sm border border-white/5;
}

/* 滚动条隐藏与遮罩 */
.mask-image-gradient {
  mask-image: linear-gradient(to bottom, transparent, black 20%);
}

.spirit-particle {
  position: absolute;
  width: 2px;
  height: 2px;
  background: rgba(16, 185, 129, 0.5);
  border-radius: 50%;
  top: -10px;
  left: 50%;
  animation: particleFall 10s infinite linear;
}
@for $i from 1 through 20 {
  .spirit-particle:nth-child(#{$i}) {
    left: random(100) * 1%;
    animation-duration: random(5) + 5s;
    animation-delay: random(5) * -1s;
    opacity: random(10) * 0.1;
  }
}
@keyframes particleFall {
  to { transform: translateY(100vh); }
}

/* 弹窗样式覆写 */
:deep(.game-dialog .el-dialog__header) { display: none; }
:deep(.game-dialog) {
  background: transparent !important;
  box-shadow: none !important;
  margin-top: 10vh !important;
}
</style>