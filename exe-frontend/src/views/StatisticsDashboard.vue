<template>
  <div class="stats-dashboard-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>知识点薄弱环节分析</template>
          <div ref="kpChart" style="height: 400px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学生个人能力分析</span>
              <el-select v-model="selectedStudentId" filterable placeholder="请选择学生" @change="loadStudentAbilityData" size="small">
                <el-option v-for="s in allStudents" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
              </el-select>
            </div>
          </template>
          <div ref="studentChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>试卷难度分析</span>
              <el-select v-model="selectedPaperId" filterable placeholder="请选择试卷" @change="loadPaperDifficultyData" size="small">
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

// -- Refs for Charts --
const kpChart = ref<HTMLElement | null>(null);
const studentChart = ref<HTMLElement | null>(null);

// -- Data for Selectors --
const allStudents = ref<Student[]>([]);
const allPapers = ref<Paper[]>([]);
const selectedStudentId = ref<number | null>(null);
const selectedPaperId = ref<number | null>(null);

// -- Data for Display --
const paperStats = ref<PaperDifficulty | null>(null);

// -- ECharts Instances --
let kpChartInstance: echarts.ECharts | null = null;
let studentChartInstance: echarts.ECharts | null = null;


// --- Data Loading Functions ---
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

const loadKpErrorData = async () => {
  const res = await fetchKnowledgePointErrorStats();
  const chartData = res.data.slice(0, 10); // 只看最薄弱的10个

  if (kpChart.value) {
    kpChartInstance = echarts.init(kpChart.value);
    kpChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: chartData.map(d => d.knowledgePointName), axisLabel: { interval: 0, rotate: 30 } },
      yAxis: { type: 'value', name: '错误人次' },
      series: [{ name: '错误人次', type: 'bar', data: chartData.map(d => d.totalErrors) }],
      grid: { bottom: 100 }
    });
  }
};

const loadStudentAbilityData = async (studentId: number) => {
  if (!studentId) return;
  const res = await fetchStudentAbilityStats(studentId);
  if(studentChart.value) {
    if (!studentChartInstance) {
      studentChartInstance = echarts.init(studentChart.value);
    }
    studentChartInstance.setOption({
      legend: { data: ['错误次数'] },
      radar: {
        indicator: res.data.radarLabels.map(name => ({ name, max: Math.max(...res.data.errorRates, 10) })) // 动态设置max
      },
      series: [{
        name: '学生能力',
        type: 'radar',
        data: [{ value: res.data.errorRates, name: '错误次数' }]
      }]
    });
  }
};

const loadPaperDifficultyData = async (paperId: number) => {
  if (!paperId) return;
  const res = await fetchPaperDifficultyStats(paperId);
  paperStats.value = res.data;
}

onMounted(async () => {
  await loadInitialData();
  await loadKpErrorData();
});

</script>

<style scoped>
.stats-dashboard-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>