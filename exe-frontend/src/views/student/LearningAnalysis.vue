<template>
  <div class="learning-analysis-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon :size="32" color="#667eea"><TrendCharts /></el-icon>
        <div class="title-text">
          <h1>📊 学习数据分析</h1>
          <p>AI驱动的学习数据分析，洞察学习规律</p>
        </div>
      </div>
      <div class="header-right">
        <el-select v-model="timeRange" @change="handleTimeRangeChange" style="width: 150px">
          <el-option label="最近7天" value="7days" />
          <el-option label="最近30天" value="30days" />
          <el-option label="最近3个月" value="3months" />
          <el-option label="本学期" value="semester" />
        </el-select>
        <el-button :icon="Refresh" @click="handleRefresh" :loading="loading">刷新</el-button>
        <el-dropdown @command="handleExport" trigger="click">
          <el-button :icon="Download" :loading="exporting">
            导出报告 <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="pdf">
                <el-icon><Document /></el-icon>导出为PDF
              </el-dropdown-item>
              <el-dropdown-item command="excel">
                <el-icon><DocumentCopy /></el-icon>导出为Excel
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="page-content">
      <!-- 顶部统计卡片 -->
      <div class="stats-cards">
        <!-- 骨架屏 -->
        <template v-if="loading">
          <el-card
            v-for="i in 4"
            :key="i"
            class="stat-card"
            :body-style="{ padding: '24px' }"
            shadow="hover"
          >
            <el-skeleton :rows="0" animated>
              <template #template>
                <div style="display: flex; align-items: center; gap: 16px">
                  <el-skeleton-item variant="circle" style="width: 60px; height: 60px" />
                  <div style="flex: 1">
                    <el-skeleton-item variant="text" style="width: 60%; margin-bottom: 8px" />
                    <el-skeleton-item variant="text" style="width: 40%; margin-bottom: 8px" />
                    <el-skeleton-item variant="text" style="width: 30%" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </el-card>
        </template>
        <!-- 实际数据 -->
        <el-card
          v-else
          v-for="stat in topStats"
          :key="stat.label"
          class="stat-card"
          :body-style="{ padding: '24px' }"
          shadow="hover"
        >
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="28"><component :is="stat.icon" /></el-icon>
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
      </div>

      <!-- AI学习建议 -->
      <el-card class="ai-suggestions-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>🤖 AI学习建议</span>
            <el-button text :icon="Refresh" @click="generateAiSuggestions" :loading="generatingAi">
              重新生成
            </el-button>
          </div>
        </template>
        <!-- 骨架屏 -->
        <div v-if="loading" class="ai-suggestions">
          <div v-for="i in 3" :key="i" class="suggestion-item">
            <el-skeleton :rows="3" animated />
          </div>
        </div>
        <!-- 实际数据 -->
        <div v-else-if="aiSuggestions.length > 0" class="ai-suggestions">
          <div
            v-for="(suggestion, index) in aiSuggestions"
            :key="index"
            class="suggestion-item"
          >
            <div class="suggestion-header">
              <el-tag :type="suggestion.type" size="small">{{ suggestion.category }}</el-tag>
              <span class="suggestion-priority">{{ suggestion.priority }}</span>
            </div>
            <p class="suggestion-content">{{ suggestion.content }}</p>
            <div class="suggestion-actions">
              <el-button text size="small" @click="acceptSuggestion(suggestion)">
                采纳建议
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无AI建议，点击重新生成" />
      </el-card>

      <!-- 数据可视化区域 -->
      <el-row :gutter="16">
        <!-- 学习时长趋势 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>⏱️ 学习时长趋势</span>
                <el-tooltip content="展示每日学习时长变化">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <!-- 骨架屏 -->
            <div v-if="loading" class="chart-container">
              <el-skeleton :rows="0" animated>
                <template #template>
                  <el-skeleton-item variant="rect" style="width: 100%; height: 300px" />
                </template>
              </el-skeleton>
            </div>
            <!-- 图表 -->
            <div v-else ref="studyTimeChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 成绩变化趋势 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>📈 成绩变化趋势</span>
                <el-tooltip content="各科成绩变化情况">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <!-- 骨架屏 -->
            <div v-if="loading" class="chart-container">
              <el-skeleton :rows="0" animated>
                <template #template>
                  <el-skeleton-item variant="rect" style="width: 100%; height: 300px" />
                </template>
              </el-skeleton>
            </div>
            <!-- 图表 -->
            <div v-else ref="scoreChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 知识点掌握度雷达图 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>🎯 知识点掌握度</span>
                <el-tooltip content="各知识点掌握情况雷达图">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <!-- 骨架屏 -->
            <div v-if="loading" class="chart-container">
              <el-skeleton :rows="0" animated>
                <template #template>
                  <el-skeleton-item variant="rect" style="width: 100%; height: 300px" />
                </template>
              </el-skeleton>
            </div>
            <!-- 图表 -->
            <div v-else ref="radarChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 错题分析 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>❌ 错题分析</span>
                <el-tooltip content="错题类型分布和错误率">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <!-- 骨架屏 -->
            <div v-if="loading" class="chart-container">
              <el-skeleton :rows="0" animated>
                <template #template>
                  <el-skeleton-item variant="rect" style="width: 100%; height: 300px" />
                </template>
              </el-skeleton>
            </div>
            <!-- 图表 -->
            <div v-else ref="wrongQuestionChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 答题准确率 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>✅ 答题准确率</span>
                <el-tooltip content="各题型答题准确率统计">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <!-- 骨架屏 -->
            <div v-if="loading" class="chart-container">
              <el-skeleton :rows="0" animated>
                <template #template>
                  <el-skeleton-item variant="rect" style="width: 100%; height: 300px" />
                </template>
              </el-skeleton>
            </div>
            <!-- 图表 -->
            <div v-else ref="accuracyChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 学习习惯分析 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>⏰ 学习习惯分析</span>
                <el-tooltip content="每小时学习活跃度">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <!-- 骨架屏 -->
            <div v-if="loading" class="chart-container">
              <el-skeleton :rows="0" animated>
                <template #template>
                  <el-skeleton-item variant="rect" style="width: 100%; height: 300px" />
                </template>
              </el-skeleton>
            </div>
            <!-- 图表 -->
            <div v-else ref="habitChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 薄弱知识点列表 -->
      <el-card class="weak-points-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>🎓 薄弱知识点</span>
            <el-button text @click="generatePractice">生成针对性练习</el-button>
          </div>
        </template>
        <!-- 骨架屏 -->
        <el-skeleton v-if="loading" :rows="5" animated />
        <!-- 实际数据 -->
        <el-table v-else :data="weakKnowledgePoints" style="width: 100%">
          <el-table-column prop="name" label="知识点" min-width="200" />
          <el-table-column prop="subject" label="科目" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ row.subject }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="masteryRate" label="掌握度" width="150">
            <template #default="{ row }">
              <el-progress
                :percentage="row.masteryRate"
                :color="getMasteryColor(row.masteryRate)"
                :stroke-width="8"
              />
            </template>
          </el-table-column>
          <el-table-column prop="wrongCount" label="错题数" width="100" align="center" />
          <el-table-column prop="practiceCount" label="练习次数" width="120" align="center" />
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }">
              <el-button text size="small" @click="reviewKnowledgePoint(row)">
                <el-icon><Reading /></el-icon>
                复习
              </el-button>
              <el-button text size="small" @click="practiceKnowledgePoint(row)">
                <el-icon><Edit /></el-icon>
                练习
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 图表详情对话框 -->
    <el-dialog
      v-model="chartDetailVisible"
      :title="chartDetailTitle"
      width="600px"
    >
      <div class="chart-detail-content">
        <p>{{ chartDetailContent }}</p>
      </div>
      <template #footer>
        <el-button @click="chartDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';
