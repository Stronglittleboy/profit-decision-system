# 代码生成器实战指南 - 生成第一个模块

## 🎯 目标

使用 jeecg-boot 代码生成器生成 **客户/供应商管理模块**（profit_counterparty 表）

---

## 📋 准备工作

### 1. 确认后端运行
- ✅ 后端地址：http://localhost:8080/jeecg-boot
- ✅ 登录账号：admin / 123456

### 2. 确认表已创建
```sql
-- 客户/供应商表
profit_counterparty
```

---

## 🚀 操作步骤

### 第一步：登录系统

1. 打开浏览器，访问：http://localhost:8080/jeecg-boot
2. 输入账号：`admin`
3. 输入密码：`123456`
4. 点击 **登录**

---

### 第二步：进入代码生成器

1. 登录成功后，点击左侧菜单：**系统管理**
2. 展开子菜单，点击：**开发工具**
3. 点击：**代码生成器**

---

### 第三步：同步数据库表

1. 在代码生成器页面，点击右上角 **同步数据库** 按钮
2. 等待同步完成（约 3-5 秒）
3. 在表列表中搜索：`profit_counterparty`
4. 确认表已出现在列表中

---

### 第四步：配置生成参数

点击 `profit_counterparty` 表后面的 **生成** 按钮，进入配置页面：

#### 基础信息
- **表名**：profit_counterparty（自动填充）
- **表描述**：客户/供应商管理
- **类名**：ProfitCounterparty（自动生成）
- **功能名称**：客户/供应商管理

#### 生成配置
- **生成方式**：单表（默认）
- **生成模板**：vue3（选择 Vue3 模板）
- **生成包路径**：org.jeecg.modules.profit
- **生成代码路径**：默认

#### 字段配置（重要！）

| 字段名 | 显示名称 | 字段类型 | 查询 | 列表显示 | 表单显示 | 必填 |
|--------|----------|----------|------|----------|----------|------|
| id | ID | 主键 | - | ❌ | ❌ | - |
| name | 名称 | 字符串 | ✅ 模糊 | ✅ | ✅ | ✅ |
| code | 编码 | 字符串 | ✅ 精确 | ✅ | ✅ | ✅ |
| type | 类型 | 字典 | ✅ 下拉 | ✅ | ✅ | ✅ |
| contact_person | 联系人 | 字符串 | ❌ | ✅ | ✅ | ❌ |
| contact_phone | 联系电话 | 字符串 | ❌ | ✅ | ✅ | ❌ |
| contact_email | 联系邮箱 | 字符串 | ❌ | ✅ | ✅ | ❌ |
| address | 地址 | 字符串 | ❌ | ❌ | ✅ | ❌ |
| bank_name | 开户银行 | 字符串 | ❌ | ❌ | ✅ | ❌ |
| bank_account | 银行账号 | 字符串 | ❌ | ❌ | ✅ | ❌ |
| tax_number | 税号 | 字符串 | ❌ | ❌ | ✅ | ❌ |
| status | 状态 | 字典 | ✅ 下拉 | ✅ | ✅ | ✅ |
| remark | 备注 | 文本域 | ❌ | ❌ | ✅ | ❌ |
| create_by | 创建人 | 系统字段 | ❌ | ❌ | ❌ | - |
| create_time | 创建时间 | 系统字段 | ❌ | ✅ | ❌ | - |
| update_by | 更新人 | 系统字段 | ❌ | ❌ | ❌ | - |
| update_time | 更新时间 | 系统字段 | ❌ | ❌ | ❌ | - |

#### 字典配置（重要！）

**type 字段（类型）：**
- 字典编码：`counterparty_type`
- 字典项：
  - customer：客户
  - supplier：供应商
  - both：客户+供应商

**status 字段（状态）：**
- 字典编码：`common_status`
- 字典项：
  - 1：启用
  - 0：禁用

---

### 第五步：生成代码

1. 检查所有配置是否正确
2. 点击页面底部 **生成代码** 按钮
3. 等待生成完成（约 2-3 秒）
4. 下载生成的代码压缩包（jeecg-boot-module-profit.zip）

---

