<template>
  <div class="cultivation-container p-4 h-[calc(100vh-100px)]">
    <el-row :gutter="20" class="h-full">
      <el-col :span="6" class="h-full">
        <el-card class="h-full flex flex-col box-card-custom" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column' }">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="text-lg font-bold">道友信息</span>
              <el-tag type="warning" effect="dark" class="text-md">{{ profile.realmName || '凡人' }}</el-tag>
            </div>
          </template>

          <div class="flex flex-col items-center mb-6 mt-4">
            <el-avatar :size="100" :src="userStore.userInfo?.avatar || ''" class="mb-4 border-4 border-blue-100 shadow-md">
              {{ userStore.userInfo?.realName?.charAt(0) }}
            </el-avatar>
            <h2 class="text-2xl font-bold text-gray-700">{{ userStore.userInfo?.realName }}</h2>
          </div>

          <div class="stats-area flex-1 overflow-y-auto pr-2">
            <div class="mb-6">
              <div class="flex justify-between text-sm mb-1 text-gray-500">
                <span>修为进度</span>
                <span>{{ profile.currentExp }} / {{ profile.maxExp }}</span>
              </div>
              <el-progress
                  :percentage="calculateProgress(profile.currentExp, profile.maxExp)"
                  :stroke-width="18"
                  striped
                  striped-flow
                  :format="formatProgress"
                  color="#409eff"
              />
            </div>

            <el-divider content-position="left">基础属性</el-divider>

            <div class="grid grid-cols-2 gap-4 mb-4">
              <div class="stat-item bg-red-50 p-3 rounded-lg text-center">
                <div class="text-xs text-gray-500 mb-1">攻击力</div>
                <div class="text-xl font-bold text-red-500">{{ profile.attack || 0 }}</div>
              </div>
              <div class="stat-item bg-green-50 p-3 rounded-lg text-center">
                <div class="text-xs text-gray-500 mb-1">防御力</div>
                <div class="text-xl font-bold text-green-500">{{ profile.defense || 0 }}</div>
              </div>
              <div class="stat-item bg-orange-50 p-3 rounded-lg text-center col-span-2">
                <div class="text-xs text-gray-500 mb-1">最大生命值 (HP)</div>
                <div class="text-xl font-bold text-orange-500">{{ profile.maxHp || 100 }}</div>
              </div>
            </div>

            <el-divider content-position="left">灵根资质</el-divider>
            <div class="flex flex-wrap gap-2">
              <template v-if="Object.keys(spiritRoots).length > 0">
                <el-tooltip
                    v-for="(level, type) in spiritRoots"
                    :key="type"
                    :content="`等级: ${level} (加成效果)`"
                >
                  <el-tag :type="getSpiritType(type)" class="text-md py-1 px-3" effect="light">
                    {{ type }} Lv.{{ level }}
                  </el-tag>
                </el-tooltip>
              </template>
              <div v-else class="text-gray-400 text-sm w-full text-center">暂无灵根觉醒</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="h-full">
        <el-card class="h-full flex flex-col relative overflow-hidden" :body-style="{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }">

          <div class="absolute top-10 left-0 w-full text-center opacity-5 pointer-events-none select-none">
            <span class="text-[150px] font-bold text-gray-900">修仙</span>
          </div>

          <div class="z-10 text-center mb-10">
            <h1 class="text-3xl font-bold text-gray-800 mb-4">{{ gameStateText }}</h1>
            <p class="text-gray-500 text-lg max-w-md mx-auto">{{ currentEventDescription }}</p>
          </div>

          <div class="z-10 flex flex-col gap-6 items-center w-full max-w-sm">

            <el-button
                type="primary"
                class="w-full h-20 text-2xl shadow-lg transition-all hover:scale-105"
                :loading="isMeditating"
                @click="handleMeditate"
                v-if="!canBreakthrough"
                round
            >
              <el-icon class="mr-2"><VideoPlay /></el-icon>
              {{ isMeditating ? '打坐中...' : '开始打坐 (+修为)' }}
            </el-button>

            <el-button
                v-else
                type="warning"
                class="w-full h-24 text-3xl shadow-xl animate-pulse"
                @click="handleBreakthrough()"
                round
            >
              <el-icon class="mr-2"><Lightning /></el-icon>
              境界突破！
            </el-button>

            <div class="flex gap-4 w-full">
              <el-button class="flex-1 h-12 text-lg" @click="handlePractice">
                <el-icon class="mr-1"><Edit /></el-icon> 练题悟道
              </el-button>
              <el-button class="flex-1 h-12 text-lg" type="success" plain @click="showShop = true">
                <el-icon class="mr-1"><Shop /></el-icon> 坊市交易
              </el-button>
            </div>
          </div>

          <div class="absolute bottom-6 text-gray-400 text-sm">
            <el-icon><InfoFilled /></el-icon> 提示：练题可获得大量灵根经验，提升属性加成
          </div>
        </el-card>
      </el-col>

      <el-col :span="6" class="h-full">
        <el-card class="h-full flex flex-col" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column', padding: '0' }">
          <el-tabs v-model="activeRightTab" class="flex-1 flex flex-col custom-tabs" stretch>

            <el-tab-pane label="储物袋" name="bag" class="h-full flex flex-col">
              <div class="p-4 flex-1 overflow-y-auto">
                <div v-if="bagItems.length > 0" class="grid grid-cols-3 gap-2">
                  <div
                      v-for="item in bagItems"
                      :key="item.id"
                      class="aspect-square bg-gray-50 border rounded-lg flex flex-col items-center justify-center p-1 cursor-pointer hover:bg-blue-50 transition-colors relative group"
                      @click="useItem(item)"
                  >
                    <div class="text-2xl mb-1">💊</div>
                    <div class="text-xs text-center truncate w-full px-1">{{ item.name || item.goodsName }}</div>

                    <div class="absolute hidden group-hover:block bottom-full left-1/2 transform -translate-x-1/2 bg-black text-white text-xs p-2 rounded w-32 z-50 mb-1 pointer-events-none">
                      {{ item.description || '暂无描述' }}
                    </div>
                  </div>
                </div>
                <el-empty v-else description="储物袋空空如也" :image-size="60"></el-empty>
              </div>
            </el-tab-pane>

            <el-tab-pane label="修仙日志" name="log" class="h-full flex flex-col">
              <div class="p-4 flex-1 overflow-y-auto bg-gray-50 font-mono text-sm" ref="logContainer">
                <ul class="space-y-2">
                  <li v-for="(log, index) in logs" :key="index" class="border-b border-gray-100 pb-1 last:border-0">
                    <span class="text-gray-400 text-xs">[{{ formatTime(new Date()) }}]</span>
                    <span :class="getLogClass(log)"> {{ log }}</span>
                  </li>
                </ul>
              </div>
            </el-tab-pane>

          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showTribulation" title="⚠️ 心魔挑战 (天劫)" width="600px" :close-on-click-modal="false" :show-close="false" center>
      <div class="text-center mb-6">
        <p class="text-lg font-bold text-red-600 mb-2">天劫降临！回答正确方可突破！</p>
        <p class="text-gray-600" v-html="currentQuestion?.content"></p>
      </div>

      <div class="grid grid-cols-1 gap-3" v-if="currentQuestionOptions.length > 0">
        <el-button
            v-for="opt in currentQuestionOptions"
            :key="opt.key"
            size="large"
            @click="answerTribulation(opt.key)"
            :class="{'w-full': true}"
        >
          {{ opt.key }}. {{ opt.value }}
        </el-button>
      </div>
      <div class="flex gap-4 justify-center" v-else-if="currentQuestion?.questionType === 4">
        <el-button type="success" size="large" @click="answerTribulation('T')">正确</el-button>
        <el-button type="danger" size="large" @click="answerTribulation('F')">错误</el-button>
      </div>
      <div v-else class="text-center text-gray-500">
        (此题型暂不支持快速作答)
      </div>
    </el-dialog>

    <el-dialog v-model="showShop" title="灵石坊市" width="800px">
      <el-table :data="shopGoods" style="width: 100%">
        <el-table-column prop="name" label="宝物名称" width="180" />
        <el-table-column prop="price" label="价格 (积分)" width="120">
          <template #default="scope">
            <span class="text-orange-500 font-bold">{{ scope.row.price }} 灵石</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="功效" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleBuyGoods(scope.row)">购买</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue';
