-- ============================================
-- 检查现有索引情况
-- ============================================

USE exam_system;

-- 查看所有自定义索引（排除主键和外键）
SELECT
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ', ') AS '包含列',
    INDEX_TYPE AS '类型',
    CASE NON_UNIQUE
        WHEN 0 THEN '唯一索引'
        WHEN 1 THEN '普通索引'
    END AS '索引类型'
FROM
    information_schema.STATISTICS
WHERE
    TABLE_SCHEMA = 'exam_system'
    AND INDEX_NAME NOT IN ('PRIMARY')
    AND TABLE_NAME IN (
        'biz_exam_result',
        'biz_wrong_record',
        'biz_paper_question',
        'biz_question',
        'biz_question_knowledge_point',
        'biz_student',
        'biz_knowledge_point',
        'biz_paper',
        'biz_class',
        'biz_learning_activity',
        'biz_sign_in',
        'biz_ai_call_log',
        'sys_user',
        'sys_oper_log',
        'sys_login_log'
    )
GROUP BY
    TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY
    TABLE_NAME, INDEX_NAME;

-- 统计各表的索引数量
SELECT
    TABLE_NAME AS '表名',
    COUNT(DISTINCT INDEX_NAME) - 1 AS '索引数量（不含主键）'
FROM
    information_schema.STATISTICS
WHERE
    TABLE_SCHEMA = 'exam_system'
    AND TABLE_NAME IN (
        'biz_exam_result',
        'biz_wrong_record',
        'biz_paper_question',
        'biz_question',
        'biz_question_knowledge_point',
        'biz_student',
        'biz_knowledge_point',
        'biz_paper',
        'biz_class',
        'biz_learning_activity',
        'biz_sign_in',
        'biz_ai_call_log',
        'sys_user',
        'sys_oper_log',
        'sys_login_log'
    )
GROUP BY
    TABLE_NAME
ORDER BY
    TABLE_NAME;
