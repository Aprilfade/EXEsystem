// 创建文件: D:\Desktop\everything\EXEsystem\exe-frontend\src\utils\request.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios';

// 创建 axios 实例
const service: AxiosInstance = axios.create({
    // 从环境变量中获取 baseURL，如果没有则使用默认值
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    // 请求超时时间
    timeout: 5000
});

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // 可以在这里添加认证 token 等
        // const token = localStorage.getItem('access_token');
        // if (token) {
        //   config.headers.Authorization = `Bearer ${token}`;
        // }
        return config;
    },
    (error) => {
        // 对请求错误做些什么
        console.error('请求错误:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        // 对响应数据做点什么
        return response.data;
    },
    (error) => {
        // 对响应错误做点什么
        console.error('响应错误:', error);
        return Promise.reject(error);
    }
);

export default service;
