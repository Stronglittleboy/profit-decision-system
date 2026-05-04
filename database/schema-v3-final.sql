-- ============================================
-- 利润决策系统 - 数据库表结构（终审版 v3.0）
-- 三方评审通过版本
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
  
  -- 发票信息（业务需求：税务合规）
  invoice_no VARCHAR(50) COMMENT '发票号',
  invoice_date DATE COMMENT '开票日期',
  tax_rate DECIMAL(5,4) COMMENT '税率',
  tax_amount DECIMAL(15,2) COMMENT '税额',
  
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
  UNIQUE KEY uk_idempotency (source, reference_id, idempotency_key)
) COMMENT='事实事件表';

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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_fact (fact_id),
  INDEX idx_attributed (attributed_to, period),
  INDEX idx_period (period),
  INDEX idx_batch (batch_id)
) COMMENT='归因表';

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
  period VARCHAR(20) NOT NULL COMMENT '周期: 2026-05',
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  
  -- 基础指标
  revenue DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '收入',
  cost DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '成本',
  profit DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '利润',
  margin DECIMAL(5,4) COMMENT '利润率',
  roi DECIMAL(5,4) COMMENT 'ROI',
  
  -- 成本结构（业务需求：按类别统计）
  fixed_cost DECIMAL(15,2) DEFAULT 0 COMMENT '固定成本',
  variable_cost DECIMAL(15,2) DEFAULT 0 COMMENT '变动成本',
  direct_cost DECIMAL(15,2) DEFAULT 0 COMMENT '直接成本',
  indirect_cost DECIMAL(15,2) DEFAULT 0 COMMENT '间接成本',
  cost_structure JSON COMMENT '详细成本结构',
  
  -- 版本控制
  calculation_version INT NOT NULL DEFAULT 1 COMMENT '计算版本（支持重算）',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
  
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  UNIQUE KEY uk_period_org_version (period, org_unit_id, calculation_version),
  INDEX idx_period (period)
) COMMENT='指标快照表';

-- 4. 预算域（Budget Context）- 新增
-- ============================================

CREATE TABLE budget (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  period VARCHAR(20) NOT NULL COMMENT '周期: 2026-05',
  category VARCHAR(50) NOT NULL COMMENT '类别: revenue/fixed_cost/variable_cost等',
  budgeted_amount DECIMAL(15,2) NOT NULL COMMENT '预算金额',
  approved_by BIGINT COMMENT '审批人ID',
  approved_at DATETIME COMMENT '审批时间',
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/approved/executing',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_org_period_category (org_unit_id, period, category),
  INDEX idx_period (period)
) COMMENT='预算表';

CREATE TABLE budget_adjustment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  budget_id BIGINT NOT NULL COMMENT '预算ID',
  old_amount DECIMAL(15,2) NOT NULL COMMENT '原金额',
  new_amount DECIMAL(15,2) NOT NULL COMMENT '新金额',
  reason TEXT COMMENT '调整原因',
  requested_by BIGINT NOT NULL COMMENT '申请人ID',
  approved_by BIGINT COMMENT '审批人ID',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_budget (budget_id)
) COMMENT='预算调整表';

-- 5. 决策域（Decision Context）
-- ============================================

CREATE TABLE goal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  period VARCHAR(20) NOT NULL COMMENT '周期',
  target_profit DECIMAL(15,2) COMMENT '目标利润',
  target_cost DECIMAL(15,2) COMMENT '目标成本',
  target_roi DECIMAL(5,4) COMMENT '目标ROI',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_org_period (org_unit_id, period)
) COMMENT='目标表';

CREATE TABLE decision_fact_relation (
  decision_id VARCHAR(50) NOT NULL COMMENT '决策ID（缓存Key）',
  fact_id BIGINT NOT NULL COMMENT '事实ID',
  impact_weight DECIMAL(5,4) COMMENT '影响权重',
  PRIMARY KEY (decision_id, fact_id),
  INDEX idx_fact (fact_id)
) COMMENT='决策-事实关联表';

CREATE TABLE action_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  decision_id VARCHAR(50) NOT NULL COMMENT '决策ID',
  problem TEXT COMMENT '问题描述',
  recommendation TEXT COMMENT '建议内容',
  executed_by BIGINT NOT NULL COMMENT '执行人ID',
  executed_at DATETIME NOT NULL COMMENT '执行时间',
  result TEXT COMMENT '执行结果',
  effectiveness VARCHAR(20) COMMENT '有效性: effective/ineffective/unknown',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_decision (decision_id),
  INDEX idx_executed_by (executed_by)
) COMMENT='行动记录表';

-- 6. 组织域（Organization Context）
-- ============================================

CREATE TABLE org_unit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '名称',
  type VARCHAR(20) NOT NULL COMMENT '类型: company/dept/amb/project',
  parent_id BIGINT COMMENT '父级ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_parent (parent_id)
) COMMENT='组织单元表';

CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  role VARCHAR(20) NOT NULL COMMENT '角色',
  org_unit_id BIGINT COMMENT '所属组织',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) COMMENT='用户表';

-- 7. 主数据域（Master Data）- 新增
-- ============================================

CREATE TABLE counterparty (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '名称',
  type VARCHAR(20) NOT NULL COMMENT '类型: customer/supplier',
  contact VARCHAR(100) COMMENT '联系人',
  phone VARCHAR(20) COMMENT '电话',
  address VARCHAR(200) COMMENT '地址',
  tax_no VARCHAR(50) COMMENT '税号',
  credit_level VARCHAR(20) COMMENT '信用等级: A/B/C/D',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/inactive',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_type (type)
) COMMENT='客户/供应商表';

CREATE TABLE project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '项目名称',
  code VARCHAR(50) NOT NULL COMMENT '项目编号',
  org_unit_id BIGINT NOT NULL COMMENT '所属组织',
  manager_id BIGINT NOT NULL COMMENT '项目经理',
  budget DECIMAL(15,2) COMMENT '预算',
  start_date DATE COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  status VARCHAR(20) NOT NULL DEFAULT 'planning' COMMENT '状态: planning/executing/closed',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code),
  INDEX idx_org (org_unit_id),
  INDEX idx_status (status)
) COMMENT='项目表';

-- 8. 审批流程 - 新增
-- ============================================

CREATE TABLE approval_flow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(50) NOT NULL COMMENT '实体类型: fact/budget/goal',
  entity_id BIGINT NOT NULL COMMENT '实体ID',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
  approver_id BIGINT NOT NULL COMMENT '审批人ID',
  approved_at DATETIME COMMENT '审批时间',
  comment TEXT COMMENT '审批意见',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_entity (entity_type, entity_id),
  INDEX idx_approver (approver_id),
  INDEX idx_status (status)
) COMMENT='审批流程表';

-- 9. 审计日志
-- ============================================

CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '操作人ID',
  action VARCHAR(50) NOT NULL COMMENT '操作类型',
  entity_type VARCHAR(50) NOT NULL COMMENT '实体类型',
  entity_id BIGINT NOT NULL COMMENT '实体ID',
  old_value JSON COMMENT '旧值',
  new_value JSON COMMENT '新值',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_entity (entity_type, entity_id),
  INDEX idx_created (created_at)
) COMMENT='审计日志表';
