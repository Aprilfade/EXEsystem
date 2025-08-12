import { createRouter, createWebHistory,} from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import MainLayout from '@/layouts/MainLayout.vue';
import { ElMessage } from "element-plus";

const routes: Array<RouteRecordRaw> = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
    },
    {
        path: '/',
        component: MainLayout,
        redirect: '/home',
        meta: { requiresAuth: true },
        children: [
            { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { permission: 'sys:home', title: '工作台' } },
            { path: 'users', name: 'UserManagement', component: () => import('@/components/user/UserManage.vue'), meta: { permission: 'sys:user:list', title: '成员管理' } },
            { path: 'roles', name: 'RoleManagement', component: () => import('@/views/RoleManage.vue'), meta: { permission: 'sys:role:perm', title: '角色管理' } },
            { path: 'subjects', name: 'SubjectManagement', component: () => import('@/views/SubjectManage.vue'), meta: { permission: 'sys:subject:list', title: '科目管理' } },
            { path: 'knowledge-points', name: 'KnowledgePointManagement', component: () => import('@/views/KnowledgePointManage.vue'), meta: { permission: 'sys:kp:list', title: '知识点管理' } },
            { path: 'questions', name: 'QuestionManagement', component: () => import('@/views/QuestionManage.vue'), meta: { permission: 'sys:question:list', title: '题库管理' } },
            { path: 'papers', name: 'PaperManagement', component: () => import('@/views/PaperManage.vue'), meta: { permission: 'sys:paper:list', title: '试卷管理' } },
            { path: 'students', name: 'StudentManagement', component: () => import('@/views/StudentManage.vue'), meta: { permission: 'sys:student:list', title: '学生管理' } },
            { path: 'wrong-records', name: 'WrongRecordManagement', component: () => import('@/views/WrongRecordManage.vue'), meta: { permission: 'sys:wrong:list', title: '错题管理' } },
            { path: 'wrong-record-stats', name: 'WrongRecordStats', component: () => import('@/views/WrongRecordStats.vue'), meta: { permission: 'sys:wrong:list', title: '错题统计' } },
            { path: 'statistics', name: 'StatisticsDashboard', component: () => import('@/views/StatisticsDashboard.vue'), meta: { permission: 'sys:stats:list', title: '教学统计' } },
            { path: 'notifications', name: 'NotificationManagement', component: () => import('@/views/NotificationManage.vue'), meta: { permission: 'sys:notify:list', title: '通知管理' } },
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 全局前置守卫 (最终优化版)
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();
    const isAuthenticated = authStore.isAuthenticated;

    if (isAuthenticated) {
        // 如果已登录
        if (!authStore.user) {
            try {
                // 如果是刷新页面，Pinia中没有用户信息，则重新获取
                await authStore.fetchUserInfo();
            } catch {
                // 如果获取失败（token失效等），则登出
                authStore.logout();
                return; // 中断导航，因为logout会重定向到/login
            }
        }

        // 检查目标路由权限
        const requiredPermission = to.meta.permission as string | undefined;
        if (requiredPermission && !authStore.hasPermission(requiredPermission)) {
            // 如果没有权限，提示并中断导航，停留在当前页面或跳转到主页
            ElMessage.error('您没有权限访问该页面');
            // 如果from.path存在且不是登录页，则返回上一页，否则去主页
            next(from.path && from.path !== '/login' ? false : '/home');
        } else {
            // 有权限，或目标路由不需要权限，直接放行
            next();
        }
    } else {
        // 如果未登录
        if (to.path !== '/login') {
            next({ path: '/login' });
        } else {
            next();
        }
    }
});

export default router;