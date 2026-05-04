# DDD 领域模型设计（终审版 v3.0）

## 系统定位

基于**经营事实 → 归因 → 指标 → 决策**的闭环系统，帮助企业持续优化利润。

---

## 整体架构

```
【产品层】Goal / Status / Action
【决策层】Decision Service（建议生成）
【指标层】Metrics（利润/ROI/成本结构）
【归因层】Attribution（成本/收入归属）
【事实层】FactEvent（唯一事实源）
【预算层】Budget（预算管理）
【主数据层】Counterparty / Project / OrgUnit / User
```

---

## 领域划分

### 1️⃣ Fact Context（事实域）

**聚合根：FactEvent**

```
FactEvent
├── id
├── 时间维度
│   ├── business_date（业务发生日期）
│   ├── accounting_date（会计确认日期）★
│   ├── cash_date（现金流日期）
│   └── event_time（记录时间）
├── 基础信息
│   ├── type（income / cost / behavior）
│   ├── amount（金额）
│   └── cost_category（fixed/variable/direct/indirect）★
├── 跨期分摊 ★
│   ├── amortization_start（分摊开始日期）
│   ├── amortization_end（分摊结束日期）
│   └── amortization_method（linear/actual_days）
├── 关联维度
│   ├── actor_id（WHO）
│   ├── org_unit_id（WHERE）
│   ├── counterparty_type（customer/supplier）★
│   ├── counterparty_id（客户/供应商ID）★
│   └── project_id（项目ID）★
├── 发票信息 ★
│   ├── invoice_no（发票号）
│   ├── invoice_date（开票日期）
│   ├── tax_rate（税率）
│   └── tax_amount（税额）
├── 业务关联
│   ├── reference_id（业务关联ID）
│   └── metadata（JSON扩展）
└── 状态控制
    ├── status（valid/reversed/attribution_failed）
    ├── approval_status（pending/approved/rejected）★
    ├── version（版本号）
    ├── source（manual/agent/system）
    └── idempotency_key（幂等键）

★ = v3.0 新增
```

**职责：** 记录"发生了什么"

**约束：**
- 允许修正（通过 reversed）
- 不做计算
- 不做解释
- 幂等性保证（idempotency_key）

**业务规则：**
1. 收入必须有客户（counterparty_type=customer）
2. 成本必须有成本类别（cost_category）
3. 跨期分摊的 Fact，amortization_start/end 必填
4. 大额支出需要审批（approval_status）

---

### 2️⃣ Attribution Context（归因域）

**聚合：Attribution**

```
Attribution
├── id
├── fact_id
├── attributed_to（org / project / amb）
├── type（income / cost）
├── amount
├── weight
├── rule_id
├── period
└── batch_id（批次ID，保证原子性）★
```

**规则实体：AttributionRule**

```
AttributionRule
├── id
├── name（规则名称）★
├── rule_type（direct/split/ratio）
├── cost_category（适用成本类别）★
├── strategy（策略类名）
├── params（策略参数 JSON）
├── priority
└── enabled
```

**职责：** 解释"钱为什么属于这里"

**特点：**
- 支持重算
- 支持批处理
- 支持多规则
- 事务保证（batch_id）

**归因策略（代码实现）：**

```java
// 1. 直接归属策略（收入、直接成本）
DirectAttributionStrategy
- 输入：Fact（有明确的 org_unit_id 或 project_id）
- 输出：单条 Attribution（weight=1.0）

// 2. 均摊策略（固定成本）
EqualSplitStrategy
- 输入：Fact（cost_category=fixed）
- 参数：target_org_ids（目标组织列表）
- 输出：多条 Attribution（weight=1/N）

// 3. 比例分摊策略（间接成本）
RatioSplitStrategy
- 输入：Fact（cost_category=indirect）
- 参数：ratio_basis（分摊依据：revenue/headcount）
- 输出：多条 Attribution（按比例计算 weight）

// 4. 时间分摊策略（跨期费用）
TimeAmortizationStrategy
- 输入：Fact（有 amortization_start/end）
- 输出：每日生成一条 Attribution（按天数分摊）
```

**归因流程：**
```
1. FactEvent 创建后发布事件
2. AttributionService 监听事件
3. 根据 Fact 属性匹配规则（按 priority 排序）
4. 执行策略，生成 Attribution（同一 batch_id）
5. 批量写入数据库（事务保证）
6. 失败时标记 Fact.status = 'attribution_failed'
```

---

### 3️⃣ Metrics Context（指标域）

**实体：MetricSnapshot**

