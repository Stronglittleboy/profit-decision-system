# 飞牛经营系统 - 开发任务看板 v4.0

**项目周期：** 2026-05-05 至 2026-05-09（5个工作日）  
**团队配置：** 架构师 + 高级开发 + 初级开发A（后端）+ 初级开发B（前端）+ DBA + 测试工程师  
**当前版本：** v4.0  
**数据库表：** 19张表，209个字段

---

## 📊 总体进度

```
Day 1: ████████░░░░░░░░░░░░ 20% (主数据 + 会计基础)
Day 2: ░░░░░░░░░░░░░░░░░░░░  0% (凭证管理 + 收支管理)
Day 3: ░░░░░░░░░░░░░░░░░░░░  0% (辅助功能)
Day 4: ░░░░░░░░░░░░░░░░░░░░  0% (完善和测试)
Day 5: ░░░░░░░░░░░░░░░░░░░░  0% (上线准备)

总进度: 0/84 小时 (0%)
```

---

## 📅 Day 1：主数据 + 会计基础（2026-05-05，20小时）

**目标：** 完成 4 个主数据模块 + 凭证自动生成策略

### 上午（10小时）

#### 架构师（2h）
- [ ] 技术架构设计文档
  - [ ] DDD 分层架构图
  - [ ] 模块依赖关系图
  - [ ] 接口设计规范
- [ ] 代码模板准备
  - [ ] Controller 模板
  - [ ] Service 模板
  - [ ] Mapper 模板

#### DBA（2h）
- [ ] 创建数据库表（v4.0）
  - [ ] 备份现有数据库
  - [ ] 执行 schema-v4-reviewed.sql
  - [ ] 验证表结构
  - [ ] 导入初始化数据（会计科目）
- [ ] 创建索引
  - [ ] fact_event 表索引
  - [ ] voucher 表索引
  - [ ] audit_log 分区表

#### 高级开发（2h）
- [ ] voucher 实体和 Mapper
  - [ ] Voucher.java（凭证头）
  - [ ] VoucherEntry.java（凭证明细）
  - [ ] VoucherMapper.java + XML
  - [ ] VoucherEntryMapper.java + XML
  - [ ] 单元测试

#### 初级开发A（3h）
- [ ] account_subject（会计科目）1.5h
  - [ ] AccountSubject.java
  - [ ] AccountSubjectMapper.java + XML
  - [ ] AccountSubjectService.java
  - [ ] AccountSubjectController.java
  - [ ] 单元测试（树形结构查询）
- [ ] counterparty（客户/供应商）1.5h
  - [ ] Counterparty.java
  - [ ] CounterpartyMapper.java + XML
  - [ ] CounterpartyService.java
  - [ ] CounterpartyController.java
  - [ ] 单元测试

#### 初级开发B（4h）
- [ ] 会计科目管理页面 2h
  - [ ] AccountSubjectList.vue（树形列表）
  - [ ] AccountSubjectModal.vue（新增/编辑）
  - [ ] api/accountSubject.ts
  - [ ] 测试：树形展开/折叠
- [ ] 客户/供应商管理页面 2h
  - [ ] CounterpartyList.vue
  - [ ] CounterpartyModal.vue
  - [ ] api/counterparty.ts
  - [ ] 测试：类型筛选、信用等级

#### 测试工程师（2h）
- [ ] 编写测试用例
  - [ ] account_subject 测试用例（20个）
  - [ ] counterparty 测试用例（15个）
  - [ ] voucher 测试用例（30个）

---

### 下午（10小时）

#### 架构师（1h）
- [ ] 代码审查
  - [ ] 审查 account_subject 代码
  - [ ] 审查 counterparty 代码
  - [ ] 审查 voucher 代码

#### 高级开发（4h）⭐ 核心
- [ ] 凭证自动生成策略
  - [ ] VoucherGenerateStrategy.java（策略接口）
  - [ ] IncomeVoucherStrategy.java（收入策略）
  - [ ] CostVoucherStrategy.java（成本策略）
  - [ ] VoucherService.java（生成逻辑）
  - [ ] 借贷平衡检查
  - [ ] 单元测试（覆盖率 > 90%）

