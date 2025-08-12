<template>
  <div class="stats-dashboard-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>知识点薄弱环节分析</span>
              <el-tooltip content="展示所有知识点中，被答错次数最多的前10个" placement="top">
                <el-icon><Warning /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div ref="kpChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学生个人能力雷达图</span>
              <el-select
                  v-model="selectedStudentId"
                  filterable
                  placeholder="请选择学生进行分析"
                  @change="loadStudentAbilityData"
                  size="small"
                  style="width: 200px;"
              >
                <el-option v-for="s in allStudents" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
              </el-select>
            </div>
          </template>
          <div ref="studentChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>试卷难度分析</span>
              <el-select v-model="selectedPaperId" filterable placeholder="请选择试卷进行分析" @change="loadPaperDifficultyData" size="small" style="width: 300px;">
                <el-option v-for="p in allPapers" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </div>
          </template>
          <div v-if="paperStats">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="试卷名称">{{ paperStats.paperName }}</el-descriptions-item>
              <el-descriptions-item label="参考学生数">{{ paperStats.totalStudents }} 人</el-descriptions-item>
              <el-descriptions-item label="平均错误率">{{ paperStats.averageErrorRate.toFixed(2) }}%</el-descriptions-item>
              <el-descriptions-item label="最难题">{{ paperStats.hardestQuestionContent }}</el-descriptions-item>
              <el-descriptions-item label="最易题">{{ paperStats.easiestQuestionContent }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="请选择一份试卷进行分析" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import { fetchKnowledgePointErrorStats, fetchStudentAbilityStats, fetchPaperDifficultyStats, type PaperDifficulty } from '@/api/statistics';
import { fetchStudentList, type Student } from '@/api/student';
import { fetchPaperList, type Paper } from '@/api/paper';
import { ElMessage } from 'element-plus';
import { Warning } from '@element-plus/icons-vue';

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
  const res = await fetchKnowledgePointErrorStats();
  const chartData = res.data.slice(0, 10); // 只看最薄弱的10个

  if (kpChart.value) {
    kpChartInstance = echarts.init(kpChart.value, 'light', { renderer: 'svg' });
    kpChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: chartData.map(d => d.knowledgePointName), axisLabel: { interval: 0, rotate: 30 } },
      yAxis: { type: 'value', name: '错误人次' },
      series: [{ name: '错误人次', type: 'bar', data: chartData.map(d => d.totalErrors), barWidth: '40%', itemStyle: { color: '#5470c6' } }],
      grid: { bottom: 80, top: 40, left: 50, right: 20 }, // 调整边距以防标签被截断
    });
  }
};

// 加载并渲染学生能力雷达图
const loadStudentAbilityData = async (studentId: number) => {
  if (!studentId) return;
  const res = await fetchStudentAbilityStats(studentId);
  if(studentChart.value) {
    if (!studentChartInstance) {
      studentChartInstance = echarts.init(studentChart.value, 'light', { renderer: 'svg' });
    }
    const maxErrorRate = Math.max(...res.data.errorRates, 5); // 设置雷达图最大值，至少为5
    studentChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { data: ['错误次数'], bottom: 5 },
      radar: {
        indicator: res.data.radarLabels.map(name => ({ name, max: maxErrorRate }))
      },
      series: [{
        name: '学生能力分析',
        type: 'radar',
        data: [{ value: res.data.errorRates, name: '错误次数' }]
      }]
    });
  }
};

// 加载试卷难度数据
const loadPaperDifficultyData = async (paperId: number) => {
  if (!paperId) return;
  const res = await fetchPaperDifficultyStats(paperId);
  paperStats.value = res.data;
}

onMounted(async () => {
  await loadInitialData();
  await loadKpErrorData();
  // 默认加载第一个学生的数据
  if (allStudents.value.length > 0) {
    selectedStudentId.value = allStudents.value[0].id;
    await loadStudentAbilityData(selectedStudentId.value);
  } else {
    // 如果没有学生数据，也初始化一个空的雷达图
    if(studentChart.value) {
      studentChartInstance = echarts.init(studentChart.value, 'light', { renderer: 'svg' });
      studentChartInstance.setOption({ radar: { indicator: [] }, series: [] });
    }
  }
});

</script>

<style scoped>
.stats-dashboard-container {
  padding: 24px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chart-container {
  height: 400px;
  width: 100%;
}
</style>