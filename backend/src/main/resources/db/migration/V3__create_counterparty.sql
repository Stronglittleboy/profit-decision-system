-- 往来方（客户/供应商）
CREATE TABLE IF NOT EXISTS `counterparty` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(100)  NOT NULL COMMENT '名称',
  `type`         VARCHAR(20)   NOT NULL COMMENT '类型: customer/supplier/both',
  `contact`      VARCHAR(100)  DEFAULT NULL COMMENT '联系人',
  `phone`        VARCHAR(20)   DEFAULT NULL COMMENT '电话',
  `address`      VARCHAR(200)  DEFAULT NULL COMMENT '地址',
  `tax_no`       VARCHAR(50)   DEFAULT NULL COMMENT '税号',
  `credit_level` VARCHAR(10)   DEFAULT NULL COMMENT '信用等级: A/B/C/D',
  `enabled`      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '启用状态: 1=启用, 0=停用',
  `remark`       VARCHAR(200)  DEFAULT NULL COMMENT '备注',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='往来方（客户/供应商）';
