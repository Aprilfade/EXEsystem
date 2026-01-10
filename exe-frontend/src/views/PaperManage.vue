<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>试卷管理</h2>
        <p>灵活组合各类题型与知识点，高效创建与分发试卷</p>
      </div>
      <div style="display: flex; gap: 10px;">
        <el-button type="success" :icon="MagicStick" size="large" @click="showAiDialog = true">AI生成试卷</el-button>
        <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">新增试卷</el-button>
      </div>
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
            <p class="value">6</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">试卷题目平均分值</p>
            <p class="value">80</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">试卷关联学生题目数量</p>
            <p class="value">45</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-input v-model="searchQuery" placeholder="输入试卷名称或编码搜索" size="large" style="width: 300px;"/>
        <div>
          <el-select v-model="queryParams.subjectId" placeholder="按科目筛选" clearable @change="handleQuery" size="large" style="width: 150px; margin-right: 20px;">
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
          </el-select>

          <el-select v-model="queryParams.grade" placeholder="按年级筛选" clearable @change="handleQuery" size="large" style="width: 150px; margin-right: 20px;">
            <el-option label="七年级" value="七年级" />
            <el-option label="八年级" value="八年级" />
            <el-option label="九年级" value="九年级" />
            <el-option label="高一" value="高一" />
            <el-option label="高二" value="高二" />
            <el-option label="高三" value="高三" />
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
                  <el-dropdown-item @click="handleExport(paper.id, false, 'word')">导出 Word</el-dropdown-item>
                  <el-dropdown-item @click="handleExport(paper.id, true, 'word')">导出 Word(含答案)</el-dropdown-item>
                  <el-dropdown-item divided @click="handleExport(paper.id, false, 'pdf')">导出 PDF (预览)</el-dropdown-item>
                  <el-dropdown-item @click="handleExportAnswerSheet(paper.id)" style="color: #626aef;">
                    <el-icon><Printer /></el-icon> 导出答题卡
                  </el-dropdown-item>
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
        <el-table-column prop="grade" label="年级" width="120" />
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
            <el-button type="success" link :icon="Download" @click="handleExport(scope.row.id, false, 'word')">Word</el-button>
            <el-button type="primary" link :icon="VideoPlay" @click="handleExport(scope.row.id, false, 'pdf')">PDF</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>

            <el-divider direction="vertical" />

            <el-button v-if="scope.row.status !== 1" type="success" link @click="handleStatusChange(scope.row, 1)">发布</el-button>
            <el-button v-else type="warning" link @click="handleStatusChange(scope.row, 0)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
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

    <!-- AI生成试卷对话框 -->
    <el-dialog
        v-model="showAiDialog"
        title="AI 智能生成试卷"
        width="800px"
        :close-on-click-modal="false"
    >
      <el-form :model="aiForm" label-width="120px" v-if="!aiGenerating && !aiGenerated">
        <el-form-item label="试卷标题" required>
          <el-input v-model="aiForm.paperTitle" placeholder="例如：高中数学期末测试卷" />
        </el-form-item>

        <el-form-item label="科目" required>
          <el-select v-model="aiForm.subjectId" placeholder="请选择科目" @change="onAiSubjectChange" style="width: 100%">
            <el-option v-for="subject in allSubjects" :key="subject.id" :label="subject.name" :value="subject.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="知识点" required>
          <el-select v-model="aiForm.knowledgePointIds" multiple placeholder="请选择知识点（可多选）" style="width: 100%">
            <el-option v-for="kp in aiKnowledgePoints" :key="kp.id" :label="kp.name" :value="kp.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="难度分布">
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
            <span style="width: 60px;">简单</span>
            <el-slider v-model="aiForm.easy" :max="100" style="flex: 1;" />
            <span style="width: 50px;">{{ aiForm.easy }}%</span>
          </div>
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
            <span style="width: 60px;">中等</span>
            <el-slider v-model="aiForm.medium" :max="100" style="flex: 1;" />
            <span style="width: 50px;">{{ aiForm.medium }}%</span>
          </div>
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
            <span style="width: 60px;">困难</span>
            <el-slider v-model="aiForm.hard" :max="100" style="flex: 1;" />
            <span style="width: 50px;">{{ aiForm.hard }}%</span>
          </div>
          <div style="margin-top: 10px; color: #409eff; font-weight: 500;">
            总计：{{ aiForm.easy + aiForm.medium + aiForm.hard }}%
          </div>
        </el-form-item>

        <el-form-item label="题型配置">
          <div v-for="(type, index) in aiForm.questionTypes" :key="index" style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
            <el-select v-model="type.type" placeholder="题型" style="width: 150px">
              <el-option label="单选题" value="单选" />
              <el-option label="多选题" value="多选" />
              <el-option label="判断题" value="判断" />
              <el-option label="填空题" value="填空" />
              <el-option label="主观题" value="主观" />
            </el-select>
            <el-input-number v-model="type.count" :min="1" :max="50" />
            <span>道</span>
            <el-button type="danger" link @click="removeAiQuestionType(index)" v-if="aiForm.questionTypes.length > 1">
              删除
            </el-button>
          </div>
          <el-button type="primary" link @click="addAiQuestionType">
            + 添加题型
          </el-button>
        </el-form-item>

        <el-form-item label="试卷总分">
          <el-input-number v-model="aiForm.totalScore" :min="10" :max="500" />
        </el-form-item>
      </el-form>

      <!-- 生成过程 -->
      <div v-if="aiGenerating" style="margin: 20px 0;">
        <el-alert type="info" :closable="false">
          <template #title>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>AI 正在生成试卷，请稍候...</span>
            </div>
          </template>
        </el-alert>
        <el-card shadow="never" style="margin-top: 15px; max-height: 400px; overflow-y: auto;">
          <pre style="white-space: pre-wrap; word-wrap: break-word; font-size: 14px;">{{ aiStreamContent }}</pre>
        </el-card>
      </div>

      <!-- 生成结果 -->
      <div v-if="aiGenerated" style="max-height: 500px; overflow-y: auto;">
        <!-- 醒目提示 -->
        <el-alert
            type="success"
            :closable="false"
            style="margin-bottom: 20px;"
        >
          <template #title>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-icon style="font-size: 20px;"><SuccessFilled /></el-icon>
              <span style="font-size: 16px; font-weight: 600;">生成完成！请点击下方"保存试卷"按钮将试卷保存到数据库</span>
            </div>
          </template>
        </el-alert>

        <div style="margin-bottom: 20px;">
          <h3>{{ aiGeneratedPaper.paperName }}</h3>
          <div style="display: flex; gap: 20px; color: #606266;">
            <span>总分：{{ aiGeneratedPaper.totalScore }}分</span>
            <span>题目数：{{ aiGeneratedPaper.questions?.length || 0 }}道</span>
          </div>
        </div>

        <div v-for="(q, index) in aiGeneratedPaper.questions" :key="index" style="background: #f5f7fa; padding: 15px; margin-bottom: 15px; border-radius: 8px;">
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
            <span style="font-weight: 600;">{{ index + 1 }}.</span>
            <el-tag size="small">{{ q.type }}</el-tag>
            <el-tag size="small" type="info">{{ q.difficulty }}</el-tag>
            <span style="margin-left: auto; color: #f56c6c; font-weight: 500;">{{ q.score }}分</span>
          </div>
          <div style="margin: 10px 0;">{{ q.content }}</div>
          <div v-if="q.options && q.options.length" style="margin: 10px 0; padding-left: 20px;">
            <div v-for="opt in q.options" :key="opt.key" style="margin: 5px 0;">
              {{ opt.key }}. {{ opt.value }}
            </div>
          </div>
          <div style="margin-top: 10px; padding: 8px; background: white; border-radius: 4px;">
            <strong>答案：</strong>{{ q.answer }}
          </div>
          <div style="margin-top: 10px; padding: 8px; background: white; border-radius: 4px;">
            <strong>解析：</strong>{{ q.analysis }}
          </div>
        </div>
      </div>

      <template #footer>
        <div v-if="!aiGenerating && !aiGenerated">
          <el-button @click="showAiDialog = false">取消</el-button>
          <el-button type="primary" @click="handleAiGenerate" :loading="aiGenerating">开始生成</el-button>
        </div>
        <div v-if="aiGenerated">
          <el-button @click="resetAiForm">重新生成</el-button>
          <el-button type="primary" @click="saveAiPaper" :loading="saving">保存试卷</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchPaperList, deletePaper, downloadPaper, updatePaperStatus, downloadPaperPdf, createPaper } from '@/api/paper';
