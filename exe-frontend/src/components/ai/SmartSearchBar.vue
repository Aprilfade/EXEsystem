<!-- SmartSearchBar.vue - AI智能搜索栏 -->
<template>
  <div class="smart-search-container">
    <el-input
      v-model="searchQuery"
      class="smart-search-input"
      placeholder="试试用自然语言搜索：我想学习函数、找一些数学题..."
      clearable
      @keyup.enter="handleSearch"
      @focus="showSuggestions = true"
    >
      <template #prefix>
        <el-icon class="search-icon"><MagicStickStick /></el-icon>
      </template>
      <template #append>
        <el-button @click="handleSearch" :loading="searching">
          智能搜索
        </el-button>
      </template>
    </el-input>

    <!-- 搜索建议 -->
    <transition name="fade">
      <div v-if="showSuggestions && !searching" class="search-suggestions">
        <div class="suggestion-title">💡 试试这些搜索</div>
        <div
          v-for="(suggestion, index) in suggestions"
          :key="index"
          class="suggestion-item"
          @click="quickSearch(suggestion.text)"
        >
          <el-icon>{{ suggestion.icon }}</el-icon>
          <span>{{ suggestion.text }}</span>
        </div>
      </div>
    </transition>

    <!-- 搜索结果对话框 -->
    <el-dialog
      v-model="showResults"
      :title="`搜索结果 - ${intentName}`"
      width="800px"
      destroy-on-close
    >
      <!-- AI直接回答 -->
      <div v-if="searchResult.answer" class="ai-answer">
        <h4>🤖 AI回答</h4>
        <div class="answer-content">{{ searchResult.answer }}</div>

        <el-divider />

        <div class="answer-actions">
          <el-button @click="copyAnswer">
            <el-icon><CopyDocument /></el-icon>
            复制回答
          </el-button>
          <el-button @click="askFollowUp">
            <el-icon><ChatDotRound /></el-icon>
            追问
          </el-button>
        </div>
      </div>

      <!-- 题目结果 -->
      <div v-if="searchResult.questions && searchResult.questions.length > 0" class="search-results">
        <h4>📝 相关题目 ({{ searchResult.questions.length }})</h4>
        <div
          v-for="q in searchResult.questions"
          :key="q.id"
          class="result-item question-result"
        >
          <div class="result-header">
            <el-tag size="small" type="info">题目</el-tag>
            <el-tag size="small" type="success">
              相关度: {{ (q.relevance * 100).toFixed(0) }}%
            </el-tag>
          </div>
          <p class="result-content">{{ q.content }}</p>
          <div class="result-actions">
            <el-button type="primary" link @click="viewQuestion(q.id)">
              查看详情
            </el-button>
            <el-button link @click="startPractice(q.id)">
              立即练习
            </el-button>
          </div>
        </div>
      </div>

      <!-- 课程结果 -->
      <div v-if="searchResult.courses && searchResult.courses.length > 0" class="search-results">
        <h4>📚 推荐课程 ({{ searchResult.courses.length }})</h4>
        <div
          v-for="c in searchResult.courses"
          :key="c.id"
          class="result-item course-result"
        >
          <div class="result-header">
            <el-tag size="small" type="warning">课程</el-tag>
            <el-tag size="small" type="success">
              相关度: {{ (c.relevance * 100).toFixed(0) }}%
            </el-tag>
          </div>
          <h5>{{ c.name }}</h5>
          <div class="result-actions">
            <el-button type="primary" @click="gotoCourse(c.id)">
              开始学习
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空结果 -->
      <el-empty
        v-if="!searchResult.answer &&
               (!searchResult.questions || searchResult.questions.length === 0) &&
               (!searchResult.courses || searchResult.courses.length === 0)"
        description="没有找到相关内容，试试换个关键词"
      />
    </el-dialog>

    <!-- 追问对话框 -->
    <el-dialog
      v-model="showFollowUp"
      title="继续提问"
      width="600px"
      append-to-body
    >
      <el-input
        v-model="followUpQuestion"
        type="textarea"
        :rows="4"
        placeholder="继续提问..."
        @keyup.enter.ctrl="submitFollowUp"
      />
      <template #footer>
        <el-button @click="showFollowUp = false">取消</el-button>
        <el-button type="primary" @click="submitFollowUp" :loading="searching">
          发送 (Ctrl+Enter)
        </el-button>
      </template>
    </el-dialog>

    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="showQuestionDetail"
      title="题目详情"
      width="700px"
      append-to-body
    >
      <div v-if="currentQuestion" v-loading="loadingQuestion">
        <div class="question-detail-header">
          <el-tag :type="getQuestionTypeTag(currentQuestion.questionType)">
            {{ getQuestionTypeText(currentQuestion.questionType) }}
          </el-tag>
          <el-tag type="info" v-if="currentQuestion.grade">
            {{ currentQuestion.grade }}
          </el-tag>
        </div>

        <div class="question-detail-content">
          <h4>题目内容</h4>
          <p>{{ currentQuestion.content }}</p>
          <img v-if="currentQuestion.imageUrl" :src="currentQuestion.imageUrl" alt="题目图片" class="question-image" />
        </div>

        <div v-if="currentQuestion.options && Array.isArray(currentQuestion.options) && currentQuestion.options.length > 0" class="question-detail-options">
          <h4>选项</h4>
          <div v-for="opt in currentQuestion.options" :key="opt.key" class="option-item">
            <span class="option-key">{{ opt.key }}.</span>
            <span class="option-value">{{ opt.value }}</span>
          </div>
        </div>

        <div class="question-detail-answer">
          <h4>答案</h4>
          <div class="answer-text">{{ currentQuestion.answer }}</div>
          <img v-if="currentQuestion.answerImageUrl" :src="currentQuestion.answerImageUrl" alt="答案图片" class="answer-image" />
        </div>

        <div v-if="currentQuestion.description" class="question-detail-description">
          <h4>解析</h4>
          <p>{{ currentQuestion.description }}</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="showQuestionDetail = false">关闭</el-button>
        <el-button type="primary" @click="startPracticeFromDetail">
          开始练习
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { MagicStick, CopyDocument, ChatDotRound } from '@element-plus/icons-vue';
import request from '@/utils/request';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { fetchQuestionById } from '@/api/question';
import type { Question } from '@/api/question';

