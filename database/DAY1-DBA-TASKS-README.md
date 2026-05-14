[归档说明] 本文件为历史记录，保留旧的 jeecg / Ant Design Vue / Java 17 方案描述。当前主线已切换为 Spring Boot + JDK 21 + Maven + MyBatis-Plus + Lombok + Hutool，前端为 Vue 3 + Vue Router + Element Plus。

# Day 1 上午 DBA任务执行指南

## 任务概述

作为DBA，需要完成以下任务：
1. 备份现有数据库
2. 执行database/schema-v4-reviewed.sql创建数据库表
3. 验证表结构
4. 导入初始化数据（会计科目）
5. 创建索引（fact_event表索引、voucher表索引、audit_log分区表）

## 前置条件

### 1. 启动MySQL容器

```bash
cd /home/hlw/work/code/profit-decision-system
docker compose up -d profit-mysql
```

等待MySQL容器启动完成（约10-30秒）：

```bash
docker compose logs -f profit-mysql
```

看到 "ready for connections" 表示启动成功。

### 2. 安装MySQL客户端（如果未安装）

Ubuntu/Debian:
```bash
sudo apt-get update
sudo apt-get install mysql-client
```

CentOS/RHEL:
```bash
sudo yum install mysql
```

### 3. 验证数据库连接

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 -e "SELECT VERSION();"
```

## 执行方式

### 方式一：自动化脚本（推荐）

使用提供的自动化脚本一键完成所有任务：

```bash
cd /home/hlw/work/code/profit-decision-system/database
chmod +x day1-dba-tasks.sh
./day1-dba-tasks.sh
```

脚本会自动完成：
- 检查数据库连接
- 备份现有数据库到 backups/ 目录
- 创建19张表
- 验证表结构
- 导入初始化数据
- 创建性能优化索引
- 生成执行报告

### 方式二：手动执行

如果需要手动执行每个步骤：

#### 步骤1: 备份现有数据库

```bash
cd /home/hlw/work/code/profit-decision-system/database
mkdir -p backups
mysqldump -h127.0.0.1 -P3306 -uroot -p123456 \
    --databases jeecg-boot \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    > backups/backup_$(date +%Y%m%d_%H%M%S).sql
```

#### 步骤2: 创建数据库表

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot < schema-v4-reviewed.sql
```

#### 步骤3: 验证表结构

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot -e "SHOW TABLES;"
```

应该看到19张表：
1. fact_event - 事实事件表
2. account_subject - 会计科目表
3. voucher - 记账凭证表
4. voucher_entry - 凭证分录表
5. attribution_rule - 归因规则表
6. attribution_result - 归因结果表
7. org_unit - 组织单元表
8. user - 用户表
9. counterparty - 客户供应商表
10. receivable - 应收应付表
11. cash_flow - 现金流表
12. profit_snapshot - 利润快照表
13. goal - 目标表
14. period_closing - 期间结账表
15. audit_log - 审计日志表
16. attachment - 附件表
17. notification - 通知表
18. sys_dict - 系统字典表
19. sys_dict_item - 字典项表

#### 步骤4: 导入初始化数据

```bash
# 导入组织、用户、归因规则等基础数据
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot < init-data.sql

# 导入字典数据
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot < dict-data.sql
```

#### 步骤5: 创建性能优化索引

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot < create-indexes.sql
```

## 验证结果

### 1. 检查表数量

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot -e "
SELECT COUNT(*) AS table_count 
FROM information_schema.tables 
WHERE table_schema='jeecg-boot';"
```

预期结果：至少19张表

### 2. 检查初始化数据

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot -e "
SELECT 'org_unit' AS table_name, COUNT(*) AS record_count FROM org_unit
UNION ALL
SELECT 'user', COUNT(*) FROM user
UNION ALL
SELECT 'account_subject', COUNT(*) FROM account_subject
UNION ALL
SELECT 'attribution_rule', COUNT(*) FROM attribution_rule
UNION ALL
SELECT 'goal', COUNT(*) FROM goal;"
```

