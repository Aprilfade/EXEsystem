<template>
  <div class="cultivation-container p-4 h-[calc(100vh-100px)] bg-slate-900 relative overflow-hidden">
    <div class="absolute top-0 left-0 w-full h-full bg-gradient-to-b from-slate-800 to-slate-900 opacity-90 -z-10"></div>
    <div class="bg-pattern -z-20"></div>

    <el-row :gutter="24" class="h-full relative z-10">
      <el-col :span="6" class="h-full">
        <el-card class="h-full flex flex-col jade-card border-none shadow-2xl" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column', padding: '1.5rem', background: 'rgba(16, 24, 39, 0.6)' }">
          <template #header>
            <div class="flex items-center justify-between border-b border-emerald-800/50 pb-3">
              <span class="text-xl font-bold text-emerald-100 calligraphy-text">道友仙籍</span>
              <el-tag effect="dark" color="#064e3b" class="text-md border-emerald-600 text-emerald-100 px-4 font-bold realm-tag shadow-lg">
                {{ profile.realmName || '凡人' }}
              </el-tag>
            </div>
          </template>

          <div class="flex flex-col items-center mb-8 mt-6 relative">
            <div class="absolute w-32 h-32 rounded-full bg-emerald-500/20 blur-xl animate-pulse"></div>
            <el-avatar :size="108" :src="studentStore.student?.avatar || ''" class="mb-4 border-[3px] border-emerald-400/50 shadow-[0_0_20px_rgba(16,185,129,0.4)] z-10 bg-slate-800">
              <span class="text-3xl text-emerald-100">{{ studentStore.student?.name?.charAt(0) }}</span>
            </el-avatar>
            <h2 class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-emerald-200 to-teal-100 shadow-sm">{{ studentStore.student?.name }}</h2>
          </div>

          <div class="stats-area flex-1 overflow-y-auto pr-2 custom-scrollbar">
            <div class="mb-8 p-4 bg-slate-800/50 rounded-xl border border-emerald-900/50 relative overflow-hidden">
              <div class="absolute inset-0 bg-gradient-to-r from-emerald-900/20 to-transparent pointer-events-none"></div>
              <div class="flex justify-between text-sm mb-2 text-emerald-300/80">
                <span>修为进度</span>
                <span class="font-mono text-emerald-100">{{ profile.currentExp }} / {{ profile.maxExp }}</span>
              </div>
              <div class="h-4 bg-slate-900 rounded-full overflow-hidden border border-emerald-900/80 p-[2px]">
                <div
                    class="h-full bg-gradient-to-r from-emerald-600 to-teal-400 rounded-full transition-all duration-500 relative"
                    :style="{ width: `${calculateProgress(profile.currentExp, profile.maxExp)}%` }"
                >
                  <div class="absolute inset-0 bg-white/20 animate-pulse-slow"></div>
                </div>
              </div>
              <div class="text-center mt-1 text-xs text-emerald-400">{{ formatProgress(calculateProgress(profile.currentExp, profile.maxExp)) }}</div>
            </div>

            <div class="divider-text text-emerald-600/70 mb-4">基础属性</div>

            <div class="grid grid-cols-2 gap-4 mb-6">
              <div class="stat-panel bg-red-900/20 border-red-800/50 text-red-200">
                <div class="text-xs text-red-400/80 mb-1">攻击力</div>
                <div class="text-xl font-bold font-mono">{{ profile.attack || 0 }}</div>
              </div>
              <div class="stat-panel bg-indigo-900/20 border-indigo-800/50 text-indigo-200">
                <div class="text-xs text-indigo-400/80 mb-1">防御力</div>
                <div class="text-xl font-bold font-mono">{{ profile.defense || 0 }}</div>
              </div>
              <div class="stat-panel col-span-2 bg-amber-900/20 border-amber-800/50 text-amber-200 flex justify-between items-center px-6">
                <div class="text-xs text-amber-400/80">气血 (HP)</div>
                <div class="text-2xl font-bold font-mono">{{ profile.maxHp || 100 }}</div>
              </div>
            </div>

            <div class="divider-text text-emerald-600/70 mb-4">灵根资质</div>
            <div class="flex flex-wrap gap-3 justify-center">
              <template v-if="Object.keys(spiritRoots).length > 0">
                <el-tooltip
                    v-for="(level, type) in spiritRoots"
                    :key="type"
                    :content="`等级: ${level} (加成效果)`"
                    placement="top"
                >
                  <div :class="`spirit-root-pill spirit-${type}`">
                    <span class="spirit-name">{{ type }}</span>
                    <span class="spirit-level">Lv.{{ level }}</span>
                  </div>
                </el-tooltip>
              </template>
              <div v-else class="text-emerald-600/60 text-sm w-full text-center italic py-4">凡胎肉体，暂无灵根觉醒</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="h-full">
        <el-card
            class="h-full flex flex-col relative overflow-hidden main-stage-card border-none shadow-2xl"
            :body-style="{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', padding: 0, position: 'relative' }"
            style="background-image: url('https://images.unsplash.com/photo-1518747763431-663144385843?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D'); background-size: cover; background-position: center;"
        >
          <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/70 to-slate-800/40 z-0"></div>

          <div class="absolute top-[10%] w-full text-center pointer-events-none select-none z-10 mix-blend-overlay opacity-60">
            <span class="text-[180px] font-bold text-slate-900 ink-text-shadow" style="font-family: 'STKaiti', 'KaiTi', serif;">修仙</span>
          </div>

          <div class="z-20 text-center mb-12 relative">
            <div class="absolute -inset-4 bg-emerald-500/20 blur-2xl rounded-full opacity-50"></div>
            <h1 class="text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-b from-yellow-100 via-yellow-200 to-amber-500 mb-6 relative calligraphy-text text-shadow-glow">
              {{ gameStateText }}
            </h1>
            <p class="text-emerald-100/80 text-lg max-w-lg mx-auto bg-slate-900/50 py-3 px-6 rounded-full border border-emerald-800/30 backdrop-blur-sm">
              {{ currentEventDescription }}
            </p>
          </div>

          <div class="z-20 flex flex-col gap-8 items-center w-full max-w-md px-6">

            <button
                v-if="!canBreakthrough"
                class="cultivate-btn group relative w-full h-24 rounded-2xl overflow-hidden shadow-[0_0_30px_rgba(16,185,129,0.3)] transition-all hover:scale-105 hover:shadow-[0_0_50px_rgba(16,185,129,0.5)] active:scale-95"
                :class="{ 'is-loading': isMeditating }"
                @click="handleMeditate"
                :disabled="isMeditating"
            >
              <div class="absolute inset-0 bg-gradient-to-br from-emerald-600 to-teal-800 opacity-90 group-hover:opacity-100 transition-opacity"></div>
              <div class="absolute inset-0 bg-[url('@/assets/noise.png')] opacity-10 mix-blend-overlay"></div>
              <div class="absolute top-0 left-0 w-full h-full bg-gradient-to-t from-transparent via-emerald-400/20 to-transparent -translate-y-full group-hover:translate-y-full transition-transform duration-1000 ease-in-out"></div>

              <div class="relative z-10 flex flex-col items-center justify-center h-full text-emerald-50">
                <el-icon class="text-4xl mb-1 animate-float" v-if="!isMeditating"><VideoPlay /></el-icon>
                <el-icon class="text-4xl mb-1 animate-spin-slow" v-else><Loading /></el-icon>
                <span class="text-2xl font-bold tracking-wider calligraphy-text">{{ isMeditating ? '吐纳中...' : '打坐修炼' }}</span>
                <span class="text-sm text-emerald-200/70" v-if="!isMeditating">吸纳天地灵气 (+修为)</span>
              </div>
            </button>

            <button
                v-else
                class="breakthrough-btn group relative w-full h-28 rounded-2xl overflow-hidden shadow-[0_0_40px_rgba(245,158,11,0.4)] animate-pulse-slow hover:scale-105 transition-all active:scale-95"
                @click="handleBreakthrough()"
            >
              <div class="absolute inset-0 bg-gradient-to-br from-amber-500 to-red-700 opacity-90"></div>
              <div class="absolute inset-0 bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-yellow-300/30 via-transparent to-transparent animate-ping-slow"></div>

              <div class="relative z-10 flex items-center justify-center h-full text-yellow-50 space-x-4">
                <el-icon class="text-5xl filter drop-shadow-[0_0_10px_rgba(251,191,36,0.8)]"><Lightning /></el-icon>
                <div class="text-left">
                  <div class="text-3xl font-bold calligraphy-text text-shadow-glow-gold">境界突破</div>
                  <div class="text-sm text-yellow-200">瓶颈已至，逆天改命！</div>
                </div>
              </div>
            </button>

            <div class="flex gap-6 w-full">
              <button class="side-action-btn bg-gradient-to-br from-blue-600 to-indigo-800 hover:from-blue-500 hover:to-indigo-700" @click="handlePractice">
                <el-icon class="mr-2 text-xl"><Edit /></el-icon> 练题悟道
              </button>
              <button class="side-action-btn bg-gradient-to-br from-teal-600 to-emerald-800 hover:from-teal-500 hover:to-emerald-700" @click="showShop = true">
                <el-icon class="mr-2 text-xl"><Shop /></el-icon> 坊市交易
              </button>
            </div>
          </div>

          <div class="absolute bottom-6 text-emerald-400/60 text-sm flex items-center bg-slate-900/60 px-4 py-2 rounded-full backdrop-blur-md">
            <el-icon class="mr-2"><InfoFilled /></el-icon> 提示：练题可获得大量灵根经验，提升属性加成
          </div>
        </el-card>
      </el-col>

      <el-col :span="6" class="h-full">
        <el-card class="h-full flex flex-col border-none shadow-2xl scroll-card" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column', padding: '0', background: 'transparent' }">
          <div class="absolute inset-0 bg-[#fdf6e3] opacity-90 parchment-texture -z-10 rounded overflow-hidden border-2 border-[#d4c4a9]"></div>
          <div class="absolute top-0 left-0 right-0 h-4 bg-gradient-to-b from-[#8b5e3c] to-[#6d4a2e] rounded-t z-20 border-b border-[#5a3d26]"></div>
          <div class="absolute bottom-0 left-0 right-0 h-4 bg-gradient-to-t from-[#8b5e3c] to-[#6d4a2e] rounded-b z-20 border-t border-[#5a3d26]"></div>

          <el-tabs v-model="activeRightTab" class="flex-1 flex flex-col custom-tabs-ink mt-5 mb-5" stretch>

            <el-tab-pane label="储物袋" name="bag" class="h-full flex flex-col">
              <div class="p-5 flex-1 overflow-y-auto custom-scrollbar-ink relative z-10">
                <div v-if="bagItems.length > 0" class="grid grid-cols-3 gap-3">
                  <div
                      v-for="item in bagItems"
                      :key="item.id"
                      class="aspect-square item-slot flex flex-col items-center justify-center p-2 cursor-pointer relative group"
                      @click="useItem(item)"
                  >
                    <div class="w-12 h-12 mb-1 relative flex items-center justify-center bg-[#eaddca] rounded-lg border border-[#c7b299] shadow-inner overflow-hidden group-hover:border-amber-500/50 transition-colors">
                      <div class="text-3xl filter drop-shadow-sm">💊</div>
                      <div class="absolute inset-0 bg-amber-200/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
                    </div>

                    <div class="text-xs text-center truncate w-full px-1 font-bold text-[#5c4b37]">{{ item.name || item.goodsName }}</div>

                    <div class="absolute hidden group-hover:block bottom-full left-1/2 transform -translate-x-1/2 bg-slate-800 text-amber-100 text-xs p-3 rounded-lg w-40 z-50 mb-2 pointer-events-none shadow-xl border border-amber-500/30">
                      <div class="font-bold mb-1 text-amber-300">{{ item.name || item.goodsName }}</div>
                      {{ item.description || '暂无描述' }}
                    </div>
                  </div>
                </div>
                <el-empty v-else description="囊中羞涩" :image-size="60">
                  <template #description>
                    <span class="text-[#8c7b67] italic">储物袋空空如也...</span>
                  </template>
                </el-empty>
              </div>
            </el-tab-pane>

            <el-tab-pane label="修仙日志" name="log" class="h-full flex flex-col">
              <div class="p-5 pl-6 flex-1 overflow-y-auto custom-scrollbar-ink font-mono text-sm relative z-10" ref="logContainer" style="background: transparent">
                <ul class="space-y-3 relative">
                  <div class="absolute left-[4.5rem] top-2 bottom-2 w-[2px] bg-[#c7b299]/50 z-0"></div>
                  <li v-for="(log, index) in logs" :key="index" class="relative z-10 flex items-start">
                    <span class="text-[#8c7b67] text-xs min-w-[4rem] text-right mr-4 pt-1 font-bold">{{ formatTime(new Date()) }}</span>
                    <div class="absolute left-[4.35rem] top-[0.4rem] w-3 h-3 rounded-full border-2 border-[#c7b299] bg-[#fdf6e3] z-20"></div>
                    <div :class="['py-1 px-3 rounded-lg text-sm flex-1 shadow-sm border', getLogClass(log)]">
                      {{ log }}
                    </div>
                  </li>
                </ul>
              </div>
            </el-tab-pane>

          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showTribulation" class="tribulation-dialog" width="600px" :close-on-click-modal="false" :show-close="false" center align-center style="background: transparent; box-shadow: none;">
      <template #header>
        <div class="text-center relative py-4">
          <h2 class="text-3xl font-bold text-red-500 calligraphy-text text-shadow-glow-red">⚠️ 天劫降临 ⚠️</h2>
          <div class="text-red-300/80 mt-2">心魔幻境，稍有不慎则万劫不复！</div>
        </div>
      </template>
      <div class="tribulation-content bg-slate-900/90 border-2 border-red-800/80 rounded-xl p-8 shadow-[0_0_50px_rgba(220,38,38,0.3)] text-center relative overflow-hidden">
        <div class="absolute inset-0 bg-[url('https://media.giphy.com/media/3o7btXkbsonFS8KzUA/giphy.gif')] opacity-10 bg-cover pointer-events-none mix-blend-screen"></div>

        <p class="text-xl text-slate-200 mb-8 font-bold relative z-10 px-4 py-6 bg-red-950/50 rounded-lg border border-red-900/50" v-html="currentQuestion?.content"></p>

        <div class="grid grid-cols-1 gap-4 relative z-10" v-if="currentQuestionOptions.length > 0">
          <button
              v-for="opt in currentQuestionOptions"
              :key="opt.key"
              @click="answerTribulation(opt.key)"
              class="w-full py-4 px-6 text-lg rounded-lg bg-gradient-to-r from-slate-800 to-slate-700 border border-slate-600 text-slate-200 hover:from-red-900 hover:to-red-800 hover:border-red-500 transition-all shadow-md active:scale-[0.98] flex items-center"
          >
            <span class="w-8 h-8 rounded-full bg-slate-900 flex items-center justify-center font-bold text-red-500 mr-4 border border-red-900">{{ opt.key }}</span>
            {{ opt.value }}
          </button>
        </div>
        <div class="flex gap-4 justify-center relative z-10 mt-6" v-else-if="currentQuestion?.questionType === 4">
          <el-button type="success" size="large" class="flex-1 !h-14 !text-xl !bg-green-700/80 !border-green-600 hover:!bg-green-600" @click="answerTribulation('T')">是 (Correct)</el-button>
          <el-button type="danger" size="large" class="flex-1 !h-14 !text-xl !bg-red-700/80 !border-red-600 hover:!bg-red-600" @click="answerTribulation('F')">否 (Wrong)</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showShop" title="灵石坊市" width="800px" class="shop-dialog">
      <template #header>
        <div class="text-xl font-bold text-[#5c4b37] calligraphy-text flex items-center"><el-icon class="mr-2 text-amber-600"><Shop /></el-icon> 灵石坊市</div>
      </template>
      <el-table :data="shopGoods" style="width: 100%; background: transparent;" row-key="id" :header-cell-style="{background: '#eaddca', color: '#5c4b37', borderColor: '#c7b299'}" :cell-style="{background: 'transparent', borderColor: '#c7b299', color: '#5c4b37'}">
        <el-table-column prop="name" label="宝物名称" width="180">
          <template #default="scope">
            <span class="font-bold">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格 (灵石)" width="140">
          <template #default="scope">
            <div class="flex items-center text-amber-600 font-bold bg-amber-100/50 px-2 py-1 rounded-full w-max">
              <span class="text-lg mr-1">💎</span> {{ scope.row.price }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="功效说明" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" color="#8b5e3c" class="!border-[#6d4a2e] hover:!bg-[#6d4a2e]" @click="handleBuyGoods(scope.row)">购买</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { useRouter } from 'vue-router';
import { ElMessage, ElNotification } from 'element-plus';
import { VideoPlay, Lightning, Edit, Shop, InfoFilled, Loading } from '@element-plus/icons-vue';

import {
  fetchGameProfile,
  meditate,
  breakthroughWithItem,
  breakthroughWithQuiz,
  fetchMyPills
} from '@/api/game';

import {
  fetchGoodsList,
  exchangeGoods
} from '@/api/goods';

// --- 接口定义保持不变 ---
interface CultivationProfile {
  realmLevel: number;
  realmName: string;
  currentExp: number;
  maxExp: number;
  attack: number;
  defense: number;
  maxHp: number;
  spiritRoots: string;
}

interface BagItem {
  id: number;
  name: string;
  description?: string;
  count?: number;
  goodsName?: string;
}

// --- 状态和逻辑保持不变 ---
const router = useRouter();
const studentStore = useStudentAuthStore();

const profile = ref<CultivationProfile>({} as CultivationProfile);
const spiritRoots = ref<Record<string, number>>({});
const bagItems = ref<BagItem[]>([]);
const shopGoods = ref([]);
const logs = ref<string[]>(['踏入仙途，道阻且长，唯勤为径。']);
const isMeditating = ref(false);
const showShop = ref(false);
const showTribulation = ref(false);
const currentQuestion = ref<any>(null);
const activeRightTab = ref('bag');
const logContainer = ref<HTMLElement | null>(null);

const gameStateText = computed(() => {
  if (canBreakthrough.value) return '瓶颈已至 · 待突破';
  if (isMeditating.value) return '吐纳天地 · 修炼中';
  return '道心稳固 · 寻机缘';
});

const currentEventDescription = computed(() => {
  if (canBreakthrough.value) return '体魄已臻化境，感应天劫将至。请备好丹药法宝，尝试逆天改命！';
  if (isMeditating.value) return '周天运转，灵气正源源不断汇入丹田...';
  return '当前暂无要事。可选择打坐积攒修为，或前往坊市寻宝，亦可练题悟道。';
});

const canBreakthrough = computed(() => {
  if (!profile.value.maxExp) return false;
  return profile.value.currentExp >= profile.value.maxExp;
});

const currentQuestionOptions = computed(() => {
  if (!currentQuestion.value || !currentQuestion.value.options) return [];
  try {
    const opts = JSON.parse(currentQuestion.value.options);
    if (Array.isArray(opts)) return opts;
    return [];
  } catch (e) {
    return [];
  }
});

const formatProgress = (percentage: number) => {
  if (percentage >= 100) return '圆满之境';
  return `当前进度: ${percentage}%`;
};

const calculateProgress = (current: number, max: number) => {
  if (!max) return 0;
  return Math.min(Math.floor((current / max) * 100), 100);
};

const formatTime = (date: Date) => {
  return date.toTimeString().split(' ')[0].substring(0, 5);
};

// 移除 getSpiritType，使用新的 CSS 类名方式

const getLogClass = (text: string) => {
  // 返回 Tailwind 类名组合，配合新的 CSS 变量
  if (text.includes('失败') || text.includes('天劫') || text.includes('受损')) return 'bg-red-100/80 text-red-800 border-red-300';
  if (text.includes('成功') || text.includes('突破')) return 'bg-emerald-100/80 text-emerald-800 border-emerald-300 font-bold';
  if (text.includes('获得') || text.includes('购得')) return 'bg-amber-100/80 text-amber-800 border-amber-300';
  return 'bg-[#f0e6d2] text-[#5c4b37] border-[#d4c4a9]'; // 默认古纸风格
};

const addLog = (msg: string) => {
  logs.value.push(msg);
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight;
    }
  });
};

