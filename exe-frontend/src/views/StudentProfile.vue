<template>
  <div class="student-profile-modern">
    <!-- 个人信息头部区域 -->
    <div class="profile-header-section">
      <div class="header-background" :style="headerBgStyle">
        <div class="header-overlay"></div>
      </div>

      <div class="header-content">
        <div class="profile-main-info">
          <!-- 头像 -->
          <div class="avatar-container">
            <el-upload
              class="avatar-uploader"
              action="/api/v1/student/files/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :headers="{ 'Authorization': 'Bearer ' + studentAuth.token }"
            >
              <div class="avatar-wrapper" :style="{ borderColor: getLevelColor(studentLevel.level) }">
                <UserAvatar
                  :src="studentInfo.avatar"
                  :name="studentInfo.name"
                  :size="120"
                  :frame-style="studentInfo.avatarFrameStyle"
                />
                <div class="avatar-mask">
                  <el-icon :size="24"><Camera /></el-icon>
                </div>
              </div>
            </el-upload>
            <div class="level-badge" :style="{ background: getLevelColor(studentLevel.level) }">
              <el-icon><TrophyBase /></el-icon>
              <span>Lv.{{ studentLevel.level }}</span>
            </div>
          </div>

          <!-- 基本信息 -->
          <div class="basic-info">
            <h1 class="user-name">{{ studentInfo.name }}</h1>
            <div class="user-tags">
              <el-tag type="primary" effect="plain">{{ studentInfo.studentNo }}</el-tag>
              <el-tag type="success" effect="plain">{{ studentInfo.grade }}</el-tag>
              <el-tag type="warning" effect="plain">{{ studentInfo.className }}</el-tag>
            </div>

            <!-- 经验值进度条 -->
            <div class="exp-progress">
              <div class="exp-info">
                <span class="exp-text">经验值</span>
                <span class="exp-value">{{ studentLevel.currentPoints }} / {{ studentLevel.nextLevelPoints }}</span>
              </div>
              <el-progress
                :percentage="studentLevel.progress"
                :stroke-width="12"
                :color="getLevelColor(studentLevel.level)"
                :show-text="false"
              />
              <p class="exp-hint">距离下一级还需 {{ studentLevel.pointsToNext }} 经验值</p>
            </div>
          </div>

          <!-- 快速统计 -->
          <div class="quick-stats">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                <el-icon :size="24"><Trophy /></el-icon>
              </div>
              <div class="stat-data">
                <div class="stat-value">{{ achievedCount }}</div>
                <div class="stat-label">成就</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
                <el-icon :size="24"><Clock /></el-icon>
              </div>
              <div class="stat-data">
                <div class="stat-value">{{ formatMinutes(studyStats.totalMinutes) }}</div>
                <div class="stat-label">学习时长</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
                <el-icon :size="24"><Coin /></el-icon>
              </div>
              <div class="stat-data">
                <div class="stat-value">{{ studentInfo.points || 0 }}</div>
                <div class="stat-label">积分</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习数据概览卡片 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card gradient-purple">
            <div class="stat-card-content">
              <div class="stat-icon-wrapper">
                <el-icon :size="40"><Timer /></el-icon>
              </div>
              <div class="stat-details">
                <div class="stat-value">{{ formatMinutes(studyStats.todayMinutes) }}</div>
                <div class="stat-label">今日学习</div>
                <div class="stat-change positive">
                  <el-icon><TrendCharts /></el-icon>
                  <span>本周 {{ formatMinutes(studyStats.weekMinutes) }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card gradient-pink">
            <div class="stat-card-content">
              <div class="stat-icon-wrapper">
                <el-icon :size="40"><Calendar /></el-icon>
              </div>
              <div class="stat-details">
                <div class="stat-value">{{ studyStats.avgDailyMinutes }}</div>
                <div class="stat-label">日均学习(分钟)</div>
                <div class="stat-change positive">
                  <el-icon><TrendCharts /></el-icon>
                  <span>本月 {{ formatMinutes(studyStats.monthMinutes) }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card gradient-blue">
            <div class="stat-card-content">
              <div class="stat-icon-wrapper">
                <el-icon :size="40"><Medal /></el-icon>
              </div>
              <div class="stat-details">
                <div class="stat-value">{{ achievedCount }}</div>
                <div class="stat-label">已解锁成就</div>
                <div class="stat-change">
                  <span>共 {{ totalAchievements }} 个</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card gradient-green">
            <div class="stat-card-content">
              <div class="stat-icon-wrapper">
                <el-icon :size="40"><TrendCharts /></el-icon>
              </div>
              <div class="stat-details">
                <div class="stat-value">{{ studyRanking.rank || '-' }}</div>
                <div class="stat-label">学习排名</div>
                <div class="stat-change">
                  <span>前 {{ Math.round(studyRanking.percentile || 0) }}%</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Tab 内容区域 -->
    <el-card class="profile-tabs-card" shadow="never">
      <el-tabs v-model="activeTab" class="modern-tabs" @tab-change="handleTabChange">
        <!-- 基本信息 -->
        <el-tab-pane name="basic">
          <template #label>
            <span class="tab-label">
              <el-icon><User /></el-icon>
              <span>基本信息</span>
            </span>
          </template>

          <div class="tab-content">
            <el-form
              ref="basicFormRef"
              :model="basicForm"
              :rules="basicRules"
              label-width="120px"
              class="profile-form"
            >
              <el-row :gutter="24">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="学号">
                    <el-input v-model="basicForm.studentNo" disabled>
                      <template #prefix><el-icon><Postcard /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="12">
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="basicForm.name" placeholder="请输入姓名">
                      <template #prefix><el-icon><EditPen /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="年级">
                    <el-input v-model="basicForm.grade" disabled>
                      <template #prefix><el-icon><School /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="12">
                  <el-form-item label="班级">
                    <el-input v-model="basicForm.className" disabled>
                      <template #prefix><el-icon><Reading /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="联系方式" prop="contact">
                    <el-input v-model="basicForm.contact" placeholder="请输入联系方式">
                      <template #prefix><el-icon><Phone /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item>
                <el-button type="primary" @click="submitBasicForm" :loading="saving" size="large">
                  <el-icon><Select /></el-icon>
                  <span>保存修改</span>
                </el-button>
                <el-button @click="resetBasicForm" size="large">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 学习数据 -->
        <el-tab-pane name="learning">
          <template #label>
            <span class="tab-label">
              <el-icon><DataAnalysis /></el-icon>
              <span>学习数据</span>
            </span>
          </template>

          <div class="tab-content">
            <el-row :gutter="20">
              <!-- 每日学习时长趋势 -->
              <el-col :span="24">
                <div class="chart-card">
                  <h3 class="chart-title">近30天学习时长趋势</h3>
                  <div ref="dailyChartRef" class="chart-container" style="height: 350px;"></div>
                </div>
              </el-col>

              <!-- 按活动类型统计 -->
              <el-col :xs="24" :md="12">
                <div class="chart-card">
                  <h3 class="chart-title">学习活动分布</h3>
                  <div ref="activityChartRef" class="chart-container" style="height: 350px;"></div>
                </div>
              </el-col>

              <!-- 按科目统计 -->
              <el-col :xs="24" :md="12">
                <div class="chart-card">
                  <h3 class="chart-title">科目学习分布</h3>
                  <div ref="subjectChartRef" class="chart-container" style="height: 350px;"></div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 成就勋章 -->
        <el-tab-pane name="achievements">
          <template #label>
            <span class="tab-label">
              <el-icon><Medal /></el-icon>
              <span>成就勋章</span>
            </span>
          </template>

          <div class="tab-content">
            <div class="achievements-header">
              <div class="achievement-stats">
                <h3>已解锁 {{ achievedCount }} / {{ totalAchievements }} 个成就</h3>
                <p>完成度: {{ Math.round((achievedCount / totalAchievements) * 100) }}%</p>
              </div>
              <el-progress
                type="circle"
                :percentage="Math.round((achievedCount / totalAchievements) * 100)"
                :width="120"
                :stroke-width="8"
              >
                <template #default="{ percentage }">
                  <span class="percentage-value">{{ percentage }}%</span>
                </template>
              </el-progress>
            </div>

            <!-- 成就筛选 -->
            <div class="achievements-filter">
              <el-radio-group v-model="achievementFilter" @change="handleFilterChange">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="unlocked">已解锁</el-radio-button>
                <el-radio-button label="locked">未解锁</el-radio-button>
              </el-radio-group>
            </div>

            <!-- 成就网格 -->
            <div class="achievements-grid" v-loading="achievementsLoading">
              <div
                v-for="achievement in filteredAchievements"
                :key="achievement.id"
                :class="['achievement-item', achievement.isUnlocked ? 'unlocked' : 'locked']"
              >
                <div class="achievement-icon">
                  <div v-if="achievement.iconUrl && isEmoji(achievement.iconUrl)" class="emoji-icon">
                    {{ achievement.iconUrl }}
                  </div>
                  <el-icon v-else :size="48"><Trophy /></el-icon>
                  <div v-if="achievement.isUnlocked" class="unlock-mark">
                    <el-icon><Check /></el-icon>
                  </div>
                </div>
                <div class="achievement-info">
                  <h4>{{ achievement.name }}</h4>
                  <p>{{ achievement.description }}</p>
                  <div class="achievement-meta">
                    <el-tag :type="getAchievementTypeTag(achievement.type)" size="small">
                      {{ getAchievementTypeLabel(achievement.type) }}
                    </el-tag>
                    <span class="reward">+{{ achievement.rewardPoints }} 积分</span>
                  </div>
                  <div v-if="achievement.isUnlocked && achievement.unlockTime" class="unlock-time">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatDate(achievement.unlockTime) }}</span>
                  </div>
                </div>
              </div>

              <el-empty v-if="filteredAchievements.length === 0" description="暂无成就" />
            </div>
          </div>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane name="security">
          <template #label>
            <span class="tab-label">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </span>
          </template>

          <div class="tab-content">
            <el-alert
              title="密码安全提示"
              type="info"
              description="为了您的账号安全，建议定期更换密码，密码长度至少6位"
              :closable="false"
              show-icon
              class="security-alert"
            />

            <el-form
              ref="securityFormRef"
              :model="securityForm"
              :rules="securityRules"
              label-width="120px"
              class="profile-form"
              style="max-width: 600px; margin-top: 24px;"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                  v-model="securityForm.oldPassword"
                  type="password"
                  show-password
                  placeholder="请输入当前密码"
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="securityForm.newPassword"
                  type="password"
                  show-password
                  placeholder="请输入新密码(至少6位)"
                />
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="securityForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入新密码"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="submitSecurityForm" :loading="saving" size="large">
                  <el-icon><Lock /></el-icon>
                  <span>修改密码</span>
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules, UploadProps } from 'element-plus';
import * as echarts from 'echarts';
import {
  Camera, User, TrophyBase, EditPen, Phone, School, Reading,
  Lock, DataAnalysis, Medal, Trophy, Check, Clock, Timer,
  Calendar, Coin, TrendCharts, Postcard, Select, Star
} from '@element-plus/icons-vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { updateStudentProfile } from '@/api/studentAuth';
import { getMyAchievements, type Achievement } from '@/api/achievement';
import {
  getStudyTimeStats,
  getDailyStudyTime,
  getActivityTypeStats,
  getSubjectStudyTime,
  getStudyRanking,
  type StudyTimeStats,
  type DailyStudyTime,
  type ActivityTypeStats,
  type SubjectStudyTime,
  type StudyRanking
} from '@/api/learningActivity';
import UserAvatar from '@/components/UserAvatar.vue';

