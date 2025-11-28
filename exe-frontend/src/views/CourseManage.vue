<template>
  <div class="page-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" icon="Plus" @click="openCourseDialog()">新建课程</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" v-for="course in courseList" :key="course.id" style="margin-bottom: 20px;">
        <el-card :body-style="{ padding: '0px' }" shadow="hover">
          <div class="course-cover" :style="{ backgroundImage: `url(${course.coverUrl || '/default-course.jpg'})` }"></div>
          <div style="padding: 14px;">
            <div class="course-title">{{ course.name }}</div>
            <div class="course-info">
              <el-tag size="small">{{ course.grade }}</el-tag>
              <span class="time">{{ formatDate(course.createTime) }}</span>
            </div>
            <div class="bottom-actions">
              <el-button type="text" @click="manageResources(course)">管理内容</el-button>
              <el-button type="text" @click="openCourseDialog(course)">编辑</el-button>
              <el-button type="text" style="color: #f56c6c;" @click="handleDelete(course.id!)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="courseDialogVisible" :title="isEdit ? '编辑课程' : '新建课程'" width="500px">
      <el-form :model="courseForm" label-width="80px">
        <el-form-item label="课程名称">
          <el-input v-model="courseForm.name" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="courseForm.grade" placeholder="请选择">
            <el-option v-for="g in ['七年级','八年级','九年级']" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload
              class="avatar-uploader"
              action="/api/v1/files/upload"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :headers="uploadHeaders"
          >
            <img v-if="courseForm.coverUrl" :src="courseForm.coverUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="courseForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCourse">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="resourceDrawerVisible" title="课程内容管理" size="600px">
      <div style="margin-bottom: 15px;">
        <el-button type="primary" size="small" @click="openResourceDialog()">添加章节/资源</el-button>
      </div>
      <el-table :data="currentResources" border>
        <el-table-column prop="name" label="资源名称" />
        <el-table-column prop="resourceType" label="类型" width="80" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" :href="row.resourceUrl" target="_blank">查看</el-button>
            <el-button link type="danger" @click="handleDeleteResource(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="resourceDialogVisible" title="添加资源" width="400px" append-to-body>
      <el-form :model="resourceForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="resourceForm.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="resourceForm.resourceType">
            <el-option label="视频" value="VIDEO" />
            <el-option label="PDF文档" value="PDF" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
              action="/api/v1/files/upload"
              :on-success="(res) => resourceForm.resourceUrl = res.data"
              :headers="uploadHeaders"
              :limit="1"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitResource">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { fetchCourseList, saveCourse, deleteCourse, fetchCourseDetail, saveResource, deleteResource, type Course, type CourseResource } from '@/api/course';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const uploadHeaders = computed(() => ({ 'Authorization': 'Bearer ' + authStore.token }));

const courseList = ref<Course[]>([]);
const courseDialogVisible = ref(false);
const resourceDrawerVisible = ref(false);
const resourceDialogVisible = ref(false);
const isEdit = ref(false);

const courseForm = ref<Partial<Course>>({});
const resourceForm = ref<Partial<CourseResource>>({});
const currentResources = ref<CourseResource[]>([]);
const currentCourseId = ref<number>(0);

const loadCourses = async () => {
  const res = await fetchCourseList({ current: 1, size: 100 });
  if (res.code === 200) courseList.value = res.data.records;
};

const openCourseDialog = (course?: Course) => {
  if (course) {
    courseForm.value = { ...course };
    isEdit.value = true;
  } else {
    courseForm.value = { name: '', grade: '', description: '', coverUrl: '' };
    isEdit.value = false;
  }
  courseDialogVisible.value = true;
};

const handleCoverSuccess = (res: any) => {
  if (res.code === 200) courseForm.value.coverUrl = res.data;
};

const submitCourse = async () => {
  await saveCourse(courseForm.value as Course);
  ElMessage.success('保存成功');
  courseDialogVisible.value = false;
  loadCourses();
};

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该课程吗？');
  await deleteCourse(id);
  ElMessage.success('删除成功');
  loadCourses();
};

const manageResources = async (course: Course) => {
  currentCourseId.value = course.id!;
  const res = await fetchCourseDetail(course.id!);
  currentResources.value = res.data.resources || [];
  resourceDrawerVisible.value = true;
};

const openResourceDialog = () => {
  resourceForm.value = { courseId: currentCourseId.value, name: '', resourceType: 'VIDEO', resourceUrl: '' };
  resourceDialogVisible.value = true;
};

const submitResource = async () => {
  await saveResource(resourceForm.value as CourseResource);
  ElMessage.success('添加成功');
  resourceDialogVisible.value = false;
  // 刷新资源列表
  const res = await fetchCourseDetail(currentCourseId.value);
  currentResources.value = res.data.resources || [];
};

const handleDeleteResource = async (id: number) => {
  await deleteResource(id);
  const res = await fetchCourseDetail(currentCourseId.value);
  currentResources.value = res.data.resources || [];
};

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString();
};

onMounted(loadCourses);
</script>

<style scoped>
.page-container { padding: 24px; }
.course-cover { height: 150px; background-size: cover; background-position: center; }
.course-title { font-size: 16px; font-weight: bold; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;}
.course-info { display: flex; justify-content: space-between; align-items: center; color: #999; font-size: 12px; }
.bottom-actions { margin-top: 12px; border-top: 1px solid #eee; padding-top: 8px; display: flex; justify-content: space-between; }
.avatar-uploader .el-upload { border: 1px dashed #d9d9d9; cursor: pointer; position: relative; overflow: hidden; }
.avatar-uploader .el-upload:hover { border-color: #409EFF; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; text-align: center; line-height: 100px;}
.avatar { width: 100px; height: 100px; display: block; }
</style>