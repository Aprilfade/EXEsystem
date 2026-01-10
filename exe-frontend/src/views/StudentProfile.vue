<template>
  <div class="student-profile-container">
    <!-- 个人信息头部卡片 -->
    <el-card class="profile-header-card" shadow="never">
      <div class="profile-header" :style="headerStyle">
        <div class="profile-header-bg"></div>
        <div class="profile-header-content">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader-large"
              action="/api/v1/student/files/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :headers="{ 'Authorization': 'Bearer ' + studentAuth.token }"
            >
              <div class="avatar-wrapper">
                <UserAvatar
                  :src="studentInfo.avatar"
                  :name="studentInfo.name"
                  :size="120"
                  :frame-style="studentInfo.avatarFrameStyle"
                />
                <div class="avatar-hover-mask">
                  <el-icon :size="24"><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
            </el-upload>
          </div>

          <!-- 用户信息 -->
          <div class="user-info-section">
            <h2 class="user-name">{{ studentInfo.name }}</h2>
            <p class="user-role">
              <el-tag type="primary" effect="dark">{{ studentInfo.grade }} | {{ studentInfo.className }}</el-tag>
            </p>
            <p class="user-meta">
              <el-icon><Postcard /></el-icon>
              <span>学号: {{ studentInfo.studentNo }}</span>
              <el-divider direction="vertical" />
              <el-icon><TrophyBase /></el-icon>
              <span>等级 {{ studentLevel.level }}</span>
            </p>

            <!-- 等级进度条 -->
            <div class="level-progress">
              <el-progress
                :percentage="studentLevel.progress"
                :stroke-width="8"
                :color="levelColors"
              >
                <template #default="{ percentage }">
                  <span class="progress-text">{{ percentage }}%</span>
                </template>
              </el-progress>
              <div class="level-hint">距离下一级还需 {{ studentLevel.pointsToNext }} 积分</div>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="quick-actions">
            <el-button :icon="Edit" @click="activeTab = 'basic'">编辑资料</el-button>
            <el-button :icon="Medal" @click="activeTab = 'achievements'">我的成就</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 学习数据统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in learningStats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-trend" v-if="stat.trend">
              <el-icon :color="stat.trend > 0 ? '#67C23A' : '#F56C6C'">
                <component :is="stat.trend > 0 ? ArrowUp : ArrowDown" />
              </el-icon>
              <span :style="{ color: stat.trend > 0 ? '#67C23A' : '#F56C6C' }">
                {{ Math.abs(stat.trend) }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab 切换内容 -->
    <el-card class="profile-content-card" shadow="never">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本信息 -->
        <el-tab-pane name="basic">
          <template #label>
            <span><el-icon><User /></el-icon> 基本信息</span>
          </template>

          <el-form
            ref="basicFormRef"
            :model="basicForm"
            :rules="basicRules"
            label-width="100px"
            class="profile-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="学号">
                  <el-input v-model="basicForm.studentNo" disabled>
                    <template #prefix><el-icon><Postcard /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="basicForm.name" placeholder="请输入姓名">
                    <template #prefix><el-icon><EditPen /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="班级">
                  <el-input :value="basicForm.grade + ' ' + basicForm.className" disabled>
                    <template #prefix><el-icon><School /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系方式" prop="contact">
                  <el-input v-model="basicForm.contact" placeholder="请输入联系方式">
                    <template #prefix><el-icon><Phone /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="个性签名">
              <el-input
                v-model="basicForm.signature"
                type="textarea"
                :rows="3"
                placeholder="写下你的个性签名..."
                maxlength="100"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitBasicForm" :loading="saving">
                保存修改
              </el-button>
              <el-button @click="resetBasicForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 学习数据 -->
        <el-tab-pane name="learning">
          <template #label>
            <span><el-icon><DataAnalysis /></el-icon> 学习数据</span>
          </template>

          <div class="learning-data-section">
            <!-- 成绩趋势图 -->
            <div class="chart-container">
              <h3 class="section-title">成绩趋势</h3>
              <div ref="scoreChartRef" class="chart" style="height: 300px;"></div>
            </div>

            <!-- 学科分析 -->
            <div class="chart-container">
              <h3 class="section-title">学科能力分析</h3>
              <div ref="radarChartRef" class="chart" style="height: 400px;"></div>
            </div>

            <!-- 学习时长统计 -->
            <div class="study-time-stats">
              <h3 class="section-title">本周学习时长</h3>
              <div class="time-cards">
                <div class="time-card" v-for="day in weekStudyTime" :key="day.day">
                  <div class="time-day">{{ day.day }}</div>
                  <div class="time-value">{{ day.minutes }}min</div>
                  <el-progress :percentage="day.percentage" :show-text="false" />
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 成就勋章 -->
        <el-tab-pane name="achievements">
          <template #label>
            <span><el-icon><Medal /></el-icon> 成就勋章</span>
          </template>

          <div class="achievements-section">
            <div class="achievements-header">
              <h3>已获得 {{ achievedCount }} / {{ achievements.length }} 个成就</h3>
              <el-progress
                :percentage="achievementProgress"
                :stroke-width="10"
                :color="progressColors"
              />
            </div>

            <div class="achievements-grid">
              <div
                v-for="achievement in achievements"
                :key="achievement.id"
                class="achievement-card"
                :class="{ achieved: achievement.achieved }"
              >
                <div class="achievement-icon">
                  <el-icon :size="40" :color="achievement.achieved ? achievement.color : '#dcdfe6'">
                    <component :is="achievement.icon" />
                  </el-icon>
                </div>
                <div class="achievement-info">
                  <h4>{{ achievement.name }}</h4>
                  <p>{{ achievement.description }}</p>
                  <div class="achievement-progress" v-if="!achievement.achieved">
                    <el-progress
                      :percentage="achievement.progress"
                      :stroke-width="4"
                      :show-text="false"
                    />
                    <span class="progress-hint">{{ achievement.current }}/{{ achievement.target }}</span>
                  </div>
                  <el-tag v-else type="success" size="small">已获得</el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane name="security">
          <template #label>
            <span><el-icon><Lock /></el-icon> 安全设置</span>
          </template>

          <el-form
            ref="securityFormRef"
            :model="securityForm"
            :rules="securityRules"
            label-width="120px"
            class="profile-form"
          >
            <el-alert
              title="密码安全提示"
              type="info"
              description="为了您的账号安全，建议定期更换密码"
              :closable="false"
              show-icon
              class="security-alert"
            />

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
                placeholder="请输入新密码"
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
              <el-button type="primary" @click="submitSecurityForm" :loading="saving">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- AI助手设置 -->
        <el-tab-pane name="ai-settings">
          <template #label>
            <span><el-icon><ChatDotRound /></el-icon> AI助手设置</span>
          </template>

          <el-form
            ref="aiFormRef"
            :model="aiForm"
            label-width="120px"
            class="profile-form"
          >
            <el-alert
              title="AI助手配置说明"
              type="info"
              :closable="false"
              show-icon
              class="security-alert"
            >
              <template #default>
                <p>配置您的AI API Key后，即可使用小艾学习助手、AI错题分析等功能</p>
                <p style="margin-top: 8px;">
                  支持的AI服务商：DeepSeek、OpenAI、文心一言、通义千问等
                </p>
              </template>
            </el-alert>

            <el-form-item label="AI服务商" prop="provider">
              <el-select v-model="aiForm.provider" placeholder="选择AI服务商" style="width: 100%">
                <el-option label="DeepSeek (推荐)" value="deepseek" />
                <el-option label="OpenAI" value="openai" />
                <el-option label="文心一言" value="ernie" />
                <el-option label="通义千问" value="qwen" />
                <el-option label="Kimi" value="kimi" />
              </el-select>
            </el-form-item>

            <el-form-item label="API Key" prop="apiKey">
              <el-input
                v-model="aiForm.apiKey"
                type="password"
                show-password
                placeholder="请输入您的AI API Key"
              >
                <template #append>
                  <el-button @click="testAiConnection" :loading="testing">测试连接</el-button>
                </template>
              </el-input>
              <div class="form-tip">
                <el-icon><InfoFilled /></el-icon>
                <span>如何获取API Key？</span>
                <el-link
                  type="primary"
                  href="https://platform.deepseek.com/api_keys"
                  target="_blank"
                  style="margin-left: 8px"
                >
                  DeepSeek官网
                </el-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitAiForm" :loading="saving">
                保存设置
              </el-button>
              <el-button @click="resetAiForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules, UploadProps } from 'element-plus';
import * as echarts from 'echarts';
import {
  Camera, Edit, Medal, User, Postcard, TrophyBase, EditPen, Phone, School,
  Lock, DataAnalysis, DocumentCopy, Star, Trophy, Flag, ArrowUp, ArrowDown,
  ChatDotRound, InfoFilled
} from '@element-plus/icons-vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { updateStudentProfile } from '@/api/studentAuth';
import type { Student } from '@/api/student';
import UserAvatar from '@/components/UserAvatar.vue';

const studentAuth = useStudentAuthStore();
const activeTab = ref('basic');
const saving = ref(false);

// 学生信息
const studentInfo = computed(() => studentAuth.student || {});

// 等级系统
const studentLevel = ref({
  level: 5,
  currentPoints: 750,
  nextLevelPoints: 1000,
  progress: 75,
  pointsToNext: 250
});

const levelColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 }
];

