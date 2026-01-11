# AI家教功能 - DeepSeek集成指南

## ✅ 已完成的优化

### 1. 真实AI模型接入
已将假的随机回复替换为真实的DeepSeek AI模型，现在AI家教具备真正的智能问答能力。

## 📦 新增文件

### 核心工具类
- `exe-frontend/src/utils/deepseek.ts` - DeepSeek AI客户端封装
- `exe-frontend/src/api/aiTutor.ts` - AI家教API接口

### 功能说明
```typescript
// DeepSeek客户端提供以下核心功能：

1. explainKnowledgePoint() - AI讲解知识点
2. tutorExercise() - AI辅导习题
3. generateExercises() - AI生成练习题
4. getStudyAdvice() - AI学习建议
5. answerQuestion() - AI答疑解惑
6. chatStream() - 流式AI对话（实时显示）
```

## 🔧 配置步骤

### 第一步：获取DeepSeek API Key

1. 访问 https://platform.deepseek.com/api_keys
2. 注册/登录账号
3. 创建新的API Key
4. 复制API Key（格式：`sk-xxxxxxxxxxxxxxxxxxxxxxxxx`）

### 第二步：配置环境变量

在项目根目录创建 `.env` 文件（或修改现有文件）：

```bash
# DeepSeek API配置
VITE_DEEPSEEK_API_KEY=sk-your-actual-api-key-here

# 可选配置
VITE_AI_TIMEOUT=30000
VITE_AI_STREAM=true
```

**注意：**
- 不要将 `.env` 文件提交到Git仓库
- `.env.example` 已包含配置模板，可以复制后修改

### 第三步：安装依赖并运行

```bash
cd exe-frontend
npm install
npm run dev
```

## 💰 费用说明

### DeepSeek定价（截至2025年1月）
- **输入Token**: ¥0.001 / 千tokens（非常便宜！）
- **输出Token**: ¥0.002 / 千tokens

### 毕业设计预算估算
假设答辩演示需要：
- 知识点讲解：20次 × 500 tokens = 10,000 tokens
- 习题辅导：30次 × 300 tokens = 9,000 tokens
- AI问答：50次 × 400 tokens = 20,000 tokens
- 总计：约 40,000 tokens ≈ **¥0.08元**

**结论：** 100元预算可以支持超过1000次的AI交互，对于毕业设计演示完全足够！

## 🎯 使用示例

### 1. AI答疑功能（已集成到AiTutor.vue）

```typescript
// 原来的假AI（已废弃）
setTimeout(() => {
  const answers = ['这是一个很好的问题！...'];
  qaHistory.value.push({ question, answer: answers[random] });
}, 1000);

// 新的真实AI
const answer = await answerQuestion({
  question: '什么是函数的单调性？',
  context: '当前正在学习：第1章 函数基础',
  subject: '数学'
});
// 返回：详细的、针对性的AI解答
```

### 2. 知识点讲解（推荐在章节详情页添加）

```typescript
import { explainKnowledgePoint } from '@/api/aiTutor';

const explainWithAI = async (kpName: string) => {
  try {
    const explanation = await explainKnowledgePoint({
      kpName: '函数的单调性',
      grade: '高一',
      subject: '数学'
    });
    // 返回：Markdown格式的详细讲解，包括概念、例子、重点
  } catch (error) {
    ElMessage.error('AI讲解失败');
  }
};
```

### 3. 习题辅导（错题时触发）

```typescript
import { tutorExercise } from '@/api/aiTutor';

// 当学生答错时
if (!exercise.correct) {
  const tutoring = await tutorExercise({
    question: exercise.question,
    studentAnswer: exercise.userAnswer,
    correctAnswer: exercise.answer,
    subject: '数学'
  });
  // 返回：耐心的错误分析和解题指导
}
```

### 4. 流式响应（更好的用户体验）

