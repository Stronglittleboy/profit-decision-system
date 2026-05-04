# 数据库表结构设计

## 1. 事实域（Fact Context）

### fact_event（事实事件表）
```sql
CREATE TABLE fact_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_time DATETIME NOT NULL COMMENT '事件时间',
  type VARCHAR(20) NOT NULL COMMENT '类型: income/cost/behavior',
  amount DECIMAL(15,2) NOT NULL COMMENT '金额',
  actor_id BIGINT COMMENT '执行人ID',
  org_unit_id BIGINT NOT NULL COMMENT '组织单元ID',
  reference_id VARCHAR(100) COMMENT '业务关联ID',
  metadata JSON COMMENT '扩展信息',
  status VARCHAR(20) NOT NULL DEFAULT 'valid' COMMENT '状态: valid/reversed',
  version INT NOT NULL DEFAULT 1 COMMENT '版本号',
  source VARCHAR(20) NOT NULL COMMENT '来源: manual/agent/system',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_event_time (event_time),
  INDEX idx_org_unit (org_unit_id),
  INDEX idx_type_status (type, status)
) COMMENT='事实事件表';
```

---

## 2. 归因域（Attribution Context）

### attribution（归因表）
```sql
CREATE TABLE attribution (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  fact_id BIGINT NOT NULL COMMENT '事实ID',
  attributed_to BIGINT NOT NULL COMMENT '归属对象ID',
  type VARCHAR(20) NOT NULL COMMENT '类型: income/cost',
  amount DECIMAL(15,2) NOT NULL COMMENT '归因金额',
  weight DECIMAL(5,4) DEFAULT 1.0000 COMMENT '权重',
  rule_id BIGINT COMMENT '规则ID',
  period VARCHAR(20) NOT NULL COMMENT '周期: 2026-05',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_fact (fact_id),
  INDEX idx_attributed (attributed_to, period),
  INDEX idx_period (period)
) COMMENT='归因表';
```

### attribution_rule（归因规则表）
```sql
CREATE TABLE attribution_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_type VARCHAR(20) NOT NULL COMMENT '规则类型: split/map',
  condition JSON NOT NULL COMMENT '条件',
  strategy JSON NOT NULL COMMENT '策略',
  priority INT NOT NULL DEFAULT 0 COMMENT '优先级',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='归因规则表';
```

---

## 3. 指标域（Metrics Context）

### metric_snapshot（指标快照表）
```sql
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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_period_org (period, org_unit_id),
  INDEX idx_period (period)
) COMMENT='指标快照表';
```

---

## 4. 组织域（Organization Context）

### org_unit（组织单元表）
```sql
CREATE TABLE org_unit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '名称',
  type VARCHAR(20) NOT NULL COMMENT '类型: company/dept/amb/project',
  parent_id BIGINT COMMENT '父级ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_parent (parent_id)
) COMMENT='组织单元表';
```

### user（用户表）
```sql
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
```

---

## 5. 决策域（Decision Context）

### goal（目标表）
```sql
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
```
