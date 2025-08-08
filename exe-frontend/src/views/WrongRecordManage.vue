<template>
  <div class="wrong-record-manage-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="queryParams" size="large">
        <el-form-item label="选择学生">
          <el-select
              v-model="queryParams.studentId"
              filterable
              placeholder="可输入姓名搜索"
              clearable
              style="width: 240px;"
              @change="handleQuery"
          >
            <el-option v-for="s in allStudents" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新增错题</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-container">
      <el-table v-loading="loading" :data="recordList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="studentName" label="学生姓名" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column prop="questionContent" label="题干" show-overflow-tooltip />
        <el-table-column prop="paperName" label="所属试卷" />
        <el-table-column prop="wrongReason" label="错误原因" show-overflow-tooltip />
        <el-table-column prop="createTime" label="记录时间" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
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

    <wrong-record-edit-dialog
        v-model:visible="isDialogVisible"
        :record-data="editingRecord"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchWrongRecordList, deleteWrongRecord } from '@/api/wrongRecord';
import type { WrongRecordVO, WrongRecordPageParams } from '@/api/wrongRecord';
import { fetchStudentList, type Student } from '@/api/student';
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue';
import WrongRecordEditDialog from '@/components/wrong-record/WrongRecordEditDialog.vue';

const recordList = ref<WrongRecordVO[]>([]);
const allStudents = ref<Student[]>([]);
const total = ref(0);
const loading = ref(true);
const isDialogVisible = ref(false);
const editingRecord = ref<WrongRecordVO | undefined>(undefined);

const queryParams = reactive<WrongRecordPageParams>({
  current: 1,
  size: 10,
  studentId: undefined,
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchWrongRecordList(queryParams);
    recordList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const loadAllStudents = async () => {
  const res = await fetchStudentList({current: 1, size: 9999});
  allStudents.value = res.data;
}

const handleQuery = () => {
  queryParams.current = 1;
  getList();
};

const resetQuery = () => {
  queryParams.studentId = undefined;
  handleQuery();
};

const handleCreate = () => {
  editingRecord.value = undefined;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这条错题记录吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deleteWrongRecord(id);
        ElMessage.success('删除成功');
        getList();
      })
      .catch(() => {});
};

onMounted(() => {
  loadAllStudents();
  getList();
});
</script>

<style scoped>
.wrong-record-manage-container {
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