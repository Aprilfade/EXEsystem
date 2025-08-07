// src/env.d.ts

/// <reference types="vite/client" />

declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

// === 在下面添加以下代码 ===
declare module '@/utils/request' {
    import { AxiosInstance } from 'axios';
    const instance: AxiosInstance;
    export default instance;
}