// --- 数据加载和交互逻辑保持不变 ---
const loadProfile = async () => {
  try {
    const res = await fetchGameProfile();
    if (res.data) {
      profile.value = res.data.data || res.data;
      if (res.data.realmName) profile.value.realmName = res.data.realmName;

      if (profile.value.spiritRoots) {
        try {
          spiritRoots.value = JSON.parse(profile.value.spiritRoots);
        } catch (e) {
          spiritRoots.value = {};
        }
      }
    }
  } catch (error) {
    console.error(error);
  }
};

const loadBag = async () => {
  try {
    const res = await fetchMyPills();
    bagItems.value = res.data || [];
  } catch(e) {
    console.error(e);
  }
};

const loadShop = async () => {
  try {
    const res = await fetchGoodsList();
    shopGoods.value = res.data || [];
  } catch(e) {
    console.error(e);
  }
};

const handleMeditate = async () => {
  isMeditating.value = true;
  addLog('盘膝而坐，开始运转周天功法...');
  try {
    const res = await meditate();
    // 模拟动画延迟
    setTimeout(() => {
      if (res.code === 200) {
        const gainMsg = typeof res.data === 'string' ? res.data : (res.data.msg || '修为有所精进');
        addLog(`修炼结束: ${gainMsg}`);
        loadProfile();
      } else {
        addLog(`心神不宁，修炼中断：${res.msg}`);
      }
      isMeditating.value = false;
    }, 1500); // 增加一点延迟感
  } catch (e) {
    isMeditating.value = false;
    addLog('修炼发生意外。');
  }
};

