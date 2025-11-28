<template>
  <div class="page-container">
    <el-card class="filter-card" shadow="never">
      <div class="header-flex">
        <h2>课程学习中心</h2>
        <div class="filter-group">
          <el-select v-model="queryParams.subjectId" placeholder="按科目筛选" clearable @change="handleQuery" style="width: 160px; margin-right: 10px;">
            <el-option
                v-for="sub in allSubjects"
                :key="sub.id"
                :label="sub.name"
                :value="sub.id"
            />
          </el-select>
          <el-input v-model="queryParams.name" placeholder="搜索课程..." prefix-icon="Search" style="width: 200px;" @keyup.enter="handleQuery" />
        </div>
      </div>
    </el-card>

    <div v-loading="loading" class="course-grid">
      <div v-for="course in courseList" :key="course.id" class="course-card" @click="openCourseDetail(course)">
        <div class="course-cover-wrapper">
          <div class="course-cover" :style="{ backgroundImage: `url(${course.coverUrl || '/default-course.jpg'})` }"></div>
          <div class="course-badge">{{ getSubjectName(course.subjectId) }}</div>
        </div>
        <div class="course-info">
          <h3 class="course-title" :title="course.name">{{ course.name }}</h3>
          <div class="course-meta">
            <el-tag size="small" type="info" effect="plain">{{ course.grade }}</el-tag>
            <span class="time">{{ formatDate(course.createTime) }}</span>
          </div>
          <p class="course-desc">{{ course.description || '暂无简介...' }}</p>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && courseList.length === 0" description="暂无相关课程" />

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

    <el-drawer v-model="detailVisible" title="课程详情" size="600px" direction="rtl">
      <template #header>
        <div class="drawer-header">
          <span class="drawer-title">{{ currentCourse?.name }}</span>
          <el-tag size="small">{{ currentCourse?.grade }}</el-tag>
        </div>
      </template>

      <div v-if="currentCourse" class="course-detail-content">
        <div class="detail-cover" v-if="currentCourse.coverUrl">
          <el-image :src="currentCourse.coverUrl" fit="cover" style="width: 100%; height: 200px; border-radius: 8px;" />
        </div>
        <p class="detail-desc">{{ currentCourse.description }}</p>

        <el-divider content-position="left">课程目录</el-divider>

        <div class="resource-list">
          <div v-for="(res, index) in currentResources" :key="res.id" class="resource-item" @click="previewResource(res)">
            <div class="res-icon">
              <el-icon v-if="res.resourceType === 'VIDEO'" color="#67C23A"><VideoPlay /></el-icon>
              <el-icon v-else-if="res.resourceType === 'PDF'" color="#F56C6C"><Document /></el-icon>
              <el-icon v-else-if="res.resourceType === 'PPT'" color="#E6A23C"><DataBoard /></el-icon>
              <el-icon v-else color="#409EFF"><Link /></el-icon>
            </div>
            <div class="res-info">
              <div class="res-name">{{ index + 1 }}. {{ res.name }}</div>
              <div class="res-type">{{ res.resourceType }}</div>
            </div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
          <el-empty v-if="currentResources.length === 0" description="暂无章节资源" image-size="60" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { fetchStudentCourseList, fetchStudentCourseDetail } from '@/api/studentCourse';
import { fetchPracticeSubjects } from '@/api/studentAuth'; // 复用获取科目接口
import type { Subject } from '@/api/subject';
import type { Course, CourseResource } from '@/api/course';
import { Search, VideoPlay, Document, DataBoard, Link, ArrowRight } from '@element-plus/icons-vue';

const loading = ref(false);
const courseList = ref<Course[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);

const detailVisible = ref(false);
const currentCourse = ref<Course | null>(null);
const currentResources = ref<CourseResource[]>([]);

