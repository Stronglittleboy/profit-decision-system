# Todo Writer

> 这是当前开发推进的活跃待办板。旧的 `jeecg` 看板仅作历史参考，不再作为执行依据。

## Current Baseline

### 已落地能力

| 能力 | 文件 | 状态 |
|------|------|------|
| Spring Boot 工程启动 | `ProfitDecisionSystemApplication.java` | ✅ 可用 |
| 登录/Token/会话 | `auth/Auth*.java` | ✅ 可用 |
| 健康检查 | `controller/HealthController.java` | ✅ 可用 |
| 仪表盘摘要 | `dashboard/Dashboard*.java` | ✅ 可用 |
| 统一返回值 | `common/api/ApiResponse.java` | ✅ 可用 |
| 统一异常处理 | `common/exception/GlobalExceptionHandler.java` | ✅ 可用 |
| 业务异常 | `common/exception/BusinessException.java` | ✅ 可用 |
| Web 配置 | `config/WebConfig.java` | ✅ 可用 |
| Flyway 数据库迁移 | `db/migration/V*__.sql` | ✅ 可用 |
| Vue 3 前端骨架 | `App.vue` + `MainLayout.vue` | ✅ 可用 |
| 登录页 | `views/LoginView.vue` | ✅ 可用 |
| 首页 | `views/HomeView.vue` | ✅ 可用 |
| 会计科目后端 | `domain/accountsubject/` + `infrastructure/accountsubject/` + `application/` + `controller/` | ✅ 可用 |
| 会计科目前端 | `views/AccountSubjectView.vue` + `api/accountSubject.ts` | ✅ 可用 |
| Docker MySQL + Redis | `docker-compose.yml` | ✅ 可用 |
| 往来方后端 | `domain/counterparty/` + `infrastructure/counterparty/` + `application/` + `controller/` | ✅ 可用 |
| 往来方前端 | `views/CounterpartyView.vue` + `api/counterparty.ts` | ✅ 可用 |
| 收支事实后端 | `domain/factevent/` + `infrastructure/factevent/` + `application/` + `controller/` | ✅ 可用 |
| 收支事实前端 | `views/FactEventView.vue` + `api/factEvent.ts` | ✅ 可用 |
| 项目管理后端 | `domain/project/` + `infrastructure/project/` + `application/` + `controller/` | ✅ 可用 |
| 项目管理前端 | `views/ProjectView.vue` + `api/project.ts` | ✅ 可用 |
| 合同管理后端 | `domain/contract/` + `infrastructure/contract/` + `application/` + `controller/` | ✅ 可用 |
| 合同管理前端 | `views/ContractView.vue` + `api/contract.ts` | ✅ 可用 |
| 应收账款后端 | `domain/receivable/` + `infrastructure/receivable/` + `application/` + `controller/` | ✅ 可用 |
| 应收账款前端 | `views/ReceivableView.vue` + `api/receivable.ts` | ✅ 可用 |
| 应付账款后端 | `domain/payable/` + `infrastructure/payable/` + `application/` + `controller/` | ✅ 可用 |
| 应付账款前端 | `views/PayableView.vue` + `api/payable.ts` | ✅ 可用 |

### 已完成设计文档

| 文档 | 状态 |
|------|------|
| 需求文档 (`requirements.md`) | ✅ |
| 产品设计 (`product-design.md`) | ✅ |
| DDD 领域模型总览 (`domain-model.md`) | ✅ |
| 数据库设计 (`database-design.md`) | ✅ 需按新主线更新 |
| 会计科目领域设计 (`account-subject-domain-design.md`) | ✅ 含 UML |
| 会计科目页面 DSL (`account-subject-page-dsl.md`) | ✅ 含细粒度状态机 |
| 往来方领域设计 (`counterparty-domain-design.md`) | ✅ 含 UML |
| 往来方页面 DSL (`counterparty-page-dsl.md`) | ✅ |
| 收支事实领域设计 (`fact-event-domain-design.md`) | ✅ 含 UML |
| 收支事实页面 DSL (`fact-event-page-dsl.md`) | ✅ |
| 项目管理领域设计 (`project-domain-design.md`) | ✅ 含状态机 |
| 项目管理页面 DSL (`project-page-dsl.md`) | ✅ |
| 合同管理领域设计 (`contract-domain-design.md`) | ✅ 含状态机 |
| 合同管理页面 DSL (`contract-page-dsl.md`) | ✅ |
| 应收/应付领域设计 (`receivable-payable-domain-design.md`) | ✅ 含对称模型 |
| 应收/应付页面 DSL (`receivable-payable-page-dsl.md`) | ✅ |
| 当前技术方案 (`current-tech-plan.md`) | ✅ 含分层架构 |
| 开发规范 (`development-standards.md`) | ✅ |

