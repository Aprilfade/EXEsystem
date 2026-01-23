import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 学习活动类型
 */
export interface LearningActivity {
    id: number;
    studentId: number;
    activityType: 'PRACTICE' | 'EXAM' | 'REVIEW' | 'COURSE' | 'AI_CHAT';
    duration: number; // 学习时长（分钟）
    subjectId?: number;
    relatedId?: number;
    createTime: string;
}

/**
 * 学习时长统计
 */
export interface StudyTimeStats {
    totalMinutes: number; // 总学习时长（分钟）
    todayMinutes: number; // 今日学习时长
    weekMinutes: number; // 本周学习时长
    monthMinutes: number; // 本月学习时长
    avgDailyMinutes: number; // 日均学习时长
}

/**
 * 按日期统计
 */
export interface DailyStudyTime {
    date: string; // 日期 YYYY-MM-DD
    minutes: number; // 学习时长（分钟）
}

/**
 * 按活动类型统计
 */
export interface ActivityTypeStats {
    activityType: string;
    minutes: number;
    percentage: number;
}

/**
 * 按科目统计
 */
export interface SubjectStudyTime {
    subjectId: number;
    subjectName: string;
    minutes: number;
    percentage: number;
}

/**
 * 学习排名
 */
export interface StudyRanking {
    rank: number;
    totalStudents: number;
    percentile: number; // 百分位
}

/**
 * 获取学习时长统计
 */
export function getStudyTimeStats(): Promise<ApiResult<StudyTimeStats>> {
    return request({
        url: '/api/v1/student/learning-activities/stats',
        method: 'get'
    });
}

/**
 * 获取每日学习时长（最近N天）
 * @param days 天数，默认30天
 */
export function getDailyStudyTime(days: number = 30): Promise<ApiResult<DailyStudyTime[]>> {
    return request({
        url: '/api/v1/student/learning-activities/daily',
        method: 'get',
        params: { days }
    });
}

/**
 * 获取按活动类型统计
 * @param days 统计天数，默认30天
 */
export function getActivityTypeStats(days: number = 30): Promise<ApiResult<ActivityTypeStats[]>> {
    return request({
        url: '/api/v1/student/learning-activities/by-type',
        method: 'get',
        params: { days }
    });
}

/**
 * 获取按科目统计
 * @param days 统计天数，默认30天
 */
export function getSubjectStudyTime(days: number = 30): Promise<ApiResult<SubjectStudyTime[]>> {
    return request({
        url: '/api/v1/student/learning-activities/by-subject',
        method: 'get',
        params: { days }
    });
}

/**
 * 获取学习排名
 */
export function getStudyRanking(): Promise<ApiResult<StudyRanking>> {
    return request({
        url: '/api/v1/student/learning-activities/ranking',
        method: 'get'
    });
}

/**
 * 记录学习活动
 */
export function recordLearningActivity(data: {
    activityType: string;
    duration: number;
    subjectId?: number;
    relatedId?: number;
}): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/student/learning-activities',
        method: 'post',
        data
    });
}
