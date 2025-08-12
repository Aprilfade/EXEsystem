// src/api/role.ts

import request from '@/utils/request';
import type { ApiResult } from './user';
import type { Role } from './user';
import type { Permission } from './auth'; // 导入Permission类型

/**
 * @description 获取所有角色列表
 */
export function fetchAllRoles(): Promise<ApiResult<Role[]>> {
    return request({
        url: '/api/v1/roles',
        method: 'get'
    });
}

// 【新增】获取角色权限详情的返回类型
interface RolePermissionResponse {
    allPermissions: Permission[];
    checkedIds: number[];
}

// 【新增】获取角色的权限详情
export function getRolePermissions(roleId: number): Promise<ApiResult<RolePermissionResponse>> {
    return request({
        url: `/api/v1/roles/${roleId}/permissions`,
        method: 'get'
    });
}

// 【新增】更新角色的权限
export function updateRolePermissions(roleId: number, permissionIds: number[]): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/roles/${roleId}/permissions`,
        method: 'put',
        data: permissionIds
    });
}