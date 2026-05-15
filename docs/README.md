# 飞牛经营系统 - 文档索引

## 核心文档（当前主线）

| 文档 | 用途 | 状态 |
|------|------|------|
| [项目总览](../README.md) | 业务背景、系统全景、模块说明、快速开始 | ✅ |
| [当前技术方案](./current-tech-plan.md) | 技术选型、分层架构、API 规范、实施路线 | ✅ |
| [活跃待办板](./todo-writer.md) | 当前开发任务、优先级与进度跟踪 | ✅ 持续更新 |
| [后端架构](../backend/ARCHITECTURE.md) | 后端分层结构、依赖与启动方式 | ✅ |
| [开发规范](./development-standards.md) | 编码约定、命名规范、开发流程 | ✅ |
| [ADR：鉴权与会话](./adr/0001-auth-session-and-production-gate.md) | P0-AUTH-1：已采纳路线 A（Redis 会话 + 日志审计） | ✅ |

## 领域设计

| 文档 | 用途 | 状态 |
|------|------|------|
| [DDD 领域模型](./domain-model.md) | 全域领域划分（Fact/Attribution/Metrics/Decision） | ✅ |
| [会计科目领域设计](./account-subject-domain-design.md) | account_subject UML类图/时序图/状态图/领域规则 | ✅ |
| [会计科目页面 DSL](./account-subject-page-dsl.md) | account_subject 页面结构/细粒度状态机/数据流 | ✅ |
| [往来方领域设计](./counterparty-domain-design.md) | counterparty UML类图/领域规则/DDL | ✅ |
| [往来方页面 DSL](./counterparty-page-dsl.md) | counterparty 页面结构/字段/交互规则 | ✅ |
| [收支事实领域设计](./fact-event-domain-design.md) | fact_event UML类图/领域规则/DDL | ✅ |
| [收支事实页面 DSL](./fact-event-page-dsl.md) | fact_event 页面结构/汇总卡片/冲正交互 | ✅ |
| [项目管理领域设计](./project-domain-design.md) | project UML类图/状态机/领域规则/DDL | ✅ |
| [项目管理页面 DSL](./project-page-dsl.md) | project 页面结构/状态流转/交互规则 | ✅ |
| [合同管理领域设计](./contract-domain-design.md) | contract UML类图/状态机/领域规则/DDL | ✅ |
| [合同管理页面 DSL](./contract-page-dsl.md) | contract 页面结构/状态流转/交互规则 | ✅ |
| [应收/应付领域设计](./receivable-payable-domain-design.md) | receivable/payable 对称模型/领域规则/DDL | ✅ |
| [应收/应付页面 DSL](./receivable-payable-page-dsl.md) | receivable/payable 页面结构/回款/逾期交互 | ✅ |

## 需求与产品

| 文档 | 用途 | 状态 |
|------|------|------|
| [需求文档](./requirements.md) | 完整业务需求、场景用例、验收标准；顶部含 **V2 路由与变更摘要** | ✅ |
| [产品设计](./product-design.md) | **V1 基线**：Goal/Status/Action 与 Agent 边界 | ✅ |
| [产品设计 V2](./product-design-v2.md) | **V2 总册 2.0**：目录、一页纸摘要、审查追溯、原则、路线图；附录索引规格各节 | ✅ |
| [产品规格 V2](./product-spec-v2.md) | **可开发 / 可验收 2.0**（第 17 节 5W2H 落地表 → 2.1）：DoD、E0–E7、逐页区块、API、追溯、Gap | ✅ 研发输入 |
| [头脑风暴二次会议纪要](./product-brainstorm-followup.md) | 边界 / 设计纠偏 / 优势叙事；驱动规格第 17 节 → **2.1** | ✅ |
| [加权多视角产品审查](./product-multiview-review.md) | 审查输入、加权结论与 P0/P1 清单 | ✅ |

## 参考资料

| 文档 | 用途 | 状态 |
|------|------|------|
| [数据库设计](./database-design.md) | 业务表结构与关系（7 张已落地表） | ✅ |
| [接口文档模板](./api-documentation.md) | API 编写通用模板 | ✅ |
| [前端页面清单](./frontend-pages-list.md) | **V2**：当前路由对照 + 目标 IA；仍以 `router/index.ts` 为实装依据 | ✅ |
| [项目总结报告](./project-summary-report.md) | 历史记录（jeecg 阶段） | 📦 归档 |
| [工作流程规范](./workflow-standards.md) | 历史记录 | 📦 归档 |
| [后端启动说明](./backend-startup-options.md) | Maven 启动与打包方式 | ✅ |

## 说明

- 技术栈：`Spring Boot 3.x + JDK 21 + Maven + MyBatis-Plus`，前端 `Vue 3 + Vue Router + Element Plus`
- `docs/todo-writer.md` 是当前唯一活跃任务板
- 标记为 📦 归档 的文档仅供历史参考，不作为当前开发依据