// 头部背景样式
const headerStyle = computed(() => {
  const bgUrl = studentInfo.value.backgroundUrl;
  if (bgUrl) {
    return { backgroundImage: `url(${bgUrl})`, backgroundSize: 'cover' };
  }
  return {};
});

// 学习统计数据
const learningStats = ref([
  { label: '已完成试卷', value: 24, color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', icon: DocumentCopy, trend: 12 },
  { label: '正确率', value: '85%', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', icon: Star, trend: 5 },
  { label: '学习天数', value: 45, color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', icon: Flag, trend: -2 },
  { label: '获得勋章', value: 12, color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', icon: Trophy, trend: 8 }
]);

// 本周学习时长
const weekStudyTime = ref([
  { day: '周一', minutes: 120, percentage: 80 },
  { day: '周二', minutes: 90, percentage: 60 },
  { day: '周三', minutes: 150, percentage: 100 },
  { day: '周四', minutes: 75, percentage: 50 },
  { day: '周五', minutes: 110, percentage: 73 },
  { day: '周六', minutes: 60, percentage: 40 },
  { day: '周日', minutes: 45, percentage: 30 }
]);

// 成就系统
const achievements = ref([
  {
    id: 1,
    name: '初出茅庐',
    description: '完成第一次考试',
    icon: Star,
    color: '#409eff',
    achieved: true,
    progress: 100,
    current: 1,
    target: 1
  },
  {
    id: 2,
    name: '勤奋学子',
    description: '连续学习7天',
    icon: Flag,
    color: '#67c23a',
    achieved: true,
    progress: 100,
    current: 7,
    target: 7
  },
  {
    id: 3,
    name: '百题大师',
    description: '完成100道题目',
    icon: Trophy,
    color: '#e6a23c',
    achieved: false,
    progress: 65,
    current: 65,
    target: 100
  },
  {
    id: 4,
    name: '全科精通',
    description: '所有科目达到80分以上',
    icon: Medal,
    color: '#f56c6c',
    achieved: false,
    progress: 40,
    current: 2,
    target: 5
  }
]);

const achievedCount = computed(() => achievements.value.filter(a => a.achieved).length);
const achievementProgress = computed(() => (achievedCount.value / achievements.value.length) * 100);
const progressColors = [
  { color: '#f56c6c', percentage: 30 },
  { color: '#e6a23c', percentage: 70 },
  { color: '#67c23a', percentage: 100 }
];

// 基本信息表单
const basicFormRef = ref<FormInstance>();
const basicForm = reactive<Partial<Student>>({});

const basicRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
});

// 安全设置表单
const securityFormRef = ref<FormInstance>();
const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const securityRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== securityForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

// AI设置表单
const aiFormRef = ref<FormInstance>();
const aiForm = reactive({
  provider: 'deepseek',
  apiKey: ''
});
const testing = ref(false);

// 图表引用
const scoreChartRef = ref<HTMLElement | null>(null);
const radarChartRef = ref<HTMLElement | null>(null);

// 初始化数据
const initData = () => {
  Object.assign(basicForm, studentInfo.value);
};

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    initScoreChart();
    initRadarChart();
  });
};

