import { defineStore } from 'pinia';
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api/auth';
import router from '../router';
import type { ApiResult, UserInfo } from '../api/user';

interface LoginResponse {
    token: string;
}

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || null,
        user: null as UserInfo | null,
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        userNickname: (state) => state.user?.nickName || '用户',

        isAdmin(state) {
            // --- 添加日志 ---
            console.log('[AuthStore Getter] 开始检查 isAdmin...');
            console.log('[AuthStore Getter] 当前 state.user:', state.user);

            const result = state.user?.roles?.some(role => role.code === 'ADMIN' || role.code === 'SUPER_ADMIN') || false;

            console.log(`[AuthStore Getter] 检查结果: user.roles 是否包含 'ADMIN' 或 'SUPER_ADMIN'? ${result}`);
            return result;
        },
        isSuperAdmin(state) {
            return state.user?.roles?.some(role => role.code === 'SUPER_ADMIN') || false;
        },
    },
    actions: {
        async login(credentials: any) {
            try {
                const response: ApiResult<LoginResponse> = await loginApi(credentials);
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
            if (!this.token) return;
            try {
                const response = await getUserInfoApi();

                // --- 添加日志 ---
                console.log('[AuthStore Action] 从后端获取到的原始用户信息:', response);

                if (response.code === 200) {
                    this.user = response.data;
                    console.log('[AuthStore Action] 用户信息已成功存入 state:', this.user);
                } else {
                    this.logout();
                }
            } catch (error) {
                console.error("获取用户信息失败", error);
                this.logout();
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