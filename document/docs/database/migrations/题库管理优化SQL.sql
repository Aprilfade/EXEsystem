-- =====================================================
-- 题库管理功能优化 - 数据库修复脚本
-- 创建时间: 2026-01-05
-- 说明: 添加缺失的答案图片字段
-- =====================================================

USE exam_system;

-- 1. 添加答案图片字段
-- 检查字段是否存在，如果不存在则添加
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_question'
  AND COLUMN_NAME = 'answer_image_url';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `biz_question` ADD COLUMN `answer_image_url` VARCHAR(500) DEFAULT NULL COMMENT ''答案图片地址'' AFTER `answer`;',
    'SELECT ''字段 answer_image_url 已存在，跳过添加'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证结果
SELECT '✅ 数据库修复完成！' AS status;
SELECT
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    IS_NULLABLE AS '允许空值',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_question'
  AND COLUMN_NAME IN ('answer', 'answer_image_url')
ORDER BY ORDINAL_POSITION;
