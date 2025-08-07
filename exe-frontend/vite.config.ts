import {defineConfig} from "vite";

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 字符串简写写法
      // '/api': 'http://localhost:8080'
      // 选项写法
      '/api': {
        target: 'http://localhost:8080', // 目标后端服务地址
        changeOrigin: true, // 需要虚拟主机站点
        rewrite: (path) => path.replace(/^\/api/, '') // 重写 API 前缀
      }
    }
  }
})