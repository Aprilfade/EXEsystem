-- ========================================
-- 科目管理性能优化 - 数据库索引创建
-- 执行时间：2026-01-05
-- 说明：为科目管理相关表添加性能优化索引
-- ========================================

USE exam_system;

-- 1. 科目表索引优化
-- ========================================

-- 1.1 年级索引（用于按年级筛选）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_subject'
  AND INDEX_NAME = 'idx_subject_grade';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_subject_grade ON biz_subject(grade);',
    'SELECT ''索引 idx_subject_grade 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 科目名称索引（用于模糊搜索）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_subject'
  AND INDEX_NAME = 'idx_subject_name';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_subject_name ON biz_subject(name);',
    'SELECT ''索引 idx_subject_name 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.3 创建时间索引（用于排序）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_subject'
  AND INDEX_NAME = 'idx_subject_create_time';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_subject_create_time ON biz_subject(create_time DESC);',
    'SELECT ''索引 idx_subject_create_time 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.4 年级+创建时间联合索引（常用组合查询）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_subject'
  AND INDEX_NAME = 'idx_subject_grade_create_time';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_subject_grade_create_time ON biz_subject(grade, create_time DESC);',
    'SELECT ''索引 idx_subject_grade_create_time 已存在'' AS message;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 验证索引创建结果
-- ========================================

SELECT '科目表索引' AS '分类', INDEX_NAME AS '索引名', COLUMN_NAME AS '列名', SEQ_IN_INDEX AS '序号', NON_UNIQUE AS '非唯一'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_subject'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 3. 分析表并优化
-- ========================================

ANALYZE TABLE biz_subject;

SELECT '数据库索引优化完成' AS '状态';
