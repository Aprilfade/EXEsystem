-- ========================================
-- 待办事项功能数据库索引优化脚本
-- 创建时间: 2026-01-10
-- 说明: 为待办事项查询优化创建索引，提升查询性能
-- ========================================

USE exam_system;

-- ========================================
-- 1. biz_student_paper 表索引优化
-- ========================================

-- 检查并删除旧的单列索引（如果需要的话）
-- 注意：如果索引不存在会报错，可以忽略该错误
-- DROP INDEX idx_status ON biz_student_paper;
-- DROP INDEX idx_submit_time ON biz_student_paper;

-- 检查索引是否已存在
SELECT
    CONCAT('检查索引 idx_status_submit_time: ',
    IF(COUNT(*) > 0, '已存在，跳过创建', '不存在，即将创建')) AS '检查结果'
FROM information_schema.statistics
WHERE table_schema = 'exam_system'
  AND table_name = 'biz_student_paper'
  AND index_name = 'idx_status_submit_time';

-- 创建复合索引：status + submit_time
-- 如果索引已存在，会报错 "Duplicate key name"，可以忽略
-- 覆盖查询: SELECT COUNT(*), MAX(submit_time) FROM biz_student_paper WHERE status = 1
ALTER TABLE biz_student_paper
ADD INDEX idx_status_submit_time (status, submit_time DESC);

-- 索引说明：
-- - status: 用于过滤待批改状态(status=1)
-- - submit_time DESC: 用于获取最新提交时间(MAX(submit_time))
-- - 索引顺序: status 在前，因为它的选择性更好（只有3个值：0/1/2）


-- ========================================
-- 2. biz_question 表索引优化
-- ========================================

-- 检查索引是否已存在
SELECT
    CONCAT('检查索引 idx_audit_status_create_time: ',
    IF(COUNT(*) > 0, '已存在，跳过创建', '不存在，即将创建')) AS '检查结果'
FROM information_schema.statistics
WHERE table_schema = 'exam_system'
  AND table_name = 'biz_question'
  AND index_name = 'idx_audit_status_create_time';

-- 创建复合索引：audit_status + create_time
-- 如果索引已存在，会报错 "Duplicate key name"，可以忽略
-- 覆盖查询: SELECT COUNT(*), MAX(create_time) FROM biz_question WHERE audit_status = 0
ALTER TABLE biz_question
ADD INDEX idx_audit_status_create_time (audit_status, create_time DESC);

-- 索引说明：
-- - audit_status: 用于过滤待审核状态(audit_status=0)
-- - create_time DESC: 用于获取最新创建时间(MAX(create_time))
-- - 索引顺序: audit_status 在前，因为它的选择性更好（只有3个值：0/1/2）


-- ========================================
-- 3. 验证索引创建结果
-- ========================================

-- 查看 biz_student_paper 表的所有索引
SELECT
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '列名',
    SEQ_IN_INDEX AS '列序号',
    COLLATION AS '排序',
    CARDINALITY AS '基数',
    INDEX_TYPE AS '索引类型'
FROM information_schema.statistics
WHERE table_schema = 'exam_system'
  AND table_name = 'biz_student_paper'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 查看 biz_question 表的所有索引
SELECT
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '列名',
    SEQ_IN_INDEX AS '列序号',
    COLLATION AS '排序',
    CARDINALITY AS '基数',
    INDEX_TYPE AS '索引类型'
FROM information_schema.statistics
WHERE table_schema = 'exam_system'
  AND table_name = 'biz_question'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;


-- ========================================
-- 4. 性能测试 - 执行前后对比
-- ========================================

-- 待批改试卷查询 - 查看执行计划
EXPLAIN SELECT COUNT(*) AS count, MAX(submit_time) AS latestTime
FROM biz_student_paper
WHERE status = 1;

-- 待审核题目查询 - 查看执行计划
EXPLAIN SELECT COUNT(*) AS count, MAX(create_time) AS latestTime
FROM biz_question
WHERE audit_status = 0;


-- ========================================
-- 5. 索引维护建议
-- ========================================

/*
索引维护建议：

1. 定期分析索引效率：
   ANALYZE TABLE biz_student_paper;
   ANALYZE TABLE biz_question;

2. 查看索引使用情况（需要开启 performance_schema）：
   SELECT * FROM performance_schema.table_io_waits_summary_by_index_usage
   WHERE OBJECT_SCHEMA = 'exam_system'
   AND OBJECT_NAME IN ('biz_student_paper', 'biz_question');

3. 如果索引不再使用，可以删除：
   DROP INDEX idx_status_submit_time ON biz_student_paper;
   DROP INDEX idx_audit_status_create_time ON biz_question;

4. 索引选择性检查：
   -- 检查 status 列的选择性
   SELECT
       COUNT(DISTINCT status) / COUNT(*) AS status_selectivity,
       COUNT(DISTINCT status) AS distinct_values,
       COUNT(*) AS total_rows
   FROM biz_student_paper;

   -- 检查 audit_status 列的选择性
   SELECT
       COUNT(DISTINCT audit_status) / COUNT(*) AS audit_status_selectivity,
       COUNT(DISTINCT audit_status) AS distinct_values,
       COUNT(*) AS total_rows
   FROM biz_question;

5. 性能预期：
   - 小表（< 1000行）：性能提升不明显
   - 中表（1000-10万行）：查询速度提升 50-80%
   - 大表（> 10万行）：查询速度提升 90%+

注意：
- 索引会占用额外的磁盘空间（每个复合索引约 1-2MB）
- 索引会略微降低 INSERT/UPDATE/DELETE 性能（< 5%）
- 但对于以查询为主的待办事项功能，索引的性能收益远大于代价
*/


-- ========================================
-- 6. 回滚脚本（如需删除索引）
-- ========================================

/*
-- 如果需要回滚，执行以下命令：
DROP INDEX idx_status_submit_time ON biz_student_paper;
DROP INDEX idx_audit_status_create_time ON biz_question;

-- 如果之前删除了单列索引，可以重新创建：
ALTER TABLE biz_student_paper ADD INDEX idx_status (status);
ALTER TABLE biz_student_paper ADD INDEX idx_submit_time (submit_time);
*/


-- ========================================
-- 执行完成提示
-- ========================================

SELECT '✅ 索引优化脚本执行完成！' AS '执行结果',
       '请查看上方的索引列表验证创建是否成功' AS '下一步操作';