## Execution Rules

- 永远优先处理"最高优先级、且当前不被阻塞"的任务
- 每完成一个开发批次，立刻更新本文件状态
- 任务描述细化到可直接编码、可直接验证
- 如果实现与看板偏离，先修正看板，再继续推进
- 编码前必须确认领域设计文档和页面 DSL 已完成

---

## Active Backlog

### P0 - account_subject 全链路闭环

> 目标：从建表到前端页面完整跑通第一个业务模块

#### 后端

- [x] **P0-B1** Flyway 迁移脚本 `db/migration/V1__create_account_subject.sql` + `V2__seed_account_subject.sql`
  - account_subject 表 DDL
  - 种子数据（8 条一级科目）
  - 使用 Flyway 自动迁移，启动即执行

- [x] **P0-B2** Domain 层实体
  - `domain/accountsubject/AccountSubject.java`（聚合根）
  - `domain/accountsubject/AccountSubjectType.java`（值对象枚举）
  - `domain/accountsubject/DebitCredit.java`（值对象枚举）

- [x] **P0-B3** Domain 层仓储接口与领域服务
  - `domain/accountsubject/AccountSubjectRepository.java`（仓储接口）
  - `domain/accountsubject/AccountSubjectDomainService.java`（校验规则 + 树形构建 + 搜索祖先链）
  - `domain/accountsubject/AccountSubjectTreeNode.java`（树节点）

- [x] **P0-B4** Infrastructure 层
  - `infrastructure/accountsubject/AccountSubjectEntity.java`（持久化实体）
  - `infrastructure/accountsubject/AccountSubjectMapper.java`（MyBatis-Plus Mapper）
  - `infrastructure/accountsubject/AccountSubjectConverter.java`（Entity ↔ Domain 转换）
  - `infrastructure/accountsubject/AccountSubjectRepositoryImpl.java`（仓储实现）

- [x] **P0-B5** Application 层
  - `application/AccountSubjectAppService.java`（用例编排 + @Transactional）
  - `dto/AccountSubjectDTO.java`（入参，含 @Valid 注解）
  - `dto/AccountSubjectStatusDTO.java`（启停入参）
  - `vo/AccountSubjectVO.java`（出参，含字典翻译字段）

- [x] **P0-B6** Controller 层
  - `controller/AccountSubjectController.java`
  - 接口：GET /tree、GET /{id}、POST、PUT /{id}、DELETE /{id}、PATCH /{id}/status

#### 前端

- [x] **P0-F1** API 层
  - `frontend/src/api/accountSubject.ts`
  - 封装 6 个接口调用

- [x] **P0-F2** 页面视图
  - `frontend/src/views/AccountSubjectView.vue`
  - 查询栏 + 树形表格 + 弹窗表单
  - 实现页面级/弹窗级/删除/启停/搜索 5 个状态机

- [x] **P0-F3** 路由注册
  - `frontend/src/router/index.ts` 添加 `/account-subject` 路由
  - `MainLayout.vue` 侧栏添加"会计科目"菜单入口

- [ ] **P0-F4** 联调验证（待启动服务后验证）
  - 前后端联调跑通 6 个接口
  - 验证所有状态机流转正确
  - 验证错误态处理正确

---

### P1 - counterparty 复用

- [x] **P1-1** 输出 counterparty 领域设计文档（`counterparty-domain-design.md`）
- [x] **P1-2** 输出 counterparty 页面 DSL（`counterparty-page-dsl.md`）
- [x] **P1-3** Flyway 迁移脚本 `db/migration/V3__create_counterparty.sql` + `V4__seed_counterparty.sql`
- [x] **P1-4** 后端全链路：Domain → Infrastructure → Application → Controller
- [x] **P1-5** 前端全链路：API → View → 路由 + 侧栏菜单
- [x] **P1-6** 单元测试 12 个测试通过
- [ ] **P1-7** 联调验证（待启动服务后验证）

---

### P2 - 开发体验与质量

- [x] **P2-1** 统一校验已落地（AccountSubjectDTO / CounterpartyDTO 均使用 @Valid）
- [x] **P2-2** 统一异常处理已落地（BusinessException + GlobalExceptionHandler 全链路覆盖）
- [x] **P2-3** 核心 Service 层单元测试已完成（36 个测试全绿）
- [x] **P2-4** 接口集成测试脚本 `backend/src/test/scripts/api-test.sh`（覆盖登录 + 健康检查 + 科目 CRUD + 往来方 CRUD + 参数校验）
- [x] **P2-5** 前端表单校验规则已实现（两个页面均按 DSL 定义的 rules）
- [x] **P2-6** 前端加载态、空状态、错误提示已完善（loading / el-empty / ElMessage.error）
- [x] **P2-7** 登录态 + 健康检查已纳入 api-test.sh 冒烟测试

