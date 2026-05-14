# account_subject 领域设计

## 目标

会计科目是财务核算的基础编码体系，属于主数据域。所有收支事实（FactEvent）在归因时都必须关联到具体的会计科目。科目管理必须先于事实录入落地。

## 领域定位

```text
系统全景中的位置：

  主数据层
  ┌─────────────────────────────────────────────┐
  │  AccountSubject ◄── 本模块                   │
  │  Counterparty                                │
  │  OrgUnit / Project / Contract / Budget       │
  └──────────────────────┬──────────────────────┘
                         │ 被引用
                         ▼
  事实层：FactEvent（每笔收支必须挂科目）
  归因层：Attribution（按科目维度归因）
  指标层：MetricSnapshot（按科目汇总）
```

`account_subject` 只承担主数据的 CRUD + 树形结构维护，不参与事实计算、归因计算或指标计算。

---

## UML 类图

```text
┌──────────────────────────────────────────────────────────────┐
│                    <<Aggregate Root>>                         │
│                     AccountSubject                            │
├──────────────────────────────────────────────────────────────┤
│ - id: Long                                                   │
│ - code: String              «唯一，不可修改»                  │
│ - name: String                                               │
│ - parentId: Long            «null = 根科目»                   │
│ - level: Integer            «由 parentId 推导，不允许手填»     │
│ - type: AccountSubjectType  «值对象»                          │
│ - debitCredit: DebitCredit   «值对象»                          │
│ - enabled: Boolean          «默认 true»                       │
│ - sort: Integer                                              │
│ - remark: String                                             │
│ - createdAt: LocalDateTime                                   │
│ - updatedAt: LocalDateTime                                   │
├──────────────────────────────────────────────────────────────┤
│ + create(code, name, parentId, type, debitCredit,            │
│          sort, remark): AccountSubject                        │
│ + update(name, parentId, type, debitCredit,                   │
│          sort, remark, enabled): void                         │
│ + enable(): void                                             │
│ + disable(): void                                            │
│ + changeParent(newParentId): void                            │
│ + isRoot(): boolean                                          │
│ + calculateLevel(parentLevel): void                           │
│ + validateParentNotSelf(newParentId): void                    │
└──────────────────────────────────────────────────────────────┘
         │ type                          │ debitCredit
         ▼                              ▼
┌─────────────────────┐    ┌──────────────────────┐
│  <<Value Object>>   │    │   <<Value Object>>   │
│ AccountSubjectType  │    │     DebitCredit       │
├─────────────────────┤    ├──────────────────────┤
│ ASSET               │    │ DEBIT                │
│ LIABILITY           │    │ CREDIT               │
│ EQUITY              │    └──────────────────────┘
│ COST                │
│ PROFIT_LOSS         │
├─────────────────────┤
│ + fromCode(String)  │
│ + toCode(): String  │
└─────────────────────┘


┌──────────────────────────────────────────────────────────────┐
│                   <<Repository Interface>>                     │
│                 AccountSubjectRepository                       │
├──────────────────────────────────────────────────────────────┤
│ + findById(id: Long): Optional<AccountSubject>                │
│ + findByCode(code: String): Optional<AccountSubject>          │
│ + findAll(): List<AccountSubject>                             │
│ + findByParentId(parentId: Long): List<AccountSubject>        │
│ + findAllDescendantIds(id: Long): Set<Long>                   │
│ + existsByCode(code: String): boolean                         │
│ + hasChildren(id: Long): boolean                              │
│ + save(subject: AccountSubject): AccountSubject               │
│ + deleteById(id: Long): void                                  │
│ + search(keyword: String): List<AccountSubject>               │
└──────────────────────────────────────────────────────────────┘
               ▲ implements
               │
┌──────────────────────────────────────────────────────────────┐
│                <<Infrastructure>>                             │
│            AccountSubjectRepositoryImpl                       │
├──────────────────────────────────────────────────────────────┤
│ - mapper: AccountSubjectMapper                                │
│ - converter: AccountSubjectConverter                          │
├──────────────────────────────────────────────────────────────┤
│   实现所有 Repository 方法                                    │
│   Entity ↔ Domain 转换在此层完成                              │
└──────────────────────────────────────────────────────────────┘
               │ uses
               ▼
┌──────────────────────────────────────────────────────────────┐
│                    <<Mapper>>                                  │
│               AccountSubjectMapper                            │
│            extends BaseMapper<AccountSubjectEntity>           │
├──────────────────────────────────────────────────────────────┤
│ + selectByKeyword(keyword: String): List<AccountSubjectEntity>│
│ + selectDescendantIds(id: Long): Set<Long>                    │
└──────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────┐
│                  <<Domain Service>>                            │
│             AccountSubjectDomainService                       │
├──────────────────────────────────────────────────────────────┤
│ - repository: AccountSubjectRepository                        │
├──────────────────────────────────────────────────────────────┤
│ + validateCodeUnique(code: String): void                      │
│ + validateParentExists(parentId: Long): AccountSubject         │
│ + validateNotCircular(id: Long, newParentId: Long): void      │
│ + validateNoDependentChildren(id: Long): void                 │
│ + resolveLevel(parentId: Long): int                           │
│ + buildTree(subjects: List<AccountSubject>):                  │
│       List<AccountSubjectTreeNode>                            │
└──────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────┐
│                  <<Application Service>>                       │
│              AccountSubjectAppService                         │
├──────────────────────────────────────────────────────────────┤
│ - domainService: AccountSubjectDomainService                  │
│ - repository: AccountSubjectRepository                        │
├──────────────────────────────────────────────────────────────┤
│ + @Transactional createSubject(dto): AccountSubjectVO         │
│ + @Transactional updateSubject(id, dto): AccountSubjectVO     │
│ + @Transactional deleteSubject(id): void                      │
│ + @Transactional toggleStatus(id, enabled): void              │
│ + getTree(keyword): List<AccountSubjectVO>                    │
│ + getDetail(id): AccountSubjectVO                             │
└──────────────────────────────────────────────────────────────┘
```

