import request from '@/utils/request';

export function login(data: any) {
    return request({
        url: '/api/v1/auth/login', // 对应后端的登录接口
        method: 'post',
        data
    });
}