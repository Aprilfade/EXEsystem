# AI功能优化实施指南

> 版本：v3.03
> 日期：2026-01-10

---

## 📋 已创建文件清单

### 文档
- ✅ `学生端AI功能优化方案.md` - 完整的优化方案文档

### 后端代码
- ✅ `RecommendationService.java` - 智能推荐服务（协同过滤算法）
- ✅ `RecommendationController.java` - 推荐API控制器

### 前端代码
- ✅ `RecommendationPanel.vue` - AI推荐面板组件
- ✅ `SmartSearchBar.vue` - 智能搜索栏组件

---

## 🚀 快速开始

### 1. 后端集成

#### 步骤1: 添加依赖（如需）

```xml
<!-- pom.xml -->
<!-- 缓存支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Redis（已有则跳过） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

#### 步骤2: 启用缓存

```java
// 在Application主类上添加注解
@EnableCaching
@SpringBootApplication
public class ExeBackendApplication {
    // ...
}
```

#### 步骤3: 配置缓存

```yaml
# application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000 # 1小时
      cache-null-values: false
```

#### 步骤4: 完善数据查询

打开 `RecommendationService.java`，找到标记为 `TODO` 的地方：

```java
// TODO: 从数据库查询真实数据
private List<UserBehavior> fetchAllUserBehaviors(Long subjectId) {
    // 示例SQL：
    // SELECT
    //   sa.student_id as userId,
    //   sa.question_id as questionId,
    //   sa.is_correct as correct,
    //   sa.time_spent as timeSpent,
    //   q.difficulty as difficulty
    // FROM student_answer sa
    // JOIN biz_question q ON sa.question_id = q.id
    // WHERE q.subject_id = ? (if subjectId != null)

    // 实现查询逻辑...
}
```

#### 步骤5: 测试API

```bash
# 启动后端
mvn spring-boot:run

# 测试推荐接口
curl -X GET "http://localhost:8080/api/v1/student/recommendation/questions?limit=10" \
     -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. 前端集成

#### 步骤1: 安装依赖（如需）

```bash
cd exe-frontend
npm install
```

#### 步骤2: 在页面中使用组件

**在StudentDashboard.vue中使用推荐面板：**

```vue
<template>
  <div class="dashboard">
    <!-- 其他内容 -->

    <!-- 添加AI推荐面板 -->
    <RecommendationPanel />

    <!-- 其他内容 -->
  </div>
</template>

<script setup lang="ts">
import RecommendationPanel from '@/components/ai/RecommendationPanel.vue';
// ...
</script>
```

**在布局中添加智能搜索栏：**

```vue
<!-- StudentLayout.vue -->
<template>
  <div class="student-layout">
    <el-container>
      <el-header>
        <!-- 原有的导航栏 -->

        <!-- 添加智能搜索栏 -->
        <SmartSearchBar />
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import SmartSearchBar from '@/components/ai/SmartSearchBar.vue';
</script>
```

#### 步骤3: 测试组件

```bash
# 启动前端
npm run dev

# 访问
http://localhost:5173/student/dashboard
```

---

## 🔧 功能配置

### 智能推荐配置

#### 1. 调整相似度阈值

在 `RecommendationService.java` 中：

```java
double similarity = cosineSimilarity(targetVector, otherVector);
if (similarity > 0.1) { // 相似度阈值，可调整为0.2或0.3
    similarities.add(new UserSimilarity(otherUserId, similarity));
}
```

#### 2. 调整推荐数量

前端调用时传入参数：

```typescript
const res = await request({
  url: '/api/v1/student/recommendation/questions',
  params: {
    limit: 20 // 推荐20个题目
  }
});
```

#### 3. 添加科目筛选

```typescript
const res = await request({
  url: '/api/v1/student/recommendation/questions',
  params: {
    subjectId: 1, // 数学
    limit: 10
  }
});
```

---

## 📊 数据准备

### 用户行为数据表结构

如果还没有用户行为数据表，建议创建：

```sql
-- 用户答题行为表
CREATE TABLE IF NOT EXISTS student_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer TEXT COMMENT '学生答案',
    is_correct TINYINT(1) DEFAULT 0 COMMENT '是否正确',
    time_spent INT DEFAULT 0 COMMENT '答题耗时（秒）',
    exam_result_id BIGINT COMMENT '考试记录ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_student (student_id),
    INDEX idx_question (question_id),
    INDEX idx_exam (exam_result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生答题记录';
```