### 第六步：解压并部署代码

#### 后端代码

1. 解压下载的压缩包
2. 找到后端代码目录：`jeecg-boot-module-profit/`
3. 复制到项目目录：
   ```bash
   cp -r jeecg-boot-module-profit/* \
     /vol3/1000/private/workProject/profit-decision-system/backend/jeecg-boot/jeecg-boot/jeecg-boot-module/jeecg-boot-module-profit/
   ```

#### 前端代码

1. 找到前端代码目录：`jeecgboot-vue3/src/views/profit/`
2. 复制到前端项目：
   ```bash
   cp -r jeecgboot-vue3/src/views/profit/* \
     /vol3/1000/private/workProject/profit-decision-system/frontend/jeecgboot-vue3/src/views/profit/
   ```

---

### 第七步：重启后端

```bash
# 停止当前后端（Ctrl+C）
# 重新启动
cd /vol3/1000/private/workProject/profit-decision-system
./start-backend.sh
```

---

### 第八步：启动前端

```bash
cd /vol3/1000/private/workProject/profit-decision-system
./start-frontend.sh
```

---

### 第九步：验证功能

1. 访问前端：http://localhost:3100
2. 登录系统（admin/123456）
3. 在左侧菜单中找到：**利润管理 → 客户/供应商管理**
4. 测试功能：
   - ✅ 新增客户
   - ✅ 编辑客户
   - ✅ 删除客户
   - ✅ 查询客户
   - ✅ 导入导出

---

## 📝 注意事项

### 1. 字典配置
- 确保字典编码与数据库中的字典数据一致
- 字典数据已通过 `database/dict-data.sql` 导入

### 2. 字段类型选择
- **字符串**：普通文本字段
- **文本域**：长文本字段（如备注）
- **字典**：下拉选择字段（需要配置字典编码）
- **日期**：日期选择器
- **数字**：数字输入框

### 3. 查询配置
- **模糊查询**：适用于名称、描述等字段
- **精确查询**：适用于编码、ID 等字段
- **范围查询**：适用于日期、金额等字段

### 4. 列表显示
- 只选择重要字段显示在列表中
- 避免显示过多字段导致列表拥挤

### 5. 表单显示
- 所有需要用户填写的字段都要显示
- 系统字段（create_by、create_time 等）不显示

---

## 🎯 生成后的文件结构

### 后端文件
```
jeecg-boot-module-profit/
├── controller/
│   └── ProfitCounterpartyController.java
├── entity/
│   └── ProfitCounterparty.java
├── mapper/
│   ├── ProfitCounterpartyMapper.java
│   └── xml/
│       └── ProfitCounterpartyMapper.xml
├── service/
│   ├── IProfitCounterpartyService.java
│   └── impl/
│       └── ProfitCounterpartyServiceImpl.java
└── vo/
    └── ProfitCounterpartyVO.java
```

### 前端文件
```
src/views/profit/counterparty/
├── ProfitCounterparty.vue          # 列表页面
├── ProfitCounterpartyModal.vue     # 新增/编辑弹窗
└── modules/
    └── ProfitCounterpartyForm.vue  # 表单组件
```

---

## 🐛 常见问题

### Q1: 生成的代码找不到？
**A**: 检查下载目录，通常在浏览器的默认下载文件夹中。

### Q2: 字典下拉框没有数据？
**A**: 检查字典编码是否正确，确认字典数据已导入。

### Q3: 前端页面空白？
**A**: 检查浏览器控制台是否有报错，确认后端是否正常运行。

### Q4: 后端启动报错？
**A**: 检查生成的代码是否正确复制到项目目录。

---

## 🎊 完成标志

当你看到以下内容时，说明第一个模块生成成功：

1. ✅ 后端代码生成并部署
2. ✅ 前端代码生成并部署
3. ✅ 后端重启成功
4. ✅ 前端启动成功
5. ✅ 菜单中出现"客户/供应商管理"
6. ✅ 可以正常新增、编辑、删除、查询数据

---

**🚀 准备好了吗？现在就去访问 http://localhost:8080/jeecg-boot 开始生成第一个模块吧！**
