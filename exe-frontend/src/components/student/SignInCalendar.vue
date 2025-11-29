<template>
  <el-card shadow="hover" class="sign-in-card">
    <div class="sign-header">
      <div class="header-left">
        <span class="title">📅 学习打卡</span>
        <span class="date">{{ currentYearMonth }}</span>
      </div>
      <el-button
          type="primary"
          size="small"
          round
          :disabled="isSignedToday"
          :loading="loading"
          @click="handleSignIn"
      >
        {{ isSignedToday ? '已签到' : '签到' }}
      </el-button>
    </div>

    <div class="calendar-grid">
      <div class="week-row">
        <span v-for="w in weeks" :key="w" class="week-day">{{ w }}</span>
      </div>
      <div class="days-row">
        <div v-for="n in startDayOfWeek" :key="'empty-'+n" class="day-cell empty"></div>
        <div
            v-for="day in daysInMonth"
            :key="day"
            class="day-cell"
            :class="{
            'is-signed': isSigned(day),
            'is-today': isToday(day)
          }"
        >
          <span class="day-text">{{ day }}</span>
          <div v-if="isSigned(day)" class="signed-dot"></div>
        </div>
      </div>
    </div>

    <div class="footer-tip">
      🔥 连签7天赢奖励
    </div>
  </el-card>
</template>

<script setup lang="ts">
// script 部分保持不变...
import { ref, computed, onMounted } from 'vue';
import { doSignIn, getSignInStatus } from '@/api/signIn';
import { ElMessage, ElNotification } from 'element-plus';
import { Check } from '@element-plus/icons-vue';
import { useStudentAuthStore } from '@/stores/studentAuth';

const store = useStudentAuthStore();
const loading = ref(false);
const isSignedToday = ref(false);
const signedDates = ref<string[]>([]);
const weeks = ['日', '一', '二', '三', '四', '五', '六'];

const now = new Date();
const currentYear = now.getFullYear();
const currentMonth = now.getMonth() + 1;

const currentYearMonth = computed(() => `${currentYear}年${currentMonth}月`);

// 计算当月有多少天
const daysInMonth = computed(() => {
  return new Date(currentYear, currentMonth, 0).getDate();
});

// 计算当月1号是星期几 (0-6)
const startDayOfWeek = computed(() => {
  return new Date(currentYear, currentMonth - 1, 1).getDay();
});

// 格式化日期 YYYY-MM-DD
const formatDate = (day: number) => {
  const m = currentMonth < 10 ? '0' + currentMonth : currentMonth;
  const d = day < 10 ? '0' + day : day;
  return `${currentYear}-${m}-${d}`;
};

const isSigned = (day: number) => {
  return signedDates.value.includes(formatDate(day));
};

const isToday = (day: number) => {
  const today = new Date();
  return day === today.getDate() &&
      currentMonth === today.getMonth() + 1 &&
      currentYear === today.getFullYear();
};

const loadStatus = async () => {
  const m = currentMonth < 10 ? '0' + currentMonth : currentMonth;
  const res = await getSignInStatus(`${currentYear}-${m}`);
  if (res.code === 200) {
    signedDates.value = res.data.signedDates;
    isSignedToday.value = res.data.todaySigned;
  }
};

const handleSignIn = async () => {
  loading.value = true;
  try {
    const res = await doSignIn();
    if (res.code === 200) {
      ElNotification({
        title: '签到成功',
        message: res.data.message,
        type: 'success',
      });
      // 刷新数据
      await loadStatus();
      // 刷新用户信息以更新积分
      store.fetchStudentInfo();
    } else {
      ElMessage.warning(res.msg);
    }
  } catch(e) {
    // error handled
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadStatus();
});
</script>

<style scoped>
/* 紧凑版样式 */
.sign-in-card {
  /* 移除固定高度，让其更紧凑 */
  border: none;
  background: transparent; /* 如果父容器已有背景，可透明；或者保持 #fff */
}
/* 针对 el-card body 的内边距进行缩减 */
.sign-in-card :deep(.el-card__body) {
  padding: 12px;
}

.sign-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px; /* 减小间距 */
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}
.header-left {
  display: flex;
  align-items: baseline; /* 基线对齐 */
  gap: 6px;
}
.title {
  font-weight: 600;
  font-size: 14px; /* 字体调小 */
  color: #303133;
}
.date {
  font-size: 12px;
  color: #909399;
}

.calendar-grid {
  margin-top: 4px;
}
.week-row, .days-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  gap: 2px; /* 减小单元格间距 */
}
.week-day {
  font-size: 12px; /* 极小字体 */
  color: #C0C4CC;
  margin-bottom: 2px;
  transform: scale(0.9); /* 视觉上更小 */
}
.day-cell {
  height: 24px; /* 高度从32px减小到24px */
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px; /* 稍微圆角 */
  font-size: 12px;
  position: relative;
  cursor: default;
  color: #606266;
}
/* 今天的样式 */
.day-cell.is-today {
  color: #409eff;
  font-weight: bold;
  background-color: #ecf5ff;
}
/* 已签到的样式 */
.day-cell.is-signed {
  background-color: #f0f9eb;
  color: #67c23a;
  font-weight: bold;
}
/* 签到小绿点 (替代大对勾图标，更简洁) */
.signed-dot {
  position: absolute;
  bottom: 2px;
  width: 4px;
  height: 4px;
  background-color: #67c23a;
  border-radius: 50%;
}

.footer-tip {
  margin-top: 8px;
  text-align: center;
  font-size: 12px;
  color: #E6A23C;
  background: #fdf6ec;
  padding: 4px;
  border-radius: 4px;
  transform: scale(0.95); /* 整体微缩 */
}
</style>