-- ========================================
-- 待办事项配置化 - 数据库脚本
-- 创建时间: 2026-01-10
-- 说明: 实现待办事项的配置化管理
-- ========================================

USE exam_system;

-- ========================================
-- 1. 创建待办事项配置表
-- ========================================

DROP TABLE IF EXISTS `sys_todo_config`;
CREATE TABLE `sys_todo_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(50) NOT NULL COMMENT '待办类型唯一标识',
  `title` varchar(100) NOT NULL COMMENT '待办标题',
  `icon` varchar(50) NULL DEFAULT NULL COMMENT '图标名称（Element Plus图标）',
  `color` varchar(100) NULL DEFAULT NULL COMMENT '渐变色CSS',
  `action` varchar(200) NULL DEFAULT NULL COMMENT '跳转路由（不含前导斜杠）',
  `mapper_method` varchar(100) NOT NULL COMMENT 'Mapper方法名',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前，相同按数量排）',
  `description` varchar(255) NULL DEFAULT NULL COMMENT '说明描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type` (`type`) USING BTREE COMMENT '待办类型唯一索引',
  INDEX `idx_enabled_sort` (`enabled`, `sort_order`) USING BTREE COMMENT '启用状态和排序索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '待办事项配置表' ROW_FORMAT = Dynamic;


-- ========================================
-- 2. 插入初始配置数据
-- ========================================

INSERT INTO `sys_todo_config`
(`type`, `title`, `icon`, `color`, `action`, `mapper_method`, `enabled`, `sort_order`, `description`)
VALUES
-- 待批改试卷
('pending_papers',
 '待批改试卷',
 'Document',
 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
 'score-manage?status=1',
 'getPendingPapersCount',
 1,
 10,
 '学生提交但尚未批改的试卷'),

-- 待审核题目
('pending_questions',
 '待审核题目',
 'Files',
 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
 'questions',
 'getPendingQuestionsCount',
 1,
 20,
 '教师导入或创建但尚未审核的题目');


-- ========================================
-- 3. 验证数据插入
-- ========================================

SELECT
    id AS 'ID',
    type AS '类型标识',
    title AS '标题',
    icon AS '图标',
    action AS '跳转路由',
    mapper_method AS 'Mapper方法',
    enabled AS '是否启用',
    sort_order AS '排序'
FROM sys_todo_config
ORDER BY sort_order;


-- ========================================
-- 4. 扩展示例（可选 - 注释掉）
-- ========================================

/*
-- 示例：添加"即将到期的考试"待办事项
INSERT INTO `sys_todo_config`
(`type`, `title`, `icon`, `color`, `action`, `mapper_method`, `enabled`, `sort_order`, `description`)
VALUES
('expiring_exams',
 '即将到期的考试',
 'Timer',
 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
 'papers?filter=expiring',
 'getExpiringExamsCount',
 1,
 30,
 '距离结束时间不足3天的考试');

-- 示例：添加"长期未批改的试卷"待办事项
INSERT INTO `sys_todo_config`
(`type`, `title`, `icon`, `color`, `action`, `mapper_method`, `enabled`, `sort_order`, `description`)
VALUES
('overdue_papers',
 '长期未批改',
 'Warning',
 'linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%)',
 'score-manage?status=1&overdue=true',
 'getOverduePapersCount',
 1,
 5,
 '提交超过7天仍未批改的试卷');
*/


-- ========================================
-- 5. 配置表说明
-- ========================================

/*
字段说明：

1. type: 待办类型的唯一标识，用于代码中区分不同的待办事项
2. title: 显示在前端的标题
3. icon: Element Plus 图标名称（Document, Files, Timer, Warning等）
4. color: CSS渐变色，用于待办事项图标背景
5. action: 点击"处理"按钮跳转的路由（不含前导斜杠）
6. mapper_method: DashboardMapper 中对应的查询方法名
7. enabled: 是否启用该待办项（1启用/0禁用）
8. sort_order: 排序序号，数字越小越靠前（相同序号按数量排序）
9. description: 待办事项的说明文字

使用方式：
1. 添加新待办：INSERT 新记录，并在 DashboardMapper 中实现对应的查询方法
2. 禁用待办：UPDATE enabled = 0
3. 调整顺序：UPDATE sort_order 字段
4. 修改样式：UPDATE icon, color 字段
*/


-- ========================================
-- 6. 回滚脚本
-- ========================================

/*
-- 如果需要回滚到硬编码方式，删除配置表：
DROP TABLE IF EXISTS sys_todo_config;
*/
