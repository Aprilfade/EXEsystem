# EXEsystem API文档 v3.06

**版本**：v3.06
**更新日期**：2026-03-06
**基础URL**：`http://localhost:8080/api/v1`

---

## 📋 目录

- [认证接口](#认证接口)
- [AI监控接口](#ai监控接口)
- [推荐系统接口](#推荐系统接口)
- [搜索接口](#搜索接口)
- [试卷管理接口](#试卷管理接口)
- [题目管理接口](#题目管理接口)
- [课程管理接口](#课程管理接口)
- [学生学习接口](#学生学习接口)
- [错误码说明](#错误码说明)

---

## 认证接口

### 1. 管理员登录

**接口**：`POST /auth/login`

**请求参数**：

```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应示例**：

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 2. 学生登录

**接口**：`POST /student/login`

**请求参数**：

```json
{
  "studentNo": "20210001",
  "password": "123456"
}
```

**响应示例**：

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "student": {
      "id": 1,
      "name": "张三",
      "studentNo": "20210001",
      "grade": "高一"
    }
  }
}
```

### 3. 获取当前用户信息

**接口**：`GET /auth/me`

**请求头**：
```
Authorization: Bearer {token}
```

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "user": {
      "id": 1,
      "username": "admin",
      "nickName": "管理员"
    },
    "permissions": [
      "sys:user:list",
      "sys:paper:create"
    ]
  }
}
```

---

## AI监控接口

### 1. 分页查询AI调用日志

**接口**：`GET /ai-monitor/logs`

**权限**：需要 `sys:stats:list` 权限

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | int | 否 | 当前页，默认1 |
| size | int | 否 | 每页大小，默认20 |
| userId | long | 否 | 用户ID |
| userType | string | 否 | 用户类型（ADMIN/STUDENT） |
| functionType | string | 否 | 功能类型 |
| provider | string | 否 | AI提供商（DEEPSEEK/CLAUDE/GEMINI） |
| success | boolean | 否 | 是否成功 |
| startTime | string | 否 | 开始时间（ISO格式） |
| endTime | string | 否 | 结束时间（ISO格式） |

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 10,
      "userType": "STUDENT",
      "functionType": "AI_CHAT",
      "provider": "DEEPSEEK",
      "success": true,
      "responseTime": 1250,
      "cached": false,
      "retryCount": 0,
      "tokensUsed": 580,
      "estimatedCost": 0.0029,
      "requestSummary": "学生提问：什么是函数",
      "createTime": "2026-03-06T10:30:00"
    }
  ],
  "total": 150
}
```

### 2. 导出AI调用日志（CSV） ⭐v3.06新增

**接口**：`GET /ai-monitor/export`

**权限**：需要 `sys:stats:list` 权限

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startTime | string | 否 | 开始时间（ISO格式） |
| endTime | string | 否 | 结束时间（ISO格式） |

**响应**：
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="ai_call_logs_20260306_103000.csv"`

**CSV格式**：
```csv
ID,用户ID,用户类型,功能类型,AI提供商,是否成功,响应时间(ms),是否缓存,重试次数,Token使用量,预估成本,错误信息,创建时间
1,10,STUDENT,AI_CHAT,DEEPSEEK,成功,1250,否,0,580,0.0029,,2026-03-06 10:30:00
```

### 3. 获取AI使用统计

**接口**：`GET /ai-monitor/stats`

**权限**：需要 `sys:stats:list` 权限

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| days | int | 否 | 统计天数，默认7 |

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "totalCalls": 1520,
    "successCalls": 1485,
    "successRate": 97.7,
    "avgResponseTime": 1150,
    "totalTokens": 125680,
    "totalCost": 628.4,
    "providerDistribution": {
      "DEEPSEEK": 1200,
      "CLAUDE": 250,
      "GEMINI": 70
    }
  }
}
```

### 4. 获取用户AI配额

**接口**：`GET /ai-monitor/quota/{userId}`

**权限**：`permitAll()`（用户自己可查询）

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "remaining": 85,
    "currentUsage": 15
  }
}
```

### 5. 健康检查

**接口**：`GET /ai-monitor/health`

**权限**：需要 `sys:stats:list` 权限

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "status": "UP",
    "rateLimiter": "enabled",
    "callLog": "enabled",
    "timestamp": 1709712600000
  }
}
```

---

## 推荐系统接口

### 1. 推荐题目

**接口**：`GET /recommendations/questions`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID |
| subjectId | long | 否 | 科目ID |
| limit | int | 否 | 推荐数量，默认10 |

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "questionId": 123,
      "content": "求函数f(x)=2x+1的反函数",
      "score": 8.5,
      "reason": "与你做过的题目相似度高，建议练习",
      "questionType": 2,
      "subjectName": "数学"
    }
  ]
}
```

### 2. 推荐课程 ⭐v3.06新增

**接口**：`GET /recommendations/courses`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID |
| limit | int | 否 | 推荐数量，默认5 |

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "courseId": 10,
      "name": "高中数学函数专题",
      "description": "深入讲解函数的概念、性质和应用",
      "score": 85.6,
      "reason": "该科目平均得分率58.5%，需要重点提升",
      "coverUrl": "https://example.com/cover.jpg"
    }
  ]
}
```

**推荐算法说明**：
- 分析用户最近3个月的考试成绩
- 按科目计算平均得分率
- 识别薄弱科目（得分率最低的前3个）
- 推荐对应科目的课程
- 推荐分数 = (100 - 平均得分率) × (0.7 + 0.3 × 置信度因子)
- 置信度因子 = min(1.0, 考试次数 / 5)

---

## 搜索接口

### 1. 自然语言搜索 ⭐v3.06优化

**接口**：`POST /search/natural-language`

**请求头**：
```
X-Ai-Api-Key: {your-api-key}
X-Ai-Provider: DEEPSEEK
```

**请求参数**：

```json
{
  "query": "我想学习函数相关的课程"
}
```

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "intent": "FIND_COURSE",
    "questions": [],
    "courses": [
      {
        "id": 10,
        "name": "高中数学函数专题",
        "relevance": 0.95
      },
      {
        "id": 15,
        "name": "微积分基础",
        "relevance": 0.78
      }
    ],
    "answer": null
  }
}
```

**意图类型**：
- `FIND_QUESTION` - 搜索题目
- `LEARN_CONCEPT` - 学习知识点
- `ASK_QUESTION` - 提问
- `FIND_COURSE` - 查找课程
- `CHECK_PROGRESS` - 查看进度

**相关度计算**：
- 课程名称匹配：权重80%
- 课程描述匹配：权重20%
- 返回Top 10最相关结果

---

## 试卷管理接口

### 1. 创建试卷

**接口**：`POST /papers`

**权限**：需要 `sys:paper:create` 权限

**请求参数**：

```json
{
  "name": "高一数学期中考试",
  "subjectId": 1,
  "description": "测试范围：函数、方程",
  "grade": "高一",
  "totalScore": 100,
  "paperType": 1,
  "status": 0,
  "groups": [
    {
      "name": "选择题",
      "score": 40,
      "questionIds": [1, 2, 3, 4]
    },
    {
      "name": "填空题",
      "score": 30,
      "questionIds": [10, 11, 12]
    }
  ]
}
```

**响应示例**：

```json
{
  "code": 200,
  "msg": "创建成功"
}
```

### 2. AI生成试卷（流式响应）

**接口**：`POST /papers/ai-generate-stream`

**权限**：需要 `sys:paper:create` 或 `sys:paper:list` 权限

**请求头**：
```
X-Ai-Api-Key: {your-api-key}
X-Ai-Provider: DEEPSEEK
```

**请求参数**：

```json
{
  "paperTitle": "高一数学函数专题测试",
  "subjectName": "数学",
  "knowledgePoints": "函数的概念，函数的性质，反函数",
  "difficultyDistribution": "简单30%，中等50%，困难20%",
  "questionTypes": "选择题10道，填空题5道，解答题3道",
  "totalScore": 100
}
```

**响应**：Server-Sent Events (SSE) 流式响应

```
data: {"type":"thinking","content":"正在分析知识点..."}

