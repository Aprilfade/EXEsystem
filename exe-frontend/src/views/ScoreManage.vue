<template>
  <div class="score-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>成绩管理与分析</h2>
      <p class="page-desc">全面的成绩查询、统计分析和批阅管理</p>
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="stats-row" v-loading="statsLoading">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总考试人数</div>
              <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">平均分</div>
              <div class="stat-value">{{ stats.averageScore?.toFixed(1) || '0.0' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon :size="28"><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">及格率</div>
              <div class="stat-value">{{ stats.passRate?.toFixed(1) || '0.0' }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
              <el-icon :size="28"><Trophy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">优秀率</div>
              <div class="stat-value">{{ stats.excellentRate?.toFixed(1) || '0.0' }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="main-card">
      <!-- 筛选区 -->
      <div class="filter-section">
        <el-form :inline="true" :model="queryParams" class="filter-form">
          <el-form-item label="试卷名称">
            <el-input
              v-model="queryParams.paperName"
              placeholder="请输入试卷名称"
              clearable
              style="width: 200px;"
            />
          </el-form-item>
          <el-form-item label="学生姓名">
            <el-input
              v-model="queryParams.studentName"
              placeholder="请输入学生姓名"
              clearable
              style="width: 200px;"
            />
          </el-form-item>
          <el-form-item label="班级">
            <el-select
              v-model="queryParams.classId"
              placeholder="请选择班级"
              clearable
              style="width: 180px;"
            >
              <el-option
                v-for="cls in classList"
                :key="cls.id"
                :label="cls.name"
                :value="cls.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="科目">
            <el-select
              v-model="queryParams.subjectId"
              placeholder="请选择科目"
              clearable
              style="width: 180px;"
            >
              <el-option
                v-for="subject in subjectList"
                :key="subject.id"
                :label="subject.name"
                :value="subject.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="批改状态">
            <el-select
              v-model="queryParams.status"
              placeholder="全部状态"
              clearable
              style="width: 180px;"
            >
              <el-option label="待批改" :value="1" />
              <el-option label="已批改" :value="2" />
            </el-select>
          </el-form-item>

          <!-- 展开更多筛选 -->
          <el-collapse-transition>
            <div v-show="showAdvancedFilter" class="advanced-filters">
              <el-form-item label="分数范围">
                <el-slider
                  v-model="scoreRange"
                  range
                  :min="0"
                  :max="100"
                  style="width: 200px;"
                  @change="handleScoreRangeChange"
                />
                <span class="score-range-text">{{ scoreRange[0] }} - {{ scoreRange[1] }}</span>
              </el-form-item>
              <el-form-item label="提交时间">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  @change="handleDateRangeChange"
                  style="width: 260px;"
                />
              </el-form-item>
              <el-form-item label="发布状态">
                <el-select
                  v-model="queryParams.published"
                  placeholder="全部"
                  clearable
                  style="width: 120px;"
                >
                  <el-option label="已发布" :value="true" />
                  <el-option label="未发布" :value="false" />
                </el-select>
              </el-form-item>
              <el-form-item label="排序方式">
                <el-select
                  v-model="queryParams.sortBy"
                  placeholder="排序字段"
                  style="width: 130px;"
                >
                  <el-option label="提交时间" value="createTime" />
                  <el-option label="分数" value="score" />
                  <el-option label="切屏次数" value="violationCount" />
                </el-select>
                <el-select
                  v-model="queryParams.sortOrder"
                  placeholder="排序方向"
                  style="width: 100px; margin-left: 10px;"
                >
                  <el-option label="降序" value="desc" />
                  <el-option label="升序" value="asc" />
                </el-select>
              </el-form-item>
            </div>
          </el-collapse-transition>

          <el-form-item>
            <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
            <el-button @click="handleReset" :icon="RefreshRight">重置</el-button>
            <el-button
              text
              @click="showAdvancedFilter = !showAdvancedFilter"
              :icon="showAdvancedFilter ? ArrowUp : ArrowDown"
            >
              {{ showAdvancedFilter ? '收起' : '展开' }}高级筛选
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button
            type="success"
            :icon="Download"
            @click="handleExport"
            :disabled="tableData.length === 0"
          >
            导出Excel
          </el-button>
          <el-button
            type="primary"
            :icon="Document"
            @click="handleBatchPublish(true)"
            :disabled="selectedIds.length === 0"
          >
            批量发布
          </el-button>
          <el-button
            type="warning"
            :icon="Hide"
            @click="handleBatchPublish(false)"
            :disabled="selectedIds.length === 0"
          >
            批量隐藏
          </el-button>
          <el-button
            type="danger"
            :icon="Delete"
            @click="handleBatchDelete"
            :disabled="selectedIds.length === 0"
          >
            批量删除
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button :icon="DataAnalysis" @click="showStatsDrawer = true">
            查看统计图表
          </el-button>
          <el-button :icon="RefreshRight" @click="fetchData" :loading="loading">
            刷新
          </el-button>
        </div>
      </div>

      <!-- 成绩表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="paperName" label="试卷名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="学生信息" min-width="150">
          <template #default="{ row }">
            <div class="student-info">
              <div class="student-name">{{ row.studentName }}</div>
              <div class="student-no">学号: {{ row.studentNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="subjectName" label="科目" width="100" />
        <el-table-column label="得分" width="140" align="center">
          <template #default="{ row }">
            <div class="score-display">
              <span
                class="score-value"
                :class="getScoreClass(row.score)"
              >
                {{ row.score }}
              </span>
              <span class="score-total">/ {{ row.totalScore }}</span>
            </div>
            <el-progress
              :percentage="(row.score / row.totalScore * 100)"
              :stroke-width="4"
              :show-text="false"
              :color="getScoreColor(row.score)"
            />
          </template>
        </el-table-column>
        <el-table-column label="切屏次数" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.violationCount > 0 ? 'danger' : 'success'" size="small">
              {{ row.violationCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.published ? 'success' : 'info'" size="small">
              {{ row.published ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openGrading(row)">
              批阅
            </el-button>
            <el-button type="success" link :icon="View" @click="viewDetail(row)">
              详情
            </el-button>
            <el-button
              :type="row.published ? 'warning' : 'primary'"
              link
              :icon="row.published ? Hide : Document"
              @click="handleTogglePublish(row)"
            >
              {{ row.published ? '隐藏' : '发布' }}
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>

    <!-- 批阅对话框 -->
    <grading-dialog
      v-model:visible="gradingVisible"
      :result-id="currentResultId"
      @success="fetchData"
    />

    <!-- 详情对话框 -->
    <score-detail-dialog
      v-model:visible="detailVisible"
      :result-id="currentResultId"
      @edit="handleEditFromDetail"
    />

    <!-- 统计图表抽屉 -->
    <el-drawer
      v-model="showStatsDrawer"
      title="成绩统计分析"
      size="70%"
      direction="rtl"
    >
      <score-stats-charts :stats="stats" :query-params="queryParams" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import {
  Search, RefreshRight, Download, Delete, Edit, View,
  Document, Hide, DataAnalysis, User, TrendCharts,
  Trophy, ArrowUp, ArrowDown, Select
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getScoreList,
  getScoreStats,
  exportScores,
  batchDeleteScores,
  batchPublishScores,
  type ScoreQueryParams,
  type ExamResultDetail,
  type ScoreStats as StatsType
} from '@/api/score';
import { fetchClassList as fetchClassListAPI } from '@/api/class';
import { fetchAllSubjects as fetchAllSubjectsAPI } from '@/api/subject';
import GradingDialog from '@/components/score/GradingDialog.vue';
import ScoreDetailDialog from '@/components/score/ScoreDetailDialog.vue';
import ScoreStatsCharts from '@/components/score/ScoreStatsCharts.vue';

// 加载状态
const loading = ref(false);
const statsLoading = ref(false);

// 表格数据
const tableData = ref<ExamResultDetail[]>([]);
const total = ref(0);
const selectedIds = ref<number[]>([]);

// 统计数据
const stats = ref<StatsType>({
  totalCount: 0,
  averageScore: 0,
  maxScore: 0,
  minScore: 0,
  passCount: 0,
  passRate: 0,
  excellentCount: 0,
  excellentRate: 0,
  scoreDistribution: {
    '0-59': 0,
    '60-69': 0,
    '70-79': 0,
    '80-89': 0,
    '90-100': 0
  }
});

// 查询参数
const queryParams = reactive<ScoreQueryParams>({
  current: 1,
  size: 10,
  paperName: '',
  studentName: '',
  classId: undefined,
  subjectId: undefined,
  minScore: undefined,
  maxScore: undefined,
  startTime: '',
  endTime: '',
  published: undefined,
  status: undefined,
  sortBy: 'createTime',
  sortOrder: 'desc'
});

// 筛选相关
const showAdvancedFilter = ref(false);
const scoreRange = ref([0, 100]);
const dateRange = ref<string[]>([]);
const classList = ref<any[]>([]);
const subjectList = ref<any[]>([]);

// 对话框和抽屉
const gradingVisible = ref(false);
const detailVisible = ref(false);
const currentResultId = ref(0);
const showStatsDrawer = ref(false);

/**
 * 获取成绩列表
 */
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getScoreList(queryParams);
    if (res.code === 200) {
      tableData.value = res.data;
      total.value = res.total;
    }
  } catch (error) {
    ElMessage.error('获取成绩列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 获取统计数据
 */
const fetchStats = async () => {
  statsLoading.value = true;
  try {
    const params = {
      classId: queryParams.classId,
      subjectId: queryParams.subjectId,
      startTime: queryParams.startTime,
      endTime: queryParams.endTime
    };
    const res = await getScoreStats(params);
    if (res.code === 200) {
      stats.value = res.data;
    }
  } catch (error) {
    console.error('获取统计数据失败', error);
  } finally {
    statsLoading.value = false;
  }
};

/**
 * 获取班级列表
 */
const fetchClassList = async () => {
  try {
    const res = await fetchClassListAPI();
    if (res.code === 200) {
      classList.value = res.data;
    }
  } catch (error) {
    console.error('获取班级列表失败', error);
  }
};

/**
 * 获取科目列表
 */
const fetchSubjectList = async () => {
  try {
    const res = await fetchAllSubjectsAPI();
    if (res.code === 200) {
      subjectList.value = res.data;
    }
  } catch (error) {
    console.error('获取科目列表失败', error);
  }
};

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.current = 1;
  fetchData();
  fetchStats();
};

/**
 * 重置
 */
const handleReset = () => {
  // 保留从待办事项跳转来的status筛选条件
  const statusFromRoute = route.query.status ? Number(route.query.status) : undefined;

  Object.assign(queryParams, {
    current: 1,
    size: 10,
    paperName: '',
    studentName: '',
    classId: undefined,
    subjectId: undefined,
    minScore: undefined,
    maxScore: undefined,
    startTime: '',
    endTime: '',
    published: undefined,
    status: statusFromRoute,  // 保留原有的status参数
    sortBy: 'createTime',
    sortOrder: 'desc'
  });
  scoreRange.value = [0, 100];
  dateRange.value = [];
  handleSearch();
};

/**
 * 分数范围变化
 */
const handleScoreRangeChange = (value: number[]) => {
  queryParams.minScore = value[0];
  queryParams.maxScore = value[1];
};

/**
 * 日期范围变化
 */
const handleDateRangeChange = (value: string[]) => {
  if (value && value.length === 2) {
    queryParams.startTime = value[0];
    queryParams.endTime = value[1];
  } else {
    queryParams.startTime = '';
    queryParams.endTime = '';
  }
};

/**
 * 表格选择变化
 */
const handleSelectionChange = (selection: ExamResultDetail[]) => {
  selectedIds.value = selection.map(item => item.id);
};

/**
 * 导出Excel
 */
const handleExport = () => {
  ElMessageBox.confirm('确定导出当前筛选条件下的所有成绩吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    exportScores(queryParams);
    ElMessage.success('导出成功，请查看下载文件');
  }).catch(() => {});
};

/**
 * 批量发布/隐藏
 */
const handleBatchPublish = async (published: boolean) => {
  const action = published ? '发布' : '隐藏';
  ElMessageBox.confirm(`确定${action}选中的 ${selectedIds.value.length} 条成绩吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await batchPublishScores(selectedIds.value, published);
      if (res.code === 200) {
        ElMessage.success(`${action}成功`);
        fetchData();
      }
    } catch (error) {
      ElMessage.error(`${action}失败`);
    }
  }).catch(() => {});
};

/**
 * 批量删除
 */
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条成绩吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      const res = await batchDeleteScores(selectedIds.value);
      if (res.code === 200) {
        ElMessage.success('删除成功');
        fetchData();
        fetchStats();
      }
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

/**
 * 切换发布状态
 */
const handleTogglePublish = async (row: ExamResultDetail) => {
  const action = row.published ? '隐藏' : '发布';
  try {
    const res = await batchPublishScores([row.id], !row.published);
    if (res.code === 200) {
      ElMessage.success(`${action}成功`);
      fetchData();
    }
  } catch (error) {
    ElMessage.error(`${action}失败`);
  }
};

/**
 * 删除单条
 */
const handleDelete = (row: ExamResultDetail) => {
  ElMessageBox.confirm('确定删除该成绩吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      const res = await batchDeleteScores([row.id]);
      if (res.code === 200) {
        ElMessage.success('删除成功');
        fetchData();
        fetchStats();
      }
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

/**
 * 打开批阅对话框
 */
const openGrading = (row: ExamResultDetail) => {
  currentResultId.value = row.id;
  gradingVisible.value = true;
};

/**
 * 查看详情
 */
const viewDetail = (row: ExamResultDetail) => {
  currentResultId.value = row.id;
  detailVisible.value = true;
};

/**
 * 从详情对话框打开批阅
 */
const handleEditFromDetail = (id: number) => {
  currentResultId.value = id;
  detailVisible.value = false;
  gradingVisible.value = true;
};

/**
 * 获取分数颜色类名
 */
const getScoreClass = (score: number) => {
  if (score >= 90) return 'score-excellent';
  if (score >= 80) return 'score-good';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
};

/**
 * 获取分数进度条颜色
 */
const getScoreColor = (score: number) => {
  if (score >= 90) return '#67C23A';
  if (score >= 80) return '#409EFF';
  if (score >= 60) return '#E6A23C';
  return '#F56C6C';
};

const route = useRoute();

onMounted(() => {
  // 读取URL参数，自动筛选待批改试卷
  if (route.query.status) {
    queryParams.status = Number(route.query.status);
  }
  fetchData();
  fetchStats();
  fetchClassList();
  fetchSubjectList();
});
</script>

<style scoped>
.score-manage-container {
  padding: 20px;
  background: var(--bg-color, #f5f7fa);
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color-primary, #303133);
  margin: 0 0 8px 0;
}

.page-desc {
  font-size: 14px;
  color: var(--text-color-secondary, #909399);
  margin: 0;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
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
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-color-secondary, #909399);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-color-primary, #303133);
}

/* 主卡片 */
.main-card {
  border-radius: 8px;
}

/* 筛选区 */
.filter-section {
  margin-bottom: 20px;
}

.filter-form {
  margin: 0;
}

.advanced-filters {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #DCDFE6;
}

.score-range-text {
  margin-left: 10px;
  font-size: 14px;
  color: var(--text-color-regular, #606266);
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-left, .toolbar-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 表格样式 */
.student-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.student-name {
  font-weight: 600;
  color: var(--text-color-primary, #303133);
}

.student-no {
  font-size: 12px;
  color: var(--text-color-secondary, #909399);
}

.score-display {
  margin-bottom: 8px;
}

.score-value {
  font-size: 20px;
  font-weight: 700;
}

.score-excellent { color: #67C23A; }
.score-good { color: #409EFF; }
.score-pass { color: #E6A23C; }
.score-fail { color: #F56C6C; }

.score-total {
  font-size: 14px;
  color: var(--text-color-secondary, #909399);
  margin-left: 4px;
}

/* 分页 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 深色模式适配 */
.dark .score-manage-container {
  background: #1a1a1a;
}

.dark .stat-card {
  background: #2d2d2d;
}

.dark .main-card {
  background: #2d2d2d;
}
</style>