```typescript
import { explainKnowledgePointStream } from '@/api/aiTutor';

let fullAnswer = '';
for await (const chunk of explainKnowledgePointStream({
  kpName: '二次函数',
  grade: '初三',
  subject: '数学'
})) {
  fullAnswer += chunk;
  // 实时更新UI显示，就像ChatGPT那样一个字一个字出现
}
```

## 🐛 常见问题

### Q1: 提示"DeepSeek API Key未配置"
**A:** 检查 `.env` 文件是否正确配置，确保文件名是 `.env` 而不是 `.env.txt`

### Q2: API调用失败
**A:** 可能的原因：
- API Key错误或过期
- 网络问题（DeepSeek服务器在国内可直接访问）
- 账户余额不足（充值最低1元）

### Q3: 响应速度慢
**A:**
- DeepSeek的响应速度通常在1-3秒
- 可以使用流式响应提升用户体验
- 添加loading状态提示

### Q4: 如何在答辩时演示AI功能？
**A:**
1. 提前测试所有AI功能，确保API Key有效
2. 准备好几个典型问题的演示
3. 可以录屏作为备份（防止网络问题）
4. 强调使用的是DeepSeek模型（国产AI，论文可加分）

## 📊 答辩展示建议

### 技术亮点说明
1. **AI模型选择**
   - "我选择了DeepSeek模型，这是国产的大语言模型"
   - "相比GPT，DeepSeek的成本更低，响应速度更快"

2. **实现方式**
   - "我封装了统一的AI客户端，支持多种场景"
   - "实现了流式响应，用户体验类似ChatGPT"
   - "添加了错误处理和重试机制"

3. **实际应用**
   - "AI可以根据学生的错误答案给出针对性辅导"
   - "支持知识点讲解、习题生成、学习建议等多种功能"
   - "所有AI回复都考虑了上下文，更加智能"

## 🔜 后续优化建议

### 已实现 ✅
- [x] 接入真实DeepSeek AI模型
- [x] AI答疑功能
- [x] 错误处理和用户提示

### 待实现（P2优先级）
- [ ] AI知识点讲解按钮
- [ ] 错题AI辅导功能
- [ ] AI生成练习题
- [ ] AI学习建议（基于学习数据）
- [ ] 流式响应UI优化

### 可选（P3优先级）
- [ ] AI语音对话
- [ ] AI绘制知识图谱
- [ ] AI批改作文
- [ ] AI个性化学习路径规划

## 📝 文件清单

```
exe-frontend/
├── src/
│   ├── utils/
│   │   └── deepseek.ts          # DeepSeek AI客户端（新增）
│   ├── api/
│   │   └── aiTutor.ts           # AI家教API接口（新增）
│   └── views/
│       └── student/
│           └── AiTutor.vue      # 已更新：使用真实AI
├── .env                          # 环境变量配置（需创建）
└── .env.example                  # 环境变量模板（已更新）
```

## 🎓 毕业论文撰写建议

### 可以写入论文的内容

#### 3.x 系统架构设计
```
本系统采用了前后端分离架构，在AI家教模块中集成了DeepSeek大语言模型。
通过封装统一的AI客户端，实现了知识点讲解、习题辅导、智能问答等功能。
```

#### 4.x 关键技术实现
```
4.x.1 DeepSeek API集成
本系统采用DeepSeek API实现智能教学功能。DeepSeek是由国内团队开发的
大语言模型，在中文理解和数学推理方面表现优异。

核心实现：
1. 封装DeepSeekClient类，统一管理API调用
2. 实现流式响应，提升用户交互体验
3. 根据不同教学场景设计专门的Prompt工程
4. 添加错误处理和降级方案

代码示例：[可以贴上关键代码]
```

#### 5.x 系统测试
```
5.x.1 AI功能测试
测试场景：学生提问"什么是函数的单调性？"
预期结果：AI给出详细的概念解释、例子和应用
实际结果：[截图展示AI回复]
结论：AI回复准确、详细，符合教学要求
```

---

**创建时间：** 2025-01-10
**版本：** v1.0
**更新日志：**
- v1.0: 初始版本，完成DeepSeek AI集成
