# account_subject 页面 DSL

## 页面目标

提供会计科目树形管理能力，包括查询、新增、编辑、删除、启停和查看父子关系。本页面是系统主数据管理的第一个模块，后续 `counterparty` 等模块复用相同模式。

## 数据源

| 数据源 | 说明 |
|--------|------|
| 后端接口 | `/api/account-subject` |
| 数据表 | `account_subject` |
| 字典 | `account_subject_type`（科目类型）、`debit_credit`（借贷方向） |

---

## 页面结构

```text
┌──────────────────────────────────────────────────────────────┐
│  MainLayout (侧栏 + 头部)                                    │
├──────────────────────────────────────────────────────────────┤
│  AccountSubjectView                                          │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  区块1：查询栏 (SearchBar)                              │  │
│  │  [搜索框] [查询] [重置] [新增科目]                       │  │
│  ├────────────────────────────────────────────────────────┤  │
│  │  区块2：树形列表 (TreeTable)                             │  │
│  │  ┌──┬──────┬──────┬────┬────┬────┬────┬────┬────────┐ │  │
│  │  │  │ 编码 │ 名称 │类型│方向│层级│状态│排序│  操作   │ │  │
│  │  ├──┼──────┼──────┼────┼────┼────┼────┼────┼────────┤ │  │
│  │  │▶ │ 1001 │库存..│资产│ 借 │ 1  │ ● │ 1  │编辑 删除│ │  │
│  │  │  │      │      │    │    │    │    │    │新增下级 │ │  │
│  │  │▶ │ 1002 │银行..│资产│ 借 │ 1  │ ● │ 2  │编辑 删除│ │  │
│  │  │  │100201│活期..│资产│ 借 │ 2  │ ● │ 1  │编辑 删除│ │  │
│  │  └──┴──────┴──────┴────┴────┴────┴────┴────┴────────┘ │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  区块3：新增/编辑弹窗 (FormDialog)                       │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │  科目编码：[          ]                           │  │  │
│  │  │  科目名称：[          ]                           │  │  │
│  │  │  父级科目：[▼ 树形选择 ]                           │  │  │
│  │  │  科目类型：[▼ 资产/负债/权益/成本/损益]             │  │  │
│  │  │  借贷方向：[▼ 借/贷    ]                           │  │  │
│  │  │  排    序：[    0     ]                           │  │  │
│  │  │  备    注：[          ]                           │  │  │
│  │  │           [取消]  [保存]                           │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

---

## 字段 DSL

| 字段 | 组件类型 | 必填 | 校验规则 | 新增时 | 编辑时 |
|------|---------|------|---------|--------|--------|
| code | el-input | 是 | 1-50 字符，字母数字，后端校验唯一 | 可编辑 | 只读 |
| name | el-input | 是 | 1-100 字符 | 可编辑 | 可编辑 |
| parentId | el-tree-select | 否 | 不能选自己及子孙节点 | 可编辑 | 可编辑 |
| type | el-select | 是 | 枚举值 `asset/liability/equity/cost/profit_loss` | 可编辑 | 可编辑 |
| debitCredit | el-select | 是 | 枚举值 `debit/credit` | 可编辑 | 可编辑 |
| level | — | — | 自动计算，不展示在表单中 | 只在列表展示 | 只在列表展示 |
| enabled | el-switch | 是 | 默认 true | 默认开启 | 可切换 |
| sort | el-input-number | 是 | 1-9999 | 可编辑 | 可编辑 |
| remark | el-input(textarea) | 否 | 0-200 字符 | 可编辑 | 可编辑 |

---

## 页面级状态机

### 全局状态枚举

```text
PageState = INIT | LOADING | READY | ERROR
```

### 状态转换图

```text
         ┌──────────────────────────────────────────────────────────┐
         │                                                          │
         ▼                                                          │
    ┌─────────┐    进入页面 / 触发加载     ┌───────────┐             │
    │  INIT   │ ──────────────────────── > │  LOADING  │             │
    └─────────┘                            └─────┬─────┘             │
                                                 │                   │
                                    ┌────────────┼────────────┐      │
                                    │ 成功       │            │ 失败  │
                                    ▼            │            ▼      │
                              ┌──────────┐       │      ┌─────────┐  │
                              │  READY   │       │      │  ERROR  │  │
                              └────┬─────┘       │      └────┬────┘  │
                                   │             │           │       │
                   ┌───────────────┼─────────┐   │    点击重试│       │
                   │               │         │   │           │       │
            查询/刷新          增删改操作    启停  └───────────┘       │
                   │               │         │                       │
                   └───────────────┼─────────┘                       │
                                   │                                 │
                                   └─────────────────────────────────┘
                                      操作完成后回到 LOADING→READY
