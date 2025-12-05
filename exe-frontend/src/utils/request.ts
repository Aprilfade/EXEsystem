import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useAuthStore } from '../stores/auth';
// 【新增】导入学生认证 store
import { useStudentAuthStore } from '../stores/studentAuth';
import { ElMessage } from 'element-plus';

// 创建 axios 实例
const service: AxiosInstance = axios.create({
    baseURL: '',
    timeout: 480000,
});

// 请求拦截器: 在每次请求前执行
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // 【核心修改】在这里同时获取两个store的实例
        const authStore = useAuthStore();
        const studentAuthStore = useStudentAuthStore();

        // 【核心修改】根据请求的URL，动态决定附加哪个Token
        if (config.url && config.url.startsWith('/api/v1/student/')) {
            // 如果是发往学生端的API
            if (studentAuthStore.isAuthenticated && studentAuthStore.token) {
                config.headers['Authorization'] = `Bearer ${studentAuthStore.token}`;
            }
        } else {
            // 否则，默认是发往管理后台的API
            if (authStore.isAuthenticated && authStore.token) {
                config.headers['Authorization'] = `Bearer ${authStore.token}`;
            }
        }

        return config;
    },
    (error) => {
        console.error('请求错误:', error);
        return Promise.reject(error);
    }
);
// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        if (response.data instanceof Blob) {
            return response;
        }
        const res = response.data;

        // 【修复点】：允许 code 200 (成功) 和 202 (特殊业务逻辑，如心魔) 通过
        // 原代码：if (res.code !== 200) {
        if (res.code !== 200 && res.code !== 202) {
            ElMessage.error(res.msg || 'Error');

            // 401 token 失效处理 (保持不变)
            if (res.code === 401 || res.code === 40001) {
                if (response.config.url?.startsWith('/api/v1/student/')) {
                    useStudentAuthStore().logout();
                } else {
                    useAuthStore().logout();
                }
            }
            return Promise.reject(new Error(res.msg || 'Error'));
        } else {
            // 200 和 202 都会走到这里，返回给组件处理
            return res;
        }
    },
    (error) => {
        // ... 错误处理保持不变 ...
        console.error('响应错误:', error);
        ElMessage.error(error.message);
        return Promise.reject(error);
    }
);

export default service;