#### 初级开发A（2h）
- [ ] project（项目管理）1h
  - [ ] Project.java
  - [ ] ProjectMapper.java + XML
  - [ ] ProjectService.java
  - [ ] ProjectController.java
- [ ] org_unit（组织管理）1h
  - [ ] OrgUnit.java
  - [ ] OrgUnitMapper.java + XML
  - [ ] OrgUnitService.java
  - [ ] OrgUnitController.java

#### 初级开发B（2h）
- [ ] 项目管理页面 2h
  - [ ] ProjectList.vue
  - [ ] ProjectModal.vue
  - [ ] api/project.ts
  - [ ] 测试：项目类型、预算管理

#### 测试工程师（2h）
- [ ] 单元测试
  - [ ] 执行 account_subject 单元测试
  - [ ] 执行 counterparty 单元测试
  - [ ] 执行 voucher 单元测试
  - [ ] 记录测试结果

---

### Day 1 交付物

- ✅ 4个主数据模块（后端+前端）
  - account_subject（会计科目）
  - counterparty（客户/供应商）
  - project（项目管理）
  - org_unit（组织管理）
- ✅ 凭证实体和自动生成策略
- ✅ 单元测试覆盖率 > 70%
- ✅ 数据库表创建完成

---

## 📅 Day 2：凭证管理 + 收支管理（2026-05-06，22小时）

**目标：** 完成凭证管理和收支管理核心功能

### 上午（11小时）

#### 架构师（1h）
- [ ] 接口设计评审
  - [ ] 收支管理接口设计
  - [ ] 凭证管理接口设计
  - [ ] 审批流程接口设计

#### 高级开发（4h）
- [ ] 凭证审批流程 2h
  - [ ] VoucherController.java（审批接口）
  - [ ] VoucherService.java（审批逻辑）
  - [ ] 状态流转（draft → reviewing → approved）
  - [ ] 单元测试
- [ ] fact_event_service 2h
  - [ ] FactEventService.java
  - [ ] 创建收支时自动生成凭证
  - [ ] 幂等性检查（idempotency_key）
  - [ ] 集成测试

#### 初级开发A（2h）
- [ ] fact_event 实体 2h
  - [ ] FactEvent.java（32个字段）
  - [ ] FactEventMapper.java + XML
  - [ ] 复杂查询（按日期、组织、类型、状态）
  - [ ] 分页查询

#### 初级开发B（5h）⭐ 复杂页面
- [ ] 凭证列表页面 2h
  - [ ] VoucherList.vue
  - [ ] api/voucher.ts
  - [ ] 按期间筛选
  - [ ] 按状态筛选
- [ ] 凭证详情页面 3h
  - [ ] VoucherDetail.vue
  - [ ] VoucherEntryTable.vue（凭证分录表格）
  - [ ] 显示借贷合计
  - [ ] 审批操作

#### DBA（1h）
- [ ] 索引优化
  - [ ] 分析慢查询
  - [ ] 优化索引
  - [ ] 查询性能测试

#### 测试工程师（2h）
- [ ] 集成测试
  - [ ] 凭证自动生成测试
  - [ ] 借贷平衡测试
  - [ ] 审批流程测试

---

### 下午（11小时）

#### 高级开发（3h）
- [ ] 审批流程通用化 3h
  - [ ] ApprovalFlow.java
  - [ ] ApprovalFlowMapper.java + XML
  - [ ] ApprovalFlowService.java
  - [ ] ApprovalFlowController.java
  - [ ] 状态机实现
  - [ ] 单元测试

#### 初级开发A（1.5h）
- [ ] fact_template（收支模板）1.5h
  - [ ] FactTemplate.java
  - [ ] FactTemplateMapper.java + XML
  - [ ] FactTemplateService.java
  - [ ] FactTemplateController.java

#### 初级开发B（4h）
- [ ] 收支列表页面 2h
  - [ ] FactEventList.vue
  - [ ] api/factEvent.ts
  - [ ] 按日期筛选
  - [ ] 按类型筛选
- [ ] 收支录入页面 2h
  - [ ] FactEventQuickForm.vue（快速模式）
  - [ ] 只显示必填字段
  - [ ] 从模板创建
  - [ ] 实时预览凭证

