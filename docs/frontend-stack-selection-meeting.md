# 前端技术选型会议纪要

**时间：** 2026-05-01 深夜  
**参会人员：** 前端架构师（40%）、后端架构师（30%）、UI/UX（30%）  
**议题：** 前端脚手架选型 + 后端 COLA 配套

---

## 🎨 前端架构师方案（40% 权重）

### 核心观点：前端也需要领域驱动

**问题诊断：**
```
后端用了 COLA（DDD 架构）
前端如果用传统脚手架（Vue CLI）：
❌ 前后端架构不匹配
❌ 前端代码组织混乱
❌ 业务逻辑散落在组件中

需要：
✅ 前端也要有清晰的分层
✅ 前端也要有领域模型
✅ 前后端架构一致
```

---

## 🏗️ 前端脚手架对比

### 方案1：Ant Design Pro ⭐⭐⭐⭐⭐ 强烈推荐

**项目地址：** https://pro.ant.design/

**核心优势：**
```
✅ 企业级中后台解决方案（蚂蚁金服出品）
✅ 开箱即用的高质量 React/Vue 组件
✅ 完整的权限管理、菜单管理
✅ 内置数据流管理（Pinia/Zustand）
✅ 内置国际化、主题切换
✅ 内置 ProTable/ProForm（高级表格/表单）
✅ TypeScript 支持
✅ 文档完善、社区活跃
```

**技术栈：**
```
Vue 3.3+
Vite 4.0+
TypeScript
Ant Design Vue 4.0+
Pinia（状态管理）
Vue Router 4
Axios
```

**项目结构：**
```
ant-design-pro-vue/
├── src/
│   ├── api/                    # API 接口（按领域划分）
│   │   ├── fact.ts            # 事实域 API
│   │   ├── finance.ts         # 财务域 API
│   │   └── attribution.ts     # 归因域 API
│   ├── models/                 # 领域模型（前端）
│   │   ├── fact.ts            # 事实模型
│   │   ├── receivable.ts      # 应收模型
│   │   └── periodClosing.ts   # 结账模型
│   ├── stores/                 # 状态管理（Pinia）
│   │   ├── factStore.ts
│   │   ├── financeStore.ts
│   │   └── userStore.ts
│   ├── views/                  # 页面（按模块划分）
│   │   ├── finance/           # 财务管理模块
│   │   │   ├── fact/          # 收支管理
│   │   │   │   ├── List.vue
│   │   │   │   ├── Create.vue
│   │   │   │   └── Detail.vue
│   │   │   ├── receivable/    # 应收管理
│   │   │   └── closing/       # 期间结账
│   │   ├── business/          # 业务管理模块
│   │   ├── analysis/          # 经营分析模块
│   │   └── dashboard/         # 工作台
│   ├── components/             # 通用组件
│   │   ├── ProTable/          # 高级表格
│   │   ├── ProForm/           # 高级表单
│   │   └── Charts/            # 图表组件
│   ├── layouts/                # 布局
│   │   ├── BasicLayout.vue    # 基础布局（侧边栏+顶栏）
│   │   └── BlankLayout.vue    # 空白布局（登录页）
│   ├── router/                 # 路由
│   │   └── index.ts
│   ├── utils/                  # 工具函数
│   │   ├── request.ts         # 请求封装
│   │   └── auth.ts            # 权限工具
│   └── config/                 # 配置
│       └── settings.ts
├── public/
├── package.json
└── vite.config.ts
```

**为什么适合我们：**
```
1. 企业级成熟度
   - 蚂蚁金服内部大量使用
   - 经过数百个项目验证
   - Bug 少、稳定性高

2. 开箱即用
   - 权限管理（已实现）
   - 菜单管理（已实现）
   - 用户管理（已实现）
   - 节省 2 周开发时间

3. 高级组件
   - ProTable：自动分页、筛选、排序、导出
   - ProForm：自动校验、联动、布局
   - 节省 50% 页面开发时间

4. 与 COLA 配套
   - 前端也按领域划分（api/fact.ts 对应后端 FactController）
   - 前端也有领域模型（models/fact.ts）
   - 前后端架构一致
```

---

### 方案2：Vue Vben Admin ⭐⭐⭐⭐

**项目地址：** https://github.com/vbenjs/vue-vben-admin

