<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>班级管理</h2>
        <p>组织和管理班级，发布作业，管理学生</p>
      </div>
      <el-button type="primary" icon="Plus" size="large" @click="handleCreate">新建班级</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" v-for="cls in classList" :key="cls.id">
        <el-card shadow="hover" class="class-card">
          <template #header>
            <div class="card-header">
              <span>{{ cls.name }}</span>
              <el-dropdown @command="(cmd) => handleCommand(cmd, cls)">
                <el-icon class="more-icon"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit" :icon="Edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="regenerate" :icon="Refresh">重新生成邀请码</el-dropdown-item>
                    <el-dropdown-item command="delete" :icon="Delete" divided>删除班级</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          <div class="card-content">
            <p><el-tag size="small">{{ cls.grade }}</el-tag></p>
            <p style="margin-top: 8px;">邀请码: <strong style="font-size: 18px; color: #409eff; cursor: pointer;" @click="copyCode(cls.code)" title="点击复制">{{ cls.code }}</strong></p>
            <p class="desc">{{ cls.description || '暂无描述' }}</p>
          </div>
          <div class="card-footer">
            <el-button text bg size="small" @click="handleAssign(cls)" :icon="DocumentAdd">发布作业</el-button>
            <el-button text bg size="small" @click="handleManageStudents(cls)" :icon="User">学生管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建/编辑班级对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="班级名称">
          <el-input v-model="form.name" placeholder="例：高三(1)班" />
        </el-form-item>
        <el-form-item label="所属年级">
          <el-select v-model="form.grade" placeholder="请选择" style="width: 100%;">
            <el-option value="七年级" label="七年级"/>
            <el-option value="八年级" label="八年级"/>
            <el-option value="九年级" label="九年级"/>
            <el-option value="高一" label="高一"/>
            <el-option value="高二" label="高二"/>
            <el-option value="高三" label="高三"/>
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入班级简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEditing ? '更新' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <!-- 发布作业对话框 -->
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
        <el-button @click="homeworkVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHomework">发布</el-button>
      </template>
    </el-dialog>

    <!-- 学生管理对话框 -->
    <el-dialog v-model="studentDialogVisible" :title="currentClassName + ' - 学生列表'" width="800px">
      <el-table :data="classStudents" v-loading="studentLoading" height="450px" stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column property="name" label="姓名" width="120" />
        <el-table-column property="studentNo" label="学号" width="150" />
        <el-table-column property="grade" label="年级" width="100" />
        <el-table-column property="className" label="班级" width="100" />
        <el-table-column label="积分" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.points || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleRemoveStudent(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="studentDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { fetchClassList, createClass, updateClass, deleteClass, assignHomework, fetchClassStudents, removeStudent, regenerateCode } from '@/api/class';
import { fetchPaperList } from '@/api/paper';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, Delete, MoreFilled, DocumentAdd, User, Refresh } from '@element-plus/icons-vue';

const classList = ref<any[]>([]);
const papers = ref<any[]>([]);
const dialogVisible = ref(false);
const homeworkVisible = ref(false);
const isEditing = ref(false);
const currentClassId = ref(0);
const currentClass = ref<any>(null);

const form = reactive({ id: null, name: '', grade: '', description: '' });
const hwForm = reactive({ title: '', paperId: undefined, deadline: '' });

const dialogTitle = computed(() => isEditing.value ? '编辑班级' : '新建班级');

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

const handleCreate = () => {
  isEditing.value = false;
  form.id = null;
  form.name = '';
  form.grade = '';
  form.description = '';
  dialogVisible.value = true;
};

const handleCommand = (command: string, cls: any) => {
  if (command === 'edit') {
    handleEdit(cls);
  } else if (command === 'delete') {
    handleDelete(cls);
  } else if (command === 'regenerate') {
    handleRegenerateCode(cls);
  }
};

const handleEdit = (cls: any) => {
  isEditing.value = true;
  form.id = cls.id;
  form.name = cls.name;
  form.grade = cls.grade;
  form.description = cls.description || '';
  dialogVisible.value = true;
};

const handleDelete = (cls: any) => {
  ElMessageBox.confirm(
    `确定要删除班级"${cls.name}"吗？如果班级下有学生或作业，将无法删除。`,
    '确认删除',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      const res = await deleteClass(cls.id);
      if (res.code === 200) {
        ElMessage.success('删除成功');
        loadData();
      }
    } catch (error: any) {
      // 错误已由拦截器处理
    }
  }).catch(() => {});
};

const handleRegenerateCode = (cls: any) => {
  ElMessageBox.confirm(
    '重新生成邀请码后，旧邀请码将失效，确定继续吗？',
    '重新生成邀请码',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      const res = await regenerateCode(cls.id);
      if (res.code === 200) {
        ElMessage.success('新邀请码：' + res.data);
        loadData();
      }
    } catch (error: any) {
      // 错误已由拦截器处理
    }
  }).catch(() => {});
};

const submitForm = async () => {
  if (!form.name) {
    ElMessage.warning('请输入班级名称');
    return;
  }
  if (!form.grade) {
    ElMessage.warning('请选择所属年级');
    return;
  }

  try {
    if (isEditing.value) {
      await updateClass(form.id!, form);
      ElMessage.success('更新成功');
    } else {
      await createClass(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch (error: any) {
    // 错误已由拦截器处理
  }
};

const handleAssign = (cls: any) => {
  currentClassId.value = cls.id;
  hwForm.title = '';
  hwForm.paperId = undefined;
  hwForm.deadline = '';
  homeworkVisible.value = true;
};

const submitHomework = async () => {
  if(!hwForm.paperId || !hwForm.title) {
    ElMessage.warning('请完善作业信息');
    return;
  }
  try {
    await assignHomework(currentClassId.value, hwForm);
    ElMessage.success('作业发布成功');
    homeworkVisible.value = false;
  } catch (error: any) {
    // 错误已由拦截器处理
  }
};

const handleManageStudents = async (cls: any) => {
  currentClass.value = cls;
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

const handleRemoveStudent = (student: any) => {
  ElMessageBox.confirm(
    `确定要将学生"${student.name}"从班级中移除吗？`,
    '确认移除',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      const res = await removeStudent(currentClass.value.id, student.id);
      if (res.code === 200) {
        ElMessage.success('移除成功');
        // 刷新学生列表
        handleManageStudents(currentClass.value);
      }
    } catch (error: any) {
      // 错误已由拦截器处理
    }
  }).catch(() => {});
};

const copyCode = (code: string) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('邀请码已复制到剪贴板');
  });
}

onMounted(loadData);
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; margin: 0; }
.page-header p { color: #909399; margin-top: 4px; font-size: 14px; }

.class-card { margin-bottom: 20px; transition: all 0.3s; }
.class-card:hover { transform: translateY(-5px); box-shadow: 0 8px 16px rgba(0,0,0,0.1); }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.more-icon {
  cursor: pointer;
  font-size: 20px;
  color: #909399;
  transition: color 0.3s;
}

.more-icon:hover {
  color: #409eff;
}

.card-content {
  margin: 15px 0;
  color: #606266;
  min-height: 80px;
}

.desc {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  border-top: 1px solid #eee;
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
