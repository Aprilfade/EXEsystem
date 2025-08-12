import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth' // 1. 导入 auth store

import './style.css'

// 2. 使用异步函数来初始化应用
const initializeApp = async () => {
    const app = createApp(App)

    app.use(createPinia())

    // 3. 在挂载路由前，先尝试获取用户信息
    // 这是解决刷新丢失数据的关键！
    const authStore = useAuthStore()

    // 注册全局权限指令 v-permission
    app.directive('permission', {
        mounted(el, binding) {
            const permissionCode = binding.value; // 获取指令的值 e.g., 'sys:user:delete'
            if (permissionCode && !authStore.hasPermission(permissionCode)) {
                // 如果用户没有权限，则从DOM中移除该元素
                el.parentNode?.removeChild(el);
            }
        },
    });


    if (authStore.token) {
        try {
            await authStore.fetchUserInfo()
        } catch (error) {
            // 如果获取失败（例如token过期），authStore内部会处理登出
            console.error('App initialization failed to fetch user info:', error)
        }
    }

    app.use(router)
    app.use(ElementPlus)

    app.mount('#app')
}

// 4. 执行初始化
initializeApp()