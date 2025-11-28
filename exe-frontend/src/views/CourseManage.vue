<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h2>课程资源中心</h2>
        <el-tag type="danger" effect="dark" size="small" style="margin-left: 10px;">资源管理</el-tag>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" icon="Plus" @click="openCourseDialog()">新建课程体系</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="科目">
          <el-select
              v-model="queryParams.subjectId"
              placeholder="请选择科目"
              clearable
              style="width: 160px"
              @change="handleQuery"
              :disabled="filteredSubjectOptions.length === 0 && !!queryParams.grade"
          >
            <el-option
                v-for="sub in filteredSubjectOptions"
                :key="sub.id"
                :label="queryParams.grade ? sub.name : `${sub.name} (${sub.grade})`"
                :value="sub.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.name" placeholder="搜索课程名称..." prefix-icon="Search" style="width: 240px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select
              v-model="queryParams.grade"
              placeholder="全部年级"
              clearable
              style="width: 140px"
              @change="() => { queryParams.subjectId = undefined; handleQuery(); }"
          >
            <el-option label="七年级" value="七年级" />
            <el-option label="八年级" value="八年级" />
            <el-option label="九年级" value="九年级" />
            <el-option label="高一" value="高一" />
            <el-option label="高二" value="高二" />
            <el-option label="高三" value="高三" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.name" placeholder="搜索课程名称..." prefix-icon="Search" style="width: 240px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="6" v-for="course in courseList" :key="course.id" style="margin-bottom: 20px;">
          <el-card :body-style="{ padding: '0px' }" shadow="hover" class="course-card">
            <div class="course-cover-wrapper">
              <div class="course-cover" :style="{ backgroundImage: `url(${course.coverUrl || '/default-course.jpg'})` }"></div>
              <div class="course-badge">{{ getSubjectName(course.subjectId) }}</div>
            </div>

            <div style="padding: 14px;">
              <div class="course-header-row">
                <div class="course-title" :title="course.name">{{ course.name }}</div>
                <el-tag size="small" effect="plain">{{ course.grade }}</el-tag>
              </div>

              <p class="course-desc">{{ course.description || '暂无简介...' }}</p>

              <div class="bottom-actions">
                <el-button type="primary" text bg icon="FolderOpened" @click="manageResources(course)">内容管理</el-button>

                <el-dropdown @command="(cmd) => handleCommand(cmd, course)">
                  <span class="el-dropdown-link">
                    <el-icon><MoreFilled /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit" icon="Edit">编辑信息</el-dropdown-item>
                      <el-dropdown-item command="delete" icon="Delete" style="color: #f56c6c;">删除课程</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && courseList.length === 0" description="暂无课程数据，请点击新建" />
    </div>

    <el-dialog v-model="courseDialogVisible" :title="isEdit ? '编辑课程元数据' : '新建课程体系'" width="550px" destroy-on-close>
      <el-form :model="courseForm" label-width="80px">
        <el-form-item label="课程名称">
          <el-input v-model="courseForm.name" placeholder="例如：高中数学必修一精讲" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用年级">
              <el-select
                  v-model="courseForm.grade"
                  placeholder="选择年级"
                  style="width: 100%"
                  clearable
                  @change="handleDialogGradeChange"
              >
                <el-option v-for="g in ['七年级','八年级','九年级','高一','高二','高三']" :key="g" :label="g" :value="g" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属科目">
              <el-select
                  v-model="courseForm.subjectId"
                  placeholder="选择科目"
                  style="width: 100%"
                  filterable
                  @change="handleDialogSubjectChange"
                  :disabled="dialogSubjectOptions.length === 0 && !!courseForm.grade"
              >
                <el-option
                    v-for="sub in dialogSubjectOptions"
                    :key="sub.id"
                    :label="courseForm.grade ? sub.name : `${sub.name} (${sub.grade})`"
                    :value="sub.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>


        <el-form-item label="封面图">
          <el-upload
              class="cover-uploader"
              action="/api/v1/files/upload"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :headers="uploadHeaders"
          >
            <img v-if="courseForm.coverUrl" :src="courseForm.coverUrl" class="cover-img" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">建议尺寸 16:9，支持 JPG/PNG</div>
            </template>
          </el-upload>
        </el-form-item>


        <el-form-item label="课程简介">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" placeholder="简要介绍课程目标和内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCourse">保存课程</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="resourceDrawerVisible" title="课程内容编排" size="700px" direction="rtl">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
          <span>课程内容编排 - <strong>{{ currentCourseName }}</strong></span>
          <el-button type="primary" icon="Plus" @click="openResourceDialog()">添加章节资源</el-button>
        </div>
      </template>

      <el-table :data="currentResources" stripe style="width: 100%">
        <el-table-column type="index" width="50" label="#" />
        <el-table-column prop="name" label="资源名称" show-overflow-tooltip />
        <el-table-column prop="resourceType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.resourceType === 'VIDEO'" type="success">视频</el-tag>
            <el-tag v-else-if="row.resourceType === 'PDF'" type="danger">PDF</el-tag>
            <el-tag v-else-if="row.resourceType === 'PPT'" type="warning">PPT</el-tag>
            <el-tag v-else type="info">链接</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联知识点" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.knowledgePointId" size="small" effect="plain">
              {{ getKpName(row.knowledgePointId) }}
            </el-tag>
            <span v-else style="color: #ccc; font-size: 12px;">未关联</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="previewResource(row)">预览</el-button>
            <el-button link type="danger" @click="handleDeleteResource(row.id)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="resourceDialogVisible" title="添加课程资源" width="500px" append-to-body destroy-on-close>
      <el-form :model="resourceForm" label-width="100px">
        <el-form-item label="资源标题">
          <el-input v-model="resourceForm.name" placeholder="例如：第一章 函数的概念" />
        </el-form-item>

        <el-form-item label="关联知识点">
          <el-select
              v-model="resourceForm.knowledgePointId"
              filterable
              clearable
              placeholder="关联后可实现智能推荐"
              style="width: 100%"
              :loading="kpLoading"
          >
            <el-option v-for="kp in subjectKnowledgePoints" :key="kp.id" :label="kp.name" :value="kp.id" />
          </el-select>
          <div class="form-tip">系统已自动筛选出【{{ getSubjectName(currentCourseSubjectId) }}】下的知识点</div>
        </el-form-item>

        <el-form-item label="资源类型">
          <el-radio-group v-model="resourceForm.resourceType" @change="resourceForm.resourceUrl = ''">
            <el-radio-button label="VIDEO">视频</el-radio-button>
            <el-radio-button label="PDF">PDF</el-radio-button>
            <el-radio-button label="PPT">PPT</el-radio-button>
            <el-radio-button label="LINK">外链</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="资源内容" v-if="resourceForm.resourceType === 'LINK'">
          <el-input v-model="resourceForm.resourceUrl" placeholder="请输入完整的 URL (如 B站链接)" >
            <template #prepend>Https://</template>
          </el-input>
        </el-form-item>

        <el-form-item label="上传文件" v-else>
          <el-upload
              class="upload-demo"
              drag
              action="/api/v1/files/upload"
              :on-success="(res) => resourceForm.resourceUrl = res.data"
              :headers="uploadHeaders"
              :limit="1"
              :show-file-list="true"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
          </el-upload>
          <div v-if="resourceForm.resourceUrl" style="margin-top: 10px; color: #67C23A;">
            <el-icon><Check /></el-icon> 文件已就绪
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resourceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResource">确认添加</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { fetchCourseList, saveCourse, deleteCourse, fetchCourseDetail, saveResource, deleteResource, type Course, type CourseResource } from '@/api/course';
import { fetchAllSubjects, type Subject } from '@/api/subject';
import { fetchKnowledgePointList, type KnowledgePoint } from '@/api/knowledgePoint';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Refresh, UploadFilled, Check, Edit, Delete, MoreFilled, FolderOpened } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const uploadHeaders = computed(() => ({ 'Authorization': 'Bearer ' + authStore.token }));

