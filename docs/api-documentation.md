# 飞牛经营系统 - API 接口文档

## 📊 接口概览

**Base URL:** `http://localhost:8080/jeecg-boot`  
**认证方式:** JWT Token  
**数据格式:** JSON  

**接口总数:** 约 50+ 个

---

## 🔐 认证接口

### 1.1 用户登录
```
POST /sys/login

Request:
{
  "username": "admin",
  "password": "123456"
}

Response:
{
  "success": true,
  "code": 200,
  "message": "登录成功",
  "result": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": "1",
      "username": "admin",
      "realname": "管理员"
    }
  }
}
```

---

## 💰 收支管理接口

### 2.1 查询收支列表
```
GET /profit/fact/list

Query Parameters:
- pageNo: 1 (页码)
- pageSize: 10 (每页条数)
- type: income/cost (类型，可选)
- startDate: 2026-05-01 (开始日期，可选)
- endDate: 2026-05-31 (结束日期，可选)

Response:
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "1",
        "businessDate": "2026-05-10",
        "type": "income",
        "amount": 50000.00,
        "counterpartyName": "客户A",
        "projectName": "项目1"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 2.2 新增收支记录
```
POST /profit/fact/add

Request:
{
  "businessDate": "2026-05-10",
  "accountingDate": "2026-05-15",
  "type": "income",
  "amount": 50000.00,
  "counterpartyId": "1",
  "projectId": "1",
  "invoiceNo": "INV-2026-001",
  "remark": "备注"
}

Response:
{
  "success": true,
  "message": "添加成功",
  "result": {
    "id": "123"
  }
}
```

### 2.3 修改收支记录
```
PUT /profit/fact/edit

Request:
{
  "id": "123",
  "businessDate": "2026-05-10",
  "amount": 55000.00
}

Response:
{
  "success": true,
  "message": "修改成功"
}
```

### 2.4 删除收支记录
```
DELETE /profit/fact/delete?id=123

Response:
{
  "success": true,
  "message": "删除成功"
}
```

### 2.5 导出收支记录
```
GET /profit/fact/export

Query Parameters:
- type: income/cost
- startDate: 2026-05-01
- endDate: 2026-05-31

Response:
Excel 文件下载
```

---

## 📊 应收账款接口

### 3.1 查询应收列表
```
GET /profit/receivable/list

Query Parameters:
- pageNo: 1
- pageSize: 10
- status: outstanding/overdue/settled (可选)
- counterpartyId: 客户ID (可选)

Response:
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "1",
        "counterpartyName": "客户A",
        "totalAmount": 50000.00,
        "receivedAmount": 30000.00,
        "outstandingAmount": 20000.00,
        "dueDate": "2026-05-20",
        "status": "outstanding"
      }
    ],
    "total": 50
  }
}
```

### 3.2 收款登记
```
POST /profit/receivable/payment

Request:
{
  "receivableId": "1",
  "paymentAmount": 10000.00,
  "paymentDate": "2026-05-15",
  "remark": "收款备注"
}

Response:
{
  "success": true,
  "message": "收款登记成功"
}
```

### 3.3 逾期预警列表
```
GET /profit/receivable/overdue

Response:
{
  "success": true,
  "result": [
    {
      "id": "2",
      "counterpartyName": "客户B",
      "outstandingAmount": 30000.00,
      "dueDate": "2026-05-15",
      "overdueDays": 3
    }
  ]
}
```

---

## 📊 应付账款接口

### 4.1 查询应付列表
```
GET /profit/payable/list

Query Parameters:
- pageNo: 1
- pageSize: 10
- status: outstanding/overdue/settled

Response:
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "1",
        "counterpartyName": "供应商A",
        "totalAmount": 30000.00,
        "paidAmount": 10000.00,
        "outstandingAmount": 20000.00,
        "dueDate": "2026-05-25",
        "status": "outstanding"
      }
    ]
  }
}
```

### 4.2 付款登记
```
POST /profit/payable/payment

Request:
{
  "payableId": "1",
  "paymentAmount": 10000.00,
  "paymentDate": "2026-05-15"
}

Response:
{
  "success": true,
  "message": "付款登记成功"
}
```

---

## 🔒 期间结账接口

### 5.1 查询结账列表
```
GET /profit/closing/list

Response:
{
  "success": true,
  "result": [
    {
      "id": "1",
      "period": "2026-05",
      "orgUnitName": "总公司",
      "status": "open",
      "closedAt": null
    }
  ]
}
```

### 5.2 结账前检查
```
GET /profit/closing/check?period=2026-05

Response:
{
  "success": true,
  "result": {
    "canClose": true,
    "checkList": [
      {
        "item": "所有收支记录已审核",
        "passed": true
      },
      {
        "item": "应收应付已核对",
        "passed": true
      }
    ]
  }
}
```

### 5.3 执行结账
```
POST /profit/closing/execute

