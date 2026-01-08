<template>
  <div class="knowledge-mastery-radar">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">知识点掌握度分析</span>
          <el-select v-model="selectedSubject" placeholder="选择科目" size="small" clearable style="width: 150px;" @change="handleSubjectChange">
            <el-option label="全部科目" :value="null" />
            <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
          </el-select>
        </div>
      </template>

      <el-skeleton :loading="loading" animated>
        <template #template>
          <el-skeleton-item variant="image" style="width: 100%; height: 400px;" />
        </template>
        <template #default>
          <div v-if="masteryList.length > 0">
            <!-- 雷达图 -->
            <div ref="radarRef" class="chart-container"></div>

            <!-- 掌握度详情表格 -->
            <el-divider>掌握度详情</el-divider>
            <el-table :data="masteryList" border stripe>
              <el-table-column prop="knowledgePointName" label="知识点" min-width="150" />
              <el-table-column label="掌握度" width="120" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="row.masteryRate"
                    :color="getMasteryColor(row.masteryRate)"
                    :stroke-width="16"
                  >
                    <template #default="{ percentage }">
                      <span :style="{ color: getMasteryColor(percentage) }">{{ percentage.toFixed(1) }}%</span>
                    </template>
                  </el-progress>
                </template>
              </el-table-column>
              <el-table-column label="等级" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getMasteryTagType(row.masteryRate)" size="small">
                    {{ getMasteryLevel(row.masteryRate) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="答题记录" width="150" align="center">
                <template #default="{ row }">
                  <span style="color: #67C23A">{{ row.correctCount }}</span> /
                  <span style="color: #909399">{{ row.totalCount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="lastUpdateTime" label="最后更新" width="180" align="center">
                <template #default="{ row }">
                  <el-text size="small" type="info">{{ formatTime(row.lastUpdateTime) }}</el-text>
                </template>
              </el-table-column>
            </el-table>

            <!-- 总体评价 -->
            <el-alert
              v-if="overallAssessment"
              :title="overallAssessment"
              type="info"
              :closable="false"
              style="margin-top: 20px;"
            />
          </div>
          <el-empty v-else description="暂无掌握度数据" />
        </template>
      </el-skeleton>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue';
import * as echarts from 'echarts';
import type { ECharts } from 'echarts';
import { fetchStudentMastery, fetchStudentMasteryBySubject, type StudentKnowledgeMasteryDTO } from '@/api/knowledgeMastery';
import { ElMessage } from 'element-plus';

const props = defineProps<{
  studentId: number;
  subjects?: Array<{ id: number; name: string }>; // 可选科目列表
}>();

const loading = ref(false);
const masteryList = ref<StudentKnowledgeMasteryDTO[]>([]);
const selectedSubject = ref<number | null>(null);
const radarRef = ref<HTMLDivElement>();
let radarInstance: ECharts | null = null;

const subjects = computed(() => props.subjects || []);

// 获取掌握度数据
const loadMasteryData = async () => {
  loading.value = true;
  try {
    let res;
    if (selectedSubject.value) {
      res = await fetchStudentMasteryBySubject(props.studentId, selectedSubject.value);
    } else {
      res = await fetchStudentMastery(props.studentId);
    }

    if (res.code === 200 && res.data) {
      masteryList.value = res.data;
      await nextTick();
      initRadar();
    }
  } catch (error) {
    ElMessage.error('获取知识点掌握度失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 初始化雷达图
const initRadar = () => {
  if (!radarRef.value || masteryList.value.length === 0) return;

  if (radarInstance) {
    radarInstance.dispose();
  }

  radarInstance = echarts.init(radarRef.value);

  const indicator = masteryList.value.map(item => ({
    name: item.knowledgePointName,
    max: 100
  }));

  const data = masteryList.value.map(item => item.masteryRate);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const index = params.dataIndex;
        const kp = masteryList.value[index];
        return `${kp.knowledgePointName}<br/>掌握度: ${kp.masteryRate.toFixed(1)}%<br/>答对: ${kp.correctCount}/${kp.totalCount}`;
      }
    },
    radar: {
      indicator,
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#666',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: [
            'rgba(64, 158, 255, 0.1)',
            'rgba(64, 158, 255, 0.2)',
            'rgba(64, 158, 255, 0.4)',
            'rgba(64, 158, 255, 0.6)',
            'rgba(64, 158, 255, 0.8)'
          ].reverse()
        }
      },
      splitArea: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(64, 158, 255, 0.5)'
        }
      }
    },
    series: [
      {
        name: '知识点掌握度',
        type: 'radar',
        areaStyle: {
          color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
            {
              color: 'rgba(64, 158, 255, 0.1)',
              offset: 0
            },
            {
              color: 'rgba(64, 158, 255, 0.9)',
              offset: 1
            }
          ])
        },
        lineStyle: {
          width: 2,
          color: '#409EFF'
        },
        itemStyle: {
          color: '#409EFF'
        },
        data: [
          {
            value: data,
            name: '掌握度'
          }
        ]
      }
    ]
  };

  radarInstance.setOption(option);
};

// 根据掌握度获取颜色
const getMasteryColor = (rate: number) => {
  if (rate >= 90) return '#67C23A'; // 优秀 - 绿色
  if (rate >= 75) return '#409EFF'; // 良好 - 蓝色
  if (rate >= 60) return '#E6A23C'; // 及格 - 橙色
  return '#F56C6C'; // 待提升 - 红色
};

// 获取掌握度等级
const getMasteryLevel = (rate: number) => {
  if (rate >= 90) return '优秀';
  if (rate >= 75) return '良好';
  if (rate >= 60) return '及格';
  return '待提升';
};

// 获取Tag类型
const getMasteryTagType = (rate: number) => {
  if (rate >= 90) return 'success';
  if (rate >= 75) return 'primary';
  if (rate >= 60) return 'warning';
  return 'danger';
};

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-';
  return new Date(time).toLocaleString('zh-CN');
};

// 科目切换处理
const handleSubjectChange = () => {
  loadMasteryData();
};

// 总体评价
const overallAssessment = computed(() => {
  if (masteryList.value.length === 0) return '';

  const avgMastery = masteryList.value.reduce((sum, item) => sum + item.masteryRate, 0) / masteryList.value.length;
  const weakPoints = masteryList.value.filter(item => item.masteryRate < 60);

  let assessment = `平均掌握度: ${avgMastery.toFixed(1)}%`;

  if (weakPoints.length > 0) {
    const weakNames = weakPoints.map(item => item.knowledgePointName).join('、');
    assessment += `，薄弱知识点: ${weakNames}`;
  } else {
    assessment += '，整体表现优秀！';
  }

  return assessment;
});

// 窗口大小变化时调整图表
const handleResize = () => {
  radarInstance?.resize();
};

onMounted(() => {
  loadMasteryData();
  window.addEventListener('resize', handleResize);
});

// 组件卸载时销毁图表
import { onUnmounted } from 'vue';
onUnmounted(() => {
  if (radarInstance) {
    radarInstance.dispose();
    radarInstance = null;
  }
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped lang="scss">
.knowledge-mastery-radar {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .chart-container {
    width: 100%;
    height: 450px;
    margin-bottom: 20px;
  }
}
</style>
