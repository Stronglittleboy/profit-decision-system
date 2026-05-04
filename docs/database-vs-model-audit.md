# 数据库表结构 vs 领域模型 审查报告

**审查时间：** 2026-05-04 18:45  
**审查人：** 旺仔助手（架构师角色）

---

## ✅ 审查结论

**数据库表结构与领域模型 100% 一致**

- ✅ 所有核心业务表已创建（12张）
- ✅ 字段定义与领域模型完全匹配
- ✅ 数据类型、约束、索引符合设计要求
- ✅ 可以直接开始代码开发

---

## 📊 核心业务表清单

| 序号 | 表名 | 字段数 | 领域 | 状态 |
|------|------|--------|------|------|
| 1 | fact_event | 29 | 事实域 | ✅ |
| 2 | attribution | 10 | 归因域 | ✅ |
| 3 | attribution_rule | 10 | 归因域 | ✅ |
| 4 | metric_snapshot | 17 | 指标域 | ✅ |
| 5 | budget | 10 | 预算域 | ✅ |
| 6 | budget_adjustment | 9 | 预算域 | ✅ |
| 7 | goal | 8 | 决策域 | ✅ |
| 8 | action_record | 9 | 决策域 | ✅ |
| 9 | counterparty | 11 | 主数据 | ✅ |
| 10 | project | 11 | 主数据 | ✅ |
| 11 | org_unit | 6 | 主数据 | ✅ |
| 12 | user | 7 | 主数据 | ✅ |

**总计：** 12 张核心表，137 个字段

---

## 🔍 详细审查

### 1️⃣ fact_event（事实域核心表）

**字段数：** 29  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ 时间维度（business_date, accounting_date, cash_date, event_time）
- ✅ 基础信息（type, amount, cost_category）
- ✅ 跨期分摊（amortization_start, amortization_end, amortization_method）
- ✅ 关联维度（actor_id, org_unit_id, counterparty_type, counterparty_id, project_id）
- ✅ 发票信息（invoice_no, invoice_date, tax_rate, tax_amount）
- ✅ 状态控制（status, approval_status, version, source, idempotency_key）

**业务规则支持：**
- ✅ 支持收入/成本/行为三种类型
- ✅ 支持跨期分摊（年度保险、季度房租）
- ✅ 支持审批流程（approval_status）
- ✅ 支持幂等性（idempotency_key）

---

### 2️⃣ attribution（归因域）

**字段数：** 10  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ fact_id（关联事实）
- ✅ attributed_to（归因目标：org/project/amb）
- ✅ type（income/cost）
- ✅ amount（归因金额）
- ✅ weight（权重）
- ✅ rule_id（归因规则）
- ✅ period（会计期间）
- ✅ batch_id（批次ID，保证原子性）

**业务规则支持：**
- ✅ 支持多维度归因（组织/项目/AMB）
- ✅ 支持权重分配
- ✅ 支持批量归因（batch_id）

---

### 3️⃣ attribution_rule（归因规则）

**字段数：** 10  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ name（规则名称）
- ✅ rule_type（规则类型）
- ✅ cost_category（成本类别）
- ✅ strategy（策略：direct/weighted/proportional）
- ✅ params（JSON参数）
- ✅ priority（优先级）
- ✅ enabled（启用状态）

**业务规则支持：**
- ✅ 支持多种归因策略
- ✅ 支持规则优先级
- ✅ 支持动态参数配置

---

### 4️⃣ metric_snapshot（指标快照）

**字段数：** 17  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ period（会计期间）
- ✅ org_unit_id（组织单元）
- ✅ revenue（收入）
- ✅ cost（成本）
- ✅ profit（利润）
- ✅ margin（利润率）
- ✅ roi（投资回报率）
- ✅ 成本结构（fixed_cost, variable_cost, direct_cost, indirect_cost）
- ✅ cost_structure（JSON详细结构）
- ✅ calculation_version（计算版本）

**业务规则支持：**
- ✅ 支持多维度指标
- ✅ 支持成本结构分析
- ✅ 支持版本控制

---

### 5️⃣ budget（预算管理）

**字段数：** 10  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ org_unit_id（组织单元）
- ✅ period（预算期间）
- ✅ category（预算类别）
- ✅ budgeted_amount（预算金额）
- ✅ approved_by（审批人）
- ✅ approved_at（审批时间）
- ✅ status（状态）

**业务规则支持：**
- ✅ 支持预算审批流程
- ✅ 支持多类别预算

---

### 6️⃣ budget_adjustment（预算调整）

**字段数：** 9  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ budget_id（关联预算）
- ✅ old_amount（原金额）
- ✅ new_amount（新金额）
- ✅ reason（调整原因）
- ✅ requested_by（申请人）
- ✅ approved_by（审批人）
- ✅ status（状态）

**业务规则支持：**
- ✅ 支持预算调整审批
- ✅ 支持调整历史追溯

---

### 7️⃣ goal（目标管理）

