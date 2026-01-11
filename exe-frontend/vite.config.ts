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
  },

  // 构建优化配置
  build: {
    // 目标浏览器
    target: 'es2015',

    // 生产环境移除 console
    minify: 'terser',
    // terserOptions: {
    //   compress: {
    //     drop_console: true,
    //     drop_debugger: true
    //   }
    // },

    // 代码分割优化
    rollupOptions: {
      output: {
        // 手动分包
        manualChunks: {
          // Vue 核心库
          'vue-vendor': ['vue', 'vue-router', 'pinia'],

          // Element Plus UI 库
          'element-plus': ['element-plus'],

          // ECharts 图表库
          'echarts': ['echarts'],

          // 其他第三方库
          'vendors': ['axios', 'gsap']
        },

        // 静态资源分类输出
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          // 图片文件
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/.test(assetInfo.name || '')) {
            return 'images/[name]-[hash][extname]';
          }
          // 字体文件
          if (/\.(woff2?|eot|ttf|otf)$/.test(assetInfo.name || '')) {
            return 'fonts/[name]-[hash][extname]';
          }
          // CSS 文件
          if (/\.css$/.test(assetInfo.name || '')) {
            return 'css/[name]-[hash][extname]';
          }
          // 其他资源
          return 'assets/[name]-[hash][extname]';
        }
      }
    },

    // Chunk 大小警告限制（500kb）
    chunkSizeWarningLimit: 500,

    // 启用 CSS 代码分割
    cssCodeSplit: true,

    // 生成 sourcemap（生产环境可设为 false）
    sourcemap: false
  },

  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'element-plus',
      'echarts',
      'axios',
      'gsap'
    ]
  }
});