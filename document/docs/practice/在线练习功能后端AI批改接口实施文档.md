# 在线练习功能后端AI批改接口实施文档

## 一、实施概述

为在线练习功能实现了**流式AI批改接口**,支持主观题实时批改反馈。

**实施时间**: v3.10
**涉及文件**: 3个文件(新建)

---

## 二、创建的文件

### 2.1 GradingRequest.java (DTO)

**路径**: `exe-backend/src/main/java/com/ice/exebackend/dto/GradingRequest.java`

**用途**: AI批改请求数据传输对象

**字段说明**:
```java
public class GradingRequest {
    private Long questionId;          // 题目ID
    private Integer questionType;      // 题目类型(1-6)
    private String questionContent;    // 题目内容
    private String correctAnswer;      // 参考答案
    private String userAnswer;         // 学生答案
    private Integer maxScore;          // 题目满分(默认100)
}
```

---

### 2.2 AiServiceV3.java (新增方法)

**路径**: `exe-backend/src/main/java/com/ice/exebackend/service/AiServiceV3.java`

**新增方法**: `gradeSubjectiveQuestionStream()`

**方法签名**:
```java
public SseEmitter gradeSubjectiveQuestionStream(
    String apiKey,
    String providerKey,
    String questionContent,
    String referenceAnswer,
    String studentAnswer,
    int maxScore,
    Long userId
)
```

**核心特性**:
1. **流式响应**: 使用SSE实时推送批改内容
2. **限流保护**: 集成全局和用户级限流
3. **日志记录**: 完整的调用日志和性能统计
4. **错误处理**: 优雅的异常处理机制

**AI批改Prompt设计**:
```
系统提示词:
- 扮演公正严谨的阅卷老师
- 输出Markdown格式
- 包含: 评分、答题分析(优点+不足)、改进建议、知识点梳理
- 语气鼓励耐心

用户提示词:
- 题目内容
- 参考答案
- 题目满分
- 学生答案
```

**技术细节**:
- 超时时间: 90秒
- Temperature: 0.5 (降低随机性,批改更稳定)
- 事件类型: `message` (数据流), `done` (完成标记)
- 并发控制: 使用信号量控制全局并发

---

### 2.3 AiGradingController.java (Controller)

**路径**: `exe-backend/src/main/java/com/ice/exebackend/controller/AiGradingController.java`

**端点**: `POST /api/v1/ai/grading/stream`

**权限**: `@PreAuthorize("hasAuthority('ROLE_STUDENT')")`

**请求Headers**:
```
X-Ai-Api-Key: <用户的AI API Key>
X-Ai-Provider: deepseek | qwen (可选,默认deepseek)
Authorization: Bearer <学生Token>
```

**请求Body**:
```json
{
  "questionId": 123,
  "questionType": 5,
  "questionContent": "简述面向对象编程的三大特性",
  "correctAnswer": "封装、继承、多态...",
  "userAnswer": "封装、继承...",
  "maxScore": 100
}
```

**响应格式** (SSE流):
```
event: message
data: ## 评分

event: message
data: 得分：85/100分

event: message
data:

## 答题分析

event: message
data: ### 优点
- 正确列出了两个特性...

event: done
data:
```

**错误处理**:
- 学生信息不存在 → 返回错误
- 缺少API Key → 返回错误提示
- AI服务异常 → 通过SSE返回错误

**额外端点**:
- `GET /api/v1/ai/grading/health` - 健康检查

---

## 三、与前端集成

### 前端调用示例 (已实现)

**文件**: `exe-frontend/src/api/ai.ts`

**方法**: `analyzeAnswerStream()`

```typescript
export function analyzeAnswerStream(
  data: GradingRequest,
  onChunk: (text: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): void {
  const baseUrl = import.meta.env.VITE_APP_BASE_API || '';

  fetch(baseUrl + '/api/v1/ai/grading/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Ai-Api-Key': store.aiKey || '',
      'X-Ai-Provider': store.aiProvider || 'deepseek',
      'Authorization': 'Bearer ' + localStorage.getItem('studentToken')
    },
    body: JSON.stringify(data)
  })
  .then(response => {
    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    function read() {
      reader.read().then(({ done, value }) => {
        if (done) {
          onComplete();
          return;
        }

        const chunk = decoder.decode(value, { stream: true });
        const lines = chunk.split('\n');

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.substring(6).trim();
            if (data && data !== '[DONE]') {
              onChunk(data);
            }
          }
        }
        read();
      }).catch(error => onError(error));
    }
    read();
  })
  .catch(error => onError(error));
}
```

---

## 四、技术栈和依赖

### 已有依赖(无需新增):
- Spring Boot Web
- Spring Security
- AiServiceV3 (已实现流式响应框架)
- AiHttpClient (已实现sendStreamRequest方法)
- AiRateLimiter (限流器)
- AiCallLogService (调用日志)
- ObjectMapper (Jackson)

