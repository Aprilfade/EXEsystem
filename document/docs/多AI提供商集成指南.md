# 多AI提供商集成指南

## 📋 概述

EXEsystem现已支持多个主流AI提供商，提供更灵活的AI服务选择：

- ✅ **DeepSeek** - 性价比最高，适合大规模使用
- ✅ **Claude** (Anthropic) - 高质量响应，擅长推理和分析
- ✅ **Gemini** (Google) - 免费额度充足，适合开发测试

---

## 🎯 快速开始

### 1. 配置API密钥

编辑 `.env` 文件（复制自 `.env.example`）：

```bash
# DeepSeek API Key
VITE_DEEPSEEK_API_KEY=sk-your-deepseek-api-key-here

# Claude API Key
VITE_CLAUDE_API_KEY=sk-ant-your-claude-api-key-here

# Gemini API Key
VITE_GEMINI_API_KEY=your-gemini-api-key-here

# 默认提供商
VITE_DEFAULT_AI_PROVIDER=deepseek
```

### 2. 前端使用示例

```typescript
import { getAIClientManager } from '@/utils/aiClient'

// 使用默认提供商
const aiClient = getAIClientManager()

// 或指定提供商
const claudeClient = getAIClientManager({ provider: 'claude' })
const geminiClient = getAIClientManager({ provider: 'gemini' })

// 发送聊天请求
const response = await aiClient.chat([
  { role: 'user', content: '请解释一下二次函数' }
])

// 流式响应
await aiClient.chatStream(
  [{ role: 'user', content: '请解释一下二次函数' }],
  undefined,
  (chunk) => {
    console.log('收到片段:', chunk)
  }
)

// 使用便捷方法
const explanation = await aiClient.explainKnowledgePoint('二次函数', '初中')
const exercises = await aiClient.generateExercises('二次函数', 3, '中等')
const advice = await aiClient.getStudyAdvice('数学', ['二次函数', '一元二次方程'])
```

### 3. 动态切换提供商

```typescript
const aiClient = getAIClientManager()

// 获取当前提供商
console.log(aiClient.getProvider()) // 'deepseek'

// 切换到Claude
aiClient.switchProvider('claude', 'sk-ant-your-key')

// 切换到Gemini
aiClient.switchProvider('gemini', 'your-gemini-key')
```

---

## 🔧 后端配置

### application.yml 配置示例

```yaml
ai:
  enabled: true
  default-provider: deepseek

  providers:
    # DeepSeek配置（OpenAI兼容）
    deepseek:
      url: https://api.deepseek.com/v1/chat/completions
      model: deepseek-chat
      temperature: 0.7
      max-tokens: 2000
      enabled: true
      priority: 1

    # Claude配置（Anthropic格式）
    claude:
      url: https://api.anthropic.com/v1/messages
      model: claude-3-5-sonnet-20241022
      temperature: 0.7
      max-tokens: 2000
      enabled: true
      priority: 2

    # Gemini配置（Google格式）
    gemini:
      url: https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
      model: gemini-1.5-flash
      temperature: 0.7
      max-tokens: 2000
      enabled: true
      priority: 3

  cache:
    enabled: true
    ttl: 3600

  retry:
    enabled: true
    max-attempts: 3
    backoff: 1000
    multiplier: 2.0

  rate-limit:
    enabled: true
    max-concurrent: 10
    max-per-user: 2

  timeout:
    connect: 10
    analyze: 30
    generate: 60
    grading: 30
```

### 后端使用示例

```java
@Autowired
private AiProviderAdapter aiProviderAdapter;

// 使用DeepSeek
String response = aiProviderAdapter.sendRequest(
    apiKey,
    "deepseek",
    messages,
    0.7,
    30
);

// 使用Claude
String claudeResponse = aiProviderAdapter.sendRequest(
    apiKey,
    "claude",
    messages,
    0.7,
    30
);

// 使用Gemini
String geminiResponse = aiProviderAdapter.sendRequest(
    apiKey,
    "gemini",
    messages,
    0.7,
    30
);

// 流式请求
aiProviderAdapter.sendStreamRequest(
    apiKey,
    "claude",
    messages,
    0.7,
    30,
    chunk -> System.out.print(chunk),  // onChunk
    () -> System.out.println("完成"),  // onComplete
    error -> System.err.println(error) // onError
);
```

---

## 📊 AI提供商对比

### 1. DeepSeek

**优势：**
- ✅ 价格最便宜（输入¥0.001/千tokens，输出¥0.002/千tokens）
- ✅ OpenAI兼容API，易于集成
- ✅ 响应速度快
- ✅ 适合大规模部署

**适用场景：**
- 生产环境大规模使用
- 成本敏感型项目
- 需要高并发的应用

**获取API Key：**
https://platform.deepseek.com/api_keys

---

### 2. Claude (Anthropic)

**优势：**
- ✅ 高质量响应，推理能力强
- ✅ 上下文理解深入
- ✅ 擅长复杂问题分析
- ✅ 安全性和对齐性好

**定价：**
- Claude 3.5 Sonnet: 输入$3/百万tokens，输出$15/百万tokens
- Claude 3 Opus: 输入$15/百万tokens，输出$75/百万tokens

**适用场景：**
- 需要高质量回答的场景
- 复杂问题解答
- 教学辅导和学术分析
- 专业内容生成

