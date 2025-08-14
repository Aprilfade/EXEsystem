import { defineStore } from 'pinia';
import { studentLogin, getStudentInfo } from '@/api/studentAuth';
import type { Student } from '@/api/student';
import router from '../router';
import type { RouteLocationRaw } from 'vue-router';

const STUDENT_TOKEN_KEY = 'student_token'; // 使用独立的localStorage Key

export const useStudentAuthStore = defineStore('studentAuth', {
    state: () => ({
        token: localStorage.getItem(STUDENT_TOKEN_KEY) || null,
        student: null as Student | null,
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        studentName: (state) => state.student?.name || '同学',
    },
    actions: {
        async login(credentials: any, redirect?: RouteLocationRaw) {
            try {
                const response = await studentLogin(credentials);
                if (response && response.code === 200) {
                    this.token = response.data.token;
                    localStorage.setItem(STUDENT_TOKEN_KEY, this.token);
                    await this.fetchStudentInfo();
                    router.push(redirect || '/student/dashboard');
                }
            } catch (error) {
                console.error('学生登录失败:', error);
            }
        },

        async fetchStudentInfo() {
            if (!this.token) return Promise.reject("No student token");
            try {
                const response = await getStudentInfo();
                if (response.code === 200) {
                    this.student = response.data;
                } else {
                    this.logout();
                }
            } catch (error) {
                console.error("获取学生信息失败", error);
                this.logout();
            }
        },

        logout() {
            this.token = null;
            this.student = null;
            localStorage.removeItem(STUDENT_TOKEN_KEY);
            // 登出后统一返回导航首页
            router.push('/portal');
        },
    },
});