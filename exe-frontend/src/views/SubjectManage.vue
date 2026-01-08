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
        <div class="header-left">
          <el-select
              v-model="queryParams.grade"
              placeholder="全部年级"
              clearable
              style="width: 140px; margin-right: 10px;"
              size="large"
              @change="handleQuery"
          >
            <el-option v-for="g in GRADE_OPTIONS" :key="g" :label="g" :value="g" />
          </el-select>

          <el-input
              v-model="queryParams.name"
              placeholder="输入科目名称搜索"
              size="large"
              style="width: 260px;"
              @keyup.enter="handleQuery"
              clearable
              @clear="handleQuery"
          >
            <template #append>
              <el-button :icon="Search" @click="handleQuery" />
            </template>
          </el-input>
        </div>

        <div class="header-right">
          <el-button-group>
            <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'"/>
            <el-button :icon="Menu" :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'"/>
          </el-button-group>
        </div>
      </div>

      <div v-if="viewMode === 'grid'" class="card-grid">
        <div v-for="subject in subjectList" :key="subject.id" class="subject-card">
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 8px;">
              <h3 class="card-title">{{ subject.name }}</h3>
              <el-tag v-if="subject.grade" size="small" effect="plain">{{ subject.grade }}</el-tag>
            </div>
            <el-dropdown @click.stop>
              <span class="el-dropdown-link"><el-icon><MoreFilled /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :icon="Edit" @click="handleUpdate(subject)">编辑</el-dropdown-item>
                  <el-dropdown-item :icon="Delete" @click="handleDelete(subject.id)" style="color: #f56c6c;">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <p class="card-desc">{{ subject.description || '暂无简介' }}</p>
          <div class="card-footer">
            <div class="stat-badge">
              <span class="num">{{ subject.knowledgePointCount || 0 }}</span>
              <span class="txt">知识点</span>
            </div>
            <div class="stat-badge">
              <span class="num">{{ subject.questionCount || 0 }}</span>
              <span class="txt">试题</span>
            </div>
          </div>
        </div>
      </div>

      <el-table
          v-if="viewMode === 'list'"
          :data="subjectList"
          v-loading="loading"
          style="width: 100%; margin-top: 20px;"
          @sort-change="handleSortChange"
      >
        <el-table-column prop="name" label="科目名称" min-width="150" />
        <el-table-column prop="grade" label="年级" width="120" sortable="custom" />
        <el-table-column prop="description" label="简介" show-overflow-tooltip />
        <el-table-column prop="knowledgePointCount" label="知识点" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info" effect="plain">{{ row.knowledgePointCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="试题量" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success" effect="plain">{{ row.questionCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable="custom" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-if="viewMode === 'list' || total > 0"
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

    <subject-edit-dialog
        v-if="isDialogVisible"
        v-model:visible="isDialogVisible"
        :subject-data="editingSubject"
        @success="getList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchSubjectList, deleteSubject } from '@/api/subject';
import type { Subject, SubjectPageParams } from '@/api/subject';
import { Plus, Edit, Delete, Grid, Menu, MoreFilled, Search } from '@element-plus/icons-vue';
import SubjectEditDialog from '@/components/subject/SubjectEditDialog.vue';
import type { Sort } from 'element-plus';
import { GRADE_OPTIONS } from '@/utils/constants'; // 引入常量

const subjectList = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);
const isDialogVisible = ref(false);
const editingSubject = ref<Subject | undefined>(undefined);
const viewMode = ref<'grid' | 'list'>('grid');

const queryParams = reactive<SubjectPageParams>({
  current: 1,
  size: 10,
  name: '',
  grade: '', // 新增
  sortField: 'createTime',
  sortOrder: 'desc'
});

const getList = async () => {
  loading.value = true;
  try {
    // 即使在 grid 模式下，也应该支持筛选，所以这里不再使用硬编码参数
    const response = await fetchSubjectList(queryParams);
    subjectList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.current = 1;
  getList();
}

// 监听视图切换，无需重置页码，保持用户上下文
watch(viewMode, () => {
  getList();
});

const handleCreate = () => {
  editingSubject.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (subject: Subject) => {
  editingSubject.value = subject;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该科目吗？系统会检查是否存在关联数据，如有关联数据将无法删除。', '确认删除', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
      .then(async () => {
        try {
          const result = await deleteSubject(id);
          if (result.code === 200) {
            ElMessage.success('删除成功');
            getList();
          }
          // 错误信息由拦截器自动处理
        } catch (error) {
          // 异常已由拦截器处理
        }
      })
      .catch(() => {
        // 用户取消删除
      });
};

const handleSortChange = ({ prop, order }: Sort) => {
  if (order) {
    queryParams.sortField = prop;
    queryParams.sortOrder = order === 'ascending' ? 'asc' : 'desc';
  } else {
    queryParams.sortField = 'createTime';
    queryParams.sortOrder = 'desc';
  }
  getList();
};

onMounted(getList);
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

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-left {
  display: flex;
  align-items: center;
}

.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }

.subject-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s;
  background: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
}
.subject-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  border-color: var(--brand-color, #409eff);
}

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.card-title { font-size: 18px; font-weight: 600; margin: 0; color: #303133; }
.card-desc { color: #909399; font-size: 14px; margin-bottom: 20px; flex-grow: 1; min-height: 40px; line-height: 1.5; }

.card-footer {
  display: flex;
  gap: 15px;
  padding-top: 15px;
  border-top: 1px solid #f2f6fc;
}
.stat-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}
.stat-badge .num {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}
.stat-badge .txt {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.el-dropdown-link { cursor: pointer; color: #909399; }
.el-dropdown-link:hover { color: var(--brand-color, #409eff); }
.pagination { margin-top: 20px; text-align: right; }
</style>