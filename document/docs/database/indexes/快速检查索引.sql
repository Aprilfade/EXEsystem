-- ============================================
-- 快速检查索引（简化版）
-- ============================================

USE exam_system;

-- 查询1：查看exam_system数据库中所有索引
SELECT
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '列名',
    INDEX_TYPE AS '类型'
FROM
    information_schema.STATISTICS
WHERE
    TABLE_SCHEMA = 'exam_system'
    AND INDEX_NAME LIKE 'idx_%'
ORDER BY
    TABLE_NAME, INDEX_NAME;

-- 如果上面没有返回数据，说明还没有创建任何idx_开头的索引
-- 那就直接执行「数据库索引优化-分段执行.sql」即可
