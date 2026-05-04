# 飞牛经营系统 - 开发规范

**项目名称：** 飞牛经营 - 一体化经营管理系统  
**技术栈：** jeecg-boot 全栈脚手架  
**开发周期：** 4周（2026-05-06 至 2026-05-31）  
**团队配置：** 架构师1人 + 高级开发1人 + 初级开发2人

---

## 1. 技术栈规范

### 1.1 后端技术栈

```
框架：jeecg-boot 3.5.3
语言：Java 17
构建工具：Maven 3.8+
数据库：MySQL 8.0
ORM：MyBatis-Plus 3.5.3
缓存：Redis 6.0
权限：Shiro 1.10
```

### 1.2 前端技术栈

```
框架：Vue 3.3
UI库：Ant Design Vue 4.0
构建工具：Vite 4.0
语言：TypeScript 5.0
状态管理：Pinia
路由：Vue Router 4
HTTP：Axios
图表：ECharts 5.4
```

---

## 2. 项目结构规范

### 2.1 后端项目结构

```
jeecg-boot/
├── jeecg-boot-base/                    # 基础模块（不修改）
├── jeecg-boot-module-system/           # 系统模块（不修改）
├── jeecg-boot-module-profit/           # 利润系统模块（我们的）
│   ├── src/main/java/com/profit/
│   │   ├── controller/                # 控制器
│   │   │   ├── FactController.java
│   │   │   ├── ReceivableController.java
│   │   │   ├── PayableController.java
│   │   │   ├── PeriodClosingController.java
│   │   │   ├── CounterpartyController.java
│   │   │   ├── ContractController.java
│   │   │   └── ProjectController.java
│   │   ├── service/                   # 服务层
│   │   │   ├── IFactService.java
│   │   │   ├── impl/
│   │   │   │   └── FactServiceImpl.java
│   │   │   └── ...
│   │   ├── entity/                    # 实体类
│   │   │   ├── FactEvent.java
│   │   │   ├── Receivable.java
│   │   │   └── ...
│   │   ├── mapper/                    # Mapper接口
│   │   │   ├── FactMapper.java
│   │   │   └── ...
│   │   ├── vo/                        # 视图对象
│   │   ├── dto/                       # 数据传输对象
│   │   └── enums/                     # 枚举类
│   └── src/main/resources/
│       └── mapper/                    # MyBatis XML
│           ├── FactMapper.xml
│           └── ...
└── jeecg-boot-starter/                 # 启动类
```

### 2.2 前端项目结构

```
ant-design-vue-jeecg/
├── src/
│   ├── api/                           # API接口
│   │   ├── profit/                   # 利润系统API
│   │   │   ├── fact.ts
│   │   │   ├── receivable.ts
│   │   │   ├── payable.ts
│   │   │   ├── closing.ts
│   │   │   ├── counterparty.ts
│   │   │   ├── contract.ts
│   │   │   └── project.ts
│   │   └── ...
│   ├── views/                         # 页面
│   │   ├── profit/                   # 利润系统页面
│   │   │   ├── dashboard/            # 工作台
│   │   │   │   └── Index.vue
│   │   │   ├── finance/              # 财务管理
│   │   │   │   ├── fact/             # 收支管理
│   │   │   │   │   ├── List.vue
│   │   │   │   │   ├── Form.vue
│   │   │   │   │   └── Detail.vue
│   │   │   │   ├── receivable/       # 应收管理
│   │   │   │   ├── payable/          # 应付管理
│   │   │   │   └── closing/          # 期间结账
│   │   │   ├── business/             # 业务管理
│   │   │   │   ├── counterparty/     # 客户/供应商
│   │   │   │   ├── contract/         # 合同管理
│   │   │   │   └── project/          # 项目管理
│   │   │   ├── analysis/             # 经营分析（占位符）
│   │   │   │   ├── cost/
│   │   │   │   ├── customer/
│   │   │   │   └── project/
│   │   │   ├── inventory/            # 库存管理（占位符）
│   │   │   ├── decision/             # 智能决策（占位符）
│   │   │   └── report/               # 报表中心
│   │   │       ├── income/
│   │   │       ├── receivable/
│   │   │       └── balance/
│   │   └── ...
│   ├── components/                    # 通用组件
│   │   ├── Placeholder/              # 占位符组件
│   │   │   └── Index.vue
│   │   └── ...
│   ├── store/                         # 状态管理
│   │   └── modules/
│   │       └── profit.ts
│   └── router/                        # 路由
│       └── modules/
│           └── profit.ts
└── ...
```

