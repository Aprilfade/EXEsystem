// 导入 axios 的默认导出，以及专门用于类型的 'type' 关键字
import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useAuthStore } from '../stores/auth';
import { ElMessage } from 'element-plus';

// 创建 axios 实例
const service: AxiosInstance = axios.create({
    // 我们在 vite.config.js 中配置了代理，所以这里使用相对路径
    baseURL: '',
    timeout: 10000,
});

// 请求拦截器: 在每次请求前执行
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const authStore = useAuthStore();
        // 检查用户是否已登录 (通过 token 判断)
        if (authStore.isAuthenticated) {
            // 如果已登录，在请求头中添加 Authorization 字段，值为 Bearer Token
            config.headers['Authorization'] = `Bearer ${authStore.token}`;
        }
        return config;
    },
    (error) => {
        // 对请求错误做些什么
        console.error('请求错误:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器: 在每次接收到响应后执行
service.interceptors.response.use(
    (response: AxiosResponse) => {
        // 后端返回的统一响应格式
        const res = response.data;

        // 根据后端的业务状态码 (code) 进行判断
        // 在你的 Result.java 中，200 代表成功
        if (res.code !== 200) {
            ElMessage.error(res.msg || 'Error');

            // 如果是 401 (通常代表 token 失效或未认证)，则触发登出操作
            if (res.code === 401 || res.code === 40001) {
                const authStore = useAuthStore();
                authStore.logout();
            }
            return Promise.reject(new Error(res.msg || 'Error'));
        } else {
            // 如果成功，直接返回后端响应中的 data 字段
            return res;
        }
    },
    (error) => {
        // 处理 HTTP 级别的错误 (如 404, 500 等)
        console.error('响应错误:', error);
        ElMessage.error(error.message);
        return Promise.reject(error);
    }
);

export default service;