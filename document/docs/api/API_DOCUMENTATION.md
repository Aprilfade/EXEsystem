# EXEsystem API 文档

> 版本: v2.51
> 更新时间: 2026-01-08
> 基础URL: `http://localhost:8080`

---

## 📖 目录

- [概述](#概述)
- [认证方式](#认证方式)
- [通用响应格式](#通用响应格式)
- [管理端API](#管理端api)
- [学生端API](#学生端api)
- [WebSocket实时通信](#websocket实时通信)

---

## 概述

EXEsystem提供完整的RESTful API接口，支持在线考试、学习管理、AI智能批改等功能。

### 在线文档

访问Swagger UI查看交互式API文档：

```
http://localhost:8080/swagger-ui.html
```

---

## 认证方式

### JWT Token认证

在请求Header中添加：

```http
Authorization: Bearer <your_jwt_token>
```

### Token获取

**管理员登录：**

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**学生登录：**

```http
POST /api/student/auth/login
Content-Type: application/json

{
  "username": "2024001",
  "password": "123456"
}
```

**响应：**

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员"
    }
  }
}
```

---

## 通用响应格式

所有API返回统一的JSON格式：

```json
{
  "code": 200,
  "msg": "成功",
  "total": 100,
  "data": {}
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码（200=成功，400=失败） |
| msg | String | 响应消息 |
| total | Long | 总记录数（分页时使用） |
| data | Object/Array | 响应数据 |

---

## 管理端API

### 1. 科目管理

#### 获取科目列表

```http
GET /api/v1/subjects?current=1&size=10&name=数学
Authorization: Bearer <token>
```

**权限：** `sys:subject:list`

**响应：**

```json
{
  "code": 200,
  "total": 50,
  "data": [
    {
      "id": 1,
      "name": "数学",
      "description": "初中数学",
      "icon": "📐"
    }
  ]
}
```

#### 创建科目

```http
POST /api/v1/subjects
Authorization: Bearer <token>
```

**权限：** `sys:subject:create`

**请求体：**

```json
{
  "name": "物理",
  "description": "初中物理",
  "icon": "⚛️"
}
```

#### 其他接口

- `PUT /api/v1/subjects/{id}` - 更新科目
- `DELETE /api/v1/subjects/{id}` - 删除科目
- `GET /api/v1/subjects/all` - 获取所有科目（不分页）

---

### 2. 知识点管理

#### 获取知识点列表

```http
GET /api/v1/knowledge-points?subjectId=1&name=函数
```

**权限：** `sys:kp:list`

#### 批量更新知识点

```http
PUT /api/v1/knowledge-points/batch-update
```

**权限：** `sys:kp:update`

**请求体：**

```json
{
  "knowledgePointIds": [1, 2, 3],
  "subjectId": 2,
  "grade": "八年级",
  "tags": "重点"
}
```

#### 获取知识图谱

```http
GET /api/v1/knowledge-points/graph/{subjectId}
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "nodes": [
      {
        "id": "1",
        "realName": "一元一次方程",
        "symbolSize": 20
      }
    ],
    "links": [
      {
        "source": "1",
        "target": "2"
      }
    ]
  }
}
```

#### AI生成知识点

```http
POST /api/v1/knowledge-points/ai-generate
X-Ai-Api-Key: <your_api_key>
X-Ai-Provider: deepseek
```

**请求体：**

```json
{
  "text": "函数的概念：函数是描述两个变量之间依赖关系...",
  "count": 5
}
```

---

### 3. 题库管理

#### 获取题目列表

```http
GET /api/v1/questions?subjectId=1&questionType=1&content=函数
```

**权限：** `sys:question:list`

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| subjectId | Long | 科目ID |
| questionType | Integer | 题型（1=单选，2=多选，3=填空，4=判断，5=主观） |
| grade | String | 年级 |
| content | String | 题目内容搜索 |

#### 创建题目

```http
POST /api/v1/questions
```

**请求体：**

```json
{
  "subjectId": 1,
  "questionType": 1,
  "content": "下列哪个是质数？",
  "options": "A.4|B.7|C.9|D.10",
  "correctAnswer": "B",
  "score": 2,
  "difficulty": 2,
  "knowledgePointIds": [1, 2]
}
```

#### Excel导入/导出

```http
POST /api/v1/questions/import
Content-Type: multipart/form-data
```

```http
GET /api/v1/questions/export?subjectId=1
```

#### AI生成题目

```http
POST /api/v1/questions/ai-generate
X-Ai-Api-Key: <your_api_key>
```

---

### 4. 试卷管理

#### 创建试卷

```http
POST /api/v1/papers
```

**请求体：**

```json
{
  "subjectId": 1,
  "name": "期中考试",
  "totalScore": 100,
  "duration": 90,
  "questions": [
    {
      "questionId": 1,
      "score": 2,
      "sortOrder": 1
    }
  ]
}
```

#### 导出试卷

```http
GET /api/v1/papers/export/{id}        # Word格式
GET /api/v1/papers/export/pdf/{id}    # PDF格式
```

#### 获取知识点分布

```http
GET /api/v1/papers/{id}/knowledge-points
```

---

### 5. 学生管理

#### Excel导入学生

```http
POST /api/v1/students/import
Content-Type: multipart/form-data
```

#### 获取知识点掌握度

```http
GET /api/v1/students/{id}/knowledge-mastery?subjectId=1
```

**响应：**

```json
{
  "code": 200,
  "data": [
    {
      "knowledgePointName": "一元一次方程",
      "correctCount": 8,
      "totalCount": 10,
      "masteryRate": 80.0
    }
  ]
}
```

---

### 6. 成绩管理

#### 发布成绩

```http
POST /api/v1/exam-results/{id}/publish
```

#### 知识点成绩分析

```http
GET /api/v1/exam-results/{id}/knowledge-analysis
```

---

## 学生端API

### 学生仪表板

```http
GET /api/student/dashboard
Authorization: Bearer <student_token>
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "totalExams": 10,
    "avgScore": 85.5,
    "rank": 5,
    "points": 120,
    "wrongCount": 15
  }
}
```

### 开始考试

```http
POST /api/student/exams/{paperId}/start
```

### 提交答卷

```http
POST /api/student/exams/{resultId}/submit
```

**请求体：**

```json
{
  "answers": {
    "1": "B",
    "2": "A,C",
    "3": "主观题答案"
  }
}
```

### 智能复习推荐

```http
GET /api/student/smart-review
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "todayReview": [
      {
        "knowledgePointName": "一元一次方程",
        "reviewReason": "根据遗忘曲线，建议复习",
        "masteryRate": 70.0
      }
    ]
  }
}
```

### 加入知识竞技场

```http
POST /api/student/battle/join
```

**请求体：**

```json
{
  "subjectId": 1,
  "mode": "ranked"
}
```

### 每日签到

```http
POST /api/student/sign-in
```

---

## WebSocket实时通信

### 连接地址

```
ws://localhost:8080/ws/battle
```

### 连接示例

```javascript
const ws = new WebSocket('ws://localhost:8080/ws/battle');

ws.onopen = () => {
  // 发送JWT Token认证
  ws.send(JSON.stringify({
    type: 'AUTH',
    token: localStorage.getItem('token')
  }));
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log('收到消息：', message);
};
```

### 消息格式

#### 对战答题

```json
{
  "type": "BATTLE_ANSWER",
  "data": {
    "questionId": 1,
    "answer": "B",
    "isCorrect": true,
    "score": 10
  }
}
```

#### 系统通知

```json
{
  "type": "NOTIFICATION",
  "data": {
    "title": "新消息",
    "content": "您有新的作业",
    "time": "2024-01-08 10:00:00"
  }
}
```

---

## 速率限制

| 端点 | 限制 | 说明 |
|------|------|------|
| AI相关API | 100次/分钟 | 全局限制 |
| 登录API | 5次/分钟 | 单IP限制 |
| 其他API | 1000次/分钟 | 单用户限制 |

---

## 完整API列表

### 认证与用户管理（6个）

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 管理员登录 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/me` | 当前用户信息 |
| POST | `/api/student/auth/login` | 学生登录 |
| GET | `/api/v1/users` | 用户列表 |
| POST | `/api/v1/users` | 创建用户 |

### 科目管理（5个）

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/subjects` | 科目列表 |
| POST | `/api/v1/subjects` | 创建科目 |
| PUT | `/api/v1/subjects/{id}` | 更新科目 |
| DELETE | `/api/v1/subjects/{id}` | 删除科目 |
| GET | `/api/v1/subjects/all` | 所有科目 |

### 知识点管理（9个）

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/knowledge-points` | 知识点列表 |
| POST | `/api/v1/knowledge-points` | 创建知识点 |
| PUT | `/api/v1/knowledge-points/{id}` | 更新知识点 |
| DELETE | `/api/v1/knowledge-points/{id}` | 删除知识点 |
| PUT | `/api/v1/knowledge-points/batch-update` | 批量更新 |
| GET | `/api/v1/knowledge-points/global-stats` | 全局统计 |
| GET | `/api/v1/knowledge-points/graph/{subjectId}` | 知识图谱 |
| POST | `/api/v1/knowledge-points/relation` | 添加关系 |
| POST | `/api/v1/knowledge-points/ai-generate` | AI生成 |

### 题库管理（8个）

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/questions` | 题目列表 |
| POST | `/api/v1/questions` | 创建题目 |
| PUT | `/api/v1/questions/{id}` | 更新题目 |
| DELETE | `/api/v1/questions/{id}` | 删除题目 |
| POST | `/api/v1/questions/import` | Excel导入 |
| GET | `/api/v1/questions/export` | Excel导出 |
| PUT | `/api/v1/questions/batch-update` | 批量更新 |
| POST | `/api/v1/questions/ai-generate` | AI生成 |

### 试卷管理（7个）

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/papers` | 试卷列表 |
| POST | `/api/v1/papers` | 创建试卷 |
| PUT | `/api/v1/papers/{id}` | 更新试卷 |
| DELETE | `/api/v1/papers/{id}` | 删除试卷 |
| GET | `/api/v1/papers/export/{id}` | 导出Word |
| GET | `/api/v1/papers/export/pdf/{id}` | 导出PDF |
| GET | `/api/v1/papers/{id}/knowledge-points` | 知识点分布 |

### 学生管理（7个）

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/students` | 学生列表 |
| POST | `/api/v1/students` | 创建学生 |
| PUT | `/api/v1/students/{id}` | 更新学生 |
| DELETE | `/api/v1/students/{id}` | 删除学生 |
| POST | `/api/v1/students/import` | Excel导入 |
| GET | `/api/v1/students/export` | 导出数据 |
| GET | `/api/v1/students/{id}/knowledge-mastery` | 掌握度分析 |

### 成绩管理（6个）

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/exam-results` | 成绩列表 |
| GET | `/api/v1/exam-results/{id}` | 成绩详情 |
| POST | `/api/v1/exam-results` | 创建成绩 |
| POST | `/api/v1/exam-results/{id}/publish` | 发布成绩 |
| GET | `/api/v1/exam-results/{id}/knowledge-analysis` | 知识点分析 |
| GET | `/api/v1/exam-results/export` | 导出成绩 |

### 学生端功能（10个）

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/student/dashboard` | 学生仪表板 |
| GET | `/api/student/courses` | 课程列表 |
| POST | `/api/student/exams/{paperId}/start` | 开始考试 |
| POST | `/api/student/exams/{resultId}/submit` | 提交答卷 |
| GET | `/api/student/wrong-records` | 错题本 |
| GET | `/api/student/smart-review` | 智能复习 |
| POST | `/api/student/battle/join` | 加入对战 |
| POST | `/api/student/sign-in` | 每日签到 |
| GET | `/api/student/achievements` | 成就列表 |
| GET | `/api/student/goods` | 积分商城 |

---

## 错误代码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求失败/业务异常 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 技术支持

- **在线文档**: http://localhost:8080/swagger-ui.html
- **问题反馈**: https://github.com/Aprilfade/EXEsystem/issues

---

<div align="center">

© 2024-2026 EXEsystem Team

</div>