#### 测试工程师（2h）
- [ ] 集成测试
  - [ ] 收支→凭证→预算 完整流程测试
  - [ ] 审批流程测试
  - [ ] 记录测试结果

---

### Day 2 交付物

- ✅ 凭证管理完整功能
- ✅ 收支管理基础功能
- ✅ 审批流程通用化
- ✅ 集成测试通过

---

## 📅 Day 3：辅助功能（2026-05-07，18小时）

**目标：** 完成预算、审批、审计等辅助功能

### 上午（9小时）

#### 架构师（1h）
- [ ] 代码审查
  - [ ] 审查 Day 2 代码
  - [ ] 性能优化建议

#### 高级开发（1h）
- [ ] 审计日志 AOP 1h
  - [ ] AuditLogAspect.java
  - [ ] @AuditLog 注解
  - [ ] 异步写入
  - [ ] 单元测试

#### 初级开发A（2.5h）
- [ ] budget（预算管理）1.5h
  - [ ] Budget.java
  - [ ] BudgetMapper.java + XML
  - [ ] BudgetService.java（含执行进度更新）
  - [ ] BudgetController.java
- [ ] audit_log（审计日志）1h
  - [ ] AuditLog.java
  - [ ] AuditLogMapper.java + XML
  - [ ] AuditLogService.java
  - [ ] AuditLogController.java

#### 初级开发B（4h）
- [ ] 收支模板管理 2h
  - [ ] FactTemplateList.vue
  - [ ] FactTemplateModal.vue
  - [ ] 保存当前收支为模板
- [ ] 预算管理页面 2h
  - [ ] BudgetList.vue
  - [ ] BudgetModal.vue
  - [ ] BudgetProgress.vue（执行进度组件）

#### 测试工程师（2h）
- [ ] 功能测试
  - [ ] 预算管理测试
  - [ ] 收支模板测试
  - [ ] 审计日志测试

---

### 下午（9小时）

#### 高级开发（2h）
- [ ] 性能优化 2h
  - [ ] account_subject 缓存
  - [ ] audit_log 异步写入
  - [ ] 批量插入优化

#### 初级开发A（2h）
- [ ] attribution（归因管理）1h
  - [ ] Attribution.java
  - [ ] AttributionMapper.java + XML
  - [ ] AttributionService.java
  - [ ] AttributionController.java
- [ ] metric_snapshot（指标快照）1h
  - [ ] MetricSnapshot.java
  - [ ] MetricSnapshotMapper.java + XML
  - [ ] MetricSnapshotService.java
  - [ ] MetricSnapshotController.java

#### 初级开发B（3h）
- [ ] 审批流程页面 2h
  - [ ] ApprovalFlowList.vue
  - [ ] ApprovalFlowDetail.vue
  - [ ] 流程图展示
- [ ] 审计日志页面 1h
  - [ ] AuditLogList.vue
  - [ ] 按用户、表名、时间查询

#### DBA（1h）
- [ ] 分区表实施
  - [ ] audit_log 按月分区
  - [ ] 验证分区效果

#### 测试工程师（2h）
- [ ] 性能测试
  - [ ] 收支录入性能测试（100并发）
  - [ ] 凭证查询性能测试（200并发）
  - [ ] 审计日志性能测试（50并发）

---

### Day 3 交付物

- ✅ 所有辅助功能完成
- ✅ 性能优化完成
- ✅ 功能测试通过
- ✅ 性能测试达标

---

## 📅 Day 4：完善和测试（2026-05-08，14小时）

**目标：** 完成剩余模块，全面测试

### 上午（7小时）

#### 架构师（1h）
- [ ] 系统集成测试
  - [ ] 完整业务流程测试
  - [ ] 异常场景测试

#### 高级开发（2h）
- [ ] Bug 修复
  - [ ] 修复 Day 1-3 发现的 Bug
  - [ ] 代码优化

#### 初级开发A（2h）
- [ ] 剩余模块
  - [ ] goal（目标管理）0.5h
  - [ ] action_record（行动记录）0.5h
  - [ ] budget_adjustment（预算调整）0.5h
  - [ ] user（用户管理）0.5h

#### 初级开发B（2h）
- [ ] UI 优化
  - [ ] 统一样式
  - [ ] 响应式适配
  - [ ] 用户体验优化