---

### P4 - fact_event 收支事实

> 目标：落地系统核心——收支事实录入，打通"事实→归因→指标"链条的起点

- [x] **P4-1** 输出 fact_event 领域设计文档（`fact-event-domain-design.md`）
- [x] **P4-2** 输出 fact_event 页面 DSL（`fact-event-page-dsl.md`）
- [x] **P4-3** Flyway 迁移脚本 `db/migration/V5__create_fact_event.sql`
- [x] **P4-4** 后端全链路：Domain（FactEvent + FactType + CostCategory + FactStatus）→ Infrastructure → Application → Controller
- [x] **P4-5** 前端全链路：API → View（汇总卡片 + 列表 + 新增弹窗 + 冲正） → 路由 + 侧栏菜单
- [x] **P4-6** 单元测试 11 个测试通过（FactEventTests 5 + FactEventDomainServiceTests 6）
- [x] **P4-7** 后端编译 + 前端 typecheck 双绿
- [ ] **P4-8** 联调验证（待启动服务后验证）

---

### P5 - project 项目管理

> 目标：落地组织域核心——项目管理，支持项目全生命周期和项目级盈亏分析基础

- [x] **P5-1** 输出 project 领域设计文档（`project-domain-design.md`）含状态机
- [x] **P5-2** 输出 project 页面 DSL（`project-page-dsl.md`）
- [x] **P5-3** Flyway 迁移脚本 `V6__create_project.sql` + `V7__seed_project.sql`
- [x] **P5-4** 后端全链路：Domain（Project + ProjectStatus + 状态机流转）→ Infrastructure → Application → Controller
- [x] **P5-5** 前端全链路：API → View（统计卡片 + 列表 + CRUD弹窗 + 状态流转 + 启停开关）→ 路由 + 侧栏菜单
- [x] **P5-6** 单元测试 20 个测试通过（ProjectTests 12 + ProjectDomainServiceTests 8）
- [x] **P5-7** 后端编译 + 前端 typecheck 双绿
- [ ] **P5-8** 联调验证（待启动服务后验证）

---

### P6 - contract 合同管理

> 目标：落地合同主数据，为后续应收/应付提供业务基础

- [x] **P6-1** 输出 contract 领域设计文档（`contract-domain-design.md`）含状态机
- [x] **P6-2** 输出 contract 页面 DSL（`contract-page-dsl.md`）
- [x] **P6-3** Flyway 迁移脚本 `V8__create_contract.sql` + `V9__seed_contract.sql`
- [x] **P6-4** 后端全链路：Domain（Contract + ContractType + ContractStatus + 状态机）→ Infrastructure → Application → Controller
- [x] **P6-5** 前端全链路：API → View（列表 + CRUD弹窗 + 状态流转 + 关联往来方/项目）→ 路由 + 侧栏菜单
- [x] **P6-6** 单元测试 20 个测试通过（ContractTests 11 + ContractDomainServiceTests 9）
- [x] **P6-7** 后端编译 + 前端 typecheck 双绿
- [ ] **P6-8** 联调验证（待启动服务后验证）

---

### P3 - 文档同步

- [x] **P3-1** database-design.md 已更新为当前主线（Flyway + 2 张已落地表 + 规划表清单）
- [x] **P3-2** docs/README.md 已添加往来方设计文档索引
- [x] **P3-3** 历史文档已标记为归档（README.md 中以 📦 标识）
- [x] **P3-4** 看板与代码状态已同步

---

## Status Notes

| 里程碑 | 状态 | 完成时间 |
|--------|------|---------|
| 后端基础骨架（auth + health + dashboard） | ✅ Done | - |
| 前端基础骨架（login + home + layout） | ✅ Done | - |
| Docker 环境可用 | ✅ Done | - |
| 设计文档体系重建 | ✅ Done | - |
| 会计科目领域设计 + 页面 DSL | ✅ Done | - |
| 会计科目后端实现 | ✅ Done | - |
| 会计科目前端实现 | ✅ Done | - |
| Flyway 数据库迁移集成 | ✅ Done | - |
| 往来方后端实现 | ✅ Done | - |
| 往来方前端实现 | ✅ Done | - |
| 收支事实后端实现 | ✅ Done | - |
| 收支事实前端实现 | ✅ Done | - |
| 项目管理后端实现 | ✅ Done | - |
| 项目管理前端实现 | ✅ Done | - |
| 合同管理后端实现 | ✅ Done | - |
| 合同管理前端实现 | ✅ Done | - |
