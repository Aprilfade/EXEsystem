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

    <!-- 课程卡片网格 -->
    <transition-group
      name="course-list"
      tag="div"
      class="course-grid"
      appear
    >
      <!-- 骨架屏 -->
      <CourseCardSkeleton
        v-if="loading"
        v-for="n in 6"
        :key="`skeleton-${n}`"
      />

      <!-- 课程卡片 -->
      <CourseCard
        v-else
        v-for="course in courseList"
        :key="course.id"
        :course="course"
        :subject-name="getSubjectName(course.subjectId)"
        :progress-percent="getCourseProgressPercent(course.id!)"
        :completed-count="getCourseCompletedCount(course.id!)"
        :total-count="getCourseResourceCount(course.id!)"
        :study-time="getCourseStudyTime(course.id!)"
        @click="openCourseDetail(course)"
        @quick-start="handleQuickStart(course)"
      />
    </transition-group>

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
          <el-image
            :src="currentCourse.coverUrl"
            fit="cover"
            lazy
            loading="lazy"
            style="width: 100%; height: 200px; border-radius: 8px;"
          >
            <template #placeholder>
              <div class="image-slot">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
            </template>
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <p class="detail-desc">{{ currentCourse.description }}</p>

        <el-tabs v-model="activeTab" class="course-tabs">
          <!-- 【新增】学习中心tab -->
          <el-tab-pane label="学习中心" name="learning">
            <div class="learning-center">
              <!-- 视频播放器 -->
              <div v-if="showVideoPlayer && currentResource" class="video-section">
                <div class="video-header">
                  <h3>{{ currentResource.name }}</h3>
                  <el-button type="danger" size="small" @click="closeViewer">关闭播放器</el-button>
                </div>
                <VideoPlayer
                  :video-url="currentResource.resourceUrl"
                  :resource-id="currentResource.id"
                  :last-position="getResourcePercent(currentResource.id)"
                />
              </div>

              <!-- 文档查看器 -->
              <div v-if="showDocumentViewer && currentResource" class="document-section">
                <DocumentViewer
                  :document-url="currentResource.resourceUrl"
                  :resource-id="currentResource.id"
                  :document-type="currentResource.resourceType as 'PDF' | 'PPT'"
                  :document-name="currentResource.name"
                  :last-page="parseInt(store.getResourceLastPosition(currentResource.id) || '1')"
                />
              </div>

              <!-- 章节树 -->
              <div class="chapter-section">
                <ChapterTree
                  :chapters="store.chapters"
                  @resource-click="handleResourceClick"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 【新增】学习数据分析tab -->
          <el-tab-pane label="学习数据" name="analytics">
            <LearningAnalytics :course-id="currentCourse?.id" />
          </el-tab-pane>

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
              <el-empty v-if="currentResources.length === 0" description="暂无章节资源" :image-size="60" />
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
                <el-empty v-if="commentsList.length === 0" description="还没有人评论，快来抢沙发！" :image-size="60" />
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
// 【新增】导入课程学习组件
import ChapterTree from './course-learning/ChapterTree.vue';
import VideoPlayer from './course-learning/VideoPlayer.vue';
import DocumentViewer from './course-learning/DocumentViewer.vue';
import LearningAnalytics from './course-learning/LearningAnalytics.vue';
import CourseCard from './course-learning/CourseCard.vue';
import CourseCardSkeleton from './course-learning/CourseCardSkeleton.vue';
import { useCourseStore } from '@/stores/courseStore';
import { useCourseProgress } from '@/hooks/useCourseProgress';
import { Search, VideoPlay, Document, DataBoard, Link, ArrowRight, Loading, Picture } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const courseList = ref<Course[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);

const detailVisible = ref(false);
const currentCourse = ref<Course | null>(null);
const currentResources = ref<CourseResource[]>([]);

// 【新增】课程学习相关状态
const store = useCourseStore();
const { calculateCourseProgress, getResourcePercent } = useCourseProgress();
const currentResource = ref<CourseResource | null>(null);
const showVideoPlayer = ref(false);
const showDocumentViewer = ref(false);

// 【新增】课程进度缓存
const courseProgressCache = ref<Map<number, {
  percent: number;
  completed: number;
  total: number;
  studyTime: number;
}>>(new Map());

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
  showVideoPlayer.value = false; // 关闭视频播放器

  // 1. 加载资源
  const res = await fetchStudentCourseDetail(course.id!);
  if (res.code === 200) {
    currentResources.value = res.data.resources || [];
  }

  // 2. 加载课程学习数据（章节树、进度等）
  await store.loadCourseDetail(course.id!, {
    id: course.id!,
    name: course.name,
    description: course.description,
    coverImage: course.coverUrl,
    teacherId: 0,
    teacherName: ''
  });

  // 3. 加载评论
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

