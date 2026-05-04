# jeecg-boot 代码生成器使用指南

## 一、访问代码生成器

### 1. 启动后端
```bash
cd /vol3/1000/private/workProject/profit-decision-system
docker compose up -d jeecg-boot
```

### 2. 访问地址
```
http://localhost:8080/jeecg-boot/
```

### 3. 登录信息
- 用户名：admin
- 密码：123456

### 4. 进入代码生成器
系统管理 → 开发工具 → 代码生成器

---

## 二、代码生成流程

### 步骤1：选择数据表
1. 点击"同步数据库"按钮
2. 在表列表中找到目标表（如 `profit_counterparty`）
3. 点击"生成代码"按钮

### 步骤2：配置生成参数

#### 基础配置
- **表名**：自动识别（如 profit_counterparty）
- **表描述**：客户/供应商管理
- **类名**：ProfitCounterparty（自动生成）
- **功能名**：客户供应商（用于菜单显示）

#### 生成选项
- ✅ 生成前端代码
- ✅ 生成后端代码
- ✅ 生成菜单
- ✅ 生成按钮权限
- ✅ 生成查询条件
- ✅ 生成表单验证

#### 高级配置
- **主键策略**：UUID
- **分页**：是
- **树形结构**：否
- **导入导出**：是
- **数据权限**：否（后续可开启）

### 步骤3：字段配置

#### 必填字段配置
| 字段名 | 显示名称 | 表单类型 | 查询条件 | 必填 |
|--------|----------|----------|----------|------|
| name | 名称 | 文本框 | 模糊查询 | 是 |
| type | 类型 | 下拉框 | 精确查询 | 是 |
| contact_person | 联系人 | 文本框 | 模糊查询 | 否 |
| contact_phone | 联系电话 | 文本框 | 精确查询 | 否 |
| contact_email | 联系邮箱 | 文本框 | 精确查询 | 否 |
| address | 地址 | 文本域 | - | 否 |
| tax_number | 税号 | 文本框 | 精确查询 | 否 |
| bank_name | 开户行 | 文本框 | - | 否 |
| bank_account | 银行账号 | 文本框 | - | 否 |
| credit_code | 统一社会信用代码 | 文本框 | 精确查询 | 否 |
| status | 状态 | 下拉框 | 精确查询 | 是 |
| remark | 备注 | 文本域 | - | 否 |

#### 字典配置
- **type 字段**：
  - 字典编码：`counterparty_type`
  - 字典项：customer（客户）、supplier（供应商）、both（客户+供应商）

- **status 字段**：
  - 字典编码：`status`
  - 字典项：1（启用）、0（禁用）

### 步骤4：生成代码
1. 点击"生成代码"按钮
2. 下载生成的代码压缩包
3. 解压到项目目录

---

## 三、代码部署

### 后端代码部署

#### 1. 解压后端代码
```bash
# 解压到 profit 模块目录
unzip jeecg-boot-backend.zip -d /vol3/1000/private/workProject/profit-decision-system/backend/jeecg-boot/jeecg-boot/jeecg-boot-module/jeecg-boot-module-profit/
```

#### 2. 目录结构
```
jeecg-boot-module-profit/
├── src/main/java/org/jeecg/modules/profit/
│   ├── controller/
│   │   └── ProfitCounterpartyController.java
│   ├── entity/
│   │   └── ProfitCounterparty.java
│   ├── mapper/
│   │   └── ProfitCounterpartyMapper.java
│   ├── service/
│   │   ├── IProfitCounterpartyService.java
│   │   └── impl/
│   │       └── ProfitCounterpartyServiceImpl.java
│   └── vo/
│       └── ProfitCounterpartyVO.java
└── src/main/resources/mapper/
    └── ProfitCounterpartyMapper.xml
```

#### 3. 重启后端
```bash
docker compose restart jeecg-boot
```

### 前端代码部署

#### 1. 解压前端代码
```bash
# 解压到前端项目目录
unzip jeecg-boot-frontend.zip -d /vol3/1000/private/workProject/profit-decision-system/frontend/src/views/profit/
```

#### 2. 目录结构
```
frontend/src/views/profit/
├── counterparty/
│   ├── ProfitCounterparty.vue          # 列表页面
│   ├── ProfitCounterpartyModal.vue     # 编辑弹窗
│   └── modules/
│       └── ProfitCounterpartyForm.vue  # 表单组件
└── api/
    └── ProfitCounterpartyApi.ts        # API 接口
```

#### 3. 配置路由
编辑 `frontend/src/router/routes/modules/profit.ts`：

```typescript
import type { AppRouteModule } from '/@/router/types';

const profit: AppRouteModule = {
  path: '/profit',
  name: 'Profit',
  component: 'LAYOUT',
  redirect: '/profit/counterparty',
  meta: {
    orderNo: 10,
    icon: 'ant-design:dollar-outlined',
    title: '经营管理',
  },
  children: [
    {
      path: 'counterparty',
      name: 'ProfitCounterparty',
      component: '/profit/counterparty/ProfitCounterparty',
      meta: {
        title: '客户供应商',
        icon: 'ant-design:team-outlined',
      },
    },
  ],
};

export default profit;
```

