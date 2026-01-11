-- 成绩管理功能优化 - 数据库表结构更新
-- 执行日期: 2026-01-07

-- 1. 为 biz_exam_result 表添加新字段
ALTER TABLE biz_exam_result
ADD COLUMN comment VARCHAR(500) COMMENT '教师评语' AFTER result_details,
ADD COLUMN published TINYINT(1) DEFAULT 0 COMMENT '是否发布给学生查看(0-未发布 1-已发布)' AFTER comment;

-- 2. 为常用查询字段添加索引
ALTER TABLE biz_exam_result
ADD INDEX idx_student_id (student_id),
ADD INDEX idx_paper_id (paper_id),
ADD INDEX idx_create_time (create_time),
ADD INDEX idx_published (published);

-- 3. 查看表结构（验证）
-- DESC biz_exam_result;