import type { Paper, PaperPageParams } from '@/api/paper';
import { fetchAllSubjects, type Subject } from '@/api/subject';
import { Plus, Edit, Delete, Grid, Menu, MoreFilled, Download, VideoPlay, VideoPause, Printer, MagicStick, Loading, SuccessFilled } from '@element-plus/icons-vue';
import PaperEditDialog from '@/components/paper/PaperEditDialog.vue';
// 导入新API
import { downloadAnswerSheet } from '@/api/paper';
import { fetchAllKnowledgePoints } from '@/api/knowledgePoint';
import { createQuestion } from '@/api/question';




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
  grade: undefined,
  name: undefined // 对应后端查询参数
});

// 4. 修改 filteredList
// 由于数据已经是后端过滤好的，前端不需要再过滤了
const filteredList = computed(() => {
  return paperListForTable.value;
});
// 3. 修改 getList 方法，只发一次分页请求
const getList = async () => {
  loading.value = true;
  try {
    // 只调用分页接口，不再拉取 9999 条
    const res = await fetchPaperList(queryParams);
    if (res.code === 200) {
      // 统一使用 table 的数据源，不再区分 allPaperList 和 paperListForTable
      // 注意：如果你的 Grid 视图和 List 视图都需要分页，这种方式最合适。
      // 如果 Grid 视图原本设计为"无限滚动"或"加载全部"，则需要改为后端分页+“加载更多”按钮。
      paperListForTable.value = res.data;
      total.value = res.total;

      // 为了兼容现有代码，让 filteredList 直接返回分页数据
      allPaperList.value = res.data;
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

// 2. 监听搜索框输入，更新 queryParams 并重新请求
// 注意：el-input v-model 需要改为 queryParams.name，或者监听 searchQuery 赋值给 queryParams.name
watch(() => searchQuery.value, (newVal) => {
  queryParams.name = newVal;
  queryParams.current = 1; // 搜索时重置回第一页
  getList();
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

const handleExport = async (id: number, includeAnswers: boolean, type: 'word' | 'pdf' = 'word') => {
  try {
    const typeName = type === 'word' ? 'Word' : 'PDF';
    ElMessage.info(`正在生成 ${typeName} 文件，请稍候...`);

    // 根据类型调用不同的 API
    let response;
    if (type === 'pdf') {
      response = await downloadPaperPdf(id, includeAnswers);
    } else {
      response = await downloadPaper(id, includeAnswers);
    }

    const contentDisposition = response.headers['content-disposition'];
    let fileName = `试卷.${type === 'word' ? 'docx' : 'pdf'}`;
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="(.+)"/);
      if (match && match.length > 1) {
        fileName = decodeURIComponent(match[1]);
      }
    }

    const blob = new Blob([response.data], { type: response.headers['content-type'] });
    const url = window.URL.createObjectURL(blob);

    if (type === 'pdf') {
      window.open(url);
    } else {
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }

    setTimeout(() => window.URL.revokeObjectURL(url), 1000);
  } catch (error) {
    console.error('文件导出失败:', error);
    ElMessage.error('文件导出失败，请稍后重试。');
  }
};

// 新增处理函数
const handleExportAnswerSheet = async (id: number) => {
  try {
    ElMessage.info('正在生成答题卡，请稍候...');
    const response = await downloadAnswerSheet(id);

    // 处理下载
    const contentDisposition = response.headers['content-disposition'];
    let fileName = '答题卡.pdf';
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="(.+)"/);
      if (match && match.length > 1) {
        fileName = decodeURIComponent(match[1]);
      }
    }

    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);

    // 在新窗口预览
    window.open(url);

    // 或者直接下载 (根据需求二选一，这里演示直接下载)
    // const link = document.createElement('a');
    // link.href = url;
    // link.setAttribute('download', fileName);
    // document.body.appendChild(link);
    // link.click();
    // document.body.removeChild(link);

    setTimeout(() => window.URL.revokeObjectURL(url), 1000);
  } catch (error) {
    console.error('答题卡导出失败:', error);
    ElMessage.error('答题卡导出失败');
  }
};



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
    getList();
  } catch (e) {
    // 取消或失败
  }
};