import type { EChartsType } from 'echarts';
import {
  TrendCharts, Refresh, Download, ArrowUp, ArrowDown, ArrowDown as ArrowDownIcon,
  QuestionFilled, Reading, Edit, Clock, Trophy, DocumentCopy, Star, Document
} from '@element-plus/icons-vue';
import request from '@/utils/request';

// ==================== 类型定义 ====================
interface Suggestion {
  category: string;
  type: 'success' | 'warning' | 'danger' | 'info';
  priority: string;
  content: string;
}

interface KnowledgePoint {
  name: string;
  subject: string;
  masteryRate: number;
  wrongCount: number;
  practiceCount: number;
}

// ==================== 状态管理 ====================
// 时间范围
const timeRange = ref('30days');
const loading = ref(false);
const generatingAi = ref(false);
const exporting = ref(false);

// 图表详情对话框
const chartDetailVisible = ref(false);
const chartDetailTitle = ref('');
const chartDetailContent = ref('');

// 顶部统计数据
const topStats = ref([
  {
    label: '学习总时长',
    value: '48.5小时',
    icon: Clock,
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    trend: 12
  },
  {
    label: '完成试卷',
    value: '24份',
    icon: DocumentCopy,
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    trend: 8
  },
  {
    label: '平均正确率',
    value: '85%',
    icon: Star,
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    trend: 5
  },
  {
    label: '获得成就',
    value: '12个',
    icon: Trophy,
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    trend: -2
  }
]);

