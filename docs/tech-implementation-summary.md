# 技术落地方案总结

**版本：** v4.0  
**日期：** 2026-05-04  
**状态：** ✅ 已批准

---

## 🎯 核心技术方案

### 1. 凭证自动生成（最复杂）

**问题：** 用户录入收支后，如何自动生成会计凭证？

**方案：** 策略模式 + 模板方法

```java
// 策略接口
public interface VoucherGenerateStrategy {
    List<VoucherEntry> generate(FactEvent factEvent);
}

// 收入策略
public class IncomeVoucherStrategy implements VoucherGenerateStrategy {
    @Override
    public List<VoucherEntry> generate(FactEvent factEvent) {
        // 收入：借银行存款，贷主营业务收入
        return Arrays.asList(
            createDebitEntry("1002", factEvent.getAmount()),   // 借：银行存款
            createCreditEntry("6001", factEvent.getAmount())   // 贷：主营业务收入
        );
    }
}

// 成本策略
public class CostVoucherStrategy implements VoucherGenerateStrategy {
    @Override
    public List<VoucherEntry> generate(FactEvent factEvent) {
        // 成本：借主营业务成本，贷银行存款
        return Arrays.asList(
            createDebitEntry("6401", factEvent.getAmount()),   // 借：主营业务成本
            createCreditEntry("1002", factEvent.getAmount())   // 贷：银行存款
        );
    }
}

// 服务层
@Service
public class VoucherService {
    
    @Autowired
    private Map<String, VoucherGenerateStrategy> strategyMap;
    
    @Transactional
    public Voucher generateFromFactEvent(FactEvent factEvent) {
        // 1. 创建凭证头
        Voucher voucher = createVoucherHeader(factEvent);
        
        // 2. 根据策略生成凭证分录
        String strategyKey = factEvent.getType(); // "income" or "cost"
        VoucherGenerateStrategy strategy = strategyMap.get(strategyKey);
        List<VoucherEntry> entries = strategy.generate(factEvent);
        
        // 3. 保存凭证分录
        entries.forEach(entry -> entry.setVoucherId(voucher.getId()));
        voucherEntryMapper.insertBatch(entries);
        
        // 4. 检查借贷平衡
        checkBalance(voucher, entries);
        
        return voucher;
    }
}
```

**优点：**
- ✅ 易于扩展（新增业务类型只需新增策略）
- ✅ 符合开闭原则
- ✅ 易于测试

---

### 2. 审批流程通用化

**问题：** 多种业务（收支、预算、凭证）都需要审批，如何统一？

**方案：** 状态机模式

```java
// 审批状态枚举
public enum ApprovalStatus {
    DRAFT("草稿"),
    PENDING("待审批"),
    REVIEWING("审核中"),
    APPROVED("已批准"),
    REJECTED("已拒绝");
}

// 审批流程实体
@Data
public class ApprovalFlow {
    private Long id;
    private String businessType;    // "fact_event", "budget", "voucher"
    private Long businessId;
    private Integer currentStep;    // 当前步骤（1/2/3）
    private Integer totalSteps;     // 总步骤数
    private ApprovalStatus status;
}

// 审批服务
@Service
public class ApprovalFlowService {
    
    // 提交审批
    @Transactional
    public void submit(String businessType, Long businessId) {
        ApprovalFlow flow = new ApprovalFlow();
        flow.setBusinessType(businessType);
        flow.setBusinessId(businessId);
        flow.setCurrentStep(1);
        flow.setTotalSteps(getApprovalSteps(businessType));
        flow.setStatus(ApprovalStatus.PENDING);
        
        approvalFlowMapper.insert(flow);
        notifyApprover(flow);
    }
    
    // 审批通过
    @Transactional
    public void approve(Long flowId, Long approverId) {
        ApprovalFlow flow = approvalFlowMapper.selectById(flowId);
        
        if (flow.getCurrentStep() < flow.getTotalSteps()) {
            // 进入下一步
            flow.setCurrentStep(flow.getCurrentStep() + 1);
            notifyApprover(flow);
        } else {
            // 审批完成
            flow.setStatus(ApprovalStatus.APPROVED);
            updateBusinessStatus(flow.getBusinessType(), flow.getBusinessId(), "approved");
        }
        
        approvalFlowMapper.updateById(flow);
    }
}
```

**优点：**
- ✅ 支持多种业务类型
- ✅ 支持多级审批
- ✅ 状态流转清晰

---

### 3. 预算执行进度自动更新

**问题：** 每次录入收支后，如何自动更新预算执行进度？

**方案：** 事件驱动 + 定时任务

