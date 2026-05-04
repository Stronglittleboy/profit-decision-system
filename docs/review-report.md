# 架构审查报告

## 🏗️ 架构师视角审查

### ✅ 优点

1. **边界清晰**
   - 5个上下文职责单一
   - Fact 作为唯一写入口，防止数据污染
   - 单向依赖，无循环引用

2. **可扩展性强**
   - Attribution 规则可插拔
   - Decision 支持规则+AI双引擎
   - Metrics 支持重算

3. **数据一致性设计**
   - Event Sourcing 思想（Fact 不可变）
   - 最终一致性（异步归因）
   - 版本控制（version 字段）

### ⚠️ 架构风险

#### 🔴 严重问题

1. **缺少防腐层（Anti-Corruption Layer）**
   ```
   问题：Organization Context 标记为"共享内核"，但没有定义如何共享
   风险：其他域直接依赖 OrgUnit 实体，耦合过紧
   
   建议：
   - 定义 OrgUnitId 值对象
   - 各域通过接口查询组织信息
   - 禁止直接引用 OrgUnit 实体
   ```

2. **归因规则的 JSON 字段设计不当**
   ```sql
   condition JSON NOT NULL
   strategy JSON NOT NULL
   ```
   ```
   问题：规则逻辑存储在数据库 JSON 中，无法编译检查
   风险：运行时错误、规则冲突无法提前发现
   
   建议：
   - 规则用代码实现（Strategy 模式）
   - 数据库只存规则类型和参数
   - 示例：
     rule_type: 'DirectMapRule'
     params: '{"target_org_id": 2}'
   ```

3. **缺少幂等性保证**
   ```
   问题：FactEvent 可能重复录入（网络重试、Agent 重复提交）
   风险：收入/成本重复计算
   
   建议：
   - 增加 idempotency_key 字段
   - 唯一索引：UNIQUE(source, reference_id, idempotency_key)
   ```

4. **归因计算的事务边界不明确**
   ```
   问题：一个 Fact 可能产生多条 Attribution，如何保证原子性？
   风险：部分归因成功、部分失败，导致指标错误
   
   建议：
   - Attribution 增加 batch_id 字段
   - 同一批次的归因要么全成功、要么全失败
   - 失败时标记 Fact 为 'attribution_failed'
   ```

#### 🟡 中等问题

5. **Metrics 重算机制缺失**
   ```
   问题：文档说"支持重算"，但没有设计如何触发、如何回滚
   
   建议：
   - MetricSnapshot 增加 calculation_version 字段
   - 保留历史版本（软删除）
   - 提供重算 API：POST /metrics/{period}/recalculate
   ```

6. **Decision 缓存策略未定义**
   ```
   问题：DecisionView 标记"可缓存"，但没有缓存失效策略
   风险：用户看到过期建议
   
   建议：
   - 缓存 Key：decision:{org_id}:{period}
   - TTL：5分钟
   - 失效触发：Metrics 更新时主动清除
   ```

7. **缺少审计日志**
   ```
   问题：无法追溯"谁在什么时候修改了什么"
   
   建议：
   - 增加 AuditLog 表
   - 记录所有写操作（Fact 录入、规则修改、目标调整）
   ```

---

## 🎨 产品经理视角审查

### ✅ 优点

1. **用户心智模型清晰**
   - Goal → Status → Action 符合决策流程
   - 三步闭环，易于理解

2. **MVP 范围合理**
   - 核心功能完整（录入→归因→指标→决策）
   - 避免过度设计

### ⚠️ 产品风险

#### 🔴 严重问题

1. **缺少"为什么"的解释能力**
   ```
   场景：用户看到"成本超标15%"
   问题：不知道是哪些具体事实导致的
   
   建议：
   - Decision 增加 related_facts 字段
   - 点击建议后，展示归因明细
   - 示例：
     "人力成本超标 → 查看 5 条相关支出"
   ```

2. **目标设置缺少引导**
   ```
   问题：用户不知道目标应该设多少
   
   建议：
   - 提供历史数据参考
   - 智能推荐目标（基于过去3个月平均值）
   - 行业对标数据（可选）
   ```

3. **Action 无法闭环**
   ```
   问题：系统给出建议后，用户执行了什么？效果如何？
   风险：无法验证决策有效性
   
   建议：
   - 增加 ActionRecord 表
   - 记录：建议ID、执行时间、执行人、执行结果
   - 下次决策时参考历史执行效果
   ```

#### 🟡 中等问题

4. **缺少预警机制**
   ```
   问题：用户需要主动查看才知道有问题
   
   建议：
   - 增加告警规则（成本超标10%触发）
   - 支持推送（邮件/企业微信/钉钉）
   ```

5. **数据录入体验差**
   ```
   问题：手动录入每笔收入/成本，效率低
   
   建议：
   - 支持批量导入（Excel/CSV）
   - 对接财务系统 API（用友/金蝶）
   - Agent 语义录入（"今天收到客户A的5000元货款"）
   ```

6. **缺少对比分析**
   ```
   问题：只看当期数据，无法判断好坏
   
   建议：
   - 同比/环比分析
   - 趋势图（最近6个月利润走势）
   ```

7. **组织层级展示不直观**
   ```
   问题：树形结构在列表中难以理解
   
   建议：
   - 首页用组织树可视化
   - 点击节点查看该组织指标
   - 支持钻取（公司 → 部门 → 阿米巴）
   ```

---

## 🎯 优先级修复建议

### P0（必须修复，否则无法上线）

1. ✅ 增加幂等性保证（idempotency_key）
2. ✅ 定义归因事务边界（batch_id）
3. ✅ 规则引擎重构（代码实现，非 JSON）

### P1（影响核心体验）

4. ✅ Decision 关联 Fact 明细
5. ✅ Action 执行记录
6. ✅ 批量导入功能

### P2（增强体验）

7. 目标智能推荐
8. 预警推送
9. 趋势分析

---

## 📝 修订后的表结构（关键变更）

```sql
-- Fact 增加幂等性
ALTER TABLE fact_event ADD COLUMN idempotency_key VARCHAR(100);
ALTER TABLE fact_event ADD UNIQUE KEY uk_idempotency (source, reference_id, idempotency_key);

-- Attribution 增加批次
ALTER TABLE attribution ADD COLUMN batch_id VARCHAR(50);
ALTER TABLE attribution ADD INDEX idx_batch (batch_id);

-- 规则表重构
ALTER TABLE attribution_rule MODIFY COLUMN strategy VARCHAR(100) COMMENT '策略类名';
ALTER TABLE attribution_rule ADD COLUMN params JSON COMMENT '策略参数';

-- Decision 增加关联
CREATE TABLE decision_fact_relation (
  decision_id BIGINT,
  fact_id BIGINT,
  impact_weight DECIMAL(5,4),
  PRIMARY KEY (decision_id, fact_id)
);

-- Action 执行记录
CREATE TABLE action_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  decision_id BIGINT,
  executed_by BIGINT,
  executed_at DATETIME,
  result TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🚀 下一步行动

1. 修复 P0 问题（架构层）
2. 补充产品细节（交互流程图）
3. 开始编码（先实现 Fact + Attribution）