#### 4. 重启前端
```bash
cd /vol3/1000/private/workProject/profit-decision-system/frontend
pnpm dev
```

---

## 四、需要生成的模块清单

### 第一批（基础数据）
1. ✅ **客户/供应商管理**（profit_counterparty）
2. ✅ **会计科目管理**（profit_account_subject）
3. ✅ **项目管理**（profit_project）
4. ✅ **合同管理**（profit_contract）

### 第二批（核心业务）
5. ⏳ **收支管理**（profit_fact_event）— 需要手写业务逻辑
6. ⏳ **应收管理**（profit_receivable）— 需要手写状态机
7. ⏳ **应付管理**（profit_payable）— 需要手写状态机
8. ⏳ **期间结账**（profit_period_closing）— 需要手写分布式锁

### 第三批（分析报表）
9. ⏳ **预算管理**（profit_budget）
10. ⏳ **预算调整**（profit_budget_adjustment）

---

## 五、代码生成后的优化

### 1. 后端优化

#### Controller 层
```java
// 添加业务校验
@PostMapping("/add")
public Result<?> add(@RequestBody ProfitCounterparty entity) {
    // 校验名称唯一性
    if (service.checkNameExists(entity.getName())) {
        return Result.error("名称已存在");
    }
    service.save(entity);
    return Result.OK("添加成功");
}
```

#### Service 层
```java
// 添加业务方法
public interface IProfitCounterpartyService extends IService<ProfitCounterparty> {
    /**
     * 检查名称是否存在
     */
    boolean checkNameExists(String name);
    
    /**
     * 根据类型查询
     */
    List<ProfitCounterparty> listByType(String type);
}
```

### 2. 前端优化

#### 列表页面
```vue
<template>
  <div>
    <!-- 添加快捷筛选 -->
    <a-tabs v-model:activeKey="activeType" @change="handleTypeChange">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane key="customer" tab="客户" />
      <a-tab-pane key="supplier" tab="供应商" />
    </a-tabs>
    
    <!-- 原有列表 -->
    <BasicTable @register="registerTable">
      <!-- ... -->
    </BasicTable>
  </div>
</template>
```

#### 表单验证
```typescript
// 添加自定义验证规则
const schemas: FormSchema[] = [
  {
    field: 'name',
    label: '名称',
    component: 'Input',
    required: true,
    rules: [
      { required: true, message: '请输入名称' },
      { max: 100, message: '名称不能超过100个字符' },
    ],
  },
  {
    field: 'contact_phone',
    label: '联系电话',
    component: 'Input',
    rules: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
    ],
  },
];
```

---

## 六、常见问题

### Q1: 代码生成后找不到菜单？
**A**: 需要重新登录，或清除浏览器缓存。菜单数据缓存在前端。

### Q2: 后端接口 404？
**A**: 检查：
1. 后端是否重启
2. Controller 的 @RequestMapping 路径是否正确
3. 模块是否正确加载（检查 pom.xml）

### Q3: 前端页面空白？
**A**: 检查：
1. 路由配置是否正确
2. 组件路径是否正确
3. 浏览器控制台是否有报错

### Q4: 字典数据不显示？
**A**: 需要在系统管理 → 字典管理中添加字典数据。

---

## 七、代码生成器最佳实践

### 1. 表设计规范
- 主键统一使用 `id`（varchar(32)）
- 必须有 `create_time`、`update_time`、`create_by`、`update_by`
- 状态字段统一使用 `status`（int）
- 删除标记统一使用 `del_flag`（int）

### 2. 字段命名规范
- 使用下划线命名（snake_case）
- 布尔字段使用 `is_` 前缀
- 金额字段使用 `amount` 后缀
- 时间字段使用 `_time` 后缀

### 3. 生成策略
- 简单 CRUD：100% 自动生成
- 复杂业务：生成基础代码 + 手写业务逻辑
- 报表页面：手写（代码生成器不适合）

### 4. 代码审查
生成代码后必须检查：
- ✅ 字段类型是否正确
- ✅ 必填校验是否完整
- ✅ 查询条件是否合理
- ✅ 按钮权限是否配置
- ✅ 数据权限是否需要

---

## 八、下一步计划

1. ✅ 生成客户/供应商管理
2. ✅ 生成会计科目管理
3. ✅ 生成项目管理
4. ✅ 生成合同管理
5. ⏳ 手写收支管理核心逻辑
6. ⏳ 手写应收应付状态机
7. ⏳ 手写期间结账逻辑
8. ⏳ 开发工作台数据聚合
9. ⏳ 开发报表中心

---

**预计时间：2天完成所有代码生成和核心业务开发**