// --- 数据状态 ---
const loading = ref(false);
const courseList = ref<Course[]>([]);
const allSubjects = ref<Subject[]>([]);
const queryParams = reactive({
  current: 1,
  size: 20, // 增加单页显示数量
  subjectId: undefined,
  grade: undefined,
  name: ''
});

// 弹窗控制
const courseDialogVisible = ref(false);
const resourceDrawerVisible = ref(false);
const resourceDialogVisible = ref(false);
const isEdit = ref(false);

// 表单数据
const courseForm = ref<Partial<Course>>({});
const resourceForm = ref<Partial<CourseResource>>({});

// 当前操作上下文
const currentCourseId = ref<number>(0);
const currentCourseName = ref('');
const currentCourseSubjectId = ref<number>(0); // 当前操作课程的科目ID
const currentResources = ref<CourseResource[]>([]);

// 知识点相关
const kpLoading = ref(false);
const subjectKnowledgePoints = ref<KnowledgePoint[]>([]); // 当前科目下的所有知识点
const allKpMap = ref<Record<number, string>>({}); // 用于列表展示 ID->Name

// --- 初始化 ---
onMounted(async () => {
  await loadSubjects();
  // 预加载所有知识点以便显示名称（优化点：实际生产中应该后端Join查询，这里前端处理）
  await loadAllKpsCache();
  loadCourses();
});

const loadSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) allSubjects.value = res.data;
};

const loadAllKpsCache = async () => {
  // 这里简单加载一次全量，用于列表回显ID转名称
  const res = await fetchKnowledgePointList({ current: 1, size: 9999 });
  if (res.code === 200) {
    res.data.forEach(kp => {
      allKpMap.value[kp.id] = kp.name;
    });
  }
}

const getSubjectName = (sid: number) => {
  const s = allSubjects.value.find(i => i.id === sid);
  return s ? s.name : '通用';
};

const getKpName = (kpId: number) => {
  return allKpMap.value[kpId] || `KP-${kpId}`;
}

// --- 课程管理 ---
const loadCourses = async () => {
  loading.value = true;
  try {
    const res = await fetchCourseList(queryParams);
    if (res.code === 200) courseList.value = res.data.records;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.current = 1;
  loadCourses();
};

// 【新增】计算属性：根据当前选中的年级，过滤科目下拉框的选项
const filteredSubjectOptions = computed(() => {
  // 如果没有选年级，显示所有科目
  if (!queryParams.grade) {
    return allSubjects.value;
  }
  // 如果选了年级，只返回该年级下的科目
  return allSubjects.value.filter(sub => sub.grade === queryParams.grade);
});

// 【修改】重置查询时，不仅清空数据，也要重置逻辑
const resetQuery = () => {
  queryParams.subjectId = undefined;
  queryParams.grade = undefined;
  queryParams.name = '';
  loadCourses();
};

const openCourseDialog = (course?: Course) => {
  if (course) {
    courseForm.value = { ...course };
    isEdit.value = true;
  } else {
    courseForm.value = { name: '', grade: '', description: '', coverUrl: '', subjectId: undefined };
    isEdit.value = false;
  }
  courseDialogVisible.value = true;
};

const handleCoverSuccess = (res: any) => {
  if (res.code === 200) courseForm.value.coverUrl = res.data;
};

const submitCourse = async () => {
  if(!courseForm.value.name || !courseForm.value.subjectId) {
    ElMessage.warning('请填写完整的课程信息（名称、科目）');
    return;
  }
  await saveCourse(courseForm.value as Course);
  ElMessage.success('保存成功');
  courseDialogVisible.value = false;
  loadCourses();
};

const handleCommand = (command: string, course: Course) => {
  if (command === 'edit') openCourseDialog(course);
  if (command === 'delete') handleDelete(course.id!);
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该课程及其所有资源吗？此操作不可恢复。', '高危操作', {
    type: 'warning',
    confirmButtonText: '确认删除',
    confirmButtonClass: 'el-button--danger'
  });
  await deleteCourse(id);
  ElMessage.success('删除成功');
  loadCourses();
};

// 【新增】弹窗内的科目选项过滤逻辑
const dialogSubjectOptions = computed(() => {
  // 如果表单中未选择年级，则显示所有科目
  if (!courseForm.value.grade) {
    return allSubjects.value;
  }
  // 如果选了年级，仅返回该年级对应的科目
  return allSubjects.value.filter(sub => sub.grade === courseForm.value.grade);
});

// 【新增】当在弹窗中改变【年级】时
const handleDialogGradeChange = () => {
  // 1. 获取当前已选的科目ID
  const currentSubId = courseForm.value.subjectId;

  // 2. 如果已选了科目，检查该科目是否属于新选择的年级
  if (currentSubId) {
    const subject = allSubjects.value.find(s => s.id === currentSubId);
    // 如果科目的年级 不等于 新选择的年级，则清空科目，强制用户重选
    if (subject && subject.grade !== courseForm.value.grade) {
      courseForm.value.subjectId = undefined;
    }
  }
};

