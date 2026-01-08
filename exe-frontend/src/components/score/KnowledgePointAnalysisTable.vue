<template>
  <div class="knowledge-point-analysis-table">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">知识点成绩分析</span>
          <el-tag v-if="!loading && analysisData.length > 0" type="info" size="small">
            共 {{ analysisData.length }} 个知识点
          </el-tag>
        </div>
      </template>

      <el-skeleton :loading="loading" animated :rows="5">
        <template #default>
          <div v-if="analysisData.length > 0">
            <!-- 统计概览 -->
            <el-row :gutter="20" class="stats-row">
              <el-col :span="6">
                <el-statistic title="平均得分率" :value="avgScoreRate.toFixed(1)" suffix="%">
                  <template #prefix>
                    <el-icon :color="getScoreRateColor(avgScoreRate)"><TrendCharts /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="最高得分率" :value="maxScoreRate.toFixed(1)" suffix="%">
                  <template #prefix>
                    <el-icon color="#67C23A"><Top /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="最低得分率" :value="minScoreRate.toFixed(1)" suffix="%">
                  <template #prefix>
                    <el-icon color="#F56C6C"><Bottom /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="薄弱知识点" :value="weakPointsCount">
                  <template #prefix>
                    <el-icon color="#E6A23C"><WarningFilled /></el-icon>
                  </template>
                  <template #suffix>
                    <span>个</span>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>

            <el-divider />

            <!-- 知识点分析表格 -->
            <el-table :data="analysisData" border stripe :default-sort="{ prop: 'scoreRate', order: 'ascending' }">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="knowledgePointName" label="知识点" min-width="150" show-overflow-tooltip />
              <el-table-column label="得分" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getScoreTagType(row.scoreRate)" size="small">
                    {{ row.score }} / {{ row.maxScore }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="scoreRate" label="得分率" width="150" align="center" sortable>
                <template #default="{ row }">
                  <el-progress
                    :percentage="row.scoreRate"
                    :color="getScoreRateProgressColor(row.scoreRate)"
                    :stroke-width="16"
                  >
                    <template #default="{ percentage }">
                      <span :style="{ color: getScoreRateColor(percentage) }">{{ percentage.toFixed(1) }}%</span>
                    </template>
                  </el-progress>
                </template>
              </el-table-column>
              <el-table-column prop="questionCount" label="题目数" width="100" align="center" sortable>
                <template #default="{ row }">
                  <el-tag type="primary" size="small" plain>{{ row.questionCount }} 题</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="掌握情况" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getMasteryTagType(row.scoreRate)" size="small">
                    {{ getMasteryLevel(row.scoreRate) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <!-- 学习建议 -->
            <el-alert
              v-if="suggestion"
              :title="suggestion"
              type="warning"
              :closable="false"
              style="margin-top: 20px;"
              show-icon
            >
              <template #default>
                <div class="suggestion-content">
                  <p v-if="weakPoints.length > 0">
                    建议重点复习以下知识点：<strong>{{ weakPoints.join('、') }}</strong>
                  </p>
                  <p v-if="strongPoints.length > 0">
                    已掌握的知识点：<strong>{{ strongPoints.join('、') }}</strong>
                  </p>
                </div>
              </template>
            </el-alert>
          </div>
          <el-empty v-else description="暂无知识点分析数据" />
        </template>
      </el-skeleton>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { getKnowledgePointAnalysis, type KnowledgePointScoreAnalysisDTO } from '@/api/score';
import { ElMessage } from 'element-plus';
import { TrendCharts, Top, Bottom, WarningFilled } from '@element-plus/icons-vue';

const props = defineProps<{
  examResultId: number;
}>();

const loading = ref(false);
const analysisData = ref<KnowledgePointScoreAnalysisDTO[]>([]);

// 加载知识点分析数据
const loadAnalysisData = async () => {
  loading.value = true;
  try {
    const res = await getKnowledgePointAnalysis(props.examResultId);
    if (res.code === 200 && res.data) {
      analysisData.value = res.data;
    }
  } catch (error) {
    ElMessage.error('获取知识点分析失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 计算统计数据
const avgScoreRate = computed(() => {
  if (analysisData.value.length === 0) return 0;
  const total = analysisData.value.reduce((sum, item) => sum + item.scoreRate, 0);
  return total / analysisData.value.length;
});

const maxScoreRate = computed(() => {
  if (analysisData.value.length === 0) return 0;
  return Math.max(...analysisData.value.map(item => item.scoreRate));
});

const minScoreRate = computed(() => {
  if (analysisData.value.length === 0) return 0;
  return Math.min(...analysisData.value.map(item => item.scoreRate));
});

const weakPointsCount = computed(() => {
  return analysisData.value.filter(item => item.scoreRate < 60).length;
});

// 薄弱知识点列表
const weakPoints = computed(() => {
  return analysisData.value
    .filter(item => item.scoreRate < 60)
    .map(item => item.knowledgePointName);
});

// 已掌握知识点列表
const strongPoints = computed(() => {
  return analysisData.value
    .filter(item => item.scoreRate >= 80)
    .map(item => item.knowledgePointName);
});

// 学习建议
const suggestion = computed(() => {
  if (analysisData.value.length === 0) return '';

  if (avgScoreRate.value >= 80) {
    return '整体表现优秀，继续保持！';
  } else if (avgScoreRate.value >= 60) {
    return '整体表现良好，仍有提升空间。';
  } else {
    return '需要加强学习，重点关注薄弱知识点。';
  }
});

// 根据得分率获取颜色
const getScoreRateColor = (rate: number) => {
  if (rate >= 80) return '#67C23A';
  if (rate >= 60) return '#409EFF';
  if (rate >= 40) return '#E6A23C';
  return '#F56C6C';
};

// 根据得分率获取进度条颜色
const getScoreRateProgressColor = (rate: number) => {
  if (rate >= 80) return '#67C23A';
  if (rate >= 60) return '#409EFF';
  if (rate >= 40) return '#E6A23C';
  return '#F56C6C';
};

// 根据得分率获取Tag类型
const getScoreTagType = (rate: number) => {
  if (rate >= 80) return 'success';
  if (rate >= 60) return 'primary';
  if (rate >= 40) return 'warning';
  return 'danger';
};

// 获取掌握情况
const getMasteryLevel = (rate: number) => {
  if (rate >= 90) return '优秀';
  if (rate >= 75) return '良好';
  if (rate >= 60) return '及格';
  return '待提升';
};

// 获取掌握情况Tag类型
const getMasteryTagType = (rate: number) => {
  if (rate >= 90) return 'success';
  if (rate >= 75) return 'primary';
  if (rate >= 60) return 'warning';
  return 'danger';
};

onMounted(() => {
  loadAnalysisData();
});
</script>

<style scoped lang="scss">
.knowledge-point-analysis-table {
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

  .stats-row {
    margin-bottom: 20px;

    :deep(.el-statistic__head) {
      font-size: 14px;
      color: #909399;
    }

    :deep(.el-statistic__content) {
      font-size: 24px;
      font-weight: 600;
    }
  }

  .suggestion-content {
    p {
      margin: 8px 0;
      line-height: 1.6;

      strong {
        color: #E6A23C;
      }
    }
  }
}
</style>
