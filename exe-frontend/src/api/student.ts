import request from '@/utils/request';
import type { ApiResult } from './user';

export interface Student {
    id: number;
    studentNo: string;
    name: string;
    contact: string;
    subjectId: number;
    grade: string;
    createTime?: string;
}

export interface StudentPageParams {
    current: number;
    size: number;
    subjectId?: number;
    name?: string;
}

export function fetchStudentList(params: StudentPageParams): Promise<ApiResult<Student[]>> {
    return request({
        url: '/api/v1/students',
        method: 'get',
        params
    });
}

export function createStudent(data: Partial<Student>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/students',
        method: 'post',
        data
    });
}

export function updateStudent(id: number, data: Partial<Student>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/students/${id}`,
        method: 'put',
        data
    });
}

export function deleteStudent(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/students/${id}`,
        method: 'delete'
    });
}

/**
 * 上传Excel文件导入学生
 * @param data FormData 对象, 包含 file 和 subjectId
 */
export function importStudents(data: FormData): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/students/import',
        method: 'post',
        data: data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}
/**
 * 【新增】导出学生Excel文件
 * @param params 查询参数
 */
export function exportStudentsExcel(params: { subjectId?: number; name?: string }): Promise<any> {
    return request({
        url: '/api/v1/students/export',
        method: 'get',
        params: params,
        responseType: 'blob' // 关键: 告诉axios期望接收一个二进制对象(文件)
    });
}
/**
 * 【新增】教师给学生增加积分
 * @param id 学生ID
 * @param data 包含积分(points)和备注(remark)
 */
export function addStudentPoints(id: number, data: { points: number; remark?: string }): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/students/${id}/points`,
        method: 'post',
        data
    });
}
// 获取我的成就
export function fetchMyAchievements(): Promise<ApiResult<any[]>> {
    return request({
        url: '/api/v1/student/achievements',
        method: 'get'
    });
}