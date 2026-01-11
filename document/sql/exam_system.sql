/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : exam_system

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 10/01/2026 13:58:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_achievement
-- ----------------------------
DROP TABLE IF EXISTS `biz_achievement`;
CREATE TABLE `biz_achievement`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '成就名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '达成条件描述',
  `icon_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '徽章图标URL (可以是图片或Emoji)',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型: SIGN_IN_STREAK(连签), TOTAL_QUESTIONS(刷题数), PERFECT_PAPER(满分试卷)',
  `threshold` int NOT NULL COMMENT '达成阈值 (如 7, 100, 1)',
  `reward_points` int NULL DEFAULT 0 COMMENT '奖励积分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成就规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_ai_call_log
-- ----------------------------
DROP TABLE IF EXISTS `biz_ai_call_log`;
CREATE TABLE `biz_ai_call_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户类型（STUDENT/TEACHER）',
  `function_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '功能类型（analyze/grading/generate/extract）',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'AI提供商（deepseek/qwen等）',
  `success` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否成功（1:是, 0:否）',
  `response_time` bigint NULL DEFAULT NULL COMMENT '响应时间（毫秒）',
  `cached` tinyint(1) NULL DEFAULT 0 COMMENT '是否命中缓存（1:是, 0:否）',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `request_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求参数摘要',
  `tokens_used` int NULL DEFAULT NULL COMMENT 'Token消耗',
  `estimated_cost` decimal(10, 6) NULL DEFAULT NULL COMMENT '预估成本（元）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_function_type`(`function_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_success`(`success` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI调用日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_battle_record
-- ----------------------------
DROP TABLE IF EXISTS `biz_battle_record`;
CREATE TABLE `biz_battle_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `opponent_id` bigint NULL DEFAULT NULL COMMENT '对手ID(可为空)',
  `opponent_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对手名称/机器人名称',
  `result` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'WIN/LOSE/DRAW',
  `score_change` int NULL DEFAULT 0 COMMENT '积分变动',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE COMMENT '用于加速查询个人战绩'
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对战记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_class
-- ----------------------------
DROP TABLE IF EXISTS `biz_class`;
CREATE TABLE `biz_class`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班级名称',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级',
  `teacher_id` bigint NOT NULL COMMENT '班主任/创建教师ID (关联 sys_user)',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '6位邀请码，用于学生加入',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_class_student
-- ----------------------------
DROP TABLE IF EXISTS `biz_class_student`;
CREATE TABLE `biz_class_student`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_class_student`(`class_id` ASC, `student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班级成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_course
-- ----------------------------
DROP TABLE IF EXISTS `biz_course`;
CREATE TABLE `biz_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程简介',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程封面图',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '关联科目ID',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '适用年级',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '创建教师ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_course_comment
-- ----------------------------
DROP TABLE IF EXISTS `biz_course_comment`;
CREATE TABLE `biz_course_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `reply_id` bigint NULL DEFAULT NULL COMMENT '回复的父评论ID(可选)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程讨论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_course_resource
-- ----------------------------
DROP TABLE IF EXISTS `biz_course_resource`;
CREATE TABLE `biz_course_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint NOT NULL COMMENT '所属课程ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源/章节名称',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: VIDEO, PDF, PPT, LINK',
  `resource_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源地址',
  `knowledge_point_id` bigint NULL DEFAULT NULL COMMENT '关联知识点ID(可选)',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_exam_result
