<template>
  <el-dialog
      :model-value="visible"
      title="💎 积分商城"
      width="800px"
      @close="handleClose"
      append-to-body
  >
    <div class="mall-header">
      <div class="my-points">
        <el-icon class="points-icon"><Coin /></el-icon>
        当前积分：<span class="points-val">{{ currentPoints }}</span>
      </div>
      <el-alert title="兑换后可立即在个人中心装配使用" type="info" show-icon :closable="false" />
    </div>

    <el-tabs v-model="activeTab" class="goods-tabs">
      <el-tab-pane label="头像框" name="AVATAR_FRAME">
        <div v-loading="loading" class="goods-grid">
          <div v-for="item in filteredGoods('AVATAR_FRAME')" :key="item.id" class="goods-card">
            <div class="goods-preview">
              <div class="avatar-frame-preview" :style="getFrameStyle(item)">
                <el-avatar :size="80" :src="authStore.student?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
              </div>
              <div v-if="item.isOwned" class="owned-badge">
                <el-icon><Check /></el-icon>
                已拥有
              </div>
            </div>
            <div class="goods-info">
              <h4>{{ item.name }}</h4>
              <p class="description">{{ item.description }}</p>
              <div class="goods-footer">
                <div class="price-tag">
                  <el-icon><Coin /></el-icon>
                  <span>{{ item.price }}</span>
                </div>
                <el-button
                    v-if="item.isOwned"
                    type="success"
                    size="small"
                    @click="handleEquip(item)"
                >
                  立即使用
                </el-button>
                <el-button
                    v-else
                    type="primary"
                    size="small"
                    :disabled="currentPoints < item.price"
                    @click="handleExchange(item)"
                >
                  兑换
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="filteredGoods('AVATAR_FRAME').length === 0" description="暂无头像框商品" :image-size="100" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="背景主题" name="BACKGROUND">
        <div v-loading="loading" class="goods-grid">
          <div v-for="item in filteredGoods('BACKGROUND')" :key="item.id" class="goods-card">
            <div class="goods-preview background-preview">
              <div class="background-demo" :style="getBackgroundStyle(item)">
                <div class="demo-overlay">
                  <el-icon :size="40"><Picture /></el-icon>
                  <span>背景预览</span>
                </div>
              </div>
              <div v-if="item.isOwned" class="owned-badge">
                <el-icon><Check /></el-icon>
                已拥有
              </div>
            </div>
            <div class="goods-info">
              <h4>{{ item.name }}</h4>
              <p class="description">{{ item.description }}</p>
              <div class="goods-footer">
                <div class="price-tag">
                  <el-icon><Coin /></el-icon>
                  <span>{{ item.price }}</span>
                </div>
                <el-button
                    v-if="item.isOwned"
                    type="success"
                    size="small"
                    @click="handleEquip(item)"
                >
                  立即使用
                </el-button>
                <el-button
                    v-else
                    type="primary"
                    size="small"
                    :disabled="currentPoints < item.price"
                    @click="handleExchange(item)"
                >
                  兑换
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="filteredGoods('BACKGROUND').length === 0" description="暂无背景主题" :image-size="100" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="道具卡片" name="BOOST_CARD">
        <div v-loading="loading" class="goods-grid">
          <div v-for="item in filteredGoodsMulti(['BOOST_CARD', 'TOOL_CARD'])" :key="item.id" class="goods-card tool-card">
            <div class="goods-preview tool-preview">
              <div class="tool-icon-wrapper" :class="getToolClass(item)">
                <el-icon :size="60"><StarFilled /></el-icon>
              </div>
              <div v-if="item.isOwned" class="owned-badge">
                <el-icon><Check /></el-icon>
                已拥有
              </div>
            </div>
            <div class="goods-info">
              <h4>{{ item.name }}</h4>
              <p class="description">{{ item.description }}</p>
              <div class="goods-footer">
                <div class="price-tag">
                  <el-icon><Coin /></el-icon>
                  <span>{{ item.price }}</span>
                </div>
                <el-button
                    v-if="item.isOwned"
                    type="info"
                    size="small"
                    disabled
                >
                  已拥有
                </el-button>
                <el-button
                    v-else
                    type="primary"
                    size="small"
                    :disabled="currentPoints < item.price"
                    @click="handleExchange(item)"
                >
                  兑换
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="filteredGoodsMulti(['BOOST_CARD', 'TOOL_CARD']).length === 0" description="暂无道具卡片" :image-size="100" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { fetchGoodsList, exchangeGoods, equipGoods, type Goods } from '@/api/goods';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Coin, Check, Picture, StarFilled } from '@element-plus/icons-vue';

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits(['update:visible', 'success']);

