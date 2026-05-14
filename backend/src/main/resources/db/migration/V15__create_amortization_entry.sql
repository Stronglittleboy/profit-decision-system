CREATE TABLE IF NOT EXISTS `amortization_entry` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `fact_event_id`  BIGINT        NOT NULL COMMENT '来源收支记录',
  `period`         VARCHAR(7)    NOT NULL COMMENT '分摊月份 yyyy-MM',
  `amount`         DECIMAL(15,2) NOT NULL COMMENT '当月分摊金额',
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_fact_event` (`fact_event_id`),
  KEY `idx_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分摊明细';
