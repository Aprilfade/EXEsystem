<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>题库管理</h2>
        <p>覆盖多学科题型，支持丰富题目的创建与创作</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">新增试题</el-button>
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
        <el-input v-model="queryParams.content" placeholder="输入题干内容搜索" size="large" style="width: 300px;" @keyup.enter="handleQuery" clearable @clear="handleQuery"/>
        <div>
          <el-select v-model="queryParams.subjectId" placeholder="按科目筛选" clearable @change="handleQuery" size="large" style="width: 150px; margin-right: 10px;">
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
          </el-select>
          <el-select v-model="queryParams.questionType" placeholder="按题型筛选" clearable @change="handleQuery" size="large" style="width: 150px; margin-right: 20px;">
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

      <div v-if="viewMode === 'grid'" class="card-grid">
        <div v-for="q in questionList" :key="q.id" class="question-card" @click="handlePreview(q.id)">
          <div class="card-header">
            <el-tag size="small">{{ getSubjectName(q.subjectId) }}</el-tag>
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

      <el-table v-if="viewMode === 'list'" :data="questionList" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="content" label="题干" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template #default="scope">
            <el-tag>{{ questionTypeMap[scope.row.questionType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属科目" width="150">
          <template #default="scope">
            {{ getSubjectName(scope.row.subjectId) }}
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
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchQuestionList, deleteQuestion, fetchQuestionById } from '@/api/question';
import type { Question, QuestionPageParams } from '@/api/question';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { getDashboardStats } from '@/api/dashboard';
import { fetchKnowledgePointList } from '@/api/knowledgePoint';
import type { KnowledgePoint } from '@/api/knowledgePoint';
import { Plus, Edit, Delete, Grid, Menu, MoreFilled, View } from '@element-plus/icons-vue';
import QuestionEditDialog from '@/components/question/QuestionEditDialog.vue';
import QuestionPreviewDialog from '@/components/question/QuestionPreviewDialog.vue';

const questionList = ref<Question[]>([]);
const allSubjects = ref<Subject[]>([]);
const allKnowledgePoints = ref<KnowledgePoint[]>([]);
const total = ref(0);
const loading = ref(true);
const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);
const viewMode = ref<'grid' | 'list'>('grid');

const queryParams = reactive<QuestionPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined,
  questionType: undefined,
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
  return ((duplicateCount.value / total.value) * 100).toFixed(0);
});

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

// 【修复点】恢复 getExtraStats 函数的定义
const getExtraStats = async () => {
  try {
    const res = await getDashboardStats();
    if (res.code === 200) {
      // 确保只获取需要的数据
      duplicateCount.value = res.data.duplicateCount;
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

onMounted(() => {
  getAllSubjects().then(getList);
  getExtraStats();
  getAllKnowledgePoints();
});
</script>
<style scoped>
/* 样式部分无需改动 */
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
.question-card {
  transition: all 0.2s ease-in-out;
  cursor: pointer;
}

.question-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
</style>