const studentAuth = useStudentAuthStore();
const activeTab = ref('basic');
const saving = ref(false);

// 学生信息
const studentInfo = computed(() => studentAuth.student || {});

// 等级系统（基于积分计算）
const studentLevel = computed(() => {
  const points = studentInfo.value.points || 0;
  const level = Math.floor(points / 200) + 1; // 每200积分升一级
  const currentLevelPoints = (level - 1) * 200;
  const nextLevelPoints = level * 200;
  const currentPoints = points - currentLevelPoints;
  const pointsToNext = nextLevelPoints - points;
  const progress = Math.round((currentPoints / 200) * 100);

  return {
    level,
    currentPoints: points,
    nextLevelPoints,
    progress: Math.min(progress, 100),
    pointsToNext: Math.max(pointsToNext, 0)
  };
});

// 头部背景
const headerBgStyle = computed(() => {
  const bgValue = studentInfo.value.backgroundUrl;
  if (bgValue) {
    // 判断是渐变色还是图片URL
    if (bgValue.startsWith('linear-gradient') || bgValue.startsWith('radial-gradient')) {
      // 渐变背景，直接使用
      return { background: bgValue };
    } else {
      // 图片URL，用url()包裹
      return { backgroundImage: `url(${bgValue})` };
    }
  }
  return { background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' };
});

// 根据等级获取颜色
const getLevelColor = (level: number): string => {
  if (level < 5) return '#909399';
  if (level < 10) return '#67c23a';
  if (level < 20) return '#409eff';
  if (level < 30) return '#e6a23c';
  return '#f56c6c';
};

// 学习统计数据
const studyStats = ref<StudyTimeStats>({
  totalMinutes: 0,
  todayMinutes: 0,
  weekMinutes: 0,
  monthMinutes: 0,
  avgDailyMinutes: 0
});

const studyRanking = ref<StudyRanking>({
  rank: 0,
  totalStudents: 0,
  percentile: 0
});

const dailyStudyData = ref<DailyStudyTime[]>([]);
const activityTypeData = ref<ActivityTypeStats[]>([]);
const subjectStudyData = ref<SubjectStudyTime[]>([]);

// 成就数据
const achievementsLoading = ref(false);
const achievements = ref<Achievement[]>([]);
const achievementFilter = ref<'all' | 'unlocked' | 'locked'>('all');

const achievedCount = computed(() => achievements.value.filter(a => a.isUnlocked).length);
const totalAchievements = computed(() => achievements.value.length);

const filteredAchievements = computed(() => {
  if (achievementFilter.value === 'unlocked') {
    return achievements.value.filter(a => a.isUnlocked);
  } else if (achievementFilter.value === 'locked') {
    return achievements.value.filter(a => !a.isUnlocked);
  }
  return achievements.value;
});

// 基本信息表单
const basicFormRef = ref<FormInstance>();
const basicForm = reactive({
  studentNo: '',
  name: '',
  grade: '',
  className: '',
  contact: ''
});

const basicRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  contact: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
};

