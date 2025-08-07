// 导入已配置好的 Axios 实例
// 我们假设 @/utils/request 是一个经过配置的 Axios 实例，其响应拦截器会返回完整的后端响应体。
import request from '@/utils/request';

/**
 * @description 对应后端 SysUser 实体类的接口
 */
export interface SysUser {
    id: number;
    username: string;
    password?: string; // 密码在创建时需要，但在获取列表时不应返回，因此设为可选
    nickName: string;
    isEnabled: number; // 账户是否启用 (1: 是, 0:否)
    isDeleted: number; // 逻辑删除标记 (1: 是, 0:否)
    createTime: string; // 在JSON传输中，日期通常序列化为字符串
    updateTime: string;
}

/**
 * @description 对应后端 Result.java 的统一响应格式泛型接口
 */
export interface ApiResult<T> {
    code: number;    // 业务状态码, e.g., 200 or 400
    msg: string;     // 响应消息, e.g., "成功" or "失败"
    total: number;   // 总记录数，主要用于分页
    data: T;         // 实际数据负载
}

/**
 * @description 用户列表的分页查询参数类型
 */
export interface UserPageParams {
    current: number; // 当前页码
    size: number;    // 每页记录数
}

/**
 * @description 分页获取用户列表
 * @param params 分页参数
 * @returns 返回一个包含用户数组和总数的Promise
 */
export function fetchUserList(params: UserPageParams): Promise<ApiResult<SysUser[]>> {
    return request({
        url: '/api/v1/users',
        method: 'get',
        params: params
    });
}

/**
 * @description 根据ID获取单个用户详情
 * @param id 用户ID
 * @returns 返回包含单个用户信息的Promise
 */
export function getUserById(id: number): Promise<ApiResult<SysUser>> {
    return request({
        url: `/api/v1/users/${id}`,
        method: 'get'
    });
}

/**
 * @description 创建一个新用户
 * @param data 用户数据，使用 Partial<SysUser> 表示可以只传入部分用户字段
 * @returns 返回操作结果的Promise
 */
export function createUser(data: Partial<SysUser>): Promise<ApiResult<null>> {
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
 * @returns 返回操作结果的Promise
 */
export function updateUser(id: number, data: Partial<SysUser>): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/users/${id}`,
        method: 'put',
        data: data
    });
}

/**
 * @description （逻辑）删除一个用户
 * @param id 要删除的用户ID
 * @returns 返回操作结果的Promise
 */
export function deleteUser(id: number): Promise<ApiResult<null>> {
    return request({
        url: `/api/v1/users/${id}`,
        method: 'delete'
    });
}