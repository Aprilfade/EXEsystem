<template>
  <div class="subject-manage-container">
    <el-card shadow="never">
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增科目</el-button>
    </el-card>

    <el-card shadow="never" class="table-container">
      <el-table v-loading="loading" :data="subjectList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="科目名称" />
        <el-table-column prop="description" label="简介" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
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

    <subject-edit-dialog
        v-model:visible="isDialogVisible"
        :subject-data="editingSubject"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchSubjectList, deleteSubject } from '@/api/subject';
import type { Subject, SubjectPageParams } from '@/api/subject';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import SubjectEditDialog from '@/components/subject/SubjectEditDialog.vue';

const subjectList = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingSubject = ref<Subject | undefined>(undefined);

const queryParams = reactive<SubjectPageParams>({
  current: 1,
  size: 10,
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchSubjectList(queryParams);
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
  ElMessageBox.confirm('确定要删除该科目吗? (请确保其下无任何试题或知识点)', '提示', { type: 'warning' })
      .then(async () => {
        await deleteSubject(id);
        ElMessage.success('删除成功');
        getList();
      })
      .catch(() => {});
};

onMounted(getList);
</script>

<style scoped>
.subject-manage-container {
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