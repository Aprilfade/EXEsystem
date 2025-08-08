<template>
  <div class="subject-manage-container">
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
        <el-input v-model="searchQuery" placeholder="输入关键词搜索" size="large" style="width: 300px;"/>
        <div>
          <el-button-group>
            <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'"/>
            <el-button :icon="Menu" :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'"/>
          </el-button-group>
        </div>
      </div>

      <div v-if="viewMode === 'grid'" class="card-grid">
        <div v-for="subject in filteredList" :key="subject.id" class="subject-card">
          <h3>{{ subject.name }}</h3>
          <p>{{ subject.description || '暂无简介' }}</p>
          <span>关联知识点: {{ subject.knowledgePointCount }}个</span>
        </div>
      </div>
      <el-table v-if="viewMode === 'list'" :data="filteredList" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="name" label="科目名称" />
        <el-table-column prop="description" label="简介" />
        <el-table-column prop="knowledgePointCount" label="关联知识点数" width="150" align="center" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

    </el-card>

    <subject-edit-dialog
        v-model:visible="isDialogVisible"
        :subject-data="editingSubject"
        @success="getList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchSubjectList, deleteSubject } from '@/api/subject';
import type { Subject, SubjectPageParams } from '@/api/subject';
import { Plus, Edit, Delete, Grid, Menu } from '@element-plus/icons-vue';
import SubjectEditDialog from '@/components/subject/SubjectEditDialog.vue';

const subjectList = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);
const isDialogVisible = ref(false);
const editingSubject = ref<Subject | undefined>(undefined);
const viewMode = ref<'grid' | 'list'>('grid');
const searchQuery = ref('');




const filteredList = computed(() => {
  // 如果搜索框为空，直接返回原始列表
  if (!searchQuery.value) {
    return subjectList.value;
  }

  // 将搜索词统一转为小写，以便进行不区分大小写的搜索
  const query = searchQuery.value.toLowerCase();

  // 使用 .filter 方法过滤列表
  return subjectList.value.filter(item => {
    // 关键修复点：在访问任何属性之前，先确保 item 是一个有效的对象
    if (!item) {
      return false;
    }

    // 安全地检查 name 属性是否存在，并且是否匹配搜索词
    const nameMatch = item.name && item.name.toLowerCase().includes(query);

    // 安全地检查 description 属性是否存在，并且是否匹配搜索词
    const descriptionMatch = item.description && item.description.toLowerCase().includes(query);

    // 只要名称或描述中有一个匹配，就返回 true
    return nameMatch || descriptionMatch;
  });
});
const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchSubjectList({current: 1, size: 999}); // 获取所有数据用于前端筛选
    subjectList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};
const handleCreate = () => {
  editingSubject.value = undefined;
  isDialogVisible.value = true;
};
const handleUpdate = (subject: Subject) => {
  editingSubject.value = subject;
  isDialogVisible.value = true;
};
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该科目吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deleteSubject(id);
        ElMessage.success('删除成功');
        getList();
      });
};
onMounted(getList);
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 24px; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; }
.stats-cards { margin-bottom: 20px; }
.stat-item .label { color: var(--text-color-regular); font-size: 14px; margin-bottom: 8px;}
.stat-item .value { font-size: 28px; font-weight: bold;}
.content-card {
  background-color: var(--bg-color-container);
}
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.subject-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  background-color: #f7f9fc;
}
.subject-card h3 { margin-bottom: 8px; }
.subject-card p { font-size: 14px; color: var(--text-color-regular); margin-bottom: 16px;}
.subject-card span { font-size: 12px; color: #999; }
</style>