<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>学生管理</h2>
        <p>高效维护学生学籍信息，助力精细化教学服务</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">新增学生</el-button>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">学生总数</p>
            <p class="value">{{ total }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">学生信息完整度</p>
            <p class="value">60%</p> </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">学生账号激活率</p>
            <p class="value">80%</p> </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-input v-model="queryParams.name" placeholder="输入姓名或学号搜索" size="large" style="width: 300px;" @keyup.enter="handleQuery" />
        <div>
          <el-select
              v-model="queryParams.subjectId"
              placeholder="按科目筛选"
              clearable
              @change="handleQuery"
              size="large"
              style="width: 150px; margin-right: 20px;"
          >
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
          </el-select>
          <el-upload
              :action="''"
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
              :http-request="handleImport"
              style="margin-right: 12px; display: inline-block;"
          >
            <el-button size="large" :icon="Upload">导入</el-button>
          </el-upload>
          <el-button size="large" :icon="Download" @click="handleExport">导出</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="studentList" style="width: 100%; margin-top: 20px;">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="studentNo" label="学号" />

        <el-table-column prop="points" label="积分" width="100" align="center">
          <template #default="scope">
            <el-tag effect="plain" type="warning">{{ scope.row.points || 0 }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="所属科目">
          <template #default="scope">
            {{ getSubjectName(scope.row.subjectId) }}
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" />
        <el-table-column prop="contact" label="联系方式" />
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <el-button type="success" link :icon="Trophy" @click="handleOpenPointsDialog(scope.row)">奖惩</el-button>
            <el-button type="primary" link @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
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

    <el-dialog v-model="isPointsDialogVisible" title="积分奖惩" width="400px">
      <el-form :model="pointsForm" label-width="80px">
        <el-form-item label="学生">
          <strong>{{ currentStudent?.name }}</strong>
        </el-form-item>
        <el-form-item label="变动类型">
          <el-radio-group v-model="pointsType">
            <el-radio label="add">奖励 (+)</el-radio>
            <el-radio label="sub">扣除 (-)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="pointsForm.val" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="pointsForm.remark" placeholder="例如：课堂表现优秀" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="isPointsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPoints">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchStudentList, deleteStudent, importStudents, exportStudentsExcel, addStudentPoints } from '@/api/student';
import type { Student, StudentPageParams } from '@/api/student';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { Plus, Edit, Delete, Search, Refresh, Upload, Download, Trophy } from '@element-plus/icons-vue';
import StudentEditDialog from '@/components/student/StudentEditDialog.vue';
import type { UploadRequestOptions } from 'element-plus';

const studentList = ref<Student[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingStudent = ref<Student | undefined>(undefined);

// --- 积分弹窗相关变量 (之前缺失的部分) ---
const isPointsDialogVisible = ref(false);
const currentStudent = ref<Student | null>(null);
const pointsType = ref('add');
const pointsForm = reactive({
  val: 10,
  remark: ''
});

const queryParams = reactive<StudentPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined,
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
      });
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

// --- 积分相关方法 (之前缺失的部分) ---
const handleOpenPointsDialog = (student: Student) => {
  currentStudent.value = student;
  pointsForm.val = 10;
  pointsForm.remark = '';
  pointsType.value = 'add';
  isPointsDialogVisible.value = true;
};

const submitPoints = async () => {
  if (!currentStudent.value) return;

  // 计算最终分值（如果是扣除，则转为负数）
  const finalPoints = pointsType.value === 'add' ? pointsForm.val : -pointsForm.val;

  try {
    await addStudentPoints(currentStudent.value.id, {
      points: finalPoints,
      remark: pointsForm.remark
    });
    ElMessage.success('操作成功');
    isPointsDialogVisible.value = false;
    getList(); // 刷新列表以显示最新积分
  } catch (error) {
    // 错误已由拦截器统一处理
  }
};

onMounted(() => {
  getAllSubjects().then(getList);
});
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
.content-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>