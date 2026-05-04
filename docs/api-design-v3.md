# API 设计（终审版 v3.0）

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
  "business_date": "2026-05-10",
  "accounting_date": "2026-05-15",
  "type": "income",
  "amount": 50000.00,
  "org_unit_id": 2,
  "counterparty_type": "customer",
  "counterparty_id": 1,
  "project_id": 6,
  "invoice_no": "12345678",
  "tax_rate": 0.13,
  "reference_id": "ORDER-001",
  "metadata": {
    "product": "产品X",
    "quantity": 100
  }
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": 123,
    "status": "valid",
    "approval_status": "approved"
  }
}
```

---

### POST /facts/cost
录入成本（带分摊）

**Request:**
```json
{
  "business_date": "2026-01-01",
  "accounting_date": "2026-01-01",
  "type": "cost",
  "amount": 12000.00,
  "cost_category": "fixed",
  "org_unit_id": 1,
  "counterparty_type": "supplier",
  "counterparty_id": 5,
  "invoice_no": "INV-2026-001",
  "amortization_start": "2026-01-01",
  "amortization_end": "2026-12-31",
  "amortization_method": "linear",
  "reference_id": "INSURANCE-2026"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": 124,
    "status": "valid",
    "amortization_info": {
      "monthly_amount": 1000.00,
      "total_months": 12
    }
  }
}
```

---

### POST /facts/batch
批量导入

**Request:**
```
Content-Type: multipart/form-data
file: facts.csv
```

**CSV 格式:**
```csv
business_date,accounting_date,type,amount,cost_category,org_unit_id,counterparty_id,invoice_no
2026-05-01,2026-05-01,income,10000,,,1,INV001
2026-05-02,2026-05-02,cost,5000,variable,2,3,INV002
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "success_count": 98,
    "failed_count": 2,
    "errors": [
      {
        "row": 5,
        "reason": "缺少必填字段: amount"
      }
    ]
  }
}
```

---

### PUT /facts/{id}/reverse
冲正

**Response:**
```json
{
  "code": 0,
  "data": {
    "original_id": 123,
    "reversed_id": 125,
    "status": "reversed"
  }
}
```

---

### GET /facts
查询事实列表

**Request:**
```
GET /facts?org_unit_id=2&start_date=2026-05-01&end_date=2026-05-31&type=income&page=1&size=20
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "total": 156,
    "items": [
      {
        "id": 123,
        "business_date": "2026-05-10",
        "accounting_date": "2026-05-15",
        "type": "income",
        "amount": 50000.00,
        "counterparty_name": "客户A",
        "project_name": "项目X",
        "invoice_no": "12345678",
        "status": "valid"
      }
    ]
  }
}
```

---

## 2. 预算管理 API ★

### POST /budgets
创建预算

**Request:**
```json
{
  "org_unit_id": 2,
  "period": "2026-05",
  "items": [
    {
      "category": "revenue",
      "budgeted_amount": 100000.00
    },
    {
      "category": "fixed_cost",
      "budgeted_amount": 20000.00
    },
    {
      "category": "variable_cost",
      "budgeted_amount": 30000.00
    }
  ]
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "budget_ids": [1, 2, 3],
    "status": "draft"
  }
}
```

---

### POST /budgets/{id}/submit
提交审批

**Response:**
```json
{
  "code": 0,
  "data": {
    "approval_id": 10,
    "status": "pending",
    "approver": "张三"
  }
}
```

---

### GET /budgets/comparison
预算对比

**Request:**
```
GET /budgets/comparison?org_unit_id=2&period=2026-05
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "period": "2026-05",
    "org_unit_name": "销售部",
    "items": [
      {
        "category": "revenue",
        "budgeted": 100000.00,
        "actual": 80000.00,
        "variance": -20000.00,
        "execution_rate": 0.80,
        "status": "warning"
      },
      {
        "category": "fixed_cost",
        "budgeted": 20000.00,
        "actual": 22000.00,
        "variance": 2000.00,
        "execution_rate": 1.10,
        "status": "alert"
      }
    ],
    "summary": {
      "budgeted_profit": 50000.00,
      "actual_profit": 28000.00,
      "variance": -22000.00
    }
  }
}
```

---

### POST /budgets/{id}/adjust
申请调整

**Request:**
```json
{
  "new_amount": 60000.00,
  "reason": "市场推广费用增加"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "adjustment_id": 5,
    "status": "pending"
  }
}
```

---

## 3. 项目核算 API ★

### POST /projects
创建项目

**Request:**
```json
{
  "name": "项目X",
  "code": "PRJ-2026-001",
  "org_unit_id": 3,
  "manager_id": 10,
  "budget": 200000.00,
  "start_date": "2026-05-01",
  "end_date": "2026-08-31"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": 6,
    "status": "planning"
  }
}
```

---

### GET /projects/{id}/profit
查询项目盈亏

**Response:**
```json
{
  "code": 0,
  "data": {
    "project_id": 6,
    "project_name": "项目X",
    "status": "executing",
    "budget": 200000.00,
    "revenue": 150000.00,
    "cost": 120000.00,
    "profit": 30000.00,
    "margin": 0.20,
    "budget_execution_rate": 0.60,
    "cost_structure": {
      "fixed_cost": 40000.00,
      "variable_cost": 50000.00,
      "direct_cost": 80000.00,
      "indirect_cost": 40000.00
    },
    "cost_details": [
      {
        "category": "人力成本",
        "amount": 80000.00,
        "percentage": 0.67
      },
      {
        "category": "原材料",
        "amount": 30000.00,
        "percentage": 0.25
      }
    ]
  }
}
```

---

### GET /projects/ranking
项目排行

**Request:**
```
GET /projects/ranking?period=2026-05&sort_by=profit&order=desc&limit=10
```

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "project_id": 6,
      "project_name": "项目X",
      "profit": 30000.00,
      "margin": 0.20,
      "rank": 1
    },
    {
      "project_id": 7,
      "project_name": "项目Y",
      "profit": 25000.00,
      "margin": 0.18,
      "rank": 2
    }
  ]
}
```

