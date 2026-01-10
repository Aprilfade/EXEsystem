<template>
  <div class="page-container">
    <div class="page-header">
      <h2>AI 智能出题</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-row :gutter="20" class="main-row">
      <el-col :span="10">
        <el-card shadow="never" class="input-card">
          <template #header>
            <div class="card-header">
              <span>素材输入</span>
              <div style="display: flex; align-items: center; gap: 15px;">
                <el-switch
                  v-model="useStreamMode"
                  active-text="流式模式"
                  inactive-text="标准模式"
                  style="--el-switch-on-color: #13ce66;"
                />
                <el-button type="primary" :loading="loading" @click="handleGenerate">
                  <el-icon><MagicStick /></el-icon> 开始生成
                </el-button>
              </div>
            </div>
          </template>

          <el-form label-position="top">
            <el-form-item label="文本内容">
              <el-input
                  v-model="form.text"
                  type="textarea"
                  :rows="15"
                  placeholder="请粘贴课文、笔记或知识点文本（建议 500-2000 字）..."
              />
            </el-form-item>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="生成数量">
                  <el-input-number v-model="form.count" :min="1" :max="10" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="题型">
                  <el-select v-model="form.type" style="width: 100%;">
                    <el-option label="单选题" :value="1" />
                    <el-option label="多选题" :value="2" />
                    <el-option label="填空题" :value="3" />
                    <el-option label="判断题" :value="4" />
                    <el-option label="主观题" :value="5" />
                    <el-option label="混合生成" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>

      <!-- 流式输出显示区域 -->
      <el-col :span="14" v-if="useStreamMode && loading && streamContent">
        <el-card shadow="never" style="margin-bottom: 20px;">
          <template #header>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>AI 正在生成题目，实时输出...</span>
            </div>
          </template>
          <div style="max-height: 400px; overflow-y: auto; background: #f5f7fa; padding: 15px; border-radius: 4px;">
            <pre style="white-space: pre-wrap; word-wrap: break-word; font-family: 'Courier New', monospace; font-size: 13px; line-height: 1.6; margin: 0;">{{ streamContent }}</pre>
          </div>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="card-header">
              <span>生成结果预览 ({{ generatedQuestions.length }})</span>
              <div>
                <el-button type="success" :disabled="generatedQuestions.length === 0" @click="openSaveDialog">
                  批量入库
                </el-button>
              </div>
            </div>
          </template>

          <div class="question-list">
            <el-empty v-if="generatedQuestions.length === 0" description="暂无生成结果，请在左侧输入文本并点击生成" />

            <div v-for="(q, index) in generatedQuestions" :key="index" class="q-item">
              <div class="q-header">
                <el-tag size="small">{{ getTypeName(q.questionType) }}</el-tag>
                <div class="actions">
                  <el-button type="danger" icon="Delete" circle size="small" @click="removeQuestion(index)" />
                </div>
              </div>
              <el-input v-model="q.content" type="textarea" :rows="2" class="q-content-input" />

              <div v-if="[1, 2].includes(q.questionType)" class="options-area">
                <div v-for="(opt, oIdx) in q.options" :key="oIdx" class="opt-row">
                  <span class="opt-key">{{ opt.key }}.</span>
                  <el-input v-model="opt.value" size="small" />
                </div>
              </div>

              <div class="answer-row">
                <span class="label">答案：</span>

                <el-input
                    v-if="q.questionType === 5"
                    v-model="q.answer"
                    type="textarea"
                    :rows="2"
                    size="small"
                    placeholder="参考答案"
                />
                <el-input
                    v-else
                    v-model="q.answer"
                    size="small"
                    style="width: 150px;"
                />
              </div>

              <div class="desc-row">
                <span class="label">解析：</span>
                <el-input v-model="q.description" type="textarea" :rows="1" size="small" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="saveDialogVisible" title="保存到题库" width="400px">
      <el-form>
        <el-form-item label="所属科目">
          <el-select v-model="saveConfig.subjectId" placeholder="请选择科目" style="width: 100%;">
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用年级">
          <el-select v-model="saveConfig.grade" placeholder="请选择年级" style="width: 100%;">
            <el-option label="七年级" value="七年级" />
            <el-option label="八年级" value="八年级" />
            <el-option label="九年级" value="九年级" />
            <el-option label="高一" value="高一" />
            <el-option label="高二" value="高二" />
            <el-option label="高三" value="高三" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmSave">确认保存</el-button>
      </template>
    </el-dialog>

    <ai-key-dialog v-model:visible="keyDialogVisible" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { MagicStick, Delete, Loading } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { generateQuestionsFromText, generateQuestionsFromTextStream, createQuestion } from '@/api/question';