```
MetricSnapshot
├── id
├── period
├── org_unit_id
├── 基础指标
│   ├── revenue（收入）
│   ├── cost（成本）
│   ├── profit（利润）
│   ├── margin（利润率）
│   └── roi（ROI）
├── 成本结构 ★
│   ├── fixed_cost（固定成本）
│   ├── variable_cost（变动成本）
│   ├── direct_cost（直接成本）
│   ├── indirect_cost（间接成本）
│   └── cost_structure（详细结构 JSON）
├── 版本控制
│   ├── calculation_version（计算版本）
│   └── is_deleted（软删除标记）
└── 时间戳
    ├── created_at
    └── updated_at
```

**职责：** 提供"决策基础数据"

**特点：**
- 周期性计算（天/月）
- 不实时强一致（最终一致性）
- 可重算（保留历史版本）

**计算流程：**
```
定时任务（每日凌晨 2:00）：
1. 查询昨日的 Attribution 记录
2. 按 org_unit_id + period 分组聚合
3. 计算基础指标：
   - revenue = SUM(amount WHERE type='income')
   - cost = SUM(amount WHERE type='cost')
   - profit = revenue - cost
   - margin = profit / revenue
4. 计算成本结构：
   - fixed_cost = SUM(amount WHERE cost_category='fixed')
   - variable_cost = SUM(amount WHERE cost_category='variable')
   - ...
5. 写入 MetricSnapshot（calculation_version++）
6. 旧版本标记 is_deleted=1（软删除）
```

**重算机制：**
```
POST /api/v1/metrics/{period}/recalculate
1. 查询该周期所有 Attribution
2. 重新计算指标
3. 创建新版本 MetricSnapshot
4. 清除 Decision 缓存
```

---

### 4️⃣ Budget Context（预算域）★ 新增

**聚合根：Budget**

```
Budget
├── id
├── org_unit_id
├── period
├── category（revenue/fixed_cost/variable_cost等）
├── budgeted_amount（预算金额）
├── approved_by（审批人）
├── approved_at（审批时间）
└── status（draft/approved/executing）
```

**实体：BudgetAdjustment**

```
BudgetAdjustment
├── id
├── budget_id
├── old_amount
├── new_amount
├── reason（调整原因）
├── requested_by（申请人）
├── approved_by（审批人）
└── status（pending/approved/rejected）
```

**职责：** 预算编制、执行监控、调整管理

**业务规则：**
1. 预算必须审批通过才能生效
2. 预算调整需要审批
3. 预算执行率 = 实际金额 / 预算金额

**领域服务：BudgetComparisonService**
```java
输入：org_unit_id + period
输出：BudgetComparisonView
  - budgeted_revenue / actual_revenue / variance
  - budgeted_cost / actual_cost / variance
  - execution_rate
  - alerts（超标预警）
```

---

### 5️⃣ Decision Context（决策域）

**模型：DecisionView**（只读模型，非聚合根）

```
DecisionView
├── problem（问题描述）
├── root_cause（根因）
├── recommendation（建议）
├── expected_impact（预期收益）
├── confidence（置信度）
├── priority（优先级）
└── related_facts（相关事实列表）★
```

**实体：ActionRecord**

```
ActionRecord
├── id
├── decision_id
├── problem（问题描述）
├── recommendation（建议内容）
├── executed_by（执行人）
├── executed_at（执行时间）
├── result（执行结果）
└── effectiveness（effective/ineffective/unknown）★
```

**职责：** 告诉用户"该做什么"

**来源：** Metrics + Budget + Goal + Attribution

**特点：**
- 动态生成
- 可缓存（TTL=5分钟）
- 不持久核心数据

**决策规则（示例）：**

```java
// 规则1：成本超标
if (actual_cost > budgeted_cost * 1.1) {
  problem = "成本超标 " + variance + "%";
  root_cause = 分析成本结构，找出占比最高的类别;
  recommendation = "优化 " + top_category + " 支出";
  expected_impact = "预计降低成本 " + estimate;
  priority = variance > 20% ? "high" : "medium";
  related_facts = 查询该类别的 Top 10 支出;
}

// 规则2：客户贡献下降
if (customer_revenue_this_month < customer_revenue_last_month * 0.8) {
  problem = "客户 " + customer_name + " 贡献下降 " + variance + "%";
  root_cause = "订单量减少";
  recommendation = "主动联系客户，了解需求变化";
  expected_impact = "挽回收入 " + estimate;
  priority = "medium";
  related_facts = 查询该客户的历史订单;
}
```

---

### 6️⃣ Master Data Context（主数据域）★ 新增

**实体：Counterparty（客户/供应商）**

