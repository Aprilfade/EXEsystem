<template>
  <div class="paper-manage-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="科目" size="large">
          <el-select
              v-model="queryParams.subjectId"
              placeholder="请选择科目 (可清空查所有)"
              clearable
              @change="handleQuery"
              style="width: 240px;"
          >
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
          </el-select>
        </el-form-item>
        <el-form-item size="large">
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;" size="large">
          <el-button type="primary" :icon="Plus" @click="handleCreate">手动组卷</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-container">
      <el-table v-loading="loading" :data="paperList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="试卷名称" show-overflow-tooltip />
        <el-table-column prop="code" label="编码" width="150" />
        <el-table-column label="所属科目" width="150">
          <template #default="scope">
            {{ getSubjectName(scope.row.subjectId) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column label="操作" width="280" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
            <el-button type="success" link :icon="Download" @click="handleExport(scope.row.id, false)">导出</el-button>
            <el-button type="warning" link :icon="Download" @click="handleExport(scope.row.id, true)">导出(含答案)</el-button>
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

    <paper-edit-dialog
        v-if="isDialogVisible"
        v-model:visible="isDialogVisible"
        :paper-id="editingId"
        :subjects="allSubjects"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
// ... <script>部分的代码保持不变 ...
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchPaperList, deletePaper, downloadPaper } from '@/api/paper';
import type { Paper, PaperPageParams } from '@/api/paper';
import { fetchAllSubjects, type Subject } from '@/api/subject';
import { Plus, Edit, Delete, Search, Refresh, Download } from '@element-plus/icons-vue';
import PaperEditDialog from '@/components/paper/PaperEditDialog.vue';

const paperList = ref<Paper[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);

const queryParams = reactive<PaperPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchPaperList(queryParams);
    paperList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const getAllSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) allSubjects.value = res.data;
};

const getSubjectName = (subjectId: number) => {
  const subject = allSubjects.value.find((s: Subject) => s.id === subjectId);
  return subject ? subject.name : '未知科目';
};
const handleQuery = () => {
  queryParams.current = 1;
  getList();
};

const resetQuery = () => {
  queryParams.current = 1;
  queryParams.subjectId = undefined;
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
  ElMessageBox.confirm('确定要删除该试卷吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deletePaper(id);
        ElMessage.success('删除成功');
        getList();
      })
      .catch(() => {});
};

const handleExport = async (id: number, includeAnswers: boolean) => {
  try {
    const response = await downloadPaper(id, includeAnswers);
    const contentDisposition = response.headers['content-disposition'];
    let fileName = '试卷.docx';
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="(.+)"/);
      if (match && match.length > 1) {
        fileName = decodeURIComponent(match[1]);
      }
    }

    const blob = new Blob([response.data], { type: response.headers['content-type'] });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('文件导出失败:', error);
    ElMessage.error('文件导出失败，请稍后重试。');
  }
};

onMounted(() => {
  getAllSubjects().then(getList);
});
</script>

<style scoped>
/* ... <style>部分的代码保持不变 ... */
.paper-manage-container {
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