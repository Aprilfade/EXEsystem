<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>试卷管理</h2>
        <p>灵活组合各类题型与知识点，高效创建与分发试卷</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">新增试卷</el-button>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">试卷总数</p>
            <p class="value">{{ total }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">试卷下载次数</p>
            <p class="value">6</p> </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">试卷题目平均分值</p>
            <p class="value">80</p> </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">试卷关联学生题目数量</p> <p class="value">45</p> </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-input v-model="searchQuery" placeholder="输入试卷名称或编码搜索" size="large" style="width: 300px;"/>
        <div>
          <el-select v-model="queryParams.subjectId" placeholder="按科目筛选" clearable @change="handleQuery" size="large" style="width: 150px; margin-right: 20px;"></el-select>
          <el-select v-model="queryParams.grade" placeholder="按年级筛选" clearable @change="handleQuery" size="large" style="width: 150px; margin-right: 20px;">
            <el-option label="七年级" value="七年级" />
            <el-option label="八年级" value="八年级" />
            <el-option label="九年级" value="九年级" />
          </el-select>
          <el-button-group>
            <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'"/>
            <el-button :icon="Menu" :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'"/>
          </el-button-group>
        </div>
      </div>

      <div v-if="viewMode === 'grid'" class="card-grid">
        <div v-for="paper in filteredList" :key="paper.id" class="paper-card">
          <div class="card-header">
            <div>
              <el-tag
                  size="small"
                  :type="paper.status === 1 ? 'success' : 'info'"
                  effect="dark"
                  style="margin-right: 8px;"
              >
                {{ paper.status === 1 ? '已发布' : '草稿' }}
              </el-tag>

              <el-tag size="small">{{ getSubjectName(paper.subjectId) }}</el-tag>
              <el-tag v-if="paper.grade" size="small" type="success" style="margin-left: 8px;">{{ paper.grade }}</el-tag>
            </div>

            <el-dropdown @click.stop>
              <el-icon class="el-dropdown-link"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                      v-if="paper.status !== 1"
                      @click="handleStatusChange(paper, 1)"
                      style="color: #67C23A;"
                  >
                    <el-icon><VideoPlay /></el-icon> 发布试卷
                  </el-dropdown-item>
                  <el-dropdown-item
                      v-else
                      @click="handleStatusChange(paper, 0)"
                      style="color: #E6A23C;"
                  >
                    <el-icon><VideoPause /></el-icon> 下架试卷
                  </el-dropdown-item>

                  <el-dropdown-item divided @click="handleUpdate(paper.id)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleExport(paper.id, false)">导出</el-dropdown-item>
                  <el-dropdown-item @click="handleExport(paper.id, true)">导出(含答案)</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(paper.id)" divided style="color: #f56c6c;">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <h3 class="card-title">{{ paper.name }}</h3>
          <p class="card-desc">{{ paper.description }}</p>
          <div class="card-footer">
            <span class="card-code">PAPER-{{ paper.code || 'N/A' }}</span>
            <span class="card-info">分数 {{ paper.totalScore }}</span>
          </div>
        </div>
      </div>

      <el-table v-if="viewMode === 'list'" :data="filteredList" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="试卷名称" show-overflow-tooltip />
        <el-table-column prop="code" label="编码" width="150" />
        <el-table-column label="所属科目" width="150">
          <template #default="scope">{{ getSubjectName(scope.row.subjectId) }}</template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="120" /> <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
            <el-button type="success" link :icon="Download" @click="handleExport(scope.row.id, false)">导出</el-button>
            <el-button type="warning" link :icon="Download" @click="handleExport(scope.row.id, true)">导出(含答案)</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
            <el-button v-if="scope.row.status !== 1" type="success" link @click="handleStatusChange(scope.row, 1)">发布</el-button>
            <el-button v-else type="warning" link @click="handleStatusChange(scope.row, 0)">下架</el-button>
            <el-divider direction="vertical" />
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-if="viewMode === 'list'"
          class="pagination"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          @size-change="getList"
          @current-change="getList"
      />
    </el-card>

    <paper-edit-dialog
        v-if="isDialogVisible"
        v-model:visible="isDialogVisible"
        :paper-id="editingId"
        :subjects="allSubjects"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchPaperList, deletePaper, downloadPaper, updatePaperStatus } from '@/api/paper';
import type { Paper, PaperPageParams } from '@/api/paper';
import { fetchAllSubjects, type Subject } from '@/api/subject';
import { Plus, Edit, Delete, Grid, Menu, MoreFilled, Download ,VideoPlay, VideoPause} from '@element-plus/icons-vue';
import PaperEditDialog from '@/components/paper/PaperEditDialog.vue';

