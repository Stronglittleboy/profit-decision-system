CREATE TABLE IF NOT EXISTS `payment_record` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `biz_type`       VARCHAR(20)   NOT NULL COMMENT 'receivable/payable',
  `biz_id`         BIGINT        NOT NULL COMMENT '应收/应付ID',
  `amount`         DECIMAL(15,2) NOT NULL COMMENT '本次金额',
  `pay_date`       DATE          NOT NULL COMMENT '收/付款日期',
  `remark`         VARCHAR(500)  DEFAULT NULL,
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`, `biz_id`),
  KEY `idx_pay_date` (`pay_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收付款流水';
