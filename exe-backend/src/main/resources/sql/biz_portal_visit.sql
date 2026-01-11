-- Portal访问记录表
CREATE TABLE IF NOT EXISTS `biz_portal_visit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_id` VARCHAR(50) NOT NULL COMMENT '系统ID',
  `system_name` VARCHAR(100) NOT NULL COMMENT '系统名称',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `user_type` VARCHAR(20) DEFAULT NULL COMMENT '用户类型: admin/teacher/student',
  `visit_time` DATETIME NOT NULL COMMENT '访问时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_system_id` (`system_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_visit_time` (`visit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Portal访问记录表';
