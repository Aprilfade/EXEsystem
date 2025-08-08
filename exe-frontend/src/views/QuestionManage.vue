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
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">题目总数</p>
            <p class="value">{{ total }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">题目创建</p>
            <p class="value">6</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">题目附件</p>
            <p class="value">122</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">重复题目数量</p>
            <p class="value">20%</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-input v-model="searchQuery" placeholder="输入题干内容搜索" size="large" style="width: 300px;"/>
        <div>
          <el-select v-model="queryParams.subjectId" placeholder="按科目筛选" clearable @change="handleQuery" size="large" style="width: 150px; margin-right: 10px;"></el-select>
          <el-select v-model="queryParams.questionType" placeholder="按题型筛选" clearable @change="handleQuery" size="large" style="width: 150px; margin-right: 20px;"></el-select>
          <el-button-group>
            <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'"/>
            <el-button :icon="Menu" :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'"/>
          </el-button-group>
        </div>
      </div>

      <div v-if="viewMode === 'grid'" class="card-grid">
        <div v-for="q in filteredList" :key="q.id" class="question-card">
          <div class="card-header">
            <el-tag size="small">{{ getSubjectName(q.subjectId) }}</el-tag>
            <el-dropdown>
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
            <span class="card-code">QUEST-{{q.id}}</span>
            <el-tag type="info" size="small">{{ questionTypeMap[q.questionType] }}</el-tag>
          </div>
        </div>
      </div>

      <el-table v-if="viewMode === 'list'" :data="filteredList" v-loading="loading" style="width: 100%; margin-top: 20px;">
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
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-if="viewMode === 'list'"
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
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchQuestionList, deleteQuestion } from '@/api/question';
import type { Question, QuestionPageParams } from '@/api/question';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { Plus, Edit, Delete, Grid, Menu, MoreFilled } from '@element-plus/icons-vue';
import QuestionEditDialog from '@/components/question/QuestionEditDialog.vue';

const allQuestionList = ref<Question[]>([]); // 存储所有题目用于前端搜索
const questionListForTable = ref<Question[]>([]); // 专门用于表格分页的数据
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);
const viewMode = ref<'grid' | 'list'>('grid');
const searchQuery = ref('');

const queryParams = reactive<QuestionPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined,
  questionType: undefined
});

const questionTypeMap: { [key: number]: string } = {
  1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题',
};

// 前端实时筛选逻辑
const filteredList = computed(() => {
  return allQuestionList.value.filter(item => {
    const searchMatch = searchQuery.value ? item.content.toLowerCase().includes(searchQuery.value.toLowerCase()) : true;
    const subjectMatch = queryParams.subjectId ? item.subjectId === queryParams.subjectId : true;
    const typeMatch = queryParams.questionType ? item.questionType === queryParams.questionType : true;
    return searchMatch && subjectMatch && typeMatch;
  });
});

// 获取数据
const getList = async () => {
  loading.value = true;
  try {
    // 卡片视图一次性加载所有数据
    const allRes = await fetchQuestionList({ current: 1, size: 9999 });
    if (allRes.code === 200) {
      allQuestionList.value = allRes.data;
    }
    // 列表视图按需加载
    const pageRes = await fetchQuestionList(queryParams);
    if (pageRes.code === 200) {
      questionListForTable.value = pageRes.data;
      total.value = pageRes.total;
    }
  } finally {
    loading.value = false;
  }
};

const getAllSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) allSubjects.value = res.data;
};

const getSubjectName = (subjectId: number) => {
  const subject = allSubjects.value.find(s => s.id === subjectId);
  return subject ? subject.name : '未知';
};

const handleQuery = () => {
  if (viewMode.value === 'list') {
    queryParams.current = 1;
    getList();
  }
};

watch([() => queryParams.subjectId, () => queryParams.questionType], () => {
  if (viewMode.value === 'list') {
    handleQuery();
  }
});

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

onMounted(() => {
  getAllSubjects().then(getList);
});
</script>

<style scoped>
/* 复用之前的样式 */
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

/* 题库卡片专属样式 */
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
  -webkit-line-clamp: 4; /* 最多显示4行 */
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
.card-code {
  font-size: 12px;
  font-family: monospace;
  color: var(--text-color-regular);
}
</style>