const handleBreakthrough = async (itemId?: number) => {
  try {
    const res = await breakthroughWithItem({ goodsId: itemId });

    if (res.code === 200) {
      ElNotification({
        title: '天降祥瑞',
        message: '恭喜道友突破瓶颈，境界大增！',
        type: 'success',
        duration: 5000,
        customClass: 'breakthrough-notification' // 可以自定义通知样式
      });
      addLog('突破成功！天地灵气灌体，境界提升，寿元大增！');
      loadProfile();
    } else if (res.code === 202) {
      showTribulation.value = true;
      currentQuestion.value = res.data.question;
      addLog('突破之际，心魔幻境陡然降临！需守住道心！');
    } else {
      ElMessage.error(res.msg);
      addLog(`突破失败，真气逆行：${res.msg}`);
    }
  } catch (e: any) {
    ElMessage.error(e.message || '天道紊乱');
  }
};

const answerTribulation = async (option: string) => {
  try {
    const res = await breakthroughWithQuiz({
      questionId: currentQuestion.value.id,
      answer: option
    });

    if (res.code === 200) {
      ElMessage.success({ message: '道心通明，成功渡过心魔劫！', duration: 3000 });
      showTribulation.value = false;
      loadProfile();
      addLog('回答正确，心魔消散，成功突破境界！');
    } else {
      ElMessage.error({ message: '道心不稳，渡劫失败！', duration: 3000 });
      // 保持弹窗或关闭看需求，这里先不关闭让用户看到错误
      addLog('回答错误，被心魔反噬，修为受损...');
    }
  } catch (e: any) {
    ElMessage.error('渡劫失败，修为大损！');
    showTribulation.value = false;
    loadProfile();
    addLog('心魔劫失败，境界跌落。');
  }
};