---

## 3. 命名规范

### 3.1 Java 命名规范

**类名：** 大驼峰（PascalCase）
```java
FactEvent.java
ReceivableService.java
PeriodClosingController.java
```

**方法名：** 小驼峰（camelCase）
```java
createFact()
getReceivableList()
executePeriodClosing()
```

**常量：** 全大写 + 下划线
```java
public static final String FACT_TYPE_INCOME = "income";
public static final int MAX_RETRY_COUNT = 3;
```

**包名：** 全小写
```java
com.profit.controller
com.profit.service.impl
```

### 3.2 数据库命名规范

**表名：** 小写 + 下划线
```sql
fact_event
receivable
period_closing
```

**字段名：** 小写 + 下划线
```sql
business_date
accounting_date
counterparty_id
```

**索引名：** idx_ + 字段名
```sql
idx_business_date
idx_org_unit_id
```

**唯一索引：** uk_ + 字段名
```sql
uk_period_org
```

### 3.3 前端命名规范

**文件名：** 大驼峰（PascalCase）
```
List.vue
Form.vue
Detail.vue
```

**组件名：** 大驼峰
```vue
<FactList />
<ReceivableForm />
```

**变量名：** 小驼峰
```typescript
const factList = ref([]);
const queryParam = reactive({});
```

**常量：** 全大写 + 下划线
```typescript
const FACT_TYPE_INCOME = 'income';
const MAX_PAGE_SIZE = 100;
```

---

## 4. 代码生成规范

### 4.1 代码生成器配置

**生成步骤：**
```
1. 登录 jeecg 后台（http://localhost:3000）
2. 进入"在线开发" → "代码生成"
3. 点击"导入表"，选择数据库表
4. 配置生成选项：
   - 生成类型：单表/树表/主子表
   - 包名：com.profit
   - 模块名：profit
   - 作者：profit-team
   - 表单布局：一行一列/一行两列
5. 点击"生成代码"
6. 下载 zip 包
7. 解压到项目对应目录
```

**生成配置模板：**
```json
{
  "tableName": "fact_event",
  "businessName": "fact",
  "className": "FactEvent",
  "packageName": "com.profit",
  "moduleName": "profit",
  "author": "profit-team",
  "genType": "0",
  "tplCategory": "crud",
  "formLayout": "one"
}
```

### 4.2 生成后必须修改的地方

**后端：**
```java
// 1. 添加业务逻辑（Service层）
@Override
@Transactional
public void createIncomeFact(FactEvent fact) {
    // 生成的代码
    this.save(fact);
    
    // 手写：创建应收账款
    Receivable receivable = new Receivable();
    receivable.setFactId(fact.getId());
    receivable.setTotalAmount(fact.getAmount());
    receivableService.save(receivable);
}

// 2. 添加数据校验（Controller层）
@PostMapping("/create")
public Result<?> create(@RequestBody @Valid FactEvent fact) {
    // 手写：业务校验
    if (fact.getType().equals("income") && fact.getCounterpartyType() == null) {
        return Result.error("收入必须关联客户");
    }
    
    factService.createIncomeFact(fact);
    return Result.OK("创建成功");
}
```

**前端：**
```vue
<!-- 1. 调整表单布局 -->
<a-form-item label="业务日期" name="businessDate">
  <a-date-picker v-model:value="form.businessDate" style="width: 100%" />
</a-form-item>

<!-- 2. 添加字段联动 -->
<a-form-item v-if="form.type === 'cost'" label="成本类别" name="costCategory">
  <a-select v-model:value="form.costCategory">
    <a-select-option value="fixed">固定成本</a-select-option>
    <a-select-option value="variable">变动成本</a-select-option>
  </a-select>
</a-form-item>

<!-- 3. 添加自定义操作 -->
<template #action="{ record }">
  <a @click="handleEdit(record)">编辑</a>
  <a-divider type="vertical" />
  <a @click="handleReverse(record)">冲正</a>
  <a-divider type="vertical" />
  <a-popconfirm title="确定删除?" @confirm="handleDelete(record)">
    <a>删除</a>
  </a-popconfirm>
</template>
```

---

## 5. 接口规范

### 5.1 RESTful API 规范

**URL 规范：**
```
GET    /api/v1/facts          # 查询列表
GET    /api/v1/facts/{id}     # 查询详情
POST   /api/v1/facts          # 新增
PUT    /api/v1/facts/{id}     # 修改
DELETE /api/v1/facts/{id}     # 删除
POST   /api/v1/facts/export   # 导出
POST   /api/v1/facts/import   # 导入
```

