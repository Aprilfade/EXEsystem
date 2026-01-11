<!-- RecommendationPanel.vue - AI智能推荐面板 -->
<template>
  <el-card class="recommendation-panel">
    <template #header>
      <div class="header-flex">
        <span>🎯 AI智能推荐</span>
        <el-button text @click="refreshRecommendations">
          <el-icon><Refresh /></el-icon>
          换一批
        </el-button>
      </div>
    </template>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 题目推荐 -->
      <el-tab-pane label="题目推荐" name="questions">
        <div v-loading="loading">
          <div
            v-for="(rec, index) in questionRecommendations"
            :key="rec.questionId"
            class="recommendation-item"
          >
            <div class="item-header">
              <el-tag type="info" size="small">推荐 #{{ index + 1 }}</el-tag>
              <el-tag type="success" size="small">
                匹配度: {{ (rec.score * 10).toFixed(0) }}%
              </el-tag>
            </div>

            <div class="item-content">
              <p class="question-text">{{ stripHtml(rec.content) }}</p>
              <p class="reason">
                <el-icon><InfoFilled /></el-icon>
                {{ rec.reason }}
              </p>
            </div>

            <div class="item-actions">
              <el-button type="primary" size="small" @click="startPractice(rec.questionId)">
                立即练习
              </el-button>
            </div>
          </div>

          <el-empty
            v-if="questionRecommendations.length === 0 && !loading"
            description="暂无推荐题目"
          />
        </div>
      </el-tab-pane>

      <!-- 课程推荐 -->
      <el-tab-pane label="课程推荐" name="courses">
        <div v-loading="loading">
          <div
            v-for="rec in courseRecommendations"
            :key="rec.courseId"
            class="course-card"
          >
            <div class="course-info">
              <h4>{{ rec.name }}</h4>
              <p class="course-description">{{ rec.description }}</p>
              <p class="course-reason">
                <el-icon><InfoFilled /></el-icon>
                {{ rec.reason }}
              </p>
            </div>
            <div class="course-actions">
              <el-button type="primary" @click="goToCourse(rec.courseId)">
                开始学习
              </el-button>
            </div>
          </div>

          <el-empty
            v-if="courseRecommendations.length === 0 && !loading"
            description="暂无推荐课程"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Refresh, InfoFilled } from '@element-plus/icons-vue';
import request from '@/utils/request';

interface QuestionRecommendation {
  questionId: number;
  content: string;
  score: number;
  reason: string;
  questionType: number;
  subjectName?: string;
}

interface CourseRecommendation {
  courseId: number;
  name: string;
  description: string;
  score: number;
  reason: string;
  coverUrl?: string;
}

const router = useRouter();

const activeTab = ref('questions');
const loading = ref(false);
const questionRecommendations = ref<QuestionRecommendation[]>([]);
const courseRecommendations = ref<CourseRecommendation[]>([]);

/**
 * 获取题目推荐
 */
const fetchQuestionRecommendations = async () => {
  loading.value = true;
  try {
    const res = await request({
      url: '/api/v1/student/recommendation/questions',
      method: 'get',
      params: {
        limit: 10
      }
    });

    if (res.code === 200) {
      questionRecommendations.value = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取推荐失败');
    }
  } catch (error) {
    console.error('获取题目推荐失败:', error);
    ElMessage.error('获取推荐失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

/**
 * 获取课程推荐
 */
const fetchCourseRecommendations = async () => {
  loading.value = true;
  try {
    const res = await request({
      url: '/api/v1/student/recommendation/courses',
      method: 'get',
      params: {
        limit: 5
      }
    });

    if (res.code === 200) {
      courseRecommendations.value = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取课程推荐失败');
    }
  } catch (error) {
    console.error('获取课程推荐失败:', error);
    ElMessage.error('获取推荐失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

/**
 * 刷新推荐
 */
const refreshRecommendations = () => {
  if (activeTab.value === 'questions') {
    fetchQuestionRecommendations();
  } else {
    fetchCourseRecommendations();
  }
};

/**
 * 标签页切换
 */
const handleTabChange = (tabName: string) => {
  if (tabName === 'questions' && questionRecommendations.value.length === 0) {
    fetchQuestionRecommendations();
  } else if (tabName === 'courses' && courseRecommendations.value.length === 0) {
    fetchCourseRecommendations();
  }
};

/**
 * 去除HTML标签
 */
const stripHtml = (html: string) => {
  if (!html) return '';
  const tmp = document.createElement('DIV');
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || '';
};

/**
 * 开始练习
 */
const startPractice = (questionId: number) => {
  console.log('开始练习题目:', questionId);

  // 跳转到练习页面
  router.push({
    name: 'StudentPractice',
    query: {
      questionId: questionId.toString(),
      from: 'ai-recommendation'
    }
  });

  ElMessage.success('正在跳转到题目练习...');
};

/**
 * 跳转到课程
 */
const goToCourse = (courseId: number) => {
  router.push(`/student/courses?id=${courseId}`);
};

onMounted(() => {
  fetchQuestionRecommendations();
});
</script>

<style scoped>
.recommendation-panel {
  margin-bottom: 20px;
}

.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recommendation-item {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.recommendation-item:hover {
  background: #e6f0ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.item-header {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.item-content {
  margin-bottom: 15px;
}

.question-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.6;
}

.reason {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

.item-actions {
  display: flex;
  gap: 10px;
}

.course-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.course-card:hover {
  background: #e6f0ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.course-info {
  flex: 1;
}

.course-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.course-description {
  font-size: 13px;
  color: #606266;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.course-reason {
  font-size: 12px;
  color: #909399;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.course-actions {
  margin-left: 15px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .recommendation-item {
    padding: 12px;
  }

  .item-actions {
    flex-direction: column;
  }

  .item-actions .el-button {
    width: 100%;
  }

  .course-card {
    flex-direction: column;
  }

  .course-actions {
    margin-left: 0;
    margin-top: 12px;
    width: 100%;
  }

  .course-actions .el-button {
    width: 100%;
  }
}
</style>