**获取API Key：**
https://console.anthropic.com/settings/keys

---

### 3. Gemini (Google)

**优势：**
- ✅ 免费额度充足（每分钟15次请求）
- ✅ Google技术支持
- ✅ 多模态能力（文本+图像）
- ✅ 适合开发测试

**定价：**
- Gemini 1.5 Flash: 免费额度内免费
- Gemini 1.5 Pro: 输入$0.00125/千tokens，输出$0.005/千tokens

**适用场景：**
- 开发和测试环境
- 预算有限的项目
- 需要多模态功能
- 个人学习项目

**获取API Key：**
https://makersuite.google.com/app/apikey

---

## 🚀 高级功能

### 1. 自动降级

系统支持自动降级，当主提供商不可用时自动切换：

```typescript
const aiClient = getAIClientManager({
  provider: 'claude'
})

// 如果Claude不可用，会自动尝试其他提供商
// 降级顺序：claude -> gemini -> deepseek
```

### 2. 并发控制

```yaml
ai:
  rate-limit:
    max-concurrent: 10        # 全局最大并发
    max-per-user: 2          # 单用户最大并发
    max-per-minute: 100      # 每分钟最大请求
    max-per-user-per-minute: 10  # 单用户每分钟最大请求
```

### 3. 缓存机制

```yaml
ai:
  cache:
    enabled: true
    ttl: 3600              # 缓存1小时
    key-prefix: "ai:cache:"
```

### 4. 重试策略

```yaml
ai:
  retry:
    enabled: true
    max-attempts: 3        # 最多重试3次
    backoff: 1000         # 初始退避1秒
    multiplier: 2.0       # 指数退避倍数
```

---

## 💡 最佳实践

### 1. 成本优化

```typescript
// 开发环境使用Gemini（免费）
const devClient = getAIClientManager({
  provider: import.meta.env.DEV ? 'gemini' : 'deepseek'
})

// 复杂任务使用Claude，简单任务使用DeepSeek
async function getAIResponse(complexity: 'simple' | 'complex', prompt: string) {
  const provider = complexity === 'complex' ? 'claude' : 'deepseek'
  const client = getAIClientManager({ provider })
  return await client.chat([{ role: 'user', content: prompt }])
}
```

### 2. 错误处理

```typescript
import { getAIClientManager } from '@/utils/aiClient'

async function robustAICall(prompt: string) {
  const providers: AIProvider[] = ['deepseek', 'claude', 'gemini']

  for (const provider of providers) {
    try {
      const client = getAIClientManager({ provider })
      return await client.chat([{ role: 'user', content: prompt }])
    } catch (error) {
      console.error(`${provider} 调用失败:`, error)
      // 尝试下一个提供商
    }
  }

  throw new Error('所有AI提供商都不可用')
}
```

### 3. 流式响应优化

```typescript
async function streamResponse(prompt: string, onUpdate: (text: string) => void) {
  const client = getAIClientManager()

  let fullText = ''

  await client.chatStream(
    [{ role: 'user', content: prompt }],
    undefined,
    (chunk) => {
      fullText += chunk
      onUpdate(fullText)
    }
  )

  return fullText
}
```

---

## 🔍 故障排查

### 问题1：API Key无效

**症状：** 401 Unauthorized 错误

**解决方案：**
1. 检查.env文件中的API Key是否正确
2. 确认API Key没有过期
3. 检查是否有足够的配额
4. 尝试重新生成API Key

### 问题2：请求超时

**症状：** Timeout错误

**解决方案：**
```bash
# 增加超时时间
VITE_AI_TIMEOUT=60000  # 60秒
```

### 问题3：提供商不可用

**症状：** 503 Service Unavailable

**解决方案：**
1. 检查网络连接
2. 查看提供商状态页面
3. 启用自动降级功能
4. 使用备用提供商

---

## 📈 监控和日志

### 前端日志

```typescript
// 开启详细日志
const client = getAIClientManager()
console.log('当前提供商:', client.getProvider())

// 监控API调用
client.chat(messages).then(response => {
  console.log('响应长度:', response.length)
  console.log('响应时间:', Date.now() - startTime)
})
```

### 后端日志

查看 Spring Boot 日志：
- AI请求开始
- 重试次数
- 缓存命中
- 响应时间
- 错误信息

---

## 🎓 示例项目

查看完整示例：

1. **AI助手聊天** - `exe-frontend/src/views/student/AiTutor.vue`
2. **智能题目生成** - `exe-backend/src/main/java/com/ice/exebackend/service/AiServiceV3.java`
3. **AI批改** - `exe-frontend/src/views/student/AiPractice.vue`

---

## 📞 技术支持

遇到问题？

1. 查看[API文档](http://localhost:8080/swagger-ui.html)
2. 检查[故障排查](#故障排查)章节
3. 提交[Issue](https://github.com/your-repo/issues)

---

## 🎉 总结

通过集成多个AI提供商，EXEsystem实现了：

1. ✅ **灵活性** - 根据需求选择最合适的AI服务
2. ✅ **可靠性** - 自动降级确保服务可用
3. ✅ **成本优化** - 在质量和成本间灵活平衡
4. ✅ **易用性** - 统一接口，无缝切换

**开始使用吧！** 🚀

---

**文档版本：** v1.0
**最后更新：** 2026-03-06
**维护者：** EXEsystem Team
