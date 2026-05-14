-- 种子数据：一级科目
INSERT INTO `account_subject` (`code`, `name`, `parent_id`, `level`, `type`, `debit_credit`, `enabled`, `sort`) VALUES
  ('1001', '库存现金',     NULL, 1, 'asset',       'debit',  1, 1),
  ('1002', '银行存款',     NULL, 1, 'asset',       'debit',  1, 2),
  ('1122', '应收账款',     NULL, 1, 'asset',       'debit',  1, 3),
  ('2202', '应付账款',     NULL, 1, 'liability',   'credit', 1, 4),
  ('4001', '主营业务收入', NULL, 1, 'profit_loss', 'credit', 1, 5),
  ('5001', '主营业务成本', NULL, 1, 'cost',        'debit',  1, 6),
  ('5401', '管理费用',     NULL, 1, 'profit_loss', 'debit',  1, 7),
  ('5402', '销售费用',     NULL, 1, 'profit_loss', 'debit',  1, 8);