### 类关系总结

```text
AccountSubjectController
    │ 调用
    ▼
AccountSubjectAppService          ← @Transactional, DTO→Domain 转换
    │ 调用            │ 调用
    ▼                 ▼
DomainService     Repository(接口)
    │ 调用            ↑ 实现
    ▼                 │
Repository ◄────── RepositoryImpl ──→ Mapper ──→ MySQL
```

---

## UML 状态图 — AccountSubject 实体生命周期

```text
                    ┌─────────┐
                    │  START  │
                    └────┬────┘
                         │ create()
                         ▼
              ┌────────────────────┐
              │      ENABLED       │
              │  (enabled = true)  │
              └──┬──────────┬──────┘
                 │          │
    disable()    │          │  update() / changeParent()
                 │          │  (仍然保持 ENABLED)
                 ▼          └──→ 自身
              ┌────────────────────┐
              │     DISABLED       │
              │ (enabled = false)  │
              └──┬──────────┬──────┘
                 │          │
     enable()    │          │  update() (仍然保持 DISABLED)
                 │          │
                 ▼          └──→ 自身
              ┌────────────────────┐
              │      ENABLED       │
              │ (enabled = true)   │
              └──────────┬─────────┘
                         │
                         │ delete()
                         │ 前提：无子节点
                         ▼
                    ┌─────────┐
                    │   END   │
                    └─────────┘
```

**状态转换规则：**

| 当前状态 | 事件 | 目标状态 | 前置条件 |
|---------|------|---------|---------|
| - | create() | ENABLED | code 唯一；parentId 合法（存在或为 null）；type/debitCredit 合法 |
| ENABLED | disable() | DISABLED | MVP 阶段无前置条件，后续补引用检查 |
| DISABLED | enable() | ENABLED | 无 |
| ENABLED/DISABLED | update() | 不变 | code 不可修改；若 parentId 变更需校验循环依赖 |
| ENABLED/DISABLED | changeParent() | 不变 | 新 parentId 不是自身且不是子孙节点 |
| ENABLED/DISABLED | delete() | END | 必须无子节点 |

---

## UML 时序图

### 时序图 1：新增科目

