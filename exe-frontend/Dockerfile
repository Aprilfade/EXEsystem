# ============================================
# 前端 Dockerfile - 多阶段构建
# 阶段1：Node.js 构建
# 阶段2：Nginx 运行
# ============================================

# ===== 阶段 1: 构建阶段 =====
FROM node:20-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制 package.json 和 package-lock.json（利用 Docker 缓存）
COPY package*.json ./

# 安装依赖（使用国内镜像加速）
RUN npm config set registry https://registry.npmmirror.com && \
    npm install

# 复制源代码
COPY . .

# 构建生产版本（跳过 TypeScript 类型检查以加快构建速度）
RUN npx vite build

# ===== 阶段 2: 运行阶段 =====
FROM nginx:1.25-alpine

# 安装必要的工具
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 删除默认的 Nginx 配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
