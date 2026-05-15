# V2 API 变更规格

> **基于**：[产品规格 V2](./product-spec-v2.md) §12、[升级清单](./v2-upgrade-checklist.md)  
> **目标**：为后端开发提供可执行的 API 变更规格

---

## 一、安全修复（P0）

### 1.1 客户排名接口安全加固

**接口**：`GET /api/analysis/customer-rank`

#### 当前实现问题

```java
// AnalysisController.java:29-30
String dateCond = "";
if (startDate != null && !startDate.isBlank()) 
    dateCond += " AND fe.business_date >= '" + startDate + "'";
if (endDate != null && !endDate.isBlank()) 
    dateCond += " AND fe.business_date <= '" + endDate + "'";
```

**风险**：
- SQL 注入：`startDate=2026-01-01' OR '1'='1` 可绕过条件
- 数据篡改：恶意日期可能导致全表扫描或错误聚合
- 无格式校验：非法日期静默执行

#### V2 实现要求

**1. 日期格式校验**

```java
private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

private void validateDate(String date, String paramName) {
    if (date == null || date.isBlank()) return;
    
    if (!DATE_PATTERN.matcher(date).matches()) {
        throw new BusinessException(400, 
            String.format("%s 格式须为 yyyy-MM-dd", paramName));
    }
    
    // 可选：进一步校验日期合法性
    try {
        LocalDate.parse(date);
    } catch (DateTimeParseException e) {
        throw new BusinessException(400, 
            String.format("%s 不是有效日期", paramName));
    }
}
```

**2. 参数化查询**

```java
@GetMapping("/customer-rank")
public ApiResponse<List<CustomerRankVO>> customerRank(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate) {
    
    // 校验
    validateDate(startDate, "startDate");
    validateDate(endDate, "endDate");
    
    // 构建参数化 SQL
    List<Object> params = new ArrayList<>();
    StringBuilder dateCond = new StringBuilder();
    
    if (startDate != null && !startDate.isBlank()) {
        dateCond.append(" AND fe.business_date >= ?");
        params.add(startDate);
    }
    if (endDate != null && !endDate.isBlank()) {
        dateCond.append(" AND fe.business_date <= ?");
        params.add(endDate);
    }
    
    String sql = """
        SELECT fe.counterparty_id, cp.name AS counterparty_name,
               COALESCE(SUM(CASE WHEN fe.type='income' THEN fe.amount ELSE 0 END),0) AS income,
               COALESCE(SUM(CASE WHEN fe.type='cost'   THEN fe.amount ELSE 0 END),0) AS cost
        FROM fact_event fe
        LEFT JOIN counterparty cp ON cp.id = fe.counterparty_id
        WHERE fe.status = 'valid' %s
        GROUP BY fe.counterparty_id, cp.name
        ORDER BY income DESC
        """.formatted(dateCond.toString());
    
    List<CustomerRankVO> rows = jdbc.query(sql, params.toArray(), (rs, i) -> {
        // ... 原有映射逻辑不变
    });
    
    return ApiResponse.ok(rows);
}
```

#### 错误响应规范

**非法日期示例**：

```bash
# 请求
GET /api/analysis/customer-rank?startDate=2026-13-99

# 响应（HTTP 200，body.code=400）
{
  "code": 400,
  "message": "startDate 格式须为 yyyy-MM-dd",
  "data": null
}
```

**注**：当前 `GlobalExceptionHandler` 对 `BusinessException` 非 401 返回 HTTP 200 + body.code。V2.2 计划统一改为 HTTP 4xx。

#### 测试用例

