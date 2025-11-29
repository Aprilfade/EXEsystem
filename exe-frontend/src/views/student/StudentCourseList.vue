<template>
  <div class="page-container">
    <el-card class="filter-card" shadow="never">
      <div class="header-flex">
        <h2>课程学习中心</h2>
        <div class="filter-group">
          <el-select v-model="queryParams.subjectId" placeholder="按科目筛选" clearable @change="handleQuery" style="width: 160px; margin-right: 10px;">
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
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

    <el-drawer v-model="detailVisible" title="课程详情" size="650px" direction="rtl">
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

        <el-tabs v-model="activeTab" class="course-tabs">
          <el-tab-pane label="课程目录" name="resources">
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
          </el-tab-pane>

          <el-tab-pane label="讨论区" name="comments">
            <div class="discussion-area">
              <div class="comment-input-box">
                <el-input
                    v-model="newComment"
                    type="textarea"
                    :rows="2"
                    placeholder="对此课程有疑问？发条评论和大家讨论吧..."
                    resize="none"
                />
                <div style="text-align: right; margin-top: 8px;">
                  <el-button type="primary" size="small" :disabled="!newComment.trim()" @click="submitComment">发布评论</el-button>
                </div>
              </div>

              <el-divider />

              <div class="comment-list" v-loading="commentsLoading">
                <div v-for="comment in commentsList" :key="comment.id" class="comment-item">
                  <div class="comment-avatar">
                    <UserAvatar
                        :src="comment.studentAvatar"
                        :name="comment.studentName"
                        :size="36"
                        :frame-style="comment.avatarFrameStyle"
                    />
                  </div>
                  <div class="comment-body">
                    <div class="comment-header">
                      <span class="user-name">{{ comment.studentName }}</span>
                      <span class="time">{{ formatTime(comment.createTime) }}</span>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                    <div class="comment-actions">
                      <el-button
                          v-if="comment.isSelf"
                          type="danger"
                          link
                          size="small"
                          @click="handleDeleteComment(comment.id)"
                      >删除</el-button>
                    </div>
                  </div>
                </div>
                <el-empty v-if="commentsList.length === 0" description="还没有人评论，快来抢沙发！" image-size="60" />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { fetchStudentCourseList, fetchStudentCourseDetail } from '@/api/studentCourse';
import { fetchPracticeSubjects } from '@/api/studentAuth';
import type { Subject } from '@/api/subject';
import type { Course, CourseResource } from '@/api/course';
// 【新增】导入评论API和组件
import { fetchComments, addComment, deleteComment, type CourseComment } from '@/api/courseComment';
import UserAvatar from '@/components/UserAvatar.vue';
import { Search, VideoPlay, Document, DataBoard, Link, ArrowRight } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const courseList = ref<Course[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);

const detailVisible = ref(false);
const currentCourse = ref<Course | null>(null);
const currentResources = ref<CourseResource[]>([]);

// 【新增】评论相关状态
const activeTab = ref('resources');
const commentsList = ref<CourseComment[]>([]);
const commentsLoading = ref(false);
const newComment = ref('');

const queryParams = reactive({
  current: 1,
  size: 12,
  subjectId: undefined as number | undefined,
  name: ''
});

// ...原有 getList, loadSubjects, getSubjectName, formatDate, handleQuery 方法保持不变...
// (为节省篇幅，这里复用你之前的逻辑，请确保这些函数在 setup 中)
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

const previewResource = (row: CourseResource) => {
  // 复用你之前的逻辑
  const url = row.resourceUrl;
  if (row.resourceType === 'LINK') {
    window.open(url, '_blank');
    return;
  }
  const fullUrl = url.startsWith('http') ? url : `http://localhost:8080${url}`;

  if (['PPT', 'PDF'].includes(row.resourceType) || url.endsWith('.pptx') || url.endsWith('.docx')) {
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

// 【修改】打开课程详情
const openCourseDetail = async (course: Course) => {
  currentCourse.value = course;
  detailVisible.value = true;
  activeTab.value = 'resources'; // 默认切回资源页

  // 1. 加载资源
  const res = await fetchStudentCourseDetail(course.id!);
  if (res.code === 200) {
    currentResources.value = res.data.resources || [];
  }

  // 2. 加载评论
  loadComments(course.id!);
};

// 【新增】加载评论
const loadComments = async (courseId: number) => {
  commentsLoading.value = true;
  try {
    const res = await fetchComments(courseId);
    if (res.code === 200) {
      commentsList.value = res.data;
    }
  } finally {
    commentsLoading.value = false;
  }
};

// 【新增】提交评论
const submitComment = async () => {
  if (!currentCourse.value) return;
  try {
    await addComment({
      courseId: currentCourse.value.id!,
      content: newComment.value
    });
    ElMessage.success('评论发布成功');
    newComment.value = '';
    // 重新加载评论
    loadComments(currentCourse.value.id!);
  } catch (e) {
    //
  }
};

// 【新增】删除评论
const handleDeleteComment = async (id: number) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteComment(id);
    ElMessage.success('删除成功');
    if (currentCourse.value) loadComments(currentCourse.value.id!);
  });
};

const formatTime = (timeStr: string) => {
  return timeStr ? timeStr.replace('T', ' ').substring(0, 16) : '';
};

onMounted(() => {
  loadSubjects();
  getList();
});
</script>

<style scoped>
/* 原有样式保持不变 */
.page-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
.filter-card { margin-bottom: 20px; border-radius: 8px; }
.header-flex { display: flex; justify-content: space-between; align-items: center; }
.header-flex h2 { margin: 0; font-size: 20px; font-weight: 600; }
.filter-group { display: flex; align-items: center; }
.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
.course-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); transition: transform 0.2s, box-shadow 0.2s; cursor: pointer; border: 1px solid #ebeef5; display: flex; flex-direction: column; }
.course-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); border-color: var(--el-color-primary-light-5); }
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
.drawer-header { display: flex; align-items: center; gap: 10px; }
.drawer-title { font-size: 18px; font-weight: bold; }
.course-detail-content { padding: 10px; }
.detail-desc { margin: 15px 0; color: #606266; line-height: 1.6; }
.resource-list { margin-top: 10px; }
.resource-item { display: flex; align-items: center; padding: 12px; border-radius: 8px; background: #f8f9fa; margin-bottom: 10px; cursor: pointer; transition: all 0.2s; }
.resource-item:hover { background: #ecf5ff; }
.res-icon { font-size: 24px; margin-right: 12px; display: flex; align-items: center; }
.res-info { flex-grow: 1; }
.res-name { font-weight: 500; color: #303133; font-size: 14px; }
.res-type { font-size: 12px; color: #909399; margin-top: 2px; }
.arrow-icon { color: #c0c4cc; }

/* --- 【新增】评论区样式 --- */
.course-tabs { margin-top: 20px; }
.discussion-area { padding-top: 10px; }
.comment-input-box { margin-bottom: 15px; }
.comment-list { display: flex; flex-direction: column; gap: 15px; }
.comment-item { display: flex; gap: 12px; }
.comment-avatar { flex-shrink: 0; }
.comment-body { flex-grow: 1; background: #f9f9f9; padding: 10px 15px; border-radius: 8px; }
.comment-header { display: flex; justify-content: space-between; font-size: 12px; color: #909399; margin-bottom: 6px; }
.user-name { font-weight: bold; color: #303133; }
.comment-content { font-size: 14px; color: #303133; line-height: 1.5; white-space: pre-wrap; }
.comment-actions { text-align: right; margin-top: 4px; height: 20px; }
</style>