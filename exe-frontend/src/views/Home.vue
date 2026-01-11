<template>
  <div class="dashboard-container">
    <!-- 顶部操作栏 -->
    <div class="dashboard-header">
      <div class="welcome-section">
        <h2 class="welcome-title">欢迎回来，{{ userName }}</h2>
        <p class="welcome-subtitle">今天是 {{ currentDate }}，{{ greetingMessage }}</p>
      </div>
      <div class="action-buttons">
        <!-- 布局编辑按钮 -->
        <el-tooltip :content="isEditMode ? '退出编辑' : '编辑布局'" placement="bottom">
          <el-button
            circle
            :icon="Setting"
            :type="isEditMode ? 'primary' : 'default'"
            @click="isEditMode = !isEditMode"
          />
        </el-tooltip>

        <!-- 布局设置按钮 -->
        <el-tooltip content="布局设置" placement="bottom">
          <el-button
            circle
            :icon="Operation"
            @click="showLayoutSettings = true"
          />
        </el-tooltip>

        <!-- 保存布局按钮（编辑模式时显示） -->
        <el-button
          v-if="isEditMode"
          type="success"
          :icon="Check"
          @click="handleSaveLayout"
          :loading="layoutLoading"
          size="small"
        >
          保存布局
        </el-button>

        <!-- 刷新数据按钮 -->
        <el-tooltip content="刷新数据" placement="bottom">
          <el-button
            circle
            :icon="RefreshRight"
            @click="handleRefresh"
            :loading="refreshing"
            :disabled="loading"
          />
        </el-tooltip>

        <!-- 数据大屏按钮 -->
        <el-button type="primary" :icon="DataLine" @click="$router.push('/data-screen')">
          数据大屏
        </el-button>
      </div>
    </div>

    <!-- 可拖拽的模块容器 -->
    <VueDraggable
      v-model="sortedModules"
      :disabled="!isEditMode"
      @end="handleDragEnd"
      item-key="id"
      class="draggable-container"
      handle=".drag-handle"
      animation="300"
      ghost-class="ghost-module"
    >
      <template #item="{ element: module }">
        <div class="draggable-item" :key="module.id">
          <!-- 快捷操作模块 -->
          <el-row v-if="module.id === 'quick-actions' && !loading" :gutter="20" class="module-row">
            <el-col :span="module.width || 24" class="module-col">
        <div class="module-wrapper" :class="{ 'edit-mode': isEditMode }">
          <div v-if="isEditMode" class="module-toolbar">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <span class="module-title">快捷操作</span>
            <el-select
              v-model="currentConfig.modules.find(m => m.id === 'quick-actions')!.width"
              size="small"
              style="width: 110px"
            >
              <el-option label="1/4" :value="6" />
              <el-option label="1/3" :value="8" />
              <el-option label="1/2" :value="12" />
              <el-option label="2/3" :value="16" />
              <el-option label="全宽" :value="24" />
            </el-select>
          </div>
          <el-card shadow="never" class="quick-actions-card">
      <div class="quick-actions">
        <div
          class="action-item"
          v-for="action in quickActions"
          :key="action.id"
          @click="handleQuickAction(action)"
        >
          <div class="action-icon" :style="{ background: action.gradient }">
            <el-icon :size="24">
              <component :is="action.icon" />
            </el-icon>
          </div>
          <div class="action-info">
            <div class="action-title">{{ action.title }}</div>
            <div class="action-desc">{{ action.desc }}</div>
          </div>
        </div>
      </div>
    </el-card>
        </div>
      </el-col>
    </el-row>

          <!-- 统计卡片模块 -->
          <el-row v-if="module.id === 'stat-cards' && !loading" :gutter="20" class="module-row">
            <el-col :span="module.width || 24" class="module-col">
        <div class="module-wrapper" :class="{ 'edit-mode': isEditMode }">
          <!-- 编辑模式工具栏 -->
          <div v-if="isEditMode" class="module-toolbar">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <span class="module-title">统计卡片</span>
            <el-select
              v-model="currentConfig.modules.find(m => m.id === 'stat-cards')!.width"
              size="small"
              style="width: 110px"
            >
              <el-option label="1/4" :value="6" />
              <el-option label="1/3" :value="8" />
              <el-option label="1/2" :value="12" />
              <el-option label="2/3" :value="16" />
              <el-option label="全宽" :value="24" />
            </el-select>
          </div>

          <!-- 统计卡片内容 -->
          <StatCardsSkeleton v-if="loading" />
          <el-row v-else :gutter="20" class="stat-row">
            <el-col :xs="12" :sm="8" :md="24/6" class="stat-card-item" v-for="(card, index) in statCards" :key="index">
              <el-card shadow="hover" :class="['stat-card-enhanced', card.class]">
                <div class="stat-content">
                  <div class="stat-icon-wrapper" :style="{ background: card.gradient }">
                    <el-icon :size="28">
                      <component :is="card.icon" />
                    </el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">{{ card.label }}</div>
                    <div class="stat-value">
                      <transition name="number-slide" mode="out-in">
                        <span :key="card.value">{{ card.value }}</span>
                      </transition>
                    </div>
                    <div class="stat-trend" v-if="card.trend !== null && card.trend !== undefined">
                      <el-icon :color="card.trend > 0 ? '#67C23A' : card.trend < 0 ? '#F56C6C' : '#909399'" :size="12">
                        <component :is="card.trend > 0 ? ArrowUp : card.trend < 0 ? ArrowDown : Minus" />
                      </el-icon>
                      <span :style="{ color: card.trend > 0 ? '#67C23A' : card.trend < 0 ? '#F56C6C' : '#909399' }">
                        {{ card.trend === 0 ? '持平' : Math.abs(card.trend) + '%' }}
                      </span>
                      <span class="trend-label">较上周</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>

          <!-- 待办事项模块 -->
          <el-row v-if="module.id === 'todo-list' && !loading" :gutter="20" class="main-content-row">
            <el-col :xs="24" :sm="24" :md="module.width || 24">
        <div class="module-wrapper" :class="{ 'edit-mode': isEditMode }">
          <!-- 编辑模式工具栏 -->
          <div v-if="isEditMode" class="module-toolbar">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <span class="module-title">待办事项 & 活动</span>
            <el-select
              v-model="currentConfig.modules.find(m => m.id === 'todo-list')!.width"
              size="small"
              style="width: 110px"
            >
              <el-option label="1/4" :value="6" />
              <el-option label="1/3" :value="8" />
              <el-option label="1/2" :value="12" />
              <el-option label="2/3" :value="16" />
              <el-option label="全宽" :value="24" />
            </el-select>
          </div>

          <!-- 三个模块横向排列 -->
          <el-row :gutter="20">
            <!-- 待办事项 -->
            <el-col :xs="24" :sm="24" :md="8">
              <SkeletonCard v-if="loading || todoLoading" :rows="4" show-header custom-class="todo-card" />
              <el-card v-else shadow="never" class="section-card todo-card">
                <template #header>
                  <div class="card-header-enhanced">
                    <div class="header-title">
                      <el-icon><Checked /></el-icon>
                      <span>待办事项</span>
                    </div>
                    <el-badge :value="todoCount" :hidden="todoCount === 0" type="danger" />
                  </div>
                </template>
                <div class="todo-list">
                  <div v-if="todoItems.length === 0" class="empty-state">
                    <el-icon :size="48" color="#C0C4CC"><Select /></el-icon>
                    <p>暂无待办事项</p>
                  </div>
                  <div v-else class="todo-item" v-for="item in todoItems" :key="item.id">
                    <div class="todo-icon" :style="{ background: item.color }">
                      <el-icon><component :is="item.icon" /></el-icon>
                    </div>
                    <div class="todo-content">
                      <div class="todo-title">{{ item.title }}</div>
                      <div class="todo-meta">{{ item.count }} 项 · {{ item.time }}</div>
                    </div>
                    <el-button type="primary" link @click="handleTodoClick(item)">
                      处理
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>

            <!-- 系统通知 -->
            <el-col :xs="24" :sm="24" :md="8">
              <SkeletonCard v-if="loading" :rows="4" show-header custom-class="notification-card" />
              <el-card v-else shadow="never" class="section-card notification-card">
                <template #header>
                  <div class="card-header-enhanced">
                    <div class="header-title">
                      <el-icon><Bell /></el-icon>
                      <span>系统通知</span>
                    </div>
                    <el-button type="primary" link @click="$router.push('/notifications')">
                      查看全部
                    </el-button>
                  </div>
                </template>
                <div class="notification-list">
                  <el-empty v-if="!stats.notifications || stats.notifications.length === 0"
                           description="暂无通知" :image-size="60" />
                  <div v-else class="notification-item" v-for="item in stats.notifications" :key="item.id"
                       @click="handleNotificationClick(item.id)">
                    <div class="notification-dot"></div>
                    <div class="notification-content">
                      <div class="notification-text">{{ item.content }}</div>
                      <div class="notification-date">{{ item.date }}</div>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>

            <!-- 最近活动 -->
            <el-col :xs="24" :sm="24" :md="8">
              <SkeletonCard v-if="loading" :rows="5" show-header custom-class="activity-card" />
              <el-card v-else shadow="never" class="section-card activity-card">
                <template #header>
                  <div class="card-header-enhanced">
                    <div class="header-title">
                      <el-icon><Clock /></el-icon>
                      <span>最近活动</span>
                    </div>
                    <el-button type="primary" link @click="showMoreActivities">查看更多</el-button>
                  </div>
                </template>
                <el-timeline class="activity-timeline">
                  <el-timeline-item
                    v-for="(activity, index) in recentActivities"
                    :key="index"
                    :timestamp="activity.time"
                    :color="activity.color"
                    placement="top"
                  >
                    <div class="activity-content">
                      <el-icon :color="activity.color"><component :is="activity.icon" /></el-icon>
                      <span>{{ activity.content }}</span>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>

          <!-- 图表模块 -->
          <el-row v-if="module.id === 'charts' && !loading" :gutter="20" class="main-content-row">
            <el-col :xs="24" :sm="24" :md="module.width || 16">
        <div class="module-wrapper" :class="{ 'edit-mode': isEditMode }">
          <!-- 编辑模式工具栏 -->
          <div v-if="isEditMode" class="module-toolbar">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <span class="module-title">数据图表</span>
            <el-select
              v-model="currentConfig.modules.find(m => m.id === 'charts')!.width"
              size="small"
              style="width: 110px"
            >
              <el-option label="1/4" :value="6" />
              <el-option label="1/3" :value="8" />
              <el-option label="1/2" :value="12" />
              <el-option label="2/3" :value="16" />
              <el-option label="全宽" :value="24" />
            </el-select>
          </div>

          <!-- 知识点&题目统计 -->
          <SkeletonCard v-if="loading" :rows="8" show-header custom-class="chart-card" />
          <el-card v-else shadow="never" class="section-card chart-card">
            <template #header>
              <div class="card-header-enhanced">
                <div class="header-title">
                  <el-icon><DataAnalysis /></el-icon>
                  <span>知识点&题目统计</span>
                </div>
                <div class="header-controls">
                  <el-select
                    v-model="selectedSubjectsForKpChart"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    placeholder="筛选科目"
                    style="width: 200px; margin-right: 12px;"
                    size="small"
                    @change="updateKpAndQuestionChart"
                    clearable
                  >
                    <el-option
                      v-for="sub in availableSubjects"
                      :key="sub"
                      :label="sub"
                      :value="sub"
                    />
                  </el-select>
                  <el-date-picker
                    v-model="selectedMonth"
                    type="month"
                    placeholder="选择月份"
                    format="YYYY.MM"
                    :clearable="false"
                    @change="handleMonthChange"
                    size="small"
                    style="width: 140px;"
                  />
                </div>
              </div>
            </template>
            <div class="chart-summary">
              <div class="summary-item">
                <div class="summary-label">知识点总数</div>
                <div class="summary-value">{{ stats.knowledgePointCount }}</div>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-item">
                <div class="summary-label">题目总数</div>
                <div class="summary-value">{{ stats.questionCount }}</div>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-item">
                <div class="summary-label">平均题目数</div>
                <div class="summary-value">{{ averageQuestions }}</div>
              </div>
            </div>
            <div ref="kpAndQuestionChart" class="chart-container" v-loading="chartLoading"></div>
          </el-card>

          <!-- 错题统计和科目统计 -->
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :xs="24" :sm="12">
              <SkeletonCard v-if="loading" :rows="5" show-header custom-class="chart-card-small" />
              <el-card v-else shadow="never" class="section-card chart-card-small">
                <template #header>
                  <div class="card-header-enhanced">
                    <div class="header-title">
                      <el-icon><Warning /></el-icon>
                      <span>错题分布</span>
                    </div>
                  </div>
                </template>
                <div ref="wrongStatsChart" class="chart-container-small" v-loading="chartLoading"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12">
              <SkeletonCard v-if="loading" :rows="5" show-header custom-class="chart-card-small" />
              <el-card v-else shadow="never" class="section-card chart-card-small">
                <template #header>
                  <div class="card-header-enhanced">
                    <div class="header-title">
                      <el-icon><PieChart /></el-icon>
                      <span>科目学生分布</span>
                    </div>
                  </div>
                </template>
                <div ref="subjectStatsChart" class="chart-container-small" v-loading="chartLoading"></div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 【知识点功能增强】知识点统计 -->
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :xs="24" :sm="12">
              <SkeletonCard v-if="loading" :rows="5" show-header custom-class="chart-card-small" />
              <el-card v-else shadow="never" class="section-card chart-card-small">
                <template #header>
                  <div class="card-header-enhanced">
                    <div class="header-title">
                      <el-icon><CollectionTag /></el-icon>
                      <span>知识点覆盖率</span>
                    </div>
                    <div class="header-controls" style="margin-left: auto;">
                      <el-select
                        v-model="selectedSubjectsForCoverage"
                        multiple
                        collapse-tags
                        collapse-tags-tooltip
                        placeholder="筛选科目"
                        style="width: 140px;"
                        size="small"
                        @change="updateKpCoverageChart"
                        clearable
                      >
                        <el-option
                          v-for="sub in availableSubjectsForCoverage"
                          :key="sub"
                          :label="sub"
                          :value="sub"
                        />
                      </el-select>
                    </div>
                  </div>
                </template>
                <div ref="kpCoverageChart" class="chart-container-small" v-loading="chartLoading"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12">
              <SkeletonCard v-if="loading" :rows="5" show-header custom-class="chart-card-small" />
              <el-card v-else shadow="never" class="section-card chart-card-small">
                <template #header>
                  <div class="card-header-enhanced">
                    <div class="header-title">
                      <el-icon><Warning /></el-icon>
                      <span>薄弱知识点 Top10</span>
                    </div>
                  </div>
                </template>
                <div ref="weakKpChart" class="chart-container-small" v-loading="chartLoading"></div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>
        </div>
      </template>
    </VueDraggable>

    <!-- 通知预览对话框 -->
    <notification-preview-dialog
      v-model:visible="isPreviewVisible"
      :notification="selectedNotification"
    />

    <!-- 布局设置对话框 -->
    <LayoutSettingsDialog
      v-model:visible="showLayoutSettings"
      :modules="currentConfig.modules"
      :loading="layoutLoading"
      @save="handleSettingsSave"
      @reset="handleSettingsReset"
      @apply-preset="handleApplyPreset"
    />

    <!-- 更多活动对话框 -->
    <el-dialog
      v-model="showActivitiesDialog"
      title="最近活动"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-loading="activitiesDialogLoading" style="min-height: 300px;">
        <el-empty v-if="allActivities.length === 0" description="暂无活动记录" :image-size="100" />
        <el-timeline v-else class="activity-timeline-dialog">
          <el-timeline-item
            v-for="(activity, index) in allActivities"
            :key="index"
            :timestamp="activity.time"
            :color="activity.color"
            placement="top"
          >
            <div class="activity-content">
              <el-icon :color="activity.color"><component :is="activity.icon" /></el-icon>
              <span>{{ activity.content }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button @click="showActivitiesDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import { getDashboardStats, getTodoList, getRecentActivities, getKnowledgePointCoverage, getWeakKnowledgePoints, type DashboardStats, type ChartData, type TodoItem, type RecentActivity, type KnowledgePointCoverage } from '@/api/dashboard';
import NotificationPreviewDialog from "@/components/notification/NotificationPreviewDialog.vue";
import { fetchNotificationById, type Notification } from '@/api/notification';
import { gsap } from "gsap";
import {
  DataLine, Warning, ArrowUp, ArrowDown, User, Document,
  Files, Edit, Checked, Bell, DataAnalysis, Clock, PieChart, Select,
  UserFilled, Notebook, FolderOpened, DocumentCopy, CollectionTag,
  RefreshRight, Plus, Upload, Notification as NotificationIcon, Minus,
  Reading, Setting, Rank, Check, Operation
} from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationSocketStore } from '@/stores/notificationSocket';
import { fetchOnlineStudentCount } from '@/api/dashboard';
import { useRouter } from 'vue-router';
import SkeletonCard from '@/components/common/SkeletonCard.vue';
import StatCardsSkeleton from '@/components/common/StatCardsSkeleton.vue';
import { useDarkMode } from '@/composables/useDarkMode';
import { useLayoutConfig, type DashboardModule } from '@/composables/useLayoutConfig';
import LayoutSettingsDialog from '@/components/dashboard/LayoutSettingsDialog.vue';
import VueDraggable from 'vuedraggable';

