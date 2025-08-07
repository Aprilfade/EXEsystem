import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
    },
    {
        path: '/',
        // 使用主布局组件
        component: () => import('@/layouts/MainLayout.vue'),
        children: [
            {
                path: 'users',
                name: 'UserManagement',
                component: () => import('@/components/user/UserManage.vue'),
                meta: { requiresAuth: true, permission: 'sys:user:list' }
            }
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

    if (requiresAuth && !authStore.isAuthenticated) {
        // 如果目标路由需要认证,但用户未登录,则重定向到登录页
        next({ name: 'Login', query: { redirect: to.fullPath } });
    } else {
        next(); // 正常放行
    }
});

export default router;