onMounted(() => {
  getAllSubjects().then(getList);
});

// ===== AI 生成试卷相关 =====
const showAiDialog = ref(false);
const aiGenerating = ref(false);
const aiGenerated = ref(false);
const aiStreamContent = ref('');
const aiGeneratedPaper = ref<any>({});
const aiKnowledgePoints = ref<any[]>([]);
const saving = ref(false);

const aiForm = reactive({
  paperTitle: '',
  subjectId: undefined as number | undefined,
  subjectName: '',
  knowledgePointIds: [] as number[],
  easy: 30,
  medium: 50,
  hard: 20,
  questionTypes: [
    { type: '单选', count: 10 },
    { type: '多选', count: 5 }
  ],
  totalScore: 100
});

const onAiSubjectChange = async () => {
  const subject = allSubjects.value.find((s: Subject) => s.id === aiForm.subjectId);
  aiForm.subjectName = subject?.name || '';

  try {
    const kpRes = await fetchAllKnowledgePoints({ subjectId: aiForm.subjectId });
    if (kpRes.code === 200) {
      aiKnowledgePoints.value = kpRes.data;
    }
  } catch (error) {
    console.error('加载知识点失败:', error);
  }
};

const addAiQuestionType = () => {
  aiForm.questionTypes.push({ type: '单选', count: 5 });
};

