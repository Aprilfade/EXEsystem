<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>知识点管理</h2>
        <p>为试题添加精准的知识体系与考点基础</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleCreate">新增知识点</el-button>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">知识点总数</p>
            <p class="value">{{ globalStats.totalCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">关联试题总数量</p>
            <p class="value">{{ globalStats.totalQuestionCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <p class="label">未关联试题的知识点占比</p>
            <p class="value">{{ globalStats.unassociatedPercentage }}%</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <div class="content-header">
        <el-button type="success" :icon="MagicStick" size="large" @click="openAiDialog" style="margin-right: 12px;">
          AI 智能提取
        </el-button>
        <el-button
          v-if="selectedKnowledgePoints.length > 0 && viewMode === 'list'"
          type="warning"
          :icon="Edit"
          size="large"
          @click="openBatchEditDialog"
          style="margin-right: 12px;"
        >
          批量编辑 ({{ selectedKnowledgePoints.length }})
        </el-button>

        <el-dialog
            v-model="aiDialogVisible"
            width="600px"
            :close-on-click-modal="false"
            append-to-body
        >
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
              <span style="font-size: 16px; font-weight: 600;">AI 智能提取知识点</span>
              <el-switch
                v-model="useStreamMode"
                active-text="流式模式"
                inactive-text="标准模式"
                style="--el-switch-on-color: #13ce66;"
              />
            </div>
          </template>

          <div v-if="step === 1">
            <el-alert type="info" :closable="false" style="margin-bottom: 15px;">
              <template #title>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span>📚 粘贴课文、教案或笔记，或上传文件，AI将自动提取核心知识点</span>
                  <el-tag v-if="textLength > 0" :type="getTextLengthType()" size="small">
                    {{ textLength }} 字符 {{ textLength > 8000 ? '(将分块处理)' : '' }}
                  </el-tag>
                </div>
              </template>
            </el-alert>

            <!-- 【新增】文件上传区域 -->
            <div style="margin-bottom: 15px; display: flex; gap: 10px; align-items: center;">
              <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleFileChange"
                  accept=".txt,.md,.pdf,.doc,.docx"
              >
                <el-button type="primary" :icon="Upload">
                  上传文件
                </el-button>
              </el-upload>
              <span style="color: #909399; font-size: 12px;">
                支持格式: .txt, .md, .pdf, .doc, .docx
              </span>
              <el-tag v-if="uploadedFileName" type="success" closable @close="clearUploadedFile">
                {{ uploadedFileName }}
              </el-tag>
            </div>

            <el-input
                v-model="aiForm.text"
                type="textarea"
                :rows="12"
                placeholder="请输入文本内容，或上传文件自动填充...（支持大文本，无字数限制）"
                @input="updateTextLength"
            />

            <div style="margin-top: 15px; display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center; gap: 10px;">
                <span>提取数量：</span>
                <el-input-number v-model="aiForm.count" :min="1" :max="50" />
                <el-tooltip content="建议：短文本5-10个，长文本20-50个" placement="top">
                  <el-icon style="cursor: help;"><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
              <div v-if="textLength > 8000" style="color: #409eff; font-size: 12px;">
                <el-icon><Loading /></el-icon>
                预计处理时间：{{ estimatedTime }}秒
              </div>
            </div>
          </div>

          <div v-else-if="step === 2">
            <!-- 流式输出显示区域 -->
            <div v-if="useStreamMode && generating && streamContent" style="margin-bottom: 20px;">
              <el-alert type="info" :closable="false" style="margin-bottom: 10px;">
                <template #title>
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>AI 正在思考中，实时输出...</span>
                  </div>
                </template>
              </el-alert>

              <el-card shadow="never" style="max-height: 300px; overflow-y: auto; background: #f5f7fa;">
                <pre style="white-space: pre-wrap; word-wrap: break-word; font-family: 'Courier New', monospace; font-size: 12px; line-height: 1.6; margin: 0;">{{ streamContent }}</pre>
              </el-card>
            </div>

            <!-- 原有的加载状态 -->
            <div v-loading="generating && !streamContent" :element-loading-text="loadingText">
              <el-form label-position="top">
              <el-form-item label="选择所属科目">
                <el-select v-model="aiSaveConfig.subjectId" placeholder="请选择" style="width: 100%">
                  <el-option v-for="s in allSubjects" :key="s.id" :label="s.name" :value="s.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="适用年级">
                <el-select v-model="aiSaveConfig.grade" placeholder="请选择" style="width: 100%">
                  <el-option v-for="g in ['七年级','八年级','九年级','高一','高二','高三']" :key="g" :label="g" :value="g" />
                </el-select>
              </el-form-item>
            </el-form>

            <div v-if="generatedPoints.length > 0">
              <el-divider>
                <el-tag type="success">已提取 {{ generatedPoints.length }} 个知识点</el-tag>
              </el-divider>
              <div class="generated-list" style="max-height: 400px; overflow-y: auto; border: 1px solid #eee; padding: 10px; border-radius: 4px;">
                <div v-for="(kp, index) in generatedPoints" :key="index" style="margin-bottom: 15px; border-bottom: 1px dashed #eee; padding-bottom: 10px;">
                  <div style="display: flex; justify-content: space-between; margin-bottom: 5px; align-items: center;">
                    <el-tag size="small" type="info" style="margin-right: 10px;">{{ index + 1 }}</el-tag>
                    <el-input v-model="kp.name" placeholder="知识点名称" style="flex: 1; font-weight: bold;" />
                    <el-button type="danger" icon="Delete" circle size="small" @click="removeGenerated(index)" style="margin-left: 10px;" />
                  </div>
                  <el-input v-model="kp.description" type="textarea" :rows="2" placeholder="描述" />
                </div>
              </div>
            </div>
            <el-empty v-else description="正在提取知识点，请稍候..." />
            </div>
          </div>

          <template #footer>
            <div v-if="step === 1">
              <el-button @click="aiDialogVisible = false">取消</el-button>
              <el-button
                type="primary"
                @click="handleAiGenerate"
                :loading="generating"
                :disabled="textLength === 0"
              >
                开始提取
              </el-button>
            </div>
            <div v-else>
              <el-button @click="step = 1" :disabled="generating">返回修改</el-button>
              <el-button
                type="success"
                @click="batchSaveAiPoints"
                :loading="saving"
                :disabled="generatedPoints.length === 0"
              >
                确认入库 ({{ generatedPoints.length }}个)
              </el-button>
            </div>
          </template>
        </el-dialog>

        <ai-key-dialog v-model:visible="keyDialogVisible" />

        <el-input
            v-model="queryParams.name"
            placeholder="输入知识点名称、编码或标签搜索"
            size="large"
            style="width: 300px;"
            clearable
            @keyup.enter="handleQuery"
            @clear="handleQuery"
        >
          <template #append>
            <el-button :icon="Search" @click="handleQuery" />
          </template>
        </el-input>

        <div>
          <el-button-group>
            <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'"/>
            <el-button :icon="Menu" :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'"/>
          </el-button-group>
        </div>
      </div>

      <div v-if="viewMode === 'grid'" class="card-grid">
        <div v-for="kp in knowledgePointList" :key="kp.id" class="knowledge-point-card" @click="handlePreview(kp.id)">
          <div class="card-header">
            <div>
              <el-tag size="small">{{ getSubjectName(kp.subjectId) }}</el-tag>
              <el-tag v-if="kp.grade" size="small" type="success" style="margin-left: 8px;">{{ kp.grade }}</el-tag>
            </div>
            <el-dropdown @click.stop>
              <el-icon class="el-dropdown-link"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleUpdate(kp.id)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(kp.id)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <h3 class="card-title">{{ kp.name }}</h3>
          <p class="card-desc">{{ kp.description }}</p>
          <div class="card-footer">
            <span class="card-code">CODE: {{ kp.code }}</span>
            <div class="card-tags">
              <el-tag v-if="kp.tags" v-for="tag in kp.tags.split(',')" :key="tag" type="info" size="small">{{ tag }}</el-tag>
              <el-tag type="success" size="small">题目: {{ kp.questionCount || 0 }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <el-table v-if="viewMode === 'list'" :data="knowledgePointList" v-loading="loading" style="width: 100%; margin-top: 20px;" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="知识点名称" />
        <el-table-column prop="grade" label="年级" width="120" />
        <el-table-column prop="code" label="编码" />
        <el-table-column prop="questionCount" label="关联题目数" width="120" align="center"/>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
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
          :page-sizes="[12, 24, 36, 48]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="margin-top: 20px; justify-content: flex-end; display: flex;"
      />
    </el-card>

    <knowledge-point-edit-dialog
        v-model:visible="isDialogVisible"
        :knowledge-point-id="editingId"
        @success="getList"
    />
    <knowledge-point-detail-dialog
        v-if="isDetailDialogVisible"
        v-model:visible="isDetailDialogVisible"
        :knowledge-point-id="selectedKpId"
    />

    <!-- 批量编辑对话框 -->
    <el-dialog
      v-model="batchEditDialogVisible"
      title="批量编辑知识点"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-alert title="批量编辑提示" type="info" :closable="false" style="margin-bottom: 15px;">
        <p>已选中 {{ selectedKnowledgePoints.length }} 个知识点</p>
        <p>留空的字段将不会被修改</p>
      </el-alert>
      <el-form :model="batchEditForm" label-width="100px">
        <el-form-item label="所属科目">
          <el-select v-model="batchEditForm.subjectId" placeholder="请选择科目（不修改请留空）" clearable style="width: 100%">
            <el-option v-for="s in allSubjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用年级">
          <el-select v-model="batchEditForm.grade" placeholder="请选择年级（不修改请留空）" clearable style="width: 100%">
            <el-option v-for="g in ['七年级','八年级','九年级','高一','高二','高三']" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="batchEditForm.tags" placeholder="请输入标签（不修改请留空）" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchUpdate" :loading="batchUpdating">确认更新</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'; // 确保导入 watch
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchKnowledgePointList, deleteKnowledgePoint, fetchKnowledgePointGlobalStats, batchUpdateKnowledgePoints } from '@/api/knowledgePoint';
import type { KnowledgePoint, KnowledgePointPageParams, KnowledgePointGlobalStats } from '@/api/knowledgePoint';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { Plus, Edit, Delete, Search, Grid, Menu, MoreFilled, MagicStick, QuestionFilled, Loading, Upload } from '@element-plus/icons-vue';
import KnowledgePointEditDialog from '@/components/knowledge-point/KnowledgePointEditDialog.vue';
import KnowledgePointDetailDialog from '@/components/knowledge-point/KnowledgePointDetailDialog.vue';
import { generateKnowledgePointsFromText, generateKnowledgePointsFromTextStream, createKnowledgePoint } from '@/api/knowledgePoint';
import AiKeyDialog from '@/components/student/AiKeyDialog.vue';

// AI 相关变量
const aiDialogVisible = ref(false);
const keyDialogVisible = ref(false);
const step = ref(1);
const generating = ref(false);
const saving = ref(false);
const aiForm = reactive({ text: '', count: 5 });
const aiSaveConfig = reactive({ subjectId: undefined as number | undefined, grade: '' });
const generatedPoints = ref<any[]>([]);
const textLength = ref(0);
const loadingText = ref('正在提取知识点...');

// 【流式响应】新增变量
const useStreamMode = ref(true); // 默认启用流式模式
const streamContent = ref(''); // 流式输出内容

// 【文件上传】新增变量
const uploadedFileName = ref('');
const uploadRef = ref();

// 计算预估处理时间（大文本分块处理）
const estimatedTime = computed(() => {
  return Math.ceil(textLength.value / 6000) * 10;
});

const knowledgePointList = ref<KnowledgePoint[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

// 【知识点功能增强】全局统计数据
const globalStats = ref<KnowledgePointGlobalStats>({
  totalCount: 0,
  totalQuestionCount: 0,
  unassociatedCount: 0,
  unassociatedPercentage: 0
});

const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);
const viewMode = ref<'grid' | 'list'>('grid');
// const searchQuery = ref(''); // 移除旧的搜索变量，合并入 queryParams

// 【新增】分页和查询参数
const queryParams = reactive<KnowledgePointPageParams>({
  current: 1,
  size: 12, // 默认一页12个，适合卡片布局
  subjectId: undefined,
  name: ''
});

const isDetailDialogVisible = ref(false);
const selectedKpId = ref<number | null>(null);

// 批量编辑相关
const selectedKnowledgePoints = ref<KnowledgePoint[]>([]);
const batchEditDialogVisible = ref(false);
const batchUpdating = ref(false);
const batchEditForm = reactive({
  subjectId: undefined as number | undefined,
  grade: '',
  tags: ''
});

// AI 相关方法保持不变 ...
const openAiDialog = () => {
  if (!localStorage.getItem('student_ai_key')) {
    ElMessage.warning('请先配置 AI API Key');
    keyDialogVisible.value = true;
    return;
  }
  step.value = 1;
  aiForm.text = '';
  generatedPoints.value = [];
  uploadedFileName.value = '';
  aiDialogVisible.value = true;
};

// 【新增】文件上传处理
const handleFileChange = async (file: any) => {
  const rawFile = file.raw;
  if (!rawFile) return;

  uploadedFileName.value = rawFile.name;
  const fileExtension = rawFile.name.split('.').pop()?.toLowerCase();

  try {
    // .txt 和 .md 文件可以直接在前端读取
    if (fileExtension === 'txt' || fileExtension === 'md') {
      const reader = new FileReader();
      reader.onload = (e) => {
        aiForm.text = e.target?.result as string;
        updateTextLength();
        ElMessage.success('文件读取成功');
      };
      reader.onerror = () => {
        ElMessage.error('文件读取失败');
      };
      reader.readAsText(rawFile, 'UTF-8');
    }
    // PDF、Word等需要后端解析
    else if (['pdf', 'doc', 'docx'].includes(fileExtension || '')) {
      const formData = new FormData();
      formData.append('file', rawFile);

      ElMessage.info('正在解析文件，请稍候...');

      // 调用后端文件解析API
      const response = await fetch('/api/v1/ai/parse-file', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: formData
      });

      if (!response.ok) {
        throw new Error('文件解析失败');
      }

      const result = await response.json();
      if (result.code === 200) {
        aiForm.text = result.data.text;
        updateTextLength();
        ElMessage.success(`文件解析成功，提取了 ${result.data.text.length} 字符`);
      } else {
        throw new Error(result.msg || '文件解析失败');
      }
    } else {
      ElMessage.warning('不支持的文件格式');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '文件处理失败');
    uploadedFileName.value = '';
  }
};

// 【新增】清除上传的文件
const clearUploadedFile = () => {
  uploadedFileName.value = '';
  aiForm.text = '';
  updateTextLength();
};

// 更新文本长度
const updateTextLength = () => {
  textLength.value = aiForm.text.length;
};

// 获取文本长度标签类型
const getTextLengthType = () => {
  if (textLength.value > 8000) return 'warning';
  if (textLength.value > 3000) return 'success';
  return 'info';
};

const handleAiGenerate = async () => {
  if (!aiForm.text) return ElMessage.warning('请输入文本');

  generating.value = true;
  streamContent.value = '';
  generatedPoints.value = [];
  step.value = 2;

  try {
    if (useStreamMode.value) {
      // 使用流式API
      generateKnowledgePointsFromTextStream(
        aiForm,
        // onChunk: 接收流式数据
        (chunk: string) => {
          streamContent.value += chunk;
        },
        // onComplete: 完成时接收完整结果
        (points: any[]) => {
          generatedPoints.value = points;
          generating.value = false;
          ElMessage.success(`成功提取 ${points.length} 个知识点`);
        },
        // onError: 错误处理
        (error: Error) => {
          generating.value = false;
          ElMessage.error('生成失败: ' + error.message);
        }
      );
    } else {
      // 使用标准API（原有代码）
      const res = await generateKnowledgePointsFromText(aiForm);
      if (res.code === 200) {
        generatedPoints.value = res.data;
      }
      generating.value = false;
    }
  } catch(e) {
    generating.value = false;
  }
};

const removeGenerated = (index: number) => {
  generatedPoints.value.splice(index, 1);
};

const batchSaveAiPoints = async () => {
  if (!aiSaveConfig.subjectId) return ElMessage.warning('请选择科目');
  saving.value = true;
  try {
    for (const kp of generatedPoints.value) {
      await createKnowledgePoint({
        name: kp.name,
        description: kp.description,
        subjectId: aiSaveConfig.subjectId,
        grade: aiSaveConfig.grade,
        code: 'AI-' + Math.floor(Math.random() * 10000)
      });
    }
    ElMessage.success(`成功入库 ${generatedPoints.value.length} 个知识点`);
    aiDialogVisible.value = false;
    getList();
  } catch(e) {
    ElMessage.error('保存部分失败，请检查');
  } finally {
    saving.value = false;
  }
};

// 【知识点功能增强】加载全局统计数据
const loadGlobalStats = async () => {
  try {
    const res = await fetchKnowledgePointGlobalStats();
    if (res.code === 200 && res.data) {
      globalStats.value = res.data;
    }
  } catch (error) {
    console.error('获取全局统计失败:', error);
  }
};

// 【修改】核心数据加载方法：使用 queryParams 调用后端接口
const getList = async () => {
  loading.value = true;
  try {
    // 直接透传分页参数和搜索条件
    const response = await fetchKnowledgePointList(queryParams);
    knowledgePointList.value = response.data;
    total.value = response.total;
    // 【知识点功能增强】加载全局统计
    await loadGlobalStats();
  } finally {
    loading.value = false;
  }
};

// 【新增】处理搜索
const handleQuery = () => {
  queryParams.current = 1; // 搜索时重置为第一页
  getList();
};

// 【新增】处理分页页码变化
const handleCurrentChange = (val: number) => {
  queryParams.current = val;
  getList();
};

// 【新增】处理每页条数变化
const handleSizeChange = (val: number) => {
  queryParams.size = val;
  queryParams.current = 1; // 重置到第一页
  getList();
};

const getAllSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) {
    allSubjects.value = res.data;
  }
}

const getSubjectName = (subjectId: number) => {
  const subject = allSubjects.value.find(s => s.id === subjectId);
  return subject ? subject.name : '未知科目';
};

const handleCreate = () => {
  editingId.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (id: number) => {
  editingId.value = id;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该知识点吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deleteKnowledgePoint(id);
        ElMessage.success('删除成功');
        getList();
      });
};

