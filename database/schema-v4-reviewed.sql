-- ============================================
-- 利润决策系统 - 数据库表结构（v4.0 四方评审版）
-- 修复：会计合规 + 业务完善 + 产品优化
-- ============================================

-- 1. 事实域（Fact Context）
-- ============================================

CREATE TABLE fact_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  
  -- 时间维度（业务需求：区分业务时间和会计时间）
  business_date DATE NOT NULL COMMENT '业务发生日期',
  accounting_date DATE NOT NULL COMMENT '会计确认日期（用于利润计算）',
  cash_date DATE COMMENT '现金流日期',
  event_time DATETIME NOT NULL COMMENT '记录时间',
  
  -- 基础信息
  type VARCHAR(20) NOT NULL COMMENT '类型: income/cost/behavior',
  amount DECIMAL(15,2) NOT NULL COMMENT '金额',
  
  -- 🆕 会计合规字段（P0）
  account_subject_id BIGINT NOT NULL COMMENT '会计科目ID',
  voucher_no VARCHAR(50) COMMENT '凭证号',
  debit_credit VARCHAR(10) NOT NULL COMMENT '借贷方向: debit/credit',
  
  -- 成本分类（业务需求：支持不同分摊策略）
  cost_category VARCHAR(20) COMMENT '成本类别: fixed/variable/direct/indirect',
  
  -- 跨期分摊（业务需求：年度保险、季度房租）
  amortization_start DATE COMMENT '分摊开始日期',
  amortization_end DATE COMMENT '分摊结束日期',
  amortization_method VARCHAR(20) COMMENT '分摊方式: linear/actual_days',
  
  -- 关联维度
  actor_id BIGINT COMMENT '执行人ID',
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  counterparty_type VARCHAR(20) COMMENT '对手方类型: customer/supplier',
  counterparty_id BIGINT COMMENT '对手方ID（客户/供应商）',
  project_id BIGINT COMMENT '项目ID',
  
  -- 🆕 付款信息（P0）
  payment_method VARCHAR(20) COMMENT '付款方式: cash/bank_transfer/check/acceptance',
  
  -- 发票信息（业务需求：税务合规）
  invoice_no VARCHAR(50) COMMENT '发票号',
  invoice_date DATE COMMENT '开票日期',
  tax_rate DECIMAL(6,4) COMMENT '税率（修改精度：支持13%）',
  tax_amount DECIMAL(15,2) COMMENT '税额',
  
  -- 🆕 附件管理（P0）
  attachment_ids JSON COMMENT '附件ID列表',
  
  -- 业务关联
  reference_id VARCHAR(100) COMMENT '业务关联ID',
  metadata JSON COMMENT '扩展信息',
  
  -- 状态控制
  status VARCHAR(20) NOT NULL DEFAULT 'valid' COMMENT '状态: valid/reversed/attribution_failed',
  approval_status VARCHAR(20) DEFAULT 'approved' COMMENT '审批状态: pending/approved/rejected',
  version INT NOT NULL DEFAULT 1 COMMENT '版本号',
  source VARCHAR(20) NOT NULL COMMENT '来源: manual/agent/system',
  idempotency_key VARCHAR(100) COMMENT '幂等键',
  
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  INDEX idx_accounting_date (accounting_date),
  INDEX idx_org_unit (org_unit_id),
  INDEX idx_type_status (type, status),
  INDEX idx_counterparty (counterparty_type, counterparty_id),
  INDEX idx_project (project_id),
  INDEX idx_account_subject (account_subject_id),
  INDEX idx_voucher (voucher_no),
  UNIQUE KEY uk_idempotency (source, reference_id, idempotency_key)
) COMMENT='事实事件表（v4.0）';

