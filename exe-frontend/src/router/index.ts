// src/router/index.ts

import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import MainLayout from '@/layouts/MainLayout.vue';
import {ElMessage} from "element-plus";




const routes = [
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
            {
                path: 'home',
                name: 'Home',
                component: () => import('@/views/Home.vue'),
            },
            {
                path: 'users',
                name: 'UserManagement',
                component: () => import('@/components/user/UserManage.vue'),
                meta: {
                    permission: 'sys:user:list', // 这里可以保留，也可以用下面的isAdmin
                    requiresAdmin: true // 新增一个标记，表示此路由需要管理员权限
                }

            },
            {
                path: 'subjects',
                name: 'SubjectManagement',
                component: () => import('@/views/SubjectManage.vue'),
                meta: {
                    requiresAdmin: true // 同样需要管理员权限
                }
            },
            // --- 新增知识点管理路由 ---
            {
                path: 'knowledge-points',
                name: 'KnowledgePointManagement',
                component: () => import('@/views/KnowledgePointManage.vue'),
                meta: {
                    requiresAdmin: true // 需要管理员权限
                }
            },
            // --- 新增试题管理路由 ---
            {
                path: 'questions',
                name: 'QuestionManagement',
                component: () => import('@/views/QuestionManage.vue'),
                meta: {
                    requiresAdmin: true // 需要管理员权限
                }
            },
            {
                path: 'papers',
                name: 'PaperManagement',
                component: () => import('@/views/PaperManage.vue'),
                meta: {
                    requiresAdmin: true
                }
            }

        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 全局前置守卫 (替换为新的逻辑)
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    const isAuthenticated = authStore.isAuthenticated;

    // 检查目标路由是否需要认证
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

    if (requiresAuth && !isAuthenticated) {
        // 如果需要认证但用户未登录，跳转到登录页
        return next({ name: 'Login' });
    }

    // 检查目标路由是否需要管理员权限
    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);

    if (requiresAdmin && !authStore.isAdmin) {
        // 如果需要管理员权限但当前用户不是管理员，则重定向到主页
        ElMessage.error('您没有权限访问该页面');
        return next({ name: 'Home' });
    }

    // 其他所有情况，正常放行
    next();
});

export default router;