#### 测试工程师（4h）
- [ ] E2E 测试
  - [ ] 完整收支录入流程（10个用例）
  - [ ] 凭证审批流程（8个用例）
  - [ ] 预算管理流程（7个用例）
  - [ ] 异常场景测试（10个用例）

---

### 下午（7小时）

#### 全员（4h）
- [ ] Bug 修复
  - [ ] 修复 E2E 测试发现的 Bug
  - [ ] 回归测试

#### 测试工程师（3h）
- [ ] 回归测试
  - [ ] 所有功能回归测试
  - [ ] 性能回归测试
  - [ ] 生成测试报告

---

### Day 4 交付物

- ✅ 所有模块完成（19个后端模块 + 12个前端页面）
- ✅ E2E 测试通过
- ✅ Bug 修复完成
- ✅ 测试报告生成

---

## 📅 Day 5：上线准备（2026-05-09，10小时）

**目标：** 最终测试，准备上线

### 上午（5小时）

#### 架构师（1h）
- [ ] 部署方案
  - [ ] 生产环境配置
  - [ ] 部署脚本
  - [ ] 回滚方案

#### DBA（1h）
- [ ] 数据库备份和优化
  - [ ] 生产数据库备份
  - [ ] 索引优化
  - [ ] 性能调优

#### 全员（3h）
- [ ] 最终测试
  - [ ] 生产环境验证
  - [ ] 性能压测
  - [ ] 安全测试

---

### 下午（5小时）

#### 全员（2h）
- [ ] 文档编写
  - [ ] API 文档
  - [ ] 用户手册
  - [ ] 运维手册

#### 全员（2h）
- [ ] 上线演练
  - [ ] 部署演练
  - [ ] 回滚演练
  - [ ] 应急预案

#### 架构师（1h）
- [ ] 上线总结
  - [ ] 项目总结报告
  - [ ] 经验教训
  - [ ] 后续优化计划

---

### Day 5 交付物

- ✅ 系统上线
- ✅ 文档完整
- ✅ 培训完成
- ✅ 项目总结

---

## 📊 工作量统计

| 角色 | Day 1 | Day 2 | Day 3 | Day 4 | Day 5 | 总计 |
|------|-------|-------|-------|-------|-------|------|
| 架构师 | 3h | 1h | 1h | 1h | 2h | 8h |
| 高级开发 | 6h | 7h | 3h | 2h | 1h | 19h |
| 初级开发A | 5h | 3.5h | 4.5h | 2h | 1h | 16h |
| 初级开发B | 6h | 9h | 7h | 2h | 1h | 25h |
| DBA | 2h | 1h | 1h | 0h | 1h | 5h |
| 测试工程师 | 4h | 4h | 4h | 7h | 3h | 22h |
| **总计** | **26h** | **25.5h** | **20.5h** | **14h** | **9h** | **95h** |

---

## 🎯 关键里程碑

| 里程碑 | 时间 | 交付物 | 状态 |
|--------|------|--------|------|
| M1：主数据完成 | Day 1 | 4个主数据模块 + 凭证策略 | ⏳ |
| M2：会计模块完成 | Day 2 | 凭证管理 + 收支管理 | ⏳ |
| M3：辅助功能完成 | Day 3 | 预算、审批、审计 | ⏳ |
| M4：测试完成 | Day 4 | 所有测试通过 | ⏳ |
| M5：系统上线 | Day 5 | 生产环境部署 | ⏳ |

---

## 📝 每日站会

**时间：** 每天 09:00 和 17:00  
**时长：** 15分钟  
**内容：**
- 昨天完成了什么
- 今天计划做什么
- 遇到什么问题

---

## 🔍 质量检查点

### 代码质量
- [ ] 代码审查通过率 100%
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 代码规范检查通过

### 测试质量
- [ ] 单元测试通过率 100%
- [ ] 集成测试通过率 100%
- [ ] E2E 测试通过率 100%

### 性能质量
- [ ] 响应时间 < 500ms
- [ ] 并发 100 用户无压力
- [ ] 数据库查询优化

---

**看板维护：** 旺仔助手  
**最后更新：** 2026-05-04 22:00  
**下一步：** 更新数据库，明天开始 Day 1