```text
Client          Controller          AppService          DomainService         Repository           DB
  │                │                    │                     │                    │                 │
  │ POST /api/     │                    │                     │                    │                 │
  │ account-subject│                    │                     │                    │                 │
  │ {code,name,    │                    │                     │                    │                 │
  │  parentId,...}  │                    │                     │                    │                 │
  │───────────────>│                    │                     │                    │                 │
  │                │ @Valid 校验入参      │                     │                    │                 │
  │                │ createSubject(dto)  │                     │                    │                 │
  │                │───────────────────>│                     │                    │                 │
  │                │                    │ validateCodeUnique   │                    │                 │
  │                │                    │───────────────────>│                    │                 │
  │                │                    │                     │ existsByCode(code) │                 │
  │                │                    │                     │───────────────────>│                 │
  │                │                    │                     │                    │ SELECT count    │
  │                │                    │                     │                    │────────────────>│
  │                │                    │                     │                    │<────────────────│
  │                │                    │                     │<───────────────────│                 │
  │                │                    │                     │ 若已存在→抛         │                 │
  │                │                    │                     │ BusinessException   │                 │
  │                │                    │                     │                    │                 │
  │                │                    │ validateParentExists │                    │                 │
  │                │                    │───────────────────>│                    │                 │
  │                │                    │                     │ findById(parentId) │                 │
  │                │                    │                     │───────────────────>│                 │
  │                │                    │                     │<───────────────────│                 │
  │                │                    │                     │ 若不存在→抛         │                 │
  │                │                    │                     │ BusinessException   │                 │
  │                │                    │                     │                    │                 │
  │                │                    │ resolveLevel         │                    │                 │
  │                │                    │───────────────────>│                    │                 │
  │                │                    │                     │ return parent.level │                 │
  │                │                    │                     │ + 1                │                 │
  │                │                    │<───────────────────│                    │                 │
  │                │                    │                     │                    │                 │
  │                │                    │ AccountSubject       │                    │                 │
  │                │                    │ .create(...)         │                    │                 │
  │                │                    │ 构建聚合根实例        │                    │                 │
  │                │                    │                     │                    │                 │
  │                │                    │ repository.save      │                    │                 │
  │                │                    │───────────────────────────────────────>│                 │
  │                │                    │                     │                    │ INSERT INTO     │
  │                │                    │                     │                    │────────────────>│
  │                │                    │                     │                    │<────────────────│
  │                │                    │<───────────────────────────────────────│                 │
  │                │                    │                     │                    │                 │
  │                │  AccountSubjectVO  │                     │                    │                 │
  │                │<───────────────────│                     │                    │                 │
  │ ApiResponse    │                    │                     │                    │                 │
  │<───────────────│                    │                     │                    │                 │
```

### 时序图 2：编辑科目（含变更父节点）

```text
Client          Controller          AppService          DomainService         Repository           DB
  │                │                    │                     │                    │                 │
  │ PUT /api/      │                    │                     │                    │                 │
  │ account-subject│                    │                     │                    │                 │
  │ /{id}          │                    │                     │                    │                 │
  │───────────────>│                    │                     │                    │                 │
  │                │ updateSubject      │                     │                    │                 │
  │                │ (id, dto)          │                     │                    │                 │
  │                │───────────────────>│                     │                    │                 │
  │                │                    │ findById(id)        │                    │                 │
  │                │                    │───────────────────────────────────────>│                 │
  │                │                    │<───────────────────────────────────────│                 │
  │                │                    │ 若不存在→404         │                    │                 │
  │                │                    │                     │                    │                 │
  │                │                    │ 若 parentId 变更：   │                    │                 │
  │                │                    │ validateNotCircular  │                    │                 │
  │                │                    │───────────────────>│                    │                 │
  │                │                    │                     │ findAllDescendant  │                 │
  │                │                    │                     │ Ids(id)            │                 │
  │                │                    │                     │───────────────────>│                 │
  │                │                    │                     │<───────────────────│                 │
  │                │                    │                     │ 若 newParentId ∈   │                 │
  │                │                    │                     │ descendants →抛     │                 │
  │                │                    │                     │ BusinessException   │                 │
  │                │                    │<───────────────────│                    │                 │
  │                │                    │                     │                    │                 │
  │                │                    │ subject.update(...)  │                    │                 │
  │                │                    │ subject.calculateLevel(parentLevel)      │                 │
  │                │                    │ repository.save      │                    │                 │
  │                │                    │───────────────────────────────────────>│                 │
  │                │                    │<───────────────────────────────────────│                 │
  │                │  AccountSubjectVO  │                     │                    │                 │
  │                │<───────────────────│                     │                    │                 │
  │ ApiResponse    │                    │                     │                    │                 │
  │<───────────────│                    │                     │                    │                 │
```

### 时序图 3：删除科目