```java
@Service
public class BudgetService {
    
    /**
     * 更新预算执行进度
     * 在 fact_event 创建后触发
     */
    @Transactional
    public void updateProgress(Long budgetId) {
        Budget budget = budgetMapper.selectById(budgetId);
        
        // 计算已用金额（从 fact_event 表聚合）
        BigDecimal usedAmount = factEventMapper.sumAmountByBudget(
            budget.getOrgUnitId(),
            budget.getPeriod(),
            budget.getCategory()
        );
        
        // 更新预算
        budget.setUsedAmount(usedAmount);
        budget.setRemainingAmount(
            budget.getBudgetedAmount().subtract(usedAmount)
        );
        
        budgetMapper.updateById(budget);
        
        // 预算超支预警
        if (budget.getRemainingAmount().compareTo(BigDecimal.ZERO) < 0) {
            sendBudgetAlert(budget);
        }
    }
}

// 事件监听器
@Component
public class FactEventListener {
    
    @Autowired
    private BudgetService budgetService;
    
    @EventListener
    @Async
    public void onFactEventCreated(FactEventCreatedEvent event) {
        // 异步更新预算执行进度
        budgetService.updateProgress(event.getBudgetId());
    }
}
```

---

### 4. 审计日志自动记录

**问题：** 如何自动记录所有操作日志？

**方案：** AOP 切面

```java
// 自定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String action(); // "create", "update", "delete"
}

// AOP 切面
@Aspect
@Component
public class AuditLogAspect {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        // 1. 获取操作前的值
        Object oldValue = getOldValue(pjp);
        
        // 2. 执行方法
        Object result = pjp.proceed();
        
        // 3. 获取操作后的值
        Object newValue = getNewValue(pjp, result);
        
        // 4. 异步记录日志
        auditLogService.saveAsync(
            getCurrentUserId(),
            auditLog.action(),
            getTableName(pjp),
            getRecordId(pjp),
            oldValue,
            newValue
        );
        
        return result;
    }
}

// 使用示例
@Service
public class FactEventService {
    
    @AuditLog(action = "create")
    public FactEvent create(FactEventCreateDTO dto) {
        // 业务逻辑
    }
    
    @AuditLog(action = "update")
    public void update(Long id, FactEventUpdateDTO dto) {
        // 业务逻辑
    }
}
```

---

## 📋 开发任务拆分

### 后端任务（19个模块，84小时）

| 优先级 | 模块 | 负责人 | 时间 | 说明 |
|--------|------|--------|------|------|
| **P0** | account_subject | 初级A | 1.5h | 会计科目（树形结构） |
| **P0** | counterparty | 初级A | 1.5h | 客户/供应商 |
| **P0** | project | 初级A | 1h | 项目管理 |
| **P0** | org_unit | 初级A | 1h | 组织管理 |
| **P0** | voucher | 高级 | 2h | 凭证实体 |
| **P0** | voucher_entry | 高级 | 1h | 凭证明细 |
| **P0** | voucher_generate | 高级 | 4h | 凭证自动生成（核心） |
| **P0** | fact_event | 初级A | 2h | 收支实体 |
| **P0** | fact_event_service | 高级 | 2h | 收支业务逻辑 |
| **P1** | fact_template | 初级A | 1.5h | 收支模板 |
| **P1** | budget | 初级A | 1.5h | 预算管理 |
| **P1** | approval_flow | 高级 | 3h | 审批流程 |
| **P1** | audit_log | 初级A | 1h | 审计日志 |
| **P2** | attribution | 初级A | 1h | 归因管理 |
| **P2** | metric_snapshot | 初级A | 1h | 指标快照 |
| **P2** | goal | 初级A | 0.5h | 目标管理 |
| **P2** | action_record | 初级A | 0.5h | 行动记录 |
| **P2** | budget_adjustment | 初级A | 0.5h | 预算调整 |
| **P2** | user | 初级A | 0.5h | 用户管理 |

**关键路径：** voucher_generate（4h）是最复杂的模块

---

### 前端任务（12个页面，48小时）

| 优先级 | 页面 | 负责人 | 时间 | 说明 |
|--------|------|--------|------|------|
| **P0** | 会计科目管理 | 初级B | 2h | 树形结构 |
| **P0** | 客户供应商管理 | 初级B | 2h | 列表+表单 |
| **P0** | 项目管理 | 初级B | 2h | 列表+表单 |
| **P0** | 凭证列表 | 初级B | 2h | 列表+筛选 |
| **P0** | 凭证详情 | 初级B | 3h | 凭证分录表格（复杂） |
| **P0** | 收支列表 | 初级B | 2h | 列表+筛选 |
| **P0** | 收支快速录入 | 初级B | 2h | 只填必填字段 |
| **P0** | 收支完整录入 | 初级B | 2h | 所有字段 |
| **P1** | 收支模板管理 | 初级B | 2h | 列表+表单 |
| **P1** | 预算管理 | 初级B | 2h | 执行进度展示 |
| **P1** | 审批流程 | 初级B | 2h | 流程图 |
| **P1** | 审计日志 | 初级B | 1h | 只读列表 |

**关键路径：** 凭证详情（3h）需要展示复杂的凭证分录

---

## 📅 5天开发计划

### Day 1：主数据 + 会计基础（20小时）

**目标：** 完成 4 个主数据模块 + 凭证自动生成策略

**上午（10h）：**
- 架构师：技术架构设计（2h）
- DBA：创建数据库表（2h）
- 高级开发：voucher 实体和 Mapper（2h）
- 初级A：account_subject + counterparty（3h）
- 初级B：会计科目页面 + 客户供应商页面（4h）
- 测试：编写测试用例（2h）

