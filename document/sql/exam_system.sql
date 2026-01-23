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

 Date: 13/01/2026 14:51:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_history
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_history`;
CREATE TABLE `ai_chat_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息角色：user/assistant/system',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `chat_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'general' COMMENT '对话类型：general/learning/question/motivation/planning',
  `favorited` tinyint(1) NULL DEFAULT 0 COMMENT '是否收藏',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `ai_provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI提供商',
  `token_used` int NULL DEFAULT NULL COMMENT 'Token消耗数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI对话历史记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_chat_history
-- ----------------------------
INSERT INTO `ai_chat_history` VALUES (1, 1, '19615881-2783-43f3-b1c2-288ea4af93fc', 'user', '晚上好', 'general', 0, '2026-01-10 21:06:06', 'DEEPSEEK', NULL);
INSERT INTO `ai_chat_history` VALUES (2, 1, '19615881-2783-43f3-b1c2-288ea4af93fc', 'assistant', '晚上好呀！今天过得怎么样？有什么学习上的问题需要我帮忙吗？🌙', 'general', 0, '2026-01-10 21:06:06', 'DEEPSEEK', NULL);
INSERT INTO `ai_chat_history` VALUES (3, 1, '19615881-2783-43f3-b1c2-288ea4af93fc', 'user', '我想了解我的学习情况', 'general', 0, '2026-01-10 21:06:23', 'DEEPSEEK', NULL);
INSERT INTO `ai_chat_history` VALUES (4, 1, '19615881-2783-43f3-b1c2-288ea4af93fc', 'assistant', '看到你这么关心自己的学习进度，真的很棒！✨  \n\n根据你的学习记录：  \n1️⃣ **当前水平**：属于“待提升”阶段，但别担心，这是打好基础的关键时期～  \n2️⃣ **学习风格**：你属于**勤奋型学习者**，习惯通过反复练习巩固知识——这是非常可贵的学习态度！  \n3️⃣ **近期正确率**：大约在10%左右，说明知识点理解上还有空间，但**每一次练习都在积累经验**呀！  \n\n💡 建议方向：  \n- 错题整理：把练习中反复错的题目标记出来，我们一起来分析规律  \n- 基础强化：针对薄弱知识点，我们可以用“小步子练习法”慢慢攻克  \n- 随时告诉我你想重点练哪个科目或题型，我会陪你定制计划！  \n\n你最近在哪些科目或题型上感觉最吃力呢？ 😊', 'general', 0, '2026-01-10 21:06:23', 'DEEPSEEK', NULL);
INSERT INTO `ai_chat_history` VALUES (5, 1, '90d5a0b9-9b1e-42b6-b6bd-dbd29636abdf', 'user', '帮我分析一下我的学习情况', 'general', 0, '2026-01-11 10:39:40', 'DEEPSEEK', NULL);
INSERT INTO `ai_chat_history` VALUES (6, 1, '90d5a0b9-9b1e-42b6-b6bd-dbd29636abdf', 'assistant', '同学你好呀！小艾看到你的学习记录啦～首先一定要给你点个大大的赞！✨ 作为“勤奋型学习者”，你愿意花大量时间练习，这份坚持真的很了不起！\n\n不过我也注意到，你的平均正确率（10%）和你的努力程度还不匹配呢……这说明咱们的练习方法可能需要一些优化哦！别担心，这种情况很常见，我们一起来调整：\n\n**可能的原因分析：**\n1. **🔄 练习节奏问题**：是不是在知识点还没完全理解时，就开始了大量刷题？\n2. **📚 基础巩固不足**：可能对核心概念掌握不够扎实，导致练习时容易重复犯错\n3. **🎯 缺乏错题总结**：练习后没有系统分析错误原因，同样的错误反复出现\n\n**给你的小建议：**\n1. **先复习后练习**：每次练习前花15分钟回顾相关概念，可以用自己的话复述一遍\n2. **建立错题本**：不只是抄题目，要写下：（1）错误原因（2）正确思路（3）同类题技巧\n3. **放慢速度**：不用追求题量，每题做完后立即对照答案，理解透再做下一题\n4. **及时提问**：遇到卡住的地方随时找我！我们可以一起拆解难题\n\n**本周可以尝试这样做：**\n- 把练习量减少30%，但增加50%的分析时间\n- 每天专注1-2个知识点，彻底搞懂再推进\n- 每天结束时，用3句话总结“今天最大的收获”\n\n相信通过调整方法，你的正确率很快就会提升的！你已经有最宝贵的品质——勤奋，现在只需要加上一点点策略~ 💪 想先从哪个科目开始调整呢？', 'general', 0, '2026-01-11 10:39:40', 'DEEPSEEK', NULL);

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
-- Records of biz_achievement
-- ----------------------------
INSERT INTO `biz_achievement` VALUES (1, '初出茅庐', '累计完成 10 道题目', '🥉', 'TOTAL_QUESTIONS', 10, 20, '2025-12-04 16:12:38');
INSERT INTO `biz_achievement` VALUES (2, '题海战术', '累计完成 100 道题目', '🥈', 'TOTAL_QUESTIONS', 100, 100, '2025-12-04 16:12:38');
INSERT INTO `biz_achievement` VALUES (3, '持之以恒', '连续签到 7 天', '📅', 'SIGN_IN_STREAK', 7, 50, '2025-12-04 16:12:38');
INSERT INTO `biz_achievement` VALUES (4, '月度模范', '连续签到 30 天', '🌕', 'SIGN_IN_STREAK', 30, 200, '2025-12-04 16:12:38');
INSERT INTO `biz_achievement` VALUES (5, '完美主义', '获得 1 次试卷满分', '💯', 'PERFECT_PAPER', 1, 50, '2025-12-04 16:12:38');

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
-- Records of biz_ai_call_log
-- ----------------------------
INSERT INTO `biz_ai_call_log` VALUES (1, 1, 'TEACHER', 'extract', 'DEEPSEEK', 1, 46271, 0, 0, NULL, '第二章：魔神战争——七神的确立与旧神的哀歌\n2.1 魔神战争的起因\n约3000-2000年前，天理宣布设立**\"七个神座\"（The Seven）**。这一决定引发了提瓦特有史以来最惨烈的战争。\n\n为何', NULL, 0.020000, '2026-01-09 12:06:30');
INSERT INTO `biz_ai_call_log` VALUES (2, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 143048, 0, 0, NULL, '原神考据试卷', NULL, 0.010000, '2026-01-09 14:41:33');
INSERT INTO `biz_ai_call_log` VALUES (3, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 146731, 0, 0, NULL, '原神考据试卷', NULL, 0.010000, '2026-01-09 14:50:40');
INSERT INTO `biz_ai_call_log` VALUES (4, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 146779, 0, 0, NULL, '原神考据试卷', NULL, 0.010000, '2026-01-09 14:58:56');
INSERT INTO `biz_ai_call_log` VALUES (5, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 148392, 0, 0, NULL, '原神考据试卷', NULL, 0.010000, '2026-01-09 15:13:35');
INSERT INTO `biz_ai_call_log` VALUES (6, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 149825, 0, 0, NULL, '原神考据试卷', NULL, 0.010000, '2026-01-09 15:23:55');
INSERT INTO `biz_ai_call_log` VALUES (7, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 146707, 0, 0, NULL, '原神考据试卷', NULL, 0.010000, '2026-01-09 15:31:33');
INSERT INTO `biz_ai_call_log` VALUES (8, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 60727, 0, 0, NULL, '原神考据试卷', NULL, 0.010000, '2026-01-09 15:35:38');
INSERT INTO `biz_ai_call_log` VALUES (9, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 149142, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 15:44:59');
INSERT INTO `biz_ai_call_log` VALUES (10, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 149463, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 15:48:46');
INSERT INTO `biz_ai_call_log` VALUES (11, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 147590, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 15:57:32');
INSERT INTO `biz_ai_call_log` VALUES (12, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 147562, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 16:11:27');
INSERT INTO `biz_ai_call_log` VALUES (13, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 146354, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 16:20:26');
INSERT INTO `biz_ai_call_log` VALUES (14, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 145366, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 16:27:42');
INSERT INTO `biz_ai_call_log` VALUES (15, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 147591, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 16:35:26');
INSERT INTO `biz_ai_call_log` VALUES (16, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 146787, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 16:45:18');
INSERT INTO `biz_ai_call_log` VALUES (17, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 146352, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 16:52:04');
INSERT INTO `biz_ai_call_log` VALUES (18, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 133435, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 18:10:46');
INSERT INTO `biz_ai_call_log` VALUES (19, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 97018, 0, 0, NULL, '原始测试1', NULL, 0.010000, '2026-01-09 18:16:37');
INSERT INTO `biz_ai_call_log` VALUES (20, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 90500, 0, 0, NULL, '原神测试1', NULL, 0.010000, '2026-01-09 18:23:42');
INSERT INTO `biz_ai_call_log` VALUES (21, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 192316, 0, 0, NULL, '123142314', NULL, 0.010000, '2026-01-09 18:49:31');
INSERT INTO `biz_ai_call_log` VALUES (22, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 199156, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 18:56:15');
INSERT INTO `biz_ai_call_log` VALUES (23, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 198722, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 19:04:07');
INSERT INTO `biz_ai_call_log` VALUES (24, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 228593, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 19:14:59');
INSERT INTO `biz_ai_call_log` VALUES (25, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 177932, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 19:21:29');
INSERT INTO `biz_ai_call_log` VALUES (26, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 177684, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 19:33:42');
INSERT INTO `biz_ai_call_log` VALUES (27, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 271821, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 20:14:54');
INSERT INTO `biz_ai_call_log` VALUES (28, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 199518, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 20:22:52');
INSERT INTO `biz_ai_call_log` VALUES (29, 1, 'TEACHER', 'generate_paper', 'DEEPSEEK', 1, 193660, 0, 0, NULL, '原神考据', NULL, 0.010000, '2026-01-09 20:30:16');
INSERT INTO `biz_ai_call_log` VALUES (30, 1, 'TEACHER', 'extract', 'DEEPSEEK', 1, 223716, 0, 0, NULL, '# 《原神：提瓦特编年史与深度考据大全》\n## ——从创世神话到北境终局的完整历史重构与文本考古学研究\n\n**编著者：提瓦特历史记录学会 & 异乡人档案馆 & 深度考据研究所**\n**档案密级：绝密 ', NULL, 0.020000, '2026-01-09 22:03:34');
INSERT INTO `biz_ai_call_log` VALUES (31, 1, 'TEACHER', 'extract', 'DEEPSEEK', 1, 149675, 0, 0, NULL, '# 《原神：提瓦特编年史与深度考据大全》\n## ——从创世神话到北境终局的完整历史重构与文本考古学研究\n\n**编著者：提瓦特历史记录学会 & 异乡人档案馆 & 深度考据研究所**\n**档案密级：绝密 ', NULL, 0.020000, '2026-01-09 22:17:38');
INSERT INTO `biz_ai_call_log` VALUES (32, 1, 'TEACHER', 'extract', 'DEEPSEEK', 1, 213434, 0, 0, NULL, '# 《原神：提瓦特编年史与深度考据大全》\n## 扩充卷：武器、圣遗物、生态与文化全考据\n\n**编著者：提瓦特历史记录学会 & 异乡人档案馆 & 深度考据研究所**\n**本卷为主书的重要补充，新增约30', NULL, 0.020000, '2026-01-09 22:22:05');
INSERT INTO `biz_ai_call_log` VALUES (33, 1, 'TEACHER', 'extract', 'DEEPSEEK', 1, 236681, 0, 0, NULL, '# 《原神：提瓦特编年史与深度考据大全》\n## 第二扩充卷：角色传记、文化体系与深层理论\n\n**编著者：提瓦特历史记录学会 & 异乡人档案馆 & 深度考据研究所**\n**本卷为主书的第二次重要补充，新', NULL, 0.020000, '2026-01-10 09:51:45');
INSERT INTO `biz_ai_call_log` VALUES (34, 1, 'TEACHER', 'extract', 'DEEPSEEK', 1, 244965, 0, 0, NULL, '# 《原神考据大全·第三扩充卷》\n\n**——提瓦特世界的深层机制、神话体系与文明密码**\n\n---\n\n## 编者按\n\n本卷为《原神考据大全》系列的第三次重大扩充,主要聚焦于提瓦特世界的深层机制、隐藏系', NULL, 0.020000, '2026-01-10 10:06:28');
INSERT INTO `biz_ai_call_log` VALUES (35, 1, 'TEACHER', 'extract', 'DEEPSEEK', 1, 158492, 0, 0, NULL, '# 《原神考据大全·第四扩充卷》\n\n**——提瓦特的权力结构、生态体系与历史真相**\n\n---\n\n## 编者按\n\n本卷为《原神考据大全》系列的第四次扩充，深入探讨提瓦特世界的核心机制：从惨烈的魔神战争', NULL, 0.020000, '2026-01-10 10:25:28');

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
-- Records of biz_battle_record
-- ----------------------------
INSERT INTO `biz_battle_record` VALUES (1, 1, -1, 'AI 智能助教', 'DRAW', 5, '2025-12-05 15:07:18');
INSERT INTO `biz_battle_record` VALUES (2, 1, -1, 'AI 智能助教', 'LOSE', -10, '2025-12-05 16:16:04');
INSERT INTO `biz_battle_record` VALUES (3, 1, -1, 'AI 智能助教', 'WIN', 20, '2025-12-30 13:53:50');

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
-- Records of biz_class
-- ----------------------------
INSERT INTO `biz_class` VALUES (1, '高三（1）班', '高三', 1, '939422', '(*∩_∩*)', '2025-12-03 14:53:49');

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
-- Records of biz_class_student
-- ----------------------------
INSERT INTO `biz_class_student` VALUES (1, 1, 1, '2025-12-03 14:54:02');

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
-- Records of biz_course
-- ----------------------------
INSERT INTO `biz_course` VALUES (1, '测试1', '测试1', '/api/v1/files/6f32b4ae-95e1-4bdd-b3b7-cfdafcb5ba59.jpg', 4, '七年级', NULL, '2025-11-28 14:26:57', '2025-11-28 14:26:57');

-- ----------------------------
-- Table structure for biz_course_chapter
-- ----------------------------
DROP TABLE IF EXISTS `biz_course_chapter`;
CREATE TABLE `biz_course_chapter`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '绔犺妭ID',
  `course_id` bigint NOT NULL COMMENT '璇剧▼ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '鐖剁珷鑺侷D锛?琛ㄧず鏍圭珷鑺',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绔犺妭鍚嶇О',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '绔犺妭鎻忚堪',
  `sort_order` int NULL DEFAULT 0 COMMENT '鎺掑簭椤哄簭',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璇剧▼绔犺妭琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_course_chapter
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程讨论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_course_comment
-- ----------------------------
INSERT INTO `biz_course_comment` VALUES (1, 1, 1, '你好', NULL, '2025-11-29 20:24:41');
INSERT INTO `biz_course_comment` VALUES (2, 1, 3, '我不好\n', NULL, '2025-11-29 20:25:32');
INSERT INTO `biz_course_comment` VALUES (3, 1, 1, '真的假的\n', NULL, '2025-11-29 20:26:02');
INSERT INTO `biz_course_comment` VALUES (4, 1, 3, '不知道哦', NULL, '2025-11-29 20:26:09');
INSERT INTO `biz_course_comment` VALUES (5, 1, 1, '1', NULL, '2026-01-11 18:51:22');

-- ----------------------------
-- Table structure for biz_course_progress
-- ----------------------------
DROP TABLE IF EXISTS `biz_course_progress`;
CREATE TABLE `biz_course_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '杩涘害ID',
  `student_id` bigint NOT NULL COMMENT '瀛︾敓ID',
  `course_id` bigint NOT NULL COMMENT '璇剧▼ID',
  `resource_id` bigint NOT NULL COMMENT '璧勬簮ID',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '璧勬簮绫诲瀷锛歏IDEO/PDF/PPT/LINK',
  `progress_percent` int NULL DEFAULT 0 COMMENT '瀹屾垚鐧惧垎姣旓紙0-100锛',
  `last_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏈?悗瀛︿範浣嶇疆锛堣?棰戠?鏁?PDF椤电爜锛',
  `study_duration` int NULL DEFAULT 0 COMMENT '绱??瀛︿範鏃堕暱锛堢?锛',
  `is_completed` tinyint NULL DEFAULT 0 COMMENT '鏄?惁瀹屾垚锛?-鏈?畬鎴愶紝1-宸插畬鎴',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '瀹屾垚鏃堕棿',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_resource`(`student_id` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_course`(`course_id` ASC) USING BTREE,
  INDEX `idx_completed`(`is_completed` ASC, `completed_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瀛︿範杩涘害琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_course_progress
-- ----------------------------

-- ----------------------------
-- Table structure for biz_course_resource
-- ----------------------------
DROP TABLE IF EXISTS `biz_course_resource`;
CREATE TABLE `biz_course_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint NOT NULL COMMENT '所属课程ID',
  `chapter_id` bigint NULL DEFAULT NULL COMMENT '章节ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源/章节名称',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: VIDEO, PDF, PPT, LINK',
  `resource_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源地址',
  `knowledge_point_id` bigint NULL DEFAULT NULL COMMENT '关联知识点ID(可选)',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_course_resource
-- ----------------------------
INSERT INTO `biz_course_resource` VALUES (10, 1, NULL, '1', 'VIDEO', '/api/v1/files/3d8b503d-64ee-42ad-bf92-4601fdce746b.mp4', NULL, 0, '2025-11-28 17:58:49');

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
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '考试成绩记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_exam_result
-- ----------------------------
INSERT INTO `biz_exam_result` VALUES (9, 1, 7, '测试1', 2, 20, '{\"58\": \"D\", \"62\": \"B\", \"63\": \"B\", \"65\": \"C\", \"66\": \"B\", \"112\": \"爱到发烧\"}', '2025-11-27 16:26:04', 0, NULL, NULL, NULL, NULL, 2, 0, 0, 2);
INSERT INTO `biz_exam_result` VALUES (12, 3, 10, '测试图片', 0, 100, '{}', '2025-11-27 19:04:30', 0, NULL, NULL, NULL, NULL, 0, 0, 0, 1);
INSERT INTO `biz_exam_result` VALUES (14, 1, 10, '测试图片', 100, 100, '{}', '2025-11-27 19:43:14', 0, NULL, NULL, NULL, NULL, 100, 0, 0, 2);
INSERT INTO `biz_exam_result` VALUES (20, 1, 6, '测试222', 5, 10, '{\"16\": \"B\", \"17\": \"B\"}', '2025-12-02 14:52:23', 0, NULL, NULL, NULL, NULL, 5, 0, 0, 2);
INSERT INTO `biz_exam_result` VALUES (29, 1, 9, '测试A', 6, 20, '{\"58\": \"D\", \"60\": \"A\", \"63\": \"A\", \"64\": \"A\", \"66\": \"A\", \"112\": \"昔涟其实是开拓者的老婆，开拓者是权杖的主人\"}', '2025-12-02 17:24:21', 0, '{\"112\":{\"feedback\":\"回答与剧情设定严重不符，完全偏离了参考答案中关于角色本质和“权杖系统”的解释。\",\"score\":0}}', NULL, NULL, NULL, 6, 0, 0, 2);
INSERT INTO `biz_exam_result` VALUES (30, 1, 11, '崩坏', 8, 20, '{\"113\": \"权杖系统\", \"114\": \"B\", \"115\": \"C\", \"116\": \"B\", \"117\": \"白厄\", \"118\": \"星神\", \"119\": \"T\", \"120\": \"迷迷\", \"121\": \"A\", \"122\": \"D\"}', '2025-12-02 20:05:23', 0, NULL, NULL, NULL, NULL, 8, 0, 0, 2);
INSERT INTO `biz_exam_result` VALUES (32, 1, 11, '崩坏', 6, 20, '{\"113\": \"123123\", \"114\": \"C\", \"115\": \"B\", \"116\": \"B\", \"117\": \"2313\", \"118\": \"123123\", \"119\": \"F\", \"120\": \"1312312\", \"121\": \"B\", \"122\": \"B\"}', '2025-12-03 18:00:57', 0, NULL, NULL, NULL, NULL, 6, 0, 0, 2);
INSERT INTO `biz_exam_result` VALUES (33, 1, 12, '原神历史', 8, 120, '{\"113\": \"123431234\", \"114\": \"B\", \"115\": \"B\", \"116\": \"B\", \"117\": \"123124\", \"118\": \"12343215\", \"119\": \"F\", \"120\": \"123512345\", \"121\": \"B\", \"122\": \"A\", \"123\": \"安师大速度发货嗯\", \"124\": \"时代峰峻沙发管家地方各级\", \"125\": \"爱上对方哈的发的顺丰发货\", \"126\": \"我去而发\", \"127\": \"阿道夫哈哈烦得很发的\", \"128\": \"是大哥爱SD给\", \"129\": \"啊实打实的大哥\", \"130\": \"安徽发生的发货都是废话\", \"131\": \"萨达说的跟\", \"132\": \"啊多发点啥地方哈东方红\"}', '2025-12-03 21:27:13', 0, '{\"128\":{\"feedback\":\"答案与问题完全无关，无法得分。\",\"score\":0},\"129\":{\"feedback\":\"回答内容与题目完全无关，无法体现任何角度的推测，不符合答题要求。\",\"score\":0},\"130\":{\"feedback\":\"回答与题目完全无关，无法得分。\",\"score\":0},\"131\":{\"feedback\":\"答案与题目无关，未提供任何有效信息，无法得分。\",\"score\":0},\"132\":{\"feedback\":\"回答内容与题目完全无关，无法体现对主题的理解，无法得分。\",\"score\":0},\"123\":{\"feedback\":\"答案与问题完全无关，无法得分。\",\"score\":0},\"124\":{\"feedback\":\"答案与问题完全无关，无法识别为有效回答。\",\"score\":0},\"125\":{\"feedback\":\"答案与题目完全无关，属于无效内容，无法得分。\",\"score\":0},\"126\":{\"feedback\":\"答案无效，未提供任何实质性分析。\",\"score\":0},\"127\":{\"feedback\":\"答案与题目完全无关，无法识别任何有效内容。\",\"score\":0}}', NULL, NULL, NULL, 8, 0, 0, 2);
INSERT INTO `biz_exam_result` VALUES (34, 1, 11, '崩坏', 4, 20, '{\"113\": \"134123421\", \"114\": \"B\", \"115\": \"C\", \"116\": \"C\", \"117\": \"124234\", \"118\": \"1341234\", \"119\": \"T\", \"120\": \"14212341234\", \"121\": \"B\", \"122\": \"A\"}', '2025-12-04 18:12:33', 0, NULL, NULL, NULL, NULL, 4, 0, 1, 3);
INSERT INTO `biz_exam_result` VALUES (35, 1, 10, '测试图片', 0, 100, '{\"1\": \"12312\", \"2\": \"123\", \"3\": \"3123\", \"4\": \"12\", \"6\": \"3\", \"11\": \"3123\"}', '2026-01-07 15:07:32', 0, NULL, NULL, NULL, NULL, 0, 0, 0, 1);
INSERT INTO `biz_exam_result` VALUES (38, 1, 16, '原神考据', 98, 98, '{\"2269\": \"F\", \"2270\": \"T\", \"2271\": \"T\", \"2272\": \"T\", \"2273\": \"F\"}', '2026-01-09 21:43:56', 0, NULL, '你好', 1, '2026-01-10 14:28:41', 10, 8, 0, 2);
INSERT INTO `biz_exam_result` VALUES (39, 1, 16, '原神考据', 0, 98, '{}', '2026-01-10 14:34:53', 3, NULL, NULL, NULL, NULL, 0, 0, 0, 1);

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
-- Records of biz_favorite
-- ----------------------------
INSERT INTO `biz_favorite` VALUES (1, 1, 94, '2025-11-27 21:36:37');

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
-- Records of biz_goods
-- ----------------------------
INSERT INTO `biz_goods` VALUES (1, '青铜学者框', '象征勤奋的青铜头像框', 50, NULL, 'AVATAR_FRAME', 'border: 3px solid #cd7f32; border-radius: 50%;', -1, 1, '2025-11-27 21:46:32');
INSERT INTO `biz_goods` VALUES (2, '白银学霸框', '只有前10%的学生才能拥有', 200, NULL, 'AVATAR_FRAME', 'border: 3px solid #c0c0c0; box-shadow: 0 0 10px #c0c0c0; border-radius: 50%;', -1, 1, '2025-11-27 21:46:32');
INSERT INTO `biz_goods` VALUES (3, '黄金战神框', '传说中的学神', 1000, NULL, 'AVATAR_FRAME', 'border: 3px solid #ffd700; box-shadow: 0 0 15px #ffd700; border-radius: 50%;', -1, 1, '2025-11-27 21:46:32');
INSERT INTO `biz_goods` VALUES (4, '星空背景', '让你的主页充满科技感', 500, NULL, 'BACKGROUND', 'uploads/test1.jpg', -1, 1, '2025-11-27 21:46:32');
INSERT INTO `biz_goods` VALUES (5, '筑基丹', '突破辅助：增加 20% 突破成功率', 100, 'https://cdn-icons-png.flaticon.com/512/3055/3055374.png', 'PILL', '0.20', -1, 1, '2025-12-05 21:25:42');
INSERT INTO `biz_goods` VALUES (6, '天雷子', '突破辅助：强行抵消天劫，增加 40% 成功率', 300, 'https://cdn-icons-png.flaticon.com/512/1175/1175470.png', 'PILL', '0.40', -1, 1, '2025-12-05 21:25:42');

-- ----------------------------
-- Table structure for biz_grading_approval
-- ----------------------------
DROP TABLE IF EXISTS `biz_grading_approval`;
CREATE TABLE `biz_grading_approval`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审批记录ID',
  `exam_result_id` bigint NOT NULL COMMENT '考试结果ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生姓名',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `paper_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试卷标题',
  `old_score` int NULL DEFAULT NULL COMMENT '原分数',
  `new_score` int NOT NULL COMMENT '新分数',
  `score_change` int NOT NULL COMMENT '分数变化（正数表示加分，负数表示扣分）',
  `change_percentage` decimal(5, 2) NULL DEFAULT NULL COMMENT '分数变化百分比',
  `old_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '原评语',
  `new_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '新评语',
  `grader_id` bigint NOT NULL COMMENT '批阅人ID（申请人）',
  `grader_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '批阅人姓名',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请原因',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '审批状态: PENDING-待审批, APPROVED-已通过, REJECTED-已驳回',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  `approver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批人姓名',
  `approval_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审批意见',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `approval_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_result`(`exam_result_id` ASC) USING BTREE COMMENT '考试结果索引',
  INDEX `idx_student`(`student_id` ASC) USING BTREE COMMENT '学生索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态索引',
  INDEX `idx_grader`(`grader_id` ASC) USING BTREE COMMENT '申请人索引',
  INDEX `idx_approver`(`approver_id` ASC) USING BTREE COMMENT '审批人索引',
  INDEX `idx_submit_time`(`submit_time` ASC) USING BTREE COMMENT '提交时间索引',
  INDEX `idx_status_submit`(`status` ASC, `submit_time` ASC) USING BTREE COMMENT '状态和提交时间复合索引'
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '批阅审批表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_grading_approval
-- ----------------------------
INSERT INTO `biz_grading_approval` VALUES (1, 38, 1, 'Student-1', 16, '原神考据', 10, 98, 88, 880.00, '你好', '你好', 1, '系统管理员', 'ai判断错误了', 'REJECTED', 1, '系统管理员', 'ai没有出错', '2026-01-10 14:25:46', '2026-01-10 14:26:04', '2026-01-10 14:25:46', '2026-01-10 14:25:46');
INSERT INTO `biz_grading_approval` VALUES (2, 38, 1, 'Student-1', 16, '原神考据', 14, 98, 84, 600.00, '你好', '你好', 1, '系统管理员', '123123', 'APPROVED', 1, '系统管理员', '同意', '2026-01-10 14:27:43', '2026-01-10 14:28:00', '2026-01-10 14:27:43', '2026-01-10 14:27:43');

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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批阅历史记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_grading_history
-- ----------------------------
INSERT INTO `biz_grading_history` VALUES (1, 38, 1, '系统管理员', 'UPDATE_COMMENT', NULL, NULL, NULL, '你好', NULL, '2026-01-10 14:07:32');
INSERT INTO `biz_grading_history` VALUES (2, 38, 1, '系统管理员', 'UPDATE_SCORE', 10, 11, NULL, NULL, NULL, '2026-01-10 14:26:59');
INSERT INTO `biz_grading_history` VALUES (3, 38, 1, '系统管理员', 'UPDATE_SCORE', 11, 12, NULL, NULL, NULL, '2026-01-10 14:27:06');
INSERT INTO `biz_grading_history` VALUES (4, 38, 1, '系统管理员', 'UPDATE_SCORE', 12, 13, NULL, NULL, NULL, '2026-01-10 14:27:11');
INSERT INTO `biz_grading_history` VALUES (5, 38, 1, '系统管理员', 'UPDATE_SCORE', 13, 14, NULL, NULL, NULL, '2026-01-10 14:27:16');
INSERT INTO `biz_grading_history` VALUES (6, 38, 1, '系统管理员', 'UPDATE_SCORE', 98, 91, NULL, NULL, NULL, '2026-01-10 14:28:16');
INSERT INTO `biz_grading_history` VALUES (7, 38, 1, '系统管理员', 'UPDATE_SCORE', 91, 98, NULL, NULL, NULL, '2026-01-10 14:28:41');

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
-- Records of biz_homework
-- ----------------------------
INSERT INTO `biz_homework` VALUES (1, 1, '数学测试', 11, '2025-12-03 10:00:00', '2025-12-03 14:58:16');
INSERT INTO `biz_homework` VALUES (2, 1, '测试', 12, NULL, '2025-12-03 21:26:16');

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
-- Records of biz_homework_record
-- ----------------------------

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
-- Records of biz_knowledge_point
-- ----------------------------
INSERT INTO `biz_knowledge_point` VALUES (10, 82, '12', '1', '', '高三', '原神', '', '2025-08-08 22:58:31', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (12, 82, 'AI-9854', '花神诞祭轮回', '教令院贤者利用大慈树王遗留的“虚空终端”系统收割须弥民众的梦境能量，意图以此创造新神。旅行者与小吉祥草王纳西妲共同经历了168次时间循环，最终识破并打破了这一困局。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (13, 82, 'AI-3391', '正机之神', '愚人众执行官“散兵”在教令院与“博士”的协助下，通过利用神之心与外部力量，短暂地成为了拥有神力的“正机之神”。但最终被旅行者击败，纳西妲借此夺回了草神之心与雷神之心。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (14, 82, 'AI-35', '世界树的改写', '为彻底根除“禁忌知识”对世界树的污染，纳西妲必须在世界树内部“删除”其前代大慈树王的所有存在信息。这一操作导致历史被重写，在新时间线中，大慈树王从未存在，纳西妲成为自古以来唯一的草神。只有身为“降临者”的旅行者保留了真实的记忆。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (15, 82, 'AI-26', '散兵的重生', '散兵试图通过世界树删除自身存在，以改变踏鞴砂的悲剧历史（如丹羽之死），但发现历史结果无法改变，仅因果被重置。最终他选择接受过去，并获得风属性神之眼，以“流浪者”的身份获得新生。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (16, 82, 'AI-966', '原始胎海预言', '枫丹流传的古老预言，预言枫丹人最终都会溶解在原始胎海之水中，唯有水神独自在神座上哭泣。这一预言是枫丹主线剧情的核心驱动力和危机根源。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (17, 82, 'AI-429', '芙卡洛斯的欺骗', '水神芙卡洛斯将自己的神格与人性（芙宁娜）分离。神格在谕示裁定枢机内积蓄能量，而芙宁娜则扮演水神长达五百年，以此共同演出一场欺骗“天理”的宏大戏剧。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (18, 82, 'AI-6423', '神座崩毁', '芙卡洛斯最终选择自我牺牲，摧毁了水神的神座，将古龙大权完整归还给完全之龙那维莱特。那维莱特随后赦免了枫丹人（实为纯水精灵拟态）的原罪，使他们成为真正的人类，从而从根本上规避了原始胎海预言的毁灭结局。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (19, 82, 'AI-5271', '水仙十字结社', '一条跨越数百年的暗线，由雷内、雅各布等孤儿院成员创立。他们试图通过“世界式”计算末日并执行“补完计划”，即让人类回归原始胎海。其行动揭示了深渊力量、坎瑞亚技术与原始胎海之水结合所产生的恐怖后果。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (20, 82, 'AI-6957', '纳塔的生死机制', '火之国纳塔处于与深渊战争的前线。火神玛薇卡揭示了纳塔人死后可在“夜神之国”复活的特殊机制，但这一维系国度的生死循环机制正濒临崩溃，构成了纳塔的核心危机。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (21, 82, 'AI-5491', '队长的牺牲', '愚人众第一席“队长”介入纳塔危机。他利用自身的不死诅咒与死之执政罗诺瓦的规则博弈，通过自我牺牲，偿还了第一代火神希巴兰克欠下的“死亡之债”，从而修复了纳塔的地脉循环，为纳塔赢得生机，自身则陷入长眠。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (22, 82, 'AI-5347', '六大部族集结', '在深渊全面入侵的危机下，火神玛薇卡集结了纳塔六大部族的英雄（基尼奇、玛拉妮等），共同在圣火竞技场击退了深渊的总攻。火神之心在此过程中成为与愚人众博弈的关键筹码。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (23, 82, 'AI-5016', '戴因斯雷布的终局', '旅行者与戴因斯雷布再次同行，揭示了“五大罪人”的完整名单及其背后的深渊力量体系。剧情确认了旅行者双子五百年前降临的细节，并表明“织运之梭”计划已进入最终阶段。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (24, 82, 'AI-3108', '诺德·克莱与月之遗民', '诺德·克莱是至冬国边缘的永夜之地，当地势力“霜月之子”崇拜古老的月亮而非七神。旅行者在此结识劳玛和弗林斯，并卷入他们与愚人众的冲突。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (25, 82, 'AI-8140', '双子飞船残骸', '在诺德·克莱的冰层下，旅行者找到了五百年前与双子一同降临提瓦特时所乘坐的飞船残骸。这一发现触发了大量记忆回溯，并证实了提瓦特星球外围存在被称为“虚假之天”的封锁。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (26, 82, 'AI-4501', '罪人雷瑞尔', '雷瑞尔是坎瑞亚五大罪人之一，被称为“索尔纳里的复仇者”。因爱人索琳迪斯死于赤月王朝覆灭，他堕入深渊，企图粉碎诺德·克莱上空的“虹色之月”来复活爱人或毁灭世界。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (27, 82, 'AI-1163', '哥伦比奈的真身', '愚人众第三席“少女”哥伦比奈在此章节展现神性一面，被证实是仙灵一族的幸存者，甚至可能是“月之三姐妹”的后裔。她背叛愚人众指令，选择与宿敌雷瑞尔对峙，意图通过“初敕法庭”重构月亮秩序。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (28, 82, 'AI-2125', '法尔伽大团长参战', '失踪已久的西风骑士团大团长法尔伽率领远征军现身诺德·克莱。在旅行者与雷瑞尔的决战陷入绝境时，他以“北风骑士”的压倒性实力介入并救下旅行者，首次向玩家展示其真实力量。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (29, 82, 'AI-2997', '诺德·克莱的结局', '6.1版本以惨胜告终。雷瑞尔被暂时击退并囚禁于月之倒影的异空间，但诺德·克莱的空间壁垒因此破碎，导致“暗之外海”的力量开始渗入。木偶桑多涅向旅行者揭示了枫丹与至冬科技的可能同源性。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (30, 82, 'AI-7215', '虚空终端', '由前代草神大慈树王留下的梦境连接系统，教令院贤者曾滥用此系统收割民众梦境以达成造神目的。它是须弥篇“花神诞祭轮回”事件的关键工具。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (31, 82, 'AI-4488', '织运之梭计划', '一个由深渊势力推动的宏大计划，其具体内容与目的在戴因斯雷布的剧情线中被逐步揭示。该计划在版本5.7的间章中确认已进入最终阶段，与旅行者双子的降临及提瓦特的命运紧密相关。', '高三', '原神', NULL, '2025-12-03 19:45:14', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (32, 82, 'AI-6139', '四风守护的背叛', '风魔龙特瓦林并非无故发狂，而是在五百年前与魔龙杜林的战斗中咽下毒血，长期遭受折磨，随后被深渊教团的深渊法师利用，导致其背叛了守护蒙德的职责。', '高三', '原神', NULL, '2025-12-03 20:34:45', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (33, 82, 'AI-8070', '温迪与自由意志', '风神温迪伪装成吟游诗人，与旅行者等人盗取天空之琴以拯救特瓦林，体现了他对“自由”的极端诠释：即使是神物也不能凌驾于自由意志之上。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (34, 82, 'AI-566', '神之心失窃事件', '在风龙废墟事件后，愚人众执行官“女士”突袭温迪并夺走风神之心，揭示了至冬国女皇正在收集七神神之心的宏大计划，这是巡礼中首个神之心失窃事件。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (35, 82, 'AI-4930', '阿贝多的身世', '西风骑士团首席炼金术士阿贝多并非人类，而是坎瑞亚大炼金术士“黄金”莱茵多特创造的“原初之人”，这一秘密在龙脊雪山剧情中被揭开。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (36, 82, 'AI-362', '虚假之天', '愚人众执行官“散兵”在接触陨石后，惊恐地发现“星空本身是个巨大的谎言”，这一伏笔后来在提瓦特边界和“蛋壳”理论中得到证实，动摇了世界观基础。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (37, 82, 'AI-2582', '岩王帝君诈死', '在请仙典仪上，岩王帝君摩拉克斯（钟离）诈死，实则是为了与冰之女皇签订“终结一切契约的契约”，为璃月过渡到人治时代铺平道路。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (38, 82, 'AI-8600', '人治的试炼', '愚人众执行官“公子”释放魔神奥赛尔，试图逼出岩神。最终璃月七星与仙人联手牺牲群玉阁将其封印，钟离确认人类有能力自保后交出岩神之心并退位。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (39, 82, 'AI-9047', '双子重逢与深渊立场', '在层岩巨渊深处，旅行者与血亲（深渊教团领导者）重逢，血亲阻止戴因斯雷布净化丘丘人诅咒，表明坎瑞亚幸存者在五百年畸变中已形成新的社会结构。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (40, 82, 'AI-6899', '无名夜叉的牺牲', '剧情揭示五百年前，雷夜叉浮舍与伯阳等人为阻挡深渊魔兽涌入璃月，将自己封印在层岩巨渊地下，展现了悲壮的牺牲精神。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (41, 82, 'AI-8312', '眼狩令与永恒追求', '雷电将军（影）为追求“永恒”，实施眼狩令并封锁国门，导致海祇岛珊瑚宫反抗军崛起，引发稻妻内部冲突。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (42, 82, 'AI-3152', '御前决斗与女士之死', '旅行者揭露愚人众幕后推波助澜的阴谋，在天守阁御前决斗中战胜女士，雷电将军随后以“无想的一刀”处决女士，标志稻妻剧变的转折点。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (43, 82, 'AI-2961', '意识空间的救赎', '在八重神子帮助下，旅行者进入影的内心世界“一心净土”，击败其固执意志，废除眼狩令。事后揭露八重神子已将雷神之心交易给散兵以换取旅行者性命。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (44, 82, 'AI-7713', '禁忌历史《日月前事》', '渊下宫的《日月前事》记载了提瓦特最禁忌的历史：原初之人法涅斯击败七龙王创造人类，以及“第二王座”引发大战。奥罗巴斯因读到该书被天理判处死刑。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (45, 82, 'AI-1424', '花神诞祭轮回', '教令院贤者利用虚空终端收割梦境试图造神，旅行者与纳西妲经历168次循环后破局，揭露了教令院的阴谋。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (46, 82, 'AI-751', '世界树的改写', '为根除禁忌知识污染，纳西妲在世界树中“删除”大慈树王的存在，历史被重写：在新时间线中，纳西妲自古是唯一草神，仅旅行者保留真实记忆。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (47, 82, 'AI-8560', '散兵的重生', '散兵试图通过世界树删除自己以挽回踏鞴砂悲剧，但历史结果无法改变。最终他接受过去，获得风属性神之眼，重生为“流浪者”。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (48, 82, 'AI-9327', '原始胎海预言', '枫丹预言“所有人都会溶解在海里，只剩下水神在神座上哭泣”，驱动了芙卡洛斯和芙宁娜长达五百年的欺骗计划。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (49, 82, 'AI-4', '芙卡洛斯的欺骗与神座崩毁', '水神芙卡洛斯将神格与人性分离，芙宁娜扮演神明欺骗天理，芙卡洛斯在谕示裁定枢机积攒能量后自杀摧毁水神神座，将古龙大权归还那维莱特，赦免枫丹人原罪。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (50, 82, 'AI-4808', '水仙十字结社的疯狂尝试', '雷内、雅各布等孤儿通过“世界式”计算末日，试图让人类回归原始胎海以达成“补完”，揭示了深渊力量、坎瑞亚技术与胎海之水结合的恐怖后果。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (51, 82, 'AI-4150', '队长的牺牲与纳塔地脉修复', '愚人众第一席“队长”利用不死诅咒与死之执政罗诺瓦博弈，通过牺牲自己偿还第一代火神希巴兰克的“死亡之债”，修复纳塔地脉循环，为纳塔赢得生机。', '高三', '原神', NULL, '2025-12-03 20:34:46', '2026-01-08 12:15:33');
INSERT INTO `biz_knowledge_point` VALUES (52, 82, 'AI-2745', '法涅斯（原初之人）', '法涅斯是提瓦特世界的第一降临者，诞生于太初之卵，背生双翼，头戴王冠，雌雄同体，象征着超越性别二元的神性。祂从混沌中孵化，标志着提瓦特作为一个独立世界泡的确立，是创世神话的核心存在。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (53, 82, 'AI-4502', '七龙王与原初元素权柄', '在法涅斯降临前，提瓦特由七位龙王统治，他们分别掌管水、火、雷、冰、岩、风、草七种元素的原初权柄。龙王权柄本质高于后来神之心赋予的力量，是提瓦特最古老的力量体系之一。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (54, 82, 'AI-6353', '龙王战争（龙争）', '法涅斯与七龙王之间爆发的史诗级战争，结果以法涅斯胜利告终。这场战争确立了‘人界力’的秩序，法涅斯从龙王手中夺取了元素权柄，是提瓦特早期秩序建立的关键事件。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (55, 82, 'AI-710', '四影的创造', '法涅斯在获胜后创造了四个影子（四影）来管理世界，他们各自掌管世界运行的核心法则：生命、死亡、时间与空间。四影是法涅斯权柄的分割与代行者，对提瓦特的根本规则有深远影响。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (56, 82, 'AI-817', '生之影', '四影之一，权能是创造生命并赋予灵性。推测与原初之海的生命起源有关。坎瑞亚的炼金术士莱茵多特所追求的‘原初之人’技术，可能试图复现或窃取这一权柄。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (57, 82, 'AI-3891', '死之影（罗诺瓦/死之执政）', '四影之一，代行者罗诺瓦掌管生命的终结与灵魂归宿，其领域为‘夜神之国’。纳塔剧情揭示了死亡法则的复杂性，如与不死诅咒的逻辑悖论。死亡在提瓦特本质上是灵魂在不同领域间的转移。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (58, 82, 'AI-577', '时之影（伊斯塔露）', '四影之一，真名伊斯塔露，别称千风之神或时刻之神。她能操纵时间线，如帮助雷电真在过去种下神樱树，形成因果闭环。她是四影中唯一未完全沉默的执政，证实提瓦特允许有限度的时间干涉。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (59, 82, 'AI-5050', '空之影（推测为天理的维系者）', '四影之一，推测为游戏开场拦截双子的神秘女神（天理的维系者），权能是操纵空间，使用暗红色方块。她可能维持着提瓦特星空这层人造‘蛋壳’，用于隔绝外部宇宙的混沌侵蚀。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (60, 82, 'AI-3202', '葬火之年与第二降临者', '法涅斯建立秩序后，一股外来力量（第二降临者）入侵提瓦特，引发灾难。关于其身份有龙王尼伯龙根、虚数之树清理者或深渊源头等多种假说。战争导致渊下宫沉没、地脉紊乱，法涅斯方惨胜后衰弱。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (61, 82, 'AI-8693', '天钉', '在葬火之年战争后，世界遭受严重损害，天理降下天钉以强行修复地脉裂隙，是战后修补世界的关键手段，体现了天理对提瓦特基础结构的干预能力。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (62, 82, 'AI-4064', '神之心的本质', '神之心是天理在战后创造的物品，据考据是用第三降临者的遗骨制成的增幅器。其作用包括元素增幅、天理监控以及作为连接龙王权柄与神明的桥梁，但对七神而言可能更像一种契约或枷锁。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (63, 82, 'AI-1327', '第三降临者', '身份不明的已故降临者，其遗骸被制成了七颗神之心。神之心作为其遗骨的产物，暗示第三降临者的力量或知识可能通过神之心被分割和利用，与天理的监控体系密切相关。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (64, 82, 'AI-982', '七神对神之心的态度', '七神对神之心的态度各异，但普遍不将其视为力量源泉（如那维莱特取回水权柄后更强）。温迪、钟离、雷电影的行为暗示神之心是一种契约或枷锁，冰之女皇收集神之心可能旨在切断监控或对抗天理。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (65, 82, 'AI-2889', '提瓦特星空之谜', '散兵曾指出‘星空本身就是个巨大的谎言’，推测提瓦特的星空是一层由空之影维持的人造‘蛋壳’或穹顶，用于隔绝外部宇宙（如量子之海）的混沌侵蚀，这也解释了旅行者无法离开的原因。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (66, 82, 'AI-1590', '时间悖论与因果闭环', '在时之影伊斯塔露的影响下，提瓦特存在时间干涉现象，如雷电真回到过去种下神樱树，形成一个因果自洽的时间闭环（神樱树在500年前已存在，但由500年后的雷电真种下）。这揭示了提瓦特时间法则的独特性。', '高三', NULL, NULL, '2026-01-09 10:55:46', '2026-01-09 10:55:46');
INSERT INTO `biz_knowledge_point` VALUES (67, 82, 'AI-3755', '七个神座', '天理在大约3000-2000年前宣布设立的七个神位，是引发魔神战争的直接原因。其数量可能与提瓦特的七元素体系及被夺取的龙王权柄有关，七神本质上是管理这些权柄的代理人。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (68, 82, 'AI-493', '魔神战争', '因天理设立七个神座而引发的、提瓦特历史上最惨烈的战争。众多魔神为争夺神位而相互征伐，决定了七神的最终归属，败者则面临被封印、流亡、被吞噬或转化为诅咒等悲惨命运。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (69, 82, 'AI-5078', '败者命运', '魔神战争中失败魔神的几种结局：被封印（如奥赛尔、若陀龙王）、逃往暗之外海、被其他魔神吞噬融合、或死后怨念转化为污染大地的诅咒（如瘴气、深渊腐蚀）。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (70, 82, 'AI-4739', '盐之魔神赫乌莉亚', '一位极度温和退让的弱小魔神。其悲剧在于：试图以和平退让避免战争却最终招致毁灭；被深爱她的子民出于扭曲的‘仁慈’刺杀；揭示了在残酷的魔神战争法则下，缺乏力量的善良难以存续。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (71, 82, 'AI-4259', '天理的维系者', '在世界边缘截击并封印了旅行者双子之一的强大存在，使用暗红色方块（空间权能）。其身份和目的成谜，是提瓦特世界高层次力量的代表。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (72, 82, 'AI-5227', '风魔龙特瓦林', '原为蒙德的四风守护之一，后被深渊教团利用500年前黑龙杜林的毒血腐蚀，陷入痛苦并引发龙灾，是蒙德主线剧情的关键角色。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (73, 82, 'AI-1399', '黑龙杜林', '由坎瑞亚炼金术士‘黄金’莱茵多特创造的深渊生物。其本质天真，却因生态不兼容，其血液和吐息对提瓦特生命是致命毒素。死后尸骸化作龙脊雪山，其深渊能量持续造成污染。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (74, 82, 'AI-7802', '‘黄金’莱茵多特', '坎瑞亚炼金术士，魔女会成员（代号R）。创造了阿贝多（白垩之子）与黑龙杜林，因后者的灾难性影响被列为坎瑞亚五大罪人之一，是炼金术巅峰与罪孽的象征。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (75, 82, 'AI-2448', '神之心', '七神持有的特殊物品，但并非其力量源泉（失去后仍能使用元素力）。推测可能是天理用于监视七神的工具，其真实用途和重要性是剧情核心谜团之一。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (76, 82, 'AI-6445', '旧蒙德与暴风魔神', '约2600年前，蒙德由暴风魔神迭卡拉庇安统治，他用暴风之墙将人民囚禁在风龙废墟，自以为保护实为囚禁，最终被巴巴托斯等人领导的革命推翻。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (77, 82, 'AI-1793', '温妮莎', '千年前领导蒙德人民推翻腐化贵族统治的斗士奴隶，革命后建立了西风骑士团。死后升天化为‘隼’的形态守护蒙德，体现了‘自由需由人自己争取’的理念。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (78, 82, 'AI-284', '虚假的星空', '愚人众执行官‘散兵’接触古老陨石核心后揭露的惊天秘密：提瓦特的星空可能是一层人造的‘蛋壳’或穹顶，用于隔绝外部混沌并控制内部命运，命之座可能是被编写好的命运程序。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (79, 82, 'AI-7894', '阿贝多', '莱茵多特利用‘白垩’创造的‘原初之人’计划成品，杜林的‘兄弟’。象征炼金术的光明面，理性克制，但体内可能潜藏因深渊力量暴走或被莱茵多特预设后门的风险。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (80, 82, 'AI-7489', '磨损', '提瓦特所有长生者（神、仙、龙）无法逃脱的诅咒。本质是记忆的持续流失导致人格逐渐崩塌乃至死亡，是推动钟离隐退、若陀龙王发狂等关键剧情的核心设定。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (81, 82, 'AI-5266', '岩王帝君隐退', '摩拉克斯策划假死，利用公子召唤奥赛尔测试璃月自治能力，最终从神治过渡到人治。原因包括对抗磨损、终结与天理的契约，以及对人类未来的信任。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (82, 82, 'AI-9944', '归终与尘世之锁', '魔神战争中阵亡的尘之魔神，钟离的故友。留给钟离的‘尘世之锁’蕴含其全部智慧与情感，只有当他真正理解人类细腻情感时才能打开，是钟离人性课题的象征。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (83, 82, 'AI-6723', '终结一切契约的契约', '钟离将神之心交易给至冬女皇所依据的契约。内容可能涉及对抗磨损的方法、默许女皇燃烧旧世界的计划或结成对抗天理的联盟，本质是契约之神对天理秩序的一种悖论性背叛。', '高三', NULL, NULL, '2026-01-09 12:06:48', '2026-01-09 12:06:48');
INSERT INTO `biz_knowledge_point` VALUES (84, 82, 'AI-5239', '若陀龙王', '钟离在魔神战争前结识的岩元素巨龙挚友，因磨损导致记忆混乱、忘记承诺而发狂，最终被钟离亲手封印。其故事深刻阐释了磨损的残酷性。', '高三', NULL, NULL, '2026-01-09 12:06:49', '2026-01-09 12:06:49');
INSERT INTO `biz_knowledge_point` VALUES (85, 82, 'AI-2578', '各国应对磨损的策略', '各国神明为对抗磨损采取的不同方法：巴巴托斯长期沉睡；摩拉克斯隐退寄托记忆于人类；雷电影创造人偶代政、自我冥想；大慈树王选择被遗忘；芙卡洛斯分离并毁灭神格；玛薇卡通过死亡-复活重置记忆。', '高三', NULL, NULL, '2026-01-09 12:06:49', '2026-01-09 12:06:49');
INSERT INTO `biz_knowledge_point` VALUES (86, 82, 'AI-2194', '记忆对抗磨损', '通过盐之魔神赫乌莉亚的悲剧引申出的哲学思考：即使肉体消亡，温柔与善良的价值可以通过被后人铭记的‘传说’和‘记忆’来延续，这是对抗绝对遗忘（磨损）的一种方式。', '高三', NULL, NULL, '2026-01-09 12:06:49', '2026-01-09 12:06:49');
INSERT INTO `biz_knowledge_point` VALUES (87, 82, 'AI-3147', '法涅斯创世', '法涅斯（Phanes），又称“原初之人”，是第一降临者。据《日月前事》记载，其诞生于蛋中，形象超越性别，象征着从混沌中孵化并确立了提瓦特作为一个独立世界的开端。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (88, 82, 'AI-7407', '七龙王', '提瓦特原初的统治者，共七位，分别掌管水、火、雷、冰、岩、风、草七种元素的原始权柄。他们在法涅斯降临前统治世界，后被法涅斯击败，权柄被夺取。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (89, 82, 'AI-3113', '龙王战争', '指第一降临者法涅斯与原初统治者七龙王之间爆发的史诗战争。战争结果以法涅斯胜利告终，确立了“人界力”的秩序，元素权柄从龙王转移至新秩序之下。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (90, 82, 'AI-4746', '四影', '法涅斯为管理世界创造的四个影子，分别掌管生命、死亡、时间与空间的核心法则，是世界运行的基石。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (91, 82, 'AI-2436', '生之影', '四影之一，掌管创造生命与赋予灵性的权能。考据推测其可能与“原初之海”的生命起源有关，坎瑞亚炼金术士莱茵多特追求的技术或许试图复现此权柄。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (92, 82, 'AI-8389', '死之影（罗诺瓦）', '四影之一，名为罗诺瓦，即“死之执政”。掌管生命的终结与灵魂归宿，其领域为“夜神之国”。其法则在纳塔剧情中与“不死诅咒”产生悖论，揭示了提瓦特死亡是灵魂转移的本质。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (93, 82, 'AI-4647', '时之影（伊斯塔露）', '四影之一，真名伊斯塔露，别称“千风之神”。掌管时间权能，是四影中唯一未完全沉默者。曾协助雷电真在过去种下神樱树，创造了因果自洽的时间闭环。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (94, 82, 'AI-9551', '空之影', '四影之一，掌管空间权能。推测其化身或代行者即为游戏开场拦截双子的“天理的维系者”（Asmoday），使用暗红色方块操纵空间，可能负责维持提瓦特与外界的隔绝。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (95, 82, 'AI-8929', '葬火之年', '指第二降临者入侵提瓦特所引发的大灾难。战争导致渊下宫沉没、地脉紊乱，法涅斯方虽惨胜但自身衰弱，天理随后降下“天钉”修复世界。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (96, 82, 'AI-9887', '第二降临者假说', '关于葬火之年入侵者身份的三种主流假说：一是龙王尼伯龙根携外界力量归来复仇；二是虚数之树派来的“清理者”；三是深渊本源的入侵。其真实身份仍是谜团。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (97, 82, 'AI-3154', '神之心起源', '神之心由第三降临者的遗骨制成，是战后天理创造的物品。其作用包括增幅七神元素力、作为天理的监控工具，以及桥接被夺取的龙王权柄。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (98, 82, 'AI-3463', '第三降临者', '一位身份不明且已确认死亡的降临者，其遗骸被制成了七颗神之心。冰之女皇收集神之心的目的可能与其力量或知识有关。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (99, 82, 'AI-3244', '七神座确立', '约3000-2000年前，天理宣布设立七个神位，直接引发了席卷提瓦特的魔神战争。神座数量与七元素及龙王权柄相关，七神本质是管理这些权柄的代理人。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (100, 82, 'AI-6017', '魔神战争败者命运', '魔神战争中失败者的几种结局：被强大魔神或执政封印（如奥赛尔）；流亡至暗之外海；被其他魔神吞噬融合；或死后怨念化为诅咒污染大地。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (101, 82, 'AI-6013', '盐之魔神赫乌莉亚', '一位极度温和弱小的魔神。其悲剧在于试图以退让换取和平却终遭毁灭，最后被深爱她的子民刺杀，体现了魔神战争中弱者无法逃避的残酷宿命。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (102, 82, 'AI-3213', '双子坠落', '旅行者与其血亲（双子）在世界边缘试图离开提瓦特时，被天理的维系者（空之影）拦截。维系者使用空间权能封印了其中一人，主角则坠入提瓦特，故事由此开始。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (103, 82, 'AI-8889', '杜林本质', '由坎瑞亚炼金术士“黄金”莱茵多特创造的黑龙。身为深渊生物，其本性天真并非邪恶，但因生态不兼容，其存在本身（血液、吐息）对提瓦特生命即为致命毒素，最终在误解中死于特瓦林之手。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (104, 82, 'AI-4113', '温迪与神之心', '风神巴巴托斯在被愚人众“女士”夺取神之心时未全力反抗，暗示神之心并非其力量源泉，反而可能是天理的监视工具或某种契约枷锁，其失去可能是有意为之。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (105, 82, 'AI-3405', '旧蒙德革命', '约2600年前，人民在少年温迪（当时的风精灵）等人带领下，推翻暴风魔神德卡拉庇安的统治。战斗中巴巴托斯的挚友牺牲，促使他继承愿望，在魔神战争中夺取神位。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (106, 82, 'AI-9337', '虚假星空假说', '由散兵在“未归的熄星”事件中揭示的核心假说，即“星空本身是谎言”。推测提瓦特的星空是一层人造的“蛋壳”或穹顶，用于隔绝外部混沌并控制内部命运的书写。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (107, 82, 'AI-1621', '命之座本质', '每个神之眼持有者对应的星辰图案。考据认为命之座可能是被预先编写好的“命运程序”，象征着个体的命运可能早已被星空（或背后的存在）注定。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (108, 82, 'AI-275', '阿贝多身世', '由“黄金”莱茵多特利用“白垩”创造的人造人，是“原初之人”计划的成品，黑龙杜林的“兄弟”。他理性克制，向往人性，但其人造本质隐藏着潜在的失控风险。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (109, 82, 'AI-9719', '白垩与黑龙的对比', '莱茵多特两大造物阿贝多（白垩）与杜林（黑龙）的象征性对比。白垩代表理性、克制与炼金术的光明面；黑龙代表混沌、天真与炼金术的阴暗面，二者命运迥异。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (110, 82, 'AI-4867', '腐殖之剑', '一把吸收了杜林生命力而诞生的剑。在雪山剧情中，阿贝多委托旅行者用元素力净化它，这把剑在某种意义上被视为“兄弟的遗骸”，承载着复杂的象征意义。', '高三', NULL, NULL, '2026-01-09 22:18:11', '2026-01-09 22:18:11');
INSERT INTO `biz_knowledge_point` VALUES (111, 82, 'AI-7099', '阿贝多的失控风险', '阿贝多曾对旅行者坦言，担心自己未来可能失控，并请求对方阻止。这暗示其人造人体内可能存在因深渊力量暴走或莱茵多特预设后门而触发的危险“开关”。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (112, 82, 'AI-5063', '岩神假死测试', '岩王帝君摩拉克斯（钟离）与冰之女皇签订“终结一切契约的契约”，策划假死，并利用公子召唤魔神奥赛尔，以此测试璃月七星与仙人在失去神明后能否独立治理国家。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (113, 82, 'AI-4256', '归终与尘世之锁', '归终是钟离在魔神战争时期的故友，智慧之神。她战死前将“尘世之锁”交给钟离，锁中封存了她的智慧与情感，并设定只有当钟离真正理解人性细腻情感时方能打开，成为他的一道长期考题。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (114, 82, 'AI-2553', '磨损', '提瓦特长生种（如魔神、仙人）所面临的一种终极困境。随着时间流逝，记忆会逐渐流失，人格会崩塌消解。这是钟离决定隐退的重要原因之一。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (115, 82, 'AI-2363', '天钉', '葬火之年战争后，天理为修复受损的世界和紊乱的地脉而降下的巨型装置。其强行修复的行为可能对当地文明（如沙域、层岩巨渊）造成毁灭性打击。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (116, 82, 'AI-2339', '原初之海', '与生命起源相关的神秘概念，推测由生之影掌管。可能与枫丹的原始胎海水及提瓦特所有生命的源头有关，是炼金术追求触及的至高奥秘之一。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (117, 82, 'AI-2415', '夜神之国', '由死之执政罗诺瓦掌管的死后领域，是灵魂的归宿之一。纳塔剧情揭示了与其规则博弈的复杂性，表明提瓦特的“死亡”是灵魂在不同领域间的转移而非彻底湮灭。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (118, 82, 'AI-3133', '时间闭环', '一种因果自洽的时间干涉现象。表现为雷电真在500年后回到过去种下神樱树，而该树在500年前就已存在。时之影伊斯塔露的权能允许此类有限度但代价高昂的时间操作。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (119, 82, 'AI-6703', '深渊生态不兼容', '指深渊生命（如杜林）与提瓦特原生碳基生命在根本上的不相容。深渊生物无心的举动（如玩耍、呼吸）都可能对提瓦特生命造成致命污染与扭曲。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (120, 82, 'AI-9896', '贵族时代腐化', '旧蒙德革命后建立的政权逐渐腐化的时期。贵族垄断权力，甚至与愚人众进行奴隶贸易，直至约1000年前，奴隶斗士温妮莎领导革命，推翻贵族统治，建立了西风骑士团。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (121, 82, 'AI-3003', '温妮莎建骑士团', '约1000年前，蒙德奴隶斗士温妮莎领导人民推翻腐化贵族，建立了西风骑士团，奠定了现代蒙德自治的基石。她死后升天化为“隼”，持续守护蒙德。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (122, 82, 'AI-155', '散兵的特殊性', '愚人众执行官“散兵”是雷电影制造的人偶。因其非天生的造物本质，他可能拥有看穿“命之座”系统虚假性的能力，这为其后来在须弥试图篡改命运的行为埋下伏笔。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (123, 82, 'AI-7232', '炼金术的罪与罚', '指“黄金”莱茵多特及其造物（阿贝多、杜林）所代表的炼金术的双重性。一方面追求创造生命（原初之人），另一方面其造物（杜林）又带来巨大灾难，体现了禁忌知识的危险。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (124, 82, 'AI-6738', '契约的终结', '岩王帝君摩拉克斯作为“契约之神”，其隐退可能意味着与天理签订的某个古老“契约”走到了终结之时。他选择用一份“终结一切契约的契约”来为神治时代画上句号。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (125, 82, 'AI-6039', '盐盏与盐尺', '盐之魔神赫乌莉亚死后遗留下的两件法器。盐盏能无限产生盐，盐尺能划出盐的领域。它们既是赫乌莉亚权能的残留，也是其悲剧命运的物证。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (126, 82, 'AI-8882', '特瓦林毒血', '风魔龙特瓦林因与黑龙杜林战斗，伤口被杜林的深渊毒血侵蚀腐蚀，导致其痛苦并陷入疯狂。这是蒙德龙灾的直接起因，也是深渊力量污染提瓦特生物的典型案例。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (127, 82, 'AI-4831', '未归的熄星事件', '1.1版本核心活动。冒险家莱纳德强烈的登峰愿望死后化为陨石坠落，愚人众执行官散兵接触其核心后昏睡，醒来后道出“星空是谎言”的震撼发言，揭示了世界观核心伏笔。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (128, 82, 'AI-8567', '莱茵多特', '代号“R”的坎瑞亚炼金术士，魔女会成员，被称为“黄金”。她创造了人造人阿贝多与黑龙杜林，因后者的灾难性影响被列为坎瑞亚五大罪人之一，追求触及“原初之人”的禁忌知识。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (129, 82, 'AI-4526', '地脉', '流淌在提瓦特地底，记录世界一切信息与记忆的能量循环系统。葬火之年战争曾导致其紊乱，天理降下天钉进行修复。地脉异常会引发记忆重现、时空错乱等现象。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (130, 82, 'AI-357', '神之眼', '当人的“愿望”足够强烈时，由天空岛降下的外置魔力器官，赋予使用者驱动元素力的能力。其发放机制与“神之心”不同，目的成谜，可能与收集“愿望”或“野心”有关。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (131, 82, 'AI-5417', '深渊教团', '由坎瑞亚遗民受深渊力量侵蚀转化而成的非人组织，效忠“深渊”。他们企图颠覆天理秩序，利用诸如特瓦林的痛苦等，将提瓦特生物转化为战争兵器。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (132, 82, 'AI-2547', '愚人众', '至冬国的外交与军事组织，实际听命于冰之女皇。其目的是收集七神之心以对抗天理。执行官是其核心战力，各自拥有强大实力与独特背景。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (133, 82, 'AI-7071', '冰之女皇的计划', '至冬国女皇领导的、旨在反抗“天理”的宏大计划。核心行动是命令愚人众收集全部七颗神之心，目的可能是切断监控、重构第三降临者力量或制造对抗天理的武器。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (134, 82, 'AI-3838', '天理的维系者', '游戏开场动画中拦截双子的神秘女神，使用暗红色方块操纵空间。推测她是“空之影”的代行者或化身，职责是维系提瓦特空间的稳定与隔绝，阻止内外穿越。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (135, 82, 'AI-2192', '坎瑞亚灾变', '约500年前，无神的国度坎瑞亚因发展“禁忌知识”触犯天理，招致七神与天理力量的联合打击，国度覆灭，国民变为怪物（丘丘人等），深渊力量大规模泄漏，污染七国。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (136, 82, 'AI-1343', '禁忌知识', '来自世界之外的、不被提瓦特法则所容的知识与力量。接触它会污染地脉、引发疯狂与变异。坎瑞亚因研究它而招致毁灭，赤王也曾因引入它而给须弥带来灾祸。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (137, 82, 'AI-5807', '魔神', '提瓦特原生或外来的强大超凡存在，拥有操纵元素、塑造地貌等伟力。在魔神战争中彼此征战，胜者成为七神统治国度，败者则面临封印、死亡或流亡等命运。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (138, 82, 'AI-2970', '降临者', '来自提瓦特世界之外的个体。目前已提及第一（法涅斯）、第二（入侵者）、第三（遗骨制神之心者）与第四（旅行者或其血亲）降临者。他们是改变世界格局的关键变量。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (139, 82, 'AI-1143', '人界力', '指法涅斯击败七龙王后，在提瓦特确立的、以“人类”或“人形智慧生命”为核心的新秩序法则。与之前龙族统治的原始元素秩序相对。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (140, 82, 'AI-4536', '虚假阿贝多', '在“白垩与黑龙”活动尾声于雪山出现的疑似由骗骗花伪装或莱茵多特制造的另一个“阿贝多”。其出现与真假难辨的暗示（如星形印记）构成了一个至今未完全揭开的悬疑伏笔。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (141, 82, 'AI-6004', '尘世七执政', '魔神战争的最终胜利者，获得天理认可并授予“神之心”，分别统治提瓦特七国的七位神明。他们管理元素，引导人民，但同时也受“天理”契约的约束。', '高三', NULL, NULL, '2026-01-09 22:18:12', '2026-01-09 22:18:12');
INSERT INTO `biz_knowledge_point` VALUES (142, 82, 'AI-8631', '风鹰剑', '西风骑士团的象征性武器，曾是蒙德的守护者之剑。可能为初代西风骑士团大团长所有，铭文\'承载千年的自由之风\'。与温妮莎推翻贵族统治相关，参与了旧蒙德革命，见证了温妮莎升天，并在龙灾中由迪卢克使用对抗深渊。象征蒙德\'自由\'理念的武装化，强调自由必须有力量守护。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (143, 82, 'AI-4152', '磐岩结绿', '传说为削月筑阳真君赠予凡人战士的剑，使用璃月特有的\'琉璃百合石髓\'锻造。赠予对象是魔神战争时期一位守护归终的凡人战士，但战士未能保护归终，剑在归终死后流落。剑身随持有者生命值变化颜色，象征生命与意志的共鸣，见证\'凡人无法保护神明\'的残酷现实。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (144, 82, 'AI-751', '雾切之回光', '雷电真生前使用的佩剑，现由雷电影保管。名字意为\'切开迷雾，洞察真相\'和\'逝者临终前的最后光辉\'。见证了雷电真在魔神战争时期的征战、与友人的相处，以及坎瑞亚灾变中的牺牲。雷电影封存此剑以封存关于姐姐的记忆，避免被磨损侵蚀，刻有铭文\'雾散，光逝，唯有永恒不变的雷鸣\'。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (145, 82, 'AI-7653', '苍古自由之誓', '象征反抗贵族统治的剑，铸造于温妮莎革命时期（约1000年前）。代表对自由的誓言，使用者为历代反抗压迫的战士。铭文\'自由之所以珍贵，是因为它是用鲜血换来的\'。与枫原万叶的友人相关，友人在面对雷电将军\'无想一刀\'时持类似理念的剑，其意志被万叶继承。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (146, 82, 'AI-2433', '狼的末路', '北风骑士的象征，代表蒙德最强的武力。首任持有者可能是温妮莎时代的北风骑士，\'狼\'隐喻野性、力量和孤独，\'末路\'意为战至最后一刻的决绝。参与了杜林之战，见证了蒙德最黑暗的时刻。与雷泽相关，他可能继承此剑成为新\'北风之狼\'。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (147, 82, 'AI-7933', '无工之剑', '传说为一位失去双手的璃月工匠用意念锻造的剑。工匠在意外失去双手后，用岩元素力量悬浮锤子锻造，耗尽生命完成。证明\'意志可以超越肉体的局限\'，是对人类潜力的赞歌。剑身自动吸收周围岩元素能量，无需持有者刻意注入。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (148, 82, 'AI-7667', '赤角石溃杵', '鬼族传承的武器，代表鬼族的力量与荣耀。由上古鬼族铸造大师使用赤鬼角与岩脊龙髓锻造，世代由鬼族首领持有。荒泷一斗继承此杵，表明他血统不纯但意志纯粹，得到鬼族认可。铭文\'角折可再生，意志不可屈\'。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (149, 82, 'AI-4278', '和璞鸢', '璃月千岩军制式长枪之祖，名字中\'和璞\'指未经雕琢的玉石，象征璞玉般的战士；\'鸢\'象征锐利与高远。在魔神战争中用于抵御魔物，归终之死时持此枪的千岩军全军覆没，摩拉克斯将其列为圣物纪念。与魈相关，代表\'凡人的勇气\'。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (150, 82, 'AI-1652', '护摩之杖', '与死亡和往生堂有关的神秘权杖，\'护摩\'意为梵语中的\'火供\'，指焚烧供养。可能是往生堂历代堂主的传承武器，胡桃作为第77代堂主继承。力量随持有者生命值降低而增强，象征\'越接近死亡，越理解生命的珍贵\'，教导在死亡面前选择生的意义。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (151, 82, 'AI-4992', '薙草之稻光', '雷电影的配枪，代表\'无想\'的境界。名字中\'薙草\'指收割稻草，隐喻\'收割杂念\'；\'稻光\'象征稻妻的闪电和绝对裁决。与雷电将军的\'无想一刀\'配合使用，能够斩断一切\'变数\'。铭文\'于静寂中，斩断因果\'。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (152, 82, 'AI-9120', '四风原典', '记载蒙德四风守护的古老魔法书，四风包括北风之狼（力量与忠诚）、东风之龙（自由与天空）、西风之鹰（守护）、南风之狮（智慧与法律）。旧蒙德时代反抗暴风魔神的魔法师编纂，记录了召唤四风力量的方法。与莫娜的占星术可能相关。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (153, 82, 'AI-2251', '不灭月华', '与璃月夜晚和月亮有关的法器，可能由归终或其门徒创造，使用\'月莲\'精华。能够召唤月华之力，记载延长寿命但避免磨损的方法（月华能够\'刷新\'记忆）。与甘雨相关，她可能知晓此法器的秘密。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (154, 82, 'AI-109', '神乐之真意', '鸣神大社代代相传的神官法器，\'神乐\'指神社祭祀舞蹈，\'真意\'指神明的真实意图。八重神子作为宫司掌握，记载了与雷元素共鸣、辨识雷电将军与雷电影、神樱大祓仪式等。最后一章被雷电影封印，据说记载\'如何唤醒沉睡的神明\'。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (155, 82, 'AI-2861', '终末嗟叹之诗', '一把诗人使用的弓，刻有悲伤诗句，记录了坎瑞亚灭亡的惨状。持有者为一位见证了坎瑞亚最后一夜的吟游诗人，诗文描述星辰陨落、大地焚烧等场景。与温迪相关，暗示他对坎瑞亚灭亡有深刻愧疚。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (156, 82, 'AI-2526', '阿莫斯之弓', '一位名叫阿莫斯的女子为心爱的旧蒙德风神（暴风魔神德卡拉比安）锻造的弓。阿莫斯被神明囚禁在风墙中，后加入反抗军用弓射向爱人高塔。神明死后她才明白爱应给予自由而非禁锢。温迪继承风神之位后封存此弓，代表旧时代悲剧。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (157, 82, 'AI-6327', '飞雷之弦振', '稻妻\'五传\'之一，代表雷电的速度与力量。稻妻五传系统包括一文字（单手剑）、飞雷（弓箭）、薙草（长枪）、万叶（法器）、镰鼬（双手剑）。与九条裟罗相关，她可能是飞雷传承候选继承人，但将弓献给雷电将军作为忠诚证明。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (158, 82, 'AI-4891', '冰风迷途的勇士套装', '圣遗物套装，讲述旧蒙德被风墙围困时代一位反抗军战士的故事。从无垢之花到理之冠，描述孩子出生在暴风魔神统治下，成长为反抗军，等待革命，最终见证自由。套装强调自由是争取来的，留给后世火种。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (159, 82, 'AI-7468', '逆飞的流星套装', '圣遗物套装，讲述璃月千岩军士兵的牺牲故事。士兵立志守护璃月，在魔神入侵时逆风而上，用身体挡下攻向平民的一击，无悔牺牲。套装体现\'明知不可为而为之\'的义士信条，以及凡人对契约的坚守。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (160, 82, 'AI-2810', '雷鸣的愤怒套装', '圣遗物套装，讲述雷电真的战记。从姐妹羁绊到坎瑞亚灾变牺牲，雷电真作为\'光明\'与雷电影\'阴影\'一体双生。真掌握雷元素但内心仁慈，牺牲前留给影遗言，要她找到\'永恒\'。套装象征悲悯与守护。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (161, 82, 'AI-231', '翠绿之影套装', '圣遗物套装，讲述风元素孩子的传说。孩子出生蒙德田野，能与风对话，成为吟游诗人流浪七国，得到温迪祝福。晚年明白自由是在枷锁中保持心灵自由，临终唱起温迪的歌。套装体现自由灵魂的理念。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (162, 82, 'AI-7141', '渡过烈火的贤人套装', '圣遗物套装，讲述须弥学者的献祭故事。学者探索禁忌知识，发现其污染世界树，选择将知识封印于自己灵魂后自我献祭，用烈火净化。套装强调真理的代价是献祭者的生命，体现学者对知识的牺牲。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (163, 82, 'AI-6089', '风啸山丘生态', '蒙德地区受风元素力场影响的地理特征，形成独特浮空岩石。生物群落包括活跃的风史莱姆、丘丘人\'睡风部落\'崇拜风元素，以及深渊法师设立据点研究风场。代表风元素对生态的塑造作用。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (164, 82, 'AI-1761', '龙脊雪山成因', '龙脊雪山由魔龙杜林死后尸体化为山体形成。严寒不仅因气候，更因杜林残留的深渊能量导致\'生命力的冻结\'。生态适应包括雪猪进化厚实毛发、冰史莱姆吸收残余能量，以及古代遗迹守卫在雪中沉睡。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (165, 82, 'AI-3463', '明冠峡谷酒文化', '蒙德酿酒业发源地，以酒文化中心闻名。每年春天蒲公英铺满峡谷形成\'白色海洋\'，是甜甜花蜜主要产地，支持蜜蜂养殖。体现蒙德自然与人文的和谐。', '高三', NULL, NULL, '2026-01-09 22:22:11', '2026-01-09 22:22:11');
INSERT INTO `biz_knowledge_point` VALUES (166, 82, 'AI-4422', '璃沙郊地脉汇聚', '璃月地脉能量最集中的地方，生长琉璃百合（归终最爱的花），长期受仙人力量浸润，普通植物具有微弱岩元素。代表地脉能量对生态的影响和仙力渗透。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (167, 82, 'AI-628', '天衡山脉仙人洞府', '璃月山脉，多位仙人修行之地，包括绝云间、奥藏山（留云借风真君道场）、琥牢山（削月筑阳真君居所）。仙力形成结界隔离魔物入侵，体现仙人隐居和生态隔离。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (168, 82, 'AI-4431', '层岩巨渊深渊污染', '璃月最大矿脉，可能由远古天钉降下砸出巨坑。深处有深渊污染，导致矿工变异成\'黑蛇骑士\'，地脉能量被扭曲，形成\'泥泽\'畸变生物群落。千岩军驻守防止深渊力量泄漏。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (169, 82, 'AI-8318', '暗色的污染来源', '指层岩巨渊中深渊的\'倒悬之物\'污染。影响包括生物变异、地脉扭曲和畸变生态形成。代表深渊力量对提瓦特自然环境的侵蚀和威胁。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (170, 82, 'AI-8270', '鸣神岛神樱树影响', '稻妻鸣神岛的神樱树具有净化作用，吸收大地的污秽，维持元素平衡。岛上动植物都带有微弱雷元素，灵性增强。体现神树对生态和灵性的调节作用。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (171, 82, 'AI-1669', '八酝岛邪神遗骸', '稻妻八酝岛有邪神雅雷尸骸成山。特殊生态包括雷樱树吸收雷元素、雷晶蝶以雷元素为食、雷音权现为雷元素凝聚的拟态生命。代表邪神遗骸对当地生态的影响。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (172, 82, 'AI-3140', '海祇岛珊瑚宫生态', '稻妻海祇岛的珊瑚宫生态，包括活体珊瑚具有微弱意识、发光水母照亮深海、血枝珊瑚吸收奥罗巴斯鲜血具有治疗效果。体现深海生态的独特性和神秘性。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (173, 82, 'AI-2635', '鹤观岛永久迷雾', '稻妻鹤观岛受雷鸟诅咒遗留，形成永久迷雾。虽然诅咒解除，但仍有时间扭曲现象，巨大的雷鸟后裔在此栖息。代表诅咒对环境和时间的长期影响。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (174, 82, 'AI-4595', '道成林兰那罗栖息地', '须弥雨林道成林是兰那罗栖息地，包括恒那兰那（梦境与现实交界）、蘑菇屋（兰那罗居所）、觉王树（记忆存储器）。体现兰那罗与自然和谐共生的生态。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (175, 82, 'AI-9407', '须弥城世界树根系', '须弥城建立在世界树的地表延伸根系上。虚空终端网络遍布全城，连接每个人意识。体现世界树对须弥文明和技术的基础作用。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (176, 82, 'AI-5310', '赤王陵沙漠怪异', '须弥沙漠赤王陵的怪异现象，包括圣甲虫以黄金梦乡残余能量为食、被封印的镇灵偶尔逃脱、赤王的记忆投影形成幻象。代表古代文明遗迹对生态的影响。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (177, 82, 'AI-2870', '千壑沙地居尔城遗迹', '须弥沙漠千壑沙地有被黄沙掩埋的居尔城遗迹。沙尘暴带有诅咒能引发幻觉，沙虫可能是古代镇灵的畸变体。体现沙漠环境的恶劣和古代文明的湮灭。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (178, 82, 'AI-6765', '枫丹廷水网都市', '枫丹水网都市，以运河系统连接整个城市，律法法庭建在水上，歌剧院等建筑体现水文系统与都市设计的结合。代表枫丹独特的水文文化和都市规划。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (179, 82, 'AI-7633', '旧蒙德革命', '旧蒙德时期温妮莎领导推翻暴风魔神德卡拉比安和贵族统治的革命。事件涉及风鹰剑等武器，见证了自由理念的诞生和蒙德新秩序建立，是蒙德历史转折点。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (180, 82, 'AI-1327', '坎瑞亚灾变', '提瓦特重大灾难事件，坎瑞亚王国灭亡，导致星辰陨落、大地焚烧。雷电真在此事件中牺牲，终末嗟叹之诗记录惨状，影响整个提瓦特，引发深渊侵蚀和神明磨损。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (181, 82, 'AI-8501', '魔神战争', '提瓦特远古时期神明之间的战争，争夺领土和信仰。涉及璃月磐岩结绿、和璞鸢等武器，归终、摩拉克斯等人物参与，塑造了七国格局和文明基础。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (182, 82, 'AI-4928', '温妮莎', '蒙德英雄，领导旧蒙德革命推翻贵族统治，后升天成为西风骑士团象征。与风鹰剑、苍古自由之誓等武器相关，代表自由、反抗和蒙德新秩序的建立。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (183, 82, 'AI-2415', '雷电真', '稻妻前代雷神，雷电真的姐姐，象征\'光明\'。在魔神战争和坎瑞亚灾变中活跃，使用雾切之回光，牺牲后遗物由雷电影保管。她的悲悯和牺牲影响稻妻永恒理念。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (184, 82, 'AI-4913', '雷电影', '稻妻现任雷神，雷电真的妹妹，象征\'阴影\'。追求永恒理念，使用薙草之稻光和无想一刀，封存雾切之回光以避免磨损。她的执念和统治塑造稻妻现代历史。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (185, 82, 'AI-6649', '归终', '璃月魔神，与摩拉克斯结盟，喜爱琉璃百合。在魔神战争中牺牲，磐岩结绿等武器与她相关，代表璃月古代文明和悲剧，她的遗产影响璃月生态和文化。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (186, 82, 'AI-6108', '蒙德自由理念', '蒙德核心理念，强调自由必须有力量守护，源自旧蒙德革命和温妮莎的斗争。体现在风鹰剑、苍古自由之誓等武器中，象征蒙德人民对自由的追求和捍卫。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (187, 82, 'AI-6030', '磨损概念', '提瓦特神明因时间流逝而记忆和力量衰退的现象。雷电真因此牺牲，雷电影封存记忆以避免磨损，影响神明决策和永恒追求。是原神世界观中的重要哲学概念。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (188, 82, 'AI-5552', '稻妻永恒理念', '稻妻雷电影追求的核心理念，旨在通过静止避免磨损和灾难。体现在薙草之稻光等武器中，但受雷电真牺牲和坎瑞亚灾变影响，代表对变化和时间的抗拒。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (189, 82, 'AI-7783', '禁忌知识', '提瓦特中能够污染世界树的危险知识，须弥学者在渡过烈火的贤人套装故事中探索并牺牲以封印它。代表知识的力量与代价，以及深渊对世界的威胁。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (190, 82, 'AI-3183', '稻妻五传系统', '稻妻五大武器传承体系，对应五种武器类型：一文字（单手剑）、飞雷（弓箭）、薙草（长枪）、万叶（法器）、镰鼬（双手剑）。体现稻妻文化和战斗艺术的传承，飞雷之弦振属于此系统。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (191, 82, 'AI-4144', '圣遗物五件套结构', '圣遗物套装由五件组成：无垢之花、贤医之羽、时之沙、理之冠、炎之爵杯，每件讲述故事的一部分，共同构成完整人物或文明史诗。例如冰风迷途的勇士套装讲述旧蒙德遗民故事，体现叙事与游戏机制的融合。', '高三', NULL, NULL, '2026-01-09 22:22:12', '2026-01-09 22:22:12');
INSERT INTO `biz_knowledge_point` VALUES (192, 82, 'AI-2760', '温迪的三重身份', '温迪的身份有三重：最初是弱小的风精灵巴巴托斯；继承挚友遗愿成为蒙德风神巴巴托斯；以及以凡人形态行走世间的吟游诗人温迪。这三重身份体现了他从纯粹精灵到神明再到融入人间的转变，反映他对自由理念的实践。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (193, 82, 'AI-8734', '温迪选择“最弱”的原因', '温迪自称“七神中最弱”，这是刻意的选择：哲学上，他认为强大力量会导致控制欲，与自由对立；为避免记忆磨损，通过沉睡减少神力使用；同时信任人类能自己解决问题，体现他的自由意志理念和对人类的信赖。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (194, 82, 'AI-8293', '温迪挚友之死的创伤', '温迪的吟游诗人形态是模仿旧蒙德革命中牺牲的红发挚友，这个形态作为“活着的墓碑”永恒纪念那位为自由而死的灵魂。挚友之死给温迪带来持久创伤，影响他的行为和深层心理，使他承载巨大孤独。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (195, 82, 'AI-2145', '温迪神之心被夺的真相', '温迪几乎未抵抗就让女士夺走神之心，原因包括：他早知神之心是“枷锁”而非力量；可能与冰之女皇有500年前的契约；认同女皇“燃烧旧世界”的部分理念。这体现他对神之本质的深刻理解和战略考量。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (196, 82, 'AI-5648', '迪卢克父亲之死的真相', '克里普斯·莱艮芬德为保护车队使用愚人众的邪眼，导致反噬异化为魔物，迪卢克被迫亲手杀死父亲。西风骑士团为维护声誉隐瞒真相，称其为意外身亡。这事件成为迪卢克人生的转折点，引发对骑士团的失望。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (197, 82, 'AI-5456', '迪卢克对骑士团的失望', '迪卢克因父亲之死看透西风骑士团的虚伪：组织重视荣誉胜过真相、程序胜过正义、形象胜过行动。这导致他辞去骑士身份，转而以黑暗骑士方式守护蒙德，体现他对体制的批判和个人正义的追求。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (198, 82, 'AI-8692', '迪卢克与凯亚的复杂关系', '迪卢克与凯亚原是亲密义兄弟，但父亲死后，凯亚坦白坎瑞亚血统，迪卢克视其为间谍，两人决裂。尽管如此，他们仍在暗中保护对方，关系充满信任与背叛的纠葛，反映身份与情感的冲突。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (199, 82, 'AI-881', '迪卢克的深层心理：罪疚感', '迪卢克背负三重罪疚感：弑父之罪（被迫杀死父亲）、无能之罪（未能保护父亲）和背叛之罪（离开骑士团辜负父亲期望）。他通过守护蒙德赎罪，但内心创伤从未愈合，驱使他以黑暗骑士身份行动。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (200, 82, 'AI-7779', '琴的代理团长压力', '琴作为代理团长，承担全部责任：团长法尔伽远征空缺，她处理政务、军务和外交，几乎没有休息时间。这种超负荷工作导致她身心濒临崩溃，体现她对蒙德的责任感和自我牺牲精神。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (201, 82, 'AI-5756', '琴完美主义的根源', '琴的完美主义源于家族期望（古恩希尔德家族的荣耀）、对蒙德的责任感（配得上蒲公英骑士称号）以及对温妮莎的崇拜。这驱使她不断追求卓越，但也导致过度压力和自我牺牲。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (202, 82, 'AI-8255', '琴与芭芭拉的姐妹关系', '琴和芭芭拉因父母离异而疏远：琴继承父亲事业成为骑士，芭芭拉随母亲成为牧师。两人渴望亲近但不知如何开口，各自以不同方式守护蒙德，却无法修复姐妹情，反映家庭分裂对情感的影响。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (203, 82, 'AI-2279', '琴的深层心理：恐惧辜负期待', '琴最深的恐惧是辜负他人期待，她宁愿自己崩溃也不愿让人失望。这种心理源于完美主义和对责任的执着，驱使她不断透支自我，体现伟大但危险的自我牺牲倾向。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (204, 82, 'AI-3072', '凯亚的真实身份：坎瑞亚后裔', '凯亚是坎瑞亚后裔，可能为贵族子嗣或被派到蒙德的“楔子”（潜伏者）。这一身份使他处于深渊教团与蒙德之间的夹缝，引发他的忠诚冲突和间谍生活，塑造其复杂人格。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (205, 82, 'AI-9362', '凯亚被抛弃的真相', '凯亚的父亲将他“抛弃”在蒙德，可能出于保护（远离深渊教团控制）、任务（关键时刻为坎瑞亚服务）或两者皆有。这事件奠定凯亚的孤独和被遗弃感，影响其人生选择和人际关系。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (206, 82, 'AI-7763', '凯亚的双面间谍生活', '凯亚可能扮演三重间谍：对蒙德是忠诚骑兵队长；对深渊教团是潜伏楔子；对自己则在寻找真正立场。这种双重生活使他无法完全信任他人，以玩世不恭态度掩盖内心挣扎。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (207, 82, 'AI-8707', '凯亚的深层心理：孤独', '凯亚因身份和欺骗而极度孤独：他无法完全信任任何人，不知自己立场，用伪装掩盖内心挣扎。这种孤独源于坎瑞亚血统和间谍角色，使他成为最孤独的角色之一。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (208, 82, 'AI-8083', '钟离六千年的孤独', '摩拉克斯（钟离）活了六千年，见证魔神战争、无数友人死亡和璃月变迁。这种长寿带来巨大孤独，他承载沉重记忆，面对磨损的威胁，体现神明永生背后的悲哀。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (209, 82, 'AI-2445', '归终之死的影响', '归终是钟离的智慧导师和可能挚爱，她的死在钟离心中留下永恒伤痛。钟离未能守护她，这影响他对情感的理解和隐退决定，归终留下的“尘世之锁”象征他对人类情感的探索。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (210, 82, 'AI-4767', '“尘世之锁”的真相', '归终留下的尘世之锁要求摩拉克斯理解人类情感才能打开。六千年未开，因神明难以理解有限生命的珍贵和非理性情感。锁象征钟离需“成为人”才能解锁情感，体现神性与人性的鸿沟。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (211, 82, 'AI-1133', '钟离隐退的原因', '钟离隐退不仅因磨损，还包括：若陀悲剧警示永生诅咒；对人类的愧疚（保护可能阻碍成长）；与天理契约要求退位。这反映他对神职的反思和将未来交给人类的理念。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (212, 82, 'AI-9315', '魈的过去罪孽与业障', '魈曾被邪神控制，作为杀戮机器屠杀无数无辜者，手上沾满鲜血。这些记忆成为他的“业障”，带来持续痛苦和心理负担，驱使他通过守护璃月赎罪。', '高三', NULL, NULL, '2026-01-10 10:01:50', '2026-01-10 10:01:50');
INSERT INTO `biz_knowledge_point` VALUES (213, 82, 'AI-4211', '五夜叉的悲剧', '魈曾与其他四位夜叉共同守护璃月，但其他夜叉均被业障吞噬或牺牲：应达发狂失踪、伐难侵蚀而死、弥怒自相残杀、金鹏封印污秽而亡。魈作为唯一幸存者，承受五份业障，加深他的孤独。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (214, 82, 'AI-2074', '魈的契约与赎罪', '魈承受巨大痛苦但从未求死，因为这是对摩拉克斯的契约、对死去同伴的责任以及对自身罪孽的赎罪。他通过守护璃月来履行义务，体现其坚韧和赎罪心理。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (215, 82, 'AI-7368', '魈与旅行者关系', '魈对旅行者态度从冷漠逐渐转变为珍视：最初因害怕业障伤害而远离；后来认可并保护旅行者；最终视旅行者为朋友，给予“若召吾名，吾必至”承诺。这象征他两千年来首次主动建立人性联系。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (216, 82, 'AI-8354', '甘雨的血统尴尬', '甘雨是人类与麒麟混血，导致她既不属于人类也不属于仙兽：仙人嫌她不够纯粹，人类视她为异类。这种血统使她找不到归属，产生身份认同危机。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (217, 82, 'AI-1963', '甘雨魔神战争的创伤', '甘雨在魔神战争中因过度进食被魔神噎死（后被摩拉克斯救活），这成为心理创伤，导致她害怕被嘲笑身材和自卑，影响她的自我认知和行为。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (218, 82, 'AI-3556', '甘雨三千年的工作', '甘雨为璃月工作三千年，见证历史变迁，但她的古老知识在现代璃月逐渐不被需要，导致她被“时代抛弃”。这引发她对存在意义的迷失和对被遗忘的恐惧。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (219, 82, 'AI-4672', '甘雨的深层心理：恐惧被遗忘', '甘雨最深的恐惧是被遗忘和抛弃，她拼命工作以证明自己“有用”，从而避免被遗弃。这种心理源于血统尴尬和长期孤独，驱使她不断寻求认可。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (220, 82, 'AI-3048', '雷电影与雷电真的关系', '雷电真与雷电影是双生神明：真掌管治理，仁慈智慧；影掌管武力，强大冷酷。影一直是姐姐的影子，负责战斗，真的死亡导致影的崩溃和永恒追求。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (221, 82, 'AI-6302', '真之死的真相', '在坎瑞亚灾变中，雷电真预知死亡，选择牺牲自己保护稻妻，并将神位传给影。真之死对影是毁灭性打击，引发她对变化和失去的恐惧，驱使她追求永恒。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (222, 82, 'AI-3499', '影的永恒追求', '影追求的“永恒”本质是对姐姐之死的逃避、对变化的拒绝和对磨损的对抗。她创造人偶并躲入一心净土，是一种自我囚禁，旨在冻结时间以避免再次失去。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (223, 82, 'AI-6885', '“无想一刀”的本质', '无想一刀不仅是剑技，更是哲学：无想指斩断一切念想（包括对姐姐的思念），一刀指一击决绝不留余地。这是影对自己情感的处决，象征她对永恒的执着。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (224, 82, 'AI-2080', '影被旅行者改变', '旅行者让影明白永恒不是静止而是永恒的瞬间，真正的永恒是记忆而非时间停滞，姐姐希望她“活下去”而非活在过去。这促使影走出一心净土，接受变化。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (225, 82, 'AI-370', '八重神子的遗愿继承', '八重神子继承了狐斋宫的遗愿：担任鸣神大社宫司、守护影的责任和对稻妻的爱。这奠定她与影的复杂关系，驱使她以智慧方式影响稻妻。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (226, 82, 'AI-8108', '八重神子轻小说的目的', '八重经营“八重堂”出版轻小说，真正目的是传播新思想对抗影的永恒、用故事进行文化革命，同时赚钱。这体现她以智慧手段推动社会变革。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (227, 82, 'AI-3573', '八重神子对影的感情', '八重对影感情复杂：担忧影困在过去；无奈无法强迫改变；等待五百年终于等到旅行者带来转机。她作为挚友狐斋宫的继承者，对影又爱又恨。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (228, 82, 'AI-2494', '纳西妲诞生的孤独', '纳西妲是大慈树王的“分支”，继承神格但无记忆，作为新生儿却要承担神的责任。这种诞生方式使她天生孤独，缺乏归属感和自我认同。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (229, 82, 'AI-2136', '纳西妲五百年的囚禁', '教令院将纳西妲囚禁在净善宫五百年，因不认可她的幼稚并掌握虚空终端权力，她成为吉祥物而非统治者。这囚禁加剧她的孤独和自我怀疑。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (230, 82, 'AI-6066', '纳西妲的自我怀疑', '纳西妲一直怀疑自己：“我真的是神吗？”“我配得上大慈树王的遗产吗？”“我有资格被爱吗？”这种自我怀疑源于囚禁和缺乏认可，影响她的成长。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (231, 82, 'AI-4251', '大慈树王的真相与纳西妲的成长', '纳西妲原本就是大慈树王，树王为拯救世界删除自己，纳西妲必须亲手从世界树中删除树王。这过程使她理解牺牲、接受孤独，成为真正的智慧之神。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (232, 82, 'AI-6279', '流浪者三次被抛弃的经历', '流浪者（散兵）三次被抛弃：被雷电影封存视为缺陷；被人类背叛导致阿桂之死；被愚人众利用作为成神实验品。这些经历塑造他“不被需要”的创伤和暴戾性格。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (233, 82, 'AI-9889', '流浪者的重生：接受过去', '在旅行者和纳西妲帮助下，流浪者接受自己的过去，选择流浪而非复仇，重新获得人性。他从散兵变为流浪者，体现从无根浮萍到主动选择旅行的转变。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (234, 82, 'AI-4860', '流浪者的深层心理：恐惧不被需要', '流浪者的核心创伤是“我不被任何人需要”，这恐惧源于三次被抛弃经历。他的暴戾和自我毁灭倾向都为此，重生后他开始寻找自我价值。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (235, 82, 'AI-6387', '芙宁娜五百年的孤独表演', '芙宁娜作为水神人格，必须扮演完美的神五百年，不能泄露真相，承受巨大心理压力。这种表演使她孤独并混淆真实自我，体现凡人为使命的牺牲。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (236, 82, 'AI-178', '芙宁娜演技背后的真相', '芙宁娜的夸张演技是为掩盖凡人本质、让人们相信她是神并维持谎言。这背后是芙卡洛斯的计划：分裂神格和人格，以凡人意志对抗预言。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (237, 82, 'AI-9579', '芙宁娜的审判终结', '审判中真相揭露，芙宁娜崩溃自认骗子，但人们意识到她作为凡人的意志比神格更伟大。她完成使命，失去神力成为真正的人，获得解脱。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (238, 82, 'AI-1268', '芙宁娜的深层心理：真实自我混淆', '芙宁娜最深的痛苦是真实自我与扮演角色混淆，她不知哪个才是真正的自己。五百年表演使她极度自卑和迷茫，审判后开始重新定义自我。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (239, 82, 'AI-2375', '温迪的命之座讽刺：歌仙座', '温迪的命之座“歌仙座”（Carmen Dei）意为“神之歌”，但他最想唱的却是为挚友永远无法完成的挽歌。这讽刺地体现他作为神却承载凡人悲伤的矛盾。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (240, 82, 'AI-1285', '迪卢克的命之座隐喻：夜枭座', '迪卢克的命之座“夜枭座”（Noctua）象征夜枭在黑暗中飞翔捕食害虫，隐喻他作为黑暗骑士在暗中守护蒙德、对抗愚人众和深渊教团的形象。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (241, 82, 'AI-3682', '琴的命之座悲剧：狮子座', '琴的命之座“狮子座”（Leo Minor，小狮座）象征她是“小狮子”试图扮演“大狮子”角色，这导致她透支青春与健康，体现她的自我牺牲和压力重负。', '高三', NULL, NULL, '2026-01-10 10:01:51', '2026-01-10 10:01:51');
INSERT INTO `biz_knowledge_point` VALUES (242, 70, 'AI-1579', '风魔龙·特瓦林', '风魔龙·特瓦林是蒙德的东风之龙，四风守护之一，因深渊教团利用杜林的毒血污染而堕落。经历五百年的痛苦与孤独后，被温迪的《风花的邀约》唤醒记忆，由旅行者净化毒血。现已重返自由，但选择独居风龙废墟，与蒙德保持距离。其泪晶可制作\'风龙的叹息\'，含有净化后的龙血精华。', '高二', NULL, NULL, '2026-01-10 10:08:05', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (243, 70, 'AI-4523', '北风狼·安德留斯', '北风狼·安德留斯是奔狼领的北风之王，狼群先祖，拒绝神明身份而自我放逐，以狼的方式生存。其哲学思想为\'狼不需要主人，强者不需要神明\'。死后以灵魂形态守护奔狼领，传授雷泽狼之骄傲。爪牙材料\'北风之尾\'含有纯粹的冰元素本源，战斗场地为灵魂空间。', '高二', NULL, NULL, '2026-01-10 10:08:05', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (244, 70, 'AI-7981', '公子·达达利亚(魔王武装形态)', '公子·达达利亚是愚人众执行官，幼年堕入深渊获得战斗技艺，魔王武装本质为古国遗迹\'魔铠\'碎片，由深渊改造。追求极致武道，视战斗为生命意义，被警告终将陨落于深渊。其武装使用深渊元素，\'断流\'技艺源自璃月武学但被扭曲，\'吞星之鲸\'形态借鉴深渊巨兽。', '高二', NULL, NULL, '2026-01-10 10:08:05', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (245, 70, 'AI-5453', '若陀龙王', '若陀龙王是岩元素龙王，摩拉克斯挚友，因地脉磨损导致记忆消失而被封印，封印为保护性沉眠。人格分裂为理性与本能交战，体现长生种的磨损诅咒。其名意为\'宝树\'，梵语音译，能驾驭多元素，封印地点\'伏龙树\'由钟离以岩元素神力生成，阿晃(昆钧)带有其善念记忆碎片。', '高二', NULL, NULL, '2026-01-10 10:08:05', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (246, 70, 'AI-2719', '古岩龙蜥', '古岩龙蜥是深海遗种，龙蜥文明的残存个体，具有能量水晶化外壳和元素吸收特性。其文明遗迹在层岩巨渊深处的龙蜥城邦废墟，具备基础文明和使用工具能力。衰亡原因与人类领地战争和深渊侵蚀有关，作为物种代表提瓦特古代生物的灭绝历史。', '高二', NULL, NULL, '2026-01-10 10:08:05', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (247, 70, 'AI-8001', '雷音权现·无想的一刀', '雷音权现是雷电将军的自律程序战斗测试，\'无想\'源自禅宗空无执念境界，一刀奥义为\'梦想的一刀\'压制版本。设计目的是测试挑战者是否有资格面见影，体现永恒与变化的对抗。战斗空间为\'一心净土\'外围，招式暗含日本剑道术语，\'梦想真说\'技能是影的终极防御机制。', '高二', NULL, NULL, '2026-01-10 10:08:05', '2026-01-10 10:08:52');
INSERT INTO `biz_knowledge_point` VALUES (248, 70, 'AI-9503', '正机之神', '正机之神是神樱大祓时封印的魔神怨念集合体，技术本质为稻妻阴阳师的式神巨构，由镇物系统和雷樱树根系压制封印。完全解封可摧毁鸣神岛，现代作为测试神之眼持有者协作能力的试炼。核心为\'祟神之骸\'，类似残余神格，设计灵感来自日本将门传说和傀儡神话。', '高二', NULL, NULL, '2026-01-10 10:08:05', '2026-01-10 10:08:52');
INSERT INTO `biz_knowledge_point` VALUES (249, 70, 'AI-6723', '雷电将军·人偶', '雷电将军人偶是影以雷元素神力制造的永恒执政者，意识构成基于\'梦想一心\'流派剑术理念，替代真身执行永恒之治。五百年执政中逐渐生成自我意识，核心矛盾为永恒程序与人性的萌芽，体现稻妻对永恒主题的哲学探讨。', '高二', NULL, NULL, '2026-01-10 10:08:05', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (250, 70, 'AI-9700', '风蚀沙虫·阿佩普的仆从', '风蚀沙虫是古龙阿佩普的元素造物，在沙漠地下潜行，通过振动捕食，具有岩、雷、冰三相元素切换能力以适应环境。智能程度低，由本能驱动，作为沙漠食物链顶端捕食者，反映须弥沙漠生态的残酷性。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (251, 70, 'AI-1997', '翠翎恐蕈·魔鳞病的产物', '翠翎恐蕈是死域能量污染森林生态导致的变异生物，为菌类集合意识的巨型载体，散播魔鳞病侵蚀生物神经。净化方法需草元素净化或兰那罗的梦境疗法，象征禁忌知识对自然的扭曲，作为须弥生态警示。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (252, 70, 'AI-8033', '掣电树', '掣电树是大慈树王设置的雷元素防御节点，运作机制为吸收雷元素能量以净化死域，失控因死域侵蚀导致程序错乱。设计智慧利用元素克制对抗禁忌知识，作为树王智慧的遗产，仍在守护须弥森林生态。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (253, 70, 'AI-234', '实验性场力发生装置', '实验性场力发生装置是枫丹科学院的力场武器实验体，因原能超载导致自主化失控，能量来源为原海水的始源能量。危险等级可引发局部原海溢出，体现人类技术僭越的伦理问题，象征枫丹科技的双刃剑效应。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (254, 70, 'AI-241', '深海龙蜥之群·原海生物', '深海龙蜥是原海本土生物，具备高度元素抗性，为勒穆利亚文明的遗民，保留部分古文明知识。作为原海上涨的先兆生物和枫丹水域的隐藏霸主，反映枫丹预言危机和古代文明遗产。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (255, 70, 'AI-3259', '「审判者」歌剧魅影', '「审判者」歌剧魅影是原海异物与枫丹戏剧文化的融合体，诞生机制为集体潜意识在原海能量中的投影，象征枫丹人对正义的病态追求具现。艺术隐喻借鉴《歌剧魅影》悲剧美学，存在价值为枫丹文化心理的镜像反射。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (256, 70, 'AI-9576', '火山口的古龙', '火山口的古龙是纳塔塔塔拉乌帕火山口的古代龙族，身份推测为焰之律者级别，具有纯粹的火元素本源力量。作为纳塔诸部族的试炼对象，生态角色为维持火山活动平衡的关键物种，体现纳塔地区的原始力量。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (257, 70, 'AI-8707', '深渊使徒·激流', '深渊使徒·激流是深渊教团的中层指挥官，组织地位高，元素掌控为纯水/激流能量操纵，战术特点包括召唤深渊法师协同作战。智慧程度完整，具备语言和战术规划能力，身份谜团可能与坎瑞亚黑蛇骑士有关，代表深渊教团的军事力量。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (258, 70, 'AI-676', '深渊王室领导层', '深渊王室是深渊教团的最高领导层，领袖为深渊王子/公主，权力来源坎瑞亚王室血统与深渊力量融合，统治方式为精神领袖加战略指挥。核心目标是颠覆天理、复兴坎瑞亚，旗下有王室卫队如深渊咏者和黑蛇骑士。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (259, 70, 'AI-2563', '深渊使徒执行层', '深渊使徒是深渊教团的执行层，身份来源为坎瑞亚高级军官或法师的诅咒形态，保留完整记忆、语言和战术思维。元素类型包括激流、渊火、冰渊使徒，能力特征有空间传送、元素护盾和战术召唤，负责关键任务执行。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (260, 70, 'AI-466', '深渊咏者作战层', '深渊咏者是深渊教团的作战层，身份来源为坎瑞亚中级军官诅咒形态，战斗特点包括紫电咏者的雷元素远程打击、渊火咏者的火元素区域控制、渊冰咏者的冰元素冻结封锁。组织角色为据点守卫、仪式主持和基层指挥。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (261, 70, 'AI-1882', '深渊法师基础层', '深渊法师是深渊教团的基础层，身份来源为坎瑞亚普通士兵或平民的诅咒形态，元素分工包括火深渊法师前锋突击、水深渊法师治疗辅助、冰深渊法师控制限制。智慧程度保留基本语言和执行命令能力，作为教团前线战力。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (262, 70, 'AI-710', '丘丘人工具层', '丘丘人是深渊教团的工具层，身份真相为坎瑞亚平民彻底诅咒形态，退化程度失去语言、文字和大部分记忆。深渊教团通过\'上古语\'操控，设立部落作为前哨，利用繁殖能力扩充兵力，语言残留部分坎瑞亚语词汇变形。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (263, 70, 'AI-6912', '风魔龙堕落计划', '风魔龙堕落计划是深渊教团的重大行动，执行者为深渊使徒·激流，目标为摧毁蒙德并削弱七神威信。方法包括收集杜林毒血结晶、污染特瓦林伤口、利用蒙德人恐惧制造隔阂，试图转化特瓦林为深渊武器，最终因温迪和旅行者净化而失败。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (264, 70, 'AI-5842', '无心钩玉窃取', '无心钩玉窃取是深渊教团在稻妻的重大行动，执行者为深渊咏者·紫电，目标为窃取雷神的无心钩玉。方法利用海祇岛抵抗军混乱期潜入，失败原因为旅行者追踪和神里绫华支援，体现深渊教团对神之物的觊觎。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (265, 70, 'AI-4164', '倒吊者仪式', '倒吊者仪式是深渊教团在层岩巨渊地下的重大行动，执行者为深渊使徒团队，目标为激活\'遗迹守卫耕地机\'军团。方法包括收集坎瑞亚遗迹守卫核心、设置\'倒吊者\'符阵、以深渊能量重启战争机器，失败原因为夜兰、魈和旅行者联合破坏。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (266, 70, 'AI-1706', '勒穆利亚遗产', '勒穆利亚遗产是深渊教团枫丹分部的重大行动，目标为获取勒穆利亚的\'和声\'技术，意图利用和声操控原海能量。行动因枫丹预言危机而搁置，体现深渊教团对古代文明技术的追求和利用。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (267, 70, 'AI-4082', '深渊之门构筑术', '深渊之门构筑术是深渊教团的技术体系之一，原理为空间折叠技术连接深渊与提瓦特，来源自坎瑞亚空间炼金术的堕落形态。限制需要元素能量节点且易被干扰，作为教团进行空间移动和入侵的关键手段。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (268, 70, 'AI-8191', '诅咒扩散实验', '诅咒扩散实验是深渊教团的技术体系，目标为将\'坎瑞亚诅咒\'武器化，方法通过培养\'深渊浊气\'污染地脉和侵蚀生物。体现教团试图利用诅咒作为攻击手段，对提瓦特生态构成威胁。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (269, 70, 'AI-1434', '遗迹守卫改造', '遗迹守卫改造是深渊教团的技术体系，技术内容包括以深渊能量替代原有动力、植入控制指令、提升元素抗性。改造型号有遗迹守卫、遗迹猎者、遗迹重甲、遗迹巨蛇等，作为教团机械化部队的基础。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (270, 70, 'AI-8980', '「织命之人」计划', '「织命之人」计划是深渊教团的终极目标，核心理念为颠覆天理法则、重写提瓦特规则，所需条件包括七神神格力量、坎瑞亚炼金术最高成就、深渊本源力量和\'织命座\'遗迹。危险性可能导致提瓦特崩毁，代表教团的极端野心。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (271, 70, 'AI-2421', '「摇光之主」真相', '「摇光之主」真相是深渊教团的幕后黑手谜团，线索包括《佩里杜尔》记载的\'摇光之主\'、坎瑞亚王族的\'摇光\'姓氏、深渊王子/公主的导师。推测可能是坎瑞亚末代摄政或大贤者，体现教团深层的权力结构。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (272, 70, 'AI-9089', '忘却之峡·塞西莉亚苗圃', '忘却之峡·塞西莉亚苗圃是蒙德秘境，历史背景为旧蒙德时代贵族花园，主人为暴君迪卡拉庇安的宫廷园艺师。秘境成因是元素能量残留形成的时空泡，核心谜团为塞西莉亚花永不凋谢，因迪卡拉庇安风元素祝福，隐藏故事涉及园艺师与反抗军少女的告别。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (273, 70, 'AI-6220', '鹰翔之庭·劳伦斯家族训练场', '鹰翔之庭·劳伦斯家族训练场是蒙德秘境，历史时期为旧贵族统治末期，建造者为劳伦斯家族军事分支。秘境特点为剑术训练幻象无限循环，隐藏boss为\'劳伦斯的幻影\'，文化意义体现优菈以旧贵族剑术对抗罪孽的矛盾。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (274, 70, 'AI-1807', '太山府·岩王帝君的藏书室', '太山府·岩王帝君的藏书室是璃月秘境，真实身份为摩拉克斯3000年前的私人图书馆，藏品包括魔神战争档案、归终机关设计图、七星治国草稿等。秘境成因为钟离设下的岩元素封印，\'太山\'源自道教泰山府君，暗示钟离的审判职能。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (275, 70, 'AI-3462', '无妄引咎密宫·夜叉的忏悔之地', '无妄引咎密宫是璃月秘境，建造者为降魔大圣魈，目的是封印杀业、隔离\'业障\'污染。运作机制以自身痛苦吸收怨念，长期使用导致魈衰弱，隐藏剧情有其他四夜叉衣冠冢，魈每次进入重历失去战友的记忆。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (276, 70, 'AI-6618', '砂流之庭·刀锋试炼场', '砂流之庭·刀锋试炼场是稻妻秘境，历史背景为天狗一族的武艺传承地，现任守护者为荒泷一斗的先祖幻影。试炼内容为破除岩元素屏障、闪避雷元素陷阱、击败\'赤鬼武者\'，文化根源来自日本\'修罗场\'概念，强调独自面对恐惧。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (277, 70, 'AI-4064', '椛染之庭·神樱大祓仪式遗迹', '椛染之庭·神樱大祓仪式遗迹是稻妻秘境，历史事件为500年前雷电真主持的大祓，封印对象为深渊污染、怨念和灾厄。秘境结构有五层结界对应五行，巫女幻影重复雷电真最后仪式，是雷电影不愿面对的记忆空间。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (278, 70, 'AI-890', '有顶塔·教令院禁忌实验室', '有顶塔·教令院禁忌实验室是须弥秘境，建造时期为六贤者时代，实验内容包括神之心能量研究、虚空终端开发、人造神计划原型。危险等级为S级封锁区，关键发现为\'散兵\'前有\'七柱试作型\'实验体全部崩溃。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (279, 70, 'AI-3028', '智慧之殿·大慈树王的梦境遗产', '智慧之殿·大慈树王的梦境遗产是须弥秘境，本质为树王残存在世界树中的记忆碎片，进入条件需纳西妲草元素神力引导。内容包括树王战斗记忆、禁忌知识真相、纳西妲诞生秘密，重大揭示为树王预见了自己的消失，留下秘境让纳西妲找到超越神明的智慧。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (280, 70, 'AI-6418', '正义之庭·模拟审判空间', '正义之庭·模拟审判空间是枫丹秘境，技术本质为枫丹科学院的虚拟现实系统，训练对象为律政官和决斗代理人，AI系统为\'永恒审判官\'芙卡洛斯思维模型。哲学探讨正义的多面性，许多律政官在此精神崩溃，体现绝对正义的虚幻。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (281, 70, 'AI-9584', '水仙十字·原海记忆封存库', '水仙十字·原海记忆封存库是枫丹秘境，建造者为艾格莉亚，封存内容为勒穆利亚文明影像、\'原罪\'真相和预言完整版本。进入代价为吸收访问者寿命，悲剧发现为艾格莉亚献出全部寿命化为秘境部分，意识仍在守护真相。', '高二', NULL, NULL, '2026-01-10 10:08:06', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (282, 70, 'AI-319', '秘境的形成机制', '秘境的形成机制是元理学解释，学术定义为地脉能量异常聚集形成时空扭曲，触发条件包括重大历史事件情感残留、元素能量长期聚集、特殊地理位置。类型分类有记忆型、试炼型、封印型和宝库型，学者理论为秘境是世界树的梦境投影。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (283, 70, 'AI-2964', '秘境的时间法则', '秘境的时间法则是元理学解释，包括内外时差（秘境内1小时等于外界数分钟）、因果隔离（秘境内死亡不影响现实）和记忆重置（某些秘境每日重置内容）。学者理论认为秘境对应世界树记录的历史，进入即读取世界树记忆。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (284, 70, 'AI-1056', '蒙德星座命名法则', '蒙德星座命名法则文化根源为古希腊神话和凯尔特传说，强调英雄主义和自由意志。典型案例包括迪卢克的\'苦闷座\'、温迪的\'歌仙座\'、琴的\'狮子幼兽座\'、优菈的\'浪沫座\'。深层含义普遍包含挣脱束缚的意象，如可莉的四叶草座象征打破规则的自由。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (285, 70, 'AI-8018', '璃月星座命名法则', '璃月星座命名法则文化根源为中国神话和道教星宿，强调宿命、责任和轮回。典型案例包括钟离的\'岩王帝君座\'、魈的\'金翅鹏王座\'、胡桃的\'彼岸蝶座\'、甘雨的\'仙麟座\'。深层含义强调生死循环，如七七的法铃座象征生死边界的守门人。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (286, 70, 'AI-7916', '稻妻星座命名法则', '稻妻星座命名法则文化根源为日本神道和佛教无常观，强调无常之梦。文本中描述不完整，但基于已有信息，稻妻星座命名特点反映无常和变化，深层含义可能涉及禅宗哲学和命运的无常性。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (287, 70, 'AI-5816', '特瓦林的泪晶与风龙的叹息', '特瓦林的泪晶可制作\'风龙的叹息\'，含有净化后的龙血精华，是特瓦林堕落与救赎过程的物质体现。这些泪晶在蒙德炼金术中有重要价值，象征从痛苦中提取的纯净力量，反映特瓦林作为东风之龙的元素本质和深渊污染的净化结果。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (288, 70, 'AI-602', '安德留斯的自我放逐哲学', '安德留斯的自我放逐哲学体现在其拒绝神明身份、选择以狼的方式生存，思想核心为\'狼不需要主人，强者不需要神明\'。这反映其追求野性尊严和独立，死后以灵魂形态守护奔狼领，传授雷泽狼之骄傲，作为北风之王的遗产。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (289, 70, 'AI-8745', '若陀龙王的磨损诅咒', '若陀龙王的磨损诅咒是所有长生种的宿命，因地脉侵蚀导致记忆消失和人格分裂。其悲剧根源在于磨损不可避免，最终遗忘一切被封印，象征提瓦特世界中时间与记忆的脆弱性，作为岩元素龙王的命运警示。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (290, 70, 'AI-6624', '雷电将军人偶的自我意识发展', '雷电将军人偶的自我意识在五百年执政中逐渐生成，核心矛盾为永恒的程序与人性的萌芽。体现影对永恒之治的反思和人偶作为工具却发展出独立意志的哲学问题，反映稻妻永恒主题的复杂性和人工智能的伦理困境。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (291, 70, 'AI-3478', '深渊教团的语言残留', '深渊教团的语言残留主要体现在丘丘人使用的\'上古语\'，为坎瑞亚语词汇的变形，如\'Olah\'(你好)、\'Mosi mita\'(吃肉)。这些残留语言证明丘丘人曾是坎瑞亚平民，深渊教团通过此操控它们，作为坎瑞亚文明毁灭的痕迹。', '高二', NULL, NULL, '2026-01-10 10:08:07', '2026-01-10 10:08:36');
INSERT INTO `biz_knowledge_point` VALUES (292, 58, 'AI-7535', '七龙主宰期', '提瓦特的第一纪元，人类诞生之前，由七位元素龙王统治世界，包括东风之龙、岩脊之龙等。文明特征为元素生物主导，人类处于原始状态，龙族建立元素法则体系。衰落原因为天理降临，第一次‘钉’降下，打破龙族统治，七龙王或死或退隐，神座空出。', '高一', NULL, NULL, '2026-01-10 10:39:59', '2026-01-10 10:39:59');
INSERT INTO `biz_knowledge_point` VALUES (293, 58, 'AI-7049', '魔神割据期', '提瓦特的第二纪元，距今3700年前～2600年前，魔神数量超过100位，以魔神领地为单位割据势力。主要魔神包括摩拉克斯、巴尔、巴巴托斯等，无固定国界，为魔神战争前的混乱时期。', '高一', NULL, NULL, '2026-01-10 10:39:59', '2026-01-10 10:39:59');
INSERT INTO `biz_knowledge_point` VALUES (294, 58, 'AI-5241', '天理的神位竞赛', '魔神战争的导火索，由天理维系者宣告，规则为提瓦特只需七位神明，每种元素选出一位最强者，通过杀死竞争者或获得信徒拥护获胜。隐藏目的是削减魔神数量、消耗提瓦特本土力量，并测试谁有资格接近真相。', '高一', NULL, NULL, '2026-01-10 10:39:59', '2026-01-10 10:39:59');
INSERT INTO `biz_knowledge_point` VALUES (295, 58, 'AI-1287', '摩拉克斯', '岩神，璃月地区的魔神，魔神战争中最强者之一，通过武力获胜成为七神之一。关键战役包括对抗奥赛尔、镇压多位魔神形成孤云阁。战后建立璃月港，退休后化名钟离。', '高一', NULL, NULL, '2026-01-10 10:39:59', '2026-01-10 10:39:59');
INSERT INTO `biz_knowledge_point` VALUES (296, 58, 'AI-9359', '巴巴托斯', '风神，最初为风精灵，参与旧蒙德革命，挚友战死后继承其形象，吸收暴风魔神迪卡拉庇安神格成为新神。七神确立后活跃，装成醉酒诗人，神之心被女士抢走。', '高一', NULL, NULL, '2026-01-10 10:39:59', '2026-01-10 10:39:59');
INSERT INTO `biz_knowledge_point` VALUES (297, 58, 'AI-5771', '雷电真', '原初雷神，稻妻的魔神之一，与影为双子，通过武力获胜成为七神。参与斩杀雷鸟和大蛇奥罗巴斯，战后统治稻妻，后死亡由影继承神位。', '高一', NULL, NULL, '2026-01-10 10:39:59', '2026-01-10 10:39:59');
INSERT INTO `biz_knowledge_point` VALUES (298, 58, 'AI-2798', '归终之死', '盐之魔神归终在魔神战争期间被人类信徒暗杀，原因是她过度温柔，信徒认为她无法在战争中保护族群。摩拉克斯为其收尸，封存于盐神遗迹，象征魔神战争中的悲剧。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (299, 58, 'AI-8066', '奥赛尔封印', '漩涡魔神奥赛尔在魔神战争中被摩拉克斯击败，封印于璃月海域。他是璃月战场的关键参战者，在群玉阁海域大战中败北，战后以封印状态残存。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (300, 58, 'AI-7562', '孤云阁之战', '魔神战争中的关键战役，摩拉克斯以‘天星’镇压多位魔神，形成孤云阁群岛（由天星碎片组成）。这场战役巩固了摩拉克斯在璃月的统治，展示了其强大武力。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (301, 58, 'AI-2197', '风墙攻防战', '魔神战争中蒙德战场的战役，暴风魔神迪卡拉庇安以风墙封锁旧蒙德城，实为囚禁子民，持续数百年。最终被革命军和温迪打破，导致旧蒙德革命和迪卡拉庇安死亡。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (302, 58, 'AI-4662', '旧蒙德革命', '发生在约公元前2600年的革命，由红发战士、无名少年和温迪（风精灵）发起，推翻暴风魔神迪卡拉庇安的统治。温迪挚友战死，温迪获得其形象并成神，巴巴托斯就此诞生。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (303, 58, 'AI-4841', '鹤观惨案', '魔神战争中稻妻战场的事件，雷鸟因族人被杀而暴怒，毁灭鹤观岛文明。雷电将军斩杀雷鸟，鹤观变为死地，留下永久雷暴和遗迹。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (304, 58, 'AI-7572', '八酝岛之战', '魔神战争中稻妻的关键战役，海祇大御神奥罗巴斯侵犯鸣神领地，被雷电将军以‘无想的一刀’斩断，形成八酝岛（大蛇骸骨岛）。奥罗巴斯临死前留下‘深海之书’，预言稻妻未来。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (305, 58, 'AI-8889', '七神确立', '魔神战争第二阶段（前2100～前2000年）的结果，七位神明就位，包括岩神摩拉克斯、风神巴巴托斯、雷神雷电真等。这标志着魔神时代结束，七国体系建立，各神获得神之心。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (306, 58, 'AI-3257', '天空岛条约', '推测在魔神战争后签订，由天理维系者与七神达成，确立战后秩序。内容包括各神获得神之心、建立七国体系，但具体细节未知，是提瓦特权力结构的基础。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (307, 58, 'AI-8375', '神之心', '七神在魔神战争后获得的神秘物品，象征神位和力量。具体功能未知，但与天理有关，部分神之心被至冬国收集（如摩拉克斯交给女士，巴巴托斯被抢走），可能涉及终极秘密。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (308, 58, 'AI-5592', '封印魔神', '魔神战争后残存的魔神类型之一，包括奥赛尔、其妻巴徹等被封印于各国海域或秘境。这些魔神未死但被限制活动，对提瓦特构成潜在威胁。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (309, 58, 'AI-5713', '退隐魔神', '魔神战争后残存的魔神类型之一，包括若陀龙王（封印前和平期）、安德留斯（自愿隐居奔狼领）和那维莱特（水龙王转生伪装人类）。他们选择退出争斗，但影响力犹存。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (310, 58, 'AI-5034', '转化形态魔神', '魔神战争后残存的魔神类型之一，包括杜林（毒龙，被炼金术师改造）和深渊生物（部分为败北魔神残渣转化）。这些生物形态改变，成为魔物或深渊势力的一部分。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (311, 58, 'AI-3029', '魔神战争代价', '魔神战争造成的巨大损失，包括人类伤亡数百万、数十个古文明消失（如盐神族群、鹤观文明）、环境破坏（层岩巨渊空洞、无妄坡怨念）和精神创伤（魈的业障、钟离磨损）。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (312, 58, 'AI-4347', '史莱姆基础生物学', '史莱姆是纯元素能量凝聚的低级生命体，智力相当于昆虫，繁殖通过元素能量达到阈值时自然分裂，寿命1-3年。生态位是元素循环的基础消费者，遍布提瓦特各地。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (313, 58, 'AI-5408', '火史莱姆', '火元素史莱姆，栖息于火山口、熔岩区，行为喜欢聚集取暖，遇水会蒸发自身。变种包括大型火史莱姆和变异火史莱姆，掉落物为史莱姆凝液，可用于制作火元素药剂。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (314, 58, 'AI-558', '水史莱姆', '水元素史莱姆，栖息于湖泊、河流、海岸，特殊能力是治愈自己和滋润周围植物。在沙漠中，水史莱姆是绿洲形成的关键，变种有大型水史莱姆和治愈水史莱姆。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (315, 58, 'AI-3252', '风史莱姆', '风元素史莱姆，栖息于高山、悬崖、风场，行为会利用风力悬浮‘飞行’，产生小型上升气流。用途被蒙德飞行俱乐部研究，掉落物包括史莱姆凝液和飓风之种。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (316, 58, 'AI-1104', '雷史莱姆', '雷元素史莱姆，栖息于雷暴区、鸣神岛、电气水晶附近，危险性高，接触会触电。特殊能力是吸收雷电增强自身，科研价值高，须弥学者研究其发电机制。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (317, 58, 'AI-8232', '冰史莱姆', '冰元素史莱姆，栖息于雪山、冻土、冰窟，行为冬眠期长，活动期短，防御机制为冻结护盾。生态作用是维持雪山生态平衡，防止冰川融化，掉落物包括史莱姆凝液。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (318, 58, 'AI-2668', '岩史莱姆', '岩元素史莱姆，栖息于矿洞、岩石山、璃月山区，防御力在七种史莱姆中最高，特殊能力是吸收岩元素形成护盾。常与水晶矿伴生，可能参与矿物形成。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (319, 58, 'AI-1680', '草史莱姆', '草元素史莱姆，栖息于须弥雨林、草丛茂密区，智力相对较高，会躲藏。生态作用是传播植物种子，促进森林繁殖，与兰那罗关系友好，被视为‘森林之子’。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (320, 58, 'AI-4729', '无相之体本质', '无相之体是高纯度元素的自我意识萌芽，形成条件需要极高浓度单一元素聚集、长时间稳定存在和未知觉醒触发。智力中等，具备战术意识，生命周期理论上永久。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (321, 58, 'AI-3288', '无相之雷', '无相之雷位于蒙德望风山地，行为模式为守护型，不主动攻击远距离目标。能力包括形态变化、雷元素领域和复活机制（生成雷晶核），起源可能是雷魔神战争残留能量具现。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (322, 58, 'AI-6585', '无相之风', '无相之风位于蒙德风起地，攻击性最低，更像在玩耍。能力包括龙卷风、吸力球和强扩散反应，生态影响是维持风起地的风场系统，蒙德官方禁止杀死除非威胁城市。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (323, 58, 'AI-9391', '无相之岩', '无相之岩位于璃月孤云阁，防御力最高，能力包括建造岩柱迷宫、陨石打击和震荡波。历史意义可能是摩拉克斯‘天星’残留能量的具现，璃月学者研究其能量转化机制。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (324, 58, 'AI-1719', '无相之冰', '无相之冰位于龙脊雪山，可能与毒龙杜林的寒气有关，是无相系列之一。作为高纯度冰元素生物，具备自我意识，守护特定区域，攻击模式包括冰元素技能。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (325, 58, 'AI-5157', '无相之水', '无相之水位于璃月荻花洲，与地脉水元素节点相连，是无相系列之一。作为水元素显现生物，具有治愈和防御能力，可能参与水元素循环。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (326, 58, 'AI-32', '无相之火', '无相之火位于稻妻，与战争遗迹有关，是无相系列之一。作为火元素生物，攻击性强，能力包括火焰领域和形态变化，可能源于魔神战争的能量残留。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (327, 58, 'AI-2550', '无相之草', '无相之草位于须弥雨林，保护森林的‘自然免疫系统’，是无相系列之一。作为草元素生物，具备治疗和领域能力，与森林生态紧密相关。', '高一', NULL, NULL, '2026-01-10 10:40:00', '2026-01-10 10:40:00');
INSERT INTO `biz_knowledge_point` VALUES (328, 58, 'AI-9101', '深渊法师', '深渊法师是坎瑞亚平民的诅咒形态，智力残留约30%，保留语言能力和基本情感。元素护盾机制来自面具（深渊教团炼金术赋予），社会结构服从深渊使徒，可操控丘丘人。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (329, 58, 'AI-4077', '深渊咏者', '深渊咏者是坎瑞亚中级军官的诅咒形态，智力残留约70%，有完整语言能力和战术规划，记得部分前世记忆。类型包括渊火、渊冰和紫电咏者，痛苦源于保留记忆但无法解除诅咒。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (330, 58, 'AI-3796', '深渊使徒', '深渊使徒是坎瑞亚王室护卫或高级军官的诅咒形态，智力残留90%以上，有完整人格和记忆，可独立思考。例如激流使徒‘恩乔’，进行过‘倒吊者’仪式，对旅行者有复杂情感。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (331, 58, 'AI-918', '遗迹守卫', '遗迹守卫是坎瑞亚黄金时代制造的机械，原始用途为农业耕作，魔神战争末期武装化对抗七神。技术规格包括高度约3米、永动机关动力源、火箭炮等武器，弱点为核心（眼部）。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (332, 58, 'AI-4994', '遗迹猎者', '遗迹猎者是遗迹守卫的空中型号，具备飞行能力和更强AI（会躲避攻击），装备锁定导弹系统。破损率高，飞行系统复杂易损坏，分布于提瓦特各地。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (333, 58, 'AI-8559', '遗迹重机', '遗迹重机是坎瑞亚的攻城兵器，体型为遗迹守卫的3倍，破坏力可摧毁城墙，弱点是双腿核心。历史战绩包括参与坎瑞亚对枫丹的战争，在层岩巨渊对抗璃月仙人时全军覆没。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (334, 58, 'AI-4973', '遗迹巨蛇/遗迹龙兽', '遗迹巨蛇或遗迹龙兽是坎瑞亚模拟古龙的最高技术成就，数量极少，制造难度高。能力包括元素抗性极高、钻地或飞行、自我修复（低效），现存于层岩巨渊地下和须弥沙漠深处。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (335, 58, 'AI-976', '丘丘人', '丘丘人是坎瑞亚平民的诅咒形态，智力退化严重，仅剩本能，社会结构为部落制（萨满为首领）。文化残留包括舞蹈、图腾崇拜和语言碎片，生存方式为采集、狩猎，艾拉语言学研究认为他们是失去家园的难民。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (336, 58, 'AI-3771', '盗宝团', '盗宝团是人类犯罪组织，成员包括各国逃犯、破产冒险家和战争遗孤，组织结构松散联盟。装备有炼金药剂、简陋武器和陷阱，目标为盗取古迹财宝、抢劫商队和绑架勒索，冒险家协会视其为合法打击目标。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (337, 58, 'AI-4010', '愚人众先遣队', '愚人众先遣队是至冬国军队，装备包括邪眼（人造神之眼）、军用武器和元素战甲。兵种有游击兵、重卫和债务处理人，任务为收集神之心、建立至冬据点和情报搜集。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (338, 58, 'AI-8460', '创世神话时期', '提瓦特远古纪元的创世神话，包括天与地分离（原初之人分裂为天理维系者）、七龙降世统治世界，以及第一次大战（天理 vs 龙王，第一根天空之钉降下），导致龙族退位。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (339, 58, 'AI-4350', '第二王座之战', '发生在距今数千年的战争，参战者为天理 vs 外来者（疑似原初人类文明），结果外来者败北，提瓦特封闭。后果是天空成为虚假之空，星空是谎言，影响提瓦特整体结构。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (340, 58, 'AI-5015', '赤王追求禁忌知识', '发生在约公元前1800年，须弥沙漠的赤王阿赫玛尔追求禁忌知识，导致灾难。这一事件与花神牺牲相关，破坏了沙漠乐园，须弥文明受到重创，遗迹留于沙漠废墟。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');
INSERT INTO `biz_knowledge_point` VALUES (341, 58, 'AI-3480', '花神牺牲', '花神在约公元前1500年为拯救世界而牺牲，她是须弥雨林的神明之一，与大慈树王前身相关。牺牲后融入大慈树王，影响须弥的草元素体系和历史发展。', '高一', NULL, NULL, '2026-01-10 10:40:01', '2026-01-10 10:40:01');

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
-- Records of biz_knowledge_point_relation
-- ----------------------------
INSERT INTO `biz_knowledge_point_relation` VALUES (5, 12, 13, '2025-12-03 21:22:51');
INSERT INTO `biz_knowledge_point_relation` VALUES (6, 12, 14, '2025-12-03 21:22:54');
INSERT INTO `biz_knowledge_point_relation` VALUES (7, 12, 15, '2025-12-03 21:22:56');
INSERT INTO `biz_knowledge_point_relation` VALUES (8, 12, 30, '2025-12-03 21:23:02');

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
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习活动日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_learning_activity
-- ----------------------------
INSERT INTO `biz_learning_activity` VALUES (1, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共7道题', '2025-11-07 14:07:44');
INSERT INTO `biz_learning_activity` VALUES (2, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共1道题', '2025-11-07 14:28:58');
INSERT INTO `biz_learning_activity` VALUES (3, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共5道题', '2025-11-07 15:02:17');
INSERT INTO `biz_learning_activity` VALUES (4, 1, 'EXAM', '参加了模拟考试《测试222》，得分：0/10', '2025-11-20 17:28:03');
INSERT INTO `biz_learning_activity` VALUES (5, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共2道题', '2025-11-27 13:50:28');
INSERT INTO `biz_learning_activity` VALUES (6, 1, 'EXAM', '参加了模拟考试《测试222》，得分：5/10', '2025-11-27 13:59:00');
INSERT INTO `biz_learning_activity` VALUES (7, 1, 'EXAM', '参加了模拟考试《测试222》，得分：5/10', '2025-11-27 14:23:20');
INSERT INTO `biz_learning_activity` VALUES (8, 1, 'EXAM', '参加了模拟考试《测试222》，得分：0/10', '2025-11-27 14:25:16');
INSERT INTO `biz_learning_activity` VALUES (9, 1, 'EXAM', '参加了模拟考试《测试222》，得分：0/10', '2025-11-27 14:25:38');
INSERT INTO `biz_learning_activity` VALUES (10, 1, 'EXAM', '参加了模拟考试《测试222》，得分：5/10', '2025-11-27 14:26:39');
INSERT INTO `biz_learning_activity` VALUES (11, 1, 'EXAM', '参加了模拟考试《测试222》，得分：5/10', '2025-11-27 15:09:51');
INSERT INTO `biz_learning_activity` VALUES (12, 1, 'EXAM', '参加了模拟考试《测试222》，得分：0/10', '2025-11-27 15:10:01');
INSERT INTO `biz_learning_activity` VALUES (13, 1, 'EXAM', '参加了模拟考试《测试1》，得分：2/20', '2025-11-27 16:25:40');
INSERT INTO `biz_learning_activity` VALUES (14, 1, 'EXAM', '参加了模拟考试《测试1》，得分：2/20', '2025-11-27 16:26:04');
INSERT INTO `biz_learning_activity` VALUES (15, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-27 17:36:01');
INSERT INTO `biz_learning_activity` VALUES (16, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-27 19:02:50');
INSERT INTO `biz_learning_activity` VALUES (17, 3, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-27 19:04:30');
INSERT INTO `biz_learning_activity` VALUES (18, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-27 19:25:40');
INSERT INTO `biz_learning_activity` VALUES (19, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-27 19:43:14');
INSERT INTO `biz_learning_activity` VALUES (20, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共10道题', '2025-11-27 21:37:57');
INSERT INTO `biz_learning_activity` VALUES (21, 1, 'TEACHER_REWARD', '教师手动奖励积分: 1000，原因：123', '2025-11-27 22:14:13');
INSERT INTO `biz_learning_activity` VALUES (22, 1, 'TEACHER_REWARD', '教师手动奖励积分: 1000，原因：23423', '2025-11-27 22:14:24');
INSERT INTO `biz_learning_activity` VALUES (23, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-28 16:47:45');
INSERT INTO `biz_learning_activity` VALUES (24, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-28 16:48:44');
INSERT INTO `biz_learning_activity` VALUES (25, 1, 'SIGN_IN', '每日签到 (连续 1 天)', '2025-11-29 21:38:38');
INSERT INTO `biz_learning_activity` VALUES (26, 3, 'SIGN_IN', '每日签到 (连续 1 天)', '2025-11-29 21:45:17');
INSERT INTO `biz_learning_activity` VALUES (27, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-29 22:07:47');
INSERT INTO `biz_learning_activity` VALUES (28, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-11-29 22:09:13');
INSERT INTO `biz_learning_activity` VALUES (29, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2025-12-02 14:51:10');
INSERT INTO `biz_learning_activity` VALUES (30, 1, 'EXAM', '参加了模拟考试《测试222》，得分：5/10', '2025-12-02 14:52:23');
INSERT INTO `biz_learning_activity` VALUES (31, 1, 'EXAM', '参加了模拟考试《测试A》，得分：2/20', '2025-12-02 14:55:36');
INSERT INTO `biz_learning_activity` VALUES (32, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共10道题', '2025-12-02 15:07:42');
INSERT INTO `biz_learning_activity` VALUES (33, 1, 'EXAM', '参加了模拟考试《测试A》，得分：0/20', '2025-12-02 15:32:07');
INSERT INTO `biz_learning_activity` VALUES (34, 1, 'EXAM', '参加了模拟考试《测试A》，得分：0/20', '2025-12-02 15:41:15');
INSERT INTO `biz_learning_activity` VALUES (35, 1, 'EXAM', '参加了模拟考试《测试A》，得分：0/20', '2025-12-02 15:44:03');
INSERT INTO `biz_learning_activity` VALUES (36, 1, 'EXAM', '参加了模拟考试《测试A》，得分：0/20', '2025-12-02 15:49:53');
INSERT INTO `biz_learning_activity` VALUES (37, 1, 'EXAM', '参加了模拟考试《测试A》，得分：2/20', '2025-12-02 17:18:11');
INSERT INTO `biz_learning_activity` VALUES (38, 1, 'EXAM', '参加了模拟考试《测试A》，得分：2/20', '2025-12-02 17:18:12');
INSERT INTO `biz_learning_activity` VALUES (39, 1, 'EXAM', '参加了模拟考试《测试A》，得分：2/20', '2025-12-02 17:22:17');
INSERT INTO `biz_learning_activity` VALUES (40, 1, 'EXAM', '参加了模拟考试《测试A》，得分：6/20', '2025-12-02 17:24:21');
INSERT INTO `biz_learning_activity` VALUES (41, 1, 'SIGN_IN', '每日签到 (连续 1 天)', '2025-12-02 18:36:20');
INSERT INTO `biz_learning_activity` VALUES (42, 1, 'EXAM', '参加了模拟考试《崩坏》，得分：8/20', '2025-12-02 20:05:23');
INSERT INTO `biz_learning_activity` VALUES (43, 1, 'EXAM', '参加了模拟考试《崩坏》，得分：4/20', '2025-12-03 14:58:41');
INSERT INTO `biz_learning_activity` VALUES (44, 1, 'EXAM', '参加了模拟考试《崩坏》，得分：6/20', '2025-12-03 18:00:57');
INSERT INTO `biz_learning_activity` VALUES (45, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共6道题', '2025-12-03 18:39:24');
INSERT INTO `biz_learning_activity` VALUES (46, 1, 'EXAM', '参加了模拟考试《原神历史》，得分：8/120', '2025-12-03 21:27:13');
INSERT INTO `biz_learning_activity` VALUES (47, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共10道题', '2025-12-03 21:28:16');
INSERT INTO `biz_learning_activity` VALUES (48, 1, 'SIGN_IN', '每日签到 (连续 1 天)', '2025-12-04 16:44:46');
INSERT INTO `biz_learning_activity` VALUES (49, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共10道题', '2025-12-04 18:11:37');
INSERT INTO `biz_learning_activity` VALUES (50, 1, 'EXAM', '参加了模拟考试《崩坏》，得分：4/20', '2025-12-04 18:12:33');
INSERT INTO `biz_learning_activity` VALUES (51, 1, 'ACHIEVEMENT', '解锁成就：【初出茅庐】，获得 20 积分！', '2025-12-04 18:12:33');
INSERT INTO `biz_learning_activity` VALUES (52, 1, 'ACHIEVEMENT', '解锁成就：【题海战术】，获得 100 积分！', '2025-12-04 18:12:33');
INSERT INTO `biz_learning_activity` VALUES (53, 3, 'SIGN_IN', '每日签到 (连续 1 天)', '2025-12-05 12:39:51');
INSERT INTO `biz_learning_activity` VALUES (54, 1, 'SIGN_IN', '每日签到 (连续 2 天)', '2025-12-05 12:39:55');
INSERT INTO `biz_learning_activity` VALUES (55, 3, 'TEACHER_REWARD', '教师手动奖励积分: 1000，原因：1', '2025-12-05 15:07:49');
INSERT INTO `biz_learning_activity` VALUES (56, 1, 'TEACHER_REWARD', '教师手动奖励积分: 1000，原因：1', '2025-12-05 15:23:17');
INSERT INTO `biz_learning_activity` VALUES (57, 1, 'TEACHER_REWARD', '教师手动奖励积分: 1000', '2025-12-05 15:23:33');
INSERT INTO `biz_learning_activity` VALUES (58, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共3道题', '2025-12-05 18:53:42');
INSERT INTO `biz_learning_activity` VALUES (59, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共7道题', '2025-12-05 20:51:22');
INSERT INTO `biz_learning_activity` VALUES (60, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共7道题，答对3题', '2025-12-05 21:10:00');
INSERT INTO `biz_learning_activity` VALUES (61, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共6道题，答对4题', '2025-12-05 21:10:56');
INSERT INTO `biz_learning_activity` VALUES (62, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共2道题，答对1题', '2025-12-05 22:11:37');
INSERT INTO `biz_learning_activity` VALUES (63, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共10道题，答对1题', '2025-12-05 22:11:59');
INSERT INTO `biz_learning_activity` VALUES (64, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共1道题，答对1题', '2025-12-05 22:12:56');
INSERT INTO `biz_learning_activity` VALUES (65, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共1道题，答对1题', '2025-12-05 22:16:28');
INSERT INTO `biz_learning_activity` VALUES (66, 1, 'SIGN_IN', '每日签到 (连续 1 天)', '2026-01-07 08:50:13');
INSERT INTO `biz_learning_activity` VALUES (67, 1, 'EXAM', '参加了模拟考试《测试图片》，得分：0/100', '2026-01-07 15:07:32');
INSERT INTO `biz_learning_activity` VALUES (68, 1, 'EXAM', '参加了模拟考试《原神考据》，得分：18/98', '2026-01-09 20:40:15');
INSERT INTO `biz_learning_activity` VALUES (69, 1, 'EXAM', '参加了模拟考试《原神考据》，得分：0/98', '2026-01-09 20:50:03');
INSERT INTO `biz_learning_activity` VALUES (70, 1, 'EXAM', '参加了模拟考试《原神考据》，得分：10/98', '2026-01-09 21:43:56');
INSERT INTO `biz_learning_activity` VALUES (71, 1, 'SIGN_IN', '每日签到 (连续 1 天)', '2026-01-10 14:33:44');
INSERT INTO `biz_learning_activity` VALUES (72, 1, 'EXAM', '参加了模拟考试《原神考据》，得分：0/98', '2026-01-10 14:34:53');
INSERT INTO `biz_learning_activity` VALUES (73, 1, 'PRACTICE_SUBMIT', '完成了在线练习，共5道题，答对3题', '2026-01-11 13:02:33');

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
-- Records of biz_paper
-- ----------------------------
INSERT INTO `biz_paper` VALUES (2, '测试1', NULL, 1, '高一', '', 100, '2025-08-08 15:59:22', '2025-11-20 17:29:25', 2, 1);
INSERT INTO `biz_paper` VALUES (3, '测试', NULL, 1, '高一', '测试', 100, '2025-08-11 10:57:32', '2025-11-20 17:29:26', 2, 1);
INSERT INTO `biz_paper` VALUES (5, '52352354', NULL, 3, '高二', '', 100, '2025-08-13 14:58:36', '2025-11-20 17:29:28', 2, 1);
INSERT INTO `biz_paper` VALUES (6, '测试222', NULL, 3, '高一', '12341234', 100, '2025-11-20 17:19:26', '2025-11-20 17:19:35', 1, 1);
INSERT INTO `biz_paper` VALUES (7, '测试1', NULL, 16, '高一', '', 20, '2025-11-27 16:24:49', '2025-11-27 16:52:08', 1, 1);
INSERT INTO `biz_paper` VALUES (9, '测试A', NULL, 16, '高一', '', 20, '2025-11-27 16:54:20', '2025-11-27 17:13:13', 1, 1);
INSERT INTO `biz_paper` VALUES (10, '测试图片', NULL, 3, '高一', '', 100, '2025-11-27 17:14:30', '2025-11-27 17:14:30', 2, 1);
INSERT INTO `biz_paper` VALUES (11, '崩坏', NULL, 18, '高一', '', 20, '2025-12-02 20:02:48', '2025-12-02 20:02:48', 1, 1);
INSERT INTO `biz_paper` VALUES (13, '历史试卷', NULL, 18, '高三', '', 208, '2025-12-04 19:02:49', '2025-12-04 19:02:49', 1, 1);
INSERT INTO `biz_paper` VALUES (14, '历史测试1', NULL, 18, '高三', '', 108, '2025-12-04 19:53:17', '2025-12-04 19:53:17', 1, 1);
INSERT INTO `biz_paper` VALUES (15, '测试12341324', NULL, 18, '高三', '', 134, '2026-01-05 20:17:06', '2026-01-05 20:17:38', 1, 1);
INSERT INTO `biz_paper` VALUES (16, '原神考据', 'AI-1767961837344', 82, '高三', 'AI生成试卷 - 共30道题，总分100分', 100, '2026-01-09 20:30:37', '2026-01-09 20:31:31', 1, 1);

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
-- Records of biz_paper_group
-- ----------------------------
INSERT INTO `biz_paper_group` VALUES (1, 6, '新分组 1', 0);
INSERT INTO `biz_paper_group` VALUES (2, 7, '一、单选题', 0);
INSERT INTO `biz_paper_group` VALUES (3, 7, '五、主观题', 1);
INSERT INTO `biz_paper_group` VALUES (4, 8, '一、单选题', 0);
INSERT INTO `biz_paper_group` VALUES (5, 8, '五、主观题', 1);
INSERT INTO `biz_paper_group` VALUES (6, 9, '一、单选题', 0);
INSERT INTO `biz_paper_group` VALUES (7, 9, '五、主观题', 1);
INSERT INTO `biz_paper_group` VALUES (11, 11, '一、单选题', 0);
INSERT INTO `biz_paper_group` VALUES (12, 11, '三、判断题', 1);
INSERT INTO `biz_paper_group` VALUES (13, 11, '四、填空题', 2);
INSERT INTO `biz_paper_group` VALUES (14, 12, '一、单选题', 0);
INSERT INTO `biz_paper_group` VALUES (15, 12, '三、判断题', 1);
INSERT INTO `biz_paper_group` VALUES (16, 12, '四、填空题', 2);
INSERT INTO `biz_paper_group` VALUES (17, 12, '五、主观题', 3);
INSERT INTO `biz_paper_group` VALUES (18, 13, '一、单选题', 0);
INSERT INTO `biz_paper_group` VALUES (19, 13, '二、多选题', 1);
INSERT INTO `biz_paper_group` VALUES (20, 13, '三、判断题', 2);
INSERT INTO `biz_paper_group` VALUES (21, 13, '四、填空题', 3);
INSERT INTO `biz_paper_group` VALUES (22, 13, '五、主观题', 4);
INSERT INTO `biz_paper_group` VALUES (23, 14, '一、单选题', 0);
INSERT INTO `biz_paper_group` VALUES (24, 14, '三、判断题', 1);
INSERT INTO `biz_paper_group` VALUES (25, 14, '四、填空题', 2);
INSERT INTO `biz_paper_group` VALUES (26, 14, '五、主观题', 3);
INSERT INTO `biz_paper_group` VALUES (27, 15, '一、单选题', 0);
INSERT INTO `biz_paper_group` VALUES (28, 15, '二、多选题', 1);
INSERT INTO `biz_paper_group` VALUES (29, 15, '三、判断题', 2);
INSERT INTO `biz_paper_group` VALUES (30, 15, '四、填空题', 3);
INSERT INTO `biz_paper_group` VALUES (31, 15, '五、主观题', 4);
INSERT INTO `biz_paper_group` VALUES (32, 16, '试题', 0);

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
-- Records of biz_paper_image
-- ----------------------------
INSERT INTO `biz_paper_image` VALUES (1, 2, '/api/v1/files/8f73b276-12aa-4545-ab67-3a1d7c5d2109.png', 0);
INSERT INTO `biz_paper_image` VALUES (3, 2, '/api/v1/files/619bafbb-504c-458c-ae7f-fbb89196deee.png', 1);
INSERT INTO `biz_paper_image` VALUES (4, 2, '/api/v1/files/20eb341d-c95c-47e9-a2d9-63cf3315aa51.png', 2);
INSERT INTO `biz_paper_image` VALUES (5, 2, '/api/v1/files/3491537c-b8a3-4263-be0d-5f7a5c2851f0.png', 3);
INSERT INTO `biz_paper_image` VALUES (6, 2, '/api/v1/files/590321c4-217a-45a2-940d-b38cd7bd606a.png', 4);
INSERT INTO `biz_paper_image` VALUES (7, 2, '/api/v1/files/a37267ce-d0d1-46cd-a9f4-f5c22c2e4e60.png', 5);
INSERT INTO `biz_paper_image` VALUES (8, 2, '/api/v1/files/c0176ab4-17da-4f4d-9cd7-89e69b589a8b.png', 6);
INSERT INTO `biz_paper_image` VALUES (16, 3, '/api/v1/files/dd816991-b3f7-450e-8999-91d6308823d0.png', 0);
INSERT INTO `biz_paper_image` VALUES (17, 3, '/api/v1/files/043c6455-08e7-4dcf-bc67-4b5a5ed00f83.png', 1);
INSERT INTO `biz_paper_image` VALUES (18, 3, '/api/v1/files/49dd7093-7459-4302-9f7f-e068c66d705d.png', 2);
INSERT INTO `biz_paper_image` VALUES (19, 5, '/api/v1/files/f07d3969-d80e-4afa-bf77-7403aa78c9b1.jpg', 0);
INSERT INTO `biz_paper_image` VALUES (20, 5, '/api/v1/files/93af86e9-ee3e-4609-a20c-79c4f3131cd2.png', 1);
INSERT INTO `biz_paper_image` VALUES (21, 5, '/api/v1/files/1ca502b7-a48b-4080-a3b7-040a8c0c57f9.png', 2);
INSERT INTO `biz_paper_image` VALUES (63, 10, '/api/v1/files/b363137a-4005-4c4a-8d31-eea5503ac396.jpg', 0);
INSERT INTO `biz_paper_image` VALUES (64, 10, '/api/v1/files/c999d642-7042-4cdc-92cb-89c030334921.jpg', 1);
INSERT INTO `biz_paper_image` VALUES (65, 10, '/api/v1/files/2f5e7dae-04bf-4c3d-b2ab-d5e35f32c76a.jpg', 2);
INSERT INTO `biz_paper_image` VALUES (66, 10, '/api/v1/files/9977b705-58bb-4a9c-8cde-ceeedcfeb7cb.jpg', 3);
INSERT INTO `biz_paper_image` VALUES (67, 10, '/api/v1/files/b5adf23a-1273-42bb-9da8-1436cdae4dd2.jpg', 4);
INSERT INTO `biz_paper_image` VALUES (68, 10, '/api/v1/files/ac460e6a-80c3-4d73-93e3-e342e4f21100.jpg', 5);
INSERT INTO `biz_paper_image` VALUES (69, 10, '/api/v1/files/6dc756e1-3235-47dc-8384-cd5c95980afb.jpg', 6);
INSERT INTO `biz_paper_image` VALUES (70, 10, '/api/v1/files/f2a7bf4c-0f9d-4807-ac77-dcb36e117d54.jpg', 7);
INSERT INTO `biz_paper_image` VALUES (71, 10, '/api/v1/files/7d650f91-1b42-4efb-90ad-537226e10dd8.jpg', 8);
INSERT INTO `biz_paper_image` VALUES (72, 10, '/api/v1/files/58555065-d173-43bd-b105-7614c5e94019.jpg', 9);
INSERT INTO `biz_paper_image` VALUES (73, 10, '/api/v1/files/9a1ad614-f9d5-412d-9ec5-0eb6d3a55b15.jpg', 10);
INSERT INTO `biz_paper_image` VALUES (74, 10, '/api/v1/files/6b8d10a2-e63d-46ad-b088-47401eb080c8.jpg', 11);
INSERT INTO `biz_paper_image` VALUES (75, 10, '/api/v1/files/cd265a6a-3ee4-4e23-ad04-cc549e77afb7.jpg', 12);
INSERT INTO `biz_paper_image` VALUES (76, 10, '/api/v1/files/9b2b1363-b400-4ed2-8427-8da33c3c7491.jpg', 13);
INSERT INTO `biz_paper_image` VALUES (77, 10, '/api/v1/files/aa5d8c32-5e58-4cb7-99e3-bbc42e8aa2fa.jpg', 14);
INSERT INTO `biz_paper_image` VALUES (78, 10, '/api/v1/files/76ded0d4-a333-41a5-ae3f-595126ba229a.jpg', 15);
INSERT INTO `biz_paper_image` VALUES (79, 10, '/api/v1/files/a72a6c83-dd2a-472b-af33-534dec8458a8.jpg', 16);
INSERT INTO `biz_paper_image` VALUES (80, 10, '/api/v1/files/f2d175db-4f12-42b6-b99f-a0e304624014.jpg', 17);
INSERT INTO `biz_paper_image` VALUES (81, 10, '/api/v1/files/87784ccf-8949-43a8-9cf1-8084f86093b6.jpg', 18);
INSERT INTO `biz_paper_image` VALUES (82, 10, '/api/v1/files/d39c38de-2499-47c5-a45e-1fd730627868.jpg', 19);
INSERT INTO `biz_paper_image` VALUES (83, 10, '/api/v1/files/660a9e19-fb75-48f5-89cf-18a25ac76bbb.jpg', 20);
INSERT INTO `biz_paper_image` VALUES (84, 10, '/api/v1/files/3f94cfa5-1971-4601-ba7a-d95d00960704.jpg', 21);
INSERT INTO `biz_paper_image` VALUES (85, 10, '/api/v1/files/ae73824f-33eb-4561-849a-85e2c91532f3.jpg', 22);
INSERT INTO `biz_paper_image` VALUES (86, 10, '/api/v1/files/b7c90388-4e9a-4546-bfc8-9fad976357a8.jpg', 23);
INSERT INTO `biz_paper_image` VALUES (87, 10, '/api/v1/files/7dcd6dd2-c30b-41b2-be81-85658638656e.jpg', 24);

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
-- Records of biz_paper_knowledge_point
-- ----------------------------
INSERT INTO `biz_paper_knowledge_point` VALUES (1, 16, 64, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (2, 16, 65, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (3, 16, 66, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (4, 16, 67, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (5, 16, 68, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (6, 16, 69, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (7, 16, 70, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (8, 16, 71, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (9, 16, 72, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (10, 16, 73, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (11, 16, 74, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (12, 16, 75, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (13, 16, 76, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (14, 16, 77, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (15, 16, 78, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (16, 16, 79, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (17, 16, 80, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (18, 16, 81, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (19, 16, 82, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (20, 16, 83, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (21, 16, 84, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (22, 16, 85, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (23, 16, 86, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (24, 16, 54, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (25, 16, 55, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (26, 16, 58, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (27, 16, 60, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (28, 16, 61, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (29, 16, 62, 30, 98, '2026-01-09 20:30:37');
INSERT INTO `biz_paper_knowledge_point` VALUES (30, 16, 63, 30, 98, '2026-01-09 20:30:37');

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
-- Records of biz_paper_question
-- ----------------------------
INSERT INTO `biz_paper_question` VALUES (7, 6, 17, 5, 0, 1);
INSERT INTO `biz_paper_question` VALUES (8, 6, 16, 5, 1, 1);
INSERT INTO `biz_paper_question` VALUES (9, 7, 63, 2, 0, 2);
INSERT INTO `biz_paper_question` VALUES (10, 7, 65, 2, 1, 2);
INSERT INTO `biz_paper_question` VALUES (11, 7, 66, 2, 2, 2);
INSERT INTO `biz_paper_question` VALUES (12, 7, 62, 2, 3, 2);
INSERT INTO `biz_paper_question` VALUES (13, 7, 58, 2, 4, 2);
INSERT INTO `biz_paper_question` VALUES (14, 7, 112, 10, 0, 3);
INSERT INTO `biz_paper_question` VALUES (15, 8, 64, 2, 0, 4);
INSERT INTO `biz_paper_question` VALUES (16, 8, 58, 2, 1, 4);
INSERT INTO `biz_paper_question` VALUES (17, 8, 65, 2, 2, 4);
INSERT INTO `biz_paper_question` VALUES (18, 8, 62, 2, 3, 4);
INSERT INTO `biz_paper_question` VALUES (19, 8, 67, 2, 4, 4);
INSERT INTO `biz_paper_question` VALUES (20, 8, 61, 2, 5, 4);
INSERT INTO `biz_paper_question` VALUES (21, 8, 59, 2, 6, 4);
INSERT INTO `biz_paper_question` VALUES (22, 8, 60, 2, 7, 4);
INSERT INTO `biz_paper_question` VALUES (23, 8, 63, 2, 8, 4);
INSERT INTO `biz_paper_question` VALUES (24, 8, 66, 2, 9, 4);
INSERT INTO `biz_paper_question` VALUES (25, 8, 112, 10, 0, 5);
INSERT INTO `biz_paper_question` VALUES (26, 9, 58, 2, 0, 6);
INSERT INTO `biz_paper_question` VALUES (27, 9, 66, 2, 1, 6);
INSERT INTO `biz_paper_question` VALUES (28, 9, 63, 2, 2, 6);
INSERT INTO `biz_paper_question` VALUES (29, 9, 64, 2, 3, 6);
INSERT INTO `biz_paper_question` VALUES (30, 9, 60, 2, 4, 6);
INSERT INTO `biz_paper_question` VALUES (31, 9, 112, 10, 0, 7);
INSERT INTO `biz_paper_question` VALUES (42, 11, 115, 2, 0, 11);
INSERT INTO `biz_paper_question` VALUES (43, 11, 114, 2, 1, 11);
INSERT INTO `biz_paper_question` VALUES (44, 11, 122, 2, 2, 11);
INSERT INTO `biz_paper_question` VALUES (45, 11, 116, 2, 3, 11);
INSERT INTO `biz_paper_question` VALUES (46, 11, 121, 2, 4, 11);
INSERT INTO `biz_paper_question` VALUES (47, 11, 119, 2, 0, 12);
INSERT INTO `biz_paper_question` VALUES (48, 11, 117, 2, 0, 13);
INSERT INTO `biz_paper_question` VALUES (49, 11, 118, 2, 1, 13);
INSERT INTO `biz_paper_question` VALUES (50, 11, 113, 2, 2, 13);
INSERT INTO `biz_paper_question` VALUES (51, 11, 120, 2, 3, 13);
INSERT INTO `biz_paper_question` VALUES (52, 12, 114, 2, 0, 14);
INSERT INTO `biz_paper_question` VALUES (53, 12, 115, 2, 1, 14);
INSERT INTO `biz_paper_question` VALUES (54, 12, 122, 2, 2, 14);
INSERT INTO `biz_paper_question` VALUES (55, 12, 121, 2, 3, 14);
INSERT INTO `biz_paper_question` VALUES (56, 12, 116, 2, 4, 14);
INSERT INTO `biz_paper_question` VALUES (57, 12, 119, 2, 0, 15);
INSERT INTO `biz_paper_question` VALUES (58, 12, 117, 2, 0, 16);
INSERT INTO `biz_paper_question` VALUES (59, 12, 113, 2, 1, 16);
INSERT INTO `biz_paper_question` VALUES (60, 12, 118, 2, 2, 16);
INSERT INTO `biz_paper_question` VALUES (61, 12, 120, 2, 3, 16);
INSERT INTO `biz_paper_question` VALUES (62, 12, 126, 10, 0, 17);
INSERT INTO `biz_paper_question` VALUES (63, 12, 128, 10, 1, 17);
INSERT INTO `biz_paper_question` VALUES (64, 12, 131, 10, 2, 17);
INSERT INTO `biz_paper_question` VALUES (65, 12, 129, 10, 3, 17);
INSERT INTO `biz_paper_question` VALUES (66, 12, 123, 10, 4, 17);
INSERT INTO `biz_paper_question` VALUES (67, 12, 130, 10, 5, 17);
INSERT INTO `biz_paper_question` VALUES (68, 12, 124, 10, 6, 17);
INSERT INTO `biz_paper_question` VALUES (69, 12, 127, 10, 7, 17);
INSERT INTO `biz_paper_question` VALUES (70, 12, 125, 10, 8, 17);
INSERT INTO `biz_paper_question` VALUES (71, 12, 132, 10, 9, 17);
INSERT INTO `biz_paper_question` VALUES (72, 13, 115, 2, 0, 18);
INSERT INTO `biz_paper_question` VALUES (73, 13, 145, 2, 1, 18);
INSERT INTO `biz_paper_question` VALUES (74, 13, 122, 2, 2, 18);
INSERT INTO `biz_paper_question` VALUES (75, 13, 121, 2, 3, 18);
INSERT INTO `biz_paper_question` VALUES (76, 13, 133, 2, 4, 18);
INSERT INTO `biz_paper_question` VALUES (77, 13, 135, 2, 5, 18);
INSERT INTO `biz_paper_question` VALUES (78, 13, 141, 2, 6, 18);
INSERT INTO `biz_paper_question` VALUES (79, 13, 152, 2, 7, 18);
INSERT INTO `biz_paper_question` VALUES (80, 13, 142, 2, 8, 18);
INSERT INTO `biz_paper_question` VALUES (81, 13, 138, 2, 9, 18);
INSERT INTO `biz_paper_question` VALUES (82, 13, 134, 2, 10, 18);
INSERT INTO `biz_paper_question` VALUES (83, 13, 137, 2, 11, 18);
INSERT INTO `biz_paper_question` VALUES (84, 13, 139, 2, 12, 18);
INSERT INTO `biz_paper_question` VALUES (85, 13, 116, 2, 13, 18);
INSERT INTO `biz_paper_question` VALUES (86, 13, 114, 2, 14, 18);
INSERT INTO `biz_paper_question` VALUES (87, 13, 148, 2, 15, 18);
INSERT INTO `biz_paper_question` VALUES (88, 13, 136, 2, 16, 18);
INSERT INTO `biz_paper_question` VALUES (89, 13, 143, 2, 17, 18);
INSERT INTO `biz_paper_question` VALUES (90, 13, 140, 2, 18, 18);
INSERT INTO `biz_paper_question` VALUES (91, 13, 146, 2, 19, 18);
INSERT INTO `biz_paper_question` VALUES (92, 13, 150, 2, 20, 18);
INSERT INTO `biz_paper_question` VALUES (93, 13, 151, 4, 0, 19);
INSERT INTO `biz_paper_question` VALUES (94, 13, 149, 4, 1, 19);
INSERT INTO `biz_paper_question` VALUES (95, 13, 147, 4, 2, 19);
INSERT INTO `biz_paper_question` VALUES (96, 13, 144, 4, 3, 19);
INSERT INTO `biz_paper_question` VALUES (97, 13, 166, 2, 0, 20);
INSERT INTO `biz_paper_question` VALUES (98, 13, 172, 2, 1, 20);
INSERT INTO `biz_paper_question` VALUES (99, 13, 119, 2, 2, 20);
INSERT INTO `biz_paper_question` VALUES (100, 13, 165, 2, 3, 20);
INSERT INTO `biz_paper_question` VALUES (101, 13, 168, 2, 4, 20);
INSERT INTO `biz_paper_question` VALUES (102, 13, 164, 2, 5, 20);
INSERT INTO `biz_paper_question` VALUES (103, 13, 163, 2, 6, 20);
INSERT INTO `biz_paper_question` VALUES (104, 13, 171, 2, 7, 20);
INSERT INTO `biz_paper_question` VALUES (105, 13, 170, 2, 8, 20);
INSERT INTO `biz_paper_question` VALUES (106, 13, 169, 2, 9, 20);
INSERT INTO `biz_paper_question` VALUES (107, 13, 167, 2, 10, 20);
INSERT INTO `biz_paper_question` VALUES (108, 13, 157, 2, 0, 21);
INSERT INTO `biz_paper_question` VALUES (109, 13, 118, 2, 1, 21);
INSERT INTO `biz_paper_question` VALUES (110, 13, 154, 2, 2, 21);
INSERT INTO `biz_paper_question` VALUES (111, 13, 156, 2, 3, 21);
INSERT INTO `biz_paper_question` VALUES (112, 13, 162, 2, 4, 21);
INSERT INTO `biz_paper_question` VALUES (113, 13, 158, 2, 5, 21);
INSERT INTO `biz_paper_question` VALUES (114, 13, 160, 2, 6, 21);
INSERT INTO `biz_paper_question` VALUES (115, 13, 153, 2, 7, 21);
INSERT INTO `biz_paper_question` VALUES (116, 13, 161, 2, 8, 21);
INSERT INTO `biz_paper_question` VALUES (117, 13, 120, 2, 9, 21);
INSERT INTO `biz_paper_question` VALUES (118, 13, 155, 2, 10, 21);
INSERT INTO `biz_paper_question` VALUES (119, 13, 113, 2, 11, 21);
INSERT INTO `biz_paper_question` VALUES (120, 13, 117, 2, 12, 21);
INSERT INTO `biz_paper_question` VALUES (121, 13, 159, 2, 13, 21);
INSERT INTO `biz_paper_question` VALUES (122, 13, 131, 10, 0, 22);
INSERT INTO `biz_paper_question` VALUES (123, 13, 173, 10, 1, 22);
INSERT INTO `biz_paper_question` VALUES (124, 13, 112, 10, 2, 22);
INSERT INTO `biz_paper_question` VALUES (125, 13, 124, 10, 3, 22);
INSERT INTO `biz_paper_question` VALUES (126, 13, 125, 10, 4, 22);
INSERT INTO `biz_paper_question` VALUES (127, 13, 129, 10, 5, 22);
INSERT INTO `biz_paper_question` VALUES (128, 13, 175, 10, 6, 22);
INSERT INTO `biz_paper_question` VALUES (129, 13, 126, 10, 7, 22);
INSERT INTO `biz_paper_question` VALUES (130, 13, 181, 10, 8, 22);
INSERT INTO `biz_paper_question` VALUES (131, 13, 180, 10, 9, 22);
INSERT INTO `biz_paper_question` VALUES (132, 14, 114, 2, 0, 23);
INSERT INTO `biz_paper_question` VALUES (133, 14, 115, 2, 1, 23);
INSERT INTO `biz_paper_question` VALUES (134, 14, 121, 2, 2, 23);
INSERT INTO `biz_paper_question` VALUES (135, 14, 116, 2, 3, 23);
INSERT INTO `biz_paper_question` VALUES (136, 14, 136, 2, 4, 23);
INSERT INTO `biz_paper_question` VALUES (137, 14, 145, 2, 5, 23);
INSERT INTO `biz_paper_question` VALUES (138, 14, 122, 2, 6, 23);
INSERT INTO `biz_paper_question` VALUES (139, 14, 137, 2, 7, 23);
INSERT INTO `biz_paper_question` VALUES (140, 14, 133, 2, 8, 23);
INSERT INTO `biz_paper_question` VALUES (141, 14, 146, 2, 9, 23);
INSERT INTO `biz_paper_question` VALUES (142, 14, 143, 2, 10, 23);
INSERT INTO `biz_paper_question` VALUES (143, 14, 152, 2, 11, 23);
INSERT INTO `biz_paper_question` VALUES (144, 14, 134, 2, 12, 23);
INSERT INTO `biz_paper_question` VALUES (145, 14, 142, 2, 13, 23);
INSERT INTO `biz_paper_question` VALUES (146, 14, 140, 2, 14, 23);
INSERT INTO `biz_paper_question` VALUES (147, 14, 168, 2, 0, 24);
INSERT INTO `biz_paper_question` VALUES (148, 14, 171, 2, 1, 24);
INSERT INTO `biz_paper_question` VALUES (149, 14, 164, 2, 2, 24);
INSERT INTO `biz_paper_question` VALUES (150, 14, 170, 2, 3, 24);
INSERT INTO `biz_paper_question` VALUES (151, 14, 166, 2, 4, 24);
INSERT INTO `biz_paper_question` VALUES (152, 14, 113, 2, 0, 25);
INSERT INTO `biz_paper_question` VALUES (153, 14, 118, 2, 1, 25);
INSERT INTO `biz_paper_question` VALUES (154, 14, 162, 2, 2, 25);
INSERT INTO `biz_paper_question` VALUES (155, 14, 158, 2, 3, 25);
INSERT INTO `biz_paper_question` VALUES (156, 14, 125, 10, 0, 26);
INSERT INTO `biz_paper_question` VALUES (157, 14, 132, 10, 1, 26);
INSERT INTO `biz_paper_question` VALUES (158, 14, 131, 10, 2, 26);
INSERT INTO `biz_paper_question` VALUES (159, 14, 176, 10, 3, 26);
INSERT INTO `biz_paper_question` VALUES (160, 14, 126, 10, 4, 26);
INSERT INTO `biz_paper_question` VALUES (161, 14, 181, 10, 5, 26);
INSERT INTO `biz_paper_question` VALUES (162, 15, 148, 2, 0, 27);
INSERT INTO `biz_paper_question` VALUES (163, 15, 135, 2, 1, 27);
INSERT INTO `biz_paper_question` VALUES (164, 15, 139, 2, 2, 27);
INSERT INTO `biz_paper_question` VALUES (165, 15, 142, 2, 3, 27);
INSERT INTO `biz_paper_question` VALUES (166, 15, 115, 2, 4, 27);
INSERT INTO `biz_paper_question` VALUES (167, 15, 134, 2, 5, 27);
INSERT INTO `biz_paper_question` VALUES (168, 15, 138, 2, 6, 27);
INSERT INTO `biz_paper_question` VALUES (169, 15, 145, 2, 7, 27);
INSERT INTO `biz_paper_question` VALUES (170, 15, 136, 2, 8, 27);
INSERT INTO `biz_paper_question` VALUES (171, 15, 140, 2, 9, 27);
INSERT INTO `biz_paper_question` VALUES (172, 15, 149, 4, 0, 28);
INSERT INTO `biz_paper_question` VALUES (173, 15, 147, 4, 1, 28);
INSERT INTO `biz_paper_question` VALUES (174, 15, 144, 4, 2, 28);
INSERT INTO `biz_paper_question` VALUES (175, 15, 151, 4, 3, 28);
INSERT INTO `biz_paper_question` VALUES (176, 15, 170, 2, 0, 29);
INSERT INTO `biz_paper_question` VALUES (177, 15, 172, 2, 1, 29);
INSERT INTO `biz_paper_question` VALUES (178, 15, 167, 2, 2, 29);
INSERT INTO `biz_paper_question` VALUES (179, 15, 119, 2, 3, 29);
INSERT INTO `biz_paper_question` VALUES (180, 15, 165, 2, 4, 29);
INSERT INTO `biz_paper_question` VALUES (181, 15, 113, 2, 0, 30);
INSERT INTO `biz_paper_question` VALUES (182, 15, 153, 2, 1, 30);
INSERT INTO `biz_paper_question` VALUES (183, 15, 117, 2, 2, 30);
INSERT INTO `biz_paper_question` VALUES (184, 15, 157, 2, 3, 30);
INSERT INTO `biz_paper_question` VALUES (185, 15, 180, 10, 0, 31);
INSERT INTO `biz_paper_question` VALUES (186, 15, 178, 10, 1, 31);
INSERT INTO `biz_paper_question` VALUES (187, 15, 176, 10, 2, 31);
INSERT INTO `biz_paper_question` VALUES (188, 15, 128, 10, 3, 31);
INSERT INTO `biz_paper_question` VALUES (189, 15, 123, 10, 4, 31);
INSERT INTO `biz_paper_question` VALUES (190, 15, 130, 10, 5, 31);
INSERT INTO `biz_paper_question` VALUES (191, 15, 177, 10, 6, 31);
INSERT INTO `biz_paper_question` VALUES (192, 15, 129, 10, 7, 31);
INSERT INTO `biz_paper_question` VALUES (193, 16, 2254, 2, 0, 32);
INSERT INTO `biz_paper_question` VALUES (194, 16, 2255, 2, 1, 32);
INSERT INTO `biz_paper_question` VALUES (195, 16, 2256, 2, 2, 32);
INSERT INTO `biz_paper_question` VALUES (196, 16, 2257, 2, 3, 32);
INSERT INTO `biz_paper_question` VALUES (197, 16, 2258, 2, 4, 32);
INSERT INTO `biz_paper_question` VALUES (198, 16, 2259, 2, 5, 32);
INSERT INTO `biz_paper_question` VALUES (199, 16, 2260, 2, 6, 32);
INSERT INTO `biz_paper_question` VALUES (200, 16, 2261, 2, 7, 32);
INSERT INTO `biz_paper_question` VALUES (201, 16, 2262, 2, 8, 32);
INSERT INTO `biz_paper_question` VALUES (202, 16, 2263, 2, 9, 32);
INSERT INTO `biz_paper_question` VALUES (203, 16, 2264, 4, 10, 32);
INSERT INTO `biz_paper_question` VALUES (204, 16, 2265, 4, 11, 32);
INSERT INTO `biz_paper_question` VALUES (205, 16, 2266, 4, 12, 32);
INSERT INTO `biz_paper_question` VALUES (206, 16, 2267, 4, 13, 32);
INSERT INTO `biz_paper_question` VALUES (207, 16, 2268, 4, 14, 32);
INSERT INTO `biz_paper_question` VALUES (208, 16, 2269, 2, 15, 32);
INSERT INTO `biz_paper_question` VALUES (209, 16, 2270, 2, 16, 32);
INSERT INTO `biz_paper_question` VALUES (210, 16, 2271, 2, 17, 32);
INSERT INTO `biz_paper_question` VALUES (211, 16, 2272, 2, 18, 32);
INSERT INTO `biz_paper_question` VALUES (212, 16, 2273, 2, 19, 32);
INSERT INTO `biz_paper_question` VALUES (213, 16, 2274, 2, 20, 32);
INSERT INTO `biz_paper_question` VALUES (214, 16, 2275, 2, 21, 32);
INSERT INTO `biz_paper_question` VALUES (215, 16, 2276, 2, 22, 32);
INSERT INTO `biz_paper_question` VALUES (216, 16, 2277, 2, 23, 32);
INSERT INTO `biz_paper_question` VALUES (217, 16, 2278, 2, 24, 32);
INSERT INTO `biz_paper_question` VALUES (218, 16, 2279, 6, 25, 32);
INSERT INTO `biz_paper_question` VALUES (219, 16, 2280, 6, 26, 32);
INSERT INTO `biz_paper_question` VALUES (220, 16, 2281, 8, 27, 32);
INSERT INTO `biz_paper_question` VALUES (221, 16, 2282, 8, 28, 32);
INSERT INTO `biz_paper_question` VALUES (222, 16, 2283, 10, 29, 32);

-- ----------------------------
-- Table structure for biz_portal_visit
-- ----------------------------
DROP TABLE IF EXISTS `biz_portal_visit`;
CREATE TABLE `biz_portal_visit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统ID',
  `system_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统名称',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户类型: admin/teacher/student',
  `visit_time` datetime NOT NULL COMMENT '访问时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_system_id`(`system_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_visit_time`(`visit_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Portal访问记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_portal_visit
-- ----------------------------
INSERT INTO `biz_portal_visit` VALUES (1, 'admin', '试题管理系统', NULL, 'student', '2026-01-10 11:58:55', '2026-01-10 19:59:14');
INSERT INTO `biz_portal_visit` VALUES (2, 'student', '在线学习系统', NULL, 'student', '2026-01-10 12:02:32', '2026-01-10 20:02:32');
INSERT INTO `biz_portal_visit` VALUES (3, 'student', '在线学习系统', NULL, NULL, '2026-01-10 12:03:22', '2026-01-10 20:03:23');
INSERT INTO `biz_portal_visit` VALUES (4, 'student', '在线学习系统', NULL, 'student', '2026-01-10 12:12:03', '2026-01-10 20:12:04');
INSERT INTO `biz_portal_visit` VALUES (5, 'student', '在线学习系统', 1, 'student', '2026-01-10 12:12:04', '2026-01-10 20:12:05');
INSERT INTO `biz_portal_visit` VALUES (6, 'student', '在线学习系统', NULL, 'student', '2026-01-10 14:30:07', '2026-01-10 22:30:08');
INSERT INTO `biz_portal_visit` VALUES (7, 'student', '在线学习系统', NULL, 'student', '2026-01-11 01:29:59', '2026-01-11 09:29:59');
INSERT INTO `biz_portal_visit` VALUES (8, 'student', '在线学习系统', NULL, 'student', '2026-01-11 05:04:06', '2026-01-11 13:04:07');
INSERT INTO `biz_portal_visit` VALUES (9, 'student', '在线学习系统', NULL, 'student', '2026-01-11 07:16:40', '2026-01-11 15:16:40');
INSERT INTO `biz_portal_visit` VALUES (10, 'student', '在线学习系统', NULL, NULL, '2026-01-11 07:25:00', '2026-01-11 15:25:01');
INSERT INTO `biz_portal_visit` VALUES (11, 'admin', '试题管理系统', 1, 'admin', '2026-01-11 07:51:42', '2026-01-11 15:51:43');
INSERT INTO `biz_portal_visit` VALUES (12, 'student', '在线学习系统', 1, 'student', '2026-01-11 08:07:44', '2026-01-11 16:07:45');
INSERT INTO `biz_portal_visit` VALUES (13, 'student', '在线学习系统', 1, 'admin', '2026-01-11 08:13:46', '2026-01-11 16:13:46');
INSERT INTO `biz_portal_visit` VALUES (14, 'student', '在线学习系统', 1, 'student', '2026-01-11 08:14:16', '2026-01-11 16:14:16');
INSERT INTO `biz_portal_visit` VALUES (15, 'student', '在线学习系统', 1, 'admin', '2026-01-11 08:19:06', '2026-01-11 16:19:07');
INSERT INTO `biz_portal_visit` VALUES (16, 'student', '在线学习系统', NULL, 'student', '2026-01-11 08:20:21', '2026-01-11 16:20:22');
INSERT INTO `biz_portal_visit` VALUES (17, 'student', '在线学习系统', 1, 'student', '2026-01-11 08:20:50', '2026-01-11 16:20:51');
INSERT INTO `biz_portal_visit` VALUES (18, 'student', '在线学习系统', 1, 'student', '2026-01-11 08:26:16', '2026-01-11 16:26:17');
INSERT INTO `biz_portal_visit` VALUES (19, 'student', '在线学习系统', 1, 'student', '2026-01-11 08:32:17', '2026-01-11 16:32:17');
INSERT INTO `biz_portal_visit` VALUES (20, 'student', '在线学习系统', 1, 'student', '2026-01-11 10:50:52', '2026-01-11 18:50:53');
INSERT INTO `biz_portal_visit` VALUES (21, 'student', '在线学习系统', 1, 'student', '2026-01-11 10:59:51', '2026-01-11 18:59:51');
INSERT INTO `biz_portal_visit` VALUES (22, 'student', '在线学习系统', 1, 'admin', '2026-01-11 12:23:43', '2026-01-11 20:23:44');
INSERT INTO `biz_portal_visit` VALUES (23, 'student', '在线学习系统', 1, 'admin', '2026-01-11 12:29:01', '2026-01-11 20:29:01');
INSERT INTO `biz_portal_visit` VALUES (24, 'student', '在线学习系统', 1, 'admin', '2026-01-11 12:45:05', '2026-01-11 20:45:05');
INSERT INTO `biz_portal_visit` VALUES (25, 'student', '在线学习系统', 1, 'student', '2026-01-11 13:06:18', '2026-01-11 21:06:18');
INSERT INTO `biz_portal_visit` VALUES (26, 'student', '在线学习系统', 1, 'student', '2026-01-11 13:10:04', '2026-01-11 21:10:04');
INSERT INTO `biz_portal_visit` VALUES (27, 'student', '在线学习系统', 1, 'admin', '2026-01-11 13:28:47', '2026-01-11 21:28:48');
INSERT INTO `biz_portal_visit` VALUES (28, 'admin', '试题管理系统', 1, 'admin', '2026-01-11 14:32:12', '2026-01-11 22:32:12');

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
-- Records of biz_question
-- ----------------------------
INSERT INTO `biz_question` VALUES (2254, 82, '高三', 1, 1, '传说中，盐之魔神赫乌莉亚为保护子民而亡。目前关于她的传说，在下列哪个地区仍有流传？', NULL, '[{\"key\": \"A\", \"value\": \"璃月-轻策庄\"}, {\"key\": \"B\", \"value\": \"蒙德-清泉镇\"}, {\"key\": \"C\", \"value\": \"稻妻-神无冢\"}, {\"key\": \"D\", \"value\": \"须弥-奥摩斯港\"}]', 'A', NULL, '盐神传说在璃月轻策庄附近的地中之盐遗迹流传，相关任务在此展开。', '2026-01-09 20:30:35', 0.5);
INSERT INTO `biz_question` VALUES (2255, 82, '高三', 1, 1, '蒙德的东风之龙特瓦林曾因何陷入沉睡与痛苦，最终被称为“风魔龙”？', NULL, '[{\"key\": \"A\", \"value\": \"被天理的维系者封印\"}, {\"key\": \"B\", \"value\": \"被深渊教团腐蚀\"}, {\"key\": \"C\", \"value\": \"被魔龙杜林的毒血侵蚀\"}, {\"key\": \"D\", \"value\": \"因磨损而失去理智\"}]', 'C', NULL, '特瓦林在战斗中吞下毒血，被腐蚀污染，陷入痛苦与沉睡。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2256, 82, '高三', 1, 1, '在提瓦特的历史设定中，‘黄金’莱茵多特最广为人知的造物是？', NULL, '[{\"key\": \"A\", \"value\": \"遗迹守卫\"}, {\"key\": \"B\", \"value\": \"魔龙杜林\"}, {\"key\": \"C\", \"value\": \"骗骗花\"}, {\"key\": \"D\", \"value\": \"无相系列元素生命\"}]', 'B', NULL, '‘黄金’莱茵多特是五百年前坎瑞亚的炼金术士，创造了魔龙杜林。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2257, 82, '高三', 1, 1, '“终结一切契约的契约”指的是岩王帝君摩拉克斯与谁订下的最终约定？', NULL, '[{\"key\": \"A\", \"value\": \"璃月七星\"}, {\"key\": \"B\", \"value\": \"若陀龙王\"}, {\"key\": \"C\", \"value\": \"冰之女皇\"}, {\"key\": \"D\", \"value\": \"旅行者\"}]', 'C', NULL, '摩拉克斯与至冬女皇订下契约，交出其神之心，象征与“神”之过去的告别。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2258, 82, '高三', 1, 1, '关于“磨损”，下列哪位角色曾明确提出“用记忆对抗磨损”的观点？', NULL, '[{\"key\": \"A\", \"value\": \"雷电将军（影）\"}, {\"key\": \"B\", \"value\": \"钟离\"}, {\"key\": \"C\", \"value\": \"温迪\"}, {\"key\": \"D\", \"value\": \"纳西妲\"}]', 'A', NULL, '雷电将军（影）认为唯有“永恒”能抵御磨损，并将记忆封存于“一心净土”。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2259, 82, '高三', 1, 1, '根据现有剧情，被称为“原初之人”法涅斯创造的“四个影子”中，司掌“时间”的是？', NULL, '[{\"key\": \"A\", \"value\": \"生之执政\"}, {\"key\": \"B\", \"value\": \"死之执政\"}, {\"key\": \"C\", \"value\": \"时之执政\"}, {\"key\": \"D\", \"value\": \"空之执政\"}]', 'C', NULL, '四影包括时、空、生、死之执政，时之执政名为“伊斯塔露”。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2260, 82, '高三', 1, 1, '岩王帝君摩拉克斯决定隐退，将璃月交给人类自己治理的关键事件（导火索）是？', NULL, '[{\"key\": \"A\", \"value\": \"若陀龙王挣脱封印\"}, {\"key\": \"B\", \"value\": \"奥赛尔被重新封印\"}, {\"key\": \"C\", \"value\": \"成功举办了“请仙典仪”\"}, {\"key\": \"D\", \"value\": \"在“请仙典仪”上假死\"}]', 'D', NULL, '帝君在典仪上假死，以此观察和考验璃月七星与仙人在危机中的应对。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2261, 82, '高三', 1, 1, '在蒙德历史中，推翻旧蒙德贵族统治，并最终成为风神眷属、登上天空岛的人类英雄是？', NULL, '[{\"key\": \"A\", \"value\": \"温妮莎\"}, {\"key\": \"B\", \"value\": \"斯坦利\"}, {\"key\": \"C\", \"value\": \"莱纳德\"}, {\"key\": \"D\", \"value\": \"鲁斯坦\"}]', 'A', NULL, '温妮莎带领族人推翻贵族统治，战后化作雄鹰（或登上天空岛）守护蒙德。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2262, 82, '高三', 1, 1, '坎瑞亚的炼金术士“黄金”创造了许多生命，其中哪位角色被称为她最完美的“人造人”作品？', NULL, '[{\"key\": \"A\", \"value\": \"魔龙杜林\"}, {\"key\": \"B\", \"value\": \"阿贝多\"}, {\"key\": \"C\", \"value\": \"埃洛伊（联动角色）\"}, {\"key\": \"D\", \"value\": \"雷电将军\"}]', 'B', NULL, '阿贝多是“黄金”莱茵多特创造的完美人造人，使用“黑土”炼成术。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2263, 82, '高三', 1, 1, '根据《日月前事》等文本，提瓦特的星空被描述为“虚假”的，其最核心的含义是？', NULL, '[{\"key\": \"A\", \"value\": \"星空是深渊制造的幻象\"}, {\"key\": \"B\", \"value\": \"星空是提瓦特世界的边界屏障\"}, {\"key\": \"C\", \"value\": \"星空是原初之神设置的、记载命运的帷幕\"}, {\"key\": \"D\", \"value\": \"星空是七神共同维持的魔法阵\"}]', 'C', NULL, '虚假的星空是原初之神法涅斯打造的蛋壳，星象命之座实为对命运的记载与编排。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2264, 82, '高三', 2, 1, '下列哪些事件或人物，与“天理的维系者”直接相关？（多选）', NULL, '[{\"key\": \"A\", \"value\": \"五百年前降临坎瑞亚，带来毁灭\"}, {\"key\": \"B\", \"value\": \"在游戏开场阻止旅行者兄妹离开提瓦特\"}, {\"key\": \"C\", \"value\": \"向七神发放“神之心”\"}, {\"key\": \"D\", \"value\": \"在层岩巨渊投下“寒天之钉”\"}]', 'AB', NULL, 'A正确，维系者领导了坎瑞亚灭国之战；B正确，开场动画即为维系者。C、D并非其直接行为。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2265, 82, '高三', 2, 1, '关于“磨损”概念，下列描述符合游戏设定的是？（多选）', NULL, '[{\"key\": \"A\", \"value\": \"是提瓦特世界长生种（魔神、仙兽等）必然面临的法则\"}, {\"key\": \"B\", \"value\": \"若陀龙王因磨损而遗忘与帝君的契约，最终被封印\"}, {\"key\": \"C\", \"value\": \"风神巴巴托斯通过长期沉睡来完全规避磨损\"}, {\"key\": \"D\", \"value\": \"磨损会导致记忆模糊、性格改变、力量衰退甚至疯狂\"}]', 'ABD', NULL, 'A、B、D均正确描述了磨损的表现与影响。C错误，沉睡只能减缓，无法完全规避。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2266, 82, '高三', 2, 1, '下列哪些是“魔神战争”的特点或结果？（多选）', NULL, '[{\"key\": \"A\", \"value\": \"胜利的七位魔神登上“神座”，成为尘世七执政\"}, {\"key\": \"B\", \"value\": \"战败魔神的权能与血肉会污染大地，形成特殊地貌\"}, {\"key\": \"C\", \"value\": \"这场战争由天空岛直接发起并指挥\"}, {\"key\": \"D\", \"value\": \"战争的核心目的是争夺对应地区的统治权柄\"}]', 'ABD', NULL, 'A、B、D均正确。C错误，天空岛设定规则（胜者为神），但并未直接指挥每一场战争。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2267, 82, '高三', 2, 1, '关于“神之心”，目前已知的信息包括？（多选）', NULL, '[{\"key\": \"A\", \"value\": \"它是魔神之位的象征，由天空岛颁发\"}, {\"key\": \"B\", \"value\": \"其本质是第三降临者的遗骸制成的外置魔力器官\"}, {\"key\": \"C\", \"value\": \"失去神之心会显著削弱对应神明的实力\"}, {\"key\": \"D\", \"value\": \"冰之神正在收集所有神之心，意图对抗天理\"}]', 'ABD', NULL, 'A、B、D均为已披露的关键设定。C错误，剧情表明神之心对神明自身实力影响不大（如钟离、温迪）。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2268, 82, '高三', 2, 1, '下列哪些概念或事件与“时间悖论”或“因果闭环”有关？（多选）', NULL, '[{\"key\": \"A\", \"value\": \"森林书任务中，兰那罗的“记忆”修复法留纳神机\"}, {\"key\": \"B\", \"value\": \"鹤观岛的雾与雷鸟的怨恨循环\"}, {\"key\": \"C\", \"value\": \"雷电将军追求的“永恒”与“梦想一心”的传承\"}, {\"key\": \"D\", \"value\": \"渊下宫任务《日月前事》的获取与内容\"}]', 'AB', NULL, 'A（森林书）、B（鹤观）的剧情都涉及时间循环与因果纠缠。C是理念对抗，D是历史记载，不直接构成时间悖论闭环。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2269, 82, '高三', 4, 1, '“尘世之锁”是尘之魔神归终赠予岩王帝君的信物，目前由旅行者保管。', NULL, '[]', '错误', NULL, '“尘世之锁”由归终赠予钟离，现收藏于璃月群玉阁，并未由旅行者保管。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2270, 82, '高三', 4, 1, '“龙王战争”指的是远古时期，提瓦特本土元素龙王与原初之神法涅斯及其创造物之间的战争。', NULL, '[]', '正确', NULL, '龙王战争（龙争）是原初之神与七位元素龙王争夺世界主导权的战争，以龙王战败告终。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2271, 82, '高三', 4, 1, '“天钉”是天空岛用来净化（或毁灭）地面上“禁忌知识”污染的一种手段，在须弥和龙脊雪山均有发现。', NULL, '[]', '正确', NULL, '“寒天之钉”坠于雪山，“赤贯之星”（类似天钉）坠于须弥沙漠，均为净化禁忌知识。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2272, 82, '高三', 4, 1, '“葬火之年”指原初之神法涅斯与第二降临者发生大战并取得胜利的纪元。', NULL, '[]', '正确', NULL, '根据《日月前事》，“葬火之年”即原初之神战胜第二降临者的年代。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2273, 82, '高三', 4, 1, '蒙德地区传说中的“暴风魔神”迭卡拉庇安，其败亡的主要原因是外部势力的讨伐。', NULL, '[]', '错误', NULL, '其败亡主因是内部人民的反抗（温妮莎先祖等），以及信仰的崩塌，而非单纯外部讨伐。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2274, 82, '高三', 3, 1, '璃月的岩元素龙王若陀，因______与______的双重影响，最终与挚友摩拉克斯兵戈相向。', NULL, '[]', '磨损、人类对层岩巨渊的过度开采（或“人类活动的影响”）', NULL, '磨损使其记忆衰退，人类采矿破坏其栖息地，加剧其痛苦与怨恨。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2275, 82, '高三', 3, 1, '目前剧情中，已知的“降临者”共有四位。第一位是原初之神______，第四位是______。', NULL, '[]', '法涅斯，旅行者###空###莹', NULL, '降临者序列：第一法涅斯，第二（未知），第三（遗骸制神之心），第四旅行者。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2276, 82, '高三', 3, 1, '蒙德的英雄温妮莎，在传说中化身为______，永远守护着蒙德城。', NULL, '[]', '（一只）雄鹰（或“鹰”）', NULL, '温妮莎的故事中，她化作雄鹰，盘旋在蒙德天空，象征着自由与守护。', '2026-01-09 20:30:36', 0.5);
INSERT INTO `biz_question` VALUES (2277, 82, '高三', 3, 1, '“______”是坎瑞亚王国覆灭后，由残存国民组建的地下组织，其目标是向天理复仇。', NULL, '[]', '深渊教团', NULL, '深渊教团由坎瑞亚遗民组成，领导者是“王子/公主”（旅行者血亲）。', '2026-01-09 20:30:37', 0.5);
INSERT INTO `biz_question` VALUES (2278, 82, '高三', 3, 1, '炼金术士阿贝多进行生命炼成的核心材料“______”，与带来毁灭的“______”本质同源。', NULL, '[]', '黑土、腐殖层（或“白垩”、“腐殖层毒素”）', NULL, '“黑土”与给龙脊雪山带来灾厄的“腐殖层”力量，均源自“黄金”的炼金术。', '2026-01-09 20:30:37', 0.5);
INSERT INTO `biz_question` VALUES (2279, 82, '高三', 5, 1, '结合若陀龙王与特瓦林的经历，简述“磨损”在不同长生种身上的具体表现有何异同。（6分）', NULL, '[]', '相同点：均导致记忆混乱、痛苦、性情大变，最终对守护之物造成威胁。若陀遗忘契约攻击璃月，特瓦林被毒血与怨恨侵蚀攻击蒙德。\n不同点：诱因不同。若陀的磨损是漫长岁月与外力（开采）共同作用，表现为地质记忆的消退与愤怒。特瓦林的磨损更直接来自外部伤害（毒血）和被遗忘的孤独，表现为生理腐蚀与精神背叛感。', NULL, '同：都导致失忆与敌对行为。异：若陀是时间+环境，特瓦林是伤害+情感。', '2026-01-09 20:30:37', 0.5);
INSERT INTO `biz_question` VALUES (2280, 82, '高三', 5, 1, '分析在“魔神战争”中战败魔神的几种典型“命运”。（6分）', NULL, '[]', '1. 死亡/被封印：如盐神赫乌莉亚被杀，奥赛尔被封印。\n2. 力量被分割或继承：如梦之魔神力量被魈等夜叉分食，尘神力量可能融入璃月大地。\n3. 化为地貌或灾害：其残骸或怨念形成特殊环境，如孤云阁（岩枪镇压）、层岩巨渊下的污秽。\n4. 改变存在形式：如北风狼王安德留斯放弃魔神资格，化作灵魂守护大地。', NULL, '战败魔神命运多样：直接消亡、力量转移、污染环境、转变形态。', '2026-01-09 20:30:37', 0.5);
INSERT INTO `biz_question` VALUES (2281, 82, '高三', 5, 1, '比较岩神摩拉克斯与风神巴巴托斯应对“磨损”策略的差异，并分析其背后反映的治国理念。（8分）', NULL, '[]', '差异：\n1. 摩拉克斯：坚守职责，以“契约”作为社会运行和自身行为的基石，用强大的意志和理性对抗磨损，最后选择假死“退休”来测试人类能否独立。\n2. 巴巴托斯：选择“不干正事”，将自由还给蒙德人，自己长期沉睡或云游，以此规避治理职责带来的磨损。\n理念反映：\n摩拉克斯的策略反映其“父权式”守护与“契约”治国的理念，强调秩序、责任与传承。巴巴托斯的策略反映其“自由”与“无为而治”的理念，相信人民的自主性，神明仅作为象征和最后保障。', NULL, '钟离：用契约和责任硬抗，最后放手。温迪：用沉睡和放权回避。前者重秩序传承，后者重自由信任。', '2026-01-09 20:30:37', 0.5);
INSERT INTO `biz_question` VALUES (2282, 82, '高三', 5, 1, '阐述“神之心”的本质及其在“尘世七执政”体系中的真正作用。（8分）', NULL, '[]', '本质：由“第三降临者”的遗骸制成的特殊外置魔力器官，与天空岛相连。\n真正作用：\n1. 并非执政力量核心：神明力量源自自身权柄，失去神之心实力不减（如钟离、温迪）。\n2. 是天空岛发放的“身份证”与“通讯器”：象征执政合法性，并可能与天空岛进行联系（如冰神收集可能用于集体通话或谈判）。\n3. 是庞大的魔力增幅与转换装置：能操控地脉能量，为国家级工程供能（如稻妻的“雷祸”屏障，须弥的虚空系统）。\n总结：它是天空岛控制七神、连接世界的工具，而非神明的力量之源。', NULL, '本质是第三降临者遗骸。作用是身份象征、天空岛通讯器、操控地脉的增幅器，非力量本源。', '2026-01-09 20:30:37', 0.5);
INSERT INTO `biz_question` VALUES (2283, 82, '高三', 5, 1, '论述“虚假的星空”这一设定对理解提瓦特世界历史与命运观的意义。（10分）', NULL, '[]', '意义：\n1. 揭示世界本质：提瓦特是一个被创造和封装的世界，星空是原初之神法涅斯打造的“蛋壳”或“帷幕”，世界存在边界与造物主。\n2. 解释命运观：星空上的“命之座”意味着个体的命运可能被预先记载和编排，挑战了自由意志的存在。占星术实为解读这套既定程序。\n3. 关联核心冲突：“虚假”暗示世界的不真实与束缚，这可能是深渊教团、乃至冰神想要反抗天理的根本动机——打破虚假，追寻真实或自由。\n4. 提供历史线索：星空的“虚假性”与原初之神改造世界、击败龙王与降临者的历史直接相关，是解读远古纪元的钥匙。\n它从哲学层面深化了《原神》的主线矛盾，将故事从国家纷争提升至世界本质与命运反抗的层面。', NULL, '表明世界是被造的、命运或被预定，解释了角色反抗天理的深层动机，是理解世界观高度的关键。', '2026-01-09 20:30:37', 0.5);

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
-- Records of biz_question_knowledge_point
-- ----------------------------
INSERT INTO `biz_question_knowledge_point` VALUES (12, 10);
INSERT INTO `biz_question_knowledge_point` VALUES (58, 101);
INSERT INTO `biz_question_knowledge_point` VALUES (59, 102);
INSERT INTO `biz_question_knowledge_point` VALUES (60, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (61, 104);
INSERT INTO `biz_question_knowledge_point` VALUES (62, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (63, 105);
INSERT INTO `biz_question_knowledge_point` VALUES (64, 106);
INSERT INTO `biz_question_knowledge_point` VALUES (65, 107);
INSERT INTO `biz_question_knowledge_point` VALUES (66, 108);
INSERT INTO `biz_question_knowledge_point` VALUES (67, 108);
INSERT INTO `biz_question_knowledge_point` VALUES (68, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (69, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (70, 106);
INSERT INTO `biz_question_knowledge_point` VALUES (71, 102);
INSERT INTO `biz_question_knowledge_point` VALUES (72, 106);
INSERT INTO `biz_question_knowledge_point` VALUES (73, 109);
INSERT INTO `biz_question_knowledge_point` VALUES (74, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (75, 109);
INSERT INTO `biz_question_knowledge_point` VALUES (76, 105);
INSERT INTO `biz_question_knowledge_point` VALUES (77, 108);
INSERT INTO `biz_question_knowledge_point` VALUES (78, 110);
INSERT INTO `biz_question_knowledge_point` VALUES (79, 104);
INSERT INTO `biz_question_knowledge_point` VALUES (80, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (81, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (82, 104);
INSERT INTO `biz_question_knowledge_point` VALUES (83, 101);
INSERT INTO `biz_question_knowledge_point` VALUES (84, 109);
INSERT INTO `biz_question_knowledge_point` VALUES (85, 102);
INSERT INTO `biz_question_knowledge_point` VALUES (86, 109);
INSERT INTO `biz_question_knowledge_point` VALUES (87, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (88, 108);
INSERT INTO `biz_question_knowledge_point` VALUES (89, 109);
INSERT INTO `biz_question_knowledge_point` VALUES (90, 106);
INSERT INTO `biz_question_knowledge_point` VALUES (91, 103);
INSERT INTO `biz_question_knowledge_point` VALUES (92, 111);
INSERT INTO `biz_question_knowledge_point` VALUES (93, 110);
INSERT INTO `biz_question_knowledge_point` VALUES (94, 104);
INSERT INTO `biz_question_knowledge_point` VALUES (95, 102);
INSERT INTO `biz_question_knowledge_point` VALUES (96, 106);
INSERT INTO `biz_question_knowledge_point` VALUES (97, 104);
INSERT INTO `biz_question_knowledge_point` VALUES (98, 112);
INSERT INTO `biz_question_knowledge_point` VALUES (99, 112);
INSERT INTO `biz_question_knowledge_point` VALUES (100, 112);
INSERT INTO `biz_question_knowledge_point` VALUES (101, 112);
INSERT INTO `biz_question_knowledge_point` VALUES (102, 112);
INSERT INTO `biz_question_knowledge_point` VALUES (103, 112);
INSERT INTO `biz_question_knowledge_point` VALUES (104, 101);
INSERT INTO `biz_question_knowledge_point` VALUES (105, 104);
INSERT INTO `biz_question_knowledge_point` VALUES (106, 104);
INSERT INTO `biz_question_knowledge_point` VALUES (2184, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2184, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2184, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2184, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2184, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2184, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2184, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2184, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2185, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2185, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2185, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2185, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2185, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2185, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2185, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2185, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2186, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2186, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2186, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2186, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2186, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2186, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2186, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2186, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2187, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2187, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2187, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2187, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2187, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2187, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2187, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2187, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2188, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2188, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2188, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2188, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2188, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2188, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2188, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2188, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2189, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2189, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2189, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2189, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2189, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2189, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2189, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2189, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2190, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2190, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2190, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2190, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2190, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2190, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2190, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2190, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2191, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2191, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2191, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2191, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2191, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2191, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2191, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2191, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2192, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2192, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2192, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2192, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2192, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2192, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2192, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2192, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2193, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2193, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2193, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2193, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2193, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2193, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2193, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2193, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2194, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2194, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2194, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2194, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2194, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2194, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2194, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2194, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2195, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2195, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2195, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2195, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2195, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2195, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2195, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2195, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2196, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2196, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2196, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2196, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2196, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2196, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2196, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2196, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2197, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2197, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2197, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2197, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2197, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2197, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2197, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2197, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2198, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2198, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2198, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2198, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2198, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2198, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2198, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2198, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2199, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2199, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2199, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2199, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2199, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2199, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2199, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2199, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2200, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2200, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2200, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2200, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2200, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2200, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2200, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2200, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2201, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2201, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2201, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2201, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2201, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2201, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2201, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2201, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2202, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2202, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2202, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2202, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2202, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2202, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2202, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2202, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2203, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2203, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2203, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2203, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2203, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2203, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2203, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2203, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2204, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2204, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2204, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2204, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2204, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2204, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2204, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2204, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2205, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2205, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2205, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2205, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2205, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2205, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2205, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2205, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2206, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2206, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2206, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2206, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2206, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2206, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2206, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2206, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2207, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2207, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2207, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2207, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2207, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2207, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2207, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2207, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2208, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2208, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2208, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2208, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2208, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2208, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2208, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2208, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2209, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2209, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2209, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2209, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2209, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2209, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2209, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2209, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2210, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2210, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2210, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2210, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2210, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2210, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2210, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2210, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2211, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2211, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2211, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2211, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2211, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2211, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2211, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2211, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2212, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2212, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2212, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2212, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2212, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2212, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2212, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2212, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2213, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2213, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2213, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2213, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2213, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2213, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2213, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2213, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2214, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2214, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2214, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2214, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2214, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2214, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2214, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2214, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2215, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2215, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2215, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2215, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2215, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2215, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2215, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2215, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2216, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2216, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2216, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2216, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2216, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2216, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2216, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2216, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2217, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2217, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2217, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2217, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2217, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2217, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2217, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2217, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2218, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2218, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2218, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2218, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2218, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2218, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2218, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2218, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2219, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2219, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2219, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2219, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2219, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2219, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2219, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2219, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2220, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2220, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2220, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2220, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2220, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2220, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2220, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2220, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2221, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2221, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2221, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2221, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2221, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2221, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2221, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2221, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2222, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2222, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2222, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2222, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2222, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2222, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2222, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2222, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2223, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2223, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2223, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2223, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2223, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2223, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2223, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2223, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2224, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2224, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2224, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2224, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2224, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2224, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2224, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2224, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2225, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2225, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2225, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2225, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2225, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2225, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2225, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2225, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2226, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2226, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2226, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2226, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2226, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2226, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2226, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2226, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2227, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2227, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2227, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2227, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2227, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2227, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2227, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2227, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2228, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2228, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2228, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2228, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2228, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2228, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2228, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2228, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2229, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2229, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2229, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2229, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2229, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2229, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2229, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2229, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2230, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2230, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2230, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2230, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2230, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2230, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2230, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2230, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2231, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2231, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2231, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2231, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2231, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2231, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2231, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2231, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2232, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2232, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2232, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2232, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2232, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2232, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2232, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2232, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2233, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2233, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2233, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2233, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2233, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2233, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2233, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2233, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2234, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2234, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2234, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2234, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2234, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2234, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2234, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2234, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2235, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2235, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2235, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2235, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2235, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2235, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2235, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2235, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2236, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2236, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2236, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2236, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2236, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2236, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2236, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2236, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2237, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2237, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2237, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2237, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2237, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2237, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2237, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2237, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2238, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2238, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2238, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2238, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2238, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2238, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2238, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2238, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2239, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2239, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2239, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2239, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2239, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2239, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2239, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2239, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2240, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2240, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2240, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2240, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2240, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2240, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2240, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2240, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2241, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2241, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2241, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2241, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2241, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2241, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2241, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2241, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2242, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2242, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2242, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2242, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2242, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2242, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2242, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2242, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2243, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2243, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2243, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2243, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2243, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2243, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2243, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2243, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2244, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2244, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2244, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2244, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2244, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2244, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2244, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2244, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2245, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2245, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2245, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2245, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2245, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2245, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2245, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2245, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2246, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2246, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2246, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2246, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2246, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2246, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2246, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2246, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2247, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2247, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2247, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2247, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2247, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2247, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2247, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2247, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2248, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2248, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2248, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2248, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2248, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2248, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2248, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2248, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2249, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2249, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2249, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2249, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2249, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2249, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2249, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2249, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2250, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2250, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2250, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2250, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2250, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2250, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2250, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2250, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2251, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2251, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2251, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2251, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2251, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2251, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2251, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2251, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2252, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2252, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2252, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2252, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2252, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2252, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2252, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2252, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2253, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2253, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2253, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2253, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2253, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2253, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2253, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2253, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2254, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2255, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2256, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2257, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2258, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2259, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2260, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2261, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2262, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2263, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2264, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2265, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2266, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2267, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2268, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2269, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2270, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2271, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2272, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2273, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2274, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2276, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2277, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2278, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2279, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2280, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2281, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2282, 86);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 54);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 55);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 58);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 60);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 61);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 62);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 63);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 64);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 65);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 66);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 67);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 68);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 69);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 70);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 71);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 72);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 73);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 74);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 75);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 76);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 77);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 78);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 79);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 80);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 81);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 82);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 83);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 84);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 85);
INSERT INTO `biz_question_knowledge_point` VALUES (2283, 86);

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生签到表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_sign_in
-- ----------------------------
INSERT INTO `biz_sign_in` VALUES (1, 1, '2025-11-29', '2025-11-29 21:38:38');
INSERT INTO `biz_sign_in` VALUES (2, 3, '2025-11-29', '2025-11-29 21:45:17');
INSERT INTO `biz_sign_in` VALUES (3, 1, '2025-12-02', '2025-12-02 18:36:20');
INSERT INTO `biz_sign_in` VALUES (4, 1, '2025-12-04', '2025-12-04 16:44:46');
INSERT INTO `biz_sign_in` VALUES (5, 3, '2025-12-05', '2025-12-05 12:39:51');
INSERT INTO `biz_sign_in` VALUES (6, 1, '2025-12-05', '2025-12-05 12:39:55');
INSERT INTO `biz_sign_in` VALUES (7, 1, '2026-01-07', '2026-01-07 08:50:13');
INSERT INTO `biz_sign_in` VALUES (8, 1, '2026-01-10', '2026-01-10 14:33:44');

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
-- Records of biz_student
-- ----------------------------
INSERT INTO `biz_student` VALUES (1, '2215033133', '李天', '17344351182', '$2a$10$Evqd1vwXoNFZsdkKnkyfg.ASq.AdKHOOs8wLMZCWJ7TtAWjvDZfFi', '/api/v1/files/f9561467-5183-457a-9c52-9eecbad2a2cd.jpg', 18, '高三', '1班', '2025-08-08 16:33:40', '2026-01-11 15:57:46', 3600, 'border: 3px solid #ffd700; box-shadow: 0 0 15px #ffd700; border-radius: 50%;', 'uploads/test1.jpg', 0);
INSERT INTO `biz_student` VALUES (3, '12345', '123124214214124', '', '$2a$10$Evqd1vwXoNFZsdkKnkyfg.ASq.AdKHOOs8wLMZCWJ7TtAWjvDZfFi', NULL, 13, '高一', NULL, '2025-08-12 13:55:46', '2026-01-11 15:57:50', 1015, NULL, NULL, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 114 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生知识点掌握度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_student_knowledge_mastery
-- ----------------------------
INSERT INTO `biz_student_knowledge_mastery` VALUES (1, 1, 10, 3, 6, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (2, 3, 10, 2, 8, 25.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (3, 1, 12, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (4, 3, 12, 4, 8, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (5, 1, 13, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (6, 3, 13, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (7, 1, 14, 4, 8, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (8, 3, 14, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (9, 1, 15, 2, 6, 33.33, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (10, 3, 15, 3, 7, 42.86, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (11, 1, 16, 3, 6, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (12, 3, 16, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (13, 1, 17, 4, 8, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (14, 3, 17, 4, 8, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (15, 1, 18, 4, 6, 66.67, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (16, 3, 18, 3, 8, 37.50, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (17, 1, 19, 2, 6, 33.33, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (18, 3, 19, 3, 6, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (19, 1, 20, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (20, 3, 20, 4, 7, 57.14, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (21, 1, 21, 3, 6, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (22, 3, 21, 4, 6, 66.67, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (23, 1, 22, 2, 8, 25.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (24, 3, 22, 3, 8, 37.50, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (25, 1, 23, 2, 6, 33.33, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (26, 3, 23, 3, 8, 37.50, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (27, 1, 24, 4, 6, 66.67, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (28, 3, 24, 4, 6, 66.67, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (29, 1, 25, 2, 8, 25.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (30, 3, 25, 4, 7, 57.14, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (31, 1, 26, 3, 8, 37.50, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (32, 3, 26, 3, 8, 37.50, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (33, 1, 27, 4, 7, 57.14, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (34, 3, 27, 2, 6, 33.33, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (35, 1, 28, 4, 8, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (36, 3, 28, 4, 7, 57.14, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (37, 1, 29, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (38, 3, 29, 4, 6, 66.67, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (39, 1, 30, 4, 8, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (40, 3, 30, 4, 7, 57.14, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (41, 1, 31, 4, 8, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (42, 3, 31, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (43, 1, 32, 4, 6, 66.67, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (44, 3, 32, 2, 7, 28.57, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (45, 1, 33, 3, 6, 50.00, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (46, 3, 33, 2, 6, 33.33, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (47, 1, 34, 4, 7, 57.14, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (48, 3, 34, 4, 6, 66.67, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (49, 1, 35, 3, 7, 42.86, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (50, 3, 35, 3, 7, 42.86, '2026-01-09 12:33:35', '2026-01-09 12:33:35');
INSERT INTO `biz_student_knowledge_mastery` VALUES (64, 1, 36, 4, 6, 66.67, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (65, 3, 36, 3, 6, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (66, 1, 37, 2, 6, 33.33, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (67, 3, 37, 2, 8, 25.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (68, 1, 38, 4, 8, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (69, 3, 38, 2, 7, 28.57, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (70, 1, 39, 3, 7, 42.86, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (71, 3, 39, 3, 8, 37.50, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (72, 1, 40, 2, 6, 33.33, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (73, 3, 40, 3, 6, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (74, 1, 41, 3, 7, 42.86, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (75, 3, 41, 3, 6, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (76, 1, 42, 2, 8, 25.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (77, 3, 42, 3, 7, 42.86, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (78, 1, 43, 3, 6, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (79, 3, 43, 3, 8, 37.50, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (80, 1, 44, 4, 6, 66.67, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (81, 3, 44, 2, 6, 33.33, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (82, 1, 45, 3, 8, 37.50, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (83, 3, 45, 2, 6, 33.33, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (84, 1, 46, 2, 7, 28.57, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (85, 3, 46, 3, 6, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (86, 1, 47, 4, 6, 66.67, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (87, 3, 47, 3, 7, 42.86, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (88, 1, 48, 3, 6, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (89, 3, 48, 4, 7, 57.14, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (90, 1, 49, 3, 8, 37.50, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (91, 3, 49, 3, 8, 37.50, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (92, 1, 50, 2, 6, 33.33, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (93, 3, 50, 2, 8, 25.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (94, 1, 51, 4, 8, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (95, 3, 51, 4, 7, 57.14, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (96, 1, 52, 4, 6, 66.67, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (97, 3, 52, 3, 7, 42.86, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (98, 1, 53, 3, 8, 37.50, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (99, 3, 53, 3, 6, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (100, 1, 54, 2, 7, 28.57, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (101, 3, 54, 4, 8, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (102, 1, 55, 4, 8, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (103, 3, 55, 4, 8, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (104, 1, 56, 2, 7, 28.57, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (105, 3, 56, 4, 8, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (106, 1, 57, 3, 7, 42.86, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (107, 3, 57, 3, 7, 42.86, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (108, 1, 58, 2, 6, 33.33, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (109, 3, 58, 2, 7, 28.57, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (110, 1, 59, 4, 8, 50.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (111, 3, 59, 4, 7, 57.14, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (112, 1, 60, 4, 7, 57.14, '2026-01-09 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_knowledge_mastery` VALUES (113, 3, 60, 2, 8, 25.00, '2026-01-09 12:46:56', '2026-01-09 12:46:56');

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
-- Records of biz_student_paper
-- ----------------------------
INSERT INTO `biz_student_paper` VALUES (1, 1, 2, 53.42, 1, '2025-12-29 12:46:56', '2026-01-05 12:46:56', NULL, NULL, '2025-12-26 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (2, 3, 2, 95.26, 1, '2025-12-11 12:46:56', '2025-12-13 12:46:56', NULL, NULL, '2025-12-28 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (3, 1, 3, 29.86, 1, '2025-12-17 12:46:56', '2025-12-11 12:46:56', NULL, NULL, '2025-12-29 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (4, 3, 3, 98.64, 2, '2026-01-03 12:46:56', '2025-12-24 12:46:56', NULL, NULL, '2025-12-10 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (5, 1, 5, 34.04, 2, '2025-12-16 12:46:56', '2025-12-17 12:46:56', NULL, NULL, '2025-12-30 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (6, 3, 5, 34.70, 2, '2025-12-15 12:46:56', '2025-12-13 12:46:56', NULL, NULL, '2025-12-11 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (7, 1, 6, 6.72, 1, '2026-01-04 12:46:56', '2025-12-23 12:46:56', NULL, NULL, '2026-01-06 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (8, 3, 6, 84.31, 2, '2026-01-03 12:46:56', '2026-01-03 12:46:56', NULL, NULL, '2025-12-29 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (9, 1, 7, 20.49, 2, '2026-01-06 12:46:56', '2025-12-16 12:46:56', NULL, NULL, '2025-12-23 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (10, 3, 7, 34.27, 0, '2025-12-27 12:46:56', '2026-01-08 12:46:56', NULL, NULL, '2025-12-17 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (11, 1, 9, 68.13, 0, '2025-12-20 12:46:56', '2025-12-11 12:46:56', NULL, NULL, '2025-12-18 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (12, 3, 9, 69.65, 1, '2025-12-19 12:46:56', '2025-12-25 12:46:56', NULL, NULL, '2026-01-01 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (13, 1, 10, 75.90, 0, '2026-01-05 12:46:56', '2025-12-24 12:46:56', NULL, NULL, '2026-01-03 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (14, 3, 10, 28.93, 2, '2025-12-19 12:46:56', '2025-12-13 12:46:56', NULL, NULL, '2026-01-03 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (15, 1, 11, 26.55, 2, '2026-01-03 12:46:56', '2025-12-20 12:46:56', NULL, NULL, '2025-12-20 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (16, 3, 11, 33.14, 2, '2025-12-30 12:46:56', '2025-12-17 12:46:56', NULL, NULL, '2025-12-18 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (17, 1, 13, 29.09, 0, '2025-12-18 12:46:56', '2025-12-17 12:46:56', NULL, NULL, '2025-12-23 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (18, 3, 13, 51.73, 2, '2025-12-11 12:46:56', '2026-01-01 12:46:56', NULL, NULL, '2026-01-02 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (19, 1, 14, 32.32, 2, '2026-01-09 12:46:56', '2026-01-07 12:46:56', NULL, NULL, '2026-01-02 12:46:56', '2026-01-09 12:46:56');
INSERT INTO `biz_student_paper` VALUES (20, 3, 14, 94.43, 0, '2025-12-25 12:46:56', '2025-12-29 12:46:56', NULL, NULL, '2025-12-31 12:46:56', '2026-01-09 12:46:56');

-- ----------------------------
-- Table structure for biz_study_session
-- ----------------------------
DROP TABLE IF EXISTS `biz_study_session`;
CREATE TABLE `biz_study_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浼氳瘽ID',
  `student_id` bigint NOT NULL COMMENT '瀛︾敓ID',
  `course_id` bigint NOT NULL COMMENT '璇剧▼ID',
  `resource_id` bigint NOT NULL COMMENT '璧勬簮ID',
  `session_start` datetime NOT NULL COMMENT '浼氳瘽寮??鏃堕棿',
  `session_end` datetime NULL DEFAULT NULL COMMENT '浼氳瘽缁撴潫鏃堕棿',
  `duration` int NULL DEFAULT 0 COMMENT '浼氳瘽鏃堕暱锛堢?锛',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_course`(`course_id` ASC) USING BTREE,
  INDEX `idx_resource`(`resource_id` ASC) USING BTREE,
  INDEX `idx_session_start`(`session_start` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瀛︿範浼氳瘽琛?紙鐢ㄤ簬瀛︿範琛屼负鍒嗘瀽锛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_study_session
-- ----------------------------

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
-- Records of biz_subject
-- ----------------------------
INSERT INTO `biz_subject` VALUES (1, '数学', '高三数学', '高三', '2025-08-08 08:52:46', '2025-08-08 08:52:46');
INSERT INTO `biz_subject` VALUES (2, '物理', '高二物理\n', '高二', '2025-08-08 08:53:08', '2025-08-08 08:53:08');
INSERT INTO `biz_subject` VALUES (3, '英语', '高一英语\n', '高一', '2025-08-08 08:53:22', '2025-08-08 08:53:22');
INSERT INTO `biz_subject` VALUES (16, '数学', '', '高一', '2025-08-15 10:24:43', '2025-08-15 10:24:43');
INSERT INTO `biz_subject` VALUES (17, '英语', '', '高二', '2025-11-20 17:30:45', '2025-11-20 17:30:45');
INSERT INTO `biz_subject` VALUES (18, '历史', '', '高三', '2025-12-02 19:58:56', '2025-12-02 19:58:56');
INSERT INTO `biz_subject` VALUES (19, '初一语文', '初中一年级语文课程，包括阅读理解、文言文、写作等', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (20, '初一数学', '初中一年级数学课程，包括代数、几何等基础内容', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (21, '初一英语', '初中一年级英语课程，学习基础词汇、语法和阅读', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (22, '初一生物', '初中一年级生物课程，学习生命科学基础知识', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (23, '初一历史', '初中一年级历史课程，学习中国古代史', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (24, '初一地理', '初中一年级地理课程，学习自然地理和人文地理', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (25, '初一政治', '初中一年级道德与法治课程', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (26, '初一体育', '初中一年级体育与健康课程', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (27, '初一音乐', '初中一年级音乐欣赏与实践课程', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (28, '初一美术', '初中一年级美术课程，培养艺术素养', '初一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (29, '初二语文', '初中二年级语文课程，深化阅读与写作能力', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (30, '初二数学', '初中二年级数学课程，学习函数、三角形等内容', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (31, '初二英语', '初中二年级英语课程，提升听说读写综合能力', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (32, '初二物理', '初中二年级物理课程，学习力学、光学等基础物理', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (33, '初二生物', '初中二年级生物课程，深入学习生命科学', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (34, '初二历史', '初中二年级历史课程，学习中国近代史和世界史', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (35, '初二地理', '初中二年级地理课程，学习中国地理和世界地理', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (36, '初二政治', '初中二年级道德与法治课程', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (37, '初二体育', '初中二年级体育与健康课程', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (38, '初二音乐', '初中二年级音乐课程', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (39, '初二美术', '初中二年级美术课程', '初二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (40, '初三语文', '初中三年级语文课程，中考备考重点', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (41, '初三数学', '初中三年级数学课程，综合复习与提升', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (42, '初三英语', '初中三年级英语课程，中考英语备考', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (43, '初三物理', '初中三年级物理课程，学习电学、热学等', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (44, '初三化学', '初中三年级化学课程，化学基础与实验', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (45, '初三生物', '初中三年级生物课程，生物总复习', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (46, '初三历史', '初中三年级历史课程，中国历史与世界历史总复习', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (47, '初三地理', '初中三年级地理课程，地理知识综合应用', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (48, '初三政治', '初中三年级道德与法治课程，中考政治备考', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (49, '初三体育', '初中三年级体育与健康课程，中考体育训练', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (50, '初三音乐', '初中三年级音乐课程', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (51, '初三美术', '初中三年级美术课程', '初三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (52, '高一语文', '高中一年级语文课程，古代文学与现代文阅读', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (53, '高一数学', '高中一年级数学课程，集合、函数、三角函数等', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (54, '高一英语', '高中一年级英语课程，高中英语基础', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (55, '高一物理', '高中一年级物理课程，运动学、牛顿定律等', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (56, '高一化学', '高中一年级化学课程，物质的量、氧化还原等', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (57, '高一生物', '高中一年级生物课程，细胞、遗传等基础知识', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (58, '高一历史', '高中一年级历史课程，中国古代史和世界古代史', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (59, '高一地理', '高中一年级地理课程，自然地理基础', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (60, '高一政治', '高中一年级思想政治课程，经济与政治生活', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (61, '高一体育', '高中一年级体育与健康课程', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (62, '高一音乐', '高中一年级音乐鉴赏课程', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (63, '高一美术', '高中一年级美术鉴赏课程', '高一', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (64, '高二语文', '高中二年级语文课程，文学鉴赏与写作训练', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (65, '高二数学', '高中二年级数学课程，解析几何、数列、立体几何等', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (66, '高二英语', '高中二年级英语课程，英语综合应用能力提升', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (67, '高二物理', '高中二年级物理课程，电磁学、光学等', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (68, '高二化学', '高中二年级化学课程，有机化学、化学反应原理', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (69, '高二生物', '高中二年级生物课程，生态学、进化等', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (70, '高二历史', '高中二年级历史课程，中国近现代史和世界近现代史', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (71, '高二地理', '高中二年级地理课程，人文地理和区域地理', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (72, '高二政治', '高中二年级思想政治课程，文化生活与哲学', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (73, '高二体育', '高中二年级体育与健康课程', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (74, '高二音乐', '高中二年级音乐课程', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (75, '高二美术', '高中二年级美术课程', '高二', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (76, '高三语文', '高中三年级语文课程，高考语文备考与冲刺', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (77, '高三数学', '高中三年级数学课程，高考数学总复习', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (78, '高三英语', '高中三年级英语课程，高考英语备考', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (79, '高三物理', '高中三年级物理课程，高考物理综合复习', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (80, '高三化学', '高中三年级化学课程，高考化学备考', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (81, '高三生物', '高中三年级生物课程，高考生物总复习', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (82, '高三历史', '高中三年级历史课程，高考历史备考', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (83, '高三地理', '高中三年级地理课程，高考地理综合复习', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (84, '高三政治', '高中三年级思想政治课程，高考政治备考', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (85, '高三体育', '高中三年级体育与健康课程', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (86, '高三音乐', '高中三年级音乐课程', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');
INSERT INTO `biz_subject` VALUES (87, '高三美术', '高中三年级美术课程', '高三', '2026-01-07 13:42:55', '2026-01-07 13:42:55');

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
-- Records of biz_user_achievement
-- ----------------------------
INSERT INTO `biz_user_achievement` VALUES (1, 1, 1, '2025-12-04 18:12:33');
INSERT INTO `biz_user_achievement` VALUES (2, 1, 2, '2025-12-04 18:12:33');

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
-- Records of biz_user_goods
-- ----------------------------
INSERT INTO `biz_user_goods` VALUES (1, 1, 4, '2025-11-27 22:15:02');
INSERT INTO `biz_user_goods` VALUES (2, 1, 3, '2025-11-27 22:15:05');
INSERT INTO `biz_user_goods` VALUES (3, 1, 1, '2025-11-27 22:17:25');
INSERT INTO `biz_user_goods` VALUES (4, 1, 2, '2025-11-27 22:17:27');

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
-- Records of biz_wrong_record
-- ----------------------------
INSERT INTO `biz_wrong_record` VALUES (1, 1, 11, NULL, NULL, '1231', '2025-08-08 17:34:26', 0, 0, '2025-12-03 17:41:26');
INSERT INTO `biz_wrong_record` VALUES (3, 3, 9, NULL, NULL, '12412412415145345', '2025-08-12 13:56:14', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (4, 3, 8, NULL, NULL, '125213513461346', '2025-08-12 13:56:26', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (5, 1, 15, NULL, NULL, '1345134513451345', '2025-08-14 15:50:10', 0, 0, '2025-12-03 17:41:30');
INSERT INTO `biz_wrong_record` VALUES (6, 1, 16, NULL, 'B', '在线练习错误', '2025-08-14 16:59:20', 0, 0, '2025-12-03 17:41:32');
INSERT INTO `biz_wrong_record` VALUES (7, 1, 17, NULL, 'D', '在线练习错误', '2025-08-14 17:15:46', 0, 1, '2025-12-03 17:41:36');
INSERT INTO `biz_wrong_record` VALUES (8, 1, 58, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 1, '2025-12-03 17:41:38');
INSERT INTO `biz_wrong_record` VALUES (9, 1, 59, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 1, '2025-12-03 17:46:34');
INSERT INTO `biz_wrong_record` VALUES (10, 1, 60, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 1, '2025-12-03 17:46:36');
INSERT INTO `biz_wrong_record` VALUES (11, 1, 61, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 1, '2025-12-03 17:46:37');
INSERT INTO `biz_wrong_record` VALUES (12, 1, 62, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 1, '2025-12-03 17:46:40');
INSERT INTO `biz_wrong_record` VALUES (13, 1, 63, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 1, '2025-12-03 17:46:41');
INSERT INTO `biz_wrong_record` VALUES (14, 1, 64, NULL, 'D', '在线练习错误', '2025-08-15 10:58:48', 0, 0, '2025-12-03 17:46:42');
INSERT INTO `biz_wrong_record` VALUES (15, 1, 65, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 0, '2025-12-03 17:46:43');
INSERT INTO `biz_wrong_record` VALUES (16, 1, 66, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 0, '2025-12-03 17:46:44');
INSERT INTO `biz_wrong_record` VALUES (17, 1, 67, NULL, 'C', '在线练习错误', '2025-08-15 10:58:48', 0, 0, '2025-12-03 18:01:46');
INSERT INTO `biz_wrong_record` VALUES (18, 1, 80, NULL, 'C', '在线练习错误', '2025-08-19 13:26:53', 0, 1, '2025-12-03 18:01:50');
INSERT INTO `biz_wrong_record` VALUES (19, 1, 83, NULL, 'B', '在线练习错误', '2025-08-19 13:26:53', 0, 1, '2025-12-03 18:02:25');
INSERT INTO `biz_wrong_record` VALUES (20, 1, 68, NULL, '的21都2耳朵', '在线练习错误', '2025-08-19 13:36:27', 0, 1, '2025-12-03 18:10:16');
INSERT INTO `biz_wrong_record` VALUES (21, 1, 69, NULL, '大事发生的发2', '在线练习错误', '2025-08-19 13:36:27', 0, 0, '2025-12-03 18:10:19');
INSERT INTO `biz_wrong_record` VALUES (22, 1, 72, NULL, '大叔大叔的', '在线练习错误', '2025-08-19 13:36:27', 0, 0, '2025-12-03 18:10:21');
INSERT INTO `biz_wrong_record` VALUES (23, 1, 73, NULL, '打算的', '在线练习错误', '2025-08-19 13:36:27', 0, 0, '2025-12-03 18:10:24');
INSERT INTO `biz_wrong_record` VALUES (24, 1, 77, NULL, '是哒是哒是哒', '在线练习错误', '2025-08-19 13:36:27', 0, 0, '2025-12-03 18:10:27');
INSERT INTO `biz_wrong_record` VALUES (25, 1, 81, NULL, 'C', '在线练习错误', '2025-08-19 13:36:27', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (26, 1, 87, NULL, 'B', '在线练习错误', '2025-08-19 13:36:27', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (27, 1, 89, NULL, '啊实打实的', '在线练习错误', '2025-08-19 13:36:27', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (28, 1, 101, NULL, 'D', '在线练习错误', '2025-08-19 13:36:27', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (29, 1, 92, NULL, '瞧得起我', '在线练习错误', '2025-08-19 15:29:10', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (30, 1, 96, NULL, '123123123', '在线练习错误', '2025-08-19 15:29:10', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (31, 1, 78, NULL, 'B', '在线练习错误', '2025-11-07 14:07:44', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (32, 1, 85, NULL, 'C', '在线练习错误', '2025-11-07 14:07:44', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (33, 1, 102, NULL, 'C', '在线练习错误', '2025-11-07 14:07:44', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (34, 1, 76, NULL, '请问请问2', '在线练习错误', '2025-11-07 15:02:16', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (35, 1, 99, NULL, 'C', '在线练习错误', '2025-11-07 15:02:16', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (36, 1, 112, NULL, '打算的2312312', '在线练习错误', '2025-11-07 15:02:16', 1, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (37, 1, 17, 6, 'A', '模拟考试错误', '2025-11-20 17:28:02', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (38, 1, 16, 6, 'B', '模拟考试错误', '2025-11-20 17:28:02', 1, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (39, 1, 63, 7, 'A', '模拟考试错误', '2025-11-27 16:25:39', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (40, 1, 65, 7, 'C', '模拟考试错误', '2025-11-27 16:25:39', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (41, 1, 66, 7, 'C', '模拟考试错误', '2025-11-27 16:25:39', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (42, 1, 62, 7, 'B', '模拟考试错误', '2025-11-27 16:25:39', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (43, 1, 112, 7, '发多少积分骄傲是的理解', '模拟考试错误', '2025-11-27 16:25:39', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (44, 1, 58, 7, 'D', '模拟考试错误', '2025-11-27 16:26:03', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (45, 1, 88, NULL, 'were', '在线练习错误', '2025-11-27 21:37:56', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (46, 1, 93, NULL, 'studied', '在线练习错误', '2025-11-27 21:37:56', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (47, 1, 94, NULL, 'is', '在线练习错误', '2025-11-27 21:37:56', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (48, 1, 98, NULL, 'i dont know ', '在线练习错误', '2025-11-27 21:37:56', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (49, 1, 86, NULL, 'B', '在线练习错误', '2025-12-02 15:07:41', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (50, 1, 90, NULL, '123123', '在线练习错误', '2025-12-02 15:07:41', 0, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (51, 1, 91, NULL, '123123', '在线练习错误', '2025-12-02 15:07:41', 1, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (52, 1, 97, NULL, '13241235125', '在线练习错误', '2025-12-02 15:07:41', 1, 0, '2025-12-02 17:34:02');
INSERT INTO `biz_wrong_record` VALUES (53, 1, 117, NULL, '爱上对方噶说过的', '在线练习错误', '2025-12-03 21:28:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (54, 1, 123, NULL, '安师大三个方法', '在线练习错误', '2025-12-03 21:28:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (55, 1, 125, NULL, '按时发大水电饭锅', '在线练习错误', '2025-12-03 21:28:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (56, 1, 149, NULL, 'B', '在线练习错误', '2025-12-03 21:28:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (57, 1, 152, NULL, 'B', '在线练习错误', '2025-12-03 21:28:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (58, 1, 165, NULL, 'T', '在线练习错误', '2025-12-03 21:28:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (59, 1, 173, NULL, '撒地瓜粉电话给', '在线练习错误', '2025-12-03 21:28:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (60, 1, 129, NULL, '不知道', '在线练习错误', '2025-12-04 18:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (61, 1, 131, NULL, '不知道', '在线练习错误', '2025-12-04 18:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (62, 1, 137, NULL, 'C', '在线练习错误', '2025-12-04 18:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (63, 1, 141, NULL, 'B', '在线练习错误', '2025-12-04 18:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (64, 1, 151, NULL, 'C', '在线练习错误', '2025-12-04 18:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (65, 1, 171, NULL, 'T', '在线练习错误', '2025-12-04 18:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (66, 1, 176, NULL, '不知道', '在线练习错误', '2025-12-04 18:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (67, 1, 178, NULL, '不知道', '在线练习错误', '2025-12-04 18:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (68, 1, 144, NULL, 'C', '在线练习错误', '2025-12-05 18:53:41', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (69, 1, 145, NULL, 'A', '在线练习错误', '2025-12-05 18:53:41', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (70, 1, 147, NULL, 'D', '在线练习错误', '2025-12-05 20:51:21', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (71, 1, 168, NULL, 'T', '在线练习错误', '2025-12-05 20:51:21', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (72, 1, 172, NULL, 'T', '在线练习错误', '2025-12-05 20:51:21', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (73, 1, 174, NULL, '偷', '在线练习错误', '2025-12-05 21:10:00', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (74, 1, 140, NULL, 'C', '在线练习错误', '2025-12-05 22:11:37', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (75, 1, 119, NULL, 'T', '在线练习错误', '2025-12-05 22:11:59', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (76, 1, 120, NULL, '32423', '在线练习错误', '2025-12-05 22:11:59', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (77, 1, 135, NULL, 'C', '在线练习错误', '2025-12-05 22:11:59', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (78, 1, 138, NULL, 'B', '在线练习错误', '2025-12-05 22:11:59', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (79, 1, 155, NULL, '423423', '在线练习错误', '2025-12-05 22:11:59', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (80, 1, 169, NULL, 'T', '在线练习错误', '2025-12-05 22:11:59', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (81, 1, 2258, NULL, 'D', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (82, 1, 2264, NULL, 'A,B,C', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (83, 1, 2265, NULL, 'A,B,C,D', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (84, 1, 2266, NULL, 'A,C', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (85, 1, 2267, NULL, 'A,B,D', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (86, 1, 2268, NULL, 'A,C,D', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (87, 1, 2269, NULL, 'F', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (88, 1, 2270, NULL, 'T', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (89, 1, 2271, NULL, 'T', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (90, 1, 2272, NULL, 'T', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (91, 1, 2273, NULL, 'F', '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (92, 1, 2274, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (93, 1, 2275, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (94, 1, 2276, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (95, 1, 2277, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (96, 1, 2278, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (97, 1, 2279, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (98, 1, 2280, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (99, 1, 2281, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (100, 1, 2282, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (101, 1, 2283, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:40:15', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (102, 1, 2254, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 1, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (103, 1, 2255, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (104, 1, 2256, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (105, 1, 2257, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (106, 1, 2259, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (107, 1, 2260, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (108, 1, 2261, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (109, 1, 2262, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 0, 0, NULL);
INSERT INTO `biz_wrong_record` VALUES (110, 1, 2263, NULL, NULL, '模拟考试《原神考据》中答错', '2026-01-09 20:50:03', 0, 0, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 6444 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
INSERT INTO `sys_login_log` VALUES (6324, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-04 18:13:56');
INSERT INTO `sys_login_log` VALUES (6325, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-04 22:07:40');
INSERT INTO `sys_login_log` VALUES (6326, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-04 22:23:30');
INSERT INTO `sys_login_log` VALUES (6327, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-04 22:27:11');
INSERT INTO `sys_login_log` VALUES (6328, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-04 22:28:50');
INSERT INTO `sys_login_log` VALUES (6329, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-04 22:29:09');
INSERT INTO `sys_login_log` VALUES (6330, 'admin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-04 22:30:53');
INSERT INTO `sys_login_log` VALUES (6331, '12345', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-04 22:31:21');
INSERT INTO `sys_login_log` VALUES (6332, 'admin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-05 12:34:56');
INSERT INTO `sys_login_log` VALUES (6333, '12345', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-05 12:37:19');
INSERT INTO `sys_login_log` VALUES (6334, '12345', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-05 15:08:19');
INSERT INTO `sys_login_log` VALUES (6335, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-05 15:08:31');
INSERT INTO `sys_login_log` VALUES (6336, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36', '2025-12-05 22:40:38');
INSERT INTO `sys_login_log` VALUES (6337, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-19 17:30:48');
INSERT INTO `sys_login_log` VALUES (6338, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-29 15:20:07');
INSERT INTO `sys_login_log` VALUES (6339, 'admin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-29 15:21:08');
INSERT INTO `sys_login_log` VALUES (6340, 'admin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 18:19:21');
INSERT INTO `sys_login_log` VALUES (6341, '123', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 18:19:39');
INSERT INTO `sys_login_log` VALUES (6342, 'admin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:03:43');
INSERT INTO `sys_login_log` VALUES (6343, 'admin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:03:44');
INSERT INTO `sys_login_log` VALUES (6344, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:04:00');
INSERT INTO `sys_login_log` VALUES (6345, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:06:32');
INSERT INTO `sys_login_log` VALUES (6346, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:06:42');
INSERT INTO `sys_login_log` VALUES (6347, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:06:43');
INSERT INTO `sys_login_log` VALUES (6348, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:06:50');
INSERT INTO `sys_login_log` VALUES (6349, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:07:03');
INSERT INTO `sys_login_log` VALUES (6350, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:20');
INSERT INTO `sys_login_log` VALUES (6351, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:27');
INSERT INTO `sys_login_log` VALUES (6352, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:28');
INSERT INTO `sys_login_log` VALUES (6353, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:28');
INSERT INTO `sys_login_log` VALUES (6354, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:28');
INSERT INTO `sys_login_log` VALUES (6355, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:34');
INSERT INTO `sys_login_log` VALUES (6356, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:41');
INSERT INTO `sys_login_log` VALUES (6357, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:45');
INSERT INTO `sys_login_log` VALUES (6358, 'superadmin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:47');
INSERT INTO `sys_login_log` VALUES (6359, 'admin', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:08:55');
INSERT INTO `sys_login_log` VALUES (6360, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:09:30');
INSERT INTO `sys_login_log` VALUES (6361, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:09:43');
INSERT INTO `sys_login_log` VALUES (6362, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 19:12:00');
INSERT INTO `sys_login_log` VALUES (6363, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-05 21:20:47');
INSERT INTO `sys_login_log` VALUES (6364, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-06 18:11:11');
INSERT INTO `sys_login_log` VALUES (6365, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-06 20:12:10');
INSERT INTO `sys_login_log` VALUES (6366, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-06 22:54:00');
INSERT INTO `sys_login_log` VALUES (6367, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-06 22:59:07');
INSERT INTO `sys_login_log` VALUES (6368, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-07 08:48:34');
INSERT INTO `sys_login_log` VALUES (6369, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-08 09:49:15');
INSERT INTO `sys_login_log` VALUES (6370, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-08 12:14:36');
INSERT INTO `sys_login_log` VALUES (6371, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-08 12:14:37');
INSERT INTO `sys_login_log` VALUES (6372, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-08 22:58:03');
INSERT INTO `sys_login_log` VALUES (6373, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-08 22:58:04');
INSERT INTO `sys_login_log` VALUES (6374, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-08 23:00:44');
INSERT INTO `sys_login_log` VALUES (6375, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 12:10:47');
INSERT INTO `sys_login_log` VALUES (6376, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 12:11:19');
INSERT INTO `sys_login_log` VALUES (6377, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 12:11:42');
INSERT INTO `sys_login_log` VALUES (6378, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 13:01:24');
INSERT INTO `sys_login_log` VALUES (6379, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 13:52:31');
INSERT INTO `sys_login_log` VALUES (6380, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 13:52:32');
INSERT INTO `sys_login_log` VALUES (6381, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 14:15:34');
INSERT INTO `sys_login_log` VALUES (6382, 'teacher', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 14:15:40');
INSERT INTO `sys_login_log` VALUES (6383, 'teacher', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 14:15:46');
INSERT INTO `sys_login_log` VALUES (6384, 'teacher', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 14:15:50');
INSERT INTO `sys_login_log` VALUES (6385, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-09 14:15:56');
INSERT INTO `sys_login_log` VALUES (6386, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 12:12:35');
INSERT INTO `sys_login_log` VALUES (6387, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 12:12:37');
INSERT INTO `sys_login_log` VALUES (6388, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 12:40:53');
INSERT INTO `sys_login_log` VALUES (6389, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 12:40:55');
INSERT INTO `sys_login_log` VALUES (6390, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 14:33:23');
INSERT INTO `sys_login_log` VALUES (6391, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 18:46:56');
INSERT INTO `sys_login_log` VALUES (6392, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 18:47:35');
INSERT INTO `sys_login_log` VALUES (6393, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 18:47:37');
INSERT INTO `sys_login_log` VALUES (6394, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 20:03:23');
INSERT INTO `sys_login_log` VALUES (6395, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 20:03:39');
INSERT INTO `sys_login_log` VALUES (6396, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-10 20:04:01');
INSERT INTO `sys_login_log` VALUES (6397, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:25:01');
INSERT INTO `sys_login_log` VALUES (6398, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:41:43');
INSERT INTO `sys_login_log` VALUES (6399, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:41:45');
INSERT INTO `sys_login_log` VALUES (6400, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:41:48');
INSERT INTO `sys_login_log` VALUES (6401, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:42:58');
INSERT INTO `sys_login_log` VALUES (6402, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:43:00');
INSERT INTO `sys_login_log` VALUES (6403, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:43:03');
INSERT INTO `sys_login_log` VALUES (6404, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:51:43');
INSERT INTO `sys_login_log` VALUES (6405, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:52:16');
INSERT INTO `sys_login_log` VALUES (6406, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:52:59');
INSERT INTO `sys_login_log` VALUES (6407, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:54:16');
INSERT INTO `sys_login_log` VALUES (6408, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:54:20');
INSERT INTO `sys_login_log` VALUES (6409, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:54:26');
INSERT INTO `sys_login_log` VALUES (6410, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:58:04');
INSERT INTO `sys_login_log` VALUES (6411, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:58:07');
INSERT INTO `sys_login_log` VALUES (6412, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:58:07');
INSERT INTO `sys_login_log` VALUES (6413, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:58:07');
INSERT INTO `sys_login_log` VALUES (6414, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:59:09');
INSERT INTO `sys_login_log` VALUES (6415, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:59:10');
INSERT INTO `sys_login_log` VALUES (6416, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:59:12');
INSERT INTO `sys_login_log` VALUES (6417, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:59:15');
INSERT INTO `sys_login_log` VALUES (6418, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:59:16');
INSERT INTO `sys_login_log` VALUES (6419, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:59:18');
INSERT INTO `sys_login_log` VALUES (6420, '2215033133', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:59:32');
INSERT INTO `sys_login_log` VALUES (6421, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 15:59:35');
INSERT INTO `sys_login_log` VALUES (6422, '未知学生', '127.0.0.1', 'LOGIN_FAILURE', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:00:30');
INSERT INTO `sys_login_log` VALUES (6423, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:07:45');
INSERT INTO `sys_login_log` VALUES (6424, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:13:46');
INSERT INTO `sys_login_log` VALUES (6425, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:14:16');
INSERT INTO `sys_login_log` VALUES (6426, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:19:14');
INSERT INTO `sys_login_log` VALUES (6427, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:19:15');
INSERT INTO `sys_login_log` VALUES (6428, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:19:17');
INSERT INTO `sys_login_log` VALUES (6429, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:19:19');
INSERT INTO `sys_login_log` VALUES (6430, 'superadmin', '127.0.0.1', 'LOGOUT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:19:20');
INSERT INTO `sys_login_log` VALUES (6431, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:19:22');
INSERT INTO `sys_login_log` VALUES (6432, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:20:51');
INSERT INTO `sys_login_log` VALUES (6433, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:26:16');
INSERT INTO `sys_login_log` VALUES (6434, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 16:32:17');
INSERT INTO `sys_login_log` VALUES (6435, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 18:50:52');
INSERT INTO `sys_login_log` VALUES (6436, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 18:59:51');
INSERT INTO `sys_login_log` VALUES (6437, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 19:00:18');
INSERT INTO `sys_login_log` VALUES (6438, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 20:23:43');
INSERT INTO `sys_login_log` VALUES (6439, 'superadmin', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 20:28:42');
INSERT INTO `sys_login_log` VALUES (6440, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 20:29:01');
INSERT INTO `sys_login_log` VALUES (6441, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 20:45:05');
INSERT INTO `sys_login_log` VALUES (6442, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 21:06:18');
INSERT INTO `sys_login_log` VALUES (6443, '2215033133', '127.0.0.1', 'LOGIN_SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-11 21:10:04');

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
-- Records of sys_notification
-- ----------------------------
INSERT INTO `sys_notification` VALUES (1, '123123', '1231231', 1, '2025-08-11 13:47:20', '2025-08-11 13:47:20', 'ALL');
INSERT INTO `sys_notification` VALUES (2, '123124', '12412412412412挨过打都是发给', 1, '2025-08-11 14:26:07', '2025-08-11 14:25:58', 'ALL');
INSERT INTO `sys_notification` VALUES (3, '13241', '431241234', 1, '2025-08-12 11:41:45', '2025-08-12 11:41:45', 'ALL');
INSERT INTO `sys_notification` VALUES (4, '3124123412512', '231513251325', 1, '2025-08-12 11:41:49', '2025-08-12 11:41:49', 'ALL');
INSERT INTO `sys_notification` VALUES (6, '312312', '12312312312312', 1, '2025-11-07 15:36:40', '2025-11-07 15:36:40', 'ALL');
INSERT INTO `sys_notification` VALUES (7, '12312312421354123512345', '12351235231512', 1, '2025-11-07 15:58:19', '2025-11-07 15:54:05', 'ALL');
INSERT INTO `sys_notification` VALUES (8, '123', '123123', 1, '2025-12-02 21:28:39', '2025-12-02 21:28:39', 'ALL');
INSERT INTO `sys_notification` VALUES (10, '123123123', '12412412412', 1, '2025-12-02 21:58:58', '2025-12-02 21:58:58', 'ALL');
INSERT INTO `sys_notification` VALUES (11, '13213123', '123123214321', 1, '2025-12-02 22:10:11', '2025-12-02 22:10:11', 'STUDENT');
INSERT INTO `sys_notification` VALUES (12, '12412421', '412412', 1, '2025-12-02 22:10:23', '2025-12-02 22:10:22', 'STUDENT');
INSERT INTO `sys_notification` VALUES (14, '12451513245', '514354325', 0, NULL, '2025-12-02 22:10:54', 'TEACHER');
INSERT INTO `sys_notification` VALUES (15, '12345132453215', '231521512352315', 1, '2025-12-02 22:11:03', '2025-12-02 22:11:02', 'TEACHER');

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
) ENGINE = InnoDB AUTO_INCREMENT = 1099 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (1057, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:14:12');
INSERT INTO `sys_oper_log` VALUES (1058, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":98} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":', '{\"code\":400,\"msg\":\"分数变化较大，需要提供修改原因以提交审批\",\"total\":0}', 0, '', '2026-01-10 14:14:19');
INSERT INTO `sys_oper_log` VALUES (1059, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":98} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":', '{\"code\":400,\"msg\":\"分数变化较大，需要提供修改原因以提交审批\",\"total\":0}', 0, '', '2026-01-10 14:14:24');
INSERT INTO `sys_oper_log` VALUES (1060, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:18:44');
INSERT INTO `sys_oper_log` VALUES (1061, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:19:14');
INSERT INTO `sys_oper_log` VALUES (1062, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:19:24');
INSERT INTO `sys_oper_log` VALUES (1063, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:25:14');
INSERT INTO `sys_oper_log` VALUES (1064, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:25:18');
INSERT INTO `sys_oper_log` VALUES (1065, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:25:26');
INSERT INTO `sys_oper_log` VALUES (1066, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:25:28');
INSERT INTO `sys_oper_log` VALUES (1067, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":98,\"reason\":\"ai判断错误了\"} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:upd', '{\"code\":200,\"data\":{\"approvalId\":1,\"needsApproval\":true,\"message\":\"分数变化超过审批阈值，已提交审批，请等待审核\"},\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:25:46');
INSERT INTO `sys_oper_log` VALUES (1068, '审批管理', 2, 'com.ice.exebackend.controller.BizGradingApprovalController.reject()', 'PUT', 0, 'superadmin', '/api/v1/grading-approvals/1/reject', '127.0.0.1', '1 {\"approvalComment\":\"ai没有出错\"} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"', '{\"code\":200,\"data\":\"已驳回审批\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:26:04');
INSERT INTO `sys_oper_log` VALUES (1069, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:26:08');
INSERT INTO `sys_oper_log` VALUES (1070, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:26:30');
INSERT INTO `sys_oper_log` VALUES (1071, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:26:32');
INSERT INTO `sys_oper_log` VALUES (1072, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":11} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":', '{\"code\":200,\"data\":\"分数修改成功\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:26:59');
INSERT INTO `sys_oper_log` VALUES (1073, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":12} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":', '{\"code\":200,\"data\":\"分数修改成功\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:27:06');
INSERT INTO `sys_oper_log` VALUES (1074, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":13} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":', '{\"code\":200,\"data\":\"分数修改成功\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:27:11');
INSERT INTO `sys_oper_log` VALUES (1075, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":14} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":', '{\"code\":200,\"data\":\"分数修改成功\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:27:16');
INSERT INTO `sys_oper_log` VALUES (1076, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":98,\"reason\":\"123123\"} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:upda', '{\"code\":200,\"data\":{\"approvalId\":2,\"needsApproval\":true,\"message\":\"分数变化超过审批阈值，已提交审批，请等待审核\"},\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:27:43');
INSERT INTO `sys_oper_log` VALUES (1077, '审批管理', 2, 'com.ice.exebackend.controller.BizGradingApprovalController.approve()', 'PUT', 0, 'superadmin', '/api/v1/grading-approvals/2/approve', '127.0.0.1', '2 {\"approvalComment\":\"同意\"} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"', '{\"code\":200,\"data\":\"审批通过\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:28:00');
INSERT INTO `sys_oper_log` VALUES (1078, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:28:08');
INSERT INTO `sys_oper_log` VALUES (1079, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":91} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":', '{\"code\":200,\"data\":\"分数修改成功\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:28:16');
INSERT INTO `sys_oper_log` VALUES (1080, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":6,\"reason\":\"123123142\"} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:up', '{\"code\":200,\"data\":{\"approvalId\":3,\"needsApproval\":true,\"message\":\"分数变化超过审批阈值，已提交审批，请等待审核\"},\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:28:24');
INSERT INTO `sys_oper_log` VALUES (1081, '审批管理', 3, 'com.ice.exebackend.controller.BizGradingApprovalController.cancelApproval()', 'DELETE', 0, 'superadmin', '/api/v1/grading-approvals/3', '127.0.0.1', '', '{\"code\":200,\"data\":\"撤销成功\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:28:29');
INSERT INTO `sys_oper_log` VALUES (1082, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:28:31');
INSERT INTO `sys_oper_log` VALUES (1083, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:28:31');
INSERT INTO `sys_oper_log` VALUES (1084, '成绩批阅-修改分数', 2, 'com.ice.exebackend.controller.BizExamResultController.updateScore()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/38/score', '127.0.0.1', '38 {\"score\":98} {\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":', '{\"code\":200,\"data\":\"分数修改成功\",\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:28:41');
INSERT INTO `sys_oper_log` VALUES (1085, '成绩管理', 2, 'com.ice.exebackend.controller.BizExamResultController.batchPublish()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/batch/publish', '127.0.0.1', '{\"ids\":[34],\"published\":false}', '{\"code\":200,\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:29:55');
INSERT INTO `sys_oper_log` VALUES (1086, '成绩管理', 2, 'com.ice.exebackend.controller.BizExamResultController.batchPublish()', 'PUT', 0, 'superadmin', '/api/v1/exam-results/batch/publish', '127.0.0.1', '{\"ids\":[34],\"published\":true}', '{\"code\":200,\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:29:57');
INSERT INTO `sys_oper_log` VALUES (1087, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:30:08');
INSERT INTO `sys_oper_log` VALUES (1088, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:35:08');
INSERT INTO `sys_oper_log` VALUES (1089, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:39:28');
INSERT INTO `sys_oper_log` VALUES (1090, '通知管理', 2, 'com.ice.exebackend.controller.SysUserNotificationController.markAllAsRead()', 'PUT', 0, 'superadmin', '/api/v1/user-notifications/read-all', '127.0.0.1', '{\"authenticated\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"authority\":\"sys:log:login\"},{\"authority\":\"sys:log:oper\"},{\"authority\":\"sys:notify:list\"},{\"authority\":\"sys:paper:create\"},{\"authority\":\"sys:paper:delete\"},{\"authority\":\"sys:paper:list\"},{\"authority\":\"sys:paper:update\"},{\"authority\":\"sys:question:create\"},{\"authority\":\"sys:question:delete\"},{\"authority\":\"sys:question:list\"},{\"authority\":\"sys:question:update\"},{\"authority\":\"sys:role:perm\"},{\"authority\":\"sys:stats:list\"},{\"authority\":\"sys:student:create\"},{\"authority\":\"sys:student:delete\"},{\"authority\":\"sys:student:list\"},{\"authority\":\"sys:student:update\"},{\"authority\":\"sys:subject:create\"},{\"authority\":\"sys:subject:delete\"},{\"authority\":\"sys:subject:list\"},{\"authority\":\"sys:subject:update\"},{\"authority\":\"sys:user\"},{\"authority\":\"sys:user:create\"},{\"authority\":\"sys:user:delete\"},{\"authority\":\"sys:user:list\"},{\"authority\":\"sys:user:update\"},{\"authority\":\"sys:wrong:list\"}],\"details\":{\"remoteAddress\":\"0:0:0:0:0:0:0:1\"},\"name\":\"superadmin\",\"principal\":{\"accountNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ROLE_SUPER_ADMIN\"},{\"authority\":\"sys\"},{\"authority\":\"sys:class\"},{\"authority\":\"sys:class:delete\"},{\"authority\":\"sys:class:edit\"},{\"authority\":\"sys:class:list\"},{\"authority\":\"sys:course\"},{\"authority\":\"sys:course:delete\"},{\"authority\":\"sys:course:edit\"},{\"authority\":\"sys:course:list\"},{\"authority\":\"sys:home\"},{\"authority\":\"sys:kp:create\"},{\"authority\":\"sys:kp:delete\"},{\"authority\":\"sys:kp:list\"},{\"authority\":\"sys:kp:update\"},{\"authority\":\"sys:log\"},{\"aut', '{\"code\":200,\"data\":{\"count\":7},\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 14:39:45');
INSERT INTO `sys_oper_log` VALUES (1091, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 15:31:45');
INSERT INTO `sys_oper_log` VALUES (1092, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-10 15:31:50');
INSERT INTO `sys_oper_log` VALUES (1093, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-11 15:52:04');
INSERT INTO `sys_oper_log` VALUES (1094, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-11 16:19:35');
INSERT INTO `sys_oper_log` VALUES (1095, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-11 19:00:21');
INSERT INTO `sys_oper_log` VALUES (1096, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-11 20:22:14');
INSERT INTO `sys_oper_log` VALUES (1097, '班级管理', 0, 'com.ice.exebackend.controller.TeacherClassController.listClasses()', 'GET', 0, 'superadmin', '/api/v1/classes', '127.0.0.1', '', '{\"code\":200,\"data\":[{\"code\":\"939422\",\"createTime\":1764744829000,\"description\":\"(*∩_∩*)\",\"grade\":\"高三\",\"id\":1,\"name\":\"高三（1）班\",\"teacherId\":1}],\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-11 20:22:17');
INSERT INTO `sys_oper_log` VALUES (1098, '题库管理', 2, 'com.ice.exebackend.controller.BizQuestionController.updateQuestion()', 'PUT', 0, 'superadmin', '/api/v1/questions/2275', '127.0.0.1', '2275 {\"answer\":\"法涅斯，旅行者###空###莹\",\"content\":\"目前剧情中，已知的“降临者”共有四位。第一位是原初之神______，第四位是______。\",\"description\":\"降临者序列：第一法涅斯，第二（未知），第三（遗骸制神之心），第四旅行者。\",\"difficulty\":0.5,\"grade\":\"高三\",\"id\":2275,\"knowledgePointIds\":[],\"questionType\":3,\"subjectId\":82}', '{\"code\":200,\"msg\":\"成功\",\"total\":0}', 0, '', '2026-01-11 21:51:29');

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
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, '系统管理', 'sys', 1, 0);
INSERT INTO `sys_permission` VALUES (2, '成员管理', 'sys:user', 1, 1);
INSERT INTO `sys_permission` VALUES (10, '查看用户', 'sys:user:list', 2, 2);
INSERT INTO `sys_permission` VALUES (11, '新增用户', 'sys:user:create', 2, 2);
INSERT INTO `sys_permission` VALUES (12, '编辑用户', 'sys:user:update', 2, 2);
INSERT INTO `sys_permission` VALUES (13, '删除用户', 'sys:user:delete', 2, 2);
INSERT INTO `sys_permission` VALUES (14, '角色权限', 'sys:role:perm', 2, 2);
INSERT INTO `sys_permission` VALUES (20, '科目管理', 'sys:subject:list', 1, 0);
INSERT INTO `sys_permission` VALUES (21, '知识点管理', 'sys:kp:list', 1, 0);
INSERT INTO `sys_permission` VALUES (22, '题库管理', 'sys:question:list', 1, 0);
INSERT INTO `sys_permission` VALUES (23, '试卷管理', 'sys:paper:list', 1, 0);
INSERT INTO `sys_permission` VALUES (24, '学生管理', 'sys:student:list', 1, 0);
INSERT INTO `sys_permission` VALUES (25, '错题管理', 'sys:wrong:list', 1, 0);
INSERT INTO `sys_permission` VALUES (26, '教学统计', 'sys:stats:list', 1, 0);
INSERT INTO `sys_permission` VALUES (27, '通知管理', 'sys:notify:list', 1, 0);
INSERT INTO `sys_permission` VALUES (28, '工作台', 'sys:home', 1, 0);
INSERT INTO `sys_permission` VALUES (30, '日志管理', 'sys:log', 1, 1);
INSERT INTO `sys_permission` VALUES (31, '登录日志', 'sys:log:login', 2, 30);
INSERT INTO `sys_permission` VALUES (32, '删除题目', 'sys:question:delete', 2, 22);
INSERT INTO `sys_permission` VALUES (36, '新增科目', 'sys:subject:create', 2, 20);
INSERT INTO `sys_permission` VALUES (37, '编辑科目', 'sys:subject:update', 2, 20);
INSERT INTO `sys_permission` VALUES (38, '删除科目', 'sys:subject:delete', 2, 20);
INSERT INTO `sys_permission` VALUES (39, '新增学生', 'sys:student:create', 2, 24);
INSERT INTO `sys_permission` VALUES (40, '编辑学生', 'sys:student:update', 2, 24);
INSERT INTO `sys_permission` VALUES (41, '删除学生', 'sys:student:delete', 2, 24);
INSERT INTO `sys_permission` VALUES (42, '新增知识点', 'sys:kp:create', 2, 21);
INSERT INTO `sys_permission` VALUES (43, '编辑知识点', 'sys:kp:update', 2, 21);
INSERT INTO `sys_permission` VALUES (44, '删除知识点', 'sys:kp:delete', 2, 21);
INSERT INTO `sys_permission` VALUES (60, '课程管理', 'sys:course', 1, 0);
INSERT INTO `sys_permission` VALUES (61, '查看课程', 'sys:course:list', 2, 60);
INSERT INTO `sys_permission` VALUES (63, '删除课程', 'sys:course:delete', 2, 60);
INSERT INTO `sys_permission` VALUES (70, '班级管理', 'sys:class', 1, 0);
INSERT INTO `sys_permission` VALUES (71, '查看班级', 'sys:class:list', 2, 70);
INSERT INTO `sys_permission` VALUES (72, '编辑班级', 'sys:class:edit', 2, 70);
INSERT INTO `sys_permission` VALUES (73, '删除班级', 'sys:class:delete', 2, 70);
INSERT INTO `sys_permission` VALUES (100, '新增试卷', 'sys:paper:create', 2, 23);
INSERT INTO `sys_permission` VALUES (101, '编辑试卷', 'sys:paper:update', 2, 23);
INSERT INTO `sys_permission` VALUES (102, '删除试卷', 'sys:paper:delete', 2, 23);
INSERT INTO `sys_permission` VALUES (103, '编辑课程', 'sys:course:edit', 1, 0);
INSERT INTO `sys_permission` VALUES (104, '操作日志', 'sys:log:oper', 1, 1);
INSERT INTO `sys_permission` VALUES (105, '创建题目', 'sys:question:create', 2, 22);
INSERT INTO `sys_permission` VALUES (106, '修改题目', 'sys:question:update', 2, 22);

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
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', '拥有系统所有权限，不受限制');
INSERT INTO `sys_role` VALUES (2, '普通管理员', 'ADMIN', '拥有大部分管理权限');
INSERT INTO `sys_role` VALUES (3, '教师', 'TEACHER', '拥有教学相关权限（题库、试卷、成绩）');
INSERT INTO `sys_role` VALUES (4, '校长', 'PRINCIPAL', '拥有系统最高决策和监督权限');
INSERT INTO `sys_role` VALUES (5, '教导主任', 'DEAN', '拥有教学管理和部分系统管理权限');
INSERT INTO `sys_role` VALUES (6, '普通用户', 'USER', '注册用户的默认角色');

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
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1);
INSERT INTO `sys_role_permission` VALUES (1, 2);
INSERT INTO `sys_role_permission` VALUES (1, 10);
INSERT INTO `sys_role_permission` VALUES (1, 11);
INSERT INTO `sys_role_permission` VALUES (1, 12);
INSERT INTO `sys_role_permission` VALUES (1, 13);
INSERT INTO `sys_role_permission` VALUES (1, 14);
INSERT INTO `sys_role_permission` VALUES (1, 20);
INSERT INTO `sys_role_permission` VALUES (1, 21);
INSERT INTO `sys_role_permission` VALUES (1, 22);
INSERT INTO `sys_role_permission` VALUES (1, 23);
INSERT INTO `sys_role_permission` VALUES (1, 24);
INSERT INTO `sys_role_permission` VALUES (1, 25);
INSERT INTO `sys_role_permission` VALUES (1, 26);
INSERT INTO `sys_role_permission` VALUES (1, 27);
INSERT INTO `sys_role_permission` VALUES (1, 28);
INSERT INTO `sys_role_permission` VALUES (1, 30);
INSERT INTO `sys_role_permission` VALUES (1, 31);
INSERT INTO `sys_role_permission` VALUES (1, 32);
INSERT INTO `sys_role_permission` VALUES (1, 36);
INSERT INTO `sys_role_permission` VALUES (1, 37);
INSERT INTO `sys_role_permission` VALUES (1, 38);
INSERT INTO `sys_role_permission` VALUES (1, 39);
INSERT INTO `sys_role_permission` VALUES (1, 40);
INSERT INTO `sys_role_permission` VALUES (1, 41);
INSERT INTO `sys_role_permission` VALUES (1, 42);
INSERT INTO `sys_role_permission` VALUES (1, 43);
INSERT INTO `sys_role_permission` VALUES (1, 44);
INSERT INTO `sys_role_permission` VALUES (1, 60);
INSERT INTO `sys_role_permission` VALUES (1, 61);
INSERT INTO `sys_role_permission` VALUES (1, 63);
INSERT INTO `sys_role_permission` VALUES (1, 70);
INSERT INTO `sys_role_permission` VALUES (1, 71);
INSERT INTO `sys_role_permission` VALUES (1, 72);
INSERT INTO `sys_role_permission` VALUES (1, 73);
INSERT INTO `sys_role_permission` VALUES (1, 100);
INSERT INTO `sys_role_permission` VALUES (1, 101);
INSERT INTO `sys_role_permission` VALUES (1, 102);
INSERT INTO `sys_role_permission` VALUES (1, 103);
INSERT INTO `sys_role_permission` VALUES (1, 104);
INSERT INTO `sys_role_permission` VALUES (1, 105);
INSERT INTO `sys_role_permission` VALUES (1, 106);
INSERT INTO `sys_role_permission` VALUES (2, 1);
INSERT INTO `sys_role_permission` VALUES (2, 2);
INSERT INTO `sys_role_permission` VALUES (2, 10);
INSERT INTO `sys_role_permission` VALUES (2, 11);
INSERT INTO `sys_role_permission` VALUES (2, 12);
INSERT INTO `sys_role_permission` VALUES (2, 13);
INSERT INTO `sys_role_permission` VALUES (2, 14);
INSERT INTO `sys_role_permission` VALUES (2, 20);
INSERT INTO `sys_role_permission` VALUES (2, 21);
INSERT INTO `sys_role_permission` VALUES (2, 22);
INSERT INTO `sys_role_permission` VALUES (2, 23);
INSERT INTO `sys_role_permission` VALUES (2, 24);
INSERT INTO `sys_role_permission` VALUES (2, 25);
INSERT INTO `sys_role_permission` VALUES (2, 26);
INSERT INTO `sys_role_permission` VALUES (2, 27);
INSERT INTO `sys_role_permission` VALUES (2, 30);
INSERT INTO `sys_role_permission` VALUES (2, 31);
INSERT INTO `sys_role_permission` VALUES (2, 32);
INSERT INTO `sys_role_permission` VALUES (2, 33);
INSERT INTO `sys_role_permission` VALUES (2, 34);
INSERT INTO `sys_role_permission` VALUES (2, 35);
INSERT INTO `sys_role_permission` VALUES (2, 36);
INSERT INTO `sys_role_permission` VALUES (2, 37);
INSERT INTO `sys_role_permission` VALUES (2, 38);
INSERT INTO `sys_role_permission` VALUES (2, 39);
INSERT INTO `sys_role_permission` VALUES (2, 40);
INSERT INTO `sys_role_permission` VALUES (2, 41);
INSERT INTO `sys_role_permission` VALUES (2, 42);
INSERT INTO `sys_role_permission` VALUES (2, 43);
INSERT INTO `sys_role_permission` VALUES (2, 44);
INSERT INTO `sys_role_permission` VALUES (2, 50);
INSERT INTO `sys_role_permission` VALUES (2, 51);
INSERT INTO `sys_role_permission` VALUES (2, 52);
INSERT INTO `sys_role_permission` VALUES (2, 60);
INSERT INTO `sys_role_permission` VALUES (2, 61);
INSERT INTO `sys_role_permission` VALUES (2, 62);
INSERT INTO `sys_role_permission` VALUES (2, 63);
INSERT INTO `sys_role_permission` VALUES (2, 70);
INSERT INTO `sys_role_permission` VALUES (2, 71);
INSERT INTO `sys_role_permission` VALUES (2, 72);
INSERT INTO `sys_role_permission` VALUES (2, 73);
INSERT INTO `sys_role_permission` VALUES (3, 20);
INSERT INTO `sys_role_permission` VALUES (3, 21);
INSERT INTO `sys_role_permission` VALUES (3, 22);
INSERT INTO `sys_role_permission` VALUES (3, 23);
INSERT INTO `sys_role_permission` VALUES (3, 24);
INSERT INTO `sys_role_permission` VALUES (3, 25);
INSERT INTO `sys_role_permission` VALUES (3, 26);
INSERT INTO `sys_role_permission` VALUES (3, 30);
INSERT INTO `sys_role_permission` VALUES (3, 31);
INSERT INTO `sys_role_permission` VALUES (3, 32);
INSERT INTO `sys_role_permission` VALUES (3, 33);
INSERT INTO `sys_role_permission` VALUES (3, 34);
INSERT INTO `sys_role_permission` VALUES (3, 35);
INSERT INTO `sys_role_permission` VALUES (3, 42);
INSERT INTO `sys_role_permission` VALUES (3, 43);
INSERT INTO `sys_role_permission` VALUES (3, 44);
INSERT INTO `sys_role_permission` VALUES (3, 60);
INSERT INTO `sys_role_permission` VALUES (3, 61);
INSERT INTO `sys_role_permission` VALUES (3, 62);
INSERT INTO `sys_role_permission` VALUES (3, 63);
INSERT INTO `sys_role_permission` VALUES (3, 70);
INSERT INTO `sys_role_permission` VALUES (3, 71);
INSERT INTO `sys_role_permission` VALUES (3, 72);
INSERT INTO `sys_role_permission` VALUES (3, 73);
INSERT INTO `sys_role_permission` VALUES (4, 1);
INSERT INTO `sys_role_permission` VALUES (4, 2);
INSERT INTO `sys_role_permission` VALUES (4, 10);
INSERT INTO `sys_role_permission` VALUES (4, 11);
INSERT INTO `sys_role_permission` VALUES (4, 12);
INSERT INTO `sys_role_permission` VALUES (4, 13);
INSERT INTO `sys_role_permission` VALUES (4, 20);
INSERT INTO `sys_role_permission` VALUES (4, 21);
INSERT INTO `sys_role_permission` VALUES (4, 22);
INSERT INTO `sys_role_permission` VALUES (4, 23);
INSERT INTO `sys_role_permission` VALUES (4, 24);
INSERT INTO `sys_role_permission` VALUES (4, 25);
INSERT INTO `sys_role_permission` VALUES (4, 26);
INSERT INTO `sys_role_permission` VALUES (4, 27);
INSERT INTO `sys_role_permission` VALUES (4, 28);
INSERT INTO `sys_role_permission` VALUES (5, 24);
INSERT INTO `sys_role_permission` VALUES (5, 25);
INSERT INTO `sys_role_permission` VALUES (5, 26);
INSERT INTO `sys_role_permission` VALUES (5, 27);
INSERT INTO `sys_role_permission` VALUES (5, 28);
INSERT INTO `sys_role_permission` VALUES (6, 28);

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
-- Records of sys_todo_config
-- ----------------------------
INSERT INTO `sys_todo_config` VALUES (1, 'pending_papers', '待批改试卷', 'Document', 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 'score-manage?status=1', 'getPendingPapersCount', 1, 10, '学生提交但尚未批改的试卷', '2026-01-10 12:37:22', '2026-01-10 12:37:22');
INSERT INTO `sys_todo_config` VALUES (2, 'pending_questions', '待审核题目', 'Files', 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', 'questions', 'getPendingQuestionsCount', 1, 20, '教师导入或创建但尚未审核的题目', '2026-01-10 12:37:22', '2026-01-10 12:37:22');

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
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'superadmin', '$2a$10$gwd4RBQ0KctPyK/TZSxyuuCBnsNl3xLKFuhzkEC3mYiKkAzhJthgS', '系统管理员', 1, 0, '2025-08-07 11:21:11', '2026-01-05 19:02:52', '/api/v1/files/acb101f3-7141-4cdf-8955-288048a3d1ea.jpg');
INSERT INTO `sys_user` VALUES (2, 'admin', '$2a$10$gwd4RBQ0KctPyK/TZSxyuuCBnsNl3xLKFuhzkEC3mYiKkAzhJthgS', '管理员', 1, 0, '2025-08-07 13:46:44', '2026-01-05 19:02:52', '/api/v1/files/ea364b07-eb03-4539-b639-ea2e691bd5de.png');
INSERT INTO `sys_user` VALUES (3, 'teacher', '$2a$10$gwd4RBQ0KctPyK/TZSxyuuCBnsNl3xLKFuhzkEC3mYiKkAzhJthgS', '老师', 1, 0, '2025-08-07 15:58:33', '2026-01-05 19:02:52', '/api/v1/files/7b33e11c-2f51-40d6-b693-f79db6d8ca06.png');
INSERT INTO `sys_user` VALUES (4, 'admin4', '$2a$10$olepTzuQsT.0qltPW32Rg.dJp4WNVZZjHP1UHtfXBtm74AAP0ctau', '校长', 1, 0, '2025-08-13 09:03:03', '2025-08-13 09:30:03', NULL);
INSERT INTO `sys_user` VALUES (5, 'admin5', '$2a$10$lS3cFWQrMdQOzsjHYX/7bupGp20eplca3zjyd33f.M3MZmCK/o2yu', '用户', 1, 0, '2025-08-13 10:42:15', '2025-08-13 10:42:15', NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户通知记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_notification
-- ----------------------------
INSERT INTO `sys_user_notification` VALUES (1, 1, '评语已更新', '您的成绩评语已由 系统管理员 更新', 'COMMENT_UPDATE', 38, 1, '2026-01-10 14:07:32', '2026-01-10 14:39:45');
INSERT INTO `sys_user_notification` VALUES (2, 1, '成绩已更新', '您的成绩已由 系统管理员 更新：10 → 11', 'SCORE_UPDATE', 38, 1, '2026-01-10 14:26:59', '2026-01-10 14:39:45');
INSERT INTO `sys_user_notification` VALUES (3, 1, '成绩已更新', '您的成绩已由 系统管理员 更新：11 → 12', 'SCORE_UPDATE', 38, 1, '2026-01-10 14:27:06', '2026-01-10 14:39:45');
INSERT INTO `sys_user_notification` VALUES (4, 1, '成绩已更新', '您的成绩已由 系统管理员 更新：12 → 13', 'SCORE_UPDATE', 38, 1, '2026-01-10 14:27:11', '2026-01-10 14:39:45');
INSERT INTO `sys_user_notification` VALUES (5, 1, '成绩已更新', '您的成绩已由 系统管理员 更新：13 → 14', 'SCORE_UPDATE', 38, 1, '2026-01-10 14:27:16', '2026-01-10 14:39:45');
INSERT INTO `sys_user_notification` VALUES (6, 1, '成绩已更新', '您的成绩已由 系统管理员 更新：98 → 91', 'SCORE_UPDATE', 38, 1, '2026-01-10 14:28:16', '2026-01-10 14:39:45');
INSERT INTO `sys_user_notification` VALUES (7, 1, '成绩已更新', '您的成绩已由 系统管理员 更新：91 → 98', 'SCORE_UPDATE', 38, 1, '2026-01-10 14:28:41', '2026-01-10 14:39:45');

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户个性化配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_preference
-- ----------------------------
INSERT INTO `sys_user_preference` VALUES (3, 1, 'dashboard_layout', '{\"layoutVersion\":\"1.0\",\"modules\":[{\"id\":\"quick-actions\",\"title\":\"快捷操作\",\"enabled\":true,\"order\":1,\"width\":24},{\"id\":\"stat-cards\",\"title\":\"统计卡片\",\"enabled\":true,\"order\":2,\"width\":24},{\"id\":\"todo-list\",\"title\":\"待办事项\",\"enabled\":true,\"order\":3,\"width\":24},{\"id\":\"notifications\",\"title\":\"系统通知\",\"enabled\":true,\"order\":4,\"width\":24},{\"id\":\"charts\",\"title\":\"数据图表\",\"enabled\":true,\"order\":5,\"width\":24},{\"id\":\"activities\",\"title\":\"最近活动\",\"enabled\":true,\"order\":6,\"width\":24}],\"theme\":\"light\",\"compactMode\":false}', '2026-01-11 23:24:32', '2026-01-11 23:24:32');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);
INSERT INTO `sys_user_role` VALUES (3, 3);
INSERT INTO `sys_user_role` VALUES (4, 4);
INSERT INTO `sys_user_role` VALUES (5, 6);

SET FOREIGN_KEY_CHECKS = 1;