// 成绩趋势图
const initScoreChart = () => {
  if (!scoreChartRef.value) return;

  const chart = echarts.init(scoreChartRef.value);
  chart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['数学', '语文', '英语']
    },
    xAxis: {
      type: 'category',
      data: ['第一次', '第二次', '第三次', '第四次', '第五次']
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    series: [
      {
        name: '数学',
        type: 'line',
        smooth: true,
        data: [75, 82, 85, 88, 92],
        itemStyle: { color: '#409eff' }
      },
      {
        name: '语文',
        type: 'line',
        smooth: true,
        data: [80, 78, 85, 87, 90],
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '英语',
        type: 'line',
        smooth: true,
        data: [70, 75, 80, 82, 88],
        itemStyle: { color: '#e6a23c' }
      }
    ]
  });
};

// 学科能力雷达图
const initRadarChart = () => {
  if (!radarChartRef.value) return;

  const chart = echarts.init(radarChartRef.value);
  chart.setOption({
    tooltip: {},
    radar: {
      indicator: [
        { name: '数学', max: 100 },
        { name: '语文', max: 100 },
        { name: '英语', max: 100 },
        { name: '物理', max: 100 },
        { name: '化学', max: 100 }
      ]
    },
    series: [{
      name: '学科能力',
      type: 'radar',
      data: [
        {
          value: [92, 90, 88, 85, 87],
          name: '我的成绩',
          areaStyle: {
            color: 'rgba(64, 158, 255, 0.3)'
          },
          itemStyle: { color: '#409eff' }
        }
      ]
    }]
  });
};