const router = useRouter();
const studentStore = useStudentAuthStore();

// 搜索相关
const searchQuery = ref('');
const searching = ref(false);
const showSuggestions = ref(false);
const showResults = ref(false);
const searchResult = ref<any>({});

// 追问相关
const showFollowUp = ref(false);
const followUpQuestion = ref('');

// 题目详情相关
const showQuestionDetail = ref(false);
const currentQuestion = ref<Question | null>(null);
const loadingQuestion = ref(false);

// 意图名称映射
const intentNames: Record<string, string> = {
  FIND_QUESTION: '题目搜索',
  LEARN_CONCEPT: '知识学习',
  ASK_QUESTION: '智能问答',
  FIND_COURSE: '课程查找',
  CHECK_PROGRESS: '学习进度',
  UNKNOWN: '搜索'
};

const intentName = computed(() => {
  return intentNames[searchResult.value.intent] || '搜索';
});

// 搜索建议
const suggestions = ref([
  { icon: '📝', text: '我想练习一元二次方程' },
  { icon: '📚', text: '什么是导数' },
  { icon: '📊', text: '查看我的学习进度' },
  { icon: '🎯', text: '找一些数学选择题' },
  { icon: '💡', text: '如何学好函数' }
]);

/**
 * 执行搜索
 */
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索内容');
    return;
  }

  if (!studentStore.aiKey) {
    ElMessage.warning('请先配置AI Key');
    return;
  }

  searching.value = true;
  showSuggestions.value = false;

  try {
    const res = await request({
      url: '/api/v1/student/nlp-search/query',
      method: 'post',
      data: { query: searchQuery.value },
      headers: {
        'X-Ai-Api-Key': studentStore.aiKey,
        'X-Ai-Provider': studentStore.aiProvider || 'deepseek'
      }
    });

    if (res.code === 200) {
      searchResult.value = res.data;
      showResults.value = true;
    } else {
      ElMessage.error(res.msg || '搜索失败');
    }
  } catch (error: any) {
    console.error('搜索失败:', error);
    ElMessage.error(error.message || '搜索失败，请稍后重试');
  } finally {
    searching.value = false;
  }
};

/**
 * 快速搜索
 */
const quickSearch = (query: string) => {
  searchQuery.value = query;
  handleSearch();
};

/**
 * 复制回答
 */
const copyAnswer = () => {
  if (!searchResult.value.answer) return;

  navigator.clipboard.writeText(searchResult.value.answer).then(() => {
    ElMessage.success('已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败');
  });
};

/**
 * 追问
 */
const askFollowUp = () => {
  followUpQuestion.value = '';
  showFollowUp.value = true;
};

/**
 * 提交追问
 */
const submitFollowUp = () => {
  if (!followUpQuestion.value.trim()) {
    ElMessage.warning('请输入问题');
    return;
  }

  searchQuery.value = followUpQuestion.value;
  showFollowUp.value = false;
  handleSearch();
};

/**
 * 查看题目
 */