-- 🆕 会计科目表（P0 - 会计合规必需）
CREATE TABLE account_subject (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL COMMENT '科目编码（如：6001）',
  name VARCHAR(100) NOT NULL COMMENT '科目名称（如：主营业务收入）',
  parent_id BIGINT COMMENT '父科目ID（支持多级科目）',
  level INT NOT NULL COMMENT '科目级别（1/2/3/4）',
  type VARCHAR(20) NOT NULL COMMENT '科目类型: asset/liability/equity/revenue/expense',
  debit_credit VARCHAR(10) NOT NULL COMMENT '余额方向: debit/credit',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code),
  INDEX idx_parent (parent_id),
  INDEX idx_type (type)
) COMMENT='会计科目表';

-- 🆕 记账凭证表（P0 - 会计合规必需）
CREATE TABLE voucher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  voucher_no VARCHAR(50) NOT NULL COMMENT '凭证号',
  voucher_date DATE NOT NULL COMMENT '凭证日期',
  period VARCHAR(20) NOT NULL COMMENT '会计期间（2026-05）',
  voucher_type VARCHAR(20) NOT NULL COMMENT '凭证类型: receipt/payment/transfer',
  total_debit DECIMAL(15,2) NOT NULL COMMENT '借方合计',
  total_credit DECIMAL(15,2) NOT NULL COMMENT '贷方合计',
  summary TEXT COMMENT '摘要',
  prepared_by BIGINT NOT NULL COMMENT '制单人',
  reviewed_by BIGINT COMMENT '审核人',
  approved_by BIGINT COMMENT '批准人',
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/reviewed/approved',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_voucher_no (voucher_no),
  INDEX idx_period (period),
  INDEX idx_date (voucher_date)
) COMMENT='记账凭证表';

-- 🆕 凭证明细表（P0 - 会计合规必需）
CREATE TABLE voucher_entry (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  voucher_id BIGINT NOT NULL COMMENT '凭证ID',
  line_no INT NOT NULL COMMENT '行号',
  account_subject_id BIGINT NOT NULL COMMENT '会计科目ID',
  debit_amount DECIMAL(15,2) DEFAULT 0 COMMENT '借方金额',
  credit_amount DECIMAL(15,2) DEFAULT 0 COMMENT '贷方金额',
  summary VARCHAR(200) COMMENT '摘要',
  fact_event_id BIGINT COMMENT '关联事实事件ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_voucher (voucher_id),
  INDEX idx_account (account_subject_id),
  INDEX idx_fact (fact_event_id)
) COMMENT='凭证明细表';

-- 2. 归因域（Attribution Context）
-- ============================================

CREATE TABLE attribution (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  fact_id BIGINT NOT NULL COMMENT '事实ID',
  attributed_to BIGINT NOT NULL COMMENT '归属对象ID',
  type VARCHAR(20) NOT NULL COMMENT '类型: income/cost',
  amount DECIMAL(15,2) NOT NULL COMMENT '归因金额',
  weight DECIMAL(5,4) DEFAULT 1.0000 COMMENT '权重',
  rule_id BIGINT COMMENT '规则ID',
  period VARCHAR(20) NOT NULL COMMENT '周期: 2026-05',
  batch_id VARCHAR(50) NOT NULL COMMENT '批次ID（保证原子性）',
  description TEXT COMMENT '🆕 归因说明（P1）',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_fact (fact_id),
  INDEX idx_attributed (attributed_to, period),
  INDEX idx_period (period),
  INDEX idx_batch (batch_id)
) COMMENT='归因表（v4.0）';

CREATE TABLE attribution_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '规则名称',
  rule_type VARCHAR(20) NOT NULL COMMENT '规则类型: direct/split/ratio',
  cost_category VARCHAR(20) COMMENT '适用成本类别',
  strategy VARCHAR(100) NOT NULL COMMENT '策略类名',
  params JSON COMMENT '策略参数',
  priority INT NOT NULL DEFAULT 0 COMMENT '优先级',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='归因规则表';

-- 3. 指标域（Metrics Context）
-- ============================================

