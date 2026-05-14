-- 项目
CREATE TABLE IF NOT EXISTS `project` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `code`         VARCHAR(50)   NOT NULL COMMENT '项目编号',
  `name`         VARCHAR(100)  NOT NULL COMMENT '项目名称',
  `status`       VARCHAR(20)   NOT NULL DEFAULT 'planning' COMMENT '状态: planning/executing/completed/suspended',
  `budget`       DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '总预算',
  `start_date`   DATE          DEFAULT NULL COMMENT '计划开始日期',
  `end_date`     DATE          DEFAULT NULL COMMENT '计划结束日期',
  `manager`      VARCHAR(50)   DEFAULT NULL COMMENT '项目经理',
  `description`  VARCHAR(500)  DEFAULT NULL COMMENT '项目描述',
  `enabled`      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '启用状态',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目';
