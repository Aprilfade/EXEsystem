import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 学生知识点掌握度DTO
 */
export interface StudentKnowledgeMasteryDTO {
    knowledgePointId: number;
    knowledgePointName: string;
    correctCount: number;
    totalCount: number;
    masteryRate: number;
    lastUpdateTime: string;
    masteryLevel?: string; // 掌握等级：优秀、良好、及格、待提升
}

/**
 * 雷达图数据
 */
export interface MasteryRadarData {
    indicator: Array<{ name: string; max: number }>;
    series: Array<{
        name: string;
        type: string;
        data: number[][];
    }>;
}

/**
 * 获取学生所有知识点的掌握度
 */
export function fetchStudentMastery(studentId: number): Promise<ApiResult<StudentKnowledgeMasteryDTO[]>> {
    return request({
        url: `/api/v1/students/${studentId}/knowledge-mastery`,
        method: 'get'
    });
}

/**
 * 获取学生某科目下的知识点掌握度
 */
export function fetchStudentMasteryBySubject(
    studentId: number,
    subjectId: number
): Promise<ApiResult<StudentKnowledgeMasteryDTO[]>> {
    return request({
        url: `/api/v1/students/${studentId}/knowledge-mastery/subject/${subjectId}`,
        method: 'get'
    });
}

/**
 * 获取学生知识点掌握度雷达图数据
 */
export function fetchStudentMasteryRadar(
    studentId: number,
    subjectId?: number
): Promise<ApiResult<MasteryRadarData>> {
    return request({
        url: `/api/v1/students/${studentId}/mastery-radar`,
        method: 'get',
        params: { subjectId }
    });
}
