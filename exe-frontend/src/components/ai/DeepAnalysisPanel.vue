<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="90%"
    :fullscreen="isMobile"
    class="deep-analysis-dialog"
    @close="handleClose"
  >
    <!-- 加载状态 -->
    <div v-if="analyzing" class="analyzing-state">
      <el-icon class="is-loading" :size="48"><Loading /></el-icon>
      <p class="analyzing-text">AI正在深度分析中，请稍候...</p>
      <p class="analyzing-hint">正在分析学生画像、知识图谱和错因...</p>
    </div>

    <!-- 分析结果 -->
    <div v-else-if="analysisResult" class="analysis-content">
      <!-- 顶部统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="isMobile ? 24 : 6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon :size="24" color="#409EFF"><TrendCharts /></el-icon>
              <div class="stat-info">
                <div class="stat-label">分析置信度</div>
                <div class="stat-value">{{ (analysisResult.confidence * 100).toFixed(0) }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="isMobile ? 24 : 6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon :size="24" color="#F56C6C"><Warning /></el-icon>
              <div class="stat-info">
                <div class="stat-label">错误类型</div>
                <div class="stat-value">{{ analysisResult.errorType || '未分类' }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="isMobile ? 24 : 6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon :size="24" color="#67C23A"><Reading /></el-icon>
              <div class="stat-info">
                <div class="stat-label">涉及知识点</div>
                <div class="stat-value">{{ analysisResult.knowledgePoints?.length || 0 }} 个</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="isMobile ? 24 : 6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon :size="24" color="#E6A23C"><Guide /></el-icon>
              <div class="stat-info">
                <div class="stat-label">学习步骤</div>
                <div class="stat-value">{{ analysisResult.learningPath?.length || 0 }} 步</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 主要内容标签页 -->
      <el-tabs v-model="activeTab" class="analysis-tabs">
        <!-- Tab 1: AI深度分析 -->
        <el-tab-pane label="🤖 AI深度分析" name="analysis">
          <el-card shadow="never" class="analysis-card">
            <template #header>
              <div class="card-header">
                <span>深度分析报告</span>
                <el-button text @click="copyAnalysis">
                  <el-icon><DocumentCopy /></el-icon>
                  复制分析
                </el-button>
              </div>
            </template>

            <!-- Markdown渲染 -->
            <div class="markdown-content" v-html="renderedMarkdown"></div>
          </el-card>

          <!-- 建议列表 -->
          <el-card v-if="analysisResult.suggestions?.length > 0" shadow="never" class="suggestions-card">
            <template #header>
              <span>💡 个性化建议</span>
            </template>
            <ul class="suggestions-list">
              <li v-for="(suggestion, index) in analysisResult.suggestions" :key="index">
                <el-icon color="#409EFF"><Check /></el-icon>
                <span>{{ suggestion }}</span>
              </li>
            </ul>
          </el-card>
        </el-tab-pane>

        <!-- Tab 2: 学习路径 -->
        <el-tab-pane label="🎯 学习路径" name="path">
          <el-card shadow="never" class="path-card">
            <template #header>
              <span>个性化学习路径</span>
            </template>

            <el-empty v-if="!analysisResult.learningPath || analysisResult.learningPath.length === 0"
                      description="暂无学习路径规划" />

            <el-steps v-else direction="vertical" :active="0">
              <el-step
                v-for="(step, index) in analysisResult.learningPath"
                :key="index"
                :title="step.title"
                :description="step.description"
              >
                <template #icon>
                  <el-icon v-if="step.type === 'REVIEW'" color="#409EFF"><Reading /></el-icon>
                  <el-icon v-else-if="step.type === 'PRACTICE'" color="#67C23A"><Edit /></el-icon>
                  <el-icon v-else color="#E6A23C"><Trophy /></el-icon>
                </template>
              </el-step>
            </el-steps>
          </el-card>
        </el-tab-pane>

        <!-- Tab 3: 学生画像 -->
        <el-tab-pane label="👤 学生画像" name="profile">
          <el-card shadow="never" class="profile-card">
            <template #header>
              <div class="card-header">
                <span>我的学习画像</span>
                <el-button text @click="refreshProfile">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>

            <div v-if="studentProfile" class="profile-content">
              <el-descriptions :column="isMobile ? 1 : 2" border>
                <el-descriptions-item label="整体水平">
                  <el-tag :type="getLevelType(studentProfile.level)">
                    {{ studentProfile.level }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="学习风格">
                  {{ studentProfile.learningStyle }}
                </el-descriptions-item>
                <el-descriptions-item label="平均正确率">
                  <el-progress :percentage="(studentProfile.averageAccuracy * 100).toFixed(0)" />
                </el-descriptions-item>
                <el-descriptions-item label="总答题数">
                  {{ studentProfile.totalQuestionsDone }} 题
                </el-descriptions-item>
                <el-descriptions-item label="薄弱知识点" :span="2">
                  <el-tag
                    v-for="point in studentProfile.weakPoints"
                    :key="point"
                    type="danger"
                    style="margin-right: 8px;"
                  >
                    {{ point }}
                  </el-tag>
                  <span v-if="studentProfile.weakPoints.length === 0" style="color: #909399;">
                    暂无明显薄弱点
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="擅长知识点" :span="2">
                  <el-tag
                    v-for="point in studentProfile.strongPoints"
                    :key="point"
                    type="success"
                    style="margin-right: 8px;"
                  >
                    {{ point }}
                  </el-tag>
                  <span v-if="studentProfile.strongPoints.length === 0" style="color: #909399;">
                    继续努力，发现你的强项！
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="常见错误" :span="2">
                  {{ studentProfile.commonMistakeType }}
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div v-else class="profile-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载学习画像中...</span>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="errorMessage" class="error-state">
      <el-result icon="error" title="分析失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="handleClose">关闭</el-button>
        </template>
      </el-result>
    </div>

    <!-- 底部操作栏 -->
    <template #footer v-if="analysisResult">
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleSaveAnalysis">
          <el-icon><Collection /></el-icon>
          收藏分析
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Loading, TrendCharts, Warning, Reading, Guide, DocumentCopy,
  Check, Edit, Trophy, Refresh, Collection
} from '@element-plus/icons-vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import request from '@/utils/request';
import { marked } from 'marked';
import { useResponsive } from '@/composables/useResponsive';

interface DeepAnalysisResult {
  fullAnalysis: string;
  knowledgePoints: string[];
  errorType: string;
  suggestions: string[];
  learningPath: LearningPathStep[];
  confidence: number;
}

interface LearningPathStep {
  title: string;
  description: string;
  type: 'REVIEW' | 'PRACTICE' | 'IMPROVE';
  resourceId?: number;
  resourceType?: string;
  order: number;
}

interface StudentProfile {
  userId: number;
  level: string;
  learningStyle: string;
  weakPoints: string[];
  strongPoints: string[];
  averageAccuracy: number;
  totalQuestionsDone: number;
  commonMistakeType: string;
}

const props = defineProps<{
  modelValue: boolean;
  questionData?: {
    questionId?: number;
    questionContent: string;
    studentAnswer: string;
    correctAnswer: string;
    analysis?: string;
    knowledgePointId?: number;
  };
}>();

const emit = defineEmits(['update:modelValue']);

const studentAuth = useStudentAuthStore();
const { isMobile } = useResponsive();

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const analyzing = ref(false);
const analysisResult = ref<DeepAnalysisResult | null>(null);
const studentProfile = ref<StudentProfile | null>(null);
const errorMessage = ref('');
const activeTab = ref('analysis');

const dialogTitle = computed(() => {
  return analyzing.value ? 'AI深度分析中...' : '🎓 错因深度分析报告';
});

// Markdown渲染
const renderedMarkdown = computed(() => {
  if (!analysisResult.value?.fullAnalysis) return '';
  return marked(analysisResult.value.fullAnalysis);
});

// 监听对话框打开，触发分析
watch(() => props.modelValue, (newVal) => {
  if (newVal && props.questionData) {
    performDeepAnalysis();
    loadStudentProfile();
  }
});

/**
 * 执行深度分析
 */
const performDeepAnalysis = async () => {
  if (!props.questionData) return;

  analyzing.value = true;
  analysisResult.value = null;
  errorMessage.value = '';

  try {
    const res = await request({
      url: '/api/v1/student/deep-analysis/analyze',
      method: 'post',
      data: {
        questionId: props.questionData.questionId,
        questionContent: props.questionData.questionContent,
        studentAnswer: props.questionData.studentAnswer,
        correctAnswer: props.questionData.correctAnswer,
        analysis: props.questionData.analysis,
        knowledgePointId: props.questionData.knowledgePointId
      },
      headers: {
        'X-Ai-Api-Key': studentAuth.student?.aiKey,
        'X-Ai-Provider': studentAuth.student?.aiProvider || 'deepseek'
      }
    });

    if (res.code === 200) {
      analysisResult.value = res.data;
    } else {
      errorMessage.value = res.message || '分析失败';
    }
  } catch (error: any) {
    errorMessage.value = error.message || '网络请求失败';
  } finally {
    analyzing.value = false;
  }
};

/**
 * 加载学生画像
 */
const loadStudentProfile = async () => {
  try {
    const res = await request({
      url: '/api/v1/student/deep-analysis/profile',
      method: 'get'
    });

    if (res.code === 200) {
      studentProfile.value = res.data;
    }
  } catch (error) {
    console.error('加载学生画像失败:', error);
  }
};

/**
 * 刷新学生画像
 */
const refreshProfile = () => {
  studentProfile.value = null;
  loadStudentProfile();
};

/**
 * 复制分析结果
 */
const copyAnalysis = () => {
  if (!analysisResult.value?.fullAnalysis) return;

  navigator.clipboard.writeText(analysisResult.value.fullAnalysis)
    .then(() => {
      ElMessage.success('分析内容已复制到剪贴板');
    })
    .catch(() => {
      ElMessage.error('复制失败');
    });
};

/**
 * 保存分析结果
 */
const handleSaveAnalysis = () => {
  // TODO: 实现保存功能
  ElMessage.success('分析已收藏');
};

/**
 * 关闭对话框
 */
const handleClose = () => {
  visible.value = false;
  activeTab.value = 'analysis';
};

/**
 * 获取水平标签类型
 */
const getLevelType = (level: string) => {
  if (level.includes('优秀')) return 'success';
  if (level.includes('良好')) return '';
  if (level.includes('中等')) return 'warning';
  return 'info';
};
</script>

<style scoped>
.deep-analysis-dialog {
  min-height: 600px;
}

.analyzing-state {
  text-align: center;
  padding: 80px 20px;
}

.analyzing-text {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin-top: 20px;
}

.analyzing-hint {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.analysis-tabs {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.markdown-content {
  line-height: 1.8;
  font-size: 15px;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3) {
  margin-top: 20px;
  margin-bottom: 12px;
  color: #303133;
}

.markdown-content :deep(h1) {
  font-size: 24px;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 8px;
}

.markdown-content :deep(h2) {
  font-size: 20px;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.markdown-content :deep(h3) {
  font-size: 18px;
}

.markdown-content :deep(p) {
  margin: 12px 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.markdown-content :deep(li) {
  margin: 8px 0;
}

.markdown-content :deep(code) {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.markdown-content :deep(strong) {
  color: #409EFF;
  font-weight: 600;
}

.suggestions-card {
  margin-top: 20px;
}

.suggestions-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestions-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.suggestions-list li:hover {
  background-color: #e6f0ff;
  transform: translateX(4px);
}

.suggestions-list li span {
  flex: 1;
  line-height: 1.6;
}

.path-card :deep(.el-step__description) {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.profile-content {
  margin-top: 20px;
}

.profile-loading {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.profile-loading span {
  margin-left: 8px;
}

.error-state {
  padding: 40px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .stats-row {
    margin-bottom: 12px;
  }

  .stat-value {
    font-size: 16px;
  }

  .markdown-content {
    font-size: 14px;
  }

  .markdown-content :deep(h1) {
    font-size: 20px;
  }

  .markdown-content :deep(h2) {
    font-size: 18px;
  }

  .markdown-content :deep(h3) {
    font-size: 16px;
  }
}
</style>
