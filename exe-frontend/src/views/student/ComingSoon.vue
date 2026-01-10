<template>
  <div class="coming-soon-container">
    <el-card class="coming-soon-card" shadow="hover">
      <div class="coming-soon-content">
        <!-- 图标 -->
        <div class="icon-wrapper">
          <el-icon :size="120" :color="iconColor">
            <component :is="icon" />
          </el-icon>
        </div>

        <!-- 标题 -->
        <h1 class="title">{{ title }}</h1>

        <!-- 描述 -->
        <p class="description">{{ description }}</p>

        <!-- 功能预告 -->
        <div class="features" v-if="features && features.length > 0">
          <h3>即将推出的功能</h3>
          <ul>
            <li v-for="(feature, index) in features" :key="index">
              <el-icon><Check /></el-icon>
              <span>{{ feature }}</span>
            </li>
          </ul>
        </div>

        <!-- 操作按钮 -->
        <div class="actions">
          <el-button type="primary" size="large" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
          <el-button size="large" @click="goToDashboard">
            <el-icon><House /></el-icon>
            前往控制台
          </el-button>
        </div>

        <!-- 进度提示 -->
        <div class="progress-hint">
          <el-progress :percentage="70" :stroke-width="8" status="success" />
          <p class="hint-text">功能开发进度约70%，敬请期待！</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  ArrowLeft, House, Check,
  ChatDotRound, Reading, Edit, TrendCharts, Histogram
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

// 根据路由路径确定页面信息
const pageConfig = computed(() => {
  const path = route.path;

  const configs: Record<string, any> = {
    '/student/ai-chat': {
      title: 'AI对话助手',
      description: '智能AI助手，随时随地为你解答学习问题',
      icon: ChatDotRound,
      iconColor: '#409EFF',
      features: [
        '多轮对话，上下文理解',
        '学科专业知识问答',
        '学习计划建议',
        '会话历史记录'
      ]
    },
    '/student/ai-tutor': {
      title: 'AI家教',
      description: '个性化的AI家教服务，提供一对一学习辅导',
      icon: Reading,
      iconColor: '#67C23A',
      features: [
        '个性化学习路径规划',
        '知识点详细讲解',
        '互动式教学体验',
        '学习进度跟踪'
      ]
    },
    '/student/ai-practice': {
      title: 'AI练习生成',
      description: '智能生成个性化练习题，针对性提升学习效果',
      icon: Edit,
      iconColor: '#E6A23C',
      features: [
        '根据薄弱点生成题目',
        '难度自适应调整',
        '多种题型支持',
        '即时反馈与解析'
      ]
    },
    '/student/learning-analysis': {
      title: '学习数据分析',
      description: 'AI驱动的学习数据分析，洞察学习规律',
      icon: TrendCharts,
      iconColor: '#F56C6C',
      features: [
        '学习行为分析',
        '成绩趋势预测',
        '薄弱知识点识别',
        '个性化学习建议'
      ]
    },
    '/student/knowledge-graph': {
      title: '知识图谱',
      description: '可视化知识关联，系统化掌握学科知识',
      icon: Histogram,
      iconColor: '#909399',
      features: [
        '知识点关联可视化',
        '学习路径推荐',
        '知识盲区识别',
        '交互式探索学习'
      ]
    }
  };

  return configs[path] || {
    title: '功能开发中',
    description: '该功能正在紧张开发中，敬请期待',
    icon: House,
    iconColor: '#409EFF',
    features: []
  };
});

const title = computed(() => pageConfig.value.title);
const description = computed(() => pageConfig.value.description);
const icon = computed(() => pageConfig.value.icon);
const iconColor = computed(() => pageConfig.value.iconColor);
const features = computed(() => pageConfig.value.features);

const goBack = () => {
  router.back();
};

const goToDashboard = () => {
  router.push('/student/dashboard');
};
</script>

<style scoped>
.coming-soon-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.coming-soon-card {
  max-width: 800px;
  width: 100%;
  border-radius: 20px;
  overflow: hidden;
}

.coming-soon-card :deep(.el-card__body) {
  padding: 60px 40px;
}

.coming-soon-content {
  text-align: center;
}

.icon-wrapper {
  margin-bottom: 30px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

.title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.description {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 40px;
  line-height: 1.6;
}

.features {
  margin-bottom: 40px;
  text-align: left;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.features h3 {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 20px;
  text-align: center;
}

.features ul {
  list-style: none;
  padding: 0;
}

.features li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
  font-size: 16px;
  color: var(--text-secondary);
}

.features li:last-child {
  border-bottom: none;
}

.features li .el-icon {
  color: #67C23A;
  font-size: 20px;
  flex-shrink: 0;
}

.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.progress-hint {
  max-width: 500px;
  margin: 0 auto;
}

.hint-text {
  margin-top: 12px;
  font-size: 14px;
  color: var(--text-secondary);
}

/* 响应式 */
@media (max-width: 768px) {
  .coming-soon-card :deep(.el-card__body) {
    padding: 40px 20px;
  }

  .title {
    font-size: 28px;
  }

  .description {
    font-size: 16px;
  }

  .icon-wrapper .el-icon {
    font-size: 80px !important;
  }

  .actions {
    flex-direction: column;
  }

  .actions .el-button {
    width: 100%;
  }
}
</style>