const handlePractice = () => {
  router.push('/student/practice');
};

const handleBuyGoods = async (item: any) => {
  try {
    await exchangeGoods(item.id);
    ElMessage.success({ message: '交易成功，宝物已收入囊中', type: 'success' });
    addLog(`在坊市耗费灵石，购得 [${item.name}] 一件。`);
    loadBag();
  } catch (e) {
    // api自会处理错误提示
  }
};

const useItem = (item: BagItem) => {
  const itemName = item.name || item.goodsName || '';

  if (canBreakthrough.value) {
    if (itemName.includes('丹') || itemName.includes('药')) {
      ElMessage.info(`尝试炼化 [${itemName}] 以辅助突破...`);
      handleBreakthrough(item.id);
    } else {
      ElMessage.warning('此物非突破所用之丹药。');
    }
  } else {
    ElMessage.info('当前境界稳固，暂无需服用此丹药。');
  }
};

onMounted(async () => {
  if (!studentStore.student) {
    await studentStore.fetchStudentInfo();
  }
  loadProfile();
  loadBag();
  loadShop();
});
</script>

<style scoped>
/* --- 全局纹理背景 --- */
.bg-pattern {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  /* 一个微妙的云纹/古风纹理图案 (SVG base64, 也可以用图片链接) */
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M54.627 0l.83.828-1.415 1.415L51.8 0h2.827zM5.373 0l-.83.828L5.96 2.243 8.2 0H5.374zM48.97 0l3.657 3.657-1.414 1.414L46.143 0h2.828zM11.03 0L7.372 3.657 8.787 5.07 13.857 0H11.03zm32.284 0L49.8 6.485 48.384 7.9l-7.9-7.9h2.83zM16.686 0L10.2 6.485 11.616 7.9l7.9-7.9h-2.83zM22.344 0L13.858 8.485 15.272 9.9l7.9-7.9h-.828zm5.656 0L19.515 8.485 18.1 9.9l-7.9-7.9h-.828zm11.313 0L30.83 8.485 29.413 9.9l-7.9-7.9h-.828zm5.657 0L36.485 8.485 35.07 9.9l-7.9-7.9h-.828zm11.313 0L47.8 8.485 46.384 9.9l-7.9-7.9h-.828zM0 0l.828.828-1.415 1.415L0 0zm0 5.373l.828-.828L2.243 5.96 0 8.2V5.374zm0 43.597l.828.828-1.415 1.415L0 48.97v2.828zm0 5.657l.828-.828L2.243 55.343 0 57.586V54.63zm0 5.657l.828.828-1.415 1.415L0 60zm54.627 60l.83-.828-1.415-1.415L51.8 60h2.827zm-49.254 0l-.83-.828L5.96 57.757 8.2 60H5.374zm43.597 0l3.657-3.657-1.414-1.414L46.143 60h2.828zm-37.94 0L7.372 56.343 8.787 54.93 13.857 60H11.03zm32.284 0L49.8 53.515 48.384 52.1l-7.9 7.9h2.83zm-26.628 0L10.2 53.515 11.616 52.1l7.9 7.9h-2.83zm5.656 0L13.858 51.515 15.272 50.1l7.9 7.9h-.828zm5.656 0L19.515 51.515 18.1 50.1l-7.9 7.9h-.828zm11.313 0L30.83 51.515 29.413 50.1l-7.9 7.9h-.828zm5.657 0L36.485 51.515 35.07 50.1l-7.9 7.9h-.828zm11.313 0L47.8 51.515 46.384 50.1l-7.9 7.9h-.828zM0 60l.828-.828-1.415-1.415L0 60zm0-5.373l.828.828L2.243 54.04 0 51.8v2.827zm0-43.597l.828-.828L2.243 4.657 0 2.414V5.03zm0-5.657l.828.828-1.415 1.415L0 0z' fill='%23ffffff' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
}

