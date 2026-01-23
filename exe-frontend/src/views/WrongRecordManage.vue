<template>
  <div class="wrong-record-manage-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div>
          <h2>错题管理</h2>
          <p>管理和分析学生错题记录，助力精准教学</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" @click="handleRefresh" :loading="loading">刷新</el-button>
        <el-button icon="Download" type="primary" plain @click="handleExport">导出</el-button>
        <el-button type="primary" :icon="Plus" @click="handleCreate">新增错题</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-total">
          <div class="stat-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">错题总数</div>
            <div class="stat-value">{{ total }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-students">
          <div class="stat-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">涉及学生</div>
            <div class="stat-value">{{ uniqueStudentCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-papers">
          <div class="stat-icon">
            <el-icon :size="32"><Reading /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">涉及试卷</div>
            <div class="stat-value">{{ uniquePaperCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-today">
          <div class="stat-icon">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">今日新增</div>
            <div class="stat-value">{{ todayCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="选择学生">
          <el-select
              v-model="queryParams.studentId"
              filterable
              placeholder="可输入姓名搜索"
              clearable
              style="width: 240px;"
              @change="handleQuery"
          >
            <el-option v-for="s in allStudents" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新增错题</el-button>
        </el-form-item>
      </el-form>
      </div>
    </el-card>

    <el-card shadow="never" class="content-card table-card">
      <el-table v-loading="loading" :data="recordList" style="width: 100%" stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="120">
          <template #default="scope">
            <div class="user-cell">
              <el-icon><User /></el-icon>
              <span>{{ scope.row.studentName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="questionContent" label="题干" show-overflow-tooltip min-width="200" />
        <el-table-column prop="paperName" label="所属试卷" width="150" show-overflow-tooltip />
        <el-table-column prop="wrongReason" label="错误原因" show-overflow-tooltip min-width="150" />
        <el-table-column prop="createTime" label="记录时间" width="180">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(scope.row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          class="pagination"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          @size-change="getList"
          @current-change="getList"
      />
    </el-card>

    <wrong-record-edit-dialog
        v-model:visible="isDialogVisible"
        :record-data="editingRecord"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchWrongRecordList, deleteWrongRecord } from '@/api/wrongRecord';
import type { WrongRecordVO, WrongRecordPageParams } from '@/api/wrongRecord';
import { fetchStudentList, type Student } from '@/api/student';
import { Plus, Delete, Search, Refresh, Warning, Document, User, Reading, Clock, Download } from '@element-plus/icons-vue';
import WrongRecordEditDialog from '@/components/wrong-record/WrongRecordEditDialog.vue';

const recordList = ref<WrongRecordVO[]>([]);
const allStudents = ref<Student[]>([]);
const total = ref(0);
const loading = ref(true);
const isDialogVisible = ref(false);
const editingRecord = ref<WrongRecordVO | undefined>(undefined);

const queryParams = reactive<WrongRecordPageParams>({
  current: 1,
  size: 10,
  studentId: undefined,
});

// 统计计算属性
const uniqueStudentCount = computed(() => {
  const studentIds = new Set(recordList.value.map(r => r.studentId));
  return studentIds.size;
});

const uniquePaperCount = computed(() => {
  const paperNames = new Set(recordList.value.map(r => r.paperName).filter(Boolean));
  return paperNames.size;
});

const todayCount = computed(() => {
  const today = new Date().toISOString().split('T')[0];
  return recordList.value.filter(r => r.createTime?.startsWith(today)).length;
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchWrongRecordList(queryParams);
    recordList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const loadAllStudents = async () => {
  const res = await fetchStudentList({current: 1, size: 9999});
  allStudents.value = res.data;
}

const handleQuery = () => {
  queryParams.current = 1;
  getList();
};

const resetQuery = () => {
  queryParams.studentId = undefined;
  handleQuery();
};

const handleCreate = () => {
  editingRecord.value = undefined;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这条错题记录吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deleteWrongRecord(id);
        ElMessage.success('删除成功');
        getList();
      })
      .catch(() => {});
};

const handleRefresh = () => {
  getList();
  ElMessage.success('刷新成功');
};

const handleExport = () => {
  ElMessage.info('导出功能开发中...');
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(0, 19);
};

onMounted(() => {
  loadAllStudents();
  getList();
});
</script>

<style scoped>
/* 页面容器 */
.wrong-record-manage-container {
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
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
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
.stat-total::before { background: linear-gradient(90deg, #f56c6c 0%, #f78989 100%); }
.stat-total .stat-icon {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.1) 0%, rgba(247, 137, 137, 0.1) 100%);
  color: #f56c6c;
}
.stat-total .stat-value { color: #f56c6c; }

.stat-students::before { background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%); }
.stat-students .stat-icon {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(102, 177, 255, 0.1) 100%);
  color: #409eff;
}
.stat-students .stat-value { color: #409eff; }

.stat-papers::before { background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%); }
.stat-papers .stat-icon {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(235, 181, 99, 0.1) 100%);
  color: #e6a23c;
}
.stat-papers .stat-value { color: #e6a23c; }

.stat-today::before { background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%); }
.stat-today .stat-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(133, 206, 97, 0.1) 100%);
  color: #67c23a;
}
.stat-today .stat-value { color: #67c23a; }

/* 内容卡片 */
.content-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
  margin-bottom: 20px;
}

.content-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.search-form {
  margin: 0;
}

.search-form .el-form-item {
  margin-bottom: 12px;
  margin-right: 16px;
}

/* 表格样式 */
.table-card {
  margin-bottom: 0;
}

.user-cell, .time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .wrong-record-manage-container {
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

  .search-form {
    display: flex;
    flex-direction: column;
  }

  .search-form .el-form-item {
    margin-right: 0;
    width: 100%;
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
}
</style>