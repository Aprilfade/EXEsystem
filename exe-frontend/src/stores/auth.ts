import { defineStore } from 'pinia';
import { login as loginApi } from '@/api/auth'; // 我们稍后会创建这个文件
import router from '@/router';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || null,
        user: null, // 可以用来存储用户信息
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
    },
    actions: {
        async login(credentials: any) {
            try {
                const response = await loginApi(credentials);
                const token = response.data.token;
                this.token = token;
                localStorage.setItem('token', token);
                // 登录成功后跳转到首页
                router.push('/');
            } catch (error) {
                console.error('登录失败:', error);
                // 这里可以添加错误提示，例如使用 Element Plus 的 ElMessage
            }
        },
        logout() {
            this.token = null;
            this.user = null;
            localStorage.removeItem('token');
            router.push('/login');
        },
    },
});