import { useUserStore } from '../../stores/auth';
import { useRouter } from 'vue-router';
import { ElMessage, ElNotification } from 'element-plus';
import { VideoPlay, Lightning, Edit, Shop, InfoFilled } from '@element-plus/icons-vue';

// 【修复 1】引入 api/game.ts 中的 fetchMyPills 用于获取背包（丹药）
import {
  fetchGameProfile,
  meditate,
  breakthroughWithItem,
  breakthroughWithQuiz,
  fetchMyPills
} from '../../api/game';

// 【修复 2】引入 api/goods.ts 中的正确方法 (fetchGoodsList 和 exchangeGoods)
import {
  fetchGoodsList,
  exchangeGoods
} from '../../api/goods';

// 类型定义
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
  id: number; // 商品ID
  name: string;
  description?: string;
  count?: number;
  goodsName?: string;
}

const router = useRouter();
const userStore = useUserStore();

// 状态变量
const profile = ref<CultivationProfile>({} as CultivationProfile);
const spiritRoots = ref<Record<string, number>>({});
const bagItems = ref<BagItem[]>([]);
const shopGoods = ref([]);
const logs = ref<string[]>(['欢迎来到修仙世界，道途漫漫，唯勤是岸。']);
const isMeditating = ref(false);
const showShop = ref(false);
const showTribulation = ref(false);
const currentQuestion = ref<any>(null);
const activeRightTab = ref('bag'); // 默认显示背包
const logContainer = ref<HTMLElement | null>(null);