// 【新增】处理章节树资源点击
const handleResourceClick = async (resource: CourseResource) => {
  currentResource.value = resource;

  // 关闭其他播放器
  showVideoPlayer.value = false;
  showDocumentViewer.value = false;

  // 根据资源类型打开相应的查看器
  if (resource.resourceType === 'VIDEO') {
    showVideoPlayer.value = true;
    activeTab.value = 'learning'; // 切换到学习tab

    // 开始学习会话
    await store.startSession(resource.id);
  } else if (resource.resourceType === 'PDF' || resource.resourceType === 'PPT') {
    showDocumentViewer.value = true;
    activeTab.value = 'learning'; // 切换到学习tab

    // 开始学习会话
    await store.startSession(resource.id);
  } else {
    // 其他资源类型，使用原有的预览方法
    previewResource(resource);
  }
};

// 【新增】关闭播放器/查看器
const closeViewer = async () => {
  showVideoPlayer.value = false;
  showDocumentViewer.value = false;

  // 结束学习会话
  if (store.hasActiveSession) {
    await store.endSession();
  }
};

// 【新增】获取课程进度百分比
const getCourseProgressPercent = (courseId: number): number => {
  const cached = courseProgressCache.value.get(courseId);
  return cached?.percent || 0;
};

// 【新增】获取课程已完成资源数
const getCourseCompletedCount = (courseId: number): number => {
  const cached = courseProgressCache.value.get(courseId);
  return cached?.completed || 0;
};

// 【新增】获取课程总资源数
const getCourseResourceCount = (courseId: number): number => {
  const cached = courseProgressCache.value.get(courseId);
  return cached?.total || 0;
};

// 【新增】获取课程学习时长
const getCourseStudyTime = (courseId: number): number => {
  const cached = courseProgressCache.value.get(courseId);
  return cached?.studyTime || 0;
};

// 【新增】快速开始学习
const handleQuickStart = async (course: Course) => {
  // 先打开课程详情
  await openCourseDetail(course);

  // 自动切换到学习中心tab
  activeTab.value = 'learning';

  // 如果有上次学习的资源，自动打开
  if (store.resources.length > 0) {
    // 找到第一个未完成的资源
    const unfinishedResource = store.resources.find(r => {
      const progress = store.progressMap.get(r.id);
      return !progress || progress.isCompleted === 0;
    });

    if (unfinishedResource) {
      await handleResourceClick(unfinishedResource);
    }
  }
};

onMounted(() => {
  loadSubjects();
  getList();
});
</script>

<style scoped lang="scss">
/* 页面容器 */
.page-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;

  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 课程网格 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

/* 过渡动画 */
.course-list-move,
.course-list-enter-active,
.course-list-leave-active {
  transition: all 0.5s cubic-bezier(0.55, 0, 0.1, 1);
}

.course-list-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.9);
}

.course-list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.course-list-leave-active {
  position: absolute;
}

/* 分页 */
.pagination {
  margin-top: 32px;
  justify-content: center;
  display: flex;
}

/* 抽屉样式 */
.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;

  .drawer-title {
    font-size: 20px;
    font-weight: 600;
  }
}

.course-detail-content {
  padding: 12px;
}

.detail-desc {
  margin: 15px 0;
  color: #606266;
  line-height: 1.8;
  font-size: 15px;
}

/* 资源列表 */
.resource-list {
  margin-top: 12px;
}

.resource-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-radius: 10px;
  background: #f8f9fa;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: #ecf5ff;
    transform: translateX(6px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  }
}

.res-icon {
  font-size: 28px;
  margin-right: 14px;
  display: flex;
  align-items: center;
}

.res-info {
  flex-grow: 1;
}

.res-name {
  font-weight: 500;
  color: #303133;
  font-size: 15px;
  margin-bottom: 4px;
}

.res-type {
  font-size: 13px;
  color: #909399;
}

.arrow-icon {
  color: #c0c4cc;
  transition: all 0.3s;
}

.resource-item:hover .arrow-icon {
  color: #409EFF;
  transform: translateX(4px);
}

/* 评论区样式 */
.course-tabs {
  margin-top: 20px;

  :deep(.el-tabs__nav-wrap) {
    padding: 0 20px;
  }
}

.discussion-area {
  padding-top: 12px;
}

.comment-input-box {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-body {
  flex-grow: 1;
  background: #f9f9f9;
  padding: 12px 16px;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.user-name {
  font-weight: 600;
  color: #303133;
}

.comment-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
}

.comment-actions {
  text-align: right;
  margin-top: 6px;
  height: 24px;
}

/* 学习中心样式 */
.learning-center {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 0;
}

.video-section,
.document-section {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.video-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
}

.chapter-section {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .course-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .header-flex {
    flex-direction: column;
    align-items: stretch;

    h2 {
      font-size: 20px;
    }
  }

  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }

  .course-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .learning-center {
    padding: 12px 0;
  }
}

@media (max-width: 480px) {
  .filter-card {
    border-radius: 8px;
  }

  .course-grid {
    gap: 12px;
  }
}
</style>