```

### 状态转换表

| 当前状态 | 事件 | 目标状态 | 副作用 |
|---------|------|---------|--------|
| INIT | 组件挂载 (onMounted) | LOADING | 调用 `fetchTree()` |
| LOADING | 接口返回成功 | READY | 更新 treeData，展开根节点 |
| LOADING | 接口返回失败 | ERROR | 设置 errorMessage |
| READY | 点击查询/回车搜索 | LOADING | 调用 `fetchTree(keyword)` |
| READY | 点击重置 | LOADING | 清空 keyword，调用 `fetchTree()` |
| READY | 保存成功(新增/编辑) | LOADING | 关闭弹窗，调用 `fetchTree()` |
| READY | 删除成功 | LOADING | 调用 `fetchTree()` |
| READY | 启停成功 | LOADING | 调用 `fetchTree()` |
| ERROR | 点击重试 | LOADING | 调用 `fetchTree()` |

---

## 弹窗级状态机（FormDialog）

### 弹窗状态枚举

```text
DialogState = CLOSED | OPEN_CREATE | OPEN_CREATE_CHILD | OPEN_EDIT | SUBMITTING
```

### 状态转换图

```text
    ┌──────────┐
    │  CLOSED  │ <────────────────────────────────────────────┐
    └────┬─────┘                                              │
         │                                                     │
         │ 点击"新增科目"                                       │
         ├─────────────────────> ┌───────────────────┐         │
         │                      │   OPEN_CREATE      │         │
         │                      │   form = 空表单     │         │
         │                      └───────┬───────────┘         │
         │ 点击"新增下级"                  │                     │
         ├─────────────────────> ┌───────┴───────────┐         │
         │                      │ OPEN_CREATE_CHILD  │         │
         │                      │ form.parentId =    │         │
         │                      │   当前行 id         │         │
         │                      └───────┬───────────┘         │
         │ 点击"编辑"                      │                     │
         ├─────────────────────> ┌───────┴───────────┐         │
         │                      │   OPEN_EDIT        │         │
         │                      │ form = 当前行数据    │         │
         │                      │ code 字段只读       │         │
         │                      └───────┬───────────┘         │
         │                              │                     │
         │                    点击"保存"  │                     │
         │                              ▼                     │
         │                      ┌───────────────────┐         │
         │                      │   SUBMITTING       │         │
         │                      │ 按钮 loading=true  │         │
         │                      │ 表单禁用            │         │
         │                      └───────┬───────────┘         │
         │                              │                     │
         │                    ┌─────────┼─────────┐           │
         │                    │ 成功    │         │ 失败       │
         │                    │         │         │           │
         │                    │         │         ▼           │
         │                    │         │  回到 OPEN_xxx      │
         │                    │         │  展示后端错误信息     │
         │                    ▼         │                     │
         │               CLOSED ────────┘                     │
         │               并触发 READY→LOADING                  │
         │                                                     │
         │ 点击"取消" / 点击遮罩 / 按 Esc                        │
         └─────────────────────────────────────────────────────┘
```

### 弹窗状态转换表

| 当前状态 | 事件 | 目标状态 | 副作用 |
|---------|------|---------|--------|
| CLOSED | 点击"新增科目" | OPEN_CREATE | 重置表单，打开弹窗 |
| CLOSED | 点击行"新增下级" | OPEN_CREATE_CHILD | 重置表单，设置 parentId，打开弹窗 |
| CLOSED | 点击行"编辑" | OPEN_EDIT | 加载行数据填充表单，code 只读 |
| OPEN_* | 点击"保存" | SUBMITTING | 触发表单校验；校验通过→调用 API；校验失败→停留 |
| SUBMITTING | API 成功 | CLOSED | 提示"保存成功"，触发列表刷新 |
| SUBMITTING | API 失败 | 恢复到 OPEN_xxx | 展示后端返回的 errorMessage |
| OPEN_* | 点击"取消"/Esc/遮罩 | CLOSED | 清空表单 |

---

## 删除确认状态机

```text
    ┌──────────┐
    │   IDLE   │
    └────┬─────┘
         │ 点击"删除"
         ▼
    ┌──────────────────┐
    │  CONFIRM_DIALOG  │
    │  二次确认弹窗      │
    └──────┬───────────┘
           │
     ┌─────┼──────┐
     │ 确认       │ 取消
     ▼            ▼
