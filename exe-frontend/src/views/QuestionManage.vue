<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>题库管理</h2>
        <p>覆盖多学科题型，支持丰富题目的创建与创作</p>
      </div>
      <div class="header-actions">
        <el-button
            type="success"
            :icon="MagicStick"
            size="large"
            @click="router.push('/text-to-quiz')"
        >
          AI 智能出题
        </el-button>

        <el-button
            type="primary"
            :icon="Plus"
            size="large"
            @click="handleCreate"
            style="margin-left: 12px;"
        >
          新增试题
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">题目总数</p>
            <p class="value">{{ total }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">重复题目占比</p>
            <p class="value">{{ duplicatePercentage }}%</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <div class="header-left-panel">
          <el-button
              type="primary"
              :icon="Edit"
              size="large"
              :disabled="selectedQuestionIds.length === 0"
              @click="handleBatchEdit"
          >
            批量修改
          </el-button>

          <el-button
              type="danger"
              :icon="Delete"
              size="large"
              :disabled="selectedQuestionIds.length === 0"
              @click="handleBatchDelete"
              style="margin-left: 12px;"
          >
            批量删除
          </el-button>

          <el-upload
              :action="''"
              :show-file-list="false"
              :http-request="handleImport"
              style="margin-left: 12px; display: inline-block;"
          >
            <el-button size="large" :icon="Upload">导入</el-button>
          </el-upload>
          <el-button size="large" :icon="Download" @click="handleExport" style="margin-left: 12px;">导出</el-button>
        </div>
        <div class="header-right-panel">
          <el-input v-model="queryParams.content" placeholder="输入题干内容搜索" size="large" style="width: 240px;" @keyup.enter="handleQuery" clearable @clear="handleQuery"/>
          <el-select v-model="queryParams.subjectId" placeholder="按科目筛选" clearable @change="handleQuery" size="large" style="width: 140px;">
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="`${sub.name} (${sub.grade})`" :value="sub.id" />
          </el-select>
          <el-select v-model="queryParams.grade" placeholder="按年级筛选" clearable @change="handleQuery" size="large" style="width: 140px;">
            <el-option label="七年级" value="七年级" />
            <el-option label="八年级" value="八年级" />
            <el-option label="九年级" value="九年级" />
            <el-option label="高一" value="高一" />
            <el-option label="高二" value="高二" />
            <el-option label="高三" value="高三" />
          </el-select>
          <el-select v-model="queryParams.questionType" placeholder="按题型筛选" clearable @change="handleQuery" size="large" style="width: 140px;">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="填空题" :value="3" />
            <el-option label="判断题" :value="4" />
            <el-option label="主观题" :value="5" />
          </el-select>
          <el-button-group>
            <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'"/>
            <el-button :icon="Menu" :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'"/>
          </el-button-group>
        </div>
      </div>

      <div v-if="viewMode === 'grid'" class="card-grid" v-loading="loading" element-loading-text="加载中...">
        <div v-for="q in questionList" :key="q.id" class="question-card" @click="handlePreview(q.id)">
          <div class="card-header">
            <div>
              <el-tag size="small">{{ getSubjectName(q.subjectId) }}</el-tag>
              <el-tag v-if="q.grade" size="small" type="success" style="margin-left: 8px;">{{ q.grade }}</el-tag>
            </div>
            <el-dropdown @click.stop>
              <el-icon class="el-dropdown-link"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleUpdate(q.id)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(q.id)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <p class="card-content">{{ q.content }}</p>
          <div class="card-footer">
            <el-tag type="info" size="small">{{ questionTypeMap[q.questionType] }}</el-tag>
          </div>
        </div>
      </div>

      <el-table
          v-if="viewMode === 'list'"
          :data="questionList"
          v-loading="loading"
          style="width: 100%; margin-top: 20px;"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="content" label="题干" show-overflow-tooltip />
        <el-table-column label="所属科目" width="150">
          <template #default="scope">
            {{ getSubjectName(scope.row.subjectId) }}
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="100" align="center" />
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template #default="scope">
            <el-tag>{{ questionTypeMap[scope.row.questionType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="scope">
            <el-button type="success" link :icon="View" @click="handlePreview(scope.row.id)">预览</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
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

    <question-edit-dialog
        v-if="isDialogVisible"
        v-model:visible="isDialogVisible"
        :question-id="editingId"
        @success="getList"
    />

    <question-preview-dialog
        v-if="isPreviewVisible"
        v-model:visible="isPreviewVisible"
        :question="selectedQuestion"
        :all-subjects="allSubjects"
        :all-knowledge-points="allKnowledgePoints"
    />

    <question-batch-edit-dialog
        v-if="isBatchEditDialogVisible"
        v-model:visible="isBatchEditDialogVisible"
        :question-ids="selectedQuestionIds"
        @success="handleBatchSuccess"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  fetchQuestionList,
  deleteQuestion,
  fetchQuestionById,
  importQuestions,
  exportQuestions,
  batchDeleteQuestions // <--- 新增引入
} from '@/api/question';
import type { Question, QuestionPageParams } from '@/api/question';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { getDashboardStats } from '@/api/dashboard';
import { fetchKnowledgePointList } from '@/api/knowledgePoint';
import type { KnowledgePoint } from '@/api/knowledgePoint';
// 2. [修改] 在图标引入中添加 MagicStick
import { Plus, Edit, Delete, Grid, Menu, MoreFilled, View, Upload, Download, MagicStick } from '@element-plus/icons-vue';
import QuestionEditDialog from '@/components/question/QuestionEditDialog.vue';
import QuestionPreviewDialog from '@/components/question/QuestionPreviewDialog.vue';
import QuestionBatchEditDialog from '@/components/question/QuestionBatchEditDialog.vue';
import type { UploadRequestOptions } from 'element-plus';
// 1. [新增] 引入 useRouter
import { useRouter } from 'vue-router';




const questionList = ref<Question[]>([]);
const allSubjects = ref<Subject[]>([]);
const allKnowledgePoints = ref<KnowledgePoint[]>([]);
const total = ref(0);
const loading = ref(true);
const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);
const viewMode = ref<'list' | 'grid'>('list'); // 默认改为列表视图
const selectedQuestionIds = ref<number[]>([]);
const isBatchEditDialogVisible = ref(false);

// 3. [新增] 初始化 router
const router = useRouter();

const queryParams = reactive<QuestionPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined,
  questionType: undefined,
  grade: undefined,
  content: ''
});