// 安全设置表单
const securityFormRef = ref<FormInstance>();
const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const validatePass = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入新密码'));
  } else if (value.length < 6) {
    callback(new Error('密码长度至少6位'));
  } else {
    if (securityForm.confirmPassword !== '') {
      securityFormRef.value?.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== securityForm.newPassword) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

const securityRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: validatePass, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
};

// 图表引用
const dailyChartRef = ref<HTMLElement>();
const activityChartRef = ref<HTMLElement>();
const subjectChartRef = ref<HTMLElement>();

let dailyChart: echarts.ECharts | null = null;
let activityChart: echarts.ECharts | null = null;
let subjectChart: echarts.ECharts | null = null;

// 加载学习数据
const loadStudyData = async () => {
  try {
    const [stats, ranking, daily, activity, subject] = await Promise.all([
      getStudyTimeStats(),
      getStudyRanking(),
      getDailyStudyTime(30),
      getActivityTypeStats(30),
      getSubjectStudyTime(30)
    ]);

    if (stats.code === 200) studyStats.value = stats.data;
    if (ranking.code === 200) studyRanking.value = ranking.data;
    if (daily.code === 200) dailyStudyData.value = daily.data;
    if (activity.code === 200) activityTypeData.value = activity.data;
    if (subject.code === 200) subjectStudyData.value = subject.data;

    // 如果在学习数据tab，初始化图表
    if (activeTab.value === 'learning') {
      nextTick(() => initCharts());
    }
  } catch (error) {
    console.error('加载学习数据失败:', error);
  }
};