### 关键技术:
1. **SSE (Server-Sent Events)** - 服务端推送
2. **SseEmitter** - Spring提供的SSE封装
3. **异步线程** - 避免阻塞主线程
4. **限流保护** - 防止滥用
5. **安全认证** - Spring Security集成

---

## 五、测试建议

### 5.1 单元测试

```java
@Test
public void testStreamGrading() throws Exception {
    GradingRequest request = new GradingRequest();
    request.setQuestionContent("测试题目");
    request.setCorrectAnswer("正确答案");
    request.setUserAnswer("学生答案");
    request.setMaxScore(100);

    SseEmitter emitter = aiServiceV3.gradeSubjectiveQuestionStream(
        "test-key", "deepseek",
        request.getQuestionContent(),
        request.getCorrectAnswer(),
        request.getUserAnswer(),
        100,
        1L
    );

    assertNotNull(emitter);
}
```

### 5.2 集成测试

使用Postman或curl测试SSE端点:

```bash
curl -X POST http://localhost:8080/api/v1/ai/grading/stream \
  -H "Content-Type: application/json" \
  -H "X-Ai-Api-Key: sk-xxx" \
  -H "X-Ai-Provider: deepseek" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "questionId": 1,
    "questionType": 5,
    "questionContent": "简述Java多态性",
    "correctAnswer": "多态是指同一个方法调用...",
    "userAnswer": "多态就是...",
    "maxScore": 100
  }' \
  --no-buffer
```

### 5.3 前后端联调

1. 启动后端服务 (端口8080)
2. 启动前端服务 (端口5173)
3. 登录学生账号
4. 进入在线练习页面
5. 答题后点击"请AI批改"
6. 观察流式响应效果

---

## 六、性能指标

### 预期性能:
- **首字节时间**: <2秒
- **流式输出速度**: 实时(无缓冲)
- **完整批改时间**: 5-15秒
- **并发支持**: 50并发(可配置)
- **限流策略**:
  - 全局: 100请求/分钟
  - 单用户: 10请求/分钟

### 资源消耗:
- **内存**: 每个SSE连接约2MB
- **线程**: 每个请求独立线程
- **网络**: 流式传输,无大量缓冲

---

## 七、监控和日志

### 日志输出:

```
2026-01-11 10:23:45 INFO  收到AI批改请求: user=123, questionId=456, questionType=5, provider=deepseek
2026-01-11 10:23:45 INFO  开始流式AI主观题批改: provider=deepseek, user=123, maxScore=100
2026-01-11 10:23:58 INFO  流式AI主观题批改完成: user=123, duration=13245ms
```

### 调用日志表 (biz_ai_call_log):
自动记录每次批改调用:
- userId
- functionType: "grading"
- provider
- success/failure
- responseTime
- errorMessage (如有)

---

## 八、常见问题

### Q1: SSE连接超时怎么办?
**A**: 超时时间设置为90秒,足够AI生成批改内容。如需调整,修改:
```java
SseEmitter emitter = new SseEmitter(90000L); // 90秒
```

### Q2: 如何支持更多AI提供商?
**A**: 在AiServiceV3中已支持多个提供商,通过Header传入`X-Ai-Provider`即可切换。

### Q3: 如何防止恶意刷批改接口?
**A**: 已集成限流器:
- 全局限流: 防止系统过载
- 用户限流: 防止单用户滥用
- 并发控制: 限制同时处理的请求数

### Q4: 批改结果如何保存?
**A**: 当前接口仅提供实时批改,不保存结果。如需保存,可在前端将结果存储到LocalStorage或后续扩展保存到数据库。

---

## 九、后续优化建议

### 短期优化:
1. ✅ 基础流式批改 (已完成)
2. 📋 批改结果持久化 (待实现)
3. 📋 批改历史查询 (待实现)

### 长期优化:
1. 批改质量评分机制
2. 批改结果缓存 (相同答案复用)
3. 批改模板自定义
4. 批改报告导出(PDF)

---

## 十、代码统计

| 文件 | 新增代码 | 说明 |
|------|---------|------|
| GradingRequest.java | ~35行 | DTO定义 |
| AiServiceV3.java | ~120行 | 流式批改方法 |
| AiGradingController.java | ~110行 | 控制器 |
| **总计** | **~265行** | **纯Java后端代码** |

---

## 十一、部署说明

### 无需额外配置!

后端接口依赖的所有服务(AiServiceV3、限流器、日志服务等)均已在系统中配置完成,**开箱即用**。

### 验证部署:

```bash
# 检查健康状态
curl http://localhost:8080/api/v1/ai/grading/health

# 预期响应
{
  "code": 200,
  "msg": "success",
  "data": "AI批改服务运行正常"
}
```

---

## 十二、总结

本次实施完成了在线练习功能的**核心后端支撑**,为前端AI批改功能提供了:
1. ✅ 流式响应接口 - 实时批改体验
2. ✅ 完善的错误处理 - 稳定可靠
3. ✅ 限流和日志 - 生产级质量
4. ✅ 灵活的配置 - 支持多AI提供商

**与前端完美对接**,共同实现优秀毕业设计级别的在线练习功能! 🎉
