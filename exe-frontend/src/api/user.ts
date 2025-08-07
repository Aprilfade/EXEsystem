import request from '@/utils/request';

/**
 * 角色接口定义
 */
export interface Role {
    id: number;
    name: string;
    code: string;
    description: string;
}

/**
 * @description 对应后端返回的用户信息结构 (包含角色)
 * 这是我们统一的用户信息类型
 */
export interface UserInfo {
    id: number;
    username: string;
    password?: string;
    nickName: string;
    isEnabled: number;
    isDeleted: number;
    createTime: string;
    updateTime: string;
    roles?: Role[]; // 角色列表
}

/**
 * @description 对应后端 Result.java 的统一响应格式泛型接口
 */
export interface ApiResult<T> {
    code: number;
    msg: string;
    total: number;
    data: T;
}

/**
 * @description 用户列表的分页查询参数类型
 */
export interface UserPageParams {
    current: number; // 当前页码
    size: number;    // 每页记录数
    id?: number; // <-- 新增此行
}

/**
 * @description 分页获取用户列表
 * @param params 分页参数
 */
export function fetchUserList(params: UserPageParams): Promise<ApiResult<UserInfo[]>> {
    return request({
        url: '/api/v1/users',
        method: 'get',
        params: params
    });
}

/**
 * @description 创建一个新用户
 * @param data 用户数据
 */
export function createUser(data: Partial<UserInfo>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/users',
        method: 'post',
        data: data
    });
}

/**
 * @description 更新一个已存在的用户信息
 * @param id 要更新的用户ID
 * @param data 要更新的用户数据
 */
export function updateUser(id: number, data: Partial<UserInfo>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/users/${id}`,
        method: 'put',
        data: data
    });
}

/**
 * @description （逻辑）删除一个用户
 * @param id 要删除的用户ID
 */
export function deleteUser(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/users/${id}`,
        method: 'delete'
    });
}
/**
 * @description 根据ID获取单个用户详情
 * @param id 用户ID
 */
export function fetchUserById(id: number): Promise<ApiResult<UserInfo>> {
    return request({
        url: `/api/v1/users/${id}`, // 使用我们修正后的正确路径
        method: 'get'
    });

}
/**
 * @description 当前登录用户更新自己的信息
 * @param data 用户数据 (只包含允许修改的字段)
 */
export function updateMyProfile(data: Partial<UserInfo>): Promise<ApiResult<null>> {
    return request({
        url: '/api/v1/users/me', // 指向我们新创建的后端接口
        method: 'put',
        data: data
    });
}