// 头像上传
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    basicForm.avatar = response.data;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.msg || '头像上传失败');
  }
};

// 提交基本信息
const submitBasicForm = async () => {
  if (!basicFormRef.value) return;
  await basicFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        saving.value = true;
        await updateStudentProfile(basicForm);
        await studentAuth.fetchStudentInfo();
        ElMessage.success('个人信息更新成功');
      } catch (error) {
        console.error('更新失败:', error);
      } finally {
        saving.value = false;
      }
    }
  });
};

// 重置表单
const resetBasicForm = () => {
  initData();
};

// 提交安全设置
const submitSecurityForm = async () => {
  if (!securityFormRef.value) return;
  await securityFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        saving.value = true;
        await updateStudentProfile({
          oldPassword: securityForm.oldPassword,
          password: securityForm.newPassword
        });
        ElMessage.success('密码修改成功');
        securityForm.oldPassword = '';
        securityForm.newPassword = '';
        securityForm.confirmPassword = '';
      } catch (error) {
        console.error('修改密码失败:', error);
      } finally {
        saving.value = false;
      }
    }
  });
};

// 加载AI设置
const loadAiSettings = () => {
  // 从localStorage加载已保存的AI设置
  const savedProvider = localStorage.getItem('student_ai_provider');
  const savedApiKey = localStorage.getItem('student_ai_key');

  if (savedProvider) {
    aiForm.provider = savedProvider;
  }
  if (savedApiKey) {
    aiForm.apiKey = savedApiKey;
  }
};

