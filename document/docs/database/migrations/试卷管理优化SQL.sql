-- ========================================
-- 试卷管理性能优化 - 数据库索引创建
-- 执行时间：2026-01-05
-- 说明：为试卷管理相关表添加性能优化索引
-- ========================================

USE exam_system;

-- 1. 试卷表索引优化
-- ========================================

-- 1.1 科目和年级联合索引（常用组合查询）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper'
  AND INDEX_NAME = 'idx_paper_subject_grade';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_paper_subject_grade ON biz_paper(subject_id, grade);',
    'SELECT ''索引 idx_paper_subject_grade 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 状态索引（用于发布/草稿筛选）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper'
  AND INDEX_NAME = 'idx_paper_status';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_paper_status ON biz_paper(status);',
    'SELECT ''索引 idx_paper_status 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.3 创建时间索引（用于排序）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper'
  AND INDEX_NAME = 'idx_paper_create_time';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_paper_create_time ON biz_paper(create_time DESC);',
    'SELECT ''索引 idx_paper_create_time 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.4 试卷名称索引（用于模糊搜索）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper'
  AND INDEX_NAME = 'idx_paper_name';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_paper_name ON biz_paper(name);',
    'SELECT ''索引 idx_paper_name 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 试卷分组表索引优化
-- ========================================

-- 2.1 试卷ID和排序联合索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper_group'
  AND INDEX_NAME = 'idx_group_paper_sort';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_group_paper_sort ON biz_paper_group(paper_id, sort_order);',
    'SELECT ''索引 idx_group_paper_sort 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 试卷题目关联表索引优化
-- ========================================

-- 3.1 试卷ID和排序联合索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper_question'
  AND INDEX_NAME = 'idx_pq_paper_sort';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_pq_paper_sort ON biz_paper_question(paper_id, sort_order);',
    'SELECT ''索引 idx_pq_paper_sort 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.2 分组ID索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper_question'
  AND INDEX_NAME = 'idx_pq_group';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_pq_group ON biz_paper_question(group_id);',
    'SELECT ''索引 idx_pq_group 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.3 题目ID索引（用于反向查询）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper_question'
  AND INDEX_NAME = 'idx_pq_question';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_pq_question ON biz_paper_question(question_id);',
    'SELECT ''索引 idx_pq_question 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 试卷图片表索引优化
-- ========================================

-- 4.1 试卷ID和排序联合索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper_image'
  AND INDEX_NAME = 'idx_pi_paper_sort';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_pi_paper_sort ON biz_paper_image(paper_id, sort_order);',
    'SELECT ''索引 idx_pi_paper_sort 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 验证索引创建结果
-- ========================================

SELECT '✅ 试卷表索引' AS '分类', INDEX_NAME AS '索引名', COLUMN_NAME AS '列名', SEQ_IN_INDEX AS '序号'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

SELECT '✅ 分组表索引' AS '分类', INDEX_NAME AS '索引名', COLUMN_NAME AS '列名', SEQ_IN_INDEX AS '序号'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper_group'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

SELECT '✅ 题目关联表索引' AS '分类', INDEX_NAME AS '索引名', COLUMN_NAME AS '列名', SEQ_IN_INDEX AS '序号'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper_question'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

SELECT '✅ 图片表索引' AS '分类', INDEX_NAME AS '索引名', COLUMN_NAME AS '列名', SEQ_IN_INDEX AS '序号'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_paper_image'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 6. 分析表并优化
-- ========================================

ANALYZE TABLE biz_paper;
ANALYZE TABLE biz_paper_group;
ANALYZE TABLE biz_paper_question;
ANALYZE TABLE biz_paper_image;

SELECT '✅ 数据库索引优化完成！' AS '状态';
