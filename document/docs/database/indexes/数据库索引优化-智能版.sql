-- ============================================
-- 数据库索引优化 - 智能版（自动跳过已存在索引）
-- ============================================

USE exam_system;

DELIMITER $$

-- 创建存储过程：安全创建索引
DROP PROCEDURE IF EXISTS safe_add_index$$
CREATE PROCEDURE safe_add_index(
    IN table_name VARCHAR(100),
    IN index_name VARCHAR(100),
    IN columns VARCHAR(200)
)
BEGIN
    DECLARE index_exists INT DEFAULT 0;

    -- 检查索引是否存在
    SELECT COUNT(*) INTO index_exists
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'exam_system'
      AND TABLE_NAME = table_name
      AND INDEX_NAME = index_name;

    -- 如果不存在则创建
    IF index_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' ADD INDEX ', index_name, ' (', columns, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('✓ 成功创建索引: ', index_name, ' on ', table_name) AS result;
    ELSE
        SELECT CONCAT('- 索引已存在，跳过: ', index_name, ' on ', table_name) AS result;
    END IF;
END$$

DELIMITER ;

-- ============================================
-- 执行索引创建（自动跳过已存在的）
-- ============================================

-- 第1部分：成绩管理表
CALL safe_add_index('biz_exam_result', 'idx_student_id', 'student_id');
CALL safe_add_index('biz_exam_result', 'idx_paper_id_exam', 'paper_id');
CALL safe_add_index('biz_exam_result', 'idx_exam_result_student_paper', 'student_id, paper_id');
CALL safe_add_index('biz_exam_result', 'idx_score_time', 'score, create_time');

-- 第2部分：错题本表
CALL safe_add_index('biz_wrong_record', 'idx_student_id_wrong', 'student_id');
CALL safe_add_index('biz_wrong_record', 'idx_question_id_wrong', 'question_id');
CALL safe_add_index('biz_wrong_record', 'idx_kp_id_wrong', 'knowledge_point_id');
CALL safe_add_index('biz_wrong_record', 'idx_wrong_record_student_kp', 'student_id, knowledge_point_id');

-- 第3部分：试卷题目关联表
CALL safe_add_index('biz_paper_question', 'idx_paper_id_pq', 'paper_id');
CALL safe_add_index('biz_paper_question', 'idx_question_id_pq', 'question_id');
CALL safe_add_index('biz_paper_question', 'idx_paper_sort', 'paper_id, sort_order');

-- 第4部分：题目表
CALL safe_add_index('biz_question', 'idx_subject_id_q', 'subject_id');
CALL safe_add_index('biz_question', 'idx_question_type', 'question_type');
CALL safe_add_index('biz_question', 'idx_grade_q', 'grade');
CALL safe_add_index('biz_question', 'idx_difficulty', 'difficulty');
CALL safe_add_index('biz_question', 'idx_question_composite', 'subject_id, question_type, difficulty');

-- 第5部分：题目知识点关联表
CALL safe_add_index('biz_question_knowledge_point', 'idx_question_id_qkp', 'question_id');
CALL safe_add_index('biz_question_knowledge_point', 'idx_kp_id_qkp', 'knowledge_point_id');

-- 第6部分：学生表
CALL safe_add_index('biz_student', 'idx_username_student', 'username');
CALL safe_add_index('biz_student', 'idx_class_id', 'class_id');
CALL safe_add_index('biz_student', 'idx_student_no', 'student_no');
CALL safe_add_index('biz_student', 'idx_name_student', 'name');

-- 第7部分：知识点表
CALL safe_add_index('biz_knowledge_point', 'idx_subject_id_kp', 'subject_id');
CALL safe_add_index('biz_knowledge_point', 'idx_code_kp', 'code');
CALL safe_add_index('biz_knowledge_point', 'idx_grade_kp', 'grade');

-- 第8部分：试卷表
CALL safe_add_index('biz_paper', 'idx_subject_id_paper', 'subject_id');
CALL safe_add_index('biz_paper', 'idx_create_time_paper', 'create_time');

-- 第9部分：班级表
CALL safe_add_index('biz_class', 'idx_teacher_id', 'teacher_id');
CALL safe_add_index('biz_class', 'idx_invite_code', 'invite_code');

-- 第10部分：学习活动表
CALL safe_add_index('biz_learning_activity', 'idx_student_activity', 'student_id, activity_type');
CALL safe_add_index('biz_learning_activity', 'idx_create_time_activity', 'create_time');

-- 第11部分：签到记录表
CALL safe_add_index('biz_sign_in', 'idx_student_date', 'student_id, sign_date');

-- 第12部分：AI调用日志表
CALL safe_add_index('biz_ai_call_log', 'idx_user_id_ai', 'user_id');
CALL safe_add_index('biz_ai_call_log', 'idx_call_time', 'call_time');
CALL safe_add_index('biz_ai_call_log', 'idx_provider', 'provider');

-- 第13部分：系统用户表
CALL safe_add_index('sys_user', 'idx_username_user', 'username');
CALL safe_add_index('sys_user', 'idx_status_user', 'status');

-- 第14部分：操作日志表
CALL safe_add_index('sys_oper_log', 'idx_operator', 'operator_name');
CALL safe_add_index('sys_oper_log', 'idx_oper_time', 'oper_time');
CALL safe_add_index('sys_oper_log', 'idx_business_type', 'business_type');

-- 第15部分：登录日志表
CALL safe_add_index('sys_login_log', 'idx_username_login', 'username');
CALL safe_add_index('sys_login_log', 'idx_login_time', 'login_time');
CALL safe_add_index('sys_login_log', 'idx_ip', 'ip_address');

-- ============================================
-- 清理存储过程
-- ============================================
DROP PROCEDURE IF EXISTS safe_add_index;

-- ============================================
-- 验证结果
-- ============================================
SELECT
    '索引优化完成！' AS '状态',
    COUNT(DISTINCT INDEX_NAME) AS '总索引数'
FROM
    information_schema.STATISTICS
WHERE
    TABLE_SCHEMA = 'exam_system'
    AND INDEX_NAME LIKE 'idx_%';

-- 查看各表的索引情况
SELECT
    TABLE_NAME AS '表名',
    COUNT(DISTINCT INDEX_NAME) - 1 AS '索引数量（不含主键）'
FROM
    information_schema.STATISTICS
WHERE
    TABLE_SCHEMA = 'exam_system'
    AND TABLE_NAME IN (
        'biz_exam_result', 'biz_wrong_record', 'biz_paper_question',
        'biz_question', 'biz_question_knowledge_point', 'biz_student',
        'biz_knowledge_point', 'biz_paper', 'biz_class',
        'biz_learning_activity', 'biz_sign_in', 'biz_ai_call_log',
        'sys_user', 'sys_oper_log', 'sys_login_log'
    )
GROUP BY
    TABLE_NAME
ORDER BY
    TABLE_NAME;