const router = useRouter();
const authStore = useAuthStore();
const socketStore = useNotificationSocketStore();
const { isDark } = useDarkMode();

// 布局配置
const {
  currentConfig,
  isEditMode,
  loading: layoutLoading,
  enabledModules,
  loadLayout,
  saveLayout,
  resetLayout,
  applyPreset,
  updateModuleWidth,
  updateModuleOrder
} = useLayoutConfig();

const showLayoutSettings = ref(false);

// 更多活动对话框
const showActivitiesDialog = ref(false);
const activitiesDialogLoading = ref(false);
const allActivities = ref<RecentActivity[]>([]);

// 响应式数据
const loading = ref(true);
const refreshing = ref(false);
const chartLoading = ref(true);
const todoLoading = ref(false); // 待办事项加载状态
const onlineCount = computed(() => socketStore.onlineStudentCount);
const userName = computed(() => authStore.user?.name || '管理员');

// 图表ref
const kpAndQuestionChart = ref<HTMLElement | null>(null);
const subjectStatsChart = ref<HTMLElement | null>(null);
const wrongStatsChart = ref<HTMLElement | null>(null);
const kpCoverageChart = ref<HTMLElement | null>(null);  // 【知识点功能增强】知识点覆盖率图表
const weakKpChart = ref<HTMLElement | null>(null);      // 【知识点功能增强】薄弱知识点图表