const viewQuestion = async (id: number) => {
  loadingQuestion.value = true;
  showQuestionDetail.value = true;
  currentQuestion.value = null;

  try {
    const res = await fetchQuestionById(id);
    if (res.code === 200) {
      // 解析选项（如果是字符串）
      if (res.data.options && typeof res.data.options === 'string') {
        try {
          res.data.options = JSON.parse(res.data.options);
        } catch (e) {
          console.error('解析题目选项失败:', e);
        }
      }
      currentQuestion.value = res.data;
    } else {
      ElMessage.error(res.msg || '获取题目详情失败');
      showQuestionDetail.value = false;
    }
  } catch (error: any) {
    console.error('获取题目详情失败:', error);
    ElMessage.error(error.message || '获取题目详情失败');
    showQuestionDetail.value = false;
  } finally {
    loadingQuestion.value = false;
  }
};

/**
 * 开始练习
 */
const startPractice = (id: number) => {
  router.push({
    name: 'StudentPractice',
    query: {
      questionId: id,
      mode: 'single'
    }
  });
};

/**
 * 从详情对话框开始练习
 */
const startPracticeFromDetail = () => {
  if (currentQuestion.value) {
    showQuestionDetail.value = false;
    startPractice(currentQuestion.value.id);
  }
};

/**
 * 获取题型标签类型
 */
const getQuestionTypeTag = (type: number): string => {
  const tagMap: Record<number, string> = {
    1: 'success',
    2: 'warning',
    3: 'info',
    4: 'primary',
    5: 'danger'
  };
  return tagMap[type] || 'info';
};

/**
 * 获取题型文本
 */
const getQuestionTypeText = (type: number): string => {
  const textMap: Record<number, string> = {
    1: '单选题',
    2: '多选题',
    3: '填空题',
    4: '判断题',
    5: '主观题'
  };
  return textMap[type] || '未知';
};

/**
 * 跳转到课程
 */
const gotoCourse = (id: number) => {
  router.push(`/student/courses?id=${id}`);
};

// 点击外部关闭建议
document.addEventListener('click', (e) => {
  const target = e.target as HTMLElement;
  if (!target.closest('.smart-search-container')) {
    showSuggestions.value = false;
  }
});
</script>

<style scoped>
.smart-search-container {
  position: relative;
  margin-bottom: 20px;
}

.smart-search-input {
  width: 100%;
}

.search-icon {
  color: #409eff;
  font-size: 18px;
}

/* 搜索建议 */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-top: 8px;
  padding: 8px 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.suggestion-title {
  padding: 8px 15px;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.suggestion-item {
  padding: 10px 15px;
  cursor: pointer;
  transition: background 0.3s;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #606266;
}

.suggestion-item:hover {
  background: #f5f7fa;
}

/* AI回答 */
.ai-answer {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  border-left: 4px solid #409eff;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
}

.ai-answer h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.answer-content {
  margin-bottom: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.answer-actions {
  display: flex;
  gap: 10px;
}

/* 搜索结果 */
.search-results {
  margin-bottom: 20px;
}

.search-results h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

.result-item {
  background: #f5f7fa;
  padding: 15px;
  margin-bottom: 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.result-item:hover {
  background: #e6f0ff;
  transform: translateX(4px);
}

.result-header {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.result-content {
  font-size: 14px;
  color: #303133;
  margin: 10px 0;
  line-height: 1.6;
}

.course-result h5 {
  margin: 10px 0;
  color: #303133;
  font-size: 15px;
}

.result-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 题目详情样式 */
.question-detail-header {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.question-detail-content,
.question-detail-options,
.question-detail-answer,
.question-detail-description {
  margin-bottom: 20px;
}

.question-detail-content h4,
.question-detail-options h4,
.question-detail-answer h4,
.question-detail-description h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

.question-detail-content p,
.question-detail-description p {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  margin: 0;
}

.question-image,
.answer-image {
  max-width: 100%;
  margin-top: 10px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.option-item {
  display: flex;
  gap: 8px;
  padding: 10px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  transition: background 0.3s;
}

.option-item:hover {
  background: #e6f0ff;
}

.option-key {
  font-weight: 600;
  color: #409eff;
  min-width: 24px;
}

.option-value {
  flex: 1;
  color: #606266;
  line-height: 1.6;
}

.answer-text {
  padding: 12px;
  background: #f0f9ff;
  border-left: 4px solid #67c23a;
  border-radius: 4px;
  color: #303133;
  font-weight: 500;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .smart-search-input :deep(.el-input__inner) {
    font-size: 14px;
  }

  .search-suggestions {
    font-size: 13px;
  }

  .ai-answer {
    padding: 15px;
  }

  .result-item {
    padding: 12px;
  }

  .result-actions {
    flex-direction: column;
  }

  .result-actions .el-button {
    width: 100%;
  }

  .answer-actions {
    flex-direction: column;
  }

  .answer-actions .el-button {
    width: 100%;
  }

  .question-detail-header {
    flex-wrap: wrap;
  }

  .option-item {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
