<template>
  <div class="stats-dashboard-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><DataAnalysis /></el-icon>
        </div>
        <div>
          <h2>教学统计分析</h2>
          <p>全面分析教学数据，洞察学习趋势与薄弱环节</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" @click="handleRefresh" :loading="loading">刷新数据</el-button>
        <el-button icon="Download" type="primary" plain @click="handleExport">导出报告</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-students">
          <div class="stat-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">学生总数</div>
            <div class="stat-value">{{ allStudents.length }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-papers">
          <div class="stat-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">试卷总数</div>
            <div class="stat-value">{{ allPapers.length }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-knowledge">
          <div class="stat-icon">
            <el-icon :size="32"><Reading /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">知识点数</div>
            <div class="stat-value">{{ knowledgePointCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-accuracy">
          <div class="stat-icon">
            <el-icon :size="32"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">平均正确率</div>
            <div class="stat-value">{{ averageAccuracy }}%</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第一行图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><Warning /></el-icon>
                <span class="header-title">知识点薄弱环节分析</span>
              </div>
              <el-tooltip content="展示所有知识点中，被答错次数最多的前10个" placement="top">
                <el-icon class="info-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div ref="kpChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><DataAnalysis /></el-icon>
                <span class="header-title">学生个人能力雷达图</span>
              </div>
              <el-select
                  v-model="selectedStudentId"
                  filterable
                  placeholder="请选择学生进行分析"
                  @change="loadStudentAbilityData"
                  size="small"
                  style="width: 220px;"
              >
                <el-option v-for="s in allStudents" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
              </el-select>
            </div>
          </template>
          <div ref="studentChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：试卷难度分析 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><Histogram /></el-icon>
                <span class="header-title">试卷难度分析</span>
              </div>
              <el-select
                v-model="selectedPaperId"
                filterable
                placeholder="请选择试卷进行分析"
                @change="loadPaperDifficultyData"
                size="small"
                style="width: 300px;"
              >
                <el-option v-for="p in allPapers" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </div>
          </template>
          <div v-if="paperStats" class="paper-stats-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="info-card">
                  <div class="info-icon">
                    <el-icon :size="24"><Document /></el-icon>
                  </div>
                  <div class="info-content">
                    <div class="info-label">试卷名称</div>
                    <div class="info-value">{{ paperStats.paperName }}</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-card">
                  <div class="info-icon">
                    <el-icon :size="24"><User /></el-icon>
                  </div>
                  <div class="info-content">
                    <div class="info-label">参考学生数</div>
                    <div class="info-value">{{ paperStats.totalStudents }} 人</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-card">
                  <div class="info-icon">
                    <el-icon :size="24"><TrendCharts /></el-icon>
                  </div>
                  <div class="info-content">
                    <div class="info-label">平均错误率</div>
                    <div class="info-value error-rate">{{ paperStats.averageErrorRate.toFixed(2) }}%</div>
                  </div>
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="question-info">
                  <div class="question-label">
                    <el-icon color="#f56c6c"><WarningFilled /></el-icon>
                    <span>最难题目</span>
                  </div>
                  <div class="question-content difficult">{{ paperStats.hardestQuestionContent }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="question-info">
                  <div class="question-label">
                    <el-icon color="#67c23a"><SuccessFilled /></el-icon>
                    <span>最易题目</span>
                  </div>
                  <div class="question-content easy">{{ paperStats.easiestQuestionContent }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
          <el-empty v-else description="请选择一份试卷进行分析" :image-size="120"></el-empty>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import * as echarts from 'echarts';
import { fetchKnowledgePointErrorStats, fetchStudentAbilityStats, fetchPaperDifficultyStats, type PaperDifficulty } from '@/api/statistics';
import { fetchStudentList, type Student } from '@/api/student';
import { fetchPaperList, type Paper } from '@/api/paper';
import { ElMessage } from 'element-plus';
import {
  DataAnalysis, Warning, Histogram, User, Document, Reading,
  TrendCharts, QuestionFilled, WarningFilled, SuccessFilled, Refresh, Download
} from '@element-plus/icons-vue';

// ECharts 实例引用
const kpChart = ref<HTMLElement | null>(null);
const studentChart = ref<HTMLElement | null>(null);
let kpChartInstance: echarts.ECharts | null = null;
let studentChartInstance: echarts.ECharts | null = null;

// 下拉框数据
const allStudents = ref<Student[]>([]);
const allPapers = ref<Paper[]>([]);

// 选中项ID
const selectedStudentId = ref<number | null>(null);
const selectedPaperId = ref<number | null>(null);

// 分析结果数据
const paperStats = ref<PaperDifficulty | null>(null);
const loading = ref(false);

// 统计数据计算
const knowledgePointCount = computed(() => {
  // 这里可以从API获取，暂时返回模拟数据
  return 156;
});

const averageAccuracy = computed(() => {
  // 这里可以从API获取，暂时返回模拟数据
  return 78.5;
});

// 加载所有下拉框所需的基础数据
const loadInitialData = async () => {
  try {
    const [studentRes, paperRes] = await Promise.all([
      fetchStudentList({ current: 1, size: 9999 }),
      fetchPaperList({ current: 1, size: 9999 })
    ]);
    allStudents.value = studentRes.data;
    allPapers.value = paperRes.data;
  } catch (error) {
    ElMessage.error('加载基础数据失败');
  }
};

// 加载并渲染知识点错误图表
const loadKpErrorData = async () => {
  try {
    const res = await fetchKnowledgePointErrorStats();
    const chartData = res.data.slice(0, 10);

  if (kpChart.value) {
    kpChartInstance = echarts.init(kpChart.value, 'light', { renderer: 'svg' });
    kpChartInstance.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      xAxis: {
        type: 'category',
        data: chartData.map(d => d.knowledgePointName),
        axisLabel: {
          interval: 0,
          rotate: 30,
          fontSize: 11
        }
      },
      yAxis: {
        type: 'value',
        name: '错误人次',
        nameTextStyle: { fontSize: 12 }
      },
      series: [{
        name: '错误人次',
        type: 'bar',
        data: chartData.map(d => d.totalErrors),
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f56c6c' },
            { offset: 1, color: '#f78989' }
          ])
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 10
        }
      }],
      grid: { bottom: 80, top: 40, left: 60, right: 20 }
    });
  }
  } catch (error) {
    console.error('加载知识点错误统计失败:', error);
    ElMessage.error('加载知识点统计失败');
  }
};

// 加载并渲染学生能力雷达图
const loadStudentAbilityData = async (studentId: number) => {
  if (!studentId) return;
  try {
    const res = await fetchStudentAbilityStats(studentId);
  if (studentChart.value) {
    if (!studentChartInstance) {
      studentChartInstance = echarts.init(studentChart.value, 'light', { renderer: 'svg' });
    }
    const maxErrorRate = Math.max(...res.data.errorRates, 5);
    studentChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { data: ['错误次数'], bottom: 5 },
      radar: {
        indicator: res.data.radarLabels.map(name => ({ name, max: maxErrorRate })),
        radius: '65%'
      },
      series: [{
        name: '学生能力分析',
        type: 'radar',
        data: [{
          value: res.data.errorRates,
          name: '错误次数',
          areaStyle: {
            color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ])
          },
          lineStyle: { color: '#409eff' },
          itemStyle: { color: '#409eff' }
        }]
      }]
    });
  }
  } catch (error) {
    console.error('加载学生能力数据失败:', error);
    ElMessage.error('加载学生能力数据失败');
  }
};

