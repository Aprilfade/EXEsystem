<template>
  <div class="page-container">
    <el-card class="join-card">
      <div class="join-box">
        <el-input v-model="inviteCode" placeholder="请输入6位班级邀请码" style="width: 200px; margin-right: 10px;" />
        <el-button type="primary" @click="handleJoin">加入班级</el-button>
      </div>
    </el-card>

    <div class="class-list">
      <el-collapse v-model="activeClass" accordion @change="handleClassChange">
        <el-collapse-item v-for="cls in myClasses" :key="cls.id" :name="cls.id">
          <template #title>
            <div class="class-title">
              <strong>{{ cls.name }}</strong>
              <el-tag size="small" style="margin-left: 10px;">{{ cls.grade }}</el-tag>
            </div>
          </template>

          <div class="homework-list" v-loading="hwLoading">
            <el-table :data="homeworks" stripe style="width: 100%">
              <el-table-column prop="title" label="作业名称" />
              <el-table-column prop="deadline" label="截止时间" />
              <el-table-column label="操作" align="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="doHomework(row)">去完成</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="homeworks.length === 0" description="暂无作业" :image-size="60" />
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchMyClasses, joinClass, fetchClassHomework } from '@/api/class';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const inviteCode = ref('');
const myClasses = ref([]);
const activeClass = ref('');
const homeworks = ref([]);
const hwLoading = ref(false);

const loadClasses = async () => {
  const res = await fetchMyClasses();
  if(res.code === 200) myClasses.value = res.data;
};

const handleJoin = async () => {
  if(!inviteCode.value) return;
  const res = await joinClass(inviteCode.value);
  if(res.code === 200) {
    ElMessage.success('加入成功');
    loadClasses();
    inviteCode.value = '';
  }
};

const handleClassChange = async (classId: number) => {
  if(!classId) return;
  hwLoading.value = true;
  try {
    const res = await fetchClassHomework(classId);
    if(res.code === 200) homeworks.value = res.data;
  } finally {
    hwLoading.value = false;
  }
};

const doHomework = (hw: any) => {
  // 复用考试页面，传入试卷ID
  router.push(`/student/exam/${hw.paperId}`);
};

onMounted(loadClasses);
</script>

<style scoped>
.page-container { padding: 20px; max-width: 800px; margin: 0 auto; }
.join-card { margin-bottom: 20px; }
.join-box { display: flex; justify-content: center; padding: 20px; }
.class-title { font-size: 16px; }
</style>