const removeAiQuestionType = (index: number) => {
  aiForm.questionTypes.splice(index, 1);
};

const handleAiGenerate = async () => {
  // 验证表单
  if (!aiForm.paperTitle) {
    ElMessage.warning('请输入试卷标题');
    return;
  }
  if (!aiForm.subjectId) {
    ElMessage.warning('请选择科目');
    return;
  }
  if (aiForm.knowledgePointIds.length === 0) {
    ElMessage.warning('请选择至少一个知识点');
    return;
  }
  const totalPercent = aiForm.easy + aiForm.medium + aiForm.hard;
  if (totalPercent !== 100) {
    ElMessage.warning('难度分布总和必须为100%');
    return;
  }

  aiGenerating.value = true;
  aiStreamContent.value = '';

  // 准备请求数据
  const knowledgePointNames = aiForm.knowledgePointIds
    .map(id => aiKnowledgePoints.value.find(kp => kp.id === id)?.name)
    .filter(Boolean)
    .join('、');

  const apiKey = localStorage.getItem('student_ai_key') || '';
  const provider = localStorage.getItem('student_ai_provider') || 'DEEPSEEK';

  const url = `${import.meta.env.VITE_API_BASE_URL || ''}/api/v1/papers/ai-generate-stream`;

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Ai-Api-Key': apiKey,
        'X-Ai-Provider': provider,
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        paperTitle: aiForm.paperTitle,
        subjectName: aiForm.subjectName,
        knowledgePoints: knowledgePointNames,
        difficultyDistribution: `简单:${aiForm.easy}%,中等:${aiForm.medium}%,困难:${aiForm.hard}%`,
        questionTypes: JSON.stringify(aiForm.questionTypes),
        totalScore: aiForm.totalScore
      })
    });

    const reader = response.body!.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    let currentEvent = '';
    let currentData = ''; // 【修复】累积当前事件的所有 data 行

    // 【新增】数据监控和保护
    let totalDataSize = 0;
    const MAX_DATA_SIZE = 5 * 1024 * 1024; // 5MB 上限
    const startTime = Date.now();
    const TIMEOUT = 10 * 60 * 1000; // 10分钟超时

    // 【新增】JSON完整性检查函数
    function isJsonComplete(str: string): boolean {
      if (!str || str.trim().length === 0) return false;

      let braces = 0; // {} 括号
      let brackets = 0; // [] 括号
      let inString = false;
      let escape = false;

      for (let i = 0; i < str.length; i++) {
        const char = str[i];

        if (escape) {
          escape = false;
          continue;
        }

        if (char === '\\') {
          escape = true;
          continue;
        }

        if (char === '"' && !escape) {
          inString = !inString;
          continue;
        }

        if (!inString) {
          if (char === '{') braces++;
          if (char === '}') braces--;
          if (char === '[') brackets++;
          if (char === ']') brackets--;
        }
      }

      // JSON完整的条件：括号都匹配，且不在字符串中
      return braces === 0 && brackets === 0 && !inString;
    }

    function processText(text: string) {
      buffer += text;
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (let line of lines) {
        line = line.trim();
        if (!line) {
          // 空行表示一个SSE事件块结束
          // 【关键修改】对于done事件，不要在这里解析，继续累积直到流结束
          if (currentEvent === 'message' && currentData) {
            // message事件可以立即处理
            aiStreamContent.value += currentData;
            currentEvent = '';
            currentData = '';
          }
          // 对于done事件，不重置，继续累积
          continue;
        }

        if (line.startsWith('event:')) {
          const newEvent = line.substring(6).trim();

          // 【修改】遇到新事件时的处理
          if (currentEvent === 'message' && currentData) {
            // 处理之前的message事件
            aiStreamContent.value += currentData;
            currentData = '';
          }

          // 【关键修复】如果新事件也是done，不重置数据，继续累积
          if (newEvent === 'done' && currentEvent === 'done') {
            console.log(`📥 发现另一个done事件，继续累积，当前大小: ${(currentData.length / 1024).toFixed(2)} KB`);
            // 不重置currentData，继续累积
          } else if (newEvent !== currentEvent) {
            // 不同事件类型，重置数据
            currentData = '';
          }

          currentEvent = newEvent;
        } else if (line.startsWith('data:')) {
          // 【修复】累积 data 内容（可能有多行）
          const dataContent = line.substring(5).trim();

          // 【简化日志】只在done事件且数据变大时输出
          if (currentEvent === 'done' && currentData.length % 1000 < dataContent.length) {
            console.log(`📥 done事件累积中: ${(currentData.length / 1024).toFixed(2)} KB`);
          }

          currentData += dataContent;

          // 【新增】数据大小监控
          totalDataSize += dataContent.length;
          if (totalDataSize > MAX_DATA_SIZE) {
            throw new Error(`数据超过${MAX_DATA_SIZE / 1024 / 1024}MB上限，请减少生成题目数量`);
          }
        }
      }
    }

    while (true) {
      // 【新增】超时检查
      const elapsedTime = Date.now() - startTime;
      if (elapsedTime > TIMEOUT) {
        throw new Error('生成超时（10分钟），请减少题目数量或稍后重试');
      }

      const { done, value } = await reader.read();
      if (done) {
        console.log(`✅ 流接收完成，总耗时: ${((Date.now() - startTime) / 1000).toFixed(2)}秒`);
        console.log(`📦 总数据大小: ${(totalDataSize / 1024).toFixed(2)} KB`);

        // 【修复】流结束时，处理剩余的缓存数据
        if (buffer) {
          processText('\n'); // 添加换行触发最后的处理
        }
        // 如果有未完成的事件数据，强制处理
        if (currentEvent && currentData) {
          console.log(`🔧 处理流结束时的剩余数据，事件类型: ${currentEvent}, 数据大小: ${currentData.length} 字节`);
          try {
            if (currentEvent === 'done') {
              console.log(`📊 最终解析JSON数据，大小: ${(currentData.length / 1024).toFixed(2)} KB`);

              // 【修复】清理智能引号，替换为标准ASCII引号
              // AI可能在中文内容中使用智能引号，这会导致JSON解析失败
              currentData = currentData
                .replace(/"/g, '"')  // 左智能引号 "
                .replace(/"/g, '"')  // 右智能引号 "
                .replace(/'/g, "'")  // 左单引号 '
                .replace(/'/g, "'"); // 右单引号 '

              console.log('🔧 已清理智能引号');

              // 【修复】先尝试解析JSON，如果成功就认为是完整的
              let isComplete = false;
              let paperData = null;

              try {
                paperData = JSON.parse(currentData);
                isComplete = true;
                console.log(`✅ JSON解析成功，数据完整`);
              } catch (parseError: any) {
                console.log(`⚠️ JSON解析失败: ${parseError.message}`);
                isComplete = isJsonComplete(currentData);
                console.log(`🔍 JSON完整性检查: ${isComplete}`);
              }

              // 【调试】统计括号数量
              let braces = 0, brackets = 0;
              for (let char of currentData) {
                if (char === '{') braces++;
                if (char === '}') braces--;
                if (char === '[') brackets++;
                if (char === ']') brackets--;
              }
              console.log(`📊 括号统计: {} 差值=${-braces}, [] 差值=${-brackets}`);

              if (!isComplete) {
                console.error('❌ JSON不完整，括号不匹配');
                console.error('数据长度:', currentData.length, '字节');
                console.error('数据前500字符:', currentData.substring(0, 500));
                console.error('数据后500字符:', currentData.substring(Math.max(0, currentData.length - 500)));

                // 【尝试修复】如果只是缺少闭合括号，尝试补全
                let fixedData = currentData;
                while (braces < 0) { fixedData += '}'; braces++; }
                while (brackets < 0) { fixedData += ']'; brackets++; }

                if (isJsonComplete(fixedData)) {
                  console.warn('⚠️ 尝试自动修复JSON成功，已补全闭合括号');
                  currentData = fixedData;
                } else {
                  ElMessage.error('试卷数据不完整，请重新生成或减少题目数量');
                  return;
                }
              }

              // 如果之前没有成功解析，现在尝试解析
              if (!paperData) {
                paperData = JSON.parse(currentData);
              }

              // 【修复】检测AI返回的数据结构
              console.log('📊 AI返回的原始数据类型:', Array.isArray(paperData) ? '数组' : '对象');
              console.log('📊 AI返回的原始数据:', paperData);

              if (Array.isArray(paperData)) {
                // 如果AI直接返回数组，需要包装成对象格式
                console.log('⚠️ AI返回的是题目数组，自动包装成试卷格式');
                aiGeneratedPaper.value = {
                  paperName: aiForm.paperTitle || '未命名试卷',
                  description: 'AI生成试卷',
                  totalScore: aiForm.totalScore,
                  questions: paperData
                };
              } else if (paperData.questions && Array.isArray(paperData.questions)) {
                // 如果AI返回对象且包含questions字段，直接使用
                console.log('✅ AI返回的是标准试卷格式');
                aiGeneratedPaper.value = paperData;
              } else {
                // 其他情况，尝试查找题目数组
                console.error('❌ 无法识别AI返回的数据格式');
                console.error('paperData:', paperData);
                throw new Error('AI返回的数据格式不正确');
              }

              console.log('📊 处理后的试卷数据:', aiGeneratedPaper.value);
              console.log('📊 题目数量:', aiGeneratedPaper.value.questions?.length || 0);

              aiGenerating.value = false;
              aiGenerated.value = true;
              ElMessage.success('试卷生成完成！');
            }
          } catch (e) {
            console.error('❌ 最终处理事件数据失败:', e);
            console.error('数据大小:', currentData.length, '字节');
            console.error('数据前100字符:', currentData.substring(0, 100));
            console.error('数据后100字符:', currentData.substring(currentData.length - 100));
            ElMessage.error('试卷数据解析失败，请检查控制台详细日志');
          }
        }
        break;
      }
      processText(decoder.decode(value, { stream: true }));
    }
  } catch (error: any) {
    aiGenerating.value = false;
    ElMessage.error('生成失败: ' + error.message);
    console.error(error);
  }
};

const saveAiPaper = async () => {
  saving.value = true;
  try {
    ElMessage.info('正在保存试卷，请稍候...');

    // 1. 将 AI 生成的题目转换为题库格式并创建题目
    const questionTypeMap: Record<string, number> = {
      '单选': 1,
      '多选': 2,
      '填空': 3,
      '判断': 4,
      '主观': 5
    };

    console.log('📊 AI生成的试卷数据:', aiGeneratedPaper.value);

    const createdQuestionIds: number[] = [];
    const questions = aiGeneratedPaper.value.questions || [];

    console.log('📊 题目数组:', questions);
    console.log('📊 题目数量:', questions.length);

    for (const aiQuestion of questions) {
      // 处理 options：转换为 JSON 字符串
      let optionsStr = typeof aiQuestion.options === 'string'
        ? aiQuestion.options
        : JSON.stringify(aiQuestion.options);

      // 处理 answer：确保是字符串格式
      let answerStr = aiQuestion.answer;
      if (Array.isArray(answerStr)) {
        // 如果是数组（多选题），用逗号连接
        answerStr = answerStr.join(',');
      } else if (typeof answerStr !== 'string') {
        // 如果不是字符串也不是数组，转为字符串
        answerStr = String(answerStr);
      }

      const questionData = {
        subjectId: aiForm.subjectId,
        grade: allSubjects.value.find(s => s.id === aiForm.subjectId)?.grade || '',
        questionType: questionTypeMap[aiQuestion.type] || 1,
        content: aiQuestion.content,
        options: optionsStr,
        answer: answerStr,
        description: aiQuestion.analysis || '',
        knowledgePointIds: aiForm.knowledgePointIds
      };

      console.log('📝 正在创建题目:', questionData);
      const result = await createQuestion(questionData);
      console.log('📝 创建题目结果:', result);

      if (result.code === 200 && result.data) {
        createdQuestionIds.push(result.data as any);
      } else {
        console.error('❌ 题目创建失败:', result);
      }
    }

    console.log('📊 成功创建题目数量:', createdQuestionIds.length);
    if (createdQuestionIds.length === 0) {
      throw new Error('题目创建失败，请查看控制台日志');
    }

    // 2. 创建试卷，将题目组织到一个分组中
    const paperData = {
      name: aiForm.paperTitle,
      code: 'AI-' + Date.now(), // 自动生成试卷编码
      subjectId: aiForm.subjectId,
      grade: allSubjects.value.find(s => s.id === aiForm.subjectId)?.grade || '',
      description: `AI生成试卷 - 共${questions.length}道题，总分${aiForm.totalScore}分`,
      totalScore: aiGeneratedPaper.value.totalScore || aiForm.totalScore,
      paperType: 1, // 手动选题
      status: 0, // 草稿状态
      groups: [
        {
          name: '试题',
          sortOrder: 0,
          questions: createdQuestionIds.map((qId, index) => ({
            questionId: qId,
            score: questions[index].score || 5,
            sortOrder: index
          }))
        }
      ]
    };

    await createPaper(paperData);
    ElMessage.success('试卷保存成功！');
    showAiDialog.value = false;
    resetAiForm();
    getList();
  } catch (error: any) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败: ' + (error.message || '未知错误'));
  } finally {
    saving.value = false;
  }
};

const resetAiForm = () => {
  aiGenerating.value = false;
  aiGenerated.value = false;
  aiStreamContent.value = '';
  aiGeneratedPaper.value = {};
  aiForm.paperTitle = '';
  aiForm.subjectId = undefined;
  aiForm.knowledgePointIds = [];
  aiForm.questionTypes = [
    { type: '单选', count: 10 },
    { type: '多选', count: 5 }
  ];
};
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
  border-color: var(--brand-color);
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