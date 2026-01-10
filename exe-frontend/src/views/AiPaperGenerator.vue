<template>
  <div class="ai-paper-generator">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Magic /></el-icon>
          <span>AI 智能生成试卷</span>
        </div>
      </template>

      <!-- 配置表单 -->
      <el-form :model="formData" label-width="120px" v-if="!generating && !generated">
        <el-form-item label="试卷标题" required>
          <el-input v-model="formData.paperTitle" placeholder="例如：高中数学期末测试卷" />
        </el-form-item>

        <el-form-item label="科目" required>
          <el-select v-model="formData.subjectId" placeholder="请选择科目" @change="onSubjectChange">
            <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="知识点" required>
          <el-select v-model="formData.knowledgePointIds" multiple placeholder="请选择知识点（可多选）" style="width: 100%">
            <el-option v-for="kp in knowledgePoints" :key="kp.id" :label="kp.name" :value="kp.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="难度分布" required>
          <div class="difficulty-config">
            <el-slider v-model="formData.easy" :max="100" :show-tooltip="false" />
            <span>简单：{{ formData.easy }}%</span>
          </div>
          <div class="difficulty-config">
            <el-slider v-model="formData.medium" :max="100" :show-tooltip="false" />
            <span>中等：{{ formData.medium }}%</span>
          </div>
          <div class="difficulty-config">
            <el-slider v-model="formData.hard" :max="100" :show-tooltip="false" />
            <span>困难：{{ formData.hard }}%</span>
          </div>
          <div class="difficulty-total">总计：{{ formData.easy + formData.medium + formData.hard }}%</div>
        </el-form-item>

        <el-form-item label="题型配置" required>
          <div v-for="(type, index) in formData.questionTypes" :key="index" class="question-type-item">
            <el-select v-model="type.type" placeholder="题型" style="width: 150px">
              <el-option label="单选题" value="单选" />
              <el-option label="多选题" value="多选" />
              <el-option label="判断题" value="判断" />
              <el-option label="填空题" value="填空" />
              <el-option label="主观题" value="主观" />
            </el-select>
            <el-input-number v-model="type.count" :min="1" :max="50" style="margin: 0 10px" />
            <span>道</span>
            <el-button type="danger" link @click="removeQuestionType(index)" v-if="formData.questionTypes.length > 1">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-button type="primary" link @click="addQuestionType">
            <el-icon><Plus /></el-icon>
            添加题型
          </el-button>
        </el-form-item>

        <el-form-item label="试卷总分">
          <el-input-number v-model="formData.totalScore" :min="10" :max="500" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="generatePaper" :loading="generating">
            <el-icon><Magic /></el-icon>
            开始生成
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 生成过程 -->
      <div v-if="generating" class="generating-view">
        <el-alert type="info" :closable="false">
          <template #title>
            <div class="generating-title">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>AI 正在生成试卷，请稍候...</span>
            </div>
          </template>
        </el-alert>
        <el-card shadow="never" class="stream-content">
          <pre>{{ streamContent }}</pre>
        </el-card>
      </div>

      <!-- 生成结果 -->
      <div v-if="generated" class="generated-view">
        <div class="result-header">
          <h2>{{ generatedPaper.paperName }}</h2>
          <div class="result-actions">
            <el-button @click="resetForm">重新生成</el-button>
            <el-button type="primary" @click="savePaper">保存试卷</el-button>
          </div>
        </div>

        <div class="paper-info">
          <span>总分：{{ generatedPaper.totalScore }}分</span>
          <span>题目数：{{ generatedPaper.questions?.length || 0 }}道</span>
        </div>

        <div class="questions-list">
          <div v-for="(q, index) in generatedPaper.questions" :key="index" class="question-item">
            <div class="question-header">
              <span class="question-number">{{ index + 1 }}.</span>
              <el-tag size="small">{{ q.type }}</el-tag>
              <el-tag size="small" type="info">{{ q.difficulty }}</el-tag>
              <span class="question-score">{{ q.score }}分</span>
            </div>
            <div class="question-content">{{ q.content }}</div>
            <div v-if="q.options && q.options.length" class="question-options">
              <div v-for="opt in q.options" :key="opt.key">
                {{ opt.key }}. {{ opt.value }}
              </div>
            </div>
            <div class="question-answer">
              <strong>答案：</strong>{{ q.answer }}
            </div>
            <div class="question-analysis">
              <strong>解析：</strong>{{ q.analysis }}
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Magic, Loading, Plus, Delete } from '@element-plus/icons-vue';
import { fetchAllSubjects } from '@/api/subject';
import { fetchAllKnowledgePoints } from '@/api/knowledgePoint';