// 【新增】当在弹窗中改变【科目】时
const handleDialogSubjectChange = (subjectId: number) => {
  const subject = allSubjects.value.find(s => s.id === subjectId);
  // 如果该科目绑定了年级，自动将表单的年级设置为该科目的年级
  if (subject && subject.grade) {
    courseForm.value.grade = subject.grade;
  }
};




// --- 资源管理 ---
const manageResources = async (course: Course) => {
  currentCourseId.value = course.id!;
  currentCourseName.value = course.name;
  currentCourseSubjectId.value = course.subjectId; // 记录当前科目ID

  // 重新获取该课程详情（含资源）
  const res = await fetchCourseDetail(course.id!);
  currentResources.value = res.data.resources || [];
  resourceDrawerVisible.value = true;
};

// 打开添加资源弹窗
const openResourceDialog = async () => {
  resourceForm.value = {
    courseId: currentCourseId.value,
    name: '',
    resourceType: 'VIDEO',
    resourceUrl: '',
    knowledgePointId: undefined
  };

  // 关键：根据当前课程的科目ID，加载对应的知识点
  kpLoading.value = true;
  resourceDialogVisible.value = true;
  try {
    if (currentCourseSubjectId.value) {
      const res = await fetchKnowledgePointList({
        current: 1,
        size: 999,
        subjectId: currentCourseSubjectId.value // 仅加载本学科知识点
      });
      subjectKnowledgePoints.value = res.data;
      // 更新一下本地映射缓存
      res.data.forEach(kp => allKpMap.value[kp.id] = kp.name);
    } else {
      subjectKnowledgePoints.value = [];
    }
  } finally {
    kpLoading.value = false;
  }
};

const submitResource = async () => {
  if (!resourceForm.value.name || !resourceForm.value.resourceUrl) {
    ElMessage.warning('请填写资源名称并上传文件/输入链接');
    return;
  }
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
  ElMessage.success('资源已移除');
};

// 修复预览功能的逻辑
const previewResource = (row: CourseResource) => {
  const url = row.resourceUrl;

  // 1. 如果是外部链接，直接跳转
  if (row.resourceType === 'LINK') {
    window.open(url, '_blank');
    return;
  }

  // 2. 获取完整的后端文件地址 (假设你的后端端口是8080)
  // 如果你的 url 已经是完整路径(http开头)，则不用拼接
  // 注意：开发环境下可能需要根据你的 .env 配置来拼接 baseURL
  const fullUrl = url.startsWith('http') ? url : `http://localhost:5173${url}`;

  // 3. 针对 PPT/Word/Excel 使用微软在线预览服务
  if (row.resourceType === 'PPT' || url.endsWith('.pptx') || url.endsWith('.ppt') || url.endsWith('.docx') || url.endsWith('.xlsx')) {
    // 使用 encodeURIComponent 对文件地址进行编码
    const officeViewerUrl = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(fullUrl)}`;
    window.open(officeViewerUrl, '_blank');
  }
  // 4. 针对视频和PDF，浏览器可以直接预览
  else {
    window.open(fullUrl, '_blank');
  }
};
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left { display: flex; align-items: center; }
.filter-card { margin-bottom: 20px; background-color: #fff; }
.filter-form .el-form-item { margin-bottom: 0; margin-right: 20px; }

/* 卡片样式优化 */
.course-card {
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
}
.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}
.course-cover-wrapper {
  position: relative;
  height: 140px;
  overflow: hidden;
}
.course-cover {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  transition: transform 0.5s;
}
.course-card:hover .course-cover {
  transform: scale(1.05);
}
.course-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  backdrop-filter: blur(4px);
}
.course-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.course-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-grow: 1;
  margin-right: 10px;
}
.course-desc {
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
  height: 40px; /* 固定高度显示两行 */
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 15px;
}
.bottom-actions {
  border-top: 1px solid #f0f2f5;
  padding-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-dropdown-link {
  cursor: pointer;
  color: #909399;
  display: flex;
  align-items: center;
}

/* 上传组件样式 */
.cover-uploader .el-upload {
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration);
}
.cover-uploader .el-upload:hover { border-color: #409eff; }
.uploader-icon { font-size: 28px; color: #8c939d; width: 200px; height: 112px; line-height: 112px; text-align: center; }
.cover-img { width: 200px; height: 112px; object-fit: cover; display: block; }
.form-tip { font-size: 12px; color: #909399; margin-top: 5px; line-height: 1.2; }
</style>