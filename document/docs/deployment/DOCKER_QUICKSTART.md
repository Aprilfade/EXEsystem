# Docker Compose 部署 - 快速开始

## ✅ 已完成的工作

Docker Compose 一键部署方案已全部完成！以下是所有创建的文件：

### 📁 文件清单

```
EXEsystem/
├── docker-compose.yml           # Docker Compose 编排文件
├── .env.example                 # 环境变量配置模板
├── deploy.bat                   # Windows 一键部署脚本
├── deploy.sh                    # Linux/Mac 一键部署脚本
├── Docker部署文档.md            # 详细部署文档
├── exe-backend/
│   ├── Dockerfile              # 后端 Docker 镜像构建文件
│   ├── .dockerignore           # 后端 Docker 忽略文件
│   └── pom.xml                 # 添加了 Actuator 依赖
└── exe-frontend/
    ├── Dockerfile              # 前端 Docker 镜像构建文件
    ├── .dockerignore           # 前端 Docker 忽略文件
    └── nginx.conf              # Nginx 配置文件
```

---

## 🚀 一键部署（推荐）

### Windows 用户

双击运行 `deploy.bat` 文件，即可自动完成以下步骤：
1. 检查 Docker 环境
2. 停止旧容器
3. 构建镜像
4. 启动所有服务
5. 显示服务状态

### Linux/Mac 用户

```bash
chmod +x deploy.sh
./deploy.sh
```

---

## 📋 手动部署

如果你想手动执行每一步，可以按照以下步骤操作：

### 步骤 1: 检查 Docker 环境

```bash
# 检查 Docker 版本
docker --version

# 检查 Docker Compose 版本
docker compose version
```

### 步骤 2: 配置环境变量（可选）

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，修改密码和密钥
# 生产环境必须修改默认密码！
```

### 步骤 3: 构建并启动服务

```bash
# 构建镜像并启动所有服务
docker compose up -d --build

# 查看启动日志
docker compose logs -f
```

### 步骤 4: 等待服务就绪

首次启动需要等待约 1-2 分钟，服务启动顺序：
1. MySQL（30-60秒）
2. Redis（5秒）
3. 后端（30-60秒，等待 MySQL/Redis 就绪）
4. 前端（5秒）

### 步骤 5: 验证部署

```bash
# 检查所有服务状态
docker compose ps

# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 检查前端是否可访问
curl http://localhost/
```

---

## 🌐 访问服务

部署成功后，可以通过以下地址访问：

| 服务 | 地址 | 说明 |
|-----|------|------|
| 前端 | http://localhost | Vue 3 应用 |
| 后端 | http://localhost:8080 | Spring Boot API |
| Swagger | http://localhost:8080/swagger-ui.html | API 文档 |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存 |

### 默认账号信息

```yaml
管理员: admin / (请查看初始数据或注册)
数据库: exeuser / exepass2024
Redis密码: exeredis2024
```

---

## 🛠️ 常用命令

### 查看日志

```bash
# 查看所有服务日志
docker compose logs -f

# 查看特定服务日志
docker compose logs backend
docker compose logs frontend
docker compose logs mysql
docker compose logs redis
```

### 服务管理

```bash
# 停止所有服务
docker compose stop

# 重启所有服务
docker compose restart

# 停止并删除容器（保留数据）
docker compose down

# 停止并删除容器和数据（完全清理）
docker compose down -v
```

### 进入容器

```bash
# 进入后端容器
docker exec -it exe-backend sh

# 进入 MySQL 容器
docker exec -it exe-mysql mysql -u root -pexesystem2024

# 进入 Redis 容器
docker exec -it exe-redis redis-cli -a exeredis2024
```

---

## 📊 服务架构

```
┌─────────────────────────────────────────────┐
│         用户浏览器                          │
│      http://localhost                       │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│   前端容器 (exe-frontend)                   │
│   Nginx + Vue 3                             │
│   Port: 80                                  │
└─────────────────┬───────────────────────────┘
                  │ API 代理 /api/*
                  ↓
┌─────────────────────────────────────────────┐
│   后端容器 (exe-backend)                    │
│   Spring Boot 3 + JWT                       │
│   Port: 8080                                │
└──────┬──────────────────┬───────────────────┘
       │                  │
       ↓                  ↓
┌──────────────┐   ┌──────────────┐
│ MySQL 容器   │   │ Redis 容器   │
│ Port: 3306   │   │ Port: 6379   │
└──────────────┘   └──────────────┘
```

---

## 🐛 故障排查

### 问题 1: 端口被占用

```bash
# Windows 查看端口占用
netstat -ano | findstr "80 3306 6379 8080"

# Linux/Mac 查看端口占用
lsof -i :80,3306,6379,8080

# 解决方案：修改 docker-compose.yml 中的端口映射
```

### 问题 2: 服务无法启动

```bash
# 查看详细日志
docker compose logs backend

# 常见原因：
# - MySQL 未就绪：等待更长时间
# - 内存不足：确保 Docker 分配至少 4GB 内存
# - 端口冲突：检查端口占用情况
```

### 问题 3: 数据库初始化失败

```bash
# 检查建表语句文件
cat exe-backend/建表语句.txt

# 重新初始化数据库
docker compose down -v
docker compose up -d
```

---

## 🔒 生产环境部署建议

### 1. 修改默认密码

编辑 `.env` 文件，修改以下配置：

```env
MYSQL_ROOT_PASSWORD=<strong-random-password>
MYSQL_PASSWORD=<strong-random-password>
REDIS_PASSWORD=<strong-random-password>
JWT_SECRET=<random-64-character-string>
```

### 2. 使用 HTTPS

建议使用 Nginx 反向代理 + Let's Encrypt SSL 证书。

### 3. 限制端口暴露

仅暴露必要的端口（80、443），不要暴露 3306、6379 等内部端口。

### 4. 定期备份数据

```bash
# 备份 MySQL 数据
docker exec exe-mysql mysqldump -u root -pexesystem2024 exe_system > backup_$(date +%Y%m%d).sql

# 备份上传文件
docker cp exe-backend:/app/uploads ./uploads_backup_$(date +%Y%m%d)
```

---

## 📚 相关文档

- **详细部署文档**: `Docker部署文档.md`
- **Swagger 文档**: `Swagger_API文档集成说明.md`
- **项目 README**: `README.md`

---

## 🎉 部署完成！

恭喜！你的 EXEsystem 项目已成功部署到 Docker 容器中。

现在你可以：
1. ✅ 访问 http://localhost 使用系统
2. ✅ 访问 http://localhost:8080/swagger-ui.html 查看 API 文档
3. ✅ 使用数据库管理工具连接 MySQL
4. ✅ 开始开发和测试

如有问题，请参考 `Docker部署文档.md` 获取详细帮助。

---

**创建时间：** 2026-01-06
**版本：** v2.51