**请求参数：**
```json
// 查询列表
{
  "pageNo": 1,
  "pageSize": 10,
  "type": "income",
  "startDate": "2026-05-01",
  "endDate": "2026-05-31"
}

// 新增/修改
{
  "businessDate": "2026-05-10",
  "accountingDate": "2026-05-15",
  "type": "income",
  "amount": 50000.00,
  "counterpartyId": 1,
  "orgUnitId": 2
}
```

**响应格式：**
```json
// 成功
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "id": 123
  },
  "timestamp": 1714550400000
}

// 失败
{
  "success": false,
  "message": "收入必须关联客户",
  "code": 500,
  "result": null,
  "timestamp": 1714550400000
}

// 分页列表
{
  "success": true,
  "message": "查询成功",
  "code": 200,
  "result": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 5.2 错误码规范

```
200: 操作成功
400: 参数错误
401: 未登录
403: 无权限
404: 资源不存在
500: 系统错误

业务错误码（自定义）：
1001: 收入必须关联客户
1002: 成本必须指定类别
1003: 已结账期间不可修改
2001: 应收账款不存在
2002: 应收账款已结清
3001: 期间未开放
3002: 期间已结账
```

---

## 6. 数据库规范

### 6.1 表设计规范

**必须字段：**
```sql
CREATE TABLE xxx (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by VARCHAR(50) COMMENT '创建人',
  updated_by VARCHAR(50) COMMENT '更新人',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0未删除 1已删除）'
);
```

**索引规范：**
```sql
-- 单列索引
INDEX idx_business_date (business_date)

-- 联合索引（最常用的查询条件放前面）
INDEX idx_type_date (type, business_date)

-- 唯一索引
UNIQUE KEY uk_period_org (period, org_unit_id)
```

**注释规范：**
```sql
CREATE TABLE fact_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  business_date DATE NOT NULL COMMENT '业务发生日期',
  type VARCHAR(20) NOT NULL COMMENT '类型: income/cost/behavior',
  amount DECIMAL(15,2) NOT NULL COMMENT '金额'
) COMMENT='事实事件表';
```

### 6.2 SQL 编写规范

**查询规范：**
```sql
-- ✅ 推荐：使用具体字段
SELECT id, business_date, type, amount FROM fact_event;

-- ❌ 不推荐：使用 *
SELECT * FROM fact_event;

-- ✅ 推荐：使用索引字段
SELECT * FROM fact_event WHERE type = 'income' AND business_date >= '2026-05-01';

-- ❌ 不推荐：不使用索引
SELECT * FROM fact_event WHERE DATE_FORMAT(business_date, '%Y-%m') = '2026-05';
```

**分页规范：**
```sql
-- ✅ 推荐：使用 LIMIT OFFSET
SELECT * FROM fact_event 
WHERE type = 'income' 
ORDER BY business_date DESC 
LIMIT 10 OFFSET 0;

-- ❌ 不推荐：深分页
SELECT * FROM fact_event 
ORDER BY business_date DESC 
LIMIT 10 OFFSET 10000;  -- 性能差
```

---

## 7. Git 规范

### 7.1 分支规范

```
master          # 主分支（生产环境）
develop         # 开发分支
feature/xxx     # 功能分支
bugfix/xxx      # Bug修复分支
hotfix/xxx      # 紧急修复分支
```

**分支命名：**
```
feature/fact-management      # 收支管理功能
feature/receivable-payment   # 应收收款功能
bugfix/closing-status-error  # 结账状态错误修复
```

### 7.2 提交规范

**Commit Message 格式：**
```
<type>(<scope>): <subject>

type:
- feat: 新功能
- fix: Bug修复
- docs: 文档更新
- style: 代码格式（不影响功能）
- refactor: 重构
- test: 测试
- chore: 构建/工具

scope: 影响范围（模块名）
subject: 简短描述
```

**示例：**
```
feat(fact): 新增收支管理功能
fix(receivable): 修复应收金额计算错误
docs(readme): 更新部署文档
refactor(closing): 重构期间结账逻辑
```

### 7.3 代码审查规范

**提交 PR 前：**
```
1. 自测通过
2. 代码格式化
3. 删除调试代码
4. 更新相关文档
```

**代码审查要点：**
```
1. 功能是否正确
2. 代码是否规范
3. 是否有性能问题
4. 是否有安全问题
5. 注释是否清晰
```

---

## 8. 测试规范

### 8.1 单元测试

**测试覆盖率要求：**
```
Service 层：80% 以上
复杂业务逻辑：100%
```

**测试示例：**
```java
@SpringBootTest
public class FactServiceTest {
    
