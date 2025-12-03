<template>
  <div class="page-container">
    <div class="page-header">
      <h2>班级管理</h2>
      <el-button type="primary" icon="Plus" @click="dialogVisible = true">新建班级</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" v-for="cls in classList" :key="cls.id">
        <el-card shadow="hover" class="class-card">
          <template #header>
            <div class="card-header">
              <span>{{ cls.name }}</span>
              <el-tag>{{ cls.grade }}</el-tag>
            </div>
          </template>
          <div class="card-content">
            <p>邀请码: <strong style="font-size: 18px; color: #409eff; cursor: pointer;" @click="copyCode(cls.code)" title="点击复制">{{ cls.code }}</strong></p>
            <p class="desc">{{ cls.description || '暂无描述' }}</p>
          </div>
          <div class="card-footer">
            <el-button text bg size="small" @click="handleAssign(cls)">发布作业</el-button>
            <el-button text bg size="small" @click="handleManageStudents(cls)">学生管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="新建班级" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="班级名称">
          <el-input v-model="form.name" placeholder="例：高三(1)班" />
        </el-form-item>
        <el-form-item label="所属年级">
          <el-select v-model="form.grade" placeholder="请选择">
            <el-option value="七年级" label="七年级"/>
            <el-option value="八年级" label="八年级"/>
            <el-option value="九年级" label="九年级"/>
            <el-option value="高一" label="高一"/>
            <el-option value="高二" label="高二"/>
            <el-option value="高三" label="高三"/>
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="homeworkVisible" title="发布作业" width="500px">
      <el-form :model="hwForm" label-width="80px">
        <el-form-item label="作业标题">
          <el-input v-model="hwForm.title" placeholder="例：周末数学测验" />
        </el-form-item>
        <el-form-item label="选择试卷">
          <el-select v-model="hwForm.paperId" filterable placeholder="请选择试卷" style="width: 100%;">
            <el-option v-for="p in papers" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="hwForm.deadline" type="datetime" placeholder="选择截止时间" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitHomework">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="studentDialogVisible" :title="currentClassName + ' - 学生列表'" width="700px">
      <el-table :data="classStudents" v-loading="studentLoading" height="400px" stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column property="name" label="姓名" width="120" />
        <el-table-column property="studentNo" label="学号" width="150" />
        <el-table-column property="contact" label="联系方式" />
        <el-table-column label="积分" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.points || 0 }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
// 【修复点 3】引入 fetchClassStudents
import { fetchClassList, createClass, assignHomework, fetchClassStudents } from '@/api/class';
import { fetchPaperList } from '@/api/paper';
import { ElMessage } from 'element-plus';

const classList = ref<any[]>([]);
const papers = ref<any[]>([]);
const dialogVisible = ref(false);
const homeworkVisible = ref(false);
const currentClassId = ref(0);

const form = reactive({ name: '', grade: '', description: '' });
const hwForm = reactive({ title: '', paperId: undefined, deadline: '' });

// 学生管理相关状态
const studentDialogVisible = ref(false);
const studentLoading = ref(false);
const classStudents = ref([]);
const currentClassName = ref('');

const loadData = async () => {
  const res = await fetchClassList();
  if(res.code === 200) classList.value = res.data;

  const pRes = await fetchPaperList({ current: 1, size: 100 });
  if(pRes.code === 200) papers.value = pRes.data;
};

const submitCreate = async () => {
  if (!form.name) {
    ElMessage.warning('请输入班级名称');
    return;
  }
  await createClass(form);
  ElMessage.success('创建成功');
  dialogVisible.value = false;
  // 重置表单
  form.name = '';
  form.grade = '';
  form.description = '';
  loadData();
};

const handleAssign = (cls: any) => {
  currentClassId.value = cls.id;
  homeworkVisible.value = true;
};

const submitHomework = async () => {
  if(!hwForm.paperId || !hwForm.title) {
    ElMessage.warning('请完善作业信息');
    return;
  }
  await assignHomework(currentClassId.value, hwForm);
  ElMessage.success('作业发布成功');
  homeworkVisible.value = false;
  // 重置表单
  hwForm.title = '';
  hwForm.paperId = undefined;
  hwForm.deadline = '';
};

// 【修复点 4】实现学生管理点击逻辑
const handleManageStudents = async (cls: any) => {
  currentClassName.value = cls.name;
  studentDialogVisible.value = true;
  studentLoading.value = true;
  classStudents.value = [];
  try {
    const res = await fetchClassStudents(cls.id);
    if (res.code === 200) {
      classStudents.value = res.data;
    }
  } finally {
    studentLoading.value = false;
  }
};

const copyCode = (code: string) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('邀请码已复制');
  });
}

onMounted(loadData);
</script>

<style scoped>
.page-container { padding: 24px; }
.class-card { margin-bottom: 20px; transition: all 0.3s; }
.class-card:hover { transform: translateY(-5px); box-shadow: 0 8px 16px rgba(0,0,0,0.1); }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; font-size: 16px; }
.card-content { margin: 15px 0; color: #606266; min-height: 60px; }
.desc { font-size: 13px; color: #909399; margin-top: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-footer { border-top: 1px solid #eee; padding-top: 12px; display: flex; justify-content: flex-end; gap: 10px; }
</style>