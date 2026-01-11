-- ============================================
-- 批阅历史记录表创建脚本
-- 版本: v3.04
-- 日期: 2026-01-10
-- 功能: 记录成绩批阅的完整历史，支持审计追踪
-- ============================================

-- 创建批阅历史表
CREATE TABLE IF NOT EXISTS `biz_grading_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_result_id` BIGINT NOT NULL COMMENT '考试成绩ID',
  `grader_id` BIGINT NOT NULL COMMENT '批阅人ID（教师）',
  `grader_name` VARCHAR(50) NOT NULL COMMENT '批阅人姓名',
  `action_type` VARCHAR(20) NOT NULL COMMENT '操作类型：UPDATE_SCORE（修改分数）/UPDATE_COMMENT（修改评语）/BATCH_UPDATE（批量修改）',
  `old_score` INT NULL COMMENT '修改前分数',
  `new_score` INT NULL COMMENT '修改后分数',
  `old_comment` VARCHAR(500) NULL COMMENT '修改前评语',
  `new_comment` VARCHAR(500) NULL COMMENT '修改后评语',
  `reason` VARCHAR(200) NULL COMMENT '修改原因（可选）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  INDEX `idx_exam_result_id` (`exam_result_id`) COMMENT '成绩ID索引',
  INDEX `idx_grader_id` (`grader_id`) COMMENT '批阅人ID索引',
  INDEX `idx_create_time` (`create_time`) COMMENT '操作时间索引',
  INDEX `idx_action_type` (`action_type`) COMMENT '操作类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批阅历史记录表';

-- ============================================
-- 验证表创建
-- ============================================

-- 查看表结构
DESC biz_grading_history;

-- 查看索引
SHOW INDEX FROM biz_grading_history;

-- ============================================
-- 示例数据（可选，用于测试）
-- ============================================

/*
-- 插入示例数据
INSERT INTO biz_grading_history
(exam_result_id, grader_id, grader_name, action_type, old_score, new_score, old_comment, new_comment, reason, create_time)
VALUES
(1, 1, '张老师', 'UPDATE_SCORE', 80, 85, NULL, NULL, '客观题评分有误，重新核算', NOW()),
(1, 1, '张老师', 'UPDATE_COMMENT', NULL, NULL, NULL, '整体表现良好，继续努力！', NULL, NOW()),
(2, 2, '李老师', 'UPDATE_SCORE', 75, 90, '基础掌握不扎实', '订正后表现优秀', '学生申诉后复查', NOW());

-- 查询示例
SELECT * FROM biz_grading_history ORDER BY create_time DESC LIMIT 5;
*/

-- ============================================
-- 常用查询示例
-- ============================================

-- 1. 查询某份成绩的所有批阅历史
/*
SELECT
    h.id,
    h.grader_name,
    h.action_type,
    CASE h.action_type
        WHEN 'UPDATE_SCORE' THEN CONCAT(h.old_score, ' → ', h.new_score)
        WHEN 'UPDATE_COMMENT' THEN CONCAT(
            COALESCE(SUBSTRING(h.old_comment, 1, 20), '无'),
            ' → ',
            COALESCE(SUBSTRING(h.new_comment, 1, 20), '无')
        )
        ELSE '批量操作'
    END AS change_summary,
    h.reason,
    h.create_time
FROM biz_grading_history h
WHERE h.exam_result_id = 1
ORDER BY h.create_time DESC;
*/

-- 2. 查询某教师的批阅历史统计
/*
SELECT
    grader_id,
    grader_name,
    COUNT(*) AS total_operations,
    SUM(CASE WHEN action_type = 'UPDATE_SCORE' THEN 1 ELSE 0 END) AS score_updates,
    SUM(CASE WHEN action_type = 'UPDATE_COMMENT' THEN 1 ELSE 0 END) AS comment_updates,
    MIN(create_time) AS first_operation,
    MAX(create_time) AS last_operation
FROM biz_grading_history
WHERE grader_id = 1
GROUP BY grader_id, grader_name;
*/

-- 3. 查询分数修改幅度较大的记录（超过10分）
/*
SELECT
    h.exam_result_id,
    e.paper_name,
    s.name AS student_name,
    h.grader_name,
    h.old_score,
    h.new_score,
    (h.new_score - h.old_score) AS score_change,
    h.reason,
    h.create_time
FROM biz_grading_history h
JOIN biz_exam_result e ON h.exam_result_id = e.id
JOIN biz_student s ON e.student_id = s.id
WHERE h.action_type = 'UPDATE_SCORE'
  AND ABS(h.new_score - h.old_score) > 10
ORDER BY ABS(h.new_score - h.old_score) DESC;
*/

-- 4. 查询最近7天的批阅操作
/*
SELECT
    DATE(create_time) AS operation_date,
    COUNT(*) AS total_operations,
    COUNT(DISTINCT grader_id) AS active_graders,
    COUNT(DISTINCT exam_result_id) AS affected_results
FROM biz_grading_history
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(create_time)
ORDER BY operation_date DESC;
*/

-- ============================================
-- 回滚脚本
-- ============================================

/*
-- 删除批阅历史表
DROP TABLE IF EXISTS biz_grading_history;
*/

-- ============================================
-- 注意事项
-- ============================================

/*
1. 此表用于审计和历史追踪，不应随意删除记录
2. 建议定期归档历史数据（如每年归档一次）
3. 索引已优化，查询性能良好
4. action_type 字段可根据需要扩展新的操作类型
5. 记录创建后不应修改，仅支持追加

操作类型说明：
- UPDATE_SCORE: 修改分数
- UPDATE_COMMENT: 修改评语
- BATCH_UPDATE: 批量修改（未来扩展）
- PUBLISH: 发布成绩（未来扩展）
- UNPUBLISH: 取消发布（未来扩展）
*/