```text
Client          Controller          AppService          DomainService         Repository           DB
  │                │                    │                     │                    │                 │
  │ DELETE /api/   │                    │                     │                    │                 │
  │ account-subject│                    │                     │                    │                 │
  │ /{id}          │                    │                     │                    │                 │
  │───────────────>│                    │                     │                    │                 │
  │                │ deleteSubject(id)  │                     │                    │                 │
  │                │───────────────────>│                     │                    │                 │
  │                │                    │ findById(id)        │                    │                 │
  │                │                    │───────────────────────────────────────>│                 │
  │                │                    │ 若不存在→404         │                    │                 │
  │                │                    │                     │                    │                 │
  │                │                    │ validateNoDependentChildren             │                 │
  │                │                    │───────────────────>│                    │                 │
  │                │                    │                     │ hasChildren(id)    │                 │
  │                │                    │                     │───────────────────>│                 │
  │                │                    │                     │<───────────────────│                 │
  │                │                    │                     │ 若有子节点→抛       │                 │
  │                │                    │                     │ BusinessException   │                 │
  │                │                    │<───────────────────│                    │                 │
  │                │                    │                     │                    │                 │
  │                │                    │ repository.delete   │                    │                 │
  │                │                    │───────────────────────────────────────>│                 │
  │                │                    │                     │                    │ DELETE / 逻辑删除│
  │                │                    │                     │                    │────────────────>│
  │                │                    │<───────────────────────────────────────│                 │
  │                │ ApiResponse<Void>  │                     │                    │                 │
  │                │<───────────────────│                     │                    │                 │
  │ 200 OK         │                    │                     │                    │                 │
  │<───────────────│                    │                     │                    │                 │
```

### 时序图 4：启停科目

```text
Client          Controller          AppService          Repository           DB
  │                │                    │                    │                 │
  │ PATCH /api/    │                    │                    │                 │
  │ account-subject│                    │                    │                 │
  │ /{id}/status   │                    │                    │                 │
  │ {enabled:false}│                    │                    │                 │
  │───────────────>│                    │                    │                 │
  │                │ toggleStatus       │                    │                 │
  │                │ (id, enabled)      │                    │                 │
  │                │───────────────────>│                    │                 │
  │                │                    │ findById(id)       │                 │
  │                │                    │───────────────────>│                 │
  │                │                    │<───────────────────│                 │
  │                │                    │ 若不存在→404        │                 │
  │                │                    │                    │                 │
  │                │                    │ subject.disable()  │                 │
  │                │                    │ 或 subject.enable()│                 │
  │                │                    │                    │                 │
  │                │                    │ repository.save    │                 │
  │                │                    │───────────────────>│                 │
  │                │                    │                    │ UPDATE          │
  │                │                    │                    │────────────────>│
  │                │                    │<───────────────────│                 │
  │                │ ApiResponse<Void>  │                    │                 │
  │                │<───────────────────│                    │                 │
  │ 200 OK         │                    │                    │                 │
  │<───────────────│                    │                    │                 │
```

---

## 领域规则清单

| # | 规则 | 执行层 | 违反时异常 |
|---|------|--------|-----------|
| R1 | `code` 全局唯一 | DomainService | `BusinessException(40901, "科目编码已存在")` |
| R2 | `code` 创建后不可修改 | Domain (聚合根方法不暴露 setCode) | 编译期保证 |
| R3 | `parentId` 不能指向自身 | Domain.validateParentNotSelf() | `BusinessException(40001, "父科目不能是自身")` |
| R4 | `parentId` 不能指向自己的子孙节点 | DomainService.validateNotCircular() | `BusinessException(40002, "不能形成循环依赖")` |
| R5 | 有子节点的科目不能删除 | DomainService.validateNoDependentChildren() | `BusinessException(40903, "该科目下存在子科目，不能删除")` |
| R6 | `type` 必须是枚举内合法值 | 入参 @Valid + 值对象构造 | `MethodArgumentNotValidException` |
| R7 | `debitCredit` 必须是枚举内合法值 | 入参 @Valid + 值对象构造 | `MethodArgumentNotValidException` |
| R8 | `level` 由 parentId 推导 | DomainService.resolveLevel() | 前端/API 不允许传入 |
| R9 | 根科目 `parentId = null`，`level = 1` | DomainService.resolveLevel() | 自动计算 |
| R10 | `enabled` 默认 true | Domain.create() | 自动设置 |

---

## 领域事件（预留）

当前 MVP 阶段暂不发布领域事件，但预留以下事件供后续扩展：

| 事件 | 触发时机 | 潜在消费者 |
|------|---------|-----------|
| `AccountSubjectCreated` | 科目新增成功 | 审计日志 |
| `AccountSubjectUpdated` | 科目信息变更 | 审计日志、缓存失效 |
| `AccountSubjectDisabled` | 科目被停用 | 收支录入模块（校验科目是否可用） |
| `AccountSubjectDeleted` | 科目被删除 | 归因规则检查 |

---

## 异常处理策略