const allPaperList = ref<Paper[]>([]); // 用于前端搜索
const paperListForTable = ref<Paper[]>([]); // 用于表格分页
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);
const viewMode = ref<'grid' | 'list'>('grid');
const searchQuery = ref('');

const queryParams = reactive<PaperPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined,
  grade: undefined // 【新增此行】
});

// 【修改】: filteredList 计算属性，增加年级过滤逻辑
const filteredList = computed(() => {
  return allPaperList.value.filter(item => {
    const searchMatch = searchQuery.value
        ? item.name.toLowerCase().includes(searchQuery.value.toLowerCase()) || (item.code && item.code.toLowerCase().includes(searchQuery.value.toLowerCase()))
        : true;
    const subjectMatch = queryParams.subjectId ? item.subjectId === queryParams.subjectId : true;
    const gradeMatch = queryParams.grade ? item.grade === queryParams.grade : true; // <-- 新增
    return searchMatch && subjectMatch && gradeMatch; // <-- 新增
  });
});

const getList = async () => {
  loading.value = true;
  try {
    const [allRes, pageRes] = await Promise.all([
      fetchPaperList({ current: 1, size: 9999 }),
      fetchPaperList(queryParams)
    ]);
    if (allRes.code === 200) {
      allPaperList.value = allRes.data;
    }
    if (pageRes.code === 200) {
      paperListForTable.value = pageRes.data;
      total.value = pageRes.total;
    }
  } finally {
    loading.value = false;
  }
};

const getAllSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) allSubjects.value = res.data;
};

const getSubjectName = (subjectId: number) => {
  const subject = allSubjects.value.find((s: Subject) => s.id === subjectId);
  return subject ? subject.name : '未知';
};

const handleQuery = () => {
  if (viewMode.value === 'list') {
    queryParams.current = 1;
    getList();
  }
};

// 【修改】: watch 侦听器，增加对 grade 的监听
watch(() => [queryParams.subjectId, queryParams.grade], () => { // <-- 修改
  if (viewMode.value === 'list') {
    handleQuery();
  }
});

const handleCreate = () => {
  editingId.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (id: number) => {
  editingId.value = id;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该试卷吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deletePaper(id);
        ElMessage.success('删除成功');
        getList();
      });
};

const handleExport = async (id: number, includeAnswers: boolean) => {
  try {
    ElMessage.info('正在生成Word文件，请稍候...');
    const response = await downloadPaper(id, includeAnswers);
    const contentDisposition = response.headers['content-disposition'];
    let fileName = '试卷.docx';
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="(.+)"/);
      if (match && match.length > 1) {
        fileName = decodeURIComponent(match[1]);
      }
    }

    const blob = new Blob([response.data], { type: response.headers['content-type'] });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('文件导出失败:', error);
    ElMessage.error('文件导出失败，请稍后重试。');
  }
};
// 2. 【新增】处理状态变更
const handleStatusChange = async (row: Paper, newStatus: number) => {
  const actionName = newStatus === 1 ? '发布' : '下架';
  try {
    await ElMessageBox.confirm(`确定要${actionName}试卷“${row.name}”吗？发布后学生即可查看。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: newStatus === 1 ? 'success' : 'warning'
    });

    await updatePaperStatus(row.id, newStatus);
    ElMessage.success(`${actionName}成功`);
    getList(); // 刷新列表
  } catch (e) {
    // 取消或失败
  }
};

onMounted(() => {
  getAllSubjects().then(getList);
});
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; font-size: 14px; }
.stats-cards { margin-bottom: 20px; }
.stat-item { padding: 8px; }
.stat-item .label { color: var(--text-color-regular); font-size: 14px; margin-bottom: 8px;}
.stat-item .value { font-size: 28px; font-weight: bold; }
.content-card { background-color: var(--bg-color-container); }
.content-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
.paper-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  background-color: #f7f9fc;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}
.paper-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: var(--brand-color); /* 使用 style.css 中定义的品牌色 */
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.el-dropdown-link {
  cursor: pointer;
  color: var(--text-color-regular);
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.card-desc {
  font-size: 14px;
  color: var(--text-color-regular);
  flex-grow: 1;
  min-height: 40px;
  margin-bottom: 16px;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
  font-size: 12px;
  color: var(--text-color-regular);
}
.card-code, .card-info {
  font-family: monospace;
}
</style>