-- 收支事实
CREATE TABLE IF NOT EXISTS `fact_event` (
  `id`               BIGINT         NOT NULL AUTO_INCREMENT,
  `type`             VARCHAR(20)    NOT NULL COMMENT '类型: income/cost',
  `amount`           DECIMAL(15,2)  NOT NULL COMMENT '金额',
  `business_date`    DATE           NOT NULL COMMENT '业务发生日期',
  `accounting_date`  DATE           NOT NULL COMMENT '会计确认日期',
  `subject_id`       BIGINT         NOT NULL COMMENT '会计科目ID',
  `counterparty_id`  BIGINT         NOT NULL COMMENT '往来方ID',
  `cost_category`    VARCHAR(20)    DEFAULT NULL COMMENT '成本类别: fixed/variable/direct/indirect',
  `invoice_no`       VARCHAR(50)    DEFAULT NULL COMMENT '发票号',
  `status`           VARCHAR(20)    NOT NULL DEFAULT 'valid' COMMENT '状态: valid/reversed',
  `remark`           VARCHAR(500)   DEFAULT NULL COMMENT '备注',
  `created_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_accounting_date` (`accounting_date`),
  KEY `idx_subject` (`subject_id`),
  KEY `idx_counterparty` (`counterparty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收支事实';