┌──────────┐  ┌──────────┐
│ DELETING │  │   IDLE   │
│ loading  │  └──────────┘
└────┬─────┘
     │
  ┌──┼──────┐
  │ 成功    │ 失败（有子节点等）
  ▼         ▼
IDLE     IDLE + 展示错误提示
(刷新列表)
```

---

## 启停切换状态机

```text
    ┌──────────┐
    │   IDLE   │
    └────┬─────┘
         │ 点击 Switch
         ▼
    ┌──────────────┐
    │   TOGGLING   │
    │  switch 禁用  │
    └──────┬───────┘
           │
     ┌─────┼──────┐
     │ 成功       │ 失败
     ▼            ▼
   IDLE        IDLE
  (刷新列表)   (回滚 switch 值 + 错误提示)
```

---

## 搜索状态机

```text
    ┌──────────────────┐
    │  SEARCH_IDLE     │
    │  keyword = ''    │
    └───────┬──────────┘
            │ 用户输入 keyword + 回车/点击查询
            ▼
    ┌──────────────────┐
    │  SEARCHING       │
    │  列表 loading    │
    └───────┬──────────┘
            │
      ┌─────┼──────┐
      │ 成功       │ 失败
      ▼            ▼
┌───────────┐  ┌──────────────────┐
│SEARCH_DONE│  │  SEARCH_ERROR    │
│有结果/无结果│  │  提示搜索失败     │
└───────┬───┘  └───────┬──────────┘
        │              │
        │ 点击"重置"     │ 点击"重置"/"重试"
        ▼              ▼
  SEARCH_IDLE      SEARCH_IDLE / SEARCHING
  清空 keyword
  重新加载全量树
```

---

## 组件状态模型（Vue Composition API）

```typescript
// AccountSubjectView 状态模型
interface PageState {
  // 页面级
  status: 'init' | 'loading' | 'ready' | 'error'
  errorMessage: string
  
  // 列表数据
  treeData: AccountSubjectVO[]
  expandedKeys: string[]
  
  // 搜索
  keyword: string
  
  // 弹窗
  dialog: {
    visible: boolean
    mode: 'create' | 'createChild' | 'edit'
    submitting: boolean
    form: AccountSubjectForm
    errors: Record<string, string>   // 后端返回的字段级错误
  }
  
  // 删除
  deleting: {
    id: number | null
    confirming: boolean
    loading: boolean
  }
  
  // 启停
  toggling: {
    id: number | null
    loading: boolean
  }
}

interface AccountSubjectForm {
  code: string
  name: string
  parentId: number | null
  type: string
  debitCredit: string
  enabled: boolean
  sort: number
  remark: string
}

interface AccountSubjectVO {
  id: number
  code: string
  name: string
  parentId: number | null
  level: number
  type: string
  typeName: string        // 字典翻译
  debitCredit: string
  debitCreditName: string // 字典翻译
  enabled: boolean
  sort: number
  remark: string
  children: AccountSubjectVO[]
}
```

---

## 数据流

### 1. 加载全量树

```text
onMounted
  │
  ▼
status = 'loading'
  │
  ▼
GET /api/account-subject/tree
  │
  ├─ 200 OK ──────────► treeData = response.data
  │                      expandedKeys = 根节点 ids
  │                      status = 'ready'
  │
  └─ 非 200 ──────────► errorMessage = response.message
                         status = 'error'
```

### 2. 搜索

```text
用户输入 keyword + 回车
  │
  ▼
status = 'loading'
  │
  ▼
GET /api/account-subject/tree?keyword=xxx
  │
  ├─ 200 OK ──────────► treeData = response.data (含匹配节点的祖先链)
  │                      expandedKeys = 所有返回节点 ids
  │                      status = 'ready'
  │
  └─ 非 200 ──────────► errorMessage = response.message
                         status = 'error'
```

### 3. 新增/编辑

```text
点击保存
  │
  ▼
前端表单校验（el-form rules）
  │
  ├─ 校验失败 ────────► 高亮字段，阻止提交
  │
  └─ 校验通过
       │
       ▼
     dialog.submitting = true
       │
       ▼
     POST 或 PUT /api/account-subject[/id]
       │
       ├─ 200 OK ─────► ElMessage.success('保存成功')
       │                 dialog.visible = false
       │                 触发 fetchTree() 刷新
       │
       └─ 业务错误 ────► dialog.submitting = false
                         ElMessage.error(response.message)
                         若有字段级错误→高亮对应字段
