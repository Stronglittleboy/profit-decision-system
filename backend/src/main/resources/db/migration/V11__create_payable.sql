CREATE TABLE IF NOT EXISTS `payable` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `code`             VARCHAR(50)   NOT NULL COMMENT '单据编号',
  `counterparty_id`  BIGINT        NOT NULL COMMENT '供应商',
  `contract_id`      BIGINT        DEFAULT NULL COMMENT '关联合同',
  `amount`           DECIMAL(15,2) NOT NULL COMMENT '应付总额',
  `paid_amount`      DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已付金额',
  `due_date`         DATE          NOT NULL COMMENT '到期日',
  `status`           VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT 'pending/partial/paid/overdue',
  `remark`           VARCHAR(500)  DEFAULT NULL,
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_counterparty` (`counterparty_id`),
  KEY `idx_status` (`status`),
  KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应付账款';
