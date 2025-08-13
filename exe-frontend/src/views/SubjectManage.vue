<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>科目管理</h2>
        <p>有序归类不同学科与年级的教学科目</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">新增科目</el-button>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">科目总创建数</p>
            <p class="value">{{ total }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-input
            v-model="queryParams.name"
            placeholder="输入关键词搜索"
            size="large"
            style="width: 300px;"
            @keyup.enter="handleQuery"
            clearable
            @clear="handleQuery"
        />
        <div>
          <el-button-group>
            <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'"/>
            <el-button :icon="Menu" :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'"/>
          </el-button-group>
        </div>
      </div>

      <div v-if="viewMode === 'grid'" class="card-grid">
        <div v-for="subject in subjectList" :key="subject.id" class="subject-card" @click="handleCardClick(subject)">
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 8px;">
              <h3 class="card-title">{{ subject.name }}</h3>
              <el-tag v-if="subject.grade" size="small">{{ subject.grade }}</el-tag>
            </div>
            <el-dropdown @click.stop>
              <el-icon class="el-dropdown-link"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleUpdate(subject)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(subject.id)" style="color: #f56c6c;">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <p class="card-desc">{{ subject.description || '暂无简介' }}</p>
          <div class="card-footer">
            <span>知识点: {{ subject.knowledgePointCount || 0 }}个</span>
            <span>试题: {{ subject.questionCount || 0 }}道</span>
          </div>
        </div>
      </div>

      <el-table v-if="viewMode === 'list'" :data="subjectList" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="name" label="科目名称" />
        <el-table-column prop="grade" label="年级" width="120" /> <el-table-column prop="description" label="简介" />
        <el-table-column prop="knowledgePointCount" label="关联知识点数" width="150" align="center" />
        <el-table-column prop="questionCount" label="关联试题数" width="150" align="center" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
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
          @current-change="getList" />
    </el-card>

    <subject-edit-dialog
        v-if="isDialogVisible"
        v-model:visible="isDialogVisible"
        :subject-data="editingSubject"
        @success="getList"
    />
    <subject-detail-dialog
        v-if="isDetailDialogVisible"
        v-model:visible="isDetailDialogVisible"
        :subject-id="selectedSubjectId"
        :subject-name="selectedSubjectName"
    />
  </div>
</template>

<script setup lang="ts">
// 1. 确认导入了 computed 和 watch
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchSubjectList, deleteSubject } from '@/api/subject';
import type { Subject, SubjectPageParams } from '@/api/subject';
import { Plus, Edit, Delete, Grid, Menu, MoreFilled } from '@element-plus/icons-vue';
import SubjectEditDialog from '@/components/subject/SubjectEditDialog.vue';
import SubjectDetailDialog from "@/components/subject/SubjectDetailDialog.vue";

const subjectList = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);
const isDialogVisible = ref(false);
const editingSubject = ref<Subject | undefined>(undefined);
const viewMode = ref<'grid' | 'list'>('grid');
const isDetailDialogVisible = ref(false);
const selectedSubjectId = ref<number | null>(null);
const selectedSubjectName = ref('');

const queryParams = reactive<SubjectPageParams>({
  current: 1,
  size: 10,
  name: ''
});

// 2. 【核心修改】改造 getList 函数
const getList = async () => {
  loading.value = true;
  try {
    // 根据视图模式决定是分页加载还是全部加载
    const paramsToUse = viewMode.value === 'list'
        ? queryParams
        : { current: 1, size: 9999, name: queryParams.name };

    const response = await fetchSubjectList(paramsToUse);
    subjectList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

// 3. 【新增】监听视图模式的变化
watch(viewMode, () => {
  // 当切换到列表视图时，确保页码回到第一页
  if (viewMode.value === 'list') {
    queryParams.current = 1;
  }
  // 切换视图后总是重新加载数据
  getList();
});

const handleQuery = () => {
  queryParams.current = 1;
  getList();
}

const handleCreate = () => {
  editingSubject.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (subject: Subject) => {
  editingSubject.value = subject;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该科目吗? 这会一并删除所有关联数据。', '提示', { type: 'warning' })
      .then(async () => {
        await deleteSubject(id);
        ElMessage.success('删除成功');
        getList();
      });
};

const handleCardClick = (subject: Subject) => {
  selectedSubjectId.value = subject.id;
  selectedSubjectName.value = subject.name;
  isDetailDialogVisible.value = true;
};

onMounted(getList);
</script>

<style scoped>
/* ... style 部分无需改动 ... */
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

/* 【修改点 3】: 新增和优化卡片相关样式 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

/* 【修改点 7】: 为 subject-card 添加 cursor: pointer 以提示用户可以点击 */
.subject-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  background-color: var(--bg-color);
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease-in-out;
  cursor: pointer; /* 新增此行 */
}
.subject-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
}
.el-dropdown-link {
  cursor: pointer;
  color: var(--text-color-regular);
  font-size: 20px;
}
.card-desc {
  font-size: 14px;
  color: var(--text-color-regular);
  flex-grow: 1;
  min-height: 40px;
  margin-bottom: 16px;
}
.card-footer {
  font-size: 12px;
  color: #999;
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
  margin-top: auto;
  display: flex;
  justify-content: space-between;
}
</style>