data: {"type":"generating","content":"正在生成选择题..."}

data: {"type":"question","content":{"questionType":1,"content":"下列函数中..."}}

data: {"type":"complete","content":"试卷生成完成"}
```

### 3. 分页查询试卷

**接口**：`GET /papers`

**权限**：需要 `sys:paper:list` 权限

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | int | 否 | 当前页，默认1 |
| size | int | 否 | 每页大小，默认10 |
| name | string | 否 | 试卷名称（模糊搜索） |
| subjectId | long | 否 | 科目ID |
| grade | string | 否 | 年级 |
| status | int | 否 | 状态（0-草稿，1-已发布） |

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "高一数学期中考试",
      "subjectId": 1,
      "grade": "高一",
      "totalScore": 100,
      "status": 1,
      "createTime": "2026-03-01T10:00:00"
    }
  ],
  "total": 50
}
```

### 4. 导出试卷（Word）

**接口**：`GET /papers/export/{id}`

**权限**：需要 `sys:paper:list` 权限

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| includeAnswers | boolean | 否 | 是否包含答案，默认false |

**响应**：
- Content-Type: `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
- 文件名：`{试卷名称}.docx`

### 5. 导出试卷（PDF）

**接口**：`GET /papers/export/pdf/{id}`

**权限**：需要 `sys:paper:list` 权限

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| includeAnswers | boolean | 否 | 是否包含答案，默认false |

**响应**：
- Content-Type: `application/pdf`
- 文件名：`{试卷名称}.pdf`

---

## 题目管理接口

### 1. 创建题目

**接口**：`POST /questions`

**权限**：需要 `sys:question:create` 权限

**请求参数**：

```json
{
  "content": "求函数f(x)=2x+1的反函数",
  "questionType": 2,
  "subjectId": 1,
  "grade": "高一",
  "difficulty": 2,
  "score": 5,
  "knowledgePointIds": [10, 15],
  "answer": "f^(-1)(x)=(x-1)/2",
  "description": "反函数的概念和求法"
}
```

**题目类型**：
- 1: 单选题
- 2: 填空题
- 3: 判断题
- 4: 简答题
- 5: 多选题

**难度等级**：
- 1: 简单
- 2: 中等
- 3: 困难

**响应示例**：

```json
{
  "code": 200,
  "msg": "创建成功",
  "data": {
    "id": 123
  }
}
```

### 2. 批量导入题目（Excel）

**接口**：`POST /questions/import`

**权限**：需要 `sys:question:create` 权限

**请求**：multipart/form-data

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | Excel文件（.xlsx） |

**响应示例**：

```json
{
  "code": 200,
  "msg": "导入成功",
  "data": {
    "successCount": 45,
    "failureCount": 2,
    "errors": [
      {
        "row": 10,
        "error": "题目内容不能为空"
      }
    ]
  }
}
```

### 3. 分页查询题目

**接口**：`GET /questions`

**权限**：需要 `sys:question:list` 权限

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | int | 否 | 当前页，默认1 |
| size | int | 否 | 每页大小，默认20 |
| content | string | 否 | 题目内容（模糊搜索） |
| subjectId | long | 否 | 科目ID |
| questionType | int | 否 | 题目类型 |
| difficulty | int | 否 | 难度等级 |
| knowledgePointId | long | 否 | 知识点ID |

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 123,
      "content": "求函数f(x)=2x+1的反函数",
      "questionType": 2,
      "subjectId": 1,
      "difficulty": 2,
      "score": 5,
      "knowledgePoints": [
        {
          "id": 10,
          "name": "反函数"
        }
      ],
      "createTime": "2026-03-01T10:00:00"
    }
  ],
  "total": 580
}
```

