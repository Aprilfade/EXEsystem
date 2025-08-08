<template>
  <div class="knowledge-point-manage-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="科目">
          <el-select v-model="queryParams.subjectId" placeholder="请选择科目" clearable @change="handleQuery">
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点名称">
          <el-input v-model="queryParams.name" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新增知识点</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-container">
      <el-table v-loading="loading" :data="knowledgePointList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="知识点名称" />
        <el-table-column prop="code" label="编码" />
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

    <knowledge-point-edit-dialog
        v-model:visible="isDialogVisible"
        :knowledge-point-id="editingId"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchKnowledgePointList, deleteKnowledgePoint } from '@/api/knowledgePoint';
import type { KnowledgePoint, KnowledgePointPageParams } from '@/api/knowledgePoint';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue';
import KnowledgePointEditDialog from '@/components/knowledge-point/KnowledgePointEditDialog.vue';

const knowledgePointList = ref<KnowledgePoint[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);

const queryParams = reactive<KnowledgePointPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined,
  name: ''
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchKnowledgePointList(queryParams);
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

const handleQuery = () => {
  queryParams.current = 1;
  getList();
}

const resetQuery = () => {
  queryParams.current = 1;
  queryParams.subjectId = undefined;
  queryParams.name = '';
  getList();
}

const handleCreate = () => {
  editingId.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (id: number) => {
  editingId.value = id;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该知识点吗? (请确保其下无任何关联试题)', '提示', { type: 'warning' })
      .then(async () => {
        await deleteKnowledgePoint(id);
        ElMessage.success('删除成功');
        getList();
      })
      .catch(() => {});
};

onMounted(() => {
  getList();
  getAllSubjects();
});
</script>

<style scoped>
.knowledge-point-manage-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.table-container {
  margin-top: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>