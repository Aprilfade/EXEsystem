import { defineStore } from 'pinia';
// 【重要修正】从各自的源文件导入类型
import { login as loginApi, getUserInfo as getUserInfoApi, logoutApi } from '../api/auth';
import type { UserInfo } from '../api/user'; // UserInfo 来自 api/user.ts
import type { UserInfoResponse } from '../api/auth';
import router from '../router';
// 【新增】从 vue-router 导入 RouteLocationRaw
import type { RouteLocationRaw } from 'vue-router';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || null,
        user: null as UserInfo | null,
        permissions: [] as string[],
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        userNickname: (state) => state.user?.nickName || '用户',

        // 【新增】判断是否为超级管理员的 getter
        isSuperAdmin(state): boolean {
            return state.user?.roles?.some(role => role.code === 'SUPER_ADMIN') || false;
        },
        // 【新增】判断是否为管理员的 getter (未来可能有用)
        isAdmin(state): boolean {
            return state.user?.roles?.some(role => role.code === 'ADMIN') || false;
        },
    },
    actions: {
        // 【修改】login action 增加一个可选的 redirect 参数
        async login(credentials: any, redirect?: RouteLocationRaw) {
            try {
                const response = await loginApi(credentials);
                if (response && response.code === 200) {
                    this.token = response.data.token;
                    localStorage.setItem('token', this.token);
                    await this.fetchUserInfo();
                    // 【修改】优先跳转到 redirect 参数指定的路径，如果没有则跳转到首页
                    router.push(redirect || '/home');
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

        // 【修改】将 logout 方法改为 async
        async logout() {
            try {
                // 先调用后端接口记录登出日志
                await logoutApi();
            } catch (error) {
                console.error("调用登出接口失败:", error);
            } finally {
                // 无论接口成功与否，都执行前端的登出操作
                this.token = null;
                this.user = null;
                this.permissions = [];
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        },
        // 【核心修复】修正 hasPermission 方法的逻辑
        hasPermission(permissionCode: string): boolean {
            if (!permissionCode) {
                return true; // 如果路由或按钮不需要权限，则直接放行
            }
            // 如果是超级管理员，则直接拥有所有权限
            if (this.isSuperAdmin) {
                return true;
            }
            // 否则，在权限列表中检查是否存在对应的权限码
            return this.permissions.includes(permissionCode);
        }
    },
});