---

## 课程管理接口

### 1. 创建课程

**接口**：`POST /courses`

**权限**：需要 `sys:course:create` 权限

**请求参数**：

```json
{
  "name": "高中数学函数专题",
  "description": "深入讲解函数的概念、性质和应用",
  "subjectId": 1,
  "grade": "高一",
  "coverUrl": "https://example.com/cover.jpg",
  "teacherId": 5
}
```

**响应示例**：

```json
{
  "code": 200,
  "msg": "创建成功",
  "data": {
    "id": 10
  }
}
```

### 2. 添加课程章节

**接口**：`POST /courses/{courseId}/chapters`

**权限**：需要 `sys:course:update` 权限

**请求参数**：

```json
{
  "name": "第一章 函数的概念",
  "parentId": null,
  "orderNum": 1,
  "resources": [
    {
      "name": "函数的定义",
      "type": "VIDEO",
      "url": "https://example.com/video1.mp4",
      "orderNum": 1
    },
    {
      "name": "函数的性质",
      "type": "PDF",
      "url": "https://example.com/doc1.pdf",
      "orderNum": 2
    }
  ]
}
```

**资源类型**：
- `VIDEO`: 视频
- `PDF`: PDF文档
- `PPT`: PPT演示文稿
- `LINK`: 外部链接

**响应示例**：

```json
{
  "code": 200,
  "msg": "创建成功",
  "data": {
    "id": 25
  }
}
```