let kpChartInstance: echarts.ECharts | null = null;
let subjectStatsChartInstance: echarts.ECharts | null = null;
let wrongStatsChartInstance: echarts.ECharts | null = null;
let kpCoverageChartInstance: echarts.ECharts | null = null;  // 【知识点功能增强】
let weakKpChartInstance: echarts.ECharts | null = null;      // 【知识点功能增强】

let resizeObserver: ResizeObserver | null = null;
let onlineCountTimer: any = null;

const isPreviewVisible = ref(false);
const selectedNotification = ref<Notification | undefined>(undefined);
const stats = ref<DashboardStats>({
  studentCount: 0, subjectCount: 0, knowledgePointCount: 0,
  questionCount: 0, paperCount: 0, notifications: [],
  kpAndQuestionStats: { categories: [], series: [] },
  wrongQuestionStats: { categories: [], series: [] },
  monthlyQuestionCreationStats: { categories: [], series: [] },
  subjectStatsByGrade: { categories: [], series: [] }
});
const selectedMonth = ref(new Date());

// 图表筛选状态
const selectedSubjectsForKpChart = ref<string[]>([]);
const availableSubjects = computed(() => stats.value.kpAndQuestionStats?.categories || []);

const selectedSubjectsForCoverage = ref<string[]>([]);
const kpCoverageRawData = ref<KnowledgePointCoverage[]>([]);
const availableSubjectsForCoverage = computed(() => kpCoverageRawData.value.map(item => item.subject_name));

