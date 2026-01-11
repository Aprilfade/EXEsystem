<template>
  <div class="document-viewer-wrapper">
    <!-- 文档头部 -->
    <div class="document-header">
      <div class="header-left">
        <h3>{{ documentName }}</h3>
        <el-tag :type="documentType === 'PDF' ? 'danger' : 'warning'" size="small">
          {{ documentType }}
        </el-tag>
      </div>
      <div class="header-right">
        <el-button-group>
          <el-button :disabled="currentPage <= 1" @click="prevPage" size="small">
            <el-icon><ArrowLeft /></el-icon>
            上一页
          </el-button>
          <el-button :disabled="currentPage >= totalPages" @click="nextPage" size="small">
            下一页
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-button-group>

        <div class="page-info">
          <el-input-number
            v-model="jumpPage"
            :min="1"
            :max="totalPages"
            size="small"
            style="width: 100px"
            @change="handleJumpPage"
          />
          <span class="page-text">/ {{ totalPages }} 页</span>
        </div>

        <el-button type="primary" size="small" @click="openInNewTab">
          <el-icon><View /></el-icon>
          新窗口打开
        </el-button>
      </div>
    </div>

    <!-- 进度条 -->
    <div class="progress-section">
      <div class="progress-info">
        <span>阅读进度：</span>
        <el-progress
          :percentage="readingProgress"
          :color="readingProgress >= 95 ? '#67C23A' : '#409EFF'"
          style="width: 200px"
        />
        <span class="progress-text">{{ readingProgress }}%</span>
      </div>
      <div class="study-time">
        <el-icon><Clock /></el-icon>
        学习时长：{{ formatTime(studyDuration) }}
      </div>
    </div>

    <!-- 文档内容区域 -->
    <div class="document-content" v-loading="loading">
      <!-- PDF查看器 -->
      <iframe
        v-if="documentType === 'PDF'"
        :src="pdfViewerUrl"
        class="document-iframe"
        frameborder="0"
        @load="handleIframeLoad"
      ></iframe>

      <!-- PPT查看器（使用Office Online Viewer） -->
      <iframe
        v-else-if="documentType === 'PPT'"
        :src="pptViewerUrl"
        class="document-iframe"
        frameborder="0"
        @load="handleIframeLoad"
      ></iframe>

      <!-- 不支持的文件类型 -->
      <el-result
        v-else
        icon="warning"
        title="不支持的文件类型"
        sub-title="该文件类型暂不支持在线预览"
      >
        <template #extra>
          <el-button type="primary" @click="downloadFile">下载文件</el-button>
        </template>
      </el-result>
    </div>

    <!-- 加载失败提示 -->
    <el-alert
      v-if="loadError"
      title="加载失败"
      :description="loadError"
      type="error"
      :closable="false"
      style="margin-top: 10px"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ArrowRight, View, Clock } from '@element-plus/icons-vue';
import { useCourseProgress } from '@/hooks/useCourseProgress';

/**
 * 组件属性
 */
interface Props {
  documentUrl: string;           // 文档URL
  resourceId: number;            // 资源ID
  documentType: 'PDF' | 'PPT';   // 文档类型
  documentName?: string;         // 文档名称
  lastPage?: number;             // 最后阅读页码
  totalPages?: number;           // 总页数（如果已知）
}

const props = withDefaults(defineProps<Props>(), {
  documentName: '文档',
  lastPage: 1,
  totalPages: 1
});

/**
 * 组件事件
 */
const emit = defineEmits<{
  (e: 'progress', page: number, total: number): void;
}>();

// Hooks
const { handleDocumentProgress, formatStudyTime } = useCourseProgress();

// 状态
const loading = ref(true);
const loadError = ref<string | null>(null);
const currentPage = ref(props.lastPage || 1);
const totalPages = ref(props.totalPages || 1);
const jumpPage = ref(currentPage.value);
const studyDuration = ref(0);

// 学习时长计时器
let studyTimer: ReturnType<typeof setInterval> | null = null;

/**
 * PDF查看器URL
 */
const pdfViewerUrl = computed(() => {
  const url = props.documentUrl.startsWith('http')
    ? props.documentUrl
    : `http://localhost:8080${props.documentUrl}`;

  // 使用浏览器内置PDF查看器
  return `${url}#page=${currentPage.value}`;
});

/**
 * PPT查看器URL（使用Office Online Viewer）
 */
const pptViewerUrl = computed(() => {
  const url = props.documentUrl.startsWith('http')
    ? props.documentUrl
    : `http://localhost:8080${props.documentUrl}`;

  // 使用Microsoft Office Online Viewer
  return `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(url)}`;
});

/**
 * 阅读进度
 */
const readingProgress = computed(() => {
  if (totalPages.value === 0) return 0;
  return Math.floor((currentPage.value / totalPages.value) * 100);
});

/**
 * 上一页
 */
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    jumpPage.value = currentPage.value;
    saveProgress();
  }
};

