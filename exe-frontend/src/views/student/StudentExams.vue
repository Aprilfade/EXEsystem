<template>
  <div class="page-container">
    <el-card class="filter-card" shadow="never">
      <div class="header-flex">
        <h2>模拟考试</h2>
        <el-select v-model="queryParams.subjectId" placeholder="按科目筛选" clearable @change="handleQuery" style="width: 200px;">
          <el-option
              v-for="sub in allSubjects"
              :key="sub.id"
              :label="sub.grade ? `${sub.name} (${sub.grade})` : sub.name"
              :value="sub.id"
          />
        </el-select>
      </div>
    </el-card>

    <div v-loading="loading" class="exam-grid">
      <div v-for="paper in paperList" :key="paper.id" class="exam-card">
        <div class="exam-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="exam-info">
          <h3>{{ paper.name }}</h3>
          <div class="tags">
            <el-tag size="small" type="info">{{ paper.grade }}</el-tag>
            <el-tag size="small" type="warning">总分: {{ paper.totalScore }}</el-tag>
          </div>
          <p class="desc">{{ paper.description || '暂无描述' }}</p>
        </div>
        <div class="exam-action">
          <el-button type="primary" round @click="startExam(paper.id)">开始考试</el-button>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && paperList.length === 0" description="当前年级暂无发布的模拟试卷" />

    <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="prev, pager, next"
        :total="total"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        @current-change="getList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { fetchStudentPapers, fetchPracticeSubjects } from '@/api/studentAuth';
import type { Subject } from '@/api/subject';
import { Document } from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const paperList = ref<any[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);

const queryParams = reactive({
  current: 1,
  size: 12,
  subjectId: undefined as number | undefined
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await fetchStudentPapers(queryParams);
    if (res.code === 200) {
      paperList.value = res.data.records;
      total.value = res.data.total;
    }
  } finally {
    loading.value = false;
  }
};

const loadSubjects = async () => {
  // 【修改 2】调用 fetchPracticeSubjects
  const res = await fetchPracticeSubjects();
  if (res.code === 200) allSubjects.value = res.data;
};
const handleQuery = () => {
  queryParams.current = 1;
  getList();
};

const startExam = (paperId: number) => {
  router.push(`/student/exam/${paperId}`);
};

onMounted(() => {
  loadSubjects();
  getList();
});
</script>

<style scoped>
.page-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
.filter-card { margin-bottom: 20px; border-radius: 8px; }
.header-flex { display: flex; justify-content: space-between; align-items: center; }
.header-flex h2 { margin: 0; font-size: 20px; font-weight: 600; }

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.exam-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #ebeef5;
}
.exam-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  border-color: var(--el-color-primary-light-5);
}

.exam-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.exam-info { flex-grow: 1; overflow: hidden; }
.exam-info h3 { margin: 0 0 8px 0; font-size: 16px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.exam-info .tags { display: flex; gap: 6px; margin-bottom: 8px; }
.exam-info .desc { font-size: 12px; color: #909399; margin: 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.exam-action { flex-shrink: 0; }

.pagination { margin-top: 30px; justify-content: center; display: flex;}
</style>