// 加载成就数据
const loadAchievements = async () => {
  achievementsLoading.value = true;
  try {
    const res = await getMyAchievements();
    if (res.code === 200) {
      achievements.value = res.data;
    }
  } catch (error) {
    console.error('加载成就失败:', error);
  } finally {
    achievementsLoading.value = false;
  }
};

// 初始化图表
const initCharts = () => {
  initDailyChart();
  initActivityChart();
  initSubjectChart();
};

// 每日学习时长趋势图
const initDailyChart = () => {
  if (!dailyChartRef.value) return;

  if (dailyChart) {
    dailyChart.dispose();
  }

  dailyChart = echarts.init(dailyChartRef.value);

  const dates = dailyStudyData.value.map(d => d.date);
  const minutes = dailyStudyData.value.map(d => d.minutes);

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        return `${params[0].name}<br/>学习时长: ${params[0].value} 分钟`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        formatter: (value: string) => {
          return value.substring(5); // 显示 MM-DD
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '分钟'
    },
    series: [
      {
        data: minutes,
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
          ])
        },
        itemStyle: {
          color: '#667eea'
        },
        lineStyle: {
          width: 3
        }
      }
    ]
  };

  dailyChart.setOption(option);
};

// 活动类型饼图
const initActivityChart = () => {
  if (!activityChartRef.value) return;

  if (activityChart) {
    activityChart.dispose();
  }

  activityChart = echarts.init(activityChartRef.value);

  const data = activityTypeData.value.map(item => ({
    name: getActivityTypeLabel(item.activityType),
    value: item.minutes
  }));

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 分钟 ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  };

  activityChart.setOption(option);
};