### 3. 查询课程列表

**接口**：`GET /courses`

**权限**：公开接口（访客可查看）

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | int | 否 | 当前页，默认1 |
| size | int | 否 | 每页大小，默认10 |
| name | string | 否 | 课程名称（模糊搜索） |
| subjectId | long | 否 | 科目ID |
| grade | string | 否 | 年级 |

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 10,
      "name": "高中数学函数专题",
      "description": "深入讲解函数的概念、性质和应用",
      "subjectId": 1,
      "grade": "高一",
      "coverUrl": "https://example.com/cover.jpg",
      "chapterCount": 10,
      "createTime": "2026-02-01T10:00:00"
    }
  ],
  "total": 25
}
```

---

## 学生学习接口

### 1. 保存学习进度

**接口**：`POST /student/progress`

**权限**：需要学生登录

**请求参数**：

```json
{
  "chapterId": 25,
  "progress": 85,
  "studyDuration": 1200
}
```

**响应示例**：

```json
{
  "code": 200,
  "msg": "保存成功"
}
```

### 2. 获取学习分析

**接口**：`GET /student/analytics`

**权限**：需要学生登录

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| days | int | 否 | 统计天数，默认30 |

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "studyTimeTrend": [
      {
        "date": "2026-03-01",
        "studyMinutes": 120,
        "questionCount": 15
      }
    ],
    "knowledgeMastery": [
      {
        "knowledgePointName": "函数的概念",
        "masteryRate": 85.5,
        "totalCount": 50,
        "correctCount": 43
      }
    ],
    "weakPoints": [
      {
        "knowledgePointName": "反函数",
        "subjectName": "数学",
        "scoreRate": 45.8,
        "wrongCount": 12,
        "recommendedPracticeCount": 20
      }
    ],
    "learningAdvice": "建议加强反函数的练习..."
  }
}
```

### 3. 获取错题本

**接口**：`GET /student/wrong-questions`

**权限**：需要学生登录

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | int | 否 | 当前页，默认1 |
| size | int | 否 | 每页大小，默认20 |
| subjectId | long | 否 | 科目ID |
| knowledgePointId | long | 否 | 知识点ID |

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 50,
      "questionId": 123,
      "questionContent": "求函数f(x)=2x+1的反函数",
      "wrongAnswer": "f^(-1)(x)=x-1",
      "correctAnswer": "f^(-1)(x)=(x-1)/2",
      "wrongReason": "计算错误",
      "createTime": "2026-03-05T15:30:00"
    }
  ],
  "total": 35
}
```

---

## 错误码说明

### 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名或密码错误 |
| 1002 | Token已过期 |
| 1003 | Token无效 |
| 2001 | 试卷不存在 |
| 2002 | 题目不存在 |
| 3001 | AI服务不可用 |
| 3002 | AI配额不足 |
| 4001 | 课程不存在 |
| 4002 | 章节不存在 |

### 响应格式

**成功响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    // 响应数据
  }
}
```

**错误响应**：

```json
{
  "code": 400,
  "msg": "请求参数错误",
  "data": null
}
```

---

## 请求头说明

### 通用请求头

```
Authorization: Bearer {token}
Content-Type: application/json
```

### AI相关请求头

```
X-Ai-Api-Key: {your-api-key}
X-Ai-Provider: DEEPSEEK | CLAUDE | GEMINI
```

---

## 分页说明

### 分页请求参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 当前页码 |
| size | int | 10-20 | 每页数量 |

### 分页响应格式

```json
{
  "code": 200,
  "data": [...],
  "total": 100
}
```

---

## 时间格式说明

### ISO 8601格式

```
2026-03-06T10:30:00
2026-03-06T10:30:00.123Z
```

### 日期格式

```
2026-03-06
```

---

## 附录

### Swagger在线文档

访问 `http://localhost:8080/swagger-ui.html` 查看完整的在线API文档。

### Postman Collection

下载 Postman Collection：[EXEsystem-v3.06.postman_collection.json](./postman/EXEsystem-v3.06.postman_collection.json)

### 测试环境

- **开发环境**：http://localhost:8080
- **测试环境**：http://test.exesystem.com
- **生产环境**：http://api.exesystem.com

---

<div align="center">

**EXEsystem API文档 v3.06**
更新日期：2026-03-06

[返回首页](../../README.md) | [查看更新日志](../../CHANGELOG.md)

</div>
