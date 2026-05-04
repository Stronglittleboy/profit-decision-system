-- ============================================
-- 利润决策系统 - 数据库表结构（修订版 v2.0）
-- ============================================

-- 1. 事实域（Fact Context）
-- ============================================

CREATE TABLE fact_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_time DATETIME NOT NULL COMMENT '事件时间',
  type VARCHAR(20) NOT NULL COMMENT '类型: income/cost/behavior',
  amount DECIMAL(15,2) NOT NULL COMMENT '金额',
  actor_id BIGINT COMMENT '执行人ID',
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  reference_id VARCHAR(100) COMMENT '业务关联ID',
  metadata JSON COMMENT '扩展信息',
  status VARCHAR(20) NOT NULL DEFAULT 'valid' COMMENT '状态: valid/reversed/attribution_failed',
  version INT NOT NULL DEFAULT 1 COMMENT '版本号',
  source VARCHAR(20) NOT NULL COMMENT '来源: manual/agent/system',
  idempotency_key VARCHAR(100) COMMENT '幂等键',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_event_time (event_time),
  INDEX idx_org_unit (org_unit_id),
  INDEX idx_type_status (type, status),
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
  revenue DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '收入',
  cost DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '成本',
  profit DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '利润',
  margin DECIMAL(5,4) COMMENT '利润率',
  roi DECIMAL(5,4) COMMENT 'ROI',
  cost_structure JSON COMMENT '成本结构',
  calculation_version INT NOT NULL DEFAULT 1 COMMENT '计算版本（支持重算）',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_period_org_version (period, org_unit_id, calculation_version),
  INDEX idx_period (period)
) COMMENT='指标快照表';

-- 4. 决策域（Decision Context）
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

-- 5. 组织域（Organization Context）
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

-- 6. 审计日志
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
