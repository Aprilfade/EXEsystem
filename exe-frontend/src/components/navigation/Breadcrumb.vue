<template>
  <div v-if="breadcrumbs.length > 0" class="breadcrumb-wrapper">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/student/dashboard' }">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </el-breadcrumb-item>
      <el-breadcrumb-item
        v-for="(crumb, index) in breadcrumbs"
        :key="index"
        :to="crumb.path ? { path: crumb.path } : undefined"
      >
        <el-icon v-if="crumb.icon">
          <component :is="crumb.icon" />
        </el-icon>
        <span>{{ crumb.title }}</span>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import {
  House,
  VideoPlay,
  UserFilled,
  DocumentCopy,
  Tickets,
  Lightning,
  Document,
  Star,
  ChatDotRound,
  Reading,
  Edit,
  TrendCharts,
  Histogram,
  User,
  Trophy,
  Clock,
  Message,
  Setting,
  DataAnalysis
} from '@element-plus/icons-vue';

interface Breadcrumb {
  title: string;
  path?: string;
  icon?: any;
}

const route = useRoute();

// Route configuration mapping
const routeConfig: Record<string, Breadcrumb[]> = {
  '/student/dashboard': [],
  '/student/courses': [{ title: '课程中心', icon: VideoPlay }],
  '/student/classes': [{ title: '我的班级', icon: UserFilled }],
  '/student/wrong-records': [{ title: '我的错题本', icon: DocumentCopy }],
  '/student/practice': [{ title: '在线练习', icon: Tickets }],
  '/student/battle': [{ title: '知识对战', icon: Lightning }],
  '/student/exams': [{ title: '模拟考试', icon: Document }],
  '/student/favorites': [{ title: '我的收藏', icon: Star }],
  '/student/ai-chat': [
    { title: 'AI助手', path: '/student/ai-chat' },
    { title: 'AI对话助手', icon: ChatDotRound }
  ],
  '/student/ai-tutor': [
    { title: 'AI助手', path: '/student/ai-chat' },
    { title: 'AI家教', icon: Reading }
  ],
  '/student/ai-practice': [
    { title: 'AI助手', path: '/student/ai-chat' },
    { title: 'AI练习生成', icon: Edit }
  ],
  '/student/learning-analysis': [
    { title: 'AI助手', path: '/student/ai-chat' },
    { title: '学习分析', icon: TrendCharts }
  ],
  '/student/knowledge-graph': [
    { title: 'AI助手', path: '/student/ai-chat' },
    { title: '知识图谱', icon: Histogram }
  ],
  '/student/profile': [{ title: '个人资料', icon: User }],
  '/student/achievements': [{ title: '我的成就', icon: Trophy }],
  '/student/study-time': [{ title: '学习时长', icon: Clock }],
  '/student/notifications': [{ title: '系统通知', icon: Message }],
  '/student/settings': [{ title: '系统设置', icon: Setting }],

  // Exam result detail - dynamic route
  '/student/exam-results': [
    { title: '模拟考试', path: '/student/exams', icon: Document },
    { title: '考试结果', icon: DataAnalysis }
  ]
};

const breadcrumbs = computed<Breadcrumb[]>(() => {
  const path = route.path;

  // Check for exact match first
  if (routeConfig[path]) {
    return routeConfig[path];
  }

  // Handle dynamic routes
  if (path.startsWith('/student/exam-results/')) {
    return [
      { title: '模拟考试', path: '/student/exams', icon: Document },
      { title: '考试详情', icon: DataAnalysis }
    ];
  }

  if (path.startsWith('/student/courses/')) {
    return [
      { title: '课程中心', path: '/student/courses', icon: VideoPlay },
      { title: '课程详情' }
    ];
  }

  if (path.startsWith('/student/classes/')) {
    return [
      { title: '我的班级', path: '/student/classes', icon: UserFilled },
      { title: '班级详情' }
    ];
  }

  // Default: parse from path
  const segments = path.split('/').filter(Boolean);
  if (segments.length <= 2) {
    return [];
  }

  return segments.slice(2).map((segment, index) => {
    const isLast = index === segments.length - 3;
    return {
      title: formatSegment(segment),
      path: isLast ? undefined : '/' + segments.slice(0, index + 3).join('/')
    };
  });
});

const formatSegment = (segment: string): string => {
  const titleMap: Record<string, string> = {
    'courses': '课程',
    'classes': '班级',
    'practice': '练习',
    'exams': '考试',
    'battle': '对战',
    'wrong-records': '错题本',
    'favorites': '收藏',
    'ai-chat': 'AI对话',
    'ai-tutor': 'AI家教',
    'ai-practice': 'AI练习',
    'learning-analysis': '学习分析',
    'knowledge-graph': '知识图谱',
    'profile': '个人资料',
    'achievements': '成就',
    'study-time': '学习时长',
    'notifications': '通知',
    'settings': '设置'
  };

  return titleMap[segment] || segment;
};
</script>

<style scoped>
.breadcrumb-wrapper {
  padding: 12px 0;
  margin-bottom: 16px;
}

.breadcrumb-wrapper :deep(.el-breadcrumb__item) {
  display: inline-flex;
  align-items: center;
}

.breadcrumb-wrapper :deep(.el-breadcrumb__inner) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--el-text-color-regular);
  transition: color 0.3s;
}

.breadcrumb-wrapper :deep(.el-breadcrumb__inner):hover {
  color: var(--el-color-primary);
}

.breadcrumb-wrapper :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.breadcrumb-wrapper :deep(.el-icon) {
  font-size: 16px;
}

/* Mobile optimization */
@media (max-width: 768px) {
  .breadcrumb-wrapper {
    padding: 8px 0;
    margin-bottom: 12px;
    overflow-x: auto;
    white-space: nowrap;
  }

  .breadcrumb-wrapper :deep(.el-breadcrumb__inner) {
    font-size: 13px;
  }

  .breadcrumb-wrapper :deep(.el-icon) {
    font-size: 14px;
  }
}
</style>