```

### 4. 删除

```text
点击删除
  │
  ▼
ElMessageBox.confirm('确认删除?')
  │
  ├─ 取消 ────────────► 无操作
  │
  └─ 确认
       │
       ▼
     deleting.loading = true
       │
       ▼
     DELETE /api/account-subject/{id}
       │
       ├─ 200 OK ─────► ElMessage.success('删除成功')
       │                 触发 fetchTree() 刷新
       │
       └─ 业务错误 ────► ElMessage.error('该科目下存在子科目，不能删除')
                         deleting.loading = false
```

### 5. 启停

```text
点击 Switch
  │
  ▼
toggling.loading = true
switch 组件 disabled
  │
  ▼
PATCH /api/account-subject/{id}/status
  │
  ├─ 200 OK ─────────► ElMessage.success('状态已更新')
  │                     触发 fetchTree() 刷新
  │
  └─ 失败 ────────────► 回滚 switch 值
                         ElMessage.error(response.message)
                         toggling.loading = false
```

---

## 交互规则

### 查询

- 输入关键字后按回车或点击"查询"触发搜索
- 搜索结果保持树结构：匹配到子节点时自动带出祖先节点
- 搜索时列表展示 loading 骨架
- 无结果时展示 el-empty

### 新增

- 点击"新增科目"打开空表单弹窗
- 点击行操作列"新增下级"打开弹窗，自动填充 parentId 并禁用父级选择
- code 输入后失焦时可选做前端预检唯一性（或交给后端统一校验）

### 编辑

- 点击行操作列"编辑"，加载当前行数据到弹窗
- `code` 字段只读（disabled），不允许修改
- 修改 parentId 时，树形选择器需排除自身及子孙节点

### 删除

- 二次确认弹窗，确认文案：`确认删除科目「{code} - {name}」？`
- 后端返回"存在子科目"时前端展示错误提示

### 启停

- 表格行内 Switch 组件直接切换
- 切换时 Switch 置为 disabled 防止重复点击
- 失败时自动回滚 Switch 视觉状态

---

## 前端表单校验规则

```typescript
const rules = {
  code: [
    { required: true, message: '请输入科目编码', trigger: 'blur' },
    { min: 1, max: 50, message: '长度 1-50 个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '只允许字母和数字', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入科目名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度 1-100 个字符', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择科目类型', trigger: 'change' },
  ],
  debitCredit: [
    { required: true, message: '请选择借贷方向', trigger: 'change' },
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' },
    { type: 'number', min: 1, max: 9999, message: '范围 1-9999', trigger: 'blur' },
  ],
  remark: [
    { max: 200, message: '最多 200 个字符', trigger: 'blur' },
  ],
}
```

---

## 错误态处理

| 场景 | 表现 | 恢复方式 |
|------|------|---------|
| 初始加载失败 | 页面展示错误占位 + "重新加载"按钮 | 点击重新加载 |
| 搜索失败 | ElMessage.error + 保留上一次数据 | 重新搜索或重置 |
| 保存失败（参数错误） | 弹窗内字段高亮 + 提示信息 | 修正后重新提交 |
| 保存失败（业务冲突） | ElMessage.error(后端 message) | 修正后重新提交 |
| 删除失败（有子节点） | ElMessage.error("该科目下存在子科目") | 先删除子科目 |
| 启停失败 | Switch 回滚 + ElMessage.error | 重试 |
| 网络超时 | ElMessage.error("网络超时") | 重试 |

---

## 页面约束

1. 不在前端计算父子合法性，树形选择器排除项由后端数据驱动（子孙节点列表从 treeData 本地计算）
2. 不允许先编码后补 DSL——所有页面实现前必须先完成本文档
3. 不允许把列表页面写成纯静态页面——必须有完整的加载/错误/空状态处理
4. 弹窗关闭后必须清空表单和校验状态
5. 所有异步操作期间对应的触发按钮必须置为 loading/disabled，防止重复提交

---

## 编码前置条件

1. 后端领域设计文档完成 ✅
2. 数据库建表和字典脚本完成（待执行）
3. 页面 DSL 完成 ✅
4. 后端接口实现完成后，再开始 Vue 页面编码
