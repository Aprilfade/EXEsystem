import request from '@/utils/request';
import type { ApiResult } from './user';

/**
 * 批阅审批接口
 * @since v3.07
 */

export interface GradingApproval {
    id: number;
    examResultId: number;
    studentId: number;
    studentName: string;
    paperId: number;
    paperTitle: string;
    oldScore: number | null;
    newScore: number;
    scoreChange: number;
    changePercentage: number;
    oldComment: string | null;
    newComment: string | null;
    graderId: number;
    graderName: string;
    reason: string;
    status: 'PENDING' | 'APPROVED' | 'REJECTED';
    approverId: number | null;
    approverName: string | null;
    approvalComment: string | null;
    submitTime: string;
    approvalTime: string | null;
    createTime: string;
    updateTime: string;
}

export interface ApprovalQueryParams {
    current?: number;
    size?: number;
    status?: string;
    studentId?: number;
    studentName?: string;
    paperId?: number;
    startDate?: string;
    endDate?: string;
}

export interface ApprovalStatistics {
    PENDING: number;
    APPROVED: number;
    REJECTED: number;
    TOTAL: number;
}

/**
 * 分页查询审批记录
 */
export function fetchApprovalList(params: ApprovalQueryParams): Promise<ApiResult<GradingApproval[]>> {
    return request({
        url: '/api/v1/grading-approvals',
        method: 'get',
        params
    });
}

/**
 * 查询我的审批记录（我提交的）
 */
export function fetchMySubmissions(params: ApprovalQueryParams): Promise<ApiResult<GradingApproval[]>> {
    return request({
        url: '/api/v1/grading-approvals/my-submissions',
        method: 'get',
        params
    });
}

/**
 * 获取审批详情
 */
export function fetchApprovalDetail(id: number): Promise<ApiResult<GradingApproval>> {
    return request({
        url: `/api/v1/grading-approvals/${id}`,
        method: 'get'
    });
}

/**
 * 审批通过
 */
export function approveGrading(id: number, approvalComment: string): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/grading-approvals/${id}/approve`,
        method: 'put',
        data: { approvalComment }
    });
}

/**
 * 审批驳回
 */
export function rejectGrading(id: number, approvalComment: string): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/grading-approvals/${id}/reject`,
        method: 'put',
        data: { approvalComment }
    });
}

/**
 * 撤销审批（仅限申请人撤销待审批状态的记录）
 */
export function cancelApproval(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/grading-approvals/${id}`,
        method: 'delete'
    });
}

/**
 * 获取待审批数量
 */
export function getPendingApprovalCount(): Promise<ApiResult<{ count: number }>> {
    return request({
        url: '/api/v1/grading-approvals/pending-count',
        method: 'get'
    });
}

/**
 * 获取审批统计
 */
export function getApprovalStatistics(): Promise<ApiResult<ApprovalStatistics>> {
    return request({
        url: '/api/v1/grading-approvals/statistics',
        method: 'get'
    });
}