/**
 * 下一页
 */
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    jumpPage.value = currentPage.value;
    saveProgress();
  }
};

/**
 * 跳转页码
 */
const handleJumpPage = (value: number | undefined) => {
  if (value && value >= 1 && value <= totalPages.value) {
    currentPage.value = value;
    saveProgress();
    ElMessage.success(`已跳转到第 ${value} 页`);
  }
};

/**
 * 保存进度
 */
const saveProgress = () => {
  handleDocumentProgress(props.resourceId, currentPage.value, totalPages.value);
  emit('progress', currentPage.value, totalPages.value);
};

/**
 * iframe加载完成
 */
const handleIframeLoad = () => {
  loading.value = false;

  // 尝试获取PDF总页数（仅在浏览器支持的情况下）
  if (props.documentType === 'PDF' && totalPages.value === 1) {
    // 注意：由于跨域限制，通常无法直接访问iframe内容
    // 这里可以通过后端API获取PDF页数
    estimateTotalPages();
  }
};

/**
 * 估算总页数（临时方案）
 */
const estimateTotalPages = () => {
  // 如果没有提供总页数，设置一个默认值
  // 实际应用中应该通过后端API获取准确的页数
  if (totalPages.value === 1) {
    totalPages.value = 10; // 默认假设10页
  }
};

/**
 * 新窗口打开
 */
const openInNewTab = () => {
  const url = props.documentType === 'PDF' ? pdfViewerUrl.value : pptViewerUrl.value;
  window.open(url, '_blank');
};

/**
 * 下载文件
 */
const downloadFile = () => {
  const url = props.documentUrl.startsWith('http')
    ? props.documentUrl
    : `http://localhost:8080${props.documentUrl}`;

  const link = document.createElement('a');
  link.href = url;
  link.download = props.documentName;
  link.click();
};

/**
 * 格式化时间
 */
const formatTime = (seconds: number): string => {
  if (seconds < 60) {
    return `${seconds}秒`;
  } else if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}分${remainingSeconds}秒`;
  } else {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours}小时${minutes}分`;
  }
};

/**
 * 开始学习计时
 */
const startStudyTimer = () => {
  studyTimer = setInterval(() => {
    studyDuration.value++;

    // 每30秒自动保存一次进度
    if (studyDuration.value % 30 === 0) {
      saveProgress();
    }
  }, 1000);
};

/**
 * 停止学习计时
 */
const stopStudyTimer = () => {
  if (studyTimer) {
    clearInterval(studyTimer);
    studyTimer = null;
  }
};

/**
 * 监听文档URL变化
 */
watch(() => props.documentUrl, () => {
  loading.value = true;
  loadError.value = null;
  currentPage.value = props.lastPage || 1;
  jumpPage.value = currentPage.value;
});

/**
 * 监听totalPages属性变化
 */
watch(() => props.totalPages, (newVal) => {
  if (newVal && newVal > 1) {
    totalPages.value = newVal;
  }
});

/**
 * 组件挂载
 */
onMounted(() => {
  // 从上次位置继续阅读
  if (props.lastPage && props.lastPage > 1) {
    currentPage.value = props.lastPage;
    jumpPage.value = currentPage.value;
    ElMessage.success(`从第 ${props.lastPage} 页继续阅读`);
  }

  // 开始学习计时
  startStudyTimer();

  // 添加键盘快捷键
  window.addEventListener('keydown', handleKeyDown);
});

/**
 * 组件卸载
 */
onUnmounted(() => {
  // 停止计时
  stopStudyTimer();

  // 保存最后进度
  saveProgress();

  // 移除键盘监听
  window.removeEventListener('keydown', handleKeyDown);
});

/**
 * 键盘快捷键
 */
const handleKeyDown = (e: KeyboardEvent) => {
  // 左箭头：上一页
  if (e.key === 'ArrowLeft') {
    prevPage();
  }
  // 右箭头：下一页
  else if (e.key === 'ArrowRight') {
    nextPage();
  }
};

/**
 * 暴露给父组件的方法
 */
defineExpose({
  getCurrentPage: () => currentPage.value,
  getTotalPages: () => totalPages.value,
  setTotalPages: (pages: number) => { totalPages.value = pages; },
  jumpToPage: (page: number) => handleJumpPage(page)
});
</script>

<style scoped lang="scss">
.document-viewer-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.document-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  flex-wrap: wrap;
  gap: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.page-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;

  .page-text {
    font-size: 14px;
    white-space: nowrap;
  }
}

.progress-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #606266;

  .progress-text {
    font-weight: 600;
    color: #409EFF;
    margin-left: 10px;
  }
}

.study-time {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #606266;
}

.document-content {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #f5f7fa;
}

.document-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: #fff;
}

// 响应式设计
@media (max-width: 768px) {
  .document-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-left,
  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .progress-section {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }

  .progress-info {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