// AI学习建议
const aiSuggestions = ref<Suggestion[]>([
  {
    category: '数学',
    type: 'warning',
    priority: '高优先级',
    content: '函数与导数部分掌握度较低（65%），建议加强基础概念理解，完成10道相关练习题'
  },
  {
    category: '英语',
    type: 'success',
    priority: '保持',
    content: '阅读理解能力优秀（92%），建议继续保持每日阅读习惯，可以尝试更高难度的文章'
  },
  {
    category: '物理',
    type: 'danger',
    priority: '紧急',
    content: '力学部分错题率高（45%），建议回顾牛顿定律相关知识点，观看教学视频并完成配套练习'
  }
]);

// 薄弱知识点
const weakKnowledgePoints = ref<KnowledgePoint[]>([
  { name: '函数与导数', subject: '数学', masteryRate: 65, wrongCount: 8, practiceCount: 15 },
  { name: '牛顿运动定律', subject: '物理', masteryRate: 55, wrongCount: 12, practiceCount: 10 },
  { name: '电磁感应', subject: '物理', masteryRate: 70, wrongCount: 6, practiceCount: 12 },
  { name: '有机化学', subject: '化学', masteryRate: 68, wrongCount: 7, practiceCount: 14 },
  { name: '阅读理解', subject: '英语', masteryRate: 92, wrongCount: 2, practiceCount: 25 }
]);

// 图表引用
const studyTimeChartRef = ref<HTMLElement>();
const scoreChartRef = ref<HTMLElement>();
const radarChartRef = ref<HTMLElement>();
const wrongQuestionChartRef = ref<HTMLElement>();
const accuracyChartRef = ref<HTMLElement>();
const habitChartRef = ref<HTMLElement>();

// 图表实例存储
const chartInstances = ref<EChartsType[]>([]);

// ==================== 工具函数 ====================
/**
 * 防抖函数
 * @param fn 要防抖的函数
 * @param delay 延迟时间（毫秒）
 */
function debounce<T extends (...args: any[]) => any>(fn: T, delay: number = 300): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null;
  return function (this: any, ...args: Parameters<T>) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
}

/**
 * 获取掌握度颜色
 */
const getMasteryColor = (rate: number): string => {
  if (rate >= 80) return '#67C23A';
  if (rate >= 60) return '#E6A23C';
  return '#F56C6C';
};

// ==================== 数据加载 ====================
/**
 * 加载数据
 */
