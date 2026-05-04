# API 设计（MVP）

## 基础约定

- **Base URL:** `/api/v1`
- **认证:** Bearer Token
- **响应格式:**
```json
{
  "code": 0,
  "msg": "success",
  "data": {}
}
```

---

## 1. 事实录入 API

### POST /facts
录入事实事件

**Request:**
```json
{
  "event_time": "2026-05-01 10:00:00",
  "type": "income",
  "amount": 5000.00,
  "org_unit_id": 1,
  "reference_id": "ORDER-001",
  "metadata": {
    "customer": "客户A",
    "product": "产品X"
  }
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": 123,
    "status": "valid"
  }
}
```

---

## 2. 指标查询 API

### GET /metrics/{period}
查询周期指标

**Request:**
```
GET /metrics/2026-05?org_unit_id=1
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "period": "2026-05",
    "org_unit_id": 1,
    "revenue": 50000.00,
    "cost": 30000.00,
    "profit": 20000.00,
    "margin": 0.4000,
    "roi": 0.6667
  }
}
```

---

## 3. 决策建议 API

### GET /decisions
获取决策建议

**Request:**
```
GET /decisions?org_unit_id=1&period=2026-05
```

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "problem": "成本超标15%",
      "root_cause": "人力成本占比过高",
      "recommendation": "优化人员配置，考虑外包非核心业务",
      "expected_impact": "预计降低成本3000元/月",
      "confidence": 0.85,
      "priority": "high"
    }
  ]
}
```

---

## 4. 组织管理 API

### GET /org-units
查询组织列表

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "name": "公司总部",
      "type": "company",
      "parent_id": null
    },
    {
      "id": 2,
      "name": "销售部",
      "type": "dept",
      "parent_id": 1
    }
  ]
}
```

---

## 5. 目标管理 API

### POST /goals
设置目标

**Request:**
```json
{
  "org_unit_id": 1,
  "period": "2026-05",
  "target_profit": 25000.00,
  "target_cost": 25000.00,
  "target_roi": 1.0000
}
```

---

## 6. 归因查询 API

### GET /attributions/{fact_id}
查询事实归因详情

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "attributed_to": 2,
      "org_name": "销售部",
      "amount": 5000.00,
      "weight": 1.0000,
      "rule_name": "直接归属"
    }
  ]
}
```
