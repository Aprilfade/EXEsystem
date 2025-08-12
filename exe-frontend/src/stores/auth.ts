import { defineStore } from 'pinia';
// 【重要修正】从各自的源文件导入类型
import { login as loginApi, getUserInfo as getUserInfoApi} from '../api/auth';
import type { UserInfo } from '../api/user'; // UserInfo 来自 api/user.ts
import type { UserInfoResponse } from '../api/auth';
import router from '../router';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || null,
        user: null as UserInfo | null,
        permissions: [] as string[],
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        userNickname: (state) => state.user?.nickName || '用户',
    },
    actions: {
        async login(credentials: any) {
            try {
                const response = await loginApi(credentials);
                if (response && response.code === 200) {
                    this.token = response.data.token;
                    localStorage.setItem('token', this.token);
                    await this.fetchUserInfo();
                    router.push('/home');
                }
            } catch (error) {
                console.error('登录失败:', error);
            }
        },

        async fetchUserInfo() {
            if (!this.token) return Promise.reject("No token");
            try {
                const response = await getUserInfoApi();
                if (response.code === 200) {
                    const data = response.data as UserInfoResponse;
                    this.user = data.user;
                    this.permissions = data.permissions;
                    return data.permissions;
                } else {
                    this.logout();
                    return Promise.reject("Failed to fetch user info");
                }
            } catch (error) {
                console.error("获取用户信息失败", error);
                this.logout();
                return Promise.reject(error);
            }
        },

        logout() {
            this.token = null;
            this.user = null;
            this.permissions = [];
            localStorage.removeItem('token');
            window.location.href = '/login';
        },

        hasPermission(permissionCode: string): boolean {
            if (!permissionCode) return true;
            if (this.permissions.includes('SUPER_ADMIN')) return true;
            return this.permissions.includes(permissionCode);
        }
    },
});