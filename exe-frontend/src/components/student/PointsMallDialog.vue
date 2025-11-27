<template>
  <el-dialog
      :model-value="visible"
      title="💎 积分商城"
      width="700px"
      @close="handleClose"
      append-to-body
  >
    <div class="mall-header">
      <div class="my-points">
        当前积分：<span class="points-val">{{ currentPoints }}</span>
      </div>
      <el-alert title="兑换后可立即在个人中心装配" type="info" show-icon :closable="false" />
    </div>

    <div v-loading="loading" class="goods-grid">
      <div v-for="item in goodsList" :key="item.id" class="goods-card" :class="{ owned: item.isOwned }">
        <div class="goods-icon">
          <div class="avatar-preview" :style="getStyle(item)">
            <el-avatar :size="50" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          </div>
        </div>
        <div class="goods-info">
          <h4>{{ item.name }}</h4>
          <p>{{ item.description }}</p>
          <div class="price-tag">
            <el-icon><Coin /></el-icon> {{ item.price }}
          </div>
        </div>
        <div class="goods-action">
          <div v-if="item.isOwned">
            <el-button type="success" size="small" @click="handleEquip(item)">立即使用</el-button>
          </div>

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
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { fetchGoodsList, exchangeGoods, equipGoods, type Goods } from '@/api/goods'; // 导入 equipGoods
import { useStudentAuthStore } from '@/stores/studentAuth';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Coin } from '@element-plus/icons-vue';

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits(['update:visible', 'success']);

const authStore = useStudentAuthStore();
const loading = ref(false);
const goodsList = ref<Goods[]>([]);
const currentPoints = ref(0);

const loadData = async () => {
  loading.value = true;
  try {
    // 并行刷新用户信息(获取最新积分)和商品列表
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

const handleExchange = (item: Goods) => {
  ElMessageBox.confirm(
      `确定消耗 ${item.price} 积分兑换 "${item.name}" 吗？`,
      '兑换确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await exchangeGoods(item.id);
      ElMessage.success('兑换成功！');
      loadData(); // 刷新列表和积分
      emit('success');
    } catch (e) {
      // 错误已由 request.ts 统一处理
    }
  });
};
// 【新增】处理装配
const handleEquip = async (item: Goods) => {
  try {
    await equipGoods(item.id);
    ElMessage.success(`已应用：${item.name}`);
    // 刷新用户信息，让背景和头像框立即生效
    await authStore.fetchStudentInfo();
    // 可选：关闭弹窗
    // handleClose();
  } catch(e) {}
};

// 解析样式用于预览
const getStyle = (item: Goods) => {
  if (item.type === 'AVATAR_FRAME') {
    return item.resourceValue; // 数据库存的是CSS样式，如 "border: 3px solid gold..."
  }
  return {};
};

const handleClose = () => emit('update:visible', false);

watch(() => props.visible, (val) => {
  if (val) loadData();
});
</script>

<style scoped>
.mall-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.points-val {
  font-size: 24px;
  font-weight: bold;
  color: #E6A23C;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  max-height: 400px;
  overflow-y: auto;
}
.goods-card {
  display: flex;
  align-items: center;
  border: 1px solid #eee;
  padding: 15px;
  border-radius: 8px;
  transition: all 0.3s;
}
.goods-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.goods-card.owned {
  background-color: #f5f7fa;
  opacity: 0.8;
}
.goods-icon {
  width: 60px;
  height: 60px;
  margin-right: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.avatar-preview {
  width: 54px;
  height: 54px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box; /* 确保border算在大小内 */
}
.goods-info {
  flex-grow: 1;
}
.goods-info h4 { margin: 0 0 5px; font-size: 16px; }
.goods-info p { margin: 0 0 5px; color: #999; font-size: 12px; }
.price-tag { color: #E6A23C; font-weight: bold; display: flex; align-items: center; gap: 4px;}
</style>