---

## 4. 指标查询 API

### GET /metrics/dashboard
经营看板

**Request:**
```
GET /metrics/dashboard?org_unit_id=1&period=2026-05
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "period": "2026-05",
    "org_unit_name": "公司总部",
    "current": {
      "revenue": 500000.00,
      "cost": 300000.00,
      "profit": 200000.00,
      "margin": 0.40
    },
    "budget_comparison": {
      "revenue": {
        "budgeted": 600000.00,
        "actual": 500000.00,
        "execution_rate": 0.83
      },
      "cost": {
        "budgeted": 250000.00,
        "actual": 300000.00,
        "execution_rate": 1.20
      }
    },
    "goal_comparison": {
      "target_profit": 250000.00,
      "actual_profit": 200000.00,
      "achievement_rate": 0.80
    },
    "cost_structure": {
      "fixed_cost": 120000.00,
      "variable_cost": 100000.00,
      "direct_cost": 150000.00,
      "indirect_cost": 150000.00
    },
    "trends": {
      "mom": {
        "revenue_growth": 0.05,
        "cost_growth": 0.15,
        "profit_growth": -0.10
      },
      "yoy": {
        "revenue_growth": 0.20,
        "cost_growth": 0.25,
        "profit_growth": 0.10
      }
    }
  }
}
```

---

### GET /metrics/{period}
查询周期指标

**Request:**
```
GET /metrics/2026-05?org_unit_id=2
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "period": "2026-05",
    "org_unit_id": 2,
    "org_unit_name": "销售部",
    "revenue": 250000.00,
    "cost": 150000.00,
    "profit": 100000.00,
    "margin": 0.40,
    "roi": 0.67,
    "fixed_cost": 60000.00,
    "variable_cost": 50000.00,
    "direct_cost": 80000.00,
    "indirect_cost": 70000.00
  }
}
```

