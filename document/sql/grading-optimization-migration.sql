-- ============================================
-- 成绩批阅功能优化 - 数据库迁移脚本
-- 版本: v3.03
-- 日期: 2026-01-10
-- 功能: 添加批阅相关字段、状态管理规范化、乐观锁支持
-- ============================================

-- 1. 添加批阅相关字段
ALTER TABLE biz_exam_result
ADD COLUMN graded_by BIGINT NULL COMMENT '批阅教师ID' AFTER comment,
ADD COLUMN graded_time DATETIME NULL COMMENT '批阅时间' AFTER graded_by,
ADD COLUMN original_score INT NULL COMMENT 'AI自动评分（原始分数）' AFTER graded_time,
ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER original_score;

-- 2. 添加索引以提升查询性能
ALTER TABLE biz_exam_result
ADD INDEX idx_graded_by (graded_by) COMMENT '批阅教师索引',
ADD INDEX idx_graded_time (graded_time) COMMENT '批阅时间索引';

-- 3. 更新状态字段注释（明确状态定义）
ALTER TABLE biz_exam_result
MODIFY COLUMN status INT NULL DEFAULT 1 COMMENT '状态: 0-未提交, 1-待批改（已提交未批阅）, 2-已批改（已批阅未发布）, 3-已发布';

-- 4. 数据迁移：将现有数据的 original_score 设置为当前 score
UPDATE biz_exam_result
SET original_score = score
WHERE original_score IS NULL;

-- 5. 数据迁移：修正状态值
-- 已发布的成绩状态应该是3
UPDATE biz_exam_result
SET status = 3
WHERE published = 1 AND status != 3;

-- 已批改但未发布的状态应该是2
UPDATE biz_exam_result
SET status = 2
WHERE published = 0 AND status != 2 AND score > 0;

-- 提交了但未批改的状态应该是1
UPDATE biz_exam_result
SET status = 1
WHERE published = 0 AND status != 1 AND score = 0;

-- ============================================
-- 验证数据迁移结果
-- ============================================

-- 查看状态分布
SELECT
    status,
    CASE status
        WHEN 0 THEN '未提交'
        WHEN 1 THEN '待批改'
        WHEN 2 THEN '已批改'
        WHEN 3 THEN '已发布'
        ELSE '未知'
    END AS status_name,
    COUNT(*) AS count
FROM biz_exam_result
GROUP BY status
ORDER BY status;

-- 查看新增字段
SELECT
    id,
    student_id,
    paper_name,
    score,
    original_score,
    graded_by,
    graded_time,
    status,
    version
FROM biz_exam_result
LIMIT 5;

-- ============================================
-- 回滚脚本（如需要）
-- ============================================
/*
ALTER TABLE biz_exam_result
DROP INDEX idx_graded_by,
DROP INDEX idx_graded_time,
DROP COLUMN graded_by,
DROP COLUMN graded_time,
DROP COLUMN original_score,
DROP COLUMN version;

ALTER TABLE biz_exam_result
MODIFY COLUMN status INT NULL DEFAULT 2 COMMENT '0-未提交，1-待批改，2-已批改';
*/

-- ============================================
-- 说明文档
-- ============================================
/*
状态流转说明：
0 (未提交) → 学生创建了考试记录但未提交答案
1 (待批改) → 学生已提交答案，系统自动评分完成，等待教师人工批阅
2 (已批改) → 教师已完成批阅（修改分数/评语），但未发布给学生
3 (已发布) → 成绩已公布，学生可以查看

字段说明：
- score: 当前得分（可能被教师修改过）
- original_score: AI自动评分的原始分数（不可修改）
- graded_by: 批阅教师的user_id
- graded_time: 批阅时间（教师修改分数或评语的时间）
- version: 乐观锁版本号，每次更新自动+1
- published: 是否发布（1=已发布，0=未发布），与status=3联动

使用建议：
1. 执行此脚本前请先备份 biz_exam_result 表
2. 执行后验证状态分布是否合理
3. 如有问题可使用回滚脚本恢复
*/