// 快捷操作
const quickActions = ref([
  {
    id: 1,
    title: '创建试卷',
    desc: '快速创建新试卷',
    icon: DocumentCopy,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    route: '/papers'
  },
  {
    id: 2,
    title: '导入学生',
    desc: '批量导入学生信息',
    icon: Upload,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    route: '/students'
  },
  {
    id: 3,
    title: '发布通知',
    desc: '发布系统通知',
    icon: NotificationIcon,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    route: '/notifications'
  },
  {
    id: 4,
    title: '添加题目',
    desc: '创建新的试题',
    icon: Plus,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    route: '/questions'
  }
]);

// 图表主题配色（根据深色模式动态切换）
const getChartTheme = () => {
  if (isDark.value) {
    return {
      backgroundColor: '#2d2d2d',
      textColor: '#e5e5e5',
      axisLineColor: '#404040',
      splitLineColor: '#404040',
      tooltipBg: 'rgba(45, 45, 45, 0.9)',
      tooltipBorder: '#404040',
      labelColor: '#c0c0c0',
      borderColor: '#2d2d2d'
    };
  }
  return {
    backgroundColor: '#ffffff',
    textColor: '#303133',
    axisLineColor: '#E4E7ED',
    splitLineColor: '#E4E7ED',
    tooltipBg: 'rgba(50, 50, 50, 0.9)',
    tooltipBorder: '#333',
    labelColor: '#606266',
    borderColor: '#fff'
  };
};

