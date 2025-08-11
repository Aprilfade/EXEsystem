import request from '@/utils/request';
import type { ApiResult } from './user';

// --- DTO 接口定义 ---

export interface KnowledgePointErrorStats {
    knowledgePointId: number;
    knowledgePointName: string;
    subjectName: string;
    totalQuestions: number;
    totalErrors: number;
}

export interface StudentAbility {
    radarLabels: string[];
    errorRates: number[];
}

export interface PaperDifficulty {
    paperId: number;
    paperName: string;
    totalStudents: number;
    averageErrorRate: number;
    easiestQuestionContent: string;
    hardestQuestionContent: string;
}


// --- API 函数 ---

export function fetchKnowledgePointErrorStats(): Promise<ApiResult<KnowledgePointErrorStats[]>> {
    return request({
        url: '/api/v1/statistics/knowledge-point-errors',
        method: 'get'
    });
}

export function fetchStudentAbilityStats(studentId: number): Promise<ApiResult<StudentAbility>> {
    return request({
        url: `/api/v1/statistics/student-ability/${studentId}`,
        method: 'get'
    });
}

export function fetchPaperDifficultyStats(paperId: number): Promise<ApiResult<PaperDifficulty>> {
    return request({
        url: `/api/v1/statistics/paper-difficulty/${paperId}`,
        method: 'get'
    });
}