**字段数：** 8  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ org_unit_id（组织单元）
- ✅ period（目标期间）
- ✅ target_profit（目标利润）
- ✅ target_cost（目标成本）
- ✅ target_roi（目标ROI）

**业务规则支持：**
- ✅ 支持多维度目标设定

---

### 8️⃣ action_record（行动记录）

**字段数：** 9  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ decision_id（决策ID）
- ✅ problem（问题描述）
- ✅ recommendation（建议）
- ✅ executed_by（执行人）
- ✅ executed_at（执行时间）
- ✅ result（执行结果）
- ✅ effectiveness（有效性）

**业务规则支持：**
- ✅ 支持决策闭环追踪

---

### 9️⃣ counterparty（客户/供应商）

**字段数：** 11  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ name（名称）
- ✅ type（类型：customer/supplier/both）
- ✅ contact（联系人）
- ✅ phone（电话）
- ✅ address（地址）
- ✅ tax_no（税号）
- ✅ credit_level（信用等级）
- ✅ status（状态）

**业务规则支持：**
- ✅ 支持客户/供应商统一管理
- ✅ 支持信用等级管理

---

### 🔟 project（项目管理）

**字段数：** 11  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ name（项目名称）
- ✅ code（项目编码）
- ✅ org_unit_id（所属组织）
- ✅ manager_id（项目经理）
- ✅ budget（预算）
- ✅ start_date（开始日期）
- ✅ end_date（结束日期）
- ✅ status（状态）

**业务规则支持：**
- ✅ 支持项目预算管理
- ✅ 支持项目生命周期管理

---

### 1️⃣1️⃣ org_unit（组织单元）

**字段数：** 6  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ name（名称）
- ✅ type（类型）
- ✅ parent_id（父节点，支持树形结构）

**业务规则支持：**
- ✅ 支持组织树形结构

---

### 1️⃣2️⃣ user（用户）

**字段数：** 7  
**状态：** ✅ 完全符合领域模型

**关键字段验证：**
- ✅ username（用户名）
- ✅ password（密码）
- ✅ role（角色）
- ✅ org_unit_id（所属组织）

**业务规则支持：**
- ✅ 支持基础用户管理
- ✅ 支持组织关联

---

## 🎯 开发优先级

### P0（核心功能，必须实现）

1. **counterparty**（客户/供应商管理）
   - 字段：11个
   - 复杂度：低
   - 预计时间：20分钟

2. **project**（项目管理）
   - 字段：11个
   - 复杂度：低
   - 预计时间：20分钟

3. **org_unit**（组织管理）
   - 字段：6个
   - 复杂度：中（树形结构）
   - 预计时间：30分钟

4. **fact_event**（收支管理）
   - 字段：29个
   - 复杂度：高（核心业务逻辑）
   - 预计时间：60分钟

### P1（重要功能）

5. **attribution**（归因管理）
   - 字段：10个
   - 复杂度：高（归因算法）
   - 预计时间：60分钟

6. **metric_snapshot**（指标快照）
   - 字段：17个
   - 复杂度：中（计算逻辑）
   - 预计时间：40分钟

7. **budget**（预算管理）
   - 字段：10个
   - 复杂度：中
   - 预计时间：30分钟

### P2（辅助功能）

8. **attribution_rule**（归因规则）
9. **budget_adjustment**（预算调整）
10. **goal**（目标管理）
11. **action_record**（行动记录）
12. **user**（用户管理）

---

## 📝 开发建议

### 1. 开发顺序

```
第一批（主数据）：
  counterparty → org_unit → project → user

第二批（核心业务）：
  fact_event → attribution → metric_snapshot

第三批（辅助功能）：
  budget → attribution_rule → budget_adjustment → goal → action_record
```

### 2. 代码生成策略

**简单 CRUD（可快速手写）：**
- counterparty
- project
- org_unit
- user
- budget
- goal

**复杂业务逻辑（需要精心设计）：**
- fact_event（跨期分摊、审批流程）
- attribution（归因算法）
- metric_snapshot（指标计算）

### 3. 技术要点

**fact_event 表：**
- 需要实现幂等性（idempotency_key）
- 需要实现审批流程（approval_status）
- 需要实现跨期分摊逻辑

**attribution 表：**
- 需要实现批量归因（batch_id）
- 需要实现归因算法（根据 rule_id）

**metric_snapshot 表：**
- 需要实现指标计算逻辑
- 需要实现版本控制

---

## ✅ 审查结论

**数据库表结构设计优秀，可以直接开始代码开发。**

**建议：**
1. 从简单的主数据表开始（counterparty, project, org_unit）
2. 验证开发流程和代码规范
3. 再开发复杂的核心业务表（fact_event, attribution）

**预计总开发时间：**
- 简单表（6个）：2小时
- 复杂表（3个）：3小时
- 辅助表（3个）：1.5小时
- **总计：6.5小时**

---

**审查人：** 旺仔助手  
**日期：** 2026-05-04  
**状态：** ✅ 审查通过，可以开始开发
