import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus'; // 假设你使用了Element Plus

const service: AxiosInstance = axios.create({
    baseURL: '/api', // 使用代理
    timeout: 10000,
});

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const authStore = useAuthStore();
        if (authStore.isAuthenticated) {
            // 让每个请求携带自定义 token
            config.headers['Authorization'] = `Bearer ${authStore.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data;
        // 假设 code === 200 为成功
        if (res.code !== 200 && res.code !== 20000) { // 兼容你的 Result 类
            ElMessage.error(res.msg || 'Error');
            return Promise.reject(new Error(res.msg || 'Error'));
        } else {
            return res;
        }
    },
    (error) => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            // token失效或无权限
            const authStore = useAuthStore();
            authStore.logout();
        } else {
            ElMessage.error(error.message);
        }
        return Promise.reject(error);
    }
);

export default service;