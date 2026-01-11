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
- [📚 完整文档中心](#完整文档中心)
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

**v3.0** - 2026年1月11日

### ✨ v3.0 更新亮点

1. **🔐 安全性增强**
   - 修复JWT异常处理，防止token验证错误日志污染
   - 优化未认证请求的处理流程

2. **🎯 智能答案匹配**
   - 填空题支持多答案格式：`答案1###答案2###答案3`
   - 多空题智能匹配：`固定答案，可变答案1###可变答案2`
   - 中英文逗号自动识别和转换
   - 大小写不敏感，自动去除空格

3. **📚 AI练习优化**
   - 科目列表动态加载（不再硬编码4个科目）
   - 成就徽章显示优化（修复转圈问题）
   - 完善答案判断逻辑，提升用户体验

4. **📖 文档完善**
   - 新增[填空题多答案智能匹配说明](document/填空题多答案智能匹配说明.md)
   - 包含详细使用示例和测试用例

---

## 核心特性

### 🤖 AI智能功能

- **智能出题**：基于文本、知识点自动生成题目
- **AI练习生成**：动态加载科目，智能生成个性化练习题
- **主观题AI批改**：自动评分、智能反馈（支持DeepSeek、通义千问）
- **智能答案匹配**：支持填空题多答案（单空/多空/中英文逗号通用）
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

## 📚 完整文档中心

> **📖 文档总数：60+ 份 | SQL脚本：5个 | 最后更新：2026-01-11**

所有项目文档已分类整理，存放在 `document/` 目录中，方便查阅和维护。

### 🎯 快速访问指南

**入口文档**：
- 📘 [文档快速访问指南](document/DOCS_GUIDE.md) - **推荐从这里开始**
- 📚 [完整文档索引](document/docs/README.md) - 60+份文档分类导航
- 🗄️ [SQL脚本说明](document/sql/README.md) - 数据库脚本文档

---

### 🔥 v3.0 最新文档（2026-01-11）

| 文档 | 说明 | 路径 |
|------|------|------|
| v3.0版本更新总结 | 完整版本发布文档 | [document/v3.0版本更新总结.md](document/v3.0版本更新总结.md) |
| 填空题智能匹配 | 多答案智能匹配功能 | [document/填空题多答案智能匹配说明.md](document/填空题多答案智能匹配说明.md) |

---

### 📂 文档分类导航

#### 🤖 AI功能模块 (`document/docs/ai/`)

**16个文档** | 核心：智能推荐、AI助手、流式响应

| 主要文档 | 说明 |
|---------|------|
| [AI智能推荐系统-完整文档](document/docs/ai/AI智能推荐系统-优秀毕业设计完整文档.md) | 完整的AI推荐系统设计 |
| [学生端AI助手优化](document/docs/ai/学生端AI助手优化总结-优秀毕业设计版.md) | AI学习助手功能 |
| [流式响应功能说明](document/docs/ai/流式响应功能说明.md) | 流式响应技术实现 |
| [DeepSeek集成指南](document/docs/DeepSeek集成指南.md) | AI API集成文档 |

**核心功能**：
- ✅ AI智能推荐（知识点+学习进度+错题分析）
- ✅ AI学习助手对话（DeepSeek API）
- ✅ 流式响应（前后端）
- ✅ 智能题目生成

---

#### 📚 课程学习模块 (`document/docs/course/`)

**4个文档** | 核心：视频播放、章节管理、进度追踪

| 主要文档 | 版本 |
|---------|------|
| [课程学习中心全面优化-最终版](document/docs/course/课程学习中心全面优化完成报告-v3.06-最终版.md) | v3.06 |
| [访客模式支持](document/docs/course/课程学习中心访客模式支持-v3.06.1.md) | v3.06.1 |
| [学生进度保存权限修复](document/docs/course/学生进度保存权限修复-v3.06.2.md) | v3.06.2 |

**核心功能**：
- ✅ 视频播放器（DPlayer，倍速/清晰度/断点续播）
- ✅ 章节树结构（无限层级）
- ✅ 学习进度追踪（自动保存）
- ✅ 文档查看器（PDF/PPT）
- ✅ 访客模式（游客预览）

---

#### 👨‍💼 管理端功能 (`document/docs/admin/`)

**7个文档** | 核心：课程管理、章节管理、数据分析

| 主要文档 | 版本 |
|---------|------|
| [管理端课程管理优化-完成报告](document/docs/admin/管理端课程管理优化完成报告-v3.07.md) | v3.07 ⭐最新 |
| [管理端课程管理-实施指南](document/docs/admin/管理端课程管理优化实施指南-v3.07.md) | v3.07 |
| [Dashboard修复方案](document/docs/admin/Dashboard修复方案-最终版.md) | 最终版 |

**核心功能**：
- ✅ 章节管理（树形结构、拖拽排序）
- ✅ 学习数据分析（学生进度/完成率/学习时长）
- ✅ 资源管理（视频/PDF/PPT/链接）
- ✅ Excel导出功能

---

#### ✏️ 在线练习模块 (`document/docs/practice/`)

**6个文档** | 核心：智能题目生成、AI批改、错因分析

| 主要文档 | 版本 |
|---------|------|
| [在线练习功能优化](document/docs/practice/在线练习功能优化实施总结-v3.10.md) | v3.10 |
| [AI批改接口实施](document/docs/practice/在线练习功能后端AI批改接口实施文档.md) | - |
| [错因深度分析](document/docs/practice/错因深度分析功能实施总结.md) | - |
| [知识对战优化](document/docs/practice/知识对战优化方案.md) | - |

**核心功能**：
- ✅ 智能题目生成（基于知识点）
- ✅ AI自动批改（DeepSeek API）
- ✅ 错因深度分析
- ✅ 知识对战（PK模式）
- ✅ 学习报告生成

---

#### 🏠 Portal导航页 (`document/docs/portal/`)

**12个文档** | 核心：学生首页、导航系统

| 主要文档 | 版本 |
|---------|------|
| [导航系统优化实施总结](document/docs/portal/导航系统优化实施总结-v3.03.md) | v3.03 |
| [Portal 8阶段优化](document/docs/portal/Portal导航页Phase1~8优化实施总结.md) | Phase 1-8 |

**优化阶段**：Phase 1-8 全面优化（UI/交互/性能/可视化）

---

#### 🔧 修复与部署 (`document/docs/fixes/` & `deployment/`)

**9个文档** | 核心：问题修复、部署指南

| 主要文档 | 说明 |
|---------|------|
| [紧急修复指南-更新版](document/docs/fixes/紧急修复指南-更新版.md) | 常见问题快速修复 |
| [403权限问题修复](document/docs/fixes/403权限问题修复说明.md) | 权限问题解决 |
| [编译问题解决方案](document/docs/deployment/编译问题解决方案.md) | 编译常见问题 |

---

#### 🗄️ SQL数据库脚本 (`document/sql/`)

**5个脚本** | 核心：完整数据库、功能表

| 脚本名称 | 说明 |
|---------|------|
| [exam_system.sql](document/sql/exam_system.sql) | 完整系统数据库 |
| [course-learning-optimization.sql](document/sql/course-learning-optimization.sql) | 课程学习优化表 |
| [grading-history-table.sql](document/sql/grading-history-table.sql) | 批阅历史表 |
| [notification-table.sql](document/sql/notification-table.sql) | 通知系统表 |

详细说明：[document/sql/README.md](document/sql/README.md)

---

### 🎯 按场景查找文档

#### 场景1：新人上手
1. [文档快速访问指南](document/DOCS_GUIDE.md)
2. [快速开始](#快速开始)
3. [Docker部署](#docker部署)
4. [编译问题解决](document/docs/deployment/编译问题解决方案.md)

#### 场景2：部署系统
1. [exam_system.sql](document/sql/exam_system.sql) - 数据库脚本
2. [SQL说明文档](document/sql/README.md)
3. [紧急修复指南](document/docs/fixes/紧急修复指南-更新版.md)

#### 场景3：AI功能开发
1. [AI智能推荐系统](document/docs/ai/AI智能推荐系统-优秀毕业设计完整文档.md)
2. [DeepSeek集成指南](document/docs/DeepSeek集成指南.md)
3. [流式响应功能](document/docs/ai/流式响应功能说明.md)

#### 场景4：课程功能开发
1. [课程学习中心-最终版](document/docs/course/课程学习中心全面优化完成报告-v3.06-最终版.md)
2. [管理端课程管理](document/docs/admin/管理端课程管理优化完成报告-v3.07.md)

#### 场景5：在线练习开发
1. [在线练习功能优化](document/docs/practice/在线练习功能优化实施总结-v3.10.md)
2. [AI批改接口](document/docs/practice/在线练习功能后端AI批改接口实施文档.md)
3. [填空题智能匹配](document/填空题多答案智能匹配说明.md) ⭐v3.0新增

---

### 📖 文档使用说明

1. **查找文档**：
   - 按功能模块：浏览 [document/docs/README.md](document/docs/README.md)
   - 按场景任务：参考 [document/DOCS_GUIDE.md](document/DOCS_GUIDE.md)
   - 按版本历史：查看下方[版本历史](#版本历史)

2. **文档命名规范**：
   - 完成报告：`功能名称-完成报告-版本号.md`
   - 实施指南：`功能名称-实施指南-版本号.md`
   - 优化方案：`功能名称-优化方案.md`

3. **版本说明**：
   - 版本号越大越新
   - 标记"最终版"是该功能的最新完整版本

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
├── exe-backend/           # 后端（Spring Boot）
│   ├── controller/        # 控制器（32个）
│   ├── service/           # 服务层（42个）
│   ├── entity/            # 实体类（37个）
│   └── mapper/            # Mapper（40个）
├── exe-frontend/          # 前端（Vue 3）
│   ├── views/             # 页面（35个）
│   ├── components/        # 组件（68个）
│   └── api/               # API（26个模块）
├── document/              # 📚 完整文档中心（60+份文档）
│   ├── DOCS_GUIDE.md      # 📘 文档快速访问指南
│   ├── v3.0版本更新总结.md # 📄 v3.0版本文档
│   ├── 填空题多答案智能匹配说明.md
│   ├── docs/              # 📂 功能文档分类（60+份）
│   │   ├── README.md      # 文档索引
│   │   ├── ai/            # 🤖 AI功能（16个文档）
│   │   ├── course/        # 📚 课程学习（4个文档）
│   │   ├── admin/         # 👨‍💼 管理端（7个文档）
│   │   ├── practice/      # ✏️ 在线练习（6个文档）
│   │   ├── portal/        # 🏠 导航页（12个文档）
│   │   ├── student/       # 👨‍🎓 学生端（3个文档）
│   │   ├── grading/       # 📝 批阅（1个文档）
│   │   ├── fixes/         # 🔧 修复（3个文档）
│   │   └── deployment/    # 🚀 部署（6个文档）
│   └── sql/               # 🗄️ SQL脚本（5个脚本）
│       ├── README.md      # SQL说明文档
│       └── exam_system.sql # 完整数据库
├── docs/                  # 旧版文档（兼容保留）
│   ├── api/               # API文档
│   ├── deployment/        # 部署文档
│   ├── optimization/      # 优化报告
│   └── database/          # 数据库脚本
└── docker-compose.yml     # Docker编排
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

### Q: JWT token验证错误？

如果看到 `MalformedJwtException` 错误：
- ✅ v3.0 已修复：添加异常处理，忽略无效token
- 检查前端是否正确设置 `Authorization` header
- 清除浏览器缓存，重新登录

### Q: 填空题多答案如何设置？

支持三种格式：
1. **单空多答案**：`cat###猫###feline`（任选其一正确）
2. **多空题（部分多答案）**：`法涅斯，旅行者###空###莹`
3. **中英文逗号通用**：系统自动识别并转换

详见：[填空题多答案智能匹配说明](document/填空题多答案智能匹配说明.md)

### Q: AI练习页面科目显示不全？

✅ v3.0 已修复：从硬编码改为动态API加载
- 刷新页面即可看到所有科目
- 科目数据来自 `/api/v1/student/subjects` 接口

---

## 版本历史

### v3.0 (2026-01-11)
- ✅ **JWT异常处理修复**：优化JWT token验证，防止MalformedJwtException错误
- ✅ **填空题智能匹配**：支持多空题、多答案、中英文逗号通用
  - 单空多答案：`cat###猫###feline`
  - 多空题：`法涅斯，旅行者###空###莹`
  - 中英文逗号自动转换
- ✅ **AI练习页面优化**：
  - 动态加载科目列表（不再硬编码）
  - 修复成就徽章转圈问题
  - 完善答案判断逻辑
- 📚 **文档完善**：[填空题多答案智能匹配说明](document/填空题多答案智能匹配说明.md)

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
