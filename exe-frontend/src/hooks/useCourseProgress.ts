import { ref, computed } from 'vue';
import { useCourseStore } from '@/stores/courseStore';
import type { ProgressUpdate } from '@/stores/courseStore';

/**
 * 防抖函数
 * @param func 要防抖的函数
 * @param wait 等待时间（毫秒）
 */
function debounce<T extends (...args: any[]) => any>(func: T, wait: number): T {
  let timeout: ReturnType<typeof setTimeout> | null = null;

  return ((...args: Parameters<T>) => {
    if (timeout) {
      clearTimeout(timeout);
    }

    timeout = setTimeout(() => {
      func(...args);
      timeout = null;
    }, wait);
  }) as T;
}

/**
 * 课程学习进度管理 Hook
 */
export function useCourseProgress() {
  const store = useCourseStore();
  const isSaving = ref(false);
  const lastSaveTime = ref<{ [key: number]: number }>({});

  /**
   * 自动保存进度（防抖3秒）
   */
  const saveProgress = debounce(async (resourceId: number, data: ProgressUpdate) => {
    if (isSaving.value) {
      return;
    }

    isSaving.value = true;

    try {
      await store.updateProgress(resourceId, data);
      lastSaveTime.value[resourceId] = Date.now();
      console.log(`进度已保存 [资源${resourceId}]: ${data.percent}%`);
    } catch (error) {
      console.error('保存进度失败:', error);
    } finally {
      isSaving.value = false;
    }
  }, 3000);

  /**
   * 视频进度自动保存
   * @param resourceId 资源ID
   * @param currentTime 当前播放时间（秒）
   * @param duration 视频总时长（秒）
   */
  const handleVideoProgress = (resourceId: number, currentTime: number, duration: number) => {
    if (duration === 0) return;

    // 计算完成百分比
    const percent = Math.min(Math.floor((currentTime / duration) * 100), 100);
    const isCompleted = percent >= 95;

    // 计算本次学习时长（避免重复计算）
    const lastTime = lastSaveTime.value[resourceId] || 0;
    const now = Date.now();
    const studyDuration = lastTime ? Math.floor((now - lastTime) / 1000) : 0;

    saveProgress(resourceId, {
      percent,
      position: currentTime.toString(),
      duration: Math.max(studyDuration, 0),
      isCompleted
    });
  };

  /**
   * PDF/PPT进度自动保存
   * @param resourceId 资源ID
   * @param currentPage 当前页码
   * @param totalPages 总页数
   */
  const handleDocumentProgress = (resourceId: number, currentPage: number, totalPages: number) => {
    if (totalPages === 0) return;

    // 计算完成百分比
    const percent = Math.min(Math.floor((currentPage / totalPages) * 100), 100);
    const isCompleted = currentPage === totalPages;

    // 计算本次学习时长
    const lastTime = lastSaveTime.value[resourceId] || 0;
    const now = Date.now();
    const studyDuration = lastTime ? Math.floor((now - lastTime) / 1000) : 0;

    saveProgress(resourceId, {
      percent,
      position: currentPage.toString(),
      duration: Math.max(studyDuration, 0),
      isCompleted
    });
  };

  /**
   * 手动保存进度（立即保存，不防抖）
   */
  const saveProgressImmediately = async (resourceId: number, data: ProgressUpdate) => {
    if (isSaving.value) {
      return;
    }

    isSaving.value = true;

    try {
      await store.updateProgress(resourceId, data);
      lastSaveTime.value[resourceId] = Date.now();
      console.log(`进度已立即保存 [资源${resourceId}]: ${data.percent}%`);
      return true;
    } catch (error) {
      console.error('保存进度失败:', error);
      return false;
    } finally {
      isSaving.value = false;
    }
  };

  /**
   * 标记资源为完成
   */
  const markAsCompleted = async (resourceId: number) => {
    const progress = store.getResourceProgress(resourceId);
    const currentPosition = progress?.lastPosition || '0';
    const currentDuration = progress?.studyDuration || 0;

    await saveProgressImmediately(resourceId, {
      percent: 100,
      position: currentPosition,
      duration: 0,
      isCompleted: true
    });
  };

  /**
   * 获取资源进度
   */
  const getResourceProgress = computed(() => (resourceId: number) => {
    return store.getResourceProgress(resourceId);
  });

  /**
   * 获取资源完成百分比
   */
  const getResourcePercent = computed(() => (resourceId: number) => {
    return store.getResourcePercent(resourceId);
  });

  /**
   * 检查资源是否已完成
   */
  const isResourceCompleted = computed(() => (resourceId: number) => {
    return store.isResourceCompleted(resourceId);
  });

  /**
   * 获取资源最后学习位置
   */
  const getResourceLastPosition = computed(() => (resourceId: number) => {
    return store.getResourceLastPosition(resourceId);
  });

  /**
   * 计算课程总进度
   */
  const calculateCourseProgress = computed(() => {
    return store.calculateCourseProgress;
  });

  /**
   * 获取已完成资源数量
   */
  const completedResourceCount = computed(() => {
    return store.completedResourceCount;
  });

  /**
   * 获取总资源数量
   */
  const totalResourceCount = computed(() => {
    return store.resources.length;
  });

  /**
   * 获取总学习时长（秒）
   */
  const totalStudyTime = computed(() => {
    return store.totalStudyTime;
  });

  /**
   * 格式化学习时长为可读字符串
   * @param seconds 秒数
   */
  const formatStudyTime = (seconds: number): string => {
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

  return {
    // 保存相关
    saveProgress,
    saveProgressImmediately,
    isSaving,

    // 进度处理
    handleVideoProgress,
    handleDocumentProgress,
    markAsCompleted,

    // 进度获取
    getResourceProgress,
    getResourcePercent,
    isResourceCompleted,
    getResourceLastPosition,
    calculateCourseProgress,

    // 统计数据
    completedResourceCount,
    totalResourceCount,
    totalStudyTime,
    formatStudyTime
  };
}