-- ----------------------------
DROP TABLE IF EXISTS `biz_exam_result`;
CREATE TABLE `biz_exam_result`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `paper_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '试卷名称快照',
  `score` int NOT NULL DEFAULT 0 COMMENT '得分',
  `total_score` int NOT NULL DEFAULT 0 COMMENT '试卷总分',
  `user_answers` json NULL COMMENT '用户答题详情(JSON)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '考试时间',
  `violation_count` int NULL DEFAULT 0 COMMENT '违规/切屏次数',
  `result_details` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '考试结果详情(存储AI评分和建议 JSON)',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '教师评语',
  `graded_by` bigint NULL DEFAULT NULL COMMENT '批阅教师ID',
  `graded_time` datetime NULL DEFAULT NULL COMMENT '批阅时间',
  `original_score` int NULL DEFAULT NULL COMMENT 'AI自动评分（原始分数）',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `published` tinyint(1) NULL DEFAULT 0 COMMENT '是否发布给学生查看(0-未发布 1-已发布)',
  `status` int NULL DEFAULT 1 COMMENT '状态: 0-未提交, 1-待批改（已提交未批阅）, 2-已批改（已批阅未发布）, 3-已发布',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_paper`(`student_id` ASC, `paper_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_paper_id`(`paper_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_published`(`published` ASC) USING BTREE,
  INDEX `idx_graded_by`(`graded_by` ASC) USING BTREE COMMENT '批阅教师索引',
  INDEX `idx_graded_time`(`graded_time` ASC) USING BTREE COMMENT '批阅时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '考试成绩记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_favorite
-- ----------------------------
DROP TABLE IF EXISTS `biz_favorite`;
CREATE TABLE `biz_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_question`(`student_id` ASC, `question_id` ASC) USING BTREE COMMENT '防止重复收藏'
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_goods
-- ----------------------------
DROP TABLE IF EXISTS `biz_goods`;
CREATE TABLE `biz_goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `price` int NOT NULL DEFAULT 0 COMMENT '兑换所需积分',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片/图标URL',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'AVATAR_FRAME' COMMENT '类型: AVATAR_FRAME(头像框), BACKGROUND(背景)',
  `resource_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源值(如css类名或图片地址)',
  `stock` int NULL DEFAULT -1 COMMENT '库存，-1表示无限',
  `is_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否上架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_grading_history
-- ----------------------------
DROP TABLE IF EXISTS `biz_grading_history`;
CREATE TABLE `biz_grading_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_result_id` bigint NOT NULL COMMENT '考试成绩ID',
  `grader_id` bigint NOT NULL COMMENT '批阅人ID（教师）',
  `grader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批阅人姓名',
  `action_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型：UPDATE_SCORE（修改分数）/UPDATE_COMMENT（修改评语）/BATCH_UPDATE（批量修改）',
  `old_score` int NULL DEFAULT NULL COMMENT '修改前分数',
  `new_score` int NULL DEFAULT NULL COMMENT '修改后分数',
  `old_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改前评语',
  `new_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改后评语',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改原因（可选）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_result_id`(`exam_result_id` ASC) USING BTREE COMMENT '成绩ID索引',
  INDEX `idx_grader_id`(`grader_id` ASC) USING BTREE COMMENT '批阅人ID索引',
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE COMMENT '操作时间索引',
  INDEX `idx_action_type`(`action_type` ASC) USING BTREE COMMENT '操作类型索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批阅历史记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_homework
-- ----------------------------
DROP TABLE IF EXISTS `biz_homework`;
CREATE TABLE `biz_homework`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL COMMENT '所属班级',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业标题',
  `paper_id` bigint NOT NULL COMMENT '关联的试卷ID',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_class`(`class_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班级作业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_homework_record
-- ----------------------------
DROP TABLE IF EXISTS `biz_homework_record`;
CREATE TABLE `biz_homework_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `homework_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `status` int NULL DEFAULT 0 COMMENT '0:未完成, 1:已完成',
  `score` int NULL DEFAULT NULL COMMENT '得分',
  `exam_result_id` bigint NULL DEFAULT NULL COMMENT '关联的考试结果ID',
  `submit_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_hw_student`(`homework_id` ASC, `student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作业提交记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_knowledge_point
-- ----------------------------
DROP TABLE IF EXISTS `biz_knowledge_point`;
CREATE TABLE `biz_knowledge_point`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `subject_id` bigint NOT NULL COMMENT '外键, 关联 biz_subject.id',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识点编码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细描述',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签, 多个用逗号隔开',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_subject_id`(`subject_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 342 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '知识点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_knowledge_point_relation
-- ----------------------------
DROP TABLE IF EXISTS `biz_knowledge_point_relation`;
CREATE TABLE `biz_knowledge_point_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL COMMENT '前置知识点ID (父节点)',
  `child_id` bigint NOT NULL COMMENT '后置知识点ID (子节点)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_relation`(`parent_id` ASC, `child_id` ASC) USING BTREE COMMENT '防止重复关联'
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识点关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_learning_activity
-- ----------------------------
DROP TABLE IF EXISTS `biz_learning_activity`;
CREATE TABLE `biz_learning_activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `activity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动类型 (例如: PRACTICE, EXAM)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习活动日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_paper
-- ----------------------------
DROP TABLE IF EXISTS `biz_paper`;
CREATE TABLE `biz_paper`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '试卷名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '试卷编码',
  `subject_id` bigint NOT NULL COMMENT '所属科目ID',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `total_score` int NULL DEFAULT 100 COMMENT '总分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `paper_type` int NULL DEFAULT 1 COMMENT '试卷类型: 1-手动选题, 2-图片拼接',
  `status` int NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_paper_subject_grade`(`subject_id` ASC, `grade` ASC) USING BTREE,
  INDEX `idx_paper_status`(`status` ASC) USING BTREE,
  INDEX `idx_paper_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_paper_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_paper_group
-- ----------------------------
DROP TABLE IF EXISTS `biz_paper_group`;
CREATE TABLE `biz_paper_group`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `paper_id` bigint NOT NULL COMMENT '所属试卷ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组标题 (例如: 一、选择题)',
  `sort_order` int NULL DEFAULT 0 COMMENT '分组排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_paper_sort`(`paper_id` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷题目分组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_paper_image
-- ----------------------------
DROP TABLE IF EXISTS `biz_paper_image`;
CREATE TABLE `biz_paper_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片访问地址',
  `sort_order` int NULL DEFAULT 0 COMMENT '图片排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pi_paper_sort`(`paper_id` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 88 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷图片关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_paper_knowledge_point
-- ----------------------------
DROP TABLE IF EXISTS `biz_paper_knowledge_point`;
CREATE TABLE `biz_paper_knowledge_point`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `knowledge_point_id` bigint NOT NULL COMMENT '知识点ID',
  `question_count` int NULL DEFAULT 0 COMMENT '该知识点的题目数量',
  `total_score` int NULL DEFAULT 0 COMMENT '该知识点的总分值',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_paper_kp`(`paper_id` ASC, `knowledge_point_id` ASC) USING BTREE,
  INDEX `idx_paper`(`paper_id` ASC) USING BTREE,
  INDEX `idx_kp`(`knowledge_point_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试卷-知识点关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_paper_question
-- ----------------------------
DROP TABLE IF EXISTS `biz_paper_question`;
CREATE TABLE `biz_paper_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `paper_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `score` int NOT NULL COMMENT '分值',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `group_id` bigint NULL DEFAULT NULL COMMENT '所属分组ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pq_paper_sort`(`paper_id` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_pq_group`(`group_id` ASC) USING BTREE,
  INDEX `idx_pq_question`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 223 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷题目关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_question
-- ----------------------------
DROP TABLE IF EXISTS `biz_question`;
CREATE TABLE `biz_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_id` bigint NOT NULL COMMENT '关联科目ID',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `question_type` int NOT NULL COMMENT '题型: 1-单选, 2-多选, 3-填空, 4-判断, 5-主观题',
  `audit_status` int NULL DEFAULT 1 COMMENT '审核状态：0-待审核，1-已通过，2-未通过',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题干',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目图片地址,多个逗号隔开',
  `options` json NULL COMMENT '选项, 仅对选择题有效, 格式: [{\"key\":\"A\",\"value\":\"选项一\"},{\"key\":\"B\",\"value\":\"选项二\"}]',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参考答案, 多选题答案用逗号隔开, 填空题答案用###隔开',
  `answer_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '答案图片地址',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '题目解析',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `difficulty` double NULL DEFAULT 0.5 COMMENT '难度系数(0.1-1.0)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question_subject_id`(`subject_id` ASC) USING BTREE,
  INDEX `idx_audit_status_create_time`(`audit_status` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2284 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_question_knowledge_point
-- ----------------------------
DROP TABLE IF EXISTS `biz_question_knowledge_point`;
CREATE TABLE `biz_question_knowledge_point`  (
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `knowledge_point_id` bigint NOT NULL COMMENT '知识点ID',
  PRIMARY KEY (`question_id`, `knowledge_point_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试题知识点关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_sign_in
-- ----------------------------
DROP TABLE IF EXISTS `biz_sign_in`;
CREATE TABLE `biz_sign_in`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `sign_date` date NOT NULL COMMENT '签到日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_date`(`student_id` ASC, `sign_date` ASC) USING BTREE COMMENT '每日只能签到一次',
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生签到表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_student
-- ----------------------------
DROP TABLE IF EXISTS `biz_student`;
CREATE TABLE `biz_student`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号, 唯一',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'BCrypt加密后的密码',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生头像URL',
  `subject_id` bigint NOT NULL COMMENT '所属科目ID',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级 (例如: 2009级)',
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `points` int NULL DEFAULT 0 COMMENT '学习积分',
  `avatar_frame_style` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前头像框CSS',
  `background_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前背景图URL',
  `total_answered` int NULL DEFAULT 0 COMMENT '累计答题数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no` ASC) USING BTREE,
  UNIQUE INDEX `idx_student_no`(`student_no` ASC) USING BTREE,
  INDEX `idx_student_subject`(`subject_id` ASC) USING BTREE,
  INDEX `idx_student_grade`(`grade` ASC) USING BTREE,
  INDEX `idx_student_name`(`name` ASC) USING BTREE,
  INDEX `idx_student_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_student_subject_grade`(`subject_id` ASC, `grade` ASC) USING BTREE,
  INDEX `idx_student_class`(`class_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 205 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_student_knowledge_mastery
-- ----------------------------
DROP TABLE IF EXISTS `biz_student_knowledge_mastery`;
CREATE TABLE `biz_student_knowledge_mastery`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `knowledge_point_id` bigint NOT NULL COMMENT '知识点ID',
  `correct_count` int NULL DEFAULT 0 COMMENT '答对题数（累计）',
  `total_count` int NULL DEFAULT 0 COMMENT '总答题数（累计）',
  `mastery_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '掌握度（百分比，0-100）',
  `last_update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_kp`(`student_id` ASC, `knowledge_point_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_kp`(`knowledge_point_id` ASC) USING BTREE,
  INDEX `idx_mastery`(`mastery_rate` ASC) USING BTREE COMMENT '用于查询薄弱知识点'
) ENGINE = InnoDB AUTO_INCREMENT = 127 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生知识点掌握度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_student_paper
-- ----------------------------
DROP TABLE IF EXISTS `biz_student_paper`;
CREATE TABLE `biz_student_paper`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '得分',
  `status` int NULL DEFAULT 0 COMMENT '状态：0-未提交，1-已提交待批改，2-已批改',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始答题时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束答题时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_paper`(`paper_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_submit_time`(`submit_time` ASC) USING BTREE,
  INDEX `idx_status_submit_time`(`status` ASC, `submit_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生试卷表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_subject
-- ----------------------------
DROP TABLE IF EXISTS `biz_subject`;
CREATE TABLE `biz_subject`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '科目名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_grade_name`(`grade` ASC, `name` ASC) USING BTREE,
  INDEX `idx_subject_grade`(`grade` ASC) USING BTREE,
  INDEX `idx_subject_name`(`name` ASC) USING BTREE,
  INDEX `idx_subject_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_subject_grade_create_time`(`grade` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 88 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '科目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_user_achievement
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_achievement`;
CREATE TABLE `biz_user_achievement`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `achievement_id` bigint NOT NULL COMMENT '成就ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_ach`(`student_id` ASC, `achievement_id` ASC) USING BTREE COMMENT '每个成就每人只能获得一次'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户成就表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_user_goods
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_goods`;
CREATE TABLE `biz_user_goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_goods`(`student_id` ASC, `goods_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户商品持有表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_wrong_record
-- ----------------------------
DROP TABLE IF EXISTS `biz_wrong_record`;
CREATE TABLE `biz_wrong_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `paper_id` bigint NULL DEFAULT NULL COMMENT '可选, 关联试卷ID',
  `wrong_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学生的错误答案',
  `wrong_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误原因分析',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_mastered` tinyint(1) NULL DEFAULT 0 COMMENT '是否已掌握 (1:是, 0:否)',
  `review_count` int NULL DEFAULT 0 COMMENT '已成功复习次数',
  `next_review_time` datetime NULL DEFAULT NULL COMMENT '计划下次复习时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_paper_id`(`paper_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '错题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名',
  `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录IP地址',
  `log_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志类型 (LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT)',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器用户代理',
  `log_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志记录时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6390 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notification
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '通知内容',
  `is_published` tinyint(1) NULL DEFAULT 0 COMMENT '是否发布 (1:是, 0:否)',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ALL' COMMENT '发布对象: ALL-全体, STUDENT-学生, TEACHER-教师/管理员',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '操作人员',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1045 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称 (e.g., 用户管理)',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识 (e.g., sys:user:list)',
  `type` tinyint NOT NULL COMMENT '类型 (1:菜单, 2:按钮/操作)',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父权限ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码, 如 \"ADMIN\", \"TEACHER\"',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_todo_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_todo_config`;
CREATE TABLE `sys_todo_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '待办类型唯一标识',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '待办标题',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标名称（Element Plus图标）',
  `color` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '渐变色CSS',
  `action` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转路由（不含前导斜杠）',
  `mapper_method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Mapper方法名',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前，相同按数量排）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '说明描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type`(`type` ASC) USING BTREE COMMENT '待办类型唯一索引',
  INDEX `idx_enabled_sort`(`enabled` ASC, `sort_order` ASC) USING BTREE COMMENT '启用状态和排序索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '待办事项配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名, 唯一',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码, BCrypt加密存储',
  `nick_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `is_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '账户是否启用 (1:是, 0:否)',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记 (0:否, 1:是)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_notification
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_notification`;
CREATE TABLE `sys_user_notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型: SCORE_UPDATE-成绩更新, COMMENT_UPDATE-评语更新, SYSTEM-系统通知',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联业务ID（如成绩ID）',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户通知记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_preference
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_preference`;
CREATE TABLE `sys_user_preference`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `preference_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键 (如: dashboard_layout, theme)',
  `preference_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配置值 (JSON格式)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_key`(`user_id` ASC, `preference_key` ASC) USING BTREE COMMENT '用户+配置键唯一索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户个性化配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