const queryParams = reactive({
  current: 1,
  size: 12,
  subjectId: undefined as number | undefined,
  name: ''
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await fetchStudentCourseList(queryParams);
    if (res.code === 200) {
      courseList.value = res.data.records;
      total.value = res.data.total;
    }
  } finally {
    loading.value = false;
  }
};

const loadSubjects = async () => {
  const res = await fetchPracticeSubjects();
  if (res.code === 200) allSubjects.value = res.data;
};

const getSubjectName = (sid: number) => {
  const s = allSubjects.value.find(i => i.id === sid);
  return s ? s.name : '综合';
};

const formatDate = (dateStr?: string) => {
  return dateStr ? dateStr.split('T')[0] : '';
};

const handleQuery = () => {
  queryParams.current = 1;
  getList();
};

const openCourseDetail = async (course: Course) => {
  currentCourse.value = course;
  detailVisible.value = true;
  // 加载资源详情
  const res = await fetchStudentCourseDetail(course.id!);
  if (res.code === 200) {
    currentResources.value = res.data.resources || [];
  }
};

const previewResource = (row: CourseResource) => {
  const url = row.resourceUrl;
  if (row.resourceType === 'LINK') {
    window.open(url, '_blank');
    return;
  }
  // 拼接完整路径 (假设后端在 8080, 前端在 5173 且配置了代理，或者直接用完整URL)
  // 如果 url 是相对路径 /api/v1/files/xxx
  const fullUrl = url.startsWith('http') ? url : `http://localhost:8080${url}`; // 这里视实际环境调整

  if (['PPT', 'PDF'].includes(row.resourceType) || url.endsWith('.pptx') || url.endsWith('.docx')) {
    // 简单处理：如果是文档，尝试用微软在线预览，或者直接打开让浏览器处理PDF
    if(url.endsWith('.pdf') || row.resourceType === 'PDF') {
      window.open(fullUrl, '_blank');
    } else {
      const officeViewerUrl = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(fullUrl)}`;
      window.open(officeViewerUrl, '_blank');
    }
  } else {
    window.open(fullUrl, '_blank');
  }
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
.filter-group { display: flex; align-items: center; }

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  border-color: var(--el-color-primary-light-5);
}

.course-cover-wrapper { position: relative; height: 150px; background: #f5f7fa; overflow: hidden; }
.course-cover { width: 100%; height: 100%; background-size: cover; background-position: center; transition: transform 0.3s; }
.course-card:hover .course-cover { transform: scale(1.05); }
.course-badge { position: absolute; top: 10px; right: 10px; background: rgba(0,0,0,0.6); color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 12px; backdrop-filter: blur(4px); }

.course-info { padding: 16px; flex-grow: 1; display: flex; flex-direction: column; }
.course-title { margin: 0 0 8px 0; font-size: 16px; font-weight: 600; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.course-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.time { font-size: 12px; color: #909399; }
.course-desc { font-size: 13px; color: #606266; line-height: 1.5; margin: 0; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }

.pagination { margin-top: 30px; justify-content: center; display: flex;}

/* 详情抽屉样式 */
.drawer-header { display: flex; align-items: center; gap: 10px; }
.drawer-title { font-size: 18px; font-weight: bold; }
.course-detail-content { padding: 10px; }
.detail-desc { margin: 15px 0; color: #606266; line-height: 1.6; }
.resource-list { margin-top: 10px; }
.resource-item {
  display: flex; align-items: center; padding: 12px; border-radius: 8px; background: #f8f9fa; margin-bottom: 10px; cursor: pointer; transition: all 0.2s;
}
.resource-item:hover { background: #ecf5ff; }
.res-icon { font-size: 24px; margin-right: 12px; display: flex; align-items: center; }
.res-info { flex-grow: 1; }
.res-name { font-weight: 500; color: #303133; font-size: 14px; }
.res-type { font-size: 12px; color: #909399; margin-top: 2px; }
.arrow-icon { color: #c0c4cc; }
</style>