/* --- 字体与文本效果 --- */
/* 模拟书法字体，如果没有STKaiti则回退到serif */
.calligraphy-text {
  font-family: "STKaiti", "KaiTi", "SimSun", serif;
}

/* 水墨文字阴影 */
.ink-text-shadow {
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3), -1px -1px 0 rgba(255,255,255,0.1);
}
/* 发光文字阴影 */
.text-shadow-glow {
  text-shadow: 0 0 10px rgba(16, 185, 129, 0.5), 0 0 20px rgba(16, 185, 129, 0.3);
}
.text-shadow-glow-gold {
  text-shadow: 0 0 10px rgba(251, 191, 36, 0.6), 0 0 20px rgba(245, 158, 11, 0.4);
}
.text-shadow-glow-red {
  text-shadow: 0 0 10px rgba(239, 68, 68, 0.8);
}

/* --- 卡片样式重置与美化 --- */
.el-card {
  border: none;
  overflow: visible; /* 允许光晕溢出 */
}
/* 玉简卡片 */
.jade-card {
  background: rgba(6, 78, 59, 0.2); /* 深绿色半透明 */
  backdrop-filter: blur(10px);
  border: 1px solid rgba(16, 185, 129, 0.2);
  box-shadow: inset 0 0 20px rgba(16, 185, 129, 0.1), 0 10px 30px -10px rgba(0,0,0,0.5);
}
/* 卷轴卡片 */
.scroll-card {
  position: relative;
  padding: 20px 0; /* 为卷轴轴头留空 */
}