```bash
# backend/src/test/scripts/api-test.sh 新增

# 非法日期格式
STATUS=$(curl -s -o /tmp/rank_err.json -w '%{http_code}' \
  -H "$AUTH" "$BASE/api/analysis/customer-rank?startDate=2026-13-99")
assert_status "GET /api/analysis/customer-rank (非法日期→200)" "200" "$STATUS"
CODE=$(jq -r '.code' /tmp/rank_err.json)
if [ "$CODE" = "400" ]; then
  green "非法日期返回 code=400 ✓"
  PASS=$((PASS + 1))
else
  red "非法日期预期 code=400，实际 $CODE"
  FAIL=$((FAIL + 1))
fi

# SQL 注入尝试
STATUS=$(curl -s -o /tmp/rank_inject.json -w '%{http_code}' \
  -H "$AUTH" "$BASE/api/analysis/customer-rank?startDate=2026-01-01'%20OR%20'1'='1")
CODE=$(jq -r '.code' /tmp/rank_inject.json)
if [ "$CODE" = "400" ]; then
  green "SQL 注入尝试被拒绝 ✓"
  PASS=$((PASS + 1))
else
  red "SQL 注入未被拦截"
  FAIL=$((FAIL + 1))
fi

# 正常日期范围
STATUS=$(curl -s -o /tmp/rank_ok.json -w '%{http_code}' \
  -H "$AUTH" "$BASE/api/analysis/customer-rank?startDate=2026-01-01&endDate=2026-12-31")
assert_status "GET /api/analysis/customer-rank (正常日期)" "200" "$STATUS"
CODE=$(jq -r '.code' /tmp/rank_ok.json)
if [ "$CODE" = "0" ]; then
  green "正常日期查询成功 ✓"
  PASS=$((PASS + 1))
else
  red "正常日期查询失败 code=$CODE"
  FAIL=$((FAIL + 1))
fi
```

#### 验收标准

- [ ] `startDate=2026-13-99` 返回 `body.code=400`，message 包含「格式」
- [ ] `startDate=2026-01-01' OR '1'='1` 返回 `body.code=400`
- [ ] `startDate=2026-01-01&endDate=2026-12-31` 返回 `body.code=0`，data 为排名列表
- [ ] SQL 使用 `jdbc.query(sql, params.toArray(), rowMapper)`，无字符串拼接
- [ ] `api-test.sh` 新增 3 个用例全部通过

---

## 二、新增接口（可选）

### 2.1 会议清单接口（E7 可选）

**接口**：`GET /api/meeting/summary`

**说明**：E7 默认形态 B 可直接复用 `GET /api/dashboard/summary` 已有字段，此接口为可选增强。

#### 请求

```
GET /api/meeting/summary
Authorization: Bearer {token}
```

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "overdueReceivableCount": 3,
    "overdueReceivableAmount": 15000.00,
    "receivableRemaining": 85000.00,
    "payableRemaining": 42000.00,
    "pendingDecisions": [
      {
        "id": "placeholder-1",
        "title": "决策建议功能规划中",
        "priority": "info"
      }
    ]
  }
}
```

#### 实现建议

```java
@RestController
@RequestMapping("/api/meeting")
@RequiredArgsConstructor
public class MeetingController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/summary")
    public ApiResponse<MeetingSummaryVO> summary() {
        DashboardSummary dashboard = dashboardService.getSummary();
        
        MeetingSummaryVO vo = new MeetingSummaryVO();
        vo.setOverdueReceivableCount(dashboard.getOverdueReceivableCount());
        vo.setOverdueReceivableAmount(dashboard.getOverdueReceivableAmount());
        vo.setReceivableRemaining(dashboard.getReceivableRemaining());
        vo.setPayableRemaining(dashboard.getPayableRemaining());
        
        // 占位决策
        vo.setPendingDecisions(List.of(
            new DecisionPlaceholder("placeholder-1", "决策建议功能规划中", "info")
        ));
        
        return ApiResponse.ok(vo);
    }
}
```

**注**：若不实现此接口，前端直接调用 `GET /api/dashboard/summary` 并提取相关字段即可。

---

## 三、无变更接口（前端调用方式调整）

### 3.1 收支记录列表

**接口**：`GET /api/fact-event`

**已有 Query 参数**：
- `type`：`income` | `cost`
- `startDate`：`yyyy-MM-dd`
- `endDate`：`yyyy-MM-dd`
- `status`：`valid` | `reversed`

**V2 变更**：无后端变更，前端需在 `onMounted` 时读取 `route.query` 并应用到筛选器。

**示例**：

```typescript
// frontend/src/views/FactEventView.vue
import { useRoute } from 'vue-router'