Request:
{
  "period": "2026-05",
  "orgUnitId": "1"
}

Response:
{
  "success": true,
  "message": "结账成功"
}
```

### 5.4 反结账
```
POST /profit/closing/reverse

Request:
{
  "closingId": "1",
  "reason": "数据错误需要修正"
}

Response:
{
  "success": true,
  "message": "反结账成功"
}
```

---

## 👥 客户管理接口

### 6.1 查询客户列表
```
GET /profit/counterparty/customer/list

Query Parameters:
- pageNo: 1
- pageSize: 10
- name: 客户名称 (模糊查询，可选)

Response:
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "1",
        "name": "客户A",
        "contact": "张三",
        "phone": "138****1234",
        "creditLevel": "A",
        "status": "active"
      }
    ]
  }
}
```

### 6.2 新增客户
```
POST /profit/counterparty/customer/add

Request:
{
  "name": "客户A",
  "contact": "张三",
  "phone": "13812345678",
  "address": "北京市朝阳区",
  "taxNo": "91110000XXXXXXXX",
  "creditLevel": "A"
}

Response:
{
  "success": true,
  "message": "添加成功"
}
```

---

## 🏢 供应商管理接口

### 7.1 查询供应商列表
```
GET /profit/counterparty/supplier/list

Query Parameters:
- pageNo: 1
- pageSize: 10

Response:
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "1",
        "name": "供应商A",
        "contact": "李四",
        "phone": "139****5678"
      }
    ]
  }
}
```

---

## 📄 合同管理接口

### 8.1 查询合同列表
```
GET /profit/contract/list

Response:
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "1",
        "contractNo": "HT-2026-001",
        "counterpartyName": "客户A",
        "totalAmount": 100000.00,
        "startDate": "2026-05-01",
        "endDate": "2026-12-31",
        "status": "executing"
      }
    ]
  }
}
```

---

## 📊 项目管理接口

### 9.1 查询项目列表
```
GET /profit/project/list

Response:
{
  "success": true,
  "result": {
    "records": [
      {
        "id": "1",
        "name": "项目A",
        "code": "PRJ-2026-001",
        "budget": 500000.00,
        "status": "executing"
      }
    ]
  }
}
```

---

## 📈 报表接口

### 10.1 收支明细表
```
GET /profit/report/income

Query Parameters:
- period: 2026-05

Response:
{
  "success": true,
  "result": {
    "summary": {
      "totalIncome": 500000.00,
      "totalCost": 300000.00,
      "profit": 200000.00
    },
    "details": [
      {
        "date": "2026-05-10",
        "type": "income",
        "amount": 50000.00,
        "counterpartyName": "客户A"
      }
    ]
  }
}
```

### 10.2 应收应付表
```
GET /profit/report/receivable

Query Parameters:
- period: 2026-05

Response:
{
  "success": true,
  "result": {
    "receivable": {
      "total": 200000.00,
      "received": 100000.00,
      "outstanding": 100000.00
    },
    "payable": {
      "total": 150000.00,
      "paid": 80000.00,
      "outstanding": 70000.00
    }
  }
}
```

### 10.3 科目余额表
```
GET /profit/report/balance

Query Parameters:
- period: 2026-05

Response:
{
  "success": true,
  "result": [
    {
      "accountCode": "6001",
      "accountName": "主营业务收入",
      "amount": 500000.00
    },
    {
      "accountCode": "6401",
      "accountName": "主营业务成本",
      "amount": 300000.00
    }
  ]
}
```

---

## 🏠 工作台接口

### 11.1 经营概览
```
GET /profit/dashboard/overview

Response:
{
  "success": true,
  "result": {
    "currentMonth": {
      "income": 500000.00,
      "cost": 300000.00,
      "profit": 200000.00
    },
    "trend": [
      {
        "month": "2026-01",
        "profit": 150000.00
      },
      {
        "month": "2026-02",
        "profit": 180000.00
      }
    ],
    "todoList": [
      {
        "type": "closing",
        "content": "5月期间结账",
        "priority": "high"
      }
    ]
  }
}
```

---

## 📊 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 系统错误 |
| 1001 | 收入必须关联客户 |
| 1002 | 成本必须指定类别 |
| 1003 | 已结账期间不可修改 |
| 2001 | 应收账款不存在 |
| 2002 | 应收账款已结清 |
| 3001 | 期间未开放 |
| 3002 | 期间已结账 |

---

## 🔧 通用参数

### 分页参数
```
pageNo: 页码（从1开始）
pageSize: 每页条数（默认10，最大100）
```

### 排序参数
```
column: 排序字段
order: asc/desc
```

### 日期格式
```
日期：YYYY-MM-DD (2026-05-10)
时间：YYYY-MM-DD HH:mm:ss (2026-05-10 14:30:00)
```

---

**文档版本:** v1.0  
**创建时间:** 2026-05-08  
**状态:** ✅ 接口设计完成
