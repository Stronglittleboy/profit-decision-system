CREATE TABLE IF NOT EXISTS `budget` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `period`           VARCHAR(7)    NOT NULL COMMENT '预算月份 yyyy-MM',
  `category`         VARCHAR(20)   NOT NULL COMMENT 'income/fixed_cost/variable_cost',
  `planned_amount`   DECIMAL(15,2) NOT NULL COMMENT '预算金额',
  `actual_amount`    DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '实际金额(冗余,由定时刷新)',
  `status`           VARCHAR(20)   NOT NULL DEFAULT 'draft' COMMENT 'draft/approved',
  `remark`           VARCHAR(500)  DEFAULT NULL,
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_period_category` (`period`, `category`),
  KEY `idx_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算';
