# EXEsystem - 智能在线考试与学习管理系统

<div align="center">

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5.17-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.2%2B-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-supported-blue.svg)](https://www.docker.com/)

**功能完整 | AI赋能 | 游戏化学习 | 企业级架构**

[在线演示](#) | [快速开始](#快速开始) | [部署文档](docs/deployment/DOCKER_QUICKSTART.md) | [API文档](docs/api/API_DOCUMENTATION.md) | [📚 文档中心](docs/README.md)

</div>

---

## 📖 目录

- [项目简介](#项目简介)
- [核心特性](#核心特性)
- [技术架构](#技术架构)
- [快速开始](#快速开始)
- [Docker部署](#docker部署)
- [文档中心](#文档中心)
- [配置说明](#配置说明)
- [开发指南](#开发指南)
- [常见问题](#常见问题)
- [版本历史](#版本历史)
- [贡献指南](#贡献指南)

---

## 项目简介

**EXEsystem** 是一个功能完整的在线考试与智能学习管理系统，采用现代化的前后端分离架构。系统集成了AI智能批改、实时知识对战、智能复习推荐等创新功能。

### 系统定位

- 🎓 **教育机构**：适用于K12、培训机构的在线教学和考试管理
- 🏫 **学校**：班级管理、成绩分析、错题追踪
- 👨‍🏫 **教师**：智能组卷、AI批改、教学数据分析  
- 👨‍🎓 **学生**：在线练习、模拟考试、智能复习、知识对战

### 当前版本

**v2.51** - 2026年1月

---

## 核心特性

### 🤖 AI智能功能

- **智能出题**：基于文本、知识点自动生成题目
- **主观题AI批改**：自动评分、智能反馈（支持DeepSeek、通义千问）
- **学习分析报告**：AI生成个性化学习建议
- **AI监控**：API调用监控、成本统计、性能分析

### 🎮 游戏化学习

- **知识竞技场**：实时1v1对战、段位匹配、连击系统
- **成就系统**：多维度成就勋章
- **积分商城**：学习成果兑换奖励
- **排行榜**：班级/学校排名竞争
- **签到打卡**：日常学习激励

### 📊 智能复习

- **艾宾浩斯遗忘曲线**：科学的复习时间推荐
- **知识点掌握度分析**：基于答题正确率实时更新
- **薄弱点推荐**：自动识别需要加强的知识点
- **错题本**：自动收集、智能分类

---

## 技术架构

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.4 | 核心框架 |
| Spring Security | 6.x | 安全认证 |
| MyBatis Plus | 3.5.12 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.2+ | 缓存 |
| JWT | 0.11.5 | Token认证 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.5.17 | 前端框架 |
| TypeScript | 5.8.3 | 类型安全 |
| Element Plus | 2.10.5 | UI组件 |
| ECharts | 5.6.0 | 图表库 |
| Vite | 7.0.4 | 构建工具 |

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9.6+
- Node.js 20+
- MySQL 8.0+
- Redis 7.2+

### 本地开发

#### 1. 克隆项目

```bash
git clone https://github.com/your-repo/EXEsystem.git
cd EXEsystem
```

#### 2. 配置数据库

```bash
mysql -u root -p
CREATE DATABASE exam_system CHARACTER SET utf8mb4;
mysql -u root -p exam_system < exe-backend/建表语句.txt
```

#### 3. 配置后端

修改 `exe-backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system
    username: root
    password: YOUR_PASSWORD
```

#### 4. 启动后端

```bash
cd exe-backend
mvn spring-boot:run
```

#### 5. 启动前端

```bash
cd exe-frontend
npm install
npm run dev
```

#### 6. 访问系统

- 管理端: http://localhost:5173/login (admin/123456)
- API文档: http://localhost:8080/swagger-ui.html

---

## Docker部署

### 一键部署

```bash
# 复制环境变量
cp .env.example .env

# 编辑配置
nano .env

# 启动服务
docker-compose up -d --build

# 查看状态
docker-compose ps
```

### 访问系统

- 前端: http://localhost:8090
- 后端: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

详细文档: [Docker快速部署指南](docs/deployment/DOCKER_QUICKSTART.md)

---

## 文档中心

所有项目文档已按功能分类整理，方便查阅：

### 📖 主要文档

- [📚 文档中心导航](docs/README.md) - **从这里开始**
- [🚀 API完整文档](docs/api/API_DOCUMENTATION.md) - 60+ API接口
- [🐳 Docker部署](docs/deployment/Docker部署文档.md) - 完整部署流程
- [⚡ 快速修复指南](docs/deployment/QUICK_FIX.md) - 常见问题解决

### 🗂️ 文档分类

```
docs/
├── api/                    # API文档和Swagger说明
├── deployment/             # Docker部署和运维文档
├── optimization/           # 功能优化报告和快速指南
└── database/               # 数据库脚本（建表/索引/初始化）
```

### 🔧 数据库脚本

- [智能索引优化](docs/database/indexes/数据库索引优化-智能版.sql) - 一键创建43个性能索引
- [初始化题库数据](docs/database/initialization/初始化题库数据.sql) - 测试数据导入
- [检查索引状态](docs/database/indexes/检查现有索引.sql) - 索引健康检查

### 📊 优化报告

查看各模块的性能优化和功能增强报告：[docs/optimization/reports/](docs/optimization/reports/)

---

## 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system
    username: root
    password: YOUR_PASSWORD
    hikari:
      maximum-pool-size: 20
```

### AI服务配置

```yaml
ai:
  enabled: true
  default-provider: deepseek
  providers:
    deepseek:
      url: https://api.deepseek.com/chat/completions
```

---

## 开发指南

### 代码规范

- 后端：阿里巴巴Java开发手册
- 前端：ESLint + Prettier
- 提交：Conventional Commits

### 项目结构

```
EXEsystem/
├── exe-backend/        # 后端（Spring Boot）
│   ├── controller/     # 控制器（32个）
│   ├── service/        # 服务层（42个）
│   ├── entity/         # 实体类（37个）
│   └── mapper/         # Mapper（40个）
├── exe-frontend/       # 前端（Vue 3）
│   ├── views/          # 页面（35个）
│   ├── components/     # 组件（68个）
│   └── api/            # API（26个模块）
├── docs/               # 📚 文档中心
│   ├── api/            # API文档
│   ├── deployment/     # 部署文档
│   ├── optimization/   # 优化报告
│   └── database/       # 数据库脚本
└── docker-compose.yml  # Docker编排
```

---

## 常见问题

### Q: Docker部署后无法访问？

检查防火墙和端口映射：

```bash
docker-compose ps
docker-compose logs backend
```

### Q: AI功能不可用？

1. 确保 `ai.enabled=true`
2. 配置有效的API Key
3. 检查网络连接

---

## 版本历史

### v2.51 (2026-01-08)
- ✅ 知识点批量编辑功能
- ✅ 移除Lombok依赖
- ✅ 完成三大性能优化（N+1查询、数据库索引、异常处理）
- ✅ 项目文档中心重构（分类整理40+文档）

### v2.50 (2026-01-07)
- ✅ 知识竞技场持久化
- ✅ Excel导入优化

---

## 贡献指南

欢迎贡献！

1. Fork项目
2. 创建分支: `git checkout -b feature/AmazingFeature`
3. 提交更改: `git commit -m 'Add feature'`
4. 推送分支: `git push origin feature/AmazingFeature`
5. 提交PR

---

## 许可证

MIT License - 详见 [LICENSE](LICENSE)

---

## 联系方式

- 项目主页: https://github.com/your-repo/EXEsystem
- 问题反馈: https://github.com/your-repo/EXEsystem/issues

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个Star！**

Made with ❤️ by EXEsystem Team

</div>