/* --- 左侧面板组件 --- */
.realm-tag {
  border-width: 1px;
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.4);
}
.stat-panel {
  @apply p-3 rounded-xl text-center border backdrop-blur-sm transition-all hover:scale-105;
}
.divider-text {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
  font-weight: bold;
}
.divider-text::before, .divider-text::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(16, 185, 129, 0.3);
  margin: 0 10px;
}

/* 灵根丹药样式 */
.spirit-root-pill {
  @apply px-3 py-1.5 rounded-full text-sm font-bold border shadow-sm flex items-center cursor-help transition-all hover:-translate-y-1;
}
.spirit-name { @apply mr-1; }
.spirit-level { @apply text-xs opacity-80; }

.spirit-金 { @apply bg-yellow-100 text-yellow-800 border-yellow-300; }
.spirit-木 { @apply bg-green-100 text-green-800 border-green-300; }
.spirit-水 { @apply bg-blue-100 text-blue-800 border-blue-300; }
.spirit-火 { @apply bg-red-100 text-red-800 border-red-300; }
.spirit-土 { @apply bg-[#E3D8C3] text-[#8C7B67] border-[#C7B299]; }

/* --- 中间面板交互组件 --- */
.main-stage-card {
  /* 确保背景图在最底层 */
  z-index: 0;
}
.cultivate-btn, .breakthrough-btn, .side-action-btn {
  @apply text-white font-bold rounded-xl flex items-center justify-center transition-all duration-300;
  border: none;
  outline: none;
  cursor: pointer;
}
.side-action-btn {
  @apply h-14 flex-1 text-lg shadow-lg border border-white/10;
}
.cultivate-btn.is-loading {
  @apply cursor-not-allowed opacity-90;
}

/* --- 右侧面板组件 (古风卷轴) --- */
.parchment-texture {
  background-color: #fdf6e3;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'%3E%3Cg fill-rule='evenodd'%3E%3Cg fill='%23d4c4a9' fill-opacity='0.4'%3E%3Cpath opacity='.5' d='M96 95h4v1h-4v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9zm-1 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm9-10v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm9-10v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm9-10v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9z'/%3E%3Cpath d='M6 5V0H5v5H0v1h5v94h1V6h94V5H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

/* 自定义水墨风格 Tabs */
:deep(.custom-tabs-ink .el-tabs__header) {
  margin-bottom: 0;
  background: transparent;
  border-bottom: 2px solid #c7b299;
}
:deep(.custom-tabs-ink .el-tabs__nav-wrap::after) {
  display: none;
}
:deep(.custom-tabs-ink .el-tabs__item) {
  font-family: "STKaiti", "KaiTi", serif;
  font-weight: bold;
  color: #8c7b67;
  font-size: 1.1rem;
  transition: all 0.3s;
}
:deep(.custom-tabs-ink .el-tabs__item.is-active) {
  color: #5c4b37;
  text-shadow: 0 0 1px currentColor;
}
:deep(.custom-tabs-ink .el-tabs__active-bar) {
  background-color: #8b5e3c;
  height: 3px;
}
:deep(.el-tabs__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 !important;
  height: 100%;
}
:deep(.el-tab-pane) {
  height: 100%;
}

/* 物品槽位纹理 */
.item-slot {
  background-image: radial-gradient(circle at center, #fdf6e3 0%, #eaddca 100%);
  border: 1px solid #d4c4a9;
  border-radius: 0.75rem;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

/* --- 弹窗样式深度定制 --- */
:deep(.el-dialog) {
  background: transparent !important;
  box-shadow: none !important;
}
:deep(.el-dialog__header) {
  padding-bottom: 0;
}
:deep(.el-dialog__body) {
  padding: 0;
}

/* 坊市弹窗 */
.shop-dialog :deep(.el-dialog) {
  background: #fdf6e3 !important; /* 恢复纸张背景 */
  border: 3px solid #8b5e3c;
  border-radius: 1rem;
  box-shadow: 0 20px 40px rgba(0,0,0,0.5) !important;
  overflow: hidden;
}
.shop-dialog :deep(.el-dialog__header) {
  background: #eaddca;
  border-bottom: 2px solid #c7b299;
  padding: 15px 20px;
  margin-right: 0;
}
.shop-dialog :deep(.el-table) {
  --el-table-border-color: #c7b299;
  --el-table-row-hover-bg-color: #eaddca;
}


/* --- 滚动条美化 --- */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(16, 185, 129, 0.5);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(0,0,0,0.1);
}

.custom-scrollbar-ink::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar-ink::-webkit-scrollbar-thumb {
  background: #c7b299;
  border-radius: 4px;
  border: 1px solid #8b5e3c;
}
.custom-scrollbar-ink::-webkit-scrollbar-track {
  background: #fdf6e3;
}

/* --- 动画定义 --- */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}
.animate-float { animation: float 3s ease-in-out infinite; }

@keyframes spin-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.animate-spin-slow { animation: spin-slow 3s linear infinite; }

@keyframes pulse-slow {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
.animate-pulse-slow { animation: pulse-slow 3s cubic-bezier(0.4, 0, 0.6, 1) infinite; }

@keyframes ping-slow {
  75%, 100% { transform: scale(1.5); opacity: 0; }
}
.animate-ping-slow { animation: ping-slow 2s cubic-bezier(0, 0, 0.2, 1) infinite; }

</style>