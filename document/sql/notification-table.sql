-- 用户通知记录表
-- 用于存储系统发送给用户的所有通知记录
-- v3.06

CREATE TABLE IF NOT EXISTS `sys_user_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `type` VARCHAR(20) NOT NULL COMMENT '通知类型: SCORE_UPDATE-成绩更新, COMMENT_UPDATE-评语更新, SYSTEM-系统通知',
  `related_id` BIGINT NULL COMMENT '关联业务ID（如成绩ID）',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` DATETIME NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_is_read` (`is_read`),
  INDEX `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知记录表';
