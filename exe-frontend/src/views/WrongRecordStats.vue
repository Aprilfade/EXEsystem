<template>
  <div class="stats-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><DataAnalysis /></el-icon>
        </div>
        <div>
          <h2>错题统计分析</h2>
          <p>多维度分析错题数据，精准定位学习薄弱环节</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" @click="handleRefresh" :loading="loading">刷新</el-button>
        <el-button icon="Download" type="primary" plain @click="handleExport">导出报告</el-button>
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
            <div class="stat-value">{{ totalRecords }}</div>
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
            <div class="stat-value">{{ totalStudents }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-questions">
          <div class="stat-icon">
            <el-icon :size="32"><EditPen /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">涉及题目</div>
            <div class="stat-value">{{ totalQuestions }}</div>
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
            <div class="stat-value">{{ totalPapers }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <el-tabs v-model="activeTab" class="stats-tabs">
        <el-tab-pane label="按学生查询" name="byStudent">
          <div class="tab-content">
            <div class="search-section">
              <el-select
                v-model="selectedStudent"
                filterable
                placeholder="请选择学生"
                @change="queryByStudent"
                size="large"
                style="width: 400px;"
                clearable
              >
                <el-option v-for="s in allStudents" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
              </el-select>
            </div>
            <el-table :data="studentResult" style="margin-top: 20px;" v-loading="loading" stripe>
              <el-table-column type="index" label="#" width="60" align="center" />
              <el-table-column prop="questionContent" label="题干" show-overflow-tooltip min-width="300" />
              <el-table-column prop="paperName" label="来源试卷" width="200" show-overflow-tooltip>
                <template #default="scope">
                  <div class="paper-cell">
                    <el-icon><Reading /></el-icon>
                    <span>{{ scope.row.paperName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="wrongReason" label="错误原因" show-overflow-tooltip min-width="200" />
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="按题目查询" name="byQuestion">
          <div class="tab-content">
            <div class="search-section">
              <el-select
                v-model="selectedQuestion"
                filterable
                remote
                :remote-method="searchQuestions"
                placeholder="请输入关键词搜索题目"
                @change="queryByQuestion"
                size="large"
                style="width: 100%;"
                clearable
              >
                <el-option v-for="q in questionOptions" :key="q.id" :label="q.content" :value="q.id" />
              </el-select>
            </div>
            <el-table :data="questionResult" style="margin-top: 20px;" v-loading="loading" stripe>
              <el-table-column type="index" label="#" width="60" align="center" />
              <el-table-column prop="studentName" label="学生姓名" width="150">
                <template #default="scope">
                  <div class="user-cell">
                    <el-icon><User /></el-icon>
                    <span>{{ scope.row.studentName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="studentNo" label="学号" width="150" />
              <el-table-column prop="wrongReason" label="错误原因" show-overflow-tooltip min-width="300" />
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="按试卷分析" name="byPaper">
          <div class="tab-content">
            <div class="search-section">
              <el-select
                v-model="selectedPaper"
                filterable
                placeholder="请选择试卷"
                @change="queryByPaper"
                size="large"
                style="width: 400px;"
                clearable
              >
                <el-option v-for="p in allPapers" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </div>
            <el-table :data="paperResult" style="margin-top: 20px;" v-loading="loading" stripe>
              <el-table-column prop="sortOrder" label="题号" width="80" align="center">
                <template #default="scope">
                  <el-tag type="info" size="small">{{ scope.row.sortOrder }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="questionContent" label="题干" show-overflow-tooltip min-width="300" />
              <el-table-column prop="errorCount" label="错误人数" width="120" align="center">
                <template #default="scope">
                  <el-tag :type="getErrorCountType(scope.row.errorCount)" effect="dark">
                    {{ scope.row.errorCount }} 人
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { fetchRecordsByStudent, fetchStudentsByQuestion, fetchStatsByPaper, type WrongRecordVO, type PaperStatsVO } from '@/api/wrongRecord';
import { fetchStudentList, type Student } from '@/api/student';
import { fetchQuestionList, type Question } from '@/api/question';
import { fetchPaperList, type Paper } from '@/api/paper';
import { ElMessage } from 'element-plus';
import { DataAnalysis, Document, User, EditPen, Reading, Refresh, Download } from '@element-plus/icons-vue';

const loading = ref(false);
const activeTab = ref('byStudent');

const allStudents = ref<Student[]>([]);
const selectedStudent = ref<number | null>(null);
const studentResult = ref<WrongRecordVO[]>([]);

const questionOptions = ref<Question[]>([]);
const selectedQuestion = ref<number | null>(null);
const questionResult = ref<WrongRecordVO[]>([]);

const allPapers = ref<Paper[]>([]);
const selectedPaper = ref<number | null>(null);
const paperResult = ref<PaperStatsVO[]>([]);

// 统计计算属性
const totalRecords = computed(() => {
  return studentResult.value.length + questionResult.value.length;
});

const totalStudents = computed(() => {
  return allStudents.value.length;
});

const totalQuestions = computed(() => {
  const uniqueQuestions = new Set([
    ...studentResult.value.map(r => r.questionContent),
    ...questionResult.value.map(r => r.questionContent)
  ].filter(Boolean));
  return uniqueQuestions.size;
});

const totalPapers = computed(() => {
  return allPapers.value.length;
});

const loadInitialData = async () => {
  const studentRes = await fetchStudentList({ current: 1, size: 9999 });
  allStudents.value = studentRes.data;
  const paperRes = await fetchPaperList({ current: 1, size: 9999 });
  allPapers.value = paperRes.data;
};

const queryByStudent = async (studentId: number) => {
  if (!studentId) return;
  const res = await fetchRecordsByStudent(studentId);
  studentResult.value = res.data;
};

const searchQuestions = async (query: string) => {
  if (query) {
    const res = await fetchQuestionList({ current: 1, size: 50 });
    questionOptions.value = res.data.filter(q => q.content.toLowerCase().includes(query.toLowerCase()));
  } else {
    questionOptions.value = [];
  }
};

const queryByQuestion = async (questionId: number) => {
  if (!questionId) return;
  const res = await fetchStudentsByQuestion(questionId);
  questionResult.value = res.data;
};

const queryByPaper = async (paperId: number) => {
  if (!paperId) return;
  loading.value = true;
  try {
    const res = await fetchStatsByPaper(paperId);
    paperResult.value = res.data;
  } catch (error) {
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};

const handleRefresh = () => {
  loadInitialData();
  ElMessage.success('刷新成功');
};

const handleExport = () => {
  ElMessage.info('导出功能开发中...');
};

const getErrorCountType = (count: number) => {
  if (count >= 10) return 'danger';
  if (count >= 5) return 'warning';
  return 'success';
};

onMounted(loadInitialData);
</script>

<style scoped>
/* 页面容器 */
.stats-container {
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

.stat-questions::before { background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); }
.stat-questions .stat-icon {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}
.stat-questions .stat-value { color: #667eea; }

.stat-papers::before { background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%); }
.stat-papers .stat-icon {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(235, 181, 99, 0.1) 100%);
  color: #e6a23c;
}
.stat-papers .stat-value { color: #e6a23c; }

/* 内容卡片 */
.content-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

/* 标签页样式 */
.stats-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 20px 0;
}

.search-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 表格单元格样式 */
.user-cell, .paper-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .stats-container {
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

  .search-section {
    flex-direction: column;
    align-items: stretch;
  }

  .search-section .el-select {
    width: 100% !important;
  }
}
</style>