const authStore = useStudentAuthStore();
const loading = ref(false);
const goodsList = ref<Goods[]>([]);
const currentPoints = ref(0);
const activeTab = ref('AVATAR_FRAME');

const loadData = async () => {
  loading.value = true;
  try {
    await authStore.fetchStudentInfo();
    currentPoints.value = authStore.student?.points || 0;

    const res = await fetchGoodsList();
    if (res.code === 200) {
      goodsList.value = res.data;
    }
  } finally {
    loading.value = false;
  }
};

// 根据类型过滤商品
const filteredGoods = (type: string) => {
  return goodsList.value.filter(item => item.type === type);
};

// 根据多个类型过滤商品
const filteredGoodsMulti = (types: string[]) => {
  return goodsList.value.filter(item => types.includes(item.type));
};

const handleExchange = (item: Goods) => {
  ElMessageBox.confirm(
      `确定消耗 ${item.price} 积分兑换 "${item.name}" 吗？`,
      '兑换确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await exchangeGoods(item.id);
      ElMessage.success('兑换成功！');
      loadData();
      emit('success');
    } catch (e) {
      // 错误已由 request.ts 统一处理
    }
  });
};

const handleEquip = async (item: Goods) => {
  try {
    await equipGoods(item.id);
    ElMessage.success(`已应用：${item.name}`);
    await authStore.fetchStudentInfo();

    // 如果是背景主题，刷新页面以应用背景
    if (item.type === 'BACKGROUND') {
      setTimeout(() => {
        window.location.reload();
      }, 500);
    }
  } catch(e) {}
};

// 获取头像框样式
const getFrameStyle = (item: Goods) => {
  if (item.type === 'AVATAR_FRAME') {
    return item.resourceValue; // CSS样式，如 "border: 3px solid gold..."
  }
  return {};
};

// 获取背景样式
const getBackgroundStyle = (item: Goods) => {
  if (item.type === 'BACKGROUND') {
    return {
      background: item.resourceValue
    };
  }
  return {};
};

// 获取道具卡片样式类
const getToolClass = (item: Goods) => {
  if (item.type === 'BOOST_CARD') {
    return 'boost-card';
  } else if (item.type === 'TOOL_CARD') {
    return 'tool-card-type';
  }
  return '';
};

const handleClose = () => emit('update:visible', false);

watch(() => props.visible, (val) => {
  if (val) {
    loadData();
    activeTab.value = 'AVATAR_FRAME';
  }
});
</script>

<style scoped lang="scss">
.mall-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.my-points {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;

  .points-icon {
    font-size: 24px;
  }

  .points-val {
    font-size: 32px;
    font-weight: bold;
    color: #FFD700;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }
}

.goods-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }

  :deep(.el-tabs__item) {
    font-size: 16px;
    font-weight: 500;
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  max-height: 500px;
  overflow-y: auto;
  padding: 4px;
}

.goods-card {
  display: flex;
  flex-direction: column;
  border: 2px solid #eee;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  background: white;
  position: relative;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
    transform: translateY(-4px);
  }
}

.goods-preview {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
  min-height: 120px;
}

.avatar-frame-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 4px;
  box-sizing: border-box;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.background-preview {
  height: 120px;
  width: 100%;
}

.background-demo {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  position: relative;
  overflow: hidden;

  .demo-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: white;
    font-size: 14px;
    gap: 8px;
  }
}

.owned-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #67c23a;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.4);
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;

  h4 {
    margin: 0 0 8px;
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
  }

  .description {
    margin: 0 0 16px;
    color: #606266;
    font-size: 13px;
    line-height: 1.5;
    flex: 1;
  }
}

.goods-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.price-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #E6A23C;
  font-weight: bold;
  font-size: 20px;

  span {
    font-size: 24px;
  }
}

// 响应式
@media (max-width: 768px) {
  .goods-grid {
    grid-template-columns: 1fr;
  }

  .mall-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

// 道具卡片特殊样式
.tool-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.tool-preview {
  min-height: 140px;
}

.tool-icon-wrapper {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;

  &.boost-card {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
  }

  &.tool-card-type {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    color: white;
  }

  &:hover {
    transform: scale(1.1) rotate(10deg);
  }
}
</style>