CREATE TABLE metric_snapshot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  period VARCHAR(20) NOT NULL COMMENT '会计期间: 2026-05',
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  
  -- 核心指标
  revenue DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '收入',
  cost DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '成本',
  profit DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '利润',
  margin DECIMAL(5,4) COMMENT '利润率',
  roi DECIMAL(5,4) COMMENT '投资回报率',
  
  -- 成本结构
  fixed_cost DECIMAL(15,2) DEFAULT 0 COMMENT '固定成本',
  variable_cost DECIMAL(15,2) DEFAULT 0 COMMENT '变动成本',
  direct_cost DECIMAL(15,2) DEFAULT 0 COMMENT '直接成本',
  indirect_cost DECIMAL(15,2) DEFAULT 0 COMMENT '间接成本',
  cost_structure JSON COMMENT '成本结构详情',
  
  -- 版本控制
  calculation_version INT NOT NULL DEFAULT 1 COMMENT '计算版本',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  UNIQUE KEY uk_period_org (period, org_unit_id),
  INDEX idx_period (period),
  INDEX idx_org_unit (org_unit_id)
) COMMENT='指标快照表';

-- 4. 预算域（Budget Context）
-- ============================================

CREATE TABLE budget (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  period VARCHAR(20) NOT NULL COMMENT '预算期间: 2026-05',
  category VARCHAR(50) NOT NULL COMMENT '预算类别',
  budgeted_amount DECIMAL(15,2) NOT NULL COMMENT '预算金额',
  used_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '🆕 已用金额（P1）',
  remaining_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '🆕 剩余金额（P1）',
  approved_by BIGINT COMMENT '审批人',
  approved_at DATETIME COMMENT '审批时间',
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/approved/rejected',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_org_period (org_unit_id, period),
  INDEX idx_period (period)
) COMMENT='预算表（v4.0）';

CREATE TABLE budget_adjustment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  budget_id BIGINT NOT NULL COMMENT '预算ID',
  old_amount DECIMAL(15,2) NOT NULL COMMENT '原金额',
  new_amount DECIMAL(15,2) NOT NULL COMMENT '新金额',
  reason TEXT COMMENT '调整原因',
  requested_by BIGINT NOT NULL COMMENT '申请人',
  approved_by BIGINT COMMENT '审批人',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_budget (budget_id)
) COMMENT='预算调整表';

-- 5. 决策域（Decision Context）
-- ============================================

CREATE TABLE goal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  period VARCHAR(20) NOT NULL COMMENT '目标期间: 2026-05',
  target_profit DECIMAL(15,2) COMMENT '目标利润',
  target_cost DECIMAL(15,2) COMMENT '目标成本',
  target_roi DECIMAL(5,4) COMMENT '目标ROI',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_org_period (org_unit_id, period)
) COMMENT='目标表';

CREATE TABLE decision_fact_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  decision_id VARCHAR(50) NOT NULL COMMENT '决策ID',
  fact_id BIGINT NOT NULL COMMENT '事实ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_decision (decision_id),
  INDEX idx_fact (fact_id)
) COMMENT='决策-事实关联表';

CREATE TABLE action_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  decision_id VARCHAR(50) NOT NULL COMMENT '决策ID',
  problem TEXT COMMENT '问题描述',
  recommendation TEXT COMMENT '建议',
  executed_by BIGINT NOT NULL COMMENT '执行人',
  executed_at DATETIME NOT NULL COMMENT '执行时间',
  result TEXT COMMENT '执行结果',
  effectiveness VARCHAR(20) COMMENT '有效性: effective/ineffective/unknown',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_decision (decision_id),
  INDEX idx_executed_by (executed_by)
) COMMENT='行动记录表';

-- 6. 主数据域（Master Data）
-- ============================================

CREATE TABLE counterparty (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '名称',
  type VARCHAR(20) NOT NULL COMMENT '类型: customer/supplier/both',
  contact VARCHAR(100) COMMENT '联系人',
  phone VARCHAR(20) COMMENT '联系电话',
  address VARCHAR(200) COMMENT '地址',
  tax_no VARCHAR(50) COMMENT '税号',
  taxpayer_type VARCHAR(20) COMMENT '🆕 纳税人类型: general/small（P1）',
  bank_name VARCHAR(100) COMMENT '🆕 开户行（P0）',
  bank_account VARCHAR(50) COMMENT '🆕 银行账号（P0）',
  credit_level VARCHAR(20) COMMENT '信用等级: A/B/C/D',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/inactive',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_type (type),
  INDEX idx_status (status)
) COMMENT='客户/供应商表（v4.0）';