const route = useRoute()

onMounted(() => {
  // 读取 query 参数
  if (route.query.type) {
    query.type = route.query.type as string
  }
  if (route.query.startDate) {
    dateRange.value[0] = route.query.startDate as string
  }
  if (route.query.endDate) {
    dateRange.value[1] = route.query.endDate as string
  }
  
  // 加载列表
  loadList()
})
```

### 3.2 应收账款列表

**接口**：`GET /api/receivable`

**已有 Query 参数**：
- `status`：`pending` | `partial` | `paid` | `overdue`

**V2 变更**：前端需读取 `route.query.status` 并应用到筛选器（G8）。

**示例**：

```typescript
// frontend/src/views/ReceivableView.vue
onMounted(() => {
  if (route.query.status) {
    statusFilter.value = route.query.status as string
  }
  loadList()
})
```

### 3.3 仪表盘摘要

**接口**：`GET /api/dashboard/summary`

**已有响应字段**（E7 会议清单可复用）：
- `overdueReceivableCount`：逾期应收笔数
- `overdueReceivableAmount`：逾期应收金额
- `receivableRemaining`：应收待收余额
- `payableRemaining`：应付待付余额

**V2 变更**：无后端变更，前端可直接使用。

---

## 四、HTTP 状态码约定（当前 vs 目标）

### 4.1 当前约定（V2.0-V2.1）

`GlobalExceptionHandler` 对 `BusinessException` 的处理：

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    if (e.getCode() == 401) {
        return ResponseEntity.status(401).body(ApiResponse.error(401, e.getMessage()));
    }
    // 其他业务异常返回 HTTP 200，错误码在 body.code
    return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
}
```

**影响**：
- 非法日期返回 HTTP 200 + `body.code=400`
- 前端需判断 `response.data.code` 而非 `response.status`

