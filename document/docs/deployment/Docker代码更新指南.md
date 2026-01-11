# Docker 代码更新指南

## 问题说明

当你修改代码后，在 Docker 中看不到更新的原因是：
- Docker 容器运行的是**已经构建好的镜像**
- 镜像是在构建时将代码打包成静态文件
- 修改代码后，旧镜像不会自动更新

## 解决方案

### 方式一：使用快捷脚本（最简单）✅

双击运行 `docker-update.bat`，选择相应的操作即可：
```
[1] 更新全部服务（前端+后端）
[2] 仅更新前端
[3] 仅更新后端
[4] 查看服务状态
...
```

### 方式二：手动执行命令

#### 1. 更新所有服务
```bash
docker-compose up -d --build
```

#### 2. 仅更新前端
```bash
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

#### 3. 仅更新后端
```bash
docker-compose build --no-cache backend
docker-compose up -d backend
```

#### 4. 完全清理后重新构建（最彻底）
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## 开发模式对比

### Docker 生产模式
- ✅ 接近真实部署环境
- ✅ 包含完整服务栈（MySQL、Redis等）
- ✅ 适合集成测试
- ❌ 需要重新构建才能看到更新
- ❌ 构建速度较慢（1-2分钟）

### IDEA 开发模式
- ✅ 热重载，修改即生效
- ✅ 启动速度快
- ✅ 方便调试
- ❌ 需要手动启动数据库等依赖
- ❌ 环境配置较复杂

## 推荐工作流程

### 日常开发
使用 IDEA 启动服务，方便调试和快速验证：
1. 启动 MySQL 和 Redis（可用 Docker）
2. IDEA 中启动后端
3. `npm run dev` 启动前端
4. 访问 http://localhost:5173

### 部署测试
使用 Docker 验证完整部署流程：
1. 修改代码后执行 `docker-update.bat`
2. 选择 [1] 更新全部服务
3. 访问 http://localhost

### 生产部署
```bash
# 1. 拉取最新代码
git pull

# 2. 构建并启动
docker-compose up -d --build

# 3. 查看日志
docker-compose logs -f
```

## 常见问题

### Q1: 为什么构建这么慢？
A: 前端需要运行 npm install 和 vite build，后端需要编译 Java 代码。
可以通过以下方式加速：
- 仅更新修改的服务（前端或后端）
- 使用 Docker 构建缓存（去掉 --no-cache）

### Q2: 端口冲突怎么办？
A: 如果 IDEA 占用了 8080 端口，需要：
- 方案1：停止 IDEA 服务，使用 Docker
- 方案2：修改 docker-compose.yml 端口映射

### Q3: 数据库数据丢失了？
A: Docker volumes 会持久化数据，除非执行：
```bash
docker-compose down -v  # 加了 -v 会删除数据卷
```

### Q4: 如何查看容器日志？
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务
docker logs -f exe-frontend
docker logs -f exe-backend
```

## 文件说明

- `docker-compose.yml` - Docker 编排配置
- `exe-frontend/Dockerfile` - 前端构建配置
- `exe-backend/Dockerfile` - 后端构建配置
- `exe-frontend/nginx.conf` - Nginx 代理配置
- `docker-update.bat` - 快捷更新脚本

## 访问地址

| 服务 | 开发模式 | Docker 模式 |
|------|---------|-------------|
| 前端 | http://localhost:5173 | http://localhost |
| 后端 | http://localhost:8080 | http://localhost:8080 |
| MySQL | localhost:3306 | localhost:3307 |
| Redis | localhost:6379 | localhost:6379 |

---

**提示**: 建议日常开发用 IDEA，部署测试用 Docker，这样既保证开发效率，又能验证部署流程。