import { fetchAllSubjects, type Subject } from '@/api/subject';
import AiKeyDialog from '@/components/student/AiKeyDialog.vue'; // 复用组件

const loading = ref(false);
const saving = ref(false);
const saveDialogVisible = ref(false);
const keyDialogVisible = ref(false);
const allSubjects = ref<Subject[]>([]);

// 【流式响应】新增变量
const useStreamMode = ref(true); // 默认启用流式模式
const streamContent = ref(''); // 流式输出内容

const form = reactive({
  text: '',
  count: 3,
  type: 1
});

const saveConfig = reactive({
  subjectId: undefined as number | undefined,
  grade: ''
});

const generatedQuestions = ref<any[]>([]);

const handleGenerate = async () => {
  if (!form.text.trim()) {
    ElMessage.warning('请输入文本内容');
    return;
  }
  // 简单检查是否有 Key
  if (!localStorage.getItem('student_ai_key')) {
    ElMessage.warning('请先配置 AI API Key');
    keyDialogVisible.value = true;
    return;
  }

  loading.value = true;
  streamContent.value = '';
  generatedQuestions.value = [];

  try {
    if (useStreamMode.value) {
      // 使用流式API
      generateQuestionsFromTextStream(
        form,
        // onChunk: 接收流式数据
        (chunk: string) => {
          streamContent.value += chunk;
        },
        // onComplete: 完成时接收完整结果
        (questions: any[]) => {
          // 对返回数据做简单的格式化处理，确保 options 是数组
          generatedQuestions.value = questions.map((q: any) => ({
            ...q,
            options: q.options || []
          }));
          loading.value = false;
          ElMessage.success(`成功生成 ${questions.length} 道题目`);
        },
        // onError: 错误处理
        (error: Error) => {
          loading.value = false;
          ElMessage.error('生成失败: ' + error.message);
        }
      );
    } else {
      // 使用标准API（原有代码）
      const res = await generateQuestionsFromText(form);
      if (res.code === 200) {
        // 对返回数据做简单的格式化处理，确保 options 是数组
        generatedQuestions.value = res.data.map((q: any) => ({
          ...q,
          options: q.options || []
        }));
        ElMessage.success(`成功生成 ${res.data.length} 道题目`);
      }
      loading.value = false;
    }
  } catch (e) {
    loading.value = false;
  }
};

const removeQuestion = (index: number) => {
  generatedQuestions.value.splice(index, 1);
};

const getTypeName = (type: number) => {
  // 【修改】增加 5: '主观题'
  const map: any = { 1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题' };
  return map[type] || '未知';
};
const openSaveDialog = async () => {
  // 加载科目
  if (allSubjects.value.length === 0) {
    const res = await fetchAllSubjects();
    if (res.code === 200) allSubjects.value = res.data;
  }
  saveDialogVisible.value = true;
};

const confirmSave = async () => {
  if (!saveConfig.subjectId) {
    ElMessage.warning('请选择科目');
    return;
  }
  saving.value = true;
  try {
    // 循环调用创建接口 (也可以后端写个 batch 接口，这里为了省事直接循环)
    let successCount = 0;
    for (const q of generatedQuestions.value) {
      const payload = {
        subjectId: saveConfig.subjectId,
        grade: saveConfig.grade,
        questionType: q.questionType,
        content: q.content,
        answer: q.answer,
        description: q.description,
        options: JSON.stringify(q.options), // 转为 JSON 字符串存入数据库
        knowledgePointIds: [] // 暂时不关联具体知识点，或者可以让 AI 推荐
      };
      await createQuestion(payload);
      successCount++;
    }
    ElMessage.success(`成功入库 ${successCount} 道题目`);
    saveDialogVisible.value = false;
    generatedQuestions.value = []; // 清空
    form.text = ''; // 清空输入
  } catch (e) {
    ElMessage.error('保存过程中出现错误');
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.input-card, .preview-card { height: 100%; min-height: 600px; display: flex; flex-direction: column; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.question-list { max-height: 600px; overflow-y: auto; padding-right: 10px; }
.q-item { border: 1px solid #e4e7ed; border-radius: 4px; padding: 15px; margin-bottom: 15px; background: #fff; }
.q-header { display: flex; justify-content: space-between; margin-bottom: 10px; }
.q-content-input { margin-bottom: 10px; font-weight: bold; }
.opt-row { display: flex; align-items: center; margin-bottom: 5px; gap: 5px; }
.opt-key { width: 20px; font-weight: bold; flex-shrink: 0; }
.answer-row, .desc-row { display: flex; align-items: center; margin-top: 10px; gap: 5px; }
.label { width: 50px; flex-shrink: 0; color: #909399; font-size: 13px; }
</style>