### 4.2 目标约定（V2.2 技术债）

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    int httpStatus = e.getCode();
    return ResponseEntity.status(httpStatus).body(ApiResponse.error(e.getCode(), e.getMessage()));
}
```

**影响**：
- 非法日期返回 HTTP 400 + `body.code=400`
- 前端可统一用 `axios` 错误拦截器处理

**迁移计划**：V2.2 统一修改，需前端配合调整错误处理逻辑。

---

## 五、API 测试矩阵

| 接口 | 场景 | 请求示例 | 预期 HTTP | 预期 body.code | 预期 message 关键词 |
|------|------|----------|-----------|----------------|---------------------|
| `/api/analysis/customer-rank` | 正常查询 | `?startDate=2026-01-01&endDate=2026-12-31` | 200 | 0 | - |
| 同上 | 无日期 | 无 query | 200 | 0 | - |
| 同上 | 非法格式 | `?startDate=2026-13-99` | 200 | 400 | 格式 |
| 同上 | SQL 注入 | `?startDate=2026-01-01' OR '1'='1` | 200 | 400 | 格式 |
| 同上 | 仅 startDate | `?startDate=2026-01-01` | 200 | 0 | - |
| 同上 | 仅 endDate | `?endDate=2026-12-31` | 200 | 0 | - |
| `/api/fact-event` | 类型筛选 | `?type=income` | 200 | 0 | - |
| 同上 | 日期范围 | `?startDate=2026-01-01&endDate=2026-12-31` | 200 | 0 | - |
| `/api/receivable` | 状态筛选 | `?status=overdue` | 200 | 0 | - |
| `/api/dashboard/summary` | 正常 | 无 query | 200 | 0 | - |

---

## 六、向后兼容性

### 6.1 破坏性变更

| 接口 | 变更 | 影响 | 缓解措施 |
|------|------|------|----------|
| `/api/analysis/customer-rank` | 非法日期从静默执行改为拒绝 | 前端若传非法日期会收到 400 错误 | 前端增加日期格式校验，避免提交非法值 |

### 6.2 非破坏性变更

| 接口 | 变更 | 影响 |
|------|------|------|
| `/api/fact-event` | 无 | 前端调用方式调整，后端无变更 |
| `/api/receivable` | 无 | 前端调用方式调整，后端无变更 |
| `/api/dashboard/summary` | 无 | 前端复用已有字段 |

---

## 七、性能考量

### 7.1 参数化查询性能

**问题**：参数化查询是否影响性能？

**答**：
- 参数化查询使用预编译语句，首次编译后可复用执行计划
- 对于 `customer-rank` 这类聚合查询，瓶颈在全表扫描而非参数绑定
- 建议：在 `fact_event(business_date, status)` 上建复合索引

```sql
CREATE INDEX idx_fact_event_date_status 
ON fact_event(business_date, status);
```

### 7.2 日期校验性能

**问题**：每次请求都校验日期是否影响性能？

**答**：
- 正则匹配 + `LocalDate.parse` 耗时 < 1ms
- 相比 SQL 执行时间（10-100ms）可忽略
- 建议：若 QPS > 1000，可考虑缓存校验结果（过度优化）

---

## 八、安全审计

### 8.1 OWASP Top 10 对照

| 风险 | 当前状态 | V2 状态 | 说明 |
|------|----------|---------|------|
| A03:2021 注入 | ❌ 存在 SQL 注入 | ✅ 已修复 | 参数化查询 + 格式校验 |
| A01:2021 访问控制失效 | ⚠️ 内存 Token | ⚠️ 待路线 | P0-AUTH-1 |
| A02:2021 加密失效 | ⚠️ 无 HTTPS | ⚠️ 部署时配置 | 非本次范围 |
| A05:2021 安全配置错误 | ⚠️ 默认密码 | ⚠️ 文档提示 | 非本次范围 |

### 8.2 代码审查检查点

- [ ] 所有 SQL 使用参数绑定，无字符串拼接用户输入
- [ ] 日期参数使用正则 + `LocalDate.parse` 双重校验
- [ ] 异常信息不泄露敏感信息（如 SQL 语句）
- [ ] 日志记录非法请求（可选，便于审计）

---

## 九、文档更新

### 9.1 API 文档

更新 `docs/api-documentation.md`（若存在）或 Swagger/OpenAPI 规范：

```yaml
/api/analysis/customer-rank:
  get:
    summary: 客户贡献排名
    parameters:
      - name: startDate
        in: query
        schema:
          type: string
          pattern: '^\d{4}-\d{2}-\d{2}$'
        description: 开始日期（yyyy-MM-dd）
      - name: endDate
        in: query
        schema:
          type: string
          pattern: '^\d{4}-\d{2}-\d{2}$'
        description: 结束日期（yyyy-MM-dd）
    responses:
      '200':
        description: 成功（body.code=0）或业务错误（body.code=400）
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ApiResponse'
```

### 9.2 CLAUDE.md

更新 `CLAUDE.md` 安全约定：

```markdown
## Security

- **SQL Injection Prevention**: All SQL queries MUST use parameterized statements. Never concatenate user input into SQL strings.
- **Input Validation**: Date parameters must match `yyyy-MM-dd` format and be valid dates.
- **Error Handling**: Business exceptions return HTTP 200 + body.code (V2.0-V2.1), will migrate to HTTP 4xx in V2.2.
```

---

**文档版本**：1.0  
**维护者**：后端技术负责人  
**更新日期**：2026-05-15