const handlePreview = (id: number) => {
  selectedKpId.value = id;
  isDetailDialogVisible.value = true;
};

// 批量编辑相关方法
const handleSelectionChange = (selection: KnowledgePoint[]) => {
  selectedKnowledgePoints.value = selection;
};

const openBatchEditDialog = () => {
  // 重置表单
  batchEditForm.subjectId = undefined;
  batchEditForm.grade = '';
  batchEditForm.tags = '';
  batchEditDialogVisible.value = true;
};

const handleBatchUpdate = async () => {
  if (selectedKnowledgePoints.value.length === 0) {
    ElMessage.warning('请先选择要编辑的知识点');
    return;
  }

  // 检查是否至少有一个字段要更新
  if (!batchEditForm.subjectId && !batchEditForm.grade && !batchEditForm.tags) {
    ElMessage.warning('请至少填写一个要更新的字段');
    return;
  }

  batchUpdating.value = true;
  try {
    const knowledgePointIds = selectedKnowledgePoints.value.map(kp => kp.id);
    await batchUpdateKnowledgePoints({
      knowledgePointIds,
      subjectId: batchEditForm.subjectId,
      grade: batchEditForm.grade || undefined,
      tags: batchEditForm.tags || undefined
    });
    ElMessage.success('批量更新成功');
    batchEditDialogVisible.value = false;
    selectedKnowledgePoints.value = [];
    getList();
  } catch (error) {
    console.error('批量更新失败:', error);
  } finally {
    batchUpdating.value = false;
  }
};

