# EXEsystem - 智能在线考试与学习管理系统

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.17-4FC08D.svg)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.3-3178C6.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1.svg)
![Redis](https://img.shields.io/badge/Redis-Latest-DC382D.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

**一个功能完整、技术先进、具有创新性的在线教育平台**

[功能特性](#功能特性) • [快速开始](#快速开始) • [技术栈](#技术栈) • [系统架构](#系统架构) • [项目截图](#项目截图)

</div>

---

## 项目简介

EXEsystem（Exam & Exercise System）是一个基于 Spring Boot 3 + Vue 3 的现代化在线考试与学习管理系统。项目采用前后端分离架构，集成了 **AI 智能批改**、**实时知识对战**、**智能复习推荐**等创新功能，旨在为师生提供高效、有趣的在线学习体验。

### 核心亮点

🤖 **AI 深度集成** - 接入 DeepSeek/通义千问大模型，实现智能出题、主观题自动批改、学习分析
⚔️ **知识竞技场** - 基于 WebSocket 的实时 1v1 答题对战，支持段位匹配、连击系统、技能释放
🧠 **智能复习** - 艾宾浩斯遗忘曲线算法，智能推荐复习时间，提升记忆效率
🎮 **游戏化学习** - 积分商城、成就系统、签到奖励、排行榜，让学习更有趣
📊 **数据可视化** - ECharts 图表展示学习数据，热力图、趋势图、知识点雷达图
🔐 **双端设计** - 管理端（教师/管理员）+ 学生端分离，权限精细控制

---

## 功能特性

### 管理端功能

#### 🔐 权限管理
- 基于 RBAC 的角色权限管理（超级管理员、普通管理员、教师）
- 菜单级、按钮级权限控制
- 登录日志、操作日志记录
- 用户管理（增删改查、禁用启用）

#### 📚 题库管理
- 支持 5 种题型：单选、多选、填空、判断、主观题
- 题目关联知识点（多对多）
- 图片上传（题干、选项、答案）
- Excel 批量导入导出
- 智能去重（基于 Levenshtein 距离算法）
- 多维度筛选（科目、年级、题型、难度）

#### 📄 试卷管理
- 手动组卷：拖拽排序、题目分组、自定义分值
- 图片试卷：上传试卷图片拼接
- 试卷预览与导出（PDF）
- 试卷统计分析

#### 👨‍🎓 学生管理
- 学生信息管理（批量导入）
- 成绩管理与统计
- 学习数据分析（知识点掌握度、错题分布）

#### 🏫 班级与作业
- 班级创建（6 位邀请码）
- 作业布置与批改
- 作业统计与分析

#### 🎓 课程管理
- 课程创建与编辑
- 多类型资源上传（视频、PDF、PPT、链接）
- 学生评论与讨论

#### 📊 数据统计
- 实时数据大屏（ECharts 可视化）
- 知识点掌握度统计
- 学生成绩分布
- 错题统计与分析

---

### 学生端功能

#### 📝 在线考试
- 模拟考试（倒计时、自动提交）
- 防切屏检测（违规记录）
- 实时答题进度
- 客观题自动判分
- 主观题 AI 智能批改（可选）

#### 📖 错题管理
- 自动收录错题
- 错题本查看（按科目、知识点分类）
- 错题重做
- 掌握状态标记

#### 🧠 智能复习
- 基于艾宾浩斯遗忘曲线的复习推荐
- 自动计算下次复习时间
- 复习进度跟踪
- 知识点掌握度可视化

#### ⚔️ 知识竞技场
- 实时 1v1 答题对战
- 段位系统（铁-青铜-白银-黄金-铂金-翡翠-钻石-大师-宗师-王者）
- 连击奖励（Combo）
- 技能系统（迷雾、时间加速、换题）
- ELO 积分算法
- 全服排行榜

#### 🎁 积分与成就
- 每日签到奖励
- 答题获得积分
- 成就系统（连续签到、刷题数量、满分试卷）
- 积分商城（虚拟头像框、背景）

#### 📊 学习数据
- 答题热力图（按时间、星期分布）
- 正确率趋势图
- 知识点雷达图
- 学习时长统计

#### 🎓 课程学习
- 浏览课程资源
- 在线观看视频
- 评论与讨论

---

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.4 | 核心框架 |
| Java | 17 | 开发语言 |
| MyBatis Plus | 3.5.9 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | Latest | 缓存 + 消息队列 |
| Spring Security | 6.x | 安全框架 |
| JWT | - | 无状态认证 |
| WebSocket | - | 实时通信 |
| Lombok | 1.18.36 | 简化代码 |
| Hutool | 5.8.34 | 工具库 |
| Apache POI | 5.3.0 | Excel 处理 |
| iText PDF | 5.5.13.4 | PDF 生成 |
| EasyExcel | 4.1.0 | Excel 导入导出 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.17 | 渐进式框架 |
| TypeScript | 5.8.3 | 类型安全 |
| Vite | 7.0.4 | 构建工具 |
| Element Plus | 2.10.5 | UI 组件库 |
| Pinia | 3.0.3 | 状态管理 |
| Vue Router | 4.5.1 | 路由管理 |
| Axios | 1.11.0 | HTTP 客户端 |
| ECharts | 5.6.0 | 数据可视化 |
| GSAP | 3.13.0 | 动画库 |
| Markdown-it | 14.1.0 | Markdown 渲染 |
| Vuedraggable | 4.1.0 | 拖拽组件 |

### AI 集成

- **DeepSeek API** - 智能出题、主观题批改
- **通义千问 API** - 学习分析、智能推荐

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                          用户层                              │
│         管理端（教师/管理员）    学生端（学生）              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                       前端层（Vue 3）                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ 管理后台页面  │  │ 学生端页面    │  │ 公共组件库    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────────────────────────────────────────┐      │
│  │       Pinia 状态管理 + Vue Router 路由            │      │
│  └──────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              ↓
                         Axios HTTP
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   后端层（Spring Boot 3）                    │
│  ┌──────────────────────────────────────────────────┐      │
│  │         Spring Security + JWT 认证过滤器          │      │
│  └──────────────────────────────────────────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Controller   │  │ WebSocket    │  │ AOP 日志切面  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────────────────────────────────────────┐      │
│  │                   Service 业务层                  │      │
│  │  题库管理 | 试卷管理 | 考试管理 | AI服务 | 对战管理 │      │
│  └──────────────────────────────────────────────────┘      │
│  ┌──────────────────────────────────────────────────┐      │
│  │         MyBatis Plus DAO 数据访问层               │      │
│  └──────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                        数据层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ MySQL 8.0+   │  │ Redis Cache  │  │ 文件存储      │      │
│  │ (36张表)     │  │ + Pub/Sub    │  │ (uploads/)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      外部服务层                              │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │ DeepSeek API │  │ 通义千问 API  │                        │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 环境要求

### 开发环境

- **操作系统**：Windows / macOS / Linux
- **JDK**：17 或更高版本
- **Node.js**：18.x 或更高版本
- **MySQL**：8.0 或更高版本
- **Redis**：最新稳定版
- **Maven**：3.8+ （或使用 IDEA 内置）
- **IDE**：IntelliJ IDEA / VS Code

### 生产环境

- **服务器**：2核4G 或更高配置
- **带宽**：5Mbps 或更高
- **存储**：20GB 或更高

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/EXEsystem.git
cd EXEsystem
```

### 2. 数据库初始化

#### 2.1 创建数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE exe_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2.2 导入表结构

```bash
mysql -u root -p exe_system < 建表语句.txt
```

#### 2.3 配置数据库连接

> 💡 **详细配置说明请查看：[CONFIG.md](CONFIG.md)**

编辑 `exe-backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password  # ⚠️ 请修改为你的MySQL密码

jwt:
  secret: your_jwt_secret_key_base64_encoded  # ⚠️ 请修改JWT密钥（详见CONFIG.md）
  expiration: 86400000
```

> ⚠️ **注意**：数据库名称为 `exam_system`，请确保与你创建的数据库名称一致。

### 3. 启动 Redis

**Windows（使用 WSL 或 Redis for Windows）：**
```bash
redis-server
```

**macOS/Linux：**
```bash
redis-server /usr/local/etc/redis.conf
```

**Docker 方式（推荐）：**
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

### 4. 启动后端

#### 方法一：使用 IDEA

1. 使用 IntelliJ IDEA 打开 `exe-backend` 目录
2. 等待 Maven 依赖下载完成
3. 找到 `ExeBackendApplication.java` 主类
4. 右键 → Run 'ExeBackendApplication'

#### 方法二：使用命令行

```bash
cd exe-backend
mvn clean install
mvn spring-boot:run
```

后端启动成功后访问：http://localhost:8080

### 5. 启动前端

```bash
cd exe-frontend
npm install          # 安装依赖
npm run dev          # 启动开发服务器
```

前端启动成功后访问：http://localhost:5173

### 6. 默认账号

#### 管理员账号
- 用户名：`admin`
- 密码：`123456`

#### 学生账号
- 学号：`2021001`（需先通过管理端创建）
- 密码：`123456`

> 注意：首次使用请先通过管理端创建学生账号，或使用 `generate_data.py` 批量生成测试数据。

---

## 项目结构

```
EXEsystem/
├── exe-backend/                 # 后端项目（Spring Boot）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ice/exebackend/
│   │   │   │   ├── annotation/         # 自定义注解
│   │   │   │   ├── aspect/            # AOP 切面
│   │   │   │   ├── common/            # 通用返回类
│   │   │   │   ├── config/            # 配置类
│   │   │   │   ├── controller/        # 控制器（30个）
│   │   │   │   ├── service/           # 业务逻辑
│   │   │   │   ├── mapper/            # MyBatis Mapper
│   │   │   │   ├── entity/            # 实体类（35个）
│   │   │   │   ├── dto/               # 数据传输对象
│   │   │   │   ├── enums/             # 枚举类
│   │   │   │   ├── handler/           # 异常处理器
│   │   │   │   └── utils/             # 工具类
│   │   │   └── resources/
│   │   │       ├── mapper/            # MyBatis XML
│   │   │       ├── application.yml    # 主配置
│   │   │       ├── application-dev.yml # 开发环境配置
│   │   │       └── application-prod.yml # 生产环境配置
│   │   └── test/                      # 测试代码
│   └── pom.xml                        # Maven 依赖
│
├── exe-frontend/                # 前端项目（Vue 3）
│   ├── src/
│   │   ├── api/                # API 接口封装（24个）
│   │   ├── components/         # 可复用组件（20+个）
│   │   ├── views/              # 页面视图（34个）
│   │   │   ├── *.vue           # 管理端页面（22个）
│   │   │   └── student/        # 学生端页面（12个）
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── utils/              # 工具函数
│   │   ├── App.vue             # 根组件
│   │   └── main.ts             # 入口文件
│   ├── public/                 # 静态资源
│   ├── index.html              # HTML 模板
│   ├── vite.config.ts          # Vite 配置
│   ├── tsconfig.json           # TypeScript 配置
│   └── package.json            # npm 依赖
│
├── uploads/                    # 文件上传目录
├── 建表语句.txt                 # 数据库DDL脚本
├── exam_stress_test.jmx        # JMeter 压力测试
├── generate_data.py            # 测试数据生成脚本
├── export_code.py              # 代码导出工具
└── README.md                   # 项目说明文档
```

---

## 核心功能详解

### 1. AI 智能批改系统

**技术实现：**
- 调用 DeepSeek/通义千问 API
- 支持流式响应
- 智能评分 + 详细反馈

**代码位置：**
- 后端：`exe-backend/src/main/java/com/ice/exebackend/service/AiService.java`
- 前端：`exe-frontend/src/views/TextToQuiz.vue`

**功能特点：**
- 自动评分（0-满分）
- 给出改进建议
- 参考答案对比
- 节省教师 70% 批改时间

**使用示例：**
```java
AiGradingResult result = aiService.gradeSubjectiveQuestion(
    apiKey,
    "deepseek",
    "请简述冒泡排序的原理",
    "冒泡排序是通过相邻元素比较和交换...",
    studentAnswer,
    10
);
```

---

### 2. 知识竞技场（实时对战）

**技术实现：**
- WebSocket 双向通信
- Redis 分布式匹配队列
- Redis Pub/Sub 消息订阅
- 段位 ELO 积分算法

**代码位置：**
- 后端：`exe-backend/src/main/java/com/ice/exebackend/service/BattleGameManager.java`
- 前端：`exe-frontend/src/views/student/Battle.vue`
- 状态管理：`exe-frontend/src/stores/battle.ts`

**游戏机制：**

| 功能 | 说明 |
|------|------|
| 匹配系统 | 相近段位优先匹配（±1段位） |
| 答题时间 | 每题 20 秒倒计时 |
| 连击系统 | 连续答对加成（Combo x2, x3...） |
| 技能系统 | 迷雾（隐藏对手进度）、时间加速、换题 |
| 积分算法 | 基于 ELO 算法，胜利+30分，失败-15分 |
| 段位晋升 | 每 100 分晋升一个段位 |

**段位体系：**
```
铁 (0-99) → 青铜 (100-199) → 白银 (200-299) → 黄金 (300-399)
→ 铂金 (400-499) → 翡翠 (500-599) → 钻石 (600-699)
→ 大师 (700-799) → 宗师 (800-899) → 王者 (900+)
```

---

### 3. 智能复习系统

**算法原理：** 艾宾浩斯遗忘曲线

**复习间隔：**
```
第 1 次复习：1 天后
第 2 次复习：3 天后
第 3 次复习：7 天后
第 4 次复习：15 天后
第 5 次复习：标记为已掌握
```

**代码位置：**
- 后端：`exe-backend/src/main/java/com/ice/exebackend/controller/StudentReviewController.java`
- 前端：`exe-frontend/src/views/student/SmartReview.vue`

**功能特点：**
- 自动计算下次复习时间
- 记录复习次数
- 掌握状态标记
- 智能推荐待复习题目

---

### 4. 权限管理系统（RBAC）

**数据库表：**
```
sys_user（用户表）
    ↓
sys_user_role（用户角色关联）
    ↓
sys_role（角色表）
    ↓
sys_role_permission（角色权限关联）
    ↓
sys_permission（权限表）
```

**角色类型：**
- 超级管理员（SUPER_ADMIN）
- 普通管理员（ADMIN）
- 教师（TEACHER）

**权限粒度：**
- 菜单权限（控制侧边栏显示）
- 按钮权限（控制操作按钮）
- 数据权限（控制数据范围）

---

## 压力测试

项目包含 JMeter 压力测试脚本：`exam_stress_test.jmx`

**测试场景：**
- 模拟 100 个并发学生
- 同时提交试卷
- 10 秒启动时间

**运行测试：**
```bash
jmeter -n -t exam_stress_test.jmx -l result.jtl
```

---

## 测试数据生成

使用 Python 脚本批量生成测试数据：

```bash
python generate_data.py
```

**生成内容：**
- 100 个测试学生
- 学号：2021001 ~ 2021100
- 默认密码：123456

---

## 部署指南

### Docker 部署（推荐）

#### 1. 构建后端镜像

```dockerfile
# exe-backend/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
cd exe-backend
mvn clean package -DskipTests
docker build -t exe-backend:latest .
```

#### 2. 构建前端镜像

```dockerfile
# exe-frontend/Dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
```

```bash
cd exe-frontend
docker build -t exe-frontend:latest .
```

#### 3. Docker Compose 启动

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: exe_system
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./建表语句.txt:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:latest
    ports:
      - "6379:6379"

  backend:
    image: exe-backend:latest
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod

  frontend:
    image: exe-frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

```bash
docker-compose up -d
```

---

### 传统部署

#### 后端部署

```bash
cd exe-backend
mvn clean package -DskipTests
java -jar target/exe-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### 前端部署

```bash
cd exe-frontend
npm run build
# 将 dist 目录部署到 Nginx
```

**Nginx 配置示例：**

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # 代理后端API
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket 代理
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 常见问题

### 1. 后端启动失败

**问题：** `com.mysql.cj.jdbc.exceptions.CommunicationsException`

**解决：**
- 检查 MySQL 是否启动
- 检查数据库连接配置（application-dev.yml）
- 确保数据库名称正确

---

### 2. Redis 连接失败

**问题：** `Unable to connect to Redis`

**解决：**
```bash
# 检查 Redis 是否运行
redis-cli ping  # 应返回 PONG

# 如未启动，启动 Redis
redis-server
```

---

### 3. 前端跨域问题

**问题：** `Access-Control-Allow-Origin`

**解决：**
- 后端已配置 CORS（CorsConfig.java）
- 确保前端 API 地址正确（.env.development）

---

### 4. AI 功能无法使用

**问题：** `AI API 调用失败`

**解决：**
- 确保已配置 AI API Key
- 检查网络连接
- 查看后端日志确认错误详情

---

## 开发路线图

### 已完成功能

- [x] 基础权限管理
- [x] 题库管理（5 种题型）
- [x] 试卷管理（手动组卷 + 图片试卷）
- [x] 在线考试（防切屏检测）
- [x] 错题本管理
- [x] AI 智能批改
- [x] 知识竞技场
- [x] 智能复习系统
- [x] 积分商城
- [x] 成就系统
- [x] 班级作业管理
- [x] 课程管理
- [x] 数据统计与可视化

### 计划中功能

- [ ] 语音答题（语音识别）
- [ ] 视频监控（考试防作弊）
- [ ] 移动端 App（React Native）
- [ ] 微信小程序
- [ ] 智能推荐题目（协同过滤算法）
- [ ] 学习社区（论坛、问答）
- [ ] 直播授课功能
- [ ] AI 学习助手（ChatBot）

---

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- 后端：遵循阿里巴巴 Java 开发手册
- 前端：遵循 Vue 3 官方风格指南
- 提交信息：使用语义化提交（Conventional Commits）

---

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 联系方式

- 项目作者：[Your Name]
- 邮箱：your.email@example.com
- GitHub：[@yourusername](https://github.com/yourusername)

---

## 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis Plus](https://baomidou.com/)
- [ECharts](https://echarts.apache.org/)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！⭐**

Made with ❤️ by [Your Name]

</div>