**下午（10h）：**
- 架构师：代码审查（1h）
- 高级开发：凭证自动生成策略（4h）⭐ 核心
- 初级A：project + org_unit（2h）
- 初级B：项目管理页面（2h）
- 测试：单元测试（2h）

**交付物：**
- ✅ 4个主数据模块（后端+前端）
- ✅ 凭证自动生成策略（核心功能）
- ✅ 单元测试覆盖率 > 70%

---

### Day 2：凭证管理 + 收支管理（22小时）

**目标：** 完成凭证管理和收支管理核心功能

**上午（11h）：**
- 架构师：接口设计评审（1h）
- 高级开发：凭证审批流程（2h）+ fact_event_service（2h）
- 初级A：fact_event 实体（2h）
- 初级B：凭证列表 + 凭证详情（5h）⭐ 复杂页面
- DBA：索引优化（1h）
- 测试：集成测试（2h）

**下午（11h）：**
- 高级开发：审批流程通用化（3h）
- 初级A：fact_template（1.5h）
- 初级B：收支列表 + 收支录入（4h）
- 测试：集成测试（2h）

**交付物：**
- ✅ 凭证管理完整功能
- ✅ 收支管理基础功能
- ✅ 集成测试通过

---

### Day 3：辅助功能（18小时）

**目标：** 完成预算、审批、审计等辅助功能

**上午（9h）：**
- 架构师：代码审查（1h）
- 高级开发：审计日志 AOP（1h）
- 初级A：budget + audit_log（2.5h）
- 初级B：收支模板 + 预算管理（4h）
- 测试：功能测试（2h）

**下午（9h）：**
- 高级开发：性能优化（2h）
- 初级A：attribution + metric_snapshot（2h）
- 初级B：审批流程页面 + 审计日志页面（3h）
- DBA：分区表实施（1h）
- 测试：性能测试（2h）

**交付物：**
- ✅ 所有辅助功能完成
- ✅ 性能优化完成
- ✅ 功能测试通过

---

### Day 4：完善和测试（14小时）

**目标：** 完成剩余模块，全面测试

**上午（7h）：**
- 架构师：系统集成测试（1h）
- 高级开发：Bug 修复（2h）
- 初级A：剩余模块（goal, action_record等）（2h）
- 初级B：UI 优化（2h）
- 测试：E2E 测试（4h）

**下午（7h）：**
- 全员：Bug 修复（4h）
- 测试：回归测试（3h）

**交付物：**
- ✅ 所有模块完成
- ✅ E2E 测试通过
- ✅ Bug 修复完成

---

### Day 5：上线准备（10小时）

**目标：** 最终测试，准备上线

**上午（5h）：**
- 架构师：部署方案（1h）
- DBA：数据库备份和优化（1h）
- 全员：最终测试（3h）

**下午（5h）：**
- 全员：文档编写（2h）
- 全员：上线演练（2h）
- 架构师：上线总结（1h）

**交付物：**
- ✅ 系统上线
- ✅ 文档完整
- ✅ 培训完成

---

## ✅ 质量保证

### 1. 代码质量

- **代码审查：** 所有代码必须经过高级开发审查
- **单元测试：** 核心模块覆盖率 ≥ 80%
- **代码规范：** ESLint + Prettier
- **静态分析：** SonarQube 扫描

### 2. 测试质量

- **单元测试：** 120个用例（12小时）
- **集成测试：** 60个用例（8小时）
- **E2E测试：** 35个用例（4小时）
- **总计：** 215个用例（24小时）

### 3. 性能指标

| 场景 | 并发数 | 响应时间 | TPS |
|------|--------|----------|-----|
| 收支录入 | 100 | < 500ms | > 200 |
| 凭证查询 | 200 | < 300ms | > 500 |
| 审计日志 | 50 | < 1s | > 100 |

---

## 🎯 关键风险与应对

| 风险 | 等级 | 应对措施 |
|------|------|----------|
| 凭证自动生成逻辑复杂 | 🔴 高 | 高级开发负责，结对编程，充分测试 |
| 借贷平衡检查 | 🟡 中 | 增加单元测试，代码审查 |
| 前端页面较多 | 🟡 中 | 使用 jeecg-boot 代码生成器 |
| 测试时间紧张 | 🟢 低 | 开发过程中同步编写单元测试 |

---

## 📝 下一步行动

### 今晚（2026-05-04）

1. **DBA：** 更新数据库表结构
2. **架构师：** 准备技术架构文档
3. **初级开发：** 学习复式记账原理（4h）

### 明天（Day 1）

1. **09:00** 晨会（15分钟）
2. **09:15** 开始开发
3. **12:00** 午餐
4. **13:00** 继续开发
5. **17:00** 每日站会（15分钟）
6. **17:15** 代码审查

---

**文档编制：** 旺仔助手（技术总监）  
**编制日期：** 2026-05-04  
**文档状态：** ✅ 已批准，可以实施