// 测试AI连接
const testAiConnection = async () => {
  if (!aiForm.apiKey) {
    ElMessage.warning('请先输入API Key');
    return;
  }

  testing.value = true;
  try {
    // 发送测试请求到AI服务
    const response = await fetch('/api/v1/student/ai/test-connection', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${studentAuth.token}`,
        'X-Ai-Api-Key': aiForm.apiKey,
        'X-Ai-Provider': aiForm.provider
      },
      body: JSON.stringify({
        message: '你好，这是一个连接测试'
      })
    });

    if (response.ok) {
      ElMessage.success('连接测试成功！API Key有效');
    } else {
      ElMessage.error('连接测试失败，请检查API Key是否正确');
    }
  } catch (error) {
    console.error('测试连接失败:', error);
    ElMessage.error('连接测试失败，请检查网络或API Key');
  } finally {
    testing.value = false;
  }
};

// 提交AI设置
const submitAiForm = async () => {
  if (!aiForm.apiKey) {
    ElMessage.warning('请输入API Key');
    return;
  }

  try {
    saving.value = true;

    // 使用store的setAiConfig方法保存配置
    studentAuth.setAiConfig(aiForm.apiKey, aiForm.provider);

    ElMessage.success('AI设置保存成功');
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败，请重试');
  } finally {
    saving.value = false;
  }
};

// 重置AI表单
const resetAiForm = () => {
  aiForm.provider = 'deepseek';
  aiForm.apiKey = '';
  studentAuth.setAiConfig('', 'deepseek');
  ElMessage.success('已重置AI设置');
};

onMounted(() => {
  initData();
  initCharts();
  loadAiSettings();
});
</script>

<style scoped>
/* 复用管理员端的大部分样式，这里只添加学生端特有的样式 */
.student-profile-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.profile-header {
  position: relative;
}

.profile-header-bg {
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.profile-header-content {
  position: relative;
  margin-top: -80px;
  padding: 0 40px 30px;
  display: flex;
  align-items: flex-end;
  gap: 30px;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border: 4px solid var(--bg-secondary, #fff);
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 12px;
}

.avatar-wrapper:hover .avatar-hover-mask {
  opacity: 1;
}

.user-info-section {
  flex-grow: 1;
}

.user-name {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-role {
  margin-bottom: 12px;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 12px;
}

.level-progress {
  max-width: 400px;
}

.level-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex-grow: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

/* 内容卡片 */
.profile-content-card {
  border-radius: 12px;
}

.profile-form {
  max-width: 800px;
  padding: 20px;
}

.security-alert {
  margin-bottom: 24px;
}

.form-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.form-tip .el-icon {
  font-size: 14px;
}

/* 学习数据 */
.learning-data-section {
  padding: 20px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-container {
  margin-bottom: 40px;
}

.study-time-stats {
  margin-top: 30px;
}

.time-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.time-card {
  text-align: center;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.time-day {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.time-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

/* 成就系统 */
.achievements-section {
  padding: 20px;
}

.achievements-header {
  margin-bottom: 30px;
}

.achievements-header h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.achievement-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  transition: all 0.3s;
}

.achievement-card.achieved {
  border-color: #67c23a;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.05) 0%, rgba(103, 194, 58, 0.1) 100%);
}

.achievement-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.achievement-icon {
  flex-shrink: 0;
}

.achievement-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: var(--text-primary);
}

.achievement-info p {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.achievement-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.achievement-progress :deep(.el-progress) {
  flex-grow: 1;
}

.progress-hint {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .student-profile-container {
    padding: 12px;
  }

  .profile-header-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 0 20px 20px;
    margin-top: -60px;
  }

  .quick-actions {
    width: 100%;
    flex-direction: column;
  }

  .achievements-grid {
    grid-template-columns: 1fr;
  }

  .time-cards {
    grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  }
}
</style>
