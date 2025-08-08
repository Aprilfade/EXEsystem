import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path'; // 1. 引入 node.js 的 'path' 模块

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],

  // 2. 添加 resolve.alias 配置项
  resolve: {
    alias: {
      // 这里告诉 Vite，'@' 符号就代表了 './src' 这个目录
      '@': path.resolve(__dirname, './src'),
    }
  },

  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
       //rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
});