const questionTypeMap: { [key: number]: string } = {
  1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题',
};

const isPreviewVisible = ref(false);
const selectedQuestion = ref<Question | undefined>(undefined);
const duplicateCount = ref(0);

const duplicatePercentage = computed(() => {
  if (total.value === 0) return 0;
  // 确保 duplicateCount.value 是一个数字
  const count = Number(duplicateCount.value) || 0;
  return ((count / total.value) * 100).toFixed(0);
});

// 【数据加载逻辑修复】
const getList = async () => {
  loading.value = true;
  try {
    const res = await fetchQuestionList(queryParams);
    if (res.code === 200) {
      questionList.value = res.data;
      total.value = res.total;
    }
  } finally {
    loading.value = false;
  }
};


// ✅ 优化：移除视图切换时的自动刷新逻辑
// 视图切换（grid/list）只是展示方式的改变，不需要重新请求数据
// watch(viewMode, () => {
//   queryParams.current = 1;
//   getList();
// });


const getExtraStats = async () => {
  try {
    const res = await getDashboardStats();
    if (res.code === 200) {
      duplicateCount.value = res.data.duplicateCount ?? 0;
    }
  } catch (error) {
    console.warn("获取额外统计数据失败:", error);
  }
};

const getAllSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) allSubjects.value = res.data;
};

const getAllKnowledgePoints = async () => {
  const res = await fetchKnowledgePointList({ current: 1, size: 9999 });
  if (res.code === 200) {
    allKnowledgePoints.value = res.data;
  }
};

const getSubjectName = (subjectId: number) => {
  const subject = allSubjects.value.find(s => s.id === subjectId);
  return subject ? subject.name : '未知';
};

const handleQuery = () => {
  queryParams.current = 1;
  getList();
};

const handleCreate = () => {
  editingId.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (id: number) => {
  editingId.value = id;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该试题吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deleteQuestion(id);
        ElMessage.success('删除成功');
        getList();
      });
};

const handlePreview = async (id: number) => {
  const res = await fetchQuestionById(id);
  if (res.code === 200) {
    selectedQuestion.value = res.data;
    isPreviewVisible.value = true;
  } else {
    ElMessage.error('获取题目详情失败');
  }
};
const handleImport = async (options: UploadRequestOptions) => {
  const formData = new FormData();
  formData.append('file', options.file);
  try {
    await importQuestions(formData);
    ElMessage.success('导入成功');
    getList();
  } catch (error) {
    ElMessage.error('导入失败');
  }
};

const handleExport = async () => {
  try {
    ElMessage.info('正在生成Excel文件，请稍候...');
    const response = await exportQuestions(queryParams);

    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '题库试题.xlsx');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    ElMessage.error('导出失败，请稍后重试。');
  }
};

// 【新增】批量删除处理逻辑
const handleBatchDelete = () => {
  if (selectedQuestionIds.value.length === 0) return;

  ElMessageBox.confirm(
      `确定要批量删除选中的 ${selectedQuestionIds.value.length} 道试题吗? 此操作不可恢复。`,
      '高危操作提示',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
  ).then(async () => {
    loading.value = true;
    try {
      await batchDeleteQuestions(selectedQuestionIds.value);
      ElMessage.success('批量删除成功');
      // 清空选中状态
      selectedQuestionIds.value = [];
      // 重新加载列表
      getList();
    } catch (error) {
      // 错误已由 request 拦截器处理，此处可留空或打印日志
      console.error(error);
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 用户取消删除
  });
};

const handleSelectionChange = (selectedRows: Question[]) => {
  selectedQuestionIds.value = selectedRows.map(row => row.id);
};

const handleBatchEdit = () => {
  isBatchEditDialogVisible.value = true;
};

const handleBatchSuccess = () => {
  isBatchEditDialogVisible.value = false;
  selectedQuestionIds.value = [];
  getList();
};

onMounted(() => {
  getAllSubjects().then(getList);
  getExtraStats();
  getAllKnowledgePoints();
});
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; font-size: 14px; }
.stats-cards { margin-bottom: 20px; }
.stat-item { padding: 8px; }
.stat-item .label { color: var(--text-color-regular); font-size: 14px; margin-bottom: 8px;}
.stat-item .value { font-size: 28px; font-weight: bold; }
.content-card { background-color: var(--bg-color-container); }
.content-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
/* 【布局修复】新增 flex 布局和间距 */
.header-left-panel, .header-right-panel {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }

/* 卡片样式 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
.question-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  background-color: var(--bg-color);
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease-in-out;
  cursor: pointer;
}
.question-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.el-dropdown-link {
  cursor: pointer;
  color: var(--text-color-regular);
}
.card-content {
  font-size: 14px;
  color: var(--text-color-primary);
  line-height: 1.6;
  flex-grow: 1;
  min-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  margin-bottom: 16px;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
}
</style>