CREATE TABLE project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '项目名称',
  code VARCHAR(50) NOT NULL COMMENT '项目编码',
  project_type VARCHAR(20) COMMENT '🆕 项目类型: rd/sales/operation（P1）',
  org_unit_id BIGINT NOT NULL COMMENT '所属组织',
  manager_id BIGINT NOT NULL COMMENT '项目经理',
  budget DECIMAL(15,2) COMMENT '预算',
  start_date DATE COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/completed/cancelled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code),
  INDEX idx_org_unit (org_unit_id),
  INDEX idx_manager (manager_id),
  INDEX idx_status (status)
) COMMENT='项目表（v4.0）';

CREATE TABLE org_unit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '组织名称',
  type VARCHAR(20) NOT NULL COMMENT '组织类型: company/department/team',
  parent_id BIGINT COMMENT '父组织ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_parent (parent_id)
) COMMENT='组织单元表';

CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  role VARCHAR(20) NOT NULL COMMENT '角色: admin/manager/accountant/user',
  org_unit_id BIGINT COMMENT '所属组织',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username),
  INDEX idx_org_unit (org_unit_id)
) COMMENT='用户表';

-- 7. 审批流程（Approval Flow）
-- ============================================

CREATE TABLE approval_flow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_type VARCHAR(50) NOT NULL COMMENT '业务类型: fact_event/budget/budget_adjustment',
  business_id BIGINT NOT NULL COMMENT '业务ID',
  current_step INT NOT NULL DEFAULT 1 COMMENT '当前步骤',
  total_steps INT NOT NULL COMMENT '总步骤数',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_business (business_type, business_id),
  INDEX idx_status (status)
) COMMENT='审批流程表';

-- 8. 审计日志（Audit Log）
-- ============================================

CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '操作人',
  action VARCHAR(50) NOT NULL COMMENT '操作: create/update/delete/approve',
  table_name VARCHAR(50) NOT NULL COMMENT '表名',
  record_id BIGINT NOT NULL COMMENT '记录ID',
  old_value JSON COMMENT '旧值',
  new_value JSON COMMENT '新值',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_table_record (table_name, record_id),
  INDEX idx_created_at (created_at)
) COMMENT='审计日志表';

-- 🆕 9. 产品优化（Product Enhancement）
-- ============================================

-- 收支模板表（P1 - 产品优化）
CREATE TABLE fact_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '模板名称',
  type VARCHAR(20) NOT NULL COMMENT '类型: income/cost',
  account_subject_id BIGINT NOT NULL COMMENT '会计科目ID',
  cost_category VARCHAR(20) COMMENT '成本类别',
  counterparty_type VARCHAR(20) COMMENT '对手方类型',
  counterparty_id BIGINT COMMENT '对手方ID',
  project_id BIGINT COMMENT '项目ID',
  payment_method VARCHAR(20) COMMENT '付款方式',
  default_amount DECIMAL(15,2) COMMENT '默认金额',
  description TEXT COMMENT '说明',
  created_by BIGINT NOT NULL COMMENT '创建人',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_created_by (created_by),
  INDEX idx_type (type)
) COMMENT='收支模板表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化会计科目（示例）
INSERT INTO account_subject (code, name, parent_id, level, type, debit_credit) VALUES
('1001', '库存现金', NULL, 1, 'asset', 'debit'),
('1002', '银行存款', NULL, 1, 'asset', 'debit'),
('1122', '应收账款', NULL, 1, 'asset', 'debit'),
('2202', '应付账款', NULL, 1, 'liability', 'credit'),
('6001', '主营业务收入', NULL, 1, 'revenue', 'credit'),
('6401', '主营业务成本', NULL, 1, 'expense', 'debit'),
('6602', '销售费用', NULL, 1, 'expense', 'debit'),
('6603', '管理费用', NULL, 1, 'expense', 'debit');
