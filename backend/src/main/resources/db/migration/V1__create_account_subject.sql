-- 会计科目表
CREATE TABLE IF NOT EXISTS `account_subject` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `code`         VARCHAR(50)   NOT NULL COMMENT '科目编码',
  `name`         VARCHAR(100)  NOT NULL COMMENT '科目名称',
  `parent_id`    BIGINT        DEFAULT NULL COMMENT '父科目ID',
  `level`        INT           NOT NULL DEFAULT 1 COMMENT '科目层级',
  `type`         VARCHAR(20)   NOT NULL COMMENT '科目类型: asset/liability/equity/cost/profit_loss',
  `debit_credit` VARCHAR(20)   NOT NULL COMMENT '借贷方向: debit/credit',
  `enabled`      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '启用状态: 1=启用, 0=停用',
  `sort`         INT           NOT NULL DEFAULT 0 COMMENT '排序',
  `remark`       VARCHAR(200)  DEFAULT NULL COMMENT '备注',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会计科目';
