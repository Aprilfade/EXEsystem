<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>知识点管理</h2>
        <p>为试题添加精准的知识体系与考点基础</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">新增知识点</el-button>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">知识点总数</p>
            <p class="value">{{ total }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">关联试题总数量</p>
            <p class="value">{{ totalAssociatedQuestions }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">未关联试题的知识点占比</p>
            <p class="value">{{ unassociatedPercentage }}%</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-input v-model="searchQuery" placeholder="输入知识点名称、编码或标签搜索" size="large" style="width: 300px;"/>
        <div>
          <el-button-group>
            <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'"/>
            <el-button :icon="Menu" :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'"/>
          </el-button-group>
        </div>
      </div>

      <div v-if="viewMode === 'grid'" class="card-grid">
        <div v-for="kp in filteredList" :key="kp.id" class="knowledge-point-card">
          <div class="card-header">
            <el-tag size="small">{{ getSubjectName(kp.subjectId) }}</el-tag>
            <el-dropdown>
              <el-icon class="el-dropdown-link"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleUpdate(kp.id)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(kp.id)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <h3 class="card-title">{{ kp.name }}</h3>
          <p class="card-desc">{{ kp.description }}</p>
          <div class="card-footer">
            <span class="card-code">CODE: {{ kp.code }}</span>
            <div class="card-tags">
              <el-tag v-if="kp.tags" v-for="tag in kp.tags.split(',')" :key="tag" type="info" size="small">{{ tag }}</el-tag>
              <el-tag type="success" size="small">题目: {{ kp.questionCount || 0 }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <el-table v-if="viewMode === 'list'" :data="filteredList" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="知识点名称" />
        <el-table-column prop="code" label="编码" />
        <el-table-column prop="questionCount" label="关联题目数" width="120" align="center"/>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <knowledge-point-edit-dialog
        v-model:visible="isDialogVisible"
        :knowledge-point-id="editingId"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchKnowledgePointList, deleteKnowledgePoint } from '@/api/knowledgePoint';
import type { KnowledgePoint, KnowledgePointPageParams } from '@/api/knowledgePoint';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { Plus, Edit, Delete, Search, Refresh, Grid, Menu, MoreFilled } from '@element-plus/icons-vue';
import KnowledgePointEditDialog from '@/components/knowledge-point/KnowledgePointEditDialog.vue';

const knowledgePointList = ref<KnowledgePoint[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);
const viewMode = ref<'grid' | 'list'>('grid');
const searchQuery = ref('');

// 计算关联试题总数
const totalAssociatedQuestions = computed(() => {
  return knowledgePointList.value.reduce((sum, kp) => sum + (kp.questionCount || 0), 0);
});

// 计算未关联试题的知识点占比
const unassociatedPercentage = computed(() => {
  if (total.value === 0) {
    return 0;
  }
  const unassociatedCount = knowledgePointList.value.filter(kp => !kp.questionCount || kp.questionCount === 0).length;
  return ((unassociatedCount / total.value) * 100).toFixed(0);
});

const filteredList = computed(() => {
  if (!searchQuery.value) {
    return knowledgePointList.value;
  }
  const query = searchQuery.value.toLowerCase();
  return knowledgePointList.value.filter(item =>
      item.name.toLowerCase().includes(query) ||
      item.code.toLowerCase().includes(query) ||
      (item.tags && item.tags.toLowerCase().includes(query))
  );
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchKnowledgePointList({ current: 1, size: 9999 }); // 获取所有数据
    knowledgePointList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const getAllSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) {
    allSubjects.value = res.data;
  }
}

const getSubjectName = (subjectId: number) => {
  const subject = allSubjects.value.find(s => s.id === subjectId);
  return subject ? subject.name : '未知科目';
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
  ElMessageBox.confirm('确定要删除该知识点吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deleteKnowledgePoint(id);
        ElMessage.success('删除成功');
        getList();
      });
};

onMounted(() => {
  getAllSubjects().then(getList);
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 24px; font-weight: 600; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; font-size: 14px; }
.stats-cards { margin-bottom: 20px; }
.stat-item { padding: 8px; }
.stat-item .label { color: var(--text-color-regular); font-size: 14px; margin-bottom: 8px;}
.stat-item .value { font-size: 28px; font-weight: bold; display: flex; align-items: center; }
.stat-item .change { font-size: 14px; margin-left: 8px; }
.stat-item .change.up { color: #67c23a; }
.stat-item .change.down { color: #f56c6c; }
.content-card { background-color: var(--bg-color-container); }
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
.knowledge-point-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  background-color: var(--bg-color);
  transition: all 0.3s;
}
.knowledge-point-card:hover {
  box-shadow: var(--card-shadow);
  transform: translateY(-5px);
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
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.card-desc {
  font-size: 14px;
  color: var(--text-color-regular);
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
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
  color: var(--text-color-regular);
}
.card-tags {
  display: flex;
  gap: 6px;
  align-items: center;
}
</style>