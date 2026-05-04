# DDD 领域模型设计

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
【基础层】User / Org / Permission
```

---

## 领域划分

### 1️⃣ Fact Context（事实域）

**聚合根：FactEvent**

```
FactEvent
├── id
├── event_time
├── type（income / cost / behavior）
├── amount
├── actor_id（WHO）
├── org_unit_id（WHERE）
├── reference_id（业务关联）
├── metadata（JSON）
├── status（valid / reversed）
├── version
└── source（manual / agent / system）
```

**职责：** 记录"发生了什么"

**约束：**
- 允许修正（通过 reversed）
- 不做计算
- 不做解释

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
└── period
```

**规则实体：AttributionRule**

```
AttributionRule
├── id
├── rule_type（分摊 / 映射）
├── condition
├── strategy
└── priority
```

**职责：** 解释"钱为什么属于这里"

**特点：**
- 支持重算
- 支持批处理
- 支持多规则

---

### 3️⃣ Metrics Context（指标域）

**实体：MetricSnapshot**

```
MetricSnapshot
├── id
├── period
├── org_unit_id
├── revenue
├── cost
├── profit
├── margin
├── ROI
└── cost_structure（JSON）
```

**职责：** 提供"决策基础数据"

**特点：**
- 周期性计算（天/月）
- 不实时强一致
- 可重算

---

### 4️⃣ Decision Context（决策域）

**模型：DecisionView**（只读模型，非聚合根）

```
DecisionView
├── problem
├── root_cause
├── recommendation
├── expected_impact
├── confidence
└── priority
```

**职责：** 告诉用户"该做什么"

**来源：** Metrics + Attribution + Goal

**特点：**
- 动态生成
- 可缓存
- 不持久核心数据

---

### 5️⃣ Organization Context（组织域）

**实体：**

```
OrgUnit
├── id
├── name
├── type（company / dept / amb / project）
└── parent_id

User
├── id
└── role
```

**职责：**
- 归属（WHERE）
- 责任（WHO）

---

## 领域服务

### AttributionService
- **输入：** Fact
- **输出：** Attribution
- **模式：** 同步（轻量）/ 异步（批量）

### MetricService
- **输入：** Attribution
- **输出：** Metrics
- **模式：** 按周期计算，支持重算

### DecisionService
- **输入：** Metrics + Goal
- **输出：** Decision
- **模式：** 规则 + AI（可插拔）

---

## 核心数据流

```
FactEvent
  ↓
Attribution（归因）
  ↓
MetricSnapshot（指标）
  ↓
Decision（建议）
  ↓
Action（执行）
  ↓
FactEvent（结果回写）
```

---

## 边界上下文映射

| 上下文 | 依赖 | 集成方式 |
|--------|------|----------|
| Fact | 无 | - |
| Attribution | Fact | 事件订阅 |
| Metrics | Attribution | 定时批处理 |
| Decision | Metrics | 查询 |
| Organization | 无 | 共享内核 |
