<template>
  <div class="student-manage-container">
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
        <el-form-item label="学生姓名" size="large">
          <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item size="large">
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;" size="large">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新增学生</el-button>
          <el-upload
              :action="''"
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
              :http-request="handleImport"
              style="margin-left: 12px; display: inline-block;"
          >
            <el-button type="success" :icon="Upload">导入Excel</el-button>
          </el-upload>
          <el-button type="warning" :icon="Download" @click="handleExport" style="margin-left: 12px;">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-container">
      <el-table v-loading="loading" :data="studentList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column label="所属科目">
          <template #default="scope">
            {{ getSubjectName(scope.row.subjectId) }}
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" />
        <el-table-column prop="contact" label="联系方式" />
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

    <student-edit-dialog
        v-model:visible="isDialogVisible"
        :student-data="editingStudent"
        :subjects="allSubjects"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchStudentList, deleteStudent, importStudents, exportStudentsExcel } from '@/api/student';
import type { Student, StudentPageParams } from '@/api/student';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { Plus, Edit, Delete, Search, Refresh, Upload, Download } from '@element-plus/icons-vue';
import StudentEditDialog from '@/components/student/StudentEditDialog.vue';
import type { UploadRequestOptions } from 'element-plus';

const studentList = ref<Student[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingStudent = ref<Student | undefined>(undefined);

const queryParams = reactive<StudentPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined, // 默认是 undefined，符合“查询所有”的逻辑
  name: ''
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchStudentList(queryParams);
    studentList.value = response.data;
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
  return subject ? subject.name : '未知';
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
  editingStudent.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (student: Student) => {
  editingStudent.value = student;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该学生吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deleteStudent(id);
        ElMessage.success('删除成功');
        getList();
      })
      .catch(() => {});
};

const handleBeforeUpload = (file: File) => {
  if (!queryParams.subjectId) {
    ElMessage.warning('请先选择一个科目再导入');
    return false;
  }
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || file.type === 'application/vnd.ms-excel';
  if (!isExcel) {
    ElMessage.error('只能上传 .xlsx 或 .xls 格式的Excel文件');
  }
  return isExcel;
}

const handleImport = async (options: UploadRequestOptions) => {
  const formData = new FormData();
  formData.append('file', options.file);
  formData.append('subjectId', queryParams.subjectId!.toString());
  try {
    await importStudents(formData);
    ElMessage.success('导入成功');
    getList();
  } catch (error) {
    console.error('导入失败', error);
    ElMessage.error('导入失败');
  }
}

const handleExport = async () => {
  try {
    ElMessage.info('正在生成Excel文件，请稍候...');
    const response = await exportStudentsExcel({
      subjectId: queryParams.subjectId,
      name: queryParams.name
    });

    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '学生列表.xlsx');
    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试。');
  }
};

onMounted(() => {
  getAllSubjects().then(getList);
});
</script>

<style scoped>
.student-manage-container {
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