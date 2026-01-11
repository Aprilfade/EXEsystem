-- ========================================
-- 班级管理功能优化 - 数据库脚本
-- 执行时间：2026-01-05
-- 说明：添加班级管理相关表和权限
-- ========================================

USE exam_system;

-- ----------------------------
-- 1. 班级表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '班级名称',
  `grade` varchar(50) DEFAULT NULL COMMENT '年级',
  `teacher_id` bigint NOT NULL COMMENT '班主任/创建教师ID (关联 sys_user)',
  `code` varchar(20) NOT NULL COMMENT '6位邀请码，用于学生加入',
  `description` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ----------------------------
-- 2. 班级学生关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_class_student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_student` (`class_id`,`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级成员表';

-- ----------------------------
-- 3. 作业表 (关联试卷)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_homework` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL COMMENT '所属班级',
  `title` varchar(200) NOT NULL COMMENT '作业标题',
  `paper_id` bigint NOT NULL COMMENT '关联的试卷ID',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_class` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级作业表';

-- ----------------------------
-- 4. 作业提交记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_homework_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `homework_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `status` int DEFAULT 0 COMMENT '0:未完成, 1:已完成',
  `score` int DEFAULT NULL COMMENT '得分',
  `exam_result_id` bigint DEFAULT NULL COMMENT '关联的考试结果ID',
  `submit_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hw_student` (`homework_id`,`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交记录';

-- ----------------------------
-- 5. 学生表添加班级字段
-- ----------------------------
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_student'
  AND COLUMN_NAME = 'class_name';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `biz_student` ADD COLUMN `class_name` VARCHAR(50) DEFAULT NULL COMMENT ''班级名称'' AFTER `grade`;',
    'SELECT ''字段 class_name 已存在，跳过添加'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为班级字段添加索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME = 'biz_student'
  AND INDEX_NAME = 'idx_student_class';

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_student_class ON biz_student(class_name);',
    'SELECT ''索引 idx_student_class 已存在'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 6. 添加班级管理权限
-- ----------------------------
-- 检查权限是否已存在
SET @perm_exists = 0;
SELECT COUNT(*) INTO @perm_exists
FROM sys_permission
WHERE code = 'sys:class:list';

-- 如果不存在则插入班级管理权限
SET @sql = IF(@perm_exists = 0,
    'INSERT INTO `sys_permission` (`id`, `name`, `code`, `type`, `parent_id`) VALUES (28, ''班级管理'', ''sys:class:list'', 1, 0);',
    'SELECT ''权限 sys:class:list 已存在'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查编辑班级权限是否已存在
SET @perm_exists = 0;
SELECT COUNT(*) INTO @perm_exists
FROM sys_permission
WHERE code = 'sys:class:edit';

SET @sql = IF(@perm_exists = 0,
    'INSERT INTO `sys_permission` (`id`, `name`, `code`, `type`, `parent_id`) VALUES (29, ''编辑班级'', ''sys:class:edit'', 2, 28);',
    'SELECT ''权限 sys:class:edit 已存在'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查删除班级权限是否已存在
SET @perm_exists = 0;
SELECT COUNT(*) INTO @perm_exists
FROM sys_permission
WHERE code = 'sys:class:delete';

SET @sql = IF(@perm_exists = 0,
    'INSERT INTO `sys_permission` (`id`, `name`, `code`, `type`, `parent_id`) VALUES (30, ''删除班级'', ''sys:class:delete'', 2, 28);',
    'SELECT ''权限 sys:class:delete 已存在'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 7. 为管理员角色分配班级管理权限
-- ----------------------------
-- 获取班级管理权限ID
SET @class_list_id = (SELECT id FROM sys_permission WHERE code = 'sys:class:list');
SET @class_edit_id = (SELECT id FROM sys_permission WHERE code = 'sys:class:edit');
SET @class_delete_id = (SELECT id FROM sys_permission WHERE code = 'sys:class:delete');

-- 为管理员角色(role_id=2)分配权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES (2, @class_list_id);
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES (2, @class_edit_id);
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES (2, @class_delete_id);

-- ----------------------------
-- 8. 验证结果
-- ----------------------------
SELECT '✅ 班级管理功能优化完成！' AS '状态';

SELECT
    TABLE_NAME AS '表名',
    TABLE_COMMENT AS '说明'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'exam_system'
  AND TABLE_NAME IN ('biz_class', 'biz_class_student', 'biz_homework', 'biz_homework_record')
ORDER BY TABLE_NAME;

SELECT
    name AS '权限名称',
    code AS '权限标识',
    type AS '类型'
FROM sys_permission
WHERE code LIKE 'sys:class:%'
ORDER BY id;

SELECT '数据库优化完成' AS '状态';