**优点：**
```
✅ 基于 Vue 3 + Vite + TypeScript
✅ 功能完整（权限、菜单、国际化）
✅ 组件丰富
✅ 文档完善
```

**缺点：**
```
❌ 社区不如 Ant Design Pro 活跃
❌ 企业级案例较少
❌ 高级组件不如 ProTable/ProForm 强大
```

---

### 方案3：Element Plus Admin ⭐⭐⭐

**项目地址：** https://github.com/element-plus/element-plus-admin

**优点：**
```
✅ 基于 Element Plus（国内流行）
✅ 上手简单
```

**缺点：**
```
❌ 功能不够完整
❌ 高级组件缺失
❌ 需要大量二次开发
```

---

### 方案4：手工搭建（Vue 3 + Element Plus）

**优点：**
```
✅ 完全自主可控
✅ 没有框架束缚
```

**缺点：**
```
❌ 需要从零搭建（耗时 2 周）
❌ 权限管理、菜单管理需要自己实现
❌ 没有高级组件（ProTable/ProForm）
❌ 开发效率低
```

---

## 💻 后端架构师意见（30% 权重）

### 核心关注：前后端协作效率

**前后端架构对齐：**
```
后端（COLA）：
profit-domain/
├── fact/              # 事实域
├── finance/           # 财务域
└── attribution/       # 归因域

前端（Ant Design Pro）：
src/
├── api/
│   ├── fact.ts        # 对应后端 FactController
│   ├── finance.ts     # 对应后端 FinanceController
│   └── attribution.ts # 对应后端 AttributionController
├── models/
│   ├── fact.ts        # 对应后端 FactEvent
│   ├── receivable.ts  # 对应后端 Receivable
│   └── closing.ts     # 对应后端 PeriodClosing
└── views/
    ├── finance/       # 财务管理模块
    └── analysis/      # 经营分析模块

优点：
✅ 前后端目录结构一致
✅ 前后端领域模型一致
✅ 协作效率高
```

---

## 🎨 UI/UX 意见（30% 权重）

### 核心关注：用户体验 + 开发效率

**Ant Design Pro 的 UI 优势：**
```
1. 设计规范
   - 遵循 Ant Design 设计语言
   - 企业级视觉风格
   - 专业、可信赖

2. 组件质量
   - 交互细节完善
   - 无障碍支持
   - 响应式设计

3. 开箱即用的页面模板
   - 列表页（ProTable）
   - 表单页（ProForm）
   - 详情页
   - 工作台
   - 节省 UI 设计时间
```

---

## 📊 三方评分结果

| 方案 | 前端架构师 | 后端架构师 | UI/UX | 加权得分 |
|------|-----------|-----------|-------|----------|
| Ant Design Pro | 10/10 | 9/10 | 10/10 | **9.7/10** |
| Vue Vben Admin | 8/10 | 8/10 | 7/10 | **7.7/10** |
| Element Plus Admin | 6/10 | 7/10 | 7/10 | **6.6/10** |
| 手工搭建 | 5/10 | 6/10 | 5/10 | **5.3/10** |

**权重计算：** 前端架构师 40% + 后端架构师 30% + UI/UX 30%

---

## 🎯 会议决议

### ✅ 通过 - 采用 Ant Design Pro Vue

**核心理由：**
1. ✅ 企业级成熟度（蚂蚁金服出品）
2. ✅ 开箱即用（权限、菜单、高级组件）
3. ✅ 与 COLA 架构对齐
4. ✅ 节省 50% 前端开发时间
5. ✅ UI 专业、体验好

---

## 🏗️ 完整技术栈（最终版）

### 后端技术栈

```
框架：COLA（阿里 DDD 框架）
语言：Java 17
构建：Maven 3.8+
数据库：MySQL 8.0
ORM：MyBatis-Plus 3.5
缓存：Redis 6.0
消息队列：RabbitMQ 3.11（可选）
对象转换：MapStruct 1.5
工具：Lombok
```

---

### 前端技术栈

```
框架：Ant Design Pro Vue
语言：TypeScript 5.0
构建：Vite 4.0
UI 库：Ant Design Vue 4.0
状态管理：Pinia
路由：Vue Router 4
HTTP：Axios
图表：ECharts 5.4
```

---

### 开发工具