// 科目学习柱状图
const initSubjectChart = () => {
  if (!subjectChartRef.value) return;

  if (subjectChart) {
    subjectChart.dispose();
  }

  subjectChart = echarts.init(subjectChartRef.value);

  const subjects = subjectStudyData.value.map(s => s.subjectName);
  const minutes = subjectStudyData.value.map(s => s.minutes);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: subjects
    },
    yAxis: {
      type: 'value',
      name: '分钟'
    },
    series: [
      {
        data: minutes,
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f093fb' },
            { offset: 1, color: '#f5576c' }
          ]),
          borderRadius: [8, 8, 0, 0]
        }
      }
    ]
  };

  subjectChart.setOption(option);
};

// 工具函数
const formatMinutes = (minutes: number): string => {
  if (minutes < 60) return `${minutes}分`;
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return mins > 0 ? `${hours}小时${mins}分` : `${hours}小时`;
};

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
};

const isEmoji = (str: string): boolean => {
  return str.length < 10 && !str.startsWith('http');
};

const getActivityTypeLabel = (type: string): string => {
  const map: Record<string, string> = {
    'EXAM': '考试',
    'PRACTICE_SUBMIT': '练习',
    'AI_CHAT': 'AI对话',
    'REVIEW': '复习',
    'COURSE': '课程学习',
    'SIGN_IN': '签到'
  };
  return map[type] || type;
};

