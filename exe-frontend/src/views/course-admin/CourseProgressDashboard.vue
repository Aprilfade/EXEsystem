<template>
  <div class="course-progress-dashboard">
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="总学生数" :value="overview.totalStudents">
            <template #prefix>
              <el-icon color="#606266"><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="完成人数" :value="overview.completedStudents">
            <template #prefix>
              <el-icon color="#67c23a"><CircleCheck /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic
            title="平均进度"
            :value="overview.averageProgress"
            suffix="%"
            :precision="1"
          >
            <template #prefix>
              <el-icon color="#409eff"><TrendCharts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic
            title="平均学习时长"
            :value="formatStudyTime(overview.averageStudyTime)"
          >
            <template #prefix>
              <el-icon color="#e6a23c"><Clock /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生进度表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>学生学习进度</span>
          <el-button type="primary" size="small" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="年级">
          <el-select v-model="filters.grade" placeholder="全部" clearable @change="loadData">
            <el-option label="高一" value="高一" />
            <el-option label="高二" value="高二" />
            <el-option label="高三" value="高三" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-input
            v-model="filters.className"
            placeholder="请输入班级"
            clearable
            @change="loadData"
            style="width: 150px;"
          />
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="studentList" v-loading="loading" stripe>
        <el-table-column prop="studentName" label="姓名" width="120" fixed />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column prop="className" label="班级" width="100" />
        <el-table-column label="完成率" width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.completionRate" :color="getProgressColor(row.completionRate)" />
          </template>
        </el-table-column>
        <el-table-column label="学习时长" width="150">
          <template #default="{ row }">
            {{ formatStudyTime(row.totalStudyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="最后学习时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastStudyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 学生详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="学生学习详情"
      width="800px"
    >
      <div v-if="currentStudent" v-loading="detailLoading">
        <!-- 学生信息 -->
        <el-descriptions :column="3" border>
          <el-descriptions-item label="姓名">{{ currentStudent.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentStudent.grade }} {{ currentStudent.className }}</el-descriptions-item>
          <el-descriptions-item label="完成率">{{ currentStudent.completionRate }}%</el-descriptions-item>
          <el-descriptions-item label="学习时长">{{ formatStudyTime(currentStudent.totalStudyTime) }}</el-descriptions-item>
          <el-descriptions-item label="最后学习">{{ formatDateTime(currentStudent.lastStudyTime) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 资源学习详情 -->
        <h4 style="margin-top: 20px;">资源学习进度</h4>
        <el-table :data="resourceProgress" stripe max-height="400">
          <el-table-column prop="resourceName" label="资源名称" min-width="200" />
          <el-table-column prop="resourceType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getResourceTypeTagType(row.resourceType)">
                {{ row.resourceType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="200">
            <template #default="{ row }">
              <el-progress :percentage="row.progressPercent" />
            </template>
          </el-table-column>
          <el-table-column label="学习时长" width="120">
            <template #default="{ row }">
              {{ formatStudyTime(row.studyDuration) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isCompleted" type="success">已完成</el-tag>
              <el-tag v-else-if="row.progressPercent > 0" type="warning">学习中</el-tag>
              <el-tag v-else type="info">未开始</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { User, CircleCheck, TrendCharts, Clock, Download } from '@element-plus/icons-vue';
import {
  getCourseProgressOverview,
  getCourseStudentProgress,
  getStudentDetailProgress,
  exportCourseProgress
} from '@/api/courseAdmin';
import type { CourseProgressOverview, StudentProgressItem } from '@/api/courseAdmin';

interface Props {
  courseId: number;
}

const props = defineProps<Props>();

const loading = ref(false);
const detailLoading = ref(false);
const detailVisible = ref(false);

const overview = ref<CourseProgressOverview>({
  totalStudents: 0,
  completedStudents: 0,
  averageProgress: 0,
  averageStudyTime: 0
});

const studentList = ref<StudentProgressItem[]>([]);
const currentStudent = ref<StudentProgressItem | null>(null);
const resourceProgress = ref<any[]>([]);

const filters = reactive({
  grade: '',
  className: ''
});

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 加载概览数据
const loadOverview = async () => {
  const res = await getCourseProgressOverview(
    props.courseId,
    filters.grade,
    filters.className
  );
  if (res.code === 200) {
    overview.value = res.data;
  }
};

// 加载学生列表
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getCourseStudentProgress(
      props.courseId,
      pagination.current,
      pagination.size,
      filters.grade,
      filters.className
    );
    if (res.code === 200) {
      studentList.value = res.data.records;
      pagination.total = res.data.total;
    }
    await loadOverview();
  } finally {
    loading.value = false;
  }
};

// 查看详情
const viewDetail = async (student: StudentProgressItem) => {
  currentStudent.value = student;
  detailVisible.value = true;
  detailLoading.value = true;

  try {
    const res = await getStudentDetailProgress(student.studentId, props.courseId);
    if (res.code === 200) {
      resourceProgress.value = res.data.resourceProgress;
    }
  } finally {
    detailLoading.value = false;
  }
};

// 导出
const handleExport = () => {
  exportCourseProgress(props.courseId);
};

// 格式化学习时长
const formatStudyTime = (seconds: number): string => {
  if (!seconds || seconds === 0) return '0秒';
  if (seconds < 60) return `${seconds}秒`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`;
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${hours}小时${minutes > 0 ? minutes + '分钟' : ''}`;
};

// 格式化日期时间
const formatDateTime = (dateTime: string | null): string => {
  if (!dateTime) return '-';
  return new Date(dateTime).toLocaleString('zh-CN');
};

// 获取进度条颜色
const getProgressColor = (percentage: number): string => {
  if (percentage >= 95) return '#67c23a';
  if (percentage >= 60) return '#409eff';
  if (percentage >= 30) return '#e6a23c';
  return '#f56c6c';
};

// 获取资源类型标签类型
const getResourceTypeTagType = (type: string): string => {
  const typeMap: Record<string, string> = {
    VIDEO: 'success',
    PDF: 'danger',
    PPT: 'warning',
    LINK: 'primary'
  };
  return typeMap[type] || '';
};

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.course-progress-dashboard {
  .overview-cards {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .filter-form {
      margin-bottom: 20px;
    }
  }
}
</style>