// 加载试卷难度数据
const loadPaperDifficultyData = async (paperId: number) => {
  if (!paperId) return;
  try {
    const res = await fetchPaperDifficultyStats(paperId);
    paperStats.value = res.data;
  } catch (error) {
    console.error('加载试卷难度数据失败:', error);
    ElMessage.error('加载试卷难度数据失败');
  }
};

// 刷新数据
const handleRefresh = async () => {
  loading.value = true;
  try {
    await loadInitialData();
    await loadKpErrorData();
    if (selectedStudentId.value) {
      await loadStudentAbilityData(selectedStudentId.value);
    }
    if (selectedPaperId.value) {
      await loadPaperDifficultyData(selectedPaperId.value);
    }
    ElMessage.success('数据刷新成功');
  } catch (error) {
    ElMessage.error('数据刷新失败');
  } finally {
    loading.value = false;
  }
};

// 导出报告
const handleExport = () => {
  ElMessage.info('导出功能开发中...');
};

onMounted(async () => {
  await loadInitialData();
  await loadKpErrorData();
  if (allStudents.value.length > 0) {
    selectedStudentId.value = allStudents.value[0].id;
    await loadStudentAbilityData(selectedStudentId.value);
  } else {
    if (studentChart.value) {
      studentChartInstance = echarts.init(studentChart.value, 'light', { renderer: 'svg' });
      studentChartInstance.setOption({ radar: { indicator: [] }, series: [] });
    }
  }
});

</script>

<style scoped>
/* 页面容器 */
.stats-dashboard-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #303133;
}

.page-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

/* 统计卡片颜色 */
.stat-students::before { background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); }
.stat-students .stat-icon {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}
.stat-students .stat-value { color: #667eea; }

.stat-papers::before { background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%); }
.stat-papers .stat-icon {
  background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(245, 87, 108, 0.1) 100%);
  color: #f093fb;
}
.stat-papers .stat-value { color: #f093fb; }

.stat-knowledge::before { background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%); }
.stat-knowledge .stat-icon {
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.1) 0%, rgba(0, 242, 254, 0.1) 100%);
  color: #4facfe;
}
.stat-knowledge .stat-value { color: #4facfe; }

.stat-accuracy::before { background: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%); }
.stat-accuracy .stat-icon {
  background: linear-gradient(135deg, rgba(67, 233, 123, 0.1) 0%, rgba(56, 249, 215, 0.1) 100%);
  color: #43e97b;
}
.stat-accuracy .stat-value { color: #43e97b; }

/* 图表行 */
.chart-row {
  margin-bottom: 20px;
}

/* 图表卡片 */
.chart-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  color: #409eff;
  font-size: 18px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.info-icon {
  color: #909399;
  cursor: help;
  font-size: 16px;
}

.chart-container {
  height: 400px;
  width: 100%;
}

/* 试卷难度分析 */
.paper-stats-content {
  padding: 20px 0;
}

.info-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.info-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.info-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.info-value.error-rate {
  color: #f56c6c;
}

.question-info {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.question-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.question-content {
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
  padding: 12px;
  border-radius: 6px;
  background: white;
}

.question-content.difficult {
  border-left: 3px solid #f56c6c;
}

.question-content.easy {
  border-left: 3px solid #67c23a;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .stats-dashboard-container {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-value {
    font-size: 24px;
  }

  .chart-container {
    height: 300px;
  }

  .info-card {
    padding: 16px;
  }

  .info-icon {
    width: 40px;
    height: 40px;
  }

  .info-value {
    font-size: 18px;
  }
}
</style>