const loadData = async () => {
  loading.value = true;
  try {
    // 模拟数据加载（1.5秒延迟）
    await new Promise(resolve => setTimeout(resolve, 1500));

    // TODO: 调用后端API获取真实数据
    // const response = await request.get('/api/student/learning-analysis', {
    //   params: { timeRange: timeRange.value }
    // });
    // topStats.value = response.data.stats;
    // aiSuggestions.value = response.data.suggestions;
    // weakKnowledgePoints.value = response.data.weakPoints;

    // 初始化图表
    await nextTick();
    initCharts();

    ElMessage.success('数据加载成功');
  } catch (error) {
    console.error('数据加载失败:', error);
    ElMessage.error('数据加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

/**
 * 处理时间范围变化
 */
const handleTimeRangeChange = () => {
  ElMessage.info(`正在加载${getTimeRangeLabel()}的数据...`);
  loadData();
};

/**
 * 处理手动刷新
 */
const handleRefresh = () => {
  ElMessage.info('正在刷新数据...');
  loadData();
};

/**
 * 获取时间范围标签
 */
const getTimeRangeLabel = (): string => {
  const labels: Record<string, string> = {
    '7days': '最近7天',
    '30days': '最近30天',
    '3months': '最近3个月',
    'semester': '本学期'
  };
  return labels[timeRange.value] || '最近30天';
};

// ==================== AI建议功能 ====================
/**
 * 生成AI建议
 */
const generateAiSuggestions = async () => {
  generatingAi.value = true;
  try {
    // 模拟AI生成过程
    await new Promise(resolve => setTimeout(resolve, 2000));

    // TODO: 调用AI API生成建议
    // const response = await request.post('/api/student/ai-suggestions', {
    //   timeRange: timeRange.value
    // });
    // aiSuggestions.value = response.data;

    ElMessage.success('AI建议已生成');
  } catch (error) {
    console.error('生成AI建议失败:', error);
    ElMessage.error('生成失败，请稍后重试');
  } finally {
    generatingAi.value = false;
  }
};

/**
 * 采纳建议
 */
const acceptSuggestion = (suggestion: Suggestion) => {
  ElMessageBox.confirm(`确定要采纳这条建议吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    ElMessage.success('建议已采纳，已加入学习计划');
  }).catch(() => {});
};

// ==================== 导出功能 ====================
/**
 * 处理导出
 */
const handleExport = async (command: string) => {
  if (command === 'pdf') {
    await exportToPDF();
  } else if (command === 'excel') {
    await exportToExcel();
  }
};

/**
 * 导出为PDF
 */
const exportToPDF = async () => {
  exporting.value = true;
  try {
    ElMessage.info('正在生成PDF报告，请稍候...');

    // 动态导入 html2canvas 和 jspdf
    const html2canvas = (await import('html2canvas')).default;
    const { jsPDF } = await import('jspdf');

    // 获取要导出的内容
    const element = document.querySelector('.page-content') as HTMLElement;
    if (!element) {
      throw new Error('无法找到要导出的内容');
    }

    // 临时移除loading状态以确保完整渲染
    const originalLoading = loading.value;
    loading.value = false;
    await nextTick();

    // 生成canvas
    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      logging: false,
      backgroundColor: '#f5f7fa'
    });

    // 恢复loading状态
    loading.value = originalLoading;

    // 创建PDF
    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4'
    });

    const imgWidth = 210; // A4宽度
    const pageHeight = 297; // A4高度
    const imgHeight = (canvas.height * imgWidth) / canvas.width;
    let heightLeft = imgHeight;
    let position = 0;

    // 添加第一页
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;

    // 如果内容超过一页，添加更多页
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }

    // 下载PDF
    const fileName = `学习分析报告_${new Date().toLocaleDateString()}.pdf`;
    pdf.save(fileName);

    ElMessage.success('PDF报告导出成功');
  } catch (error) {
    console.error('PDF导出失败:', error);
    ElMessage.error('PDF导出失败，请稍后重试');
  } finally {
    exporting.value = false;
  }
};

/**
 * 导出为Excel
 */
const exportToExcel = async () => {
  exporting.value = true;
  try {
    ElMessage.info('正在生成Excel报告，请稍候...');

    // 动态导入 xlsx
    const XLSX = await import('xlsx');

    // 准备数据
    const worksheetData = [
      // 表头
      ['知识点', '科目', '掌握度(%)', '错题数', '练习次数'],
      // 数据行
      ...weakKnowledgePoints.value.map(point => [
        point.name,
        point.subject,
        point.masteryRate,
        point.wrongCount,
        point.practiceCount
      ])
    ];

    // 创建工作表
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);

    // 设置列宽
    worksheet['!cols'] = [
      { wch: 20 }, // 知识点
      { wch: 10 }, // 科目
      { wch: 12 }, // 掌握度
      { wch: 10 }, // 错题数
      { wch: 12 }  // 练习次数
    ];

    // 创建工作簿
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, '学习数据');

    // 下载Excel
    const fileName = `学习数据报告_${new Date().toLocaleDateString()}.xlsx`;
    XLSX.writeFile(workbook, fileName);

    ElMessage.success('Excel报告导出成功');
  } catch (error) {
    console.error('Excel导出失败:', error);
    ElMessage.error('Excel导出失败，请稍后重试');
  } finally {
    exporting.value = false;
  }
};

// ==================== 图表初始化 ====================
/**
 * 初始化所有图表
 */
const initCharts = () => {
  // 清除旧的图表实例
  chartInstances.value.forEach(chart => chart.dispose());
  chartInstances.value = [];

  // 初始化新图表
  initStudyTimeChart();
  initScoreChart();
  initRadarChart();
  initWrongQuestionChart();
  initAccuracyChart();
  initHabitChart();
};

/**
 * 显示图表详情
 */
const showChartDetail = (title: string, content: string) => {
  chartDetailTitle.value = title;
  chartDetailContent.value = content;
  chartDetailVisible.value = true;
};

// ==================== 图表配置 ====================
/**
 * 学习时长趋势图
 */
const initStudyTimeChart = () => {
  if (!studyTimeChartRef.value) return;

  const chart = echarts.init(studyTimeChartRef.value);
  chartInstances.value.push(chart);

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const data = params[0];
        return `${data.name}<br/>学习时长: ${data.value}分钟`;
      }
    },
    toolbox: {
      feature: {
        saveAsImage: { title: '保存为图片' },
        dataView: {
          title: '数据视图',
          readOnly: false,
          lang: ['数据视图', '关闭', '刷新']
        },
        restore: { title: '重置' }
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value',
      name: '分钟'
    },
    series: [{
      name: '学习时长',
      type: 'bar',
      data: [120, 90, 150, 75, 110, 180, 95],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(102, 126, 234, 0.5)'
        }
      }
    }]
  });

  // 添加点击事件
  chart.on('click', (params: any) => {
    showChartDetail(
      '学习时长详情',
      `${params.name}的学习时长为 ${params.value} 分钟`
    );
  });
};

/**
 * 成绩趋势图
 */
const initScoreChart = () => {
  if (!scoreChartRef.value) return;

  const chart = echarts.init(scoreChartRef.value);
  chartInstances.value.push(chart);

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let result = `${params[0].name}<br/>`;
        params.forEach((param: any) => {
          result += `${param.marker}${param.seriesName}: ${param.value}分<br/>`;
        });
        return result;
      }
    },
    toolbox: {
      feature: {
        saveAsImage: { title: '保存为图片' },
        dataView: {
          title: '数据视图',
          readOnly: false,
          lang: ['数据视图', '关闭', '刷新']
        },
        restore: { title: '重置' }
      }
    },
    legend: { data: ['数学', '英语', '物理'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['第1次', '第2次', '第3次', '第4次', '第5次']
    },
    yAxis: { type: 'value', min: 0, max: 100, name: '分数' },
    series: [
      {
        name: '数学',
        type: 'line',
        smooth: true,
        data: [75, 82, 85, 88, 92],
        itemStyle: { color: '#409eff' },
        emphasis: { focus: 'series' }
      },
      {
        name: '英语',
        type: 'line',
        smooth: true,
        data: [80, 78, 85, 87, 90],
        itemStyle: { color: '#67c23a' },
        emphasis: { focus: 'series' }
      },
      {
        name: '物理',
        type: 'line',
        smooth: true,
        data: [70, 75, 72, 78, 82],
        itemStyle: { color: '#e6a23c' },
        emphasis: { focus: 'series' }
      }
    ]
  });

  // 添加点击事件
  chart.on('click', (params: any) => {
    showChartDetail(
      '成绩详情',
      `${params.seriesName} - ${params.name}: ${params.value}分`
    );
  });
};

/**
 * 知识点掌握度雷达图
 */
const initRadarChart = () => {
  if (!radarChartRef.value) return;

  const chart = echarts.init(radarChartRef.value);
  chartInstances.value.push(chart);

  chart.setOption({
    tooltip: {
      formatter: (params: any) => {
        const data = params.value;
        const indicators = ['函数', '几何', '代数', '统计', '概率'];
        let result = '知识点掌握度<br/>';
        indicators.forEach((indicator, index) => {
          result += `${indicator}: ${data[index]}%<br/>`;
        });
        return result;
      }
    },
    toolbox: {
      feature: {
        saveAsImage: { title: '保存为图片' },
        restore: { title: '重置' }
      }
    },
    radar: {
      indicator: [
        { name: '函数', max: 100 },
        { name: '几何', max: 100 },
        { name: '代数', max: 100 },
        { name: '统计', max: 100 },
        { name: '概率', max: 100 }
      ]
    },
    series: [{
      type: 'radar',
      data: [{
        value: [65, 85, 78, 90, 82],
        name: '掌握度',
        areaStyle: {
          color: 'rgba(102, 126, 234, 0.3)'
        },
        itemStyle: { color: '#667eea' },
        emphasis: {
          lineStyle: { width: 4 }
        }
      }]
    }]
  });

  // 添加点击事件
  chart.on('click', () => {
    showChartDetail(
      '知识点掌握度',
      '点击查看各知识点详细掌握情况'
    );
  });
};

/**
 * 错题分析图
 */
const initWrongQuestionChart = () => {
  if (!wrongQuestionChartRef.value) return;

  const chart = echarts.init(wrongQuestionChartRef.value);
  chartInstances.value.push(chart);

  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    toolbox: {
      feature: {
        saveAsImage: { title: '保存为图片' }
      }
    },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '错题类型',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 35, name: '计算错误' },
        { value: 28, name: '概念理解' },
        { value: 22, name: '粗心大意' },
        { value: 15, name: '其他' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      label: {
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  });

  // 添加点击事件
  chart.on('click', (params: any) => {
    showChartDetail(
      '错题类型详情',
      `${params.name}: ${params.value}道题 (${params.percent.toFixed(1)}%)`
    );
  });
};

/**
 * 答题准确率图
 */
const initAccuracyChart = () => {
  if (!accuracyChartRef.value) return;

  const chart = echarts.init(accuracyChartRef.value);
  chartInstances.value.push(chart);

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const data = params[0];
        return `${data.name}<br/>准确率: ${data.value}%`;
      }
    },
    toolbox: {
      feature: {
        saveAsImage: { title: '保存为图片' },
        dataView: {
          title: '数据视图',
          readOnly: false,
          lang: ['数据视图', '关闭', '刷新']
        },
        restore: { title: '重置' }
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', max: 100, name: '准确率(%)' },
    yAxis: {
      type: 'category',
      data: ['选择题', '填空题', '计算题', '应用题', '证明题']
    },
    series: [{
      name: '准确率',
      type: 'bar',
      data: [92, 85, 78, 82, 75],
      itemStyle: {
        color: (params: any) => {
          const colors = ['#67C23A', '#E6A23C', '#F56C6C'];
          if (params.value >= 85) return colors[0];
          if (params.value >= 70) return colors[1];
          return colors[2];
        }
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      }
    }]
  });

  // 添加点击事件
  chart.on('click', (params: any) => {
    showChartDetail(
      '答题准确率详情',
      `${params.name}的准确率为 ${params.value}%`
    );
  });
};

/**
 * 学习习惯分析图
 */
const initHabitChart = () => {
  if (!habitChartRef.value) return;

  const chart = echarts.init(habitChartRef.value);
  chartInstances.value.push(chart);

  const hours = [];
  const activityData = [];

  for (let i = 0; i < 24; i++) {
    hours.push(`${i}:00`);
    // 模拟数据：早上8点-12点、下午14点-18点、晚上19点-22点活跃
    let activity = 0;
    if ((i >= 8 && i <= 12) || (i >= 14 && i <= 18) || (i >= 19 && i <= 22)) {
      activity = Math.floor(Math.random() * 30) + 20;
    } else {
      activity = Math.floor(Math.random() * 10);
    }
    activityData.push(activity);
  }

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0];
        return `${data.name}<br/>活跃度: ${data.value}`;
      }
    },
    toolbox: {
      feature: {
        saveAsImage: { title: '保存为图片' },
        dataView: {
          title: '数据视图',
          readOnly: false,
          lang: ['数据视图', '关闭', '刷新']
        },
        restore: { title: '重置' },
        dataZoom: { title: { zoom: '区域缩放', back: '还原' } }
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: hours,
      axisLabel: {
        interval: 2
      }
    },
    yAxis: {
      type: 'value',
      name: '活跃度'
    },
    series: [{
      name: '活跃度',
      type: 'line',
      smooth: true,
      data: activityData,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
        ])
      },
      itemStyle: { color: '#667eea' },
      emphasis: {
        focus: 'series',
        itemStyle: {
          borderColor: '#667eea',
          borderWidth: 2
        }
      }
    }]
  });

  // 添加点击事件
  chart.on('click', (params: any) => {
    showChartDetail(
      '学习习惯详情',
      `${params.name}的学习活跃度为 ${params.value}`
    );
  });
};

// ==================== 响应式调整 ====================
/**
 * 调整所有图表大小
 */
const resizeCharts = () => {
  chartInstances.value.forEach(chart => {
    if (chart && !chart.isDisposed()) {
      chart.resize();
    }
  });
};

// 使用防抖优化resize性能
const debouncedResize = debounce(resizeCharts, 300);

// ==================== 其他操作 ====================
/**
 * 生成针对性练习
 */
const generatePractice = () => {
  ElMessage.info('正在为您生成针对性练习...');
  // TODO: 调用后端API生成练习
};

/**
 * 复习知识点
 */
const reviewKnowledgePoint = (point: KnowledgePoint) => {
  ElMessage.success(`开始复习：${point.name}`);
  // TODO: 跳转到复习页面
};

/**
 * 练习知识点
 */
const practiceKnowledgePoint = (point: KnowledgePoint) => {
  ElMessage.success(`开始练习：${point.name}`);
  // TODO: 跳转到练习页面
};

// ==================== 生命周期 ====================
onMounted(() => {
  // 初始加载数据
  loadData();

  // 添加窗口resize监听
  window.addEventListener('resize', debouncedResize);
});

onUnmounted(() => {
  // 移除resize监听
  window.removeEventListener('resize', debouncedResize);

  // 销毁所有图表实例
  chartInstances.value.forEach(chart => {
    if (chart && !chart.isDisposed()) {
      chart.dispose();
    }
  });
  chartInstances.value = [];
});
</script>

<style scoped>
.learning-analysis-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: white;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-text p {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 主要内容 */
.page-content {
  padding: 20px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
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
  flex: 1;
}

.stat-value {
  font-size: 28px;
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
  font-size: 14px;
  font-weight: 500;
}

/* AI建议卡片 */
.ai-suggestions-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.ai-suggestions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.suggestion-item {
  padding: 16px;
  background: linear-gradient(135deg, #667eea11 0%, #764ba211 100%);
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.suggestion-priority {
  font-size: 12px;
  color: var(--text-secondary);
}

.suggestion-content {
  margin: 0 0 12px 0;
  line-height: 1.6;
  color: var(--text-primary);
}

.suggestion-actions {
  display: flex;
  justify-content: flex-end;
}

/* 图表卡片 */
.chart-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.chart-container {
  height: 300px;
}

/* 薄弱知识点卡片 */
.weak-points-card {
  border-radius: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
  }

  .header-left,
  .header-right {
    width: 100%;
  }

  .header-right {
    flex-wrap: wrap;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .ai-suggestions {
    grid-template-columns: 1fr;
  }

  .chart-container {
    height: 250px;
  }
}
</style>
