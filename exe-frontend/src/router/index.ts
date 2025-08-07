// src/router/index.ts
// ... 其他路由
import component from "element-plus/es/components/tree-select/src/tree-select-option";

{
    path: '/system',
        component: Layout, // 假设已有主布局组件
    children: [
    {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/system/User.vue'),
        meta: {
            title: '用户管理',
            requiresAuth: true,
            permission: 'sys:user:list' // 页面级权限控制 [cite: 501]
        }
    }
]
}