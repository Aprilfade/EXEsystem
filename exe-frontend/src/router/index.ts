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
    // 【新增】导航首页的路由，注意没有 meta: { requiresAuth: true }
    {
        path: '/portal',
        name: 'Portal',
        component: () => import('@/views/Portal.vue'),
    },
    {
        path: '/',
        component: MainLayout,
        redirect: '/portal',
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
            { path: 'logs/login', name: 'LoginLog', component: () => import('@/views/LoginLog.vue'), meta: { permission: 'sys:log:login', title: '登录日志' } },
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 【修改】全局前置守卫 (最终优化版)
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();
    const isAuthenticated = authStore.isAuthenticated;

    // 目标路径需要认证
    if (to.meta.requiresAuth) {
        if (isAuthenticated) {
            // 如果已登录
            if (!authStore.user) {
                try {
                    await authStore.fetchUserInfo();
                } catch {
                    authStore.logout();
                    // 【新增】如果获取用户信息失败，也应该重定向到登录页
                    return next({ path: '/login', query: { redirect: to.fullPath } });
                }
            }

            // 权限检查
            const requiredPermission = to.meta.permission as string | undefined;
            if (requiredPermission && !authStore.hasPermission(requiredPermission)) {
                ElMessage.error('您没有权限访问该页面');
                next(from.path && from.path !== '/login' ? false : '/home');
            } else {
                next();
            }
        } else {
            // 如果未登录，则重定向到登录页，并带上目标路径
            next({ path: '/login', query: { redirect: to.fullPath } });
        }
    } else {
        // 目标路径不需要认证（例如登录页、导航首页），直接放行
        next();
    }
});

export default router;