```
Counterparty
├── id
├── name
├── type（customer/supplier）
├── contact（联系人）
├── phone
├── address
├── tax_no（税号）
├── credit_level（A/B/C/D）
└── status（active/inactive）
```

**实体：Project（项目）**

```
Project
├── id
├── name
├── code（项目编号）
├── org_unit_id
├── manager_id（项目经理）
├── budget（预算）
├── start_date
├── end_date
└── status（planning/executing/closed）
```

**实体：OrgUnit（组织单元）**

```
OrgUnit
├── id
├── name
├── type（company/dept/amb/project）
└── parent_id
```

**实体：User（用户）**

```
User
├── id
├── username
├── password
├── role
└── org_unit_id
```

**职责：**
- 提供主数据查询
- 维护组织结构
- 管理客户/供应商信息

---

### 7️⃣ Approval Context（审批域）★ 新增

**实体：ApprovalFlow**

```
ApprovalFlow
├── id
├── entity_type（fact/budget/goal）
├── entity_id
├── status（pending/approved/rejected）
├── approver_id
├── approved_at
└── comment（审批意见）
```

**职责：** 审批流程管理

**业务规则：**
1. 大额 Fact（amount > 10000）需要审批
2. Budget 必须审批
3. BudgetAdjustment 必须审批

---

## 领域服务

### AttributionService
- **输入：** FactEvent
- **输出：** List<Attribution>
- **模式：** 事件驱动（异步）
- **事务：** 批次控制（batch_id）

### MetricService
- **输入：** List<Attribution>
- **输出：** MetricSnapshot
- **模式：** 定时批处理（每日）
- **特性：** 支持重算

### DecisionService
- **输入：** Metrics + Budget + Goal
- **输出：** List<DecisionView>
- **模式：** 查询时动态生成
- **缓存：** Redis（TTL=5分钟）

### BudgetComparisonService ★
- **输入：** org_unit_id + period
- **输出：** BudgetComparisonView
- **模式：** 查询时计算

### AmortizationService ★
- **输入：** 定时任务触发
- **输出：** 生成当日分摊的 Attribution
- **模式：** 每日凌晨执行

---

## 核心数据流

```
FactEvent（事实录入）
  ↓
Attribution（归因计算）
  ↓
MetricSnapshot（指标聚合）
  ↓
Decision（决策生成）← Budget（预算对比）
  ↓
Action（执行记录）
  ↓
FactEvent（结果回写）
```

---

## 边界上下文映射

| 上下文 | 依赖 | 集成方式 | 说明 |
|--------|------|----------|------|
| Fact | 无 | - | 唯一写入口 |
| Attribution | Fact | 事件订阅 | 异步归因 |
| Metrics | Attribution | 定时批处理 | 每日聚合 |
| Budget | 无 | - | 独立管理 |
| Decision | Metrics + Budget | 查询 | 动态生成 |
| MasterData | 无 | 共享内核 | 只读查询 |
| Approval | Fact + Budget | 状态同步 | 审批流程 |

---

## 防腐层设计

### OrgUnitId 值对象
```java
// 各域不直接依赖 OrgUnit 实体，而是通过值对象
public class OrgUnitId {
    private Long value;
    
    // 通过接口查询组织信息
    public OrgUnit getOrgUnit() {
        return orgUnitRepository.findById(value);
    }
}
```

### CounterpartyId 值对象
```java
public class CounterpartyId {
    private Long value;
    private CounterpartyType type; // customer/supplier
}
```

---

## 关键设计决策

### 1. 为什么 Decision 不持久化？
- Decision 是计算结果，不是业务事实
- 数据来源（Metrics/Budget）变化时，Decision 自动失效
- 缓存即可，无需持久化

### 2. 为什么 Attribution 可以重算？
- Attribution 是解释性数据，不是事实
- 规则调整后需要重算
- 通过 batch_id 保证原子性

### 3. 为什么 Metrics 保留历史版本？
- 支持重算后对比
- 审计需要
- 回滚能力

### 4. 为什么区分 business_date 和 accounting_date？
- 符合会计准则（权责发生制）
- 业务发生和收入确认可能不同期
- 利润计算用 accounting_date

---

## v3.0 核心改进

1. ✅ **成本分类** → 支持不同分摊策略
2. ✅ **时间维度** → 符合会计准则
3. ✅ **跨期分摊** → 自动按期分摊
4. ✅ **预算管理** → 新增 Budget Context
5. ✅ **客户/项目维度** → 新增 MasterData Context
6. ✅ **审批流程** → 新增 Approval Context
7. ✅ **决策闭环** → ActionRecord 记录执行效果