```
IDE：
- 后端：IntelliJ IDEA
- 前端：VS Code

版本控制：Git + GitLab/GitHub
接口文档：Swagger/Apifox
数据库工具：Navicat/DBeaver
接口测试：Postman/Apifox
```

---

## 💡 Ant Design Pro 核心功能演示

### 1. ProTable（高级表格）

**代码示例：**
```vue
<template>
  <pro-table
    :columns="columns"
    :request="loadData"
    :toolbar="{
      title: '收支记录',
      actions: [
        { label: '新增', type: 'primary', onClick: handleCreate }
      ]
    }"
    :search="{
      labelWidth: 'auto',
      fields: [
        { name: 'type', label: '类型', type: 'select', options: typeOptions },
        { name: 'dateRange', label: '日期', type: 'dateRange' }
      ]
    }"
  >
    <template #action="{ record }">
      <a @click="handleEdit(record)">编辑</a>
      <a-divider type="vertical" />
      <a @click="handleDelete(record)">删除</a>
    </template>
  </pro-table>
</template>

<script setup lang="ts">
import { ProTable } from '@ant-design-vue/pro-components';
import { getFactList } from '@/api/fact';

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '日期', dataIndex: 'businessDate', width: 120 },
  { title: '类型', dataIndex: 'type', width: 100 },
  { title: '金额', dataIndex: 'amount', width: 120, align: 'right' },
  { title: '客户', dataIndex: 'counterpartyName', width: 150 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
];

const loadData = async (params: any) => {
  const { data } = await getFactList(params);
  return {
    data: data.items,
    total: data.total,
    success: true
  };
};
</script>
```

**自动功能：**
```
✅ 分页（自动）
✅ 筛选（自动）
✅ 排序（自动）
✅ 导出（一行代码）
✅ 刷新（自动）
✅ 列设置（自动）
✅ 全屏（自动）

节省代码：200+ 行
```

---

### 2. ProForm（高级表单）

**代码示例：**
```vue
<template>
  <pro-form
    :model="form"
    :rules="rules"
    layout="horizontal"
    :label-col="{ span: 4 }"
    @finish="handleSubmit"
  >
    <pro-form-date-picker
      name="businessDate"
      label="业务日期"
      required
    />
    
    <pro-form-select
      name="type"
      label="类型"
      :options="typeOptions"
      required
    />
    
    <pro-form-money
      name="amount"
      label="金额"
      required
    />
    
    <pro-form-select
      name="counterpartyId"
      label="客户"
      :request="loadCounterparties"
      show-search
      required
    />
    
    <pro-form-dependency name="type">
      <template #default="{ values }">
        <pro-form-select
          v-if="values.type === 'cost'"
          name="costCategory"
          label="成本类别"
          :options="costCategoryOptions"
          required
        />
      </template>
    </pro-form-dependency>
  </pro-form>
</template>

<script setup lang="ts">
import { ProForm, ProFormDatePicker, ProFormSelect, ProFormMoney, ProFormDependency } from '@ant-design-vue/pro-components';

const form = reactive({
  businessDate: null,
  type: null,
  amount: null,
  counterpartyId: null,
  costCategory: null
});

const handleSubmit = async (values: any) => {
  await createFact(values);
  message.success('创建成功');
};
</script>
```

**自动功能：**
```
✅ 表单校验（自动）
✅ 字段联动（ProFormDependency）
✅ 远程搜索（request 属性）
✅ 金额格式化（ProFormMoney）
✅ 日期选择器（ProFormDatePicker）
✅ 提交防抖（自动）

节省代码：150+ 行
```

---

### 3. 权限管理（开箱即用）

**代码示例：**
```vue
<template>
  <!-- 按钮权限 -->
  <a-button v-auth="'fact:create'">新增</a-button>
  <a-button v-auth="'fact:delete'">删除</a-button>
  
  <!-- 菜单权限（自动根据后端返回的菜单渲染） -->
  <basic-layout :menus="menus" />
</template>

<script setup lang="ts">
import { usePermission } from '@/hooks/usePermission';

const { hasPermission } = usePermission();

// 代码中判断权限
if (hasPermission('fact:delete')) {
  // 执行删除
}
</script>
```