// 监听视图模式切换，自动重置分页到第一页并刷新
watch(viewMode, () => {
  queryParams.current = 1;
  getList();
});

onMounted(() => {
  getAllSubjects().then(getList);
});
</script>

<style scoped>
/* 样式保持不变 */
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; font-weight: 600; }
.page-header p { color: var(--text-color-regular); margin-top: 4px; font-size: 14px; }
.stats-cards { margin-bottom: 20px; }
.stat-item { padding: 8px; }
.stat-item .label { color: var(--text-color-regular); font-size: 14px; margin-bottom: 8px;}
.stat-item .value { font-size: 28px; font-weight: bold; display: flex; align-items: center; }
.content-card { background-color: var(--bg-color-container); }
.content-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.knowledge-point-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  background-color: var(--bg-color);
  transition: all 0.3s;
  cursor: pointer;
}
.knowledge-point-card:hover { box-shadow: var(--card-shadow); transform: translateY(-5px); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.el-dropdown-link { cursor: pointer; color: var(--text-color-regular); }
.card-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.card-desc { font-size: 14px; color: var(--text-color-regular); height: 40px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; margin-bottom: 16px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 12px; }
.card-code { font-size: 12px; color: var(--text-color-regular); }
.card-tags { display: flex; gap: 6px; align-items: center; }
</style>