    @Autowired
    private IFactService factService;
    
    @Test
    public void testCreateIncomeFact() {
        // 准备数据
        FactEvent fact = new FactEvent();
        fact.setType("income");
        fact.setAmount(new BigDecimal("5000"));
        fact.setCounterpartyId(1L);
        
        // 执行
        factService.createIncomeFact(fact);
        
        // 验证
        assertNotNull(fact.getId());
        
        // 验证应收账款已创建
        Receivable receivable = receivableService.getByFactId(fact.getId());
        assertNotNull(receivable);
        assertEquals(fact.getAmount(), receivable.getTotalAmount());
    }
}
```

### 8.2 接口测试

**使用 Postman/Apifox：**
```
1. 创建测试集合
2. 编写测试用例
3. 设置环境变量
4. 执行自动化测试
```

---

## 9. 部署规范

### 9.1 环境配置

**开发环境：**
```yaml
# application-dev.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/profit_dev
    username: root
    password: 123456
```

**生产环境：**
```yaml
# application-prod.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/profit_prod
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 9.2 Docker 部署

**Dockerfile：**
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/profit-system.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml：**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: profit_prod
    ports:
      - "3306:3306"
  
  redis:
    image: redis:6.0
    ports:
      - "6379:6379"
  
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
```

---

## 10. 文档规范

### 10.1 代码注释

**类注释：**
```java
/**
 * 事实事件服务
 * 
 * @author profit-team
 * @date 2026-05-06
 */
@Service
public class FactServiceImpl implements IFactService {
}
```

**方法注释：**
```java
/**
 * 创建收入事实
 * 
 * @param fact 事实对象
 * @return 事实ID
 * @throws BizException 业务异常
 */
@Override
@Transactional
public Long createIncomeFact(FactEvent fact) {
    // ...
}
```

### 10.2 接口文档

**使用 Swagger：**
```java
@Api(tags = "收支管理")
@RestController
@RequestMapping("/api/v1/facts")
public class FactController {
    
    @ApiOperation("创建收支记录")
    @PostMapping
    public Result<?> create(@RequestBody @ApiParam("收支对象") FactEvent fact) {
        // ...
    }
}
```

---

## 11. 性能规范

### 11.1 数据库优化

```
1. 使用索引（查询条件字段）
2. 避免 SELECT *
3. 分页查询（LIMIT）
4. 批量操作（批量插入/更新）
5. 避免 N+1 查询
```

### 11.2 缓存策略

```java
// 查询缓存
@Cacheable(value = "fact", key = "#id")
public FactEvent getById(Long id) {
    return factMapper.selectById(id);
}

// 更新缓存
@CachePut(value = "fact", key = "#fact.id")
public FactEvent update(FactEvent fact) {
    factMapper.updateById(fact);
    return fact;
}

// 删除缓存
@CacheEvict(value = "fact", key = "#id")
public void delete(Long id) {
    factMapper.deleteById(id);
}
```

---

## 12. 安全规范

### 12.1 SQL 注入防护

```java
// ✅ 推荐：使用参数化查询
factMapper.selectList(new QueryWrapper<FactEvent>()
    .eq("type", type)
    .ge("business_date", startDate));

// ❌ 不推荐：拼接 SQL
String sql = "SELECT * FROM fact_event WHERE type = '" + type + "'";
```

### 12.2 XSS 防护

```java
// 前端输入过滤
import org.apache.commons.text.StringEscapeUtils;

String safeInput = StringEscapeUtils.escapeHtml4(userInput);
```

### 12.3 权限控制

```java
// 使用 Shiro 注解
@RequiresPermissions("fact:create")
@PostMapping
public Result<?> create(@RequestBody FactEvent fact) {
    // ...
}
```

---

## 附录：常用命令

### Maven 命令
```bash
mvn clean install          # 编译打包
mvn spring-boot:run        # 启动项目
mvn test                   # 运行测试
```

### Git 命令
```bash
git checkout -b feature/xxx    # 创建功能分支
git add .                      # 添加所有修改
git commit -m "feat: xxx"      # 提交
git push origin feature/xxx    # 推送
```

### Docker 命令
```bash
docker-compose up -d       # 启动容器
docker-compose down        # 停止容器
docker logs -f app         # 查看日志
```
