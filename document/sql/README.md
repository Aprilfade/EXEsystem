# SQL脚本文档

本目录包含EXE在线考试系统的所有数据库脚本和迁移文件。

---

## 📊 脚本列表

### 1. exam_system.sql
**完整系统数据库脚本**

包含整个EXE系统的完整数据库结构，包括所有表、索引、约束等。

**主要表结构**:
- 用户系统: `sys_user`, `sys_role`, `sys_permission`
- 课程系统: `biz_course`, `biz_course_resource`
- 考试系统: `biz_exam`, `biz_exam_question`, `biz_student_exam`
- 知识点: `biz_knowledge_point`, `biz_subject`
- 学生管理: `biz_student`, `biz_class`

**使用方式**:
```bash
mysql -u root -p exam_system < exam_system.sql
```

---

### 2. course-learning-optimization.sql
**课程学习优化相关表**

课程学习中心优化(v3.05-v3.06)新增的数据库表。

**新增表**:
- `biz_course_chapter` - 课程章节表（支持无限层级）
- `biz_course_progress` - 学习进度表（记录学生学习进度）
- `biz_study_session` - 学习会话表（用于学习行为分析）

**字段修改**:
- `biz_course_resource.chapter_id` - 资源关联章节

**相关文档**: `docs/course/课程学习中心全面优化完成报告-v3.06-最终版.md`

**使用方式**:
```bash
mysql -u root -p exam_system < course-learning-optimization.sql
```

---

### 3. grading-history-table.sql
**成绩批阅历史表**

记录教师批阅试卷的历史操作。

**新增表**:
- `biz_grading_history` - 批阅历史表

**字段**:
- `id` - 主键
- `student_exam_id` - 学生考试ID
- `grader_id` - 批阅人ID
- `score_before` - 修改前分数
- `score_after` - 修改后分数
- `comment` - 批阅意见
- `grading_time` - 批阅时间

**使用方式**:
```bash
mysql -u root -p exam_system < grading-history-table.sql
```

---

### 4. grading-optimization-migration.sql
**成绩批阅优化迁移脚本**

成绩批阅功能优化(v3.03)的数据库迁移。

**修改内容**:
- 添加批阅状态字段
- 添加批阅时间索引
- 优化批阅查询性能

**相关文档**: `docs/grading/成绩批阅功能优化完成报告-v3.03.md`

**使用方式**:
```bash
mysql -u root -p exam_system < grading-optimization-migration.sql
```

---

### 5. notification-table.sql
**系统通知表**

系统通知功能的数据库表。

**新增表**:
- `sys_notification` - 系统通知表

**字段**:
- `id` - 主键
- `user_id` - 用户ID
- `title` - 通知标题
- `content` - 通知内容
- `type` - 通知类型（SYSTEM/COURSE/EXAM/GRADE）
- `is_read` - 是否已读
- `created_time` - 创建时间

**使用方式**:
```bash
mysql -u root -p exam_system < notification-table.sql
```

---

## 🔄 脚本执行顺序

如果从零开始部署，推荐按以下顺序执行：

```bash
# 1. 创建完整数据库结构
mysql -u root -p exam_system < exam_system.sql

# 2. 添加课程学习优化表
mysql -u root -p exam_system < course-learning-optimization.sql

# 3. 添加批阅历史表
mysql -u root -p exam_system < grading-history-table.sql

# 4. 执行批阅优化迁移
mysql -u root -p exam_system < grading-optimization-migration.sql

# 5. 添加通知表
mysql -u root -p exam_system < notification-table.sql
```

---

## 📋 核心表说明

### 课程学习核心表

**biz_course_chapter（章节表）**
```sql
CREATE TABLE biz_course_chapter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL COMMENT '课程ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父章节ID，0表示根章节',
  name VARCHAR(200) NOT NULL COMMENT '章节名称',
  description TEXT COMMENT '章节描述',
  sort_order INT DEFAULT 0 COMMENT '排序顺序',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_course_id (course_id),
  INDEX idx_parent_id (parent_id)
);
```

**biz_course_progress（学习进度表）**
```sql
CREATE TABLE biz_course_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL COMMENT '学生ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  resource_id BIGINT NOT NULL COMMENT '资源ID',
  resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：VIDEO/PDF/PPT/LINK',
  progress_percent INT DEFAULT 0 COMMENT '完成百分比（0-100）',
  last_position VARCHAR(50) COMMENT '最后学习位置（视频秒数/PDF页码）',
  study_duration INT DEFAULT 0 COMMENT '累计学习时长（秒）',
  is_completed TINYINT DEFAULT 0 COMMENT '是否完成：0-未完成，1-已完成',
  completed_time DATETIME COMMENT '完成时间',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student_resource (student_id, resource_id),
  INDEX idx_student_course (student_id, course_id)
);
```

---

## ⚠️ 注意事项

1. **备份数据**: 执行任何SQL脚本前，请先备份现有数据库
   ```bash
   mysqldump -u root -p exam_system > backup_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **环境检查**: 确保MySQL版本 >= 5.7（推荐8.0+）

3. **字符集**: 所有表使用 `utf8mb4` 字符集，支持emoji等特殊字符

4. **索引优化**: 脚本中已包含性能优化索引，无需额外添加

5. **外键约束**: 为保证灵活性，大部分外键仅在应用层处理，未在数据库层添加约束

---

## 🛠️ 常用SQL操作

### 查看章节树结构
```sql
SELECT 
  c.id,
  c.name,
  c.parent_id,
  c.sort_order,
  COUNT(r.id) as resource_count
FROM biz_course_chapter c
LEFT JOIN biz_course_resource r ON r.chapter_id = c.id
WHERE c.course_id = 1
GROUP BY c.id
ORDER BY c.parent_id, c.sort_order;
```

### 查看学生学习进度
```sql
SELECT 
  s.name AS student_name,
  cr.name AS resource_name,
  cp.progress_percent,
  cp.study_duration,
  cp.is_completed
FROM biz_course_progress cp
JOIN biz_student s ON cp.student_id = s.id
JOIN biz_course_resource cr ON cp.resource_id = cr.id
WHERE cp.course_id = 1
ORDER BY s.name, cr.sort_order;
```

### 计算课程完成率
```sql
SELECT 
  c.name AS course_name,
  COUNT(DISTINCT r.id) AS total_resources,
  COUNT(DISTINCT CASE WHEN cp.is_completed = 1 THEN r.id END) AS completed_resources,
  ROUND(COUNT(DISTINCT CASE WHEN cp.is_completed = 1 THEN r.id END) * 100.0 / COUNT(DISTINCT r.id), 2) AS completion_rate
FROM biz_course c
LEFT JOIN biz_course_resource r ON r.course_id = c.id
LEFT JOIN biz_course_progress cp ON cp.resource_id = r.id AND cp.student_id = 1
WHERE c.id = 1
GROUP BY c.id;
```

---

**最后更新**: 2026-01-11
**维护者**: Claude Sonnet 4.5
