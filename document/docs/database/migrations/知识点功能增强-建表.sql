-- 知识点功能增强 - 数据库建表SQL
-- 创建时间：2026-01-08
-- 说明：增强知识点与试卷、学生、成绩的关联

-- ============================================
-- 1. 试卷-知识点关联表
-- ============================================
CREATE TABLE IF NOT EXISTS biz_paper_knowledge_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    paper_id BIGINT NOT NULL COMMENT '试卷ID',
    knowledge_point_id BIGINT NOT NULL COMMENT '知识点ID',
    question_count INT DEFAULT 0 COMMENT '该知识点的题目数量',
    total_score INT DEFAULT 0 COMMENT '该知识点的总分值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 唯一索引：一张试卷中同一个知识点只能有一条记录
    UNIQUE KEY uk_paper_kp (paper_id, knowledge_point_id),

    -- 普通索引：提高查询性能
    INDEX idx_paper (paper_id),
    INDEX idx_kp (knowledge_point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷-知识点关联表';

-- ============================================
-- 2. 学生知识点掌握度表
-- ============================================
CREATE TABLE IF NOT EXISTS biz_student_knowledge_mastery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    knowledge_point_id BIGINT NOT NULL COMMENT '知识点ID',
    correct_count INT DEFAULT 0 COMMENT '答对题数（累计）',
    total_count INT DEFAULT 0 COMMENT '总答题数（累计）',
    mastery_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '掌握度（百分比，0-100）',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 唯一索引：一个学生对一个知识点只能有一条掌握度记录
    UNIQUE KEY uk_student_kp (student_id, knowledge_point_id),

    -- 普通索引：提高查询性能
    INDEX idx_student (student_id),
    INDEX idx_kp (knowledge_point_id),
    INDEX idx_mastery (mastery_rate) COMMENT '用于查询薄弱知识点'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生知识点掌握度表';

-- ============================================
-- 说明
-- ============================================
-- biz_paper_knowledge_point 表：
--   - 记录每张试卷包含哪些知识点，以及每个知识点的题目数量和分值
--   - 在试卷保存后自动计算和插入
--   - 用于试卷详情页展示知识点分布图
--
-- biz_student_knowledge_mastery 表：
--   - 记录每个学生对各知识点的掌握情况
--   - 基于正确率计算：mastery_rate = (correct_count / total_count) * 100
--   - 累计方式统计，每次考试后更新
--   - 用于学生详情页展示知识点雷达图，以及Dashboard的薄弱知识点分析