const getAchievementTypeLabel = (type: string): string => {
  const map: Record<string, string> = {
    'SIGN_IN_STREAK': '签到',
    'TOTAL_QUESTIONS': '答题',
    'PERFECT_PAPER': '满分',
    'LEARNING_TIME': '学习',
    'BATTLE': '对战',
    'WRONG_RECORD': '错题',
    'AI_CHAT': 'AI',
    'EXAM_SCORE': '成绩',
    'FAVORITE': '收藏'
  };
  return map[type] || '其他';
};

const getAchievementTypeTag = (type: string): string => {
  const map: Record<string, string> = {
    'SIGN_IN_STREAK': 'success',
    'TOTAL_QUESTIONS': 'primary',
    'PERFECT_PAPER': 'warning',
    'LEARNING_TIME': 'info',
    'BATTLE': 'danger'
  };
  return map[type] || '';
};

// 头像上传成功
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    ElMessage.success('头像更新成功');
    studentAuth.updateAvatar(response.data);
  } else {
    ElMessage.error(response.message || '头像上传失败');
  }
};

// 提交基本信息
const submitBasicForm = async () => {
  if (!basicFormRef.value) return;

  await basicFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true;
      try {
        const res = await updateStudentProfile({
          name: basicForm.name,
          contact: basicForm.contact
        });

        if (res.code === 200) {
          ElMessage.success('信息更新成功');
          await studentAuth.fetchUserInfo();
        } else {
          ElMessage.error(res.message || '更新失败');
        }
      } catch (error) {
        ElMessage.error('更新失败');
      } finally {
        saving.value = false;
      }
    }
  });
};

// 重置基本信息表单
const resetBasicForm = () => {
  Object.assign(basicForm, {
    studentNo: studentInfo.value.studentNo || '',
    name: studentInfo.value.name || '',
    grade: studentInfo.value.grade || '',
    className: studentInfo.value.className || '',
    contact: studentInfo.value.contact || ''
  });
};

// 提交密码修改
const submitSecurityForm = async () => {
  if (!securityFormRef.value) return;

  await securityFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true;
      try {
        // 调用修改密码API
        await updateStudentProfile({
          oldPassword: securityForm.oldPassword,
          password: securityForm.newPassword
        });
        ElMessage.success('密码修改成功，请重新登录');
        securityFormRef.value?.resetFields();

        // 修改密码成功后，自动退出登录
        setTimeout(() => {
          authStore.logout();
        }, 2000);
      } catch (error: any) {
        console.error('密码修改失败:', error);
        ElMessage.error(error.message || '密码修改失败，请检查当前密码是否正确');
      } finally {
        saving.value = false;
      }
    }
  });
};

// Tab切换处理
const handleTabChange = (tabName: string) => {
  if (tabName === 'learning') {
    nextTick(() => initCharts());
  } else if (tabName === 'achievements' && achievements.value.length === 0) {
    loadAchievements();
  }
};

const handleFilterChange = () => {
  // 筛选逻辑已在计算属性中处理
};

// 监听窗口大小变化，重新渲染图表
const handleResize = () => {
  dailyChart?.resize();
  activityChart?.resize();
  subjectChart?.resize();
};

// 初始化
onMounted(async () => {
  // 初始化表单数据
  resetBasicForm();

  // 加载学习数据
  await loadStudyData();

  // 添加窗口resize监听
  window.addEventListener('resize', handleResize);
});

// 清理
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  dailyChart?.dispose();
  activityChart?.dispose();
  subjectChart?.dispose();
});
</script>

<style scoped lang="scss">
.student-profile-modern {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

// 头部区域
.profile-header-section {
  position: relative;
  margin-bottom: 24px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 280px;
  background-size: cover;
  background-position: center;
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.6) 100%);
}

.header-content {
  position: relative;
  padding: 32px;
  background: white;
  margin-top: 180px;
  border-radius: 16px 16px 0 0;
}

.profile-main-info {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 32px;
  align-items: start;
}

// 头像区域
.avatar-container {
  position: relative;
  margin-top: -80px;
}