---

## 🎯 优化建议

### 性能优化

#### 1. 使用Redis缓存

推荐结果会自动缓存1小时，可根据需要调整：

```java
@Cacheable(
    value = "questionRecommendations",
    key = "#userId + '_' + #subjectId + '_' + #limit",
    cacheManager = "redisCacheManager"
)
```

#### 2. 异步计算

对于复杂的推荐算法，可以异步计算：

```java
@Async
public CompletableFuture<List<QuestionRecommendation>> recommendQuestionsAsync(Long userId) {
    // ...
}
```

#### 3. 数据库索引

确保关键字段有索引：

```sql
CREATE INDEX idx_student_question ON student_answer(student_id, question_id);
CREATE INDEX idx_question_subject ON biz_question(subject_id);
```

### 算法优化

#### 1. 增加特征维度

在 `buildFeatureVector` 方法中添加更多特征：

```java
private double[] buildFeatureVector(UserBehavior behavior) {
    return new double[]{
        behavior.correct ? 1.0 : 0.0,
        Math.min(behavior.timeSpent / 300.0, 1.0),
        behavior.difficulty / 5.0,
        behavior.answerCount / 100.0, // 答题总数
        behavior.recentAccuracy // 最近正确率
    };
}
```

#### 2. 使用矩阵分解（进阶）

对于大规模数据，可以使用SVD等矩阵分解算法：

```java
// 使用Apache Commons Math库
RealMatrix userItemMatrix = createUserItemMatrix();
SingularValueDecomposition svd = new SingularValueDecomposition(userItemMatrix);
// 获取低维特征向量...
```

---

## 🐛 常见问题

### Q1: 推荐结果为空？

**原因**: 用户行为数据不足

**解决**:
1. 检查 `fetchAllUserBehaviors` 方法是否正确查询数据
2. 确认数据库中有足够的答题记录
3. 降低相似度阈值（从0.1降到0.05）

### Q2: 推荐不准确？

**解决**:
1. 增加特征维度（添加更多用户行为特征）
2. 调整特征权重
3. 收集更多用户反馈数据

### Q3: 性能问题？

**解决**:
1. 启用Redis缓存
2. 限制计算的用户数量（Top 1000活跃用户）
3. 使用定时任务预计算推荐结果

### Q4: 前端组件不显示？

**解决**:
1. 检查API是否正常返回数据
2. 查看浏览器控制台错误信息
3. 确认token是否有效

---

## 📈 效果监控

### 关键指标

1. **推荐准确率**
   - 用户点击率（CTR）
   - 用户完成练习率

2. **系统性能**
   - API响应时间
   - 缓存命中率

3. **用户体验**
   - AI功能使用率
   - 用户满意度

### 监控实现

```java
// 在Service中添加监控日志
log.info("推荐耗时: {}ms, 用户: {}, 结果数: {}",
    duration, userId, recommendations.size());

// 记录用户点击行为
@PostMapping("/recommendations/click")
public Result recordClick(@RequestBody ClickEvent event) {
    // 记录到数据库，用于后续分析
}
```

---

## 🔮 后续计划

### Phase 1: 基础功能（已完成）
- ✅ 协同过滤推荐算法
- ✅ AI推荐面板组件
- ✅ 智能搜索栏组件

### Phase 2: 增强功能（待开发）
- [ ] 自然语言搜索后端服务
- [ ] 错因深度分析服务
- [ ] 知识图谱构建
- [ ] 学习路径规划

### Phase 3: 高级功能（规划中）
- [ ] 实时推荐（WebSocket）
- [ ] 多模态推荐（文本+图像）
- [ ] A/B测试框架
- [ ] 推荐效果评估体系

---

## 📞 技术支持

如有问题，请联系：

- **技术文档**: `学生端AI功能优化方案.md`
- **GitHub Issues**: 提交问题和建议
- **团队邮箱**: dev@example.com

---

## 📝 更新日志

### v3.03 (2026-01-10)
- ✅ 创建智能推荐服务
- ✅ 创建推荐API控制器
- ✅ 创建前端推荐组件
- ✅ 创建智能搜索组件
- ✅ 编写完整技术文档

---

**祝您使用愉快！**
