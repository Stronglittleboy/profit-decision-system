-- 合同
CREATE TABLE IF NOT EXISTS `contract` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `code`             VARCHAR(50)   NOT NULL COMMENT '合同编号',
  `name`             VARCHAR(200)  NOT NULL COMMENT '合同名称',
  `counterparty_id`  BIGINT        NOT NULL COMMENT '签约往来方',
  `project_id`       BIGINT        DEFAULT NULL COMMENT '关联项目',
  `type`             VARCHAR(20)   NOT NULL COMMENT '类型: sales/purchase/service',
  `amount`           DECIMAL(15,2) NOT NULL COMMENT '合同金额',
  `sign_date`        DATE          DEFAULT NULL COMMENT '签约日期',
  `start_date`       DATE          DEFAULT NULL COMMENT '生效日期',
  `end_date`         DATE          DEFAULT NULL COMMENT '到期日期',
  `status`           VARCHAR(20)   NOT NULL DEFAULT 'draft' COMMENT '状态: draft/active/completed/terminated',
  `remark`           VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_counterparty` (`counterparty_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同';