预期结果：
- org_unit: 6条记录
- user: 2条记录
- account_subject: 根据会计科目数量
- attribution_rule: 2条记录
- goal: 2条记录

### 3. 检查索引创建

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot -e "
SELECT 
    table_name,
    COUNT(DISTINCT index_name) AS index_count
FROM information_schema.statistics 
WHERE table_schema = 'jeecg-boot'
  AND index_name != 'PRIMARY'
GROUP BY table_name
ORDER BY table_name;"
```

### 4. 检查fact_event表结构

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot -e "
DESCRIBE fact_event;"
```

### 5. 检查voucher表结构

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot -e "
DESCRIBE voucher;"
```

## 关键索引说明

### fact_event表索引
- idx_fact_event_profit_calc: 用于利润计算（accounting_date, org_unit_id, type, status）
- idx_fact_event_cash_flow: 用于现金流分析（cash_date, payment_method）
- idx_fact_event_amortization: 用于分摊计算（amortization_start, amortization_end）
- idx_fact_event_business_date: 用于业务日期查询
- idx_fact_event_invoice: 用于发票管理

### voucher表索引
- idx_voucher_period_status: 用于凭证查询（period, status）
- idx_voucher_created: 用于审计（created_at, created_by）
- idx_voucher_date: 用于凭证日期查询

### audit_log表索引
- idx_audit_log_entity: 用于审计查询（entity_type, entity_id, operation）
- idx_audit_log_time: 用于时间范围查询
- idx_audit_log_user: 用于用户操作追踪
- idx_audit_log_ip: 用于IP追踪

## 故障排查

### 问题1: 无法连接到数据库

检查MySQL容器是否运行：
```bash
docker ps | grep mysql
```

如果未运行，启动容器：
```bash
cd /home/hlw/work/code/profit-decision-system
docker compose up -d profit-mysql
```

### 问题2: 表已存在错误

如果需要重新创建表，先删除现有表：
```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 jeecg-boot -e "
DROP TABLE IF EXISTS 
    fact_event, account_subject, voucher, voucher_entry,
    attribution_rule, attribution_result, org_unit, user,
    counterparty, receivable, cash_flow, profit_snapshot,
    goal, period_closing, audit_log, attachment,
    notification, sys_dict, sys_dict_item;"
```

然后重新执行schema-v4-reviewed.sql。

### 问题3: 索引已存在错误

索引创建脚本会忽略重复索引错误，可以安全重复执行。

### 问题4: Docker权限问题

如果遇到Docker权限问题：
```bash
# 将用户添加到docker组
sudo usermod -aG docker $USER

# 重新登录或执行
newgrp docker
```

## 文件说明

- schema-v4-reviewed.sql: 数据库表结构定义（19张表）
- init-data.sql: 初始化数据（组织、用户、规则、目标）
- dict-data.sql: 字典数据（系统字典项）
- create-indexes.sql: 性能优化索引创建脚本
- day1-dba-tasks.sh: 自动化执行脚本
- backups/: 数据库备份目录

## 执行时间估算

- 备份数据库: 5-30秒（取决于数据量）
- 创建表结构: 10-20秒
- 导入初始化数据: 5-10秒
- 创建索引: 10-30秒

总计: 约1-2分钟

## 注意事项

1. 执行前确保MySQL容器正在运行
2. 备份文件保存在 backups/ 目录，建议定期清理旧备份
3. 索引创建会占用一定时间，大表可能需要更长时间
4. 如果数据库中已有数据，建议先备份再执行
5. 生产环境执行前请在测试环境验证

## 下一步

完成DBA任务后，可以进行：
1. 后端服务启动测试
2. API接口测试
3. 前端集成测试
4. 数据导入测试

## 联系支持

如有问题，请联系：
- 技术负责人
- DBA团队
- 项目经理