---

### GET /metrics/trends
趋势分析

**Request:**
```
GET /metrics/trends?org_unit_id=1&start_period=2026-01&end_period=2026-05
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "periods": ["2026-01", "2026-02", "2026-03", "2026-04", "2026-05"],
    "revenue": [400000, 420000, 450000, 480000, 500000],
    "cost": [250000, 260000, 270000, 290000, 300000],
    "profit": [150000, 160000, 180000, 190000, 200000],
    "margin": [0.375, 0.381, 0.400, 0.396, 0.400]
  }
}
```

---

### POST /metrics/{period}/recalculate
重算指标

**Response:**
```json
{
  "code": 0,
  "data": {
    "period": "2026-05",
    "old_version": 1,
    "new_version": 2,
    "affected_org_units": 15
  }
}
```

---

## 5. 客户分析 API ★

### GET /counterparties/ranking
客户/供应商排行

**Request:**
```
GET /counterparties/ranking?type=customer&period=2026-05&sort_by=profit&limit=10
```

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "counterparty_id": 1,
      "counterparty_name": "客户A",
      "revenue": 100000.00,
      "cost": 60000.00,
      "profit": 40000.00,
      "margin": 0.40,
      "order_count": 15,
      "rank": 1
    },
    {
      "counterparty_id": 2,
      "counterparty_name": "客户B",
      "revenue": 80000.00,
      "cost": 50000.00,
      "profit": 30000.00,
      "margin": 0.375,
      "order_count": 12,
      "rank": 2
    }
  ]
}
```

---

### GET /counterparties/{id}/analysis
客户详细分析

**Response:**
```json
{
  "code": 0,
  "data": {
    "counterparty_id": 1,
    "counterparty_name": "客户A",
    "type": "customer",
    "credit_level": "A",
    "current_period": {
      "revenue": 100000.00,
      "profit": 40000.00,
      "order_count": 15
    },
    "last_period": {
      "revenue": 120000.00,
      "profit": 48000.00,
      "order_count": 18
    },
    "variance": {
      "revenue_change": -20000.00,
      "revenue_change_rate": -0.17,
      "order_change": -3
    },
    "alerts": [
      {
        "type": "revenue_decline",
        "message": "本月收入下降 17%",
        "severity": "warning"
      }
    ],
    "recent_orders": [
      {
        "fact_id": 123,
        "business_date": "2026-05-10",
        "amount": 50000.00,
        "project_name": "项目X"
      }
    ]
  }
}
```

---

## 6. 决策建议 API

### GET /decisions
获取决策建议

**Request:**
```
GET /decisions?org_unit_id=2&period=2026-05
```

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "decision_id": "dec_20260501_001",
      "problem": "成本超标 15%",
      "root_cause": "人力成本占比过高（67%）",
      "recommendation": "优化人员配置，考虑外包非核心业务",
      "expected_impact": "预计降低成本 5,000 元/月",
      "confidence": 0.85,
      "priority": "high",
      "related_facts_count": 8
    },
    {
      "decision_id": "dec_20260501_002",
      "problem": "客户A贡献下降 17%",
      "root_cause": "订单量减少 3 单",
      "recommendation": "主动联系客户，了解需求变化",
      "expected_impact": "挽回收入 20,000 元",
      "confidence": 0.70,
      "priority": "medium",
      "related_facts_count": 5
    }
  ]
}
```

---

### GET /decisions/{decision_id}/details
决策详情（含相关事实）

**Response:**
```json
{
  "code": 0,
  "data": {
    "decision_id": "dec_20260501_001",
    "problem": "成本超标 15%",
    "root_cause": "人力成本占比过高（67%）",
    "recommendation": "优化人员配置，考虑外包非核心业务",
    "expected_impact": "预计降低成本 5,000 元/月",
    "confidence": 0.85,
    "priority": "high",
    "related_facts": [
      {
        "fact_id": 201,
        "business_date": "2026-05-05",
        "amount": 15000.00,
        "description": "员工工资",
        "impact_weight": 0.30
      },
      {
        "fact_id": 202,
        "business_date": "2026-05-10",
        "amount": 12000.00,
        "description": "员工工资",
        "impact_weight": 0.24
      }
    ]
  }
}
```