```text
Controller 层
  │
  │ 参数格式错误
  │ → GlobalExceptionHandler 捕获 MethodArgumentNotValidException
  │ → 返回 ApiResponse(code=40001, message="字段校验失败: ...")
  │
  ▼
Application 层
  │
  │ 资源不存在
  │ → 抛出 BusinessException(40401, "会计科目不存在")
  │
  ▼
Domain 层 / DomainService
  │
  │ 业务规则违反
  │ → 抛出 BusinessException(409xx, "具体业务错误信息")
  │
  ▼
Infrastructure 层
  │
  │ 数据库异常（唯一键冲突等）
  │ → GlobalExceptionHandler 捕获 DuplicateKeyException
  │ → 返回 ApiResponse(code=50001, "系统错误")
```

---

## 数据库映射

### 建表 DDL

```sql
CREATE TABLE `account_subject` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `code`         VARCHAR(50)   NOT NULL COMMENT '科目编码',
  `name`         VARCHAR(100)  NOT NULL COMMENT '科目名称',
  `parent_id`    BIGINT        DEFAULT NULL COMMENT '父科目ID',
  `level`        INT           NOT NULL DEFAULT 1 COMMENT '科目层级',
  `type`         VARCHAR(20)   NOT NULL COMMENT '科目类型: asset/liability/equity/cost/profit_loss',
  `debit_credit` VARCHAR(20)   NOT NULL COMMENT '借贷方向: debit/credit',
  `enabled`      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '启用状态: 1=启用, 0=停用',
  `sort`         INT           NOT NULL DEFAULT 0 COMMENT '排序',
  `remark`       VARCHAR(200)  DEFAULT NULL COMMENT '备注',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会计科目';
```

### 字典初始数据

```sql
-- 科目类型
INSERT INTO `sys_dict` (`dict_code`, `dict_name`) VALUES ('account_subject_type', '会计科目类型');
INSERT INTO `sys_dict_item` (`dict_code`, `item_value`, `item_text`, `sort`) VALUES
  ('account_subject_type', 'asset',       '资产类', 1),
  ('account_subject_type', 'liability',   '负债类', 2),
  ('account_subject_type', 'equity',      '权益类', 3),
  ('account_subject_type', 'cost',        '成本类', 4),
  ('account_subject_type', 'profit_loss', '损益类', 5);

-- 借贷方向
INSERT INTO `sys_dict` (`dict_code`, `dict_name`) VALUES ('debit_credit', '借贷方向');
INSERT INTO `sys_dict_item` (`dict_code`, `item_value`, `item_text`, `sort`) VALUES
  ('debit_credit', 'debit',  '借', 1),
  ('debit_credit', 'credit', '贷', 2);
```

### 种子数据（一级科目示例）

```sql
INSERT INTO `account_subject` (`code`, `name`, `parent_id`, `level`, `type`, `debit_credit`, `enabled`, `sort`) VALUES
  ('1001', '库存现金',     NULL, 1, 'asset',       'debit',  1, 1),
  ('1002', '银行存款',     NULL, 1, 'asset',       'debit',  1, 2),
  ('1122', '应收账款',     NULL, 1, 'asset',       'debit',  1, 3),
  ('2202', '应付账款',     NULL, 1, 'liability',   'credit', 1, 4),
  ('4001', '主营业务收入', NULL, 1, 'profit_loss', 'credit', 1, 5),
  ('5001', '主营业务成本', NULL, 1, 'cost',        'debit',  1, 6),
  ('5401', '管理费用',     NULL, 1, 'profit_loss', 'debit',  1, 7),
  ('5402', '销售费用',     NULL, 1, 'profit_loss', 'debit',  1, 8);
```

---

## 编码顺序

| 步骤 | 产出 | 涉及文件 |
|------|------|---------|
| 1 | 建表脚本 + 字典 + 种子数据 | `database/V001__account_subject.sql` |
| 2 | Domain 实体 + 值对象 + Repository 接口 | `domain/accountsubject/*.java` |
| 3 | Infrastructure Mapper + Entity + Repository 实现 | `infrastructure/accountsubject/*.java` |
| 4 | DomainService（校验规则） | `domain/accountsubject/AccountSubjectDomainService.java` |
| 5 | Application Service（用例编排） | `application/AccountSubjectAppService.java` |
| 6 | DTO + VO | `dto/AccountSubjectDTO.java`, `vo/AccountSubjectVO.java` |
| 7 | Controller | `controller/AccountSubjectController.java` |
| 8 | 前端 API + 页面 | `frontend/src/api/accountSubject.ts`, `frontend/src/views/accountSubject/` |
| 9 | 测试 | `test/.../AccountSubjectAppServiceTest.java` |