// 计算属性：状态文本
const gameStateText = computed(() => {
  if (canBreakthrough.value) return '瓶颈期 - 需突破';
  if (isMeditating.value) return '正在打坐吐纳...';
  return '道心稳固';
});

const currentEventDescription = computed(() => {
  if (canBreakthrough.value) return '修为已至圆满，感应到天劫将至，请准备丹药或直接尝试突破！';
  if (isMeditating.value) return '天地灵气正在汇聚入体...';
  return '当前无事发生，你可以选择打坐、练题或整理行囊。';
});

// 计算是否可以突破
const canBreakthrough = computed(() => {
  return profile.value.currentExp >= profile.value.maxExp;
});

// 解析题目选项
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

// 辅助函数
const formatProgress = (percentage: number) => {
  if (percentage >= 100) return '圆满';
  return `${percentage}%`;
};

const calculateProgress = (current: number, max: number) => {
  if (!max) return 0;
  return Math.min(Math.floor((current / max) * 100), 100);
};

const formatTime = (date: Date) => {
  return date.toTimeString().split(' ')[0];
};

const getSpiritType = (type: string) => {
  const map: Record<string, string> = {
    '金': 'warning', '木': 'success', '水': 'primary', '火': 'danger', '土': 'info'
  };
  return map[type] || '';
};

const getLogClass = (text: string) => {
  if (text.includes('失败') || text.includes('天劫')) return 'text-red-500';
  if (text.includes('成功') || text.includes('突破')) return 'text-green-600 font-bold';
  if (text.includes('获得')) return 'text-orange-500';
  return 'text-gray-700';
};

const addLog = (msg: string) => {
  logs.value.push(msg);
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight;
    }
  });
};

// API 调用
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

// 核心功能
const handleMeditate = async () => {
  isMeditating.value = true;
  addLog('开始打坐，运转周天...');
  try {
    const res = await meditate();
    setTimeout(() => {
      if (res.code === 200) {
        const gainMsg = typeof res.data === 'string' ? res.data : (res.data.msg || '修为提升');
        addLog(`打坐结束: ${gainMsg}`);
        loadProfile();
      } else {
        addLog(`打坐被打断：${res.msg}`);
      }
      isMeditating.value = false;
    }, 1000);
  } catch (e) {
    isMeditating.value = false;
  }
};

const handleBreakthrough = async (itemId?: number) => {
  try {
    const res = await breakthroughWithItem({ goodsId: itemId });

    if (res.code === 200) {
      ElNotification({
        title: '突破成功',
        message: '恭喜道友境界提升！',
        type: 'success',
      });
      addLog('突破成功！境界提升！属性大幅增加！');
      loadProfile();
    } else if (res.code === 202) {
      // 触发心魔答题
      showTribulation.value = true;
      currentQuestion.value = res.data.question;
      addLog('突破遭遇心魔侵蚀，需通过考验！');
    } else {
      ElMessage.error(res.msg);
      addLog(`突破失败：${res.msg}`);
    }
  } catch (e: any) {
    ElMessage.error(e.message || '系统异常');
  }
};

const answerTribulation = async (option: string) => {
  try {
    const res = await breakthroughWithQuiz({
      questionId: currentQuestion.value.id,
      answer: option
    });

    if (res.code === 200) {
      ElMessage.success('心魔已破，渡劫成功！');
      showTribulation.value = false;
      loadProfile();
      addLog('回答正确，成功破除心魔，境界提升！');
    } else {
      ElMessage.error(res.msg || '回答错误');
    }
  } catch (e: any) {
    ElMessage.error('回答错误，渡劫失败，修为受损！');
    showTribulation.value = false;
    loadProfile();
    addLog('回答错误，心魔反噬，修为倒退！');
  }
};

const handlePractice = () => {
  router.push('/student/practice');
};

const handleBuyGoods = async (item: any) => {
  try {
    // 【修复】使用 exchangeGoods 兑换商品
    await exchangeGoods(item.id);
    ElMessage.success('购买成功');
    addLog(`在坊市购得 [${item.name}]`);
    loadBag(); // 刷新背包
  } catch (e) {
    // 错误由 request 拦截器处理
  }
};

const useItem = (item: BagItem) => {
  const itemName = item.name || item.goodsName || '';

  if (canBreakthrough.value) {
    if (itemName.includes('丹') || itemName.includes('药')) {
      ElMessage.info(`尝试使用 ${itemName} 辅助突破...`);
      // 注意：breakthroughWithItem 需要的是 goodsId (商品ID)，
      // api/game.ts 的 items 结构通常为 BizGoods 对象，直接传 item.id 即可
      handleBreakthrough(item.id);
    } else {
      ElMessage.warning('此物品无法直接使用');
    }
  } else {
    ElMessage.info('当前状态无需使用此物');
  }
};

onMounted(() => {
  loadProfile();
  loadBag();
  loadShop();
});
</script>

<style scoped>
.box-card-custom {
  transition: all 0.3s;
}

/* 隐藏滚动条但保持可滚动 */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}
.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 3px;
}
.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

/* 自定义 Element Tabs 样式使其充满高度 */
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
</style>