---

### POST /decisions/{decision_id}/execute
执行决策并记录

**Request:**
```json
{
  "executed_by": 10,
  "result": "已将2名员工调整到其他部门"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "action_id": 15,
    "executed_at": "2026-05-20T10:30:00"
  }
}
```

---

### GET /actions/history
执行历史

**Request:**
```
GET /actions/history?org_unit_id=2&start_date=2026-04-01&end_date=2026-05-31
```

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "action_id": 15,
      "decision_id": "dec_20260501_001",
      "problem": "成本超标 15%",
      "recommendation": "优化人员配置",
      "executed_by": "张三",
      "executed_at": "2026-05-20T10:30:00",
      "result": "已将2名员工调整到其他部门",
      "effectiveness": "effective",
      "actual_impact": "成本降低 6,000 元/月"
    }
  ]
}
```

---

## 7. 归因查询 API

### GET /attributions/{fact_id}
查询事实归因详情

**Response:**
```json
{
  "code": 0,
  "data": {
    "fact_id": 123,
    "fact_amount": 50000.00,
    "attributions": [
      {
        "attributed_to": 2,
        "org_name": "销售部",
        "amount": 30000.00,
        "weight": 0.60,
        "rule_name": "直接归属"
      },
      {
        "attributed_to": 6,
        "project_name": "项目X",
        "amount": 20000.00,
        "weight": 0.40,
        "rule_name": "项目归属"
      }
    ]
  }
}
```

---

## 8. 审批流程 API ★

### GET /approvals/pending
待审批列表

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "approval_id": 10,
      "entity_type": "budget",
      "entity_id": 5,
      "description": "销售部 2026-05 预算",
      "requested_by": "李四",
      "requested_at": "2026-04-25T09:00:00",
      "status": "pending"
    },
    {
      "approval_id": 11,
      "entity_type": "fact",
      "entity_id": 150,
      "description": "大额支出 50,000 元",
      "requested_by": "王五",
      "requested_at": "2026-05-01T14:30:00",
      "status": "pending"
    }
  ]
}
```

---

### POST /approvals/{id}/approve
审批通过

**Request:**
```json
{
  "comment": "同意"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "approval_id": 10,
    "status": "approved",
    "approved_at": "2026-05-02T10:00:00"
  }
}
```

---

### POST /approvals/{id}/reject
审批驳回

**Request:**
```json
{
  "comment": "预算不合理，请重新编制"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "approval_id": 10,
    "status": "rejected"
  }
}
```

---

## 9. 组织管理 API

### GET /org-units
查询组织列表（树形）

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "name": "公司总部",
      "type": "company",
      "parent_id": null,
      "children": [
        {
          "id": 2,
          "name": "销售部",
          "type": "dept",
          "parent_id": 1,
          "children": [
            {
              "id": 4,
              "name": "阿米巴A组",
              "type": "amb",
              "parent_id": 2
            }
          ]
        }
      ]
    }
  ]
}
```

---

## 10. 目标管理 API

### POST /goals
设置目标

**Request:**
```json
{
  "org_unit_id": 2,
  "period": "2026-05",
  "target_profit": 100000.00,
  "target_cost": 150000.00,
  "target_roi": 0.67
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": 5
  }
}
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 1001 | 参数错误 |
| 1002 | 数据不存在 |
| 1003 | 权限不足 |
| 2001 | 归因失败 |
| 2002 | 指标计算失败 |
| 3001 | 预算已存在 |
| 3002 | 预算未审批 |
| 4001 | 审批流程错误 |
| 5000 | 系统错误 |
