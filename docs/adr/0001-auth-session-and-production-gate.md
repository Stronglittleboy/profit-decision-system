# ADR 0001：鉴权与会话持久化（P0-AUTH-1）

**状态**：**已采纳 — 路线 A（2026-05-15）**  
**日期**：2026-05-15  
**关联**：[产品规格 V2 §1 DoD 第 2 条](../product-spec-v2.md)（P0-AUTH-1）

## 背景

V2 DoD 要求对「生产就绪」与对外演示的鉴权能力有**书面二选一**：要么走可上线的会话与审计方案，要么明确**仅 Demo/内网**及上线闸门。

## 决议（已采纳）

**路线 A — 上线准备**

| 选项 | 勾选 |
|------|------|
| A 上线准备 | **✓** |
| B Demo/内网闸门 |  |

## 实现摘要（与代码一致）

1. **会话存储**：默认 `app.auth.session-store=redis`。`RedisTokenStore` 使用 `StringRedisTemplate`，键前缀 `profit:auth:session:`，值为会话 JSON，TTL 与 `app.auth.token-ttl-minutes` 一致。
2. **测试 / 无 Redis 环境**：`ProfitDecisionSystemApplicationTests` 使用 `@SpringBootTest(properties = "app.auth.session-store=memory")` 与 `@ActiveProfiles("dev")`；本地开发若未起 Redis，可在 `application-dev.yml` 中临时 `session-store: memory`。
3. **审计（首期）**：`AuthService` 对登录成功、登录失败、登出输出 **结构化日志**（含 `remote` / `user`）；**未**建独立审计表，后续可另立 ADR 落库或对接 SIEM。

## 历史备选（路线 B，未采纳）

- 对外声明仅 Demo/内网、上线前再切 Redis — **本次不采用**。

## 后果与后续

- **运维**：生产需可用 Redis；多实例水平扩展时会话共享依赖 Redis 可用性。
- **后续 ADR 候选**：多账号与权限模型、审计落库、`IllegalArgumentException` 类业务错误统一 HTTP 状态码（见规格 §12 技术债）。
