# Dashboard 功能修复指南

## 📋 问题描述

前端控制台出现以下错误：
```
Home.vue:1100 获取待办事项失败: Error: 获取待办事项失败
Home.vue:1116 获取最近活动失败: Error: 获取最近活动失败
Home.vue:1045 获取薄弱知识点失败: Error: 获取薄弱知识点失败
```

## 🔍 问题原因

经过检查，**后端代码完全正常**，所有 API 都已经实现：

### ✅ 已实现的后端代码

1. **Controller层** (`DashboardController.java`)：
   - `/api/v1/dashboard/todos` - 待办事项接口（第86行）
   - `/api/v1/dashboard/recent-activities` - 最近活动接口（第103行）
   - `/api/v1/dashboard/weak-knowledge-points` - 薄弱知识点接口（第219行）

2. **Service层** (`DashboardServiceImpl.java`)：
   - `getTodoList()` - 完整实现（第170行）
   - `getRecentActivities()` - 完整实现（第221行）
   - `getWeakKnowledgePointsTop10()` - 完整实现（第312行）

3. **Mapper层** (`DashboardMapper.xml`)：
   - `getPendingPapersCount` - SQL 已实现（第77行）
   - `getPendingQuestionsCount` - SQL 已实现（第85行）
   - `getRecentActivities` - SQL 已实现（第94行）
   - `getWeakKnowledgePointsTop10` - SQL 已实现（第199行）

### ❌ 问题根源

**数据库缺少必要的表或数据**：

1. **缺少表** `biz_student_knowledge_mastery`（学生知识点掌握度表）
2. **缺少表** `biz_paper_knowledge_point`（试卷知识点关联表）
3. 或者表存在但**缺少测试数据**，导致查询返回空结果

## 🛠️ 解决方案

### 步骤 1：执行数据库修复 SQL

在项目根目录找到文件：**`修复Dashboard功能-建表.sql`**

在你的数据库管理工具（如 Navicat、MySQL Workbench）中执行这个 SQL 文件。

**SQL 文件会自动完成：**
- ✅ 创建 `biz_student_knowledge_mastery` 表（如果不存在）
- ✅ 创建 `biz_paper_knowledge_point` 表（如果不存在）
- ✅ 插入测试数据（让图表有内容显示）
- ✅ 执行验证查询（确认表和数据都正常）

### 步骤 2：重启后端服务

执行 SQL 后，重启后端服务：

**方法 1 - 如果后端正在运行**：
1. 停止后端（Ctrl+C 或关闭窗口）
2. 重新启动后端

**方法 2 - 清理编译**：
```bash
cd exe-backend
./mvnw clean spring-boot:run
```

或者在 Windows 上：
```bash
cd exe-backend
mvnw.cmd clean spring-boot:run
```

### 步骤 3：刷新前端页面

1. 在浏览器中刷新页面（F5 或 Ctrl+R）
2. 检查控制台是否还有错误
3. 查看 Dashboard 页面是否正常显示数据

## 📊 预期结果

修复后，Dashboard 页面应该显示：

### 1. 待办事项卡片
- ✅ 显示"待批改试卷"数量（如果有）
- ✅ 显示"待审核题目"数量（如果有）
- ✅ 显示时间（如"2小时前"）

### 2. 最近活动列表
- ✅ 显示最近的学生提交试卷活动
- ✅ 显示最近创建的题目
- ✅ 显示最近导入的学生
- ✅ 默认显示最新5条

### 3. 薄弱知识点图表
- ✅ 显示掌握度低于60%的知识点
- ✅ 按掌握度从低到高排序
- ✅ 显示Top 10

## 🔧 如果仍然报错

### 检查项 1：数据库连接
确保后端能正常连接到数据库。查看后端日志中是否有数据库连接错误。

### 检查项 2：表字段
确认以下字段存在：

**biz_question 表**：
- `audit_status` 字段（INT 类型，0=待审核）

**biz_student_paper 表**：
- `status` 字段（INT 类型，1=已提交待批改）
- `submit_time` 字段（DATETIME 类型）

**检查SQL**：
```sql
-- 检查 biz_question 表
SHOW COLUMNS FROM biz_question LIKE 'audit_status';

-- 检查 biz_student_paper 表
SHOW COLUMNS FROM biz_student_paper LIKE 'status';
SHOW COLUMNS FROM biz_student_paper LIKE 'submit_time';
```

### 检查项 3：后端日志
查看后端控制台输出，搜索关键词：
- `获取待办事项`
- `获取最近活动`
- `获取薄弱知识点`

如果有 SQL 错误，日志会显示具体的错误信息。

## 📝 添加字段（如果缺少）

如果检查发现缺少字段，执行以下SQL：

### 为 biz_question 添加审核状态
```sql
-- 添加审核状态字段
ALTER TABLE biz_question
ADD COLUMN audit_status INT DEFAULT 1 COMMENT '审核状态：0-待审核，1-已通过，2-未通过'
AFTER question_type;

-- 将现有题目设置为已通过
UPDATE biz_question SET audit_status = 1 WHERE audit_status IS NULL;
```

### 为 biz_student_paper 添加状态和提交时间
```sql
-- 添加状态字段
ALTER TABLE biz_student_paper
ADD COLUMN status INT DEFAULT 0 COMMENT '状态：0-未提交，1-已提交待批改，2-已批改'
AFTER score;

-- 添加提交时间字段
ALTER TABLE biz_student_paper
ADD COLUMN submit_time DATETIME COMMENT '提交时间'
AFTER status;

-- 将现有数据设置默认状态
UPDATE biz_student_paper
SET status = 2, submit_time = create_time
WHERE status IS NULL AND score IS NOT NULL;
```

## 🎯 验证修复

执行以下查询，确认数据正常：

```sql
-- 1. 检查待办事项
SELECT
    (SELECT COUNT(*) FROM biz_student_paper WHERE status = 1) AS pending_papers,
    (SELECT COUNT(*) FROM biz_question WHERE audit_status = 0) AS pending_questions;

-- 2. 检查最近活动
SELECT COUNT(*) FROM (
    SELECT 'submit_paper' AS type FROM biz_student_paper WHERE submit_time IS NOT NULL
    UNION ALL
    SELECT 'create_question' FROM biz_question WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
    UNION ALL
    SELECT 'import_student' FROM biz_student WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
) activities;

-- 3. 检查薄弱知识点数据
SELECT COUNT(*) FROM biz_student_knowledge_mastery WHERE total_count >= 5 AND mastery_rate < 60;
```

如果这三个查询都返回数字（即使是0），说明表结构正常。

## 📚 相关文件

- **SQL修复文件**: `修复Dashboard功能-建表.sql`
- **后端Controller**: `exe-backend/src/main/java/com/ice/exebackend/controller/DashboardController.java`
- **后端Service**: `exe-backend/src/main/java/com/ice/exebackend/service/impl/DashboardServiceImpl.java`
- **Mapper XML**: `exe-backend/src/main/resources/mapper/DashboardMapper.xml`
- **前端API**: `exe-frontend/src/api/dashboard.ts`
- **前端页面**: `exe-frontend/src/views/Home.vue`

---

**最后更新**: 2026-01-09
**状态**: ✅ 后端代码完整，需执行SQL修复数据库
