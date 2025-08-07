import { createApp } from 'vue'
import { createPinia } from 'pinia' // 引入
import App from './App.vue'
import router from './router'; // 引入路由

import './style.css'

const app = createApp(App)

app.use(createPinia()) // 使用
app.use(router) // 使用路由
app.mount('#app')