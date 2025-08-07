// src/api/role.ts

import request from '@/utils/request';
import type { ApiResult } from './user';
import type { Role } from './user';

/**
 * @description 获取所有角色列表
 */
export function fetchAllRoles(): Promise<ApiResult<Role[]>> {
    return request({
        url: '/api/v1/roles',
        method: 'get'
    });
}