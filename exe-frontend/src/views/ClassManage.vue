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
            <p>邀请码: <strong style="font-size: 18px; color: #409eff;">{{ cls.code }}</strong></p>
            <p class="desc">{{ cls.description || '暂无描述' }}</p>
          </div>
          <div class="card-footer">
            <el-button text bg size="small" @click="handleAssign(cls)">发布作业</el-button>
            <el-button text bg size="small">学生管理</el-button>
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
            <el-option value="高三" label="高三"/>
            <el-option value="高二" label="高二"/>
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
          <el-select v-model="hwForm.paperId" filterable placeholder="请选择试卷">
            <el-option v-for="p in papers" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="hwForm.deadline" type="datetime" placeholder="选择截止时间" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitHomework">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { fetchClassList, createClass, assignHomework } from '@/api/class';
import { fetchPaperList } from '@/api/paper';
import { ElMessage } from 'element-plus';

const classList = ref([]);
const papers = ref([]);
const dialogVisible = ref(false);
const homeworkVisible = ref(false);
const currentClassId = ref(0);

const form = reactive({ name: '', grade: '', description: '' });
const hwForm = reactive({ title: '', paperId: undefined, deadline: '' });

const loadData = async () => {
  const res = await fetchClassList();
  if(res.code === 200) classList.value = res.data;

  // 预加载试卷列表用于发布作业
  const pRes = await fetchPaperList({ current: 1, size: 100 });
  if(pRes.code === 200) papers.value = pRes.data;
};

const submitCreate = async () => {
  await createClass(form);
  ElMessage.success('创建成功');
  dialogVisible.value = false;
  loadData();
};

const handleAssign = (cls: any) => {
  currentClassId.value = cls.id;
  homeworkVisible.value = true;
};

const submitHomework = async () => {
  await assignHomework(currentClassId.value, hwForm);
  ElMessage.success('作业发布成功');
  homeworkVisible.value = false;
};

onMounted(loadData);
</script>

<style scoped>
.page-container { padding: 24px; }
.class-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.card-content { margin: 15px 0; color: #606266; }
.card-footer { border-top: 1px solid #eee; padding-top: 10px; text-align: right; }
</style>