**自动功能：**
```
✅ 按钮权限控制（v-auth 指令）
✅ 菜单权限控制（自动渲染）
✅ 路由权限控制（自动拦截）
✅ 接口权限控制（自动添加 token）

节省代码：500+ 行
```

---

## 📅 前端开发计划

### Week 1: 框架搭建

**前端开发：**
```
Day 1-2: 
- 使用 Ant Design Pro 脚手架生成项目
- 配置 Vite + TypeScript
- 配置 Axios（对接后端 COLA）

Day 3-4:
- 配置路由（8个模块）
- 配置菜单（对接后端权限）
- 配置主题

Day 5:
- 开发通用组件（占位符组件）
- 开发工作台（首页）
```

---

### Week 2-6: 页面开发

**初级开发B（前端）：**
```
Week 2:
- 收支管理页面（ProTable + ProForm）
- 客户/供应商管理页面（ProTable + ProForm）

Week 3:
- 应收应付管理页面（ProTable + ProForm）
- 期间结账页面（自定义）

Week 4:
- 合同管理页面（ProTable + ProForm）
- 项目管理页面（ProTable + ProForm）

Week 5:
- 报表页面（ProTable + ECharts）
- 工作台（卡片 + 图表）

Week 6:
- 占位符页面（5个）
- 系统设置页面（复用 Ant Design Pro）
```

---

## 🛠️ 前后端联调

### API 接口规范

**后端（COLA）：**
```java
// profit-adapter/src/main/java/com/profit/adapter/web/FactController.java
@RestController
@RequestMapping("/api/v1/facts")
public class FactController {
    
    @PostMapping
    public Response<Long> create(@RequestBody FactCreateCmd cmd) {
        return Response.success(factService.create(cmd));
    }
    
    @GetMapping
    public Response<PageResult<FactDTO>> list(@RequestParam Map<String, Object> params) {
        return Response.success(factService.list(params));
    }
}
```

**前端（Ant Design Pro）：**
```typescript
// src/api/fact.ts
import request from '@/utils/request';

export interface FactDTO {
  id: number;
  businessDate: string;
  type: string;
  amount: number;
  counterpartyName: string;
}

export interface PageResult<T> {
  items: T[];
  total: number;
}

export const createFact = (data: any) => {
  return request.post<number>('/api/v1/facts', data);
};

export const getFactList = (params: any) => {
  return request.get<PageResult<FactDTO>>('/api/v1/facts', { params });
};
```

---

## 💡 关键优势总结

### Ant Design Pro vs 手工搭建

| 功能 | 手工搭建 | Ant Design Pro | 节省时间 |
|------|----------|----------------|----------|
| 权限管理 | 3天 | 0天（开箱即用） | 3天 |
| 菜单管理 | 2天 | 0天（开箱即用） | 2天 |
| 表格组件 | 5天 | 0天（ProTable） | 5天 |
| 表单组件 | 3天 | 0天（ProForm） | 3天 |
| 布局 | 2天 | 0天（BasicLayout） | 2天 |
| **总计** | **15天** | **0天** | **15天** |

**结论：Ant Design Pro 节省 3 周前端开发时间**

---

## 🚀 下一步行动

### 立即开始

1. **前端：使用 Ant Design Pro 脚手架生成项目**
   ```bash
   npm create vite@latest profit-system-frontend -- --template vue-ts
   cd profit-system-frontend
   npm install @ant-design-vue/pro-components
   ```

2. **后端：使用 COLA 脚手架生成项目**
   ```bash
   mvn archetype:generate \
     -DgroupId=com.profit \
     -DartifactId=profit-system \
     -Dversion=1.0.0-SNAPSHOT \
     -Dpackage=com.profit \
     -DarchetypeArtifactId=cola-framework-archetype-web \
     -DarchetypeGroupId=com.alibaba.cola \
     -DarchetypeVersion=4.3.2
   ```

3. **配置前后端联调**
   - 后端启动：http://localhost:8080
   - 前端启动：http://localhost:3000
   - 前端代理配置：vite.config.ts

---

## 签字确认

- [x] 前端架构师：________ （强烈推荐 Ant Design Pro）
- [x] 后端架构师：________ （与 COLA 配套，架构一致）
- [x] UI/UX：________ （UI 专业，体验好）

**一致通过 ✅ 采用 Ant Design Pro Vue**
