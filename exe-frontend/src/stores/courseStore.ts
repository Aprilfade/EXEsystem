import { defineStore } from 'pinia';
import type { CourseProgress, ChapterTreeNode, CourseResource } from '@/api/courseProgress';
import {
  getCourseProgress,
  getResourceProgress,
  getCourseCompletion,
  getChapterTree,
  updateProgress as updateProgressApi,
  startSession as startSessionApi,
  endSession as endSessionApi
} from '@/api/courseProgress';

/**
 * 课程详情接口
 */
export interface CourseDetail {
  id: number;
  name: string;
  description?: string;
  coverImage?: string;
  teacherId: number;
  teacherName?: string;
}

/**
 * 进度更新数据
 */
export interface ProgressUpdate {
  percent: number;
  position: string;
  duration: number;
  isCompleted?: boolean;
}

/**
 * 课程学习状态管理
 */
export const useCourseStore = defineStore('course', {
  state: () => ({
    // 课程数据
    currentCourse: null as CourseDetail | null,
    chapters: [] as ChapterTreeNode[],
    resources: [] as CourseResource[],

    // 学习进度
    progressMap: new Map<number, CourseProgress>() as Map<number, CourseProgress>,
    currentResource: null as CourseResource | null,

    // 学习会话
    currentSessionId: null as number | null,
    sessionStart: null as Date | null,
    sessionDuration: 0,

    // 学习统计
    totalStudyTime: 0,
    completedResourceCount: 0,
    courseCompletionRate: 0,

    // 加载状态
    loading: false,
    error: null as string | null
  }),

  getters: {
    /**
     * 获取当前课程ID
     */
    currentCourseId: (state) => state.currentCourse?.id || null,

    /**
     * 获取资源进度
     */
    getResourceProgress: (state) => (resourceId: number): CourseProgress | null => {
      return state.progressMap.get(resourceId) || null;
    },

    /**
     * 检查资源是否已完成
     */
    isResourceCompleted: (state) => (resourceId: number): boolean => {
      const progress = state.progressMap.get(resourceId);
      return progress ? progress.isCompleted === 1 : false;
    },

    /**
     * 获取资源完成百分比
     */
    getResourcePercent: (state) => (resourceId: number): number => {
      const progress = state.progressMap.get(resourceId);
      return progress ? progress.progressPercent : 0;
    },

    /**
     * 获取资源最后学习位置
     */
    getResourceLastPosition: (state) => (resourceId: number): string => {
      const progress = state.progressMap.get(resourceId);
      return progress ? progress.lastPosition || '0' : '0';
    },

    /**
     * 计算课程总进度
     */
    calculateCourseProgress(): number {
      if (this.resources.length === 0) return 0;

      const completedCount = this.resources.filter(r =>
        this.isResourceCompleted(r.id)
      ).length;

      return Math.floor((completedCount / this.resources.length) * 100);
    },

    /**
     * 获取当前会话时长（秒）
     */
    getCurrentSessionDuration: (state): number => {
      if (!state.sessionStart) return 0;

      const now = new Date();
      const duration = Math.floor((now.getTime() - state.sessionStart.getTime()) / 1000);
      return duration;
    },

    /**
     * 检查是否有活动会话
     */
    hasActiveSession: (state): boolean => {
      return state.currentSessionId !== null && state.sessionStart !== null;
    }
  },

  actions: {
    /**
     * 加载课程详情
     */
    async loadCourseDetail(courseId: number, courseData?: CourseDetail) {
      this.loading = true;
      this.error = null;

      try {
        // 设置课程基础信息
        if (courseData) {
          this.currentCourse = courseData;
        } else {
          this.currentCourse = { id: courseId, name: '课程' };
        }

        // 加载章节树
        const chapterResponse = await getChapterTree(courseId);
        if (chapterResponse.code === 200) {
          this.chapters = chapterResponse.data;

          // 提取所有资源
          this.resources = this.extractResources(this.chapters);
        }

        // 加载学习进度（如果失败不影响课程查看）
        try {
          const progressResponse = await getCourseProgress(courseId);
          if (progressResponse.code === 200) {
            this.progressMap.clear();
            progressResponse.data.forEach(progress => {
              this.progressMap.set(progress.resourceId, progress);
            });

            // 统计完成数量和学习时长
            this.completedResourceCount = progressResponse.data.filter(p => p.isCompleted === 1).length;
            this.totalStudyTime = progressResponse.data.reduce((sum, p) => sum + (p.studyDuration || 0), 0);
          }
        } catch (progressError: any) {
          console.warn('加载学习进度失败，将以访客模式浏览:', progressError.message);
          // 进度加载失败不影响课程查看，清空进度数据即可
          this.progressMap.clear();
          this.completedResourceCount = 0;
          this.totalStudyTime = 0;
        }

        // 加载完成率（如果失败不影响课程查看）
        try {
          const completionResponse = await getCourseCompletion(courseId);
          if (completionResponse.code === 200) {
            this.courseCompletionRate = completionResponse.data;
          }
        } catch (completionError: any) {
          console.warn('加载课程完成率失败:', completionError.message);
          this.courseCompletionRate = 0;
        }

      } catch (error: any) {
        console.error('加载课程详情失败:', error);
        this.error = error.message || '加载失败';
      } finally {
        this.loading = false;
      }
    },

    /**
     * 递归提取所有资源
     */
    extractResources(nodes: ChapterTreeNode[]): CourseResource[] {
      let resources: CourseResource[] = [];

      nodes.forEach(node => {
        // 添加当前节点的资源
        if (node.resources && node.resources.length > 0) {
          resources = resources.concat(node.resources);
        }

        // 递归处理子节点
        if (node.children && node.children.length > 0) {
          resources = resources.concat(this.extractResources(node.children));
        }
      });

      return resources;
    },

    /**
     * 更新学习进度
     */
    async updateProgress(resourceId: number, data: ProgressUpdate) {
      try {
        const response = await updateProgressApi({
          resourceId,
          progressPercent: data.percent,
          lastPosition: data.position,
          studyDuration: data.duration
        });

        if (response.code === 200) {
          // 更新本地进度
          const existingProgress = this.progressMap.get(resourceId);

          const updatedProgress: CourseProgress = {
            id: existingProgress?.id || 0,
            studentId: existingProgress?.studentId || 0,
            courseId: this.currentCourseId!,
            resourceId,
            resourceType: this.resources.find(r => r.id === resourceId)?.resourceType || 'VIDEO',
            progressPercent: data.percent,
            lastPosition: data.position,
            studyDuration: (existingProgress?.studyDuration || 0) + data.duration,
            isCompleted: data.percent >= 95 ? 1 : 0,
            completedTime: data.percent >= 95 ? new Date().toISOString() : null,
            createdTime: existingProgress?.createdTime || new Date().toISOString(),
            updatedTime: new Date().toISOString()
          };

          this.progressMap.set(resourceId, updatedProgress);

          // 如果资源完成，更新完成数量
          if (data.percent >= 95 && (!existingProgress || existingProgress.isCompleted === 0)) {
            this.completedResourceCount++;
          }

          // 更新总学习时长
          this.totalStudyTime += data.duration;

          // 重新计算课程完成率
          this.courseCompletionRate = this.calculateCourseProgress;
        }

        return response;
      } catch (error: any) {
        console.warn('更新进度失败（访客模式无法保存进度）:', error.message);
        // 不再抛出错误，允许用户继续学习
        return null;
      }
    },

    /**
     * 开始学习会话
     */
    async startSession(resourceId: number) {
      if (!this.currentCourse) {
        console.error('未加载课程信息');
        return;
      }

      try {
        const response = await startSessionApi({
          courseId: this.currentCourse.id,
          resourceId
        });

        if (response.code === 200) {
          this.currentSessionId = response.data;
          this.sessionStart = new Date();
          this.sessionDuration = 0;

          // 设置当前资源
          this.currentResource = this.resources.find(r => r.id === resourceId) || null;

          console.log('学习会话已开始:', this.currentSessionId);
        }

        return response;
      } catch (error: any) {
        console.warn('开始会话失败（访客模式）:', error.message);
        // 不抛出错误，允许用户继续学习
        return null;
      }
    },

    /**
     * 结束学习会话
     */
    async endSession() {
      if (!this.currentSessionId) {
        console.warn('没有活动的学习会话');
        return;
      }

      try {
        const response = await endSessionApi(this.currentSessionId);

        if (response.code === 200) {
          // 计算会话时长
          if (this.sessionStart) {
            this.sessionDuration = this.getCurrentSessionDuration;
          }

          console.log('学习会话已结束，时长:', this.sessionDuration, '秒');

          // 清除会话状态
          this.currentSessionId = null;
          this.sessionStart = null;
          this.currentResource = null;
        }

        return response;
      } catch (error: any) {
        console.warn('结束会话失败（访客模式）:', error.message);
        // 清除会话状态，即使失败也允许继续
        this.currentSessionId = null;
        this.sessionStart = null;
        this.currentResource = null;
        return null;
      }
    },

    /**
     * 重新加载资源进度
     */
    async reloadResourceProgress(resourceId: number) {
      try {
        const response = await getResourceProgress(resourceId);
        if (response.code === 200 && response.data) {
          this.progressMap.set(resourceId, response.data);
        }
      } catch (error) {
        console.error('重新加载资源进度失败:', error);
      }
    },

    /**
     * 清除课程数据
     */
    clearCourseData() {
      this.currentCourse = null;
      this.chapters = [];
      this.resources = [];
      this.progressMap.clear();
      this.currentResource = null;
      this.currentSessionId = null;
      this.sessionStart = null;
      this.sessionDuration = 0;
      this.totalStudyTime = 0;
      this.completedResourceCount = 0;
      this.courseCompletionRate = 0;
      this.loading = false;
      this.error = null;
    }
  }
});