.avatar-wrapper {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  border: 5px solid white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);

    .avatar-mask {
      opacity: 1;
    }
  }
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;

  span {
    font-size: 12px;
  }
}

.level-badge {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  padding: 6px 16px;
  border-radius: 20px;
  color: white;
  font-weight: bold;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  white-space: nowrap;
}

// 基本信息
.basic-info {
  flex: 1;
  padding-top: 8px;
}

.user-name {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
}

.user-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.exp-progress {
  margin-top: 16px;

  .exp-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    color: #606266;
  }

  .exp-value {
    font-weight: 600;
    color: #409eff;
  }

  .exp-hint {
    margin: 8px 0 0 0;
    font-size: 12px;
    color: #909399;
  }
}

// 快速统计
.quick-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 200px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:hover {
    transform: translateX(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-data {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

// 统计卡片
.stats-overview {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
  }

  &.gradient-purple {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }

  &.gradient-pink {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
  }

  &.gradient-blue {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    color: white;
  }

  &.gradient-green {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    color: white;
  }
}

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-details {
  flex: 1;

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    line-height: 1.2;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
  }

  .stat-change {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    opacity: 0.8;

    &.positive {
      color: rgba(255, 255, 255, 0.95);
    }
  }
}

// Tab内容区域
.profile-tabs-card {
  border-radius: 16px;
  border: none;
}

.modern-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    font-size: 16px;
    font-weight: 500;
    padding: 0 24px;
    height: 52px;
    line-height: 52px;
  }

  :deep(.el-tabs__active-bar) {
    height: 3px;
  }
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-content {
  padding: 24px;
}

// 表单样式
.profile-form {
  max-width: 800px;

  :deep(.el-form-item__label) {
    font-weight: 500;
  }

  :deep(.el-input__inner) {
    height: 44px;
  }
}

.security-alert {
  margin-bottom: 24px;
}

// 图表卡片
.chart-card {
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.chart-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.chart-container {
  width: 100%;
}

// 成就区域
.achievements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  margin-bottom: 24px;

  h3 {
    margin: 0 0 8px 0;
    font-size: 24px;
  }

  p {
    margin: 0;
    opacity: 0.9;
  }
}

.percentage-value {
  font-size: 20px;
  font-weight: 700;
}

.achievements-filter {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.achievement-item {
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 16px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  }

  &.unlocked {
    border: 2px solid #67c23a;
  }

  &.locked {
    opacity: 0.7;
  }
}

.achievement-icon {
  position: relative;
  width: 64px;
  height: 64px;
  flex-shrink: 0;

  .emoji-icon {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    background: linear-gradient(135deg, #ffd700, #ffed4e);
    border-radius: 12px;
  }

  .el-icon {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #d0d0d0, #e0e0e0);
    border-radius: 12px;
    color: white;
  }

  .locked & {
    filter: grayscale(100%);
  }
}

.unlock-mark {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 24px;
  height: 24px;
  background: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border: 2px solid white;
}

.achievement-info {
  flex: 1;

  h4 {
    margin: 0 0 8px 0;
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
  }

  p {
    margin: 0 0 12px 0;
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
  }
}

.achievement-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  .reward {
    font-size: 13px;
    color: #e6a23c;
    font-weight: 600;
  }
}

.unlock-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

// 响应式
@media (max-width: 768px) {
  .profile-main-info {
    grid-template-columns: 1fr;
    gap: 20px;
    text-align: center;
  }

  .avatar-container {
    margin: -60px auto 0;
  }

  .basic-info {
    padding-top: 0;
  }

  .user-tags {
    justify-content: center;
  }

  .quick-stats {
    flex-direction: row;
    width: 100%;
    overflow-x: auto;
  }

  .stat-item {
    min-width: 150px;
  }

  .achievements-grid {
    grid-template-columns: 1fr;
  }

  .achievements-header {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
}
</style>