// 计算属性
const currentDate = computed(() => {
  const now = new Date();
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekdays[now.getDay()]}`;
});

const greetingMessage = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '夜深了，注意休息';
  if (hour < 9) return '早上好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  if (hour < 22) return '晚上好';
  return '夜深了，注意休息';
});

const averageQuestions = computed(() => {
  if (stats.value.subjectCount === 0) return 0;
  return Math.round(stats.value.questionCount / stats.value.subjectCount);
});

// 统计卡片数据
const statCards = computed(() => [
  {
    label: '在线学生',
    value: onlineCount.value,
    icon: UserFilled,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    class: 'online-card',
    trend: 12.5
  },
  {
    label: '学生总数',
    value: stats.value.studentCount,
    icon: User,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    trend: 8.3
  },
  {
    label: '科目数量',
    value: stats.value.subjectCount,
    icon: Notebook,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    trend: 0
  },
  {
    label: '知识点',
    value: stats.value.knowledgePointCount,
    icon: CollectionTag,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    trend: 15.7
  },
  {
    label: '题目数量',
    value: stats.value.questionCount,
    icon: DocumentCopy,
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    trend: 23.1
  },
  {
    label: '试卷数量',
    value: stats.value.paperCount,
    icon: FolderOpened,
    gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
    trend: 5.2
  }
]);

// 待办事项数据 - 【修改】从 API 加载
const todoItems = ref<TodoItem[]>([]);

const todoCount = computed(() => todoItems.value.reduce((sum: number, item: TodoItem) => sum + item.count, 0));

// 最近活动数据 - 【修改】从 API 加载
const recentActivities = ref<RecentActivity[]>([]);

// 方法
const handleRefresh = async () => {
  refreshing.value = true;
  try {
    await fetchData();
    await updateOnlineCount();
    await loadTodoList();  // 【新增】刷新待办事项
    await loadRecentActivities();  // 【新增】刷新最近活动
    ElMessage.success('数据刷新成功');
  } catch (error) {
    ElMessage.error('数据刷新失败');
  } finally {
    refreshing.value = false;
  }
};

const handleQuickAction = (action: any) => {
  router.push(action.route);
};

const handleMonthChange = (newVal: Date | null) => {
  if (!newVal) return;
  const year = newVal.getFullYear();
  const month = newVal.getMonth() + 1;
  ElMessage.info(`已切换到 ${year}年${month}月`);
  fetchData();
};

const handleTodoClick = (item: any) => {
  ElMessage.info(`正在跳转到${item.title}...`);
  router.push(`/${item.action}`);
};

// 布局相关方法
/**
 * 检查模块是否启用
 */
const isModuleEnabled = (moduleId: string) => {
  const module = currentConfig.value.modules.find(m => m.id === moduleId);
  return module?.enabled ?? true;
};

/**
 * 获取模块宽度
 */
const getModuleWidth = (moduleId: string) => {
  const module = currentConfig.value.modules.find(m => m.id === moduleId);
  return module?.width ?? 24;
};

/**
 * 保存布局
 */
const handleSaveLayout = async () => {
  const success = await saveLayout();
  if (success) {
    isEditMode.value = false;
  }
};

/**
 * 处理设置对话框的保存
 */
const handleSettingsSave = async (modules: DashboardModule[], compactMode: boolean) => {
  currentConfig.value.modules = modules;
  currentConfig.value.compactMode = compactMode;
  const success = await saveLayout();
  if (success) {
    showLayoutSettings.value = false;
  }
};

/**
 * 重置布局
 */
const handleSettingsReset = async () => {
  await resetLayout();
  showLayoutSettings.value = false;
};

/**
 * 应用预设模板
 */
const handleApplyPreset = (presetName: string) => {
  applyPreset(presetName as any);
};

/**
 * 可排序的模块列表（用于拖拽）
 */
const sortedModules = computed({
  get: () => enabledModules.value,
  set: (value) => {
    updateModuleOrder(value);
  }
});

/**
 * 处理模块拖拽结束
 */
const handleDragEnd = () => {
  // 拖拽结束后更新模块顺序
  updateModuleOrder(sortedModules.value);
  ElMessage.success('布局已更新，请点击"保存布局"以保存更改');
};

/**
 * 更新知识点&题目统计图表（根据筛选）
 */
const updateKpAndQuestionChart = () => {
  const fullCategories = stats.value.kpAndQuestionStats?.categories;
  const fullSeries = stats.value.kpAndQuestionStats?.series;

  // 如果没有数据，直接返回
  if (!fullCategories || fullCategories.length === 0) return;

  // 确定的目标科目列表
  const targetSubjects = selectedSubjectsForKpChart.value.length > 0
    ? selectedSubjectsForKpChart.value
    : fullCategories; // 默认全部

  // 找到对应的索引，以保持原始顺序
  const indices = fullCategories
    .map((cat, idx) => targetSubjects.includes(cat) ? idx : -1)
    .filter(idx => idx !== -1);

  const filteredCategories = indices.map(i => fullCategories[i]);
  const filteredSeries = fullSeries.map(s => ({
    ...s,
    data: indices.map(i => s.data[i])
  }));

  initKpAndQuestionChart({ categories: filteredCategories, series: filteredSeries });
};

/**
 * 更新知识点覆盖率图表（根据筛选）
 */
const updateKpCoverageChart = () => {
  if (!kpCoverageRawData.value || kpCoverageChart.value === null) return;

  const theme = getChartTheme();
  if (!kpCoverageChartInstance) {
    kpCoverageChartInstance = echarts.init(kpCoverageChart.value);
  }

  // 筛选数据
  let filteredData = kpCoverageRawData.value;
  if (selectedSubjectsForCoverage.value.length > 0) {
    filteredData = filteredData.filter(item => selectedSubjectsForCoverage.value.includes(item.subject_name));
  }

  const chartData = filteredData.map((item: any) => ({
    value: item.coverage_rate || 0,
    name: item.subject_name
  }));

  const colors = ['#667eea', '#43e97b', '#4facfe', '#f093fb', '#fa709a'];
  kpCoverageChartInstance.setOption({
    backgroundColor: theme.backgroundColor,
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}%'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { fontSize: 12, color: theme.textColor }
    },
    series: [{
      name: '覆盖率',
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: theme.borderColor,
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{c}%',
        fontSize: 11,
        color: theme.labelColor
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: chartData.map((item: any, index: number) => ({
        ...item,
        itemStyle: { color: colors[index % colors.length] }
      }))
    }]
  });
};

/**
 * 显示更多活动
 */
const showMoreActivities = async () => {
  showActivitiesDialog.value = true;
  activitiesDialogLoading.value = true;

  try {
    // 加载更多活动，这里获取50条
    const res = await getRecentActivities(50);
    if (res.code === 200) {
      allActivities.value = res.data.activities || [];
    } else {
      ElMessage.warning(res.message || '获取活动记录失败');
    }
  } catch (error: any) {
    console.error('获取活动记录失败:', error);
    ElMessage.error(error.message || '获取活动记录失败');
  } finally {
    activitiesDialogLoading.value = false;
  }
};

// 初始化图表
const initKpAndQuestionChart = (chartData: ChartData) => {
  if (kpAndQuestionChart.value && chartData?.series) {
    const theme = getChartTheme();
    // 重用图表实例，避免重复初始化
    if (!kpChartInstance) {
      kpChartInstance = echarts.init(kpAndQuestionChart.value);
    }

    kpChartInstance.setOption({
      backgroundColor: theme.backgroundColor,
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: theme.tooltipBg,
        borderColor: theme.tooltipBorder,
        textStyle: { color: isDark.value ? '#fff' : '#fff' }
      },
      legend: {
        data: chartData.series.map(s => s.name),
        right: '4%',
        top: 0,
        icon: 'roundRect',
        itemWidth: 12,
        itemHeight: 4,
        textStyle: { fontSize: 12, color: theme.textColor }
      },
      grid: { top: '18%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: chartData.categories,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: theme.axisLineColor } },
        axisLabel: {
          color: theme.labelColor,
          interval: 0,
          fontSize: 12,
          rotate: chartData.categories.length > 6 ? 30 : 0  // 科目超过6个时旋转标签
        }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: theme.splitLineColor } },
        axisLabel: { color: theme.labelColor }
      },
      series: chartData.series.map((s: any, index: number) => ({
        name: s.name,
        type: 'bar',
        barWidth: '30%',
        data: s.data,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: index === 0 ? '#667eea' : '#43e97b' },
            { offset: 1, color: index === 0 ? '#764ba2' : '#38f9d7' }
          ])
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        animationDelay: (idx: number) => idx * 50
      }))
    }, true);  // 使用 true 覆盖之前的配置，防止残留数据
  }
};

const initWrongStatsChart = (chartData: ChartData) => {
  if (wrongStatsChart.value && chartData?.series?.[0]?.data) {
    const theme = getChartTheme();
    wrongStatsChartInstance = echarts.init(wrongStatsChart.value);
    const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a'];
    wrongStatsChartInstance.setOption({
      backgroundColor: theme.backgroundColor,
      tooltip: { trigger: 'item' },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'center',
        icon: 'circle',
        itemWidth: 8,
        itemHeight: 8,
        textStyle: { fontSize: 12, color: theme.textColor }
      },
      series: [{
        name: '错题分布',
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: theme.borderColor,
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        data: chartData.categories.map((name, index) => ({
          value: chartData.series[0].data[index],
          name: name,
          itemStyle: { color: colors[index % colors.length] }
        })),
        animationType: 'scale',
        animationEasing: 'elasticOut'
      }]
    });
  }
};

const initSubjectStatsChart = (chartData: ChartData) => {
  if (subjectStatsChart.value && chartData?.series?.[0]?.data) {
    const theme = getChartTheme();
    subjectStatsChartInstance = echarts.init(subjectStatsChart.value);
    subjectStatsChartInstance.setOption({
      backgroundColor: theme.backgroundColor,
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: theme.tooltipBg,
        borderColor: theme.tooltipBorder,
        textStyle: { color: isDark.value ? '#fff' : '#fff' }
      },
      grid: { top: 20, left: 10, right: 10, bottom: 5, containLabel: true },
      xAxis: {
        type: 'category',
        data: chartData.categories,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: theme.axisLineColor } },
        axisLabel: { color: theme.labelColor, fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: theme.splitLineColor } },
        axisLabel: { color: theme.labelColor, fontSize: 11 }
      },
      series: [{
        name: '学生数量',
        type: 'bar',
        barWidth: '50%',
        data: chartData.series[0].data,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4facfe' },
            { offset: 1, color: '#00f2fe' }
          ])
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        animationDelay: (idx: number) => idx * 50
      }]
    });
  }
};

// 【知识点功能增强】加载并初始化知识点覆盖率图表
const loadAndInitKpCoverageChart = async () => {
  try {
    const res = await getKnowledgePointCoverage();
    if (res.code === 200 && res.data) {
      kpCoverageRawData.value = res.data;

      // 【新增】默认只显示前5个科目，避免图表过于拥挤
      const allSubjects = res.data.map((item: KnowledgePointCoverage) => item.subject_name);
      if (allSubjects.length > 5) {
        selectedSubjectsForCoverage.value = allSubjects.slice(0, 5);
      } else {
        selectedSubjectsForCoverage.value = []; // 5个以内全部显示
      }

      updateKpCoverageChart();
    }
  } catch (error) {
    console.error('获取知识点覆盖率失败:', error);
  }
};

// 【知识点功能增强】初始化薄弱知识点Top10图表
const initWeakKpChart = async () => {
  try {
    const res = await getWeakKnowledgePoints();
    if (res.code === 200 && res.data && weakKpChart.value) {
      const theme = getChartTheme();
      weakKpChartInstance = echarts.init(weakKpChart.value);

      const kpNames = res.data.map((item: any) => item.knowledge_point_name);
      const masteryRates = res.data.map((item: any) => item.avg_mastery_rate || 0);

      weakKpChartInstance.setOption({
        backgroundColor: theme.backgroundColor,
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: (params: any) => {
            const item = params[0];
            return `${item.name}<br/>平均掌握度: ${item.value}%`;
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '10px',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          max: 100,
          splitLine: { lineStyle: { type: 'dashed', color: theme.splitLineColor } },
          axisLabel: { color: theme.labelColor, formatter: '{value}%' }
        },
        yAxis: {
          type: 'category',
          data: kpNames,
          axisTick: { show: false },
          axisLine: { lineStyle: { color: theme.axisLineColor } },
          axisLabel: {
            color: theme.labelColor,
            fontSize: 11,
            interval: 0
          }
        },
        series: [{
          name: '掌握度',
          type: 'bar',
          data: masteryRates,
          barWidth: '60%',
          itemStyle: {
            borderRadius: [0, 4, 4, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#F56C6C' },
              { offset: 1, color: '#E6A23C' }
            ])
          },
          label: {
            show: true,
            position: 'right',
            formatter: '{c}%',
            color: theme.labelColor
          }
        }]
      });
    }
  } catch (error) {
    console.error('获取薄弱知识点失败:', error);
  }
};

const fetchData = async () => {
  try {
    chartLoading.value = true;
    const res = await getDashboardStats();
    if (res.code === 200) {
      stats.value = res.data;

      // 【新增】更新统计卡片的趋势数据
      if (stats.value.trends) {
        statCards.value[0].trend = stats.value.trends.onlineCountTrend;
        statCards.value[1].trend = stats.value.trends.studentCountTrend;
        statCards.value[2].trend = stats.value.trends.subjectCountTrend;
        statCards.value[3].trend = stats.value.trends.knowledgePointCountTrend;
        statCards.value[4].trend = stats.value.trends.questionCountTrend;
        statCards.value[5].trend = stats.value.trends.paperCountTrend;
      }

      // 【新增】初始化科目筛选：默认选中前7个科目，避免初始加载时文字重叠
      const allCats = stats.value.kpAndQuestionStats?.categories || [];
      if (allCats.length > 7) {
        selectedSubjectsForKpChart.value = allCats.slice(0, 7);
      } else {
        selectedSubjectsForKpChart.value = []; // 空数组表示全选
      }

      // 延迟初始化图表，确保DOM已渲染
      setTimeout(() => {
        updateKpAndQuestionChart();
        initSubjectStatsChart(stats.value.subjectStatsByGrade);
        initWrongStatsChart(stats.value.wrongQuestionStats);
        // 【知识点功能增强】初始化知识点统计图表
        loadAndInitKpCoverageChart();
        initWeakKpChart();
        chartLoading.value = false;
      }, 100);
    } else {
      ElMessage.error(res.msg || '获取统计数据失败');
      chartLoading.value = false;
    }
  } catch (error) {
    console.error("获取工作台数据时发生错误:", error);
    ElMessage.error("获取统计数据失败");
    chartLoading.value = false;
  } finally {
    loading.value = false;
  }
};

// 【新增】加载待办事项
const loadTodoList = async () => {
  todoLoading.value = true;
  try {
    const res = await getTodoList();
    if (res.code === 200) {
      todoItems.value = res.data.items || [];
    } else {
      console.error("获取待办事项失败:", res.message);
      ElMessage.warning(res.message || '获取待办事项失败');
    }
  } catch (error: any) {
    console.error("获取待办事项失败:", error);
    ElMessage.error(error.message || '获取待办事项失败');
  } finally {
    todoLoading.value = false;
  }
};

// 【新增】加载最近活动
const loadRecentActivities = async () => {
  try {
    const res = await getRecentActivities(5);
    if (res.code === 200) {
      recentActivities.value = res.data.activities || [];
    } else {
      console.error("获取最近活动失败:", res.message);
      ElMessage.warning(res.message || '获取最近活动失败');
    }
  } catch (error: any) {
    console.error("获取最近活动失败:", error);
    ElMessage.error(error.message || '获取最近活动失败');
  }
};

const updateOnlineCount = async () => {
  try {
    const res = await fetchOnlineStudentCount();
    if (res.code === 200) {
      socketStore.onlineStudentCount = res.data.count;
    }
  } catch (e) {
    console.error("获取在线人数失败", e);
  }
};

const handleNotificationClick = async (id: number) => {
  try {
    const res = await fetchNotificationById(id);
    if (res.code === 200) {
      selectedNotification.value = res.data;
      isPreviewVisible.value = true;
    }
  } catch (error) {
    console.error('获取通知详情失败', error);
  }
};

onMounted(async () => {
  // 加载布局配置
  await loadLayout();

  await fetchData();

  // 【新增】加载待办事项和最近活动
  await loadTodoList();
  await loadRecentActivities();

  // 入场动画
  gsap.from(".stat-card-item", {
    duration: 0.6,
    y: 30,
    opacity: 0,
    stagger: 0.08,
    ease: "power2.out"
  });

  // 监听窗口大小变化
  if (kpAndQuestionChart.value) {
    resizeObserver = new ResizeObserver(() => {
      kpChartInstance?.resize();
      subjectStatsChartInstance?.resize();
      wrongStatsChartInstance?.resize();
      kpCoverageChartInstance?.resize();    // 【知识点功能增强】
      weakKpChartInstance?.resize();        // 【知识点功能增强】
    });
    resizeObserver.observe(kpAndQuestionChart.value);
  }

  // 定时更新在线人数
  await updateOnlineCount();
  onlineCountTimer = setInterval(updateOnlineCount, 5000);
});

// 监听深色模式变化，重新渲染图表
watch(isDark, () => {
  updateKpAndQuestionChart();
  if (stats.value.wrongQuestionStats?.series?.length) {
    initWrongStatsChart(stats.value.wrongQuestionStats);
  }
  if (stats.value.subjectStatsByGrade?.series?.length) {
    initSubjectStatsChart(stats.value.subjectStatsByGrade);
  }
  // 【知识点功能增强】重新渲染知识点统计图表
  updateKpCoverageChart();
  initWeakKpChart();
});

onUnmounted(() => {
  kpChartInstance?.dispose();
  subjectStatsChartInstance?.dispose();
  wrongStatsChartInstance?.dispose();
  kpCoverageChartInstance?.dispose();    // 【知识点功能增强】
  weakKpChartInstance?.dispose();        // 【知识点功能增强】

  if (kpAndQuestionChart.value && resizeObserver) {
    resizeObserver.unobserve(kpAndQuestionChart.value);
  }

  if (onlineCountTimer) {
    clearInterval(onlineCountTimer);
    onlineCountTimer = null;
  }
});
</script>

<style scoped>
/* 全局容器 */
.dashboard-container {
  padding: 24px;
  background: linear-gradient(180deg, #f5f7fa 0%, #e8ecf1 100%);
  min-height: calc(100vh - 60px);
}

/* 顶部欢迎区 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.welcome-section .welcome-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-section .welcome-subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.action-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 快捷操作区 */
.quick-actions-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.quick-actions-card :deep(.el-card__body) {
  padding: 16px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  background: #e8ecf1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-info {
  flex: 1;
  min-width: 0;
}

.action-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 12px;
  color: #909399;
}

/* 统计卡片 - 增强版 */
.stat-row {
  margin-bottom: 20px;
}

.stat-card-enhanced {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
}

.stat-card-enhanced:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card-enhanced :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.trend-label {
  color: #909399;
  margin-left: 4px;
}

/* 数字滑动动画 */
.number-slide-enter-active,
.number-slide-leave-active {
  transition: all 0.3s ease;
}

.number-slide-enter-from {
  transform: translateY(-10px);
  opacity: 0;
}

.number-slide-leave-to {
  transform: translateY(10px);
  opacity: 0;
}

/* 主要内容区 */
.main-content-row {
  margin-bottom: 20px;
}

.section-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 20px;
  overflow: hidden;
}

.section-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.card-header-enhanced {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 待办事项 */
.todo-card :deep(.el-card__body) {
  padding: 0;
  height: 280px;
  overflow-y: auto;
}

.todo-list {
  padding: 12px;
}

.empty-state {
  height: 256px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.empty-state p {
  margin-top: 12px;
  font-size: 14px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.todo-item:hover {
  background: #e8ecf1;
  transform: translateX(4px);
}

.todo-item:last-child {
  margin-bottom: 0;
}

.todo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.todo-meta {
  font-size: 12px;
  color: #909399;
}

/* 系统通知 */
.notification-card :deep(.el-card__body) {
  padding: 0;
  height: 280px;
  overflow-y: auto;
}

/* 最近活动卡片 */
.activity-card :deep(.el-card__body) {
  padding: 0;
  height: 280px;
  overflow-y: auto;
}

.notification-list {
  padding: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item:last-child {
  margin-bottom: 0;
}

.notification-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409EFF;
  margin-top: 6px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-date {
  font-size: 12px;
  color: #909399;
}

/* 图表卡片 */
.chart-card :deep(.el-card__body) {
  padding: 20px;
}

.chart-summary {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.summary-item {
  text-align: center;
}

.summary-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.summary-divider {
  width: 1px;
  height: 40px;
  background: #dcdfe6;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.chart-card-small :deep(.el-card__body) {
  padding: 16px;
}

.chart-container-small {
  height: 260px;
  width: 100%;
}

/* 最近活动 */
.activity-timeline {
  padding: 12px;
}

.activity-timeline-dialog {
  padding: 10px 20px;
  max-height: 500px;
  overflow-y: auto;
}

.activity-timeline-dialog::-webkit-scrollbar {
  width: 6px;
}

.activity-timeline-dialog::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.activity-timeline-dialog::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.activity-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

/* 响应式 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-icon-wrapper {
    width: 48px;
    height: 48px;
  }

  .chart-container {
    height: 260px;
  }
}

/* 滚动条美化 */
:deep(.el-card__body)::-webkit-scrollbar {
  width: 6px;
}

:deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

:deep(.el-card__body)::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* ==================== 布局编辑模式样式 ==================== */
.draggable-container {
  width: 100%;
}

.draggable-item {
  width: 100%;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.ghost-module {
  opacity: 0.5;
  background: rgba(64, 158, 255, 0.1);
  border: 2px dashed #409EFF !important;
}

.module-row {
  margin-bottom: 0 !important;
}

.module-col {
  padding-bottom: 0 !important;
}

.module-wrapper {
  position: relative;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.module-wrapper.edit-mode {
  border: 2px dashed #409EFF;
  border-radius: 8px;
  padding: 8px;
  background: rgba(64, 158, 255, 0.02);
  cursor: move;
}

.module-wrapper.edit-mode:hover {
  border-color: #66b1ff;
  background: rgba(64, 158, 255, 0.05);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
}

.module-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 6px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.drag-handle {
  cursor: move;
  font-size: 18px;
  transition: all 0.2s ease;
  opacity: 0.7;
}

.drag-handle:hover {
  transform: scale(1.2);
  opacity: 1;
  color: #66b1ff;
}

.module-wrapper.edit-mode .drag-handle {
  animation: pulse-drag 2s infinite;
}

@keyframes pulse-drag {
  0%, 100% {
    opacity: 0.7;
  }
  50% {
    opacity: 1;
  }
}

.module-title {
  flex: 1;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.module-toolbar .el-select {
  width: 110px;
}

.module-toolbar :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.2);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.module-toolbar :deep(.el-input__inner) {
  color: white;
  font-weight: 500;
}

.module-toolbar :deep(.el-select__placeholder) {
  color: rgba(255, 255, 255, 0.8);
}

.module-toolbar :deep(.el-select__icon) {
  color: white;
}

/* 编辑模式提示 */
.dashboard-container {
  position: relative;
}

.edit-mode-indicator {
  position: fixed;
  bottom: 24px;
  right: 24px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  font-weight: 600;
  font-size: 14px;
  z-index: 100;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  }
}

/* 深色模式适配 */
.dark .module-wrapper.edit-mode {
  border-color: #409EFF;
  background: rgba(64, 158, 255, 0.05);
}

.dark .module-wrapper.edit-mode:hover {
  background: rgba(64, 158, 255, 0.08);
}
</style>