// 表单数据
const formData = reactive({
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

// 数据
const subjects = ref<any[]>([]);
const knowledgePoints = ref<any[]>([]);
const generating = ref(false);
const generated = ref(false);
const streamContent = ref('');
const generatedPaper = ref<any>({});

// 加载科目和知识点
onMounted(async () => {
  try {
    const subjectRes = await fetchAllSubjects();
    if (subjectRes.code === 200) {
      subjects.value = subjectRes.data;
    }
  } catch (error) {
    console.error('加载科目失败:', error);
  }
});

const onSubjectChange = async () => {
  const subject = subjects.value.find(s => s.id === formData.subjectId);
  formData.subjectName = subject?.name || '';

  try {
    const kpRes = await fetchAllKnowledgePoints({ subjectId: formData.subjectId });
    if (kpRes.code === 200) {
      knowledgePoints.value = kpRes.data;
    }
  } catch (error) {
    console.error('加载知识点失败:', error);
  }
};

const addQuestionType = () => {
  formData.questionTypes.push({ type: '单选', count: 5 });
};

const removeQuestionType = (index: number) => {
  formData.questionTypes.splice(index, 1);
};

const generatePaper = async () => {
  // 验证表单
  if (!formData.paperTitle) {
    ElMessage.warning('请输入试卷标题');
    return;
  }
  if (!formData.subjectId) {
    ElMessage.warning('请选择科目');
    return;
  }
  if (formData.knowledgePointIds.length === 0) {
    ElMessage.warning('请选择至少一个知识点');
    return;
  }
  const totalPercent = formData.easy + formData.medium + formData.hard;
  if (totalPercent !== 100) {
    ElMessage.warning('难度分布总和必须为100%');
    return;
  }

  generating.value = true;
  streamContent.value = '';

  // 准备请求数据
  const knowledgePointNames = formData.knowledgePointIds
    .map(id => knowledgePoints.value.find(kp => kp.id === id)?.name)
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
        paperTitle: formData.paperTitle,
        subjectName: formData.subjectName,
        knowledgePoints: knowledgePointNames,
        difficultyDistribution: `简单:${formData.easy}%,中等:${formData.medium}%,困难:${formData.hard}%`,
        questionTypes: JSON.stringify(formData.questionTypes),
        totalScore: formData.totalScore
      })
    });

    const reader = response.body!.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    let currentEvent = '';

    function processText(text: string) {
      buffer += text;
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (let line of lines) {
        line = line.trim();
        if (!line) {
          currentEvent = '';
          continue;
        }

        if (line.startsWith('event:')) {
          currentEvent = line.substring(6).trim();
        } else if (line.startsWith('data:')) {
          const dataContent = line.substring(5).trim();
          if (currentEvent === 'done') {
            const paperData = JSON.parse(dataContent);
            generatedPaper.value = paperData;
            generating.value = false;
            generated.value = true;
            ElMessage.success('试卷生成完成！');
          } else if (currentEvent === 'message' || !currentEvent) {
            streamContent.value += dataContent;
          }
        }
      }
    }

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      processText(decoder.decode(value, { stream: true }));
    }
  } catch (error: any) {
    generating.value = false;
    ElMessage.error('生成失败: ' + error.message);
    console.error(error);
  }
};

const savePaper = async () => {
  ElMessage.info('保存功能开发中...');
  // TODO: 调用保存试卷 API
};

const resetForm = () => {
  generating.value = false;
  generated.value = false;
  streamContent.value = '';
  generatedPaper.value = {};
};
</script>

<style scoped>
.ai-paper-generator {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 500;
}

.difficulty-config {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.difficulty-config .el-slider {
  flex: 1;
}

.difficulty-total {
  margin-top: 10px;
  font-weight: 500;
  color: #409eff;
}

.question-type-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.generating-view, .generated-view {
  margin-top: 20px;
}

.generating-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stream-content {
  margin-top: 15px;
  max-height: 400px;
  overflow-y: auto;
}

.stream-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.paper-info {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  font-size: 16px;
  color: #606266;
}

.questions-list {
  margin-top: 30px;
}

.question-item {
  background: #f5f7fa;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.question-number {
  font-weight: 600;
  font-size: 16px;
}

.question-score {
  margin-left: auto;
  color: #f56c6c;
  font-weight: 500;
}

.question-content {
  margin: 15px 0;
  font-size: 15px;
  line-height: 1.6;
}

.question-options {
  margin: 15px 0;
  padding-left: 20px;
}

.question-options > div {
  margin: 8px 0;
}

.question-answer, .question-analysis {
  margin-top: 15px;
  padding: 10px;
  background: white;
  border-radius: 4px;
}

.result-actions {
  display: flex;
  gap: 10px;
}
</style>
