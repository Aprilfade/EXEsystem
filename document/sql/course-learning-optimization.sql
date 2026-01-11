-- ============================================================
-- 课程学习中心优化 - 数据库脚本
-- 创建时间: 2026-01-11
-- 说明: 添加学习进度追踪、课程章节、学习会话功能
-- ============================================================

USE exe_system;

-- ============================================================
-- 表1: biz_course_chapter (课程章节表)
-- 说明: 支持无限层级的章节嵌套结构
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_course_chapter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '章节ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父章节ID，0表示根章节',
  name VARCHAR(200) NOT NULL COMMENT '章节名称',
  description TEXT COMMENT '章节描述',
  sort_order INT DEFAULT 0 COMMENT '排序顺序',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_course_id (course_id),
  INDEX idx_parent_id (parent_id),
  INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程章节表';

-- ============================================================
-- 表2: biz_course_progress (学习进度表)
-- 说明: 记录学生对每个资源的学习进度
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_course_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进度ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  resource_id BIGINT NOT NULL COMMENT '资源ID',
  resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：VIDEO/PDF/PPT/LINK',
  progress_percent INT DEFAULT 0 COMMENT '完成百分比（0-100）',
  last_position VARCHAR(50) COMMENT '最后学习位置（视频秒数/PDF页码）',
  study_duration INT DEFAULT 0 COMMENT '累计学习时长（秒）',
  is_completed TINYINT DEFAULT 0 COMMENT '是否完成：0-未完成，1-已完成',
  completed_time DATETIME COMMENT '完成时间',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY uk_student_resource (student_id, resource_id),
  INDEX idx_student_course (student_id, course_id),
  INDEX idx_course (course_id),
  INDEX idx_completed (is_completed, completed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习进度表';

-- ============================================================
-- 表3: biz_study_session (学习会话表)
-- 说明: 记录学习会话，用于学习行为分析
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_study_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  resource_id BIGINT NOT NULL COMMENT '资源ID',
  session_start DATETIME NOT NULL COMMENT '会话开始时间',
  session_end DATETIME COMMENT '会话结束时间',
  duration INT DEFAULT 0 COMMENT '会话时长（秒）',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  INDEX idx_student (student_id),
  INDEX idx_course (course_id),
  INDEX idx_resource (resource_id),
  INDEX idx_session_start (session_start),
  INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习会话表（用于学习行为分析）';

-- ============================================================
-- 修改现有表: biz_course_resource
-- 说明: 添加章节关联字段
-- ============================================================
ALTER TABLE biz_course_resource
  ADD COLUMN chapter_id BIGINT COMMENT '章节ID' AFTER course_id,
  ADD INDEX idx_chapter_id (chapter_id);

-- ============================================================
-- 初始化示例数据（可选）
-- ============================================================

-- 示例：为现有课程添加章节结构
-- INSERT INTO biz_course_chapter (course_id, parent_id, name, description, sort_order) VALUES
-- (1, 0, '第一章：基础知识', '本章介绍基础概念', 1),
-- (1, 0, '第二章：进阶内容', '本章介绍进阶内容', 2);

-- 示例：将现有资源关联到章节
-- UPDATE biz_course_resource SET chapter_id = 1 WHERE id IN (1, 2, 3);

-- ============================================================
-- 数据清理（如果需要重新创建表，取消下面注释）
-- ============================================================
-- DROP TABLE IF EXISTS biz_study_session;
-- DROP TABLE IF EXISTS biz_course_progress;
-- DROP TABLE IF EXISTS biz_course_chapter;
-- ALTER TABLE biz_course_resource DROP COLUMN chapter_id;

COMMIT;

-- ============================================================
-- 脚本执行完成
-- ============================================================
SELECT 'Course Learning Optimization Database Script - Executed Successfully!' AS Status;
